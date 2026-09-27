#!/usr/bin/env python3
"""Validate app/src/main/assets/items.json, the catalog CatalogRepository seeds.

Checks the invariants CatalogRepository and CatalogDao depend on:

  * a parseable ``version`` int, which is the only thing that triggers a reseed
  * unique category names, in title case, alphabetically ordered
  * unique item names across the whole catalog, in title case, and
    alphabetically ordered within their category

Item uniqueness is case-insensitive because ``CatalogItemEntity.name`` is
declared ``COLLATE NOCASE`` and ``CatalogDao.autocomplete`` matches with
``LIKE``, so "Olive Oil" and "olive oil" would be the same catalog row as far
as the user is concerned.

Exits 0 when every check passes, 1 otherwise.
"""

from __future__ import annotations

import json
import sys
from collections import defaultdict
from pathlib import Path

ITEMS_JSON = Path(__file__).resolve().parents[2] / "app/src/main/assets/items.json"

STATUS_WIDTH = 6


def fold(text: str) -> str:
    """Case-insensitive alphabetical key.

    Deliberately *not* a plain ``str`` comparison: the catalog mixes cases
    ("Balsamic Vinaigrette" before "BBQ Sauce"), so an ordinal compare reports
    a false out-of-order pair. And deliberately not a
    strip-punctuation key: that would flag "Bread Flour" before "Breadcrumbs",
    which is correct word order. ``casefold`` matches the ordering the catalog
    is actually written in.
    """
    return text.casefold()


def is_title_case(text: str) -> bool:
    return bool(text) and text[0].isupper() and text[0].isalpha()


def check(data: object) -> list[tuple[str, bool, list[str]]]:
    """Return (name, passed, problems) for every check, in report order."""
    results: list[tuple[str, bool, list[str]]] = []

    def record(name: str, problems: list[str]) -> None:
        results.append((name, not problems, problems))

    if not isinstance(data, dict):
        record("top level is an object", ["expected a JSON object"])
        return results

    version = data.get("version")
    if not isinstance(version, int) or isinstance(version, bool) or version < 1:
        record(
            "version is a positive integer",
            [f"got {version!r}"],
        )
    else:
        record("version is a positive integer", [])

    categories = data.get("categories")
    if not isinstance(categories, list) or not categories:
        record("categories is a non-empty list", [f"got {type(categories).__name__}"])
        return results
    record("categories is a non-empty list", [])

    for index, category in enumerate(categories):
        if not isinstance(category, dict):
            record(
                "every category is an object",
                [f"index {index}: got {type(category).__name__}"],
            )
            return results
        if not isinstance(category.get("name"), str) or not isinstance(
            category.get("items"), list
        ):
            record(
                "every category has a string name and a list of items",
                [f"index {index}: got keys {sorted(category)}"],
            )
            return results
    record("every category has a string name and a list of items", [])

    names = [c["name"] for c in categories]
    record("category names are unique", duplicates(names))

    record(
        "category names are in title case",
        [f"{n!r} does not start with an uppercase letter" for n in names if not is_title_case(n)],
    )

    record(
        "category names are alphabetically ordered",
        out_of_order(names),
    )

    items = [item for c in categories for item in c["items"]]
    locations: dict[str, list[str]] = defaultdict(list)
    for category in categories:
        for item in category["items"]:
            if isinstance(item, str):
                locations[fold(item)].append(f"{category['name']} / {item!r}")

    bad_type = [f"{i!r} is not a string" for i in items if not isinstance(i, str)]
    record("every item is a string", bad_type)

    record(
        "item names are unique",
        [
            f"{key!r} appears {len(where)} times: {', '.join(where)}"
            for key, where in sorted(locations.items())
            if len(where) > 1
        ],
    )

    record(
        "item names are in title case",
        [f"{i!r} does not start with an uppercase letter" for i in items if isinstance(i, str) and not is_title_case(i)],
    )

    record(
        "item names have no surrounding whitespace",
        [f"{i!r} has leading or trailing whitespace" for i in items if isinstance(i, str) and i != i.strip()],
    )

    record(
        "items are alphabetically ordered within their category",
        [p for c in categories for p in out_of_order(c["items"], f"{c['name']}: ")],
    )

    return results


def duplicates(values: list[str]) -> list[str]:
    seen: dict[str, list[int]] = defaultdict(list)
    for position, value in enumerate(values):
        seen[fold(value)].append(position)
    return [f"{key!r} appears {len(where)} times" for key, where in sorted(seen.items()) if len(where) > 1]


def out_of_order(values: list[str], prefix: str = "") -> list[str]:
    return [
        f"{prefix}{earlier!r} should come before {later!r}"
        for earlier, later in zip(values, values[1:])
        if isinstance(earlier, str) and isinstance(later, str) and fold(earlier) > fold(later)
    ]


def main(argv: list[str]) -> int:
    path = Path(argv[1]) if len(argv) > 1 else ITEMS_JSON

    try:
        raw = path.read_text(encoding="utf-8")
    except OSError as error:
        print(f"FAIL  cannot read {path}: {error}", file=sys.stderr)
        return 1

    try:
        data = json.loads(raw)
    except json.JSONDecodeError as error:
        print(f"FAIL  {path} is not valid JSON: {error}", file=sys.stderr)
        return 1

    print(f"items.json  {path}")
    if isinstance(data, dict):
        print(f"  version {data.get('version')!r}")
        categories = data.get("categories")
        if isinstance(categories, list):
            lists = [c["items"] for c in categories if isinstance(c, dict) and isinstance(c.get("items"), list)]
            if lists:
                print(f"  {len(lists)} categories, {sum(len(i) for i in lists)} items")

    results = check(data)
    print()

    failed = 0
    for name, passed, problems in results:
        print(f"{'PASS' if passed else 'FAIL':<{STATUS_WIDTH}}{name}")
        for problem in problems:
            print(f"{'':<{STATUS_WIDTH}}{problem}")
        if not passed:
            failed += 1

    print()
    if failed:
        plural = "s" if failed != 1 else ""
        print(f"FAIL  {failed} check{plural} failed")
        return 1
    print(f"PASS  all {len(results)} checks passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))

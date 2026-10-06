# Agent Guide

TODO and Grocery List App using Jetpack Compose, Material 3 and Room 3.

- `app/` - Android app source
- `gradle/libs.versions.toml` - Library versions
- `Taskfile.yml` - [task](https://github.com/go-task/task) commands

## Android

- applicationId = org.cssnr.todolist.dev
- minSdk = 26
- targetSdk = 37
- compileSdk = 37

## Commands

ALWAYS use the `task *` commands

| Command        | Purpose                                  |
| -------------- | ---------------------------------------- |
| `task lint`    | Gradle Lint - MUST READ report, NOT exit |
| `task compile` | Compile Kotlin - DO NOT truncate output  |
| `task debug`   | Build debug variant (APK)                |
| `task release` | Build release variant (APK)              |
| `task bundle`  | Build Android App Bundle (AAB)           |
| `task check`   | Prettier check (check non-kotlin files)  |
| `task format`  | Prettier write (format non-kotlin files) |

Do NOT run task lint/compile/debug/release/bundle every turn unless it is REQUIRED!!!

### Lint

`task lint` exits 0 on warnings. `abortOnError` only fails the build on errors, so a green
exit does NOT mean the code is clean. READ THE REPORT:

    task lint -- --rerun-tasks

Without `--rerun-tasks` Gradle marks `lintReportDebug` up-to-date and leaves a stale report
behind, so a "passing" run may be reporting on older source than what is on disk. Even when
analysis output is unchanged the report file may not be rewritten, so an old timestamp is not
proof analysis was skipped either.

Reports (written fresh each run):

- `app/build/reports/lint-results-debug.sarif` - machine readable, parse this
- `app/build/reports/lint-results-debug.html`

Confirm the report timestamp is newer than the sources before trusting it. Prove a check is
actually live before trusting a zero count: add a deliberate violation of that rule, confirm
lint reports it, then remove it. `NewApi` is error-severity and fails the build.

## Testing

To test on a device use the `adb` command. If no devices are running and attached, ask the user to do this!

DO NOT uninstall the application to clear data, use: `adb shell pm clear`

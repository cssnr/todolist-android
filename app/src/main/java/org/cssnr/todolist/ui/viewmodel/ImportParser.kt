package org.cssnr.todolist.ui.viewmodel

private const val UNCATEGORIZED = "Uncategorized"

private val checkboxMarker = Regex("^\\[(?:[xX]|\\s|-)?]")

private val mojibakeMarkers = listOf("âœ“", "âˆš", "â€¢", "Ã¢Â€Â¢")

fun parseItemsForImport(text: String): List<Pair<String, String?>> = buildList {
    var category: String? = null
    var previousBlank = true
    for (rawLine in text.lineSequence()) {
        val line = rawLine.trim()
        if (line.isEmpty()) {
            previousBlank = true
            continue
        }
        if (line.startsWith("#")) {
            val heading = line.removePrefix("#").trim()
            category = if (heading.isEmpty() || heading.equals(UNCATEGORIZED, ignoreCase = true)) {
                null
            } else {
                heading
            }
            previousBlank = false
            continue
        }
        if (previousBlank && isAllCapsHeading(line)) {
            category = if (line.equals(UNCATEGORIZED, ignoreCase = true)) {
                null
            } else {
                line
            }
            previousBlank = false
            continue
        }
        val itemText = stripLeadingSymbols(line)
        previousBlank = false
        if (itemText.isEmpty()) continue
        add(itemText to category)
    }
}

private fun isAllCapsHeading(line: String): Boolean {
    if (line.length < 2) return false
    if (!line.first().isLetterOrDigit()) return false
    if (line != line.uppercase()) return false
    return line.any { it.isLetter() }
}

private fun stripLeadingSymbols(text: String): String {
    var result = text.replaceFirst(checkboxMarker, "").trimStart()
    for (marker in mojibakeMarkers) {
        if (result.startsWith(marker)) {
            result = result.removePrefix(marker).trimStart()
        }
    }
    return result.trimStart { !it.isLetterOrDigit() }
}

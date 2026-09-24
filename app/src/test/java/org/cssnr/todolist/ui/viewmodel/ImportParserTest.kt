package org.cssnr.todolist.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class ImportParserTest {

    @Test
    fun emptyInput() {
        assertEquals(emptyList<Pair<String, String?>>(), parseItemsForImport(""))
        assertEquals(emptyList<Pair<String, String?>>(), parseItemsForImport("\n\n  \n"))
    }

    @Test
    fun legacyHeadingFormat() {
        val text = """
            # Bakery
            Hamburger Buns
            Bread

            # Beverages
            Tea Bags
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(
            listOf(
                "Hamburger Buns" to "Bakery",
                "Bread" to "Bakery",
                "Tea Bags" to "Beverages",
            ),
            parsed,
        )
    }

    @Test
    fun hashWithoutSpaceIsHeading() {
        val text = """
            #Bakery
            Hamburger Buns
            #Beverages
            Tea
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(
            listOf("Hamburger Buns" to "Bakery", "Tea" to "Beverages"),
            parsed,
        )
    }

    @Test
    fun uncategorizedHeadingResetsCategory() {
        val text = """
            # Bakery
            Bread
            # Uncategorized
            Paper Towels
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(listOf("Bread" to "Bakery", "Paper Towels" to null), parsed)
    }

    @Test
    fun allCapsExampleFormat() {
        val text = """
            BAKERY
            âˆš Hamburger Buns
            âˆš Bread

            BEVERAGES
            âˆš Tea Bags
            âˆš Orange Juice

            CONDIMENTS & DRESSINGS
            â€¢ Guacamole
            âˆš Distilled White Vinegar
            âˆš Worcestershire Sauce
            âˆš Olive Oil
            âˆš Brown Mustard
            âˆš Canola Oil
            âˆš Hot Sauce
            âˆš Salsa
            â€¢ Pickles
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(13, parsed.size)
        assertEquals("Hamburger Buns" to "BAKERY", parsed[0])
        assertEquals("Bread" to "BAKERY", parsed[1])
        assertEquals("Tea Bags" to "BEVERAGES", parsed[2])
        assertEquals("Orange Juice" to "BEVERAGES", parsed[3])
        assertEquals("Guacamole" to "CONDIMENTS & DRESSINGS", parsed[4])
        assertEquals("Pickles" to "CONDIMENTS & DRESSINGS", parsed[12])
    }

    @Test
    fun allCapsFirstLineIsCategory() {
        val text = """
            BAKERY
            Hamburger Buns
            Bread
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(
            listOf("Hamburger Buns" to "BAKERY", "Bread" to "BAKERY"),
            parsed,
        )
    }

    @Test
    fun allCapsRequiresBlankLineBefore() {
        val text = """
            BAKERY
            Hamburger Buns
            CLEANING
            Soap
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(
            listOf("Hamburger Buns" to "BAKERY", "CLEANING" to "BAKERY", "Soap" to "BAKERY"),
            parsed,
        )
    }

    @Test
    fun markerItemsAreStrippedAndTrimmed() {
        val text = """
            ✓ Milk
            • Eggs
            - Bread
            * Coffee
            > Tea
            [x] Done Item
            [ ] Not Done
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(
            listOf(
                "Milk" to null,
                "Eggs" to null,
                "Bread" to null,
                "Coffee" to null,
                "Tea" to null,
                "Done Item" to null,
                "Not Done" to null,
            ),
            parsed,
        )
    }

    @Test
    fun allCapsMarkerLineIsItem() {
        val text = """
            BAKERY
            ✓ MILK
            Bread
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(
            listOf("MILK" to "BAKERY", "Bread" to "BAKERY"),
            parsed,
        )
    }

    @Test
    fun unicodeCheckmarksAreStripped() {
        val text = """
            ✓ Milk
            ✔ Eggs
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(listOf("Milk" to null, "Eggs" to null), parsed)
    }

    @Test
    fun leadingWhitespaceIsTrimmed() {
        val text = "   Milk  \n\tTea   \n  Coffee  "
        val parsed = parseItemsForImport(text)
        assertEquals(
            listOf("Milk" to null, "Tea" to null, "Coffee" to null),
            parsed,
        )
    }

    @Test
    fun symbolOnlyLineIsIgnored() {
        val text = """
            # Bakery
            Milk
            -
        """.trimIndent()
        val parsed = parseItemsForImport(text)
        assertEquals(listOf("Milk" to "Bakery"), parsed)
    }
}
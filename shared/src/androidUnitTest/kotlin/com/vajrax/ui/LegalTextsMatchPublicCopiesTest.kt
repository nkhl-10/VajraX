package com.vajrax.ui

import com.vajrax.ui.features.legal.LegalDoc
import com.vajrax.ui.features.legal.LegalTexts
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The in-app privacy policy and terms must say exactly what the website publishes
 * (the Markdown in doc/legal). After editing a Markdown file, regenerate LegalTexts.kt.
 */
class LegalTextsMatchPublicCopiesTest {
    @Test
    fun privacyPolicyMatches() = assertEquals(plain("privacy-policy.md"), inApp(LegalDoc.PRIVACY))

    @Test
    fun termsMatch() = assertEquals(plain("terms-of-use.md"), inApp(LegalDoc.TERMS))

    @Test
    fun effectiveDateMatches() {
        val md = File("../doc/legal/privacy-policy.md").readText()
        assertEquals("Effective ${LegalTexts.EFFECTIVE_DATE}", Regex("Effective [^\\n]+").find(md)!!.value)
    }

    /** Headings, paragraphs and bullets in reading order, from the Markdown. */
    private fun plain(name: String): List<String> =
        File("../doc/legal/$name").readText()
            .replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            .lines()
            .filterNot { it.startsWith("# ") || it.startsWith("Effective ") }
            .joinToString("\n")
            .split(Regex("\\n\\s*\\n"))
            .map { block -> block.lines().filter { it.isNotBlank() } }
            .filter { it.isNotEmpty() }
            .flatMap(::blockLines)

    private fun blockLines(lines: List<String>): List<String> = when {
        lines.first().startsWith("## ") -> listOf(lines.first().removePrefix("## ").trim())
        lines.all { it.startsWith("- ") } -> lines.map { it.removePrefix("- ").trim() }
        else -> listOf(lines.joinToString(" ") { it.trim() })
    }

    private fun inApp(doc: LegalDoc): List<String> =
        LegalTexts.sections(doc).flatMap { listOfNotNull(it.heading) + it.paragraphs + it.bullets }
}

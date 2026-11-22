package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Rich Markdown + LaTeX Math + Assamese-friendly renderer for Nova AI responses.
 * Eliminates raw `$` / `$$` / `\frac` / `###` / `**` noise, renders true stacked fractions
 * inside styled equation cards, ensures generous vertical breathing room for Assamese
 * conjuncts & vowel signs, and provides clean plain-text / speech formatters.
 */
@Composable
fun NovaRichMessageContent(
    rawText: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(rawText) {
        NovaMarkdownParser.parseBlocks(NovaRichTextFormatter.normalizeAssameseScript(rawText))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is NovaBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 17.sp
                        2 -> 16.sp
                        else -> 15.sp
                    }
                    val color = when (block.level) {
                        1 -> Color(0xFF7DD3FC)
                        2 -> Color(0xFFA5F3FC)
                        else -> Color(0xFFC4B5FD)
                    }
                    Text(
                        text = buildInlineAnnotatedString(
                            text = block.text,
                            baseColor = color,
                            boldColor = Color.White,
                            mathColor = Color(0xFF67E8F9),
                            baseBold = true
                        ),
                        fontSize = fontSize,
                        lineHeight = (fontSize.value + 9f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 1.dp)
                    )
                }

                is NovaBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = Color(0xFF4C2A85).copy(alpha = 0.65f)
                    )
                }

                is NovaBlock.MathGroup -> {
                    DisplayMathCard(equations = block.equations)
                }

                is NovaBlock.CodeBlock -> {
                    CodeBlockCard(language = block.language, code = block.code)
                }

                is NovaBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.indentLevel * 12 + 2).dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 9.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8))
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text),
                            fontSize = 14.5.sp,
                            lineHeight = 23.sp,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is NovaBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${block.marker}.",
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF67E8F9)
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text),
                            fontSize = 14.5.sp,
                            lineHeight = 23.sp,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is NovaBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF211647).copy(alpha = 0.75f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .heightIn(min = 20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFA855F7))
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text, baseColor = Color(0xFFE2E8F0)),
                            fontSize = 14.sp,
                            lineHeight = 22.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is NovaBlock.AnswerBanner -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = buildInlineAnnotatedString(
                                text = block.text,
                                baseColor = Color(0xFFECFDF5),
                                boldColor = Color(0xFF6EE7B7),
                                mathColor = Color(0xFFA7F3D0)
                            ),
                            fontSize = 14.5.sp,
                            lineHeight = 23.5.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }

                is NovaBlock.TableBlock -> {
                    MarkdownTableCard(headers = block.headers, rows = block.rows)
                }

                is NovaBlock.Paragraph -> {
                    Text(
                        text = buildInlineAnnotatedString(block.text),
                        fontSize = 14.5.sp,
                        lineHeight = 23.5.sp,
                        color = Color(0xFFF1F5F9)
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayMathCard(equations: List<String>) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1F164A),
        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.42f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            equations.forEach { eq ->
                MathExpressionRow(rawLatex = eq)
            }
        }
    }
}

/**
 * Renders a single display math equation line with true visual stacked fractions for `\frac{a}{b}`
 * and crisp Unicode math styling for everything else.
 */
@Composable
private fun MathExpressionRow(rawLatex: String) {
    val tokens = remember(rawLatex) { NovaLatexConverter.tokenizeForVisualRow(rawLatex) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        tokens.forEach { token ->
            when (token) {
                is MathVisualToken.Text -> {
                    Text(
                        text = buildMathAnnotatedString(token.content),
                        fontSize = 15.5.sp,
                        lineHeight = 23.sp,
                        color = Color(0xFFE0F2FE),
                        fontWeight = FontWeight.Medium
                    )
                }

                is MathVisualToken.Fraction -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = buildMathAnnotatedString(token.numerator),
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF7DD3FC),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .widthIn(min = 24.dp)
                                .width(
                                    (maxOf(
                                        token.numerator.length,
                                        token.denominator.length
                                    ).coerceIn(2, 22) * 8 + 10).dp
                                )
                                .height(1.5.dp)
                                .background(Color(0xFF38BDF8))
                        )
                        Text(
                            text = buildMathAnnotatedString(token.denominator),
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF7DD3FC),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(language: String, code: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0C0A1D),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (language.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1B4B).copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = language.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Text(
                text = code,
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                color = Color(0xFFE2E8F0),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun MarkdownTableCard(headers: List<String>, rows: List<List<String>>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1D1542),
        border = BorderStroke(1.dp, Color(0xFF4338CA).copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF312E81).copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    headers.forEach { cell ->
                        Text(
                            text = buildInlineAnnotatedString(cell, baseColor = Color(0xFFBAE6FD), baseBold = true),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 64.dp, max = 160.dp)
                        )
                    }
                }
            }
            rows.forEach { row ->
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            text = buildInlineAnnotatedString(cell),
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.widthIn(min = 64.dp, max = 160.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================================
// Block-level Markdown + Display Math Parser
// ============================================================================================

private sealed class NovaBlock {
    data class Heading(val level: Int, val text: String) : NovaBlock()
    data object Divider : NovaBlock()
    data class MathGroup(val equations: List<String>) : NovaBlock()
    data class CodeBlock(val language: String, val code: String) : NovaBlock()
    data class BulletItem(val indentLevel: Int, val text: String) : NovaBlock()
    data class NumberedItem(val marker: String, val text: String) : NovaBlock()
    data class BlockQuote(val text: String) : NovaBlock()
    data class AnswerBanner(val text: String) : NovaBlock()
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : NovaBlock()
    data class Paragraph(val text: String) : NovaBlock()
}

private object NovaMarkdownParser {

    private val headingRegex = Regex("^(#{1,6})\\s+(.+)$")
    private val dividerRegex = Regex("^\\s*([-*_])\\s*\\1\\s*\\1[\\s\\1]*$")
    private val bulletRegex = Regex("^(\\s*)[*•-]\\s+(.+)$")
    private val numberedRegex = Regex("^\\s*([0-9০-৯]+|[a-zA-Z]|[ivxIVX]+)[.)]\\s+(.+)$")
    private val answerPrefixRegex = Regex(
        "^\\s*(?:\\*{1,2}|_{1,2})?\\s*(উত্তৰ|উত্তর|उत्तर|Final Answer|Answer|সঠিক উত্তৰ)\\s*[:ঃ-]\\s*(?:\\*{1,2}|_{1,2})?.*",
        RegexOption.IGNORE_CASE
    )
    private val tableSeparatorRegex = Regex("^\\s*\\|?[\\s:|-]+\\|?\\s*$")

    fun parseBlocks(raw: String): List<NovaBlock> {
        val normalized = raw
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()
        if (normalized.isEmpty()) return emptyList()

        val lines = normalized.split("\n")
        val blocks = mutableListOf<NovaBlock>()
        var i = 0

        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty()) {
                i++
                continue
            }

            // 1. Fenced code blocks ```lang ... ```
            if (trimmed.startsWith("```")) {
                val lang = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                if (i < lines.size) i++
                blocks.add(NovaBlock.CodeBlock(lang, codeLines.joinToString("\n")))
                continue
            }

            // 2. Display math blocks: $$...$$ or \[...\] (single or multi-line, grouped consecutively)
            if (isDisplayMathStart(trimmed)) {
                val equations = mutableListOf<String>()
                while (i < lines.size) {
                    val cur = lines[i].trim()
                    if (cur.isEmpty()) {
                        // Look ahead: if next non-empty line is also display math, keep grouping
                        var nextIdx = i + 1
                        while (nextIdx < lines.size && lines[nextIdx].trim().isEmpty()) nextIdx++
                        if (nextIdx < lines.size && isDisplayMathStart(lines[nextIdx].trim())) {
                            i = nextIdx
                            continue
                        } else {
                            break
                        }
                    }
                    if (!isDisplayMathStart(cur)) break

                    val (extracted, nextIndex) = consumeDisplayMath(lines, i)
                    extracted.split("\n", "\\\\")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .forEach { equations.add(it) }
                    i = nextIndex
                }
                if (equations.isNotEmpty()) {
                    blocks.add(NovaBlock.MathGroup(equations))
                }
                continue
            }

            // 3. Horizontal divider (---, ***, ___)
            if (dividerRegex.matches(trimmed)) {
                blocks.add(NovaBlock.Divider)
                i++
                continue
            }

            // 4. Markdown Heading (#, ##, ###)
            val headingMatch = headingRegex.matchEntire(trimmed)
            if (headingMatch != null) {
                val level = headingMatch.groupValues[1].length
                val title = headingMatch.groupValues[2].trim()
                blocks.add(NovaBlock.Heading(level, title))
                i++
                continue
            }

            // 5. Markdown Table
            if (trimmed.contains("|") && i + 1 < lines.size &&
                lines[i + 1].trim().contains("|") &&
                tableSeparatorRegex.matches(lines[i + 1].trim())
            ) {
                val headers = splitTableRow(trimmed)
                val tableRows = mutableListOf<List<String>>()
                i += 2
                while (i < lines.size && lines[i].trim().contains("|")) {
                    val rowCells = splitTableRow(lines[i].trim())
                    if (rowCells.isNotEmpty()) tableRows.add(rowCells)
                    i++
                }
                blocks.add(NovaBlock.TableBlock(headers, tableRows))
                continue
            }

            // 6. Blockquote (> ...)
            if (trimmed.startsWith(">")) {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    quoteLines.add(lines[i].trim().removePrefix(">").trim())
                    i++
                }
                blocks.add(NovaBlock.BlockQuote(quoteLines.joinToString("\n")))
                continue
            }

            // 7. Highlighted Final Answer line (e.g. **উত্তৰ:** ..., **Answer:** ...)
            if (answerPrefixRegex.matches(trimmed)) {
                blocks.add(NovaBlock.AnswerBanner(trimmed))
                i++
                continue
            }

            // 8. Bullet List item (* ..., - ..., • ...)
            val bulletMatch = bulletRegex.matchEntire(rawLine)
            if (bulletMatch != null) {
                val indent = (bulletMatch.groupValues[1].length / 2).coerceIn(0, 3)
                blocks.add(NovaBlock.BulletItem(indent, bulletMatch.groupValues[2].trim()))
                i++
                continue
            }

            // 9. Numbered List item (1. ..., ১. ...) — avoid matching "(i)" which is handled as heading/paragraph
            val numMatch = numberedRegex.matchEntire(rawLine)
            if (numMatch != null && !trimmed.startsWith("(")) {
                val marker = numMatch.groupValues[1]
                val body = numMatch.groupValues[2].trim()
                blocks.add(NovaBlock.NumberedItem(marker, body))
                i++
                continue
            }

            // 10. Regular paragraph (merge consecutive plain lines, splitting if a line has inline $$...$$)
            if (trimmed.contains("$$")) {
                // Split mixed line around $$ ... $$ so embedded display equations render cleanly
                val parts = trimmed.split("$$")
                parts.forEachIndexed { idx, part ->
                    val p = part.trim()
                    if (p.isNotEmpty()) {
                        if (idx % 2 == 1) {
                            blocks.add(NovaBlock.MathGroup(listOf(p)))
                        } else {
                            blocks.add(NovaBlock.Paragraph(p))
                        }
                    }
                }
                i++
                continue
            }

            val paraLines = mutableListOf(trimmed)
            i++
            while (i < lines.size) {
                val nextRaw = lines[i]
                val nextTrim = nextRaw.trim()
                if (nextTrim.isEmpty() ||
                    nextTrim.startsWith("```") ||
                    isDisplayMathStart(nextTrim) ||
                    nextTrim.contains("$$") ||
                    dividerRegex.matches(nextTrim) ||
                    headingRegex.matches(nextTrim) ||
                    bulletRegex.matches(nextRaw) ||
                    (numberedRegex.matches(nextRaw) && !nextTrim.startsWith("(")) ||
                    nextTrim.startsWith(">") ||
                    answerPrefixRegex.matches(nextTrim)
                ) {
                    break
                }
                paraLines.add(nextTrim)
                i++
            }
            blocks.add(NovaBlock.Paragraph(paraLines.joinToString("\n")))
        }

        return blocks
    }

    private fun isDisplayMathStart(trimmed: String): Boolean {
        return trimmed.startsWith("$$") || trimmed.startsWith("\\[")
    }

    private fun consumeDisplayMath(lines: List<String>, startIndex: Int): Pair<String, Int> {
        val first = lines[startIndex].trim()
        if (first.startsWith("$$")) {
            val afterOpen = first.removePrefix("$$")
            val closeIdx = afterOpen.indexOf("$$")
            if (closeIdx >= 0) {
                return afterOpen.substring(0, closeIdx).trim() to (startIndex + 1)
            }
            val sb = StringBuilder()
            if (afterOpen.isNotBlank()) sb.append(afterOpen.trim()).append("\n")
            var idx = startIndex + 1
            while (idx < lines.size) {
                val cur = lines[idx].trim()
                val endPos = cur.indexOf("$$")
                if (endPos >= 0) {
                    val before = cur.substring(0, endPos).trim()
                    if (before.isNotEmpty()) sb.append(before)
                    return sb.toString().trim() to (idx + 1)
                }
                sb.append(cur).append("\n")
                idx++
            }
            return sb.toString().trim() to idx
        } else {
            val afterOpen = first.removePrefix("\\[")
            val closeIdx = afterOpen.indexOf("\\]")
            if (closeIdx >= 0) {
                return afterOpen.substring(0, closeIdx).trim() to (startIndex + 1)
            }
            val sb = StringBuilder()
            if (afterOpen.isNotBlank()) sb.append(afterOpen.trim()).append("\n")
            var idx = startIndex + 1
            while (idx < lines.size) {
                val cur = lines[idx].trim()
                val endPos = cur.indexOf("\\]")
                if (endPos >= 0) {
                    val before = cur.substring(0, endPos).trim()
                    if (before.isNotEmpty()) sb.append(before)
                    return sb.toString().trim() to (idx + 1)
                }
                sb.append(cur).append("\n")
                idx++
            }
            return sb.toString().trim() to idx
        }
    }

    private fun splitTableRow(line: String): List<String> {
        return line
            .trim()
            .removePrefix("|")
            .removeSuffix("|")
            .split("|")
            .map { it.trim() }
    }
}

// ============================================================================================
// Inline Markdown + LaTeX -> Compose AnnotatedString
// ============================================================================================

private fun buildInlineAnnotatedString(
    text: String,
    baseColor: Color = Color(0xFFF1F5F9),
    boldColor: Color = Color.White,
    mathColor: Color = Color(0xFF7DD3FC),
    baseBold: Boolean = false
): AnnotatedString {
    // First convert \( ... \) inline math delimiters to $...$ for unified scanning
    val prepped = text
        .replace(Regex("""\\\((.+?)\\\)""")) { m -> "$${m.groupValues[1].trim()}$" }

    return buildAnnotatedString {
        val len = prepped.length
        var i = 0
        while (i < len) {
            // 1. Escaped dollar \$ -> literal $
            if (prepped[i] == '\\' && i + 1 < len && prepped[i + 1] == '$') {
                append('$')
                i += 2
                continue
            }

            // 2. Inline Math $...$ (Pandoc rule: no leading/trailing space so currency "$50 and $100" stays intact)
            if (prepped[i] == '$' && (i + 1 < len && prepped[i + 1] != '$')) {
                val close = findClosingInlineDollar(prepped, i + 1)
                if (close > i + 1) {
                    val mathInner = prepped.substring(i + 1, close)
                    val renderedMath = NovaLatexConverter.toReadableInlineMath(mathInner)
                    appendMathStyled(
                        text = renderedMath,
                        color = mathColor,
                        fontWeight = if (baseBold) FontWeight.Bold else FontWeight.SemiBold
                    )
                    i = close + 1
                    continue
                }
            }

            // 3. Inline code `...`
            if (prepped[i] == '`') {
                val close = prepped.indexOf('`', i + 1)
                if (close > i + 1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF67E8F9),
                            background = Color(0xFF1E1B4B),
                            fontSize = 13.sp
                        )
                    ) {
                        append(" ${prepped.substring(i + 1, close)} ")
                    }
                    i = close + 1
                    continue
                }
            }

            // 4. Bold **...** or __...__
            if (i + 1 < len && ((prepped[i] == '*' && prepped[i + 1] == '*') || (prepped[i] == '_' && prepped[i + 1] == '_'))) {
                val delim = prepped.substring(i, i + 2)
                val close = prepped.indexOf(delim, i + 2)
                if (close > i + 2) {
                    val inner = prepped.substring(i + 2, close)
                    val nested = buildInlineAnnotatedString(
                        text = inner,
                        baseColor = boldColor,
                        boldColor = boldColor,
                        mathColor = mathColor,
                        baseBold = true
                    )
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                        append(nested)
                    }
                    i = close + 2
                    continue
                }
            }

            // 5. Strikethrough ~~...~~
            if (i + 1 < len && prepped[i] == '~' && prepped[i + 1] == '~') {
                val close = prepped.indexOf("~~", i + 2)
                if (close > i + 2) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = baseColor.copy(alpha = 0.7f))) {
                        append(prepped.substring(i + 2, close))
                    }
                    i = close + 2
                    continue
                }
            }

            // 6. Italic *...* (only if non-space after opening *)
            if (prepped[i] == '*' && i + 1 < len && !prepped[i + 1].isWhitespace() && prepped[i + 1] != '*') {
                val close = prepped.indexOf('*', i + 1)
                if (close > i + 1 && !prepped[close - 1].isWhitespace()) {
                    val inner = prepped.substring(i + 1, close)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(NovaLatexConverter.cleanStrayLatexInText(inner))
                    }
                    i = close + 1
                    continue
                }
            }

            // 7. Stray LaTeX command outside $...$ (e.g. \times, \frac{a}{b}, \dots)
            if (prepped[i] == '\\' && i + 1 < len && prepped[i + 1].isLetter()) {
                val (converted, consumedLen) = NovaLatexConverter.consumeInlineLatexCommand(prepped, i)
                if (consumedLen > 0) {
                    withStyle(SpanStyle(color = mathColor, fontWeight = FontWeight.Medium)) {
                        append(converted)
                    }
                    i += consumedLen
                    continue
                }
            }

            // Regular character
            withStyle(
                SpanStyle(
                    color = baseColor,
                    fontWeight = if (baseBold) FontWeight.Bold else FontWeight.Normal
                )
            ) {
                append(prepped[i])
            }
            i++
        }
    }
}

private fun findClosingInlineDollar(text: String, start: Int): Int {
    if (start >= text.length) return -1
    // First char inside $...$ must not be whitespace
    if (text[start].isWhitespace()) return -1
    var i = start
    while (i < text.length) {
        val ch = text[i]
        if (ch == '\n') return -1
        if (ch == '\\') {
            i += 2
            continue
        }
        if (ch == '$') {
            // Char right before closing $ must not be whitespace
            if (i > start && !text[i - 1].isWhitespace()) {
                // Guard against currency pair like "$50 and $100": if content has digits followed by space + English words, skip
                val inner = text.substring(start, i)
                if (Regex("^\\d+(?:[.,]\\d+)?\\s+[a-zA-Z]{2,}.*").matches(inner) &&
                    !inner.contains("\\") && !inner.contains("=") && !inner.contains("+") && !inner.contains("-")
                ) {
                    return -1
                }
                return i
            }
            return -1
        }
        i++
    }
    return -1
}

private fun buildMathAnnotatedString(
    cleanMath: String,
    color: Color = Color(0xFFE0F2FE)
): AnnotatedString = buildAnnotatedString {
    appendMathStyled(cleanMath, color, FontWeight.Medium)
}

/**
 * Appends math text with true Compose superscript/subscript styling for any remaining
 * `^{...}` / `_{...}` markers that didn't have direct Unicode glyphs.
 */
private fun AnnotatedString.Builder.appendMathStyled(
    text: String,
    color: Color,
    fontWeight: FontWeight,
    scriptSize: TextUnit = 11.sp
) {
    var i = 0
    while (i < text.length) {
        if ((text[i] == '^' || text[i] == '_') && i + 1 < text.length) {
            val isSuper = text[i] == '^'
            val (group, nextIdx) = if (text[i + 1] == '{') {
                NovaLatexConverter.readBracedGroup(text, i + 1)
            } else {
                text[i + 1].toString() to (i + 2)
            }
            if (group.isNotEmpty()) {
                withStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = fontWeight,
                        fontSize = scriptSize,
                        baselineShift = if (isSuper) BaselineShift.Superscript else BaselineShift.Subscript
                    )
                ) {
                    append(group)
                }
                i = nextIdx
                continue
            }
        }
        withStyle(SpanStyle(color = color, fontWeight = fontWeight)) {
            append(text[i])
        }
        i++
    }
}

// ============================================================================================
// LaTeX Math Tokenization & Conversion Engine
// ============================================================================================

internal sealed class MathVisualToken {
    data class Text(val content: String) : MathVisualToken()
    data class Fraction(val numerator: String, val denominator: String) : MathVisualToken()
}

internal object NovaLatexConverter {

    private val unicodeSuperscripts = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ', 'a' to 'ᵃ',
        'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'k' to 'ᵏ', 'm' to 'ᵐ', 't' to 'ᵗ'
    )

    private val unicodeSubscripts = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'k' to 'ₖ',
        'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ', 'p' to 'ₚ', 'r' to 'ᵣ',
        's' to 'ₛ', 't' to 'ₜ', 'x' to 'ₓ'
    )

    private val simpleFractions = mapOf(
        "1/2" to "½", "1/3" to "⅓", "2/3" to "⅔",
        "1/4" to "¼", "3/4" to "¾", "1/5" to "⅕",
        "2/5" to "⅖", "3/5" to "⅗", "4/5" to "⅘"
    )

    private val latexSymbolMap = linkedMapOf(
        "\\Rightarrow" to "⇒",
        "\\Leftarrow" to "⇐",
        "\\Leftrightarrow" to "⇔",
        "\\rightarrow" to "→",
        "\\leftarrow" to "←",
        "\\implies" to "⇒",
        "\\iff" to "⇔",
        "\\to" to "→",
        "\\therefore" to "∴",
        "\\because" to "∵",
        "\\times" to "×",
        "\\cdot" to "·",
        "\\div" to "÷",
        "\\pm" to "±",
        "\\mp" to "∓",
        "\\ldots" to "…",
        "\\cdots" to "…",
        "\\vdots" to "⋮",
        "\\ddots" to "⋱",
        "\\dots" to "…",
        "\\leq" to "≤",
        "\\le" to "≤",
        "\\geq" to "≥",
        "\\ge" to "≥",
        "\\neq" to "≠",
        "\\ne" to "≠",
        "\\approx" to "≈",
        "\\equiv" to "≡",
        "\\propto" to "∝",
        "\\infty" to "∞",
        "\\degree" to "°",
        "\\circ" to "°",
        "\\angle" to "∠",
        "\\triangle" to "△",
        "\\perp" to "⊥",
        "\\parallel" to "∥",
        "\\cong" to "≅",
        "\\sim" to "∼",
        "\\notin" to "∉",
        "\\in" to "∈",
        "\\subseteq" to "⊆",
        "\\subset" to "⊂",
        "\\supseteq" to "⊇",
        "\\supset" to "⊃",
        "\\cup" to "∪",
        "\\cap" to "∩",
        "\\emptyset" to "∅",
        "\\sum" to "∑",
        "\\prod" to "∏",
        "\\int" to "∫",
        "\\partial" to "∂",
        "\\nabla" to "∇",
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\Gamma" to "Γ",
        "\\delta" to "δ",
        "\\Delta" to "Δ",
        "\\epsilon" to "ε",
        "\\varepsilon" to "ε",
        "\\theta" to "θ",
        "\\Theta" to "Θ",
        "\\lambda" to "λ",
        "\\Lambda" to "Λ",
        "\\mu" to "μ",
        "\\pi" to "π",
        "\\Pi" to "Π",
        "\\rho" to "ρ",
        "\\sigma" to "σ",
        "\\Sigma" to "Σ",
        "\\tau" to "τ",
        "\\phi" to "φ",
        "\\Phi" to "Φ",
        "\\psi" to "ψ",
        "\\omega" to "ω",
        "\\Omega" to "Ω",
        "\\sin" to "sin",
        "\\cos" to "cos",
        "\\tan" to "tan",
        "\\cot" to "cot",
        "\\sec" to "sec",
        "\\csc" to "csc",
        "\\log" to "log",
        "\\ln" to "ln",
        "\\lim" to "lim",
        "\\max" to "max",
        "\\min" to "min",
        "\\mod" to "mod",
        "\\qquad" to "  ",
        "\\quad" to " ",
        "\\," to " ",
        "\\;" to " ",
        "\\:" to " ",
        "\\!" to "",
        "\\left" to "",
        "\\right" to "",
        "\\{" to "{",
        "\\}" to "}",
        "\\%" to "%",
        "\\$" to "$"
    )

    /**
     * Splits a display equation into alternating Text and stacked Fraction tokens.
     */
    fun tokenizeForVisualRow(rawLatex: String): List<MathVisualToken> {
        val cleaned = rawLatex
            .trim()
            .removePrefix("$$").removeSuffix("$$")
            .removePrefix("$").removeSuffix("$")
            .replace("\\dfrac", "\\frac")
            .replace("\\tfrac", "\\frac")
            .trim()

        val tokens = mutableListOf<MathVisualToken>()
        val textBuf = StringBuilder()
        var i = 0

        while (i < cleaned.length) {
            if (cleaned.startsWith("\\frac", i) &&
                (i + 5 >= cleaned.length || !cleaned[i + 5].isLetter())
            ) {
                var pos = i + 5
                while (pos < cleaned.length && cleaned[pos].isWhitespace()) pos++
                val (num, afterNum) = readArg(cleaned, pos)
                var pos2 = afterNum
                while (pos2 < cleaned.length && cleaned[pos2].isWhitespace()) pos2++
                val (den, afterDen) = readArg(cleaned, pos2)
                if (num.isNotEmpty() && den.isNotEmpty()) {
                    if (textBuf.isNotEmpty()) {
                        tokens.add(MathVisualToken.Text(toReadableInlineMath(textBuf.toString())))
                        textBuf.clear()
                    }
                    tokens.add(
                        MathVisualToken.Fraction(
                            numerator = toReadableInlineMath(num),
                            denominator = toReadableInlineMath(den)
                        )
                    )
                    i = afterDen
                    continue
                }
            }
            textBuf.append(cleaned[i])
            i++
        }

        if (textBuf.isNotEmpty()) {
            tokens.add(MathVisualToken.Text(toReadableInlineMath(textBuf.toString())))
        }
        return tokens
    }

    /**
     * Converts a LaTeX math string into clean Unicode math text with zero `$` or raw `\command` noise.
     */
    fun toReadableInlineMath(rawLatex: String): String {
        var s = rawLatex
            .trim()
            .removePrefix("$$").removeSuffix("$$")
            .removePrefix("$").removeSuffix("$")
            .replace("\\dfrac", "\\frac")
            .replace("\\tfrac", "\\frac")

        // Unwrap \text{...}, \mathrm{...}, \mathbf{...}, \textbf{...}, \mathit{...}, \boxed{...}
        s = unwrapCommand(s, "\\text")
        s = unwrapCommand(s, "\\mathrm")
        s = unwrapCommand(s, "\\mathbf")
        s = unwrapCommand(s, "\\textbf")
        s = unwrapCommand(s, "\\mathit")
        s = unwrapCommand(s, "\\operatorname")
        s = unwrapCommand(s, "\\boxed")

        // Convert inline \frac{a}{b} -> a/b or (a)/(b)
        s = convertFractionsInline(s)

        // Convert \sqrt[n]{x} and \sqrt{x}
        s = convertSqrt(s)

        // Degree shorthand ^{\circ} or ^\circ -> °
        s = s.replace("^{\\circ}", "°").replace("^\\circ", "°")

        // Replace LaTeX commands (sorted longest-first by our linkedMap order + boundary check)
        for ((cmd, replacement) in latexSymbolMap) {
            if (cmd.length > 2 && cmd[1].isLetter()) {
                s = s.replace(Regex("${Regex.escape(cmd)}(?![a-zA-Z])"), replacement)
            } else {
                s = s.replace(cmd, replacement)
            }
        }

        // Convert superscripts ^{...} and ^x -> Unicode superscripts where possible
        s = convertScripts(s, '^', unicodeSuperscripts)
        // Convert subscripts _{...} and _x -> Unicode subscripts where possible
        s = convertScripts(s, '_', unicodeSubscripts)

        // Clean any leftover math grouping braces { } that aren't part of remaining ^{...}/_{...}
        s = stripPlainBraces(s)

        // Prettify minus between spaces & normalize spaces
        s = s.replace(" - ", " − ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        return s
    }

    fun cleanStrayLatexInText(text: String): String {
        if (!text.contains("\\") && !text.contains("$")) return text
        var s = text
        s = s.replace(Regex("""\$([^$\n]+?)\$""")) { m -> toReadableInlineMath(m.groupValues[1]) }
        if (s.contains("\\")) {
            s = convertFractionsInline(s)
            s = convertSqrt(s)
            for ((cmd, replacement) in latexSymbolMap) {
                if (cmd.length > 2 && cmd[1].isLetter()) {
                    s = s.replace(Regex("${Regex.escape(cmd)}(?![a-zA-Z])"), replacement)
                }
            }
        }
        return s
    }

    fun consumeInlineLatexCommand(text: String, start: Int): Pair<String, Int> {
        if (text.startsWith("\\frac", start) || text.startsWith("\\dfrac", start) || text.startsWith("\\tfrac", start)) {
            val cmdLen = if (text.startsWith("\\frac", start)) 5 else 6
            var pos = start + cmdLen
            while (pos < text.length && text[pos].isWhitespace()) pos++
            val (num, afterNum) = readArg(text, pos)
            var pos2 = afterNum
            while (pos2 < text.length && text[pos2].isWhitespace()) pos2++
            val (den, afterDen) = readArg(text, pos2)
            if (num.isNotEmpty() && den.isNotEmpty()) {
                return formatInlineFraction(toReadableInlineMath(num), toReadableInlineMath(den)) to (afterDen - start)
            }
        }
        if (text.startsWith("\\sqrt", start)) {
            val pos = start + 5
            val (inner, after) = readArg(text, pos)
            if (inner.isNotEmpty()) {
                val cleanInner = toReadableInlineMath(inner)
                val out = if (cleanInner.length <= 2) "√$cleanInner" else "√($cleanInner)"
                return out to (after - start)
            }
        }
        for ((cmd, replacement) in latexSymbolMap) {
            if (cmd.length > 2 && text.startsWith(cmd, start)) {
                val after = start + cmd.length
                if (after >= text.length || !text[after].isLetter()) {
                    return replacement to cmd.length
                }
            }
        }
        return "" to 0
    }

    fun readBracedGroup(text: String, openBraceIdx: Int): Pair<String, Int> {
        if (openBraceIdx >= text.length || text[openBraceIdx] != '{') return "" to openBraceIdx
        var depth = 0
        var i = openBraceIdx
        while (i < text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return text.substring(openBraceIdx + 1, i) to (i + 1)
                    }
                }
            }
            i++
        }
        return text.substring(openBraceIdx + 1) to text.length
    }

    private fun readArg(text: String, startIdx: Int): Pair<String, Int> {
        if (startIdx >= text.length) return "" to startIdx
        return if (text[startIdx] == '{') {
            readBracedGroup(text, startIdx)
        } else {
            text[startIdx].toString() to (startIdx + 1)
        }
    }

    private fun unwrapCommand(input: String, cmd: String): String {
        var s = input
        var searchFrom = 0
        while (true) {
            val idx = s.indexOf(cmd, searchFrom)
            if (idx < 0) break
            val afterCmd = idx + cmd.length
            if (afterCmd < s.length && s[afterCmd].isLetter()) {
                searchFrom = afterCmd
                continue
            }
            var pos = afterCmd
            while (pos < s.length && s[pos].isWhitespace()) pos++
            if (pos < s.length && s[pos] == '{') {
                val (inner, endIdx) = readBracedGroup(s, pos)
                s = s.substring(0, idx) + inner + s.substring(endIdx)
                searchFrom = idx + inner.length
            } else {
                searchFrom = afterCmd
            }
        }
        return s
    }

    private fun convertFractionsInline(input: String): String {
        var s = input
        var guard = 0
        while (s.contains("\\frac") && guard++ < 20) {
            val idx = s.indexOf("\\frac")
            var pos = idx + 5
            while (pos < s.length && s[pos].isWhitespace()) pos++
            val (num, afterNum) = readArg(s, pos)
            var pos2 = afterNum
            while (pos2 < s.length && s[pos2].isWhitespace()) pos2++
            val (den, afterDen) = readArg(s, pos2)
            if (num.isEmpty() || den.isEmpty()) break
            val replacement = formatInlineFraction(num.trim(), den.trim())
            s = s.substring(0, idx) + replacement + s.substring(afterDen)
        }
        return s
    }

    private fun formatInlineFraction(num: String, den: String): String {
        simpleFractions["$num/$den"]?.let { return it }
        val needsNumParens = num.any { it == '+' || it == '-' || it == '−' || it == ' ' }
        val needsDenParens = den.any { it == '+' || it == '-' || it == '−' || it == ' ' }
        val n = if (needsNumParens) "($num)" else num
        val d = if (needsDenParens) "($den)" else den
        return "$n/$d"
    }

    private fun convertSqrt(input: String): String {
        var s = input
        var guard = 0
        while (s.contains("\\sqrt") && guard++ < 20) {
            val idx = s.indexOf("\\sqrt")
            var pos = idx + 5
            var rootPrefix = "√"
            if (pos < s.length && s[pos] == '[') {
                val closeBracket = s.indexOf(']', pos + 1)
                if (closeBracket > pos) {
                    val rootDeg = s.substring(pos + 1, closeBracket).trim()
                    rootPrefix = when (rootDeg) {
                        "3" -> "∛"
                        "4" -> "∜"
                        else -> "${toUnicodeScript(rootDeg, unicodeSuperscripts) ?: rootDeg}√"
                    }
                    pos = closeBracket + 1
                }
            }
            val (inner, after) = readArg(s, pos)
            if (inner.isEmpty()) break
            val clean = inner.trim()
            val formatted = if (clean.length <= 2 && !clean.contains(" ")) "$rootPrefix$clean" else "$rootPrefix($clean)"
            s = s.substring(0, idx) + formatted + s.substring(after)
        }
        return s
    }

    private fun convertScripts(input: String, marker: Char, map: Map<Char, Char>): String {
        val sb = StringBuilder()
        var i = 0
        while (i < input.length) {
            if (input[i] == marker && i + 1 < input.length) {
                val (content, nextIdx) = if (input[i + 1] == '{') {
                    readBracedGroup(input, i + 1)
                } else {
                    input[i + 1].toString() to (i + 2)
                }
                val unicode = toUnicodeScript(content.trim(), map)
                if (unicode != null) {
                    sb.append(unicode)
                } else {
                    // Keep marker^{content} so Compose BaselineShift renders it cleanly
                    sb.append(marker).append('{').append(content.trim()).append('}')
                }
                i = nextIdx
                continue
            }
            sb.append(input[i])
            i++
        }
        return sb.toString()
    }

    private fun toUnicodeScript(content: String, map: Map<Char, Char>): String? {
        if (content.isEmpty()) return null
        val out = StringBuilder(content.length)
        for (ch in content) {
            if (ch == ' ') continue
            val mapped = map[ch] ?: return null
            out.append(mapped)
        }
        return out.toString()
    }

    private fun stripPlainBraces(input: String): String {
        val sb = StringBuilder(input.length)
        var i = 0
        while (i < input.length) {
            val ch = input[i]
            if ((ch == '^' || ch == '_') && i + 1 < input.length && input[i + 1] == '{') {
                val (inner, endIdx) = readBracedGroup(input, i + 1)
                sb.append(ch).append('{').append(inner).append('}')
                i = endIdx
                continue
            }
            if (ch != '{' && ch != '}') {
                sb.append(ch)
            }
            i++
        }
        return sb.toString()
    }
}

// ============================================================================================
// Clean Copy, Speech & Assamese Script Normalization Helpers
// ============================================================================================

object NovaRichTextFormatter {

    private val assameseMarkers = listOf(
        "ৰ", "ৱ", "অসমীয়া", "হ'ল", "হৈছে", "ইয়াত", "দিয়া আছে", "এতিয়া",
        "প্ৰথম", "সাধাৰণ", "অন্তৰ", "সমান্তৰ", "প্ৰগতি", "যোগফল", "সূত্ৰ",
        "পাওঁ", "বহুৱাই", "গতিকে", "তেন্তে", "উত্তৰ", "পদৰ", "পদলৈ",
        "কৰি", "আৰু", "নহয়", "হয়", "হ'ব", "আমাৰ", "তোমাৰ", "আপোনাৰ", "কেনেদৰে"
    )

    /**
     * When a response is in Assamese, fixes accidental Bengali 'র' (U+09B0) characters into
     * authentic Assamese 'ৰ' (U+09F0) so Assamese script renders purely and accurately.
     * Pure Bengali responses (without Assamese markers) are left untouched.
     */
    fun normalizeAssameseScript(text: String): String {
        if (!text.contains('\u09B0')) return text
        val isAssamese = assameseMarkers.any { text.contains(it) }
        return if (isAssamese) text.replace('\u09B0', '\u09F0') else text
    }

    /**
     * Converts raw Markdown + LaTeX into clean, readable plain text for Clipboard Copy
     * (zero raw `$`, `$$`, `\frac`, `###`, or `**` clutter).
     */
    fun formatForPlainCopy(raw: String): String {
        val normalized = normalizeAssameseScript(raw)
        val lines = normalized.replace("\r\n", "\n").split("\n")
        return lines.joinToString("\n") { line ->
            val trimmed = line.trim()
            when {
                trimmed.matches(Regex("^[-*_]{3,}$")) -> "────────────"
                trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length > 4 -> {
                    NovaLatexConverter.toReadableInlineMath(trimmed)
                }
                else -> {
                    var s = line
                    s = s.replace(Regex("^#{1,6}\\s+"), "")
                    s = s.replace(Regex("^(\\s*)[*-]\\s+"), "$1• ")
                    s = s.replace(Regex("""\$\$(.+?)\$\$""")) { m -> NovaLatexConverter.toReadableInlineMath(m.groupValues[1]) }
                    s = s.replace(Regex("""\\\((.+?)\\\)""")) { m -> NovaLatexConverter.toReadableInlineMath(m.groupValues[1]) }
                    s = s.replace(Regex("""\$([^$\n]+?)\$""")) { m -> NovaLatexConverter.toReadableInlineMath(m.groupValues[1]) }
                    s = NovaLatexConverter.cleanStrayLatexInText(s)
                    s = s.replace("**", "").replace("__", "").replace("`", "")
                    s
                }
            }
        }.trim()
    }

    /**
     * Converts Markdown + LaTeX math into natural spoken text so TTS never reads
     * "dollar dollar backslash frac" aloud.
     */
    fun formatForSpeech(raw: String): String {
        var s = formatForPlainCopy(raw)
        s = s.replace(Regex("```[\\s\\S]*?```"), " Here is the code example. ")
        s = s.replace("×", " times ")
            .replace("÷", " divided by ")
            .replace("±", " plus or minus ")
            .replace("≤", " less than or equal to ")
            .replace("≥", " greater than or equal to ")
            .replace("≠", " not equal to ")
            .replace("≈", " approximately equal to ")
            .replace("√", " square root of ")
            .replace("π", " pi ")
            .replace("∞", " infinity ")
            .replace("⇒", " implies ")
            .replace("∴", " therefore ")
            .replace("∵", " because ")
            .replace("────────────", " ")
            .replace(Regex("[*#`_]"), " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
        return s
    }
}

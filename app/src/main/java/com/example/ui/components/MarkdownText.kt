package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.CosmicMuted
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.RadiantAqua
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.StarlightWhite

@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = StarlightWhite
) {
    val clipboardManager = LocalClipboardManager.current
    val blocks = parseMarkdownBlocks(content)

    SelectionContainer {
        Column(modifier = modifier) {
            blocks.forEach { block ->
                when (block) {
                    is MarkdownBlock.Header -> {
                        val fontSize = when (block.level) {
                            1 -> 22.sp
                            2 -> 19.sp
                            else -> 17.sp
                        }
                        Text(
                            text = parseInlineMarkdown(block.text, textColor),
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = CelestialCyan,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    is MarkdownBlock.CodeBlock -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SpaceBlack.copy(alpha = 0.85f))
                                .border(1.dp, ElectricIndigo.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = block.language.ifEmpty { "code" }.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RadiantAqua,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(block.code))
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("copy_code_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy code",
                                            tint = CosmicMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = block.code,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    is MarkdownBlock.BulletItem -> {
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "• ",
                                color = CelestialCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = parseInlineMarkdown(block.text, textColor),
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor
                            )
                        }
                    }

                    is MarkdownBlock.NumberedItem -> {
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "${block.number}. ",
                                color = CelestialCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = parseInlineMarkdown(block.text, textColor),
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor
                            )
                        }
                    }

                    is MarkdownBlock.Paragraph -> {
                        Text(
                            text = parseInlineMarkdown(block.text, textColor),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

private fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code block check ```
        if (line.trim().startsWith("```")) {
            val language = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            // Skip the closing ```
            if (i < lines.size) i++
            blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            continue
        }

        val trimmed = line.trim()
        when {
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                val number = trimmed.substringBefore(".")
                val rest = trimmed.substringAfter(".").trim()
                blocks.add(MarkdownBlock.NumberedItem(number, rest))
            }
            trimmed.isNotEmpty() -> {
                blocks.add(MarkdownBlock.Paragraph(line))
            }
        }
        i++
    }

    return blocks
}

private fun parseInlineMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val pattern = Regex("(\\*\\*|\\*|`)(.*?)\\1")
        val matches = pattern.findAll(text)

        matches.forEach { match ->
            val range = match.range
            if (range.first > cursor) {
                append(text.substring(cursor, range.first))
            }
            val delimiter = match.groupValues[1]
            val innerContent = match.groupValues[2]

            when (delimiter) {
                "**" -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor))
                    append(innerContent)
                    pop()
                }
                "*" -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor))
                    append(innerContent)
                    pop()
                }
                "`" -> {
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = DeepNavy,
                            color = RadiantAqua,
                            fontSize = 13.sp
                        )
                    )
                    append(" $innerContent ")
                    pop()
                }
            }
            cursor = range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}

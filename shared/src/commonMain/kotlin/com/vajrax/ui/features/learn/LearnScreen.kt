package com.vajrax.ui.features.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LuminaTheme

/**
 * Lumina Design: Learn Screen - Book to Practice Transformation Module.
 * Dynamically supports Light and Dark modes.
 */
@Composable
fun LearnScreen(
    state: LearnUiState,
    onIntent: (LearnIntent) -> Unit
) {
    val colors = LuminaTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(44.dp))

        // Top Header Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("✨", fontSize = 12.sp)
            Text(
                text = "TRANSFORMATION MODULE",
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Theory to Practice",
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Distill complex ideas into actionable daily rituals. The true measure of knowledge is behavior change.",
            color = colors.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // List of Book Ideas
        state.ideas.forEach { idea ->
            BookTransformationCard(
                idea = idea,
                isApplying = state.isApplyingIdeaId == idea.id,
                customText = state.userCustomPracticeText,
                onStartApply = { onIntent(LearnIntent.StartApplyFlow(idea.id)) },
                onCancelApply = { onIntent(LearnIntent.CancelApplyFlow) },
                onTextChange = { onIntent(LearnIntent.UpdatePracticeText(it)) },
                onLaunch = { onIntent(LearnIntent.Launch7DayExperiment(idea.id, state.userCustomPracticeText)) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

/**
 * Book Transformation Card with Key Idea, Inset Interpretation, and Practice Conversion.
 */
@Composable
private fun BookTransformationCard(
    idea: BookIdea,
    isApplying: Boolean,
    customText: String,
    onStartApply: () -> Unit,
    onCancelApply: () -> Unit,
    onTextChange: (String) -> Unit,
    onLaunch: () -> Unit
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x0A0F172A)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Column {
            // Book Title & Author
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = idea.bookTitle,
                        color = colors.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = idea.author,
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                if (idea.isExperimentActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primaryContainer)
                            .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${idea.experimentDaysLeft ?: 7}d left",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // KEY IDEA Quote Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceContainerLow)
                    .padding(14.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "KEY IDEA",
                            color = colors.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${idea.idea}\"",
                        color = colors.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Practice Conversion Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceContainerLowest)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ CONVERT TO PRACTICE",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "7-Day Experiment",
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = idea.practicalApplication,
                        color = colors.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!idea.isExperimentActive && !isApplying) {
                        Button(
                            onClick = onStartApply,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("▶ Begin Practice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else if (isApplying) {
                        Column {
                            OutlinedTextField(
                                value = customText,
                                onValueChange = onTextChange,
                                placeholder = { Text("How will you practice this daily?", color = colors.onSurfaceVariant, fontSize = 12.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceContainerLow,
                                    unfocusedContainerColor = colors.surfaceContainerLow,
                                    focusedTextColor = colors.onSurface,
                                    unfocusedTextColor = colors.onSurface,
                                    focusedIndicatorColor = colors.primary,
                                    unfocusedIndicatorColor = colors.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = onCancelApply) {
                                    Text("Cancel", color = colors.onSurfaceVariant, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onLaunch,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary,
                                        contentColor = colors.onPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Launch 7-Day Experiment", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓", color = colors.statusSuccess, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("7-Day Experiment actively tracked in Today timeline", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

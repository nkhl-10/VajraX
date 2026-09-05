package com.vajrax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.domain.ai.AiHumanTouchEngine
import com.vajrax.domain.ai.SageContext
import com.vajrax.domain.ai.SageResponse
import com.vajrax.ui.theme.LuminaTheme

data class ChatMessage(
    val id: String,
    val sender: String, // "User" or "Sage"
    val content: String,
    val sageResponse: SageResponse? = null
)

/**
 * Interactive Vajra AI Sage (Human Touch Life Companion).
 * Clean, calm, editorial companion accessible across all screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VajraAiCompanionSheet(
    onDismissRequest: () -> Unit,
    activePathName: String = "High Performance",
    currentFocusTitle: String? = "Deep Work",
    completedCount: Int = 2,
    totalCount: Int = 5,
    engine: AiHumanTouchEngine = remember { AiHumanTouchEngine() }
) {
    val colors = LuminaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputQuery by remember { mutableStateOf("") }

    val initialResponse = remember {
        engine.askSage(
            "guidance",
            SageContext(
                activePathName = activePathName,
                currentTaskTitle = currentFocusTitle,
                completedCount = completedCount,
                totalCount = totalCount
            )
        )
    }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "init_sage",
                    sender = "Sage",
                    content = "Peace, traveler. I am here to walk alongside you on the $activePathName path. What is present in your mind right now?",
                    sageResponse = initialResponse
                )
            )
        )
    }

    val listState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.onSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // 1. Clean Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 16.sp)
                    }

                    Column {
                        Text(
                            text = "AI SAGE COMPANION",
                            color = colors.primary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Philosophical Life Guidance",
                            color = colors.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceContainerLow)
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable { onDismissRequest() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("Close", color = colors.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Quick Prompts (Clean Outlined Chips)
            val quickPrompts = listOf(
                "Overcome Friction" to "I feel resistance starting my task",
                "Pre-Focus Ritual" to "Give me a pre focus activation ritual",
                "Daily Stoic Wisdom" to "Share daily stoic wisdom",
                "Evening Wind Down" to "I need evening wind down and decompression"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.take(2).forEach { (label, query) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceContainerLow)
                            .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
                            .clickable {
                                val userMsg = ChatMessage(id = "u_${messages.size}", sender = "User", content = query)
                                val sageResp = engine.askSage(
                                    query,
                                    SageContext(activePathName, currentFocusTitle, completedCount, totalCount)
                                )
                                val sageMsg = ChatMessage(
                                    id = "s_${messages.size}",
                                    sender = "Sage",
                                    content = sageResp.empathyMessage,
                                    sageResponse = sageResp
                                )
                                messages = messages + userMsg + sageMsg
                            }
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = colors.onSurface, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.drop(2).forEach { (label, query) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceContainerLow)
                            .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
                            .clickable {
                                val userMsg = ChatMessage(id = "u_${messages.size}", sender = "User", content = query)
                                val sageResp = engine.askSage(
                                    query,
                                    SageContext(activePathName, currentFocusTitle, completedCount, totalCount)
                                )
                                val sageMsg = ChatMessage(
                                    id = "s_${messages.size}",
                                    sender = "Sage",
                                    content = sageResp.empathyMessage,
                                    sageResponse = sageResp
                                )
                                messages = messages + userMsg + sageMsg
                            }
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = colors.onSurface, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Conversation Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    if (msg.sender == "User") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary)
                                    .padding(12.dp)
                            ) {
                                Text(msg.content, color = colors.onPrimary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceContainerLow)
                                .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val resp = msg.sageResponse
                                if (resp != null) {
                                    Text(
                                        text = resp.title.uppercase(),
                                        color = colors.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = resp.empathyMessage,
                                        color = colors.onSurface,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.surface)
                                            .border(1.dp, colors.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(
                                                text = "ACTIONABLE MICRO-STEP",
                                                color = colors.statusWarning,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                            Text(
                                                text = resp.practicalMicroStep,
                                                color = colors.onSurface,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Text(
                                        text = resp.philosophicalAnchor,
                                        color = colors.onSurfaceVariant,
                                        fontSize = 11.5.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                } else {
                                    Text(
                                        text = msg.content,
                                        color = colors.onSurface,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask the Sage anything...", fontSize = 13.sp, color = colors.onSurfaceVariant) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceContainerLow,
                        unfocusedContainerColor = colors.surfaceContainerLow,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedIndicatorColor = colors.primary,
                        unfocusedIndicatorColor = colors.outlineVariant
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            val userMsg = ChatMessage(id = "u_${messages.size}", sender = "User", content = inputQuery)
                            val sageResp = engine.askSage(
                                inputQuery,
                                SageContext(activePathName, currentFocusTitle, completedCount, totalCount)
                            )
                            val sageMsg = ChatMessage(
                                id = "s_${messages.size}",
                                sender = "Sage",
                                content = sageResp.empathyMessage,
                                sageResponse = sageResp
                            )
                            messages = messages + userMsg + sageMsg
                            inputQuery = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Ask", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


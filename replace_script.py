import sys

file_path = r'c:\Users\User\AndroidStudioProjects\VajraX\shared\src\commonMain\kotlin\com\vajrax\ui\features\today\TodayScreen.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

imports_to_add = """import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
"""
content = content.replace('import com.vajrax.ui.theme.LuminaTheme', imports_to_add + 'import com.vajrax.ui.theme.LuminaTheme')

task_item_class = """
sealed class TodayTaskItem {
    abstract val id: String
    data class Completed(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
    data class Active(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
    data class Next(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
    data class Later(val title: String, val timeTag: String, override val id: String) : TodayTaskItem()
}
"""
content = content.replace('fun TodayScreen(', task_item_class + '\n@Composable\nfun TodayScreen(')

lazy_col_impl = """    val allTasks = remember(state.completedItems, state.currentFocus, state.nextItems, state.laterItems) {
        buildList {
            if (state.completedItems.isNotEmpty() || state.currentFocus != null || state.nextItems.isNotEmpty() || state.laterItems.isNotEmpty()) {
                state.completedItems.forEach { add(TodayTaskItem.Completed(it.title, it.scheduledTime ?: "Done", it.id)) }
                state.currentFocus?.let { add(TodayTaskItem.Active(it.title, it.scheduledTime ?: "Today", it.id)) }
                state.nextItems.forEach { add(TodayTaskItem.Next(it.title, it.scheduledTime ?: "Next", it.id)) }
                state.laterItems.forEach { add(TodayTaskItem.Later(it.title, it.scheduledTime ?: "Later", it.id)) }
            } else {
                add(TodayTaskItem.Completed("planning", "Tomorrow", "mc1"))
                add(TodayTaskItem.Completed("Weekly planning", "Tomorrow", "mc2"))
                add(TodayTaskItem.Active("Daily review", "Today", "ma1"))
                add(TodayTaskItem.Later("Notes", "2 days ago", "ml1"))
                add(TodayTaskItem.Later("Notes", "2 days ago", "ml2"))
            }
        }
    }

    val listState = rememberLazyListState()

    // 2. Main Screen Body
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item(key = "header") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Good Morning.",
                color = colors.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Task Completion",
                                color = colors.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You're on track this week",
                                color = colors.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "o",
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.targetPercentage}%",
                                    color = colors.primary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    val completedFraction = if (state.todayTotalCount > 0) {
                        (state.todayCompletedCount.toFloat() / state.todayTotalCount.toFloat()).coerceIn(0f, 1f)
                    } else 0.20f
                    val ringPercentText = "${(completedFraction * 100).toInt()}%"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = colors.surfaceContainerHigh
                        val progressColor = colors.primary

                        Canvas(modifier = Modifier.size(86.dp)) {
                            val strokeWidth = 8.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                            val arcSize = Size(diameter, diameter)

                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = 360f * (if (completedFraction > 0f) completedFraction else 0.20f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Text(
                            text = if (state.todayCompletedCount > 0) ringPercentText else "20%",
                            color = colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeStatPod(
                            label = "Today",
                            value = "${state.todayCompletedCount}/${state.todayTotalCount}",
                            modifier = Modifier.weight(1f)
                        )
                        HomeStatPod(
                            label = "Week",
                            value = "${state.consistencyPercentage}%",
                            modifier = Modifier.weight(1f)
                        )
                        HomeStatPod(
                            label = "Consistency",
                            value = "${state.pacePercentage}%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.lastCompletedActionId != null) {
            item(key = "evidence_banner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainerLow)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "o",
                                color = Color(0xFF16A34A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.lastCompletedActionTitle ?: "Practice"} completed",
                                color = colors.onSurface,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primaryContainer)
                                    .clickable {
                                        onIntent(TodayIntent.OpenEvidenceForAction(state.lastCompletedActionId))
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "+ Add evidence",
                                    color = colors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "x",
                                color = colors.outline,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { onIntent(TodayIntent.DismissOptionalEvidencePrompt) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(26.dp))
            }
        } else {
            item(key = "spacer_if_no_banner") {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        itemsIndexed(allTasks, key = { _, task -> task.id }) { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp) // Provide space for maxTranslationX
                    .padding(vertical = 8.dp)
                    .graphicsLayer {
                        val layoutInfo = listState.layoutInfo
                        val viewportHeight = layoutInfo.viewportSize.height.toFloat()
                        val centerY = viewportHeight / 2f

                        val itemInfo = layoutInfo.visibleItemsInfo.find { it.key == item.id }
                        if (itemInfo != null) {
                            val itemY = itemInfo.offset.toFloat() + (itemInfo.size.toFloat() / 2f)
                            val distanceFromCenter = itemY - centerY
                            
                            val normalizedDist = (distanceFromCenter / (viewportHeight / 1.6f)).coerceIn(-1f, 1f)
                            
                            val maxTranslationX = 60.dp.toPx()
                            
                            // User provided parabolic true arc formula
                            translationX = maxTranslationX * (1f - (normalizedDist * normalizedDist))
                            
                            alpha = 1f - 0.7f * abs(normalizedDist)
                            
                            val scale = 1f - 0.15f * abs(normalizedDist)
                            scaleX = scale
                            scaleY = scale
                        }
                    }
            ) {
                when (item) {
                    is TodayTaskItem.Completed -> CompletedTaskItem(
                        title = item.title,
                        timeTag = item.timeTag,
                        onClick = { onIntent(TodayIntent.OpenEvidenceForAction(item.id)) }
                    )
                    is TodayTaskItem.Active -> ActiveFocusCard(
                        title = item.title,
                        timeTag = item.timeTag,
                        onComplete = { onIntent(TodayIntent.QuickCompletePractice(item.id)) },
                        onClick = { onIntent(TodayIntent.StartPractice(item.id)) }
                    )
                    is TodayTaskItem.Next -> UpcomingTaskItem(
                        title = item.title,
                        timeTag = item.timeTag,
                        onClick = { onIntent(TodayIntent.StartPractice(item.id)) }
                    )
                    is TodayTaskItem.Later -> NoteHistoryItem(
                        title = item.title,
                        timeTag = item.timeTag
                    )
                }
            }
        }
    }"""

start_idx = content.find('// 2. Main Screen Body')
end_idx = content.find('// Evidence Sheet')

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + lazy_col_impl + '\n    ' + content[end_idx:]
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print('Replaced')
else:
    print('Failed to find indices')

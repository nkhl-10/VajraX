package com.vajrax.ui.features.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.TrackingMode
import com.vajrax.ui.utils.gyroShadowCard

@Composable
fun TodayScreen(
    state: TodayUiState,
    onIntent: (TodayIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 120.dp)
        ) {
            item {
                HomeDashboardCard(state)
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = state.activePathName.ifBlank { "30-Day Challenge" },
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete daily routine",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(state.allTimelineItems) { item ->
                val isActive = state.currentFocus?.id == item.id
                val isCompleted = item.status == ActionStatus.COMPLETE || item.status == ActionStatus.MINIMUM

                if (isActive) {
                    ActiveTaskCard(
                        item = item,
                        onComplete = { onIntent(TodayIntent.CompletePractice(item.id)) },
                        onNotes = { onIntent(TodayIntent.OpenEvidenceForAction(item.id)) }
                    )
                } else if (isCompleted) {
                    CompletedTaskRow(item)
                } else {
                    PendingTaskRow(item)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HomeDashboardCard(state: TodayUiState) {
    val progress = if (state.todayTotalCount > 0) state.todayCompletedCount.toFloat() / state.todayTotalCount else 0f
    val percentStr = "${(progress * 100).toInt()}%"
    val greetingTitle = state.greeting.split("\n").firstOrNull() ?: "Good Morning."

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .gyroShadowCard()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greetingTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Task Completion",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "You're on track this Day",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(
                        progress = 1f,
                        color = Color(0xFFE5E7EB),
                        strokeWidth = 6.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    CircularProgressIndicator(
                        progress = progress,
                        color = Color(0xFF5D3FD3),
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = percentStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(modifier = Modifier.weight(1f), title = "Today", value = "${state.todayCompletedCount}/${state.todayTotalCount}")
                StatBox(modifier = Modifier.weight(1f), title = "Week", value = "${state.pacePercentage}%")
                StatBox(modifier = Modifier.weight(1f), title = "Consistency", value = "${state.consistencyPercentage}%")
            }
        }
    }
}

@Composable
fun StatBox(modifier: Modifier, title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            .background(Color(0xFFFAFAFA), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp)
    ) {
        Text(text = title, color = Color(0xFF4B5563), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ActiveTaskCard(item: ActionTimelineItem, onComplete: () -> Unit, onNotes: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F4FF), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Active circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF5D3FD3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.title,
                    color = Color(0xFF5D3FD3),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.scheduledTime ?: "",
                    color = Color(0xFF5D3FD3),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(start = 44.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionButton(icon = "✓", text = "Mark done", onClick = onComplete)
                ActionButton(icon = "✎", text = "Notes", onClick = onNotes)
            }
        }
    }
}

@Composable
fun ActionButton(icon: String, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 12.sp, color = Color(0xFF5D3FD3))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFF5D3FD3),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CompletedTaskRow(item: ActionTimelineItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFF5D3FD3), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = Color.White, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = item.scheduledTime ?: "",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp
        )
    }
}

@Composable
fun PendingTaskRow(item: ActionTimelineItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFFE5E7EB), CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = item.scheduledTime ?: "",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp
        )
    }
}

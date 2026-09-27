import sys

file_path = r'c:\Users\User\AndroidStudioProjects\VajraX\shared\src\commonMain\kotlin\com\vajrax\ui\features\today\TodayScreen.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

start_sig = '@Composable\nprivate fun ActiveFocusCard('
start_idx = content.find(start_sig)
end_sig = '@Composable\nprivate fun UpcomingTaskItem('
end_idx = content.find(end_sig)

if start_idx != -1 and end_idx != -1:
    new_impl = """@Composable
private fun ActiveFocusCard(
    title: String,
    timeTag: String,
    onComplete: () -> Unit,
    onSaveNote: (String) -> Unit,
    onClick: () -> Unit
) {
    val colors = LuminaTheme.colors
    var isNotesExpanded by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3F4FB))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Solid Blue Circle with white dot
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    Text(
                        text = title,
                        color = colors.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = timeTag,
                    color = colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mark Done Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
                        .clickable(onClick = onComplete)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("✓", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Mark done", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                // Notes Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(20.dp))
                        .clickable { isNotesExpanded = !isNotesExpanded }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("✏️", fontSize = 13.sp)
                    Text("Notes", color = colors.primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Expanded Notes Area
            if (isNotesExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                androidx.compose.foundation.text.BasicTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = colors.onSurface,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text("Add a quick note for this workout...", color = Color(0xFF9CA3AF), fontSize = 15.sp)
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Save Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.primary)
                        .clickable { 
                            onSaveNote(noteText)
                            isNotesExpanded = false
                            noteText = ""
                        }
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text("Save", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
"""

    new_content = content[:start_idx] + new_impl + content[end_idx:]
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print('Replaced ActiveFocusCard successfully')
else:
    print('Could not find start/end indices')

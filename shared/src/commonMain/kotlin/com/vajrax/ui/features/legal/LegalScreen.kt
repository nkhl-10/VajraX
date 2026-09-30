package com.vajrax.ui.features.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vajrax.ui.designsystem.VxTopBar
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace

/** Read-only legal document (privacy policy, terms, health notice, licenses), available offline. */
@Composable
fun LegalScreen(doc: LegalDoc, onBack: () -> Unit) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize().background(colors.background)) {
        VxTopBar(title = doc.title, onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = VxSpace.gutter, end = VxSpace.gutter, top = VxSpace.lg, bottom = VxSpace.xxxl)
        ) {
            if (doc != LegalDoc.LICENSES) {
                item {
                    Text(
                        "Effective ${LegalTexts.EFFECTIVE_DATE}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(VxSpace.md))
                }
            }
            items(LegalTexts.sections(doc)) { section ->
                Column(Modifier.fillMaxWidth().padding(bottom = VxSpace.xl)) {
                    section.heading?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(Modifier.height(VxSpace.sm))
                    }
                    section.paragraphs.forEach { p ->
                        Text(p, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                        Spacer(Modifier.height(VxSpace.sm))
                    }
                    section.bullets.forEach { b ->
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Text("•", style = MaterialTheme.typography.bodyMedium, color = colors.primary)
                            Spacer(Modifier.width(VxSpace.sm))
                            Text(b, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

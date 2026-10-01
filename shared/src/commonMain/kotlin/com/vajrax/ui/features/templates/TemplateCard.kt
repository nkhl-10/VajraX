package com.vajrax.ui.features.templates

import org.jetbrains.compose.resources.stringResource
import com.vajrax.resources.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.ui.designsystem.UsePill
import com.vajrax.ui.designsystem.TonalAction
import com.vajrax.ui.designsystem.VxCard
import com.vajrax.ui.designsystem.VxIcons
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace

/** Template card from the Discover design: title, description, habit count, frequency and "Use". */
@Composable
fun TemplateCard(
    template: DefaultTemplate,
    onOpen: () -> Unit,
    onUse: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    /** The routine already running: shows "Active" (opens it) instead of "Use". */
    isActive: Boolean = false
) {
    val colors = LuminaTheme.colors
    VxCard(modifier = modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (template.author != null) {
                    Text(stringResource(Res.string.templates_by_fmt, template.author), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    template.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(VxSpace.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(VxIcons.ListChecks, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (template.habits.size == 1) stringResource(Res.string.templates_1_habit) else stringResource(Res.string.profile_habits_fmt, template.habits.size), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Spacer(Modifier.width(VxSpace.md))
                    Icon(VxIcons.Clock, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(template.frequencyLabel, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    if (badge != null) {
                        Spacer(Modifier.width(VxSpace.md))
                        Text(badge, style = MaterialTheme.typography.labelMedium, color = colors.primary)
                    }
                }
            }
            Spacer(Modifier.width(VxSpace.md))
            if (isActive) {
                TonalAction(stringResource(Res.string.templates_active), VxIcons.Check, onOpen, modifier = Modifier.semantics { contentDescription = "${template.name} is your current routine" })
            } else {
                UsePill(onClick = onUse, modifier = Modifier.semantics { contentDescription = "Use ${template.name}" })
            }
        }
    }
}

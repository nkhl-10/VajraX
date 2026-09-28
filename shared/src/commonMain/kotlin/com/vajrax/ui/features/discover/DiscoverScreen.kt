package com.vajrax.ui.features.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.TemplateCatalog
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.features.templates.DashedAddBox
import com.vajrax.ui.features.templates.TemplateCard
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace

@Composable
fun DiscoverScreen(
    state: DiscoverState,
    onIntent: (DiscoverIntent) -> Unit,
    onOpenTemplate: (String) -> Unit,
    onUseTemplate: (String) -> Unit,
    onCreateTemplate: () -> Unit
) {
    val colors = LuminaTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(start = VxSpace.gutter, end = VxSpace.gutter, bottom = VxSpace.navClearance)
    ) {
        item(key = "header") {
            Spacer(Modifier.statusBarsPadding().height(VxSpace.lg))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ScreenTitle("Discover", Modifier.weight(1f))
                RoundIconButton(
                    if (state.searchOpen) VxIcons.Close else VxIcons.Search,
                    if (state.searchOpen) "Close search" else "Search templates",
                    onClick = { onIntent(DiscoverIntent.ToggleSearch) }
                )
            }
            if (state.searchOpen) {
                Spacer(Modifier.height(VxSpace.md))
                VxTextField(
                    label = "Search templates",
                    value = state.query,
                    onValueChange = { onIntent(DiscoverIntent.Search(it)) },
                    placeholder = "Name, habit or goal"
                )
            }
            Spacer(Modifier.height(VxSpace.md))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                CategoryChip("All", state.category == null, onClick = { onIntent(DiscoverIntent.SelectCategory(null)) })
                state.categories.forEach { c ->
                    CategoryChip(c, state.category == c, onClick = { onIntent(DiscoverIntent.SelectCategory(if (state.category == c) null else c)) })
                }
            }
            Spacer(Modifier.height(VxSpace.xl))
        }
        if (state.isLoading) {
            item { LoadingSkeleton() }
            return@LazyColumn
        }
        val nothing = state.visibleProvided.isEmpty() && state.visibleCommunity.isEmpty() && state.visibleMine.isEmpty()
        if (nothing) {
            item {
                EmptyState(
                    VxIcons.Search, "No templates found", "Try another word or category, or build your own.",
                    actionLabel = "Create a template", onAction = onCreateTemplate
                )
            }
        }
        section("Provided templates", state.visibleProvided, state.activeTemplateId, onOpenTemplate, onUseTemplate)
        section("Community templates", state.visibleCommunity, state.activeTemplateId, onOpenTemplate, onUseTemplate)
        section("My templates", state.visibleMine, state.activeTemplateId, onOpenTemplate, onUseTemplate)
        item(key = "create") {
            if (!state.isFiltering) {
                DashedAddBox(
                    title = "Build your own template",
                    subtitle = "Turn a routine you already do into a reusable tracker.",
                    buttonLabel = "Create template",
                    onClick = onCreateTemplate
                )
                Spacer(Modifier.height(VxSpace.md))
                val blank = TemplateCatalog.BLANK_ID
                ListRow("Start a blank tracker", onClick = { onUseTemplate(blank) }, icon = VxIcons.Plus)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    list: List<DefaultTemplate>,
    activeId: String?,
    onOpen: (String) -> Unit,
    onUse: (String) -> Unit
) {
    if (list.isEmpty()) return
    item(key = "label_$title") {
        SectionLabel(title)
        Spacer(Modifier.height(VxSpace.sm))
    }
    items(list, key = { "${title}_${it.id}" }) { t ->
        TemplateCard(
            template = t,
            onOpen = { onOpen(t.id) },
            onUse = { onUse(t.id) },
            badge = if (t.id == activeId) "Active" else null
        )
        Spacer(Modifier.height(VxSpace.md))
    }
    item(key = "gap_$title") { Spacer(Modifier.height(VxSpace.md)) }
}

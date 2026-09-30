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
    // statusBarsPadding outside the list: scrolled cards never slide under the status bar.
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background).statusBarsPadding(),
        contentPadding = PaddingValues(start = VxSpace.gutter, end = VxSpace.gutter, bottom = VxSpace.navClearance)
    ) {
        item(key = "header") {
            Spacer(Modifier.height(VxSpace.lg))
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
            SegmentedToggle(
                options = DiscoverTab.entries.map { it.label },
                selectedIndex = state.tab.ordinal,
                onSelect = { onIntent(DiscoverIntent.SelectTab(DiscoverTab.entries[it])) },
                modifier = Modifier.fillMaxWidth()
            )
            if (state.visibleCategories.isNotEmpty()) {
                Spacer(Modifier.height(VxSpace.md))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                    CategoryChip("All", state.category == null, onClick = { onIntent(DiscoverIntent.SelectCategory(null)) })
                    state.visibleCategories.forEach { c ->
                        CategoryChip(c, state.category == c, onClick = { onIntent(DiscoverIntent.SelectCategory(if (state.category == c) null else c)) })
                    }
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
                if (state.tab == DiscoverTab.MINE && !state.isFiltering) {
                    EmptyState(VxIcons.Pen, "No templates yet", "Build your own in a minute.", actionLabel = "Create template", onAction = onCreateTemplate)
                } else {
                    EmptyState(VxIcons.Search, "No templates found", "Try another word or category.")
                }
            }
        }
        section(if (state.tab == DiscoverTab.ARC) "Arc templates" else "Provided templates", state.visibleProvided, state.activeTemplateId, onOpenTemplate, onUseTemplate)
        // Bundled picks: there is no user community yet, so they aren't presented as one.
        section("Featured templates", state.visibleCommunity, state.activeTemplateId, onOpenTemplate, onUseTemplate)
        section("My templates", state.visibleMine, state.activeTemplateId, onOpenTemplate, onUseTemplate)
        item(key = "create") {
            if (!state.isFiltering && state.tab != DiscoverTab.ARC && !(state.tab == DiscoverTab.MINE && nothing)) {
                DashedAddBox(
                    title = "Build your own template",
                    subtitle = "Your routine, reusable.",
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

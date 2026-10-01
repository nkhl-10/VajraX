package com.vajrax.ui.features.discover

import org.jetbrains.compose.resources.stringResource
import com.vajrax.resources.*
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
                ScreenTitle(stringResource(Res.string.discover_discover), Modifier.weight(1f))
                RoundIconButton(
                    if (state.searchOpen) VxIcons.Close else VxIcons.Search,
                    if (state.searchOpen) stringResource(Res.string.discover_close_search) else stringResource(Res.string.discover_search_templates),
                    onClick = { onIntent(DiscoverIntent.ToggleSearch) }
                )
            }
            if (state.searchOpen) {
                Spacer(Modifier.height(VxSpace.md))
                VxTextField(
                    label = stringResource(Res.string.discover_search_templates),
                    value = state.query,
                    onValueChange = { onIntent(DiscoverIntent.Search(it)) },
                    placeholder = stringResource(Res.string.discover_name_habit_or_goal)
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
                    CategoryChip(stringResource(Res.string.discover_all), state.category == null, onClick = { onIntent(DiscoverIntent.SelectCategory(null)) })
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
                    EmptyState(VxIcons.Pen, stringResource(Res.string.discover_no_templates_yet), stringResource(Res.string.discover_build_your_own_in_a), actionLabel = stringResource(Res.string.discover_create_template), onAction = onCreateTemplate)
                } else {
                    EmptyState(VxIcons.Search, stringResource(Res.string.discover_no_templates_found), stringResource(Res.string.discover_try_another_word_or_category))
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
                    title = stringResource(Res.string.discover_build_your_own_template),
                    subtitle = stringResource(Res.string.discover_your_routine_reusable),
                    buttonLabel = stringResource(Res.string.discover_create_template),
                    onClick = onCreateTemplate
                )
                Spacer(Modifier.height(VxSpace.md))
                val blank = TemplateCatalog.BLANK_ID
                ListRow(stringResource(Res.string.discover_start_a_blank_tracker), onClick = { onUseTemplate(blank) }, icon = VxIcons.Plus)
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
            isActive = t.id == activeId
        )
        Spacer(Modifier.height(VxSpace.md))
    }
    item(key = "gap_$title") { Spacer(Modifier.height(VxSpace.md)) }
}

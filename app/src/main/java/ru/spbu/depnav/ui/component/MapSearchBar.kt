/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.spbu.depnav.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING

// These are basically copied from SearchBar implementation
private val ACTIVATION_ENTER_SPEC = tween<Float>(
    durationMillis = 600,
    delayMillis = 100,
    easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
)
private val ACTIVATION_EXIT_SPEC = tween<Float>(
    durationMillis = 350,
    delayMillis = 100,
    easing = CubicBezierEasing(0.0f, 1.0f, 0.0f, 1.0f)
)

/**
 * Search bar for querying map markers on [ru.spbu.depnav.ui.screen.MapScreen].
 */
@Composable
@Suppress(
    "LongMethod", // No point in further shrinking
    "LongParameterList" // Considered OK for composables
)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    results: Map<Marker, MarkerText>,
    onResultClick: (Int) -> Unit,
    onInfoClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activationAnimationProgress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = if (active) ACTIVATION_ENTER_SPEC else ACTIVATION_EXIT_SPEC,
        label = "Map search bar activation animation progress"
    )

    val (insetsStartPadding, insetsEndPadding) = with(WindowInsets.systemBars.asPaddingValues()) {
        val layoutDirection = LocalLayoutDirection.current
        calculateStartPadding(layoutDirection) to calculateEndPadding(layoutDirection)
    }

    val outerStartPadding = insetsStartPadding * (1 - activationAnimationProgress)
    val outerEndPadding = insetsEndPadding * (1 - activationAnimationProgress)
    val innerStartPadding = insetsStartPadding * activationAnimationProgress
    val innerEndPadding = insetsEndPadding * activationAnimationProgress

    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onActiveChange(false) },
        active = active,
        onActiveChange = onActiveChange,
        modifier = Modifier
            .apply {
                if (active) padding(start = outerStartPadding, end = outerEndPadding)
                else windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            }
            .then(modifier),
        placeholder = { Text(stringResource(R.string.search_markers), maxLines = 1) },
        leadingIcon = {
            AnimatedLeadingIcon(active, modifier = Modifier.padding(start = innerStartPadding)) {
                onActiveChange(false)
            }
        },
        trailingIcon = {
            AnimatedTrailingIcons(
                active,
                query.isEmpty(),
                onClearClick = { onQueryChange("") },
                onInfoClick = onInfoClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier.padding(end = innerEndPadding)
            )
        }
    ) {
        val keyboard = LocalSoftwareKeyboardController.current
        // Without remember this may change quicker than the history itself arrives because VM
        // debounces the queries
        val isHistory = remember(results) { query.isEmpty() }

        SearchResults(
            markersWithTexts = results,
            isHistory = isHistory,
            onScroll = { onTop -> keyboard?.apply { if (onTop) show() else hide() } },
            onResultClick = {
                onActiveChange(false)
                onResultClick(it)
            },
            modifier = Modifier
                .padding(horizontal = DEFAULT_PADDING * 1.5f)
                .padding(
                    start = innerStartPadding,
                    end = innerEndPadding,
                    bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
        )
    }
}

@Composable
private fun AnimatedLeadingIcon(
    searchBarActive: Boolean,
    modifier: Modifier = Modifier,
    onNavigateBackClick: () -> Unit
) {
    AnimatedContent(
        searchBarActive,
        modifier = modifier,
        label = "Map search bar leading icon change"
    ) { showBackButton ->
        if (showBackButton) {
            IconButton(onClick = onNavigateBackClick) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.label_navigate_back)
                )
            }
        } else {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                // To have the same size as the back button above
                modifier = Modifier.minimumInteractiveComponentSize()
            )
        }
    }
}

@Composable
@Suppress("LongParameterList") // Considered OK for composables
private fun AnimatedTrailingIcons(
    searchBarActive: Boolean,
    queryEmpty: Boolean,
    onClearClick: () -> Unit,
    onInfoClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        searchBarActive to queryEmpty,
        modifier = modifier,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Map search bar trailing icon change"
    ) { (active, emptyQuery) ->
        if (active) {
            if (emptyQuery) {
                Spacer(modifier = Modifier.minimumInteractiveComponentSize())
            } else {
                IconButton(onClick = onClearClick) {
                    Icon(
                        Icons.Rounded.Clear,
                        contentDescription = stringResource(R.string.label_clear_text_field)
                    )
                }
            }
        } else {
            Row {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = stringResource(R.string.label_open_map_info)
                    )
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.label_open_settings)
                    )
                }
            }
        }
    }
}

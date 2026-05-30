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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.lerp
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.theme.MAP_OVERLAY_ALPHA
import ru.spbu.depnav.ui.viewmodel.SearchResults

// These are basically copied from SearchBar implementation
private val EXPANSION_ENTER_SPEC = tween<Float>(
    durationMillis = 600,
    delayMillis = 100,
    easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
)
private val EXPANSION_EXIT_SPEC = tween<Float>(
    durationMillis = 350,
    delayMillis = 100,
    easing = CubicBezierEasing(0.0f, 1.0f, 0.0f, 1.0f)
)

/**
 * Search bar for querying map markers on [ru.spbu.depnav.ui.screen.MapScreen].
 */
@Composable
@Suppress("LongParameterList") // Considered OK for a composable
@OptIn(ExperimentalMaterial3Api::class)
fun MapSearchBar(
    queryState: TextFieldState,
    mapTitle: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    results: SearchResults,
    onResultClick: (Int) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expansionProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = if (expanded) EXPANSION_ENTER_SPEC else EXPANSION_EXIT_SPEC,
        label = "Map search bar activation animation progress"
    )

    // SearchBar always consumes the insets but adds padding only when collapsed
    val horizontalInsetsPadding =
        (SearchBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal) *
                expansionProgress).asPaddingValues()

    // TODO: on older Android versions SearchBar auto-opens after rotation for some reason, seems
    //  to be a Google's bug, not of just this app.
    // TODO: SearchBar + ExpandedFullScreenSearchBar is now preferred but currently it has its own
    //  problems (after collapsing keyboard flashes and if there are horizontal insets input field
    //  jumps horizontally). To be revisited in the future.
    SearchBar(
        inputField = {
            val focusManager = LocalFocusManager.current
            SearchBarDefaults.InputField(
                state = queryState,
                onSearch = { focusManager.clearFocus() },
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                placeholder = {
                    Text(
                        stringResource(R.string.search_on_map, mapTitle),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    AnimatedLeadingIcon(
                        expanded,
                        onMenuClick = onMenuClick,
                        onNavigateBackClick = { onExpandedChange(false) }
                    )
                },
                trailingIcon = {
                    AnimatedTrailingIcon(
                        queryEmpty = queryState.text.isEmpty(),
                        onClearClick = queryState::clearText
                    )
                },
                colors = SearchBarDefaults.inputFieldColors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(horizontalInsetsPadding)
                    // On older Android versions it is impossible to leave the search without this
                    .focusProperties { canFocus = expanded || expansionProgress == 0f }
            )
        },
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        colors = SearchBarDefaults.colors(
            containerColor = SearchBarDefaults.colors().containerColor.copy(
                alpha = lerp(MAP_OVERLAY_ALPHA, 1f, expansionProgress)
            )
        )
    ) {
        val keyboard = LocalSoftwareKeyboardController.current
        SearchResultsView(
            results,
            onScroll = { onTop -> keyboard?.apply { if (onTop) show() else hide() } },
            onResultClick = {
                onExpandedChange(false)
                onResultClick(it)
            },
            modifier = Modifier
                .padding(horizontalInsetsPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        )
    }

    LaunchedEffect(expanded) {
        if (!expanded) {
            queryState.clearText()
        }
    }
}

@Composable
private operator fun WindowInsets.times(num: Float): WindowInsets {
    val paddings = asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    return WindowInsets(
        paddings.calculateLeftPadding(layoutDirection) * num,
        paddings.calculateTopPadding() * num,
        paddings.calculateRightPadding(layoutDirection) * num,
        paddings.calculateBottomPadding() * num
    )
}

@Composable
private fun AnimatedLeadingIcon(
    searchBarActive: Boolean,
    onMenuClick: () -> Unit,
    onNavigateBackClick: () -> Unit
) {
    AnimatedContent(searchBarActive) { active ->
        if (active) {
            IconButton(onClick = onNavigateBackClick) {
                Icon(
                    painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.label_navigate_back)
                )
            }
        } else {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painterResource(R.drawable.ic_menu),
                    contentDescription = stringResource(R.string.label_open_main_menu)
                )
            }
        }
    }
}

@Composable
private fun AnimatedTrailingIcon(queryEmpty: Boolean, onClearClick: () -> Unit) {
    AnimatedVisibility(!queryEmpty, enter = fadeIn(), exit = fadeOut()) {
        IconButton(onClick = onClearClick) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.label_clear_text_field)
            )
        }
    }
}

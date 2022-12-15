/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofey Pushkin
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

package ru.spbu.depnav.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING

/** Screen containing a marker search and the results found. */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun SearchScreen(
    vm: SearchScreenViewModel = hiltViewModel(),
    onResultClick: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val fillMaxWidthModifier = Modifier.fillMaxWidth(0.9f)

            SearchField(
                onTextChange = vm.queryTextFlow::tryEmit,
                onClear = vm::clearMatches,
                onBackClick = onNavigateBack,
                modifier = fillMaxWidthModifier,
                placeholder = stringResource(R.string.search_markers)
            )

            Divider(
                modifier = fillMaxWidthModifier,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            if (vm.searchMatches.let { it == null || it.isNotEmpty() }) {
                val keyboard = LocalSoftwareKeyboardController.current

                SearchResults(
                    markersWithTexts = vm.searchMatches ?: vm.searchHistory,
                    isHistory = vm.searchMatches == null,
                    onStateChange = { onTop -> keyboard?.apply { if (onTop) show() else hide() } },
                    onResultClick = { markerId ->
                        vm.addToSearchHistory(markerId)
                        onResultClick(markerId)
                    },
                    modifier = fillMaxWidthModifier
                )
            } else {
                CompositionLocalProvider(
                    LocalContentColor provides
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ) {
                    Text(
                        text = stringResource(R.string.nothing_found),
                        modifier = Modifier.padding(DEFAULT_PADDING * 2)
                    )
                }
            }
        }
    }
}

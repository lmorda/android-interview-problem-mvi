package com.lmorda.homework.ui.theme

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.lmorda.homework.ui.shared.PAGING_ITEM_BUFFER
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun PaginationEffect(
    listState: LazyListState,
    buffer: Int = PAGING_ITEM_BUFFER,
    scrollToLastVisibleItemOnLoadMore: Boolean = false,
    onLoadMore: () -> Unit,
) {
    require(buffer >= 0) { "Paging buffer must be non-negative." }

    val shouldLoadMore = remember {
        derivedStateOf { listState.shouldLoadMore(buffer) }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collectLatest {
                onLoadMore()
                if (buffer == 0 && scrollToLastVisibleItemOnLoadMore) {
                    listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                }
            }
    }
}

private fun LazyListState.shouldLoadMore(buffer: Int): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return true
    return if (buffer > 0) {
        lastVisibleItem.index >= layoutInfo.totalItemsCount - 1 - buffer
    } else {
        lastVisibleItem.index == layoutInfo.totalItemsCount - 1
    }
}

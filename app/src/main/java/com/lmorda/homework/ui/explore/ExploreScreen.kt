package com.lmorda.homework.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lmorda.homework.R
import com.lmorda.homework.domain.model.GithubRepo
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.explore.ExploreContract.Event
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnLoadNextPage
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnRefresh
import com.lmorda.homework.ui.explore.ExploreContract.State
import com.lmorda.homework.ui.shared.HomeworkBeginSearch
import com.lmorda.homework.ui.shared.HomeworkLoadingError
import com.lmorda.homework.ui.shared.HomeworkProgressIndicator
import com.lmorda.homework.ui.shared.RepositoryStats
import com.lmorda.homework.ui.theme.DayAndNightPreview
import com.lmorda.homework.ui.theme.HomeworkTheme
import com.lmorda.homework.ui.theme.PaginationEffect
import com.lmorda.homework.ui.theme.sizeDefault
import com.lmorda.homework.ui.theme.sizeLarge
import com.lmorda.homework.ui.theme.sizeMedium
import com.lmorda.homework.ui.theme.sizeSmall

@Composable
fun ExploreScreenRoute(
    viewModel: ExploreViewModel,
    onNavigateToDetails: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    ExploreScreen(
        state = state,
        push = viewModel::push,
        onNavigateToDetails = onNavigateToDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreScreen(
    state: State,
    push: (Event) -> Unit,
    onNavigateToDetails: (Long) -> Unit,
) {
    val listState = rememberLazyListState()
    val isFiltering = rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                ExploreTopBar(
                    isFiltering = isFiltering,
                    push = push,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = sizeSmall),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                )
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state is State.LoadingRefresh,
            onRefresh = { push(OnRefresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ExploreContent(
                state = state,
                listState = listState,
                onNavigateToDetails = onNavigateToDetails,
                push = push,
            )
        }
    }
}

@Composable
private fun ExploreContent(
    state: State,
    listState: LazyListState,
    onNavigateToDetails: (Long) -> Unit,
    push: (Event) -> Unit
) {
    when (state) {
        is State.Initial -> HomeworkBeginSearch()
        is State.LoadingRefresh -> HomeworkProgressIndicator()
        is State.LoadingPage -> {
            ExploreList(
                listState = listState,
                githubRepos = state.githubRepos,
                isLoadingNextPage = true,
                onNavigateToDetails = onNavigateToDetails,
            )
        }

        is State.Loaded -> when {
            state.githubRepos.isEmpty() -> HomeworkLoadingError(stringResId = R.string.list_empty)
            else -> {
                ExploreList(
                    listState = listState,
                    githubRepos = state.githubRepos,
                    isLoadingNextPage = false,
                    onNavigateToDetails = onNavigateToDetails,
                )
                PaginationEffect(
                    listState = listState,
                    onLoadMore = { push(OnLoadNextPage) },
                )
            }
        }

        is State.LoadError -> HomeworkLoadingError(stringResId = R.string.list_error)
    }
}

@Composable
private fun ExploreList(
    listState: LazyListState,
    githubRepos: List<GithubRepo>,
    isLoadingNextPage: Boolean,
    onNavigateToDetails: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = sizeDefault),
        state = listState,
    ) {
        items(githubRepos) { githubRepo ->
            ExploreItem(
                githubRepo = githubRepo,
                onNavigateToDetails = onNavigateToDetails,
            )
        }
        if (isLoadingNextPage) {
            item { ExploreNextPageIndicator() }
        }
    }

}

@Composable
private fun ExploreItem(githubRepo: GithubRepo, onNavigateToDetails: (Long) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .padding(all = sizeDefault)
            .clickable {
                keyboardController?.hide()
                focusManager.clearFocus()
                onNavigateToDetails(githubRepo.id)
            },
    ) {
        ExploreItemTitle(githubRepo = githubRepo)

        Text(
            modifier = Modifier.padding(top = sizeSmall),
            text = githubRepo.description.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        RepositoryStats(githubRepo)
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}

@Composable
private fun ExploreItemTitle(githubRepo: GithubRepo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                modifier = Modifier
                    .size(size = sizeLarge)
                    .clip(shape = CircleShape),
                model = githubRepo.owner.avatarUrl,
                placeholder = painterResource(id = R.mipmap.ic_launcher_foreground),
                error = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(R.string.accessibility_owner_image),
            )
            Text(
                modifier = Modifier.padding(start = sizeMedium),
                text = githubRepo.owner.login,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        }
        Text(
            modifier = Modifier.padding(top = sizeSmall),
            text = githubRepo.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ExploreNextPageIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(sizeMedium),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(dimensionResource(R.dimen.explore_next_page_indicator_size)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
@DayAndNightPreview
private fun ExploreScreenPreview() {
    HomeworkTheme {
        ExploreScreen(
            state = State.Loaded(
                githubRepos = mockDomainData,
                nextPage = null,
                query = null,
            ),
            push = {},
            onNavigateToDetails = {},
        )
    }
}

@Composable
@DayAndNightPreview
private fun ExploreItemPreview() {
    HomeworkTheme {
        ExploreItem(
            githubRepo = mockDomainData[0],
            onNavigateToDetails = {},
        )
    }
}

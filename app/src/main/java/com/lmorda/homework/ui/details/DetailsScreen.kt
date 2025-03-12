package com.lmorda.homework.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.lmorda.homework.R
import com.lmorda.homework.domain.model.GithubRepo
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.details.DetailsContract.Event.OnLoadDetails
import com.lmorda.homework.ui.details.DetailsContract.State
import com.lmorda.homework.ui.shared.HomeworkLoadingError
import com.lmorda.homework.ui.shared.HomeworkProgressIndicator
import com.lmorda.homework.ui.shared.RepositoryStats
import com.lmorda.homework.ui.theme.DayAndNightPreview
import com.lmorda.homework.ui.theme.HomeworkTheme
import com.lmorda.homework.ui.theme.sizeDefault
import com.lmorda.homework.ui.theme.sizeXLarge
import com.lmorda.homework.ui.theme.topAppBarColors

@Composable
fun DetailsScreenRoute(
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.push(OnLoadDetails)
    }
    val state = requireNotNull(viewModel.state.observeAsState().value)
    DetailsScreen(
        state = state,
        onBack = onBack,
    )
}

@Composable
internal fun DetailsScreen(
    state: State,
    onBack: () -> Unit,
) = when (state) {
    State.Initial -> {}
    is State.Loading -> DetailsScaffold(
        screenContent = {
            HomeworkProgressIndicator()
        },
        onBack = onBack,
    )

    is State.Loaded -> DetailsScaffold(
        titleContent = {
            Text(
                text = state.githubRepo.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        screenContent = {
            DetailsContent(details = state.githubRepo)
        },
        onBack = onBack,
    )

    is State.LoadError -> DetailsScaffold(
        onBack = onBack,
        screenContent = {
            HomeworkLoadingError(stringResId = R.string.details_error)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsScaffold(
    titleContent: @Composable () -> Unit = {},
    screenContent: @Composable () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(),
                title = titleContent,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            modifier = Modifier.size(sizeXLarge),
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(R.string.accessibility_back),
                        )
                    }
                },
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = contentPadding)
                .fillMaxSize(),
        ) {
            screenContent()
        }
    }
}

@Composable
private fun DetailsContent(details: GithubRepo) {
    Column(modifier = Modifier.padding(sizeDefault)) {
        AsyncImage(
            modifier = Modifier
                .align(CenterHorizontally)
                .size(dimensionResource(id = R.dimen.details_image_size)),
            model = details.owner.avatarUrl,
            placeholder = painterResource(id = R.mipmap.ic_launcher_foreground),
            error = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "avatar",
        )
        Text(
            text = details.owner.login,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Text(
            text = details.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Text(
            modifier = Modifier.padding(top = sizeDefault),
            text = details.description.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        RepositoryStats(details = details)
    }
}

@DayAndNightPreview
@Composable
private fun DetailsLoadedScreenNoMapPreview() {
    HomeworkTheme {
        DetailsScreen(
            state = State.Loaded(
                githubRepo = mockDomainData[0],
            ),
            onBack = {},
        )
    }
}

@DayAndNightPreview
@Composable
private fun DetailsErrorScreenPreview() {
    HomeworkTheme {
        DetailsScreen(
            state = State.LoadError(errorMessage = null),
            onBack = {},
        )
    }
}

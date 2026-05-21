---
name: android-interview-problem-mvi
description: Use when working in /Users/loumorda/Documents/android-interview-problem-mvi or creating similar Android/Kotlin code that follows this repo's single-module Clean Architecture, MVI ViewModel, Jetpack Compose, Hilt, Retrofit, and MockK testing patterns.
---

# Android Interview Problem MVI Skill

Use this skill for `/Users/loumorda/Documents/android-interview-problem-mvi` and for small Android/Kotlin apps meant to match its style.

## Architecture

- Keep the project single-module unless the app grows beyond interview/homework size.
- Preserve package boundaries:
  - `ui`: Compose screens, contracts, MVI base class, navigation, shared UI.
  - `domain`: repository interfaces and pure domain models.
  - `data`: Retrofit APIs, DTOs, mappers, repository implementations, Hilt data modules.
  - `coroutines`: dispatcher abstractions and DI bindings.
- Use Clean Architecture direction: UI depends on domain interfaces; data implements domain interfaces; DTOs never leak into UI.
- Feature UI uses a `FeatureContract` file with nested sealed `State` and `Event`.
- ViewModels extend `MviViewModel<State, Event>`, expose `state: StateFlow<State>`, and accept events through `push(event)`.
- Reducers are the only place where UI events become new states. Async work launched from reducers pushes internal events back into the same reducer path.

Representative contract:

```kotlin
interface FeatureContract {
    sealed class State {
        data object Initial : State()
        data object Loading : State()
        data class Loaded(val item: GithubRepo) : State()
        data class LoadError(val errorMessage: String?) : State()
    }

    sealed class Event {
        data object OnLoad : Event()

        sealed class Internal : Event() {
            data class OnLoaded(val item: GithubRepo) : Internal()
            data class OnLoadError(val errorMessage: String?) : Internal()
        }
    }
}
```

Representative ViewModel shape:

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    dispatcherProvider: DispatcherProvider,
) : MviViewModel<State, Event>(
    initialState = State.Initial,
    eventsDispatcher = dispatcherProvider.events(),
) {
    override fun reduce(state: State, event: Event): State = when (event) {
        is OnLoad -> {
            loadItem()
            State.Loading
        }
        is OnLoaded -> State.Loaded(item = event.item)
        is OnLoadError -> State.LoadError(errorMessage = event.errorMessage)
    }

    private fun loadItem() {
        viewModelScope.launch {
            try {
                push(OnLoaded(item = dataRepository.getRepo(id = 123L)))
            } catch (e: Exception) {
                push(OnLoadError(errorMessage = e.message))
            }
        }
    }
}
```

## Kotlin Style

- Use Kotlin DSL Gradle files, Java/Kotlin 21, and `allWarningsAsErrors.set(true)`.
- Prefer immutable `data class` models with `val` properties.
- Use `data object` for singleton states/events.
- Name user-facing events with `On...`: `OnRefresh`, `OnLoadNextPage`, `OnSearchName`.
- Put reducer result events under `Event.Internal`: `OnLoaded`, `OnLoadError`, `OnRateLimitReached`.
- Use top-level constants for routes, API constants, saved-state keys, debounce durations, and pagination settings.
- Use constructor injection for mappers, repositories, ViewModels, and dispatcher providers.
- Keep DTOs in `data.model`, annotate with `@Serializable` and `@SerialName`, and map them explicitly to domain models.
- Use `require(...)` for programmer/input invariants, such as non-blank repository queries or non-negative paging buffers.
- Log and rethrow API exceptions in `safeApiCall`; let ViewModels translate exceptions into UI states.
- Preserve cancellation when catching broad exceptions in coroutine search/debounce flows.

```kotlin
private fun Exception.toLoadErrorEvent(): Event.Internal = if (
    this is HttpException && code() == HTTP_FORBIDDEN
) {
    OnRateLimitReached
} else {
    OnLoadError
}
```

## Coroutine And Flow Patterns

- Use `DispatcherProvider` instead of hardcoding dispatchers in ViewModels.
- Production events use `Dispatchers.Main.immediate`; tests use `UnconfinedTestDispatcher`.
- `MviViewModel` owns a `Channel<Event>(Channel.UNLIMITED)`, consumes it as a flow, and applies `scan(initialState) { state, event -> reduce(state, event) }`.
- Launch network work in `viewModelScope`; when it completes, call `push(InternalEvent)`.
- For debounced search, store the active `Job`, cancel it before starting a new search, delay by the shared debounce constant, then call the repository.
- For Compose scroll/paging effects, use `snapshotFlow`, `distinctUntilChanged`, and `collectLatest`.

## Compose Patterns

- Split each feature into a route composable and an internal stateless screen:
  - `FeatureScreenRoute(viewModel, navigationCallbacks)` collects state and passes `viewModel::push`.
  - `internal fun FeatureScreen(state, push, callbacks)` renders state and is used by tests/previews.
- Screens should switch exhaustively on sealed `State`.
- Keep reusable state-specific UI in small private composables.
- Keep UI event handling thin: composables call `push(Event.OnSomething)` or navigation callbacks; business logic stays in the ViewModel.
- Use `Scaffold` with Material 3 top bars for screens.
- Use `stringResource`, `dimensionResource`, theme spacing constants, and `MaterialTheme` colors/typography.
- Use Coil `AsyncImage` for remote images with launcher placeholder/error assets.
- Use `DayAndNightPreview` and wrap previews in `HomeworkTheme`.
- Prefer internal/private composables unless they are route entry points or shared UI.

Representative route/screen split:

```kotlin
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

@Composable
internal fun ExploreScreen(
    state: State,
    push: (Event) -> Unit,
    onNavigateToDetails: (Long) -> Unit,
) {
    when (state) {
        is State.Initial -> HomeworkBeginSearch()
        is State.Loaded -> ExploreList(
            githubRepos = state.githubRepos,
            onNavigateToDetails = onNavigateToDetails,
        )
        is State.LoadError -> HomeworkLoadingError(stringResId = R.string.list_error)
        else -> HomeworkProgressIndicator()
    }
}
```

## Dependency Injection

- Use Hilt throughout:
  - `@HiltAndroidApp` on `HomeworkApplication`.
  - `@AndroidEntryPoint` on `MainActivity`.
  - `@HiltViewModel` plus `@Inject constructor` on ViewModels.
- Use `@Binds` modules for interfaces implemented by project classes:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindsDataRepository(
        dataRepositoryImpl: DataRepositoryImpl,
    ): DataRepository
}
```

- Use `@Provides` modules for third-party object construction such as `OkHttpClient`, `Retrofit`, and `ApiService`.
- Keep repository interfaces in `domain`; implementations and API services belong in `data`.
- Keep dispatcher abstractions injectable so ViewModel tests can control coroutine execution.

## Testing

- Unit tests use JUnit 4, MockK, `kotlinx.coroutines.test`, and `InstantTaskExecutorRule`.
- ViewModel tests instantiate the ViewModel directly with a mocked `DataRepository` and `TestDispatcherProvider`.
- Use backtick test names for ViewModel/repository behavior, e.g. ``fun `search loads first page with query after debounce`()``.
- Use `runTest(testDispatcherRule.dispatcher)` for ViewModel tests and call `advanceUntilIdle()` after debounced or launched work.
- Assert exact sealed state values with `assertEquals`.
- Verify repository/API interactions with `coVerify(exactly = n)`.
- Use `CompletableDeferred` when a test needs to assert an intermediate loading state before completing async work.
- Repository tests mock `ApiService`, use real mappers, and verify API parameters/constants.
- Compose tests instantiate internal stateless screens directly, call `waitForIdle()`, and assert text/content descriptions/click callbacks.

Representative ViewModel test:

```kotlin
@Test
fun `search loads first page with query after debounce`() = runTest(testDispatcherRule.dispatcher) {
    val query = "compose"
    coEvery { repository.searchRepos(page = 1, query = query) } returns mockDomainData
    viewModel = ExploreViewModel(
        dataRepository = repository,
        dispatcherProvider = TestDispatcherProvider(),
    )

    viewModel.push(OnSearchName(query))
    advanceUntilIdle()

    assertEquals(
        State.Loaded(
            githubRepos = mockDomainData,
            nextPage = 2,
            searchQuery = query,
        ),
        viewModel.state.value,
    )
}
```

Representative Compose test:

```kotlin
composeTestRule.setContent {
    ExploreScreen(
        state = State.Loaded(
            githubRepos = mockDomainData,
            nextPage = null,
            searchQuery = "android",
        ),
        push = {},
        onNavigateToDetails = { navigatedId = it },
    )
}
composeTestRule.waitForIdle()
composeTestRule.onNodeWithText("my-application-1").performClick()
assertEquals(0L, navigatedId)
```

## Do

- Add new features by copying the contract/ViewModel/route/screen/test structure.
- Keep reducer states explicit and immutable.
- Push internal events for async success/failure instead of mutating state directly inside async callbacks.
- Keep UI code stateless enough that Compose tests can render it without Hilt or Navigation.
- Use domain mock data for previews and UI tests.
- Add tests for initial state, loading transitions, success, empty results, error states, and interaction callbacks.

## Don't

- Do not expose DTOs or Retrofit models to UI.
- Do not put repository implementations in `domain`.
- Do not inject concrete repositories into ViewModels when a domain interface exists.
- Do not perform business logic inside composables.
- Do not bypass `push(event)` for user actions.
- Do not hardcode dispatchers in ViewModels.
- Do not add broad mutable UI state to ViewModels when it can be represented as sealed `State`.
- Do not use Paging 3 unless explicitly requested; this repo intentionally uses custom paging.

## Verification

Before finishing code changes, run the narrowest relevant checks:

```bash
./gradlew :app:testDebugUnitTest
```

For UI changes, also run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Use `./gradlew :app:assembleDebug` when the user needs an APK or installable build.

/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.feature.search

import androidx.lifecycle.SavedStateHandle
import com.google.samples.apps.nowinandroid.core.analytics.NoOpAnalyticsHelper
import com.google.samples.apps.nowinandroid.core.domain.GetRecentSearchQueriesUseCase
import com.google.samples.apps.nowinandroid.core.domain.GetSearchContentsUseCase
import com.google.samples.apps.nowinandroid.core.testing.data.newsResourcesTestData
import com.google.samples.apps.nowinandroid.core.testing.data.topicsTestData
import com.google.samples.apps.nowinandroid.core.testing.repository.TestRecentSearchRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestSearchContentsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestUserDataRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.emptyUserData
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import com.google.samples.apps.nowinandroid.feature.search.RecentSearchQueriesUiState.Success
import com.google.samples.apps.nowinandroid.feature.search.SearchResultUiState.EmptyQuery
import com.google.samples.apps.nowinandroid.feature.search.SearchResultUiState.Loading
import com.google.samples.apps.nowinandroid.feature.search.SearchResultUiState.SearchNotReady
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

//Tools Used:
//Junit, Coroutines Testing, Analytics Helper, SavedStateHandle, Use Cases

class RamySearchViewmodelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule() // Sets up a rule to handle the coroutine dispatcher for tests.

    private val userDataRepository = TestUserDataRepository() // Creates a test repository for user data.
    private val searchContentsRepository = TestSearchContentsRepository() // Creates a test repository for search contents.
    private val getSearchContentsUseCase = GetSearchContentsUseCase( // Initializes the use case for fetching search contents.
        searchContentsRepository = searchContentsRepository,
        userDataRepository = userDataRepository,
    )
    private val recentSearchRepository = TestRecentSearchRepository() // Creates a test repository for recent searches.
    private val getRecentQueryUseCase = GetRecentSearchQueriesUseCase(recentSearchRepository) // Initializes the use case for fetching recent search queries.

    private lateinit var viewModel: SearchViewModel // ViewModel under test.

    @Before
    fun setup() {
        viewModel = SearchViewModel( // Initializes the ViewModel with necessary dependencies.
            getSearchContentsUseCase = getSearchContentsUseCase,
            recentSearchQueriesUseCase = getRecentQueryUseCase,
            searchContentsRepository = searchContentsRepository,
            savedStateHandle = SavedStateHandle(), // Provides an empty SavedStateHandle for the ViewModel.
            recentSearchRepository = recentSearchRepository,
            userDataRepository = userDataRepository,
            analyticsHelper = NoOpAnalyticsHelper(), // Provides a no-op implementation of analytics helper.
        )
        userDataRepository.setUserData(emptyUserData) // Sets up initial user data for the repository.
    }

    // Test 1: Ensures the initial state of the search results UI is Loading
    // Explanation:
    // This test verifies that the initial state of the search results UI is Loading.
    // It is important to confirm that the UI starts in the correct loading state before any data is loaded.
    // It follows the pattern of validating initial UI states to ensure the UI behaves correctly at the start.
    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(Loading, viewModel.searchResultUiState.value) // Asserts that the initial UI state is Loading.
    }

    // Test 2: Ensures the UI state is EmptyQuery when the search query is empty
    // Explanation:
    // This test verifies that when the search query is empty, the UI state transitions to EmptyQuery.
    // It is important to ensure that the UI correctly reflects an empty query state.
    // It follows the pattern of testing how the UI handles empty or invalid input.
    @Test
    fun stateIsEmptyQuery_withEmptySearchQuery() = runTest {
        searchContentsRepository.addNewsResources(newsResourcesTestData) // Adds test news resources to the repository.
        searchContentsRepository.addTopics(topicsTestData) // Adds test topics to the repository.
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() } // Launches a coroutine to collect search result UI state.

        viewModel.onSearchQueryChanged("") // Simulates an empty search query.

        assertEquals(EmptyQuery, viewModel.searchResultUiState.value) // Asserts that the UI state is EmptyQuery when the search query is empty.

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }

    // Test 3: Ensures that an empty result is returned when the query does not match any data
    // Explanation:
    // This test verifies that when the search query does not match any data, the UI correctly reflects an empty result.
    // It is important to validate that the UI handles non-matching queries appropriately.
    // It follows the pattern of testing how the UI behaves when no results are found.
    @Test
    fun emptyResultIsReturned_withNotMatchingQuery() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() } // Launches a coroutine to collect search result UI state.

        viewModel.onSearchQueryChanged("XXX") // Simulates a search query that doesn't match any results.
        searchContentsRepository.addNewsResources(newsResourcesTestData) // Adds test news resources to the repository.
        searchContentsRepository.addTopics(topicsTestData) // Adds test topics to the repository.

        val result = viewModel.searchResultUiState.value // Retrieves the current UI state.
        assertIs<SearchResultUiState.Success>(result) // Verifies that the result is of type SearchResultUiState.Success.

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }

    // Test 4: Ensures that recent searches UI state is Success after a search query is triggered
    // Explanation:
    // This test verifies that triggering a search query updates the recent searches UI state to Success.
    // It is important to ensure that recent searches are correctly handled and displayed in the UI.
    // It follows the pattern of validating that user interactions correctly update the UI state.
    @Test
    fun recentSearches_verifyUiStateIsSuccess() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.recentSearchQueriesUiState.collect() } // Launches a coroutine to collect recent search queries UI state.
        viewModel.onSearchTriggered("kotlin") // Simulates a search trigger with a specific query.

        val result = viewModel.recentSearchQueriesUiState.value // Retrieves the current UI state.
        assertIs<Success>(result) // Verifies that the result is of type Success.

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }

    // Test 5: Ensures that the UI state is SearchNotReady when no FTS table entity is available
    // Explanation:
    // This test verifies that when the FTS table entity is not ready, the UI state reflects SearchNotReady.
    // It is important to validate that the UI handles cases where essential data is missing or not yet available.
    // It follows the pattern of checking UI behavior when critical data is not ready.
    @Test
    fun searchNotReady_withNoFtsTableEntity() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() } // Launches a coroutine to collect search result UI state.

        viewModel.onSearchQueryChanged("") // Simulates an empty search query.

        assertEquals(SearchNotReady, viewModel.searchResultUiState.value) // Asserts that the UI state is SearchNotReady when no FTS table entity is available.

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }

    // Test 6: Ensures the bookmark state is updated correctly when a news resource is toggled
    // Explanation:
    // This test verifies that when the bookmark state of a news resource is toggled, the user's bookmarked resources are updated correctly.
    // It is important to validate that user interactions with bookmarks are reflected in the user data.
    // It follows the pattern of testing state changes in response to user actions.
    @Test
    fun whenToggleNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = "123" // Defines a test news resource ID.
        viewModel.setNewsResourceBookmarked(newsResourceId, true) // Simulates bookmarking a news resource.

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedNewsResources, // Verifies that the news resource ID is added to the bookmarked resources.
        )

        viewModel.setNewsResourceBookmarked(newsResourceId, false) // Simulates removing the bookmark from the news resource.

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedNewsResources, // Verifies that the news resource ID is removed from the bookmarked resources.
        )
    }
}
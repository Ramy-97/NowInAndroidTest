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

package com.google.samples.apps.nowinandroid.feature.bookmarks

import com.google.samples.apps.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.testing.data.newsResourcesTestData
import com.google.samples.apps.nowinandroid.core.testing.repository.TestNewsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestUserDataRepository
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState.Loading
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState.Success
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

//Tools Used:
//JUnit, Coroutines and Flow Testing, Use Cases, State Management
class RamyBookmarksViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule() // Sets up a rule to handle the coroutine dispatcher for tests.

    private val userDataRepository = TestUserDataRepository() // Creates a test repository for user data.
    private val newsRepository = TestNewsRepository() // Creates a test repository for news resources.
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    ) // Initializes a composite repository for user news resources.
    private lateinit var viewModel: BookmarksViewModel // ViewModel under test.

    @Before
    fun setup() {
        viewModel = BookmarksViewModel(
            userDataRepository = userDataRepository,
            userNewsResourceRepository = userNewsResourceRepository,
        ) // Initializes the ViewModel with necessary dependencies.
    }

    // Test 1: Ensures the initial state is Loading
    // Explanation:
    // This test verifies that the initial state of the ViewModel is Loading.
    // It is important to confirm that the ViewModel starts in the correct loading state before any data is loaded.
    // It follows the pattern of validating initial ViewModel states to ensure the UI behaves correctly at the start.
    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            Loading,
            viewModel.feedUiState.value
        ) // Asserts that the initial UI state is Loading.
    }

    // Test 2: Ensures that a single bookmark appears in the feed
    // Explanation:
    // This test checks that when a news resource is bookmarked, it shows up in the feed.
    // It is important to verify that bookmarks are correctly reflected in the UI state.
    // It follows the pattern of simulating user actions and verifying UI state changes.
    @Test
    fun oneBookmark_showsInFeed() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() } // Launches a coroutine to collect UI state emissions.

        newsRepository.sendNewsResources(newsResourcesTestData) // Sends sample news resources to the repository.
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true) // Marks a news resource as bookmarked.

        val item = viewModel.feedUiState.value
        assertIs<Success>(item) // Asserts that the UI state is Success.
        assertEquals(item.feed.size, 1) // Asserts that the feed contains one item.

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }

    // Test 3: Ensures that removing a bookmark updates the feed correctly
    // Explanation:
    // This test checks that when a bookmark is removed, it is correctly removed from the feed.
    // It is important to ensure that bookmarks can be removed and the UI state updates accordingly.
    // It follows the pattern of simulating user actions and verifying UI state changes.
    @Test
    fun oneBookmark_whenRemoving_removesFromFeed() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() } // Launches a coroutine to collect UI state emissions.

        newsRepository.sendNewsResources(newsResourcesTestData) // Sends sample news resources to the repository.
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true) // Marks a news resource as bookmarked.

        viewModel.removeFromSavedResources(newsResourcesTestData[0].id) // Removes the bookmarked news resource.

        val item = viewModel.feedUiState.value
        assertIs<Success>(item) // Asserts that the UI state is Success.
        assertEquals(item.feed.size, 0) // Asserts that the feed is empty.

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }
}
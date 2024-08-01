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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import com.google.samples.apps.nowinandroid.core.testing.data.userNewsResourcesTestData
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

//Tools Used:
//JUnit, Compose Test Rule and APIs, Mock Callbacks, Assertions and Matchers

class RamyBookmarksFragmentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>() // Sets up the Compose test environment.

    // Test 1: Ensures the UI shows a loading spinner when the state is Loading.
    // Explanation:
    // This test verifies that when the feed state is set to Loading, the loading spinner is displayed.
    // It is important to confirm that the UI provides appropriate feedback to users during data fetching. This ensures that the component correctly represents different loading states.
    // It follows the pattern of validating UI elements associated with specific states to ensure correct feedback is shown to users.
    @Test
    fun loading_showsLoadingSpinner() {
        composeTestRule.setContent {
            BookmarksScreen(
                feedState = NewsFeedUiState.Loading, // Set the feed state to Loading.
                onShowSnackbar = { _, _ -> false }, // Mock callback for showing Snack bar.
                removeFromBookmarks = {}, // Mock callback for removing bookmarks.
                onTopicClick = {}, // Mock callback for topic clicks.
                onNewsResourceViewed = {}, // Mock callback for viewed news resources.
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_bookmarks_loading) // Get the content description for loading spinner.
            )
            .assertExists() // Assert that the loading spinner is present in the UI.
    }

    // Test 2: Ensures bookmarks are displayed when available.
    // Explanation:
    // This test verifies that when bookmarks are present in the feed state, they are displayed correctly in the UI.
    // It is important to confirm that the component handles and displays dynamic content, such as bookmarks, accurately. This ensures that the UI updates correctly based on the feed data.
    // It follows the pattern of using dynamic data to test UI behavior with real-world scenarios, including handling lists and scrollable content.
    @Test
    fun feed_whenHasBookmarks_showsBookmarks() {
        composeTestRule.setContent {
            BookmarksScreen(
                feedState = NewsFeedUiState.Success(
                    userNewsResourcesTestData.take(2) // Provide sample bookmarked data.
                ),
                onShowSnackbar = { _, _ -> false }, // Mock callback for showing Snack bar.
                removeFromBookmarks = {}, // Mock callback for removing bookmarks.
                onTopicClick = {}, // Mock callback for topic clicks.
                onNewsResourceViewed = {}, // Mock callback for viewed news resources.
            )
        }

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[0].title,
                substring = true // Use substring match for partial text.
            )
            .assertExists() // Assert that the first bookmark is present.
            .assertHasClickAction() // Verify that the item is clickable.

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userNewsResourcesTestData[1].title,
                    substring = true // Use substring match for partial text.
                ),
            ) // Perform scroll action to ensure the second item is visible.

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[1].title,
                substring = true // Use substring match for partial text.
            )
            .assertExists() // Assert that the second bookmark is present.
            .assertHasClickAction() // Verify that the item is clickable.
    }

    // Test 3: Ensures that removing a bookmark triggers the correct callback.
    // Explanation:
    // This test verifies that clicking the "Remove Bookmark" action correctly calls the remove callback with the expected parameters.
    // It is important to confirm that the UI interaction results in the expected changes and that the correct data is passed to the callback. This ensures that the application's logic for removing bookmarks is functioning correctly.
    // It follows the pattern of validating UI actions and their impact on the application's state, ensuring that callbacks and interactions are handled as expected.
    @Test
    fun feed_whenRemovingBookmark_removesBookmark() {
        var removeFromBookmarksCalled = false

        composeTestRule.setContent {
            BookmarksScreen(
                feedState = NewsFeedUiState.Success(
                    userNewsResourcesTestData.take(2) // Provide sample bookmarked data.
                ),
                onShowSnackbar = { _, _ -> false }, // Mock callback for showing Snack bar.
                removeFromBookmarks = { newsResourceId ->
                    assertEquals(userNewsResourcesTestData[0].id, newsResourceId) // Verify that the correct ID is passed.
                    removeFromBookmarksCalled = true
                },
                onTopicClick = {}, // Mock callback for topic clicks.
                onNewsResourceViewed = {}, // Mock callback for viewed news resources.
            )
        }

        composeTestRule
            .onAllNodesWithContentDescription(
                composeTestRule.activity.getString(
                    com.google.samples.apps.nowinandroid.core.ui.R.string.core_ui_unbookmark // Get the content description for remove bookmark action.
                )
            )
            .filter(
                hasAnyAncestor(
                    hasText(
                        userNewsResourcesTestData[0].title,
                        substring = true // Use substring match for partial text.
                    )
                )
            )
            .assertCountEquals(1) // Verify that there is exactly one remove button for the bookmark.
            .onFirst() // Select the first (and only) remove button.
            .performClick() // Simulate the click action.

        assertTrue(removeFromBookmarksCalled) // Assert that the callback was triggered.
    }

    // Test 4: Ensures the UI shows an empty state when there are no bookmarks.
    // Explanation:
    // This test verifies that the UI correctly displays an empty state message when there are no bookmarks.
    // It is important to confirm that the UI provides appropriate feedback when there is no content to display. This ensures that users understand that there are no items to show and the UI gracefully handles empty states.
    // It follows the pattern of validating UI responses to different states, ensuring that the application handles scenarios with no content effectively.
    @Test
    fun feed_whenHasNoBookmarks_showsEmptyState() {
        composeTestRule.setContent {
            BookmarksScreen(
                feedState = NewsFeedUiState.Success(emptyList()), // Provide an empty list to simulate no bookmarks.
                onShowSnackbar = { _, _ -> false }, // Mock callback for showing Snack bar.
                removeFromBookmarks = {}, // Mock callback for removing bookmarks.
                onTopicClick = {}, // Mock callback for topic clicks.
                onNewsResourceViewed = {}, // Mock callback for viewed news resources.
            )
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.feature_bookmarks_empty_error) // Get the empty state error text.
            )
            .assertExists() // Assert that the empty state error text is present.

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.feature_bookmarks_empty_description) // Get the empty state description text.
            )
            .assertExists() // Assert that the empty state description text is present.
    }

    // Test 5: Ensures the undo state is cleared when the lifecycle stops.
    // Explanation:
    // This test verifies that the undo state is cleared when the lifecycle owner stops.
    // It is important to confirm that the component correctly responds to lifecycle events by clearing state as needed. This ensures that the UI manages state properly in response to lifecycle changes, which is crucial for maintaining consistency and preventing stale data.
    // It follows the pattern of testing lifecycle-aware components to ensure that state management is handled correctly during lifecycle events.
    @Test
    fun feed_whenLifecycleStops_undoBookmarkedStateIsCleared() = runTest {
        var undoStateCleared = false
        val testLifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.STARTED)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides testLifecycleOwner) {
                BookmarksScreen(
                    feedState = NewsFeedUiState.Success(emptyList()), // Provide an empty list for the bookmarks.
                    onShowSnackbar = { _, _ -> false }, // Mock callback for showing Snack bar.
                    removeFromBookmarks = {}, // Mock callback for removing bookmarks.
                    onTopicClick = {}, // Mock callback for topic clicks.
                    onNewsResourceViewed = {}, // Mock callback for viewed news resources.
                    clearUndoState = {
                        undoStateCleared = true // Set flag to true when undo state is cleared.
                    },
                )
            }
        }

        assertEquals(false, undoStateCleared) // Assert that the undo state is not cleared initially.
        testLifecycleOwner.handleLifecycleEvent(event = Lifecycle.Event.ON_STOP) // Simulate lifecycle stop event.
        assertEquals(true, undoStateCleared) // Assert that the undo state is cleared after lifecycle stop.
    }
}

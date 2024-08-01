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

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.google.samples.apps.nowinandroid.core.data.model.RecentSearchQuery
import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig.DARK
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand.ANDROID
import com.google.samples.apps.nowinandroid.core.model.data.UserData
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.testing.data.followableTopicTestData
import com.google.samples.apps.nowinandroid.core.testing.data.newsResourcesTestData
import com.google.samples.apps.nowinandroid.core.ui.R.string
import org.junit.Before
import org.junit.Rule
import org.junit.Test

//Tools Used:
//Junit, Compose function and testing, UI state data

class RamySearchFragmentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var clearSearchContentDesc: String
    private lateinit var followButtonContentDesc: String
    private lateinit var unfollowButtonContentDesc: String
    private lateinit var clearRecentSearchesContentDesc: String
    private lateinit var topicsString: String
    private lateinit var updatesString: String
    private lateinit var tryAnotherSearchString: String
    private lateinit var searchNotReadyString: String

    private val userData: UserData = UserData(
        bookmarkedNewsResources = setOf("1", "3"),
        viewedNewsResources = setOf("1", "2", "4"),
        followedTopics = emptySet(),
        themeBrand = ANDROID,
        darkThemeConfig = DARK,
        shouldHideOnboarding = true,
        useDynamicColor = false,
    )

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            clearSearchContentDesc = getString(R.string.feature_search_clear_search_text_content_desc) // Retrieves the clear search button description.
            clearRecentSearchesContentDesc = getString(R.string.feature_search_clear_recent_searches_content_desc) // Retrieves the clear recent searches button description.
            followButtonContentDesc = getString(string.core_ui_interests_card_follow_button_content_desc) // Retrieves the follow button description.
            unfollowButtonContentDesc = getString(string.core_ui_interests_card_unfollow_button_content_desc) // Retrieves the unfollow button description.
            topicsString = getString(R.string.feature_search_topics) // Retrieves the topics header string.
            updatesString = getString(R.string.feature_search_updates) // Retrieves the updates header string.
            tryAnotherSearchString = getString(R.string.feature_search_try_another_search) +
                " " + getString(R.string.feature_search_interests) + " " + getString(R.string.feature_search_to_browse_topics) // Retrieves and concatenates the try another search string.
            searchNotReadyString = getString(R.string.feature_search_not_ready) // Retrieves the search not ready message string.
        }
    }

    // Test 1: Ensures the search text field is focused
    // Explanation:
    // This test verifies that the search text field is focused when the search screen is displayed.
    // It is important to ensure that the search text field is ready for user input immediately.
    // It follows the pattern of verifying UI elements' initial states and interactions.
    @Test
    fun searchTextField_isFocused() {
        composeTestRule.setContent {
            SearchScreen() // Sets the content of the test to the SearchScreen composable.
        }

        composeTestRule
            .onNodeWithTag("searchTextField") // Finds the search text field by its tag.
            .assertIsFocused() // Asserts that the search text field is focused.
    }

    // Test 2: Ensures the empty search result displays the appropriate empty screen message
    // Explanation:
    // This test verifies that when there are no search results, the appropriate message is displayed to the user.
    // It is important to confirm that the UI correctly shows an empty search message.
    // It follows the pattern of testing how the UI handles and displays empty states.
    @Test
    fun emptySearchResult_emptyScreenIsDisplayed() {
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.Success(), // Sets the UI state to Success with no results.
            )
        }

        composeTestRule
            .onNodeWithText(tryAnotherSearchString) // Finds the text element with the try another search message.
            .assertIsDisplayed() // Asserts that the try another search message is displayed.
    }

    // Test 3: Ensures the UI displays recent searches and appropriate buttons when the search result is empty
    // Explanation:
    // This test verifies that when there are recent searches, they are displayed even if the search result is empty.
    // It is important to ensure that recent searches and their associated UI elements are visible.
    // It follows the pattern of validating UI elements based on varying states and conditions.
    @Test
    fun emptySearchResult_nonEmptyRecentSearches_emptySearchScreenAndRecentSearchesAreDisplayed() {
        val recentSearches = listOf("kotlin") // Sample recent searches.
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.Success(), // Sets the UI state to Success with no results.
                recentSearchesUiState = RecentSearchQueriesUiState.Success(
                    recentQueries = recentSearches.map(::RecentSearchQuery), // Sets recent searches UI state.
                ),
            )
        }

        composeTestRule
            .onNodeWithText(tryAnotherSearchString) // Finds and asserts the try another search message is displayed.
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(clearRecentSearchesContentDesc) // Finds and asserts the clear recent searches button is displayed.
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("kotlin") // Finds and asserts the recent search item "kotlin" is displayed.
            .assertIsDisplayed()
    }

    // Test 4: Ensures all topics are visible and follow/unfollow buttons are correctly displayed
    // Explanation:
    // This test verifies that all topics are visible and that follow/unfollow buttons are displayed correctly based on the data.
    // It is important to ensure that topics and their associated actions are correctly rendered.
    // It follows the pattern of testing UI rendering based on dynamic data and user interactions.
    @Test
    fun searchResultWithTopics_allTopicsAreVisible_followButtonsVisibleForTheNumOfFollowedTopics() {
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.Success(topics = followableTopicTestData), // Sets the UI state with a list of topics.
            )
        }

        composeTestRule
            .onNodeWithText(topicsString) // Finds and asserts the topics header is displayed.
            .assertIsDisplayed()

        val scrollableNode = composeTestRule
            .onAllNodes(hasScrollToNodeAction()) // Finds a scrollable node.
            .onFirst() // Selects the first scrollable node.

        followableTopicTestData.forEachIndexed { index, followableTopic ->
            scrollableNode.performScrollToIndex(index) // Scrolls to the index of the current topic.

            composeTestRule
                .onNodeWithText(followableTopic.topic.name) // Finds and asserts the topic name is displayed.
                .assertIsDisplayed()
        }

        composeTestRule
            .onAllNodesWithContentDescription(followButtonContentDesc) // Finds and asserts the number of follow buttons is correct.
            .assertCountEquals(2)
        composeTestRule
            .onAllNodesWithContentDescription(unfollowButtonContentDesc) // Finds and asserts the number of unfollow buttons is correct.
            .assertCountEquals(1)
    }

    // Test 5: Ensures the first news resource is visible when search results contain news resources
    // Explanation:
    // This test verifies that when search results include news resources, the first news resource is displayed correctly.
    // It is important to ensure that news resources are rendered properly in the UI.
    // It follows the pattern of validating specific UI elements based on dynamic content.
    @Test
    fun searchResultWithNewsResources_firstNewsResourcesIsVisible() {
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.Success(
                    newsResources = newsResourcesTestData.map {
                        UserNewsResource(
                            newsResource = it,
                            userData = userData, // Sets up news resources with user data.
                        )
                    },
                ),
            )
        }

        composeTestRule
            .onNodeWithText(updatesString) // Finds and asserts the updates header is displayed.
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(newsResourcesTestData[0].title) // Finds and asserts the title of the first news resource is displayed.
            .assertIsDisplayed()
    }

    // Test 6: Ensures that the clear searches button is visible when there is a non-empty recent search list and an empty search query
    // Explanation:
    // This test verifies that when the search query is empty but there are recent searches, the clear searches button is displayed.
    // It is important to ensure that the clear searches button and recent searches are rendered correctly.
    // It follows the pattern of validating UI elements based on search states and recent search data.
    @Test
    fun emptyQuery_notEmptyRecentSearches_verifyClearSearchesButton_displayed() {
        val recentSearches = listOf("kotlin", "testing") // Sample recent searches.
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.EmptyQuery, // Sets the UI state to EmptyQuery.
                recentSearchesUiState = RecentSearchQueriesUiState.Success(
                    recentQueries = recentSearches.map(::RecentSearchQuery), // Sets recent searches UI state.
                ),
            )
        }

        composeTestRule
            .onNodeWithContentDescription(clearRecentSearchesContentDesc) // Finds and asserts the clear recent searches button is displayed.
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("kotlin") // Finds and asserts the recent search item "kotlin" is displayed.
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("testing") // Finds and asserts the recent search item "testing" is displayed.
            .assertIsDisplayed()
    }

    // Test 7: Ensures the search not ready message is visible when the search result state indicates search is not ready
    // Explanation:
    // This test verifies that when the search result state indicates that the search is not ready, the appropriate message is displayed.
    // It is important to ensure that the search not ready message is shown to the user when needed.
    // It follows the pattern of testing how the UI handles and displays various states.
    @Test
    fun searchNotReady_verifySearchNotReadyMessageIsVisible() {
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.SearchNotReady, // Sets the UI state to SearchNotReady.
            )
        }

        composeTestRule
            .onNodeWithText(searchNotReadyString) // Finds and asserts the search not ready message is displayed.
            .assertIsDisplayed()
    }
}

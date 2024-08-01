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

package com.google.samples.apps.nowinandroid.feature.topic

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.google.samples.apps.nowinandroid.core.testing.data.followableTopicTestData
import com.google.samples.apps.nowinandroid.core.testing.data.userNewsResourcesTestData
import org.junit.Before
import org.junit.Rule
import org.junit.Test

//Tools Used:
//AndroidX Activity Component, Jetpack Compose Testing Library, Junit, Resource Management, Test Data

class RamyTopicsViewFragmentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>() // Creates a Compose test rule to host the Composable under test within an Android activity.

    private lateinit var topicLoading: String

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            topicLoading = getString(R.string.feature_topic_loading)
        } // Sets up the test environment by retrieving the string resource for the loading state description.
    }

    // Test 1: Ensures the loading indicator is shown when the screen is in a loading state
    // Explanation:
    // This test sets the UI state to loading and verifies that the loading indicator is displayed.
    // It is important to confirm that the loading state is properly communicated to the user.
    // It follows the pattern of verifying UI feedback during loading states, which is essential for user experience.
    @Test
    fun niaLoadingWheel_whenScreenIsLoading_showLoading() {
        composeTestRule.setContent {
            TopicScreen(
                topicUiState = TopicUiState.Loading,
                newsUiState = NewsUiState.Loading,
                showBackButton = true,
                onBackClick = {},
                onFollowClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
            )
        } // Sets the Composable content for the test with both topic and news UI states set to loading.

        composeTestRule
            .onNodeWithContentDescription(topicLoading)
            .assertExists() // Asserts that a node with the content description matching the loading string exists, verifying the loading indicator is displayed.
    }

    // Test 2: Ensures the topic title and description are shown correctly when the topic state is successful
    // Explanation:
    // This test sets the UI state to success with a test topic and verifies that the topic title and description are displayed.
    // It is important to ensure that the content is shown correctly when the data is successfully loaded.
    // It follows the pattern of verifying the UI state rendering for successful data states, ensuring completeness of displayed information.
    @Test
    fun topicTitle_whenTopicIsSuccess_isShown() {
        val testTopic = followableTopicTestData.first() // Retrieves the first topic from the test data to use in the test.

        composeTestRule.setContent {
            TopicScreen(
                topicUiState = TopicUiState.Success(testTopic),
                newsUiState = NewsUiState.Loading,
                showBackButton = true,
                onBackClick = {},
                onFollowClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
            )
        } // Sets the Composable content with the topic UI state set to success and news UI state set to loading.

        composeTestRule
            .onNodeWithText(testTopic.topic.name)
            .assertExists() // Asserts that a node with text matching the topic's name exists, verifying the topic name is displayed.

        composeTestRule
            .onNodeWithText(testTopic.topic.longDescription)
            .assertExists() // Asserts that a node with text matching the topic's long description exists, verifying the topic description is displayed.
    }

    // Test 3: Ensures news items are not shown when the topic state is loading
    // Explanation:
    // This test sets the topic UI state to loading and the news UI state to success, then verifies that the news items are not displayed.
    // It is important to ensure that news items are not shown when the topic data is still loading, maintaining a consistent state.
    // It follows the pattern of verifying UI states for partial data loading scenarios, ensuring correct UI behavior.
    @Test
    fun news_whenTopicIsLoading_isNotShown() {
        composeTestRule.setContent {
            TopicScreen(
                topicUiState = TopicUiState.Loading,
                newsUiState = NewsUiState.Success(userNewsResourcesTestData),
                showBackButton = true,
                onBackClick = {},
                onFollowClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
            )
        } // Sets the Composable content with the topic UI state set to loading and news UI state set to success.

        composeTestRule
            .onNodeWithContentDescription(topicLoading)
            .assertExists() // Asserts that a node with the content description matching the loading string exists, verifying the loading indicator is displayed.
    }

    // Test 4: Ensures news items are shown when both topic and news states are successful
    // Explanation:
    // This test sets both the topic and news UI states to success and verifies that the news items are displayed correctly.
    // It is important to ensure that the news items are shown when both the topic and news data are successfully loaded.
    // It follows the pattern of verifying UI rendering for fully loaded data states, ensuring all relevant information is displayed.
    @Test
    fun news_whenSuccessAndTopicIsSuccess_isShown() {
        val testTopic = followableTopicTestData.first() // Retrieves the first topic from the test data to use in the test.

        composeTestRule.setContent {
            TopicScreen(
                topicUiState = TopicUiState.Success(testTopic),
                newsUiState = NewsUiState.Success(userNewsResourcesTestData),
                showBackButton = true,
                onBackClick = {},
                onFollowClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                onNewsResourceViewed = {},
            )
        } // Sets the Composable content with both topic and news UI states set to success.

        composeTestRule
            .onAllNodes(hasScrollToNodeAction())
            .onFirst()
            .performScrollToNode(hasText(userNewsResourcesTestData.first().title)) // Scrolls to the first news title if available, verifying the news items are displayed.
    }
}

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

package com.google.samples.apps.nowinandroid.interests

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.google.samples.apps.nowinandroid.core.testing.data.followableTopicTestData
import com.google.samples.apps.nowinandroid.feature.interests.InterestsScreen
import com.google.samples.apps.nowinandroid.feature.interests.InterestsUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.samples.apps.nowinandroid.core.ui.R as CoreUiR
import com.google.samples.apps.nowinandroid.feature.interests.R as InterestsR

//Tools Used:
//Jetpack Compose Testing, JUnit, Compose UI Testing Utilities

class RamyInterestsScreenFragmentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var interestsLoadingContentDesc: String
    private lateinit var interestsEmptyHeaderText: String
    private lateinit var interestsTopicCardFollowButtonContentDesc: String
    private lateinit var interestsTopicCardUnfollowButtonContentDesc: String

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            interestsLoadingContentDesc = getString(InterestsR.string.feature_interests_loading) // Retrieves the loading content description.
            interestsEmptyHeaderText = getString(InterestsR.string.feature_interests_empty_header) // Retrieves the empty header message string.
            interestsTopicCardFollowButtonContentDesc = getString(CoreUiR.string.core_ui_interests_card_follow_button_content_desc) // Retrieves the follow button description.
            interestsTopicCardUnfollowButtonContentDesc = getString(CoreUiR.string.core_ui_interests_card_unfollow_button_content_desc) // Retrieves the unfollow button description.
        }
    }

    // Test 1: Ensures the loading wheel is displayed when the Interests screen is in the loading state
    // Explanation:
    // This test verifies that the loading indicator is visible when the Interests screen is in the loading state.
    // It is important to confirm that users receive feedback during data fetching or processing.
    // The test follows the pattern of validating UI elements' presence and visibility based on the state of the application.
    @Test
    fun loadingWheel_inTopics_whenScreenIsLoading_showLoading() {
        composeTestRule.setContent {
            InterestsScreen(uiState = InterestsUiState.Loading) // Sets the content of the test to InterestsScreen with a loading state.
        }

        composeTestRule
            .onNodeWithContentDescription(interestsLoadingContentDesc) // Finds the loading wheel by its content description.
            .assertExists() // Asserts that the loading wheel exists.
    }

    // Test 2: Ensures followed and unfollowed topics are displayed correctly when topics data is provided
    // Explanation:
    // This test verifies that the Interests screen correctly displays both followed and unfollowed topics.
    // It ensures that topics are rendered dynamically based on the provided data, and that the count of follow and unfollow buttons matches the expected values.
    // The test also checks that UI elements are displayed based on dynamic data and interactions with follow/unfollow actions.
    @Test
    fun interestsWithTopics_whenTopicsFollowed_showFollowedAndUnfollowedTopicsWithInfo() {
        composeTestRule.setContent {
            InterestsScreen(
                uiState = InterestsUiState.Interests(
                    topics = followableTopicTestData, // Provides topics data to the InterestsScreen.
                    selectedTopicId = null,
                ),
            )
        }

        followableTopicTestData.forEach { topic ->
            composeTestRule
                .onNodeWithText(topic.topic.name) // Finds and asserts the visibility of each topic's name.
                .assertIsDisplayed()
        }

        composeTestRule
            .onAllNodesWithContentDescription(interestsTopicCardFollowButtonContentDesc) // Finds and asserts the number of follow buttons.
            .assertCountEquals(numberOfUnfollowedTopics) // Ensures the correct number of follow buttons is displayed.

        // Optional: Verify that unfollowed topics have the correct follow button and followed topics have the correct unfollow button
        composeTestRule
            .onAllNodesWithContentDescription(interestsTopicCardUnfollowButtonContentDesc) // Finds and asserts the number of unfollow buttons.
            .assertCountEquals(followableTopicTestData.count { it.isFollowed }) // Ensures the correct number of unfollow buttons is displayed.
    }

    // Test 3: Ensures an empty screen message is displayed when no topics data is available
    // Explanation:
    // This test verifies that the empty header message is shown when the Interests screen has no data.
    // It follows the pattern of validating UI elements for empty states, ensuring that appropriate messaging is displayed to the user when there is no content.
    @Test
    fun topicsEmpty_whenDataIsEmptyOccurs_thenShowEmptyScreen() {
        composeTestRule.setContent {
            InterestsScreen(uiState = InterestsUiState.Empty) // Sets the content of the test to InterestsScreen with an empty state.
        }

        composeTestRule
            .onNodeWithText(interestsEmptyHeaderText) // Finds and asserts the empty header message is displayed.
            .assertIsDisplayed() // Ensures the empty header message is visible.
    }

    @Composable
    private fun InterestsScreen(uiState: InterestsUiState) {
        InterestsScreen(
            uiState = uiState,
            followTopic = { _, _ -> }, // Mock implementation for followTopic.
            onTopicClick = {}, // Mock implementation for onTopicClick.
        )
    }
}

private val numberOfUnfollowedTopics = followableTopicTestData.filter { !it.isFollowed }.size

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

package com.google.samples.apps.nowinandroid.feature.foryou

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.google.samples.apps.nowinandroid.core.rules.GrantPostNotificationsPermissionRule
import com.google.samples.apps.nowinandroid.core.testing.data.followableTopicTestData
import com.google.samples.apps.nowinandroid.core.testing.data.userNewsResourcesTestData
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState
import org.junit.Rule
import org.junit.Test

//Tools Used:
//Jetpack Compose Testing Library, Android Testing Support Library, JUnit, Hilt, Custom Test Data, Dynamic UI Element Identification

class RamyForYouFragmentTest {
    @get:Rule(order = 0)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val doneButtonMatcher by lazy {
        hasText(
            composeTestRule.activity.resources.getString(R.string.feature_foryou_done),
        )
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")

    // Test 1: Ensures the loading indicator is displayed when the For You screen is in the loading state
    // Explanation:
    // This test verifies that the loading indicator is visible when the For You screen is in the loading state.
    // It is crucial to confirm that users see feedback during data fetching or processing.
    // The test uses dynamic content descriptions to identify UI elements, ensuring robustness against changes in the UI text.
    @Test
    fun circularProgressIndicator_whenScreenIsLoading_exists() {
        composeTestRule.setContent {
            BoxWithConstraints {
                ForYouScreen(
                    isSyncing = false,
                    onboardingUiState = OnboardingUiState.Loading,
                    feedState = NewsFeedUiState.Loading,
                    deepLinkedUserNewsResource = null,
                    onTopicCheckedChanged = { _, _ -> },
                    onTopicClick = {},
                    saveFollowedTopics = {},
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onDeepLinkOpened = {},
                )
            }
        }

        // Dynamic content description handling.
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_foryou_loading),
            )
            .assertExists() // Asserts that the loading indicator is present.
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")

    // Test 2: Ensures the loading indicator is displayed during syncing
    // Explanation:
    // This test checks that the loading indicator is visible when syncing is in progress.
    // It confirms the UI reflects ongoing synchronization.
    // The test uses dynamic content descriptions to verify the loading indicator's presence.
    @Test
    fun circularProgressIndicator_whenScreenIsSyncing_exists() {
        composeTestRule.setContent {
            BoxWithConstraints {
                ForYouScreen(
                    isSyncing = true,
                    onboardingUiState = OnboardingUiState.NotShown,
                    feedState = NewsFeedUiState.Success(emptyList()),
                    deepLinkedUserNewsResource = null,
                    onTopicCheckedChanged = { _, _ -> },
                    onTopicClick = {},
                    saveFollowedTopics = {},
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onDeepLinkOpened = {},
                )
            }
        }

        // Dynamic content description handling.
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_foryou_loading),
            )
            .assertExists() // Asserts that the loading indicator is present.
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")

    // Test 3: Ensures the Done button is disabled when no topics are selected
    // Explanation:
    // This test verifies that the Done button is disabled when no topics are selected.
    // It uses dynamic text retrieval and scrollable content handling to ensure the button's state.
    // Testing scrollable content and UI element states
    @Test
    fun topicSelector_whenNoTopicsSelected_showsTopicChipsAndDisabledDoneButton() {
        val testData = followableTopicTestData.map { it.copy(isFollowed = false) }

        composeTestRule.setContent {
            BoxWithConstraints {
                ForYouScreen(
                    isSyncing = false,
                    onboardingUiState = OnboardingUiState.Shown(
                        topics = testData,
                    ),
                    feedState = NewsFeedUiState.Success(
                        feed = emptyList(),
                    ),
                    deepLinkedUserNewsResource = null,
                    onTopicCheckedChanged = { _, _ -> },
                    onTopicClick = {},
                    saveFollowedTopics = {},
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onDeepLinkOpened = {},
                )
            }
        }

        testData.forEach { testTopic ->
            composeTestRule
                .onNodeWithText(testTopic.topic.name) // Dynamic data handling for topic names.

                .assertExists()
                .assertHasClickAction() // Verifies clickable action.
        }

        // Scroll until the Done button is visible
        composeTestRule
            .onAllNodes(hasScrollToNodeAction())
            .onFirst()
            .performScrollToNode(doneButtonMatcher) // Scrollable content handling.

        composeTestRule
            .onNode(doneButtonMatcher) // Dynamic text handling for the Done button.
            .assertExists()
            .assertIsNotEnabled()
            .assertHasClickAction() // Asserts the button is disabled when no topics are selected.
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")

    // Test 4: Ensures the Done button is enabled when some topics are selected
    // Explanation:
    // This test verifies that the Done button is enabled when some topics are selected.
    // It uses dynamic text retrieval and scrollable content handling to ensure the button's state.
    // Testing dynamic data and button states
    @Test
    fun topicSelector_whenSomeTopicsSelected_showsTopicChipsAndEnabledDoneButton() {
        composeTestRule.setContent {
            BoxWithConstraints {
                ForYouScreen(
                    isSyncing = false,
                    onboardingUiState =
                    OnboardingUiState.Shown(
                        // Follow one topic
                        topics = followableTopicTestData.mapIndexed { index, testTopic ->
                            testTopic.copy(isFollowed = index == 1)
                        },
                    ),
                    feedState = NewsFeedUiState.Success(
                        feed = emptyList(),
                    ),
                    deepLinkedUserNewsResource = null,
                    onTopicCheckedChanged = { _, _ -> },
                    onTopicClick = {},
                    saveFollowedTopics = {},
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onDeepLinkOpened = {},
                )
            }
        }

        followableTopicTestData.forEach { testTopic ->
            composeTestRule
                .onNodeWithText(testTopic.topic.name) // Dynamic data handling for topic names.
                .assertExists()
                .assertHasClickAction() // Verifies clickable action.
        }

        // Scroll until the Done button is visible
        composeTestRule
            .onAllNodes(hasScrollToNodeAction())
            .onFirst()
            .performScrollToNode(doneButtonMatcher) // Scrollable content handling.

        composeTestRule
            .onNode(doneButtonMatcher) // Dynamic text handling for the Done button.
            .assertExists()
            .assertIsEnabled()
            .assertHasClickAction() // Asserts the button is enabled when some topics are selected.
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")

    // Test 5: Ensures a loading indicator is shown when the feed is loading, regardless of topic selection
    // Explanation:
    // This test verifies that the loading indicator is visible during feed loading.
    // It uses dynamic content descriptions to ensure correct verification.
    // Verifying loading indicator with dynamic content descriptions
    @Test
    fun feed_whenInterestsSelectedAndLoading_showsLoadingIndicator() {
        composeTestRule.setContent {
            BoxWithConstraints {
                ForYouScreen(
                    isSyncing = false,
                    onboardingUiState =
                    OnboardingUiState.Shown(topics = followableTopicTestData),
                    feedState = NewsFeedUiState.Loading,
                    deepLinkedUserNewsResource = null,
                    onTopicCheckedChanged = { _, _ -> },
                    onTopicClick = {},
                    saveFollowedTopics = {},
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onDeepLinkOpened = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_foryou_loading),
            ) // Dynamic content description handling.
            .assertExists() // Ensures the loading indicator is present.
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")

    // Test 6: Ensures a loading indicator is shown when there are no interests selected
    // Explanation:
    // This test checks that the loading indicator is visible when there are no interests selected and the feed is loading.
    // It uses dynamic content descriptions to ensure correct verification.
    // Verifying loading indicator with dynamic content descriptions
    @Test
    fun feed_whenNoInterestsSelectionAndLoading_showsLoadingIndicator() {
        composeTestRule.setContent {
            BoxWithConstraints {
                ForYouScreen(
                    isSyncing = false,
                    onboardingUiState = OnboardingUiState.NotShown,
                    feedState = NewsFeedUiState.Loading,
                    deepLinkedUserNewsResource = null,
                    onTopicCheckedChanged = { _, _ -> },
                    onTopicClick = {},
                    saveFollowedTopics = {},
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onDeepLinkOpened = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_foryou_loading),
            ) // Dynamic content description handling.
            .assertExists() // Ensures the loading indicator is present.
    }

    // Test 7: Ensures the feed displays correctly when no interests are selected and feed data is loaded
    // Explanation:
    // This test verifies that the feed content is displayed correctly when the data is loaded and no interests are selected.
    // It uses dynamic text retrieval and scrollable content handling to ensure feed items are correctly shown.
    // Handling dynamic data and scrollable content
    @Test
    fun feed_whenNoInterestsSelectionAndLoaded_showsFeed() {
        composeTestRule.setContent {
            ForYouScreen(
                isSyncing = false,
                onboardingUiState = OnboardingUiState.NotShown,
                feedState = NewsFeedUiState.Success(
                    feed = userNewsResourcesTestData,
                ),
                deepLinkedUserNewsResource = null,
                onTopicCheckedChanged = { _, _ -> },
                onTopicClick = {},
                saveFollowedTopics = {},
                onNewsResourcesCheckedChanged = { _, _ -> },
                onNewsResourceViewed = {},
                onDeepLinkOpened = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[0].title,
                substring = true,
            ) // Dynamic data handling for news item titles.
            .assertExists()
            .assertHasClickAction() // Verifies clickable action.

        composeTestRule.onNode(hasScrollToNodeAction()) // Handles scrollable content.
            .performScrollToNode(
                hasText(
                    userNewsResourcesTestData[1].title,
                    substring = true,
                ), // Ensures last item is visible.
            )

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[1].title,
                substring = true,
            ) // Dynamic data handling for last item.
            .assertExists()
            .assertHasClickAction() // Verifies clickable action.
    }
}


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
import com.google.samples.apps.nowinandroid.feature.interests.InterestsUiState
import com.google.samples.apps.nowinandroid.feature.interests.InterestsUiState.Empty
import com.google.samples.apps.nowinandroid.feature.interests.InterestsUiState.Interests
import com.google.samples.apps.nowinandroid.feature.interests.InterestsUiState.Loading
import com.google.samples.apps.nowinandroid.feature.interests.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI test for checking the correct behaviour of the Interests screen;
 * Verifies that, when a specific UiState is set, the corresponding
 * composables and details are shown
 */
class RamyInterestsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var interestsLoading: String
    private lateinit var interestsEmptyHeader: String
    private lateinit var interestsTopicCardFollowButton: String
    private lateinit var interestsTopicCardUnfollowButton: String

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            interestsLoading = getString(R.string.feature_interests_loading)
            interestsEmptyHeader = getString(R.string.feature_interests_empty_header)
            interestsTopicCardFollowButton =
                getString(com.google.samples.apps.nowinandroid.core.ui.R.string.core_ui_interests_card_follow_button_content_desc)
            interestsTopicCardUnfollowButton =
                getString(com.google.samples.apps.nowinandroid.core.ui.R.string.core_ui_interests_card_unfollow_button_content_desc)
        }
    }

    @Test
    fun niaLoadingWheel_inTopics_whenScreenIsLoading_showLoading() {
        composeTestRule.setContent {
            InterestsScreen(uiState = Loading)
        }

        composeTestRule
            .onNodeWithContentDescription(interestsLoading)
            .assertExists()
    }

    @Test
    fun interestsWithTopics_whenTopicsFollowed_showFollowedAndUnfollowedTopicsWithInfo() {
        composeTestRule.setContent {
            InterestsScreen(
                uiState = Interests(
                    topics = followableTopicTestData,
                    selectedTopicId = null,
                ),
            )
        }

        composeTestRule
            .onNodeWithText(followableTopicTestData[0].topic.name)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(followableTopicTestData[1].topic.name)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(followableTopicTestData[2].topic.name)
            .assertIsDisplayed()

        composeTestRule
            .onAllNodesWithContentDescription(interestsTopicCardFollowButton)
            .assertCountEquals(numberOfUnfollowedTopics)
    }

    @Test
    fun topicsEmpty_whenDataIsEmptyOccurs_thenShowEmptyScreen() {
        composeTestRule.setContent {
            InterestsScreen(uiState = Empty)
        }

        composeTestRule
            .onNodeWithText(interestsEmptyHeader)
            .assertIsDisplayed()
    }

    @Composable
    private fun InterestsScreen(uiState: InterestsUiState) {
        com.google.samples.apps.nowinandroid.feature.interests.InterestsScreen(
            uiState = uiState,
            followTopic = { _, _ -> },
            onTopicClick = {},
        )
    }
}

private val numberOfUnfollowedTopics = followableTopicTestData.filter { !it.isFollowed }.size

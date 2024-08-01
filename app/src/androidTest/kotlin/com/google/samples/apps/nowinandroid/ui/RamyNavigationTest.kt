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

package com.google.samples.apps.nowinandroid.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import com.google.samples.apps.nowinandroid.MainActivity
import com.google.samples.apps.nowinandroid.R
import com.google.samples.apps.nowinandroid.core.data.repository.TopicsRepository
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.rules.GrantPostNotificationsPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.inject.Inject
import com.google.samples.apps.nowinandroid.feature.bookmarks.R as BookmarksR
import com.google.samples.apps.nowinandroid.feature.foryou.R as FeatureForyouR
import com.google.samples.apps.nowinandroid.feature.search.R as FeatureSearchR
import com.google.samples.apps.nowinandroid.feature.settings.R as SettingsR

@HiltAndroidTest

//Tools Used:
//Junit, Hilt injection, Hilt Android Rule, Compose Test Rule and APIs
class RamyNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this) // Sets up dependency injection for tests.

    @BindValue
    @get:Rule(order = 1)
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build() // Provides a temporary folder for file operations.

    @get:Rule(order = 2)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule() // Grants permissions required for notifications.

    @get:Rule(order = 3)
    val composeTestRule = createAndroidComposeRule<MainActivity>() // Sets up the Compose test environment.

    @Inject
    lateinit var topicsRepository: TopicsRepository // Injects TopicsRepository for data access.

    // The strings used for matching in these tests
    private val navigateUp by composeTestRule.stringResource(FeatureForyouR.string.feature_foryou_navigate_up)
    private val forYou by composeTestRule.stringResource(FeatureForyouR.string.feature_foryou_title)
    private val interests by composeTestRule.stringResource(FeatureSearchR.string.feature_search_interests)
    private val sampleTopic = "Headlines"
    private val appName by composeTestRule.stringResource(R.string.app_name)
    private val saved by composeTestRule.stringResource(BookmarksR.string.feature_bookmarks_title)
    private val settings by composeTestRule.stringResource(SettingsR.string.feature_settings_top_app_bar_action_icon_description)
    private val brand by composeTestRule.stringResource(SettingsR.string.feature_settings_brand_android)
    private val ok by composeTestRule.stringResource(SettingsR.string.feature_settings_dismiss_dialog_button_text)

    @Before
    fun setup() = hiltRule.inject() // Inject dependencies before each test.

    // Test 1: Ensures the "For You" screen is initially selected.
    // Explanation:
    // This test verifies that the initial screen shown to the user is "For You".
    // It is important to confirm that the app starts with the correct screen to ensure a smooth user experience.
    // This test follows the pattern of verifying initial screen states to ensure the navigation setup is correct.
    @Test
    fun firstScreen_isForYou() {
        composeTestRule.apply {
            onNodeWithText(forYou).assertIsSelected() // Assert that "For You" is selected.
        }
    }

    // Test 2: Verifies that navigating away and back restores the content of the "For You" tab.
    // Explanation:
    // This test ensures that if a user navigates away from the "For You" tab and then returns, the state of "For You" is restored.
    // This is crucial for maintaining user context and ensuring that the UI accurately reflects previous interactions.
    // The pattern used here validates state restoration across navigation.
    @Test
    fun navigationBar_navigateToPreviouslySelectedTab_restoresContent() {
        composeTestRule.apply {
            // GIVEN the user follows a topic
            onNodeWithText(sampleTopic).performClick()
            // WHEN the user navigates to the Interests destination
            onNodeWithText(interests).performClick()
            // AND the user navigates to the For You destination
            onNodeWithText(forYou).performClick()
            // THEN the state of the For You destination is restored
            onNodeWithContentDescription(sampleTopic).assertIsOn()
        }
    }

    // Test 3: Ensures reelecting a tab keeps its state.
    // Explanation:
    // This test verifies that reelecting the "For You" tab retains its state.
    // This is important to ensure that user interactions do not unexpectedly reset or change the state of the selected tab.
    // The pattern used here ensures consistency of UI state when navigating between tabs.
    @Test
    fun navigationBar_reselectTab_keepsState() {
        composeTestRule.apply {
            // GIVEN the user follows a topic
            onNodeWithText(sampleTopic).performClick()
            // WHEN the user taps the For You navigation bar item
            onNodeWithText(forYou).performClick()
            // THEN the state of the For You destination is restored
            onNodeWithContentDescription(sampleTopic).assertIsOn()
        }
    }

    // Test 4: Verifies that the Up arrow is not shown on top-level destinations.
    // Explanation:
    // This test ensures that the Up arrow (indicating back navigation) is not present on top-level screens where it shouldn't be.
    // This is important for maintaining consistent navigation behavior and adhering to design guidelines.
    // The pattern checks for the presence or absence of navigation elements based on the screen context.
    @Test
    fun topLevelDestinations_doNotShowUpArrow() {
        composeTestRule.apply {
            // GIVEN the user is on any of the top-level destinations, THEN the Up arrow is not shown.
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()

            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()

            onNodeWithText(interests).performClick()
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()
        }
    }

    // Test 5: Ensures that the top bar displays the correct title on each top-level destination.
    // Explanation:
    // This test verifies that the top bar shows the expected title for each top-level destination, such as "Saved" or "Interests".
    // This is important for user orientation and ensuring that the top bar accurately reflects the current screen context.
    // The pattern validates UI content dynamically across different screens.
    @Test
    fun topLevelDestinations_showTopBarWithTitle() {
        composeTestRule.apply {
            // Verify that the top bar contains the app name on the first screen.
            onNodeWithText(appName).assertExists()

            // Go to the saved tab, verify that the top bar contains "saved".
            onNodeWithText(saved).performClick()
            onAllNodesWithText(saved).assertCountEquals(2)

            // As above but for the interests tab.
            onNodeWithText(interests).performClick()
            onAllNodesWithText(interests).assertCountEquals(2)
        }
    }

    // Test 6: Ensures that the settings icon is present on all top-level destinations.
    // Explanation:
    // This test verifies that the settings icon is visible on all major screens, allowing users to access settings from any top-level destination.
    // This is important for ensuring consistent access to app settings.
    // The pattern checks for the presence of UI elements across different contexts.
    @Test
    fun topLevelDestinations_showSettingsIcon() {
        composeTestRule.apply {
            onNodeWithContentDescription(settings).assertExists()

            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(settings).assertExists()

            onNodeWithText(interests).performClick()
            onNodeWithContentDescription(settings).assertExists()
        }
    }

    // Test 7: Verifies that clicking the settings icon displays the settings dialog.
    // Explanation:
    // This test ensures that clicking on the settings icon opens the settings dialog and displays the expected settings content.
    // This is crucial for verifying that the settings functionality works as intended and users can access settings options.
    // The pattern tests interaction with dialogs and verifies their content.
    @Test
    fun whenSettingsIconIsClicked_settingsDialogIsShown() {
        composeTestRule.apply {
            onNodeWithContentDescription(settings).performClick()

            // Check that one of the settings is actually displayed.
            onNodeWithText(brand).assertExists()
        }
    }

    // Test 8: Ensures that dismissing the settings dialog returns the user to the previous screen.
    // Explanation:
    // This test verifies that after closing the settings dialog, the app returns to the previously visible screen.
    // This is important for maintaining a seamless user experience and ensuring that navigation state is preserved.
    // The pattern checks the restoration of previous UI states after interacting with dialogs.
    @Test
    fun whenSettingsDialogDismissed_previousScreenIsDisplayed() {
        composeTestRule.apply {
            // Navigate to the saved screen, open the settings dialog, then close it.
            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(settings).performClick()
            onNodeWithText(ok).performClick()

            // Check that the saved screen is still visible and selected.
            onNode(hasText(saved) and hasTestTag("NiaNavItem")).assertIsSelected()
        }
    }

    // Test 9: Verifies that pressing the back button from the home destination quits the app.
    // Explanation:
    // This test checks that using the system back button or gesture on the home screen terminates the app, as expected behavior.
    // This is important for verifying that the app handles the back navigation correctly at the top level.
    // The pattern tests system-level navigation interactions and their impact on app state.
    @Test(expected = NoActivityResumedException::class)
    fun homeDestination_back_quitsApp() {
        composeTestRule.apply {
            // GIVEN the user navigates to the Interests destination
            onNodeWithText(interests).performClick()
            // and then navigates to the For You destination
            onNodeWithText(forYou).performClick()
            // WHEN the user uses the system button/gesture to go back
            Espresso.pressBack()
            // THEN the app quits
        }
    }

    // Test 10: Ensures that pressing the back button from any destination returns to the "For You" screen.
    // Explanation:
    // This test verifies that pressing the back button from any screen within the app returns the user to the "For You" screen.
    // This is important for ensuring consistent and predictable navigation behavior.
    // The pattern tests navigation behavior and verifies the destination screen.
    @Test
    fun navigationBar_backFromAnyDestination_returnsToForYou() {
        composeTestRule.apply {
            // GIVEN the user navigated to the Interests destination
            onNodeWithText(interests).performClick()
            // WHEN the user uses the system button/gesture to go back,
            Espresso.pressBack()
            // THEN the app shows the For You destination
            onNodeWithText(forYou).assertExists()
        }
    }

    // Test 11: Ensures that multiple back stack interactions in the Interests tab work as expected.
    // Explanation:
    // This test checks that navigating through topics and switching tabs preserves the topic state when returning to the Interests tab.
    // This is crucial for verifying that the back stack and navigation state are properly maintained.
    // The pattern tests dynamic data interactions and state preservation across multiple navigation.
    @Test
    fun navigationBar_multipleBackStackInterests() {
        composeTestRule.apply {
            onNodeWithText(interests).performClick()

            // Select the last topic
            val topic = runBlocking {
                topicsRepository.getTopics().first().sortedBy(Topic::name).last()
            }
            onNodeWithTag("interests:topics").performScrollToNode(hasText(topic.name))
            onNodeWithText(topic.name).performClick()

            // Switch tab
            onNodeWithText(forYou).performClick()

            // Come back to Interests
            onNodeWithText(interests).performClick()

            // Verify the topic is still shown
            onNodeWithTag("topic:${topic.id}").assertExists()
        }
    }
}
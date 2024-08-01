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

package com.google.samples.apps.nowinandroid.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig.DARK
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand.ANDROID
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand.DEFAULT
import com.google.samples.apps.nowinandroid.feature.settings.SettingsUiState.Loading
import com.google.samples.apps.nowinandroid.feature.settings.SettingsUiState.Success
import org.junit.Rule
import org.junit.Test

//Tools Used:
//JetPack Compose, Resource Management, Compose testing library

class RamySettingsFragmentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()  // Provides the Compose test environment.

    private fun getString(id: Int) = composeTestRule.activity.resources.getString(id)  // Retrieves string resources for UI assertions.

    // Test 1: Ensures that when the state is loading, the loading text is displayed
    // Explanation:
    // This test verifies that the UI correctly displays a loading indicator when the settings state is `Loading`.
    // It follows the pattern of testing initial UI states, ensuring the UI behaves correctly before any data is loaded.
    @Test
    fun whenLoading_showsLoadingText() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Loading,  // Sets the UI state to Loading.
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
            )
        }

        composeTestRule
            .onNodeWithText(getString(R.string.feature_settings_loading))  // Finds the UI element with the loading text.
            .assertExists()  // Asserts that the loading text is present in the UI.
    }

    // Test 2: Ensures that when the state is success, all default settings are displayed
    // Explanation:
    // This test verifies that when the settings state is `Success`, all expected settings options are displayed.
    // It ensures that the UI correctly reflects all available settings and their default selections.
    // This pattern tests the completeness and correctness of the UI content based on the successful state.
    @Test
    fun whenStateIsSuccess_allDefaultSettingsAreDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(
                    UserEditableSettings(
                        brand = ANDROID,  // Sets the brand to Android.
                        useDynamicColor = false,  // Sets dynamic color preference to false.
                        darkThemeConfig = DARK,  // Sets dark theme configuration.
                    ),
                ),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
            )
        }

        // Check that all the possible settings are displayed.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_brand_default)).assertExists()  // Asserts that default brand setting is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_brand_android)).assertExists()  // Asserts that Android brand setting is visible.
        composeTestRule.onNodeWithText(
            getString(R.string.feature_settings_dark_mode_config_system_default),
        ).assertExists()  // Asserts that system default dark mode setting is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_light)).assertExists()  // Asserts that light theme dark mode setting is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_dark)).assertExists()  // Asserts that dark theme dark mode setting is visible.

        // Check that the correct settings are selected.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_brand_android)).assertIsSelected()  // Asserts that Android brand setting is selected.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_dark)).assertIsSelected()  // Asserts that dark theme dark mode setting is selected.
    }

    // Test 3: Ensures that when dynamic color support is enabled, the dynamic color options are displayed
    // Explanation:
    // This test verifies that when dynamic color support is enabled, the corresponding options are shown.
    // It also ensures that the correct default dynamic color option is selected.
    // This pattern tests conditional UI elements based on feature flags or settings, checking both visibility and selection.
    @Test
    fun whenStateIsSuccess_supportsDynamicColor_usesDefaultBrand_DynamicColorOptionIsDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(
                    UserEditableSettings(
                        brand = DEFAULT,  // Sets the brand to the default value.
                        darkThemeConfig = DARK,  // Sets dark theme configuration.
                        useDynamicColor = false,  // Sets dynamic color preference to false.
                    ),
                ),
                supportDynamicColor = true,  // Enables dynamic color support.
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_preference)).assertExists()  // Asserts that dynamic color preference option is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_yes)).assertExists()  // Asserts that 'Yes' option for dynamic color is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_no)).assertExists()  // Asserts that 'No' option for dynamic color is visible.

        // Check that the correct default dynamic color setting is selected.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_no)).assertIsSelected()  // Asserts that 'No' option for dynamic color is selected.
    }

    // Test 4: Ensures that when dynamic color support is not enabled, the dynamic color options are not displayed
    // Explanation:
    // This test verifies that when dynamic color support is disabled, the dynamic color options are not shown.
    // It ensures that the UI does not display options that are irrelevant when dynamic color support is not available.
    // This pattern checks the UI's adaptability based on feature flags or settings.
    @Test
    fun whenStateIsSuccess_notSupportDynamicColor_DynamicColorOptionIsNotDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(
                    UserEditableSettings(
                        brand = ANDROID,  // Sets the brand to Android.
                        darkThemeConfig = DARK,  // Sets dark theme configuration.
                        useDynamicColor = false,  // Sets dynamic color preference to false.
                    ),
                ),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_preference))
            .assertDoesNotExist()  // Asserts that dynamic color preference option is not visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_yes)).assertDoesNotExist()  // Asserts that 'Yes' option for dynamic color is not visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_no)).assertDoesNotExist()  // Asserts that 'No' option for dynamic color is not visible.
    }

    // Test 5: Ensures that when the Android brand is used, dynamic color options are not displayed
    // Explanation:
    // This test verifies that when the Android brand is selected, dynamic color options are not shown.
    // It ensures that the UI correctly hides irrelevant options for the selected brand.
    // This pattern checks the UI's behavior when certain settings make other options irrelevant.
    @Test
    fun whenStateIsSuccess_usesAndroidBrand_DynamicColorOptionIsNotDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(
                    UserEditableSettings(
                        brand = ANDROID,  // Sets the brand to Android.
                        darkThemeConfig = DARK,  // Sets dark theme configuration.
                        useDynamicColor = false,  // Sets dynamic color preference to false.
                    ),
                ),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_preference))
            .assertDoesNotExist()  // Asserts that dynamic color preference option is not visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_yes)).assertDoesNotExist()  // Asserts that 'Yes' option for dynamic color is not visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dynamic_color_no)).assertDoesNotExist()  // Asserts that 'No' option for dynamic color is not visible.
    }

    // Test 6: Ensures that all relevant links are displayed in the settings dialog
    // Explanation:
    // This test verifies that all necessary links (e.g., privacy policy, licenses) are visible when the settings state is `Success`.
    // It ensures that important actions are accessible to the user from the settings UI.
    // This pattern validates that all required elements are present and accessible in the UI.
    @Test
    fun whenStateIsSuccess_allLinksAreDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(
                    UserEditableSettings(
                        brand = ANDROID,  // Sets the brand to Android.
                        darkThemeConfig = DARK,  // Sets dark theme configuration.
                        useDynamicColor = false,  // Sets dynamic color preference to false.
                    ),
                ),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_privacy_policy)).assertExists()  // Asserts that privacy policy link is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_licenses)).assertExists()  // Asserts that licenses link is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_brand_guidelines)).assertExists()  // Asserts that brand guidelines link is visible.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_feedback)).assertExists()  // Asserts that feedback link is visible.
    }
}
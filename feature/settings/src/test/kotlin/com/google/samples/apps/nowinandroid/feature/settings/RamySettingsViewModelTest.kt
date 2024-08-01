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

import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig.DARK
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand.ANDROID
import com.google.samples.apps.nowinandroid.core.testing.repository.TestUserDataRepository
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import com.google.samples.apps.nowinandroid.feature.settings.SettingsUiState.Loading
import com.google.samples.apps.nowinandroid.feature.settings.SettingsUiState.Success
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

//Tools Used:
//Junit, Jetpack, Kotlin. TestData, Compose UI state

class RamySettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Creates a rule to control the coroutine dispatcher for testing.

    private val userDataRepository = TestUserDataRepository() // Initializes a test repository for user data.

    private lateinit var viewModel: SettingsViewModel // Declares the ViewModel to be tested.

    @Before
    fun setup() {
        viewModel = SettingsViewModel(userDataRepository) // Sets up the ViewModel with the test repository before each test.
    }

    // Test 1: Ensures the initial state of the settings UI is Loading
    // Explanation:
    // This test verifies that the initial state of the settings UI is Loading.
    // It is important to confirm that the UI starts in the correct loading state before any data is loaded.
    // It follows the pattern of validating initial UI states to ensure the UI behaves correctly at the start.
    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(Loading, viewModel.settingsUiState.value) // Asserts that the initial UI state is Loading.
    }

    // Test 2: Ensures the settings UI state is Success after user data is loaded
    // Explanation:
    // This test verifies that the settings UI state transitions to Success after setting the theme brand and dark theme configuration.
    // It is important to ensure that the ViewModel correctly updates the UI state based on the loaded user data.
    // It follows the pattern of testing asynchronous data updates and their effect on the UI state.
    @Test
    fun stateIsSuccessAfterUserDataLoaded() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.settingsUiState.collect() } // Launches a coroutine to collect UI state emissions.

        userDataRepository.setThemeBrand(ANDROID) // Sets the theme brand in the test repository.
        userDataRepository.setDarkThemeConfig(DARK) // Sets the dark theme configuration in the test repository.

        assertEquals(
            Success(
                UserEditableSettings(
                    brand = ANDROID,
                    darkThemeConfig = DARK,
                    useDynamicColor = false,
                ),
            ),
            viewModel.settingsUiState.value, // Asserts that the UI state is Success with the correct user settings after data is loaded.
        )

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }
}
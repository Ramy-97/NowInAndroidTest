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

import androidx.lifecycle.SavedStateHandle
import com.google.samples.apps.nowinandroid.core.domain.GetFollowableTopicsUseCase
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.testing.repository.TestTopicsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestUserDataRepository
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import com.google.samples.apps.nowinandroid.feature.interests.InterestsUiState.Interests
import com.google.samples.apps.nowinandroid.feature.interests.InterestsUiState.Loading
import com.google.samples.apps.nowinandroid.feature.interests.InterestsViewModel
import com.google.samples.apps.nowinandroid.feature.interests.navigation.TOPIC_ID_ARG
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

//Tools Used:
//JUnit, Use Case, runTest, coroutine dispatcher, SavedStateHandle

class RamyInterestsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Sets up a rule to handle the coroutine dispatcher for tests.

    private val userDataRepository = TestUserDataRepository() // Creates a test repository for user data.
    private val topicsRepository = TestTopicsRepository() // Creates a test repository for topics.
    private val getFollowableTopicsUseCase = GetFollowableTopicsUseCase( // Initializes the use case for fetching followable topics.
        topicsRepository = topicsRepository,
        userDataRepository = userDataRepository,
    )
    private lateinit var viewModel: InterestsViewModel // ViewModel under test.

    @Before
    fun setup() {
        viewModel = InterestsViewModel( // Initializes the ViewModel with necessary dependencies.
            savedStateHandle = SavedStateHandle(mapOf(TOPIC_ID_ARG to testInputTopics[0].topic.id)), // Provides an initial topic ID for the ViewModel.
            userDataRepository = userDataRepository,
            getFollowableTopics = getFollowableTopicsUseCase,
        )
    }

    // Test 1: Ensures the initial state of the UI is Loading
    // Explanation:
    // This test verifies that the initial state of the Interests UI is Loading.
    // It is important to confirm that the UI starts in the correct loading state before any data is loaded.
    // It follows the pattern of validating initial UI states to ensure the UI behaves correctly at the start.
    @Test
    fun uiState_whenInitialized_thenShowLoading() = runTest {
        assertEquals(Loading, viewModel.uiState.value) // Asserts that the initial UI state is Loading.
    }

    // Test 2: Ensures the UI state is Loading when followed topics are being fetched
    // Explanation:
    // This test verifies that when the followed topics are being fetched, the UI state shows Loading.
    // It is important to ensure that the UI reflects the loading state while data is being retrieved.
    // It follows the pattern of validating UI behavior during data fetch operations.
    @Test
    fun uiState_whenFollowedTopicsAreLoading_thenShowLoading() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() } // Launches a coroutine to collect UI state.

        userDataRepository.setFollowedTopicIds(emptySet()) // Sets followed topic IDs to an empty set.
        assertEquals(Loading, viewModel.uiState.value) // Asserts that the UI state is Loading.

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }

    // Test 3: Ensures that the UI state updates correctly when following a new topic
    // Explanation:
    // This test verifies that when a new topic is followed, the UI state reflects the updated list of topics.
    // It is important to validate that the UI correctly updates to show the new followed state.
    // It follows the pattern of testing state changes in response to user interactions.
    @Test
    fun uiState_whenFollowingNewTopic_thenShowUpdatedTopics() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() } // Launches a coroutine to collect UI state.

        val toggleTopicId = testOutputTopics[1].topic.id // Defines a topic ID to be toggled.
        topicsRepository.sendTopics(testInputTopics.map { it.topic }) // Sends test topics to the repository.
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[0].topic.id)) // Sets initial followed topic IDs.

        assertEquals(
            false,
            (viewModel.uiState.value as Interests) // Asserts that the topic is initially not followed.
                .topics.first { it.topic.id == toggleTopicId }.isFollowed,
        )

        viewModel.followTopic(
            followedTopicId = toggleTopicId,
            true, // Simulates following the topic.
        )

        assertEquals(
            Interests(
                topics = testOutputTopics, // Asserts that the UI state reflects updated topics.
                selectedTopicId = testInputTopics[0].topic.id,
            ),
            viewModel.uiState.value,
        )

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }

    // Test 4: Ensures that the UI state updates correctly when unfollowing topics
    // Explanation:
    // This test verifies that when a topic is unfollowed, the UI state reflects the updated list of topics.
    // It is important to validate that the UI correctly updates to show the removed followed state.
    // It follows the pattern of testing state changes in response to user interactions.
    @Test
    fun uiState_whenUnfollowingTopics_thenShowUpdatedTopics() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() } // Launches a coroutine to collect UI state.

        val toggleTopicId = testOutputTopics[1].topic.id // Defines a topic ID to be toggled.
        topicsRepository.sendTopics(testOutputTopics.map { it.topic }) // Sends test topics to the repository.
        userDataRepository.setFollowedTopicIds(
            setOf(testOutputTopics[0].topic.id, testOutputTopics[1].topic.id),
        ) // Sets initial followed topic IDs.

        assertEquals(
            true,
            (viewModel.uiState.value as Interests) // Asserts that the topic is initially followed.
                .topics.first { it.topic.id == toggleTopicId }.isFollowed,
        )

        viewModel.followTopic(
            followedTopicId = toggleTopicId,
            false, // Simulates unfollowing the topic.
        )

        assertEquals(
            Interests(
                topics = testInputTopics, // Asserts that the UI state reflects updated topics.
                selectedTopicId = testInputTopics[0].topic.id,
            ),
            viewModel.uiState.value,
        )

        collectJob.cancel() // Cancels the coroutine collecting UI state emissions.
    }
}


private const val TOPIC_1_NAME = "Android Studio"
private const val TOPIC_2_NAME = "Build"
private const val TOPIC_3_NAME = "Compose"
private const val TOPIC_SHORT_DESC = "At vero eos et accuseds."
private const val TOPIC_LONG_DESC = "At vero eos et accuseds et gusto Podio pianissimos dulcimers."
private const val TOPIC_URL = "URL"
private const val TOPIC_IMAGE_URL = "Image URL"

private val testInputTopics = listOf(
    FollowableTopic(
        Topic(
            id = "0",
            name = TOPIC_1_NAME,
            shortDescription = TOPIC_SHORT_DESC,
            longDescription = TOPIC_LONG_DESC,
            url = TOPIC_URL,
            imageUrl = TOPIC_IMAGE_URL,
        ),
        isFollowed = true,
    ),
    FollowableTopic(
        Topic(
            id = "1",
            name = TOPIC_2_NAME,
            shortDescription = TOPIC_SHORT_DESC,
            longDescription = TOPIC_LONG_DESC,
            url = TOPIC_URL,
            imageUrl = TOPIC_IMAGE_URL,
        ),
        isFollowed = false,
    ),
    FollowableTopic(
        Topic(
            id = "2",
            name = TOPIC_3_NAME,
            shortDescription = TOPIC_SHORT_DESC,
            longDescription = TOPIC_LONG_DESC,
            url = TOPIC_URL,
            imageUrl = TOPIC_IMAGE_URL,
        ),
        isFollowed = false,
    ),
)

private val testOutputTopics = listOf(
    FollowableTopic(
        Topic(
            id = "0",
            name = TOPIC_1_NAME,
            shortDescription = TOPIC_SHORT_DESC,
            longDescription = TOPIC_LONG_DESC,
            url = TOPIC_URL,
            imageUrl = TOPIC_IMAGE_URL,
        ),
        isFollowed = true,
    ),
    FollowableTopic(
        Topic(
            id = "1",
            name = TOPIC_2_NAME,
            shortDescription = TOPIC_SHORT_DESC,
            longDescription = TOPIC_LONG_DESC,
            url = TOPIC_URL,
            imageUrl = TOPIC_IMAGE_URL,
        ),
        isFollowed = true,
    ),
    FollowableTopic(
        Topic(
            id = "2",
            name = TOPIC_3_NAME,
            shortDescription = TOPIC_SHORT_DESC,
            longDescription = TOPIC_LONG_DESC,
            url = TOPIC_URL,
            imageUrl = TOPIC_IMAGE_URL,
        ),
        isFollowed = false,
    ),
 )

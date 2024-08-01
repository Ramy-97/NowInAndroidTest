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

import androidx.lifecycle.SavedStateHandle
import com.google.samples.apps.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.NewsResource
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.testing.repository.TestNewsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestTopicsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestUserDataRepository
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import com.google.samples.apps.nowinandroid.feature.topic.navigation.TOPIC_ID_ARG
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

//Tools Used:
//JUnit,Kotlin,Coroutines Test, LiveData and flow

class RamyTopicsViewModelTest {  // Defines a test class for the TopicViewModel
    @get:Rule
    val dispatcherRule = MainDispatcherRule()  // Sets the main dispatcher for testing

    private val userDataRepository = TestUserDataRepository()  // Mock user data repository
    private val topicsRepository = TestTopicsRepository()  // Mock topics repository
    private val newsRepository = TestNewsRepository()  // Mock news repository
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )  // Combines news and user data repositories
    private lateinit var viewModel: TopicViewModel  // ViewModel under test

    @Before
    fun setup() {
        // Initializes the ViewModel with required dependencies before each test
        viewModel = TopicViewModel(
            savedStateHandle = SavedStateHandle(mapOf(TOPIC_ID_ARG to testInputTopics[0].topic.id)),
            userDataRepository = userDataRepository,
            topicsRepository = topicsRepository,
            userNewsResourceRepository = userNewsResourceRepository,
        )
    }

    // Test 1: Ensures the topic ID in the ViewModel matches the one in the SavedStateHandle
    // Explanation:
    // This test ensures that the ViewModel is initialized correctly with the topic ID from the SavedStateHandle.
    // This is important as it verifies the basic setup and data flow from SavedStateHandle to ViewModel.
    // It follows the pattern of validating initial state, which is fundamental for building reliable tests.
    @Test
    fun topicId_matchesTopicIdFromSavedStateHandle() =
        assertEquals(testInputTopics[0].topic.id, viewModel.topicId)

    // Test 2: Ensures the UI state for the topic is successfully loaded and matches the repository data
    // Explanation:
    // This test ensures that the ViewModel correctly loads the topic from the repository and updates the UI state.
    // This is important for verifying the ViewModel's interaction with the repository and the data flow.
    // It follows the pattern of testing asynchronous data flows and state updates, which is crucial for complex data-driven applications.

    @Test
    fun uiStateTopic_whenSuccess_matchesTopicFromRepository() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }  // Collects UI state

        topicsRepository.sendTopics(testInputTopics.map(FollowableTopic::topic))  // Sends topics to the repository
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))  // Sets followed topic IDs in the repository
        val item = viewModel.topicUiState.value  // Gets the current UI state
        assertIs<TopicUiState.Success>(item)  // Asserts the UI state is successful

        val topicFromRepository = topicsRepository.getTopic(
            testInputTopics[0].topic.id,
        ).first()  // Gets the topic from the repository

        assertEquals(topicFromRepository, item.followableTopic.topic)  // Asserts the topic matches the repository data

        collectJob.cancel()  // Cancels the collection job
    }

    // Test 3: Ensures the UI state for news shows loading when initialized
    // Explanation:
    // This test checks that the initial state of the news UI is set to loading.
    // This is important to ensure the user sees a loading indicator while data is being fetched.
    // It follows the pattern of validating initial loading states, which is a common scenario in user interfaces.
    @Test
    fun uiStateNews_whenInitialized_thenShowLoading() = runTest {
        assertEquals(NewsUiState.Loading, viewModel.newsUiState.value)  // Asserts the news UI state is loading
    }

    // Test 4: Ensures the UI state for the topic shows loading when initialized
    // Explanation:
    // This test checks that the initial state of the topic UI is set to loading.
    // This is important to ensure the user sees a loading indicator while data is being fetched.
    // It follows the pattern of validating initial loading states, which is a common scenario in user interfaces.
    @Test
    fun uiStateTopic_whenInitialized_thenShowLoading() = runTest {
        assertEquals(TopicUiState.Loading, viewModel.topicUiState.value)  // Asserts the topic UI state is loading
    }

    // Test 5: Ensures the UI state for the topic shows loading when followed topic IDs are successfully set but the topic is still loading
    // Explanation:
    // This test ensures the topic UI state remains in loading state even after successfully setting followed topic IDs.
    // This is important for verifying the sequence of state changes and handling intermediate loading states.
    // It follows the pattern of testing state transitions, which is critical for ensuring smooth user experience.
    @Test
    fun uiStateTopic_whenFollowedIdsSuccessAndTopicLoading_thenShowLoading() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }  // Collects UI state

        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))  // Sets followed topic IDs in the repository
        assertEquals(TopicUiState.Loading, viewModel.topicUiState.value)  // Asserts the topic UI state is loading

        collectJob.cancel()  // Cancels the collection job
    }

    // Test 6: Ensures the UI state for the topic shows success and news shows loading when both followed topic IDs and topic are successfully loaded
    // Explanation:
    // This test ensures that when followed topic IDs and topic data are successfully loaded, the topic UI state shows success and news UI state shows loading.
    // This is important for verifying the correct sequence of state updates and ensuring the user sees accurate state representations.
    // It follows the pattern of testing multiple state dependencies and transitions, which is crucial for complex UI logic.
    @Test
    fun uiStateTopic_whenFollowedIdsSuccessAndTopicSuccess_thenTopicSuccessAndNewsLoading() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }  // Collects UI state

        topicsRepository.sendTopics(testInputTopics.map { it.topic })  // Sends topics to the repository
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))  // Sets followed topic IDs in the repository
        val topicUiState = viewModel.topicUiState.value  // Gets the current topic UI state
        val newsUiState = viewModel.newsUiState.value  // Gets the current news UI state

        assertIs<TopicUiState.Success>(topicUiState)  // Asserts the topic UI state is successful
        assertIs<NewsUiState.Loading>(newsUiState)  // Asserts the news UI state is loading

        collectJob.cancel()  // Cancels the collection job
    }

    // Test 7: Ensures both the topic and news UI states show success when all data is successfully loaded
    // Explanation:
    // This test ensures that both topic and news UI states show success when all related data is successfully loaded.
    // This is important for validating complete data flow and state updates, ensuring the user sees the final successful state.
    // It follows the pattern of testing complex state dependencies and final states, which is essential for robust applications.
    @Test
    fun uiStateTopic_whenFollowedIdsSuccessAndTopicSuccessAndNewsIsSuccess_thenAllSuccess() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) {
            combine(
                viewModel.topicUiState,
                viewModel.newsUiState,
                ::Pair,
            ).collect()
        }  // Collects combined UI states

        topicsRepository.sendTopics(testInputTopics.map { it.topic })  // Sends topics to the repository
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))  // Sets followed topic IDs in the repository
        newsRepository.sendNewsResources(sampleNewsResources)  // Sends news resources to the repository
        val topicUiState = viewModel.topicUiState.value  // Gets the current topic UI state
        val newsUiState = viewModel.newsUiState.value  // Gets the current news UI state

        assertIs<TopicUiState.Success>(topicUiState)  // Asserts the topic UI state is successful
        assertIs<NewsUiState.Success>(newsUiState)  // Asserts the news UI state is successful

        collectJob.cancel()  // Cancels the collection job
    }

    // Test 8: Ensures the topic UI state is updated correctly when a topic is followed
    // Explanation:
    // This test ensures that when a topic is followed, the topic UI state is updated to reflect the new follow state.
    // This is important for verifying user interactions and ensuring the UI accurately reflects user actions.
    // It follows the pattern of testing user-driven state changes, which is crucial for interactive applications.
    @Test
    fun uiStateTopic_whenFollowingTopic_thenShowUpdatedTopic() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.topicUiState.collect() }  // Collects UI state

        topicsRepository.sendTopics(testInputTopics.map { it.topic })  // Sends topics to the repository
        // Set which topic IDs are followed, not including 0.
        userDataRepository.setFollowedTopicIds(setOf(testInputTopics[1].topic.id))  // Sets followed topic IDs in the repository

        viewModel.followTopicToggle(true)  // Toggles the follow state for the topic

        assertEquals(
            TopicUiState.Success(followableTopic = testOutputTopics[0]),
            viewModel.topicUiState.value,
        )  // Asserts the topic UI state is updated correctly

        collectJob.cancel()  // Cancels the collection job
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

private val sampleNewsResources = listOf(
    NewsResource(
        id = "1",
        title = "Thanks for helping us reach 1M YouTube Subscribers",
        content = "Thank you everyone for following the Now in Android series and everything the " +
            "Android Developers YouTube channel has to offer. During the Android Developer " +
            "Summit, our YouTube channel reached 1 million subscribers! Here’s a small video to " +
            "thank you all.",
        url = "https://youtu.be/-fJ6poHQrjM",
        headerImageUrl = "https://i.ytimg.com/vi/-fJ6poHQrjM/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-09T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(
            Topic(
                id = "0",
                name = "Headlines",
                shortDescription = "",
                longDescription = "long description",
                url = "URL",
                imageUrl = "image URL",
            ),
        ),
    ),
)

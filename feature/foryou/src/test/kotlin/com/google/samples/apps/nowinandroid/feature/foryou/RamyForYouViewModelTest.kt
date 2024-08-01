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

import androidx.lifecycle.SavedStateHandle
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsEvent
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsEvent.Param
import com.google.samples.apps.nowinandroid.core.data.repository.CompositeUserNewsResourceRepository
import com.google.samples.apps.nowinandroid.core.domain.GetFollowableTopicsUseCase
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.NewsResource
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.model.data.UserNewsResource
import com.google.samples.apps.nowinandroid.core.model.data.mapToUserNewsResources
import com.google.samples.apps.nowinandroid.core.testing.repository.TestNewsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestTopicsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestUserDataRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.emptyUserData
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import com.google.samples.apps.nowinandroid.core.testing.util.TestAnalyticsHelper
import com.google.samples.apps.nowinandroid.core.testing.util.TestSyncManager
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState
import com.google.samples.apps.nowinandroid.core.ui.NewsFeedUiState.Success
import com.google.samples.apps.nowinandroid.feature.foryou.OnboardingUiState.Loading
import com.google.samples.apps.nowinandroid.feature.foryou.OnboardingUiState.NotShown
import com.google.samples.apps.nowinandroid.feature.foryou.OnboardingUiState.Shown
import com.google.samples.apps.nowinandroid.feature.foryou.navigation.LINKED_NEWS_RESOURCE_ID
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

//Tools Used:
//Junit, Kotlin Coroutines Testing, Mocking and Testing Frameworks

class RamyForYouViewModelTest {
    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule() // Sets up a rule to handle the coroutine dispatcher for tests.

    private val syncManager = TestSyncManager() // Creates a test manager for syncing operations.
    private val analyticsHelper = TestAnalyticsHelper() // Creates a test helper for analytics.
    private val userDataRepository =
        TestUserDataRepository() // Creates a test repository for user data.
    private val topicsRepository = TestTopicsRepository() // Creates a test repository for topics.
    private val newsRepository =
        TestNewsRepository() // Creates a test repository for news resources.
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    ) // Initializes a composite repository for user news resources.

    private val getFollowableTopicsUseCase = GetFollowableTopicsUseCase(
        topicsRepository = topicsRepository,
        userDataRepository = userDataRepository,
    ) // Initializes the use case for fetching followable topics.

    private val savedStateHandle =
        SavedStateHandle() // Initializes the saved state handle for ViewModel.
    private lateinit var viewModel: ForYouViewModel // ViewModel under test.

    @Before
    fun setup() {
        viewModel = ForYouViewModel(
            syncManager = syncManager,
            savedStateHandle = savedStateHandle,
            analyticsHelper = analyticsHelper,
            userDataRepository = userDataRepository,
            userNewsResourceRepository = userNewsResourceRepository,
            getFollowableTopics = getFollowableTopicsUseCase,
        ) // Initializes the ViewModel with necessary dependencies.
    }

    // Test 1: Ensures the initial state is Loading
    // Explanation:
    // This test verifies that the initial state of the ViewModel is Loading.
    // It is important to confirm that the ViewModel starts in the correct loading state before any data is loaded.
    // It follows the pattern of validating initial ViewModel states to ensure the UI behaves correctly at the start.
    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(
            Loading,
            viewModel.onboardingUiState.value,
        ) // Asserts that the initial onboarding UI state is Loading.
        assertEquals(
            NewsFeedUiState.Loading,
            viewModel.feedState.value
        ) // Asserts that the initial feed state is Loading.
    }

    // Test 2: Ensures the state is Loading when followed topics are being fetched
    // Explanation:
    // This test verifies that when the followed topics are being fetched, the UI state shows Loading.
    // It is important to ensure that the UI reflects the loading state while data is being retrieved.
    // It follows the pattern of validating UI behavior during data fetch operations.
    @Test
    fun stateIsLoadingWhenFollowedTopicsAreLoading() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() } // Launches a coroutine to collect onboarding UI state.
        val collectJob2 =
            launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() } // Launches a coroutine to collect feed state.

        topicsRepository.sendTopics(sampleTopics) // Sends sample topics to the repository.

        assertEquals(
            Loading,
            viewModel.onboardingUiState.value,
        ) // Asserts that the onboarding UI state is Loading.
        assertEquals(
            NewsFeedUiState.Loading,
            viewModel.feedState.value
        ) // Asserts that the feed state is Loading.

        collectJob1.cancel() // Cancels the coroutine collecting onboarding UI state emissions.
        collectJob2.cancel() // Cancels the coroutine collecting feed state emissions.
    }

    // Test 3: Ensures the state is Loading when the app is syncing with no interests
    // Explanation:
    // This test verifies that when the app is syncing with no interests, the UI state shows Loading.
    // It is important to ensure that the UI reflects the loading state during app synchronization.
    // It follows the pattern of validating UI behavior during sync operations.
    @Test
    fun stateIsLoadingWhenAppIsSyncingWithNoInterests() = runTest {
        syncManager.setSyncing(true) // Sets the sync manager to syncing state.

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.isSyncing.collect() } // Launches a coroutine to collect syncing state.

        assertEquals(
            true,
            viewModel.isSyncing.value,
        ) // Asserts that the syncing state is true.

        collectJob.cancel() // Cancels the coroutine collecting syncing state emissions.
    }

    // Test 4: Ensures the onboarding state is Loading when topics are being fetched
    // Explanation:
    // This test verifies that the onboarding state shows Loading when topics are being fetched.
    // It is important to ensure that the UI reflects the correct loading state during topic retrieval.
    // It follows the pattern of validating UI behavior during data fetch operations.
    @Test
    fun onboardingStateIsLoadingWhenTopicsAreLoading() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() } // Launches a coroutine to collect onboarding UI state.
        val collectJob2 =
            launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() } // Launches a coroutine to collect feed state.

        userDataRepository.setFollowedTopicIds(emptySet()) // Sets followed topic IDs to an empty set.

        assertEquals(
            Loading,
            viewModel.onboardingUiState.value,
        ) // Asserts that the onboarding UI state is Loading.
        assertEquals(
            Success(emptyList()),
            viewModel.feedState.value
        ) // Asserts that the feed state is Success with an empty list.

        collectJob1.cancel() // Cancels the coroutine collecting onboarding UI state emissions.
        collectJob2.cancel() // Cancels the coroutine collecting feed state emissions.
    }

    // Test 5: Ensures the onboarding UI is shown when news resources are being fetched
    // Explanation:
    // This test verifies that the onboarding UI is shown while news resources are being fetched.
    // It is important to ensure that the onboarding UI correctly displays topics for selection during news resource retrieval.
    // It follows the pattern of validating UI behavior during multi-step data fetch operations.
    @Test
    fun onboardingIsShownWhenNewsResourcesAreLoading() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() } // Launches a coroutine to collect onboarding UI state.
        val collectJob2 =
            launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() } // Launches a coroutine to collect feed state.

        topicsRepository.sendTopics(sampleTopics) // Sends sample topics to the repository.
        userDataRepository.setFollowedTopicIds(emptySet()) // Sets followed topic IDs to an empty set.

        assertEquals(
            Shown(
                topics = listOf(
                    FollowableTopic(
                        topic = Topic(
                            id = "0",
                            name = "Headlines",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "1",
                            name = "UI",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "2",
                            name = "Tools",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                ),
            ),
            viewModel.onboardingUiState.value,
        ) // Asserts that the onboarding UI state is Shown with sample topics.
        assertEquals(
            Success(
                feed = emptyList(),
            ),
            viewModel.feedState.value,
        ) // Asserts that the feed state is Success with an empty list.

        collectJob1.cancel() // Cancels the coroutine collecting onboarding UI state emissions.
        collectJob2.cancel() // Cancels the coroutine collecting feed state emissions.
    }

    // Test 6: Ensures the onboarding UI is shown after loading empty followed topics
    // Explanation:
    // This test verifies that the onboarding UI is shown after loading topics when there are no followed topics.
    // It is important to ensure that the onboarding UI displays topics for user selection when no topics are followed.
    // It follows the pattern of validating UI behavior during initial data load with no user selections.
    @Test
    fun onboardingIsShownAfterLoadingEmptyFollowedTopics() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() } // Launches a coroutine to collect onboarding UI state.
        val collectJob2 =
            launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() } // Launches a coroutine to collect feed state.

        topicsRepository.sendTopics(sampleTopics) // Sends sample topics to the repository.
        userDataRepository.setFollowedTopicIds(emptySet()) // Sets followed topic IDs to an empty set.
        newsRepository.sendNewsResources(sampleNewsResources) // Sends sample news resources to the repository.

        assertEquals(
            Shown(
                topics = listOf(
                    FollowableTopic(
                        topic = Topic(
                            id = "0",
                            name = "Headlines",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "1",
                            name = "UI",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "2",
                            name = "Tools",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                ),
            ),
            viewModel.onboardingUiState.value,
        ) // Asserts that the onboarding UI state is Shown with sample topics.
        assertEquals(
            Success(
                feed = emptyList(),
            ),
            viewModel.feedState.value,
        ) // Asserts that the feed state is Success with an empty list.

        collectJob1.cancel() // Cancels the coroutine collecting onboarding UI state emissions.
        collectJob2.cancel() // Cancels the coroutine collecting feed state emissions.
    }

    // Test 7: Ensures the onboarding UI is not shown after loading followed topics
    // Explanation:
    // This test verifies that the onboarding UI is not shown after loading topics when there are followed topics.
    // It is important to ensure that the onboarding UI is hidden when the user has already followed topics.
    // It follows the pattern of validating UI behavior during initial data load with existing user selections.
    @Test
    fun onboardingIsNotShownAfterUserDismissesOnboarding() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }  // Launches a coroutine to collect onboarding UI state.
        val collectJob2 =
            launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() } // Launches a coroutine to collect feed state.

        topicsRepository.sendTopics(sampleTopics) // Sends sample topics to the repository.

        val followedTopicIds =
            setOf("0", "1") // Sets followed topic IDs to a set containing "0" and "1".
        val userData = emptyUserData.copy(followedTopics = followedTopicIds)
        userDataRepository.setUserData(userData) // Sends user data resources to the repository.
        viewModel.dismissOnboarding()

        assertEquals(
            NotShown,
            viewModel.onboardingUiState.value,
        ) // Asserts that the onboarding UI state is Hidden.
        assertEquals(NewsFeedUiState.Loading, viewModel.feedState.value)

        newsRepository.sendNewsResources(sampleNewsResources)

        assertEquals(
            NotShown,
            viewModel.onboardingUiState.value,
        )
        assertEquals(
            Success(
                feed = sampleNewsResources.mapToUserNewsResources(userData),
            ),
            viewModel.feedState.value,
        ) // Asserts that the feed state is Success with sample news resources.

        collectJob1.cancel() // Cancels the coroutine collecting onboarding UI state emissions.
        collectJob2.cancel() // Cancels the coroutine collecting feed state emissions.
    }

    // Test 8: Ensures the topic selection updates after selecting a topic
    // Importance:
    // This test ensures that when a topic is selected, it is properly reflected in the onboarding UI and the news feed is updated accordingly.
    // It validates that user actions (selecting a topic) correctly influence the UI and data shown, which is crucial for a responsive and user-friendly application.
    // Coroutine handling to simulate asynchronous data flow and validate UI changes in response to user actions.
    @Test
    fun topicSelectionUpdatesAfterSelectingTopic() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        // Collects onboarding UI state asynchronously to monitor updates.

        val collectJob2 = launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        // Collects feed state asynchronously to monitor updates.

        topicsRepository.sendTopics(sampleTopics)
        // Sends sample topics to simulate the initial topic data.

        userDataRepository.setFollowedTopicIds(emptySet())
        // Simulates a user with no followed topics initially.

        newsRepository.sendNewsResources(sampleNewsResources)
        // Sends sample news resources to simulate the initial news feed.

        assertEquals(
            Shown(
                topics = sampleTopics.map {
                    FollowableTopic(it, false)
                    // Verifies that all topics are shown as not followed initially.
                },
            ),
            viewModel.onboardingUiState.value,
            // Asserts that the onboarding UI correctly displays all topics as not followed.
        )

        assertEquals(
            Success(
                feed = emptyList(),
                // Ensures the feed is empty initially since no topics are followed.
            ),
            viewModel.feedState.value,
        )

        val followedTopicId = sampleTopics[1].id
        // Simulates selecting the second topic from the sample topics.

        viewModel.updateTopicSelection(followedTopicId, isChecked = true)
        // Updates the topic selection to follow the selected topic.

        assertEquals(
            Shown(
                topics = sampleTopics.map {
                    FollowableTopic(it, it.id == followedTopicId)
                    // Verifies that the selected topic is marked as followed.
                },
            ),
            viewModel.onboardingUiState.value,
            // Asserts that the onboarding UI correctly reflects the updated selection state.
        )

        val userData = emptyUserData.copy(followedTopics = setOf(followedTopicId))
        // Creates user data with the followed topic ID.

        assertEquals(
            Success(
                feed = listOf(
                    UserNewsResource(sampleNewsResources[1], userData),
                    UserNewsResource(sampleNewsResources[2], userData),
                    // Ensures the feed state displays news resources related to the followed topic.
                ),
            ),
            viewModel.feedState.value,
        )

        collectJob1.cancel()
        // Cancels the coroutine collecting onboarding UI state emissions.

        collectJob2.cancel()
        // Cancels the coroutine collecting feed state emissions.
    }

    // Test 9: Ensures the topic selection updates after unselecting topic
    // Importance:
    // This test ensures that unselecting a topic correctly updates the onboarding UI and clears the news feed
    // It verifies that the UI accurately reflects the unselected state and that no stale data is shown.
    // Ensures all asynchronous operations are completed, providing a reliable state for assertions.

    @Test
    fun topicSelectionUpdatesAfterUnselectingTopic() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        // Collects onboarding UI state asynchronously.

        val collectJob2 = launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        // Collects feed state asynchronously.

        topicsRepository.sendTopics(sampleTopics)
        // Sends sample topics to simulate the initial topic data.

        userDataRepository.setFollowedTopicIds(emptySet())
        // Simulates a user with no followed topics initially.

        newsRepository.sendNewsResources(sampleNewsResources)
        // Sends sample news resources to simulate the initial news feed.

        viewModel.updateTopicSelection("1", isChecked = true)
        // Simulates selecting a topic.

        viewModel.updateTopicSelection("1", isChecked = false)
        // Simulates unselecting the topic.

        advanceUntilIdle()
        // Ensures all state updates are processed.

        assertEquals(
            Shown(
                topics = listOf(
                    FollowableTopic(
                        topic = Topic(
                            id = "0",
                            name = "Headlines",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "1",
                            name = "UI",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                    FollowableTopic(
                        topic = Topic(
                            id = "2",
                            name = "Tools",
                            shortDescription = "",
                            longDescription = "long description",
                            url = "URL",
                            imageUrl = "image URL",
                        ),
                        isFollowed = false,
                    ),
                ),
            ),
            viewModel.onboardingUiState.value,
            // Asserts that the onboarding UI correctly reflects no topics followed after un-selection.
        )
        assertEquals(
            Success(
                feed = emptyList(),
                // Ensures the feed is empty after unselecting the topic.
            ),
            viewModel.feedState.value,
        )

        collectJob1.cancel()
        // Cancels the coroutine collecting onboarding UI state emissions.

        collectJob2.cancel()
        // Cancels the coroutine collecting feed state emissions.
    }

    // Test 10: Ensures news resources selection updates after loading followed topic
    // Explanation:
    // This test ensures that the feed state and onboarding UI reflect the changes after followed topics are loaded and news resources are updated.
    // It verifies that the UI and data reflect user preferences and updates correctly, which is critical for user satisfaction.
    // Uses a combination of data loading and state updates to validate end-to-end functionality, ensuring that followed topics and saved resources are handled properly.
    @Test
    fun newsResourceSelectionUpdatesAfterLoadingFollowedTopics() = runTest {
        val collectJob1 =
            launch(UnconfinedTestDispatcher()) { viewModel.onboardingUiState.collect() }
        // Collects onboarding UI state asynchronously.

        val collectJob2 = launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        // Collects feed state asynchronously.

        val followedTopicIds = setOf("1")
        // Defines followed topic IDs.

        val userData = emptyUserData.copy(
            followedTopics = followedTopicIds,
            shouldHideOnboarding = true,
        )
        // Creates user data with followed topics and hides onboarding.

        topicsRepository.sendTopics(sampleTopics)
        // Sends sample topics to the repository.

        userDataRepository.setUserData(userData)
        // Sets user data in the repository.

        newsRepository.sendNewsResources(sampleNewsResources)
        // Sends sample news resources to the repository.

        val bookmarkedNewsResourceId = "2"
        // Defines the ID of a news resource to be bookmarked.

        viewModel.updateNewsResourceSaved(
            newsResourceId = bookmarkedNewsResourceId,
            isChecked = true,
        )
        // Updates the news resource selection to save the bookmarked resource.

        val userDataExpected = userData.copy(
            bookmarkedNewsResources = setOf(bookmarkedNewsResourceId),
        )
        // Updates user data to include the bookmarked news resource.

        assertEquals(
            NotShown,
            viewModel.onboardingUiState.value,
            // Ensures the onboarding UI state is hidden after loading followed topics.
        )
        assertEquals(
            Success(
                feed = listOf(
                    UserNewsResource(newsResource = sampleNewsResources[1], userDataExpected),
                    UserNewsResource(newsResource = sampleNewsResources[2], userDataExpected),
                ),
            ),
            viewModel.feedState.value,
            // Asserts that the feed state displays news resources related to followed topics and bookmarked resources.
        )

        collectJob1.cancel()
        // Cancels the coroutine collecting onboarding UI state emissions.

        collectJob2.cancel()
        // Cancels the coroutine collecting feed state emissions.
    }

    // Test 11: Ensures dep linked news resources is fetched and reset after viewing
    // Importance:
    // This test ensures that deep-linked news resources are correctly fetched and then reset after being viewed.
    // It verifies that the deep-link functionality works as expected, providing a smooth user experience when navigating through links.
    // Combines deep-link handling with state verification and analytics logging, ensuring end-to-end functionality and proper tracking of user interactions.
    @Test
    fun deepLinkedNewsResourceIsFetchedAndResetAfterViewing() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.deepLinkedNewsResource.collect() }
        // Collects deep-linked news resource state asynchronously.

        newsRepository.sendNewsResources(sampleNewsResources)
        // Sends sample news resources to the repository.

        userDataRepository.setUserData(emptyUserData)
        // Sets user data in the repository.

        savedStateHandle[LINKED_NEWS_RESOURCE_ID] = sampleNewsResources.first().id
        // Sets a deep-linked news resource ID in the saved state handle.

        assertEquals(
            expected = UserNewsResource(
                newsResource = sampleNewsResources.first(),
                userData = emptyUserData,
            ),
            actual = viewModel.deepLinkedNewsResource.value,
        )
        // Asserts that the deep-linked news resource is fetched correctly.

        viewModel.onDeepLinkOpened(
            newsResourceId = sampleNewsResources.first().id,
        )
        // Simulates the user opening the deep-linked news resource.

        assertNull(
            viewModel.deepLinkedNewsResource.value,
        )
        // Asserts that the deep-linked news resource state is reset after viewing.

        assertTrue(
            analyticsHelper.hasLogged(
                AnalyticsEvent(
                    type = "news_deep_link_opened",
                    extras = listOf(
                        Param(
                            key = LINKED_NEWS_RESOURCE_ID,
                            value = sampleNewsResources.first().id,
                        ),
                    ),
                ),
            ),
        )
        // Verifies that the analytics event for deep-link opening is logged correctly.

        collectJob.cancel()
        // Cancels the coroutine collecting deep-linked news resource state emissions.
    }

    // Test 12: Ensures bookmark state is updated when news resource is called
    // Importance:
    // This test ensures that the bookmark state of a news resource is updated correctly when the resource is saved or removed from bookmarks.
    // It validates that the bookmark functionality works as intended, allowing users to save and remove news resources as needed.
    // Tests the update mechanism directly by asserting changes in the user data repository, ensuring accurate state management.
    @Test
    fun whenUpdateNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = "123"
        // Defines the ID of the news resource to be updated.

        viewModel.updateNewsResourceSaved(newsResourceId, true)
        // Marks the news resource as saved (bookmarked).

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )
        // Asserts that the bookmarked news resources include the updated resource ID.

        viewModel.updateNewsResourceSaved(newsResourceId, false)
        // Unmarks the news resource (removes from bookmarks).

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )
        // Asserts that the bookmarked news resources are empty after unmarking.
    }
}
private val sampleTopics = listOf(
    Topic(
        id = "0",
        name = "Headlines",
        shortDescription = "",
        longDescription = "long description",
        url = "URL",
        imageUrl = "image URL",
    ),
    Topic(
        id = "1",
        name = "UI",
        shortDescription = "",
        longDescription = "long description",
        url = "URL",
        imageUrl = "image URL",
    ),
    Topic(
        id = "2",
        name = "Tools",
        shortDescription = "",
        longDescription = "long description",
        url = "URL",
        imageUrl = "image URL",
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
        url = "https://www.youtube.com/watch?v=PFdiTzd6CbI",
        headerImageUrl = "https://img.youtube.com/vi/PFdiTzd6CbI/maxresdefault.jpg",
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
    NewsResource(
        id = "2",
        title = "Transformations and customisations in the Paging Library",
        content = "A demonstration of different operations that can be performed with Paging. " +
            "Transformations like inserting separators, when to create a new pager, and " +
            "customisation options for consuming PagingData.",
        url = "https://youtu.be/ZARz0pjm5YM",
        headerImageUrl = "https://i.ytimg.com/vi/ZARz0pjm5YM/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-01T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(
            Topic(
                id = "1",
                name = "UI",
                shortDescription = "",
                longDescription = "long description",
                url = "URL",
                imageUrl = "image URL",
            ),
        ),
    ),
    NewsResource(
        id = "3",
        title = "Community tip on Paging",
        content = "Tips for using the Paging library from the developer community",
        url = "https://youtu.be/r5JgIyS3t3s",
        headerImageUrl = "https://i.ytimg.com/vi/r5JgIyS3t3s/maxresdefault.jpg",
        publishDate = Instant.parse("2021-11-08T00:00:00.000Z"),
        type = "Video 📺",
        topics = listOf(
            Topic(
                id = "1",
                name = "UI",
                shortDescription = "",
                longDescription = "long description",
                url = "URL",
                imageUrl = "image URL",
            ),
        ),
    ),
)


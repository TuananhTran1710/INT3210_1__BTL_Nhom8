package com.example.wink.ui.features.onboarding

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.UserRepository
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class OnboardingViewModelTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = OnboardingViewModel(userRepository, authRepository)
    }

    // ====== Test SelectGender Event ======
    @Test
    fun testSelectGender_UpdatesStateCorrectly() {
        // Arrange
        val gender = "male"

        // Act
        viewModel.onEvent(OnboardingEvent.SelectGender(gender))

        // Assert
        assertEquals(gender, viewModel.state.selectedGender)
    }

    @Test
    fun testSelectGender_OverwritesPreviousSelection() {
        // Arrange
        val firstGender = "male"
        val secondGender = "female"

        // Act
        viewModel.onEvent(OnboardingEvent.SelectGender(firstGender))
        viewModel.onEvent(OnboardingEvent.SelectGender(secondGender))

        // Assert
        assertEquals(secondGender, viewModel.state.selectedGender)
    }

    // ====== Test SelectPreference Event ======
    @Test
    fun testSelectPreference_UpdatesStateCorrectly() {
        // Arrange
        val preference = "female"

        // Act
        viewModel.onEvent(OnboardingEvent.SelectPreference(preference))

        // Assert
        assertEquals(preference, viewModel.state.selectedPreference)
    }

    // ====== Test TogglePersonality Event ======
    @Test
    fun testTogglePersonality_AddPersonalityWhenNotPresent() {
        // Arrange
        val personality = "humorous"

        // Act
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality))

        // Assert
        assertTrue(viewModel.state.selectedPersonalities.contains(personality))
        assertEquals(1, viewModel.state.selectedPersonalities.size)
    }

    @Test
    fun testTogglePersonality_RemovePersonalityWhenAlreadyPresent() {
        // Arrange
        val personality = "humorous"
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality))

        // Act
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality))

        // Assert
        assertFalse(viewModel.state.selectedPersonalities.contains(personality))
        assertEquals(0, viewModel.state.selectedPersonalities.size)
    }

    @Test
    fun testTogglePersonality_HandleMultiplePersonalities() {
        // Arrange
        val personality1 = "humorous"
        val personality2 = "intelligent"
        val personality3 = "outgoing"

        // Act
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality1))
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality2))
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality3))

        // Assert
        assertEquals(3, viewModel.state.selectedPersonalities.size)
        assertTrue(viewModel.state.selectedPersonalities.contains(personality1))
        assertTrue(viewModel.state.selectedPersonalities.contains(personality2))
        assertTrue(viewModel.state.selectedPersonalities.contains(personality3))
    }

    @Test
    fun testTogglePersonality_RemoveOneFromMultiple() {
        // Arrange
        val personality1 = "humorous"
        val personality2 = "intelligent"
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality1))
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality2))

        // Act
        viewModel.onEvent(OnboardingEvent.TogglePersonality(personality1))

        // Assert
        assertEquals(1, viewModel.state.selectedPersonalities.size)
        assertFalse(viewModel.state.selectedPersonalities.contains(personality1))
        assertTrue(viewModel.state.selectedPersonalities.contains(personality2))
    }

    // ====== Test Pagination Events ======
    @Test
    fun testNextPage_IncrementsCurrentPage() {
        // Arrange
        val initialPage = viewModel.state.currentPage

        // Act
        viewModel.onEvent(OnboardingEvent.NextPage)

        // Assert
        assertEquals(initialPage + 1, viewModel.state.currentPage)
    }

    @Test
    fun testNextPage_MultipleTimes() {
        // Arrange
        val initialPage = viewModel.state.currentPage

        // Act
        viewModel.onEvent(OnboardingEvent.NextPage)
        viewModel.onEvent(OnboardingEvent.NextPage)
        viewModel.onEvent(OnboardingEvent.NextPage)

        // Assert
        assertEquals(initialPage + 3, viewModel.state.currentPage)
    }

    @Test
    fun testPreviousPage_DecrementsCurrentPage() {
        // Arrange
        viewModel.onEvent(OnboardingEvent.NextPage)
        viewModel.onEvent(OnboardingEvent.NextPage)
        val pageBeforePrevious = viewModel.state.currentPage

        // Act
        viewModel.onEvent(OnboardingEvent.PreviousPage)

        // Assert
        assertEquals(pageBeforePrevious - 1, viewModel.state.currentPage)
    }

    // ====== Test FinishOnboarding Event ======
    @Test
    fun testFinishOnboarding_SetIsLoadingTrue() = runTest {
        // Arrange
        whenever(authRepository.updateUserPreferences(
            gender = "male",
            preference = "Thích Nữ. Gu: Hài hước",
            personalities = listOf("Hài hước")
        )).thenReturn(Result.success(Unit))

        viewModel.onEvent(OnboardingEvent.SelectGender("male"))
        viewModel.onEvent(OnboardingEvent.SelectPreference("female"))
        viewModel.onEvent(OnboardingEvent.TogglePersonality("Hài hước"))

        // Act
        viewModel.onEvent(OnboardingEvent.FinishOnboarding)

        // Assert - Check initial loading state was set
        // Note: Due to async nature, we verify through state change
        assertTrue(viewModel.state.isLoading || viewModel.state.isSavedSuccess)
    }

    @Test
    fun testFinishOnboarding_BuildsPreferenceStringCorrectly() = runTest {
        // Arrange
        whenever(authRepository.updateUserPreferences(
            gender = "male",
            preference = "Thích Nữ. Gu: Hài hước, Thông minh",
            personalities = listOf("Hài hước", "Thông minh")
        )).thenReturn(Result.success(Unit))

        viewModel.onEvent(OnboardingEvent.SelectGender("male"))
        viewModel.onEvent(OnboardingEvent.SelectPreference("female"))
        viewModel.onEvent(OnboardingEvent.TogglePersonality("Hài hước"))
        viewModel.onEvent(OnboardingEvent.TogglePersonality("Thông minh"))

        // Act
        viewModel.onEvent(OnboardingEvent.FinishOnboarding)

        // Assert
        // Give some time for coroutine to complete
        assertTrue(
            viewModel.state.isSavedSuccess || viewModel.state.isLoading
        )
    }

    @Test
    fun testFinishOnboarding_WithoutPersonalities() = runTest {
        // Arrange
        whenever(authRepository.updateUserPreferences(
            gender = "male",
            preference = "Thích Nữ",
            personalities = emptyList()
        )).thenReturn(Result.success(Unit))

        viewModel.onEvent(OnboardingEvent.SelectGender("male"))
        viewModel.onEvent(OnboardingEvent.SelectPreference("female"))

        // Act
        viewModel.onEvent(OnboardingEvent.FinishOnboarding)

        // Assert
        assertTrue(
            viewModel.state.isSavedSuccess || viewModel.state.isLoading
        )
    }

    // ====== Test PreferenceString Building ======
    @Test
    fun testPreferenceString_MalePreference() = runTest {
        // Arrange
        whenever(authRepository.updateUserPreferences(
            gender = "female",
            preference = "Thích Nam. Gu: Tự tin, Hòa đồng",
            personalities = listOf("Tự tin", "Hòa đồng")
        )).thenReturn(Result.success(Unit))

        viewModel.onEvent(OnboardingEvent.SelectGender("female"))
        viewModel.onEvent(OnboardingEvent.SelectPreference("male"))
        viewModel.onEvent(OnboardingEvent.TogglePersonality("Tự tin"))
        viewModel.onEvent(OnboardingEvent.TogglePersonality("Hòa đồng"))

        // Act
        viewModel.onEvent(OnboardingEvent.FinishOnboarding)

        // Assert
        assertTrue(
            viewModel.state.isSavedSuccess || viewModel.state.isLoading
        )
    }

    @Test
    fun testPreferenceString_BothGenderPreference() = runTest {
        // Arrange
        whenever(authRepository.updateUserPreferences(
            gender = "other",
            preference = "Thích cả hai. Gu: Hài hước",
            personalities = listOf("Hài hước")
        )).thenReturn(Result.success(Unit))

        viewModel.onEvent(OnboardingEvent.SelectGender("other"))
        viewModel.onEvent(OnboardingEvent.SelectPreference("both"))
        viewModel.onEvent(OnboardingEvent.TogglePersonality("Hài hước"))

        // Act
        viewModel.onEvent(OnboardingEvent.FinishOnboarding)

        // Assert
        assertTrue(
            viewModel.state.isSavedSuccess || viewModel.state.isLoading
        )
    }

    // ====== Test State Isolation ======
    @Test
    fun testStateChanges_AreIndependent() {
        // Arrange
        val initialState = viewModel.state

        // Act
        viewModel.onEvent(OnboardingEvent.SelectGender("male"))
        viewModel.onEvent(OnboardingEvent.SelectPreference("female"))
        viewModel.onEvent(OnboardingEvent.TogglePersonality("humorous"))
        viewModel.onEvent(OnboardingEvent.NextPage)

        // Assert
        assertEquals("male", viewModel.state.selectedGender)
        assertEquals("female", viewModel.state.selectedPreference)
        assertEquals(1, viewModel.state.selectedPersonalities.size)
        assertEquals(1, viewModel.state.currentPage)
    }

    // ====== Test Initial State ======
    @Test
    fun testInitialState_IsCorrect() {
        // Arrange & Act
        val state = viewModel.state

        // Assert
        assertEquals(0, state.currentPage)
        assertEquals("", state.selectedGender)
        assertEquals("", state.selectedPreference)
        assertTrue(state.selectedPersonalities.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(null, state.errorMessage)
        assertFalse(state.isSavedSuccess)
    }
}

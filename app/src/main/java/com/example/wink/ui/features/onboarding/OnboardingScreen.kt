package com.example.wink.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val pagerState = rememberPagerState(pageCount = { 4 })

    LaunchedEffect(state.currentPage) {
        pagerState.animateScrollToPage(state.currentPage)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> IntroPage()
                1 -> GenderPage(state, viewModel)
                2 -> PreferencePage(state, viewModel)
                3 -> PersonalityPage(state, viewModel)
            }
        }

        BottomControls(
            currentPage = state.currentPage,
            onNext = {
                if (state.currentPage < 3) {
                    viewModel.onEvent(OnboardingEvent.NextPage)
                } else {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.AUTH_GRAPH_ROUTE) { inclusive = true }
                    }
                }
            },
            onBack = {
                if (state.currentPage > 0) {
                    viewModel.onEvent(OnboardingEvent.PreviousPage)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun IntroPage() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Welcome to Wink!",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Hãy giúp chúng tôi hiểu bạn hơn nhé!",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GenderPage(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Giới tính của bạn là?", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenderBox("Nam", state.selectedGender == "male") {
                viewModel.onEvent(OnboardingEvent.SelectGender("male"))
            }
            GenderBox("Nữ", state.selectedGender == "female") {
                viewModel.onEvent(OnboardingEvent.SelectGender("female"))
            }
            GenderBox("Khác", state.selectedGender == "other") {
                viewModel.onEvent(OnboardingEvent.SelectGender("other"))
            }
        }
    }
}

@Composable
fun PreferencePage(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bạn quan tâm đến?", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenderBox("Con trai", state.selectedPreference == "male") {
                viewModel.onEvent(OnboardingEvent.SelectPreference("male"))
            }
            GenderBox("Con gái", state.selectedPreference == "female") {
                viewModel.onEvent(OnboardingEvent.SelectPreference("female"))
            }
            GenderBox("Cả hai", state.selectedPreference == "both") {
                viewModel.onEvent(OnboardingEvent.SelectPreference("both"))
            }
        }
    }
}

@Composable
fun PersonalityPage(state: OnboardingState, viewModel: OnboardingViewModel) {
    val personalities = listOf("Hài hước", "Lãng mạn", "Thông minh", "Gia trưởng", "Cơ bắp")
    var selectedPersonality by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chọn tính cách mà bạn thích ở người khác", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            personalities.forEach { personality ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPersonality = personality
                            viewModel.onEvent(OnboardingEvent.SelectPreference(personality))
                        }
                        .shadow(if (selectedPersonality == personality) 8.dp else 2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPersonality == personality) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        personality,
                        modifier = Modifier.padding(16.dp),
                        color = if (selectedPersonality == personality) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun GenderBox(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BottomControls(
    currentPage: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentPage > 0) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        } else {
            Spacer(Modifier.width(1.dp))
        }

        Button(onClick = onNext) {
            Text(if (currentPage == 3) "Finish" else "Next")
        }
    }
}
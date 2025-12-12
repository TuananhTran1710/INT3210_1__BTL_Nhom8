package com.example.wink.ui.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.wink.ui.features.social.SocialViewModel
import com.example.wink.ui.navigation.Screen


<<<<<<< HEAD
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel()
=======

data class DailyTask(
    val id: Int,
    val title: String,
    val reward: Int,
    val isCompleted: Boolean = false
)

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // RIZZ Points Card
            item {
                AnimatedDashboardItem(delay = 0) {
                    RizzPointsCard(
                        points = uiState.rizzPoints,
                        streakDays = uiState.dailyStreak,
                        attended = uiState.hasDailyCheckIn,
                        onStreakClick = {
                            viewModel.onEvent(DashboardEvent.OnDailyCheckIn)
                        }
                    )
                }
            }

            // AI Chat Feature
            item {
                AnimatedDashboardItem(delay = 100) {
                    AIFeatureCard(
                        onClick = {

                            viewModel.onEvent(DashboardEvent.OnStartAIChat) }
                    )
                }
            }

            // Daily Tasks Section
            item {
                AnimatedDashboardItem(delay = 200) {
                    DailyTasksSection(
                        onTaskClick = { viewModel.onEvent(DashboardEvent.OnCompleteTask) }
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "TRANG CHỦ",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp)
    )
}

@Composable
private fun AnimatedDashboardItem(
    delay: Int,
    content: @Composable () -> Unit
>>>>>>> main
) {
    val uiState by viewModel.uiState.collectAsState()
    val friendRequests by socialViewModel.friendRequests.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang chủ") },
                actions = {
                    // Notification icon with badge
                    BadgedBox(
                        badge = {
                            if (friendRequests.isNotEmpty()) {
                                Badge {
                                    Text("${friendRequests.size}")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("friend_requests")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Thông báo"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Profile icon
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Profile.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Hồ sơ"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Chào mừng trở lại!",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.userEmail,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
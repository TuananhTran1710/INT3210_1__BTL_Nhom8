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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel()
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
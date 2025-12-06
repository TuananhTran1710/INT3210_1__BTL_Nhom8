package com.example.wink.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    Scaffold(
        topBar = {
            TopAppBar(
                title = {}, // Để trống để hiện ảnh bìa
                navigationIcon = {
                    // Nút Back nổi trên nền (có background mờ để dễ nhìn)
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy người dùng")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())
            ) {
                // 1. HEADER (Ảnh bìa + Avatar)
                item {
                    Box(modifier = Modifier.height(180.dp)) {
                        Spacer(modifier = Modifier.fillMaxWidth())
                        // Avatar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(140.dp)
                                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(user.username.take(1), style = MaterialTheme.typography.displayMedium)
                            }
                        }
                    }
                }

                // 2. INFO & ACTIONS
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text(user.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("RIZZ Master • Level ${user.rizzPoints / 100}", color = MaterialTheme.colorScheme.secondary)

                        Spacer(Modifier.height(24.dp))

                        // Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Nút Kết Bạn
                            Button(
                                onClick = { viewModel.sendFriendRequest() },
                                modifier = Modifier.weight(1f),
                                enabled = !state.requestSent && !state.isFriend
                            ) {
                                if (state.requestSent) {
                                    Icon(Icons.Default.Check, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Đã gửi lời mời")
                                } else if (state.isFriend) {
                                    Icon(Icons.Default.PersonAdd, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Bạn bè")
                                } else {
                                    Icon(Icons.Default.PersonAdd, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Kết bạn")
                                }
                            }

                            // Nút Nhắn Tin
                            FilledTonalButton(
                                onClick = { viewModel.sendMessage() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Nhắn tin")
                            }
                        }
                    }
                }

                // 3. STATS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = "${user.rizzPoints}", label = "RIZZ")
                        VerticalDivider()
                        StatItem(value = "${user.loginStreak}", label = "Streak")
                        VerticalDivider()
                        StatItem(value = "10", label = "Bạn bè")
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(30.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// Tái sử dụng StatItem từ ProfileScreen (hoặc copy lại)
@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
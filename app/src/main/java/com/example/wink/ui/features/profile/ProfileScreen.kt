package com.example.wink.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wink.ui.features.dashboard.FeatureType
import com.example.wink.ui.navigation.Screen

data class FriendProfile(
    val id: String,
    val name: String,
    val rizz: Int,
    val avatar: String = ""
)

data class PostData(
    val id: String,
    val author: String,
    val timeAgo: String,
    val content: String,
    val likes: Int,
    val comments: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
//    onNavigateToUserDetail: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    // Mock data for demonstration
    val mockFriends = listOf(
        FriendProfile("1", "Girl Hài Hước", 15),
        FriendProfile("2", "Kiên J", 40),
        FriendProfile("3", "Khánh", 100)
    )

    val mockPosts = listOf(
        PostData(
            "1",
            "Ếch Xanh Lè",
            "2 ngày",
            "Vừa áp dụng vần mẫu của AI tán đổ crush 2 năm. Uy tín luôn anh em à!",
            3842,
            247
        )
    )

    // Handle logout navigation
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTab == 1) {
                IconButton(onClick = { selectedTab = 0 }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        if (selectedTab == 0) {
            // Main Profile View
            ProfileMainContent(
                username = uiState.username.ifBlank { "Ếch Xanh Lè" },
                handle = "@echxanhle",
                rizzPoints = 100,
                streak = 200, // Mock data
                friends = 3, // Mock data
                posts = mockPosts,
                onFriendsClick = { selectedTab = 1 },
                onLogout = { viewModel.onEvent(ProfileEvent.LogoutClick) }
            )
        } else {
            // Friends List View
            FriendsListContent(
                friends = mockFriends,
                onBack = { selectedTab = 0 },
                onFriendClick = { friendId ->
                    // Handle friend click
                    navController.navigate("user_detail/$friendId")
                }
            )
        }
    }
}

@Composable
private fun ProfileMainContent(
    username: String,
    handle: String,
    rizzPoints: Int,
    streak: Int,
    friends: Int,
    posts: List<PostData>,
    onFriendsClick: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            // Profile Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8C5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = Color(0xFF9C27B0)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = username,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = handle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard("$rizzPoints", "RIZZ")
                    StatCard("$streak", "STREAK")
                    StatCard("$friends", "BẠN BÈ", onClick = onFriendsClick)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            // Posts Section Header
            Text(
                text = "📝 Bài viết",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(posts) { post ->
            PostCard(post = post)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF4444)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FriendsListContent(
    friends: List<FriendProfile>,
    onBack: () -> Unit,
    onFriendClick: (id: String) -> Unit
//    onFriendClick: (String) -> Unit = {}
) {
    Column {
        // Header
        Text(
            text = "Danh sách bạn bè",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Friends List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends) { friend ->
                FriendCard(
                    friend = friend,
                    onFriendClick = { onFriendClick(friend.id) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PostCard(post: PostData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8C5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF9C27B0)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.author,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = post.timeAgo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Post Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likes.toString(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.comments.toString(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendCard(
    friend: FriendProfile,
    onFriendClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFriendClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8C5FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF9C27B0)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "RIZZ: ${friend.rizz}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { /* Send message */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Nhắn tin",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Create a simple preview without ViewModel dependency
    ProfileScreenContent()
}

@Composable
private fun ProfileScreenContent() {
    var selectedTab by remember { mutableStateOf(0) }

    // Mock data for preview
    val mockFriends = listOf(
        FriendProfile("1", "Girl Hài Hước", 15),
        FriendProfile("2", "Kiên J", 40),
        FriendProfile("3", "Khánh", 100)
    )
    
    val mockPosts = listOf(
        PostData(
            "1", 
            "Ếch Xanh Lè", 
            "2 ngày", 
            "Vừa áp dụng vần mẫu của AI tán đổ crush 2 năm. Uy tín luôn anh em à!", 
            3842, 
            247
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTab == 1) {
                IconButton(onClick = { selectedTab = 0 }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        if (selectedTab == 0) {
            // Main Profile View
            ProfileMainContent(
                username = "Ếch Xanh Lè",
                handle = "@echxanhle",
                rizzPoints = 4022,
                streak = 200,
                friends = 3,
                posts = mockPosts,
                onFriendsClick = { selectedTab = 1 },
                onLogout = { }
            )
        } else {
            // Friends List View
            FriendsListContent(
                friends = mockFriends,
                onBack = { selectedTab = 0 },
                onFriendClick = { friendId -> /* Navigation in preview */ }
            )
        }
    }
}

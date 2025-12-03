package com.example.wink.ui.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wink.ui.features.profile.UserDetailScreen

/**
 * Complete navigation example showing the flow from Profile -> Friends List -> User Detail
 * This demonstrates how users can navigate through the social features of the app
 */
@Composable
fun NavigationDemoScreen(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "demo_main"
    ) {
        composable("demo_main") {
            DemoMainScreen(
                onNavigateToProfile = {
                    navController.navigate("demo_profile")
                }
            )
        }
        
        composable("demo_profile") {
            DemoProfileScreen(
                onNavigateToUserDetail = { userId ->
                    navController.navigate("user_detail/$userId")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("user_detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "1"
            UserDetailScreen(
                userId = userId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun DemoMainScreen(
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WINK Navigation Demo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9C27B0)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Demonstration of the navigation flow from Profile screen to individual user details",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateToProfile,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Open Profile Screen",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun DemoProfileScreen(
    onNavigateToUserDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val friends = listOf(
        DemoFriend("1", "Girl Hài Hước", 15),
        DemoFriend("2", "Kiên J", 40),
        DemoFriend("3", "Khánh", 100)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("← Back")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Friends List",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Click on any friend to view their profile details",
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Friends List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends.size) { index ->
                val friend = friends[index]
                DemoFriendCard(
                    friend = friend,
                    onFriendClick = { onNavigateToUserDetail(friend.id) }
                )
            }
        }
    }
}

@Composable
private fun DemoFriendCard(
    friend: DemoFriend,
    onFriendClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onFriendClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.name.first().toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "RIZZ: ${friend.rizz}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = "→",
                fontSize = 18.sp,
                color = Color(0xFF9C27B0)
            )
        }
    }
}

data class DemoFriend(
    val id: String,
    val name: String,
    val rizz: Int
)

@Preview(showBackground = true)
@Composable
private fun NavigationDemoPreview() {
    NavigationDemoScreen()
}
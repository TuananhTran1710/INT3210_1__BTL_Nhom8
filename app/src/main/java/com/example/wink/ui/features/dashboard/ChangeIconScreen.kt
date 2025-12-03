package com.example.wink.ui.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeIconScreen(
    navController: NavController
) {
    var selectedIcon by remember { mutableStateOf("default") }
    val iconOptions = listOf(
        "default" to "⭐",
        "fire" to "🔥", 
        "crown" to "👑",
        "diamond" to "💎",
        "heart" to "❤️",
        "thunder" to "⚡"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thay đổi Icon") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Chọn icon yêu thích của bạn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Icon hiện tại sẽ hiển thị trong hồ sơ của bạn",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Current selected icon preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8C5FF)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Icon đã chọn:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = iconOptions.find { it.first == selectedIcon }?.second ?: "⭐",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Icon selection grid
            Text(
                text = "Chọn icon mới:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            iconOptions.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (id, emoji) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedIcon == id) 
                                    Color(0xFF9C27B0) else Color(0xFFE0E0E0)
                            ),
                            onClick = { selectedIcon = id }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 32.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    // Save selected icon and go back
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Lưu thay đổi",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangeIconScreenPreview() {
    ChangeIconScreen(navController = rememberNavController())
}
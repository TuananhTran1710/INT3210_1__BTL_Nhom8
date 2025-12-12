package com.example.wink.ui.features.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen

data class ExploreItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val colorStart: Color,
    val colorEnd: Color,
    val isFullWidth: Boolean = false
)

@Composable
fun ExploreScreen(
    navController: NavController
) {
    val exploreItems = listOf(
        ExploreItem(
            id = "tips",
            title = "Bí kíp tán gái",
            description = "Tuyệt chiêu thả thính & tâm lý",
            icon = Icons.Default.MenuBook,
            // Màu Hồng Đỏ Đậm -> Tím Hồng
            colorStart = Color(0xFFEC407A),
            colorEnd = Color(0xFFAB47BC),
            isFullWidth = true
        ),
        ExploreItem(
            id = "quiz",
            title = "Quiz EQ",
            description = "Đo trình độ EQ",
            icon = Icons.Default.Psychology,
            // Xanh Dương Đậm -> Xanh Nhạt
            colorStart = Color(0xFF4facfe),
            colorEnd = Color(0xFF00f2fe)
        ),
        ExploreItem(
            id = "games",
            title = "Minigames",
            description = "AI vs Human",
            icon = Icons.Default.SportsEsports,
            // Cam Đậm -> Vàng Cam
            colorStart = Color(0xFFff9966),
            colorEnd = Color(0xFFff5e62)
        ),
        ExploreItem(
            id = "tarot",
            title = "Bói Tarot",
            description = "Thông điệp vũ trụ",
            icon = Icons.Default.AutoAwesome,
            // Tím Đậm -> Xanh Tím
            colorStart = Color(0xFF667eea),
            colorEnd = Color(0xFF764ba2)
        ),
        ExploreItem(
            id = "shop",
            title = "Cửa hàng",
            description = "Dùng điểm RIZZ để mua vật phẩm",
            icon = Icons.Default.Storefront,
            // Xanh Lá Đậm -> Xanh Ngọc
            colorStart = Color(0xFF0ba360),
            colorEnd = Color(0xFF3cba92)
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)) {
                    Text(
                        text = "Khám phá",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Nâng cấp bản thân & Giải trí cùng AI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- LIST ITEMS ---
            items(exploreItems, span = { item ->
                GridItemSpan(if (item.isFullWidth) 2 else 1)
            }) { item ->
                ExploreCard(item) {
                    when (item.id) {
                        "tips" -> navController.navigate(Screen.Tips.route)
                        "quiz" -> navController.navigate(Screen.Quiz.route)
                        "tarot" -> navController.navigate(Screen.TarotHub.route)
                        "shop"  -> navController.navigate(Screen.ChangeIcon.route)
                        "games" -> navController.navigate(Screen.HumanAiGame.route)
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ExploreCard(
    item: ExploreItem,
    onClick: () -> Unit
) {
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.3f),
        offset = Offset(2f, 2f),
        blurRadius = 4f
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (item.isFullWidth) 150.dp else 170.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(item.colorStart, item.colorEnd),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
            )

            // Nội dung chính
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (item.isFullWidth) 22.sp else 20.sp,
                            shadow = textShadow
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            shadow = textShadow
                        ),
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
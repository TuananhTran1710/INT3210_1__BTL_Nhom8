package com.example.wink.ui.features.explore

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wink.ui.navigation.Screen

// Data Model
data class CategoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color // Màu chủ đạo của Icon
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController
) {
    // List tiện ích (Bỏ Game ra để làm Hero)
    val categories = listOf(
        CategoryItem("tips", "Bí kíp tán gái", "Thả thính & tâm lý", Icons.Default.MenuBook, Color(0xFFE91E63)), // Pink
        CategoryItem("quiz", "Quiz EQ", "Đo chỉ số cảm xúc", Icons.Default.Psychology, Color(0xFF03A9F4)), // Blue
        CategoryItem("tarot", "Bói Tình Yêu", "Giải mã tình duyên", Icons.Outlined.AutoAwesome, Color(0xFF9C27B0)), // Purple
        CategoryItem("shop", "Cửa hàng", "Đổi điểm RIZZ", Icons.Default.Storefront, Color(0xFF4CAF50))  // Green
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Khám phá",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HERO SECTION: HUMAN VS AI (Poster Game) ---
            item {
                HeroGameCard(
                    onClick = { navController.navigate(Screen.HumanAiGame.route) }
                )
            }

            // --- SECTION TITLE ---
            item {
                Text(
                    text = "Tiện ích khác",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // --- GRID ITEMS (Adaptive Cards) ---
            items(categories.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        AdaptiveCategoryCard(
                            item = item,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (item.id) {
                                    "tips" -> navController.navigate(Screen.Tips.route)
                                    "quiz" -> navController.navigate(Screen.Quiz.route)
                                    "tarot" -> navController.navigate(Screen.TarotHub.route)
                                    "shop"  -> navController.navigate(Screen.ChangeIcon.route)
                                }
                            }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

// --- HERO CARD: Giữ nguyên vẻ "Gaming" (Dark Gradient) cho cả 2 chế độ ---
// Lý do: Đây là Poster/Cover Art của game, nó cần nổi bật và ngầu.
@Composable
fun HeroGameCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Để gradient hiện ra
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Gradient (Luôn tối để chữ trắng nổi bật)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF002A88), Color(0xFFC745A8)), // Deep Purple -> Black
                            start = Offset(0f, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
            )

            // Decoration Icon
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = -20.dp)
            )

            // Nội dung
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary, // Dùng màu Primary của theme
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "HOT GAME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Human or AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
                Text(
                    text = "Thử thách phân biệt hành vi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nút hành động
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Quất luôn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- ADAPTIVE CARD: Tự đổi màu theo Light/Dark Mode ---
@Composable
fun AdaptiveCategoryCard(
    item: CategoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // KEY CHANGE: Dùng token màu của Material Theme
            // surfaceContainer: Màu nền chuẩn cho card trong M3 (Xám nhạt ở Light, Xám đậm ở Dark)
            // Nếu bản compose cũ chưa có surfaceContainer, dùng surfaceVariant
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat style hiện đại
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Icon với nền dynamic
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.15f)), // Nền icon theo màu chủ đạo pha loãng
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconColor, // Icon giữ nguyên màu brand
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text tự động đổi màu
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface, // Tự động Đen (Light) / Trắng (Dark)
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Màu phụ (Xám) tự thích ứng
                    maxLines = 1
                )
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewExploreLight() {
    MaterialTheme(colorScheme = lightColorScheme()) { // Giả lập Light Theme
        ExploreScreen(rememberNavController())
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewExploreDark() {
    MaterialTheme(colorScheme = darkColorScheme()) { // Giả lập Dark Theme
        ExploreScreen(rememberNavController())
    }
}
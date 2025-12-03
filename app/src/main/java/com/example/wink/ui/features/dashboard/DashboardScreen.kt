package com.example.wink.ui.features.dashboard

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import com.example.wink.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.annotation.DrawableRes



enum class FeatureType {
    SECRET_BOOK, // Bí kíp
    CHANGE_ICON, // Đổi icon
    TAROT        // Bói Tarot
}
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Main content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                DashboardHeader()
            }

            // Weekly Calendar
            item {
                WeeklyCalendar(
                    streakDays = uiState.dailyStreak,
                    onDayClick = { viewModel.onEvent(DashboardEvent.OnDailyCheckIn) }
                )
            }

            // RIZZ Points Card
            item {
                RizzPointsCard(
                    points = uiState.rizzPoints,
                    streakDays = uiState.dailyStreak,
                    attended = uiState.hasDailyCheckIn,
                    onStreakClick = {
                        viewModel.onEvent(DashboardEvent.OnDailyCheckIn) }
                )
            }

            // AI Chat Feature
            item {
                AIFeatureCard(
                    onClick = { viewModel.onEvent(DashboardEvent.OnStartAIChat) }
                )
            }

            // Daily Tasks
            item {
                TasksSection(
                    onTaskClick = { viewModel.onEvent(DashboardEvent.OnCompleteTask) },
                    onGameClick = { viewModel.onEvent(DashboardEvent.OnPlayGame) }
                )
            }
            item {
                ProgressFeaturesSection(
                    onFeatureClick = { featureType ->
                        when (featureType) {
                            FeatureType.SECRET_BOOK -> {
                                navController.navigate("secret_book")
                            }

                            FeatureType.CHANGE_ICON -> {
                                navController.navigate("change_icon")
                            }

                            FeatureType.TAROT -> {
                                navController.navigate("tarot")
                            }
                        }

                    }
                )
            }

        }

    }
}

@Composable
private fun DashboardHeader() {
    Text(
        text = "TRANG CHỦ",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WeeklyCalendar(
    streakDays: Int,
    onDayClick: () -> Unit
) {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEachIndexed { index, day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.clickable { onDayClick() }
            ) {
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    if (index < streakDays) {
                        Icon(
                            painter = painterResource(id = R.drawable.fire1),
                            contentDescription = null,
                            tint = Color.Unspecified, // Giữ nguyên màu gốc của SVG
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.fire2),
                            contentDescription = null,
                            tint = Color.Unspecified, // Giữ nguyên màu gốc của SVG
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RizzPointsCard(
    points: Int,
    streakDays: Int,
    attended: Boolean,
    onStreakClick: () -> Unit
) {
    // 1. Định nghĩa màu Gradient giống Figma
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD6E8), // Màu Hồng phấn (Góc trên trái)
            Color(0xFFC5B0FF)  // Màu Tím nhạt (Góc dưới phải)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // 2. Dùng Box để dễ dàng vẽ nền Gradient
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)) // Bo góc 20dp
            .background(brush = brush)       // <--- Set gradient ở đây
            .clickable { onStreakClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Text tiêu đề
            Text(
                text = "Tổng điểm RIZZ",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )

            // Số điểm to đùng
            Text(
                text = points.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // 3. Thanh Streak bên trong (Màu đen mờ - giống Figma)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Màu đen mờ 10% (0.1f)
                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon lửa (Nền trắng mờ)
                    Box(
                        modifier = Modifier
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (attended) {
                            Icon(
                                painter = painterResource(id = R.drawable.fire1),
                                contentDescription = null,
                                tint = Color.Unspecified, // Giữ nguyên màu gốc của SVG
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.fire2),
                                contentDescription = null,
                                tint = Color.Unspecified, // Giữ nguyên màu gốc của SVG
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "$streakDays ngày",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Streak đăng nhập",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Nút Điểm danh
                Button(
                    onClick = onStreakClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Điểm danh",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
@Composable
private fun AIFeatureCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8C5FF)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "AI crush",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Lan Anh",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Bắt đầu hội thoại ngay",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF9800),
                                Color(0xFFFF5722)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

@Composable
private fun TasksSection(
    onTaskClick: () -> Unit,
    onGameClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily Task Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onTaskClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nhiệm vụ hôm nay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Game Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onGameClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Game: AI hay thật?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProgressFeaturesSection(
    onFeatureClick: (FeatureType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bí kíp đang khóa
        ProgressFeatureCard(
            title = "Bí kíp đang khóa",
            R.drawable.book,
            onClick = {onFeatureClick(FeatureType.SECRET_BOOK)}
        )

        // Thay đổi icon
        ProgressFeatureCard(
            title = "Thay đổi icon",
            R.drawable.change,
            onClick = {onFeatureClick(FeatureType.CHANGE_ICON)}
        )

        // Bói Tarot
        ProgressFeatureCard(
            title = "Bói Tarot",
            R.drawable.change,
            onClick = { onFeatureClick(FeatureType.TAROT) }
        )
    }
}

@Composable
private fun ProgressFeatureCard(
    title: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified, // Giữ nguyên màu gốc của SVG
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = true
            )
            BottomNavItem(
                icon = Icons.Default.ChatBubble,
                label = "Chat",
                isSelected = false
            )
            BottomNavItem(
                icon = Icons.Default.Group,
                label = "Social",
                isSelected = false
            )
            BottomNavItem(
                icon = Icons.Default.Explore,
                label = "Explore",
                isSelected = false
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = false
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF9C27B0) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) Color(0xFF9C27B0) else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
private fun DashboardHeaderPreview() {
    DashboardHeader()
}

@Preview(showBackground = true)
@Composable
private fun WeeklyCalendarPreview() {
    WeeklyCalendar(
        streakDays = 3,
        onDayClick = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun RizzPointsCardPreview() {
    RizzPointsCard(
        points = 1250,
        streakDays = 3,
        attended = false,
        onStreakClick = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun AIFeatureCardPreview() {
    AIFeatureCard(
        onClick = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun TasksSectionPreview() {
    TasksSection(
        onTaskClick = { },
        onGameClick = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun ProgressFeaturesSectionPreview() {
    ProgressFeaturesSection(
        onFeatureClick = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    BottomNavigationBar()
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//private fun DashboardScreenContentPreview() {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5))
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .weight(1f)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            item { DashboardHeader() }
//            item { WeeklyCalendar(streakDays = 3, onDayClick = { }) }
//            item { RizzPointsCard(points = 1250, streakDays = 3, attended = true, onStreakClick = { }) }
//            item { AIFeatureCard(onClick = { }) }
//            item { TasksSection(onTaskClick = { }, onGameClick = { }) }
//            item { ProgressFeaturesSection(onFeatureClick = { }) }
//        }
//        BottomNavigationBar()
//    }
//}
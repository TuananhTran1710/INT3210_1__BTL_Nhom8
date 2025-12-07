package com.example.wink.ui.features.tarot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.wink.ui.navigation.Screen

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotHubScreen(
    navController: NavController,
    viewModel: TarotHubViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navEvents.collectLatest { event ->
            when (event) {
                is TarotHubNav.OpenFeature -> {
                    when (event.type) {
                        LoveFortuneType.BY_NAME ->
                            navController.navigate(Screen.TarotName.route)

                        LoveFortuneType.ZODIAC ->
                            navController.navigate(Screen.TarotZodiac.route)

                        LoveFortuneType.TAROT_CARD ->
                            navController.navigate(Screen.TarotCard.route)
                    }
                }
            }
        }
    }


    // Dialog: không đủ điểm Rizz
    if (state.showNotEnoughDialogFor != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text("Không đủ Rizz") },
            text = { Text("Bạn không đủ điểm RIZZ để dùng chức năng này.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Dialog: xác nhận dùng 50 Rizz
    state.confirmingFor?.let { type ->
        val feature = state.features.first { it.type == type }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            title = { Text("Hết lượt miễn phí!") },
            text = {
                Text(
                    "Bạn đã dùng hết lượt miễn phí hôm nay.\n" +
                            "Dùng ${feature.price} Rizz để chơi tiếp nhé?"
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmSpendRizz() }) {
                    Text("Chốt đơn")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text("Thôi")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bói Tình Yêu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF050B1A))  // nền tối giống thiết kế
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.features.forEach { feature ->
                    LoveFortuneItem(
                        feature = feature,
                        onClick = { viewModel.onFeatureClick(feature.type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoveFortuneItem(
    feature: TarotSubFeatureUi,
    onClick: () -> Unit
) {
    // Gradient cho từng loại
    val gradient = when (feature.type) {
        LoveFortuneType.BY_NAME -> Brush.horizontalGradient(
            listOf(Color(0xFFFF4081), Color(0xFF8E24AA))
        )

        LoveFortuneType.ZODIAC -> Brush.horizontalGradient(
            listOf(Color(0xFF5C6BC0), Color(0xFF283593))
        )

        LoveFortuneType.TAROT_CARD -> Brush.horizontalGradient(
            listOf(Color(0xFFFF7043), Color(0xFF5D4037))
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon tròn bên trái (placeholder, cậu có thể đổi thành Image riêng)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❤", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = feature.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = feature.description,
                        fontSize = 13.sp,
                        color = Color(0xFFEEEEEE),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge: "Miễn phí" hoặc "50 Rizz"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (feature.usedFreeToday) Color(0xFFFFC107) else Color(0xFF388E3C)
                ) {
                    Text(
                        text = if (feature.usedFreeToday) "${feature.price} Rizz" else "Miễn phí",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

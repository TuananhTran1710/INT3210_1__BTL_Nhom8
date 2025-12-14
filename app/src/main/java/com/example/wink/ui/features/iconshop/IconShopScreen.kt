package com.example.wink.ui.features.iconshop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconShopScreen(
    navController: NavController,
    viewModel: IconShopViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi icon", fontWeight = FontWeight.Bold) },
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
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {

            // Thẻ hiển thị RIZZ
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Điểm RIZZ của bạn",
                            fontSize = 14.sp,
                            color = colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = state.rizzPoints.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onTertiaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thông báo lỗi (nếu có)
            state.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lưới icon 4 cột
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.icons) { icon ->
                    IconItem(
                        item = icon,
                        onClick = { viewModel.onIconClicked(icon.id) }
                    )
                }
            }
        }

        // --- THÊM PHẦN NÀY VÀO CUỐI CÙNG TRONG SCAFFOLD ---

        if (state.showRestartDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelChangeIcon() },
                title = { Text(text = "Thay đổi biểu tượng") },
                text = {
                    Text("Hiệu ứng sẽ chỉ áp dụng trong lần chạy sau. Bạn có muốn thoát ngay bây giờ không?")
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmChangeIcon() }
                    ) {
                        Text("Đồng ý", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.cancelChangeIcon() }
                    ) {
                        Text("Để sau")
                    }
                }
            )
        }
    }
}

@Composable
private fun IconItem(
    item: IconItemUi,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                // Xóa background màu cũ đi
                // .background(item.color)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // 1. HIỂN THỊ ẢNH ICON
            Image(
                painter = painterResource(id = item.iconResId), // Load ảnh từ ID
                contentDescription = null,
                contentScale = ContentScale.Crop, // Co dãn ảnh cho vừa khung
                modifier = Modifier.matchParentSize()
            )

            // 2. LỚP PHỦ KHI ĐƯỢC CHỌN (Overlay)
            if (item.isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        // Phủ một lớp màu trắng mờ hoặc đen mờ lên trên ảnh để biết đang chọn
                        .background(Color.Black.copy(alpha = 0.3f))
                    // Hoặc dùng viền (border) nếu bạn không muốn làm tối ảnh
                )

                // Thêm icon Check ở giữa để rõ ràng hơn (Optional)
                Icon(
                    imageVector = Icons.Default.Check, // Cần import Icons.Default.Check
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. TRẠNG THÁI KHÓA / TIM (SỞ HỮU)
            // Phần này giữ nguyên logic cũ nhưng có thể chỉnh màu cho dễ nhìn trên nền ảnh
            if (item.isOwned) {
                // Nếu đã sở hữu nhưng không chọn -> Không cần hiện icon tim đè lên ảnh cho rối
                // Hoặc chỉ hiện một chấm nhỏ góc phải
            } else {
                // Nếu chưa mua -> Hiện cái ổ khóa mờ ở giữa
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f)), // Làm tối ảnh bị khóa
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (!item.isOwned) {
            Text(
                text = "${item.price} RP",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = if (item.isSelected) "Đang dùng" else "Đã mua",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = if (item.isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }
    }
}

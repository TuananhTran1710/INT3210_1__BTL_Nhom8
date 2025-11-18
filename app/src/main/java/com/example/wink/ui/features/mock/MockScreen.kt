package com.example.wink.ui.features.mock // Gói của bạn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.* // <-- Quan trọng: Đảm bảo import từ material3
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * Đây là một màn hình giả lập (mock) để trình diễn các component M3.
 * Bạn có thể gọi Composable này từ AppNavigation của mình để xem nó.
 */
@OptIn(ExperimentalMaterial3Api::class) // Cần thiết cho TopAppBar và Scaffold
@Composable
fun MockM3Screen() {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        // 1. TopAppBar (Thanh tiêu đề)
        topBar = {
            TopAppBar(
                title = { Text("Wink") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Yêu thích")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Chat ngay") },
                icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                onClick = { }
            )
        }
    ) { innerPadding ->

        // 3. Nội dung màn hình
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Item 1: TextField
            item {
                Text(
                    "Đăng nhập để luyện tập",
                    style = MaterialTheme.typography.titleLarge, // Dùng typography từ M3 Theme
                    color = MaterialTheme.colorScheme.primary // Dùng màu từ M3 Theme
                )
            }

            item {
                // 4. TextField (Kiểu Outlined)
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Tên đăng nhập") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // 5. TextField (Kiểu Filled - mặc định của M3)
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Item 2: Các loại Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // 6. Button (Kiểu Filled - mặc định)
                    Button(onClick = { /*TODO*/ }) {
                        Text("Đăng nhập")
                    }

                    // 7. OutlinedButton (Kiểu viền)
                    OutlinedButton(onClick = { /*TODO*/ }) {
                        Text("Đăng ký")
                    }

                    // 8. TextButton (Kiểu chữ)
                    TextButton(onClick = { /*TODO*/ }) {
                        Text("Quên mật khẩu?")
                    }
                }
            }

            // Item 3: Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        // 1. Đặt màu NỀN là tertiaryContainer
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,

                        // 2. (Tùy chọn) Đặt màu CHỮ/ICON mặc định là onTertiaryContainer
                        //    Nếu không đặt, nó sẽ tự động chọn màu phù hợp.
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Tình huống giả lập",
                            style = MaterialTheme.typography.titleMedium
                            // Màu của Text này sẽ tự động lấy từ contentColor ở trên
                        )
                        Text(
                            "Bạn thấy crush đang đi một mình. Bạn sẽ nói gì?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Bài học hôm nay: EQ là gì?",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tích lũy 10 điểm RIZZ để mở khóa bài học này. " +
                                    "Hãy bắt đầu bằng cách đăng nhập hàng ngày!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                ElevatedCard( // Card có hiệu ứng đổ bóng và nâng lên
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Tình huống giả lập",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Bạn vô tình làm đổ nước lên người crush. Bạn sẽ nói gì?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
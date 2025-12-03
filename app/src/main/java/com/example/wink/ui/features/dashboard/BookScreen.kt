package com.example.wink.ui.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class BookChapter(
    val id: String,
    val title: String,
    val description: String,
    val requiredRizz: Int,
    val isUnlocked: Boolean,
    val icon: ImageVector,
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    navController: NavController
) {
    var selectedChapter by remember { mutableStateOf<BookChapter?>(null) }
    val currentRizz = 1250 // This should come from ViewModel
    
    val chapters = listOf(
        BookChapter(
            id = "intro",
            title = "Giới thiệu về EQ",
            description = "Tìm hiểu về trí tuệ cảm xúc và tầm quan trọng của nó",
            requiredRizz = 0,
            isUnlocked = true,
            icon = Icons.Default.Star,
            content = "Trí tuệ cảm xúc (EQ) là khả năng nhận biết, hiểu và quản lý cảm xúc của bản thân và người khác. Đây là kỹ năng quan trọng trong việc xây dựng mối quan hệ tốt đẹp."
        ),
        BookChapter(
            id = "listening",
            title = "Nghệ thuật lắng nghe",
            description = "Học cách lắng nghe tích cực và hiệu quả",
            requiredRizz = 500,
            isUnlocked = currentRizz >= 500,
            icon = Icons.Default.Book,
            content = "Lắng nghe không chỉ là nghe những gì người khác nói, mà còn là hiểu được cảm xúc và ý nghĩa sâu xa đằng sau lời nói. Hãy tập trung hoàn toàn vào người đang nói."
        ),
        BookChapter(
            id = "conversation",
            title = "Nghệ thuật bắt chuyện",
            description = "Cách bắt đầu và duy trì cuộc trò chuyện thú vị",
            requiredRizz = 800,
            isUnlocked = currentRizz >= 800,
            icon = Icons.Default.Lock,
            content = "Bắt chuyện thành công bắt đầu từ việc quan sát môi trường xung quanh và tìm điểm chung. Hãy đặt câu hỏi mở để khuyến khích người khác chia sẻ."
        ),
        BookChapter(
            id = "body_language",
            title = "Ngôn ngữ cơ thể",
            description = "Hiểu và sử dụng ngôn ngữ cơ thể hiệu quả",
            requiredRizz = 1200,
            isUnlocked = currentRizz >= 1200,
            icon = Icons.Default.Lock,
            content = "70% giao tiếp là ngôn ngữ cơ thể. Tư thế đứng thẳng, ánh mắt giao tiếp và nụ cười chân thành sẽ tạo ấn tượng tích cực."
        ),
        BookChapter(
            id = "confidence",
            title = "Xây dựng tự tin",
            description = "Cách phát triển và duy trì sự tự tin",
            requiredRizz = 1500,
            isUnlocked = currentRizz >= 1500,
            icon = Icons.Default.Lock,
            content = "Tự tin không phải là cảm thấy mình hoàn hảo, mà là chấp nhận bản thân và biết rằng mình có giá trị. Thực hành và kinh nghiệm sẽ giúp bạn tự tin hơn."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bí Kíp Đang Khóa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (selectedChapter == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "📚 Sách Hướng Dẫn EQ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "RIZZ hiện tại: $currentRizz",
                    fontSize = 14.sp,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chapters) { chapter ->
                        ChapterCard(
                            chapter = chapter,
                            currentRizz = currentRizz,
                            onClick = { 
                                if (chapter.isUnlocked) {
                                    selectedChapter = chapter
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // Show chapter content
            selectedChapter?.let { chapter ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = chapter.icon,
                            contentDescription = null,
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = chapter.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Text(
                            text = chapter.content,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = { selectedChapter = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        )
                    ) {
                        Text(
                            text = "Quay lại",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: BookChapter,
    currentRizz: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isUnlocked) 
                Color(0xFFE8F5E8) else Color(0xFFE0E0E0)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (chapter.isUnlocked) chapter.icon else Icons.Default.Lock,
                contentDescription = null,
                tint = if (chapter.isUnlocked) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chapter.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (chapter.isUnlocked) Color.Black else Color.Gray
                )
                Text(
                    text = chapter.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                if (!chapter.isUnlocked) {
                    Text(
                        text = "Cần ${chapter.requiredRizz} RIZZ (còn thiếu ${chapter.requiredRizz - currentRizz})",
                        fontSize = 11.sp,
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            if (chapter.isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Unlocked",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookScreenPreview() {
    BookScreen(navController = rememberNavController())
}
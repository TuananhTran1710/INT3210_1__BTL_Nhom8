package com.example.wink.ui.features.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLogout() // Gọi callback ra ngoài thay vì tự navigate
        }
    }

    // Launcher chọn ảnh
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onAvatarSelected(uri)
    }

    // Xử lý thông báo (Toast)
    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onSaveProfile() }, enabled = !state.isLoading) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Lưu", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. PHẦN AVATAR (Có chức năng chọn ảnh)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        // Hiển thị ảnh: Ưu tiên ảnh mới chọn (Uri), nếu không thì dùng ảnh cũ (Url)
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(100.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        ) {
                            val model = state.selectedAvatarUri ?: state.currentAvatarUrl
                            if (model.toString().isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(model).crossfade(true).build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Icon Edit nhỏ
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp).offset(x = 4.dp, y = 4.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.background)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Thay đổi ảnh", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            // 2. FORM NHẬP LIỆU
            SettingsGroup(title = "Thông tin cá nhân") {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    label = { Text("Tên hiển thị") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Person, null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Gmail (Đăng nhập)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Email, null) }
                )
            }

            // 3. NÚT ĐĂNG XUẤT
            Button(
                onClick = { viewModel.onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Helper Composable
@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        content()
    }
}
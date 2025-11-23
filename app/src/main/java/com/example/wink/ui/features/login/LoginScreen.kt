package com.example.wink.ui.features.login

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.wink.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel() // Lấy ViewModel từ Hilt
) {

    val uiState by viewModel.uiState.collectAsState()
    // Lấy backStackEntry hiện tại (chính là Login)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val savedStateHandle = navBackStackEntry?.savedStateHandle
    val context = LocalContext.current

    // Lắng nghe sự kiện điều hướng
    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is LoginViewModel.NavigationEvent.NavigateToMain -> {

                    Toast.makeText(
                        context,
                        "Đăng nhập thành công!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Chuyển sang Main Graph và xóa Auth Graph khỏi backstack
                    navController.navigate(Screen.MAIN_GRAPH_ROUTE) {
                        popUpTo(Screen.AUTH_GRAPH_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    // Đọc dữ liệu prefill từ Signup trả về
    LaunchedEffect(savedStateHandle) {
        val prefillEmail = savedStateHandle?.get<String>("prefill_email")
        val prefillPass = savedStateHandle?.get<String>("prefill_pass")

        if (prefillEmail != null && prefillPass != null) {
            // Bắn event vào ViewModel để cập nhật UI state
            viewModel.onEvent(LoginEvent.OnEmailChanged(prefillEmail))
            viewModel.onEvent(LoginEvent.OnPasswordChanged(prefillPass))

            // Xoá để tránh handle lại lần nữa khi recomposition
            savedStateHandle.remove<String>("prefill_email")
            savedStateHandle.remove<String>("prefill_pass")
        }
    }


    // Giao diện
    if (uiState.isCheckingSession) {
        // 👇 ĐANG KIỂM TRA PHIÊN CŨ → KHÔNG SHOW LOGIN
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()   // hoặc bỏ luôn cho trắng tinh cũng được
        }
    }
    else {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Đăng nhập",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(32.dp))

                // Email
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEvent(LoginEvent.OnEmailChanged(it)) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = uiState.pass,
                    onValueChange = { viewModel.onEvent(LoginEvent.OnPasswordChanged(it)) },
                    label = { Text("Mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(Modifier.height(24.dp))

                // Nút Login
                Button(
                    onClick = { viewModel.onEvent(LoginEvent.OnLoginClicked) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isLoading // Vô hiệu hóa khi đang tải
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Đăng nhập")
                    }
                }

                // Hiển thị lỗi (nếu có)
                uiState.error?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Nút chuyển sang Đăng ký
                TextButton(onClick = {
                    navController.navigate(Screen.Signup.route) // Chuyển sang màn Signup
                }) {
                    Text("Chưa có tài khoản? Đăng ký ngay")
                }
            }
        }
    }
}
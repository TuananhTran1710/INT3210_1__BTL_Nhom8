package com.example.wink.ui.features.signup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wink.ui.navigation.Screen

@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    // Xử lý điều hướng
    LaunchedEffect(true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is SignupViewModel.NavigationEvent.NavigateToOnboarding -> {
                    // Đăng ký xong -> Đi tới Onboarding (Chọn gu)
                    navController.navigate(Screen.Onboarding.route) {
                        // Xóa sạch backstack để user không back lại màn đăng ký được
                        popUpTo(Screen.AUTH_GRAPH_ROUTE) { inclusive = true }
                    }
                }
                is SignupViewModel.NavigationEvent.NavigateBackToLogin -> {

                    // HIỆN TOAST —— y như SignupActivity.this
                    Toast.makeText(
                        context,
                        "Đăng ký thành công!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 1. Gửi email & pass cho back stack entry của Login
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("prefill_email", event.email)

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("prefill_pass", event.pass)

                    // 3. Quay lại Login
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tạo tài khoản",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 1. Tên người dùng
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.onEvent(SignupEvent.OnUsernameChanged(it)) },
                label = { Text("Tên hiển thị") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(SignupEvent.OnEmailChanged(it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Mật khẩu
            OutlinedTextField(
                value = uiState.pass,
                onValueChange = { viewModel.onEvent(SignupEvent.OnPasswordChanged(it)) },
                label = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Xác nhận mật khẩu
            OutlinedTextField(
                value = uiState.confirmPass,
                onValueChange = { viewModel.onEvent(SignupEvent.OnConfirmPasswordChanged(it)) },
                label = { Text("Nhập lại mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                isError = uiState.error != null && uiState.pass != uiState.confirmPass
            )

            // Hiển thị lỗi
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Nút Đăng ký
            Button(
                onClick = { viewModel.onEvent(SignupEvent.OnSignupClicked) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Đăng ký ngay")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Nút quay lại Login
            TextButton(onClick = { viewModel.onEvent(SignupEvent.OnLoginNavClicked) }) {
                Text("Đã có tài khoản? Đăng nhập")
            }
        }
    }
}
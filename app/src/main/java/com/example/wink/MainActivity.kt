package com.example.wink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope // 1. Import lifecycleScope
import com.example.wink.data.repository.UserRepository // 2. Import UserRepository
import com.example.wink.ui.navigation.AppNavigation
import com.example.wink.ui.theme.WinkTheme
import com.example.wink.util.AppIconManager // 3. Import AppIconManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // 4. Import launch
import javax.inject.Inject // 5. Import Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 6. Inject UserRepository để lấy thông tin icon người dùng đã chọn
    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 7. Gọi hàm đồng bộ icon ngay khi App khởi động
        syncAppIcon()

        setContent {
            WinkTheme { // Sửa AppTheme thành WinkTheme (hoặc tên theme của bạn)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    // 8. Hàm logic đồng bộ icon
    private fun syncAppIcon() {
        // Sử dụng lifecycleScope để chạy coroutine (vì hàm loadIconShopState là suspend)
        lifecycleScope.launch {
            try {
                // Lấy thông tin từ database: (danh sách đã mua, icon đang chọn)
                val (_, selectedId) = userRepository.loadIconShopState()

                // Nếu người dùng có chọn icon (selectedId khác null)
                if (selectedId != null) {
                    // Gọi hàm thay đổi Alias trong AppIconManager
                    // Hàm này sẽ set lại state Enabled/Disabled cho các Alias trong Manifest
                    AppIconManager.changeAppIcon(this@MainActivity, selectedId)

                    // Lưu ý: Ở đây ta KHÔNG gọi restartApp(), vì app đang mở rồi.
                    // PackageManager sẽ tự ghi nhận thay đổi, lần sau mở lại hoặc ra màn hình home sẽ thấy.
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
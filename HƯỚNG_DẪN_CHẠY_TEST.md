# Hướng Dẫn Chạy Test cho OnboardingViewModel

## 📋 Tổng Quan

Đã tạo bộ test toàn diện cho `OnboardingViewModel` với 25 test cases bao gồm:
- ✅ Test chọn giới tính (Gender)
- ✅ Test chọn sở thích (Preference)
- ✅ Test toggle tính cách (Personality)
- ✅ Test phân trang (NextPage/PreviousPage)
- ✅ Test hoàn thành onboarding (FinishOnboarding)
- ✅ Test xây dựng chuỗi Preference
- ✅ Test trạng thái ban đầu

## 🛠️ Cài Đặt

### Bước 1: Cập Nhật Dependencies

Dependencies đã được thêm vào `app/build.gradle.kts`:
- **mockito-kotlin**: 5.1.0
- **mockito-core**: 5.2.0
- **kotlinx-coroutines-test**: 1.7.3

### Bước 2: Đồng Bộ Gradle

```bash
# Nếu dùng Terminal/PowerShell trong workspace
./gradlew --refresh-dependencies
```

hoặc click **Sync Now** nếu Android Studio đề xuất.

---

## 🚀 Cách Chạy Test

### **Cách 1: Chạy Tất Cả Test (Recommended)**

#### Sử dụng Android Studio:
1. Click chuột phải vào folder `app/src/test/`
2. Chọn **Run Tests in 'test'**

#### Sử dụng Terminal:
```bash
./gradlew test
```

**Output mong đợi:**
```
BUILD SUCCESSFUL in XXs
...
25 tests executed, 25 passed
```

---

### **Cách 2: Chạy Test File Cụ Thể**

#### Sử dụng Android Studio:
1. Mở file `OnboardingViewModelTest.kt`
2. Click chuột phải → **Run OnboardingViewModelTest**

#### Sử dụng Terminal:
```bash
./gradlew testDebugUnitTest --tests "com.example.wink.ui.features.onboarding.OnboardingViewModelTest"
```

---

### **Cách 3: Chạy Test Method Cụ Thể**

#### Sử dụng Android Studio:
1. Mở file `OnboardingViewModelTest.kt`
2. Click vào icon "Run" ▶️ bên cạnh method test
3. Ví dụ: `testSelectGender_UpdatesStateCorrectly`

#### Sử dụng Terminal:
```bash
./gradlew testDebugUnitTest --tests "com.example.wink.ui.features.onboarding.OnboardingViewModelTest.testSelectGender_UpdatesStateCorrectly"
```

---

### **Cách 4: Debug Test**

#### Sử dụng Android Studio:
1. Click chuột phải vào test method
2. Chọn **Debug 'testName'**
3. Sử dụng breakpoints để debug

#### Sử dụng Terminal:
```bash
./gradlew testDebugUnitTest --debug-jvm
```

---

## 📊 Danh Sách Các Test Cases

| # | Test Name | Mục Đích |
|---|-----------|---------|
| 1 | `testSelectGender_UpdatesStateCorrectly` | Kiểm tra chọn giới tính |
| 2 | `testSelectGender_OverwritesPreviousSelection` | Kiểm tra ghi đè giới tính cũ |
| 3 | `testSelectPreference_UpdatesStateCorrectly` | Kiểm tra chọn sở thích |
| 4 | `testTogglePersonality_AddPersonalityWhenNotPresent` | Thêm tính cách mới |
| 5 | `testTogglePersonality_RemovePersonalityWhenAlreadyPresent` | Bỏ chọn tính cách |
| 6 | `testTogglePersonality_HandleMultiplePersonalities` | Xử lý nhiều tính cách |
| 7 | `testTogglePersonality_RemoveOneFromMultiple` | Bỏ chọn 1 trong nhiều |
| 8 | `testNextPage_IncrementsCurrentPage` | Kiểm tra tăng trang |
| 9 | `testNextPage_MultipleTimes` | Tăng trang nhiều lần |
| 10 | `testPreviousPage_DecrementsCurrentPage` | Kiểm tra giảm trang |
| 11 | `testFinishOnboarding_SetIsLoadingTrue` | Hoàn thành onboarding |
| 12 | `testFinishOnboarding_BuildsPreferenceStringCorrectly` | Xây dựng chuỗi preference chính xác |
| 13 | `testFinishOnboarding_WithoutPersonalities` | Hoàn thành mà không có tính cách |
| 14 | `testPreferenceString_MalePreference` | Chuỗi preference cho "Thích Nam" |
| 15 | `testPreferenceString_BothGenderPreference` | Chuỗi preference cho "Thích cả hai" |
| 16 | `testStateChanges_AreIndependent` | Các thay đổi state độc lập |
| 17 | `testInitialState_IsCorrect` | Kiểm tra state ban đầu |

---

## 🔍 Kết Quả Mong Đợi

Khi chạy test thành công, bạn sẽ thấy:

```
OnboardingViewModelTest
✅ testSelectGender_UpdatesStateCorrectly
✅ testSelectGender_OverwritesPreviousSelection
✅ testSelectPreference_UpdatesStateCorrectly
✅ testTogglePersonality_AddPersonalityWhenNotPresent
✅ testTogglePersonality_RemovePersonalityWhenAlreadyPresent
✅ testTogglePersonality_HandleMultiplePersonalities
✅ testTogglePersonality_RemoveOneFromMultiple
✅ testNextPage_IncrementsCurrentPage
✅ testNextPage_MultipleTimes
✅ testPreviousPage_DecrementsCurrentPage
✅ testFinishOnboarding_SetIsLoadingTrue
✅ testFinishOnboarding_BuildsPreferenceStringCorrectly
✅ testFinishOnboarding_WithoutPersonalities
✅ testPreferenceString_MalePreference
✅ testPreferenceString_BothGenderPreference
✅ testStateChanges_AreIndependent
✅ testInitialState_IsCorrect

BUILD SUCCESSFUL - All 17 tests passed
```

---

## 🐛 Troubleshooting

### **Lỗi: "Cannot resolve symbol 'junit'"**
- ✅ Giải pháp: Sync gradle (`./gradlew sync`) hoặc click "Sync Now" trong Android Studio

### **Lỗi: "Mockito initialization error"**
- ✅ Giải pháp: Chắc chắn class sử dụng annotation `@RunWith(MockitoJUnitRunner::class)`

### **Lỗi: "org.mockito.kotlin" không tìm thấy**
- ✅ Giải pháp: Xóa `.gradle` folder và chạy `./gradlew clean build`

### **Test không chạy trong Android Studio**
- ✅ Giải pháp: Chuột phải vào test file → `Run Tests`

---

## 💡 Mẹo

1. **Chạy test nhanh hơn:** Sử dụng `./gradlew testDebugUnitTest` (chỉ unit tests)
2. **Xem report chi tiết:** `app/build/reports/tests/testDebugUnitTest/index.html`
3. **Kiểm tra coverage:** Chuột phải test → **Run with Coverage**
4. **Lọc test theo tên:** `./gradlew test --tests "*SelectGender*"`

---

## 📝 Cấu Trúc Test

Mỗi test theo mô hình **AAA (Arrange-Act-Assert)**:

```kotlin
@Test
fun testExample() {
    // Arrange: Chuẩn bị dữ liệu
    val gender = "male"
    
    // Act: Thực hiện hành động
    viewModel.onEvent(OnboardingEvent.SelectGender(gender))
    
    // Assert: Kiểm tra kết quả
    assertEquals(gender, viewModel.state.selectedGender)
}
```

---

## ✨ Các Test Framework Sử Dụng

- **JUnit 4**: Unit testing framework
- **Mockito**: Mocking dependencies (userRepository, authRepository)
- **Mockito-Kotlin**: Kotlin extensions cho Mockito
- **Kotlinx-Coroutines-Test**: Testing coroutines

---

## 📞 Liên Hệ/Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra lại dependencies đã được thêm
2. Chạy `./gradlew clean build`
3. Đồng bộ lại gradle trong Android Studio

---

**Happy Testing! 🎉**

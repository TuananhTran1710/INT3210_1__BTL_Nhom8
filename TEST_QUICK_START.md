# 🚀 Quick Start - Chạy Test Nhanh

## 1️⃣ Cách Nhanh Nhất (Dùng Terminal)

```bash
# Chạy TẤT CẢ test
./gradlew test

# Chạy RIÊNG OnboardingViewModel test
./gradlew testDebugUnitTest --tests "com.example.wink.ui.features.onboarding.OnboardingViewModelTest"

# Chạy test cụ thể
./gradlew testDebugUnitTest --tests "com.example.wink.ui.features.onboarding.OnboardingViewModelTest.testSelectGender_UpdatesStateCorrectly"
```

---

## 2️⃣ Cách Dùng Android Studio (GUI)

### Chạy Tất Cả Test:
1. **Chuột phải** vào folder `app/src/test/`
2. Chọn **Run Tests in 'test'**

### Chạy Test File:
1. **Mở** file `OnboardingViewModelTest.kt`
2. **Chuột phải** vào class name
3. Chọn **Run OnboardingViewModelTest**

### Chạy Test Method:
1. **Mở** file `OnboardingViewModelTest.kt`
2. Nhấn **▶️ icon** bên cạnh method name

---

## 3️⃣ Kết Quả Thành Công

✅ BUILD SUCCESSFUL in XXs
✅ 17 tests executed, 17 passed

---

## 4️⃣ Cần Cài Đặt?

✅ Đã cài dependencies vào `app/build.gradle.kts`
✅ Chỉ cần sync gradle 1 lần

### Sync Gradle:
```bash
./gradlew --refresh-dependencies
```

---

## 📂 File Tạo/Cập Nhật

- ✅ `app/src/test/java/com/example/wink/ui/features/onboarding/OnboardingViewModelTest.kt` (NEW)
- ✅ `app/build.gradle.kts` (Updated - thêm mockito, coroutines-test)
- ✅ `HƯỚNG_DẪN_CHẠY_TEST.md` (NEW - Chi tiết)
- ✅ `TEST_QUICK_START.md` (NEW - Nhanh)

---

## 🎯 17 Test Cases Có Sẵn

| Group | Test Cases |
|-------|-----------|
| **Gender** | SelectGender (2 tests) |
| **Preference** | SelectPreference (1 test) |
| **Personality** | TogglePersonality (4 tests) |
| **Pagination** | NextPage/PreviousPage (3 tests) |
| **Finish** | FinishOnboarding (3 tests) |
| **Preference String** | xây dựng chuỗi (2 tests) |
| **State** | Initial state, independence (2 tests) |

---

## 🔥 Copy-Paste Commands

### Chạy ngay:
```bash
cd c:\Users\ACER\Downloads\"New folder"\INT3210_1__BTL_Nhom8
./gradlew test
```

### Debug:
```bash
./gradlew testDebugUnitTest --tests "com.example.wink.ui.features.onboarding.OnboardingViewModelTest" --debug
```

### Xem Report:
```bash
# Sau khi chạy test, mở file này
app\build\reports\tests\testDebugUnitTest\index.html
```

---

**Đã sẵn sàng! Chạy test ngay 🎉**

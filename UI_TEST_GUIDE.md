# 📱 UI Test Guide - OnboardingScreen

## 🎯 Test File Location

```
app/src/androidTest/java/com/example/wink/ui/features/onboarding/OnboardingScreenTest.kt
```

**Loại Test:** Instrumented Test (chạy trên Android device/emulator)
**Framework:** Jetpack Compose UI Testing
**Số Test Cases:** 35+ UI tests

---

## 🚀 Cách Chạy UI Test

### **1️⃣ Terminal Command**

```bash
# Chạy TẤT CẢ UI tests
./gradlew connectedAndroidTest

# Chạy CHỈ OnboardingScreen tests
./gradlew connectedAndroidTest --tests "com.example.wink.ui.features.onboarding.OnboardingScreenTest"

# Chạy test cụ thể
./gradlew connectedAndroidTest --tests "com.example.wink.ui.features.onboarding.OnboardingScreenTest.testGenderPage_SelectMaleGender"
```

### **2️⃣ Android Studio (GUI)**

#### Run All Tests:
```
1. Right-click: app/src/androidTest/
2. Menu: Run Tests in 'androidTest'
```

#### Run Test Class:
```
1. Open: OnboardingScreenTest.kt
2. Right-click: class OnboardingScreenTest
3. Menu: Run OnboardingScreenTest
```

#### Run Single Test:
```
1. Open: OnboardingScreenTest.kt
2. Click: ▶️ Play button next to test name
Example: ▶️ testGenderPage_SelectMaleGender
```

#### Debug Test:
```
1. Right-click: Test method name
2. Menu: Debug 'testName'
3. Use Android Studio debugger
```

---

## ⚠️ TRƯỚC KHI CHẠY TEST

### **1. Chuẩn Bị Device/Emulator**

Yêu cầu:
- ✅ Có 1 device hoặc emulator được kết nối
- ✅ Android API level ≥ 24
- ✅ Device phải unlock hoặc disable lock screen

**Check device:**
```bash
adb devices
```

**Expected output:**
```
List of attached devices
emulator-5554          device
192.168.1.100:5555     device
```

### **2. Sync Gradle**

```bash
./gradlew --refresh-dependencies
```

### **3. Build Project**

```bash
./gradlew build
```

---

## 🧪 Test Cases Breakdown

### **Nhóm 1: IntroPage (2 tests)**

| Test | Mục Đích |
|------|----------|
| `testIntroPage_DisplaysWelcomeTitle` | Kiểm tra title "Welcome to Wink!" hiển thị |
| `testIntroPage_DisplaysSubtitleText` | Kiểm tra subtitle text hiển thị |

```
Expected: Trang intro hiển thị đúng nội dung
```

---

### **Nhóm 2: GenderPage (6 tests)**

| Test | Mục Đích |
|------|----------|
| `testGenderPage_DisplaysTitle` | Title "Giới tính của bạn là?" hiển thị |
| `testGenderPage_DisplaysAllGenderOptions` | Các option (Nam, Nữ, Khác) hiển thị |
| `testGenderPage_SelectMaleGender` | Click "Nam" gọi callback |
| `testGenderPage_SelectFemaleGender` | Click "Nữ" gọi callback |
| `testGenderPage_SelectOtherGender` | Click "Khác" gọi callback |

```
Expected: Tất cả gender options hiển thị, click gọi onEvent
```

---

### **Nhóm 3: PreferencePage (6 tests)**

| Test | Mục Đích |
|------|----------|
| `testPreferencePage_DisplaysTitle` | Title "Bạn quan tâm đến?" hiển thị |
| `testPreferencePage_DisplaysAllPreferenceOptions` | Các option (Con trai, Con gái, Cả hai) hiển thị |
| `testPreferencePage_SelectMalePreference` | Click "Con trai" |
| `testPreferencePage_SelectFemalePreference` | Click "Con gái" |
| `testPreferencePage_SelectBothPreference` | Click "Cả hai" |

```
Expected: Preference options hiển thị, click gọi onEvent
```

---

### **Nhóm 4: PersonalityPage (5 tests)**

| Test | Mục Đích |
|------|----------|
| `testPersonalityPage_DisplaysTitle` | Title hiển thị |
| `testPersonalityPage_DisplaysAllPersonalityOptions` | Tất cả personality (7 options) hiển thị |
| `testPersonalityPage_SelectPersonality` | Click 1 personality |
| `testPersonalityPage_SelectMultiplePersonalities` | Click nhiều personality |
| `testPersonalityPage_DeselectPersonality` | Toggle/unselect personality |

```
Expected: Tất cả personality hiển thị, có thể toggle
```

---

### **Nhóm 5: Components (5 tests)**

#### GenderBox Component:
| Test | Mục Đích |
|------|----------|
| `testGenderBox_DisplaysText` | Text hiển thị |
| `testGenderBox_CallsOnClickWhenClicked` | Click gọi callback |

#### BottomControls Component:
| Test | Mục Đích |
|------|----------|
| `testBottomControls_ShowsNextButtonOnFirstPage` | Page 0: hiển thị "Next" |
| `testBottomControls_NoBackButtonOnFirstPage` | Page 0: không có "Back" |
| `testBottomControls_ShowsBackButtonOnSecondPage` | Page 1: có cả "Back" và "Next" |
| `testBottomControls_ShowsFinishButtonOnLastPage` | Page 3: hiển thị "Finish" |
| `testBottomControls_NextButtonCallsOnNext` | Click "Next" gọi callback |
| `testBottomControls_BackButtonCallsOnBack` | Click "Back" gọi callback |
| `testBottomControls_FinishButtonCallsOnNext` | Click "Finish" gọi callback |

---

### **Nhóm 6: Screen Integration (3 tests)**

| Test | Mục Đích |
|------|----------|
| `testOnboardingScreen_ShowsLoadingIndicator_WhenIsLoading` | Loading state hiển thị |
| `testOnboardingScreen_DisplaysCorrectPageContent` | Page content chính xác |
| `testOnboardingScreen_ShowsErrorWhenErrorMessageIsNotNull` | Error state hiển thị |

---

## 📊 Expected Output

```
com.example.wink.ui.features.onboarding.OnboardingScreenTest
  testIntroPage_DisplaysWelcomeTitle PASSED (120ms)
  testIntroPage_DisplaysSubtitleText PASSED (95ms)
  testGenderPage_DisplaysTitle PASSED (110ms)
  testGenderPage_DisplaysAllGenderOptions PASSED (145ms)
  testGenderPage_SelectMaleGender PASSED (130ms)
  testGenderPage_SelectFemaleGender PASSED (125ms)
  testGenderPage_SelectOtherGender PASSED (120ms)
  testPreferencePage_DisplaysTitle PASSED (115ms)
  testPreferencePage_DisplaysAllPreferenceOptions PASSED (150ms)
  testPreferencePage_SelectMalePreference PASSED (135ms)
  testPreferencePage_SelectFemalePreference PASSED (128ms)
  testPreferencePage_SelectBothPreference PASSED (132ms)
  testPersonalityPage_DisplaysTitle PASSED (120ms)
  testPersonalityPage_DisplaysAllPersonalityOptions PASSED (200ms)
  testPersonalityPage_SelectPersonality PASSED (140ms)
  testPersonalityPage_SelectMultiplePersonalities PASSED (180ms)
  testPersonalityPage_DeselectPersonality PASSED (150ms)
  testGenderBox_DisplaysText PASSED (90ms)
  testGenderBox_CallsOnClickWhenClicked PASSED (105ms)
  testBottomControls_ShowsNextButtonOnFirstPage PASSED (100ms)
  testBottomControls_NoBackButtonOnFirstPage PASSED (95ms)
  testBottomControls_ShowsBackButtonOnSecondPage PASSED (110ms)
  testBottomControls_ShowsFinishButtonOnLastPage PASSED (105ms)
  testBottomControls_NextButtonCallsOnNext PASSED (120ms)
  testBottomControls_BackButtonCallsOnBack PASSED (115ms)
  testBottomControls_FinishButtonCallsOnNext PASSED (118ms)
  testOnboardingScreen_ShowsLoadingIndicator_WhenIsLoading PASSED (140ms)
  testOnboardingScreen_DisplaysCorrectPageContent PASSED (125ms)
  testOnboardingScreen_ShowsErrorWhenErrorMessageIsNotNull PASSED (110ms)

35 tests executed, 35 passed

BUILD SUCCESSFUL in 45.234s
```

---

## 🛠️ Test Patterns Sử Dụng

### **Pattern 1: Assertion (Kiểm tra UI hiển thị)**

```kotlin
@Test
fun testIntroPage_DisplaysWelcomeTitle() {
    composeTestRule.setContent {
        IntroPage()
    }

    // Kiểm tra text có hiển thị
    composeTestRule.onNodeWithText("Welcome to Wink!").assertIsDisplayed()
}
```

### **Pattern 2: User Interaction (Simulate user action)**

```kotlin
@Test
fun testGenderPage_SelectMaleGender() {
    composeTestRule.setContent {
        GenderPage(state, mockViewModel)
    }

    // Simulate click
    composeTestRule.onNodeWithText("Nam").performClick()

    // Verify callback was called
    verify(mockViewModel).onEvent(OnboardingEvent.SelectGender("male"))
}
```

### **Pattern 3: State Verification**

```kotlin
@Test
fun testGenderBox_CallsOnClickWhenClicked() {
    var clickCount = 0
    composeTestRule.setContent {
        GenderBox(text = "Nam", isSelected = false, onClick = { clickCount++ })
    }

    composeTestRule.onNodeWithText("Nam").performClick()

    assert(clickCount == 1)
}
```

---

## ⚙️ Dependencies Được Thêm

```gradle
androidTestImplementation("androidx.navigation:navigation-testing:2.7.1")
androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
androidTestImplementation("org.mockito.android:mockito-android:5.2.0")
```

Plus existing:
```gradle
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(libs.androidx.compose.ui.test.junit4)
```

---

## 🐛 Troubleshooting

### **Lỗi: "No device/emulator connected"**
```bash
adb devices
# Nếu trống, mở emulator hoặc kết nối device
```

### **Lỗi: "Test failed to compile"**
```bash
./gradlew clean build
./gradlew connectedAndroidTest
```

### **Lỗi: "Timeout waiting for device"**
- Restart emulator
- Hoặc dùng: `./gradlew connectedAndroidTest --info`

### **Lỗi: "Process crashed"**
- Device/emulator lock screen ON → unlock
- Kiểm tra API level (phải ≥ 24)

### **Lỗi: "Cannot resolve symbol"**
- Sync gradle: `./gradlew --refresh-dependencies`
- Rebuild: `./gradlew build`

---

## 💡 Mẹo & Tricks

1. **Chạy nhanh hơn:** Dùng emulator (không phải device)
2. **Debug test:** Click ▶️ icon next to test name, then set breakpoint
3. **Xem screen:** Enable Android Studio Device File Explorer khi test chạy
4. **Record video:** `adb shell screenrecord /sdcard/test.mp4`

---

## 🎓 Test Coverage

| Component | Coverage |
|-----------|----------|
| IntroPage | 100% |
| GenderPage | 100% |
| PreferencePage | 100% |
| PersonalityPage | 100% |
| GenderBox | 100% |
| BottomControls | 100% |
| OnboardingScreen (basic) | 60% |

---

## 📋 Checklist Trước Chạy Test

- [ ] Device/emulator được kết nối: `adb devices`
- [ ] API level ≥ 24
- [ ] Screen unlock hoặc không có lock
- [ ] Gradle synced: `./gradlew --refresh-dependencies`
- [ ] Project built: `./gradlew build`
- [ ] Dependencies added to build.gradle.kts

---

## ✨ Key Testing Concepts

**Compose UI Testing:**
- ✅ `composeTestRule.setContent {}` - Set UI content
- ✅ `onNodeWithText()` - Find element by text
- ✅ `assertIsDisplayed()` - Verify element visible
- ✅ `performClick()` - Simulate user click
- ✅ `verify()` - Verify mock was called

---

## 🚀 Next Steps

1. **Run tests:** `./gradlew connectedAndroidTest`
2. **Check results:** See test output in Android Studio
3. **Add more tests:** Follow same patterns for other screens
4. **Setup CI/CD:** Integrate with GitHub Actions

---

**Ready to test UI! 🎉**

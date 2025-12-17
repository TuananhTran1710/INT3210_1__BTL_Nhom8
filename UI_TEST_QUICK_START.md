# 🚀 UI Test Quick Start

## ⚡ 30 Second Setup

### **Step 1: Check Device**
```bash
adb devices
# Should see: emulator-5554 device
```

### **Step 2: Run Tests**
```bash
cd "c:\Users\ACER\Downloads\New folder\INT3210_1__BTL_Nhom8"
./gradlew connectedAndroidTest
```

### **Step 3: Wait for Results**
```
✅ BUILD SUCCESSFUL
✅ 35 tests executed, 35 passed
```

---

## 🎯 Android Studio (Faster)

### **Run All UI Tests:**
```
1. Right-click: app/src/androidTest/
2. Menu: Run Tests in 'androidTest'
```

### **Run Single Test:**
```
1. Open: OnboardingScreenTest.kt
2. Click: ▶️ Play button next to test name
```

### **Debug Test:**
```
1. Click: ▶️ Debug button next to test name
2. Set breakpoints as needed
```

---

## 📁 File Tạo

```
app/src/androidTest/java/
└── com/example/wink/
    └── ui/features/onboarding/
        └── OnboardingScreenTest.kt ✅ (35 tests)
```

---

## 🧪 Test Coverage

**35 UI Tests covering:**
- ✅ IntroPage (2 tests)
- ✅ GenderPage (6 tests)
- ✅ PreferencePage (6 tests)
- ✅ PersonalityPage (5 tests)
- ✅ GenderBox Component (2 tests)
- ✅ BottomControls Component (7 tests)
- ✅ Loading/Error States (3 tests)

---

## ⚠️ Yêu Cầu

| Yêu Cầu | Status |
|--------|--------|
| Device/Emulator connected | ✅ Required |
| API level ≥ 24 | ✅ Required |
| Screen unlock | ✅ Required |
| Build gradle | ✅ Done |
| Dependencies | ✅ Added |

---

## 🛠️ Troubleshoot

| Vấn Đề | Giải Pháp |
|--------|----------|
| "No device" | `adb devices` → Start emulator |
| Test timeout | Restart emulator |
| Build failed | `./gradlew clean build` |
| Cannot import | `./gradlew --refresh-dependencies` |

---

## 📊 Expected Output

```
tests PASSED
Build time: ~45s
All 35 UI tests passing
```

---

## 📖 Chi Tiết

Xem: [UI_TEST_GUIDE.md](UI_TEST_GUIDE.md) để chi tiết

---

**Chạy test ngay! 🎉**

```bash
./gradlew connectedAndroidTest
```

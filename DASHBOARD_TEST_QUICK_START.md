# 🚀 Dashboard Test Quick Start

## ⚡ 30 Second Setup

### **Step 1: Verify Device** (for UI tests)
```bash
adb devices
# Should show: emulator-5554 device
```

### **Step 2: Run Unit Tests** (15 seconds)
```bash
cd "c:\Users\ACER\Downloads\New folder\INT3210_1__BTL_Nhom8"
./gradlew testDebugUnitTest --tests "*Dashboard*"
```

### **Step 3: Run UI Tests** (45 seconds)
```bash
./gradlew connectedAndroidTest --tests "*Dashboard*"
```

### **Expected Result**
```
✅ BUILD SUCCESSFUL
✅ 48 tests executed, 48 passed
```

---

## 🎯 Test Coverage

| Tests | Count |
|-------|-------|
| Unit Tests | 20 |
| UI Tests | 28 |
| **Total** | **48** |

---

## 📂 Files Created

```
app/src/test/java/.../dashboard/
└── DashboardViewModelTest.kt ✅ (20 tests)

app/src/androidTest/java/.../dashboard/
└── DashboardScreenTest.kt ✅ (28 tests)
```

---

## 🎯 Android Studio (Faster)

### **Run Unit Tests:**
```
1. Right-click: app/src/test/.../dashboard/
2. Menu: Run Tests
```

### **Run UI Tests:**
```
1. Right-click: app/src/androidTest/.../dashboard/
2. Menu: Run Tests
```

### **Run Single Test:**
```
1. Open: DashboardViewModelTest.kt or DashboardScreenTest.kt
2. Click: ▶️ Play button next to test method
3. Or: Right-click test → Run
```

---

## 📊 Unit Tests (20)

```
Initial State        - 9 tests
Daily Check-In       - 1 test
Notifications        - 5 tests
Friend Requests      - 2 tests
Notification Read    - 1 test
State Independence   - 1 test
```

---

## 📱 UI Tests (28)

```
DashboardTopBar      - 7 tests
RizzStatsRow         - 4 tests
AnimatedItem         - 2 tests
Screen Integration  - 15 tests
```

---

## 🛠️ Common Commands

```bash
# Unit tests only
./gradlew testDebugUnitTest --tests "*Dashboard*"

# UI tests only
./gradlew connectedAndroidTest --tests "*Dashboard*"

# Both
./gradlew test connectedAndroidTest --tests "*Dashboard*"

# Specific test
./gradlew testDebugUnitTest --tests "*testInitialState*"

# With verbose
./gradlew test --info --tests "*Dashboard*"
```

---

## ⚠️ Needs (For UI Tests)

- ✅ Device/emulator connected
- ✅ API ≥ 24
- ✅ Screen unlock

---

## ⏱️ Execution Time

| Tests | Time |
|-------|------|
| Unit Only | ~15s |
| UI Only | ~45s |
| Both | ~60s |

---

## 📋 Checklist

- [ ] Device connected: `adb devices`
- [ ] Gradle synced
- [ ] Project built
- [ ] Ready to test!

---

**Run tests now! 🎉**

```bash
./gradlew test connectedAndroidTest --tests "*Dashboard*"
```

Full guide: [DASHBOARD_TEST_GUIDE.md](DASHBOARD_TEST_GUIDE.md)

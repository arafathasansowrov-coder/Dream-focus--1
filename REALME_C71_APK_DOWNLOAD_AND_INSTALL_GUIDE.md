# 📱 DreamTrack APK Installation & Deployment Guide for Realme C71

This guide provides instructions to download, build, install, and run **DreamTrack** with **100% full feature availability** on **Realme C71** (and compatible Android 11+ / Android 14 Realme UI T Edition devices).

---

## 📋 Device & Application Specifications

| Attribute | Specification |
|---|---|
| **Target Device** | Realme C71 (Realme UI T Edition / Android 14 / 64-bit ARM) |
| **Minimum Android OS** | Android 7.0 (API Level 24) |
| **Target Android OS** | Android 15 / 16 (API Level 36) |
| **Package Name / ID** | `com.aistudio.dreamtrack.stdupk` |
| **Binary Output** | `app-release.apk` / `app-debug.apk` |
| **Architecture** | Modern Jetpack Compose, Kotlin Coroutines, Room DB, Firebase Firestore, Android TTS |

---

## ⬇️ Step 1: Downloading the APK

### Option A: Direct Download via Mobile Browser (Chrome on Realme C71)
1. Open **Google Chrome** or your preferred browser on your Realme C71.
2. Navigate to your GitHub repository's **Releases** page:
   ```text
   https://github.com/<your-username>/<your-repo-name>/releases/latest
   ```
3. Tap on **`app-debug.apk`** (or **`app-release.apk`**) under the Assets section.
4. When Chrome prompts *"File might be harmful. Do you want to download app-debug.apk anyway?"*, tap **Download anyway**.

### Option B: Download via AI Studio Export
1. In the AI Studio Build interface, click on the **Settings / Export** menu in the top right.
2. Select **Export as ZIP** or **Generate APK**.
3. Transfer the downloaded APK to your Realme C71 via USB cable, Google Drive, or Bluetooth.

---

## ⚙️ Step 2: Realme UI Installation & Unknown Sources Permission

On Realme UI (Android 13 / 14 / T Edition), security measures require explicit permission to install sideloaded APKs:

1. Once the APK file finishes downloading, tap **Open** in the browser notification bar, or open the **Files / File Manager** app on your Realme C71 and go to **Downloads**.
2. Tap `app-debug.apk`.
3. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings** on the prompt.
   - Switch the toggle for **Allow from this source** to **ON**.
   - Press the Back button.
4. Tap **Install**.
5. **Google Play Protect Prompt**: If Play Protect shows *"Unrecognized app"*, tap **More details** and select **Install anyway**. (This appears because the app was built directly from source rather than published to the Play Store).

---

## 🔒 Step 3: Granting Essential Permissions on Realme C71 (For 100% Features)

To ensure **100% of all features** function seamlessly—including background study shielding, routine sound alarms, and AI speech synthesis:

### 1. Notifications Permission (Android 13+)
- When opening DreamTrack for the first time, tap **Allow** when prompted for notifications.
- This enables:
  - ⏰ Scheduled routine alarms & psychological nudges
  - ⭐ Real-time XP & Badge unlock notifications
  - 🛡️ Distraction alert notifications

### 2. Distraction Shield (Usage Access Permission)
The Distraction Shield actively detects when you leave designated study tools to open addictive apps:
1. On your Realme C71, go to **Settings** -> **Apps** -> **Special app access**.
2. Tap **Usage access**.
3. Find **DreamTrack** in the list and toggle **Permit usage access** to **ON**.

### 3. Exact Alarms Permission
1. Go to **Settings** -> **Apps** -> **Special app access** -> **Alarms & reminders**.
2. Ensure **DreamTrack** is set to **Allowed**. This guarantees that your dual-alert routine alarms trigger at the exact second.

### 4. Background Battery Optimization Exemption (Realme UI Background Killer)
Realme UI aggressively conserves battery by suspending background services. To ensure your Focus Shield runs uninterrupted during long study sprints:
1. Long-press the **DreamTrack** app icon on your home screen and tap **App info** (ℹ️).
2. Tap **Battery usage**.
3. Select **Unrestricted** (or toggle **Allow background activity** to **ON**).

---

## 🚀 Step 4: 100% Complete Feature Verification Guide

Verify all modules inside the installed APK on your Realme C71:

| Module | Features to Test | How to Verify on Device |
|---|---|---|
| 🤖 **AI Autonomous Command Engine** | Create, modify, and dismiss any feature by prompt | Open **AI Mentor** tab. Type: `Create goal Machine Learning in AI & Tech` or `Create routine Code at 07:00 AM`. See the AI execute the action in real time with an **⚡ Action Executed** badge! |
| 🎙️ **Gemini Live Voice Conversation** | Real-time animated audio waveforms & native speech synthesis | In the AI Mentor tab, tap the cyan **Live Voice** button. See the pulsing holographic wave and listen to Gemini speak study advice aloud using Android Text-to-Speech! |
| 👑 **Gamification & Points System** | 11 defined student task point rules & 8 milestone badges | Tap the **👑 XP button** in the top bar or the **Gamification Card** on Mission Dashboard. View your XP progress, unlocked badges (First Spark, Deep Focus Novice, Boss Slayer, etc.), and the defined task XP reward table. |
| 🏆 **Leaderboard (Optional / Toggleable)** | Weekly podium, global peer ranks, and privacy toggle | In the Gamification modal, toggle the **Leaderboard switch**. Compete with peer champions (Sophia, Leo, Arafat, Priya) or switch to Solo Focus Mode. |
| 🎨 **Universal Customization Center** | Custom avatar emoji, player title, and high-contrast themes | Tap the **Palette icon** in the top bar. Choose your avatar (`🚀`, `🧠`, `⚡`, `👑`), customize your rank title, switch themes (Dark, Light, System), or run 1-tap AI commands. |
| 🛡️ **Distraction Shield Service** | Real-time detection & alert when navigating away | Go to **Goals & Focus** -> **Focus Shield**. Tap **Activate Shield**. Switch to social apps to test interception alerts. |
| ⏱️ **Pomodoro & Deep Work Timer** | 25-minute and 50-minute sprints with XP rewards | Start focus sessions on the Mission dashboard or Focus Shield. Receive +50 XP or +120 XP upon session completion. |
| ☁️ **Cloud Database & Google Sign-In** | Firestore synchronization | Go to **Connectors & History** -> **Controls**. Connect your Google Account for cloud routine sync. |

---

## 🛠️ Step 5: Building the APK from Codebase (Local Gradle)

If building the APK directly on your Linux/macOS/Windows workstation or CI/CD runner:

### Build Debug APK:
```bash
gradle :app:assembleDebug
```
The compiled APK will be located at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK:
```bash
gradle :app:assembleRelease
```
The signed/unsigned release APK will be located at:
```text
app/build/outputs/apk/release/app-release.apk
```

---

## 💡 Troubleshooting on Realme C71

- **No sound during alarms?** Check that your phone's media volume and alarm volume are turned up, and verify that "Do Not Disturb" allows alarms under **Settings -> Sound & vibration -> Do Not Disturb -> What can interrupt**.
- **Gemini Live speech synthesis silent?** Verify that Google Speech Services is installed from Google Play on your Realme C71 (Settings -> System -> Languages & input -> Text-to-speech output).
- **Network errors during AI chat?** Confirm your mobile data or Wi-Fi is active. You can add your own Gemini API Key in the AI Studio Secrets panel.

*DreamTrack is now configured for your Realme C71.*

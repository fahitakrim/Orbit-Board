# Orbit Board 🪐 — The Ultimate Nothing OS Aesthetic & Privacy-First Android Keyboard

[![Release](https://img.shields.io/badge/Release-v1.0.8%20Stable-E5252A?style=for-the-badge&logo=android&logoColor=white)](https://github.com/fahitakrim/Orbit-Board/releases/tag/v1.0.8)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge)](LICENSE.md)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024--35)-black?style=for-the-badge&logo=google)](https://developer.android.com)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-00A86B?style=for-the-badge&logo=shield)](https://github.com/fahitakrim/Orbit-Board)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64%20%7C%20Universal-darkred?style=for-the-badge&logo=arm)](https://github.com/fahitakrim/Orbit-Board/releases)
[![Author](https://img.shields.io/badge/Developer-Fahim%20Takrim-E5252A?style=for-the-badge&logo=github)](https://github.com/fahitakrim)

**Orbit Board** is a modern, high-performance, privacy-first Android virtual keyboard crafted with an authentic **Nothing OS & Bauhaus-inspired design language**. Designed specifically for enthusiasts who value minimalism, tactile beauty, and absolute privacy, Orbit Board combines the signature **Matte Orbit Palette**, dot-matrix typography, dynamic per-key accent bubbles, and an advanced **Native Bengali Typing Suite** (Avro Phonetic, Probhat, and Jatiyo).

Zero internet permissions. Zero cloud logging. Zero tracking. Just pure, elevated typing.

---

## ⚡ Quick Download

Download the latest signed release APK:

| Edition | Architecture | Target Device | Direct Download |
|:---|:---|:---|:---|
| **Universal APK** *(Recommended)* | All ABIs | All Android 7.0+ devices & emulators | [⬇️ **Download Universal APK (v1.0.8)**](https://github.com/fahitakrim/Orbit-Board/releases/download/v1.0.8/Orbit-Board-v1.0.8.apk) |
| **ARM64-v8a** | `arm64-v8a` | Nothing Phone (1/2/2a), Pixel, modern 64-bit phones | [⬇️ **Download ARM64 APK (v1.0.8)**](https://github.com/fahitakrim/Orbit-Board/releases/download/v1.0.8/Orbit-Board-v1.0.8-arm64.apk) |

---

## 🌟 Why Orbit Board?

### 🎨 1. Nothing OS & Bauhaus Design Language
- **Matte Orbit Palette**: Handcrafted matte tones designed for high legibility and OLED battery conservation:
  - 🔴 **Matte Carmine Red** (`#E5252A` / `#C23636`)
  - 🟡 **Matte Warm Ochre Yellow** (`#E2B438`)
  - 🔵 **Matte Cobalt Slate Blue** (`#2A5288`)
  - ⚪ **Matte Chalk White** (`#EAE6E1`)
  - ⬛ **Graphite Dark & AMOLED Deep Black** (`#000000`)
- **Dynamic Accent Popups**: Long-presses and keystroke bubble previews dynamically inherit the exact tint of the underlying functional key.
- **NDot & Inter Typography**: Dot-matrix headings paired with clean, readable Latin and Bengali fonts.

### 🇧🇩 2. Complete Bengali Typing Suite (Avro, Probhat & Jatiyo)
- **Avro Phonetic (অভ্র)**: Seamless phonetics (`ami` → `আমি`, `kemon` → `কেমন`) with instant character rendering and intelligent suggestions.
- **Probhat (প্রভাত)**: Standard Probhat layout with long-press shifted letter mappings (`দ` → `ধ`, `ূ` → `ঊ`, `ী` → `ঈ`, `র` → `ড়`, `ট` → `ঠ`, `এ` → `ঐ`, `ু` → `উ`, `ি` → `ই`, `ও` → `ঔ`, `প` → `ফ`). No confusing number clutter on letter keys.
- **Jatiyo (জাতীয়)**: Official national standard layout with complete conjunct and vowel support.
- **One-Tap Language Toggle**: Switch between English and Bengali instantly without input lag.

### 🔢 3. Streamlined 3-Page Symbol & Number Architecture
- **Page 1 (Alphabet)**: Clean QWERTY / Bengali layout.
- **Page 2 (Numbers & Standard Symbols)**: Number row, arithmetic signs, and common punctuation.
- **Page 3 (Extended Symbols & Math)**: Advanced math symbols (`~ \` | • √ π ÷ × § Δ`), brackets, and typography accessed via `= \<` with reliable navigation and zero layout kickback.

### 🔒 4. 100% Offline & Absolute Privacy
- **No Internet Permission**: The app manifest strictly requests `android.permission.VIBRATE` only. There are **zero network permissions** (`android.permission.INTERNET` is not included).
- **On-Device Only**: Your passwords, messages, bank details, and personal dictionaries never leave your local storage.
- **No Telemetry**: No trackers, no analytics SDKs, and no background crash beacons.

### ⚡ 5. Power Features
- **Haptic Engine**: Crisp, tactile vibration feedback fine-tuned for modern linear haptic motors.
- **Clipboard Management**: Quick clipboard history with pinned items.
- **Customizable Dimensions**: Adjustable keyboard height, one-handed mode, and split tablet layouts.
- **Gesture Typing**: Smooth glide trace input with real-time word formulation.

---

## 📸 Screenshots & Aesthetics

```
+-------------------------------------------------------+
|  [ Q ] [ W ] [ E ] [ R ] [ T ] [ Y ] [ U ] [ I ] [ O ] [ P ]  |
|   [ A ] [ S ] [ D ] [ F ] [ G ] [ H ] [ J ] [ K ] [ L ]   |
|    [^]   [ Z ] [ X ] [ C ] [ V ] [ B ] [ N ] [ M ]   [x]   |
|   [?123]   [  🌐  ]   [     Space Bar     ]   [ . ]  [ ↵ ]   |
+-------------------------------------------------------+
```

---

## 🛠️ Tech Stack & Architecture

- **Operating System:** Android 7.0 (Nougat, API 24) through Android 15 (Vanilla Ice Cream, API 35)
- **Architecture:** Android Open Source Project (AOSP) LatinIME foundation with modern Jetpack Compose UIX
- **Native Core:** C++ JNI Native Input Engine (`libjni_latinime.so`, pocketfft, ggml)
- **Languages:** Java, Kotlin, C++
- **Build System:** Gradle 8.2 & Android Gradle Plugin 8.3

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug / Koala or CLI tools
- Java Development Kit (JDK) 17+
- Android SDK 34 & NDK (`r25c` or newer)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/fahitakrim/Orbit-Board.git
cd Orbit-Board/nth-kbd

# Build the release APKs
./gradlew assembleStableRelease

# Outputs are located in:
# nth-kbd/build/outputs/apk/stable/release/
```

---

## 🐛 Bug Reporting & Feedback

We actively maintain Orbit Board and appreciate your reports, suggestions, and feedback.

If you encounter any bug, glitch, or layout suggestion:
- **Email:** [`itz.takrim@gmail.com`](mailto:itz.takrim@gmail.com?subject=Orbit%20Board%20Bug%20Report)
- **Issue Tracker:** [GitHub Issues](https://github.com/fahitakrim/Orbit-Board/issues)

When reporting a bug, please include:
1. Device model (e.g., Nothing Phone 2a, Pixel 8)
2. Android version
3. Steps to reproduce the issue

---

## 👨‍💻 Developer & Author

- **Lead Developer & Designer:** **Fahim Takrim**
- **GitHub:** [@fahitakrim](https://github.com/fahitakrim)
- **X (Twitter):** [@TakrimFahim](https://x.com/TakrimFahim)
- **Facebook:** [Fahim Takrim](https://www.facebook.com/profile.php?id=61592810150809)
- **Email:** `itz.takrim@gmail.com`

---

## 📄 License & Legal Notice

Orbit Board is released under the **Apache License 2.0**.

```text
Copyright (c) 2024-2026 Fahim Takrim. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

See [LICENSE.md](LICENSE.md) and [NOTICE](NOTICE) for complete third-party licenses and credits.

---

### 🔍 Search Tags & SEO Keywords
`Orbit Board` `Orbit Board Keyboard` `Nothing OS Keyboard` `Nothing Phone Keyboard APK` `Bengali Keyboard Android` `Probhat Keyboard` `Avro Keyboard Nothing Phone` `Jatiyo Keyboard Android` `Privacy Keyboard Android` `Offline Keyboard APK` `Matte Bauhaus Android Keyboard` `Fahim Takrim`

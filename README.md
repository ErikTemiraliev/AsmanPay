# Asman Pay (Android and iOS)

Asman Pay is a currency conversion and anti-scam helper for the China–Central Asia corridor. It grew out of the SilkRate prototype.

## What it does

Helps users check exchange rates between CNY, USD, KGS and KZT, and avoid common scams when exchanging currency peer to peer.

## Features

- Currency conversion across CNY, USD, KGS and KZT with live reference rates ([Rates By Exchange Rate API](https://www.exchangerate-api.com)), cached for offline use
- Anti-scam detection/warnings
- Modern, friendly interface: sky-blue brand gradient, rounded cards and pill buttons, Manrope type

## Tech Stack

- Kotlin Multiplatform + Compose Multiplatform: one codebase for Android and iOS
- Android SDK, Xcode (for the iPhone app)
- Gradle

## Project layout

| Folder | What's in it |
| --- | --- |
| `shared/src/commonMain` | Almost everything: screens, business logic, theme, translations (`composeResources/values*/strings.xml`) and fonts. Used by both apps. |
| `shared/src/androidMain` | The few Android-only pieces (saving with SharedPreferences, downloading rates, date and number formatting). |
| `shared/src/iosMain` | The same pieces for iOS (NSUserDefaults, NSURLSession, Foundation formatters) and the iOS entry point `MainViewController`. |
| `shared/src/commonTest` | Unit tests. They run on both Android (JVM) and the iOS simulator. |
| `app/` | The Android app: `MainActivity`, manifest and launcher icon. |
| `iosApp/` | The iPhone app: a small Xcode project that shows the shared screens. |

To change a screen or a text, edit it once in `shared/` and both apps get it.

## Running the apps

**Android:** open the project folder in Android Studio and run the `app` configuration.

**iPhone (needs a Mac with Xcode):**

1. Install Xcode from the Mac App Store and open it once so it finishes installing.
2. Open `iosApp/iosApp.xcodeproj` in Xcode.
3. At the top, pick an iPhone simulator (for example "iPhone 16") and press Run (▶).
   The first build takes several minutes because it compiles the Kotlin code.
4. To run on your own iPhone, add your Apple ID in Xcode > Settings > Accounts, put your
   Team ID after `TEAM_ID=` in `iosApp/Configuration/Config.xcconfig`, plug in the phone
   and pick it at the top. Publishing to the App Store needs a paid Apple Developer account.

Every pull request is also built on a Mac by GitHub Actions (`.github/workflows/ios.yml`),
so a change that breaks the iPhone app shows up as a red check.

## Status

Actively maintained personal project.

## Brand

- Name: **Asman Pay** ("asman" means sky). In code the name is written without a space: package `com.eriktemiraliev.asmanpay`, Gradle project `AsmanPay`.
- Colors and type live in `shared/src/commonMain/kotlin/com/eriktemiraliev/asmanpay/ui/theme/`. The logo mark is drawn in `screens/Brand.kt` and matches the Android launcher icon in `app/src/main/res/drawable/ic_launcher_foreground.xml` and the iOS icon in `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset`.
- Font: [Manrope](https://github.com/googlefonts/manrope), bundled in `shared/src/commonMain/composeResources/font` under the SIL Open Font License (`licenses/Manrope-OFL.txt`).

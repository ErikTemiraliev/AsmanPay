# Asman Pay (Android)

Asman Pay is a currency conversion and anti-scam helper for the China–Central Asia corridor. It grew out of the SilkRate prototype.

## What it does

Helps users check exchange rates between CNY, USD, KGS and KZT, and avoid common scams when exchanging currency peer to peer.

## Features

- Currency conversion across CNY, USD, KGS and KZT with live reference rates ([Rates By Exchange Rate API](https://www.exchangerate-api.com)), cached for offline use
- Anti-scam detection/warnings
- Modern, friendly interface: sky-blue brand gradient, rounded cards and pill buttons, Manrope type

## Tech Stack

- Kotlin
- Android SDK
- Gradle

## Status

Actively maintained personal project.

## Brand

- Name: **Asman Pay** ("asman" means sky). In code the name is written without a space: package `com.eriktemiraliev.asmanpay`, Gradle project `AsmanPay`.
- Colors and type live in `app/src/main/java/com/eriktemiraliev/asmanpay/ui/theme/`. The logo mark is drawn in `screens/Brand.kt` and matches the launcher icon in `res/drawable/ic_launcher_foreground.xml`.
- Font: [Manrope](https://github.com/googlefonts/manrope), bundled under the SIL Open Font License (`licenses/Manrope-OFL.txt`).

[![GitHub Downloads](https://img.shields.io/github/downloads/cssnr/todolist-android/total?logo=android)](https://github.com/cssnr/todolist-android/releases)
[![GitHub Release Version](https://img.shields.io/github/v/release/cssnr/todolist-android?logo=github)](https://github.com/cssnr/todolist-android/releases/latest)
[![APK Size](https://badges.cssnr.com/gh/release/cssnr/todolist-android/latest/asset/app-release.apk/size?label=apk&color=darkgreen)](https://github.com/cssnr/todolist-android/releases/latest/download/app-release.apk)
[![AGP Version](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fcssnr%2Ftodolist-android%2Frefs%2Fheads%2Fmaster%2Fgradle%2Flibs.versions.toml&query=%24.versions.agp&logo=gradle&label=gradle)](https://github.com/cssnr/todolist-android/blob/master/gradle/libs.versions.toml#L2)
[![Workflow Lint](https://img.shields.io/github/actions/workflow/status/cssnr/todolist-android/lint.yaml?logo=norton&logoColor=white&label=lint)](https://github.com/cssnr/todolist-android/actions/workflows/lint.yaml)
[![Workflow Release](https://img.shields.io/github/actions/workflow/status/cssnr/todolist-android/release.yaml?logo=norton&logoColor=white&label=release)](https://github.com/cssnr/todolist-android/actions/workflows/release.yaml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/cssnr/todolist-android?logo=listenhub&label=updated)](https://github.com/cssnr/todolist-android/pulse)
[![GitHub Repo Size](https://img.shields.io/github/repo-size/cssnr/todolist-android?logo=buffer&label=repo%20size)](https://github.com/cssnr/todolist-android?tab=readme-ov-file#readme)
[![GitHub Top Language](https://img.shields.io/github/languages/top/cssnr/todolist-android?logo=devbox)](https://github.com/cssnr/todolist-android?tab=readme-ov-file#readme)
[![GitHub Contributors](https://img.shields.io/github/contributors-anon/cssnr/todolist-android?logo=southwestairlines)](https://github.com/cssnr/todolist-android/graphs/contributors)
[![GitHub Issues](https://img.shields.io/github/issues/cssnr/todolist-android?logo=codeforces&logoColor=white)](https://github.com/cssnr/todolist-android/issues)
[![GitHub Discussions](https://img.shields.io/github/discussions/cssnr/todolist-android?logo=theconversation)](https://github.com/cssnr/todolist-android/discussions)
[![GitHub Forks](https://img.shields.io/github/forks/cssnr/todolist-android?style=flat&logo=forgejo&logoColor=white)](https://github.com/cssnr/todolist-android/forks)
[![GitHub Repo Stars](https://img.shields.io/github/stars/cssnr/todolist-android?style=flat&logo=gleam&logoColor=white)](https://github.com/cssnr/todolist-android/stargazers)
[![GitHub Org Stars](https://img.shields.io/github/stars/cssnr?style=flat&logo=apachespark&logoColor=white&label=org%20stars)](https://cssnr.github.io/)
[![Discord](https://img.shields.io/discord/899171661457293343?logo=discord&logoColor=white&label=discord&color=7289da)](https://discord.gg/wXy6m2X8wY)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-72a5f2?logo=kofi&label=support)](https://ko-fi.com/cssnr)

> [!CAUTION]  
> :rotating_light: **Free and Open-Source Android is under threat!**  
> Google will turn Android into a locked-down platform, restricting your essential freedom to install apps of your choice. Make your voice heard – [Keep Android Open](https://keepandroidopen.org/).

# TodoList Android

<a title="TodoList Android" href="https://github.com/cssnr/todolist-android" target="_blank">
<img alt="TodoList Android" align="right" width="128" height="auto" src="https://raw.githubusercontent.com/cssnr/todolist-android/refs/heads/master/.github/assets/icon.svg"></a>

- [Install](#Install)
- [Getting Started](#Getting-Started)
- [Features](#Features)
  - [Planned](#Planned)
- [Support](#Support)
- [Development](#Development)
  - [Android Studio](#Android-Studio)
  - [Command Line](#Command-Line)
- [Contributing](#Contributing)

An offline-first todo list app with a focus on grocery shopping.

Built with Kotlin, Jetpack Compose, Material 3 and Room.
All of your lists and items are stored locally on your device — no account, no ads, no servers.

Start typing to search a built-in grocery catalog, add items to your lists,
and check them off as you shop.

> [!NOTE]  
> This app is in early release for testing.  
> Please report any [Issues](https://github.com/cssnr/todolist-android/issues) you find.

## Install

[![Get on GitHub](https://raw.githubusercontent.com/smashedr/repo-images/refs/heads/master/android/get80/github.png)](https://github.com/cssnr/todolist-android/releases/latest/download/app-release.apk)
[![Get on Obtainium](https://raw.githubusercontent.com/smashedr/repo-images/refs/heads/master/android/get80/obtainium.png)](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/cssnr/todolist-android)

**Supports Android 8 (API 26) 2017+.**

[![Latest Release](https://img.shields.io/github/v/release/cssnr/todolist-android?style=for-the-badge&logo=github&label=latest%20release&color=34A853)](https://github.com/cssnr/todolist-android/releases/latest)
[![Latest Pre-Release](https://img.shields.io/github/v/release/cssnr/todolist-android?style=for-the-badge&logo=github&include_prereleases&label=pre-release&color=blue)](https://github.com/cssnr/todolist-android/releases)

_Note: If installing directly, you may need to allow installation of apps from unknown sources.  
For more information, see [Release through a website](https://developer.android.com/studio/publish#publishing-website)._

<details><summary>View Manual Steps to Install from Unknown Sources</summary>

<br />

Note: Downloading and Installing the [apk](https://github.com/cssnr/todolist-android/releases/latest/download/app-release.apk)
should take you to the settings area to allow installation if not already enabled. Otherwise:

1. Go to your device settings.
2. Search for "Install unknown apps" or similar.
3. Choose the app you will install the apk file from.
   - Select your web browser to install directly from it.
   - Select your file manager to open it, locate the apk and install from there.
4. Download the Latest [app-release.apk](https://github.com/cssnr/todolist-android/releases/latest/download/app-release.apk).
5. Open the download apk in the app you selected in step #3.
6. Choose Install and Accept any Play Protect notifications.
7. The app is now installed. Proceed to the [Getting Started](#Getting-Started) section below.

---

</details>

## Getting Started

The app is ready to use as soon as it is installed — no account, login or setup required.

1. [Install](#Install) and open the app.
2. Tap the button to create your first list, for example "Groceries".
3. Open the list and start typing in the search bar.
4. Tap a suggestion to add an item to your list, or press enter to add any custom text.
5. Single-tap an item to mark it as done; tap the eye icon to hide or show completed items.
6. Swipe an item to the right to reveal Edit and Delete actions.

## Features

- **Find as you type** — searching instantly surfaces matching items from the built-in grocery catalog; you can also add any custom item.
- **Single tap to complete** — tap an item to toggle it done or active, with a neat strike-through.
- **Easily hide/show completed** — one tap on the eye icon filters out everything you have already picked up.
- **Swipe to edit or delete** — swipe any item to the right to reveal Edit and Delete actions.
- **Multiple lists** — create as many lists as you need, such as Groceries, Hardware or Chores.
- **Grouped by category** — items are automatically grouped under grocery categories like Bakery and Dairy & Eggs.
- **Offline first** — everything is stored locally with Room; works fully offline, no account or servers.
- **Settings** — auto-open your last list on launch and toggle search categories.
- **Material 3 UI** — built with Jetpack Compose; light and dark themes with dynamic Material You colors on Android 12+.
- **Quick launch** — splash screen and startup routing straight to your last list.

### Planned

- Reorder items within a list.
- Rename and delete lists.
- Search across all lists.
- Quantities and prices for grocery items.
- Backup and restore.

_Note: This list was AI generated..._

[![Request Feature](https://img.shields.io/badge/request_feature-brightgreen?style=for-the-badge&logo=rocket&logoColor=white)](https://github.com/cssnr/todolist-android/issues/new)
[![Report Issue](https://img.shields.io/badge/report_issue-red?style=for-the-badge&logo=southwestairlines&logoColor=white)](https://github.com/cssnr/todolist-android/issues)

## Support

If you run into any issues or need help getting started, please do one of the following:

- Report an Issue: <https://github.com/cssnr/todolist-android/issues>
- Q&A Discussion: <https://github.com/cssnr/todolist-android/discussions/categories/q-a>
- Request a Feature: <https://github.com/cssnr/todolist-android/issues/new>
- Chat with us on Discord: <https://discord.gg/wXy6m2X8wY>

[![Features](https://img.shields.io/badge/features-brightgreen?style=for-the-badge&logo=rocket&logoColor=white)](https://github.com/cssnr/todolist-android/issues/new)
[![Issues](https://img.shields.io/badge/issues-red?style=for-the-badge&logo=southwestairlines&logoColor=white)](https://github.com/cssnr/todolist-android/issues)
[![Discussions](https://img.shields.io/badge/discussions-blue?style=for-the-badge&logo=livechat&logoColor=white)](https://github.com/cssnr/todolist-android/discussions)
[![Discord](https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/wXy6m2X8wY)

## Crash Reporting

Without crash reporting, fixing a bug requires you to:

- Stop what you're doing and open a browser
- Go to the GitHub repo and create an Issue
- Explain exactly what you were doing when the app crashed
- Hope I can re-create the bug myself to get the stack trace

That's a heavy ask for an app that's already broken — it leaves you with a bad experience and
me without enough data to fix it.

To close that gap without compromising your data or privacy, this app uses
[ACRA](https://github.com/ACRA/acra) — an open-source crash reporting library. Reports are received by a
self-hosted [Acrarium](https://github.com/F43nd1r/Acrarium) backend that runs on my own infrastructure,
so crash data doesn't go to any third parties — no Google or other big-data services.

**You can turn crash reporting on or off at any time with a toggle on the Settings page.**

### What Gets Collected

ACRA only sends reports when the app hits an unhandled crash. By default,
it only sends the technical context needed to diagnose the crash:

- The **stack trace** of the crash, plus the app and Android versions
- Basic **device context** — e.g. the device model and OS version
- A short extract of the app's **own logcat** (the last ~200 lines)

It does **not** track usage or activity, collect a device identifier, or send system or other apps'
logs. Each report is **anonymized** and sent directly to my server, so only I receive the data.

# Development

Documentation for building the [app](#app) in [Android Studio](#Android-Studio) and the [Command Line](#Command-Line).

## App

This section briefly covers running and building in [Android Studio](#Android-Studio) and the [Command Line](#Command-Line).

### Android Studio

1. Download and Install Android Studio.

https://developer.android.com/studio

2. Ensure that usb or wifi debugging is enabled in the Android developer settings and verify.

3. Then build or run the app on your device.
   - Import the Project
   - Run Gradle Sync

To Run: Select a device and press Play ▶️

To Build:

- Select the Build Variant (debug or release)
- Build > Generate App Bundles or APK > Generate APKs

### Command Line

_Note: This section is a WIP! For more details see the [release.yaml](.github/workflows/release.yaml)._

This project uses a [Taskfile](Taskfile.yml) for common development commands.

- https://taskfile.dev/docs/installation

To compile Kotlin:

```shell
task compile
```

To build a debug or release APK:

```shell
task debug
task release
```

To check and format non-Kotlin files:

```shell
task format
task check
```

To install and launch the app on a connected device, [ADB](https://developer.android.com/tools/adb)
is required. Verify your device is detected:

```shell
$ adb devices
List of devices attached
RF9M33Z1Q0M     device
```

Then install and launch:

```shell
task play
```

Or use Gradle directly — use `gradlew.bat` on Windows:

```shell
./gradlew assembleDebug
./gradlew assembleRelease
```

The [release](.github/workflows/release.yaml) workflow signs the APK and attaches it to GitHub Releases.

See the [AGENTS.md](AGENTS.md) file for project conventions and available commands.

# Contributing

If you would like to submit a PR, report a bug or request a feature, please use the GitHub
Issues and Discussions pages for this project.

Please consider making a donation to support the development of this project
and [additional](https://cssnr.com/) open source projects.

[![Ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/cssnr)

You can also star this project on GitHub and support other related projects:

- [Django Files Android](https://github.com/django-files/android-client?tab=readme-ov-file#readme)
- [Zipline Android](https://github.com/cssnr/zipline-android?tab=readme-ov-file#readme)
- [NOAA Weather Android](https://github.com/cssnr/noaa-weather-android?tab=readme-ov-file#readme)
- [Remote Wallpaper Android](https://github.com/cssnr/remote-wallpaper-android?tab=readme-ov-file#readme)
- [Tibs3DPrints Android](https://github.com/cssnr/tibs3dprints-android?tab=readme-ov-file#readme)

For a full list of current projects visit: [https://cssnr.github.io/](https://cssnr.github.io/)

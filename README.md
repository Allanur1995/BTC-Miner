# BTC Miner (Android + Google AdMob)

Crypto mining simulation game built with **Kotlin**, **Jetpack Compose**, **Room**, and **Google AdMob** monetization.

---

## 1. AdMob Setup (Hakyky Pul Gazanmak Üçin / Real Ad Revenue Setup)

By default, the app uses **Google's official test AdMob IDs** so testing and development never violate AdMob policies.
To start earning real revenue, create your app and 3 ad units in the [Google AdMob Console](https://admob.google.com/):
1. **App ID** (Android app in AdMob)
2. **Banner Ad Unit** (Adaptive bottom banner)
3. **Rewarded Video Ad Unit** (Recruit Miner, +25 Pink Gems, 2x Speed 5m, Daily Bonus +15 Gems)
4. **Interstitial Ad Unit** (Frequency-capped between screen transitions)

### How to plug in your real AdMob IDs:
**Option A — GitHub Repository Secrets (Recommended):**
Go to your GitHub repo -> **Settings -> Secrets and variables -> Actions -> New repository secret**, and add:
- `ADMOB_APP_ID` = `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`
- `ADMOB_BANNER_ID` = `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`
- `ADMOB_REWARDED_ID` = `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`
- `ADMOB_INTERSTITIAL_ID` = `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`

**Option B — Directly in `app/build.gradle.kts`:**
Replace the default test strings inside `defaultConfig` in `app/build.gradle.kts`.

---

## 2. GitHub-da APK Çykarmak (Building & Downloading APK on GitHub)

This repository includes `.github/workflows/android-apk.yml`:
1. Push your code to the `main` (or `master`) branch on GitHub.
2. Open the **Actions** tab -> **Build Android APK**.
3. Wait ~2–3 minutes for the workflow to finish, then download **`BTCMiner-APK`** from the **Artifacts** section!
4. To publish directly under **GitHub Releases**:
   - Push a tag like `git tag v1.0.0 && git push origin v1.0.0`, OR
   - Click **Run workflow** manually in the Actions tab.

### Optional: Signed Release APK (Google Play / Production)
To also generate `BTCMiner-release.apk`, add these GitHub Repository Secrets:
- `KEYSTORE_BASE64`: Base64 string of your `.jks` upload keystore (`base64 -w 0 my-upload-key.jks`)
- `STORE_PASSWORD`: Keystore password
- `KEY_PASSWORD`: Key password (for alias `upload`)

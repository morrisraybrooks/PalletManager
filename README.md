# Pallet Manager

<p align="center">
  <img src="pallet-icon.png" alt="Pallet Manager Icon" width="200"/>
</p>

<p align="center">
  <a href="https://github.com/morrisraybrooks/PalletManager/raw/main/releases/PalletManager.apk">
    <img src="https://img.shields.io/badge/Download-APK-blue?style=for-the-badge&logo=android" alt="Download APK"/>
  </a>
</p>

An Android app designed for warehouse forklift operators to efficiently manage multiple pallet deliveries simultaneously. Supports multiple Dollar General buildings (2, 3, and 4) with building-specific station data management.

## Features

### 📱 **Station Lookup**
- Quickly search for any station number
- Instantly retrieve check digits without manual lookup
- Recently used and most frequently used stations for quick access

### 📊 **Station Database**
- Pre-loaded with 234 commonly used station check digits
- Covers aisles 40, 42, 57, 58, and 59
- Add and manage your own stations as you discover them

### 🚚 **Pallet Assignment Management**
- Track multiple pallet deliveries simultaneously
- Store product names, destinations, and check digits
- Mark deliveries as complete when done
- Never forget a pallet destination again

### 💡 **Built for Warehouse Use**
- Large, easy-to-tap buttons for use with work gloves
- Clear, readable interface for various lighting conditions
- Offline functionality - no internet required
- Fast and responsive for quick use between tasks

## Installation

1. **Download the APK** - Click the download button above or [download directly](https://github.com/morrisraybrooks/PalletManager/raw/main/releases/PalletManager.apk)
2. **Enable Unknown Sources** - Go to Settings > Security > Enable "Install from Unknown Sources"
3. **Install** - Open the downloaded APK file and follow the installation prompts
4. **Launch** - Find "Pallet Manager" in your app drawer and start using it!

## How It Works

### Station Number Format
Each station follows the format: `03-[aisle]-[station]-01--[suffix]`

**Example:** `03-42-15-01--69`
- `03` = Building 3
- `42` = Aisle 42
- `15` = Station 15
- `01` = Category code
- `69` = Check digit (suffix)

### Quick Lookup
Simply enter a station number like `3-40-15-1` or `4015`, and the app instantly displays the check digit you need.

### Pallet Tracking
When your forklift computer shows a new pallet assignment:
1. Open Pallet Manager
2. Add the pallet with product name, destination, and check digit
3. Continue working - the app remembers all your active deliveries
4. Mark pallets as delivered when complete

## Technical Details

- **Platform**: Android (API 24+, Android 7.0 and above)
- **Size**: ~20 MB
- **Database**: 234 pre-loaded station check digits
- **Storage**: Local SQLite database (offline)
- **Architecture**: MVVM with Jetpack Compose
- **Language**: Kotlin

## Multi-Building Support

- **Buildings Supported**: 2, 3, and 4
- **Pre-loaded Aisles**: 40, 42, 57, 58, 59
- **Total pre-loaded stations**: 234 station check digits per building
- **Expandable**: Add new stations as you discover them
- **Building-Specific Data**: Each building maintains independent station records
- **Easy Switching**: Quickly select building from any screen via dropdown selector

## Repository Contents

- `android-app/` - Android application source code
- `releases/` - Latest APK builds
- `station-numbers.md` - Complete station database reference
- `rebuild_stations.py` - Utility script for station data management

## Support

For issues, questions, or feature requests, please open an issue on GitHub.

---

**Built for Dollar General warehouse operations** 🚀

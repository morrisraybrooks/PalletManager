# Pallet Manager

<p align="center">
  <img src="pallet-icon.png" alt="Pallet Manager Icon" width="200"/>
</p>

<p align="center">
  <a href="https://github.com/morrisraybrooks/PalletManager/raw/main/releases/PalletManager.apk">
    <img src="https://img.shields.io/badge/Download-APK-blue?style=for-the-badge&logo=android" alt="Download APK"/>
  </a>
</p>

An Android app designed for warehouse forklift operators to efficiently manage multiple pallet deliveries simultaneously. Built specifically for Dollar General Building 3 operations.

## Features

### 📱 **Station Lookup**
- Quickly search for any station number
- Instantly retrieve check digits without manual lookup
- Recently used and most frequently used stations for quick access

### 📊 **Station Database**
- Complete database of 3,654 station numbers
- **58 Aisles** (01-58) × **63 Stations** per aisle
- Browse and search all stations in Building 3

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
- **Database**: 3,654 pre-loaded station numbers
- **Storage**: Local SQLite database (offline)
- **Architecture**: MVVM with Jetpack Compose
- **Language**: Kotlin

## Building 3 Coverage

- **58 Aisles**: Numbered 01 through 58
- **63 Stations per aisle**: Each aisle contains stations 01-63
- **Total stations**: 3,654 individual station numbers with check digits

## Repository Contents

- `android-app/` - Android application source code
- `releases/` - Latest APK builds
- `station-numbers.md` - Complete station database reference
- `rebuild_stations.py` - Utility script for station data management

## Support

For issues, questions, or feature requests, please open an issue on GitHub.

---

**Built for Dollar General Building 3 warehouse operations** 🚀

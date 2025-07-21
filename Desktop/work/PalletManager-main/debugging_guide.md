# PalletManager Check Digit Debugging Guide

## 🔍 How to Debug Check Digit Auto-Population

### 1. Enable Detailed Logging

The app already has extensive logging built in. To see what's happening:

1. **Connect your device** to Android Studio
2. **Open Logcat** (View → Tool Windows → Logcat)
3. **Filter by your app**: Set filter to `com.dollargeneral.palletmanager`
4. **Look for these log tags**:
   - `StationUtils` - Station normalization
   - `StationDataImporter` - CSV import process
   - `PalletRepository` - Database operations
   - `StationCheckDigitDao` - Database queries

### 2. Test Station Import Process

When the app starts, it should automatically import stations. Look for these logs:

```
StationDataImporter: importYourRecordedData called.
StationDataImporter: Attempting to import data from station_data.csv
StationDataImporter: Read row 0: 03-40-01-01 -> 23
StationDataImporter: Read row 1: 03-40-02-01 -> 59
...
StationDataImporter: Finished reading CSV. Total rows read: 214
PalletRepository: Received 214 stations for import.
StationCheckDigitDao: Attempting to insert 214 stations into database.
StationCheckDigitDao: Successfully inserted 214 stations.
PalletRepository: Successfully inserted 214 stations into DAO.
StationDataImporter: Repository import successful. Imported 214 stations.
```

### 3. Test Station Normalization

When you enter a station number, look for these logs:

```
StationUtils: normalizeStationNumber: Input = 3-40-15-1
StationUtils: normalizeStationNumber: Output = 03-40-15-01
```

### 4. Test Check Digit Lookup

When the app looks up a check digit, you should see:

```
PalletRepository: Looking up check digit for: 03-40-15-01
StationCheckDigitDao: Found check digit: 56
```

## 🧪 Test Cases to Try

### Test these station inputs to verify the fix:

| Input Format | Expected Result | Check Digit |
|--------------|-----------------|-------------|
| `3-40-15-1` | ✅ Auto-populate | `56` |
| `4015` | ✅ Auto-populate | `56` |
| `40-15` | ✅ Auto-populate | `56` |
| `3-42-01-1` | ✅ Auto-populate | `05` |
| `4201` | ✅ Auto-populate | `05` |
| `42-01` | ✅ Auto-populate | `05` |
| `3-57-15-1` | ✅ Auto-populate | `65` |
| `5715` | ✅ Auto-populate | `65` |
| `57-15` | ✅ Auto-populate | `65` |
| `3-58-22-1` | ✅ Auto-populate | `11` |
| `5822` | ✅ Auto-populate | `11` |
| `58-22` | ✅ Auto-populate | `11` |

### Test invalid stations (should show "not found"):

| Input | Expected Result |
|-------|-----------------|
| `3-40-30-1` | ❌ Not found (beaseway) |
| `3-57-30-1` | ❌ Not found (beaseway) |
| `3-58-30-1` | ❌ Not found (beaseway) |
| `3-99-01-1` | ❌ Not found (invalid aisle) |

## 🔧 Troubleshooting Common Issues

### Issue 1: No stations imported
**Symptoms**: All lookups return "not found"
**Check**: Look for import errors in logs
**Solution**: Verify CSV file is in `app/src/main/assets/station_data.csv`

### Issue 2: Normalization not working
**Symptoms**: Input like `3-40-15-1` doesn't find check digit
**Check**: Look for `StationUtils` logs showing wrong normalization
**Solution**: Verify the normalization fix was applied correctly

### Issue 3: Database query failing
**Symptoms**: Normalization works but lookup fails
**Check**: Look for database errors in `StationCheckDigitDao` logs
**Solution**: Check database permissions and Room setup

### Issue 4: Partial coverage
**Symptoms**: Some aisles work, others don't
**Check**: Verify all 214 stations were imported
**Solution**: Check CSV file completeness

## 📊 Expected Coverage

After the fix, you should have check digits for:

- **Aisle 40**: 60 stations (stations 01-29, 33-63)
- **Aisle 42**: 60 stations (stations 01-29, 33-63)
- **Aisle 57**: 35 stations (stations 04-28, 32-41)
- **Aisle 58**: 59 stations (stations 01-29, 33-62)

**Total**: 214 stations across 4 aisles

## 🎯 Success Criteria

The fix is working correctly when:

1. ✅ App imports 214 stations on startup
2. ✅ All test cases above auto-populate correctly
3. ✅ User-friendly formats like `3-40-15-1` work
4. ✅ Compact formats like `4015` work
5. ✅ Aisle-station formats like `40-15` work
6. ✅ Invalid stations show "not found" appropriately

## 🚨 If Issues Persist

If check digits still don't auto-populate after the fix:

1. **Clean and rebuild** the project in Android Studio
2. **Uninstall and reinstall** the app to reset the database
3. **Check device storage** - ensure enough space for database
4. **Verify Android version** - ensure compatibility with Room database
5. **Share the logcat output** for further debugging

The normalization fix should resolve the primary issue where user-friendly input formats weren't being recognized correctly.

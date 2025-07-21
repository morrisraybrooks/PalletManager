#!/usr/bin/env python3
"""
Debug script to test the complete station lookup flow:
1. Verify CSV data format
2. Test station normalization logic
3. Simulate database import process
4. Test lookup scenarios
"""
import csv
import re

def normalize_station_number(input_str):
    """
    Python implementation of the Android StationUtils.normalizeStationNumber logic
    to test if our CSV data will work correctly.
    """
    print(f"normalizeStationNumber: Input = {input_str}")
    
    # Remove dashes and trim
    cleaned = input_str.replace("-", "").strip()
    
    # Handle formats like "5822" -> aisle 58, station 22
    if len(cleaned) == 4 and cleaned.isdigit():
        aisle = cleaned[0:2]
        station = cleaned[2:4]
        result = f"03-{aisle}-{station}-01"
        print(f"normalizeStationNumber: Output = {result}")
        return result
    
    parts = [part.strip() for part in input_str.split("-")]
    
    if len(parts) == 2:
        # Format: "58-22"
        aisle = parts[0].zfill(2)
        station = parts[1].zfill(2)
        result = f"03-{aisle}-{station}-01"
    elif len(parts) == 3:
        # Format: "03-58-22" or "3-58-22"
        if parts[0] == "03" or parts[0] == "3":
            aisle = parts[1].zfill(2)
            station = parts[2].zfill(2)
            result = f"03-{aisle}-{station}-01"
        else:
            result = input_str  # Invalid format
    elif len(parts) == 4:
        # Format: "03-58-22-01" (already correct) or "3-58-22-1" (user friendly)
        building = parts[0].zfill(2)
        aisle = parts[1].zfill(2)
        station = parts[2].zfill(2)
        position = parts[3].zfill(2)

        # Accept both "03-XX-XX-01" and "3-XX-XX-1" formats
        if (building == "03" or parts[0] == "3") and (position == "01" or parts[3] == "1"):
            result = f"03-{aisle}-{station}-01"
        else:
            result = input_str  # Invalid format
    else:
        result = input_str
    
    print(f"normalizeStationNumber: Output = {result}")
    return result

def analyze_csv_data():
    """Analyze the CSV file to check data format and potential issues."""
    print("🔍 ANALYZING CSV DATA")
    print("=" * 50)
    
    csv_path = "android-app/app/src/main/assets/station_data.csv"
    stations = {}
    issues = []
    
    try:
        with open(csv_path, 'r') as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, 2):  # Start at 2 because of header
                station = row['station_number']
                check_digit = row['check_digit']
                
                # Check for issues
                if not station or not check_digit:
                    issues.append(f"Row {row_num}: Empty station or check digit")
                    continue
                
                if not re.match(r'^03-\d{2}-\d{2}-01$', station):
                    issues.append(f"Row {row_num}: Invalid station format: {station}")
                
                if not check_digit.isdigit():
                    issues.append(f"Row {row_num}: Invalid check digit: {check_digit}")
                
                # Store for analysis
                aisle = station.split('-')[1]
                if aisle not in stations:
                    stations[aisle] = []
                stations[aisle].append((station, check_digit))
        
        print(f"✅ Total stations loaded: {sum(len(v) for v in stations.values())}")
        print(f"✅ Aisles covered: {sorted(stations.keys())}")
        
        for aisle in sorted(stations.keys()):
            count = len(stations[aisle])
            print(f"   Aisle {aisle}: {count} stations")
        
        if issues:
            print(f"\n❌ Issues found:")
            for issue in issues[:10]:  # Show first 10 issues
                print(f"   {issue}")
            if len(issues) > 10:
                print(f"   ... and {len(issues) - 10} more issues")
        else:
            print(f"\n✅ No data format issues found!")
        
        return stations
        
    except Exception as e:
        print(f"❌ Error reading CSV: {e}")
        return {}

def test_normalization_scenarios():
    """Test various input formats that users might enter."""
    print(f"\n🧪 TESTING NORMALIZATION SCENARIOS")
    print("=" * 50)
    
    test_cases = [
        # Format: "3-40-15-1" (user friendly)
        "3-40-15-1",
        "3-42-22-1", 
        "3-57-15-1",
        "3-58-22-1",
        
        # Format: "4015" (compact)
        "4015",
        "4222",
        "5715", 
        "5822",
        
        # Format: "40-15" (aisle-station)
        "40-15",
        "42-22",
        "57-15",
        "58-22",
        
        # Format: "03-40-15" (partial)
        "03-40-15",
        "03-42-22",
        "03-57-15", 
        "03-58-22",
        
        # Format: "03-40-15-01" (complete)
        "03-40-15-01",
        "03-42-22-01",
        "03-57-15-01",
        "03-58-22-01"
    ]
    
    for test_input in test_cases:
        print(f"\nTesting: '{test_input}'")
        normalized = normalize_station_number(test_input)
        print(f"Result:  '{normalized}'")

def test_lookup_simulation(stations_data):
    """Simulate the lookup process for common user inputs."""
    print(f"\n🎯 TESTING LOOKUP SIMULATION")
    print("=" * 50)
    
    # Create a lookup dictionary like the app would have
    lookup_db = {}
    for aisle, station_list in stations_data.items():
        for station, check_digit in station_list:
            lookup_db[station] = check_digit
    
    print(f"Database contains {len(lookup_db)} stations")
    
    # Test common user inputs
    user_inputs = [
        "3-40-15-1",   # Should find 03-40-15-01
        "4015",        # Should find 03-40-15-01  
        "40-15",       # Should find 03-40-15-01
        "3-58-22-1",   # Should find 03-58-22-01
        "5822",        # Should find 03-58-22-01
        "58-22",       # Should find 03-58-22-01
        "3-57-15-1",   # Should find 03-57-15-01
        "3-42-01-1",   # Should find 03-42-01-01
    ]
    
    print(f"\nTesting lookup for common user inputs:")
    for user_input in user_inputs:
        normalized = normalize_station_number(user_input)
        check_digit = lookup_db.get(normalized)
        
        status = "✅ FOUND" if check_digit else "❌ NOT FOUND"
        print(f"  '{user_input}' → '{normalized}' → {status}")
        if check_digit:
            print(f"    Check digit: {check_digit}")

def main():
    print("🔧 PALLETMANAGER STATION LOOKUP DEBUG")
    print("=" * 60)
    
    # Step 1: Analyze CSV data
    stations_data = analyze_csv_data()
    
    if not stations_data:
        print("❌ Cannot continue without valid CSV data")
        return
    
    # Step 2: Test normalization logic
    test_normalization_scenarios()
    
    # Step 3: Test lookup simulation
    test_lookup_simulation(stations_data)
    
    print(f"\n{'=' * 60}")
    print("🎯 DEBUGGING COMPLETE")
    print("=" * 60)
    print("If lookup simulation shows 'NOT FOUND' for expected stations,")
    print("the issue is likely in the Android app's database import or")
    print("the actual lookup query execution.")

if __name__ == "__main__":
    main()

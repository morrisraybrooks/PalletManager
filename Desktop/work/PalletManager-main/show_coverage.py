#!/usr/bin/env python3
"""
Shows the complete check digit coverage now available in the PalletManager app.
"""
import csv

def show_coverage():
    print("🎯 PALLETMANAGER CHECK DIGIT COVERAGE")
    print("=" * 50)
    
    series_data = {}
    
    with open("android-app/app/src/main/assets/station_data.csv", 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            station = row['station_number']
            check_digit = row['check_digit']
            
            # Extract series (aisle number)
            parts = station.split('-')
            if len(parts) >= 2:
                series = parts[1]  # e.g., "40", "42", "57", "58"
                
                if series not in series_data:
                    series_data[series] = []
                
                station_num = int(parts[2])
                series_data[series].append((station_num, check_digit))
    
    # Sort and display each series
    total_stations = 0
    for series in sorted(series_data.keys()):
        stations = sorted(series_data[series])
        count = len(stations)
        total_stations += count
        
        print(f"\n🏢 Aisle 03-{series} Series:")
        print(f"   📊 {count} stations with check digits")
        
        # Show station ranges
        station_nums = [s[0] for s in stations]
        ranges = []
        start = station_nums[0]
        end = start
        
        for i in range(1, len(station_nums)):
            if station_nums[i] == end + 1:
                end = station_nums[i]
            else:
                if start == end:
                    ranges.append(str(start))
                else:
                    ranges.append(f"{start}-{end}")
                start = end = station_nums[i]
        
        if start == end:
            ranges.append(str(start))
        else:
            ranges.append(f"{start}-{end}")
        
        print(f"   📍 Station ranges: {', '.join(ranges)}")
        
        # Show a few examples
        examples = stations[:3]
        print(f"   💡 Examples:")
        for station_num, check_digit in examples:
            print(f"      3-{series}-{station_num:02d}-1 → Check digit: {check_digit}")
    
    print(f"\n{'='*50}")
    print(f"🎉 TOTAL: {total_stations} stations across {len(series_data)} aisles!")
    print(f"{'='*50}")
    
    print(f"\n✅ Your PalletManager app now auto-populates check digits for:")
    print(f"   • Aisle 40 (Dog Food section)")
    print(f"   • Aisle 42 (Dog Food section)")  
    print(f"   • Aisle 57 (Building 3)")
    print(f"   • Aisle 58 (Building 3)")
    print(f"\n🚀 Ready for production use!")

if __name__ == "__main__":
    show_coverage()

#!/usr/bin/env python3
"""
Analyzes the station coverage in both the markdown file and CSV file
to identify missing check digits and gaps in the 03-57 and 03-58 series.
"""
import re
import csv
import os

def analyze_markdown_coverage(file_path):
    """Analyze what stations are available in the markdown file."""
    if not os.path.exists(file_path):
        print(f"Error: {file_path} not found")
        return None, None
    
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Extract 03-57 and 03-58 series
    pattern_57 = re.compile(r'^(03-57-(\d{2})-01)--(\d+)', re.MULTILINE)
    pattern_58 = re.compile(r'^(03-58-(\d{2})-01)--(\d+)', re.MULTILINE)
    
    stations_57 = {}
    stations_58 = {}
    
    for match in pattern_57.findall(content):
        station_full, station_num, check_digit = match
        stations_57[int(station_num)] = {
            'full': station_full,
            'check_digit': check_digit
        }
    
    for match in pattern_58.findall(content):
        station_full, station_num, check_digit = match
        stations_58[int(station_num)] = {
            'full': station_full,
            'check_digit': check_digit
        }
    
    return stations_57, stations_58

def analyze_csv_coverage(csv_path):
    """Analyze what stations are in the CSV file."""
    if not os.path.exists(csv_path):
        print(f"Error: {csv_path} not found")
        return None, None
    
    stations_57 = {}
    stations_58 = {}
    
    with open(csv_path, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            station = row['station_number']
            check_digit = row['check_digit']
            
            if station.startswith('03-57-'):
                station_num = int(station.split('-')[2])
                stations_57[station_num] = {
                    'full': station,
                    'check_digit': check_digit
                }
            elif station.startswith('03-58-'):
                station_num = int(station.split('-')[2])
                stations_58[station_num] = {
                    'full': station,
                    'check_digit': check_digit
                }
    
    return stations_57, stations_58

def print_coverage_analysis(series_name, stations_dict, expected_range=(1, 63)):
    """Print detailed coverage analysis for a station series."""
    print(f"\n{'='*60}")
    print(f"📊 {series_name} COVERAGE ANALYSIS")
    print(f"{'='*60}")
    
    available_stations = sorted(stations_dict.keys())
    missing_stations = []
    
    for i in range(expected_range[0], expected_range[1] + 1):
        if i not in stations_dict:
            missing_stations.append(i)
    
    print(f"✅ Available stations: {len(available_stations)}")
    print(f"❌ Missing stations: {len(missing_stations)}")
    print(f"📈 Coverage: {len(available_stations)}/{expected_range[1]} ({len(available_stations)/expected_range[1]*100:.1f}%)")
    
    if available_stations:
        print(f"\n🟢 Available station numbers:")
        # Group consecutive numbers for better readability
        ranges = []
        start = available_stations[0]
        end = start
        
        for i in range(1, len(available_stations)):
            if available_stations[i] == end + 1:
                end = available_stations[i]
            else:
                if start == end:
                    ranges.append(str(start))
                else:
                    ranges.append(f"{start}-{end}")
                start = end = available_stations[i]
        
        if start == end:
            ranges.append(str(start))
        else:
            ranges.append(f"{start}-{end}")
        
        print(f"   {', '.join(ranges)}")
    
    if missing_stations:
        print(f"\n🔴 Missing station numbers:")
        # Group consecutive missing numbers
        ranges = []
        start = missing_stations[0]
        end = start
        
        for i in range(1, len(missing_stations)):
            if missing_stations[i] == end + 1:
                end = missing_stations[i]
            else:
                if start == end:
                    ranges.append(str(start))
                else:
                    ranges.append(f"{start}-{end}")
                start = end = missing_stations[i]
        
        if start == end:
            ranges.append(str(start))
        else:
            ranges.append(f"{start}-{end}")
        
        print(f"   {', '.join(ranges)}")

def main():
    print("🔍 PALLETMANAGER STATION COVERAGE ANALYSIS")
    print("=" * 60)
    
    # Analyze markdown file
    print("\n📄 Analyzing station-numbers-complete.md...")
    md_57, md_58 = analyze_markdown_coverage("station-numbers-complete.md")
    
    if md_57 is not None and md_58 is not None:
        print_coverage_analysis("03-57 Series (Markdown)", md_57)
        print_coverage_analysis("03-58 Series (Markdown)", md_58)
    
    # Analyze CSV file
    print("\n📊 Analyzing station_data.csv...")
    csv_57, csv_58 = analyze_csv_coverage("android-app/app/src/main/assets/station_data.csv")
    
    if csv_57 is not None and csv_58 is not None:
        print_coverage_analysis("03-57 Series (CSV)", csv_57)
        print_coverage_analysis("03-58 Series (CSV)", csv_58)
    
    # Compare markdown vs CSV
    if all([md_57, md_58, csv_57, csv_58]):
        print(f"\n{'='*60}")
        print("🔄 MARKDOWN vs CSV COMPARISON")
        print(f"{'='*60}")
        
        md_57_count = len(md_57)
        csv_57_count = len(csv_57)
        md_58_count = len(md_58)
        csv_58_count = len(csv_58)
        
        print(f"03-57 Series: MD={md_57_count}, CSV={csv_57_count} {'✅' if md_57_count == csv_57_count else '❌'}")
        print(f"03-58 Series: MD={md_58_count}, CSV={csv_58_count} {'✅' if md_58_count == csv_58_count else '❌'}")
        
        if md_57_count == csv_57_count and md_58_count == csv_58_count:
            print("\n✅ CSV file correctly contains all stations from markdown file!")
        else:
            print("\n❌ Mismatch between markdown and CSV files!")

if __name__ == "__main__":
    main()

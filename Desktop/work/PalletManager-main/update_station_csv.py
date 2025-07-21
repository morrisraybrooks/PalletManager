#!/usr/bin/env python3
"""
Extracts check digits from station-numbers-complete.md and updates 
the Android app's station_data.csv file for auto-population.
"""
import re
import os
import sys
import csv

def extract_check_digits_from_md(file_path):
    """
    Parses a station-numbers markdown file and extracts station/check digit pairs.
    """
    if not os.path.exists(file_path):
        print(f"Error: File not found at '{file_path}'", file=sys.stderr)
        return None

    with open(file_path, 'r') as f:
        content = f.read()

    # Regex to find station numbers with a check digit suffix
    # Format: 03-XX-XX-01--YY (looking for ALL series with check digits)
    pattern = re.compile(r'^(03-\d{2}-\d{2}-01)--(\d+)', re.MULTILINE)
    matches = pattern.findall(content)

    if not matches:
        print("No station data with check digits found in the markdown file.", file=sys.stderr)
        print(f"Make sure lines in '{os.path.basename(file_path)}' have check digits: '03-XX-XX-01--YY'.", file=sys.stderr)
        return None

    print(f"Found {len(matches)} stations with check digits in the markdown file")
    return matches

def update_android_csv(station_data, csv_path):
    """
    Updates the Android app's station_data.csv file with the extracted check digits.
    """
    try:
        # Read existing data
        existing_data = {}
        if os.path.exists(csv_path):
            with open(csv_path, 'r', newline='') as csvfile:
                reader = csv.DictReader(csvfile)
                for row in reader:
                    existing_data[row['station_number']] = row['check_digit']
            print(f"Read {len(existing_data)} existing stations from CSV")
        
        # Add new data from markdown file
        new_count = 0
        updated_count = 0
        for station, check_digit in station_data:
            if station in existing_data:
                if existing_data[station] != check_digit:
                    print(f"Updating {station}: {existing_data[station]} -> {check_digit}")
                    updated_count += 1
                existing_data[station] = check_digit
            else:
                existing_data[station] = check_digit
                new_count += 1
        
        # Write updated data back to CSV
        with open(csv_path, 'w', newline='') as csvfile:
            writer = csv.writer(csvfile)
            writer.writerow(['station_number', 'check_digit'])
            
            # Sort by station number for better organization
            sorted_stations = sorted(existing_data.items())
            for station, check_digit in sorted_stations:
                writer.writerow([station, check_digit])
        
        print(f"Successfully updated {csv_path}")
        print(f"New stations added: {new_count}")
        print(f"Existing stations updated: {updated_count}")
        print(f"Total stations in CSV: {len(existing_data)}")
        return True
        
    except Exception as e:
        print(f"Error updating CSV file: {e}", file=sys.stderr)
        return False

def main():
    # File paths
    md_file = "station-numbers-complete.md"
    csv_file = "android-app/app/src/main/assets/station_data.csv"
    
    print("Extracting check digits from markdown file...")
    station_data = extract_check_digits_from_md(md_file)
    
    if not station_data:
        print("No station data found. Exiting.")
        return 1
    
    print(f"Extracted {len(station_data)} stations with check digits")
    
    # Show some examples
    print("\nFirst 5 stations found:")
    for i, (station, check_digit) in enumerate(station_data[:5]):
        print(f"  {station} -> {check_digit}")
    
    print(f"\nUpdating Android CSV file: {csv_file}")
    success = update_android_csv(station_data, csv_file)
    
    if success:
        print("\n✅ Successfully updated the Android app's station database!")
        print("The app should now auto-populate check digits for 03-57 and 03-58 series stations.")
        return 0
    else:
        print("\n❌ Failed to update the CSV file.")
        return 1

if __name__ == "__main__":
    sys.exit(main())

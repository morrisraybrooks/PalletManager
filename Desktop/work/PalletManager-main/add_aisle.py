#!/usr/bin/env python3
"""
Script to easily add an entire aisle of check digits to the PalletManager app
"""
import csv
import os

def add_aisle_to_csv(aisle_number, check_digits, csv_path):
    """
    Add an entire aisle of check digits to the station_data.csv file
    
    Args:
        aisle_number: The aisle number (e.g., 59)
        check_digits: List of check digits for positions 01-63
        csv_path: Path to the station_data.csv file
    """
    
    # Read existing data
    existing_data = []
    if os.path.exists(csv_path):
        with open(csv_path, 'r', newline='') as csvfile:
            reader = csv.reader(csvfile)
            existing_data = list(reader)
    
    # Add new aisle data
    new_stations = []
    for i, check_digit in enumerate(check_digits, 1):
        if check_digit:  # Only add if check digit is provided
            station_number = f"{aisle_number:02d}-{i:02d}"
            new_stations.append([station_number, str(check_digit)])
    
    # Combine and sort
    all_data = existing_data[1:] + new_stations  # Skip header from existing
    all_data.sort(key=lambda x: x[0])  # Sort by station number
    
    # Write back to file
    with open(csv_path, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['station_number', 'check_digit'])  # Header
        writer.writerows(all_data)
    
    print(f"✅ Added {len(new_stations)} stations for aisle {aisle_number}")
    print(f"📁 Updated file: {csv_path}")

def generate_sample_aisle(aisle_number):
    """Generate sample check digits for an aisle (you'll replace with real ones)"""
    import random
    check_digits = []
    for i in range(1, 64):  # Positions 01-63
        # Generate random 2-digit check digits as placeholders
        # Replace these with your actual recorded check digits!
        check_digits.append(random.randint(10, 99))
    return check_digits

def main():
    print("🚛 PalletManager Aisle Check Digit Importer")
    print("=" * 50)
    
    # Get aisle number
    try:
        aisle_num = int(input("Enter aisle number (e.g., 59): "))
    except ValueError:
        print("❌ Invalid aisle number")
        return
    
    # Path to CSV file
    csv_path = "android-app/app/src/main/assets/station_data.csv"
    
    print(f"\n📋 Adding aisle {aisle_num} to {csv_path}")
    
    # Option 1: Manual entry
    print("\nChoose input method:")
    print("1. Enter check digits manually")
    print("2. Generate sample data (for testing)")
    print("3. Load from file")
    
    choice = input("Enter choice (1-3): ").strip()
    
    if choice == "1":
        print(f"\nEnter check digits for aisle {aisle_num} (positions 01-63)")
        print("Press Enter to skip a position, or type 'done' to finish early")
        
        check_digits = []
        for pos in range(1, 64):
            while True:
                digit = input(f"Position {pos:02d}: ").strip()
                if digit.lower() == 'done':
                    break
                if digit == "":
                    check_digits.append(None)
                    break
                try:
                    check_digits.append(int(digit))
                    break
                except ValueError:
                    print("Please enter a valid number or press Enter to skip")
            
            if digit.lower() == 'done':
                break
    
    elif choice == "2":
        print("🎲 Generating sample check digits...")
        check_digits = generate_sample_aisle(aisle_num)
        print("⚠️  Remember to replace these with your actual recorded check digits!")
    
    elif choice == "3":
        file_path = input("Enter path to file with check digits: ").strip()
        if os.path.exists(file_path):
            with open(file_path, 'r') as f:
                lines = f.readlines()
            check_digits = []
            for line in lines:
                line = line.strip()
                if line and line.isdigit():
                    check_digits.append(int(line))
        else:
            print("❌ File not found")
            return
    
    else:
        print("❌ Invalid choice")
        return
    
    # Add to CSV
    add_aisle_to_csv(aisle_num, check_digits, csv_path)
    
    print("\n🔄 To apply changes:")
    print("1. Rebuild the app: ./gradlew clean build installDebug")
    print("2. Or use 'Import Sample' button in the app")
    print("\n✅ Done!")

if __name__ == "__main__":
    main()

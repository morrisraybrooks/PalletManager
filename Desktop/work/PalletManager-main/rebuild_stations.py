#!/usr/bin/env python3
"""
Script to rebuild the complete station-numbers.md file with all districts 01-58
"""
import os

def generate_district_section(aisle_num, existing_data=None):
    """Generate a complete district section for a given aisle number"""
    section = f"## District 03-{aisle_num:02d} Series\n"
    
    if existing_data and aisle_num in existing_data:
        # Use existing data if available
        for station_data in existing_data[aisle_num]:
            section += station_data + "\n"
    else:
        # Generate empty template
        for station in range(1, 64):  # 01 to 63
            section += f"03-{aisle_num:02d}-{station:02d}-01--\n"
    
    section += "\n"
    return section

def parse_existing_data(base_path):
    """Parse existing station data from the current file"""
    existing_data = {}
    source_file = os.path.join(base_path, 'station-numbers.md')
    
    try:
        if not os.path.exists(source_file):
            print(f"Source file not found: {source_file}. Starting with no existing data.")
            return {}
        with open(source_file, 'r') as f:
            content = f.read()
        
        import re
        
        # Split by district sections
        sections = re.split(r'## District 03-(\d+) Series', content)
        
        for i in range(1, len(sections), 2):
            if i + 1 < len(sections):
                district_num = int(sections[i])
                district_content = sections[i + 1].strip()
                
                # Extract station lines
                lines = [line.strip() for line in district_content.split('\n') 
                        if line.strip() and line.strip().startswith('03-')]
                
                existing_data[district_num] = lines
        
        return existing_data
    
    except Exception as e:
        print(f"Error parsing existing data: {e}")
        return {}

def main():
    # Get the directory where the script is located
    script_dir = os.path.dirname(os.path.abspath(__file__))

    # Parse existing station data
    existing_data = parse_existing_data(script_dir)
    print(f"Found existing data for districts: {sorted(existing_data.keys())}")
    
    # Generate complete file
    complete_content = "# Station Numbers - Organized and Sorted\n\n"
    
    # Generate all districts 01-58
    for aisle in range(1, 59):
        complete_content += generate_district_section(aisle, existing_data)
    
    # Add summary section
    complete_content += "## Summary\n"
    complete_content += "**Building 3 Complete Coverage:**\n"
    complete_content += "- Districts 03-01 through 03-58 (58 aisles)\n"
    complete_content += "- Each district contains stations 01-63 (63 stations per aisle)\n"
    complete_content += f"- Total stations: {58 * 63} stations\n"
    
    # Write to new file
    output_file = os.path.join(script_dir, 'station-numbers-complete.md')
    with open(output_file, 'w') as f:
        f.write(complete_content)
    
    print(f"\nComplete station numbers file generated: {output_file}")
    print(f"Total districts: 58")
    print(f"Total stations: {58 * 63}")

if __name__ == "__main__":
    main()

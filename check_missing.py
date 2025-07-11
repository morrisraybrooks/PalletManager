#!/usr/bin/env python3
"""
Script to identify missing station number sections
"""

# Read the current file and identify existing districts
existing_districts = set()

try:
    with open('station-numbers.md', 'r') as f:
        content = f.read()
        
    # Find all existing district headers
    import re
    pattern = r'## District 03-(\d+) Series'
    matches = re.findall(pattern, content)
    
    for match in matches:
        existing_districts.add(int(match))
    
    print("Existing districts:", sorted(existing_districts))
    
    # Identify missing districts (01-58)
    all_districts = set(range(1, 59))  # 1 to 58
    missing_districts = all_districts - existing_districts
    
    print("Missing districts:", sorted(missing_districts))
    print(f"Total missing: {len(missing_districts)}")
    
    # Generate the missing sections
    missing_sections = []
    for district in sorted(missing_districts):
        section = f"## District 03-{district:02d} Series\n"
        for station in range(1, 64):  # 01 to 63
            section += f"03-{district:02d}-{station:02d}-01--\n"
        section += "\n"
        missing_sections.append((district, section))
    
    print(f"Generated {len(missing_sections)} sections")
    
    # Save to file for reference
    with open('missing_sections.txt', 'w') as f:
        for district, section in missing_sections:
            f.write(section)
    
    print("Missing sections saved to missing_sections.txt")
    
except Exception as e:
    print(f"Error: {e}")

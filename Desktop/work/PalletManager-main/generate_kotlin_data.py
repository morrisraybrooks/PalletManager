#!/usr/bin/env python3
"""
Reads the station-numbers-complete.md file and generates a Kotlin
code snippet for pre-populating the app's station database.
"""
import re
import os
import sys

def generate_kotlin_data_from_md(file_path):
    """
    Parses a station-numbers markdown file and generates a Kotlin
    code snippet for pre-populating the database.
    """
    if not os.path.exists(file_path):
        print(f"Error: File not found at '{file_path}'", file=sys.stderr)
        print("Please run 'python3 rebuild_stations.py' first to generate 'station-numbers-complete.md'.", file=sys.stderr)
        print("Then, fill it with your check digits in the format '03-XX-XX-01--YY'.", file=sys.stderr)
        return None

    with open(file_path, 'r') as f:
        content = f.read()

    # Regex to find station numbers with a check digit suffix
    # Format: 03-XX-XX-01--YY
    pattern = re.compile(r'^(03-\d{2}-\d{2}-01)--(\d+)', re.MULTILINE)
    matches = pattern.findall(content)

    if not matches:
        print("No station data with check digits found in the file.", file=sys.stderr)
        print(f"Make sure lines in '{os.path.basename(file_path)}' have a check digit: '03-XX-XX-01--YY'.", file=sys.stderr)
        return None

    kotlin_list_items = [f'    "{station}" to "{check_digit}"' for station, check_digit in matches]

    kotlin_code = "val yourStationData = listOf(\n"
    kotlin_code += ",\n".join(kotlin_list_items)
    kotlin_code += "\n)"

    return kotlin_code

if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))
    source_file = os.path.join(script_dir, 'station-numbers-complete.md')
    
    kotlin_snippet = generate_kotlin_data_from_md(source_file)
    
    if kotlin_snippet:
        print(kotlin_snippet)
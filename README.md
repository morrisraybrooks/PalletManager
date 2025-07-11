# Building 3 Station Numbers

A comprehensive station numbering system for Building 3, containing all aisles and stations organized in a structured format.

## Overview

This project contains the complete station numbering system for Building 3, which includes:
- **58 Aisles**: Numbered from 01 to 58
- **63 Stations per aisle**: Each aisle contains stations numbered 01-63
- **Total stations**: 3,654 individual station numbers

## File Structure

- `station-numbers.md` - Main file containing all station numbers organized by district
- `station-numbers-complete.md` - Complete backup version
- `rebuild_stations.py` - Python script to generate and organize station sections
- `check_missing.py` - Utility script to identify missing station sections

## Station Number Format

Each station follows the format: `03-[aisle]-[station]-01--[suffix]`

Where:
- `03` = Building/Region code
- `[aisle]` = Aisle number (01-58)
- `[station]` = Station number within aisle (01-63)
- `01` = Category/Type code
- `[suffix]` = Two-digit suffix code (varies by station)

## Example

```
03-42-15-01--69
```
- Building 3
- Aisle 42
- Station 15
- Category 01
- Suffix 69

## Districts

The file is organized into 58 districts:
- District 03-01 through District 03-58
- Each district represents one aisle in Building 3
- Each district contains exactly 63 stations

## Usage

The station numbers can be used for:
- Inventory management
- Location tracking
- Warehouse organization
- Asset management systems

## Scripts

### rebuild_stations.py
Generates the complete station numbering structure while preserving existing suffix codes.

### check_missing.py
Identifies any missing station sections and generates reports.

## Building 3 Layout

Building 3 uses a station numbering pattern where each aisle (01-58) contains 63 stations numbered 01-63, following the format `03-[aisle]-[station]-01--[suffix]`.

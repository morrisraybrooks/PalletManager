#!/usr/bin/env python3
"""
Generate a comprehensive PDF map of Building 3 warehouse layout
Shows all 58 aisles with 63 stations each, including breezeways
"""

import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.backends.backend_pdf import PdfPages
import numpy as np
import re
import os
import subprocess
import sys

def load_station_data(base_path):
    """Load station data from station-numbers.md"""
    station_data = {}
    station_file_path = os.path.join(base_path, 'station-numbers.md')
    
    try:
        print(f"Attempting to load station data from: {station_file_path}")
        with open(station_file_path, 'r') as f:
            content = f.read()
        
        # Find all stations with check digits
        pattern = r'03-(\d+)-(\d+)-01--(\d+)'
        matches = re.findall(pattern, content)
        
        for aisle, station, suffix in matches:
            aisle_num = int(aisle)
            station_num = int(station)
            if aisle_num not in station_data:
                station_data[aisle_num] = {}
            station_data[aisle_num][station_num] = suffix
            
        return station_data
    except Exception as e:
        print(f"Error loading station data from {station_file_path}: {e}")
        return {}

def find_breezeways(station_data):
    """Identify breezeway locations in each aisle"""
    breezeways = {}
    
    for aisle in station_data:
        stations = sorted(station_data[aisle].keys())
        gaps = []
        
        for i in range(len(stations) - 1):
            if stations[i+1] - stations[i] > 1:
                gap_start = stations[i] + 1
                gap_end = stations[i+1] - 1
                gaps.append((gap_start, gap_end))
        
        if gaps:
            breezeways[aisle] = gaps[0]  # Take the first (main) breezeway
    
    return breezeways

def create_warehouse_map(station_data, breezeways):
    """Create the main warehouse layout map"""

    # Create figure
    fig, ax = plt.subplots(1, 1, figsize=(20, 30))
    
    # Constants
    AISLE_WIDTH = 0.8
    AISLE_HEIGHT = 12
    STATION_HEIGHT = AISLE_HEIGHT / 63
    AISLE_SPACING = 1.2
    
    # Colors
    STATION_COLOR = '#E3F2FD'  # Light blue
    BREEZEWAY_COLOR = '#FFEB3B'  # Yellow
    STATION_WITH_DATA_COLOR = '#4CAF50'  # Green
    AISLE_BORDER_COLOR = '#1976D2'  # Dark blue
    
    # Draw title
    ax.text(29, 62, 'Building 3 Warehouse Layout', 
            fontsize=24, fontweight='bold', ha='center')
    ax.text(29, 60.5, 'Dollar General Distribution Center', 
            fontsize=16, ha='center')
    ax.text(29, 59.5, f'58 Aisles × 63 Stations = {58*63} Total Locations', 
            fontsize=14, ha='center')
    
    # Draw aisles
    for aisle_idx in range(58):
        aisle_num = aisle_idx + 1
        x_pos = aisle_idx * AISLE_SPACING
        
        # Draw aisle border
        aisle_rect = patches.Rectangle(
            (x_pos, 0), AISLE_WIDTH, AISLE_HEIGHT,
            linewidth=2, edgecolor=AISLE_BORDER_COLOR, 
            facecolor='none'
        )
        ax.add_patch(aisle_rect)
        
        # Draw aisle number
        ax.text(x_pos + AISLE_WIDTH/2, -1, f'{aisle_num:02d}', 
                fontsize=8, ha='center', fontweight='bold')
        
        # Draw stations
        for station_idx in range(63):
            station_num = station_idx + 1
            y_pos = station_idx * STATION_HEIGHT
            
            # Determine station color
            is_breezeway = False
            if aisle_num in breezeways:
                gap_start, gap_end = breezeways[aisle_num]
                if gap_start <= station_num <= gap_end:
                    is_breezeway = True
            
            has_data = (aisle_num in station_data and 
                       station_num in station_data[aisle_num])
            
            if is_breezeway:
                color = BREEZEWAY_COLOR
            elif has_data:
                color = STATION_WITH_DATA_COLOR
            else:
                color = STATION_COLOR
            
            # Draw station rectangle
            station_rect = patches.Rectangle(
                (x_pos + 0.05, y_pos), AISLE_WIDTH - 0.1, STATION_HEIGHT - 0.02,
                facecolor=color, edgecolor='gray', linewidth=0.5
            )
            ax.add_patch(station_rect)
            
            # Add station number for key stations
            if station_num % 10 == 1 or station_num in [30, 31, 32, 33]:
                ax.text(x_pos + AISLE_WIDTH/2, y_pos + STATION_HEIGHT/2, 
                       f'{station_num:02d}', fontsize=6, ha='center', va='center')
    
    # Add legend
    legend_x = 50
    legend_y = 55
    
    # Legend rectangles
    legend_items = [
        (STATION_COLOR, 'Empty Station'),
        (STATION_WITH_DATA_COLOR, 'Station with Check Digit'),
        (BREEZEWAY_COLOR, 'Breezeway'),
    ]
    
    ax.text(legend_x, legend_y + 2, 'Legend:', fontsize=12, fontweight='bold')
    
    for i, (color, label) in enumerate(legend_items):
        y = legend_y - i * 1.5
        legend_rect = patches.Rectangle(
            (legend_x, y), 1, 0.8, facecolor=color, edgecolor='gray'
        )
        ax.add_patch(legend_rect)
        ax.text(legend_x + 1.5, y + 0.4, label, fontsize=10, va='center')
    
    # Add breezeway information
    ax.text(legend_x, legend_y - 6, 'Breezeway Locations:', 
            fontsize=12, fontweight='bold')
    
    breezeway_info = []
    for aisle in sorted(breezeways.keys()):
        gap_start, gap_end = breezeways[aisle]
        breezeway_info.append(f'Aisle {aisle:02d}: Stations {gap_start}-{gap_end}')
    
    for i, info in enumerate(breezeway_info[:10]):  # Show first 10
        ax.text(legend_x, legend_y - 7.5 - i * 0.8, info, fontsize=8)
    
    if len(breezeway_info) > 10:
        ax.text(legend_x, legend_y - 15.5, f'... and {len(breezeway_info) - 10} more', 
                fontsize=8, style='italic')
    
    # Set axis properties
    ax.set_xlim(-2, 60)
    ax.set_ylim(-3, 65)
    ax.set_aspect('equal')
    ax.axis('off')
    
    return fig

def create_detailed_aisle_maps(station_data, breezeways):
    """Create detailed maps for aisles with check digit data"""
    figures = []
    
    # Find aisles with substantial data
    aisles_with_data = [aisle for aisle in station_data.keys() 
                       if len(station_data[aisle]) > 20]
    
    for aisle_num in sorted(aisles_with_data):
        fig, ax = plt.subplots(1, 1, figsize=(16, 10))
        
        # Title
        ax.text(0.5, 0.95, f'Aisle {aisle_num:02d} - Detailed Station Map', 
                fontsize=20, fontweight='bold', ha='center', transform=ax.transAxes)
        
        # Draw stations in a grid
        stations_per_row = 21
        rows = 3
        
        station_width = 0.8 / stations_per_row
        station_height = 0.2
        
        for station_num in range(1, 64):
            row = (station_num - 1) // stations_per_row
            col = (station_num - 1) % stations_per_row
            
            x = col * station_width + 0.1
            y = 0.7 - row * 0.25
            
            # Determine color
            is_breezeway = False
            if aisle_num in breezeways:
                gap_start, gap_end = breezeways[aisle_num]
                if gap_start <= station_num <= gap_end:
                    is_breezeway = True
            
            has_data = station_num in station_data[aisle_num]
            
            if is_breezeway:
                color = '#FFEB3B'  # Yellow
                text_color = 'black'
            elif has_data:
                color = '#4CAF50'  # Green
                text_color = 'white'
            else:
                color = '#E3F2FD'  # Light blue
                text_color = 'black'
            
            # Draw station
            station_rect = patches.Rectangle(
                (x, y), station_width * 0.9, station_height * 0.8,
                facecolor=color, edgecolor='gray', linewidth=1
            )
            ax.add_patch(station_rect)
            
            # Add station number
            ax.text(x + station_width * 0.45, y + station_height * 0.5, 
                   f'{station_num:02d}', fontsize=8, ha='center', va='center',
                   color=text_color, fontweight='bold')
            
            # Add check digit if available
            if has_data:
                check_digit = station_data[aisle_num][station_num]
                ax.text(x + station_width * 0.45, y + station_height * 0.2, 
                       f'({check_digit})', fontsize=6, ha='center', va='center',
                       color=text_color)
        
        # Add information
        info_text = f"""
Aisle {aisle_num:02d} Information:
• Total Stations: 63
• Stations with Check Digits: {len(station_data[aisle_num])}
• Format: 03-{aisle_num:02d}-XX-01--YY
"""
        
        if aisle_num in breezeways:
            gap_start, gap_end = breezeways[aisle_num]
            info_text += f"• Breezeway: Stations {gap_start}-{gap_end}\n"
        
        ax.text(0.05, 0.15, info_text, fontsize=12, va='top', 
                transform=ax.transAxes, bbox=dict(boxstyle="round,pad=0.3", 
                facecolor="lightgray", alpha=0.8))
        
        ax.set_xlim(0, 1)
        ax.set_ylim(0, 1)
        ax.axis('off')
        
        figures.append(fig)
    
    return figures

def create_two_row_layout_map(station_data, breezeways):
    """Create a warehouse map based on the user's two-row layout description."""
    # There are 29 odd and 29 even aisles. A wide figure is needed.
    fig, ax = plt.subplots(1, 1, figsize=(30, 12))

    # Constants
    AISLE_WIDTH = 0.8
    AISLE_HEIGHT = 4.0
    AISLE_SPACING_X = 1.0
    TOP_ROW_Y = 5.5
    BOTTOM_ROW_Y = 0.0

    # Colors
    ODD_AISLE_COLOR = '#E3F2FD'  # Light blue for odd aisles
    EVEN_AISLE_COLOR = '#F3E5F5'  # Light purple for even aisles
    DATA_AISLE_COLOR = '#C8E6C9'  # Light green for aisles with data
    BREEZEWAY_COLOR = '#FFEB3B'  # Yellow for breezeways

    # Title
    ax.text(14.5, 10.5, 'Building 3 - Warehouse Layout',
            fontsize=24, fontweight='bold', ha='center')
    ax.text(14.5, 9.7, 'Top Row: Odd Aisles (57-1) | Bottom Row: Even Aisles (58-2)',
            fontsize=14, ha='center', style='italic')

    # Top Row - Odd Aisles (decreasing from 57)
    odd_aisles = sorted([aisle for aisle in range(1, 58, 2)], reverse=True)  # [57, 55, ..., 1]
    for i, aisle in enumerate(odd_aisles):
        x_pos = i * AISLE_SPACING_X + 0.5
        y_pos = TOP_ROW_Y

        # Determine color
        color = DATA_AISLE_COLOR if aisle in station_data else ODD_AISLE_COLOR
        label = f'{aisle:02d}'

        # Draw aisle
        aisle_rect = patches.Rectangle(
            (x_pos, y_pos), AISLE_WIDTH, AISLE_HEIGHT,
            facecolor=color, edgecolor='#1976D2', linewidth=1.5, zorder=2
        )
        ax.add_patch(aisle_rect)

        # Add label
        ax.text(x_pos + AISLE_WIDTH / 2, y_pos - 0.5,
                label, fontsize=10, ha='center', va='top', fontweight='bold', zorder=3)

        # Add breezeway indicator if present
        if aisle in breezeways:
            breezeway_patch = patches.Rectangle((x_pos, y_pos), AISLE_WIDTH, 0.5, facecolor=BREEZEWAY_COLOR, alpha=0.9, zorder=3)
            ax.add_patch(breezeway_patch)
            ax.text(x_pos + AISLE_WIDTH/2, y_pos + 0.25, 'BW', fontsize=8, ha='center', va='center', color='black', fontweight='bold', zorder=4)

    # Bottom Row - Even Aisles (decreasing from 58)
    even_aisles = sorted([aisle for aisle in range(2, 59, 2)], reverse=True)  # [58, 56, ..., 2]
    for i, aisle in enumerate(even_aisles):
        x_pos = i * AISLE_SPACING_X + 0.5
        y_pos = BOTTOM_ROW_Y

        # Determine color
        color = DATA_AISLE_COLOR if aisle in station_data else EVEN_AISLE_COLOR
        label = f'{aisle:02d}'

        # Draw aisle
        aisle_rect = patches.Rectangle(
            (x_pos, y_pos), AISLE_WIDTH, AISLE_HEIGHT,
            facecolor=color, edgecolor='#7B1FA2', linewidth=1.5, zorder=2
        )
        ax.add_patch(aisle_rect)

        # Add label
        ax.text(x_pos + AISLE_WIDTH / 2, y_pos - 0.5,
                label, fontsize=10, ha='center', va='top', fontweight='bold', zorder=3)
        
        if aisle == 58:
            ax.text(x_pos + AISLE_WIDTH / 2, y_pos + AISLE_HEIGHT / 2, '🐕\nDog\nFood', 
                    fontsize=9, ha='center', va='center', color='black', zorder=3)

        # Add breezeway indicator if present
        if aisle in breezeways:
            breezeway_patch = patches.Rectangle((x_pos, y_pos), AISLE_WIDTH, 0.5, facecolor=BREEZEWAY_COLOR, alpha=0.9, zorder=3)
            ax.add_patch(breezeway_patch)
            ax.text(x_pos + AISLE_WIDTH/2, y_pos + 0.25, 'BW', fontsize=8, ha='center', va='center', color='black', fontweight='bold', zorder=4)

    # Add legend
    legend_x = 0.5
    legend_y = -2.5

    ax.text(legend_x, legend_y + 1.0, 'Legend:', fontsize=12, fontweight='bold')

    legend_items = [
        (ODD_AISLE_COLOR, 'Odd Aisle'),
        (EVEN_AISLE_COLOR, 'Even Aisle'),
        (DATA_AISLE_COLOR, 'Aisle with Check Digit Data'),
        (BREEZEWAY_COLOR, 'Breezeway Indicator'),
    ]

    for i, (color, label) in enumerate(legend_items):
        y = legend_y - i * 0.6
        legend_rect = patches.Rectangle(
            (legend_x, y), 0.4, 0.4, facecolor=color, edgecolor='gray'
        )
        ax.add_patch(legend_rect)
        ax.text(legend_x + 0.6, y + 0.2, label, fontsize=10, va='center')

    ax.set_xlim(0, 30)
    ax.set_ylim(-3, 12)
    ax.set_aspect('equal')
    ax.axis('off')

    return fig

def save_and_open_pdf():
    """Generate enhanced PDF and open with default viewer"""
    # Determine the script's directory to locate station-numbers.md
    script_dir = os.path.dirname(os.path.abspath(__file__))

    # Load data
    station_data = load_station_data(script_dir)
    breezeways = find_breezeways(station_data)

    # Use the script's directory for output, creating an 'output' subfolder
    output_dir = os.path.join(script_dir, 'output')
    os.makedirs(output_dir, exist_ok=True)

    # Create new filename with timestamp
    import datetime
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    pdf_filename = os.path.join(output_dir, f'Building3_Warehouse_Map_Enhanced_{timestamp}.pdf')

    print(f"\nGenerating enhanced warehouse map PDF: {pdf_filename}")

    with PdfPages(pdf_filename) as pdf:
        # Two-row layout overview
        print("Creating two-row layout overview...")
        two_row_fig = create_two_row_layout_map(station_data, breezeways)
        pdf.savefig(two_row_fig, bbox_inches='tight', dpi=300)
        plt.close(two_row_fig)

        # Main warehouse overview
        print("Creating main warehouse overview...")
        main_fig = create_warehouse_map(station_data, breezeways)
        pdf.savefig(main_fig, bbox_inches='tight', dpi=300)
        plt.close(main_fig)

        # Detailed aisle maps
        print("Creating detailed aisle maps...")
        detailed_figs = create_detailed_aisle_maps(station_data, breezeways)

        for i, fig in enumerate(detailed_figs):
            print(f"Adding detailed map {i+1}/{len(detailed_figs)}...")
            pdf.savefig(fig, bbox_inches='tight', dpi=300)
            plt.close(fig)

    print(f"Enhanced PDF saved to: {pdf_filename}")

    # Open with default PDF viewer
    try:
        if sys.platform.startswith('linux'):
            subprocess.run(['xdg-open', pdf_filename])
        elif sys.platform.startswith('darwin'):  # macOS
            subprocess.run(['open', pdf_filename])
        elif sys.platform.startswith('win'):  # Windows
            os.startfile(pdf_filename)
        else:
            print(f"Please manually open: {pdf_filename}")

        print("Enhanced PDF opened with default viewer!")

    except Exception as e:
        print(f"Could not auto-open PDF: {e}")
        print(f"Please manually open: {pdf_filename}")

    return pdf_filename

if __name__ == "__main__":
    save_and_open_pdf()

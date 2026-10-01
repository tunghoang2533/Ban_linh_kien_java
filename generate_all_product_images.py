import os
import math
from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = 'src/main/resources/static/img/products'
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Load fonts
try:
    FONT_BOLD_XL = ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf', 32)
    FONT_BOLD_LG = ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf', 24)
    FONT_BOLD_MD = ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf', 18)
    FONT_BOLD_SM = ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf', 14)
    FONT_REG_MD = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 16)
    FONT_REG_SM = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 13)
    FONT_BADGE = ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf', 12)
except Exception:
    FONT_BOLD_XL = ImageFont.load_default()
    FONT_BOLD_LG = ImageFont.load_default()
    FONT_BOLD_MD = ImageFont.load_default()
    FONT_BOLD_SM = ImageFont.load_default()
    FONT_REG_MD = ImageFont.load_default()
    FONT_REG_SM = ImageFont.load_default()
    FONT_BADGE = ImageFont.load_default()

def draw_header_badge(draw, brand_name, brand_color, category_name):
    # Category badge top-left
    draw.rounded_rectangle([30, 28, 140, 56], radius=6, fill=(241, 245, 249, 255), outline=(203, 213, 225, 255), width=1)
    draw.text((42, 34), category_name.upper(), fill=(71, 85, 105, 255), font=FONT_BADGE)
    
    # Brand badge top-right
    bbox = draw.textbbox((0, 0), brand_name.upper(), font=FONT_BOLD_SM)
    bw = bbox[2] - bbox[0] + 28
    draw.rounded_rectangle([570 - bw, 28, 570, 56], radius=6, fill=brand_color)
    draw.text((570 - bw + 14, 34), brand_name.upper(), fill=(255, 255, 255, 255), font=FONT_BOLD_SM)

def draw_footer_info(draw, title, subtitle, spec_tag, accent_color):
    # Spec badge pill
    bbox = draw.textbbox((0, 0), spec_tag, font=FONT_BADGE)
    sw = bbox[2] - bbox[0] + 24
    draw.rounded_rectangle([30, 485, 30 + sw, 510], radius=12, fill=accent_color)
    draw.text((42, 489), spec_tag, fill=(255, 255, 255, 255), font=FONT_BADGE)
    
    # Product Title
    # Truncate title if too long
    display_title = title
    if len(display_title) > 34:
        display_title = display_title[:32] + '...'
    draw.text((30, 518), display_title, fill=(15, 23, 42, 255), font=FONT_BOLD_LG)
    draw.text((30, 552), subtitle, fill=(100, 116, 139, 255), font=FONT_REG_SM)

def render_cpu(filename, title, brand, model_code, cores_threads, socket, base_freq, accent_theme):
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    # Background card border
    draw.rounded_rectangle([12, 12, 588, 588], radius=20, fill=(255, 255, 255, 255), outline=(226, 232, 240, 255), width=2)
    
    is_intel = (brand.lower() == 'intel')
    brand_color = (0, 102, 204, 255) if is_intel else (227, 43, 33, 255)
    draw_header_badge(draw, brand, brand_color, "CPU Vi Xử Lý")
    
    # Central CPU Visual
    # PCB Substrate
    sub_color = (15, 76, 129, 255) if is_intel else (35, 60, 40, 255)
    draw.rounded_rectangle([165, 110, 435, 380], radius=14, fill=sub_color, outline=(218, 165, 32, 255), width=3)
    
    # Gold notches and alignment pin
    draw.polygon([(168, 113), (185, 113), (168, 130)], fill=(255, 215, 0, 255))
    draw.ellipse([160, 240, 170, 250], fill=(255, 255, 255, 255))
    draw.ellipse([430, 240, 440, 250], fill=(255, 255, 255, 255))
    
    # Metallic Integrated Heat Spreader (IHS)
    ihs_color = (226, 232, 240, 255)
    draw.rounded_rectangle([190, 135, 410, 355], radius=10, fill=ihs_color, outline=(148, 163, 184, 255), width=3)
    
    # Inner engraved plate
    draw.rounded_rectangle([205, 150, 395, 340], radius=6, fill=(241, 245, 249, 255), outline=(203, 213, 225, 255), width=1)
    
    # Brand text engraved on IHS
    if is_intel:
        draw.text((250, 165), "intel.", fill=(0, 102, 204, 255), font=FONT_BOLD_XL)
        draw.text((230, 215), "CORE", fill=(15, 23, 42, 255), font=FONT_BOLD_LG)
        draw.text((220, 250), model_code, fill=(30, 58, 138, 255), font=FONT_BOLD_LG)
        draw.text((225, 290), f"LGA {socket.replace('LGA', '').strip()}", fill=(100, 116, 139, 255), font=FONT_BOLD_SM)
        draw.text((235, 312), base_freq, fill=(100, 116, 139, 255), font=FONT_REG_SM)
    else:
        draw.text((235, 165), "AMDa", fill=(227, 43, 33, 255), font=FONT_BOLD_XL)
        draw.text((230, 215), "RYZEN", fill=(15, 23, 42, 255), font=FONT_BOLD_LG)
        draw.text((220, 250), model_code, fill=(194, 65, 12, 255), font=FONT_BOLD_LG)
        draw.text((240, 290), socket, fill=(100, 116, 139, 255), font=FONT_BOLD_SM)
        draw.text((235, 312), base_freq, fill=(100, 116, 139, 255), font=FONT_REG_SM)
        
    # Bottom info
    draw_footer_info(draw, title, f"{cores_threads} | Socket {socket} | {base_freq}", f"{cores_threads}", brand_color)
    
    img.save(os.path.join(OUTPUT_DIR, filename), 'PNG')

def render_mainboard(filename, title, brand, chipset, form_factor, socket, ddr_type):
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    draw.rounded_rectangle([12, 12, 588, 588], radius=20, fill=(255, 255, 255, 255), outline=(226, 232, 240, 255), width=2)
    
    brand_colors = {
        'asus': (26, 86, 219, 255),
        'msi': (220, 38, 38, 255),
        'gigabyte': (234, 88, 12, 255),
        'asrock': (15, 23, 42, 255)
    }
    brand_color = brand_colors.get(brand.lower(), (79, 70, 229, 255))
    draw_header_badge(draw, brand, brand_color, "Bo Mạch Chủ")
    
    # Motherboard PCB
    draw.rounded_rectangle([150, 95, 450, 440], radius=10, fill=(15, 23, 42, 255), outline=(51, 65, 85, 255), width=3)
    
    # IO Shield / Armor
    draw.rounded_rectangle([155, 100, 210, 260], radius=6, fill=(30, 41, 59, 255), outline=(71, 85, 105, 255), width=2)
    draw.text((165, 140), brand.upper(), fill=brand_color, font=FONT_BOLD_SM)
    
    # VRM Heatsink top
    draw.rounded_rectangle([215, 100, 360, 140], radius=4, fill=(51, 65, 85, 255))
    
    # CPU Socket
    draw.rounded_rectangle([230, 155, 340, 265], radius=6, fill=(203, 213, 225, 255), outline=(148, 163, 184, 255), width=2)
    draw.rounded_rectangle([250, 175, 320, 245], radius=4, fill=(241, 245, 249, 255))
    draw.text((260, 202), socket.replace("Socket ", ""), fill=(51, 65, 85, 255), font=FONT_BADGE)
    
    # RAM DIMM Slots (2 or 4 slots)
    for i in range(4):
        x = 365 + i * 16
        draw.rectangle([x, 140, x + 8, 280], fill=(2, 6, 23, 255), outline=(100, 116, 139, 255), width=1)
        draw.rectangle([x + 2, 145, x + 6, 275], fill=(30, 41, 59, 255))
    draw.text((375, 290), ddr_type, fill=(203, 213, 225, 255), font=FONT_BADGE)
    
    # Chipset Heatsink
    draw.rounded_rectangle([335, 330, 435, 410], radius=6, fill=(30, 41, 59, 255), outline=brand_color, width=2)
    draw.text((350, 360), chipset, fill=(255, 255, 255, 255), font=FONT_BOLD_SM)
    
    # PCIe x16 Armor Slot
    draw.rounded_rectangle([180, 295, 325, 315], radius=3, fill=(203, 213, 225, 255), outline=(148, 163, 184, 255), width=2)
    draw.rectangle([185, 302, 320, 308], fill=(15, 23, 42, 255))
    
    # M.2 Shield
    draw.rounded_rectangle([200, 330, 310, 350], radius=3, fill=(51, 65, 85, 255))
    draw.text((220, 334), "M.2 PCIe 4.0", fill=(148, 163, 184, 255), font=FONT_BADGE)
    
    # PCIe slot 2
    draw.rounded_rectangle([180, 370, 310, 385], radius=3, fill=(30, 41, 59, 255))
    
    draw_footer_info(draw, title, f"{chipset} | {form_factor} | {socket} | {ddr_type}", f"Chipset {chipset}", brand_color)
    img.save(os.path.join(OUTPUT_DIR, filename), 'PNG')

def render_ram(filename, title, brand, capacity, ddr, speed, is_rgb):
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    draw.rounded_rectangle([12, 12, 588, 588], radius=20, fill=(255, 255, 255, 255), outline=(226, 232, 240, 255), width=2)
    
    brand_colors = {
        'corsair': (234, 179, 8, 255),
        'kingston': (220, 38, 38, 255),
        'g.skill': (239, 68, 68, 255),
        'crucial': (2, 132, 199, 255)
    }
    brand_color = brand_colors.get(brand.lower(), (147, 51, 234, 255))
    draw_header_badge(draw, brand, brand_color, "Bộ Nhớ RAM")
    
    # Draw Dual RAM Sticks angled / layered
    for offset_y, alpha in [(140, 200), (220, 255)]:
        # Gold contacts at bottom
        draw.rectangle([110, offset_y + 110, 490, offset_y + 125], fill=(218, 165, 32, 255))
        # Notch in gold contacts
        draw.rectangle([280, offset_y + 110, 295, offset_y + 125], fill=(255, 255, 255, 255))
        
        # Black PCB base
        draw.rounded_rectangle([100, offset_y + 20, 500, offset_y + 115], radius=6, fill=(15, 23, 42, alpha), outline=(51, 65, 85, alpha), width=2)
        
        # Metal Heatspreader
        draw.rounded_rectangle([105, offset_y + 25, 495, offset_y + 105], radius=4, fill=(30, 41, 59, alpha))
        
        # RGB light bar if RGB
        if is_rgb:
            # Gradient colored bar
            colors = [(239, 68, 68), (249, 115, 22), (234, 179, 8), (34, 197, 94), (6, 182, 212), (168, 85, 247)]
            step = 380 / len(colors)
            for ci, c in enumerate(colors):
                draw.rounded_rectangle([110 + int(ci * step), offset_y + 12, 110 + int((ci + 1) * step), offset_y + 26], radius=3, fill=(*c, alpha))
        else:
            draw.rounded_rectangle([110, offset_y + 16, 490, offset_y + 26], radius=3, fill=(71, 85, 105, alpha))
            
        # Text on heatspreader
        draw.text((130, offset_y + 50), brand.upper(), fill=(241, 245, 249, alpha), font=FONT_BOLD_MD)
        draw.text((260, offset_y + 52), f"{ddr} {speed}", fill=(203, 213, 225, alpha), font=FONT_BOLD_SM)
        draw.text((410, offset_y + 50), capacity, fill=(251, 191, 36, alpha), font=FONT_BOLD_MD)
        
    draw_footer_info(draw, title, f"{capacity} | {ddr} Bus {speed} | {brand}", f"{ddr} {capacity}", brand_color)
    img.save(os.path.join(OUTPUT_DIR, filename), 'PNG')

def render_vga(filename, title, brand, vram, fans, model_name):
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    draw.rounded_rectangle([12, 12, 588, 588], radius=20, fill=(255, 255, 255, 255), outline=(226, 232, 240, 255), width=2)
    
    brand_colors = {
        'asus': (26, 86, 219, 255),
        'msi': (220, 38, 38, 255),
        'gigabyte': (234, 88, 12, 255),
        'sapphire': (13, 148, 136, 255)
    }
    brand_color = brand_colors.get(brand.lower(), (16, 185, 129, 255))
    draw_header_badge(draw, brand, brand_color, "Card Màn Hình (VGA)")
    
    # GPU Shroud
    draw.rounded_rectangle([80, 140, 520, 380], radius=16, fill=(15, 23, 42, 255), outline=(51, 65, 85, 255), width=3)
    
    # Metal Backplate line
    draw.line([(80, 150), (520, 150)], fill=(71, 85, 105, 255), width=4)
    
    # PCIe Connector bottom
    draw.rectangle([140, 380, 320, 395], fill=(218, 165, 32, 255))
    
    # Metal bracket left
    draw.rounded_rectangle([68, 160, 82, 360], radius=3, fill=(148, 163, 184, 255), outline=(100, 116, 139, 255), width=1)
    
    # Fans
    if fans == 3:
        centers = [160, 300, 440]
        radius = 55
    else:
        centers = [215, 385]
        radius = 70
        
    for cx in centers:
        cy = 260
        # Fan ring
        draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius], fill=(2, 6, 23, 255), outline=(71, 85, 105, 255), width=2)
        # Fan blades
        for a in range(0, 360, 45):
            rad = math.radians(a)
            bx = cx + math.cos(rad) * (radius - 10)
            by = cy + math.sin(rad) * (radius - 10)
            draw.line([(cx, cy), (bx, by)], fill=(51, 65, 85, 255), width=4)
        # Center hub
        draw.ellipse([cx - 20, cy - 20, cx + 20, cy + 20], fill=(30, 41, 59, 255), outline=brand_color, width=2)
        
    # Model text on shroud
    draw.text((100, 160), model_name.upper(), fill=(255, 255, 255, 255), font=FONT_BOLD_SM)
    draw.text((440, 160), vram, fill=(251, 191, 36, 255), font=FONT_BOLD_SM)
    
    draw_footer_info(draw, title, f"{model_name} | {vram} GDDR6 | {fans} Quạt Tản Nhiệt", f"{vram} GDDR6", brand_color)
    img.save(os.path.join(OUTPUT_DIR, filename), 'PNG')

def render_storage(filename, title, brand, capacity, storage_type, speed):
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    draw.rounded_rectangle([12, 12, 588, 588], radius=20, fill=(255, 255, 255, 255), outline=(226, 232, 240, 255), width=2)
    
    is_hdd = 'hdd' in storage_type.lower()
    brand_color = (14, 165, 233, 255)
    draw_header_badge(draw, brand, brand_color, "Ổ Cứng Lưu Trữ")
    
    if not is_hdd:
        # M.2 NVMe Stick or 2.5" SATA
        if 'sata' in storage_type.lower():
            # 2.5" SATA Enclosure
            draw.rounded_rectangle([150, 140, 450, 390], radius=14, fill=(15, 23, 42, 255), outline=(71, 85, 105, 255), width=3)
            # Label
            draw.rounded_rectangle([170, 160, 430, 370], radius=8, fill=(30, 41, 59, 255))
            draw.text((190, 180), brand.upper(), fill=(255, 255, 255, 255), font=FONT_BOLD_LG)
            draw.text((190, 220), "SOLID STATE DRIVE 2.5\"", fill=(148, 163, 184, 255), font=FONT_BOLD_SM)
            draw.text((190, 270), capacity, fill=(56, 189, 248, 255), font=FONT_BOLD_XL)
            draw.text((190, 320), speed, fill=(203, 213, 225, 255), font=FONT_REG_MD)
        else:
            # M.2 2280 NVMe SSD
            # PCB
            draw.rounded_rectangle([110, 210, 490, 320], radius=6, fill=(15, 76, 129, 255), outline=(30, 58, 138, 255), width=2)
            # Gold pin contacts right
            draw.rectangle([475, 225, 490, 305], fill=(218, 165, 32, 255))
            draw.rectangle([475, 255, 490, 275], fill=(15, 76, 129, 255))
            # Mounting screw semi-circle left
            draw.ellipse([100, 250, 120, 280], fill=(255, 255, 255, 255))
            
            # Controller and NAND flash chips
            draw.rectangle([140, 225, 210, 305], fill=(2, 6, 23, 255), outline=(51, 65, 85, 255), width=1)
            draw.rectangle([230, 225, 330, 305], fill=(2, 6, 23, 255), outline=(51, 65, 85, 255), width=1)
            draw.rectangle([350, 225, 450, 305], fill=(2, 6, 23, 255), outline=(51, 65, 85, 255), width=1)
            
            # Thermal sticker over chips
            draw.rounded_rectangle([130, 230, 460, 300], radius=4, fill=(15, 23, 42, 230), outline=brand_color, width=1)
            draw.text((145, 245), brand.upper(), fill=(255, 255, 255, 255), font=FONT_BOLD_MD)
            draw.text((250, 248), capacity, fill=(56, 189, 248, 255), font=FONT_BOLD_LG)
            draw.text((360, 252), "PCIe NVMe", fill=(203, 213, 225, 255), font=FONT_BADGE)
    else:
        # 3.5" Mechanical HDD
        draw.rounded_rectangle([150, 130, 450, 400], radius=14, fill=(226, 232, 240, 255), outline=(100, 116, 139, 255), width=3)
        # Silver platter cover
        draw.rounded_rectangle([165, 145, 435, 385], radius=10, fill=(241, 245, 249, 255), outline=(148, 163, 184, 255), width=2)
        # HDD Label
        draw.rounded_rectangle([180, 160, 420, 330], radius=6, fill=(15, 23, 42, 255))
        draw.text((200, 180), brand.upper(), fill=(34, 197, 94, 255), font=FONT_BOLD_LG)
        draw.text((200, 220), "BARRACUDA 3.5\" HDD", fill=(255, 255, 255, 255), font=FONT_BOLD_SM)
        draw.text((200, 260), capacity, fill=(255, 255, 255, 255), font=FONT_BOLD_XL)
        
    draw_footer_info(draw, title, f"{capacity} | {storage_type} | {speed}", f"{capacity} {storage_type}", brand_color)
    img.save(os.path.join(OUTPUT_DIR, filename), 'PNG')

def render_psu(filename, title, brand, wattage, efficiency, modular):
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    draw.rounded_rectangle([12, 12, 588, 588], radius=20, fill=(255, 255, 255, 255), outline=(226, 232, 240, 255), width=2)
    
    brand_color = (234, 179, 8, 255)
    draw_header_badge(draw, brand, (15, 23, 42, 255), "Nguồn Máy Tính (PSU)")
    
    # PSU Chassis
    draw.rounded_rectangle([120, 140, 480, 390], radius=12, fill=(15, 23, 42, 255), outline=(51, 65, 85, 255), width=3)
    
    # Fan grill circular
    cx, cy, r = 240, 265, 85
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(2, 6, 23, 255), outline=(71, 85, 105, 255), width=3)
    for concentric in [30, 55, 75]:
        draw.ellipse([cx - concentric, cy - concentric, cx + concentric, cy + concentric], outline=(51, 65, 85, 255), width=2)
    draw.ellipse([cx - 20, cy - 20, cx + 20, cy + 20], fill=(30, 41, 59, 255), outline=(234, 179, 8, 255), width=2)
    
    # Side spec panel
    draw.rounded_rectangle([355, 165, 465, 365], radius=8, fill=(30, 41, 59, 255), outline=(71, 85, 105, 255), width=1)
    draw.text((370, 185), brand.upper(), fill=(255, 255, 255, 255), font=FONT_BOLD_SM)
    draw.text((370, 220), wattage, fill=(234, 179, 8, 255), font=FONT_BOLD_LG)
    draw.text((370, 260), efficiency, fill=(255, 255, 255, 255), font=FONT_BADGE)
    draw.text((370, 290), modular, fill=(148, 163, 184, 255), font=FONT_BADGE)
    
    draw_footer_info(draw, title, f"Công suất {wattage} | Chuẩn {efficiency} | {modular}", f"{wattage} {efficiency}", (217, 119, 6, 255))
    img.save(os.path.join(OUTPUT_DIR, filename), 'PNG')

def render_case(filename, title, brand, form_factor, side_panel, rgb_fans):
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    draw.rounded_rectangle([12, 12, 588, 588], radius=20, fill=(255, 255, 255, 255), outline=(226, 232, 240, 255), width=2)
    
    draw_header_badge(draw, brand, (79, 70, 229, 255), "Vỏ Case Máy Tính")
    
    # Case Tower Frame
    draw.rounded_rectangle([170, 110, 430, 420], radius=14, fill=(15, 23, 42, 255), outline=(51, 65, 85, 255), width=4)
    
    # Tempered Glass Side Window
    draw.rounded_rectangle([200, 130, 410, 360], radius=8, fill=(30, 41, 59, 240), outline=(99, 102, 241, 255), width=2)
    
    # Interior components silhouette & RGB lighting
    # Motherboard silhouette
    draw.rounded_rectangle([215, 145, 330, 270], radius=4, fill=(15, 23, 42, 255))
    # CPU Cooler RGB circle
    draw.ellipse([245, 175, 295, 225], fill=(30, 41, 59, 255), outline=(236, 72, 153, 255), width=3)
    # GPU horizontal bar
    draw.rounded_rectangle([215, 280, 380, 310], radius=4, fill=(2, 6, 23, 255), outline=(59, 130, 246, 255), width=2)
    
    # Front Fans (right side)
    for fy in [160, 245, 330]:
        draw.ellipse([410, fy - 25, 425, fy + 25], fill=(168, 85, 247, 255), outline=(234, 179, 8, 255), width=1)
        
    # PSU Shroud bottom
    draw.rounded_rectangle([180, 375, 420, 410], radius=4, fill=(2, 6, 23, 255))
    draw.text((195, 385), brand.upper(), fill=(148, 163, 184, 255), font=FONT_BADGE)
    
    draw_footer_info(draw, title, f"{form_factor} | {side_panel} | {rgb_fans}", form_factor, (79, 70, 229, 255))
    img.save(os.path.join(OUTPUT_DIR, filename), 'PNG')

print("Generating images for all 76 products...")

# 1. CPU mapping
render_cpu('intel-i5-12400f.png', 'Intel Core i5-12400F', 'Intel', 'i5-12400F', '6 Nhân / 12 Luồng', 'LGA1700', '2.5GHz - 4.4GHz', 'blue')
render_cpu('intel-i3-12100f.png', 'Intel Core i3-12100F', 'Intel', 'i3-12100F', '4 Nhân / 8 Luồng', 'LGA1700', '3.3GHz - 4.3GHz', 'blue')
render_cpu('intel-i5-13400f.png', 'Intel Core i5-13400F', 'Intel', 'i5-13400F', '10 Nhân / 16 Luồng', 'LGA1700', '2.5GHz - 4.6GHz', 'blue')
render_cpu('intel-i7-12700f.png', 'Intel Core i7-12700F', 'Intel', 'i7-12700F', '12 Nhân / 20 Luồng', 'LGA1700', '2.1GHz - 4.9GHz', 'blue')
render_cpu('intel-i7-13700f.png', 'Intel Core i7-13700F', 'Intel', 'i7-13700F', '16 Nhân / 24 Luồng', 'LGA1700', '2.1GHz - 5.2GHz', 'blue')
render_cpu('intel-i9-13900f.png', 'Intel Core i9-13900F', 'Intel', 'i9-13900F', '24 Nhân / 32 Luồng', 'LGA1700', '2.0GHz - 5.6GHz', 'blue')

render_cpu('ryzen-5-5500.png', 'AMD Ryzen 5 5500', 'AMD', 'Ryzen 5 5500', '6 Nhân / 12 Luồng', 'Socket AM4', '3.6GHz - 4.2GHz', 'orange')
render_cpu('ryzen-5-5600x.png', 'AMD Ryzen 5 5600X', 'AMD', 'Ryzen 5 5600X', '6 Nhân / 12 Luồng', 'Socket AM4', '3.7GHz - 4.6GHz', 'orange')
render_cpu('ryzen-7-5700x.png', 'AMD Ryzen 7 5700X', 'AMD', 'Ryzen 7 5700X', '8 Nhân / 16 Luồng', 'Socket AM4', '3.4GHz - 4.6GHz', 'orange')
render_cpu('ryzen-5-7600x.png', 'AMD Ryzen 5 7600X', 'AMD', 'Ryzen 5 7600X', '6 Nhân / 12 Luồng', 'Socket AM5', '4.7GHz - 5.3GHz', 'orange')
render_cpu('ryzen-7-7700x.png', 'AMD Ryzen 7 7700X', 'AMD', 'Ryzen 7 7700X', '8 Nhân / 16 Luồng', 'Socket AM5', '4.5GHz - 5.4GHz', 'orange')
render_cpu('ryzen-9-7900x.png', 'AMD Ryzen 9 7900X', 'AMD', 'Ryzen 9 7900X', '12 Nhân / 24 Luồng', 'Socket AM5', '4.7GHz - 5.6GHz', 'orange')

# Legacy names aliases
render_cpu('i5.jpg', 'Intel Core i5-12400F', 'Intel', 'i5-12400F', '6 Nhân / 12 Luồng', 'LGA1700', '2.5GHz - 4.4GHz', 'blue')
render_cpu('1778572386_6a02dc6259c7f.webp', 'AMD Ryzen 5 5600X', 'AMD', 'Ryzen 5 5600X', '6 Nhân / 12 Luồng', 'Socket AM4', '3.7GHz - 4.6GHz', 'orange')
render_cpu('1778572395_6a02dc6bbdc93.jpg', 'AMD Ryzen 7 5700X', 'AMD', 'Ryzen 7 5700X', '8 Nhân / 16 Luồng', 'Socket AM4', '3.4GHz - 4.6GHz', 'orange')
render_cpu('1778572401_6a02dc71bbad1.webp', 'Intel Core i7-12700F', 'Intel', 'i7-12700F', '12 Nhân / 20 Luồng', 'LGA1700', '2.1GHz - 4.9GHz', 'blue')

# 2. Mainboard mapping
render_mainboard('asus-tuf-b760m-plus.png', 'ASUS TUF GAMING B760M-PLUS', 'ASUS', 'B760', 'mATX', 'LGA 1700', 'DDR4')
render_mainboard('1778571058_6a02d732241a5.png', 'ASUS TUF GAMING B760M-PLUS', 'ASUS', 'B760', 'mATX', 'LGA 1700', 'DDR4')
render_mainboard('msi-mag-b550-tomahawk.png', 'MSI MAG B550 TOMAHAWK', 'MSI', 'B550', 'ATX', 'AM4', 'DDR4')
render_mainboard('b550-tomahawk.jpg', 'MSI MAG B550 TOMAHAWK', 'MSI', 'B550', 'ATX', 'AM4', 'DDR4')
render_mainboard('gigabyte-b760m-ds3h.png', 'Gigabyte B760M DS3H', 'Gigabyte', 'B760', 'mATX', 'LGA 1700', 'DDR4')
render_mainboard('b760m-ds3h.jpg', 'Gigabyte B760M DS3H', 'Gigabyte', 'B760', 'mATX', 'LGA 1700', 'DDR4')
render_mainboard('asus-prime-b660m-a.png', 'ASUS PRIME B660M-A', 'ASUS', 'B660', 'mATX', 'LGA 1700', 'DDR4')
render_mainboard('msi-pro-b660m-a.png', 'MSI PRO B660M-A DDR4', 'MSI', 'B660', 'mATX', 'LGA 1700', 'DDR4')
render_mainboard('gigabyte-b660m-ds3h.png', 'Gigabyte B660M DS3H DDR4', 'Gigabyte', 'B660', 'mATX', 'LGA 1700', 'DDR4')
render_mainboard('asus-rog-strix-b660-f.png', 'ASUS ROG STRIX B660-F Gaming', 'ASUS', 'B660', 'ATX', 'LGA 1700', 'DDR5')
render_mainboard('msi-mag-z690-tomahawk.png', 'MSI MAG Z690 TOMAHAWK DDR4', 'MSI', 'Z690', 'ATX', 'LGA 1700', 'DDR4')
render_mainboard('asus-prime-b650-plus.png', 'ASUS PRIME B650-PLUS', 'ASUS', 'B650', 'ATX', 'AM5', 'DDR5')
render_mainboard('gigabyte-b650-aorus-elite-ax.png', 'Gigabyte B650 AORUS Elite AX', 'Gigabyte', 'B650', 'ATX', 'AM5', 'DDR5')
render_mainboard('msi-mpg-x670e-carbon-wifi.png', 'MSI MPG X670E Carbon WiFi', 'MSI', 'X670E', 'ATX', 'AM5', 'DDR5')
render_mainboard('gigabyte-b550m-ds3h.png', 'Gigabyte B550M DS3H', 'Gigabyte', 'B550', 'mATX', 'AM4', 'DDR4')
render_mainboard('asus-tuf-b550-plus.png', 'ASUS TUF Gaming B550-PLUS', 'ASUS', 'B550', 'ATX', 'AM4', 'DDR4')

# 3. RAM mapping
render_ram('kingston-fury-beast-ddr4-8gb.png', 'Kingston FURY Beast 8GB DDR4', 'Kingston', '8GB', 'DDR4', '3200MHz', False)
render_ram('fury-8gb.jpg', 'Kingston Fury Beast 8GB DDR4', 'Kingston', '8GB', 'DDR4', '3200MHz', False)
render_ram('kingston-fury-beast-ddr4-16gb.png', 'Kingston FURY Beast 16GB DDR4', 'Kingston', '16GB', 'DDR4', '3200MHz', False)
render_ram('fury-16gb.jpg', 'Kingston Fury Beast 16GB DDR4', 'Kingston', '16GB', 'DDR4', '3200MHz', False)
render_ram('kingston-fury-beast-ddr4-32gb.png', 'Kingston FURY Beast 32GB (2x16GB)', 'Kingston', '32GB Kit', 'DDR4', '3200MHz', False)
render_ram('kingston-fury-beast-ddr5-32gb.png', 'Kingston FURY Beast 32GB DDR5', 'Kingston', '32GB Kit', 'DDR5', '5200MHz', False)
render_ram('corsair-vengeance-ddr5-32gb.png', 'Corsair Vengeance 32GB DDR5', 'Corsair', '32GB', 'DDR5', '5600MHz', True)
render_ram('vengeance-32gb.jpg', 'Corsair Vengeance 32GB DDR5', 'Corsair', '32GB', 'DDR5', '5600MHz', True)
render_ram('corsair-vengeance-lpx-16gb.png', 'Corsair Vengeance LPX 16GB DDR4', 'Corsair', '16GB', 'DDR4', '3200MHz', False)
render_ram('corsair-vengeance-rgb-pro-16gb.png', 'Corsair Vengeance RGB Pro 16GB', 'Corsair', '16GB Kit', 'DDR4', '3600MHz', True)
render_ram('gskill-trident-z-rgb-32gb.png', 'G.Skill Trident Z RGB 32GB DDR4', 'G.Skill', '32GB Kit', 'DDR4', '3600MHz', True)
render_ram('corsair-dominator-platinum-rgb-32gb.png', 'Corsair Dominator Platinum 32GB', 'Corsair', '32GB Kit', 'DDR5', '5600MHz', True)

# 4. VGA mapping
render_vga('msi-rtx-4060-gaming-x.png', 'MSI GeForce RTX 4060 GAMING X', 'MSI', '8GB', 2, 'RTX 4060')
render_vga('rtx4060.jpg', 'MSI GeForce RTX 4060 GAMING X', 'MSI', '8GB', 2, 'RTX 4060')
render_vga('gigabyte-rx-7600-gaming-oc.png', 'Gigabyte RX 7600 GAMING OC', 'Gigabyte', '8GB', 3, 'RX 7600')
render_vga('rx7600.jpg', 'Gigabyte RX 7600 GAMING OC', 'Gigabyte', '8GB', 3, 'RX 7600')
render_vga('asus-dual-rtx-3060-12gb.png', 'ASUS Dual GeForce RTX 3060', 'ASUS', '12GB', 2, 'RTX 3060')
render_vga('msi-rtx-3060-ti-gaming-x.png', 'MSI Gaming X RTX 3060 Ti', 'MSI', '8GB', 2, 'RTX 3060 Ti')
render_vga('asus-tuf-rtx-3070-oc.png', 'ASUS TUF Gaming RTX 3070 OC', 'ASUS', '8GB', 3, 'RTX 3070')
render_vga('gigabyte-rtx-4060-gaming-oc.png', 'Gigabyte GeForce RTX 4060 OC', 'Gigabyte', '8GB', 3, 'RTX 4060')
render_vga('msi-rtx-4070-ventus-3x.png', 'MSI GeForce RTX 4070 VENTUS 3X', 'MSI', '12GB', 3, 'RTX 4070')
render_vga('sapphire-pulse-rx-6600.png', 'Sapphire PULSE RX 6600', 'Sapphire', '8GB', 2, 'RX 6600')
render_vga('sapphire-nitro-rx-6700-xt.png', 'Sapphire NITRO+ RX 6700 XT', 'Sapphire', '12GB', 3, 'RX 6700 XT')

# 5. Storage mapping
render_storage('wd-blue-sn580-500gb.png', 'WD Blue SN580 500GB NVMe', 'WD', '500GB', 'NVMe PCIe 4.0', '4150 MB/s')
render_storage('sn580-500.jpg', 'WD Blue SN580 500GB NVMe', 'WD', '500GB', 'NVMe PCIe 4.0', '4150 MB/s')
render_storage('wd-blue-sn580-1tb.png', 'WD Blue SN580 1TB NVMe', 'WD', '1TB', 'NVMe PCIe 4.0', '4150 MB/s')
render_storage('sn580-1tb.jpg', 'WD Blue SN580 1TB NVMe', 'WD', '1TB', 'NVMe PCIe 4.0', '4150 MB/s')
render_storage('seagate-barracuda-1tb-hdd.png', 'Seagate Barracuda 1TB HDD', 'Seagate', '1TB', '3.5" SATA HDD', '7200 RPM')
render_storage('barracuda-1tb.jpg', 'Seagate Barracuda 1TB HDD', 'Seagate', '1TB', '3.5" SATA HDD', '7200 RPM')
render_storage('seagate-barracuda-2tb-hdd.png', 'Seagate Barracuda 2TB HDD', 'Seagate', '2TB', '3.5" SATA HDD', '7200 RPM')
render_storage('samsung-870-evo-500gb.png', 'Samsung 870 EVO 500GB SATA', 'Samsung', '500GB', '2.5" SATA SSD', '560 MB/s')
render_storage('samsung-870-evo-1tb.png', 'Samsung 870 EVO 1TB SATA', 'Samsung', '1TB', '2.5" SATA SSD', '560 MB/s')
render_storage('samsung-980-pro-1tb.png', 'Samsung 980 PRO 1TB NVMe', 'Samsung', '1TB', 'NVMe PCIe 4.0', '7000 MB/s')
render_storage('wd-blue-sn570-1tb.png', 'WD Blue SN570 1TB NVMe', 'WD', '1TB', 'NVMe PCIe 3.0', '3500 MB/s')
render_storage('wd-black-sn850x-2tb.png', 'WD Black SN850X 2TB NVMe', 'WD', '2TB', 'NVMe PCIe 4.0', '7300 MB/s')
render_storage('crucial-p3-1tb.png', 'Crucial P3 1TB NVMe', 'Crucial', '1TB', 'NVMe PCIe 3.0', '3500 MB/s')

# 6. PSU mapping
render_psu('seasonic-focus-gx-650w.png', 'Seasonic Focus GX-650', 'Seasonic', '650W', '80+ Gold', 'Full Modular')
render_psu('focus-gx650.jpg', 'Seasonic Focus GX-650', 'Seasonic', '650W', '80+ Gold', 'Full Modular')
render_psu('corsair-rm750e-750w.png', 'Corsair RM750e 750W', 'Corsair', '750W', '80+ Gold', 'Full Modular')
render_psu('rm750e.jpg', 'Corsair RM750e 750W', 'Corsair', '750W', '80+ Gold', 'Full Modular')
render_psu('coolermaster-mwe-550w-white.png', 'Cooler Master MWE 550W', 'Cooler Master', '550W', '80+ White', 'Non Modular')
render_psu('coolermaster-mwe-gold-650w.png', 'Cooler Master MWE Gold 650W', 'Cooler Master', '650W', '80+ Gold', 'Semi Modular')
render_psu('seasonic-focus-gx-750w.png', 'Seasonic Focus GX-750W', 'Seasonic', '750W', '80+ Gold', 'Full Modular')
render_psu('seasonic-prime-tx-850w.png', 'Seasonic Prime TX-850W', 'Seasonic', '850W', '80+ Titanium', 'Full Modular')
render_psu('bequiet-straight-power-11-750w.png', 'be quiet! Straight Power 11 750W', 'be quiet!', '750W', '80+ Gold', 'Silent Wings 3')
render_psu('deepcool-pq650m-650w.png', 'DeepCool PQ650M 650W', 'DeepCool', '650W', '80+ Gold', 'Semi Modular')

# 7. Case mapping
render_case('coolermaster-masterbox-q300l.png', 'Cooler Master MasterBox Q300L', 'Cooler Master', 'mATX Mini Tower', 'Kính Cường Lực', 'Lưới Từ Tính')
render_case('q300l.jpg', 'Cooler Master MasterBox Q300L', 'Cooler Master', 'mATX Mini Tower', 'Kính Cường Lực', 'Lưới Từ Tính')
render_case('msi-mag-forge-100r.png', 'MSI MAG FORGE 100R', 'MSI', 'ATX Mid Tower', 'Kính Cường Lực', '3x ARGB Fans')
render_case('forge100r.jpg', 'MSI MAG FORGE 100R', 'MSI', 'ATX Mid Tower', 'Kính Cường Lực', '3x ARGB Fans')
render_case('coolermaster-masterbox-520-mesh.png', 'Cooler Master MasterBox 520 Mesh', 'Cooler Master', 'ATX Mid Tower', 'Kính Cường Lực', '3x CF120 ARGB')
render_case('nzxt-h510-flow.png', 'NZXT H510 Flow', 'NZXT', 'ATX Mid Tower', 'Kính Cường Lực', 'Front Mesh Airflow')
render_case('nzxt-h7-flow-rgb.png', 'NZXT H7 Flow RGB', 'NZXT', 'ATX Mid Tower', 'Kính Cường Lực', '3x F140 RGB Core')
render_case('lian-li-pc-o11-dynamic-evo.png', 'Lian Li PC-O11 Dynamic EVO', 'Lian Li', 'ATX Dual Chamber', 'Kính 2 Mặt', 'Kính Trong Suốt')
render_case('fractal-design-meshify-c.png', 'Fractal Design Meshify C', 'Fractal Design', 'ATX Mid Tower', 'Kính Cường Lực', 'Lưới Đa Giác')
render_case('thermaltake-view-71-tg.png', 'Thermaltake View 71 TG ARGB', 'Thermaltake', 'E-ATX Full Tower', 'Kính 4 Mặt 5mm', '3x 140mm ARGB')

print("All product images generated successfully!")

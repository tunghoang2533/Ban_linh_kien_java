import os
import time
import re
import io
import urllib.request
import urllib.parse
from PIL import Image

OUTPUT_DIR = 'src/main/resources/static/img/products'
os.makedirs(OUTPUT_DIR, exist_ok=True)

# List of all 76 products in db_ban_linh_kien.sql
PRODUCTS = [
    (1, "Core i5-12400F", "Intel Core i5-12400F box", "intel-i5-12400f.png", ["i5.jpg"]),
    (2, "Mainboard ASUS TUF GAMING B760M-PLUS", "ASUS TUF GAMING B760M-PLUS DDR4 motherboard", "asus-tuf-b760m-plus.png", ["1778571058_6a02d732241a5.png"]),
    (3, "AMD Ryzen 5 5600X", "AMD Ryzen 5 5600X box product", "ryzen-5-5600x.png", ["1778572386_6a02dc6259c7f.webp"]),
    (4, "AMD Ryzen 7 5700X", "AMD Ryzen 7 5700X box product", "ryzen-7-5700x.png", ["1778572395_6a02dc6bbdc93.jpg"]),
    (5, "Intel Core i7-12700F", "Intel Core i7-12700F box product", "intel-i7-12700f.png", ["1778572401_6a02dc71bbad1.webp"]),
    (6, "Kingston Fury Beast 8GB DDR4 3200MHz", "Kingston Fury Beast 8GB DDR4 3200MHz RAM", "kingston-fury-beast-ddr4-8gb.png", ["fury-8gb.jpg"]),
    (7, "Kingston Fury Beast 16GB DDR4 3200MHz", "Kingston Fury Beast 16GB DDR4 3200MHz RAM", "kingston-fury-beast-ddr4-16gb.png", ["fury-16gb.jpg"]),
    (8, "Corsair Vengeance 32GB DDR5 5600MHz", "Corsair Vengeance 32GB DDR5 5600MHz RAM", "corsair-vengeance-ddr5-32gb.png", ["vengeance-32gb.jpg"]),
    (9, "MSI MAG B550 TOMAHAWK", "MSI MAG B550 TOMAHAWK motherboard", "msi-mag-b550-tomahawk.png", ["b550-tomahawk.jpg"]),
    (10, "Gigabyte B760M DS3H", "Gigabyte B760M DS3H motherboard", "gigabyte-b760m-ds3h.png", ["b760m-ds3h.jpg"]),
    (11, "MSI GeForce RTX 4060 GAMING X", "MSI GeForce RTX 4060 GAMING X 8GB", "msi-rtx-4060-gaming-x.png", ["rtx4060.jpg"]),
    (12, "Gigabyte RX 7600 GAMING OC", "Gigabyte Radeon RX 7600 GAMING OC 8GB", "gigabyte-rx-7600-gaming-oc.png", ["rx7600.jpg"]),
    (13, "WD Blue SN580 500GB NVMe", "WD Blue SN580 500GB NVMe SSD", "wd-blue-sn580-500gb.png", ["sn580-500.jpg"]),
    (14, "WD Blue SN580 1TB NVMe", "WD Blue SN580 1TB NVMe SSD", "wd-blue-sn580-1tb.png", ["sn580-1tb.jpg"]),
    (15, "Seagate Barracuda 1TB HDD", "Seagate Barracuda 1TB HDD 3.5", "seagate-barracuda-1tb-hdd.png", ["barracuda-1tb.jpg"]),
    (16, "Seasonic Focus GX-650 80+ Gold", "Seasonic Focus GX-650 650W 80 Plus Gold PSU", "seasonic-focus-gx-650w.png", ["focus-gx650.jpg"]),
    (17, "Corsair RM750e 80+ Gold", "Corsair RM750e 750W 80 Plus Gold PSU", "corsair-rm750e-750w.png", ["rm750e.jpg"]),
    (18, "Cooler Master MasterBox Q300L", "Cooler Master MasterBox Q300L case", "coolermaster-masterbox-q300l.png", ["q300l.jpg"]),
    (19, "MSI MAG FORGE 100R", "MSI MAG FORGE 100R case", "msi-mag-forge-100r.png", ["forge100r.jpg"]),
    (20, "Intel Core i3-12100F", "Intel Core i3-12100F box product", "intel-i3-12100f.png", []),
    (21, "Intel Core i5-12400F", "Intel Core i5-12400F processor box", "intel-i5-12400f.png", []),
    (22, "Intel Core i5-13400F", "Intel Core i5-13400F processor box", "intel-i5-13400f.png", []),
    (23, "Intel Core i7-12700F", "Intel Core i7-12700F processor box", "intel-i7-12700f.png", []),
    (24, "Intel Core i7-13700F", "Intel Core i7-13700F processor box", "intel-i7-13700f.png", []),
    (25, "Intel Core i9-13900F", "Intel Core i9-13900F processor box", "intel-i9-13900f.png", []),
    (26, "AMD Ryzen 5 7600X", "AMD Ryzen 5 7600X box processor", "ryzen-5-7600x.png", []),
    (27, "AMD Ryzen 7 7700X", "AMD Ryzen 7 7700X box processor", "ryzen-7-7700x.png", []),
    (28, "AMD Ryzen 9 7900X", "AMD Ryzen 9 7900X box processor", "ryzen-9-7900x.png", []),
    (29, "AMD Ryzen 5 5500", "AMD Ryzen 5 5500 box processor", "ryzen-5-5500.png", []),
    (30, "AMD Ryzen 7 5700X", "AMD Ryzen 7 5700X box processor", "ryzen-7-5700x.png", []),
    (31, "ASUS PRIME B660M-A", "ASUS PRIME B660M-A motherboard", "asus-prime-b660m-a.png", []),
    (32, "MSI PRO B660M-A DDR4", "MSI PRO B660M-A DDR4 motherboard", "msi-pro-b660m-a.png", []),
    (33, "Gigabyte B660M DS3H DDR4", "Gigabyte B660M DS3H DDR4 motherboard", "gigabyte-b660m-ds3h.png", []),
    (34, "ASUS ROG STRIX B660-F Gaming", "ASUS ROG STRIX B660-F Gaming WiFi motherboard", "asus-rog-strix-b660-f.png", []),
    (35, "MSI MAG Z690 TOMAHAWK DDR4", "MSI MAG Z690 TOMAHAWK WIFI DDR4 motherboard", "msi-mag-z690-tomahawk.png", []),
    (36, "ASUS PRIME B650-PLUS", "ASUS PRIME B650-PLUS AM5 motherboard", "asus-prime-b650-plus.png", []),
    (37, "Gigabyte B650 AORUS Elite AX", "Gigabyte B650 AORUS Elite AX motherboard", "gigabyte-b650-aorus-elite-ax.png", []),
    (38, "MSI MPG X670E Carbon WiFi", "MSI MPG X670E Carbon WiFi motherboard", "msi-mpg-x670e-carbon-wifi.png", []),
    (39, "Gigabyte B550M DS3H", "Gigabyte B550M DS3H motherboard", "gigabyte-b550m-ds3h.png", []),
    (40, "ASUS TUF Gaming B550-PLUS", "ASUS TUF Gaming B550-PLUS motherboard", "asus-tuf-b550-plus.png", []),
    (41, "Kingston FURY Beast 8GB DDR4 3200MHz", "Kingston FURY Beast 8GB DDR4 3200MHz RAM", "kingston-fury-beast-ddr4-8gb.png", []),
    (42, "Kingston FURY Beast 32GB DDR4 3200MHz (2x16GB)", "Kingston FURY Beast 32GB 2x16GB DDR4 3200MHz RAM", "kingston-fury-beast-ddr4-32gb.png", []),
    (43, "Corsair Vengeance LPX 16GB DDR4 3200MHz", "Corsair Vengeance LPX 16GB DDR4 3200MHz RAM", "corsair-vengeance-lpx-16gb.png", []),
    (44, "Corsair Vengeance RGB Pro 16GB DDR4 3600MHz", "Corsair Vengeance RGB Pro 16GB DDR4 3600MHz RAM", "corsair-vengeance-rgb-pro-16gb.png", []),
    (45, "G.Skill Trident Z RGB 32GB DDR4 3600MHz (2x16GB)", "G.Skill Trident Z RGB 32GB DDR4 3600MHz RAM", "gskill-trident-z-rgb-32gb.png", []),
    (46, "Kingston FURY Beast 32GB DDR5 5200MHz (2x16GB)", "Kingston FURY Beast 32GB DDR5 5200MHz RAM", "kingston-fury-beast-ddr5-32gb.png", []),
    (47, "Corsair Dominator Platinum RGB 32GB DDR5 5600MHz", "Corsair Dominator Platinum RGB 32GB DDR5 RAM", "corsair-dominator-platinum-rgb-32gb.png", []),
    (48, "ASUS Dual GeForce RTX 3060 12GB", "ASUS Dual GeForce RTX 3060 12GB GPU", "asus-dual-rtx-3060-12gb.png", []),
    (49, "MSI Gaming X RTX 3060 Ti 8GB", "MSI GeForce RTX 3060 Ti Gaming X 8GB GPU", "msi-rtx-3060-ti-gaming-x.png", []),
    (50, "ASUS TUF Gaming RTX 3070 OC 8GB", "ASUS TUF Gaming RTX 3070 OC 8GB GPU", "asus-tuf-rtx-3070-oc.png", []),
    (51, "Gigabyte GeForce RTX 4060 Gaming OC 8GB", "Gigabyte GeForce RTX 4060 Gaming OC 8GB GPU", "gigabyte-rtx-4060-gaming-oc.png", []),
    (52, "MSI GeForce RTX 4070 VENTUS 3X 12GB", "MSI GeForce RTX 4070 VENTUS 3X 12GB GPU", "msi-rtx-4070-ventus-3x.png", []),
    (53, "Sapphire PULSE RX 6600 8GB", "Sapphire PULSE Radeon RX 6600 8GB GPU", "sapphire-pulse-rx-6600.png", []),
    (54, "Sapphire NITRO+ RX 6700 XT 12GB", "Sapphire NITRO+ Radeon RX 6700 XT 12GB GPU", "sapphire-nitro-rx-6700-xt.png", []),
    (55, "Gigabyte Radeon RX 7600 Gaming OC 8GB", "Gigabyte Radeon RX 7600 Gaming OC 8GB GPU", "gigabyte-rx-7600-gaming-oc.png", []),
    (56, "Samsung 870 EVO 500GB SATA SSD", "Samsung 870 EVO 500GB SATA 2.5 SSD", "samsung-870-evo-500gb.png", []),
    (57, "Samsung 870 EVO 1TB SATA SSD", "Samsung 870 EVO 1TB SATA 2.5 SSD", "samsung-870-evo-1tb.png", []),
    (58, "Samsung 980 PRO 1TB NVMe PCIe 4.0", "Samsung 980 PRO 1TB NVMe M.2 SSD", "samsung-980-pro-1tb.png", []),
    (59, "WD Blue SN570 1TB NVMe PCIe 3.0", "WD Blue SN570 1TB NVMe SSD", "wd-blue-sn570-1tb.png", []),
    (60, "WD Black SN850X 2TB NVMe PCIe 4.0", "WD Black SN850X 2TB NVMe SSD", "wd-black-sn850x-2tb.png", []),
    (61, "Crucial P3 1TB NVMe PCIe 3.0", "Crucial P3 1TB NVMe M.2 SSD", "crucial-p3-1tb.png", []),
    (62, "Seagate Barracuda 1TB HDD 3.5\"", "Seagate Barracuda 1TB 3.5 SATA HDD", "seagate-barracuda-1tb-hdd.png", []),
    (63, "Seagate Barracuda 2TB HDD 3.5\"", "Seagate Barracuda 2TB 3.5 SATA HDD", "seagate-barracuda-2tb-hdd.png", []),
    (64, "Cooler Master MWE 550W 80+ White", "Cooler Master MWE 550W 80 Plus White PSU", "coolermaster-mwe-550w-white.png", []),
    (65, "Cooler Master MWE Gold 650W 80+ Gold", "Cooler Master MWE Gold 650W 80 Plus Gold PSU", "coolermaster-mwe-gold-650w.png", []),
    (66, "Seasonic Focus GX-750W 80+ Gold", "Seasonic Focus GX-750 750W 80 Plus Gold PSU", "seasonic-focus-gx-750w.png", []),
    (67, "Seasonic Prime TX-850W 80+ Titanium", "Seasonic Prime TX-850 850W Titanium PSU", "seasonic-prime-tx-850w.png", []),
    (68, "be quiet! Straight Power 11 750W 80+ Gold", "be quiet! Straight Power 11 750W Gold PSU", "bequiet-straight-power-11-750w.png", []),
    (69, "DeepCool PQ650M 650W 80+ Gold", "DeepCool PQ650M 650W 80 Plus Gold PSU", "deepcool-pq650m-650w.png", []),
    (70, "Cooler Master MasterBox Q300L", "Cooler Master MasterBox Q300L case", "coolermaster-masterbox-q300l.png", []),
    (71, "Cooler Master MasterBox 520 Mesh", "Cooler Master MasterBox 520 Mesh case", "coolermaster-masterbox-520-mesh.png", []),
    (72, "NZXT H510 Flow", "NZXT H510 Flow matte black case", "nzxt-h510-flow.png", []),
    (73, "NZXT H7 Flow RGB", "NZXT H7 Flow RGB case", "nzxt-h7-flow-rgb.png", []),
    (74, "Lian Li PC-O11 Dynamic EVO", "Lian Li O11 Dynamic EVO case", "lian-li-pc-o11-dynamic-evo.png", []),
    (75, "Fractal Design Meshify C", "Fractal Design Meshify C case", "fractal-design-meshify-c.png", []),
    (76, "Thermaltake View 71 TG ARGB", "Thermaltake View 71 TG ARGB case", "thermaltake-view-71-tg.png", [])
]

def search_bing_images(query):
    try:
        url = f"https://www.bing.com/images/async?q={urllib.parse.quote(query + ' transparent white background')}&first=1&count=10&mmasync=1"
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.9',
            'Referer': 'https://www.bing.com/'
        }
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=8) as resp:
            content = resp.read().decode('utf-8', errors='ignore')
            
        murls = re.findall(r'murl&quot;:&quot;(https?://[^&]+?)&quot;', content)
        if not murls:
            murls = re.findall(r'"murl":"(https?://[^"]+?)"', content)
        return murls
    except Exception as e:
        print(f"Search error for {query}: {e}")
        return []

def download_and_process_image(url):
    try:
        req = urllib.request.Request(url, headers={
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
            'Accept': 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8'
        })
        with urllib.request.urlopen(req, timeout=10) as r:
            data = r.read()
            if len(data) < 2000: # too small
                return None
            
            raw_img = Image.open(io.BytesIO(data))
            # Convert to RGBA
            img = raw_img.convert('RGBA')
            
            # Create a clean white square canvas 600x600
            canvas_size = 600
            canvas = Image.new('RGBA', (canvas_size, canvas_size), (255, 255, 255, 255))
            
            # Resize image to fit nicely within 520x520 area
            max_w, max_h = 520, 520
            img.thumbnail((max_w, max_h), Image.Resampling.LANCZOS)
            
            # Center on canvas
            offset_x = (canvas_size - img.width) // 2
            offset_y = (canvas_size - img.height) // 2
            canvas.paste(img, (offset_x, offset_y), img)
            
            return canvas
    except Exception as e:
        # print(f"Download error for {url[:50]}: {e}")
        return None

downloaded_count = 0
for pid, name, query, target_filename, aliases in PRODUCTS:
    print(f"[{pid}/76] Fetching real image for: {name}...")
    target_path = os.path.join(OUTPUT_DIR, target_filename)
    
    # Search real image URLs
    urls = search_bing_images(query)
    success = False
    
    for u in urls[:5]:
        processed = download_and_process_image(u)
        if processed is not None:
            # Save target
            processed.save(target_path, 'PNG')
            
            # Save all aliases (legacy names)
            for alias in aliases:
                alias_path = os.path.join(OUTPUT_DIR, alias)
                if alias.endswith('.jpg') or alias.endswith('.jpeg'):
                    rgb_ver = processed.convert('RGB')
                    rgb_ver.save(alias_path, 'JPEG', quality=95)
                else:
                    processed.save(alias_path)
                    
            print(f"  -> SUCCESS! Saved real photo for {name} to {target_filename}")
            downloaded_count += 1
            success = True
            break
            
    if not success:
        print(f"  -> WARN: Could not fetch URL for {name}, will keep fallback.")
        
    time.sleep(0.3)

print(f"\nFinished! Downloaded {downloaded_count}/76 real product photos.")

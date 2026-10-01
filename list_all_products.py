import sys, codecs
import re

with open('db_ban_linh_kien.sql', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

m = re.search(r'INSERT INTO `products` VALUES\s*(.*?);', content, re.DOTALL)
if m:
    val_str = m.group(1)
    cats = {1: 'CPU', 2: 'RAM', 3: 'Mainboard', 4: 'VGA', 5: 'SSD/HDD', 6: 'PSU', 7: 'Case'}
    for match in re.finditer(r'\((\d+),\s*(\d+),\s*(\d+),\s*\'([^\']+)\'', val_str):
        pid, cat_id, brand_id, name = match.groups()
        cname = cats.get(int(cat_id), 'Khác')
        print(f"ID: {int(pid):2d} | Category: {cname:10s} | Name: {name}")

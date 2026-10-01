import re
import sys

with open('db_ban_linh_kien.sql', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

m = re.search(r'INSERT INTO `products` VALUES\s*(.*?);', content, re.DOTALL)
if m:
    val_str = m.group(1)
    # Split by lines or tuples
    rows = []
    # Match tuples: (1,1,1,'Core i5-12400F',...)
    for match in re.finditer(r'\((\d+),\s*(\d+),\s*(\d+),\s*\'([^\']+)\',\s*([\d\.]+),\s*([\d\.]+),\s*(\d+),\s*([^,]+),\s*([^,]+),\s*(\d+),\s*\'([^\']*)\'', val_str):
        pid, cat_id, brand_id, name, price, cost_price, discount, sale_start, sale_end, qty, img = match.groups()
        rows.append({
            'id': int(pid),
            'cat_id': int(cat_id),
            'brand_id': int(brand_id),
            'name': name,
            'price': float(price),
            'discount': int(discount),
            'qty': int(qty),
            'image': img
        })
    print(f"Total parsed: {len(rows)}")
    for r in rows:
        print(f"ID: {r['id']:2d} | Cat: {r['cat_id']} | Img: {r['image']:25s} | Name: {r['name']}")

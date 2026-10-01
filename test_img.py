import os
from PIL import Image, ImageDraw, ImageFont

os.makedirs('src/main/resources/static/img/products', exist_ok=True)

def create_sample():
    w, h = 600, 600
    img = Image.new('RGBA', (w, h), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)
    
    # Soft background gradient or container
    draw.rounded_rectangle([20, 20, 580, 580], radius=24, fill=(248, 250, 252, 255), outline=(226, 232, 240, 255), width=2)
    
    # Draw CPU
    # Outer gold / substrate
    draw.rounded_rectangle([150, 150, 450, 450], radius=16, fill=(30, 41, 59, 255), outline=(203, 213, 225, 255), width=4)
    # Heatspreader
    draw.rounded_rectangle([180, 180, 420, 420], radius=10, fill=(241, 245, 249, 255), outline=(148, 163, 184, 255), width=3)
    
    # Brand banner
    draw.rounded_rectangle([40, 40, 140, 75], radius=8, fill=(37, 99, 235, 255))
    draw.text((55, 48), "intel", fill=(255, 255, 255, 255))
    
    img.save('src/main/resources/static/img/products/test_sample.png')
    print("Saved test_sample.png")

create_sample()

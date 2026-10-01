import urllib.request
import urllib.parse
import json
import re

def search_bing_images(query):
    try:
        url = f"https://www.bing.com/images/async?q={urllib.parse.quote(query + ' product')}&first=1&count=5&mmasync=1"
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.9',
            'Referer': 'https://www.bing.com/'
        }
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=10) as resp:
            content = resp.read().decode('utf-8', errors='ignore')
            
        # Extract murl from bing images response
        murls = re.findall(r'murl&quot;:&quot;(https?://[^&]+?)&quot;', content)
        if not murls:
            # Try raw json format
            murls = re.findall(r'"murl":"(https?://[^"]+?)"', content)
        return murls
    except Exception as e:
        print(f"Bing search error for {query}: {e}")
        return []

res = search_bing_images("Intel Core i5-12400F box")
print("Bing found images:", len(res))
for r in res[:3]:
    print("URL:", r)

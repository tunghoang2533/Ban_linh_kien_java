import urllib.request
import urllib.parse
import json

def search_wikimedia(query):
    try:
        url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(query)}&gsrlimit=3&prop=imageinfo&iiprop=url|mime&format=json"
        req = urllib.request.Request(url, headers={'User-Agent': 'HardwareImageBot/1.0 (contact@banlinhkien.com)'})
        with urllib.request.urlopen(req, timeout=10) as r:
            data = json.loads(r.read().decode('utf-8'))
            pages = data.get('query', {}).get('pages', {})
            for pid, p in pages.items():
                infos = p.get('imageinfo', [])
                if infos:
                    return infos[0].get('url')
    except Exception as e:
        print("Wikimedia error:", e)
    return None

print("Ryzen 5:", search_wikimedia("AMD Ryzen 5"))
print("Intel i5:", search_wikimedia("Intel Core i5"))
print("GeForce RTX:", search_wikimedia("GeForce RTX"))

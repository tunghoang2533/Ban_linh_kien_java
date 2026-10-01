import urllib.request
import urllib.parse
import json
import re
import time

def get_real_image_url(query):
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
        }
        
        search_query = query + " product png"
        token_url = f"https://duckduckgo.com/?q={urllib.parse.quote(search_query)}&t=h_&iar=images&iax=images&ia=images"
        req = urllib.request.Request(token_url, headers=headers)
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode('utf-8', errors='ignore')
            
        vqd_match = re.search(r'vqd=([0-9-_]+)', body) or re.search(r'vqd="([0-9-_]+)"', body) or re.search(r'vqd:\s*\'([0-9-_]+)\'', body)
        if not vqd_match:
            print(f"Could not get vqd for {query}")
            return None
            
        vqd = vqd_match.group(1)
        api_url = f"https://duckduckgo.com/i.js?q={urllib.parse.quote(search_query)}&o=json&vqd={vqd}&p=1"
        api_req = urllib.request.Request(api_url, headers=headers)
        with urllib.request.urlopen(api_req, timeout=10) as api_resp:
            data = json.loads(api_resp.read().decode('utf-8', errors='ignore'))
            results = data.get('results', [])
            if results:
                # Return first high-res or thumbnail image
                return results[0].get('image') or results[0].get('thumbnail')
    except Exception as e:
        print(f"Error fetching for {query}: {e}")
    return None

test_query = "Intel Core i5-12400F box"
url = get_real_image_url(test_query)
print("Found URL for", test_query, ":", url)

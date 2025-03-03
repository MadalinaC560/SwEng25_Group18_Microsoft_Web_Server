# test_load.py
import requests
import time
import random

def generate_load():
    urls = [
        "http://localhost:5001/",  # Main server
        "http://localhost:5001/app1",
        "http://localhost:5001/app2"
    ]
    
    while True:
        url = random.choice(urls)
        try:
            requests.get(url)
        except:
            pass
        time.sleep(random.random())  # Random delay

if __name__ == "__main__":
    generate_load()
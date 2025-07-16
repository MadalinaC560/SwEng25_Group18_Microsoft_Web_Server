import requests
import time
from tqdm import tqdm
import concurrent.futures

def make_request(i):
    try:
        response = requests.get('http://localhost:8080')
        return response.status_code == 200
    except Exception as e:
        print(f"Request {i} failed: {str(e)}")
        return False

def generate_requests(total_requests, concurrent_requests=10):
    """
    Generate requests to the counter app
    total_requests: Total number of requests to make
    concurrent_requests: Number of concurrent requests to make
    """
    successful_requests = 0
    failed_requests = 0
    
    print(f"\nStarting {total_requests} requests to http://localhost:5002")
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrent_requests) as executor:
        # Create futures for all requests
        futures = [executor.submit(make_request, i) for i in range(total_requests)]
        
        # Track progress with tqdm
        for future in tqdm(concurrent.futures.as_completed(futures), total=total_requests):
            if future.result():
                successful_requests += 1
            else:
                failed_requests += 1

    print(f"\nCompleted:")
    print(f"Successful requests: {successful_requests}")
    print(f"Failed requests: {failed_requests}")
    print(f"Total requests: {total_requests}")

if __name__ == "__main__":
    # Number of requests to make
    TOTAL_REQUESTS = 500
    
    # Number of concurrent requests
    CONCURRENT_REQUESTS = 10
    
    generate_requests(TOTAL_REQUESTS, CONCURRENT_REQUESTS)

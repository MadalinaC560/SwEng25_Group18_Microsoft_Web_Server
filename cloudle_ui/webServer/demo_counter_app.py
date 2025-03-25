from flask import Flask, render_template
from flask_cors import CORS
from datetime import datetime
import requests
import time
import threading
import atexit

app = Flask(__name__)
CORS(app)

# Metrics tracking
visit_count = 0
start_time = datetime.now()
requests_by_hour = {}
response_times = []
errors = 0

def reset_hourly_stats():
    """Reset hourly statistics at the start of each hour"""
    global requests_by_hour
    current_hour = datetime.now().strftime("%H:00")
    if current_hour not in requests_by_hour:
        requests_by_hour = {current_hour: 0}

def update_main_server():
    """Send metrics to the main server"""
    while True:
        try:
            current_hour = datetime.now().strftime("%H:00")
            avg_response_time = sum(response_times) / len(response_times) if response_times else 0
            error_rate = errors / visit_count if visit_count > 0 else 0
            
            metrics = {
                "requests24h": visit_count,
                "avgResponseTime": avg_response_time,
                "errorRate": error_rate,
                "storageUsed": 0.1,  # Mock value for storage
                "currentHour": current_hour,
                "hourlyRequests": requests_by_hour.get(current_hour, 0)
            }
            
            # Send metrics to main server
            requests.post('http://localhost:5001/api/metrics/update', json=metrics)
        except Exception as e:
            print(f"Error updating metrics: {e}")
        time.sleep(3)  # Update every ... seconds

# Start the background thread for metrics reporting
metrics_thread = threading.Thread(target=update_main_server, daemon=True)
metrics_thread.start()

@app.route('/')
def home():
    global visit_count
    start_request = time.time()
    
    try:
        visit_count += 1
        
        # Update hourly stats
        current_hour = datetime.now().strftime("%H:00")
        requests_by_hour[current_hour] = requests_by_hour.get(current_hour, 0) + 1
        
        # Calculate response time
        end_request = time.time()
        response_time = (end_request - start_request) * 1000  # Convert to milliseconds
        response_times.append(response_time)
        
        # Keep only last 100 response times for memory efficiency
        if len(response_times) > 100:
            response_times.pop(0)
            
        return f"""
        <!DOCTYPE html>
        <html>
            <head>
                <title>Sample Counter App</title>
                <style>
                    body {{
                        font-family: Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background-color: #f0f0f0;
                    }}
                    .counter-container {{
                        text-align: center;
                        padding: 2rem;
                        background-color: white;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }}
                    .count {{
                        font-size: 4rem;
                        color: #2563eb;
                        margin: 1rem 0;
                    }}
                </style>
            </head>
            <body>
                <div class="counter-container">
                    <h1>Visit Counter</h1>
                    <div class="count">{visit_count}</div>
                    <p>Total page visits</p>
                </div>
            </body>
        </html>
        """
    except Exception as e:
        global errors
        errors += 1
        raise e

def cleanup():
    """Cleanup function to be called when the server shuts down"""
    print("Shutting down metrics reporting...")

atexit.register(cleanup)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5002)
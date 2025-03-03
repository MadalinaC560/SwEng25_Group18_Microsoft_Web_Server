from flask import Flask, jsonify, request
from flask_cors import CORS
from datetime import datetime
import random
from werkzeug.utils import secure_filename
import os

app = Flask(__name__)
CORS(app)

UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'html', 'zip', 'py'}

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Store real metrics for each application
application_metrics = {}

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

# Mock database
applications = {}

@app.route('/api/applications', methods=['GET'])
def get_applications():
    """Get all applications"""
    return jsonify(applications)

@app.route('/api/applications', methods=['POST'])
def create_application():
    """Create a new application"""
    data = request.get_json()
    
    new_id = max(applications.keys()) + 1 if applications else 1
    
    # Create a new application with the provided data
    app_url = "http://localhost:5002"
    
    applications[new_id] = {
        "id": new_id,
        "name": data.get('name', f"App{new_id}"),
        "status": "stopped",
        "url": app_url,  # Use the sample app URL
        "runtime": data.get('runtime', "Python 3.9"),
        "language": data.get('runtime', 'python').upper(),
        "environment": 'Development',
        "ssl": True,
        "sslStatus": "Active",
        "autoScaling": "Enabled",
        "version": "v1.0.0",
        "lastDeployment": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    }
    
    return jsonify(applications[new_id]), 201

@app.route('/api/applications/<int:app_id>/deploy', methods=['POST'])
def deploy_application(app_id):
    """Deploy application files"""
    if 'file' not in request.files:
        return jsonify({"error": "No file provided"}), 400
        
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No file selected"}), 400
        
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        return jsonify({"message": "File uploaded successfully"}), 200
    
    return jsonify({"error": "Invalid file type"}), 400

@app.route('/api/applications/<int:app_id>', methods=['GET'])
def get_application(app_id):
    """Get application details"""
    if app_id not in applications:
        return jsonify({"error": "Application not found"}), 404
    
    return jsonify(applications[app_id])

@app.route('/api/metrics/update', methods=['POST'])
def update_metrics():
    """Receive metrics updates from deployed applications"""
    data = request.get_json()
    app_id = 1  # For demo, we'll use ID 1 for the counter app
    
    if app_id not in application_metrics:
        application_metrics[app_id] = {
            'requests24h': 0,
            'avgResponseTime': 0,
            'errorRate': 0,
            'storageUsed': 0,
            'performanceData': []
        }
    
    # Update the metrics
    metrics = application_metrics[app_id]
    metrics['requests24h'] = data['requests24h']
    metrics['avgResponseTime'] = data['avgResponseTime']
    metrics['errorRate'] = data['errorRate']
    metrics['storageUsed'] = data['storageUsed']
    
    # Update performance data for the current hour
    current_hour = data['currentHour']
    hourly_requests = data['hourlyRequests']
    
    # Find and update or add the current hour's data
    hour_updated = False
    for perf_data in metrics['performanceData']:
        if perf_data['time'] == current_hour:
            perf_data['requests'] = hourly_requests
            perf_data['responseTime'] = data['avgResponseTime']
            perf_data['errors'] = int(hourly_requests * data['errorRate'])
            hour_updated = True
            break
    
    if not hour_updated:
        metrics['performanceData'].append({
            'time': current_hour,
            'requests': hourly_requests,
            'responseTime': data['avgResponseTime'],
            'errors': int(hourly_requests * data['errorRate'])
        })
    
    # Keep only last 24 hours of data
    if len(metrics['performanceData']) > 24:
        metrics['performanceData'] = metrics['performanceData'][-24:]
    
    return jsonify({"status": "success"}), 200



@app.route('/api/applications/<int:app_id>/status', methods=['PUT'])
def update_status(app_id):
    """Update application status"""
    if app_id not in applications:
        return jsonify({"error": "Application not found"}), 404
    
    data = request.get_json()
    new_status = data.get('status')
    
    if new_status not in ['running', 'stopped']:
        return jsonify({"error": "Invalid status"}), 400
    
    applications[app_id]['status'] = new_status
    return jsonify({"message": "Status updated successfully"})

# Keep the existing metrics endpoints the same
def generate_performance_data():
    """Generate random performance data for the last 24 hours"""
    data = []
    for i in range(24):
        time = f"{i:02d}:00"
        data.append({
            "time": time,
            "responseTime": random.uniform(50, 200),
            "requests": random.randint(100, 1000),
            "errors": random.randint(0, 20)
        })
    return data

@app.route('/api/applications/<int:app_id>/metrics', methods=['GET'])
def get_metrics(app_id):
    """Get application metrics"""
    if app_id not in applications:
        return jsonify({"error": "Application not found"}), 404
    
    # Check if we have real metrics for this app
    if app_id in application_metrics:
        return jsonify(application_metrics[app_id])
    
    # If no real metrics, generate mock data as before
    app = applications[app_id]
    creation_time = datetime.strptime(app['lastDeployment'], "%Y-%m-%d %H:%M:%S")
    is_new_app = (datetime.now() - creation_time).total_seconds() < 300
    
    if is_new_app or app['status'] == 'stopped':
        return jsonify({
            "requests24h": 0,
            "avgResponseTime": 0,
            "errorRate": 0,
            "storageUsed": 0,
            "performanceData": [
                {
                    "time": f"{i:02d}:00",
                    "responseTime": 0,
                    "requests": 0,
                    "errors": 0
                } for i in range(24)
            ]
        })
    
    # Generate mock data for apps without real metrics
    perf_data = generate_performance_data()
    return jsonify({
        "requests24h": sum(d["requests"] for d in perf_data),
        "avgResponseTime": round(sum(d["responseTime"] for d in perf_data) / len(perf_data), 2),
        "errorRate": round(sum(d["errors"] for d in perf_data) / sum(d["requests"] for d in perf_data), 4),
        "storageUsed": round(random.uniform(1, 10), 2),
        "performanceData": perf_data
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
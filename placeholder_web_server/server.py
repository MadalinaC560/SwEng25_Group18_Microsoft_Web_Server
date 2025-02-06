from flask import Flask, request, send_from_directory
import os

app = Flask(__name__)
UPLOAD_FOLDER = 'uploads'

# Ensure upload folder exists
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

# Upload files
@app.route('/upload', methods=['POST'])
def upload_files():
    if 'files' not in request.files:
        return "No files uploaded", 400
    
    files = request.files.getlist('files')
    for file in files:
        if file.filename == '':
            continue
        file.save(os.path.join(UPLOAD_FOLDER, file.filename))
    
    return "File uploaded successfully", 201

# Serve uploaded files
@app.route('/uploads/<filename>')
def serve_file(filename):
    return send_from_directory(UPLOAD_FOLDER, filename)

if __name__ == '__main__':
    app.run(debug=True)

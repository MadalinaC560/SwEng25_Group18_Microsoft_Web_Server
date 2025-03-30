from flask import Flask, request, jsonify
from dotenv import load_dotenv
import subprocess
import tempfile
import os

load_dotenv()
API_KEY = os.getenv("API_KEY")

app = Flask(__name__)


@app.before_request
def check_api_key():
    if request.endpoint == 'run_php':
        key = request.headers.get("X-API-Key")
        if key != API_KEY:
            return jsonify({
                "output": "",
                "error": "Unauthorised Access",
                "exit_code": 2
            }), 401


@app.route('/run-php', methods=['POST'])
def run_php():

    code = request.json.get("code", "").strip()
    if not code or code.strip() in ["<?php ?>", "<?php", "<?php\n?>"]:
        return jsonify({
            "output": "",
            "error": "Missing PHP code",
            "exit_code": 1
        }), 400

    with tempfile.NamedTemporaryFile(suffix=".php", delete=False) as tmp:
        tmp.write(code.encode('utf-8'))
        tmp_path = tmp.name

    try:
        result = subprocess.run(["php", "-c", "/path/to/php/ini", tmp_path], capture_output=True, text=True, timeout=10)
        output = result.stdout
        error = result.stderr
        exit_code = result.returncode
    except subprocess.TimeoutExpired:
        output = ""
        error = "PHP script timed out after 10 seconds"
        exit_code = 124
    finally:
        os.remove(tmp_path)

    return jsonify({
        "output": output,
        "error": error,
        "exit_code": exit_code
    })

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)



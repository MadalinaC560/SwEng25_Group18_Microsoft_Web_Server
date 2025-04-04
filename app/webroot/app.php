<?php
// Array of inspirational quotes
$quotes = [
    "'Believe you can and you're halfway there.' - Theodore Roosevelt",
    "'The only way to do great work is to love what you do.' - Steve Jobs",
    "'Success is not final, failure is not fatal: It is the courage to continue that counts.' - Winston Churchill",
    "'Don't watch the clock; do what it does. Keep going.' - Sam Levenson",
    "'Keep your face always toward the sunshine-and shadows will fall behind you.' - Walt Whitman"
];

// Pick a random quote
$quote = $quotes[array_rand($quotes)];

// Get current time
$time = date("l, F j, Y - H:i:s");

// Get system info
$phpVersion = phpversion();
$serverOS = PHP_OS;

// Output with some simple styling
echo <<<HTML
<!DOCTYPE html>
<html>
<head>
    <title>PHP Test</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; color: #333; text-align: center; padding: 50px; }
        h1 { color: #007BFF; }
        .quote { font-style: italic; margin: 20px 0; color: #555; }
        .info-box { background: #fff; border-radius: 10px; padding: 20px; display: inline-block; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .label { font-weight: bold; }
    </style>
</head>
<body>
    <h1>Hello from PHP!</h1>
    <div class="quote">$quote</div>
    <div class="info-box">
        <p><span class="label">Current Server Time:</span> $time</p>
        <p><span class="label">PHP Version:</span> $phpVersion</p>
        <p><span class="label">Server OS:</span> $serverOS</p>
    </div>
</body>
</html>
HTML;
?>

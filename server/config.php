<?php
$DB_HOST = getenv('SMS_DB_HOST') ?: '127.0.0.1';
$DB_NAME = getenv('SMS_DB_NAME') ?: 'smsdb';
$DB_USER = getenv('SMS_DB_USER') ?: 'root';
$DB_PASS = getenv('SMS_DB_PASS') ?: '';
$UPLOAD_TOKEN = getenv('SMS_UPLOAD_TOKEN') ?: '';

function db() {
    global $DB_HOST, $DB_NAME, $DB_USER, $DB_PASS;
    static $pdo;
    if ($pdo) return $pdo;
    $dsn = "mysql:host=$DB_HOST;dbname=$DB_NAME;charset=utf8mb4";
    $pdo = new PDO($dsn, $DB_USER, $DB_PASS, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ]);
    return $pdo;
}

function require_token() {
    global $UPLOAD_TOKEN;
    if ($UPLOAD_TOKEN === '') return; // no auth
    $hdr = isset($_SERVER['HTTP_AUTHORIZATION']) ? $_SERVER['HTTP_AUTHORIZATION'] : '';
    if (strpos($hdr, 'Bearer ') !== 0) {
        http_response_code(401);
        echo json_encode(['ok'=>false,'error'=>'missing token']);
        exit;
    }
    $token = substr($hdr, 7);
    if ($token !== $UPLOAD_TOKEN) {
        http_response_code(403);
        echo json_encode(['ok'=>false,'error'=>'invalid token']);
        exit;
    }
}

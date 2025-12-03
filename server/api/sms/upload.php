<?php
header('Content-Type: application/json');
require_once __DIR__ . '/../../config.php';
require_token();

$raw = file_get_contents('php://input');
$data = json_decode($raw, true);
if (!$data) {
  http_response_code(400);
  echo json_encode(['ok'=>false,'error'=>'invalid json']);
  exit;
}

function str_or_empty($v) { return is_string($v) ? $v : ''; }
function int_or_zero($v) { return is_numeric($v) ? intval($v) : 0; }

$phone = str_or_empty($data['phone'] ?? '');
$address = str_or_empty($data['address'] ?? '');
$body = str_or_empty($data['body'] ?? '');
$type = int_or_zero($data['type'] ?? 0);
$date_ts = intval($data['date_ts'] ?? 0);

if ($phone === '' || $body === '' || $date_ts <= 0) {
  http_response_code(422);
  echo json_encode(['ok'=>false,'error'=>'missing fields']);
  exit;
}

if (strlen($body) > 2048) {
  $body = substr($body, 0, 2048);
}

$pdo = db();
$stmt = $pdo->prepare('INSERT INTO messages (phone_number,address,body,type,date_ts) VALUES (?,?,?,?,?)');
$stmt->execute([$phone, $address, $body, $type, $date_ts]);
echo json_encode(['ok'=>true]);

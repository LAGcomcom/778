<?php
require_once __DIR__ . '/config.php';
$pdo = db();
header('Content-Type: application/json');
echo json_encode(['ok'=>true]);

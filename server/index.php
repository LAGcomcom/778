<?php
require_once __DIR__ . '/config.php';
$pdo = db();
try {
  $rows = $pdo->query('SELECT phone_number, COUNT(*) AS cnt, MAX(date_ts) AS latest FROM messages GROUP BY phone_number ORDER BY latest DESC')->fetchAll();
} catch (Throwable $e) {
  header('Content-Type: text/plain; charset=utf-8');
  echo '数据库未初始化或查询失败';
  exit;
}
?><!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>短信分组</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <h2>手机号分组</h2>
  <div class="grid">
    <?php foreach ($rows as $r): ?>
      <div class="card">
        <div class="title"><?php echo htmlspecialchars($r['phone_number']); ?></div>
        <div class="sub">短信数量：<?php echo intval($r['cnt']); ?></div>
        <div class="sub">最新时间：<?php echo date('Y-m-d H:i:s', intval($r['latest']/1000)); ?></div>
        <div style="margin-top:8px">
          <a href="/messages.php?phone=<?php echo urlencode($r['phone_number']); ?>">查看短信</a>
        </div>
      </div>
    <?php endforeach; ?>
  </div>
</body>
</html>

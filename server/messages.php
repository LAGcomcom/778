<?php
require_once __DIR__ . '/config.php';
$pdo = db();
$phone = isset($_GET['phone']) ? $_GET['phone'] : '';
$q = isset($_GET['q']) ? $_GET['q'] : '';
$page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
$size = isset($_GET['size']) ? min(100, max(10, intval($_GET['size']))) : 20;
$offset = ($page-1)*$size;

if ($phone === '') {
  http_response_code(400);
  echo 'missing phone';
  exit;
}

try {
  if ($q !== '') {
    $stmt = $pdo->prepare('SELECT address, body, type, date_ts FROM messages WHERE phone_number=? AND body LIKE ? ORDER BY date_ts DESC LIMIT ? OFFSET ?');
    $stmt->execute([$phone, '%'.$q.'%', $size, $offset]);
  } else {
    $stmt = $pdo->prepare('SELECT address, body, type, date_ts FROM messages WHERE phone_number=? ORDER BY date_ts DESC LIMIT ? OFFSET ?');
    $stmt->execute([$phone, $size, $offset]);
  }
  $rows = $stmt->fetchAll();
} catch (Throwable $e) {
  header('Content-Type: text/plain; charset=utf-8');
  echo '数据库未初始化或查询失败';
  exit;
}
?><!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title><?php echo htmlspecialchars($phone); ?> 的短信</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div class="nav"><a href="/index.php">返回</a></div>
  <h2><?php echo htmlspecialchars($phone); ?> 的短信</h2>
  <form method="get" class="search">
    <input type="hidden" name="phone" value="<?php echo htmlspecialchars($phone); ?>" />
    <input name="q" value="<?php echo htmlspecialchars($q); ?>" placeholder="搜索内容" />
    <button type="submit">搜索</button>
  </form>
  <div class="list">
    <?php foreach ($rows as $r): ?>
      <div class="item">
        <div class="meta">时间：<?php echo date('Y-m-d H:i:s', intval($r['date_ts']/1000)); ?> | 地址：<?php echo htmlspecialchars($r['address']); ?></div>
        <div class="body">内容：<?php echo nl2br(htmlspecialchars($r['body'])); ?></div>
      </div>
    <?php endforeach; ?>
  </div>
  <div style="margin-top:12px;">
    <a href="/messages.php?phone=<?php echo urlencode($phone); ?>&page=<?php echo max(1,$page-1); ?>&size=<?php echo $size; ?>&q=<?php echo urlencode($q); ?>">上一页</a>
    |
    <a href="/messages.php?phone=<?php echo urlencode($phone); ?>&page=<?php echo ($page+1); ?>&size=<?php echo $size; ?>&q=<?php echo urlencode($q); ?>">下一页</a>
  </div>
</body>
</html>

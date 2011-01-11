<?php 
$GoTo="digi-e/login.php";// 如果这里的目标链接取自数据库就实现了动态转向 
header(sprintf("Location: %s", $GoTo)); 
?> 

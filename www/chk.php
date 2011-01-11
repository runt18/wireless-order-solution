<?php
header('content-Type=text/html;charset=utf-8');//表头定义utf8
$name = $_REQUEST['username'];
$password = $_REQUEST['password'];
$code = strtolower($_REQUEST['code']);
session_start();
$code1=strtolower($_SESSION['code']);
if ($code != $code1)
{
	echo '输入的验证码不符,';	
	echo '<a href="login.php">请重新登录!</a>';
	
} 
else
{
	include("conn.php"); 

	//	if (!$con)
	//  	{
	// 		die('Could not connect: ' . mysql_error());
	//	  }

	//	mysql_select_db($database, $con);
	mysql_query("SET NAMES utf8"); //解决MSQL乱码
	// mysql_close($con);
	
	$sql = "SELECT * FROM restaurant where account='" .$name . "' and pwd='" .md5($password) ."'" ; 
	$rs = $db->GetRow($sql);
	/* 检查密码是否正确 */
	if ($rs)
	{
		$_SESSION["restaurant_id"] = $rs["id"];
		$_SESSION["restaurant_name"] = $rs["restaurant_name"];
		$_SESSION["total_income"] = $rs["total_income"];				
		if($rs["id"] > 10)
		{
			header('Location: main.php');
		}
		else
		{
			$sql = "SELECT restaurant_info FROM restaurant where id = 1";  
			$rs = $db ->GetOne($sql);
			$_SESSION["root_restaurant_info"] = $rs;
			$_SESSION["password"] = $password;
			header('Location: admin_main.php');
		}
	} 
	else
	{
		echo '账号或密码错误,';	
		echo '<a href="login.php">请重新登录!</a>';
	}


	
}
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>无标题文档</title>
<style type="text/css">
a:link {
	text-decoration: none;
}
a:visited {
	text-decoration: none;
}
a:hover {
	text-decoration: none;
}
a:active {
	text-decoration: none;
}
</style>
</head>

<body>
</body>
</html>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>e点通－会员中心</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="css/general.css" rel="stylesheet" type="text/css" />
 
 
<style type="text/css"> 
#header-div {
  background: #278296;
  border-bottom: 1px solid #FFF;
}
 
#logo-div {
	height: 25px;
	width: 200px;
	float: left;
	text-align: center;
	margin-top: 10px;
}
#logo-div     a,#logo-div a:visited,  #logo-div a:link {
	color: #FFFFFF;
	font-family: "微软雅黑";
	font-size: 24px;
	text-align: center;
}
#logo-div  a:hover {
	text-decoration: none;
	color: #FFFFFF;
	font-size: 24px;
}
 
#license-div {
  height: 50px;
  float: left;
  text-align:center;
  vertical-align:middle;
  line-height:50px;
}
 
#license-div a:visited, #license-div a:link {
  color: #EB8A3D;
}
 
#license-div a:hover {
  text-decoration: none;
  color: #EB8A3D;
}
 
#submenu-div {
  height: 50px;
}
 
#submenu-div ul {
  margin: 0;
  padding: 0;
  list-style-type: none;
}
 
#submenu-div li {
  float: right;
  padding: 0 10px;
  margin: 3px 0;
  border-left: 1px solid #FFF;
}
 
#submenu-div a:visited, #submenu-div a:link {
  color: #FFF;
  text-decoration: none;
}
 
#submenu-div a:hover {
  color: #F5C29A;
}
 
#loading-div {
  clear: right;
  text-align: right;
  display: block;
}
 
#menu-div {
	font-weight: bold;
	height: 24px;
	line-height:24px;
	background-color: #7cb4c3;
}
 
#menu-div ul {
  margin: 0;
  padding: 0;
  list-style-type: none;
}
 
#menu-div li {
  float: left;
  border-right: 1px solid #192E32;
  border-left:1px solid #BBDDE5;
}
 
#menu-div a:visited, #menu-div a:link {
	display:block;
	padding: 0 20px;
	text-decoration: none;
	color: #2a7d8d;
	background:#80c1cc;
}
 
#menu-div a:hover {
	color: #FFF;
	background:#619caa;
}
 
#submenu-div a.fix-submenu{
	clear:both;
	margin-left:5px;
*padding:3px 5px 5px; 						background:#DDEEF2;
	color:#278296;
	padding-top: 2px;
	padding-right: 10px;
	padding-bottom: 2px;
	padding-left: 10px;
}
#submenu-div a.fix-submenu:hover{*padding:3px 5px 5px;
	background:#FFF;
	color:#278296;
	clear: both;
	padding-top: 2px;
	padding-right: 10px;
	padding-bottom: 2px;
	padding-left: 10px;
}
#menu-div li.fix-spacel{width:30px; border-left:none;}
#menu-div li.fix-spacer{border-right:none;}
</style>

</head>
<body>
<div id="header-div">
  <div id="logo-div" style="bgcolor:#000000;"><a href="#"><?php session_start(); echo $_SESSION["restaurant_name"] ?></a></div>
  <div id="license-div" style="bgcolor:#000000;"></div>
  <div id="submenu-div">
 
    <div id="send_info" style="padding: 20px 50px 0 0; clear:right; text-align: right; color: #FF9900; width:40%; float: right;">
	  <a href="../pserver/pserver.exe" target="_top" class="fix-submenu">下载e点通打印程序</a>    
	  <a href="#" onclick="javascript:window.parent.frames(1).changePassword('','')" class="fix-submenu">修改密码</a>    
      <a href="help.html" target="_blank" class="fix-submenu">帮助</a>    
      <a href="login.php" target="_top" class="fix-submenu">退出</a>    
    </div>
  </div>
</div>
<div id="menu-div">
  <ul>
    <li class="fix-spacel">&nbsp;&nbsp;&nbsp;&nbsp;</li>
    <li><a href="food.php" target="main-frame">菜谱管理</a></li>
	<li><a href="taste.php" target="main-frame">口味管理</a></li>
	<li><a href="kitchen.php" target="main-frame">分厨管理</a></li>
    <li><a href="order.php" target="main-frame">当日帐单</a></li>	
	<li><a href="order_history.php" target="main-frame">历史帐单</a></li>
	<li><a href="material.php" target="main-frame">食材管理</a></li>
	<li><a href="member.php" target="main-frame">会员管理</a></li>
    <li><a href="terminal.php" target="main-frame">终端信息</a></li>
    <li><a href="table.php" target="main-frame">餐台信息</a></li>
    <li class="fix-spacer">&nbsp;</li>
  </ul>
  <br class="clear" />
</div>
</body>
</html>

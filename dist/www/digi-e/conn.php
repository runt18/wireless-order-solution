<?php

$database="wireless_order_db";
$sa="custom_user";
$pwd="custom_user@digi-e";
$ip="127.0.0.1";
$con = mysql_connect($ip,$sa,$pwd);

Include_once("libs/adodb/adodb.inc.php");

$db = NewADOConnection("mysql");
$db->Connect($ip, $sa, $pwd, $database) or die("Unable to connect");

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php
include("hasLogin.php"); 
?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>e点通－会员中心</title>
<link rel="stylesheet" type="text/css" href="css/jquery-ui-1.8.5.custom.css"/>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/general.css" />
<link rel="stylesheet" href="css/style.css" />
<link rel="stylesheet" href="css/pop_up.css" />
<script type="text/javascript" src="js/pop-up.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.5.custom.min.js"></script>
<style type="text/css">
h3 {
	font-size:14px;
	line-height:30px;
}	
</style>
<script language="javascript" type="text/javascript" src="js/order.js"></script>
<script language="javascript" type="text/javascript" src="js/table.js"></script>

</head>
<body>
<?php
include("changePassword.php"); 
?>
<?php
include("conn.php"); 
$addId = $_POST["tableCode"];
$restaurant_id = $_SESSION["restaurant_id"];
if($addId != null && $addId != "")
{
	$id = $restaurant_id;
	for($i=0;$i < 32;$i++)
	{
		$id *= 2;
	}
	$tableId = $id + $addId;
	$sql = "SELECT * FROM `table` WHERE enabled =1 AND id = $tableId";
	$rs = $db ->GetOne($sql);
	if($rs)
	{
		echo "<script>alert('餐桌已存在！');addTable('$addId')</script>";
	}
	else
	{
		$sql = "SELECT id FROM `table` WHERE enabled =0 AND id = $tableId";
		$rs = $db ->GetOne($sql);
		if($rs)
		{
			$sql = "UPDATE `table` SET enabled=1 WHERE id=$rs";
			if($db->Execute($sql))
			{
				echo "<script>alert('保存成功！');</script>";
			}	
			else{
				echo "<script>alert('保存失败！');</script>";
			}
		}		
		else
		{
			$sql = "INSERT INTO `table` VALUES($tableId,$addId,$restaurant_id,1)";	
			if($db->Execute($sql))
			{
				echo "<script>alert('保存成功！');</script>";
			}
			else{
				echo "<script>alert('保存失败！');</script>";
			}
		}
	}
}
$deleteId = $_POST["deleteId"];
if($deleteId != null && $deleteId != "")
{
	/*$sql = "DELETE FROM `order_food` WHERE order_id IN (SELECT id FROM `order` WHERE table_id=(SELECT id FROM `table` WHERE alias_id =$deleteId))";
	$db->Execute($sql);
	$sql = "DELETE FROM `order` WHERE table_id=(SELECT id FROM `table` WHERE alias_id =$deleteId)";
	$db->Execute($sql);
	$sql = "DELETE FROM `table` WHERE alias_id=$deleteId";
	if($db->Execute($sql))
	{
		echo "<script>alert('删除成功！');</script>";
	}	
	else{
		echo "<script>alert('删除失败！');</script>";
	}*/
	
	$sql = "SELECT id FROM `table` WHERE enabled=1 AND alias_id=$deleteId AND restaurant_id=$restaurant_id";	
	$rs = $db ->GetOne($sql);
	if($rs)
	{
		$sql = "UPDATE `table` SET enabled=0 WHERE id=$rs";	
		if($db->Execute($sql))
		{
			echo "<script>alert('删除成功！');</script>";
		}	
		else{
			echo "<script>alert('删除失败！');</script>";
		}
	}
	else
	{
		echo "<script>alert('没有对应的餐桌信息！');showMenu('$deleteId')</script>";
	}
}
?>
<h1><span class="action-span"><a href="#" onclick="addTable('');">添加餐桌</a></span>  <span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 餐台信息</span>&nbsp;&nbsp;&nbsp;&nbsp;
    <img alt="" title="总数" src="images/table-total.png" height="25" width="25" /><span id="total_Table"  title="总数" style="color: #6666FF; font-weight: bold">16</span>&nbsp;&nbsp;
    <img alt=""  title="就餐" src="images/table-used.png" height="25" width="25" /><span id="used_Table"  title="就餐" style="color: #6666FF; font-weight: bold">16</span>&nbsp;&nbsp;
    <img alt=""  title="空闲" src="images/table-idle.png" height="25" width="25" /><span id="idle_Table"  title="空闲" style="color: #6666FF; font-weight: bold">16</span>

  <div style="clear:both"></div>
</h1>
<div style="margin:0 auto; width:1000px;">
  <div class="side_btn" id="left_btn"></div>
  <div id="wrap">
    <div id="list">
	<?php
	/*include("conn.php"); */
	mysql_query("SET NAMES utf8"); 
	$sql = "SELECT od.id,t.alias_id,od.order_date,total_price,num,foods,is_paid,waiter FROM `table` t LEFT OUTER JOIN 

(SELECT MAX(id) AS OrderId,table_id ".
		" FROM `order` GROUP BY `order`.table_id) AS o ON t.id = o.table_id".
		" LEFT OUTER JOIN `order_view` od ON o.OrderId = od.id WHERE t.enabled=1 AND t.restaurant_id=" . $_SESSION["restaurant_id"] ;
	/*echo $sql;*/
	$rs = $db->Execute($sql);
	$ts = 0;
	$content = '';
	$roomNo = 1;	
	$isPaidNum = 0;
	foreach ($rs as $row){	
		
		if($ts % 24 == 0)
		{
			$content.='<div class="item"><h3>'.$roomNo.'号厅</h3><ul class="table_list">';
			$roomNo++;
		}
		$is_paid = $row["is_paid"];
		$on = "";
		
		if($is_paid == null || $is_paid != 0)	
		{
			$action = ' onclick="showMenu(&quot;' .$row["alias_id"].'&quot;)"';
			$isPaidNum++;			
		}
		else
		{			
			$on = "class='on'";
			$action = ' onclick="showOrderDetail(&quot;'.$row["id"].'&quot;,&quot;'.$row["alias_id"].'&quot;,&quot;'.$row["order_date"].'&quot;,&quot;'.$row["total_price"].'&quot;,&quot;'.$row["num"].'&quot;,&quot;'.$row["foods"].'&quot;,&quot;'.$row["is_paid"].'&quot;,&quot;'.$row["waiter"].'&quot;)"';
		}
		$content .= ('<li  '.$on. $action . '>' . $row["alias_id"] . '</li>');					
		if($ts % 24 == 23)
		{
			$content .= '</ul></div>';
		}
		$ts++;
	} 	
	if(!($ts % 24 == 0))
	{
		$content .= '</ul></div>';
	}
	echo $content;
	mysql_close($con);
	echo "<script>statTable($ts,$isPaidNum)</script>";
	?>
    </div>
  </div>
  <div class="side_btn" id="right_btn"></div>
</div>
</body>
</html>
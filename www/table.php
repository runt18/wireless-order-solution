<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php
include("hasLogin.php"); 
?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="refresh" content="60">
<title>e点通－会员中心</title>
<link rel="stylesheet" type="text/css" href="css/jquery-ui-1.8.5.custom.css"/>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/general.css" />
<link rel="stylesheet" href="css/style.css" />
<link rel="stylesheet" href="css/pop_up.css" />
<style type="text/css">
h3 {
	font-size:14px;
	line-height:30px;
}	
</style>
<script type="text/javascript" src="js/pop-up.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.5.custom.min.js"></script>
<script language="javascript" type="text/javascript" src="js/order.js"></script>
<script language="javascript" type="text/javascript" src="js/table.js"></script>
<script type="text/javascript">
var left;
$(function(){
    left = parseInt($("#list").css("left"));
    $(".side_btn").click(function(){
        move(this.id);
    });
    
});

</script>


</head>
<body>
<?php
include("changePassword.php"); 
?>
<?php
include("conn.php"); 
$name = $_POST["name"];
$editType = $_POST["editType"];
$restaurant_id = $_SESSION["restaurant_id"];
if($editType == "addTable")
{
	$alias_id = $_POST["alias_id"];	
	$sql = "SELECT * FROM `table` WHERE alias_id = $alias_id AND restaurant_id=$restaurant_id";
	$rs = $db ->GetOne($sql);
	if($rs)
	{
		echo "<script>alert('餐桌已存在！');editTable('','$alias_id','$name','table.php')</script>";
	}
	else
	{		
		$sql = "INSERT INTO `table`(alias_id,restaurant_id,name) VALUES($alias_id,$restaurant_id,'$name')";	
		if($db->Execute($sql))
		{
			echo "<script>alert('保存成功！');</script>";
		}
		else{
			echo "<script>alert('保存失败！');</script>";
		}		
	}
}
else if($editType == "editTable")
	{
		$id = $_POST["id"];
		$sql = "UPDATE `table` SET name='$name' WHERE id=$id";
		if($db->Execute($sql))
		{
			echo "<script>alert('保存成功！');</script>";
		}	
		else{
			echo "<script>alert('保存失败！');</script>";
		}
	}
if($editType == "deleteTable")
{		
	$deleteId = $_POST["deleteId"];
	$sql = "SELECT id FROM `table` WHERE alias_id=$deleteId AND restaurant_id=$restaurant_id";	
	$rs = $db ->GetOne($sql);
	if($rs)
	{
		$sql = "DELETE FROM `table` WHERE id=$rs";	
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
		echo "<script>alert('没有对应的餐桌信息！');</script>";
	}
}
?>
<h1>
<span class="action-span"><a href="#" onclick="editTable('','','','table.php','')">添加餐桌</a></span> 
<span class="action-span"><a href="#" onclick="javascript:window.location.href = 'table_list.php'">列表显示</a></span>  
<span class="action-span1">e点通会员中心</span>
<span id="search_id" class="action-span2">&nbsp;- 餐台信息</span>&nbsp;&nbsp;&nbsp;&nbsp;
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
$sql = "SELECT od.id,t.alias_id,od.order_date,total_price,num,foods,is_paid,waiter,od.comment,od.service_rate,od.table_name FROM `table` t LEFT OUTER JOIN 
		
		(SELECT MAX(id) AS OrderId,table_id,MAX(restaurant_id) AS restaurant_id".
	" FROM `order` WHERE `order`.`restaurant_id`=".$_SESSION["restaurant_id"]." GROUP BY `order`.table_id) AS o ON t.alias_id = o.table_id ".
	" LEFT OUTER JOIN `order_view` od ON o.OrderId = od.id WHERE t.restaurant_id=" . $_SESSION["restaurant_id"]. " ORDER BY t.alias_id" ;
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
	
	if($is_paid == NULL || $is_paid == 1)	
	{
		$action = ' onclick="showMenu(&quot;' .$row["alias_id"].'&quot;)"';
		$isPaidNum++;			
	}
	else
	{			
		$on = "class='on'";
		$action = ' onclick="showOrderDetail(&quot;'.$row["id"].'&quot;,&quot;'.$row["alias_id"].'&quot;,&quot;'.$row["order_date"].'&quot;,&quot;'.$row["total_price"].'&quot;,&quot;'.$row["num"].'&quot;,&quot;'.$row["foods"].'&quot;,&quot;'.$row["is_paid"].'&quot;,&quot;'.$row["waiter"].'&quot;,&quot;'.$row["type_name"]."&quot;,&quot;".$row["totalPrice_2"].'&quot;,&quot;'.$row["category_name"].'&quot;,&quot;'.$row["comment"].'&quot;,'.$row["service_rate"].',&quot;'.$row["table_name"].'&quot;)"';
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
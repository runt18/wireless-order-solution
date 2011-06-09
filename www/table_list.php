<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php
include("hasLogin.php"); 
?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>tbt</title>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/general.css" />
<link rel="stylesheet" href="css/style.css" />
<link rel="stylesheet" href="css/pop_up.css" />
<script type="text/javascript" src="js/pop-up.js"></script>
<script type="text/javascript" src="js/table.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script language="javascript" type="text/javascript" src="js/order.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$name = $_POST["name"];
$editType = $_POST["editType"];
$restaurant_id = $_SESSION["restaurant_id"];
if($editType == "addTable")
{
	$alias_id = $_POST["alias_id"];
	$id = $restaurant_id;
	for($i=0;$i < 32;$i++)
	{
		$id *= 2;
	}
	$tableId = $id + $alias_id;
	$sql = "SELECT * FROM `table` WHERE id = $tableId";
	$rs = $db ->GetOne($sql);
	if($rs)
	{
		echo "<script>alert('餐桌已存在！');editTable('','$alias_id','$name','table_list.php')</script>";
	}
	else
	{		
		$sql = "INSERT INTO `table`(id,alias_id,restaurant_id,enabled,name) VALUES($tableId,$alias_id,$restaurant_id,'$name')";	
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
//echo $sql;
?>
<h1>
<span class="action-span"><a href="#" onclick="editTable('','','','table_list.php');">添加餐桌</a></span>
<span class="action-span"><a href="#" onclick="javascript:window.location.href = 'table.php'">图标显示</a></span>  
<span class="action-span1">e点通会员中心</span>
<span id="search_id" class="action-span2">&nbsp;- 餐台信息 </span>
<div style="clear:both"></div></h1>
<div class="form-div">
  <form action="table_list.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideTableCondition(this)">
	<option value="0">全部</option>
	<option value="alias_id">编号</option>
	<option value="name">名称</option>
	<option value="status">状态</option>
	</select>
	<select id="condition_type" name="condition_type" style="display:none">
		<option value="1">空闲</option>
		<option value="0" selected="selected">就餐</option>
	</select>
	
    <input type="text" id="keyword" name="keyword"   />
    <input type="submit" value=" 搜索 " class="button" />
  </form>
</div>
<div class="Content">


        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>编&nbsp;号</h3></th>
				<th><h3>名&nbsp;称</h3></th>
				<th><h3>状&nbsp;态</h3></th>				
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 				
$xm=$_REQUEST["keyword_type"];
$ct=$_REQUEST["condition_type"];
$kw=$_REQUEST["keyword"]; 
$sql = "SELECT od.id,t.id AS table_id,t.alias_id,t.name,CASE is_paid WHEN 0 THEN '就餐' ELSE '空闲' END AS status,od.order_date,total_price,num,foods,is_paid,waiter,od.category FROM `table` t LEFT OUTER JOIN 
		
		(SELECT MAX(id) AS OrderId,table_id,MAX(restaurant_id) AS restaurant_id".
	" FROM `order` WHERE `order`.`restaurant_id`=".$_SESSION["restaurant_id"]." GROUP BY `order`.table_id) AS o ON t.alias_id = o.table_id ".
	" LEFT OUTER JOIN `order_view` od ON o.OrderId = od.id WHERE t.restaurant_id=" . $_SESSION["restaurant_id"] ;			
switch ($xm)
{
	case "alias_id":
		if ($kw!="")
			$sql .= " AND t.alias_id = $kw" ;  			
		break;
	case "name":
		if ($kw!="")
			$sql .= " AND t.name like '%$kw%'" ;  			
		break;
	case "status":		
		if($ct == "1")
		{
			$sql .= " AND (is_paid IS NULL || is_paid = 1)" ;  
		}
		elseif($ct == "0")
		{
			$sql .= " AND is_paid = 0" ; 
		}							
		break;		
}			
$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
$rs = $db->GetAll($sql);
foreach ($rs as $row){	
	$bh=$bh+1;
	$table_name = "-";
	if($row["name"] != null)
	{
		$table_name = $row["name"];
	}
	echo "<tr>";
	echo "<td>" .$row["alias_id"] ."</td>";
	echo "<td>" .$table_name ."</td>";
	echo "<td>" .$row["status"] ."</td>";
	$is_paid = $row["is_paid"];
	if($is_paid == NULL || $is_paid == 1)	
	{
		echo "<td><a href='#' onclick='editTable(&quot;".$row["table_id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$table_name.
			"&quot;,&quot;table_list.php&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
			"<a href='#' onclick='confirmDelete(".$row["alias_id"].",&quot;table_list.php&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
	}
	else
	{
		echo "<td>
				<a href='#' onclick='showOrderDetail(&quot;".$row["table_id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["order_date"]."&quot;,&quot;".$row["total_price"].
			"&quot;,&quot;".$row["num"]."&quot;,&quot;".$row["foods"]."&quot;,&quot;".$row["is_paid"]."&quot;,&quot;".$row["waiter"]."&quot;,&quot;".$row["type_name"]."&quot;,&quot;".$row["total_price_2"]."&quot;,&quot;".$row["category_name"]."&quot;,&quot;".$row["comment"]."&quot;)'>
				<img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;查看</a>&nbsp;&nbsp;&nbsp;&nbsp;
				<a href='#' onclick='editTable(&quot;".$row["table_id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$table_name.
			"&quot;,&quot;table_list.php&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
			"<a href='#' onclick='confirmDelete(".$row["alias_id"].",&quot;table_list.php&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
	}
	echo "</tr>";
}
//mysql_query("SET NAMES utf8"); 
mysql_close($con);
		?>			
	</tbody>
  </table>
  <?PHP //echo $sql; ?>
  </div>
	<div id="controls">
       <div id="text"><?php echo "总计:" .$bh ."&nbsp;条记录"; ?>&nbsp;&nbsp;&nbsp;&nbsp;当前第 <span id="currentpage"></span> 页，每页 </div>
        <div id="perpage">
			<select onchange="sorter.size(this.value)">
			<option value="5">5</option>
				<option value="10">10</option>
				<option value="20" selected="selected">20</option>
				<option value="50">50</option>
				<option value="100">100</option>
			</select>
		</div>
        
        <div id="navigation">
        <span id="page-link"> </span>
          <a href="#" onclick="sorter.move(-1,true)">第一页</a>
          <a href="#" onclick="sorter.move(-1)">上一页</a>
          <a href="#" onclick="sorter.move(1)">下一页</a>
          <a href="#" onclick="sorter.move(1,true)">最末页</a>
        </div>
       </div>
<?php
echo "<input type='hidden' id='keyword_type_value' value='$xm' />";
echo "<input type='hidden' id='condition_type_value' value='$ct' />";
echo "<input type='hidden' id='keyword_value' value='$kw' />";
    ?>
	<script type="text/javascript" src="js/script.js"></script>
	<script type="text/javascript">
  var sorter = new TINY.table.sorter("sorter");
	sorter.head = "head";
	sorter.asc = "asc";
	sorter.desc = "desc";
	sorter.even = "evenrow";
	sorter.odd = "oddrow";
	sorter.evensel = "evenselected";
	sorter.oddsel = "oddselected";
	sorter.paginate = true;
	sorter.currentid = "currentpage";
	sorter.limitid = "pagelimit";
	sorter.init("table",0);
	initializeTable();
  </script>
</body>
</html>
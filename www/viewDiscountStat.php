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
<script type="text/javascript" src="js/order.js" ></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/common.js"></script>
</head>
<body style="width:99%;height:100%" onkeydown="orderStatKeyDown()" onload="this.focus()">
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
?>
<!--startprint1-->  
<div id="printTitle" style="display:none">
<?PHP

echo "退菜汇总";
?>
</div>
<div id="printContent" class="Content">        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr style="height: 25px;">
				<th><h3>帐单号</h3></th>
				<th><h3>日期</h3></th>
				<th><h3>名称</h3></th>
				<th><h3>单价（￥）</h3></th>
				<th><h3>数量</h3></th>
				<th><h3>折扣</h3></th>
				<th><h3>口味</h3></th>
				<th><h3>口味价钱（￥）</h3></th>
				<th><h3>厨房</h3></th>	
				<th><h3>服务员</h3></th>	
				<th><h3>金额（￥）</h3></th>				
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 		


$sql = "SELECT a.*,b.order_date,k.name AS kitchen_name,format((((`a`.`unit_price` * (1-`a`.`discount`)) + `a`.`taste_price`) * `a`.`order_count`),2) AS total_price FROM order_food_history a INNER JOIN order_history b ON a.order_id = b.id LEFT JOIN kitchen k ON a.kitchen=k.alias_id AND k.restaurant_id =".
	$_SESSION["restaurant_id"] . " WHERE a.food_status <> 8 AND a.discount < 1 AND a.order_count > 0 AND b.restaurant_id=" . $_SESSION["restaurant_id"];


$dateFrom = $_REQUEST["dateFrom"];
$dateTo = $_REQUEST["dateTo"];
if($dateFrom != "")
{
	$sql .= (" AND b.order_date >='" . $dateFrom . " 0:0:0'");
}
if($dateTo != "")
{
	$sql .= (" AND b.order_date <='" . $dateTo . " 23:59:59'");
}	

$sql .= " ORDER BY b.order_date DESC";

mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;	
$total_price = 0;	
$rs = $db->GetAll($sql);
foreach ($rs as $row){	
	$taste = $row["taste"];
	if($taste == "")
	{
		$taste = "无口味";
	}
	$kitchen_name = $row["kitchen_name"];
	if($kitchen_name == null)
	{
		$kitchen_name = "-";
	}
	$total_price += $row["total_price"];
	echo "<tr>";
	echo "<td>" .$row["order_id"] ."</td>";
	echo "<td>" .$row["order_date"] ."</td>";		
	echo "<td>" .$row["name"] ."</td>";
	echo "<td>" .$row["unit_price"] ."</td>";
	echo "<td>" .$row["order_count"] ."</td>";
	echo "<td>" .$row["discount"] ."</td>";
	echo "<td>" .$taste ."</td>";
	echo "<td>" .$row["taste_price"] ."</td>";	
	echo "<td>" .$kitchen_name ."</td>";
	echo "<td>" .$row["waiter"] ."</td>";
	echo "<td>" .$row["total_price"] ."</td>";
	echo "</tr>";
}	
		?>			
	</tbody>
	<tfoot>
		<tr>
			<td colspan="10" style="text-align:right">汇总：</td>
			<td><?php echo number_format($total_price,2); ?></td>		
		</tr>
	</tfoot>
  </table>
<?PHP //echo $sql; ?>
  </div>
    <!--endprint1-->
	<div id="controls" style="width:420px;text-align:right;margin: 0px -20px;">      
        <div id="text" style="font-size:12px;text-align:right"><?php echo "总计" .$bh ."&nbsp;条记录"; ?></div>

		<div id="navigation" style="font-size:12px;text-align:right">
  		      <span id="page-link"> </span>
				<a href="#" onclick="javascript:sorter.pagesize = 10000;sorter.init('table',0);window.open('PrintPage.html');">打印</a>
			  <a href="#" onclick="sorter.move(-1,true)">首页</a>
			  <a href="#" onclick="sorter.move(-1)">上页</a>
			  <a href="#" onclick="sorter.move(1)">下页</a>
			  <a href="#" onclick="sorter.move(1,true)">末页</a>
			  <a href="#" onclick="parent.closeWindow()">退出</a>
		</div>

		<span id="currentpage" style="display:none"></span>  
        <div id="perpage" style="display:none"></div>
   </div>
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
	sorter.pagesize = 10;
	sorter.init("table",0);
  </script>
<?php
//echo $sql;
?>
</body>
</html>
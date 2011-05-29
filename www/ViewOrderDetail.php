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
<script type="text/javascript" src="js/food.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/date.js"></script>
<script type="text/javascript" src="js/common.js"></script>
</head>
<body style="width:98%;height:100%" onkeydown="iframeKeyDown()" onload="this.focus()">
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
?>
<div id="divContent">
<div class="Content">        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr style="height: 25px;">
				<th>编号</th>				
				<th>日期</th>						
				<th>名称</th>	
				<th>单价（￥）</th>	
				<th>数量</th>	
				<th>折扣</th>	
				<th>口味</th>
				<th>口味价钱（￥）</th>		
				<th>厨房</th>		
				<th>服务员</th>			
				<th>备注</th>			
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 				
$id = $_REQUEST["id"];
$table_name = $_REQUEST["table_name"];
$sql = "SELECT of.id,of.order_date,of.name AS food_name,of.unit_price,of.order_count,of.discount,
CASE of.taste WHEN '' THEN '无口味' ELSE of.taste END AS taste,of.taste_price,
CASE WHEN k.name IS NULL THEN '空' ELSE k.name END AS kitchen_name,of.waiter,CASE WHEN comment IS NULL THEN '-' ELSE comment END AS comment
		from $table_name of
		LEFT JOIN kitchen k ON of.kitchen = k.alias_id 
		WHERE order_id = $id
		ORDER BY of.order_date DESC";


$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;

$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$bh=$bh+1;
	echo "<tr>";
	echo "<td>" .$bh ."</td>";
	echo "<td>" .$row["order_date"] ."</td>";				
	echo "<td>" .$row["food_name"] ."</td>";			
	echo "<td>" .$row["unit_price"]."</td>";
	echo "<td>" .number_format($row["order_count"],2) ."</td>";		
	echo "<td>" .number_format($row["discount"],2) ."</td>";	
	echo "<td>" .$row["taste"] ."</td>";							
	echo "<td>" .$row["taste_price"] ."</td>";	
	echo "<td>" .$row["kitchen_name"] ."</td>";		
	echo "<td>" .$row["waiter"] ."</td>";	
	echo "<td>" .$row["comment"] ."</td>";				
	echo "</tr>";
}	
mysql_close($con);
		?>			
	</tbody>	
  </table>
  </div>
  <?PHP //echo $sql; ?>
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
	</div>
</body>
</html>
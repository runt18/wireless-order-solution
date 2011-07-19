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
				<th><h3>编号</h3></th>
				<th><h3>交班人</h3></th>
				<th><h3>开始时间</h3></th>
				<th><h3>结束时间</h3></th>
				<th><h3>帐单数</h3></th>
				<th><h3>现金（￥）</h3></th>
				<th><h3>刷卡（￥）</h3></th>
				<th><h3>会员卡（￥）</h3></th>
				<th><h3>挂账（￥）</h3></th>
				<th><h3>签单（￥）</h3></th>
				<th><h3>金额（￥）</h3></th>	
				<th><h3>实收（￥）</h3></th>				
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 		
$dateFrom = $_REQUEST["dateFrom"];
$dateTo = $_REQUEST["dateTo"];
$where = "";
if($dateFrom != "")
{
	$where .= (" AND a.on_duty >='" . $dateFrom . " 0:0:0'");
}
if($dateTo != "")
{
	$where .= (" AND a.on_duty <='" . $dateTo . " 23:59:59'");
}	

$sql = "SELECT name,
on_duty,
off_duty,
COUNT(order_id) AS o_num,
				Sum(CASE type_value WHEN 1 THEN t_price ELSE 0.00 END) AS '现金',
				Sum(CASE type_value WHEN 2 THEN t_price ELSE 0.00 END) AS '刷卡',
				Sum(CASE type_value WHEN 3 THEN t_price ELSE 0.00 END) AS '会员卡',
				Sum(CASE type_value WHEN 4 THEN t_price ELSE 0.00 END) AS '挂账',
				Sum(CASE type_value WHEN 5 THEN t_price ELSE 0.00 END) AS '签单',
				CASE WHEN SUM(t_price) IS NULL THEN 0.00 ELSE SUM(t_price) END AS '金额',		
				CASE WHEN SUM(t_price_2) IS NULL THEN 0.00 ELSE SUM(t_price_2) END AS '实收'	
				FROM
(SELECT a.*,b.id AS order_id,Replace(b.total_price,',','') AS t_price,total_price_2 AS t_price_2, b.type_value FROM shift a
LEFT JOIN order_history_view b
ON a.name = b.waiter AND b.order_date BETWEEN a.on_duty AND a.off_duty AND a.restaurant_id = b.restaurant_id
WHERE a.restaurant_id = ".$_SESSION["restaurant_id"].$where.
") AS c
GROUP BY c.id ORDER BY on_duty ASC";

$bh=0;
$total_1=0;
$total_2=0;
$total_3=0;
$total_4=0;
$total_5=0;
$total_all=0;
$total_2_all=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
$rs = $db->GetAll($sql);
foreach ($rs as $row){	
	$bh=$bh+1;
	$total_1+=$row["现金"];
	$total_2+=$row["刷卡"];
	$total_3+=$row["会员卡"];
	$total_4+=$row["挂账"];
	$total_5+=$row["签单"];
	$total_all+=$row["金额"];	
	$total_2_all+=$row["实收"];
	echo "<tr>";
	echo "<td>" .$bh ."</td>";
	echo "<td>" .$row["name"] ."</td>";		
	echo "<td>" .$row["on_duty"] ."</td>";
	echo "<td>" .$row["off_duty"] ."</td>";
	echo "<td>" .$row["o_num"] ."</td>";
	echo "<td>" .number_format($row["现金"],2) ."</td>";
	echo "<td>" .number_format($row["刷卡"],2) ."</td>";
	echo "<td>" .number_format($row["会员卡"],2) ."</td>";	
	echo "<td>" .number_format($row["挂账"],2) ."</td>";
	echo "<td>" .number_format($row["签单"],2) ."</td>";
	echo "<td>" .number_format($row["金额"],2) ."</td>";
	echo "<td>" .number_format($row["实收"],2) ."</td>";
	echo "</tr>";
}	
		?>			
	</tbody>
		<tfood>
			<tr style="height: 25px;">							
				<td colspan="5" style="text-align:right">汇总：</td>
<?PHP
echo "<td>".number_format($total_1,2)."</td>";
echo "<td>".number_format($total_2,2)."</td>";
echo "<td>".number_format($total_3,2)."</td>";
echo "<td>".number_format($total_4,2)."</td>";
echo "<td>".number_format($total_5,2)."</td>";
echo "<td>".number_format($total_all,2)."</td>";	
echo "<td>".number_format($total_2_all,2)."</td>";								
				?>	
			</tr>
		</tfood>
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
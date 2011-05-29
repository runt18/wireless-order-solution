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
<body style="width:99%;height:100%" onkeydown="iframeKeyDown()" onload="this.focus()">
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
?>
<!--startprint1-->  
<div id="printContent" class="Content">        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr style="height: 25px;">
				<th><h3>编&nbsp;号</h3></th>
				<th><h3>日&nbsp;期</h3></th>
				<th><h3>名称</h3></th>
				<th><h3>现金（￥）</h3></th>
				<th><h3>刷卡（￥）</h3></th>
				<th><h3>会员卡（￥）</h3></th>
				<th><h3>挂账（￥）</h3></th>
				<th><h3>签单（￥）</h3></th>
				<th><h3>合计（￥）</h3></th>	
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 		
$sql = "SELECT o_date,
		kitchen,
		Sum(CASE type_value WHEN 1 THEN t_price ELSE 0.00 END) AS '现金',
		Sum(CASE type_value WHEN 2 THEN t_price ELSE 0.00 END) AS '刷卡',
		Sum(CASE type_value WHEN 3 THEN t_price ELSE 0.00 END) AS '会员卡',
		Sum(CASE type_value WHEN 4 THEN t_price ELSE 0.00 END) AS '挂账',
		Sum(CASE type_value WHEN 5 THEN t_price ELSE 0.00 END) AS '签单',
		SUM(t_price) AS '合计'
		FROM
		(SELECT DATE(c.order_date) AS o_date, a.name AS kitchen, c.type AS type_value,
		FORMAT(SUM((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`,2) AS t_price
		FROM kitchen a
		LEFT JOIN order_food_history b ON  a.alias_id = b.kitchen 
		LEFT JOIN order_history c ON b.order_id = c.id AND a.restaurant_id = c.restaurant_id WHERE a.restaurant_id=" . $_SESSION["restaurant_id"];	

$dateFrom = $_REQUEST["dateFrom"];
$dateTo = $_REQUEST["dateTo"];
$ids = $_REQUEST["ids"];
if($dateFrom != "")
{
	$sql .= (" AND c.order_date >='" . $dateFrom . " 0:0:0'");
}
if($dateTo != "")
{
	$sql .= (" AND c.order_date <='" . $dateTo . " 23:59:59'");
}	
if($ids != "")
{
	$sql .= (" AND a.id IN ($ids)");
}	

$sql .= (" GROUP BY DATE(c.order_date), a.name,c.type) AS b
			GROUP BY o_date,kitchen ORDER BY o_date DESC,kitchen DESC");


$bh=0;
$total_1=0;
$total_2=0;
$total_3=0;
$total_4=0;
$total_5=0;
$total_all=0;
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
	$total_all+=$row["合计"];
	echo "<tr>";
	echo "<td>" .$bh ."</td>";
	echo "<td>" .$row["o_date"] ."</td>";
	echo "<td>" .$row["kitchen"] ."</td>";		
	echo "<td>" .$row["现金"] ."</td>";
	echo "<td>" .$row["刷卡"] ."</td>";
	echo "<td>" .$row["会员卡"] ."</td>";
	echo "<td>" .$row["挂账"] ."</td>";
	echo "<td>" .$row["签单"] ."</td>";
	echo "<td>" .$row["合计"] ."</td>";
	echo "</tr>";
}	
/*$sql = "SELECT * FROM restaurant where id=".$_SESSION["restaurant_id"]; 
$rs = $db->GetRow($sql);
$total_income = $rs["total_income"];
mysql_close($con);

echo "<script>addTitle('$total_income');</script>";*/
		?>			
	</tbody>
	<tfood>
			<tr style="height: 25px;">							
				<td colspan="3" style="text-align:right">汇总：</td>
<?PHP
echo "<td>".number_format($total_1,2)."</td>";
echo "<td>".number_format($total_2,2)."</td>";
echo "<td>".number_format($total_3,2)."</td>";
echo "<td>".number_format($total_4,2)."</td>";
echo "<td>".number_format($total_5,2)."</td>";
echo "<td>".number_format($total_all,2)."</td>";				
				?>	
			</tr>
		</tfood>
  </table>
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
<?php
if($dateFrom != "" & $dateTo != "")
{
	echo "parent.document.getElementById('titleName').innerText = '分厨汇总(".$dateFrom."~".$dateTo.")';";
}
else if($dateFrom != "" & $dateTo == "")
	{
		echo "parent.document.getElementById('titleName').innerText = '分厨汇总(".$dateFrom."之后)';";
	}
	else if($dateFrom == "" & $dateTo != "")
		{
			echo "parent.document.getElementById('titleName').innerText = '分厨汇总(".$dateTo."之前)';";
		}
echo "parent.document.getElementById('div_add_foot').style.width = '800px';";
echo "parent.document.getElementById('mesWindow').style.left = '210px';";
echo "parent.document.getElementById('div_title_left').style.width = '764px';";
	?>
  </script>
<?php
//echo $sql;
?>
</body>
</html>
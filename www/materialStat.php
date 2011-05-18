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
<div class="Content">        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr style="height: 25px;">
				<th><h3>编&nbsp;号</h3></th>
				<th><h3>日&nbsp;期</h3></th>
<?php
$ids = $_REQUEST["ids"];
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$sql = "SELECT name FROM material WHERE restaurant_id=" . $_SESSION["restaurant_id"]." AND id IN($ids)";	

$materials = "";
$rs_material = $db->GetAll($sql);
foreach ($rs_material as $row){
	$materials.="Sum(CASE material_name WHEN '".$row["name"]."' THEN count ELSE 0.00 END) AS '".$row["name"]."_count',";
	$materials.="Sum(CASE material_name WHEN '".$row["name"]."' THEN t_price ELSE 0.00 END) AS '".$row["name"]."_total_price',";
	echo "<th><h3>".$row["name"]."</h3></th>";
}
	?>
				<th><h3>合计（￥）</h3></th>	
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 		
$statType = $_REQUEST["statType"];
$dateType = "";
if($statType == "daily")
{		
	$dateType = "DATE(c.order_date)";
}
else
{
	$dateType = "DATE_FORMAT(c.order_date,'%Y-%m')";	
}
$where = "";		
$dateFrom = $_REQUEST["dateFrom"];
$dateTo = $_REQUEST["dateTo"];
if($dateFrom != "")
{
	$where .= (" AND order_date >='" . $dateFrom . " 0:0:0'");
}
if($dateTo != "")
{
	$where .= (" AND order_date <='" . $dateTo . " 23:59:59'");
}	
$sql = "SELECT o_date,"
		.$materials
	."SUM(t_price) AS '合计'
		FROM
		(SELECT ".$dateType." AS o_date,a.name AS material_name,b.consumption*c.order_count AS count,
		b.price*b.consumption*c.order_count AS t_price 
		FROM material a
		LEFT JOIN `order_food_material_history` b ON a.alias_id = b.material_id
		INNER JOIN `order_food_history` c ON b.order_food_id = c.id WHERE a.restaurant_id=".$_SESSION["restaurant_id"].$where.
		" GROUP BY ".$dateType.",a.name) AS d
		GROUP BY o_date";


/*echo "<script>alert('" . $_SESSION["total_income"] . "');</script>";*/
$bh=0;
$total_all=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
$rs = $db->GetAll($sql);
$total = array();
foreach ($rs_material as $row_material){			
	$total[$row_material["name"]."_count"] = 0;
	$total[$row_material["name"]."_total_price"] = 0;
}
foreach ($rs as $row){
	$bh=$bh+1;
	$total_all+=$row["合计"];
	echo "<tr>";
	echo "<td>" .$bh ."</td>";
	echo "<td>" .$row["o_date"] ."</td>";
	foreach ($rs_material as $row_material){		
		echo "<td>".$row[$row_material["name"]."_count"]."份/￥".number_format($row[$row_material["name"]."_total_price"],2)."</td>";
		$total[$row_material["name"]."_count"] += $row[$row_material["name"]."_count"];
		$total[$row_material["name"]."_total_price"] += $row[$row_material["name"]."_total_price"];
	}
	echo "<td>￥" .number_format($row["合计"],2) ."</td>";
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
				<td colspan="2" style="text-align:right">汇总：</td>
<?PHP
foreach ($rs_material as $row_material){			
	echo "<td>".$total[$row_material["name"]."_count"]."份</td>";	
}		
				?>	
			<td>-</td></tr>
						<tr style="height: 25px;">							
				<td colspan="2" style="text-align:right">&nbsp;</td>
<?PHP
foreach ($rs_material as $row_material){			
	echo "<td>￥".number_format($total[$row_material["name"]."_total_price"],2)."</td>";	
}	
echo "<td>".number_format($total_all,2)."</td>";	
				?>	
			</tr>
		</tfood>
  </table>
  </div>
    <!--endprint1-->
	<?PHP //echo $statType; ?>
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
	echo "parent.document.getElementById('titleName').innerText = parent.document.getElementById('titleName').innerText + '(".$dateFrom."~".$dateTo.")';";
}
else if($dateFrom != "" & $dateTo == "")
	{
		echo "parent.document.getElementById('titleName').innerText = parent.document.getElementById('titleName').innerText + '(".$dateFrom."之后)';";
	}
	else if($dateFrom == "" & $dateTo != "")
		{
			echo "parent.document.getElementById('titleName').innerText = parent.document.getElementById('titleName').innerText + '(".$dateTo."之前)';";
		}
echo "parent.document.getElementById('div_add_foot').style.width = '800px';";
echo "parent.document.getElementById('mesWindow').style.left = '210px';";
echo "parent.document.getElementById('div_title_left').style.width = '764px';";
	?>
  </script>
</body>
</html>
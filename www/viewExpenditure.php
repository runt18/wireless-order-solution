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
<script type="text/javascript" src="js/member.js"></script>
</head>
<body style="width:98%;height:100%" onkeydown="viewExpenditureKeyDown()" onload="this.focus()">
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
				<th>编&nbsp;号</th>
				<th>帐单号</th>
				<th>日期</th>						
				<th>金&nbsp;额（￥）</th>			
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 				
	$id = $_REQUEST["id"];
	$dateFrom = $_REQUEST["dateFrom"];
	$dateTo = $_REQUEST["dateTo"];
	$sql = "SELECT id,order_date,total_price FROM `order_history` WHERE member_id = $id AND total_price > 0 AND restaurant_id=" . $_SESSION["restaurant_id"];
	if($dateFrom != "")
	{
		$sql .= (" AND order_date >='" . $dateFrom . " 0:0:0'");
	}
	if($dateTo != "")
	{
		$sql .= (" AND order_date <='" . $dateTo . " 23:59:59'");
	}	
	//echo "<script>alert('$sql');</script>";
	$bh=0;
	$total_price = 0;
	mysql_query("SET NAMES utf8"); 
	// mysql_query("set names 'utf-8'") ;
		
	$rs = $db->GetAll($sql);
	foreach ($rs as $row){
		$bh=$bh+1;
		$total_price += $row["total_price"];
		echo "<tr>";
		echo "<td>" .$bh ."</td>";
		echo "<td>" .$row["id"] ."</td>";	
		echo "<td>" .$row["order_date"] ."</td>";				
		echo "<td>" .$row["total_price"] ."</td>";					
		echo "</tr>";
	}	
mysql_close($con);
		?>			
	</tbody>
  </table>
  </div>
  
	<div id="controls" style="width:420px;text-align:right;margin: 0px -50px;">      
        <div id="text" style="font-size:12px;text-align:right"><?php echo "汇总（￥）：" .$total_price ."&nbsp;"; ?></div>

		<div id="navigation" style="font-size:12px;text-align:right">
  		      <span id="page-link"> </span>			  
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
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
<script type="text/javascript">
function submitSearch() {
if(getSelectedItem("selectedList", "searchForm") == "")
{
	alert("请至少选择一种菜品进行统计！");
	return;
}
document.getElementById("searchForm").submit();
}
</script>
</head> 
<body style="width:98%;height:100%" onkeydown="foodRankedKeyDown()" onload="this.focus()">
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
?>
<div id="divSearch">
	<form id="searchForm" action="foodRanked.php" method="post">
	 <div style="text-align:center">
		日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />
		&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />
	</div>
	<div style="width:100%;margin-top:10px;height:280px">
		<div style="float:left;width:40%;text-align:right;height:100%">
			<select id="foodList" multiple="multiple" ondblclick="moveSelectedItem('foodList','selectedList')" style="width:80%;height:100%">
<?PHP
$ids = $_POST["ids"];
if($ids == null)
{
	$sql = "SELECT id, name FROM food WHERE restaurant_id=" . $_SESSION["restaurant_id"];			      
	$rs = $db->GetAll($sql);
	foreach ($rs as $row){
		echo "<option value='".$row["id"]."'>".$row["name"]."</option>";
	}
}
				?>								
			</select>
		</div>
		<div style="float:left;width:20%;text-align:center;height:100%;padding-top:100px">		
			<div style="width:100%;text-align:center;margin-top:10px;font-size:14px"><a href="#" onclick="moveSelectedItem('foodList','selectedList')">&gt;</a></div>
			<div style="width:100%;text-align:center;margin-top:10px;font-size:14px"><a href="#" onclick="moveAllItem('foodList','selectedList')">&gt;&gt;</a></div>
			<div style="width:100%;text-align:center;margin-top:10px;font-size:14px"><a href="#" onclick="moveAllItem('selectedList','foodList')">&lt;&lt;</a></div>
			<div style="width:100%;text-align:center;margin-top:10px;font-size:14px"><a href="#" onclick="moveSelectedItem('selectedList','foodList')">&lt;</a></div>		
		</div>
		<div style="float:left;width:40%;text-align:left;height:100%">
			<select id="selectedList" multiple="multiple" ondblclick="moveSelectedItem('selectedList','foodList')" style="width:80%;height:100%">				
			</select>
		</div>
	</div>
	<div>
		 <span class="pop_action-span" style="margin-left:70px;margin-top:10px"><a href="#" onclick="submitSearch()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>
		 <span class="pop_action-span1" style="margin-right:70px;margin-top:10px"><a href="#" onclick="parent.closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>
	</div>
	</form>
</div>
<div id="divContent" style="display:none">
<div id="printTitle" style="display:none">
<?PHP
echo "点菜统计";
?>
</div>
<!--startprint1-->    
<div  id="printContent" class="Content">    
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr style="height: 25px;">
				<th>编&nbsp;号</th>
				<th>菜&nbsp;名</th>
				<th>厨&nbsp;房（￥）</th>		
				<th>数&nbsp;量</th>
				<th>金&nbsp;额（￥）</th>			
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 				

if($ids != null)
{	
	$dateFrom = $_POST["dateFrom"];
	$dateTo = $_POST["dateTo"];
	$sql = "SELECT f.id,f.alias_id,d.order_count,f.name,f.unit_price,CASE WHEN d.order_count IS NULL THEN 0 ELSE d.order_count END AS order_count,
			CASE WHEN format(d.total_price,2) IS NULL THEN 0.00 ELSE format(d.total_price,2) END AS total_price,CASE WHEN k.name IS NULL THEN '空' ELSE k.name END AS kitchen_name FROM 
			food f LEFT JOIN kitchen k ON f.kitchen = k.alias_id AND k.restaurant_id = " . $_SESSION["restaurant_id"]." LEFT JOIN 
			(SELECT a.food_id,SUM(a.order_count) AS order_count,SUM((a.unit_price*a.discount+a.taste_price)*a.order_count) AS total_price FROM order_food_history_view a 
			INNER JOIN `order_history` b ON a.order_id = b.id WHERE b.restaurant_id=" . $_SESSION["restaurant_id"];
	if($dateFrom != "")
	{
		$sql .= (" AND c.order_date >='" . $dateFrom . " 0:0:0'");
	}
	if($dateTo != "")
	{
		$sql .= (" AND c.order_date <='" . $dateTo . " 23:59:59'");
	}
	
	$sql .= (" GROUP BY a.food_id) AS d ON f.alias_id = d.food_id AND f.restaurant_id =" . $_SESSION["restaurant_id"]);	
	if($ids != "")
	{
		$sql.= " WHERE f.id IN ($ids)";
	}	
	$sql .=" ORDER BY f.alias_id ASC";
	//echo "<script>alert('$sql');</script>";
	$bh=0;
	mysql_query("SET NAMES utf8"); 
	// mysql_query("set names 'utf-8'") ;		
	$rs = $db->GetAll($sql);
	$total_price = 0;
	foreach ($rs as $row){
		$bh=$bh+1;
		$total_price  += $row["total_price"];
		echo "<tr>";
		echo "<td>" .$row["alias_id"] ."</td>";
		echo "<td>" .$row["name"] ."</td>";
		echo "<td>" .$row["kitchen_name"] ."</td>";	
		echo "<td>" .$row["order_count"] ."</td>";	
		echo "<td>" .$row["total_price"] ."</td>";			
		echo "</tr>";
	}	
}
mysql_close($con);
		?>			
	</tbody>
	<tfoot>
		<tr>
			<td colspan="4" style="text-align:right">汇总：</td>			
			<td><?php echo number_format($total_price,2); ?></td>
		</tr>
	</tfoot>
  </table>
  </div>
<?PHP //echo $sql; ?>
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
if($ids != null)
{	
	echo "document.getElementById('divSearch').style.display='none';document.getElementById('divContent').style.display = 'block';";
	if($dateFrom != "" & $dateTo != "")
	{
		echo "parent.document.getElementById('titleName').innerText = '点菜统计(".$dateFrom."~".$dateTo.")';";
	}
	else if($dateFrom != "" & $dateTo == "")
		{
			echo "parent.document.getElementById('titleName').innerText = '点菜统计(".$dateFrom."之后)';";
		}
		else if($dateFrom == "" & $dateTo != "")
			{
				echo "parent.document.getElementById('titleName').innerText = '点菜统计(".$dateTo."之前)';";
			}
}
	?>
  </script>
	</div>
</body>
</html>
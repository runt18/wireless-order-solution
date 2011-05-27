<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php
include("hasLogin.php"); 
?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>e点通－会员中心</title>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/general.css" />
<link rel="stylesheet" href="css/style.css" />
<link rel="stylesheet" href="css/pop_up.css" />
<link rel="stylesheet" type="text/css" href="css/calendar.css" media="screen" />
<script type="text/javascript" src="js/pop-up.js"></script>
<script type="text/javascript" src="js/order.js" ></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/mootools.js"></script>
<script type="text/javascript" src="js/date.js"></script>
<?php
include("conn.php");
$deleteId = $_POST["deleteId"];
$editType = $_POST["editType"];
if($deleteId != null)
{
	$sql2 = "DELETE FROM order_food WHERE order_id=$deleteId";
	$db->Execute($sql2);
	/*$sql3 = "DELETE FROM food WHERE id IN ($ids)";
	$db->Execute($sql3);*/
	$sql4 = "DELETE FROM `order` WHERE id=$deleteId";
	if($db->Execute($sql4))
	{
		echo "<script>alert('删除成功！');</script>";
	}	
	else{
		echo "<script>alert('删除失败！');</script>";
	}
}  

if($editType == "editOrder")
{
	$id = $_POST["id"];	
	$total_price_2 = $_POST["total_price_2"];			
	$type_value = $_POST["sel_type"];
	$table_id = $_POST["table_id"];
	$sel_category = $_POST["sel_category"];
	if($table_id != "-")
	{
		$sql = "UPDATE `order` SET total_price_2 = $total_price_2,type=$type_value,table_id=$table_id,category=$sel_category WHERE id=$id";
	}
	else
	{
		$sql = "UPDATE `order` SET total_price_2 = $total_price_2,type=$type_value,category=$sel_category WHERE id=$id";	
	}
	
	if($db->Execute($sql))
	{			
		echo "<script>alert('修改成功！');</script>";
	}	
	else{
		echo "<script>alert('修改失败！');</script>";
	}	
}
?>
</head>
<body>
<?php
//echo $sql;
include("changePassword.php"); 
$editType = $_POST["editType"];
if($editType == "dailyCheckOut")
{	
	$sql1 = "INSERT INTO `order_history`(`id`, `restaurant_id`,`order_date`, `total_price`,`total_price_2`, `custom_num`, 
			`waiter`,`type`, `member_id`, `member`,`terminal_pin`, `terminal_model`, `table_id`)
			SELECT `id`, `restaurant_id`,`order_date`, `total_price`,`total_price_2`, `custom_num`, 
			`waiter`,`type`, `member_id`, `member`,`terminal_pin`, `terminal_model`, `table_id` FROM `order` WHERE total_price IS NOT NULL AND restaurant_id=" . $_SESSION["restaurant_id"];		
	$sql2 = "INSERT INTO `order_food_history`(`id`,`order_id`, `food_id`, `order_date`, `order_count`, 
			`unit_price`,`name`, `taste`,`taste_price`,`taste_id`,`discount`,`kitchen`,`comment`,`waiter`)
			SELECT `id`,`order_id`, `food_id`, `order_date`, `order_count`, 
			`unit_price`,`name`, `taste`,`taste_price`,`taste_id`,`discount`,`kitchen`,`comment`,`waiter`
			FROM `order_food` WHERE `order_food`.`order_id` IN (SELECT id FROM `order` WHERE total_price  IS NOT NULL AND restaurant_id=" . $_SESSION["restaurant_id"].")";
	$sql3 = "DELETE FROM `order_food` WHERE `order_id` IN (SELECT id FROM `order` WHERE total_price  IS NOT NULL AND restaurant_id=" . $_SESSION["restaurant_id"].")";
	$sql4 = "DELETE FROM `order` WHERE total_price  IS NOT NULL AND restaurant_id=" . $_SESSION["restaurant_id"];	
	if($db->Execute($sql1) && $db->Execute($sql2) && $db->Execute($sql3) && $db->Execute($sql4))
	{
		echo "<script>alert('日结成功！');</script>";
	}	
	else{
		echo "<script>alert('日结失败！');</script>";
	}
}    
if($editType == "canEditOrder")
{
	$_id = $_POST["id"];	
	$_total_price_2 = $_POST["total_price_2"];	
	$_target = $_POST["target"];	
	$_type_value = $_POST["type_value"];	
	$_table_id = $_POST["table_id"];
	$_category = $_POST["category_value"];
	$_pwd2 = $_POST["pwd2"];		
	if(md5($_pwd2) == $_SESSION["pwd2"])	
	{
		echo "<script>editOrder('$_id','$_total_price_2','$_target','$_type_value','$_table_id','$_category')</script>";
	}
	else
	{
		echo "<script>alert('权限密码错误！');canEditOrder('$_id','$_total_price_2','$_target','$_type_value','$_table_id','$_category')</script>";
	}
}
?>
<h1>
<span class="action-span"><a href="#" onclick="showSearch('order.php');">高级搜索</a></span>
<span class="action-span"><a href="#" onclick="dailyCheckOut();">营业日结</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 当日帐单(<?php echo date("Y-m-d");?>)</span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form name="form1" action="order.php" method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type" onchange="showHideCondition(this)">
	<option value="0">全部</option>
	<option value="is_no">帐单号</option>
	<option value="is_name">台号</option>
	<option value="is_day">日期</option>
	<option value="is_category">类型</option>
	<option value="is_type">结帐方式</option>
	<option value="is_Price">金额</option>
	</select>
	<select id="type" name="type" style="display:none">
	<option value="1" selected="selected">现金</option>
	<option value="2">刷卡</option>	
	<option value="3">会员卡</option>
	<option value="4">挂账</option>	
	<option value="5">签单</option>
	</select>
	<select id="category" name="category" style="display:none">
	<option value="1" selected="selected">一般</option>
	<option value="2">外卖</option>	
	<option value="3">拼台</option>	
	</select>
	<select id="condition_type" name="condition_type" style="display:none"><option value="Equal">等于</option><option value="EqualOrGrater" selected="selected">大于等于</option><option value="EqualOrLess">小于等于</option></select>
    <!-- 关键字 -->
    <input type="text" id="keyword" name="keyword" style="width:136px" />
	<input type="hidden" name="viewId" />
    <input type="submit" value=" 搜索 " class="button" />
  
  </form>
</div>

<div class="Content">
	<!--startprint1-->
	<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>帐单号</h3></th>
				<th><h3>台&nbsp;号</h3></th>
                <th><h3>日&nbsp;期</h3></th>
				<th><h3>类&nbsp;型</h3></th>
				<th><h3>结帐方式</h3></th>
				<th><h3>金额（￥）</h3></th>
				<th><h3>实收（￥）</h3></th>
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
        
<?php         
include("conn.php");  
$xm=$_REQUEST["keyword_type"];
$ct=$_REQUEST["condition_type"];
$kw=$_REQUEST["keyword"]; 
$type = $_REQUEST["type"];
$category = $_REQUEST["category"];

$dateFrom=$_POST["dateFrom"];
$dateTo = $_POST["dateTo"];
$priceFrom = $_POST["priceFrom"];
$priceTo = $_POST["priceTo"];
$alias_id = $_POST["alias_id"];      
$type1 = $_POST["type"]; 
$category1 = $_POST["category"]; 
/*session_start(); */
$where = "";
$sql = "SELECT * FROM order_view WHERE is_paid <> 0 AND restaurant_id=" . $_SESSION["restaurant_id"];
if($dateFrom != null && $dateFrom !="")
{
	$sql .= " AND order_date >='" . $dateFrom . " 0:0:0'";
}
if($dateTo != null && $dateTo !="")
{
	$sql .= (" AND order_date <='" . $dateTo . " 23:59:59'");
}
if($priceFrom != null && $priceFrom != "")
{
	$sql .= (" AND total_price >=" . $priceFrom);					
}
if($priceTo != null && $priceTo != "")
{
	$sql .= (" AND total_price <=" . $priceTo);					
}
if($alias_id != null && $alias_id !="")
{
	$sql .= (" AND alias_id like '%" .$alias_id . "%'");
}
if($type1 != null && $type1 !="")
{
	$sql .= (" AND type_value = $type1");
}
if($category1 != null && $category1 !="")
{
	$sql .= (" AND category = $category1");
}
switch ($xm)
{
	case "is_no":
		if ($kw!="")
			$sql .= " AND id = '" .$kw. "'" ;  		
		break;
	case "is_name":
		if ($kw!="")
			$sql .= " AND alias_id like '" .$kw. "%'" ;  		
		break;
	case "is_day":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= (" AND order_date >= '" .$kw. " 0:0:0' AND order_date <= '"  .$kw.  " 23:59:59'");  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= (" AND order_date >= '" .$kw. " 0:0:0'"); 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= (" AND order_date <= '" .$kw. " 23:59:59'"); 
			}           			
		}						
		break;
	case "is_Price":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= (" AND total_price = $kw");   
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= (" AND total_price >= $kw"); 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= (" AND total_price <= $kw"); 
			}         			
		}		
		break;
	case "is_type":
		if ($type!="")
			$sql .= " AND type_value = $type" ;  		
		break;
	case "is_category":
		if ($category!="")
			$sql .= " AND category = $category" ;  		
		break;
}
$bh=0;
mysql_query("SET NAMES utf8"); 

$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$alias_id = $row["alias_id"];
	if($row["category"] != 1)
	{
		$alias_id = "-";
	}
	$bh=$bh+1;
	echo "<tr>";
	echo "<td>" .$row["id"] ."</td>";
	echo "<td>" .$alias_id ."</td>";
	echo "<td>" .$row["order_date"]. "</td>";
	echo "<td>" .$row["category_name"]."</td>";
	echo "<td>" .$row["type_name"]."</td>";
	echo "<td>" .$row["total_price"]."</td>";
	echo "<td>" .$row["total_price_2"]."</td>";
	echo "<td><a href='#' onclick='canEditOrder(&quot;".$row["id"]."&quot;,&quot;".$row["total_price_2"]."&quot;,&quot;order.php&quot;,&quot;".$row["type_value"]."&quot;,&quot;".$alias_id."&quot;,&quot;".$row["category"]."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='showOrderDetail(&quot;".$row["id"]."&quot;,&quot;".$alias_id."&quot;,&quot;".$row["order_date"]."&quot;,&quot;".$row["total_price"].
		"&quot;,&quot;".$row["num"]."&quot;,&quot;".$row["foods"]."&quot;,&quot;".$row["is_paid"]."&quot;,&quot;".$row["waiter"]."&quot;,&quot;".$row["type_name"]."&quot;,&quot;".$row["total_price_2"]."&quot;,&quot;".$row["category_name"]."&quot;,&quot;".$row["comment"]."&quot;)'>
			<img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;查看</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick='deleteOrder(&quot;".$row["id"]."&quot;,&quot;order.php&quot;)'>
			<img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
	echo "</tr>";
}

mysql_close($con);
         ?>      
		</tbody>
  </table>
<!--endprint1-->
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
echo "<input type='hidden' id='type_value' value='$type' />";
echo "<input type='hidden' id='category_value' value='$category' />";
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
	sorter.init("table",1);
	ininOriginal();
  </script>

</body>
</html>
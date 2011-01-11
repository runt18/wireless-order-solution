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
	<script type="text/javascript">		
		/*window.addEvent('domready', function() { 
			myCal1 = new Calendar({ dateFrom: 'd/m/Y' }, { direction: 1, tweak: {x: 6, y: 0} });		
				});*/
	</script>
<?php
include("conn.php");
$deleteId = $_POST["deleteId"];
if($deleteId != null)
{
	
	/*$sql1 = "SELECT food_id FROM order_food WHERE order_id=$deleteId";	
	$rs = $db->GetAll($sql);
	$ids = "";
	foreach ($rs as $row){
		if($ids != "")
			{
			$ids .= ",";
				}
		$ids .= $rs[0];
	}*/
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
?>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<h1>
<span class="action-span"><a href="#" onclick="showSearch();">高级搜索</a></span>
<span class="action-span"><a href="#" onclick="showOrderStat('daily');">日结汇总</a></span>
<span class="action-span"><a href="#" onclick="showOrderStat('monthly');">月结汇总</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 帐单管理 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form name="form1" action="order.php" method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type" onchange="showHideCondition(this)"><option value="0">全部</option><option value="is_no">帐单号</option><option value="is_name">台号</option><option value="is_day">日期</option><option value="is_Price">金额</option></select>
	<select id="condition_type" name="condition_type" style="display:none"><option value="Equal">等于</option><option value="EqualOrGrater" selected="selected">大于等于</option><option value="EqualOrLess">小于等于</option></select>
    <!-- 关键字 -->
    <input type="text" id="keyword" name="keyword" style="width:136px" />
	<input type="hidden" name="viewId" />
    <input type="submit" value=" 搜索 " class="button" />
  
  </form>
</div>

<div class="Content">
	<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>帐单号</h3></th>
				<th><h3>台&nbsp;号</h3></th>
                <th><h3>日&nbsp;期</h3></th>
				<th><h3>金&nbsp;额</h3></th>
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
        
         <?php         
         include("conn.php");        
         $xm=$_REQUEST["keyword_type"];
         $ct=$_REQUEST["condition_type"];
         $kw=$_REQUEST["keyword"]; 
         $dateFrom=$_POST["dateFrom"];
         $dateTo = $_POST["dateTo"];
         $priceFrom = $_POST["priceFrom"];
         $priceTo = $_POST["priceTo"];
         $alias_id = $_POST["alias_id"];         
         /*session_start(); */
         $where = "";
         $sql = "";
         if($dateFrom != null && $dateFrom !="")
         {
         	$where .= " AND order_date >='" . $dateFrom . " 0:0:0'";
         }
         if($dateTo != null && $dateTo !="")
         {
         	$where .= (" AND order_date <='" . $dateTo . " 23:59:59'");
         }
         if($priceFrom != null && $priceFrom != "")
         {
         	$where .= (" AND total_price >=" . $priceFrom);					
         }
         if($priceTo != null && $priceTo != "")
         {
         	$where .= (" AND total_price <=" . $priceTo);					
         }
         if($alias_id != null && $alias_id !="")
         {
         	$where .= (" AND alias_id like '%" .$alias_id . "%'");
         }
         switch ($xm)
         {
         	case "is_no":
         		if ($kw!="")
         			$sql = "SELECT * FROM order_view where id = '" .$kw. "'" ;  
         		else
         			$sql = "SELECT * FROM order_view WHERE 1=1" ; 
         		break;
         	case "is_name":
         		if ($kw!="")
         			$sql = "SELECT * FROM order_view  where alias_id like '" .$kw. "%'" ;  
         		else
         			$sql = "SELECT * FROM order_view WHERE 1=1" ; 
         		break;
         	case "is_day":
         		if ($kw!="")
         		{
         			if($ct == "Equal")
         			{
         				$sql = "SELECT * FROM order_view  where order_date >= '" .$kw. " 0:0:0' AND order_date <= '"  .$kw.  " 23:59:59'" ;  
         			}
         			elseif($ct == "EqualOrGrater")
         			{
         				$sql = "SELECT * FROM order_view  where order_date >= '" .$kw. " 0:0:0'" ; 
         			}
         			elseif($ct == "EqualOrLess")
         			{
         				$sql = "SELECT * FROM order_view  where order_date <= '" .$kw. " 23:59:59'" ; 
         			}           			
         		}				
         		else
         		{
         			$sql = "SELECT * FROM order_view WHERE 1=1" ; 
         		}
         		break;
         	case "is_Price":
         		if ($kw!="")
         		{
         			if($ct == "Equal")
         			{
         				$sql = "SELECT * FROM order_view  where total_price =" .$kw;   
         			}
         			elseif($ct == "EqualOrGrater")
         			{
         				$sql = "SELECT * FROM order_view  where total_price >= " .$kw. "" ; 
         			}
         			elseif($ct == "EqualOrLess")
         			{
         				$sql = "SELECT * FROM order_view  where total_price <= " .$kw. "" ; 
         			}         			
         		}
         		else
         		{
         			$sql = "SELECT * FROM order_view WHERE 1=1" ; 
         		} 
         		break;
         	default:
         		$sql = "SELECT * FROM order_view WHERE 1=1" . $where ; 
         }
         $sql .= (" AND is_paid <> 0 AND restaurant_id=" . $_SESSION["restaurant_id"]) ;
         $bh=0;
         mysql_query("SET NAMES utf8"); 
         
         $rs = $db->GetAll($sql);
         foreach ($rs as $row){
         	$bh=$bh+1;
         	echo "<tr>";
         	echo "<td>" .$row["id"] ."</td>";
         	echo "<td>" .$row["alias_id"] ."</td>";
         	echo "<td>" .$row["order_date"]. "</td>";
         	echo "<td>￥" .$row["total_price"]."</td>";
         	echo "<td><a href='#' onclick='showOrderDetail(&quot;".$row["id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["order_date"]."&quot;,&quot;".$row["total_price"]."&quot;,&quot;".$row["num"]."&quot;,&quot;".$row["foods"]."&quot;,&quot;".$row["is_paid"]."&quot;,&quot;".$row["waiter"]."&quot;)'><img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;查看</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick='deleteOrder(&quot;$row[0]&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
         	echo "</tr>";
         }

         mysql_close($con);
         ?>      
		</tbody>
  </table>
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
	sorter.init("table",1);
	ininOriginal();
  </script>

</body>
</html>
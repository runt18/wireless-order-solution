<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<?php
include("hasLogin.php"); 
?>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>e点通－管理中心</title>
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/general.css" />
<link rel="stylesheet" href="css/style.css" />
<link rel="stylesheet" href="css/pop_up.css" />
<link rel="stylesheet" type="text/css" href="css/calendar.css" media="screen" />
<script type="text/javascript" src="js/pop-up.js"></script>
<script type="text/javascript" src="js/order.js" ></script>
<script type="text/javascript" src="js/restaurant.js" ></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript">
	function ininTableOriginal() {
    var keyword_type = document.getElementById("keyword_type");    
    var keyword = document.getElementById("keyword");
    var keyword_type_value = document.getElementById("keyword_type_value").value;   
    var keyword_value = document.getElementById("keyword_value").value;
    for (var i = 0; i < keyword_type.options.length; i++) {
        if (keyword_type.options[i].value == keyword_type_value) {
            keyword_type.options[i].selected = true;
            break;
        }
    }   
    keyword.value = keyword_value;  
}
</script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<h1>
<span class="action-span1">Digi-e 管理中心</span><span id="search_id" class="action-span2"> - 餐台信息 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
	<form action="admin_table.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  ><option value="0">全部</option><option value="alias_id">编号</option><option value="restaurant_id">餐厅编号</option><option value="restaurant_name">餐厅名称</option></select>	
    <!-- 关键字 -->
    <input type="text" id="keyword" name="keyword"   />
    <input type="submit" value=" 搜索 " class="button" />
  </form>  
</div>

<div class="Content">
	<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>编&nbsp;号</h3></th>
				<th><h3>内部编号</h3></th>
                <th><h3>餐厅名称</h3></th>
                <th><h3>状&nbsp;态</h3></th>
			</tr>
		</thead>
        
		<tbody>
       
         <?php 
         include("conn.php");
         mysql_query("SET NAMES utf8"); 
         $sql = "SELECT od.id as order_id,t.alias_id,od.order_date,total_price,num,foods,is_paid,r.id AS restaurant_id,od.waiter,r.*,t.id as table_id FROM `table` t LEFT OUTER JOIN ".
         	"(SELECT MAX(id) AS OrderId,table_id ".
         	" FROM `order` GROUP BY `order`.table_id) AS o ON t.id = o.table_id".
         	" LEFT OUTER JOIN `order_view` od ON o.OrderId = od.id".
         	" INNER JOIN restaurant_view r ON t.restaurant_id = r.id WHERE t.enabled=1" ;
         $rs = $db->Execute($sql);
         
         $bh=0;                          
         
         $kw=$_REQUEST["keyword"]; 
         $xm=$_REQUEST["keyword_type"];

         switch ($xm)
         {
         	case "alias_id":
         		if ($kw!="")
         			$sql .= " AND t.id =$kw" ;       		
         		break;
         	case "restaurant_id":
         		if ($kw!="")
         			$sql .= " AND r.id = $kw" ;           		
         		break;
         	case "restaurant_name":
         		if ($kw!="")
         			$sql .= (" AND r.restaurant_name like '%" .$kw. "%'");           		
         		break;

         	default:         		
         }          
         /*echo $sql;*/                            
         $rs = $db->GetAll($sql);
         foreach ($rs as $row){
         	$bh=$bh+1;
         	$viewOrder = "";
         	$is_paid = $row["is_paid"];
         	if($is_paid != null && $is_paid != 1)	         	
         	{			         		
         		$viewOrder = "<a href='#' onclick='showOrderDetail(&quot;".$row["order_id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["order_date"]."&quot;,&quot;".$row["total_price"]."&quot;,&quot;".$row["num"]."&quot;,&quot;".$row["foods"]."&quot;,&quot;".$row["is_paid"]."&quot;,&quot;".$row["waiter"]."&quot;)'>就餐</a>";
         	}
         	else
         	{
         		$viewOrder="空闲";
         	}
         	echo "<tr>";
         	echo "<td>" .$row["table_id"] ."</td>";
         	echo "<td>" .$row["alias_id"] ."</td>";
         	echo "<td>"."<a href='#' onclick='viewRestaurant(&quot;".$row["id"]."&quot;,&quot;".$row["account"]."&quot;,&quot;".$row["restaurant_name"]."&quot;,&quot;".($row["record_alive"]/24/3600)."&quot;,&quot;".$row["order_num"]."&quot;,&quot;".$row["terminal_num"]."&quot;,&quot;".$row["food_num"]."&quot;,&quot;".$row["table_num"]."&quot;,&quot;".$row["order_paid"]."&quot;,&quot;".$row["table_using"]."&quot;)'>".
         		$row["restaurant_name"]."（".$row["id"]."）</a></td>";		
         	echo "<td>" .$viewOrder."</td>";
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
	ininTableOriginal();
  </script>
</body>
</html>
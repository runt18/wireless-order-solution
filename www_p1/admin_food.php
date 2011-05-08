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
<script type="text/javascript" src="js/restaurant.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<h1>
<span class="action-span"><a href="#" onclick="showFoodSearch();">高级搜索</a></span>
<span class="action-span1">e点通管理中心</span><span id="search_id" class="action-span2">  - 菜谱信息 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="admin_food.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideCondition(this)">
	<option value="0">全部</option>
	<option value="is_no">编号</option>
	<option value="is_name">名称</option>
	<option value="restaurant_id">餐厅编号</option>
	<option value="restaurant_name">餐厅名称</option>
	<option value="is_Price">单价</option>
	<option value="is_kitchen">厨房</option>
	</select>
	<select id="condition_type" name="condition_type" style="display:none">
	<option value="Equal">等于</option>
	<option value="EqualOrGrater" selected="selected">大于等于</option>
	<option value="EqualOrLess">小于等于</option>
	</select>
	<select id="kitchen" name="kitchen" style="display:none;width:150px;"><option value="0" selected="selected">厨房1</option><option value="1">厨房2</option>'+
	                            ' <option value="2">厨房3</option><option value="3">厨房4</option><option value="4">厨房5</option><option value="5">厨房6</option><option value="6">厨房7</option>'+
	                            ' <option value="7">厨房8</option><option value="8">厨房9</option><option value="9">厨房10</option><option value="255">空</option></select>
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
				<th><h3>名&nbsp;称</h3></th>
				<th><h3>单价（￥）</h3></th>
				<th><h3>厨&nbsp;房</h3></th>
				<th><h3>餐&nbsp;厅</h3></th>
			</tr>
		</thead>
		<tbody>
		<?php 	  		
		include("conn.php"); 		 		
		$xm=$_REQUEST["keyword_type"];
		$ct=$_REQUEST["condition_type"];
		$kw=$_REQUEST["keyword"]; 
		$kitchen_value=$_REQUEST["kitchen"];
		$food_name = $_POST["food_name"]; 
		$restaurant = $_POST["restaurant"]; 
		/*$restaurant_name = $_POST["restaurant_name"]; */
		$search_condition_type=$_POST["search_condition_type"];
		$search_restaurant_type=$_POST["search_restaurant_type"];
		$unit_price = $_POST["unit_price"];
		
		$sql = "SELECT a.id as `no`,a.alias_id,a.name,a.unit_price,a.kitchen,b.* FROM food a INNER JOIN restaurant_view b ON a.restaurant_id = b.id WHERE a.enabled=1";
		
		if($food_name != null && $food_name !="")
		{
			$sql .= (" AND name like '%" .$food_name . "%'");
		}
		/*if($restaurant_id != null && $restaurant_id !="")
		{
			$sql .= " AND restaurant_id =$restaurant_id";
		}*/
		if($restaurant != null && $restaurant !="")
		{
			if($search_restaurant_type == "Code")
			{
				$sql .= " AND restaurant_id =$restaurant";
			}
			else
			{
				$sql .= (" AND restaurant_name like '%" .$restaurant . "%'");
			}
		}	
		if ($unit_price!="")
		{
			if($search_condition_type == "Equal")
			{
				$sql .= " AND unit_price = $unit_price" ;  
			}
			elseif($search_condition_type == "EqualOrGrater")
			{
				$sql .= " AND unit_price >=  $unit_price" ; 
			}
			elseif($search_condition_type == "EqualOrLess")
			{
				$sql .= " AND unit_price <= $unit_price" ; 
			}
		}		
		switch ($xm)
		{
			case "is_no":
				if ($kw!="")
					$sql .= " AND a.id = '$kw'" ;  			
				break;
			case "is_name":
				if ($kw!="")
					$sql .= " AND name like '%$kw%'" ;  				
				break;
			case "restaurant_id":
				if ($kw!="")
					$sql .= (" AND restaurant_id=$kw");           		
				break;
			case "restaurant_name":
				if ($kw!="")
					$sql .= (" AND restaurant_name like '%" .$kw . "%'");        		
				break;
			case "is_Price":
				if ($kw!="")
				{
					if($ct == "Equal")
					{
						$sql .= " AND unit_price = $kw" ;  
					}
					elseif($ct == "EqualOrGrater")
					{
						$sql .= " AND unit_price >=  $kw" ; 
					}
					elseif($ct == "EqualOrLess")
					{
						$sql .= " AND unit_price <= $kw" ; 
					}
				}			
				break;		
			case "is_kitchen":
				if ($kitchen_value!="")
					$sql .= " AND kitchen=$kitchen_value" ;  			
				break;			
		}
		
		/*	$sql .=" ORDER BY id DESC";
			echo "<script>alert('$sql');</script>";*/
		$bh=0;
		mysql_query("SET NAMES utf8"); 	
		$kitchen = array('0'=>'厨房1', '1'=>'厨房2', '2'=>'厨房3', '3'=>'厨房4', '4'=>'厨房5', '5'=>'厨房6', '6'=>'厨房7', '7'=>'厨房8', '8'=>'厨房9', '9'=>'厨房10', '255'=>'空');	
		$rs = $db->GetAll($sql);
		foreach ($rs as $row){
			$bh=$bh+1;
			echo "<tr>";
			echo "<td>" .$row["no"]."</td>";
			echo "<td>" .$row["alias_id"] ."</td>";
			echo "<td>" .$row["name"] ."</td>";
			echo "<td>" .$row["unit_price"] ."</td>";
			echo "<td>" .$kitchen[$row["kitchen"]] ."</td>";
			echo "<td>"."<a href='#' onclick='viewRestaurant(&quot;".$row["id"]."&quot;,&quot;".$row["account"]."&quot;,&quot;".$row["restaurant_name"]."&quot;,&quot;".($row["record_alive"]/24/3600)."&quot;,&quot;".$row["order_num"]."&quot;,&quot;".$row["terminal_num"]."&quot;,&quot;".$row["food_num"]."&quot;,&quot;".$row["table_num"]."&quot;,&quot;".$row["order_paid"]."&quot;,&quot;".$row["table_using"]."&quot;)'>".
				$row["restaurant_name"]."（".$row["id"]."）</a></td>";			
			echo "</tr>";
		}
		//mysql_query("SET NAMES utf8"); 
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
    echo "<input type='hidden' id='kitchen_value' value='$kitchen_value' />";
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
	sorter.init("table",0);
	initializeFood();
  </script>
</body>
</html>
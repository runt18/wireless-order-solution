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
</head>
<body>
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
$foodCode = $_POST["foodCode"];
if($foodCode != null)
{
	$foodId = $_POST["foodId"];
	
	$foodName = $_POST["foodName"];
	$pinyin = $_POST["pinyin"];
	$foodPrice = $_POST["foodPrice"];
	$kitchen = $_POST["kitchenSelect"];
	$status = $_POST["statusValue"];
	$restaurant_id = $_SESSION["restaurant_id"];
	
	if($foodId == "" || $foodId == null)//如果是新增
	{
		
		$id = $restaurant_id;
		for($i=0;$i < 32;$i++)
		{
			$id *= 2;
		}
		$foodId = $id + $foodCode;//4 << 32 | $foodCode;
		
		$sql = "SELECT * FROM food WHERE id=$foodId";
		/*echo "<script>alert('$sql');</script>";*/
		$rs = $db ->GetOne($sql);
		if($rs)
		{
			echo "<script>alert('编号已存在！');editFood('','$foodCode','$foodName','$foodPrice','$kitchen',$status,'$pinyin');</script>";
		}
		else
		{			
			$sql = "INSERT INTO food(id,alias_id,`name`,unit_price,restaurant_id,kitchen,status,pinyin) VALUES($foodId,$foodCode,'$foodName',$foodPrice,$restaurant_id,$kitchen,$status,'$pinyin')";
			/*echo "<script>alert('$sql');</script>";*/
			if($db->Execute($sql))
			{
				echo "<script>alert('保存成功！');</script>";
			}
			else{
				echo "<script>alert('保存失败！');</script>";
			}			
		}
	}
	else//如果是编辑
	{
		
		$sql = "UPDATE food SET name='$foodName', unit_price=$foodPrice,kitchen=$kitchen,status = $status,pinyin='$pinyin' WHERE id=$foodId";	
		$db->Execute($sql);	
		if($db->Execute($sql))
		{
			echo "<script>alert('保存成功！');</script>";
		}
		else{
			echo "<script>alert('保存失败！');</script>";
		}	
	}	
}
$deleteId = $_POST["deleteId"];
if($deleteId != null)
{
	/*$sql = "DELETE FROM order_food WHERE food_id=$deleteId";*/
	/*$db->Execute($sql);*/
	$sql = "DELETE FROM food WHERE id=$deleteId";	
	if($db->Execute($sql))
	{
		echo "<script>alert('删除成功！');</script>";
	}	
	else{
		echo "<script>alert('删除失败！');</script>";
	}
}
//echo $sql;
?>
<h1>
<?php
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$kitchens = "";
$sql = "SELECT alias_id, name FROM kitchen WHERE restaurant_id=" . $_SESSION["restaurant_id"];
$rs = $db->GetAll($sql);
foreach ($rs as $row){	
	if($kitchens  != "")
	{
		$kitchens .= "@";
	}
	$kitchens .= ($row["alias_id"] . "|" . $row["name"]);
}
	?>
<span class="action-span"><a href="#" onclick="editFood('','','','','0','<?php echo $kitchens; ?>','','')">添加新菜</a></span><span class="action-span"><a href="#" onclick="showFoodRanked()">点菜统计</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 菜单管理 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="food.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideCondition(this)">
	<option value="0">全部</option>
	<option value="is_no">
	编号</option>
	<option value="is_name">名称</option>
	<option value="is_pinyin">拼音</option>
	<option value="is_Price">价格</option>
	<option value="is_kitchen">厨房</option>
	</select>
	<select id="condition_type" name="condition_type" style="display:none">
	<option value="Equal">等于</option>
	<option value="EqualOrGrater" selected="selected">大于等于</option>
	<option value="EqualOrLess">小于等于</option>
	</select>
	<select id="kitchen" name="kitchen" style="display:none;width:150px;">
<?php
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$sql = "SELECT alias_id, name FROM kitchen WHERE restaurant_id=" . $_SESSION["restaurant_id"];		
$rs = $db->GetAll($sql);
foreach ($rs as $row){
	echo "<option value='".$row["alias_id"]."'>".$row["name"]."</option>";	
}
	?>
<option value="255">空</option></select>
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
				<th><h3>名&nbsp;称</h3></th>
				<th><h3>拼&nbsp;音</h3></th>
				<th><h3>价格（￥）</h3></th>
				<th><h3>厨房打印</h3></th>
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 	  		

$xm=$_REQUEST["keyword_type"];
$ct=$_REQUEST["condition_type"];
$kw=$_REQUEST["keyword"]; 
$kitchen_value=$_REQUEST["kitchen"];
$sql = "SELECT f.*,CASE WHEN k.name IS NULL THEN '空' ELSE k.name END AS kitchen,f.kitchen AS kitchen_value FROM food f LEFT JOIN kitchen k ON f.kitchen = k.alias_id AND f.restaurant_id = k.restaurant_id WHERE f.restaurant_id=" . $_SESSION["restaurant_id"];		
switch ($xm)
{
	case "is_no":
		if ($kw!="")
			$sql .= " AND f.alias_id = $kw" ;  			
		break;
	case "is_name":
		if ($kw!="")
			$sql .= " AND f.name like '%$kw%'" ;  			
		break;
	case "is_pinyin":
		if ($kw!="")
			$sql .= " AND f.pinyin like '%$kw%'" ;  			
		break;
	case "is_Price":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND f.unit_price = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND f.unit_price >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND f.unit_price <= $kw" ; 
			}
		}				
		break;	
	case "is_kitchen":
		if ($kitchen_value!="")
			$sql .= " AND f.kitchen=$kitchen_value" ;  			
		break;		
}	
$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;
/*$kitchen = array('0'=>'厨房1', '1'=>'厨房2', '2'=>'厨房3', '3'=>'厨房4', '4'=>'厨房5', '5'=>'厨房6', '6'=>'厨房7', '7'=>'厨房8', '8'=>'厨房9', '9'=>'厨房10', '255'=>'空');*/
$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$bh=$bh+1;
	$py = "-";
	if($row["pinyin"] != null)
	{
		$py = $row["pinyin"];
	}
	$status = $row["status"];
	echo "<tr>";
	echo "<td>" .$row["alias_id"] ."</td>";
	echo "<td>" .$row["name"];
	if (($status & 1) != 0) {
		echo "  <img src='images/icon_tip_te.gif'  height='19' width='19' border='0'/>" ;
	}
	if (($status & 2) != 0) {
		echo "  <img src='images/icon_tip_jian.gif'  height='19' width='19' border='0'/>" ;
	}
	if (($status & 4) != 0) {
		echo "  <img src='images/icon_tip_ting.gif'  height='19' width='19' border='0'/>" ;
	}
	echo "</td>";
	echo "<td>" .$py ."</td>";
	echo "<td>" .$row["unit_price"] ."</td>";
	echo "<td>" .$row["kitchen"] ."</td>";
	echo "<td><a href='#' onclick='editFood(&quot;".$row["id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["name"]."&quot;,&quot;".$row["unit_price"]."&quot;,&quot;".$row["kitchen_value"]."&quot;,&quot;".$kitchens.
		"&quot;,".$row["status"].",&quot;".$row["pinyin"]."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='deleteFood(".$row["id"].")'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a>&nbsp;&nbsp;&nbsp;&nbsp;".
		"<a href='food_material.php?id=".$row["id"]."&name=".$row["name"]."'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;关联</a></td>";
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
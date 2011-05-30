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
<script type="text/javascript" src="js/food_material.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<?PHP
$id = $_REQUEST["id"];
$name = $_REQUEST["name"];
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$materials = "";
$sql = "SELECT id, name FROM material WHERE restaurant_id=" . $_SESSION["restaurant_id"];
$rs = $db->GetAll($sql);
foreach ($rs as $row){	
	if($materials  != "")
	{
		$materials .= "@";
	}
	$materials .= ($row["id"] . "|" . $row["name"]);
}

$editType = $_POST["editType"];
if($editType == "addFoodMaterial" || $editType == "editFoodMaterial")
{
	$food_id = $_POST["food_id"];
	$material_id = $_POST["material_id"];
	$material_name = $_POST["material_name"];
	$consumption = $_POST["consumption"];
	
	$validate = true;
	if($editType == "addFoodMaterial")
	{		
		$sql = "SELECT * FROM food_material WHERE food_id=$food_id AND material_id=$material_id";				
		$rs = $db ->GetOne($sql);		
		if($rs)
		{			
			echo "<script>alert('已关联此食材，选择其它食材！');editFoodMaterial('$food_id','$material_id','','$consumption','$materials');</script>";
			$validate = false;
		}
		else
		{
			$sql = "INSERT INTO food_material(food_id,material_id,consumption) VALUES($food_id,$material_id,$consumption)";
		}
	}
	else
	{		
		$sql = "UPDATE food_material SET consumption=$consumption WHERE food_id=$food_id AND material_id = $material_id";		
	}	
	if($validate)
	{
		if($db->Execute($sql))
		{
			echo "<script>alert('保存成功！');</script>";			
		}
		else{
			echo "<script>alert('保存失败！');</script>";
		}
	}
}
else if($editType == "deleteFoodMaterial")
	{
		$food_id = $_POST["food_id"];
		$material_id = $_POST["material_id"];
		$sql = "DELETE FROM food_material WHERE food_id=$food_id AND material_id = $material_id";
		
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
<span class="action-span"><a href="food.php">返回</a></span>
<span class="action-span"><a href="#" onclick="editFoodMaterial('<?PHP echo $id; ?>','','','','<?PHP echo $materials; ?>')">添加食材</a></span>
<span class="action-span1">关联食材</span><span id="search_id" class="action-span2">&nbsp;- <?PHP echo $name; ?> </span>
<div style="clear:both"></div>
</h1>

<div class="Content">


        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>编&nbsp;号</h3></th>
				<th><h3>食&nbsp;材</h3></th>
				<th><h3>消&nbsp;耗</h3></th>				
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		

$sql = "SELECT f.id AS food_id, m.id AS material_id, m.name AS material_name, fm.consumption FROM food f
INNER JOIN food_material fm ON f.id = fm.food_id
INNER JOIN material m ON fm.material_id = m.id WHERE f.id = $id";			
	
$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$bh=$bh+1;
	echo "<tr>";
	echo "<td>" .$bh ."</td>";	
	echo "<td>" .$row["material_name"] ."</td>";
	echo "<td>" .$row["consumption"] ."</td>";	
	echo "<td><a href='#' onclick='editFoodMaterial(&quot;".$row["food_id"]."&quot;,&quot;".$row["material_id"]."&quot;,&quot;".$row["material_name"]."&quot;,&quot;".$row["consumption"].
		"&quot;,&quot;&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='deleteFoodMaterial(&quot;".$row["food_id"]."&quot;,&quot;".$row["material_id"]."&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
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
  </script>
</body>
</html>
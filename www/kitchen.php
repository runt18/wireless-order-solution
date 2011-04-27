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
<script type="text/javascript" src="js/kitchen.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
$editType = $_POST["editType"];
if($editType == "addKitchen" || $editType == "editKitchen")
{
	//$alias_id = $_POST["alias_id"];
	$name = $_POST["name"];
	$discount = $_POST["discount"];
	$member_discount_1 = $_POST["member_discount_1"];
	$member_discount_2 = $_POST["member_discount_2"];
	
	$validate = true;
	if($editType == "addKitchen")
	{		
		/*$sql = "SELECT * FROM kitchen WHERE alias_id=$alias_id AND restaurant_id=" . $_SESSION["restaurant_id"];				
		$rs = $db ->GetOne($sql);		
		if($rs)
		{			
			echo "<script>alert('已存在此编号的厨房，请输入其它编号！');editKitchen('','$alias_id','$name','$discount','$member_discount_1','$member_discount_2');</script>";
			$validate = false;
		}
		else
		{
			$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name,discount,member_discount_1,member_discount_2) VALUES(".$_SESSION["restaurant_id"].",$alias_id,'$name',$discount,$member_discount_1,$member_discount_2)";
		}*/
	}
	else
	{
		$id = $_POST["id"];				
		$sql = "UPDATE kitchen SET name='$name',discount=$discount,member_discount_1=$member_discount_1,member_discount_2=$member_discount_2 WHERE id=$id";		
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
else if($editType == "deleteKitchen")
	{
		$id = $_POST["id"];
		
		if($db->Execute("DELETE FROM kitchen WHERE id=$id"))
		{			
			echo "<script>alert('删除成功！');</script>";
		}	
		else{
			echo "<script>alert('删除失败！');</script>";
		}	
	}
?>
<h1>
<span class="action-span" style="display:none"><a href="#" onclick="editKitchen('','','','','','')">添加厨房</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 分厨管理 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="kitchen.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideCondition(this)">
		<option value="0">全部</option>
		<option value="alias_id">编号</option>
		<option value="name">名称</option>
		<option value="discount">一般折扣</option>
		<option value="member_discount_1">会员折扣1</option>
		<option value="member_discount_2">会员折扣2</option>
	</select>
	<select id="condition_type" name="condition_type" style="display:none">
	<option value="Equal">等于</option>
	<option value="EqualOrGrater" selected="selected">大于等于</option>
	<option value="EqualOrLess">小于等于</option>
	</select>
	
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
				<th><h3>一般折扣</h3></th>				
				<th><h3>会员折扣1</h3></th>
				<th><h3>会员折扣2</h3></th>
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 				
$xm=$_REQUEST["keyword_type"];
$ct=$_REQUEST["condition_type"];
$kw=$_REQUEST["keyword"]; 
$sql = "SELECT id,alias_id,name,discount,member_discount_1,member_discount_2 FROM kitchen WHERE restaurant_id=" . $_SESSION["restaurant_id"];			
switch ($xm)
{
	case "alias_id":
		if ($kw!="")
			$sql .= " AND alias_id = $kw" ;  			
		break;
	case "name":
		if ($kw!="")
			$sql .= " AND name like '%$kw%'" ;  			
		break;	
	case "discount":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND discount = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND discount >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND discount <= $kw" ; 
			}
		}				
		break;		
	case "member_discount_1":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND member_discount_1 = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND member_discount_1 >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND member_discount_1 <= $kw" ; 
			}
		}				
		break;		
	case "member_discount_2":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND member_discount_2 = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND member_discount_2 >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND member_discount_2 <= $kw" ; 
			}
		}				
		break;		
}			
$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$bh=$bh+1;
	echo "<tr>";
	echo "<td>" .$bh ."</td>";
	echo "<td>" .$row["name"] ."</td>";
	echo "<td>" .$row["discount"] ."</td>";
	echo "<td>" .$row["member_discount_1"] ."</td>";
	echo "<td>" .$row["member_discount_2"] ."</td>";	
	echo "<td><a href='#' onclick='editKitchen(&quot;".$row["id"]."&quot;,&quot;".$bh."&quot;,&quot;".$row["name"]."&quot;,&quot;".$row["discount"]."&quot;,&quot;".$row["member_discount_1"]."&quot;,&quot;".$row["member_discount_2"].
		"&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' style='display:none' onclick='deleteKitchen(".$row["id"].")'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
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
	initializeKitchen();
  </script>
</body>
</html>
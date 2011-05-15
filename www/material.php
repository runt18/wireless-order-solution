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
<script type="text/javascript" src="js/material.js"></script>
<script type="text/javascript" src="js/date.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/common.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
$editType = $_POST["editType"];
if($editType == "addMaterial" || $editType == "editMaterial")
{
	$alias_id = $_POST["alias_id"];
	$name = $_POST["name"];
	$stock = $_POST["stock"];
	$price = $_POST["price"];
	$warning_threshold = $_POST["warning_threshold"];
	$danger_threshold = $_POST["danger_threshold"];
	
	$validate = true;
	if($editType == "addMaterial")
	{		
		$sql = "SELECT * FROM material WHERE alias_id=$alias_id AND restaurant_id=" . $_SESSION["restaurant_id"];				
		$rs = $db ->GetOne($sql);		
		if($rs)
		{			
			echo "<script>alert('已存在此编号的食材，请输入其它编号！');editMaterial('','$alias_id','$name','$stock','$price','$warning_threshold','$danger_threshold');</script>";
			$validate = false;
		}
		else
		{
			$sql = "INSERT INTO material(restaurant_id,alias_id,name,stock,price,warning_threshold,danger_threshold) VALUES(".$_SESSION["restaurant_id"].",$alias_id,'$name',$stock,$price,$warning_threshold,$danger_threshold)";
		}
	}
	else
	{
		$id = $_POST["id"];				
		$sql = "UPDATE material SET alias_id=$alias_id, name='$name',stock=$stock,price=$price,warning_threshold=$warning_threshold,danger_threshold=$danger_threshold WHERE id=$id";		
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
else if($editType == "deleteMaterial")
	{
		$id = $_POST["id"];
		
		if($db->Execute("DELETE FROM material WHERE id=$id"))
		{			
			echo "<script>alert('删除成功！');</script>";
		}	
		else{
			echo "<script>alert('删除失败！');</script>";
		}	
	}
	else if($editType == "inWarehouse")
		{
			$id = $_POST["id"];	
			$amount = $_POST["amount"];		
			$price = $_POST["price"];		
			$date = $_POST["date"];	
			$sql1 = "INSERT INTO material_history(material_id,amount,price,date) VALUES($id,$amount,$price,'$date')";	
			$sql2 = "UPDATE material SET stock = (stock + $amount), price = (stock*price + $price*$amount)/(stock + $amount) WHERE id=$id";
			
			if($db->Execute($sql1) && $db->Execute($sql2))
			{			
				echo "<script>alert('入库成功！');</script>";
			}	
			else{
				echo "<script>alert('入库失败！');</script>";
			}	
		}
		else if($editType == "viewDetail")
			{								
				$id = $_POST["id"];
				$name = $_POST["name"];
				$dateFrom = $_POST["dateFrom"];
				$dateTo = $_POST["dateTo"];
			
						echo "<script>viewInWarehouse('$id','$name','$dateFrom','$dateTo');</script>";				
			}
?>
<h1>
<span class="action-span"><a href="#" onclick="editMaterial('','','','','','','')">添加食材</a></span>
<span class="action-span"><a href="#" onclick="viewMaterialStat('daily');">日结汇总</a></span>
<span class="action-span"><a href="#" onclick="viewMaterialStat('monthly');">月结汇总</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 食材管理 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="material.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideCondition(this)">
		<option value="0">全部</option>
		<option value="alias_id">编号</option>
		<option value="name">名称</option>
		<option value="stock">库存量</option>
		<option value="price">价格</option>
		<option value="warning_threshold">预警阀值</option>
		<option value="danger_threshold">危险阀值</option>
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
				<th><h3>库存量</h3></th>				
				<th><h3>价格（￥）</h3></th>
				<th><h3>预警阀值</h3></th>				
				<th><h3>危险阀值</h3></th>						
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 				
$xm=$_REQUEST["keyword_type"];
$ct=$_REQUEST["condition_type"];
$kw=$_REQUEST["keyword"]; 
$discount_type_select = $_REQUEST["discount_type_select"];
$sql = "SELECT id,alias_id,name,stock,price,warning_threshold,danger_threshold FROM material WHERE restaurant_id=" . $_SESSION["restaurant_id"];	
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
	case "stock":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND stock = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND stock >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND stock <= $kw" ; 
			}
		}				
		break;	
	case "price":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND price = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND price >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND price <= $kw" ; 
			}
		}				
		break;		
	case "warning_threshold":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND warning_threshold = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND warning_threshold >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND warning_threshold <= $kw" ; 
			}
		}				
		break;		
	case "danger_threshold":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND danger_threshold = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND danger_threshold >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND danger_threshold <= $kw" ; 
			}
		}				
		break;		
}			
$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
echo $sql;
$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$bh=$bh+1;
	echo "<tr>";
	echo "<td>" .$row["alias_id"] ."</td>";	
	echo "<td>" .$row["name"] ."</td>";	
	echo "<td>" .$row["stock"] ."</td>";
	echo "<td>" .$row["price"] ."</td>";	
	echo "<td>" .$row["warning_threshold"] ."</td>";
	echo "<td>" .$row["danger_threshold"] ."</td>";		
	echo "<td>".		
		"<a href='#' onclick='inWarehouse(&quot;".$row["id"]."&quot;,&quot;".$row["name"]."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;入库</a>&nbsp;&nbsp;&nbsp;&nbsp;".
		"<a href='#' onclick='viewDetail(&quot;".$row["id"]."&quot;,&quot;".$row["name"]."&quot;)'><img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;明细&nbsp;&nbsp;&nbsp;&nbsp;</a>".
		"<a href='#' onclick='editMaterial(&quot;".$row["id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["name"]."&quot;,&quot;".$row["stock"]."&quot;,&quot;".$row["price"]."&quot;,&quot;".$row["warning_threshold"].
		"&quot;,&quot;".$row["danger_threshold"]."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='deleteMaterial(".$row["id"].")'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";		
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
echo "<input type='hidden' id='discount_type_select_value' value='$discount_type_select' />";
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
	initializeMaterial();
  </script>
</body>
</html>
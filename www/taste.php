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
<script type="text/javascript" src="js/taste.js"></script>
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
if($editType == "addTaste" || $editType == "editTaste")
{
	$alias_id = $_POST["alias_id"];
	$preference = $_POST["preference"];
	$price = $_POST["price"];
	
	$validate = true;
	if($editType == "addTaste")
	{		
		$sql = "SELECT * FROM taste WHERE alias_id=$alias_id";		
		$rs = $db ->GetOne($sql);		
		if($rs)
		{			
			echo "<script>alert('已存在此序号的口味，请输入其它序号！');editTaste('','$alias_id','$preference','$price','');</script>";
			$validate = false;
		}
		else
		{
			$sql = "INSERT INTO taste(restaurant_id,alias_id,preference,price) VALUES(".$_SESSION["restaurant_id"].",$alias_id,'$preference',$price)";
		}
	}
	else
	{
		$id = $_POST["id"];
		$old_alias_id = $_POST["old_alias_id"];
		if($old_alias_id != $alias_id)
		{
			$sql = "SELECT * FROM taste WHERE alias_id=$alias_id'";		
			$rs = $db ->GetOne($sql);		
			if($rs)
			{			
				echo "<script>alert('已存在此序号的口味，请输入其它序号！');editTaste('$id','$alias_id','$preference','$price','$old_alias_id');</script>";
				$validate = false;
			}
		}		
		$sql = "UPDATE taste SET alias_id=$alias_id, preference='$preference',price=$price WHERE id=$id";		
	}
	/*echo "<script>alert('$sql');</script>";*/
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
else if($editType == "deleteTaste")
	{
		$id = $_POST["id"];
		
		if($db->Execute("DELETE FROM taste WHERE id=$id"))
		{			
			echo "<script>alert('删除成功！');</script>";
		}	
		else{
			echo "<script>alert('删除失败！');</script>";
		}	
	}
?>
<h1>
<span class="action-span"><a href="#" onclick="editTaste('','','','','')">添加口味</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 口味管理 </span>
<div style="clear:both"></div>
</h1>

<div class="Content">


        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>序&nbsp;号</h3></th>
				<th><h3>口&nbsp;味</h3></th>
				<th><h3>价格（￥）</h3></th>				
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 		 			
$sql = "SELECT id,alias_id,preference,price FROM taste WHERE restaurant_id=" . $_SESSION["restaurant_id"]." ORDER BY alias_id";				
$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$bh=$bh+1;
	echo "<tr>";
	echo "<td>" .$row["alias_id"] ."</td>";
	echo "<td>" .$row["preference"] ."</td>";
	echo "<td>" .$row["price"] ."</td>";
	echo "<td><a href='#' onclick='editTaste(&quot;".$row["id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["preference"]."&quot;,&quot;".$row["price"]."&quot;,&quot;".$row["old_alias_id"].
		"&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='deleteTaste(".$row["id"].")'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
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
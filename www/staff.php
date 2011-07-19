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
<script type="text/javascript" src="js/staff.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/date.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
$editType = $_POST["editType"];
if($editType == "addStaff" || $editType == "editStaff")
{
	$alias_id = $_POST["alias_id"];
	$name = $_POST["name"];
	$pwd = $_POST["pwd"];
	$quota = $_POST["quota"];
	$validate = true;
	if($editType == "addStaff")
	{		
		$sql = "SELECT * FROM staff WHERE alias_id=$alias_id AND restaurant_id=" . $_SESSION["restaurant_id"];				
		$rs = $db ->GetOne($sql);		
		if($rs)
		{			
			echo "<script>alert('已存在此编号的员工，请输入其它编号！');editStaff('','$alias_id','$name','$pwd','','$quota');</script>";
			$validate = false;
		}
		else
		{
			$sql = "SELECT CASE MAX(pin) WHEN NULL THEN 0 ELSE MAX(pin) END FROM terminal WHERE model_name = 'Staff' AND pin < 1000000000";
			$rs = $db ->GetOne($sql);	
			$pin = $rs + 1;
			if($quota == "")
			{
				$sql = "INSERT INTO `terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`
						) VALUES ($pin, ".$_SESSION["restaurant_id"].", 255, 'Staff', '$name')";
			}
			else
			{
				$sql = "INSERT INTO `terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`,`gift_quota`
						) VALUES ($pin, ".$_SESSION["restaurant_id"].", 255, 'Staff', '$name',$quota)";
			}
			$db->Execute($sql);
			$sql = "SELECT id FROM terminal WHERE pin = $pin AND restaurant_id = ".$_SESSION["restaurant_id"];
			$rs = $db ->GetOne($sql);	
			$sql = "INSERT INTO staff(restaurant_id,terminal_id,alias_id,name,pwd) VALUES(".$_SESSION["restaurant_id"].",$rs,$alias_id,'$name','".md5($pwd)."')";
		}
	}
	else
	{
		$id = $_POST["id"];				
		$random = $_POST["random"];
		$sql = "SELECT terminal_id FROM staff WHERE id=$id";
		$terminal_id = $db ->GetOne($sql);	
		if($quota == "")
		{
			$db->Execute("UPDATE terminal SET `owner_name`='$name' WHERE id=$terminal_id");
		}
		else
		{
			$db->Execute("UPDATE terminal SET `owner_name`='$name',gift_quota=$quota WHERE id=$terminal_id");
		}
		
		if($pwd != $random)
		{		
			$sql = "UPDATE staff SET name='$name',pwd='".md5($pwd)."' WHERE id=$id";		
		}
		else
		{
			$sql = "UPDATE staff SET name='$name' WHERE id=$id";		
		}
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
else if($editType == "deleteStaff")
	{
		$id = $_POST["id"];
		$sql = "SELECT terminal_id FROM staff WHERE id=$id";
		$terminal_id = $db ->GetOne($sql);	
		if($db->Execute("DELETE FROM staff WHERE id=$id") && $db->Execute("DELETE FROM terminal WHERE id = $terminal_id"))
		{			
			echo "<script>alert('删除成功！');</script>";
		}	
		else{
			echo "<script>alert('删除失败！');</script>";
		}	
	}
$editType = $_POST["editType"];
if($editType == "viewShiftRecord")
{	
	$dateFrom = $_POST["dateFrom"];
	$dateTo = $_POST["dateTo"];
	echo "<script>showShiftRecord('$dateFrom','$dateTo');</script>";				
}  
//echo $sql;
?>
<h1>
<span class="action-span"><a href="#" onclick="editStaff('','','','','','')">添加员工</a></span>
<span class="action-span"><a href="#" onclick="viewShiftRecord();">交班记录</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 员工管理 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="staff.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideCondition(this)">
	<option value="0">全部</option>
	<option value="alias_id">编号</option>	
	<option value="name">姓名</option>	
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
				<th><h3>姓&nbsp;名</h3></th>							
				<th><h3>赠送额度（￥）</h3></th>		
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 	  		
include("conn.php"); 	
include("common.php"); 				
$xm=$_REQUEST["keyword_type"];
$kw=$_REQUEST["keyword"]; 
$sql = "SELECT a.id,a.alias_id,a.name,b.gift_quota FROM staff a INNER JOIN terminal b ON a.terminal_id = b.id WHERE a.restaurant_id=" . $_SESSION["restaurant_id"];			
switch ($xm)
{
	case "alias_id":
		if ($kw!="")
			$sql .= " AND a.alias_id = $kw" ;  			
		break;
	case "name":
		if ($kw!="")
			$sql .= " AND a.name like '%$kw%'" ;  			
		break;	
}			
$bh=0;
mysql_query("SET NAMES utf8"); 
// mysql_query("set names 'utf-8'") ;		
$rs = $db->GetAll($sql);
$pwd = random(6);
foreach ($rs as $row){
	$bh=$bh+1;	
	echo "<tr>";
	echo "<td>" .$row["alias_id"] ."</td>";
	echo "<td>" .$row["name"] ."</td>";	
	echo "<td>" .$row["gift_quota"] ."</td>";		
	echo "<td><a href='#' onclick='editStaff(&quot;".$row["id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["name"]."&quot;,&quot;".$pwd."&quot;,&quot;".$pwd."&quot;,&quot;".$row["gift_quota"].
		"&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='deleteStaff(".$row["id"].")'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
	echo "</tr>";
}
//mysql_query("SET NAMES utf8"); 
mysql_close($con);
		?>			
	</tbody>
  </table>
  </div>
  <?PHP //echo $sql ?>
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
	initializeStaff();
  </script>
</body>
</html>
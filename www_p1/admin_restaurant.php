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
<script type="text/javascript" src="js/pop-up.js"></script>
<link rel="stylesheet" href="css/pop_up.css" />
<link rel="stylesheet" href="css/style.css" />
<script type="text/javascript" src="js/pop-up.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/restaurant.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$editType = $_POST["editType"];
//echo "<script>alert('$editType');</script>";
if($editType == "editInfo")
{
	$sql = "UPDATE restaurant SET restaurant_info ='" . $_POST["restaurant_info"] . "' WHERE id = 1";
	if($db->Execute($sql))
	{
		echo "<script>alert('保存成功！');</script>";
		$_SESSION["root_restaurant_info"] = $_POST["restaurant_info"];
	}
	else{
		echo "<script>alert('保存失败！');</script>";
	}
}
else if($editType == "addRestaurant" || $editType == "editRestaurant")
	{
		$account = $_POST["account"];
		$pwd = $_POST["newPassword"];
		$restaurant_name = $_POST["restaurant_name"];
		$record_alive = $_POST["record_alive"];
		$restaurant_info = $_POST["restaurant_info"];
		$random_num = $_POST["random_num"];
		
		$record_alive = $record_alive*24*3600;
		/*$sql = "";*/
		$validate = true;
		if($editType == "addRestaurant")
		{		
			$sql = "SELECT * FROM restaurant WHERE account='$account'";		
			$rs = $db ->GetOne($sql);		
			if($rs)
			{			
				echo "<script>alert('已存在此帐户名，请选用另外一个帐户名！');editRestaurant('','$account','$restaurant_name','$restaurant_info','$record_alive','');</script>";
				$validate = false;
			}
			else
			{
				$sql = "INSERT INTO restaurant(pwd,account,restaurant_name,restaurant_info,total_income,record_alive) VALUES('".md5($pwd)."','$account','$restaurant_name','$restaurant_info',0,$record_alive)";
			}
		}
		else
		{
			$id = $_POST["id"];
			$old_account = $_POST["old_account"];
			if($old_account != $account)
			{
				$sql = "SELECT * FROM restaurant WHERE account='$account'";		
				$rs = $db ->GetOne($sql);		
				if($rs)
				{			
					echo "<script>alert('已存在此帐户名，请选用另外一个帐户名！');editRestaurant('$id','$account','$restaurant_name','$restaurant_info','$record_alive','$random_num','$old_account');</script>";
					$validate = false;
				}
			}
			if($pwd != $random_num)
			{
				$sql = "UPDATE restaurant SET account='$account', pwd='".md5($pwd)."',restaurant_name='$restaurant_name',restaurant_info='$restaurant_info',record_alive=$record_alive WHERE id=$id";
			}
			else
			{
				$sql = "UPDATE restaurant SET account='$account', restaurant_name='$restaurant_name',restaurant_info='$restaurant_info',record_alive=$record_alive WHERE id=$id";
			}
		}
		/*echo "<script>alert('$sql');</script>";*/
		if($validate)
		{
			if($db->Execute($sql))
			{
				echo "<script>alert('保存成功！');</script>";
				if($id == "1")
				{
					$_SESSION["root_restaurant_info"] = $restaurant_info;
				}
				if($editType == "addRestaurant")
				{
					$sql = "SELECT id FROM restaurant WHERE account='$account'";
					$rs = $db ->GetOne($sql);		
					$id = $rs;
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,0,'厨房1')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,1,'厨房2')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,2,'厨房3')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,3,'厨房4')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,4,'厨房5')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,5,'厨房6')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,6,'厨房7')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,7,'厨房8')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,8,'厨房9')";
					$db->Execute($sql);
					$sql = "INSERT INTO kitchen(restaurant_id,alias_id,name) VALUES($id,9,'厨房10')";
					$db->Execute($sql);
				}
			}
			else{
				echo "<script>alert('保存失败！');</script>";
			}
		}
	}
	else if($editType == "deleteRestaurant")
		{
			$id = $_POST["id"];
			$password = $_POST["password"];
			if($password == $_SESSION["password"])
			{
				if($db->Execute("DELETE FROM order_food WHERE food_id IN (SELECT id from food WHERE restaurant_id=$id)") && $db->Execute("DELETE FROM food WHERE restaurant_id=$id") 
						&& $db->Execute("DELETE FROM `order` WHERE restaurant_id=$id") && $db->Execute("DELETE FROM `table` WHERE restaurant_id=$id")  
						&& $db->Execute("DELETE FROM `taste` WHERE restaurant_id=$id")
						&& $db->Execute("UPDATE `terminal` SET restaurant_id=2,idle_date=NOW(),expire_date=NULL WHERE restaurant_id=$id") && $db->Execute("DELETE FROM restaurant WHERE id=$id"))
				{			
					echo "<script>alert('删除成功！');</script>";
				}	
				else{
					echo "<script>alert('删除失败！');</script>";
				}
			}
			else
			{
				echo "<script>alert('密码错误！');</script>";
			}
		}
?>
<h1>
<span class="action-span"><a href="#" onclick="editRestaurant('','','','','','')">添加餐厅</a></span><span class="action-span"><a href="#" onclick="editInfo('<?php echo str_replace("\n","<br />",str_replace("\r\n","<br />",$_SESSION["root_restaurant_info"])) ?>')">编辑信息</a></span>
<span class="action-span1">Digi-e 管理中心</span><span id="search_id" class="action-span2"> - 餐厅信息 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="admin_restaurant.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="document.getElementById('keyword').value='';">
	<option value="0">全部</option>
	<option value="is_id">编号</option>	
	<option value="is_account">帐号名</option>
	<option value="is_restaurant_name">餐厅名</option>	
	</select>	
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
				<th><h3>帐户名</h3></th>
				<th><h3>餐厅名</h3></th>
				<th><h3>帐单有效期</h3></th>
				<th><h3>餐厅信息</h3></th>			
				<th><h3>操作</h3></th>						
			</tr>
		</thead>
		<tbody>
<?php 

include("conn.php"); 		 		
$xm=$_REQUEST["keyword_type"];        
$kw=$_REQUEST["keyword"]; 	

$restaurant_id = $_SESSION["restaurant_id"];
$bh=0;
$sql = "SELECT * FROM restaurant_view WHERE 1=1";  

switch ($xm)
{
	case "is_id":
		if ($kw!="")
			$sql .= " AND id = $kw" ;  			
		break;
	case "is_account":
		if ($kw!="")
			$sql .= " AND account like '%$kw%'" ; 		
		break;
	case "is_restaurant_name":
		if ($kw!="")
			$sql .= " AND restaurant_name like '%$kw%'" ;  				
		break;           						
}		

$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$bh=$bh+1;
	$record_alive = "永久";
	if($row["record_alive"] !=0)
	{
		$record_alive = ($row["record_alive"]/24/3600)."天";
	}			
	$r_info = $row["restaurant_info"];
	$r_info_subject = str_replace("\n","<br />",str_replace("\r\n","<br />",cut_str($row["restaurant_info"],12)));			
	
	echo "<tr>";
	echo "<td>" .$row["id"] ."</td>";
	echo "<td>" .$row["account"] ."</td>";
	echo "<td>" .$row["restaurant_name"] ."</td>";
	echo "<td>" .$record_alive ."</td>";
	echo "<td title='".$r_info."'>" .$r_info_subject."</td>";
	echo "<td>".
		"<a href='#' onclick='viewRestaurant(&quot;".$row["id"]."&quot;,&quot;".$row["account"]."&quot;,&quot;".$row["restaurant_name"]."&quot;,&quot;".($row["record_alive"]/24/3600)."&quot;,&quot;".$row["order_num"]."&quot;,&quot;".$row["terminal_num"]."&quot;,&quot;".$row["food_num"]."&quot;,&quot;".$row["table_num"]."&quot;,&quot;".$row["order_paid"]."&quot;,&quot;".$row["table_using"]."&quot;)'><img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;查看</a>".
		"&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='editRestaurant(&quot;".$row["id"]."&quot;,&quot;".$row["account"]."&quot;,&quot;".$row["restaurant_name"]."&quot;,&quot;". str_replace("\n","<br />",str_replace("\r\n","<br />",$row["restaurant_info"])) ."&quot;,&quot;".($row["record_alive"]/24/3600)."&quot;,&quot;".random(6)."&quot;,&quot;".$row["account"]."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>".
		"&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='deleteRestaurant(&quot;".$row["id"]."&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a></td>";
	echo "</tr>";
}
//mysql_query("SET NAMES utf8"); 
mysql_close($con);
function cut_str($string, $sublen, $start = 0, $code = 'UTF-8') 
{ 
	if($code == 'UTF-8') 
	{ 
		$pa = "/[\x01-\x7f]|[\xc2-\xdf][\x80-\xbf]|\xe0[\xa0-\xbf][\x80-\xbf]|[\xe1-\xef][\x80-\xbf][\x80-\xbf]|\xf0[\x90-\xbf][\x80-\xbf][\x80-\xbf]|[\xf1-\xf7][\x80-\xbf][\x80-\xbf][\x80-\xbf]/"; 
		preg_match_all($pa, $string, $t_string); 
		
		if(count($t_string[0]) - $start > $sublen) return join('', array_slice($t_string[0], $start, $sublen))."..."; 
		return join('', array_slice($t_string[0], $start, $sublen)); 
	} 
	else 
	{ 
		$start = $start*2; 
		$sublen = $sublen*2; 
		$strlen = strlen($string); 
		$tmpstr = ''; 
		
		for($i=0; $i< $strlen; $i++) 
		{ 
			if($i>=$start && $i< ($start+$sublen)) 
			{ 
				if(ord(substr($string, $i, 1))>129) 
				{ 
					$tmpstr.= substr($string, $i, 2); 
				} 
				else 
				{ 
					$tmpstr.= substr($string, $i, 1); 
				} 
			} 
			if(ord(substr($string, $i, 1))>129) $i++; 
		} 
		if(strlen($tmpstr)< $strlen ) $tmpstr.= "..."; 
		return $tmpstr; 
	} 
} 
           ?>
			
		</tbody>
  </table>
  </div>
	<div id="controls">
       <div id="text"><?php echo "总计:" .$bh ."&nbsp;条记录"; ?>&nbsp;&nbsp;&nbsp;&nbsp;当前第 <span id="currentpage"></span> 页，每页 </div>
        <div id="perpage">
			<select onchange="sorter.size(this.value)">
			<option value="5">5</option>
				<option value="10" selected="selected">10</option>
				<option value="20">20</option>
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
	sorter.init("table",0);
	ininRestaurantOriginal();
  </script>
<?php
function random($length) { 
	$hash = array();
	$number = "";
	$chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz'; 
	$max = strlen($chars) - 1; 
	while(count($hash)<=$length){
		$hash[] = mt_rand(0,$max);
		$hash = array_unique($hash);
	}
	for ($j=0;$j<$length;$j++){
		$number.=substr($chars,$hash[$j],1);
	}
	return $number;
} 
?>
</body>
</html>
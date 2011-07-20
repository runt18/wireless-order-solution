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
<script type="text/javascript" src="js/pop-up.js"></script>
<link rel="stylesheet" href="css/pop_up.css" />
<link rel="stylesheet" href="css/style.css" />
<script type="text/javascript" src="js/pop-up.js"></script>
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/terminal.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$editType = $_POST["editType"];
//echo "<script>alert('$editType');</script>";
if($editType == "setOwner")
{
	$pin=$_POST["pin"];
	$quota=$_POST["gift_quota"];
	if($quota < 0)
	{
		$quota = -1;
	}
	$sql = "UPDATE terminal SET owner_name ='" . $_POST["owner_name"] . "' , gift_quota = $quota WHERE pin = $pin";
	if($db->Execute($sql))
	{
		echo "<script>alert('保存成功！');</script>";		
	}
	else{
		echo "<script>alert('保存失败！');</script>";
	}
}
?>
<h1>

<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 终端信息 </span>
<div style="clear:both"></div>
</h1>


<div class="Content">
	<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>编&nbsp;号</h3></th>
				<th><h3>机&nbsp;型</h3></th>
				<th><h3>持有人</h3></th>
				<th><h3>PIN</h3></th>
				<th><h3>有效期</h3></th>
				<th><h3>已赠送（￥）</h3></th>
				<th><h3>赠送额度（￥）</h3></th>
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
<?php 
include("conn.php"); 
$restaurant_id = $_SESSION["restaurant_id"];
$bh=0;
$sql = "SELECT model_id,model_name,owner_name,pin,expire_date,gift_amount,gift_quota FROM `terminal` WHERE restaurant_id=$restaurant_id and model_id<=0x7F ORDER BY restaurant_id" ;  

$rs = $db->GetAll($sql);
foreach ($rs as $row){
	$background = "";
	$toolTip = "";
	$currentDate = date("Y-m-d");
	$time1 = strtotime($row["expire_date"]);
	$time2 = strtotime("now");
	$diff = ($time1 - $time2)/(3600*24);
	/*$currentDate = getdate(*/
	//$diff= abs(($time2 – $time1)/(3600*24));
	$disable = "";
	if($row["model_id"] == 255)
	{
		$disable = "disabled='disabled'";
	}
	if($row["expire_date"] != null)
	{
		if($diff<0)
		{
			//过期
			$background = "style='background-color:red'";
			$toolTip=" title='过期'";
		}
		else
		{
			if($diff <= 15)
			{
				$background = "style='background-color:yellow'";
				$toolTip=" title='距离有效期还有" .ceil($diff)."天'";
			}
		}
	}
	$bh=$bh+1;
	$expire_date = $row["expire_date"];
	if($expire_date == null)
	{
		$expire_date = "-";
	}
	$gift_quota = $row["gift_quota"];
	if($gift_quota < 0)
	{
		$gift_quota = "-";
	}
	echo "<tr>";
	echo "<td ".$background.$toolTip.">" .$bh ."</td>";
	echo "<td ".$background.$toolTip.">" .$row["model_name"] ."</td>";
	echo "<td ".$background.$toolTip.">" .$row["owner_name"] ."</td>";
	echo "<td ".$background.$toolTip.">" .strtoupper(base_convert($row["pin"],10,16)) ."</td>";
	echo "<td ".$background.$toolTip.">" .$expire_date ."</td>";
	echo "<td ".$background.$toolTip.">" .$row["gift_amount"] ."</td>";
	echo "<td ".$background.$toolTip.">" .$gift_quota ."</td>";
	echo "<td $disable ".$background.$toolTip."><a href='#' onclick='setOwner(&quot;".$row["pin"]."&quot;,&quot;".$row["owner_name"]."&quot;,&quot;".$gift_quota."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;设定</a>";
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
  </script>
</body>
</html>
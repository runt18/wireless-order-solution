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
<script type="text/javascript" src="js/date.js"></script>
<script type="text/javascript" src="js/common.js"></script>
</head> 
<body style="width:98%;height:100%" onkeydown="iframeKeyDown()" onload="this.focus()">
<?php
include("changePassword.php"); 
?>
<?PHP
include("conn.php");
mysql_query("SET NAMES utf8"); 
$target = $_REQUEST["target"];
$statType = $_REQUEST["statType"];
?>
<div id="divSearch">
	<form id="searchForm" action="<?PHP echo $target."?statType=".$statType; ?>" method="post">
	<input id="editType" type="hidden" name="editType" value="viewStat"></input>
	 <div style="text-align:center">
		日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;至&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />
	</div>
	<div style="width:100%;margin-top:10px;height:280px">
		<div style="float:left;width:45%;text-align:right;height:100%">
			<select id="itemList" multiple="multiple" ondblclick="moveSelectedItem('itemList','selectedList')" style="width:80%;height:100%">
<?PHP
$table = $_REQUEST["table"];
$name = $_REQUEST["name"];
$value = $_REQUEST["value"];
$msg = $_REQUEST["msg"];
$sql = "SELECT $value, $name FROM $table WHERE restaurant_id=" . $_SESSION["restaurant_id"];			      
$rs = $db->GetAll($sql);
foreach ($rs as $row){
	echo "<option value='".$row["$value"]."'>".$row["$name"]."</option>";
}
				?>								
			</select>
		</div>
		<div style="float:left;width:10%;text-align:center;height:100%;">		
			<div style="width:100%;text-align:center;margin-top:100px;font-size:14px"><a href="#" onclick="moveSelectedItem('itemList','selectedList')">&gt;</a></div>
			<div style="width:100%;text-align:center;margin-top:10px;font-size:14px"><a href="#" onclick="moveAllItem('itemList','selectedList')">&gt;&gt;</a></div>
			<div style="width:100%;text-align:center;margin-top:10px;font-size:14px"><a href="#" onclick="moveAllItem('selectedList','itemList')">&lt;&lt;</a></div>
			<div style="width:100%;text-align:center;margin-top:10px;font-size:14px"><a href="#" onclick="moveSelectedItem('selectedList','itemList')">&lt;</a></div>		
		</div>
		<div style="float:left;width:45%;text-align:left;height:100%">
			<select id="selectedList" multiple="multiple" ondblclick="moveSelectedItem('selectedList','itemList')" style="width:80%;height:100%">				
			</select>
		</div>
	</div>
	<div>
		 <span class="pop_action-span" style="margin-left:130px;"><a href="#" onclick="submitSearch('<?PHP echo $msg; ?>')">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>
		 <span class="pop_action-span1" style="margin-right:130px;"><a href="#" onclick="parent.closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>
	</div>
	</form>
</div>
</body>
</html>
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
<script type="text/javascript" src="js/changePassword.js"></script>
<script type="text/javascript" src="js/terminal.js"></script>
<script type="text/javascript" src="js/restaurant.js"></script>
<script type="text/javascript" src="js/mootools.js"></script>
<script type="text/javascript" src="js/date.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
include("conn.php"); 
mysql_query("SET NAMES utf8"); 
$editType = $_POST["editType"];
//echo "<script>alert('$editType');</script>";

if($editType == "addTerminal")
{
	$new_pin = $_POST["new_pin"];
	$model_id = $_POST["sel_model"];	
	$model_name = $_POST["model_name"];	
	$validate = true;	
	$sql = "SELECT * FROM terminal WHERE pin=".base_convert($new_pin,16,10);		
	$rs = $db ->GetOne($sql);
	if($rs)
	{
		echo "<script>alert('已存在此PIN的终端，请重新输入新终端的PIN码！');addTerminal('$pin','$new_pin','$model_id','$model_name')</script>";
		$validate = false;
	}
	if($validate)
	{
		$sql = "INSERT INTO terminal(pin,restaurant_id,model_id,model_name,entry_date,idle_date,owner_name) VALUES(".strtoupper(base_convert($new_pin,16,10)).",2,$model_id,'$model_name',NOW(),NOW(),'')";
		if($db->Execute($sql))
		{
			echo "<script>alert('保存成功！');</script>";		
		}
		else{
			echo "<script>alert('保存失败！');</script>";
		}
	}
}
else if($editType == "editTerminal")
{
	$pin = $_POST["pin"];
	$new_pin = $_POST["new_pin"];
	$model_id = $_POST["sel_model"];	
	$model_name = $_POST["model_name"];	
	$owner_name = $_POST["owner_name"];	
	$validate = true;
	if($pin != $new_pin)
	{
		$sql = "SELECT * FROM terminal WHERE pin=".base_convert($new_pin,16,10);		
		$rs = $db ->GetOne($sql);
		if($rs)
		{
				echo "<script>alert('已存在此PIN的终端，请重新输入新终端的PIN码！');editTerminal('$pin','$new_pin','$model_id','$model_name','$owner_name')</script>";
			$validate = false;
		}
	}	
	if($validate)
	{
			$sql = "UPDATE terminal SET pin=".base_convert($new_pin,16,10). ",model_id=$model_id,model_name='$model_name',owner_name='$owner_name' WHERE pin=".base_convert($pin,16,10);		
		/*echo $sql;*/	
		if($db->Execute($sql))
		{
			echo "<script>alert('保存成功！');</script>";		
		}
		else{
			echo "<script>alert('保存失败！');</script>";
		}
	}
}
else if($editType == "editUsingTerminal")
{
	$pin = $_POST["pin"];
	$new_pin = $_POST["new_pin"];
	$restaurant_id = $_POST["restaurant_id"];	
	$model_name = $_POST["model_name"];
	$expire_date = $_POST["expire_date"];
	$owner_name = $_POST["owner_name"];	
	$validate = true;
	if($pin != $new_pin)
	{
		$sql = "SELECT * FROM terminal WHERE pin=".strtoupper(base_convert($new_pin,16,10));
		$rs = $db ->GetOne($sql);
		if($rs)
		{
			echo "<script>alert('已存在此PIN的终端，请重新输入新终端的PIN码！');editUsingTerminal('$pin','$new_pin','$restaurant_id','$model_name','$expire_date','$owner_name');</script>";
			$validate = false;
		}
	}
	$sql = "SELECT * FROM restaurant WHERE id=$restaurant_id";
	$rs = $db ->GetOne($sql);
	if($rs == null)
	{
		echo "<script>alert('餐厅编号不存在！');editUsingTerminal('$pin','$new_pin','$restaurant_id','$model_name','$expire_date','$owner_name');</script>";
		$validate = false;
	}
	if($validate)
	{		
		$sql = "UPDATE terminal SET pin=".strtoupper(base_convert($new_pin,16,10)). ",restaurant_id=$restaurant_id,model_name='$model_name',expire_date='$expire_date',owner_name='$owner_name' WHERE pin=".strtoupper(base_convert($pin,16,10));
		/*echo $sql;*/
		if($db->Execute($sql))
		{
			echo "<script>alert('保存成功！');</script>";
		}	
		else{
			echo "<script>alert('保存失败！');</script>";
		}
	}
}
else if($editType == "uninstallTerminal")
{
	$pin = $_POST["pin"];
	$sql = "UPDATE terminal SET restaurant_id=2,idle_date=NOW(),expire_date=NULL WHERE pin=".strtoupper(base_convert($pin,16,10));
	if($db->Execute($sql))
	{
		echo "<script>alert('卸载成功！');</script>";
	}	
	else{
		echo "<script>alert('卸载失败！');</script>";
	}
}
else if($editType == "installTerminal")
{
	$pin = $_POST["pin"];
	$restaurant_id = $_POST["restaurant_id"];		
	$expire_date = $_POST["expire_date"];
	
	$sql = "SELECT * FROM restaurant WHERE id=$restaurant_id";
	$rs = $db ->GetOne($sql);
	if($rs == null)
	{
		echo "<script>alert('餐厅编号不存在！');installTerminal('$pin','$restaurant_id','$expire_date');</script>";		
	}
	else
	{		
		$sql = "UPDATE terminal SET restaurant_id=$restaurant_id,expire_date='$expire_date',work_date=NOW() WHERE pin=".strtoupper(base_convert($pin,16,10));
		if($db->Execute($sql))
		{
			echo "<script>alert('挂载成功！');</script>";
		}	
		else{
			echo "<script>alert('挂载失败！');</script>";
		}
	}
}
else if($editType == "discardTerminal")
{
	$pin = $_POST["pin"];
	$sql = "UPDATE terminal SET restaurant_id=3,discard_date=NOW(),expire_date=NULL WHERE pin=".strtoupper(base_convert($pin,16,10));
	if($db->Execute($sql))
	{
		echo "<script>alert('废弃成功！');</script>";
	}	
	else{
		echo "<script>alert('废弃失败！');</script>";
	}
}
else if($editType == "recycleTerminal")
{
	$pin = $_POST["pin"];
	$sql = "UPDATE terminal SET restaurant_id=2,idle_date=NOW(),discard_date=NULL WHERE pin=".strtoupper(base_convert($pin,16,10));
	if($db->Execute($sql))
	{
		echo "<script>alert('回收成功！');</script>";
	}	
	else{
		echo "<script>alert('回收失败！');</script>";
	}
}
else if($editType == "deleteTerminal")
{
	$pin = $_POST["pin"];
	$sql = "DELETE FROM terminal WHERE pin=$pin";
	if($db->Execute($sql))
	{
		echo "<script>alert('删除成功！');</script>";
	}	
	else{
		echo "<script>alert('还存在依赖的帐单数据，暂时不能删除这台终端！');</script>";
	}
}
$db->Execute("UPDATE terminal t SET idle_duration = idle_duration + datediff(date(now()),date(idle_date))*3600*24+time_to_sec(timediff(time(now()),time(idle_date))),
idle_date = NOW() WHERE `t`.`restaurant_id` = 2 AND t.idle_date IS NOT NULL");
$db->Execute("UPDATE terminal t SET work_duration = work_duration + datediff(date(now()),date(work_date))*3600*24+time_to_sec(timediff(time(now()),time(work_date))),
work_date = NOW() WHERE (((`t`.`restaurant_id` > 10) and (date(now()) <= `t`.`expire_date`)) OR 
((`t`.`restaurant_id` > 10) and (date(now()) > `t`.`expire_date`))) AND work_date IS NOT NULL");
	//$db->Execute("CALL update_duration()");
	//mysql_query("call update_duration()");
	//$database="wireless_order_db";
	//$sa="root";
	//$pwd="li";
	//$ip="127.0.0.1";
	//$con1 = mysql_connect($ip,$sa,$pwd);
	//mysql_query("call update_duration()",$con1);

	//mysqli_query($con1,"call update_duration()");
	//mysql_close($con1);
//echo $sql;	
?>
<h1>
<span class="action-span"><a href="#" onclick="addTerminal('','','','','')">添加终端</a></span>
<span class="action-span1">e点通管理中心</span><span id="search_id" class="action-span2">  - 终端信息 </span>
<span id="terminalStat" class="action-span1" style="color:brown;margin-left:20px">空闲:23.5   使用:136.2  使用率:78.3%</span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="admin_terminal.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideCondition(this,true)">
	<option value="0">全部</option>
	<option value="is_pin">PIN</option>	
	<option value="is_restaurant_id">餐厅编号</option>
	<option value="is_restaurant_name">餐厅名称</option>
	<option value="is_owner_name">持有人</option>
	<option value="is_model_name">型号</option>
	<option value="is_expire_date">有效期</option>
	<option value="is_status">状态</option>
	</select>
	<select id="condition_type" name="condition_type" style="display:none">
	<option value="Equal">等于</option>
	<option value="EqualOrGrater" selected="selected">大于等于</option>
	<option value="EqualOrLess">小于等于</option>
	</select>
	</select>
	<select id="status_type" name="status_type" style="display:none;">
	<option value="使用" selected="selected">使用</option>
	<option value="空闲">空闲</option>
	<option value="废弃">废弃</option>
	<option value="过期">过期</option>
	</select>
    <!-- 关键字 -->
    <input type="text" id="keyword" name="keyword" style=" width: 137px;"/>
    <input type="submit" value=" 搜索 " class="button" />
  </form>
</div>

<div class="Content">


        
<table cellpModifying="0" cellspacing="0" border="0" id="table" class="sortable">
		<thead>
			<tr>
				<th><h3>PIN</h3></th>
				<th><h3>餐厅名称</h3></th>
				<th><h3>型号</h3></th>
				<th><h3>添加日期</h3></th>
				<th><h3>废弃日期</h3></th>
				<th><h3>使用(月)</h3></th>
				<th><h3>空闲(月)</h3></th>
				<th><h3>有效期</h3></th>
				<th><h3>持有人</h3></th>
				<th><h3>状态</h3></th>
				<th><h3>使用率</h3></th>
				<th><h3>操作</h3></th>
			</tr>
		</thead>
		<tbody>
		<?php 	  		
		include("conn.php"); 		 		
		$xm=$_REQUEST["keyword_type"];
		$ct=$_REQUEST["condition_type"];
		$st=$_REQUEST["status_type"];
		$kw=$_REQUEST["keyword"]; 	
		
		$sql = "SELECT * FROM terminal_view t INNER JOIN restaurant_view r ON t.restaurant_id=r.id WHERE 1=1";
		$total_work = 0;
		$total_idle = 0;
		switch ($xm)
		{
			case "is_pin":
				if ($kw!="")
					$sql .= " AND pin = '".strtoupper(base_convert($kw,16,10))."'" ;  			
				break;
			case "is_restaurant_id":
				if ($kw!="")
					$sql .= " AND restaurant_id=$kw" ;  				
				break;
			case "is_restaurant_name":
				if ($kw!="")
					$sql .= " AND r.restaurant_name like '%$kw%'" ;  				
				break;
			case "is_owner_name":
				if ($kw!="")
					$sql .= " AND owner_name like '%$kw%'" ;  				
				break;
			case "is_model_name":
				if ($kw!="")
					$sql .= " AND model_name like '%$kw%'" ;          		
				break;			
			case "is_expire_date":
				if ($kw!="")
				{
					if($ct == "Equal")
					{
						$sql .= " AND expire_date >= '" .$kw. " 0:0:0' AND expire_date <= '"  .$kw.  " 23:59:59'" ; 
					}
					elseif($ct == "EqualOrGrater")
					{
						$sql .= " AND expire_date >= '" .$kw. " 0:0:0'" ; 
					}
					elseif($ct == "EqualOrLess")
					{
						$sql .= " AND expire_date <= '" .$kw. " 23:59:59'" ; 
					}
				}			
				break;	
			case "is_status":
				if ($st!="")
					$sql .= " AND `status` = '$st'" ;  			
				break;						
		}			
		$bh=0;
		mysql_query("SET NAMES utf8"); 
		echo $sql;		
		$rs = $db->GetAll($sql);
		foreach ($rs as $row){
			$bh=$bh+1;
			$total_work += $row["work_duration"];
			$total_idle += $row["idle_duration"];
			$operation = "";
			$owner_name = str_replace('""',"",$row["owner_name"]);
			$Ex_date = $row["expire_date"];
			$Dis_date = $row["discard_date"];		
			if($row["status"] == "空闲" || $row["status"] == "废弃")
			{
				$Ex_date = "-";
			}
			if($row["status"] != "废弃")
			{
				$Dis_date = "-";	
			}		
			if($row["status"] == "使用" || $row["status"] == "过期")
			{
				$operation = "<a href='#' onclick='viewTerminal(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".$row["restaurant_id"]."&quot;,&quot;".$row["model_name"]."&quot;,&quot;".$Ex_date.
					"&quot;,&quot;".$row["status"]."&quot;,&quot;".$row["entry_date"]."&quot;,&quot;".$row["work_month"]."&quot;,&quot;".$row["idle_month"]."&quot;,&quot;".$Dis_date.
					"&quot;,&quot;".$row["use_rate"]."&quot;,&quot;".$owner_name.
					"&quot;)'><img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;查看</a>"."&nbsp;".
					"<a href='#' onclick='editUsingTerminal(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".$row["restaurant_id"]."&quot;,&quot;".$row["model_name"]."&quot;,&quot;".$Ex_date."&quot;,&quot;".$owner_name."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>"."&nbsp;".					
					"<a href='#' onclick='changeTerminalStatus(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;uninstallTerminal&quot;,&quot;卸载&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;卸载</a>";
			}
			if($row["status"] == "空闲")
			{
		$operation = "<a href='#' onclick='viewTerminal(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".$row["restaurant_id"]."&quot;,&quot;".$row["model_name"]."&quot;,&quot;".$Ex_date.
			"&quot;,&quot;".$row["status"]."&quot;,&quot;".$row["entry_date"]."&quot;,&quot;".$row["work_month"]."&quot;,&quot;".$row["idle_month"]."&quot;,&quot;".$Dis_date.
			"&quot;,&quot;".$row["use_rate"]."&quot;,&quot;".$owner_name.
			"&quot;)'><img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;查看</a>"."&nbsp;".
			"<a href='#' onclick='editTerminal(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".$row["model_id"]."&quot;,&quot;".$row["model_name"]."&quot;,&quot;".$owner_name."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>"."&nbsp;".				
					"<a href='#' onclick='installTerminal(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".$row["restaurant_id"]."&quot;,&quot;".date("Y-m-d")."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;挂载</a>"."&nbsp;".
					"<a href='#' onclick='changeTerminalStatus(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;discardTerminal&quot;,&quot;废弃&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;废弃</a>";
			}
			if($row["status"] == "废弃")
			{
		$operation = "<a href='#' onclick='viewTerminal(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".$row["restaurant_id"]."&quot;,&quot;".$row["model_id"]."&quot;,&quot;".$row["model_name"]."&quot;,&quot;".$Ex_date.
					"&quot;,&quot;".$row["status"]."&quot;,&quot;".$row["entry_date"]."&quot;,&quot;".$row["work_month"]."&quot;,&quot;".$row["idle_month"]."&quot;,&quot;".$Dis_date.
					"&quot;,&quot;".$row["use_rate"]."&quot;,&quot;".$owner_name.
					"&quot;)'><img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;查看</a>"."&nbsp;".
					"<a href='#' onclick='editTerminal(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;".$row["model_name"]."&quot;,&quot;".$owner_name."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>"."&nbsp;".								
					"<a href='#' onclick='changeTerminalStatus(&quot;".strtoupper(base_convert($row["pin"],10,16))."&quot;,&quot;recycleTerminal&quot;,&quot;回收&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;回收</a>"."&nbsp;".
					"<a href='#' onclick='deleteTerminal(&quot;".$row["pin"]."&quot;)'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a>";
			}
			
			$Dis_date = $row["discard_date"];
			if($Dis_date == null)
			{
				$Dis_date = "-";
			}
			$use_rate = "-";
			if($row["use_rate"] != null)
			{
				$use_rate = $row["use_rate"]."%";
			}
			
			echo "<tr>";
			echo "<td>" .strtoupper(base_convert($row["pin"],10,16)) ."</td>";
			echo "<td>"."<a href='#' onclick='viewRestaurant(&quot;".$row["id"]."&quot;,&quot;".$row["account"]."&quot;,&quot;".$row["restaurant_name"]."&quot;,&quot;".($row["record_alive"]/24/3600)."&quot;,&quot;".$row["order_num"]."&quot;,&quot;".$row["terminal_num"]."&quot;,&quot;".$row["food_num"]."&quot;,&quot;".$row["table_num"]."&quot;,&quot;".$row["order_paid"]."&quot;,&quot;".$row["table_using"]."&quot;)'>".
				$row["restaurant_name"]."（".$row["id"]."）</a></td>";	
			echo "<td>" .$row["model_name"] ."</td>";
			echo "<td>" .$row["entry_date"] ."</td>";
			echo "<td>" .$Dis_date ."</td>";
			echo "<td>" .$row["work_month"] ."</td>";
			echo "<td>" .$row["idle_month"] ."</td>";
			echo "<td>" .$Ex_date ."</td>";
			echo "<td>" .$owner_name ."</td>";
			echo "<td>" .$row["status"] ."</td>";
			echo "<td>" .$use_rate ."</td>";
			echo "<td>$operation</td>";			
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
    echo "<input type='hidden' id='status_type_value' value='$st' />";
    echo "<input type='hidden' id='keyword_value' value='$kw' />";
    echo "<script>statTerminal($total_work,$total_idle)</script>";
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
	ininTerminalOriginal();
  </script>
</body>
</html>
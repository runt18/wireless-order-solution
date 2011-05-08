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
<script type="text/javascript" src="js/member.js"></script>
<script type="text/javascript" src="js/date.js"></script>
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
if($editType == "addMember" || $editType == "editMember")
{
	$alias_id = $_POST["alias_id"];
	$name = $_POST["name"];
	$birth = $_POST["birth"];
	$tele = $_POST["tele"];
	$exchange_rate = $_POST["exchange_rate"];
	$discount_type = $_POST["discount_type"];
	
	$validate = true;
	if($editType == "addMember")
	{		
		$sql = "SELECT * FROM member WHERE alias_id=$alias_id AND restaurant_id=" . $_SESSION["restaurant_id"];				
		$rs = $db ->GetOne($sql);		
		if($rs)
		{			
			echo "<script>alert('已存在此编号的会员，请输入其它编号！');editMember('','$alias_id','$name','$birth','$tele','$exchange_rate','$discount_type');</script>";
			$validate = false;
		}
		else
		{
			$sql = "INSERT INTO member(restaurant_id,alias_id,name,birth,tele,exchange_rate,discount_type) VALUES(".$_SESSION["restaurant_id"].",$alias_id,'$name','$birth','$tele',$exchange_rate,$discount_type)";
		}
	}
	else
	{
		$id = $_POST["id"];				
		$sql = "UPDATE member SET alias_id=$alias_id, name='$name',birth='$birth',tele='$tele',exchange_rate=$exchange_rate,discount_type=$discount_type WHERE id=$id";		
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
else if($editType == "deleteMember")
	{
		$id = $_POST["id"];
		
		if($db->Execute("DELETE FROM member WHERE id=$id"))
		{			
			echo "<script>alert('删除成功！');</script>";
		}	
		else{
			echo "<script>alert('删除失败！');</script>";
		}	
	}
	else if($editType == "recharge")
		{
			$id = $_POST["id"];	
			$money = $_POST["money"];		
			$sql1 = "INSERT INTO member_charge(member_id,date,money) VALUES($id,NOW(),$money)";	
			$sql2 = "UPDATE member SET balance=balance+$money WHERE id=$id";
			
			if($db->Execute($sql1) && $db->Execute($sql2))
			{			
				echo "<script>alert('冲值成功！');</script>";
			}	
			else{
				echo "<script>alert('冲值失败！');</script>";
			}	
		}
		else if($editType == "viewDetail")
			{
				
				$viewType = $_POST["viewType"];
				$id = $_POST["id"];
				$name = $_POST["name"];
				$dateFrom = $_POST["dateFrom"];
				$dateTo = $_POST["dateTo"];
				if($viewType == "expenditure")		
				{					
					echo "<script>viewExpenditure('$id','$name','$dateFrom','$dateTo');</script>";
					//echo "<script>viewExpenditure(&quot;".$id."&quot;,&quot;".$name."&quot;,&quot;".$dateFrom."&quot;,&quot;".$dateTo."&quot;);</script>";
				}
				else if($viewType == "recharge")		
					{
						echo "<script>viewRecharge('$id','$name','$dateFrom','$dateTo');</script>";
					}
			}
?>
<h1>
<span class="action-span"><a href="#" onclick="editMember('','','','','','','')">添加会员</a></span>
<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2">&nbsp;- 会员管理 </span>
<div style="clear:both"></div>
</h1>
<div class="form-div">
  <form action="member.php"  method="get">
    <!-- 搜索条件 -->
    <div class="font" style="color:#2a7d8d;font-size:15px;font-weight:bold;text-align:right;">过滤：</div>
    <select id="keyword_type" name="keyword_type"  onchange="showHideCondition(this)">
		<option value="0">全部</option>
		<option value="alias_id">编号</option>
		<option value="name">姓名</option>
		<option value="tele">电话</option>
		<option value="discount_type">折扣方式</option>
		<option value="balance">余额</option>
		<option value="expenditure">消费额</option>
	</select>
	<select id="condition_type" name="condition_type" style="display:none">
	<option value="Equal">等于</option>
	<option value="EqualOrGrater" selected="selected">大于等于</option>
	<option value="EqualOrLess">小于等于</option>
	</select>
	<select id="discount_type_select" name="discount_type_select" style="display:none">
	<option value="0" selected="selected">折扣方式1</option>
	<option value="1">折扣方式2</option>	
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
				<th><h3>出生日期</h3></th>				
				<th><h3>电&nbsp;话</h3></th>
				<th><h3>折扣方式</h3></th>				
				<th><h3>余额（￥）</h3></th>				
				<th><h3>兑换折扣</h3></th>
				<th><h3>消费额（￥）</h3></th>
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
$sql = "SELECT id,alias_id,name,birth,tele,discount_type,balance,exchange_rate,CASE WHEN expenditure IS NULL THEN 0.00 ELSE expenditure END as expenditure FROM member a"
	." LEFT JOIN (SELECT member_id,SUM(total_price) AS expenditure FROM `order_history` GROUP BY member_id) AS b ON a.alias_id = b.member_id WHERE restaurant_id=" . $_SESSION["restaurant_id"];			
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
	case "tele":
		if ($kw!="")
			$sql .= " AND tele = $kw" ;  				
		break;		
	case "discount_type":
		$sql .= " AND discount_type = $discount_type_select" ;  
		break;		
	case "balance":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND balance = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND balance >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND balance <= $kw" ; 
			}
		}				
		break;		
	case "expenditure":
		if ($kw!="")
		{
			if($ct == "Equal")
			{
				$sql .= " AND expenditure = $kw" ;  
			}
			elseif($ct == "EqualOrGrater")
			{
				$sql .= " AND expenditure >= $kw" ; 
			}
			elseif($ct == "EqualOrLess")
			{
				$sql .= " AND expenditure <= $kw" ; 
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
	echo "<td>" .$row["name"];
	if($row["birth"] == date("Y-m-d"))
	{
		echo "&nbsp;<img src='images/birth.png'  height='16' width='16' border='0'/>";
	}	
	echo "</td>";
	echo "<td>" .$row["birth"];
	
	echo"</td>";	
	echo "<td>" .$row["tele"] ."</td>";	
	echo "<td>" .GetDiscountTypeName($row["discount_type"]) ."</td>";	
	echo "<td>" .$row["balance"] ."</td>";
	echo "<td>" .$row["exchange_rate"] ."</td>";	
	echo "<td>" .$row["expenditure"] ."</td>";		
	echo "<td><a href='#' onclick='editMember(&quot;".$row["id"]."&quot;,&quot;".$row["alias_id"]."&quot;,&quot;".$row["name"]."&quot;,&quot;".$row["birth"]."&quot;,&quot;".$row["tele"]."&quot;,&quot;".$row["exchange_rate"].
		"&quot;,&quot;".$row["discount_type"]."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;修改</a>&nbsp;&nbsp;&nbsp;&nbsp;" .
		"<a href='#' onclick='recharge(&quot;".$row["id"]."&quot;,&quot;".$row["name"]."&quot;)'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;冲值</a>&nbsp;&nbsp;&nbsp;&nbsp;".
		"<a href='#' onclick='deleteMember(".$row["id"].")'><img src='images/del.png'  height='16' width='14' border='0'/>&nbsp;删除</a>&nbsp;&nbsp;&nbsp;&nbsp;".
		"<a href='#' onclick='viewDetail(&quot;".$row["alias_id"]."&quot;,&quot;".$row["name"]."&quot;)'><img src='images/View.png'  height='16' width='14' border='0'/>&nbsp;明细</a></td>";
	echo "</tr>";
}
//mysql_query("SET NAMES utf8"); 
mysql_close($con);
function GetDiscountTypeName($discount_type)
{
	if($discount_type == 0)
	{
		return "会员折扣1";
	}
	else if($discount_type == 1)
		{
			return "会员折扣2";
		}
	return "";
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
	initializeMember();
  </script>
</body>
</html>
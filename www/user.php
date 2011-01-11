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
<script type="text/javascript" src="js/user.js"></script>
</head>
<body>
<?php
include("changePassword.php"); 
?>
<h1>

<span class="action-span1">e点通会员中心</span><span id="search_id" class="action-span2"> - 终端信息 </span>
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
				<th><h3>操&nbsp;作</h3></th>
			</tr>
		</thead>
		<tbody>
           <?php 
           //header('content-Type=text/html;charset=GB2312');//表头定义gb2312
           include("conn.php"); 
           $restaurant_id = $_SESSION["restaurant_id"];
           $bh=0;
           $sql = "SELECT model_name,owner_name,pin,expire_date FROM `terminal` WHERE restaurant_id=$restaurant_id ORDER BY restaurant_id" ;  
           
           //mysql_query("SET NAMES GBK"); 

           // mysql_query("set names 'utf-8'") ;
           $rs = $db->GetAll($sql);
           foreach ($rs as $row){
           	$bh=$bh+1;
           	echo "<tr>";
           	echo "<td>" .$bh ."</td>";
           	echo "<td>" .$row[0] ."</td>";
			echo "<td>" .$row[1] ."</td>";
           	echo "<td>" .strtoupper(base_convert($row[2],10,16)) ."</td>";
           	echo "<td>" .$row[3] ."</td>";
			echo "<td><a href='#' onclick='setOwner()'><img src='images/Modify.png'  height='16' width='14' border='0'/>&nbsp;设定持有人</a>";
           	echo "</tr>";
           }
           //mysql_query("SET NAMES utf8"); 
           mysql_close($con);
           ?>
        
	<!--
    		<tr>
				<td>1</td>
				<td>黑莓8888</td>
				<td>45678358978</td>
				<td>2010年08月28日</td>
				
			</tr>
	-->
			
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
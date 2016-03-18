<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<% 
	float v = 1.1f; 
	response.setHeader("Pragma","No-cache"); 
	response.setHeader("Cache-Control","no-cache"); 
	response.setDateHeader("Expires", 0);  
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>智易点餐系统</title>
<!-- 客户端页面缓存清理 -->
<meta http-equiv="pragma" content="no-cache"> 
<meta http-equiv="cache-control" content="no-cache"> 
<meta http-equiv="expires" content="0">	
<!-- 样式 -->
<link rel="stylesheet" href="css/common/jquery.mobile-1.3.2.css">
<!-- keyboard -->
<link rel="stylesheet" href="css/keyboard/keyboard.css">
<!-- 自定义样式 -->
<link rel="stylesheet" href="css/takeout/login.css?v=<%=v %>">

<script type="text/javascript" src="../jquery/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/common/jquery.mobile-1.3.2.min.js"></script>
<script type="text/javascript" src="../js/components/md5.js"></script>
<!-- 共享数据 -->
<script type="text/javascript" src="js/global/share.js?v=<%=v %>"></script>
<!-- 获取systemStatus -->
<script type="text/javascript" src="js/global/systemStatus.js?<%=v %>"></script>
<!-- 分页控件 -->
<script type="text/javascript" src="./js/padding/padding.js"></script>
<!--keyboard  -->
<script type="text/javascript" src="js/keyboard/keyboard.js"></script>
<!-- 工具类 -->
<script type="text/javascript" src="js/Util.js?v=<%=v %>"></script>
<!-- 自定义js -->
<script type="text/javascript" src="js/login/login.js?v=<%=v %>"></script>


<meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
</head>

<body>

<div data-role="page" id="restaurantLoginPage" align="center" data-theme="e">
	<!-- 智易广告 start-->
	<div data-role="header" data-tap-toggle="false" data-theme="b" class="advertisementCmp">
		<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal">
			<div class="adv_logo">
				<img src="images/logo.png">
			</div>
			<div class="adv_msg">
				<img src="images/advertisement.png" height="40">
			</div>
		</div>
		<div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
			<div >
				<img src="images/contact.png">
			</div>
		</div>		
	</div>	
	<!-- end 智易广告 -->

	<!-- 餐厅登陆面板 -->
	<div data-role="content" id="loginRestaurantCmp" class="ui-corner-all lgRestCmp" data-theme="a">
        <div class="div4Input" align="center" >
            <input type="text" id="txtRestaurantAccount" autofocus placeholder="餐厅账号" class="numkeyboard">
        </div>
        <div class="div4Input" align="center" >
            <input type="text" id="txtRestaurantDynamicCode" placeholder="输入验证码( 请联系客服获取 )" class="numkeyboard" onkeypress="intOnly(this)">
        </div>	        
         <a onclick="restaurantLoginHandler()"><img src="images/login.png"></a>
	</div>		
	
</div>

<div id="staffLoginPage" data-role="page" align="center" data-theme="e">
	<!-- 智易广告 start-->
	<div data-role="header" data-tap-toggle="false" data-theme="b" class="advertisementCmp">
		<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal">
			<div class="adv_logo">
				<img src="images/logo.png">
			</div>
			<div class="adv_msg">
				<img src="images/advertisement.png" height="40">
			</div>
		</div>
		<div id="headDisplayBillboard" data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
			<div id="divDisplayBillboard" class="bb_btn">
				<a href="#billboardsCmp" id="btnDisplayBillboard" data-rel="popup" data-role="button" data-inline="true"  data-transition="slideup" data-icon="info" data-theme="e" style="display: none;">公告</a>
			</div>
			<div class="adv_logo">
				<img src="images/contact.png">
			</div>
		</div>	
	</div>
	<!-- end 智易广告-->
	
	<!-- 公告列表 -->
	<div data-role="popup" id="billboardsCmp" data-theme="d">
	        <ul id="billboardList" data-role="listview" data-inset="true" style="min-width:210px;" data-theme="d"></ul>
	</div>	
	
	<!-- 员工列表 -->
     <div id="selectStaffCmp" data-role="popup" data-theme="c" class="ui-corner-all selectStaffCmp">
		<div data-role="header" data-theme="b" class="ui-corner-top">
			<h1>选择员工登陆</h1>
		</div>   
		<div data-role="content" id="divAllStaffForUserLogin" class="content" align="center"></div>  
			<!-- <a href="#" data-role="button" data-inline="true" class="loginName" onclick="selectedName(this)" data-value="1" data-theme="c">波风水门<br>(楼面主任)</a> -->
			
		<div id="staffPaddingBar" data-role="footer" data-tap-toggle="false" data-theme="b" class="bar">
			 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
				<a onclick="lg.staffPaging.prev()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="staffsPaging">L</a>
				<a onclick="lg.staffPaging.next()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="staffsPaging">R</a>				
			 </div>
		</div>			
     </div>
     
	<!-- 公告列表 -->
     <div id="billboardCmp" data-role="popup" data-dismissible="false"  data-theme="c" class="ui-corner-all billboardCmp">
		<div data-role="header" data-theme="b" class="ui-corner-top win_head">
			<span id="billboardTitle"></span>
		</div>   
		<div class="fixedHeight">
			<div data-role="content" id="billboardDesc" class="bb_content" align="left"></div>  
		</div>			
		<div id="staffPaddingBar" data-role="footer" data-tap-toggle="false" data-theme="b" class="bb_bar">
			 <div data-role="controlgroup" class="ui-btn-left" data-type="horizontal">
				<a data-role="button" data-inline="true" class="btnBillboard" onclick="lastBillboard()">上一条</a>
				<a data-role="button" data-inline="true" class="btnBillboard" onclick="nextBillboard()">下一条</a>
			 </div>		
			 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
				<a data-role="button" data-inline="true" class="btnBillboard" onclick="knowedBillboard()">我知道了</a>
			 </div>
		</div>			
     </div>     
     
	<!-- 员工登陆面板 -->
     <div id="loginStaffCmp" class="ui-corner-all loginStaffCmp">
		<div data-role="header" data-theme="b" class="ui-corner-top">
			<font id="lab4RestaurantName" class="lgStaff_title"></font>
		</div>    	
        <div class="lgStaff_content" >
           	<a onclick="openStaffSelectCmp()" data-role="button" data-icon="arrow-r" data-iconpos="right" data-inline="true" data-rel="popup" data-transition="pop" data-theme="b">选择员工</a>
  				<label id="lab4StaffName" class="sName0Select"></label>
            <input type="password"  name="pass" id="loginPassword" value="" placeholder="密码" data-theme="a" class="numkeyboard">
            <!-- 触摸屏 -->
            <div id="btnLogin4Touch" style="display: none;">
	            <button type="button" data-theme="b" data-icon="check" onclick="staffLoginHandler()">登陆</button>
            </div>
            <!-- pos端 -->
            <div id="btnLogin4Pos" style="display: none;">
	            <button type="button" data-theme="b" data-inline="true" data-icon="home" onclick="staffLoginHandler({part:'basic'})">系统后台</button>
	            <button type="button" data-theme="b" data-inline="true" data-icon="check" onclick="staffLoginHandler()">前台点菜</button>
            </div>
        </div>
        <div style="position:absolute;bottom:15px;left:10px;"><a href="javascript:void(0)" onclick="window.open('http://wx.e-tones.net/manual/digie_manual.html')">智易系统帮助文档</a></div>
	</div>
</div>

</body>
<script src="http://code.54kefu.net/kefu/js/b150/852550.js" type="text/javascript" charset="utf-8"></script>	
</html>

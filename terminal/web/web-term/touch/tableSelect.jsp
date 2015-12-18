<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<% 
	float v = 1.8f; 
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

<meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
<!-- jquery mobile -->
<link rel="stylesheet" href="css/common/jquery.mobile-1.3.2.css">
<!-- 日期插件.css -->
<link rel="stylesheet" type="text/css" href="css/dateCmp/datebox.css" />
<!-- 时分插件.css -->
<link rel="stylesheet" type="text/css" href="css/timepicki.css" />
<!-- 数字键盘样式 -->
<link rel="stylesheet" href="css/calculate/datouwang.css">
<!-- 自定义样式 -->
<link rel="stylesheet" href="css/takeout/main.css?v=<%=v %>">
<link rel="stylesheet" href="css/table.css?v=<%=v %>">

<script type="text/javascript" src="../jquery/jquery-1.8.2.min.js"></script>
<!-- 解决jqm1.3.2的popup问题 -->
<script type="text/javascript">
	//Override of $.fn.animationComplete must be called before initialise jquery mobile js
	$(document).on('mobileinit', function() {
	    $.fn.animationComplete = function(callback) {
	      if ($.support.cssTransitions) {
	    	  var superfy = "WebKitTransitionEvent" in window ? "webkitAnimationEnd" : "animationend";
	          return $(this).one(superfy, callback);
	      }else{
	          setTimeout(callback, 0);
	          return $(this);
	      }
	    };
	});
</script>
<script type="text/javascript" src="js/common/jquery.mobile-1.3.2.min.js"></script>
<!-- 日期插件.js -->
<script type="text/javascript" src="js/common/datebox.core.js"></script>
<script type="text/javascript" src="js/common/jqm.datebox.language-CN.js"></script>
<!-- 时分插件.js -->
<script type="text/javascript" src="js/book/timepicki.js"></script>
<!-- 手写板控件 -->
<script type="text/javascript" src="js/handWriting/handWriting.js?v=<%=v %>"></script>
<!-- jqm弹出框控件 -->
<script type="text/javascript" src="js/popup/jqmPopup.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/Util.js?v=<%=v %>"></script>
<script type="text/javascript" src="../extjs/wireless.ux.js"></script>
<script type="text/javascript" src="js/orderFood/orderFood.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/checkOut/checkOut.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/stopSet/stopSet.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/payment/payment.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/tableSelect/tableSelect.js?v=<%=v %>"></script>
<!-- 共享数据 -->
<script type="text/javascript" src="js/global/share.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/global/tables.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/global/foods.js?v=<%=v %>"></script>
<script type="text/javascript" src="js/global/staff.js?v=<%=v %>"></script>
<!-- 分页控件 -->
<script type="text/javascript" src="./js/padding/padding.js"></script>
<!-- 数字键盘控件 -->
<script type="text/javascript" src="js/numKeyBoard/numKeyBoard.js?v=<%=v %>"></script>
<!-- 餐台选择控件 -->
<!-- <script type="text/javascript" src="./js/popup/table/askTable.js"></script> -->
<!-- JqmPopup弹出控件 -->
<script type="text/javascript" src="./js/popup/jqmPopup.js?v=<%=v %>"></script>
<!-- issuePopup弹出控件 -->
<script type="text/javascript" src="./js/popup/coupon/issuePopup.js?v=<%=v %>"></script>
<!-- usepopup弹出控件 -->
<script type="text/javascript" src="./js/popup/coupon/usePopup.js?v=<%=v %>"></script>
<!-- 会员读取弹出控件 -->
<script type="text/javascript" src="./js/popup/member/read.js"></script>
<!--添加会员弹出控件 -->
<script type="text/javascript" src="./js/popup/member/add.js"></script>
<!-- 完善会员资料 -->
<script type="text/javascript" src="./js/popup/member/perfect.js"></script>
<!-- 打印机绑定 -->
<script type="text/javascript" src="./js/popup/print/print.js"></script>
<!-- 查台等控件绑定 -->
<script type="text/javascript" src="./js/popup/table/ask.js"></script>
<!-- 明细控件绑定 -->
<script type="text/javascript" src="./js/popup/detail/detail.js"></script>
<!-- 微信预定 -->
<script type="text/javascript" src="./js/popup/wxOrder/wxOrder.js"></script>
<!-- 混合结账 -->
<script type="text/javascript" src="./js/popup/mixedPay/mixedPay.js"></script>
<!--禁止触摸时选中文字  -->
<script type="text/javascript">
	document.onselectstart = function(){
		return false;	
	} 
</script>

</head>
<body>

<!-- 餐台选择 start -->
<div data-role="page" id="tableSelectMgr" data-theme="d">
	<div id="divPosOperation" data-role="header" data-theme="b" style="height: 44px;text-align: center;display: none;">
		<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal" style="margin-top: 2px;">
			<div style="float: left;">
				<img src="images/logo.png" height="40px">
			</div>
			<div style="float: left;">
				<img src="images/contact.png" height="40px">
			</div>
		</div>	
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="topBtnFont" onclick="ts.displayPrintConnection()">打印机诊断</a>
		 	<a data-role="button" data-inline="true" class="topBtnFont"  data-rel="popup" data-transtion="pop" href="#frontPageMemberOperation">会员</a>
		 	<a id="todayBill_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">账单</a>
		 	<a id="personSettle_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">交款</a>
			<a id="phraseSettle_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">交班</a>
			<a id="dailySettle_a_tableSelect" data-role="button" data-inline="true" class="topBtnFont">日结</a>
			<a data-role="button" data-inline="true" class="topBtnFont" id="btnToBasicPage" data-rel="popup" data-transtion="pop" href="#toBasicMgr">后台</a>
		 </div>
	</div>
	
	<div data-role="popup" id="frontPageMemberOperation" data-theme="d" class="payment_searchMemberType">
		<ul id="charge_searchMemberTypeCmp" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
			<li class="popupButtonList" id="searchMember_a_tableSelect"><a>会员查询</a></li>
			<li class="popupButtonList" id="addMember_a_tableSelect"><a >添加会员</a></li>
			<li class="popupButtonList" onclick="ts.member.openMemberChargeWin()"><a >会员充值</a></li>
			<li class="popupButtonList" onclick="ts.member.openMemberPointConsumeWin()"><a >积分消费</a></li>
			<li class="popupButtonList" onclick="ts.member.openMemberConsumeDetailWin()"><a >消费明细</a></li>
			<li class="popupButtonList" id="memberWxBind_li_tableSelect"><a>微信会员绑定</a></li>
			<li class="popupButtonList" id="fastIssue_a_tableSelect"><a >快速发券</a></li>
			<li class="popupButtonList" id="fastUse_a_tableSelect"><a >快速用券</a></li>
		</ul>
	</div>	
	
	<div data-role="popup" id="toBasicMgr" data-theme="d" class="payment_searchMemberType">
		<ul id="charge_searchMemberTypeCmp" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
			<li class="popupButtonList" ><a href="../pages/Mgr/DigieBasic.html?page=1" target="_blank" onclick="$('#toBasicMgr').popup('close')">菜谱管理</a></li>
			<li class="popupButtonList" ><a href="../pages/Mgr/DigieBasic.html?page=2" target="_blank" onclick="$('#toBasicMgr').popup('close')">打印设置</a></li>
		</ul>
	</div>		
	
   <div id="tableAndRegionsCmp" class="ui-grid-a" style="height: 590px;background-color: rgb(135, 206, 235);">
   	<!-- 餐台数据 -->
     <div id="divTableShowForSelect" class="ui-block-a" style="width: -webkit-calc(100% - 120px);width: -moz-calc(100% - 120px);width: -ms-calc(100% - 120px);width: -o-calc(100% - 120px);height: inherit;">
    	
     </div>
     <div class="ui-block-b" style="width: 120px;height: inherit;border-left: 1px solid white;">
    	<a id="labTableStatus" data-role="button" data-theme="e" data-inline="true" class="regionBtn" data-icon="arrow-d" data-iconpos="right" data-rel="popup"  data-transition="pop" href="#popupAllStatusCmp">全部台</a>
    	<div style="height: 510px;overflow-y: auto;overflow-x: hidden;">
    	<div id="divSelectRegionForTS">
	    	<!-- <a data-role="button" data-inline="true" class="regionBtn" onclick="">区域1</a> -->
    	</div>
   		</div>
     </div>
   </div>
   
	<div data-role="popup" id="popupAllStatusCmp" data-theme="d" >
        <ul data-role="listview" data-inset="true" style="min-width:100px;" data-theme="b">
            <li id="idleTable_li_tableSelect" class="tempFoodKitchen" data-icon="false"><a>空闲台( <label id="idleTableAmount_label_tableSelect" style="color: #f7c942;">0</label> )</a></li>
            <li id="busyTable_li_tableSelect" class="tempFoodKitchen" data-icon="false"><a>就餐台( <label id="busyTableAmount_label_tableSelect" style="color: #f7c942;">0</label> )</a></li>
            <li id="allTable_li_tableSelect" class="tempFoodKitchen" data-icon="false"><a>全部台</a></li>
        </ul>
	</div>	

	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div class="bottomGeneralBar">
			<div id="loginStaffName_div_tableSelect" style="float: left;margin-left: 10px;">操作人: ---</div>
			<div style="float: left;margin-left: 15px;">空闲台: <font id="idleTableAmount_font_tableSelect" color="green">--</font></div>
			<div style="float: left;margin-left: 15px;">就餐台: <font id="busyTableAmount_font_tableSelect" color="green">--</font></div>
			<div style="float: left;margin-left: 15px;">暂结台: <font id="tmpPaidTableAmount_font_tableSelect" color="green">--</font></div>
			<div id="divDescForTableSelect-padding-msg" style="float: right;margin-right: 20px;">共--项</div>
		</div>	
		 <a id="logout_a_tableSelect" data-role="button" data-inline="true" class="bottomBtnFont">注销</a>
		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="location.reload()" >刷新</a>
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="WxOrder_a_tableSelect">微定</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ts.bookListEntry()">
		 	<div>
		 		<!-- 预订单数 -->
		 		<div id="amount4Book" style="display:none;width:28px;height: 28px;border-radius: 14px;background-color: red;float: right;margin-top: -11px;line-height:28px;">0</div>
		 		预订
		 	</div>	
		 	</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="searchTable_a_tableSelect">查台</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="apartTable_a_tableSelect">拆台</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="tranTable_a_tableSelect">转台</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ts.stopSellMgr()">沽清</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#tableSelectOtherOperateCmp">更多</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="prevTablePage_a_tableSelect">上一页</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="nextTablePage_a_tableSelect">下一页</a>		 
		 </div>
	</div>

	 <!-- 餐台更多操作 -->
	<div data-role="popup" id="tableSelectOtherOperateCmp" data-theme="d">
        <ul data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
         	<li id="fastFood_li_tableSelect" class="tempFoodKitchen" style="display:none;"><a >快餐模式</a></li>
         	<li id="printBind_a_tableSelect" class="tempFoodKitchen"><a>打印机绑定</a></li>
         	<li class="tempFoodKitchen"  onclick="ts.displayFeastPayWin()"><a>酒席入账</a></li>
            <li class="tempFoodKitchen" onclick="ts.openMultiOpenTableCmp()"><a >多台开席</a></li>
            <li class="tempFoodKitchen" onclick="ts.openMultiPayTableCmp()"><a >拼台</a></li>
        </ul>
	</div>		
	
	<!-- 多台开席选台 -->of.addFood
	<div id="multiOpenTableCmp" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: 100px 0px 0px -200px;width:550px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
	       	 多台开席选台
        	<div style="float: right">
  				<a onclick="ts.closeMultiOpenTableCmp()" data-role="button" data-corners="false" class="popupWinCloseBtn4Book">X</a>      		
        	</div>		
	    </div>
	    
	    <div data-role="content" id="multiOpenTableHadChoose" style="height:100px" align="left">
	    
   		</div>	    
	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" id="multiTables_a_tableSelect">添加餐台</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.multiOpenTableOrderFood()">确定</a>		 
			 </div>
	    </div>	    	    
	</div>		
	
	<!-- 并台埋单 -->
	<div id="multiPayTableCmp" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: 100px 0px 0px -200px;width:550px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
	       	拼台(菜品将合并到第一张餐台)
        	<div style="float: right">
  				<a onclick="ts.closeMultiPayTableCmp()" data-role="button" data-corners="false" class="popupWinCloseBtn4Book">X</a>      		
        	</div>		
	    </div>
	    
	    <div data-role="content" id="multiPayTableHadChoose" style="height:100px" align="left">
	    
   		</div>	    
	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" id="combineTable_a_tableSelect">添加餐台</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.multiPayTableOrderFood()">确定</a>		 
			 </div>
	    </div>	    	    
	</div>		
	
	
	<!-- 远程诊断打印机 -->
	<div id="printerConnectionCmp" class="ui-overlay-shadow ui-corner-all" style="width:1020px;z-index: 1102;position: absolute; top: 150px; left: 50%; margin: -100px 0px 0px -500px;background-color: white;display: none;" align="center">
	    <div data-role="header" class="ui-corner-top ui-header ui-bar-b" style="height: 35px;">
	    		<div id="printerConnectionCmpTitle" style="float: left;line-height: 35px;margin-left: 10px;">
					远程打印机诊断, 共 <span id="printerConnectionCount" style="color: DarkOrange;">0</span> 台打印机在使用
	    		</div>
	        	<div style="float: right">
	  				<a onclick="ts.closePrintConnection()" data-role="button" data-corners="false" class="popupWinCloseBtn">X</a>      		
	        	</div>
	    </div> 	    
	    
	    <div data-role="content">
			<div class="ui-grid-a">
			    <div class="ui-block-a ui-bar-c" style="width: 100%;line-height: 45px;">
			    	<a href="javascript:void()" onclick="ts.displayPrintConnection()" style="color: blue;margin-right: 20px;">刷新</a>打印服务状态: 
			    	<span id="printerServiceState"> 未知 </span> 
				</div>
			</div><!-- /grid-a -->				
			<br> 
			<div style="max-height: 500px;overflow: auto;"> 
			<table id="printer" data-role="table" data-mode="columntoggle"  class="ui-body-d ui-shadow table-stripe ui-responsive">	
				<caption style="font-weight: bold;margin-bottom: 0;color: blue">打印机列表</caption>
				<tr>
					<th></th>
					<th >打印机</th>
					<th >驱动</th>
					<th >端口</th>
					<th >网关</th>
					<th >连接状态</th>
					<th >机盖</th>
					<th >卷纸</th>
					<th >切刀</th>
				</tr>
				<tbody id="printerConnectionList" >
<!-- 					<tr>
						<td>192.168.1.201</td>
						<td>中厨</td>
						<td>192.168.1.201</td>
						<td><a href="#printerPingErrorCmp" data-rel="popup" data-transition="pop">未连接(打印机不出单, 点击解决)</a></td>
						<td><a href="#">未安装(请联系客服)</a></td>
						<td><a href="#">未关闭(请盖好打印机)</a></td>
						<td><a href="#">已用完(请更换打印纸)</a></td>
						<td><a href="#">已损坏(请更换切刀)</a></td>
					</tr>	 -->
				</tbody>			
			</table>	
			</div>    
		</div>	
	</div>
	
	<!-- 打印服务没开时提示 -->
	<div id="printerServiceUnopenCmp" data-role="popup"  data-dismissible="false" style="width:900px;" class="ui-corner-all" align="center">
		<div data-role="header" class="ui-corner-top ui-header ui-bar-b" style="height: 35px;">
	   		<div style="float: left;line-height: 35px;margin-left: 10px;">
				打印服务检查
	   		</div>
	       	<div style="float: right">
	 				<a onclick="" data-rel="back" data-role="button" data-corners="false" class="popupWinCloseBtn">X</a>      		
	       	</div>
       	</div>	
       	<div data-role="content">
			<h2>所有打印机不出单:</h2>
				<p>屏幕右下角打开电脑菜单 -> 所有程序 -> e点通打印服务 -> 点击e点通打印服务即可</p>
				
			<img alt="" src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintError/openService.jpg">
		</div>
	</div>	
	
	<!-- 打印服务开两个时提示 -->
	<div id="printerServiceOpenTwiceCmp" data-role="popup"  data-dismissible="false" style="width:900px;" class="ui-corner-all" align="center">
		<div data-role="header" class="ui-corner-top ui-header ui-bar-b" style="height: 35px;">
	   		<div style="float: left;line-height: 35px;margin-left: 10px;">
				打印服务检查
	   		</div>
	       	<div style="float: right">
	 				<a onclick="" data-rel="back" data-role="button" data-corners="false" class="popupWinCloseBtn">X</a>      		
	       	</div>
       	</div>	
       	<div data-role="content">
			<h2>所有打印机出重单:</h2>
				<p>屏幕右下角打开电脑菜单 -> 所有程序 -> e点通打印服务 -> 卸载e点通打印服务即可</p>
			<img alt="" src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintError/closeService.jpg">	
		</div>
	</div>					
	
	<!-- ping不通时提示 -->
	<div id="printerPingErrorCmp" data-role="popup"  data-dismissible="false" style="width:900px;" class="ui-corner-all" align="center">
		<div data-role="header" class="ui-corner-top ui-header ui-bar-b" style="height: 35px;">
	   		<div style="float: left;line-height: 35px;margin-left: 10px;">
				网线检查
	   		</div>
	       	<div style="float: right">
	 				<a onclick="" data-rel="back" data-role="button" data-corners="false" class="popupWinCloseBtn">X</a>      		
	       	</div>
       	</div>	
       	<div data-role="content" style="max-height:650px;overflow: auto;">
			<h3>1, 检查当前打印机后面接网线的地方有没有黄色的灯亮着</h3>
			<img alt="" src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintError/theYellowLight.jpg">
			<h3>2, 检查交换机连上路由器的灯是否亮着</h3>
			<img alt="" src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintError/luyouqi.jpg">
		</div>
	</div>	
	
	<!-- 联系客服提示 -->
	<div id="printerDriverErrorCmp" data-role="popup"  data-dismissible="false" style="width:350px;" class="ui-corner-all" align="center">
		<div data-role="header" class="ui-corner-top ui-header ui-bar-b" style="height: 35px;">
	   		<div style="float: left;line-height: 35px;margin-left: 10px;">
				驱动失败
	   		</div>
	       	<div style="float: right">
	 				<a onclick="" data-rel="back" data-role="button" data-corners="false" class="popupWinCloseBtn">X</a>      		
	       	</div>
       	</div>	
       	<div data-role="content">
			<h3>请安装驱动, 或联系客服QQ: 850774706</h3>
		</div>
	</div>		
	
	
	<!-- 会员充值 -->
	<div id="memberChargeWin" class="ui-overlay-shadow ui-corner-all" style="width:900px;z-index: 1102;position: absolute; top: 40%; left: 50%; margin: -200px 0px 0px -450px;background-color: white;display: none;" align="center">	
	    <div data-role="header" data-theme="b" class="ui-corner-top ui-header ui-bar-b" style="line-height: 35px;">
	        	会员充值
	    </div> 
	    <table>
		    <tr>
		    	<td></td>
		    	<td colspan="3">
		    		<input placeholder="手机号/卡号/微信卡号" style="font-size:20px;font-weight: bold;" id="txtMemberCharge4Read">
		    	</td>
		    	<td>
		    		<a data-role="button" id="btnReadMember4Charge" class="numberInputStyle" data-theme="b" onclick="ts.member.readMemberByCondtion4Charge()">读取会员</a>
		    	</td>
		    	<td></td>
		    </tr>
	    	<tr>
	    		<td class="readMemberTd">账户余额:</td>
	    		<td class="readMemberTdValue"><label id="rd_numTotalBalance">----</label></td>    	
	    		<td class="readMemberTd">基础余额:</td>
	    		<td class="readMemberTdValue"><label id="rd_numBaseBalance">----</label></td>
	    		<td class="readMemberTd">赠送余额:</td>
	    		<td class="readMemberTdValue"><label id="rd_numExtraBalance">----</label></td>       			
	    	</tr>
	    	<tr>
	    		<td class="readMemberTd">实收金额:</td>
	    		<td><input id="rd_numPayMannerMoney" onkeypress="intOnly()" ></td>    
	    		
	    		<td class="readMemberTd">账户充额:</td>
	    		<td><input id="rd_numRechargeMoney" onkeypress="intOnly()"></td>  
	    		
	    		<td class="readMemberTd">收款方式:</td>
	    		<td class="selectionCmp">
	    			<select  id="rd_comboRechargeType" data-native-menu="false" >
						<option value="1">现金</option>
						<option value="2">刷卡</option>
					</select>	    		
	    		</td>
	    	</tr>	
	    	<tr>
	    		<td class="readMemberTd">会员名称:</td>
	    		<td class="readMemberTdValue"><label id="rd_txtMemberName">----</label></td>
	    		<td class="readMemberTd">会员类别:</td>
	    		<td class="readMemberTdValue"><label id="rd_txtMmeberType">----</label></td>   
	    		<td class="readMemberTd">性别:</td>
	    		<td class="readMemberTdValue"><label id="rd_txtMemberSex">----</label></td>     		   		
	    	</tr> 
	    	<tr>
	    		<td class="readMemberTd">手机:</td>
	    		<td class="readMemberTdValue"><label id="rd_numMemberMobileForRecharge">----</label></td>
	    		<td class="readMemberTd">实体卡号:</td>
	    		<td class="readMemberTdValue"><label id="rd_numMemberCardForRecharge">----</label></td>   
	    		<td class="readMemberTd">微信会员卡:</td>
	    		<td class="readMemberTdValue"><label id="rd_numWeixinMemberCard">----</label></td>     		   		
	    	</tr> 
	    	<tr>
	    		<td colspan="2">
					<label>
				        <input type="checkbox" id="chbPrintRecharge" data-theme="e" checked="checked">打印充值信息
				    </label>	    		
	    		</td>
	    		<td colspan="3" >
	    			<div id="td4ChbSendCharge" style="display: none;">
						<label >
					        <input type="checkbox" id="chbSendCharge" data-theme="e"><label id="lab4SendSms">发送短信</label>
					    </label>
				    </div>	    		
	    		</td>
	    	</tr>		    		    	
	    </table>
	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.member.rechargeControlCenter()">充值</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.member.closeMemberChargeWin()">取消</a>		 
			 </div>
	    </div>	
	</div>		
	
	<div data-role="popup" id="charge_searchMemberType" data-theme="d" class="payment_searchMemberType">
		<ul id="charge_searchMemberTypeCmp" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
			<li data-role="divider" data-theme="e" style="line-height: 30px;">选择号码来源:</li>
			<li  class="popupButtonList" onclick="ts.member.readMemberByCondtion4Charge(1)"><a >手机卡</a></li>
			<li  class="popupButtonList" onclick="ts.member.readMemberByCondtion4Charge(3)"><a >会员实体卡</a></li>
			<li  class="popupButtonList" onclick="ts.member.readMemberByCondtion4Charge(2)"><a >微信卡</a></li>
		</ul>
	</div>
	
	<!-- 积分消费 -->
	<div id="memberPointConsume" class="ui-overlay-shadow ui-corner-all" style="width:340px;z-index: 1102;position: absolute; top: 30%; left: 50%; margin: -100px 0px 0px -150px;background-color: white;display: none;" align="center">	
	    <div data-role="header" data-theme="b" class="ui-corner-top ui-header ui-bar-b" style="line-height: 35px;">
	        	会员积分消费
	    </div> 
	    <table>
	    	<tr>
		    	<td>
		    		<input placeholder="手机号/卡号/微信卡号" style="font-size:20px;font-weight: bold;" id="txtMember4PointConsume">
		    	</td>
		    	<td width="30px">
		    		<a data-role="button" id="btnReadMember4PointConsume" class="numberInputStyle" data-theme="b" onclick="ts.member.readMemberByCondtion4PointConsume()">读取会员</a>
		    	</td>
		    </tr>
	    	<tr>
	    		<td class="readMemberTd">会员名称:</td>
	    		<td class="readMemberTdValue"><label id="numMemberNameForConsumePoint">----</label></td>
	    	</tr>
	    	<tr>
	    		<td class="readMemberTd">会员类别:</td>
	    		<td class="readMemberTdValue"><label id="numMemberTypeForConsumePoint">----</label></td>   		   		
	    	</tr> 
	    	<tr id="tr_memberFirstTimeCharge">
	    		<td class="readMemberTd">当前积分:</td>
	    		<td class="readMemberTdValue"><label id="numMemberPointForConsumePoint">----</label></td>   
	    	</tr>	  
	    	<tr id="tr_memberFirstTimeChargePrint">
	    		<td class="readMemberTd">
	    		消费积分:</td>
	    		<td class="readMemberTdValue"><input id="numConsumePointForConsumePoint" onkeypress="intOnly()"></td>   
	    	</tr>	    	  
	    </table>
	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.member.memberPointConsumeAction()">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.member.closeMemberPointConsumeWin()">取消</a>		 
			 </div>
	    </div>	
	</div>		

	<div data-role="popup" id="pointConsume_searchMemberType" data-theme="d" class="payment_searchMemberType">
		<ul id="pointConsume_searchMemberTypeCmp" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
			<li data-role="divider" data-theme="e" style="line-height: 30px;">选择号码来源:</li>
			<li  class="popupButtonList" onclick="ts.member.readMemberByCondtion4PointConsume(1)"><a >手机卡</a></li>
			<li  class="popupButtonList" onclick="ts.member.readMemberByCondtion4PointConsume(3)"><a >会员实体卡</a></li>
			<li  class="popupButtonList" onclick="ts.member.readMemberByCondtion4PointConsume(2)"><a >微信卡</a></li>
		</ul>
	</div>	
		
	<!-- 系统共用会员绑定start -->
	<div id="loadMemberBind4Tableselect"></div>
	
	<!-- 会员消费明细  -->
	<div id="memberConsumeDetailWin" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 200px; left: 50%;width:1200px; margin: -100px 0px 0px -600px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top ui-header ui-bar-b" style="height: 35px;">
	    		<div id="memberConsumeDetailHead" style="float: left;line-height: 35px;margin-left: 10px;">
					<span style="line-height: 35px;float: left;margin-left: 5px;">
						会员消费明细 -- <font color="#f7c942">当日</font>
					</span>
	    		</div>
	        	<div style="float: right">
	  				<a onclick="ts.member.closeMemberConsumeDetailWin()" data-role="button" data-corners="false" class="popupWinCloseBtn">X</a>      		
	        	</div>
	    </div>     
	    <table style="width: 100%;border-bottom: 1px solid gray;">
	    	<tr>
	    		<td class="table_toolBarLab">操作类型:</td>	  
	    		<td class="table_toolBarCmp" style="width: 320px;">
					<fieldset data-role="controlgroup" data-type="horizontal">
						<label>
				        	<input type="radio" name="memberConsumeType" data-value="-1" value="on" checked="checked" onclick="ts.member.searchMemberDetail()">全部
				        </label>
				        <label>
				        	<input type="radio" name="memberConsumeType" data-value="1" value="on" onclick="ts.member.searchMemberDetail()">消费
				        </label>
				        <label>
				        	<input type="radio" name="memberConsumeType" data-value="2" value="on" onclick="ts.member.searchMemberDetail()">充值
				        </label>
				        <label>
				        	<input type="radio" name="memberConsumeType" data-value="3" value="on" onclick="ts.member.searchMemberDetail()">积分
				        </label>				        
				    </fieldset>				        		
	    		</td>
	    		<td class="table_toolBarLab" style="width: 150px;">手机号/会员名/卡号:</td>	  
	    		<td class="table_toolBarCmp">
	    			<input id="consumeDetail_memberName">		        		
	    		</td>		    		 		
	    		<td style="text-align: right;padding-right: 20px;">
	    			<a data-role="button" data-icon="search" data-iconpos="left" data-inline="true" data-theme="b" onclick="ts.member.searchMemberDetail()">搜索</a>
	    		</td>
	    	</tr>
	    </table>
	
	    <div data-theme="d" class="ui-corner-bottom ui-content">
	    	<div style="max-height: 480px; overflow-y: auto;">
			<table  data-role="table"  data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr" >
	         <thead>
		           <tr class="ui-bar-d">
		             <th style="width: 25px;"></th>
		             <th style="width: 100px;">账单号</th>
		             <th >消费时间</th>
		             <th >会员名称</th>
		             <th >会员类型</th>
		             <th >操作类型</th>
		             <th class="text_right">金额</th>
		             <th class="text_right">积分情况</th>
		             <th >操作人</th>
					 <th id="lab4CancelReasonOrComment" style="width: 250px;">备注</th>          
		           </tr>
	         </thead>
	         <tbody id="front_memberConsumeDetailBody">
	         	<tr>
	         		<td>1</td>
	         		<td >56487</td>
	         		<td >2014-12-12 18:03:56</td>
	         		<td >龙虾排骨</td>
	         		<td >充值</td>
	         		<td>黄炆,拼上</td>
	         		<td>8.88</td>
	         		<td>2.6</td>
	         		<td>管理员</td>
	         		<td>上菜太慢, 太辣</td>
	         	</tr>
	         </tbody>
	       </table>     
			</div>
	    </div>
	</div>			
	
	<!-- 酒席入账 -->
	<div id="feastPayWin" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 200px; left: 50%;width:400px; margin: -100px 0px 0px -200px;display: none;background-color: white;" align="center">
	    <div data-role="header" class="ui-corner-top" data-theme="b">
	        <h1>酒席入账</h1>
	    </div>
	    <div style="padding: 5px 10px;" >
		    <a data-rel="popup" data-role="button" data-transition="pop" data-theme="b" data-icon="plus" href="#popupDepartmentsCmp">添加入账部门</a>
		    <table id="feastPayWinTable">
<!-- 		    	<tr id="tr4Feast">
		    		<td><a data-role="button" data-theme="e" >现金</a></td>
		    		<td style="padding-right: 10px;"><input id="" class="mixPayInputFont numberInputStyle" onkeypress="intOnly()"></td>
		    		<td> <a data-role="button" data-for="" data-icon="delete" data-iconpos="notext" data-theme="b" data-iconshadow="false" data-inline="true" >D</a></td>
		    	</tr> -->
		    </table>
		    <div style="text-align: right;padding-right: 20px;font-size: 30px;color: green;">
    			总金额 : <label id="feastPayTotalPrice">0</label>
    		</div>
	    </div>
		<div data-role="footer" data-theme="b" class="ui-corner-bottom">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.doFeastOrder()">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.closeFeastPayWin()">取消</a>		 
			 </div>
	    </div>	    
	    
	 </div>	
	 
	<div data-role="popup" id="popupDepartmentsCmp" data-theme="d" >
        <ul id="departmentsListCmp" data-role="listview" data-inset="true" style="min-width:100px;" data-theme="b">
            <!-- <li class="popupButtonList " data-icon="false"><a onclick="">部门一</a></li> -->
        </ul>
	</div>		 
</div>
<!-- end 餐台选择  -->


<!-- 弹出框时, 挡住其他操作的div阴影 -->
<div id="shadowForPopup" style="z-index: 1101;opacity:0; position: absolute; top:0; left:0; width: 100%; height: 100%; background: #DDD;display: none;" ></div>

<!-- 预订订单列表start -->
<div data-role="page" id="bookOrderListMgr" data-theme="e">
	<div data-role="header" data-position="fixed" data-tap-toggle="false" data-theme="b" style="height: 40px;">
		<span id="" class="ui-btn-left" style="line-height: 40px;">
			预订列表
		</span>
	</div>
    <table style="width: 1024px;">
    	<tr>
    		<td style="width:10%;">
			    <div style="padding: 0 10px" align="left" >
					<fieldset data-role="controlgroup" data-type="horizontal" data-theme="b" style="width:400px;">
						<label>
				        	<input type="radio" name="bookDateType" data-type="bookDate_all" value="" checked="checked" onclick="ts.searchBookList()">全部
				        </label>
				        <label>
				        	<input type="radio" name="bookDateType" id="bookDate_today" value="" onclick="ts.searchBookList()">今天
				        </label>
				        <label>
				        	<input type="radio" name="bookDateType" id="bookDate_tomorrow" value="" onclick="ts.searchBookList()">明天
				        </label>
				        <label>
				        	<input type="radio" name="bookDateType" id="bookDate_afterday" value="" onclick="ts.searchBookList()">后天
				        </label>
				        <label>
				        	<input type="radio" name="bookDateType" id="bookDate_conditionday" value="" onclick="ts.openConditionDay()">自定义
				        </label>				        
				    </fieldset>
			    </div>	    		
    		</td>
    		<td id="conditionDayBegin" style="width:15%;display: none">
    			<input type="date" data-role="datebox" id="conditionDayBeginDay" data-type="neither" data-options='{"mode": "datebox"}' style="font-size: 12px;line-height: 30px;">
    		</td>
    		<td id="conditionDayEnd" style="width:15%;display: none;">
    			<input type="date" data-role="datebox" id="conditionDayEndDay" data-type="neither" data-options='{"mode": "datebox"}' style="font-size: 12px;line-height: 30px;">
    		</td>    		
    		<td style="width:15%;">
    			<input type="text" id="searchBookPerson" placeholder="姓名">
    		</td>
    		<td style="width:15%;">
    			<input type="text" id="searchBookPhone" placeholder="电话" onkeypress="intOnly()">
    		</td>
    		<td style="line-height: 20px;padding: 0 3px;width:9%;">
				<select  id="searchBookStatus" onchange="ts.searchBookList()" style="font-size: 20px;">
					<option value="-1">订单状态</option>
					<option value="1">待确认</option>
					<option value="2">待入座</option>
					<option value="3">已入座</option>
				</select>
    		</td>    		
<!--     		<td style="text-align: right;width:10%;">
    			<a data-role="button" onclick="ts.searchBookList()">搜索</a>
    		</td> -->
    	</tr>
    </table>	
	<div id="bookOrderListCmp" style="overflow-y: auto;">
	<table id="bookOrderLists" data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr">
         <thead>
           <tr class="ui-bar-d">
             <th style="width: 5px;"></th>
             <th style="width: 200px;">预订日期</th>
             <th >区域</th>
             <th style="width: 130px;">预订人</th>
             <th >预订电话</th>
			 <th style="width: 80px;">预订人数</th>
			 <th style="width: 110px;">状态</th>
			 <th style="width: 90px;">经手人</th>
			  <th style="width: 100px;">详情</th>
			 <th style="width: 230px;">操作</th>             
           </tr>
         </thead>
         <!-- 菜品列表 -->
         <tbody id="bookOrderListBody" style="font-weight:bold;color: blue">
<!--            <tr>
				<td>1</td>
				<td>2014-12-11 18:06:53</td>
				<td>大厅</td>
				<td>李先生</td>
				<td>13533464033</td>
				<td>8</td>
				<td><a href="#">查看</a></td>
				<td>未处理</td>
				<td>小明</td>
				<td>
					<div data-role="controlgroup" data-type="horizontal"><a href="#" data-role="button">确认</a><a href=""  data-role="button" >取消</a></div>					
				</td>
           </tr> -->
         </tbody>
    </table>
	</div>
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">

		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ts.bookListBack()">返回</a>
		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ts.refreshBookList()">刷新</a>
		 
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ts.addBookInfo({type:'add'})">添加</a>
			<a href="javascript: Util.to.scroll({content:'bookOrderListCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			<a href="javascript: Util.to.scroll({content:'bookOrderListCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>		 
		 </div>
	</div>	
	<!-- 入座选台 -->
	<div id="bookOperateChooseTable" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: 100px 0px 0px -200px;width:550px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
	       	 入座选台
        	<div style="float: right">
  				<a onclick="ts.closeBookOperateTable()" data-role="button" data-corners="false" class="popupWinCloseBtn4Book">X</a>      		
        	</div>		
	    </div>
	    
		<div class="ui-grid-a">
		    <div class="ui-block-a">
		    	<div class="ui-bar-e" >
				    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
				       	 已订餐台
				    </div>		    	
				    <div id="bookTableToChoose" class="ui-bar ui-bar-e" style="height:360px;text-align: left;">
				    </div>
			    </div>
		    </div>
		    <div class="ui-block-b">
		    	<div class="ui-bar-e" style="height:360px;text-align: left;">
			    	<div data-role="header" data-theme="b" class="ui-corner-top win_head" align="center">
				       	 入座餐台
				    </div>	
		    		<div data-role="content" id="bookTableHadChoose" style="height:  -webkit-calc(100% - 63px);height: -moz-calc(100% - 63px);height: -ms-calc(100% - 63px);height: -o-calc(100% - 63px);">
		    		</div>
					<div data-role="footer" data-theme="b" class="ui-corner-bottom">
						 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
							 <a  data-role="button" data-inline="true" class="countPopbottomBtn" id="bookChoose_a_tableSelect">添加餐台</a>
							 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ts.bookTableOrderFood()">入座</a>		 
						 </div>
				    </div>
		    	</div>
		    </div>
		</div>
	    	    
	</div>	
	
	<!-- 添加预订 -->
	<div id="addBookInfo" class="ui-overlay-shadow ui-corner-all" style="width:1000px;z-index: 1102;position: absolute; top: 20%; left: 50%; margin: -100px 0px 0px -450px;background-color: white;display: none;" align="center">	
	    <div data-role="header" data-theme="b" class="ui-corner-top ui-header ui-bar-b" style="line-height: 35px;">
	        <span id="title4AddBook">填写预订</span>	
        	<div id="btnCloseAddBook" style="float: right">
  				<a onclick="ts.closeAddBookInfo()" data-role="button" data-corners="false" class="popupWinCloseBtn4Book">X</a>      		
        	</div>			        	
	    </div> 
	    <div style="max-height: 600px;min-height: 225px;overflow-y: auto;">
	    <table>
	    	<tr>
	    		<td class="readMemberTd">预订日期:</td>
	    		<td><input type="date" data-role="datebox" id="add_bookDate" data-options='{"mode": "datebox"}' ></td>
	    		
	    		<td class="readMemberTd">预订时间:</td>
	    		<td id="add_bookTimeBox"><input id="add_bookTime" class="bookTime" ></td>
  	
	    		<td class="readMemberTd">预留时长(单位/分钟):</td>
	    		<td class="selectionCmp" style="line-height: 20px;padding: 0 3px;width: 100px;">
					<input id="cm_bookReserved" onkeypress="intOnly()" > 
	    		</td>  		  	
 			
	    	</tr>
	    	<tr>
	    		<td class="readMemberTd">预订人:</td>
	    		<td><input id="add_bookPerson" ></td>    
	    		
	    		<td class="readMemberTd">预订电话:</td>
	    		<td><input id="add_bookPhone" onkeypress="intOnly()" ></td>   
	    		
	    		<td class="readMemberTd">预订人数:</td>
	    		<td><input id="add_bookAmount" onkeypress="intOnly()" ></td>     	    	
	    	</tr>
	    	<tr>
	    		<td class="readMemberTd">经手人:</td>
	    		<td>
	    			<select  id="add_staff" style="font-size: 20px;">
						<!-- <option value="29">管理员</option> -->
					</select>	    		
	    		</td>    
	    		<td class="readMemberTd">类型:</td>
	    		<td>
	    			<select  id="cm_bookCate" style="font-size: 20px;">
						<option value="散台">散台</option>
						<option value="包间">包间</option>												
						<option value="婚宴">婚宴</option>
						<option value="寿宴">寿宴</option>
						<option value="满月">满月</option>
						<option value="年会">年会</option>
						<option value="周岁">周岁</option>			
						<option value="入学">入学</option>
						<option value="生日">生日</option>
					</select>	    		
	    		</td>    
	    		<td class="readMemberTd">订金:</td>
	    		<td><input id="add_bookMoney" onkeypress="intOnly()"></td>   
	    	</tr> 
	    
	    	<tr >
	    		<td colspan="6" id="box4BookTableList" style="display:none;">
	    			<table id="" data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr">
						<caption style="font-weight: bold;color: black;margin-bottom: initial;">预定餐台列表( 点击餐台可取消选择 ):</caption>
				         <tbody style="font-weight:bold;color: blue">
				         <tr >
	    					<td colspan="6" id="add_bookTableList">
	    					</td>
	    				</tr>	
				         </tbody>
				     </table>	
						    			
	    		</td>    
	    	</tr>	  
	    	<tr>
	    		<td colspan="6" id="box4BookOrderFoodList" style="display:none;">
					<table id="" data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr">
						<caption style="font-weight: bold;color: black;margin-bottom: initial;">预定菜列表:</caption>
				         <thead>
				           <tr class="ui-bar-d">
				             <th style="width: 5px;"></th>
				             <th style="width: 25%;">菜名</th>
				             <th >数量</th>
				             <th style="width: 15%;">口味</th>
				             <th >单价</th>
				           </tr>
				         </thead>
				         <!-- 菜品列表 -->
				         <tbody id="bookOrderFoodListBody" style="font-weight:bold;color: blue">
				         </tbody>
				     </table>	    		
	    		</td>
	    	</tr>
	    	
	    </table>
	    <br>
	    </div>
		<div id="footer4AddBook" data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" style="width:25%" id="addBookTable_a_tableSelect">添加餐台</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" style="width:25%" onclick="ts.toOrderFoodPage()">菜品预订</a>				 
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" style="width:25%" onclick="ts.commitAddBook()">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" style="width:24%" onclick="ts.closeAddBookInfo()">取消</a>		 
			 </div>
	    </div>	
	</div>		
		
</div>
<!-- end 预订订单列表 -->

<!-- 已点菜界面 start-->
<div data-role="page" id="orderFoodListMgr" data-theme="e">
	<div data-role="header" data-position="fixed" data-tap-toggle="false" data-theme="b" style="height: 40px;">
		<span id="divNorthForUpdateOrder" class="ui-btn-left" style="line-height: 40px;"></span>
	</div>
	<div id="orderFoodListCmp" style="overflow-y: auto;">
	<table id="orderFoodLists" data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr">
         <thead>
           <tr class="ui-bar-d">
             <th style="width: 5px;"></th>
             <th style="width: 25%;">菜名</th>
             <th >数量</th>
             <th style="width: 15%;">口味</th>
             <th >单价</th>
			 <!-- <th >总价</th> -->
			 <th style="width: 80px;">时间</th>
			 <th style="width: 230px;">操作</th>
			 <th style="width: 70px;">服务员</th>             
           </tr>
         </thead>
         <!-- 菜品列表 -->
         <tbody id="orderFoodListBody" style="font-weight:bold;color: blue">
<!--            <tr>
				<td>1</td>
				<td>利雅园虾饺</td>
				<td>1</td>
				<td>无口味</td>
				<td>100</td>
				<td>100.00</td>
				<td>2014-12-11 18:06:53</td>
				<td>
					<div data-role="controlgroup" data-type="horizontal">
					    <a href="#takeoutOrderDetail" data-role="button">退菜</a>
					    <a href="javascript:deleteOrder()"  data-role="button" >转菜</a>
					</div>					
				</td>
				<td>鸣人</td>
           </tr> -->
         </tbody>
       </table>
		</div>
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div  class="bottomGeneralBar">
			<div id="spanTotalPriceUO" style="float: left;margin-left: 10px;">消费总额:--</div>
			<!-- 暂结状态 -->
			<div id="spanToTempPayStatus" style="float: left;margin-left: 20px;"></div>
			<div id="divDescForUpdateOrder" style="float: right;margin-right: 20px;"></div>
		</div>	
		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="uo.cancelForUO()">返回</a>
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="ss" onclick="uo.goToCreateOrder()">点菜</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="uo.tempPayForUO()">暂结</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont none" id="payOrder_a_checkOut">结账</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="detail_a_tableSelect">明细</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#popupDiscountCmp">折扣</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="checkOutTranTable_a_tableSelect">转台</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="memberRead_a_orderFood">会员</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#updateFoodOtherOperateCmp" onclick="uo.openMoreOperate()">更多</a>
			<a href="javascript: Util.to.scroll({content:'orderFoodListCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			<a href="javascript: Util.to.scroll({content:'orderFoodListCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>		 
		 </div>
	</div>
	
	<div data-role="popup" id="popupDiscountCmp" data-theme="d" >
        <ul id="discountCmp" data-role="listview" data-inset="true" style="min-width:200px;" data-theme="b">
        	<li class="tempFoodKitchen"><a >八八折</a></li>
        </ul>
	</div>	
	 <!-- 账单更多操作 -->
	<div data-role="popup" id="updateFoodOtherOperateCmp" data-theme="d">
        <ul data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
            <li class="tempFoodKitchen" onclick="uo.showOperatePeople()"><a >人数</a></li>
            <li class="tempFoodKitchen" onclick="uo.openCommentOperate()"><a >修改备注</a></li>
            <li class="tempFoodKitchen" onclick="uo.tempPayForPrintAll()"><a >补打总单</a></li>
            <li class="tempFoodKitchen" onclick="uo.printDetailPatch()"><a >补打明细</a></li>
            <li class="tempFoodKitchen" id="allTrantable_li_tableSelect"><a >全单转菜</a></li>
            <li class="tempFoodKitchen" onclick="uo.allFoodHurried()"><a >全单催菜</a></li>
            <li class="tempFoodKitchen" onclick="uo.cancelTable()"><a >撤台</a></li>
            <li id="uo_btnCheckoutBook" class="tempFoodKitchen" onclick="ts.checkBookTable()"><a >查看预订</a></li>
        </ul>
	</div>		
	
     <!-- 单条已点菜更多操作 -->
	<div data-role="popup" id="orderFoodMoreOperateCmp" data-theme="d">
        <ul data-role="listview" id="orderFood_moreOpe" data-inset="true" style="min-width:100px;" data-theme="b">
            <li style="line-height: 40px;" id="btnWeighFood" onclick="uo.weighAction()"><a >称重</a></li>
            <li style="line-height: 40px;" id="btnGiftFood" onclick="uo.giftAction()"><a >赠送</a></li>
            <li style="line-height: 40px;" onclick="uo.hurriedFoodAction()"><a >催菜</a></li>
            <!-- <li style="line-height: 40px;" id="addPrint_li_tableSelect"><a>补打</a></li>  -->
        </ul>
	</div>		

	
	
	<!-- 修改备注 -->
	<div id="orderFoodCommentCmp" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: 100px 0px 0px -200px;min-width:400px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
	       	 修改备注
	    </div>
	    <table style="width:80%;">
	    	<tr>
	    		<td>
	    			<input id="inputUpdateComment" type="text" placeholder="备注" data-type="txt" class="countInputStyle">
	    		</td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('inputUpdateComment')">D</a>
					</div>		    		
	    		</td>
	    	</tr>
	    </table>
	    	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.saveComment()">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.closeComment()">取消</a>		 
			 </div>
	    </div>
	</div>		
	
	<div id="shadowForPopup" style="z-index: 1101;opacity:0; position: absolute; top:0; left:0; width: 100%; height: 100%; background: #DDD;display: none;" ></div>
	<div id="cancelFoodSet" class="ui-overlay-shadow ui-corner-all" style="max-width:650px;z-index: 1102;position: absolute; top: 150px; left: 45%; margin: -100px 0px 0px -250px;background-color: white;display: none;" align="center">	
	<!-- <div id="cancelFoodSet" data-role="popup" data-theme="c" data-dismissible="false" style="max-width:650px;" class="ui-corner-all" align="center"> -->
	    <div data-role="header" data-theme="b" class="ui-corner-top win_head">
	        	请确定退菜原因和退菜数量
	    </div>
		<div id="calculator4CancelFood" class="calculator" style="width: inherit;" align="center">
			<div class="top">
				<span class="clear" id="cancelAdd_span_checkOut">+</span>
				<span class="inputs">
					<input id="inputCancelFoodSet" class="numberInputStyle" >
				</span>
				<span class="clear" id="cancelReduce_span_checkout">-</span>
			</div>
		</div>	
		<!-- 退菜原因列表 -->
		<div style="max-height: 175px;overflow-y: auto;padding-top: 5px;">
	    	<div id="cancelFoodReasonCmp" style="width:inherit;" align="left" >
	    		<!-- <a data-role="button" data-inline="true" class="regionBtn" onclick="">无原因</a> -->
	    	</div>
    	</div>
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" >
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.cancelFoodAction()">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="uo.closeCancelFoodCmp()">取消</a>		 
			 </div>
	    </div>
	</div>		
	
</div>	
<!-- end 已点菜界面 -->

<!-- 点菜界面 start-->
<div data-role="page" id="orderFoodMgr" >
	<div class="ui-grid-a" style="width: 100%;background-color: skyblue;border-bottom:1px solid white;">
		<div class="ui-block-a" data-role="controlgroup" data-corners="false" style="width:102px;">
			 <a id="divNFCOTableBasicMsg" data-role="button" data-inline="true" style="font-size:20px;" class="tableBasicMsg">112<br>虞美人</a>
			 <a data-role="button" data-inline="true" class="tableStatus">已点菜</a>		 
		</div>
		<div class="ui-block-b" style="width: -webkit-calc(100% - 102px);width: -moz-calc(100% - 102px);width: -ms-calc(100% - 102px);width: -o-calc(100% - 102px);">
			<!-- 部门 -->
			 <div id="deptsCmp" data-role="controlgroup" data-type="horizontal">
				 <!--<a data-role="button" data-inline="true" class="deptKitBtnFont">全部部门</a>
 				<a href="#" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow left</a>
				<a href="#" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow right</a>	 -->				 	 
			 </div>		
			<!-- 厨房 -->
			 <div id="kitchensCmp" data-role="controlgroup" data-type="horizontal" >
<!-- 				 <a data-role="button" data-inline="true" class="deptKitBtnFont">全部厨房</a>
				<a href="#" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow left</a>
				<a href="#" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="deptKitBtnFontPage">Arrow right</a> -->					 	 
			 </div>					 		
		</div>		
	</div>

	<div id="orderFoodCenterCmp" class="ui-grid-a" style="height: 470px;overflow: hidden;">
	     <div class="ui-block-a" style="width: 35%;height: inherit;background-color: skyblue;">
		   <div class="ui-grid-a" style="height: inherit;">
		     <div id="divOrderFoodsCmp"  class="ui-block-a" style="width: -webkit-calc(100% - 99px);width: -moz-calc(100% - 99px);width: -ms-calc(100% - 99px);width: -o-calc(100% - 99px);height: inherit;overflow-y: auto;">
		     	<!--已点菜列表  -->
				<ul id="orderFoodsCmp" data-type="sdf" data-role="listview" data-theme="d" data-inset="true">
<!-- 				    <li data-icon="forFree">
				    	<a href="#">
					    	<h1>忒观音</h1>
					    	<span style="color:gray;">打包,加水,加热</span>
					    	<div>
					    		<span style="float: left;color: red;">叫起</span>
					    		<span style="float: right;">￥89 X <font color="green">1</font></span>
					    	</div>
					    	<br>
							<div ><ul data-role="listview" data-inset="false" class="div4comboFoodList">
						            <li>┕饮料<font color="blue"> —加大,中牌</font></li>
						            <li>┕水果<font color="blue"> —加辣</font></li>
						            <li>┕甜品<font color="blue"> —临时口味</font></li>
						    </ul></div>
					    
				    </a></li>
				    <li data-icon="">
				    	<a href="#">
					    	<h1>炒番茄</h1>
					    	<span style="color:gray;">加热</span>
					    	<div>
					    		<span style="float: left;color: red;">叫起</span>
					    		<span style="float: right;">￥89 X <font color="green">1</font></span>
					    	</div>
				    </a></li> -->
				</ul>		    	
		     </div>
		     <div class="ui-block-b" style="width: 99px;height: inherit;background-color: skyblue;border-left: 1px solid white;border-right: 1px solid white;" >
				 <div data-role="controlgroup" >
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foodAmountAdd_a_orderFood">数量+1</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foodAmountCut_a_orderFood">数量-1</a>	
					 <a href="#" data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foodAmountSet_a_orderFood">数量=</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="deleteFood_a_orderFood">删除</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="selectTaste_a_orderFood">口味</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="addTaste_a_orderFood">手写口味</a>
					 <a id="giftFoodOperate_a_orderFood" data-role="button" data-inline="true" class="orderOperBtn" style="display: none;" data-theme="b">赠送</a>	
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" id="foopHangUp_a_orderFood">叫起</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" data-rel="popup"  data-transition="pop" href="#orderFoodOtherOperateCmp">更多</a>
					 <!--<a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" onclick="operateOrderFoodTaste({type:1})">全单口味</a>
					 <a data-role="button" data-inline="true" class="orderOperBtn" data-theme="b" onclick="of.foodHangup({type : 1})">全单叫起</a>
					 <a onclick="foodCommonTasteLoad()" data-rel="popup" data-position-to="window" data-role="button" data-inline="true" data-transition="pop" data-theme="b">数字键盘</a> -->
					 
				 </div>		
		     </div>
		     <!-- 点菜更多操作 -->
			<div data-role="popup" id="orderFoodOtherOperateCmp" data-theme="d">
		        <ul data-role="listview" data-inset="true" style="min-width:130px;" data-theme="b">
		        	<li class="tempFoodKitchen" id="updateUnit_li_orderFood"><a>修改单位</a></li>	
		        	<li class="tempFoodKitchen" id="updatePrice_li_orderFood"><a>修改时价</a></li>
		            <li class="tempFoodKitchen" id="allFoodTaste_li_orderFood"><a >全单口味</a></li>
		            <li class="tempFoodKitchen" id="allFoodHangUp_li_orderFood"><a>全单叫起</a></li>
		        </ul>
			</div>		     
		   </div>			     
	     
	     </div>
	     
	     <!-- 菜品列表 -->
	     <div id="foodsCmp_div_orderFood" class="ui-block-b" style="width: 65%;background-color: skyblue;">

	     </div>			
	</div>
	
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div id="divDescForCreateOrde" class="bottomGeneralBar">
			<div style="float: left;margin-left: 20px;">总数量: --, 合计: ￥--.--</div>
			<div id="foodPagingDesc_div_orderFood" style="float: right;margin-right: 20px;">共0项, 第1/1页, 每页?项</div>
		</div>	
		
		<div id="normalOperateFoodCmp" style="height: 60px;padding-top: 25px;">		
			<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="orderFoodBack_a_orderFood">返回</a>
			 	<a onclick="Util.to.scroll({content:'divOrderFoodsCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			 	<a onclick="Util.to.scroll({content:'divOrderFoodsCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>
			 </div>
			 <div data-role="controlgroup" class="ui-btn-right" data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="addBookOrderFood" onclick="ts.bookFoodChooseFinish()">选好了</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="bookSeatOrderFood" onclick="ts.bookTableCommitOrderFood()">入座</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="multiOpenTable" onclick="ts.multiOpenTableCommitOrderFood()">多台开席</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="normalOrderFood_a_orderFood">下单</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="fastPay_a_orderFood">结账</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="brand_a_orderFood">牌号</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#orderMore_div_orderFood" id="orderFoodMore_a_orderFood">下单>></a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="aliasOrderFood_a_orderFood">助记码</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="handWriteBoard_a_orderFood">手写板</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="pinyinBoard_a_orderFood">拼音</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup" data-position-to="window" id="addTemp_a_orderFood" data-transition="pop" data-theme="b"  >临时菜</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="getPrevious_a_orderFood">上一页</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="getNextPage_a_orderFood">下一页</a>
<!-- 			 	<a onclick="of.foodPaging.getPreviousPage()" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="foodsPaging">L</a>
				<a onclick="of.foodPaging.getNextPage()" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="foodsPaging">R</a> -->	
			 </div>

		</div>
		<!--下单>>操作  -->	
		<div data-role="popup" id="orderMore_div_orderFood" data-theme="d">
	        <ul data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">
	       	    <li id="orderPay_li_orderFood" class="tempFoodKitchen"><a>下单并结账</a></li>
	            <li id="orderNotPrint_li_orderFood" class="tempFoodKitchen"><a >下单不打印</a></li>
	            <li id="orderPre_li_orderFood" class="tempFoodKitchen"><a >先送</a></li>       	     
	        </ul>
		</div>
		
		 
		 
	</div>
	
	<div id="orderFoodTasteCmp" data-role="popup" data-theme="c" data-dismissible="false" style="width:820px;" class="ui-corner-all" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top">
	    	<!-- 口味组 -->
	    	<div id="tasteGroupCmp" data-role="controlgroup" data-type="horizontal" >
	    			<!-- 文字外的div是为了不让字出现省略号... -->
<!-- 				<a onclick="chooseTaste({event: this, id: 2})" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index=0 data-value=2 data-theme="c">
					<div>
						口味1<br>￥10
					</div>
				</a> -->
			</div>	
	    </div>
	    <!-- 口味列表 -->
    	<div id="tastesCmp" class="ui-bar ui-bar-c" style="height:300px;" align="left">

    	</div>
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 48px;">
			<div class="bottomTastesBar">
				<div style="float: left;margin-left: 20px;">已选口味 : </div>
				<div id="divDescForChooseTaste" style="float: left;color: green;font-weight: bold;"></div>
				<div id="tastePagingDesc" style="float: right;margin-right: 20px;">共0项, 第1/1页, 每页?项</div>				
				
			</div>	
			 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal" >
			 	 <a  data-role="button" data-inline="true" class="tastePopTopBtn" onclick="of.ot.saveOrderFoodTaste()">确定</a>
				 <a  data-role="button" data-inline="true" class="tastePopTopBtn" onclick="of.ot.back()">取消</a>
				 <a  data-role="button" data-inline="true" class="tastePopTopBtn" onclick="tasteCmpPrePage()">上一页</a>
				 <a  data-role="button" data-inline="true" class="tastePopTopBtn" onclick="tasteCmpNextPage()">下一页</a>
<!-- 				 <a data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="tasteGroupPage" onclick="tasteCmpPrePage()">L</a>
				 <a data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="tasteGroupPage" onclick="tasteCmpNextPage()">R</a>	 -->	 
			 </div>
	    </div>
	</div>	 
	
	<!-- 套菜点菜 -->
	<div id="divComboFoodFloat" class="ui-overlay-shadow ui-corner-all commonTasteFloat" >
		<div data-role="header" data-theme="b" class="ui-corner-top" style="height: 50px">
    		<div id="comboFoodsGroupCmp" data-role="controlgroup" style="float: left;margin-left: 5px;">
    		
    		</div>
        	<div style="float: right">
  				<a onclick="closeComboFoodTasteUnit()" data-role="button" data-corners="false" class="popupWinCloseBtn4comboFood">X</a>      		
        	</div>			 
			 
	    </div>
	    
	    <div data-role="collapsible" data-mini="true" id="collapsibleComboFoodTaste" >
	    	<h3>常用口味</h3>
			<div id="divComboFoodTastes">
<!-- 				<a onclick="chooseOrderFoodCommonTaste({event: this, id: 2})" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index=0 data-value=2 data-theme="c">
					<div>
						口味1<br>￥10
					</div>
				</a> -->
			</div> 
	    </div>
	    <div data-role="collapsible" data-mini="true" id="collapsibleComboFoodMultiPrice">
	        <h3>选择单位</h3>
	        <div id="divComboFoodMultiPrices">
<!-- 				<a onclick="" data-role="button" data-corners="false" data-inline="true" class="multiPriceCmp" data-index=0 data-value=2 data-theme="c">
					<div>
						¥45/例
					</div>
				</a> -->
			</div> 
	    </div>
	    
	</div>		
	
	
	<!-- 拼音搜索 -->
	<div id="orderPinyinCmp" class="ui-overlay-shadow ui-corner-all pinyinWindow" > 		
	   	<div class="handWritingtext"><input type="text" value="" id="pinyinInput_input_orderFood" style="font-size: 15px;font-weight: bold;"></div>
	   	<div class="handWritingbutton"><a id="pinyinDel_a_orderFood"  data-role="button" data-theme="d" class="ui-corner-bottom">删除</a></div>
	   	<div class="handWritingbutton"><a id="pinyinVal_a_orderFood"  data-role="button" data-theme="d" class="ui-corner-bottom">清空</a></div>	
	    <div class="handWritingbutton"><a data-role="button" data-theme="e" class="ui-corner-bottom" id="closePinyin_a_orderFood">关闭</a></div>	
	   		
	   		
	<!-- 拼音键盘 -->
	   		<div id="pinyin_div_orderFood" style="width:100%;height:100%;display:none;margin-top:8%;" > 
	  
	    	</div>	
	</div>	
	
	
	
	
		<!-- 手写搜索 -->
	<div id="orderHandCmp" class="ui-overlay-shadow ui-corner-all handWritingWindow" >
	   	<div class="handWritingtext"><input type="text" value="" id="handWritingInput_input_orderFood" style="font-size: 15px;font-weight: bold;"></div>
	   	<div class="handWritingbutton"><a id="handDel_a_orderFood"  data-role="button" data-theme="d" class="ui-corner-bottom">清空</a></div>	
		<div class="handWritingbutton"><a data-role="button" data-theme="d" class="ui-corner-bottom" id="rewrite_a_orderFood">重写</a></div>
	    <div class="handWritingbutton"><a data-role="button" data-theme="e" class="ui-corner-bottom" id="handWritingClose_a_orderFood">关闭</a></div>	
		<div class="handWritingWord" style="clear:both;" id="searchWord_div_orderFood"></div>
		<div id="handWritingPanel_th_orderFood" class="handWritingPanel"></div>
	</div>		
	
	
	
		
	
	<div id="addTempTasteCmp" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: -100px 0px 0px -250px;min-width:500px;display: none;background-color: white;" align="center">
	<!-- <div id="addTempTasteCmp" data-role="popup"  data-theme="c" data-dismissible="false" style="min-width:550px;" class="ui-corner-all" align="center"> -->
	    <div data-role="header" data-theme="b" class="ui-corner-top">
	        <h1>添加临时口味</h1>
	    </div>
	    <table style="width:80%;">
	    	<tr>
	    		<td style="width:40px;"><label for="tempTasteName">名称:</label></td> 
	    		<td>
	    			<input id="tempTasteName" type="text" placeholder="填写临时口味名称" data-type="txt" class="countInputStyle">
	    		</td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempTasteName')">D</a>
					</div>		    		
	    		</td>
	    	</tr>
	    	<tr>
	    		<td style="width:40px;"><label for="tempTastePrice">价格:</label></td>
	    		<td>
					<input id="tempTastePrice" type="text" placeholder="填写临时口味价钱"  class="countInputStyle" data-type="num">    			
	    		</td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempTastePrice')">D</a>
					</div>	    		
	    		</td>
	    	</tr>	    	
	    </table>
		<div data-role="footer" data-theme="b" class="ui-corner-bottom">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" id="saveTempTaste_a_orderFood">确定</a>
				 <a  data-role="button" data-inline="true" class="countPopbottomBtn" id="closeTempTaste_a_orderFood">取消</a>		 
			 </div>
	    </div>			
	</div>	
	<!-- <div class="ui-popup-screen in" id="addTempFoodCmp-screen" style="display: none;"></div> -->
	<div id="addTempFoodCmp" class="ui-overlay-shadow ui-corner-all" style="z-index: 1102;position: absolute; top: 100px; left: 50%; margin: -100px 0px 0px -250px;min-width:400px;display: none;background-color: white;" align="center">
	    <div data-role="header" data-theme="b" class="ui-corner-top">
	        <h1>添加临时菜</h1>
	    </div>
	    
	    <table style="width:85%;">
	    	<tr>
	    		<td style="width:40px;"><label>名称:</label></td>
	    		<td><input id="tempFoodName_input_addTemp" type="text" placeholder="填写临时菜名称" data-type="txt" class="countInputStyle" ></td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempFoodName_input_addTemp')">D</a>
					</div>	    		
	    		</td>
	    	</tr>
	    	<tr>
	    		<td style="width:40px;"><label>价格:</label></td>
	    		<td><input id="tempFoodPrice_input_addTemp" type="text" placeholder="填写临时菜价格" data-type="num"  class="countInputStyle" ></td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempFoodPrice_input_addTemp')">D</a>
					</div>	    		
	    		</td>
	    	</tr>	    	
	    	<tr>
	    		<td style="width:40px;"><label>数量:</label></td>
	    		<td><input id="tempFoodCount_input_addTemp" type="text" placeholder="填写临时菜数量" value="1" data-type="num"  class="countInputStyle" ></td>
	    		<td>
					<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:inherit;">
					    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="deleteSingleWord('tempFoodCount_input_addTemp')">D</a>
					</div>		    		
	    		</td>
	    	</tr>	    	
	    	<tr>
	    		<td style="width:40px;"><label >分厨:</label></td>
	    		<td colspan="2">
	    			<a href="#popupTempFoodKitchensCmp" data-rel="popup" data-role="button" data-inline="true" data-transition="pop" data-theme="b">选择分厨</a>
	    			<label id="lab4TempKitchen" style="font-size: 25px;font-weight: bold;color: green;">临时厨房</label>
	    		</td>

	    	</tr>	    	
	    </table>

		<!-- <a href="#popupMenu" onclick="secondPop()" data-rel="popup" data-role="button" data-inline="true" data-transition="pop" data-theme="b">选择分厨t</a> -->
		
		<div data-role="popup" id="popupTempFoodKitchensCmp" data-theme="d" >
	        <ul id="tempFoodKitchensCmp" data-role="listview" data-inset="true" style="min-width:200px;" data-theme="d">
	        
	        </ul>
		</div>		
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" >
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a  data-role="button" id="saveTemp_a_orderFood" data-inline="true" class="countPopbottomBtn">确定</a>
				 <a  data-role="button" id="closeTemp_a_orderFood" data-inline="true" class="countPopbottomBtn">取消</a>		 
			 </div>
	    </div>			
	</div>
	
	<!-- 动态口味组件 -->
	<div id="divFoodTasteFloat" class="ui-overlay-shadow ui-corner-all commonTasteFloat" >
		<div  data-role="header" data-theme="b" class="ui-corner-top" style="height: 35px">
    		<div id="txtChooosedFoodName" style="float: left;line-height: 35px;margin-left: 10px;"></div>
        	<div style="float: right">
  				<a onclick="closeFoodCommonTaste()" data-role="button" data-corners="false" class="popupWinCloseBtn">X</a>      		
        	</div>			 
			 
	    </div>
	    
	    <div data-role="collapsible" data-mini="true" id="collapsibleCommonTaste" >
	    	<h3>常用口味</h3>
			<div id="divFloatFoodTastes">
<!-- 				<a onclick="chooseOrderFoodCommonTaste({event: this, id: 2})" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index=0 data-value=2 data-theme="c">
					<div>
						口味1<br>￥10
					</div>
				</a> -->
			</div> 
	    </div>
	    <div data-role="collapsible" data-mini="true" id="collapsibleMultiPrice">
	        <h3>选择单位</h3>
	        <div id="divFloatFoodMultiPrices">
<!-- 				<a onclick="" data-role="button" data-corners="false" data-inline="true" class="multiPriceCmp" data-index=0 data-value=2 data-theme="c">
					<div>
						¥45/例
					</div>
				</a> -->
			</div> 
	    </div>
	    
	    
 	
	</div>	
</div>
<!-- end 点菜界面-->

<!-- 沽清界面 start -->
<div data-role="page" id="stopSellMgr" >
	<div class="ui-grid-a" style="width: 100%;background-color: skyblue;border-bottom:1px solid white;">
		<div class="ui-block-a" data-role="controlgroup" data-corners="false" style="width:102px;">
			 <a id="divBtnSellFood" data-role="button" data-inline="true" class="tableStatus" onclick="ss.searchData({event:this, isStop:false})">在售菜品</a>
			 <a data-role="button" data-inline="true" class="tableStatus" onclick="ss.searchData({event:this,isStop:true})">沽清菜品</a>		 
		</div>
		<div class="ui-block-b" style="width: -webkit-calc(100% - 102px);width: -moz-calc(100% - 102px);width: -ms-calc(100% - 102px);width: -o-calc(100% - 102px);">
			<!-- 部门 -->
			 <div id="depts4StopSellCmp" data-role="controlgroup" data-type="horizontal"></div>		
			<!-- 厨房 -->
			 <div id="kitchens4StopSellCmp" data-role="controlgroup" data-type="horizontal" ></div>					 		
		</div>	

	</div>
	<div id="stopSellCmp" class="ui-grid-a" style="height: 470px;overflow: hidden;">
	     <div id="divFoods4StopSellCmp" class="ui-block-a" style="width: 350px;height: inherit;overflow-y: auto;background-color: skyblue;border-right: 1px solid white;">
	     	<!--已点菜列表  -->
			<ul id="toStopSellFoodCmp" data-role="listview" data-theme="d" data-inset="true"></ul>		    	
	     </div>
	     
	     <!-- 菜品列表 -->
	     <div id="foods4StopSellCmp" class="ui-block-b" style="width: -webkit-calc(100% - 350px);width: -moz-calc(100% - 350px);width: -ms-calc(100% - 350px);width: -o-calc(100% - 350px);background-color: skyblue;"></div>			
	</div>
	
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div class="bottomGeneralBar">
			<div id="count4StopSellFoods" style="float: left;margin-left: 20px;">总数量: 4份, 合计: ￥300.50</div>
			<div id="foods4StopSellCmp-padding-msg" style="float: right;margin-right: 20px;">共0项, 第1/1页, 每页?项</div>
		</div>	
		
		<div id="normalOperateFood4StopSellCmp" style="height: 60px;padding-top: 25px;">		
			<div data-role="controlgroup" class="ui-btn-left " data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.back()">返回</a>
			 	<a onclick="Util.to.scroll({content:'divFoods4StopSellCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			 	<a onclick="Util.to.scroll({content:'divFoods4StopSellCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>
			 </div>
			 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.resetFoodLimit()">限量重置</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : true})">沽清</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : false})">开售</a>
			 	<!-- <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="of.openAliasOrderFood()">助记码</a> -->
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="searchSelloutFood('on')">搜索</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.tp.prev()">上一页</a>
			 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.tp.next()">下一页</a>
			 </div>
		</div>
		<!-- 搜索组件 -->
		<div id="searchSelloutFoodCmp" style="height: 60px;padding-top: 25px;display: none;">		
			 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal" style="margin-right: 410px;">
			 <table>
			 	<tr>
			 		<td>
			 			<input id="searchSelloutFoodInput"  type="text" placeholder="输入菜名"  style="font-size: 20px;font-weight: bold;width: 150px;">
			 		</td>
			 		<td>
						<div data-role="controlgroup" data-type="horizontal" data-mini="true" class="ui-block-b" style="width:60px;" align="center">
						    <a data-role="button" data-iconpos="notext" data-icon="delete" data-theme="b" class="btnDeleteWord" onclick="ss.s.valueBack()">D</a>
						</div>			 		
			 		</td>
			 		<td>
					 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : true})">沽清</a>
					 	<a data-role="button" data-inline="true" class="bottomBtnFont" onclick="ss.soldOut({type : false})">开售</a>
			 			<a data-role="button" data-inline="true" class="bottomBtnFont" style="margin-top: -5px;" onclick="searchSelloutFood()">关闭</a>
			 		</td>
			 	</tr>
			 </table>
			 </div>
		 </div>			
	
		<!--限量沽清修改数量 -->
		<div id="orderFoodLimitCmp" data-role="popup" data-theme="c" data-dismissible="false" style="max-width:900px;" class="ui-corner-all" align="center">
		    <div data-role="header" data-theme="b" class="ui-corner-top">
		        <h1>输入剩余数量</h1>
		    </div>
		    <div style="min-height: 300px; overflow-y: auto;">
				<div class="calculator">
					<div class="top">
						<span class="clear">+</span>
						<span class="inputs">
							<input id="inputOrderFoodLimitCountSet" style="font-size: 20px;font-weight: bold;" onkeypress="intOnly()" onfocus="setInput('inputOrderFoodLimitCountSet')">
						</span>
						<span class="clear">-</span>
					</div>
					<div class="keys">
						<span>7</span>
						<span>8</span>
						<span>9</span>
						<span>0</span>
						
						<span>4</span>
						<span>5</span>
						<span>6</span>
						<span>.</span>
						
						<span>1</span>
						<span>2</span>
						<span>3</span>
						<span class="clear">C</span>
					</div>
				</div>		    
		    
			</div>	
			<div data-role="footer" data-theme="b" class="ui-corner-bottom">
				 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
					 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ss.setFoodLimitRemaining()">确定</a>
					 <a  data-role="button" data-inline="true" class="countPopbottomBtn" onclick="ss.closeFoodLimitCmp()">取消</a>		 
				 </div>
		    </div>
		</div>			
		 
	</div>	
</div>
<!-- end 沽清界面  -->

<!-- 结账界面 start -->
<div data-role="page" id="paymentMgr" data-theme="e">
	<div id="paymentCmp" class="ui-grid-a">
		<div class="ui-block-a" style="width:330px;border-right: 2px solid gray;height: inherit;">
			<table id="orderDetialTable"  data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr" >
		         <thead>
		           <tr class="ui-bar-d">
		             <th >收款</th>
		             <th>金额</th>
		             <th >操作</th>
		           </tr>
		         </thead>
		         <tbody>	
		         	<tr>
		         		<td >实收:</td>
		         		<td id="actualPrice_td_payment" style="color: blue;">0.00</td>
		         		<td>----</td>
		         	</tr>
		         	<tr>
		         		<td>应收:</td>
		         		<td id="totalPrice">0.00</td>
		         		<td>----</td>
		         	</tr>
		         	<tr>
		         		<td>赠送:</td>
		         		<td id="forFree">0.00</td>
		         		<td ><label id="lab_replaceGiftBtn">----</label><a id="spanSeeGiftFoodAmount_label_tableSelect" href="#" style="display: none;">明细</a></td>
		         	</tr>
		         	<tr>
		         		<td>折扣:</td>
		         		<td id="discountPrice">0.00</td>
		         		<td ><label id="lab_replaceDiscountBtn">----</label><a id="spanSeeDiscountFoodAmount_label_tableSelect" href="#" style="display: none;">明细</a></td>
		         	</tr>
		         	<tr>
		         		<td>退菜:</td>
		         		<td id="spanCancelFoodAmount">0.00</td>
		         		<td ><label id="lab_replaceCancelBtn">----</label><a id="spanSeeCancelFoodAmount_label_tableSelect" href="#" style="display: none;" >明细</a></td>
		         	</tr>	
		         	<tr id="eraseQuota_tr_payment">
		         		<td colspan="2" style="width: 200px;line-height: 60px;font-size: 20px;text-align: right;" >抹数金额(上限:￥<font id="eraseQuota_font_payment" color="red">--</font>)：</td>
		         		<td width="70" style="padding-right: 5px;">
		         			<input id="erasePrice_input_payment" class="countInputStyle">
		         		</td>
		         	</tr>
		         	<tr>
		         		<td style="width: 90px;line-height: 60px;">备注:</td>
		         		<td colspan="2" style="padding-right: 5px;">
		         			<input id="remark">
		         		</td>
		         	</tr>	         	
		         </tbody>
		    </table>	    
		    <!-- 按钮 -->
		    <div align="center">
		    	<a id="cash_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">现金(+)</a>
		    	<a id="credit_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">刷卡</a>
		    	<a id="sign_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">签单</a>
		    	<a id="hang_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">挂账</a>		    	
		    	<a id="cashReceive_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">现金找零</a>
		    	<a id="mixed_a_payment" data-role="button" data-theme="b" data-rel="popup" data-position-to="window" data-transition="pop" data-inline="true" style="width: 45%;">其他结账</a>
		    	<a data-role="button" data-theme="b" id="memberBalance_a_payment" data-rel="popup" data-position-to="window" data-transition="pop" data-inline="true" style="width: 45%;">读取会员</a>
		    	<a id="wx_a_payment" data-role="button" data-theme="b" data-inline="true" style="width: 45%;">微信支付</a>
		    </div>
		    
		</div>
		<div class="ui-block-b" data-role="content" style="width : -webkit-calc(100% - 330px);width: -moz-calc(100% - 330px);width : -ms-calc(100% - 330px);width: -o-calc(100% - 330px);height: inherit;">
			<div id="divNorthForUpdateOrder"  data-role="header" data-tap-toggle="false" data-theme="b" style="height: 36px;overflow: hidden;">
				<span id="orderIdInfo" style="line-height: 35px;float: left;margin-left: 5px;">
					结账 -- 账单号:<font color="#f7c942">-----</font>
				</span>
				<span id="orderTableInfo" style="line-height: 35px;float: left;margin-left: 20px;">
					 餐桌号:<font color="#f7c942">-- (---)</font>
				</span>
				<span style="line-height: 35px;float: left;margin-left: 20px;">
					 当前服务费: <font id="spanDisplayCurrentServiceRate" color="#f7c942">--</font>
				</span>
				<span style="line-height: 35px;float: left;margin-left: 20px;">
					 人数 : <font id="orderCustomNum" color="#f7c942">1</font>
				</span>
			</div>
			<div id="payment_orderFoodListCmp" style="overflow-y: auto;">
			<table id="orderFoodsTable"  data-theme="c" data-role="table" data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr" >
		         <thead>
		           <tr class="ui-bar-d">
		             <th style="width: 5px;"></th>
		             <th style="width: 25%;">菜名</th>
		             <th >数量</th>
		             <th style="width: 15%;">口味</th>
		             <th style="min-width: 68px;">口味价钱</th>
		             <th >单价</th>
					 <th style="min-width: 55px;">折扣率</th>
					 <th >总价</th>
					 <th >时间</th>
					 <th style="min-width: 55px;">服务员</th>             
		           </tr>
		         </thead>
		         <tbody id="payment_orderFoodListBody" style="font-weight:bold;color: blue">	
<!-- 		         	<tr>
		         		<td>1</td>
		         		<td >龙虾排骨</td>
		         		<td >1</td>
		         		<td>无口味</td>
		         		<td>0.00</td>
		         		<td>8.88</td>
		         		<td>0.5</td>
		         		<td >11.5</td>
		         		<td>14:23:23</td>
		         		<td>管理员</td>
		         	</tr> -->
		         </tbody>
		    </table>	
		    </div>		
		</div>
	</div>
	
	
	<div data-role="footer" data-position="fixed" data-tap-toggle="false" data-theme="b">
		<div  class="bottomGeneralBar" style="background-color:#f7c942">
			<div id="divDescForUpdateOrder" style="float: right;">
				<span id="orderCouponInfo" style="float: left;margin-right: 20px;"></span>	
				<span id="memberInfo_span_payment" style="float: left;margin-right: 20px;"></span>			
				<span id="orderDiscountDesc" style="float: left;margin-right: 20px;">
					当前折扣: <font color="green" >----</font>
				</span>						
			</div>
		</div>		
		 <a data-role="button" data-inline="true" class="bottomBtnFont" href="#tableSelectMgr" >返回</a>
		 <a data-role="button" data-inline="true" class="bottomBtnFont" onclick="refreshOrderData({calc:true})" >刷新</a>
		 <div data-role="controlgroup" class="ui-btn-right " data-type="horizontal">
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" id="issueCoupon_a_orderFood">发券</a>
			<a data-role="button" data-inline="true" class="bottomBtnFont" id="useCoupon_a_orderFood">用券</a>
		 	<a id="tempPay_a_payment" data-role="button" data-inline="true" class="bottomBtnFont">暂结(-)</a>
		 	<a id="updateOrder_a_payment" data-role="button" data-inline="true" class="bottomBtnFont">改单</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#discount_div_payment">折扣</a>
		 	<a data-role="button" data-inline="true" class="bottomBtnFont" data-rel="popup"  data-transition="pop" href="#servicePlan_div_payment">服务费</a>
			<a id="memberRead_a_payment" data-role="button" data-inline="true" class="bottomBtnFont">会员</a>
			<a href="javascript: Util.to.scroll({content:'payment_orderFoodListCmp', otype:'up'})" data-role="button" data-inline="true" class="bottomBtnFont">上翻</a>
			<a href="javascript: Util.to.scroll({content:'payment_orderFoodListCmp', otype:'down'})" data-role="button" data-inline="true" class="bottomBtnFont">下翻</a>			
		 </div>
	</div>	
	
	<div data-role="popup" id="discount_div_payment" data-theme="d" >
        <ul id="discount_ul_payment" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b"></ul>
	</div>	
	<div data-role="popup" id="servicePlan_div_payment" data-theme="d" >
        <ul id="servicePlan_ul_payment" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b"></ul>
	</div>	
	
<!-- 收款操作 -->	
	<div id="cashReceive_div_payment" data-role="popup"  data-overlay-theme="e" data-theme="d" data-dismissible="false" class="ui-corner-all" align="center">
	    <div data-role="header" class="ui-corner-top" data-theme="b">
	        <h1>输入收款显示找零</h1>
	    </div>
	    <div style="width: 100%;padding: 5px 10px;" >
	    <table id="inputReciptWinTable">
	    	<tr>
	    		<td>消费金额:</td>
	    		<td><label id="consume4CashReceive_a_payment"></label></td>
	    	</tr>
	    	<tr>
	    		<td>输入收款:</td>
	    		<td><input class="numberInputStyle" id="cashReceive_input_payment" onkeypress="intOnly()"></td>
	    	</tr>
	    	<tr>
	    		<td>找零:</td>
	    		<td><label id="cashBack_label_payment">0</label></td>
	    	</tr>	    		    	
	    </table>
	    </div>
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a id="receivedCashConfirm_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">确定</a>
				 <a id="receivedCashCancel_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">取消</a>		 
			 </div>
	    </div>
	 </div>	
	 
	 <!-- 显示会员信息 -->
	<div id="showMemberInfoWin" data-role="popup"  data-overlay-theme="e" data-theme="d" data-dismissible="false" class="ui-corner-all" style="min-width:700px;" align="center">	
	    <div data-role="header" data-theme="b" class="ui-corner-top ui-header ui-bar-b" style="line-height: 35px;">
	        	会员信息
	    </div>    
	    <table>
	    	<tr>
	    		<td class="readMemberTd">会员名称:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainName">----</label></td>
	    		<td class="readMemberTd">会员余额:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainBalance">----</label></td>    	
	    		<td class="readMemberTd">会员手机:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainPhone">----</label></td>       			
	    	</tr>
	    	<tr>
	    		<td class="readMemberTd">会员类型:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainType">----</label></td>
	    		<td class="readMemberTd">会员积分:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainPoint">----</label></td>   
	    		<td class="readMemberTd">会员卡号:</td>
	    		<td class="readMemberTdValue"><label id="payment4MemberCertainCard">----</label></td>     		   		
	    	</tr> 
	    	<tr>
	    		<td colspan="2">
					<label>
				        <input type="checkbox" id="memberPaymentSendSMS">是否发送短信
				    </label>	    		
	    		</td>
<!-- 	    		<td colspan="2">
					<label>
				        <input type="checkbox" id="memberPaymentPrintCore">是否打印二维码
				    </label>	    		
	    		</td> -->
	    	</tr>	    
	    </table>
	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-corners="false" data-type="horizontal" class="bottomBarFullWidth ui-bar-b barBottomBeCorner">
				 <a id="memberPay_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">结账</a>
				 <a id="memberPayCancel_a_payment" data-role="button" data-inline="true" class="countPopbottomBtn">取消</a>		 
			 </div>
	    </div>	
	</div>		 
	 
	 <!-- 系统共用会员绑定start -->
	<div id="loadMemberBind4Payment"></div>
	 
</div>
<!-- end 结账界面 -->
</body>
</html>
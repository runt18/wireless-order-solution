<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<% 
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
<!-- 工具类 -->
<script type="text/javascript" src="js/Util.js"></script>

<script type="text/javascript">
var oldMemberName = '<%=request.getParameter("memberName") %>';

$(function(){
 	Util.LM.show();
	$.ajax({
		url : '../QueryMemberType.do',
		type : 'post',
		async:false,
		data : {dataSource : 'normal'},
		success : function(jr, status, xhr){
			if(jr.success){
				Util.LM.hide();
				var html = [];
				var weixin;
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].name == "微信会员"){
						weixin = jr.root[i];
						continue;
					}
					html.push('<option value={id} data-attrVal={attrVal} data-chargeRate={chargeRate}>{name}</option>'.format({
						id : jr.root[i].id,
						attrVal : jr.root[i].attributeValue,
						chargeRate : jr.root[i].chargeRate,
						name : jr.root[i].name
					}));
				}
				//加上微信会员选项
				html.unshift('<option value={id} data-attrVal={attrVal} data-chargeRate={chargeRate}>{name}</option>'.format({
					id : weixin.id,
					attrVal : weixin.attributeValue,
					chargeRate : weixin.chargeRate,
					name : weixin.name
				}));
				//$('#fm_comboMemberType').html(html.join("")).trigger('create').selectmenu('refresh');
				
				$('#fm_comboMemberType').html(html.join(""));
				
				uo.memberInfoBind.typeList = true;
				uo.memberInfoBind.firstOption = weixin;
				
				$('#shadowForPopup').show();
				$('#finishMemberInfo').show();	
				
				setTimeout(function(){
					$('#fm_txtMemberName').val(uo.orderMember.name);
					$('#fm_txtMemberName').select();
					
				}, 250);
			}else{
				Util.msg.alert({
					renderTo : 'orderFoodListMgr',
					msg : jr.msg
				});
			}
		},
		error : function(request, status, err){
			Util.msg.alert({
				renderTo : 'orderFoodListMgr',
				msg : request.msg
			});
		}
	});  
	
 	setTimeout(function(){
		$('#fm_txtMemberName').val(oldMemberName);
		$('#fm_txtMemberName').select();
	}, 250);	
	
	
});


</script>

<style>
select{
	font-size: 20px;
	width: 125px;
  	height: 38px;
}
#finishMemberInfo input{
	font-size: 18px !important;
	font-weight: bold !important;
}
</style>

</head>
<body>
	<!-- 系统共用会员绑定start -->
	<div id="finishMemberInfo" class="ui-overlay-shadow ui-corner-all" style="width:740px;z-index: 1102;position: absolute; top: 30%; left: 50%; margin: -100px 0px 0px -370px;background-color: white;" align="center">	
	    <div data-role="header" data-theme="b" class="ui-corner-top ui-header ui-bar-b" style="line-height: 35px;">
	        	完善会员资料
	    </div> 
	    <table data-theme="b">
	    	<tr>
	    		<td class="readMemberTd">会员名称:</td>
	    		<td><input id="fm_txtMemberName" data-type="txt"></td>
	 	
	    		<td class="readMemberTd">会员手机:</td>
	    		<td><input id="fm_txtMemberMobile" onkeypress="intOnly()" data-type="num" class="countInputStyle"></td>    
	    		
	    		<td class="readMemberTd">会员卡号:</td>
	    		<td><input id="fm_numberMemberCard" onkeypress="intOnly()" data-type="num" class="countInputStyle"></td>     			
	    	</tr>
	    	<tr>
	    		<td class="readMemberTd">性别:</td>
	    		<td >
					<fieldset id="comboRadioSex" data-role="controlgroup" data-type="horizontal" >
				        <input type="radio" name="fm_comboMemberSex" data-theme="c" id="memberSexMan" value="0" checked="checked"><label for="memberSexMan">男</label>
				        <input type="radio" name="fm_comboMemberSex" data-theme="c" id="memberSexWoman" value="1"><label for="memberSexWoman">女</label>
				    </fieldset>			
					   		
	    		</td>    
	    		
	    		<td class="readMemberTd">生日:</td>
	    		<td><input type="date" data-role="datebox" id="fm_dateMemberBirthday" data-type="neither" data-options='{"mode": "datebox"}'></td>   
	    		
	    		<td class="readMemberTd">会员类型:</td>
	    		<td class="selectionCmp" style="line-height: 20px;padding: 0 3px;">
					<select id="fm_comboMemberType" data-role="none" data-native-menu="false" data-theme="b" onchange="ts.member.add_changeMemberType()">
						<option value="aaa" >aaaaaa</option>
						<option value="bbbb" >bbbbb</option>
					</select>		 
	    		</td>  		   		
	    	</tr> 
	    </table>
	    
	    <div id="divConfirmMember" class="none">
	    	<hr>
	    	<h2>请确认会员资料</h2>
		    <table id="tableConfirmMember" data-theme="b">

		    	<tr>
		    		<td class="readMemberTd">会员名称:</td>
		    		<td id="confirmMemberName">微信会员</td>
		 	
		    		<td class="readMemberTd">会员手机:</td>
		    		<td id="confirmMembeMobile">----</td>    
		    		
		    		<td class="readMemberTd">会员卡号:</td>
		    		<td id="confirmMembeCard">----</td>     			
		    	</tr>
		    	<tr>
		    		<td class="readMemberTd">性别:</td>
		    		<td id="confirmMembeSex">----</td>    
		    		
		    		<td class="readMemberTd">生日:</td>
		    		<td id="confirmMembeBirthday">----</td>   
		    		
		    		<td class="readMemberTd">会员类型:</td>
		    		<td id="confirmMembeType">----</td>  		   		
		    	</tr> 
		    </table>	    	
	    </div>
	    
		<div data-role="footer" data-theme="b" class="ui-corner-bottom" style="height: 47px;">
			 <div data-role="controlgroup" data-type="horizontal" class="bottomBarFullWidth">
				 <a id="weixinMemberCertain" data-role="button" data-theme="b" data-inline="true" class="countPopbottomBtn" onclick="ts.member.readMemberByDetail()">确定</a>
				 <a  data-role="button" data-theme="b" data-inline="true" class="countPopbottomBtn" onclick="ts.member.closeMemberInfoBind()">取消</a>		 
			 </div>
	    </div>	
	</div>	
	<!-- end会员绑定 -->

</body>
</html>
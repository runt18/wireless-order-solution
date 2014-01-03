
$(function(){
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'getInfo',
			oid : oid,
			fid : fid
		},
		dataType : 'json',
		success : function(data, status, xhr){
			if(data.success){
				$('#divMemberCard').css('display', 'block');
				$('#divMemberContent').css('display', 'block');
				member = data.other.member;
//				alert(JSON.stringify(member))
				member.restaurant = data.other.restaurant;
				initMemberMsg({data:member});
			}else{
				if(data.code == 7400){
					$('#divVerifyAndBind').css('display', 'block');
				}else{
					alert(data.msg);
				}
			}
		},
		error : function(data, errotType, eeor){
			alert('服务器请求失败, 请稍候再试.');
		}
	});
	
});
/**
 * 
 */
function initMemberMsg(c){
	c = c == null ? {} : c;
	var data = typeof c.data == 'undefined' ? {} : c.data;
	
	var restaurantName = $('#divRestaurantName');
	var name = $('#spanMemberName');
	var mobile = $('#spanMemberMobile');
	var point = $('#spanMemberPoint');
	var totalBalance = $('#spanMemberTotalBalance');
	
	restaurantName.html(typeof data.restaurant == 'undefined' ? '--' : data.restaurant.name);
	name.html(typeof data.name == 'undefined' ? '--' : data.name);
	mobile.html(typeof data.mobile == 'undefined' ? '--' : data.mobile);
	point.html(typeof data.point == 'undefined' ? '--' : data.point.toFixed(2));
	totalBalance.html(typeof data.totalBalance == 'undefined' ? '--' : data.totalBalance.toFixed(2));
	
}

/**
 * 
 * @param s
 */
function setBtnDisabled(s){
	var btnVerifyCode = document.getElementById('btnVerifyCode');
	var btnBindMember = document.getElementById('btnBindMember');
	if(s===false){
		btnVerifyCode.removeAttribute('disabled');
		btnBindMember.removeAttribute('disabled');
	}else{
		btnVerifyCode.setAttribute('disabled', true);
		btnBindMember.setAttribute('disabled', true);
	}
}

/**
 * 获取验证码
 * @param c
 */
function getVerifyCode(c){
	c = c == null ? {} : c;
	var mobile = $('#txtVerifyMobile');
	if(!/^1[3,5,8][0-9]{9}$/.test(mobile.val().trim())){
		alert('请输入 11 位纯数字的有效手机号码.');
		return;
	}
	
	c.event = typeof c.event == 'undefined' ? document.getElementById('btnVerifyCode') : c.event;
	var btnBindMember = document.getElementById('btnBindMember');
	
	setBtnDisabled(true);
	mobile.attr('disabled', true);
	verifyMobile = mobile;
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'getVerifyCode',
			rid : rid,
			oid : oid,
			fid : fid,
			mobile : mobile.val()
		},
		dataType : 'json',
		success : function(data, status, xhr){
			if(data.success){
				verifyCode = data.other.code;
				btnBindMember.removeAttribute('disabled');
				Util.lineTD($('#divVerifyAndBind > div[data-type=detail]'), 'show');
				var interval = null, time = 60;
				interval = window.setInterval(function(){
					if(time == 0){
						c.event.value = '获取验证码';
						c.event.removeAttribute('disabled');
						window.clearInterval(interval);
						mobile.removeAttr('disabled');
					}else{
						c.event.value = time+' 秒后可重新获取验证码';
					}
					time--;
				}, 1000);
			}else{
				setBtnDisabled(false);
				alert("获取验证码失败, 请稍候再试.");
			}
		},
		error : function(data, errotType, eeor){
			setBtnDisabled(false);
			alert('服务器请求失败, 请稍候再试.');
		}
	});
}
/**
 * 绑定会员
 * @param c
 */
function bindMember(c){
	c = c == null ? {} : c;
	var mobile = $('#txtVerifyMobile');
	var name = $('#txtVerifyName');
	var code = $('#txtVerifyCode');
	var sex = $('input[name=radioVerifySex]');
	for(var i = 0; i < sex.length; i++){
		if(sex[i].checked){
			sex = sex[i];
			break;
		}
	}
	if(!/^[^x00-xff]{2,16}$/.test(name.val().trim())){
		alert('请输入至少两个中文字的用户名.');
		return;
	}
	if(!/^[0-9]{4}$/.test(code.val().trim())){
		alert('请输入 4 位纯数字验证码.');
		return;
	}
	
	c.event = typeof c.event == 'undefined' ? document.getElementById('btnBindMember') : c.event;
	setBtnDisabled(true);
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'bind',
			rid : rid,
			oid : oid,
			fid : fid,
			codeId : verifyCode.id,
			code : code.val().trim(),
			name : name.val().trim(),
			mobile : mobile.val().trim(),
			sex : sex.value
		},
		dataType : 'json',
		success : function(data, status, xhr){
			alert(data.msg);
			if(data.success){
				window.location.reload();
			}
			setBtnDisabled(false);
		},
		error : function(data, errotType, eeor){
			setBtnDisabled(false);
			alert('服务器请求失败, 请稍候再试.');
		}
	});
}
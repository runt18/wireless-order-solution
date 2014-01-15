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
function setBtnDisabledByReset(s){
	var btnVerifyCodeByReset = document.getElementById('btnVerifyCodeByReset');
	var btnBindMemberByReset = document.getElementById('btnBindMemberByReset');
	if(s===false){
		btnVerifyCodeByReset.removeAttribute('disabled');
		btnBindMemberByReset.removeAttribute('disabled');
	}else{
		btnVerifyCodeByReset.setAttribute('disabled', true);
		btnBindMemberByReset.setAttribute('disabled', true);
	}
}

/**
 * 获取验证码
 * @param c
 */
function getVerifyCode(c){
	c = c == null ? {} : c;
	var mobile = $('#txtVerifyMobile');
	if(!params.MobileCode.code.test(mobile.val().trim())){
		alert(params.MobileCode.text);
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
			oid : Util.mp.oid,
			fid : Util.mp.fid,
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
	if(!params.UserNameCode.code.test(name.val().trim())){
		alert(params.UserNameCode.text);
		return;
	}
	if(!params.VerifyCode.code.test(code.val().trim())){
		alert(params.VerifyCode.text);
		return;
	}
	
	c.event = typeof c.event == 'undefined' ? document.getElementById('btnBindMember') : c.event;
	setBtnDisabled(true);
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'bind',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
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
/**
 * 重新绑定手机号码
 */
function resetMobile(){
	$('#divMemberContent').css('display', 'none');
	$('#divResetMobile').css('display', 'block');
}

function getVerifyCodeByReset(c){
	c = c == null ? {} : c;
	var mobile = $('#txtVerifyMobileByReset');
	if(!params.MobileCode.code.test(mobile.val().trim())){
		alert(params.MobileCode.text);
		return;
	}
	if(mobile.val().trim() == member.mobile){
		alert('请绑定新号码.');
		return;
	}
	
	c.event = typeof c.event == 'undefined' ? document.getElementById('txtVerifyMobileByReset') : c.event;
	var btnBindMember = document.getElementById('btnBindMemberByReset');
	
	setBtnDisabledByReset(true);
	mobile.attr('disabled', true);
	
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'getVerifyCode',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			mobile : mobile.val()
		},
		dataType : 'json',
		success : function(data, status, xhr){
			if(data.success){
				verifyCode = data.other.code;
				btnBindMember.removeAttribute('disabled');
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
				setBtnDisabledByReset(false);
				alert("获取验证码失败, 请稍候再试.");
			}
		},
		error : function(data, errotType, eeor){
			setBtnDisabledByReset(false);
			alert('服务器请求失败, 请稍候再试.');
		}
	});
}

function bindMemberByReset(c){
	c = c == null ? {} : c;
	var mobile = $('#txtVerifyMobileByReset');
	var code = $('#txtVerifyCodeByReset');
	if(!params.VerifyCode.code.test(code.val().trim())){
		alert(params.VerifyCode.text);
		return;
	}
	
	c.event = typeof c.event == 'undefined' ? document.getElementById('btnBindMember') : c.event;
	setBtnDisabled(true);
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'rebind',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			codeId : verifyCode.id,
			code : code.val().trim(),
			mobile : mobile.val().trim()
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
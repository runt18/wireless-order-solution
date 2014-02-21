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
	var typeName = $('#spanMemberTypeName');
	var typeDesc = $('#divMemberTypeDesc');
	
	restaurantName.html(typeof data.restaurant == 'undefined' ? '--' : data.restaurant.name);
	name.html(typeof data.name == 'undefined' ? '--' : data.name);
	mobile.html(typeof data.mobile == 'undefined' ? '--' : data.mobile);
	point.html(typeof data.point == 'undefined' ? '--' : data.point.toFixed(2));
	totalBalance.html(typeof data.totalBalance == 'undefined' ? '--' : data.totalBalance.toFixed(2));
	typeName.html(typeof data.memberType.name == 'undefined' ? '--' : data.memberType.name);
	typeDesc.html(typeof data.memberType.desc == 'undefined' ? '&nbsp;' : data.memberType.desc);
	
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
		Util.dialog.show({msg: params.MobileCode.text});
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
						c.event.innerHTML = '点击获取验证码';
						c.event.removeAttribute('disabled');
						window.clearInterval(interval);
						mobile.removeAttr('disabled');
					}else{
						c.event.innerHTML = '<font color="red">'+time+'</font>秒后可重新获取';
					}
					time--;
				}, 1000);
			}else{
				setBtnDisabled(false);
				Util.dialog.show({msg: "获取验证码失败, 请稍候再试."});
			}
		},
		error : function(data, errotType, eeor){
			setBtnDisabled(false);
			Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
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
		Util.dialog.show({msg: params.UserNameCode.text});
		return;
	}
	if(!params.VerifyCode.code.test(code.val().trim())){
		Util.dialog.show({msg: params.VerifyCode.text});
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
			Util.dialog.show({title: data.title, msg: data.msg});
			if(data.success){
				window.location.reload();
			}
			setBtnDisabled(false);
		},
		error : function(data, errotType, eeor){
			setBtnDisabled(false);
			Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
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
		Util.dialog.show({msg: params.MobileCode.text});
		return;
	}
	if(mobile.val().trim() == member.mobile){
		Util.dialog.show({msg: '请绑定新号码.'});
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
						c.event.innerHTML = '点击获取验证码';
						c.event.removeAttribute('disabled');
						window.clearInterval(interval);
						mobile.removeAttr('disabled');
					}else{
						c.event.innerHTML = '<font color="red">'+time+'</font>秒后可重新获取';
					}
					time--;
				}, 1000);
			}else{
				setBtnDisabledByReset(false);
				Util.dialog.show({msg: "获取验证码失败, 请稍候再试."});
			}
		},
		error : function(data, errotType, eeor){
			setBtnDisabledByReset(false);
			Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
		}
	});
}
function bindMemberByReset(c){
	c = c == null ? {} : c;
	var mobile = $('#txtVerifyMobileByReset');
	var code = $('#txtVerifyCodeByReset');
	if(!params.VerifyCode.code.test(code.val().trim())){
		Util.dialog.show({msg: params.VerifyCode.text});
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
			Util.dialog.show({title: data.title, msg: data.msg});
			if(data.success){
				window.location.reload();
			}
			setBtnDisabled(false);
		},
		error : function(data, errotType, eeor){
			setBtnDisabled(false);
			Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
		}
	});
}
/**
 * 查看消费明细
 */
function toggleConsumeDetails(){
	var mainView = $('#divConsumeDetails');
	var tbody = mainView.find('table > tbody');
	var templet = '<tr class="d-list-item">'
		+ '<td>{date}</td>'
		+ '<td>{balance}</td>'
		+ '<td>{point}</td>'
		+ '</tr>';
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			if(!toggleConsumeDetails.load){
				// 加载近5条消费记录
				toggleConsumeDetails.load = function(){
					Util.lm.show();
					$.ajax({
						url : '../../WXQueryMemberOperation.do',
						type : 'post',
						data : {
							dataSource : 'consumeDetails',
							oid : Util.mp.oid,
							fid : Util.mp.fid
						},
						dataType : 'json',
						success : function(data, status, xhr){
							Util.lm.hide();
							if(data.success){
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(templet.format({
										date : temp.operateDateFormat.substring(0, temp.operateDateFormat.length - 3),
										balance : temp.deltaTotalMoney.toFixed(2),
										point : temp.deltaPoint.toFixed(0)
									}));
								}
								tbody.html(html.length == 0 ? '暂无消费记录' : html.join(''));
							}else{
								Util.dialog.show({title: data.title, msg: data.msg});						
							}
						},
						error : function(data, errotType, eeor){
							Util.lm.hide();
							Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
						}
					});
				};
			}
			toggleConsumeDetails.load();
		}else{
			tbody.html('');
		}
	});
}
/**
 * 查看充值明细
 */
function toggleRechargeDetails(){
	var mainView = $('#divRechargeDetails');
	var tbody = mainView.find('table > tbody');
	var templet = '<tr class="d-list-item">'
		+ '<td>{date}</td>'
		+ '<td>{chargeMoney}</td>'
		+ '<td>{deltaTotalMoney}</td>'
		+ '</tr>';
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			if(!toggleRechargeDetails.load){
				// 加载近5条消费记录
				toggleRechargeDetails.load = function(){
					Util.lm.show();
					$.ajax({
						url : '../../WXQueryMemberOperation.do',
						type : 'post',
						data : {
							dataSource : 'chargeDetails',
							oid : Util.mp.oid,
							fid : Util.mp.fid
						},
						dataType : 'json',
						success : function(data, status, xhr){
							Util.lm.hide();
							if(data.success){
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(templet.format({
										date : temp.operateDateFormat.substring(0, temp.operateDateFormat.length - 3),
										chargeMoney : temp.chargeMoney.toFixed(2),
										deltaTotalMoney : temp.deltaTotalMoney.toFixed(2)
									}));
								}
								tbody.html(html.length == 0 ? '暂无充值记录' : html.join(''));
							}else{
								Util.dialog.show({title: data.title, msg: data.msg});						
							}
						},
						error : function(data, errotType, eeor){
							Util.lm.hide();
							Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
						}
					});
				};
			}
			toggleRechargeDetails.load();
		}else{
			tbody.html('');
		}
	});
}
/**
 * 现有优惠券
 */
function toggleCouponContent(){
	var mainView = $('#divMemberCouponContentView');
	var tbody = mainView.find('table > tbody');
	var templet = '<tr class="d-list-item">'
		+ '<td>{name}</td>'
		+ '<td>{price}</td>'
		+ '<td>{count}</td>'
		+ '</tr>';
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			if(!toggleCouponContent.load){
				// 加载近5条消费记录
				toggleCouponContent.load = function(){
					Util.lm.show();
					$.ajax({
						url : '../../WXQueryMemberOperation.do',
						type : 'post',
						data : {
							dataSource : 'hasCouponDetails',
							oid : Util.mp.oid,
							fid : Util.mp.fid
						},
						dataType : 'json',
						success : function(data, status, xhr){
							Util.lm.hide();
							if(data.success){
								var html = [], temp = null;
								for(var i = 0; i < data.other.root.length; i++){
									temp = data.other.root[i];
									html.push(templet.format({
										name : temp.name,
										price : temp.price.toFixed(2),
										count : temp.items.length
									}));
								}
								tbody.html(html.length == 0 ? '暂无优惠券' : html.join(''));
							}else{
								Util.dialog.show({title: data.title, msg: data.msg});						
							}
						},
						error : function(data, errotType, eeor){
							Util.lm.hide();
							Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
						}
					});
				};
			}
			toggleCouponContent.load();
		}else{
			tbody.html('');
		}
	});
}
/**
 * 近5条优惠券使用记录
 */
function toggleCouponConsumeDetails(){
	var mainView = $('#divMemberCouponConsumeDetails');
	var tbody = mainView.find('table > tbody');
	var templet = '<tr class="d-list-item">'
		+ '<td>{name}</td>'
		+ '<td>{payMoney}</td>'
		+ '<td>{couponMoney}</td>'
		+ '</tr>';
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			if(!toggleCouponConsumeDetails.load){
				// 加载近5条消费记录
				toggleCouponConsumeDetails.load = function(){
					Util.lm.show();
					$.ajax({
						url : '../../WXQueryMemberOperation.do',
						type : 'post',
						data : {
							dataSource : 'couponConsumeDetails',
							oid : Util.mp.oid,
							fid : Util.mp.fid
						},
						dataType : 'json',
						success : function(data, status, xhr){
							Util.lm.hide();
							if(data.success){
								var html = [], temp = null;
								for(var i = 0; i < data.root.length; i++){
									temp = data.root[i];
									html.push(templet.format({
										name : temp.name,
										payMoney : temp.payMoney.toFixed(2),
										couponMoney : temp.couponMoney.toFixed(2)
									}));
								}
								tbody.html(html.length == 0 ? '暂无优惠券使用记录.' : html.join(''));
							}else{
								Util.dialog.show({title: data.title, msg: data.msg});						
							}
						},
						error : function(data, errotType, eeor){
							Util.lm.hide();
							Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
						}
					});
				};
			}
			toggleCouponConsumeDetails.load();
		}else{
			tbody.html('');
		}
	});
}
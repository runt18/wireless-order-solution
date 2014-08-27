/**
 * 
 */
function initMemberMsg(c){
	c = c == null ? {} : c;
	var data = typeof c.data == 'undefined' ? {} : c.data;
	
//	var restaurantName = $('#divRestaurantName');
	var name = $('#spanMemberName');
	var mobile = $('#spanMemberMobile');
	var point = $('#spanMemberPoint');
	var totalBalance = $('#spanMemberTotalBalance');
	var typeName = $('#spanMemberTypeName');
	var typeNameInCard = $('#divMemberTypeName');
	var weixinMemberCard = $('#divWXMemberCard');
	var defaultMemberDiscount = $('#fontMemberDiscount');
	var memberTotalPoint = $('#fontMemberPoint');
	
//	restaurantName.html(typeof data.restaurant == 'undefined' ? '--' : data.restaurant.name);
	name.html(typeof data.name == 'undefined' ? '--' : data.name);
	mobile.html(typeof data.mobile == 'undefined' ? '--' : data.mobile);
	point.html(typeof data.point == 'undefined' ? '--' : data.point);
	totalBalance.html(typeof data.totalBalance == 'undefined' ? '--' : checkDot(data.totalBalance)?parseFloat(data.totalBalance).toFixed(2) : data.totalBalance);
	typeName.html(typeof data.memberType.name == 'undefined' ? '--' : data.memberType.name);
	typeNameInCard.html(typeof data.memberType.name == 'undefined' ? '未激活' : data.memberType.name);
//	weixinMemberCard.html(typeof data.memberType.name == 'undefined' ? '未激活' : data.memberType.name);
	defaultMemberDiscount.html(typeof data.memberType.discount != 'undefined' && data.memberType.discount.type != 2 ? data.memberType.discount.name : '');
	memberTotalPoint.html(typeof data.totalPoint == 'undefined' ? '--' : data.totalPoint);
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
	var btnVerifyCode = $('#btnGetVerifyCode');
	var btnBindMember = $('#btnBindMember');
	var mobile = $('#txtVerifyMobile');
	
	if(!params.MobileCode.code.test(mobile.val().trim())){
		Util.dialog.show({msg: params.MobileCode.text, callback : function(){showMemberBind();}});
		return;
	}
	
	c.event =  document.getElementById('btnVerifyCode');
	
	
//	setBtnDisabled(true);
//	mobile.attr('disabled', true);
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
				Util.lineTD($('#divVerifyAndBind > div[data-type=detail]'), 'show');
				var interval = null, time = 60;
				btnVerifyCode.hide();
				btnBindMember.show();
								
				interval = window.setInterval(function(){
					if(time == 0){
						window.clearInterval(interval);
						mobile.removeAttr('disabled');
						btnBindMember.hide();
						btnVerifyCode.show();
						c.event.innerHTML ="";
					}else{
						c.event.innerHTML = '<font color="red">'+time+'</font>秒后可重新获取';
					}
					time--;
				}, 1000);
			}else{
//				setBtnDisabled(false);
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
//	var name = $('#txtVerifyName');
	var code = $('#txtVerifyCode');
//	var sex = $('input[name=radioVerifySex]');
//	for(var i = 0; i < sex.length; i++){
//		if(sex[i].checked){
//			sex = sex[i];
//			break;
//		}
//	}
	
	
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
		Util.dialog.show({msg: params.MobileCode.text, callback : function(){showMemberBind();}});
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

function fnDateInChinese(date){
	var month = date.substring(5, 7);
	var day = date.substring(8, 10);
	var time = date.substring(11, date.length - 3);
	
	return month+ '月' +day + '日' + ' ' + time;
	
}
/**
 * 查看消费明细
 */
function toggleConsumeDetails(){
	
	var mainView = $('#divConsumeDetails');
	var tbody = mainView.find('table > tbody');
	var templet = '<tr class="d-list-item-consume">'
		+ '<td>{date}</td>'
		+ '<td>{balance}</td>'
		+ '<td>{point}</td>'
		+ '</tr>';
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			$('html, body').animate({scrollTop: 0}, 'fast'); 
			$('html, body').animate({scrollTop: 300}, 'fast'); 
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
										date : fnDateInChinese(temp.operateDateFormat) + '</br><font style="font-size:13px;">账单号:' + temp.orderId + '</font>',
										balance : (checkDot(temp.deltaTotalMoney)?parseFloat(temp.deltaTotalMoney).toFixed(2) : temp.deltaTotalMoney) + '元',
										point : temp.deltaPoint.toFixed(0) + '分'
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
			$('html, body').animate({scrollTop: 0}, 'fast');
			$('html, body').animate({scrollTop: (370+($('#table_consumeDetails').height() != 0?$('#table_consumeDetails').height():-55))}, 'fast');
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
										date : fnDateInChinese(temp.operateDateFormat),
										chargeMoney : (checkDot(temp.chargeMoney)?parseFloat(temp.chargeMoney).toFixed(2) : temp.chargeMoney) + '元',
										deltaTotalMoney : (checkDot(temp.deltaTotalMoney)?parseFloat(temp.deltaTotalMoney).toFixed(2) : temp.deltaTotalMoney) + '元'
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

function calcFloatDivs(){
	var divCouponsW = $('#divMemberCouponContentView').width();
	var i = member.couponCount;
	var totalW = 130 * i;
	while(totalW > divCouponsW){
		i --;
		totalW = 130 * i;
	}
	
	var paddingW = divCouponsW - totalW;
	var divPadding = 0;
	if(paddingW > 0){
		divPadding = paddingW/2;
	}
	
	var rowCount = (130 * member.couponCount) / divCouponsW;
	var divCouponsH = 0 + 'px';
	if(0 < rowCount && rowCount <1){
		divCouponsH = 160 + 'px';
	}else if(1 < rowCount && rowCount < 2){
		divCouponsH = 160*2 +'px';
	}else if(2 < rowCount && rowCount < 3){
		divCouponsH = 160*3 +'px';
	}	
	
	$('#divMemberCouponContentView').css({'padding-left' : divPadding+'px'});
	$('#divMemberCouponContentView').css({'height' : divCouponsH});
	
} 


/**
 * 现有优惠券
 */
function toggleCouponContent(){
	var mainView = $('#divMemberCouponContentView');
	
	var templet = '<div class="box">' +
					'<div class="box_in"><img src="{couponImg}"></div>' +
					'<span>{name}</span><br><span>到期 : {expiredTime}</span>' +
				  '</div>';	
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			if(!toggleCouponContent.load){
				toggleCouponContent.load = function(){
					Util.lm.show();
/*					$.ajax({
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
								member.couponCount = data.root.length;
								if(data.root.length > 0){
									for(var i = 0; i < data.root.length; i++){
										temp = data.root[i];
										html.push(templet.format({
											couponImg : temp.couponType.image,
											name : temp.couponType.name,
											expiredTime : temp.couponType.expiredFormat
										}));
									}
									mainView.html(html);
									
									calcFloatDivs();
									
								}else{
									mainView.html('暂无优惠券');
								}
							}else{
								Util.dialog.show({title: data.title, msg: data.msg});						
							}
						},
						error : function(data, errotType, eeor){
							Util.lm.hide();
							Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
						}
					});*/
				};
			}
			toggleCouponContent.load();
		}else{
			mainView.html('');
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
		+ '<td>{time}</td>'
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
										time : temp.operateDateFormat,
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

function toggleMemberLevel(){
	var mainView = $('#divMemberLevelChart');
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			if(!toggleMemberLevel.load){
				toggleMemberLevel.load = function(){
					Util.lm.show();
/*					$.post('../../QueryMemberType.do', {dataSource : 'getMemberLevel', fid : Util.mp.fid, oid : Util.mp.oid}, function(result){
						if(result.success){
							totalMath = result.root[result.root.length - 1].pointThreshold;
							member.totalPoint = member.totalPoint >= totalMath ? totalMath : member.totalPoint; 
							mainView.append('<h3>会员等级列表</h3>');
							for(var i in result.root){
								var levelWidth = result.root[i].pointThreshold / totalMath * 100;
								var memberLevelWidth = member.totalPoint / totalMath * 100;
								var tipHtml = result.root[i].inLevel == true ? '<div id="positionTip" class="tooltip" style="left : -13%">'+ member.totalPoint +'分</div>' : '';
								var spanHtml = result.root[i].inLevel == true ? '<span style="width:'+ memberLevelWidth +'%;background-color:'+ colors[1] +';word-break:keep-all;white-space:nowrap;">'+ result.root[i].pointThreshold +'分&nbsp;('+ result.root[i].memberType.discount.name +')</span>'
																				: '<span style="width:'+ levelWidth +'%;background-color:'+ colors[2] +';word-break:keep-all;white-space:nowrap;">'+ result.root[i].pointThreshold +'分&nbsp;('+ result.root[i].memberType.discount.name +')</span>';
								
								mainView.append('<div class="memberLevelList"><div class="memberLevelHead"><p class="memberLevelHeadFont">'+ result.root[i].memberTypeName+'</div>'
										+ '<div class="memberLevelBody">'
										+ '<div class="graph">' + spanHtml 
										+ tipHtml
										+ '</div></div></div>');
							}
							var width = ((member.totalPoint/totalMath) - .13) * 100 + '%';
							$('#positionTip').css({left : width});
							
						}else{
							$('#divMemberTypeDesc').append('<div>会员等级建立中...</div>');
						}
						Util.lm.hide();
					});*/
					$('html, body').animate({scrollTop: 0});
					$('html, body').animate({scrollTop: 310+$('#divMemberPointContent').height()+$('#divMemberBalanceContent').height()}, 'fast');					
					$.post('../../QueryMemberLevel.do', {dataSource : 'chart', rid:member.restaurant.id}, function(result){
						if(result.success){
//							totalMath = result.root[result.root.length - 1].pointThreshold;
//							member.totalPoint = member.totalPoint >= totalMath ? totalMath : member.totalPoint; 
							mainView.prepend('<h3>会员等级列表</h3>');
							mainView.css('margin-left', '-40%');
							
							memberLevelData = result.root;
							memberLevelData.push(currentMemberLevelData);
							
							chartDatas = eval('(' + result.other.chart + ')');
					
							yAxisData = chartDatas.data;
							
							var chartMinAndMax;
							
							if(yAxisData[yAxisData.length-1].x >= currentMemberLevelData.x){
								chartMinAndMax = yAxisData[yAxisData.length-1].x;
							}else{
								chartMinAndMax = currentMemberLevelData.x;
							}
							yAxisData.push(currentMemberLevelData);
							
							member_loadMemberTypeChart({minY:-chartMinAndMax * 0.15, maxY:chartMinAndMax * 1.2, series:yAxisData});
							
						}else{
							mainView.css('height', 'auto');
							mainView.append('<div>会员等级建立中...</div>');
						}
						Util.lm.hide();
					});					
				};
			}
			toggleMemberLevel.load();
		}else{
			mainView.html('');
		}
	});
}

function weixinPhoneFocus(){
	$('#txtVerifyMobile').focus();
}


function showMemberBind(){
	$('#btn_activate').hide();
	$('#ulVerifyAndBind').show();
//	$('#divOccupyHtml').show();
	$('html, body').animate({scrollTop: 190+($('#divMemberPrivilegeDetail').height()>=30?$('#divMemberPrivilegeDetail').height():-25)}, 'fast'); 
//	$('#txtVerifyMobile').focus();
	weixinPhoneFocus();
	weixinPhoneFocus();
}

function getLevelChartInfo(x,point){
	var temp = {};
	if(point){
		temp = currentMemberLevelData;
	}else{
		for (var i = 0; i < memberLevelData.length; i++) {
			if(memberLevelData[i].pointThreshold == x){
				temp = memberLevelData[i];
				break;
			}
		}	
	}
	return '<span style="font-size : 14px;">' + temp.memberTypeName + (temp.pointThreshold >0 || point? '--' + temp.pointThreshold +'分' :'')+ '</span>'
			+ (temp.discount.type != 2 ? '<br/>' + '<font style="font-size: 13px;color:maroon">' + temp.discount.name : '') + '</font>' 
			+ (temp.chargeRate >1 ? '<br/>'+ '<font style="font-size: 13px;color:maroon">' + temp.chargeRate +'倍充值优惠, 充100送'+parseInt((temp.chargeRate-1)*100)+'元':'')  + '</font>' 
			+ (temp.exchangeRate >1 ? '<br/>'+ '<font style="font-size: 12px;color:maroon">' + temp.exchangeRate +'倍积分特权, 消费1元积'+temp.exchangeRate+'分':'') + '</font>' 
			;		
}

function member_loadMemberTypeChart(c){
	 	var chart = {
			    chart: {
			        type: 'spline',
			        inverted: true,
			        renderTo : 'divMemberLevelChart'
			    },
			    title: {
			        text: ''
			    },
			    xAxis: {
			    	reversed : false,
			        title: {
			            enabled: false,
			            text: '积分',
			            align : 'high'
			        },
			        labels: {
			            formatter: function() {
			                return this.value;
			            }
			        },
			        max : c.maxY,
			        min: c.minY,
			        showLastLabel: true
			    },
			    yAxis: {
			        title: {
			            text: '等级'
			        },
			        labels: {
			            formatter: function() {
			                return '' ;
			            }
			        },
			        lineWidth: 2
			    },
			    legend: {
			        enabled: false
			    },
			    tooltip: {
			    	enabled : false,
			        headerFormat: '<b>{series.name}</b><br/>',
			        pointFormat: '{point.x} km: {point.y}°C',
			        followPointer : true
			    },
				plotOptions : {
					spline : {
						cursor : 'pointer',
						dataLabels : {
							x: 10,
							y : 23,							
							align : 'left',
							enabled : true,
							style : {
								fontWeight: 'bold', 
								color: 'green'
							},
							formatter : function(){
				                return getLevelChartInfo(this.x, this.point.memberTypeName);
							}
						},
						marker: {
							radius: 8,
		                    lineColor: 'white',
		                    lineWidth: 1
			            }
					}
				},			    
			    credits : {
			    	enabled : false
			    },         
			    exporting : {
			    	enabled : false
			    },			    
			    series:	[{data:c.series}]	    
			};
			new Highcharts.Chart(chart);
}

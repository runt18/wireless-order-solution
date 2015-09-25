


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
		+ '<td style="text-align: center;">{date}</td>'
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
		+ '<td style="text-align: center;">{date}</td>'
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
	
	var templet = '<div class="box" onclick="Util.jump(\'sales.html\', {couponId})">' +
					'<div class="box_in"><img src="{couponImg}"></div>' +
					'<span>{name}</span><br><span>面额 : {cPrice} 元</span><br><span>到期 : {expiredTime}</span><br><span>来自 : {promotionName}</span>' +
				  '</div>';	
	mainView.fadeToggle(function(){
		if(mainView.css('display') == 'block'){
			$('html, body').animate({scrollTop: 0});
			$('html, body').animate({scrollTop: 310+$('#divMemberPointContent').height()+$('#divMemberBalanceContent').height()+$('#divMemberTypeContent').height()}, 'fast');			
			if(!toggleCouponContent.load){
				toggleCouponContent.load = function(){
					Util.lm.show();
					$.ajax({
						url : '../../WxOperateCoupon.do',
						type : 'post',
						data : {
							dataSource : 'getByCond',
							status : 'drawn',
							oid : Util.mp.oid,
							fid : Util.mp.fid
						},
						dataType : 'json',
						success : function(data, status, xhr){
							Util.lm.hide();
							if(data.success){
								var html = [];
								var couponAmount = 0;
								
								for(var i = 0; i < data.root.length; i++){
									var coupon = data.root[i];
									//不显示纯显示和已过期的优惠券
									if(coupon.promotion.rule != 1 && parseInt(coupon.couponType.expired) > new Date()){
										html.push(templet.format({
											couponImg : coupon.couponType.ossImage ? coupon.couponType.ossImage.image : 'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg',
											name : coupon.couponType.name,
											cPrice : coupon.couponType.price,
											expiredTime : coupon.couponType.expiredFormat,
											promotionName : coupon.promotion.title,
											couponId : coupon.couponId
										}));
										couponAmount++;
									}
								}
								mainView.html(html);
								
								//计算图片居中
//								calcFloatDivs();
								
								if(couponAmount == 0){
									mainView.html('暂无优惠券');
								}
								member.couponCount = couponAmount;
								
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
	var templet = '<tr class="d-list-item-coupon">'
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
										time : fnDateInChinese(temp.operateDateFormat) + '</br><font style="font-size:13px;">' + temp.couponName +'</font></br><font style="font-size:13px;">账单号:' + temp.orderId + '</font>',
										payMoney : (checkDot(temp.payMoney)?parseFloat(temp.payMoney).toFixed(2) : temp.payMoney) + '元',
										couponMoney : (checkDot(temp.couponMoney)?parseFloat(temp.couponMoney).toFixed(2) : temp.couponMoney) + '元'
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
					$('html, body').animate({scrollTop: 0});
					var height = 350;
					height += $('#divMemberPointContent').height();
					height += $('#divMemberBalanceContent').height();
					if($('#bindMember_div_member').is(':visible')){
						height += $('#bindMember_div_member').height();
					}
					$('html, body').animate({scrollTop: height}, 'fast');					
					$.post('../../WXQueryMemberOperation.do', {dataSource : 'chart', rid:member.restaurant.id}, function(result){
						if(typeof result == 'string'){
							result = eval('(' + result + ')');
						}
						
						if(result.success){
							memberLevelData = result.root;
							memberLevelData.push(currentMemberLevelData);
							
							chartDatas = eval('(' + result.other.chart + ')');
					
							yAxisData = chartDatas.data;
							
							if(yAxisData.length > 0){
								mainView.prepend('<h3>会员等级列表</h3>');
								mainView.css('margin-left', '-40%');
								
								//动态变化chart高度
								$('#divMemberLevelChart').height(yAxisData.length * (document.body.clientWidth > 330 ? 70 : 95) + 140);
								
								var chartMinAndMax;
								
								if(yAxisData[yAxisData.length-1].x >= currentMemberLevelData.x){
									chartMinAndMax = yAxisData[yAxisData.length-1].x;
								}else{
									chartMinAndMax = currentMemberLevelData.x;
								}
								//添加用户等级位置
								yAxisData.push(currentMemberLevelData);
								
								member_loadMemberTypeChart({minY:-chartMinAndMax * 0.15, maxY:chartMinAndMax * 1.2, series:yAxisData});								
							}else{
								mainView.css('height', 'auto');
								mainView.append('<div>会员等级建立中...</div>');
							}
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
	
	var pointFormat;
	if(document.body.clientWidth > 330){
		pointFormat = '<span style="font-size : 14px;">' + temp.memberTypeName + (temp.pointThreshold >0 || point? '--' + temp.pointThreshold +'分' :'')+ '</span>'
			+ (temp.discount.type != 2 ? '<br/>' + '<font style="font-size: 13px;color:maroon">' + temp.discount.name : '') + '</font>'
			+ (temp.chargeRate > 1 ? '<br/>'+ '<font style="font-size: 13px;color:maroon">' + temp.chargeRate +'倍充值优惠，充100送'+parseInt((temp.chargeRate*100 - 100))+'元':'')  + '</font>' 
			+ (temp.exchangeRate > 1 ? '<br/>'+ '<font style="font-size: 12px;color:maroon">' + temp.exchangeRate +'倍积分特权，消费1元积'+temp.exchangeRate+'分':'') + '</font>' 			
			;
	}else{
		pointFormat = '<span style="font-size : 14px;">' + temp.memberTypeName + (temp.pointThreshold >0 || point? '--' + temp.pointThreshold +'分' :'')+ '</span>'
			+ (temp.discount.type != 2 ? '<br/>' + '<font style="font-size: 13px;color:maroon">' + temp.discount.name : '') + '</font>' 
			+ (temp.chargeRate > 1 ? '<br/>'+ '<font style="font-size: 13px;color:maroon">' + temp.chargeRate +'倍充值优惠，</font> <br/><font style="font-size: 13px;color:maroon">充100送'+parseInt((temp.chargeRate*100 - 100))+'元':'')  + '</font>' 
			+ (temp.exchangeRate > 1 ? '<br/>'+ '<font style="font-size: 12px;color:maroon">' + temp.exchangeRate +'倍积分特权，</font> <br/><font style="font-size: 13px;color:maroon">消费1元积'+temp.exchangeRate+'分':'') + '</font>' 
			;
	}
	
	return pointFormat;		
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
							y : 37,							
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


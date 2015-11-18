$(function(){
	
	$('html, body').animate({scrollTop: 0}, 'fast');
	Util.lbar('', function(html){ $(document.body).append(html);  });
	Util.lm.show();
	$.ajax({
		url : '../../WXOperateMember.do',
		type : 'post',
		data : {
			dataSource : 'getInfo',
			oid : Util.mp.oid,
			fid : Util.mp.fid
		},
		dataType : 'json',
		success : function(data, status, xhr){
			Util.lm.hide();
			if(data.success){
				//会员卡号
				$('#divWXMemberCard').html('No.' + data.other.weixinCard);
				
				if(data.other.status == 2){
					//手机号已绑定状态
					$('#phone_div_member').show();
					$('#bind_div_member').hide();
				}else if(data.other.status == 1){
					//手机号码未绑定状态
					$('#bind_div_member').show();
					$('#phone_div_member').hide();					
				}
				
				$.ajax({
					url : '../../WXQueryMemberOperation.do',
					type : 'post',
					data : {
						dataSource : 'recent',
						oid : Util.mp.oid,
						fid : Util.mp.fid
					},
					dataType : 'json',
					success : function(data, status, xhr){
						if(data.other.nearByCharge >= 0){
							$('#spanNearByCharge').html(data.other.nearByCharge);
							$('#divMemberBalanceContent').css('display', 'block');
						}
						if(data.other.nearByConsume >= 0){
							$('#spanNearByCost').html(data.other.nearByConsume);
							$('#divMemberPointContent').css('display', 'block');
						}
						if(data.other.couponConsume >= 0){
							$('#couponDetails_div_member').css('display', 'block');
						}							
					},
					error : function(data, errotType, eeor){
						Util.dialog.show({msg: '服务器请求超时, 请刷新.'});
					}
				});					
				
				$('#divMemberCard').css('display', 'block');
				$('#divMemberContent').css('display', 'block');
				$('#divMemberTypeContent').css('display', 'block');
				$('#divMemberBalanceAndPiont').css('display', 'block');
				
				if(data.other.member.memberType.desc){
					$('#divCurrentMemberTypeDesc').css('display', 'block');
					$('#spanCurrentMemberTypeDesc').html(data.other.member.memberType.desc);
				}
				
				if(data.other.hasCoupon){
					$('#divMemberCouponContent').css('display', 'block');
					if(Util.mp.extra && Util.mp.extra == 'coupon'){
						$('#myCoupon_li_member').click();
					}
				}					
								
				member = data.other.member;
				member.restaurant = data.other.restaurant;
				initMemberMsg({data:member});
				
				//添加会员等级当前位置
				currentMemberLevelData = {y : 0, memberTypeName : '您的积分', currentPoint:true, x:member.totalPoint, pointThreshold:member.totalPoint, discount:{type :2},chargeRate:-1, exchangeRate:-1, marker:{symbol:'url(images/currentPosition.png)'}, color : 'red', dataLabels : {x:-1, align : 'right', style : {fontWeight: 'bold',color: 'red'}}};
				
				//设置会员排名
				if(member.rank > 0){
					$('#fontDefeatMemberCount').text(member.rank);
					$('#span_currentMemberDefeat').show();
				}
				
				var memberType = data.other.member.memberType;
				$('#privilege_ul_member').append(memberType.discount.type != 2 ? '<li>'+ memberType.discount.name +'优惠</li>' : '');	
				$('#privilege_ul_member').append(memberType.chargeRate >1 ? '<li>'+ memberType.chargeRate +'倍充值优惠, 充 100 元送 '+parseInt((memberType.chargeRate-1)*100)+' 元</li>':'');
				$('#privilege_ul_member').append(memberType.exchangeRate >1 ? '<li>'+ memberType.exchangeRate +'倍积分特权, 消费 1 元积 '+memberType.exchangeRate+' 分</li>':'');
				$('#privilege_ul_member').append(typeof memberType.desc != 'undefined' && memberType.desc != '' ? '<li>'+ memberType.desc +'</li>':'');
				
				
				if($('#privilege_ul_member').html()){
					$('#privilege_div_member').css('display', 'block');
				}else{
					$('#privilege_div_member').css('display', 'none');
				}					
					
			}else{
				Util.dialog.show({title:'提示', msg: data.msg});
			}
		},
		error : function(data, errotType, err){
			Util.lm.hide();
			Util.dialog.show({title:'错误', msg: '服务器请求失败, 请稍候再试.'});
		}
	});
	
	function initMemberMsg(c){
		c = c == null ? {} : c;
		var data = typeof c.data == 'undefined' ? {} : c.data;
		
	//	var restaurantName = $('#divRestaurantName');
		var name = $('#name_span_member');
		var mobile = $('#mobile_span_member');
		var point = $('#spanMemberPoint');
		var totalBalance = $('#spanMemberTotalBalance');
		var baseBalance = $('#spanMemberBaseBalance');
		var extraBalance = $('#spanMemberExtraBalance');
		var typeName = $('#spanMemberTypeName');
		var birthday = $('#memberBirthday_span_member');
		var typeNameInCard = $('#divMemberTypeName');
	//	var weixinMemberCard = $('#divWXMemberCard');
		var defaultMemberDiscount = $('#fontMemberDiscount');
		var memberTotalPoint = $('#fontMemberPoint');
		var myCoupon = $('#myCoupon');
		
		$.ajax({
			url : '../../WxOperateCoupon.do',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				status : 'issued',
				expired : false,
				oid : Util.mp.oid,
				fid : Util.mp.fid
			},
			dataType : 'json',
			success : function(data, status, xhr){
				Util.lm.hide();
				if(data.success){
					var couponAmount = 0;
					if(data.root.length > 0){
						myCoupon.html(data.root.length + '张');
					}else{
						myCoupon.html('无优惠券');
					}
				}
			}
		});
	//	restaurantName.html(typeof data.restaurant == 'undefined' ? '--' : data.restaurant.name);
		name.html(typeof data.name == 'undefined' ? '--' : data.name);
		mobile.html(typeof data.mobile == 'undefined' ? '--' : data.mobile);
		point.html(typeof data.point == 'undefined' ? '--' : data.point);
		totalBalance.html(typeof data.totalBalance == 'undefined' ? '--' : checkDot(data.totalBalance)?parseFloat(data.totalBalance).toFixed(2) : data.totalBalance);
		extraBalance.html(typeof data.extraBalance == 'undefined' ? '--' : checkDot(data.extraBalance)?parseFloat(data.extraBalance).toFixed(2) : data.extraBalance);
		baseBalance.html(typeof data.baseBalance == 'undefined' ? '--' : checkDot(data.baseBalance)?parseFloat(data.baseBalance).toFixed(2) : data.baseBalance);
		typeName.html(typeof data.memberType.name == 'undefined' ? '--' : data.memberType.name);
		typeNameInCard.html(typeof data.memberType.name == 'undefined' ? '未激活' : data.memberType.name);
	//	weixinMemberCard.html(typeof data.memberType.name == 'undefined' ? '未激活' : data.memberType.name);
		defaultMemberDiscount.html(typeof data.memberType.discount != 'undefined' && data.memberType.discount.type != 2 ? data.memberType.discount.name : '');
		memberTotalPoint.html(typeof data.totalPoint == 'undefined' ? '--' : data.totalPoint);
		if(data.birthdayFormat){
			birthday.html(data.birthdayFormat);
		}else{
			$('#birthday_li_member').hide();
		}
		
	}
	
	//绑定会员	
	$('#bind_div_member').toggle(
		function(){
			$('#bindMember_div_member').show();
			$('html, body').animate({scrollTop: 200}, 'fast'); 
		},
		function(){
			$('#bindMember_div_member').hide();
		}
	);
	
	//'确认绑定'Click
	$('#bind_a_member').click(function(){
		var mobile = $('#mobile_input_member').val().trim();
		if(!/^1[3,5,8][0-9]{9}$/.test(mobile)){
			Util.dialog.show({
				msg: '请输入 11 位纯数字的有效手机号码',
				callback : function(){
					$('#mobile_input_member').select();
				}
			});
			return;
		}
		
		var name = $('#name_input_member').val().trim();
		if(!/^([^x00-xff]{2,16})|([a-zA-z][a-zA-z0-9]{3,17})$/.test(name)){
			Util.dialog.show({
				msg: '请输入至少两个中文字或字母开头4至18位的会员名称',
				callback : function(){
					$('#name_input_member').select();
				}
			});
			return;
		}
		
		var birthday = $('#birthday_input_member').val();
		
		$.ajax({
			url : '../../WXOperateMember.do',
			type : 'post',
			data : {
				dataSource : 'bind',
				oid : Util.mp.oid,
				fid : Util.mp.fid,
				mobile : mobile,
				name : name,
				birthday : birthday
			},
			dataType : 'json',
			success : function(data, status, xhr){
				if(data.success){
					Util.dialog.show({msg: '绑定成功', callback : function(){
						$('#bind_div_member').hide();
						$('#bindMember_div_member').hide();
						$('#phone_div_member').show();
						if(name.length != 0){
							$('#name_span_member').html(name);
						}
						$('#mobile_span_member').html(mobile);
					}});
				}else{
					Util.dialog.show({msg: data.msg});
				}					
			}
		});
		
	});
	
	//我的优惠券
	$('#myCoupon_li_member').click(function(){
		var mainView = $('#coupons_div_member');
		
		function showCoupons(){

			Util.lm.show();
			$.ajax({
				url : '../../WxOperateCoupon.do',
				type : 'post',
				data : {
					dataSource : 'getByCond',
					status : 'issued',
					expired : false,
					oid : Util.mp.oid,
					fid : Util.mp.fid
				},
				dataType : 'json',
				success : function(data, status, xhr){
					Util.lm.hide();
					if(data.success){
						var couponAmount = 0;
						if(data.root.length > 0){
							var templet = '<div class="box" promotion-id="{promotionId}">' +
											'<div class="box_in"><img src="{couponImg}"></div>' +
											'<span>{name}</span><br><span>面额 : {cPrice} 元</span><br><span>到期 : {expiredTime}</span><br><span>来自 : {promotionName}</span>' +
			  							  '</div>';
							var html = [];
						
							for(var i = 0; i < data.root.length; i++){
								var coupon = data.root[i];
								html.push(templet.format({
									couponImg : coupon.couponType.ossImage ? coupon.couponType.ossImage.image : 'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg',
									name : coupon.couponType.name,
									cPrice : coupon.couponType.price,
									expiredTime : coupon.couponType.expiredFormat,
									promotionName : coupon.promotion.title,
									promotionId : coupon.promotion.id
								}));
								couponAmount++;
							}
							mainView.html(html);
							
							//点击优惠券的处理事件
							mainView.find('.box').each(function(index, element){
								element.onclick = function(){
									Util.jump('sales.html?pid=' + $(element).attr('promotion-id'));
								}
							});
							
						}else{
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
		mainView.fadeToggle(function(){
			if(mainView.css('display') == 'block'){
				$('html, body').animate({scrollTop: 0});
				$('html, body').animate({scrollTop: 310 + $('#divMemberPointContent').height() + $('#divMemberBalanceContent').height() + $('#divMemberTypeContent').height()}, 'fast');			
				showCoupons();
			}else{
				mainView.html('');
			}
		});
	});
	
	//优惠券消费记录
	$('#couponDetails_li_member').click(function(){
		var mainView = $('#couponDetailItems_div_member');
		var tbody = mainView.find('table > tbody');
	
		// 加载近5条消费记录
		function showCouponDetails(){
			Util.lm.show();
			$.ajax({
				url : '../../WxOperateCoupon.do',
				type : 'post',
				data : {
					dataSource : 'getDetails',
					status : 'order_use',
					limit : 5,
					oid : Util.mp.oid,
					fid : Util.mp.fid
				},
				dataType : 'json',
				success : function(data, status, xhr){
					Util.lm.hide();
					if(data.success){
						if(data.root.length > 0){
							var template = '<tr class="d-list-item-coupon">' +
											'<td>{time}</td>' +
											'<td>{useMode}</td>' +
											'<td>{couponMoney}</td>' +
										  '</tr>';
							var html = [];
							for(var i = 0; i < data.root.length; i++){
								var temp = data.root[i];
								html.push(template.format({
									time : fnDateInChinese(temp.operateDate) + '</br><div style="font-size:13px;text-align:center;">' + temp.couponName +'</div>',
									useMode : temp.operateText + '</br><font style="font-size:13px;">(' + temp.associateId + ')</font>',
									couponMoney : (checkDot(temp.couponPrice) ? parseFloat(temp.couponPrice).toFixed(2) : temp.couponPrice) + '元'
								}));
							}
							tbody.html(html);
						}else{
							tbody.html('暂无优惠券使用记录.');
						}
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
		
		mainView.fadeToggle(function(){
			if(mainView.css('display') == 'block'){
				$('html, body').animate({scrollTop : $('#couponDetailItems_div_member').offset().top}, 'fast');
				showCouponDetails();
			}else{
				tbody.html('');
			}
		});
	});
	
	//消费记录
	$('#consumeDetails_li_member').click(function(){
		var mainView = $('#consumeDetails_div_member');
		var tbody = mainView.find('table > tbody');
		
		function showConsumeDetails(){
		
			// 加载近5条消费记录
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
						if(data.root.length > 0){
							var template = '<tr class="d-list-item-consume">' +
											'<td style="text-align: center;">{date}</td>' +
											'<td>{balance}</td>' +
											'<td>{point}</td>' +
										   '</tr>';
							var html = [], temp = null;
							for(var i = 0; i < data.root.length; i++){
								temp = data.root[i];
								html.push(template.format({
									date : fnDateInChinese(temp.operateDateFormat) + '</br><font style="font-size:13px;">账单号:' + temp.orderId + '</font>',
									balance : (checkDot(temp.deltaTotalMoney)?parseFloat(temp.deltaTotalMoney).toFixed(2) : temp.deltaTotalMoney) + '元',
									point : temp.deltaPoint.toFixed(0) + '分'
								}));
							}
							tbody.html(html.join(''));
						}else{
							tbody.html('暂无消费记录');
						}
					}else{
						Util.dialog.show({title: data.title, msg: data.msg});						
					}
				},
				error : function(data, errotType, eeor){
					Util.lm.hide();
					Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
				}
			});
		}
	
		mainView.fadeToggle(function(){
			if(mainView.css('display') == 'block'){
				$('html, body').animate({scrollTop: 0}, 'fast'); 
				$('html, body').animate({scrollTop: 300}, 'fast'); 
				showConsumeDetails();
			}else{
				tbody.html('');
			}
		});
	});
});
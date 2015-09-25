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
				
				//FIXME to delete
				$('#bind_div_member').show();
				$('#phone_div_member').hide();
				//------------------------------------
				
				$.ajax({
					url : '../../WXQueryMemberOperation.do',
					type : 'post',
					data : {
						dataSource : 'chargeAndPointTitle',
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
							$('#divMemberCouponConsume').css('display', 'block');
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
						$('#li_myCoupon').click();
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
		var typeName = $('#spanMemberTypeName');
		var typeNameInCard = $('#divMemberTypeName');
	//	var weixinMemberCard = $('#divWXMemberCard');
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
			Util.dialog.show({msg: '请输入 11 位纯数字的有效手机号码'});
			return;
		}
		var name = $('#name_input_member').val().trim();
		
		$.ajax({
			url : '../../WXOperateMember.do',
			type : 'post',
			data : {
				dataSource : 'bind',
				oid : Util.mp.oid,
				fid : Util.mp.fid,
				mobile : mobile,
				name : name
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
	
});
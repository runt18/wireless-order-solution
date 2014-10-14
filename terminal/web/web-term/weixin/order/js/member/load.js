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
				$('#divWXMemberCard').html('微信卡号:'+data.other.weixinCard);
//				member_loadMemberTypeChart();
				if(data.other.status == 2){
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
					currentMemberLevelData = {y : 0, memberTypeName : '您的积分', currentPoint:true, x:member.point, pointThreshold:member.point, discount:{type :2},chargeRate:-1, exchangeRate:-1, marker:{symbol:'url(../../images/currentPosition.png)'}, color : 'red', dataLabels : {x:-1, align : 'right', style : {fontWeight: 'bold',color: 'red'}}};
					
					//设置会员排名
					if(member.rank > 0){
						$('#fontDefeatMemberCount').text(member.rank);
						$('#span_currentMemberDefeat').show();
					}
					
				}else if(data.other.status == 1){
					
					var memberType = data.other.memberType[0];
					$('#ulMemberPrivilegeDetail').append(memberType.discount.type != 2 ? '<li>'+ memberType.discount.name +'优惠</li>' : '');	
					$('#ulMemberPrivilegeDetail').append(memberType.chargeRate >1 ? '<li>'+ memberType.chargeRate +'倍充值优惠, 充 100 元送 '+parseInt((memberType.chargeRate-1)*100)+' 元</li>':'');
					$('#ulMemberPrivilegeDetail').append(memberType.exchangeRate >1 ? '<li>'+ memberType.exchangeRate +'倍积分特权, 消费 1 元积 '+memberType.exchangeRate+' 分</li>':'');
					$('#ulMemberPrivilegeDetail').append(typeof memberType.desc != 'undefined' && memberType.desc != '' ? '<li>'+ memberType.desc +'</li>':'');
					
					
					if($('#ulMemberPrivilegeDetail').html()){
						$('#divMemberPrivilegeDetail').css('display', 'block');
					}else{
						$('#divMemberPrivilegeDetail').css('display', 'none');
					}
					
					$('#divMemberCard').css('display', 'block');
					$('#divVerifyAndBind').css('display', 'block');
				}
			}else{
				Util.dialog.show({title:'提示', msg: data.msg});
			}
		},
		error : function(data, errotType, eeor){
			Util.lm.hide();
			Util.dialog.show({title:'错误', msg: '服务器请求失败, 请稍候再试.'});
		}
	});
	
	//优惠劵宽度自适应
/*	var autoWidth = function()
	{
		 calcFloatDivs();
	};
	window.onresize = autoWidth;*/
	
	$.post('../../OperatePromotion.do', {dataSource : 'HaveWelcomePage'}, function(jr){
			if(jr.success && jr.root.length > 0){
				haveWelcomePageId = jr.root[0].id;
			}		
	});
	
	$('#txtVerifyCode').focus(function(){
		$('html, body').animate({scrollTop: 190+($('#divMemberPrivilegeDetail').height()>=30?$('#divMemberPrivilegeDetail').height():-15)}, 'fast'); 
	});
});
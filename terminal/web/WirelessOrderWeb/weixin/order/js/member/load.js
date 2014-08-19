$(function(){
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
				$('#divWXMemberCard').html(data.other.weixinCard);
//				member_loadMemberTypeChart();
				if(data.other.status == 2){
					$('#divMemberCard').css('display', 'block');
					$('#divMemberContent').css('display', 'block');
					$('#divMemberTypeContent').css('display', 'block');
					$('#divMemberPointContent').css('display', 'block');
					$('#divMemberBalanceContent').css('display', 'block');
//					$('#divMemberCouponContent').css('display', 'block');
//					$('#divMemberCouponConsume').css('display', 'block');
					
					member = data.other.member;
					member.restaurant = data.other.restaurant;
					initMemberMsg({data:member});
					
					//添加会员等级当前位置
					currentMemberLevelData = {y : 0, memberTypeName : '您的积分', x:member.point, discount:{type :2},chargeRate:-1, exchangeRate:-1, pointThreshold:member.point, marker:{symbol:'url(../../images/currentPosition.png)'}, color : 'red', dataLabels : {x:-1, align : 'right', style : {fontWeight: 'bold',color: 'red'}}};
					//设置会员排名
					$('#fontDefeatMemberCount').text(member.rank);
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
							if(data.success){
								if(data.root.length > 0){
									$('#divMyCoupon').prepend('<img src="../../images/WXnew.png" style="margin-top: 10px;"></img>');
								}else{
									$('#divMyCoupon img').html('');
								}						
							}
						},
						error : function(data, errotType, eeor){
							Util.dialog.show({msg: '服务器请求失败, 请稍候再试.'});
						}
					});			*/	
				}else if(data.other.status == 1){
					
					var memberType = data.other.memberType[0];
					$('#ulMemberPrivilegeDetail').append('<li>'+ memberType.discount.name +'优惠</li>');	
					$('#ulMemberPrivilegeDetail').append(memberType.chargeRate >1 ? '<li>'+ memberType.chargeRate +'倍充值优惠, 充100送'+(memberType.chargeRate-1)*100+'元</li>':'');
					$('#ulMemberPrivilegeDetail').append(memberType.exchangeRate >1 ? '<li>'+ memberType.exchangeRate +'倍积分特权, 消费1元积'+memberType.exchangeRate+'分</li>':'');
					$('#ulMemberPrivilegeDetail').append(typeof memberType.desc != 'undefined' && memberType.desc != '' ? '<li>'+ memberType.desc +'</li>':'');	
					
					$('#divMemberCard').css('display', 'block');
					$('#divVerifyAndBind').css('display', 'block');
					$('#divMemberPrivilegeDetail').css('display', 'block');
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
	
	var autoWidth = function()
	{
		 calcFloatDivs();
	};
	window.onresize = autoWidth;
	
	$('#txtVerifyCode').focus(function(){
		$('html, body').animate({scrollTop: 200}, 'fast'); 
	});

	
});
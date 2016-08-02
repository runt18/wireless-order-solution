$(function(){
	
	//��Ա�����ж�
	//��
	$('#birthdayMonth_input_member').on('blur', function(){
		var month = parseInt($('#birthdayMonth_input_member').val());
		if(month < 1 || month > 12){
			Util.dialog.show({title:'��ʾ', msg: '�·��������,����������', btn : 'yes', callback : function(){
				$('#birthdayMonth_input_member').val('');
				$('#birthdayMonth_input_member').focus();
			}});
		}
	});
	//��
	$('#birthdayDay_input_member').on('blur', function(){
		var month = parseInt($('#birthdayDay_input_member').val());
		if(month < 1 || month > 31){
			Util.dialog.show({title:'��ʾ', msg: '�����������,����������', btn : 'yes', callback : function(){
				$('#birthdayDay_input_member').val('');
				$('#birthdayDay_input_member').focus();
			}});
		}
	});
	
	//�Ա���
	$('#bindMember_div_member').find('[data-type="personSex"]').each(function(index, element){
		element.onclick = function(){
			if($(element).hasClass('selectedRegion_css_book')){
				$(element).addClass('selectedRegion_css_book');
			}else{
				$('#bindMember_div_member').find('[data-type="personSex"]').removeClass('selectedRegion_css_book');
				$(element).addClass('selectedRegion_css_book');
			}
		}
	});
	
	$('html, body').animate({scrollTop: 0}, 'fast');
//	Util.lbar('', function(html){$(document.body).append(html);  });
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
				//��Ա����
				$('#divWXMemberCard').html('No.' + data.other.weixinCard);
				
				if(data.other.status == 2){
					//�ֻ����Ѱ�״̬
					$('#phone_div_member').show();
					$('#bind_div_member').hide();
				}else if(data.other.status == 1){
					//�ֻ�����δ��״̬
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
						Util.dialog.show({msg: '����������ʱ, ��ˢ��.'});
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
				
				//��ӻ�Ա�ȼ���ǰλ��
				currentMemberLevelData = {y : 0, memberTypeName : '���Ļ���', currentPoint:true, x:member.totalPoint, pointThreshold:member.totalPoint, discount:{type :2},chargeRate:-1, exchangeRate:-1, marker:{symbol:'url(images/currentPosition.png)'}, color : 'red', dataLabels : {x:-1, align : 'right', style : {fontWeight: 'bold',color: 'red'}}};
				
				//���û�Ա����
				if(member.rank > 0){
					$('#fontDefeatMemberCount').text(member.rank);
					$('#span_currentMemberDefeat').show();
				}
				
				var memberType = data.other.member.memberType;
				$('#privilege_ul_member').append(memberType.discount.type != 2 ? '<li>'+ memberType.discount.name +'�Ż�</li>' : '');	
				$('#privilege_ul_member').append(memberType.chargeRate >1 ? '<li>'+ memberType.chargeRate +'����ֵ�Ż�, �� 100 Ԫ�� '+parseInt((memberType.chargeRate-1)*100)+' Ԫ</li>':'');
				$('#privilege_ul_member').append(memberType.exchangeRate >1 ? '<li>'+ memberType.exchangeRate +'��������Ȩ, ���� 1 Ԫ�� '+memberType.exchangeRate+' ��</li>':'');
				$('#privilege_ul_member').append(typeof memberType.desc != 'undefined' && memberType.desc != '' ? '<li>'+ memberType.desc +'</li>':'');
				
				
				if($('#privilege_ul_member').html()){
					$('#privilege_div_member').css('display', 'block');
				}else{
					$('#privilege_div_member').css('display', 'none');
				}					
					
			}else{
				Util.dialog.show({title:'��ʾ', msg: data.msg});
			}
		},
		error : function(data, errotType, err){
			Util.lm.hide();
			Util.dialog.show({title:'����', msg: '����������ʧ��, ���Ժ�����.'});
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
		//����
		$('#memberAge_select_member').val("3");
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
						myCoupon.html(data.root.length + '��');
					}else{
						myCoupon.html('���Ż�ȯ');
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
		typeNameInCard.html(typeof data.memberType.name == 'undefined' ? 'δ����' : data.memberType.name);
	//	weixinMemberCard.html(typeof data.memberType.name == 'undefined' ? 'δ����' : data.memberType.name);
		defaultMemberDiscount.html(typeof data.memberType.discount != 'undefined' && data.memberType.discount.type != 2 ? data.memberType.discount.name : '');
		memberTotalPoint.html(typeof data.totalPoint == 'undefined' ? '--' : data.totalPoint);
		if(data.birthdayFormat){
			birthday.html(data.birthdayFormat);
		}else{
			$('#birthday_li_member').hide();
		}
		
	}
	
	//�󶨻�Ա	
	$('#bind_div_member').toggle(
		function(){
			$('#bindMember_div_member').show();
			$('html, body').animate({scrollTop: 200}, 'fast'); 
		},
		function(){
			$('#bindMember_div_member').hide();
		}
	);
	
	//'ȷ�ϰ�'Click
	$('#bind_a_member').click(function(){
		var mobile = $('#mobile_input_member').val().trim();
		if(!/^1[3,5,8][0-9]{9}$/.test(mobile)){
			Util.dialog.show({
				msg: '������ 11 λ�����ֵ���Ч�ֻ�����',
				callback : function(){
					$('#mobile_input_member').select();
				}
			});
			return;
		}
		
		var name = $('#name_input_member').val().trim();
		if(!/^([^x00-xff]{2,16})|([a-zA-z][a-zA-z0-9]{3,17})$/.test(name)){
			Util.dialog.show({
				msg: '�������������������ֻ���ĸ��ͷ4��18λ�Ļ�Ա����',
				callback : function(){
					$('#name_input_member').select();
				}
			});
			return;
		}
		
		var month = $('#birthdayMonth_input_member').val();
		var day = $('#birthdayDay_input_member').val();
		if(month == '' || day == ''){
			Util.dialog.show({
				msg: '���ղ���Ϊ��',
				callback : function(){
					$('#birthdayMonth_input_member').focus();
				},
				btn: 'yes'
			});
			return;
		}
		
		var birthday = month + '-' + day;
		var age = $('#memberAge_select_member').val();
		var sex;
		$('#bindMember_div_member').find('[data-type="personSex"]').each(function(index, element){
			if($(element).hasClass('selectedRegion_css_book')){
				sex = $(element).attr('data-value');
			}
		});
		
		
		$.ajax({
			url : '../../WXOperateMember.do',
			type : 'post',
			data : {
				dataSource : 'bind',
				oid : Util.mp.oid,
				fid : Util.mp.fid,
				mobile : mobile,
				name : name,
				birthday : birthday,
				sex : sex,
				age : age
			},
			dataType : 'json',
			success : function(data, status, xhr){
				if(data.success){
					Util.dialog.show({msg: '�󶨳ɹ�', callback : function(){
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
	
	//�ҵ��Ż�ȯ
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
											'<span>{name}</span><br><span>��� : {cPrice} Ԫ</span><br><span>���� : {expiredTime}</span><br><span>���� : {promotionName}</span>' +
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
							
							//����Ż�ȯ�Ĵ����¼�
							mainView.find('.box').each(function(index, element){
								element.onclick = function(){
									Util.jump('sales.html?pid=' + $(element).attr('promotion-id'));
								}
							});
							
						}else{
							mainView.html('�����Ż�ȯ');
						}
						member.couponCount = couponAmount;
						
					}else{
						Util.dialog.show({title: data.title, msg: data.msg});						
					}
				},
				error : function(data, errotType, eeor){
					Util.lm.hide();
					Util.dialog.show({msg: '����������ʧ��, ���Ժ�����.'});
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
	
	//�Ż�ȯ���Ѽ�¼
	$('#couponDetails_li_member').click(function(){
		var mainView = $('#couponDetailItems_div_member');
		var tbody = mainView.find('table > tbody');
	
		// ���ؽ�5�����Ѽ�¼
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
									couponMoney : (checkDot(temp.couponPrice) ? parseFloat(temp.couponPrice).toFixed(2) : temp.couponPrice) + 'Ԫ'
								}));
							}
							tbody.html(html);
						}else{
							tbody.html('�����Ż�ȯʹ�ü�¼.');
						}
					}else{
						Util.dialog.show({title: data.title, msg: data.msg});						
					}
				},
				error : function(data, errotType, eeor){
					Util.lm.hide();
					Util.dialog.show({msg: '����������ʧ��, ���Ժ�����.'});
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
	
	//���Ѽ�¼
	$('#consumeDetails_li_member').click(function(){
		var mainView = $('#consumeDetails_div_member');
		var tbody = mainView.find('table > tbody');
		
		function showConsumeDetails(){
		
			// ���ؽ�5�����Ѽ�¼
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
									date : fnDateInChinese(temp.operateDateFormat) + '</br><font style="font-size:13px;">�˵���:' + temp.orderId + '</font>',
									balance : (checkDot(temp.deltaTotalMoney)?parseFloat(temp.deltaTotalMoney).toFixed(2) : temp.deltaTotalMoney) + 'Ԫ',
									point : temp.deltaPoint.toFixed(0) + '��'
								}));
							}
							tbody.html(html.join(''));
						}else{
							tbody.html('�������Ѽ�¼');
						}
					}else{
						Util.dialog.show({title: data.title, msg: data.msg});						
					}
				},
				error : function(data, errotType, eeor){
					Util.lm.hide();
					Util.dialog.show({msg: '����������ʧ��, ���Ժ�����.'});
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
	
	
	 //�������
	  $('#pickOrderFood_a_member').click(function(){
		  Util.jump('food.html');
	  });
	
});

	
function MemberReadPopup(param){
	
	param = param || {
		confirm : function(member){}	//会员读取后的确定事件
	};
	
	var _self = this;
	var _member = null;
	
	var _popupInstance = null;
	_popupInstance = new JqmPopup({
		loadUrl : './popup/member/read.html',
		pageInit : function(self){
			//设置button样式
			self.find('[id=read_a_memberRead]').attr("data-theme", "b");
			self.find('[id=read_a_memberRead]').buttonMarkup("refresh");
			$('#link_payment_popupDiscountCmp4Member').attr("data-theme", "e");
			$('#link_payment_popupDiscountCmp4Member').buttonMarkup("refresh");
			$('#link_payment_popupPricePlanCmp4Member').attr("data-theme", "e");
			$('#link_payment_popupPricePlanCmp4Member').buttonMarkup("refresh");
			$('#link_payment_popupCouponCmp4Member').attr("data-theme", "e");
			$('#link_payment_popupCouponCmp4Member').buttonMarkup("refresh");	
			
			//取消按钮
			self.find('[id=cancel_a_memberRead]').click(function(){
				_self.close();
			});
			
			//确定按钮
			self.find('[id=confirm_a_memberRead]').click(function(){
				if(param.confirm && typeof param.confirm == 'function'){
					param.confirm(_member);
				}
			});
			
			//读取会员
			self.find('[id=read_a_memberRead]').click(function(){
				readMemberByCondtion(MemberReadPopup.SearchType.FUZZY, self);
			});
		}
	});	
	
	this.open = function(afterOpen){

		_popupInstance.open(function(self){
			//查找输入框关联数字键盘
			NumKeyBoardAttacher.instance().attach(self.find('[id=fuzzy_input_memberRead]')[0]);
			self.find('[id=fuzzy_input_memberRead]').focus();
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		});
	};
	
	this.close = function(afterOpen, timeout){
		_popupInstance.close(function(self){
			NumKeyBoardAttacher.instance().detach(self.find('[id=fuzzy_input_memberRead]')[0]);
			if(afterOpen && typeof afterOpen == 'function'){
				if(timeout){
					setTimeout(afterOpen, timeout);
				}else{
					afterOpen();
				}
			}
		});
	};
	
	//读取会员信息
	function readMemberByCondtion(searchType, self){
		
		var memberInfo = self.find('[id=fuzzy_input_memberRead]');
		
		if(!memberInfo.val()){
			Util.msg.alert({msg : '请填写会员相关信息', topTip : true});
			memberInfo.focus();
			return;
		}
		
		Util.LM.show();
		$.ajax({
			url : "../QueryMember.do",
			type : 'post',
			data : {
				dataSource:'normal',
				sType: searchType.val > 0 ? searchType.valueOf() : '',
				forDetail : true,
				memberCardOrMobileOrName : memberInfo.val()
			},
	//		async : false,
			dataType : 'json',
			success : function(jr, status, xhr){
				Util.LM.hide();
				if(jr.success){
					if(jr.root.length == 1){
						Util.msg.alert({msg : '会员信息读取成功.', topTip : true});
						loadMemberInfo(jr.root[0]);
						
					}else if(jr.root.length > 1){
						//FIXME
						$('#payment_searchMemberType').popup().popup('open');
						$('#payment_searchMemberType').css({top:$('#btnReadMember').position().top - 270, left:$('#btnReadMember').position().left-300});
						$('#payment_searchMemberTypeCmp').listview().listview('refresh');
						
					}else{
//						Util.msg.alert({msg:'该会员信息不存在, 请重新输入条件后重试.', renderTo : 'paymentMgr', fn : function(){
//							memberInfo.focus();
//						}});
						Util.msg.alert({msg : '该会员信息不存在, 请重新输入条件后重试', topTip : true});
						setTimeout(function(){
							memberInfo.focus();
						}, 200);
					}
				}else{
					alert(jr.msg);
				}
			},
			error : function(request, status, err){
				alert(err);
			}
		}); 	
	}
	
	function loadMemberInfo(member){
		$('#name_lable_memberRead').text(member.name);
		$('#memberType_label_memberRead').text(member.memberType.name);
		$('#balance_label_memberRead').text(member.totalBalance);
		$('#point_label_memberRead').text(member.point);
		$('#phone_label_memberRead').text(member.mobile?member.mobile:'----');
		$('#payment4MemberCard').text(member.memberCard?member.memberCard:'----');	
		
		$('#defaultDiscount_label_memberRead').text(member.memberType.discount.name);
		$('#defaultDiscount_label_memberRead').attr('data-value', member.memberType.discount.id);
		
		var discounts = member.memberType.discounts;
		
		var discountHtml = '', pricePlanHtml = '';
		for (var i = 0; i < discounts.length; i++) {
			discountHtml += '<li data-icon="false" class="popupButtonList" onclick="chooseMemberDiscount({id:'+ discounts[i].id +',name:\''+ discounts[i].name +'\'})"><a >'+ discounts[i].name +'</a></li>';
		}
		$('#payment_discountList4Member').html(discountHtml).trigger('create');
		
		$('#link_payment_popupDiscountCmp4Member').click(function(){
			readMemberWinToSelectDiscount();
		});

		//价格方案
		var pricePlans = member.memberType.pricePlans;
		if(pricePlans.length > 0){
			$('#defaultPricePlan_label_memberRead').text(member.memberType.pricePlan.name);
			$('#defaultPricePlan_label_memberRead').attr('data-value', member.memberType.pricePlan.id);
			for (var i = 0; i < pricePlans.length; i++) {
				pricePlanHtml += '<li data-icon="false" class="popupButtonList" onclick="chooseMemberPricePlan({id:'+ pricePlans[i].id +',name:\''+ pricePlans[i].name +'\'})"><a >'+ pricePlans[i].name +'</a></li>';
			}
			$('#payment_pricePlanList4Member').html(pricePlanHtml).trigger('create');
		}
		
		_member = member;
	}
	
	function readMemberWinToSelectDiscount(){
		//$('#payment_popupDiscountCmp4Member').popup().popup('open');
		$('#payment_popupDiscountCmp4Member').show();
		$('#payment_popupDiscountCmp4Member').css({top:$('#link_payment_popupDiscountCmp4Member').position().top - 270, left:$('#link_payment_popupDiscountCmp4Member').position().left-300});
		$('#payment_discountList4Member').listview().listview('refresh');	
	}

	function readMemberWinToSelectPricePlan(){
		$('#payment_popupPricePlanCmp4Member').popup().popup('open');
		$('#payment_popupPricePlanCmp4Member').css({top:$('#link_payment_popupPricePlanCmp4Member').position().top - 270, left:$('#link_payment_popupPricePlanCmp4Member').position().left-300});
		$('#payment_pricePlanList4Member').listview().listview('refresh');	
	}
}

MemberReadPopup.SearchType = {
	FUZZY : { val : 0, desc : '模糊查找' },
	MOBILE : { val : 1, desc : '按电话号码' },
	WX_CARD : { val : 2, desc : '按微信卡号' },
	CARD : { val : 3, desc : '按卡号' }
};
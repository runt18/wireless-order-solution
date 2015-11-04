
	
function MemberReadPopup(param){
	
	param = param || {
		confirm : function(member, discount, pricePlan){}	//会员读取后的确定事件
	};
	
	var _self = this;
	var _member = null;
	var _selectedDiscount = { id : null };
	var _selectedPricePlan = { id : null };
	
	var _popupInstance = null;
	_popupInstance = new JqmPopup({
		loadUrl : './popup/member/read.html',
		pageInit : function(self){
			//设置button样式
			self.find('[id=read_a_memberRead]').attr("data-theme", "b");
			self.find('[id=read_a_memberRead]').buttonMarkup("refresh");
			$('#selectDiscount_a_memberRead').attr("data-theme", "e");
			$('#selectDiscount_a_memberRead').buttonMarkup("refresh");
			$('#selectPlanPrice_a_memberRead').attr("data-theme", "e");
			$('#selectPlanPrice_a_memberRead').buttonMarkup("refresh");
			//取消按钮
			self.find('[id=cancel_a_memberRead]').click(function(){
				_self.close();
			});
			
			//确定按钮
			self.find('[id=confirm_a_memberRead]').click(function(){
				if(param.confirm && typeof param.confirm == 'function'){
					param.confirm(_member, _selectedDiscount, _selectedPricePlan);
				}
			});
			
			//读取会员
			self.find('[id=read_a_memberRead]').click(function(){
				readMemberByCondtion(MemberReadPopup.SearchType.FUZZY, self);
			});
			
			self.find('[id=fuzzy_input_memberRead]').on('keypress', function(event){
				if(event.keyCode == "13"){
					readMemberByCondtion(MemberReadPopup.SearchType.FUZZY, self);
					$('#selectSearch_div_memberRead').hide();
					$('#eachSearch_ul_memberRead').hide();
				}
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
				sType: searchType.val > 0 ? searchType.val : '',
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
						loadMemberInfo(jr.root[0], self);
						$('#selectSearch_div_memberRead').hide();
						$('#eachSearch_ul_memberRead').hide();
					}else if(jr.root.length > 1){
						$('#selectSearch_div_memberRead').show();
						$('#eachSearch_ul_memberRead').show();
						$('#eachSearch_ul_memberRead').find('.popupButtonList').each(function(index, element){
							element.onclick = function(){
								if($(element).attr('data') == 'selectSearchByTel'){
									readMemberByCondtion(MemberReadPopup.SearchType.MOBILE, self);
									console.log($(element).attr('data'));
								}else if($(element).attr('data') == 'selectSearchByNum'){
									readMemberByCondtion(MemberReadPopup.SearchType.CARD, self);
									console.log($(element).attr('data'));
								}else if($(element).attr('data') == 'selectSearchByWeixin'){
									readMemberByCondtion(MemberReadPopup.SearchType.WX_CARD, self);
									console.log($(element).attr('data'));
								}
								$('#selectSearch_div_memberRead').hide();
								$('#eachSearch_ul_memberRead').hide();
							};
						});
					}else{
//						Util.msg.alert({msg:'该会员信息不存在, 请重新输入条件后重试.', renderTo : 'paymentMgr', fn : function(){
//							memberInfo.focus();
//						}});
						Util.msg.alert({msg : '该会员信息不存在, 请重新输入条件后重试', topTip : true});
						setTimeout(function(){
							memberInfo.select();
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
	
	function loadMemberInfo(member, self){
		self.find('[id=name_lable_memberRead]').text(member.name);
		self.find('[id=memberType_label_memberRead]').text(member.memberType.name);
		self.find('[id=balance_label_memberRead]').text(member.totalBalance);
		self.find('[id=point_label_memberRead]').text(member.point);
		self.find('[id=phone_label_memberRead]').text(member.mobile ? member.mobile : '----');
		self.find('[id=payment4MemberCard]').text(member.memberCard ? member.memberCard : '----');	
		
		self.find('[id=defaultDiscount_label_memberRead]').text(member.memberType.discount.name);
		self.find('[id=defaultDiscount_label_memberRead]').attr('data-value', member.memberType.discount.id);
		_selectedDiscount = member.memberType.discount;
	
		var discounts = member.memberType.discounts;
		
		var discountHtml = '', pricePlanHtml = '';
		for (var i = 0; i < discounts.length; i++) {
			discountHtml += '<li data-icon="false" class="popupButtonList" data-index="' + i + '"><a>' + discounts[i].name +'</a></li>';
		}
		self.find('[id=eachDiscount_ul_memberRead]').html(discountHtml).trigger('create');
		self.find('[id=eachDiscount_ul_memberRead] .popupButtonList').each(function(index, element){
			element.onclick = function(){
				_selectedDiscount = discounts[($(element).attr('data-index'))];
				self.find('[id=defaultDiscount_label_memberRead]').text($(element).text());
				self.find('[id=eachDiscount_ul_memberRead]').hide();
				
			};
		});
		
		//选择折扣方案
		self.find('[id=selectDiscount_a_memberRead]').click(function(){
			readMemberWinToSelectDiscount(self);
		});

		//价格方案
		var pricePlans = member.memberType.pricePlans;
		if(pricePlans.length > 0){
			self.find('[id=defaultPricePlan_label_memberRead]').text(member.memberType.pricePlan.name);
			_selectedPricePlan = member.memberType.pricePlan;
			self.find('[id=defaultPricePlan_label_memberRead]').attr('data-value', member.memberType.pricePlan.id);
			for (var i = 0; i < pricePlans.length; i++) {
				pricePlanHtml += '<li data-icon="false" class="popupButtonList" data-index="' + i + '" ><a>'+ pricePlans[i].name +'</a></li>';
			}
			self.find('[id=eachPricePlan_ul_memberRead]').html(pricePlanHtml).trigger('create');
			
			self.find('[id=eachPricePlan_ul_memberRead] .popupButtonList').each(function(index, element){
				element.onclick = function(){
					_selectedPricePlan = pricePlans[($(element).attr('data-index'))];
					self.find('[id=defaultPricePlan_label_memberRead]').text($(element).text());
					self.find('[id=eachPricePlan_ul_memberRead]').hide();
				}
			});
		}
		
		//选择价格方案
		self.find('[id=selectPlanPrice_a_memberRead]').click(function(){
			readMemberWinToSelectPricePlan(self);
		});
		
		_member = member;
	}
	
	function readMemberWinToSelectDiscount(self){
		//$('#discounts_div_memberRead').popup().popup('open');
		self.find('[id=discounts_div_memberRead]').show();
		self.find('[id=eachDiscount_ul_memberRead]').show();
		self.find('[id=pricePlan_div_memberRead]').hide();
		self.find('[id=eachPricePlan_ul_memberRead]').hide();
		self.find('[id=eachDiscount_ul_memberRead]').listview().listview('refresh');	
	}

	function readMemberWinToSelectPricePlan(self){
		self.find('[id=pricePlan_div_memberRead]').show();
		self.find('[id=eachPricePlan_ul_memberRead]').show();
		self.find('[id=discounts_div_memberRead]').hide();
		self.find('[id=eachDiscount_ul_memberRead]').hide();
		self.find('[id=eachPricePlan_ul_memberRead]').listview().listview('refresh');
	}
}

MemberReadPopup.SearchType = {
	FUZZY : { val : 0, desc : '模糊查找' },
	MOBILE : { val : 1, desc : '按电话号码' },
	WX_CARD : { val : 2, desc : '按微信卡号' },
	CARD : { val : 3, desc : '按卡号' }
};
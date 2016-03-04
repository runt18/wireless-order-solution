

function MemberRechargePopup(){
	
	var _self = this;
	var _rechargePopup = null;
	//保持充值会员对象
	var _rechargeMember = null;
	//充值比率
	var _chargeRate = null;
	_rechargePopup = new JqmPopup({
		loadUrl : './popup/member/recharge.html',
		pageInit : function(self){
			
			self.find('[id=container_div_recharge]').trigger('create').trigger('refresh');
			
			//会员读取按钮
			self.find('[id=loadMember_button_recharge]').click(function(){
				readMemberMsg(self);
			});
			
			//绑定enter键
			self.find('[id=loadMember_input_recharge]').focus(function(){
				self.find('[id=loadMember_input_recharge]').keypress(function(event){
					if(event.keyCode == 13){ //keyCode == 13 为enter键
						readMemberMsg(self);
					}
				});
			});
			
			//充值按钮
			self.find('[id=toRecharge_a_recharge]').click(function(){
				meberRechargeCheck(self);
			});
			
			//取消按钮
			self.find('[id=canelRecharge_a_recharge]').click(function(){
				_self.close();
			});
			
			//金额联动
			self.find('[id=payMannerMoney_input_recharge]').keyup(function(){
				getCharge(self.find('[id=payMannerMoney_input_recharge]'), self.find('[id=rechargeMoney_input_recharge]'), _chargeRate);
			});
			
			//读取短信数量
			if(Util.sys.smsModule){
				self.find('[id=td4ChbSendCharge]').show();
				self.find('[id=rechargeMsgs_label_recharge]').html('发送充值信息'+(Util.sys.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Util.sys.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Util.sys.smsCount+'条, 请及时充值</font>)'));
			}
			
			//读取缓存的选项
			if(getcookie(document.domain+'_chargeSms') == 'true'){
				self.find('[id=sendReChargeMsg_check_recharge]').attr('checked', true).checkboxradio("refresh");
			}else{
				self.find('[id=sendReChargeMsg_check_recharge]').attr('checked', false).checkboxradio("refresh");
			}
			
		}
	});
	
	this.open = function(afterOpen){
		_rechargePopup.open(function(self){		
			
			NumKeyBoardAttacher.instance().attach(self.find('[id=loadMember_input_recharge]')[0]);
			NumKeyBoardAttacher.instance().attach(self.find('[id=payMannerMoney_input_recharge]')[0], function(){
				self.find('[id=payMannerMoney_input_recharge]').keyup();
			});
			NumKeyBoardAttacher.instance().attach(self.find('[id=rechargeMoney_input_recharge]')[0]);
			
			self.find('[id=loadMember_input_recharge]').focus();
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}	
		});
	};
	
	this.close = function(afterClose, timeout){
		_rechargePopup.close(function(self){
			NumKeyBoardAttacher.instance().detach(self.find('[id=loadMember_input_recharge]')[0]);
			NumKeyBoardAttacher.instance().detach(self.find('[id=payMannerMoney_input_recharge]')[0]);
			NumKeyBoardAttacher.instance().detach(self.find('[id=rechargeMoney_input_recharge]')[0]);
		});
	};
	
	//读取会员信息
	function readMemberMsg(self, searchType){
		var memberInfo = self.find('[id=loadMember_input_recharge]');//获取输入框
		if(!memberInfo.val()){
			Util.msg.tip('请填写会员相关信息');
			memberInfo.focus();
			return;
		}
		
		//searchType : 手机会员value=1   微信会员value=2  实卡会员value=3
		if(searchType){
			self.find('[id=searchMemberType_div_recharge]').hide();
			self.find('[id=searchMemberTypeCmp_ul_recharge]').hide();
		}else{
			searchType = '';
		}
		
		Util.LM.show();
		
		$.ajax({
			url : "../QueryMember.do",
			type : 'post',
			data : {
				dataSource :'normal',
				sType : searchType,
				forDetail : true,
				memberCardOrMobileOrName : memberInfo.val()
			},
			dataType : 'json',
			success : function(response, status, xhr){
				Util.LM.hide();
				if(response.success){
					if(response.root.length == 1){
						Util.msg.tip('会员信息读取成功.');
						_rechargeMember = response.root[0];
						_chargeRate = response.root[0].memberType.chargeRate ? response.root[0].memberType.chargeRate : 1;
						writeMemberMsg(self, response.root[0]);
					}else if(response.root.length > 1){
						self.find('[id=searchMemberType_div_recharge]').show();
						self.find('[id=searchMemberTypeCmp_ul_recharge]').show();
						self.find('[id=searchMemberTypeCmp_ul_recharge]').find('li').each(function(index, element){
							$(element).click(function(){
								readMemberMsg(self,$(element).val());
							});
						});
					}else{
						Util.msg.tip('该会员信息不存在, 请重新输入条件后重试.');
						memberInfo.select();
						memberInfo.focus();
					}
				}else{
					Util.msg.tip(response.msg);
				}
			},
			error : function(request, status, err){
				Util.msg.tip('读取错误.');
			}
		});
	}
	
	//显示会员信息
	function writeMemberMsg(self,member){
		var memberType = member.memberType ? member.memberType : {};
		
		self.find('[id=payMannerMoney_input_recharge]').val('');
		self.find('[id=rechargeMoney_input_recharge]').val('');
		
		self.find('[id=baseBalance_label_recharge]').text(member.baseBalance);
		self.find('[id=extraBalance_label_recharge]').text(member.extraBalance);
		self.find('[id=totalBalance_label_recharge]').text(member.totalBalance);
		self.find('[id=memberName_label_recharge]').text(member.name);
		self.find('[id=memberType_label_recharge]').text(memberType.name);
		self.find('[id=memberSex_label_recharge]').text(member.sexText);	
		
		self.find('[id=memberMobileNum_label_recharge]').text(member.mobile ? member.mobile : '----');
		self.find('[id=memberCard_label_recharge]').text(member.memberCard ? member.memberCard : '----');	
		self.find('[id=weixinMemberCard_label_recharge]').text(member.weixinCard ? member.weixinCard : '----');
		
	}
	
	
	//充值功能
	function meberRechargeCheck(self){
		if(_rechargeMember == null || typeof _rechargeMember == 'undefined'){
			Util.msg.tip('未读取会员信息, 请先刷卡.');
			return;
		}
		if(_rechargeMember.memberType.attributeValue != 0){
			Util.msg.tip('积分属性会员不允许充值, 请重新刷卡.');
			return;
		}
	
		var rechargeMoney = self.find('[id=rechargeMoney_input_recharge]');
		var rechargeType = self.find('[id=rechargeType_select_recharge]');
		var payMannerMoney = self.find('[id=payMannerMoney_input_recharge]');
		
		if(!rechargeMoney.val()){
			Util.msg.tip('请输入充值金额');
			return;
		}
	
		if(!payMannerMoney.val()){
			Util.msg.tip('请输入账户充额');
			return;		
		}
		
		Util.LM.show();
		
		$.post('../OperateMember.do', {
			dataSource : 'charge',
			memberID : _rechargeMember.id,
			rechargeMoney : rechargeMoney.val(),
			rechargeType : rechargeType.val(),
			payMannerMoney : payMannerMoney.val(),
			isPrint : self.find('[id=printRecharge_checkbox_recharge]').attr('checked') ? true : false,
			sendSms : self.find('[id=sendReChargeMsg_check_recharge]').attr('checked') ? true : false,
			orientedPrinter : getcookie(document.domain + '_printers')
		}, function(response){
			Util.LM.hide();
			if(response.success){
				if(self.find('[id=sendReChargeMsg_check_recharge]').attr('checked')){
					setcookie(document.domain+'_chargeSms', true);
				}else{
					delcookie(document.domain+'_chargeSms');
				}
				_self.close();
				//更新短信
				Util.sys.checkSmStat();
				Util.msg.tip(response.msg);
			}else{
				Util.msg.tip(response.msg);
			}		
		});
	}
	
	
	//金额联动
	function getCharge(basePay, getBalance, rate){
		if(basePay.val()){
			if(rate){
				getBalance.val(parseInt(basePay.val()) * parseInt(rate));
			}else{
				getBalance.val(basePay.val());
			}
		}else{
			getBalance.val(0);
		}
	}
}



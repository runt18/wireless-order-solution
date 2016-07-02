define(function(require, exports, module){
	function MemberTakeMoneyPopup(){
		var _self = this;
		var _takeMoneyPopup = null;
		var _takeMoneyMember = null;
		//防止连点下单
		var _isProcessing = false;
		
		
		_takeMoneyPopup = new JqmPopup({
			loadUrl : './popup/member/takeMoney.html',
			pageInit : function(self){
				
				self.find('[id=container_div_takeMoney]').trigger('create').trigger('refresh');
				
				//会员读取按钮
				self.find('[id=loadMember_button_takeMoney]').click(function(){
					readMemberMsg(self);
				});
				
				//绑定enter键
				self.find('[id=loadMember_input_takeMoney]').focus(function(){
					self.find('[id=loadMember_input_takeMoney]').keypress(function(event){
						if(event.keyCode == 13){ //keyCode == 13 为enter键
							readMemberMsg(self);
						}
					});
				});
				
				//充值按钮
				self.find('[id="takeMoney_a_takeMoney"]').click(function(){
					
					if(!_isProcessing){
						_isProcessing = true;
						memberTakeMoney(self);
					}
				});
				
				
				//取消按钮
				self.find('[id=canelTakeMoney_a_takeMoney]').click(function(){
					_self.close();
				});
				
				//金额联动
				self.find('[id=payTakeMoney_input_takeMoney]').keyup(function(){
					getTakeMoney(self.find('[id=payTakeMoney_input_takeMoney]'), self.find('[id=takeMoneyMoney_input_takeMoney]'));
				});
				
				//读取缓存的选项
				if(getcookie(document.domain+'_takeMoney') == 'true'){
					self.find('[id=printTakeMoney_checkbox_takeMoney]').attr('checked', true).checkboxradio("refresh");
				}else{
					self.find('[id=printTakeMoney_checkbox_takeMoney]').attr('checked', false).checkboxradio("refresh");
				}
			
			}
		});
		
		this.open = function(afterOpen){
			_takeMoneyPopup.open(function(self){		
				
				NumKeyBoardAttacher.instance().attach(self.find('[id=loadMember_input_takeMoney]')[0]);
				NumKeyBoardAttacher.instance().attach(self.find('[id=payTakeMoney_input_takeMoney]')[0], function(){
					self.find('[id=payTakeMoney_input_takeMoney]').keyup();
				});
				NumKeyBoardAttacher.instance().attach(self.find('[id=takeMoneyMoney_input_takeMoney]')[0]);
				
				self.find('[id=loadMember_input_takeMoney]').focus();
				
				if(afterOpen && typeof afterOpen == 'function'){
					afterOpen();
				}	
			});
		};
		
		this.close = function(afterClose, timeout){
			_takeMoneyPopup.close(function(self){
				NumKeyBoardAttacher.instance().detach(self.find('[id=loadMember_input_takeMoney]')[0]);
				NumKeyBoardAttacher.instance().detach(self.find('[id=payTakeMoney_input_takeMoney]')[0]);
				NumKeyBoardAttacher.instance().detach(self.find('[id=takeMoneyMoney_input_takeMoney]')[0]);
			});
		};
	
		function readMemberMsg(self, searchType){
			var memberInfo = self.find('[id=loadMember_input_takeMoney]');//获取输入框
			if(!memberInfo.val()){
				Util.msg.tip('请填写会员相关信息');
				memberInfo.focus();
				return;
			}
			
			//searchType : 手机会员value=1   微信会员value=2  实卡会员value=3
			if(searchType){
				self.find('[id=searchMemberType_div_takeMoney]').hide();
				self.find('[id=searchMemberTypeCmp_ul_takeMoney]').hide();
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
							_takeMoneyMember = response.root[0];
							writeMemberMsg(self, response.root[0]);
						}else if(response.root.length > 1){
							self.find('[id=searchMemberType_div_takeMoney]').show();
							self.find('[id=searchMemberTypeCmp_ul_takeMoney]').show();
							self.find('[id=searchMemberTypeCmp_ul_takeMoney]').find('li').each(function(index, element){
								$(element).click(function(){
									readMemberMsg(self, $(element).val());
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
			
			self.find('[id=payTakeMoney_input_takeMoney]').val('');
			self.find('[id=takeMoneyMoney_input_takeMoney]').val('');
			
			self.find('[id=baseBalance_label_takeMoney]').text(member.baseBalance);
			self.find('[id=extraBalance_label_takeMoney]').text(member.extraBalance);
			self.find('[id=totalBalance_label_takeMoney]').text(member.totalBalance);
			self.find('[id=memberName_label_takeMoney]').text(member.name);
			self.find('[id=memberType_label_takeMoney]').text(memberType.name);
			self.find('[id=memberSex_label_takeMoney]').text(member.sexText);	
			
			self.find('[id=memberMobileNum_label_takeMoney]').text(member.mobile ? member.mobile : '----');
			self.find('[id=memberCard_label_takeMoney]').text(member.memberCard ? member.memberCard : '----');	
			self.find('[id=weixinMemberCard_label_takeMoney]').text(member.weixinCard ? member.weixinCard : '----');
			
		}
		
		//金额联动
		function getTakeMoney(basePay, balance){
			if(basePay.val()){
				balance.val(Number(parseInt(basePay.val()).toFixed(2)));
			}else{
				balance.val(0);
			}
		}
		
		//取款
		function memberTakeMoney(self){
			if(_takeMoneyMember == null || typeof _takeMoneyMember == 'undefined'){
				Util.msg.tip('未读取会员信息, 请先刷卡.');
				return;
			}
			
			if(_takeMoneyMember.memberType.attributeValue != 0){
				Util.msg.tip('积分属性会员不允许充值, 请重新刷卡.');
				return;
			}
			
			var TakeMoney = self.find('[id=payTakeMoney_input_takeMoney]');
			var takeMoneyMoney = self.find('[id=takeMoneyMoney_input_takeMoney]');
		
			if(!TakeMoney.val()){
				Util.msg.tip('请输入实退金额');
				return;
			}
		
			if(!takeMoneyMoney.val()){
				Util.msg.tip('请输入账户扣额');
				return;		
			}
			
			Util.LM.show();
			
			$.post('../OperateMember.do', {
				dataSource : 'takeMoney',
				memberID : _takeMoneyMember.id,
				takeMoney : takeMoneyMoney.val(),
				payMannerMoney : TakeMoney.val(),
				isPrint : self.find('[id=printTakeMoney_checkbox_takeMoney]').attr('checked') ? true : false,
				orientedPrinter : getcookie(document.domain + '_printers')
			}, function(response){
				Util.LM.hide();
				if(response.success){
					if(self.find('[id=printTakeMoney_checkbox_takeMoney]').attr('checked')){
						setcookie(document.domain+'_takeMoney', true);
					}else{
						delcookie(document.domain+'_takeMoney');
					}
					_self.close();
					Util.msg.tip(response.msg);
				}else{
					Util.msg.tip(response.msg);
				}	
				_isProcessing = false;
			});
		}

	}
	
	exports.newInstance = function(){
		return new MemberTakeMoneyPopup();
	}
});
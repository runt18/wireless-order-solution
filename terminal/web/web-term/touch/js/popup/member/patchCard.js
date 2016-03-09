function PatchCardPopup(){
	
	var _self = this;
	var _patchPopup = null;
	//保存会员
	var _patchCardMember = null;

	_patchPopup = new JqmPopup({
		loadUrl : './popup/member/patchCard.html',
		pageInit : function(self){
			
			self.find('[id=container_div_patchCard]').trigger('create').trigger('refresh');
			
			//会员读取按钮
			self.find('[id=loadMember_button_patchCard]').click(function(){
				readMemberMsg(self);
			});
			
			//绑定enter键
			self.find('[id=loadMember_input_patchCard]').focus(function(){
				self.find('[id=loadMember_input_patchCard]').keypress(function(event){
					if(event.keyCode == 13){ //keyCode == 13 为enter键
						readMemberMsg(self);
					}
				});
			});
			
			//确定按钮
			self.find('[id=checkpatchCard_a_patchCard]').click(function(){
				bindCard(self);
			});
			
			//取消按钮
			self.find('[id=canelpatchCard_a_patchCard]').click(function(){
				_self.close();
			});
			
		}
	});
	
	this.open = function(afterOpen){
		_patchPopup.open(function(self){		
			
			NumKeyBoardAttacher.instance().attach(self.find('[id=loadMember_input_patchCard]')[0]);
			
			self.find('[id=loadMember_input_patchCard]').focus();
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}	
		});
	};
	
	this.close = function(afterClose, timeout){
		_patchPopup.close(function(self){
			NumKeyBoardAttacher.instance().detach(self.find('[id=loadMember_input_patchCard]')[0]);
			if(typeof afterClose === 'function'){
				if(timeout){
					afterClose();
				}else{
					setTimeout(afterClose, timeout);
				}
			}
		});
	};
	
	//读取会员信息
	function readMemberMsg(self, searchType){
		var memberInfo = self.find('[id=loadMember_input_patchCard]');//获取输入框
		if(!memberInfo.val()){
			Util.msg.tip('请填写会员相关信息');
			memberInfo.focus();
			return;
		}
		
		//searchType : 手机会员value=1   微信会员value=2  实卡会员value=3
		if(searchType){
			self.find('[id=searchMemberType_div_patchCard]').hide();
		}else{
			searchType = '';
		}
		
		Util.LM.show();
		
		$.ajax({
			url : "../QueryMember.do",
			type : 'post',
			data : {
				dataSource : 'normal',
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
						_patchCardMember = response.root[0];
						writeMemberMsg(self, response.root[0]);
					}else if(response.root.length > 1){
						self.find('[id=searchMemberType_div_patchCard]').show();
						self.find('[id=searchMemberTypeCmp_ul_patchCard]').find('li').each(function(index, element){
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
		
		self.find('[id=baseBalance_label_patchCard]').text(member.baseBalance);
		self.find('[id=extraBalance_label_patchCard]').text(member.extraBalance);
		self.find('[id=totalBalance_label_patchCard]').text(member.totalBalance);
		self.find('[id=memberName_label_patchCard]').text(member.name);
		self.find('[id=memberType_label_patchCard]').text(memberType.name);
		self.find('[id=memberSex_label_patchCard]').text(member.sexText);	
		
		self.find('[id=memberMobileNum_label_patchCard]').text(member.mobile ? member.mobile : '----');
		self.find('[id=weixinmemberCard_label_patchCard]').text(member.weixinCard ? member.weixinCard : '----');
		self.find('[id=memberCard_label_patchCard]').text(member.memberCard);
		
		if(member.memberCard != ''){
			self.find('[id=memberCard_label_patchCard]').css('display', 'block');
			self.find('[id=setMemberCard_div_patchCard]').css('display', 'none');
			self.find('[id=tips_td_patchCard]').text('(已有实体卡)');
		}else{
			self.find('[id=memberCard_label_patchCard]').css('display', 'none');
			self.find('[id=setMemberCard_div_patchCard]').css('display', 'block');
			self.find('[id=tips_td_patchCard]').text('');
			self.find('[id=setMemberCard_input_patchCard]').focus();
		}
		
	}
	
	//绑定实体卡功能
	function bindCard(self){
		if(_patchCardMember == null){
			Util.msg.tip('请先读取会员信息');
			return;
		}
		//获取实体卡输入框值
		var memberCardNum = self.find('[id=setMemberCard_input_patchCard]').val();
		if(_patchCardMember.memberCard == ''){
			if(memberCardNum != ''){
				$.ajax({
					url : '../OperateMember.do',
					type : 'post',
					dataType : 'json',				
					data : {
						dataSource : 'update',
						id : _patchCardMember.id,
						name : _patchCardMember.name,
						mobile : _patchCardMember.mobile,
						memberTypeId : _patchCardMember.memberType.id,
						memberCard : memberCardNum,
						sex : _patchCardMember.sexValue,
						birthday : _patchCardMember.birthday,
						telt : _patchCardMember.tele,
						addr : _patchCardMember.contactAddress,
						age : _patchCardMember.ageVal
					},
					success : function(response){
						if(response.success){
							Util.msg.tip('绑定成功');
							_self.close();
						}else{
							Util.msg.tip(response.msg);
						}
					},
					error : function(){
						Util.msg.tip('绑定错误');
					}
				});
			}else{
				Util.msg.tip('请输入实体卡号');
			}
			
		}else{
			console.log(0);
			_self.close();
		}
	}
	
}



function PatchWxCardPopup(){
	var _self = this;
	var _patchPopup = null;
	//保存会员
	var _patchMember = null;
	//保存微信会员读取出来的账号
	var _wxCardMemberMsg = null;
	//键位设置
	var keys = {
		enter : 13
	}

	_patchPopup = new JqmPopup({
		loadUrl : './popup/member/patchCard.html',
		pageInit : function(self){
			
			
//			self.find('[id=container_div_patchCard]').trigger('create').trigger('refresh');
			//设置标题
			self.find('[data-role=header]').text('补发电子卡');
			
			//会员读取按钮
			self.find('[id=loadMember_button_patchCard]').click(function(){
				readMemberMsg(self, false);
			});
			
			//绑定enter键
			self.find('[id=loadMember_input_patchCard]').keypress(function(event){
				if(event.keyCode == keys.enter){
					readMemberMsg(self, false);
				}
			});
			
			
			//读取微信会员操作
			self.find('[id=setMemeberWxCard_a_patchCard]').click(function(){
//				self.find('[id=wxCardMessages_table_patchCard]').css('display', 'block');
				readMemberMsg(self, true);
				var value = self.find('[id=setMemberWxCard_input_patchCard]').val();
				self.find('[id=wxCardIdContainer_span_patchCard]').text(value);
			});
			
			//绑定enter键
			self.find('[id=setMemberWxCard_input_patchCard]').keypress(function(e){
				if(e.keyCode == keys.enter){
					self.find('[id=setMemeberWxCard_a_patchCard]').click();
				}
			});
			//确定按钮
			self.find('[id=checkpatchCard_a_patchCard]').click(function(){
				//判定是否读取了微信会员
				if(_wxCardMemberMsg){
						var checkPopup = null;
						checkPopup = new JqmPopupDiv({
						loadUrl: './popup/dialog/dialog.html',
						pageInit: function(self){
							//设置提示内容
							self.find('[data-type=dialogContent]').text('确定绑定后，电子卡信息将会丢失');
							//关闭提示框
							self.find('[id=cancel_a_dialog]').click(function(){
								checkPopup.close();
							});
							//确认绑定
							self.find('[id=check_a_dialog]').click(function(){
								checkPopup.close();
								Util.LM.show();
								bindWxCard();
							});
						}
					});
					checkPopup.open();
				}else{
					Util.msg.tip('请先读取微信会员卡号');
				}
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
	
	//读取会员信息 isLoadWxCard 用于判断是否读取微信会员
	function readMemberMsg(self, isLoadWxCard, searchType){
		if(isLoadWxCard){
			var memberInfo = self.find('[id=setMemberWxCard_input_patchCard]');//获取输入框
			if(!memberInfo.val()){
				Util.msg.tip('请输入电子卡号');
				memberInfo.focus();
				return;
			}
		}else{
			var memberInfo = self.find('[id=loadMember_input_patchCard]');//获取输入框
			if(!memberInfo.val()){
				Util.msg.tip('请填写会员相关信息');
				memberInfo.focus();
				return;
			}
		}
		
		//searchType : 手机会员value=1   微信会员value=2  实卡会员value=3
		if(searchType){
			self.find('[id=searchMemberType_div_patchCard]').hide();
		}else{
			if(isLoadWxCard){
				searchType = 2;
			}else{
				searchType = '';
			}
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
						//判定是读取会员信息还是微信会员卡读取
						if(isLoadWxCard){
							_wxCardMemberMsg = response.root[0];
							writeWxMemberMsg(self, response.root[0]);
							self.find('[id=wxCardMessages_table_patchCard]').slideDown();
						}else{
							_patchMember = response.root[0];
							writeMemberMsg(self, response.root[0]);
						}
						
					}else if(response.root.length > 1){
						
						if(isLoadWxCard){
							Util.msg.tip('输入的微信账号有误');
							memberInfo.select();
							memberInfo.focus();
						}else{
							//显示选择搜索栏
							self.find('[id=searchMemberType_div_patchCard]').show();
							self.find('[id=searchMemberTypeCmp_ul_patchCard]').find('li').each(function(index, element){
								$(element).click(function(){
									readMemberMsg(self, false, $(element).val());
								});
							});
						}
						
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
		
		self.find('[id=baseBalance_label_patchCard]').text(member.baseBalance);
		self.find('[id=extraBalance_label_patchCard]').text(member.extraBalance);
		self.find('[id=totalBalance_label_patchCard]').text(member.totalBalance);
		self.find('[id=memberName_label_patchCard]').text(member.name);
		self.find('[id=memberType_label_patchCard]').text(member.memberType.name);
		self.find('[id=memberSex_label_patchCard]').text(member.sexText);	
		
		self.find('[id=memberMobileNum_label_patchCard]').text(member.mobile ? member.mobile : '----');
		self.find('[id=memberCard_label_patchCard]').text(member.memberCard ? member.memberCard : '----' );
		self.find('[id=weixinMemberCard_label_patchCard]').text(member.weixinCard);		
		
		if(member.weixinCard != ''){
			self.find('[id=weixinMemberCard_label_patchCard]').css('display', 'block');
			self.find('[id=setMemeberWxCard_div_patchCard]').css('display', 'none');
		}else{
			self.find('[id=weixinMemberCard_label_patchCard]').css('display', 'none');
			self.find('[id=setMemeberWxCard_div_patchCard]').css('display', 'block');
		}
	}
	
	//显示电子卡信息
	function writeWxMemberMsg(self, member){
		self.find('[id=wxBaseBalance_label_patchCard]').text(member.baseBalance);
		self.find('[id=wxExtraBalance_label_patchCard]').text(member.extraBalance);
		self.find('[id=wxTotalBalance_label_patchCard]').text(member.totalBalance);
		self.find('[id=wxMemberName_label_patchCard]').text(member.name);
		self.find('[id=wxMemberType_label_patchCard]').text(member.memberType.name);
		self.find('[id=wxMemberSex_label_patchCard]').text(member.sexText);
	}
	
	
	
	

	//绑定微信会员
	function bindWxCard(){
		$.ajax({
			url: '../OperateMember.do',
			type: 'post',
			datatype: 'json',
			data: {
				dataSource: 'patchWxCard',
				memberId : _patchMember.id,
				wxMemberId: _wxCardMemberMsg.id
			},
			success: function(data, status, xhr){
				Util.LM.hide();
				if(data.success){
					_patchPopup.close();
					Util.msg.tip('绑定成功');
					
				}else{
					Util.msg.tip(data.msg);
				}
			},
			error: function(){
				Util.LM.hide();
				Util.msg.tip('绑定错误');
			}
		});
	}
}
define(function(require, exports, module){
	
	function AjustPointPopup(){
		var _self = this;
		
		var _ajustPointPopup = null;
		var _ajustMember = null;
		//防止连点下单
		var _isProcessing = false;
		
		_ajustPointPopup = new JqmPopup({
			loadUrl : './popup/member/ajustPointPopup.html',
			pageInit : function(self){
			
				self.find('[id=container_div_ajustPoint]').trigger('create').trigger('refresh');
				
				//会员读取按钮
				self.find('[id=loadMember_button_ajustPoint]').click(function(){
					readMemberMsg(self);
				});
				
				//绑定enter键
				self.find('[id=loadMember_input_ajustPoint]').focus(function(){
					self.find('[id=loadMember_input_ajustPoint]').keypress(function(event){
						if(event.keyCode == 13){ //keyCode == 13 为enter键
							readMemberMsg(self);
						}
					});
				});
				
				//确定按钮
				self.find('[id="confrim_a_ajustPoint"]').click(function(){
					if(!_isProcessing){
						_isProcessing = true;
						ajustPoint(self);
					}
				});
				
				//取消按钮
				self.find('[id="cancel_a_ajustPoint"]').click(function(){
					_self.close();
				});
				
				
			}
			
			
		});
		
		this.open = function(afterOpen){
			_ajustPointPopup.open(function(self){
				NumKeyBoardAttacher.instance().attach(self.find('[id=loadMember_input_ajustPoint]')[0]);
				NumKeyBoardAttacher.instance().attach(self.find('[id=point_input_ajustPoint]')[0]);
				
				self.find('[id=loadMember_input_ajustPoint]').focus();
				
				if(afterOpen && typeof afterOpen == 'function'){
					afterOpen();
				}	
			});		
		};
		
		
		this.close = function(afterClose, timeout){
			_ajustPointPopup.close(function(self){
				NumKeyBoardAttacher.instance().detach(self.find('[id=loadMember_input_ajustPoint]')[0]);
				NumKeyBoardAttacher.instance().detach(self.find('[id=point_input_ajustPoint]')[0]);
			});
			
		};
		
		//读取会员信息
		function readMemberMsg(self, searchType){
			var memberInfo = self.find('[id=loadMember_input_ajustPoint]');//获取输入框
			if(!memberInfo.val()){
				Util.msg.tip('请填写会员相关信息');
				memberInfo.focus();
				return;
			}
			
			//searchType : 手机会员value=1   微信会员value=2  实卡会员value=3
			if(searchType){
				self.find('[id=searchMemberType_div_ajustPoint]').hide();
				self.find('[id=searchMemberTypeCmp_ul_ajustPoint]').hide();
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
							_ajustMember = response.root[0];
							
							wirteMember(self, response.root[0]);
							//TODO赋值
						}else if(response.root.length > 1){
							self.find('[id=searchMemberType_div_ajustPoint]').show();
							self.find('[id=searchMemberTypeCmp_ul_ajustPoint]').show();
							self.find('[id=searchMemberTypeCmp_ul_ajustPoint]').find('li').each(function(index, element){
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
		
		function wirteMember(self, member){
			self.find('[id="memberPoint_label_ajustPoint"]').text(member.point);
			
			self.find('[id="memberName_label_recharge"]').text(member.name);
		}
		
		function ajustPoint(self){
			var point = self.find('[id="point_input_ajustPoint"]');
			
			var ajust = null;
			self.find('[id="pointType_fieldset_ajust"] input').each(function(index, element){
				if($(element).attr('checked') == 'checked'){
					ajust = $(element).attr('value');
				}
			})
			
			if(_ajustMember == null || typeof _ajustMember == 'undefined'){
				Util.msg.tip('未读取会员信息, 请先刷卡.');
				return;
			}
			
			if(!point.val()){
				Util.msg.tip('积分不能为空');
				return;
			}
			
			Util.LM.show();
			
			$.post('../OperateMember.do', {
				dataSource : 'adjustPoint',
				memberId :_ajustMember.id,
				point : point.val(),
				adjust : ajust
			}, function(response){
				Util.LM.hide();
				if(response.success){
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
		return new AjustPointPopup();
	}
	
});
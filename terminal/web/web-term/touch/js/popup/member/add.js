function AddMemberPopup(){
	
	var _addMemberPopup = null;
	_addMemberPopup = new JqmPopup({
		loadUrl : './popup/member/add.html',
		pageInit : function(self){
			self.find('[id=addMember_div_memberAdd]').trigger('create').trigger('refresh');
			
			//会员类型改变的时候做出的操作
			self.find('[id=memberType_select_memberAdd]').on('change', function(e){
				var selected = $(e.target).find('option:selected');
				if(parseInt(selected.attr('data-attrVal')) == 0){
					self.find('[id=memberMoney_tr_memberAdd]').show();
					self.find('[id=memberPrint_tr_memberAdd]').show();
					ts.member.chargeRate = parseFloat(selected.attr('data-chargeRate'));
					setTimeout(function(){
						$('#cm_numFirstCharge').focus();
					}, 250);
				}else{
					self.find('[id=memberMoney_tr_memberAdd]').hide();
					self.find('[id=memberPrint_tr_memberAdd]').hide();
				}
			});
			
			//确定按钮
			self.find('[id=confirm_a_memberAdd]').click(function(){
				var memberType = $('#memberType_select_memberAdd');
				var memberName = $('#memberName_input_memberAdd');
				var memberMobile = $('#memberMobile_input_Add');
				var memberCard = $('#memberCard_input_memberAdd');
				var memberSex = $('#memberSex_select_memberAdd');
				var birthday = $('#memberBirthday_input_memberAdd');
				var firstCharge = $('#memberFirst_input_memberAdd');
				var firstActualCharge = $('#memberActual_input_memberAdd');
				var rechargeType = $('#memberRechargeType_select_memberAdd');
				var referrer = $('#memberReferrer_select_memberAdd');
				
				if(!memberMobile.val() && !memberCard.val()){
					Util.msg.tip('至少要输入手机或会员卡号');
					return;
				}	
				
				if(!memberName.val()){
					Util.msg.tip('请输入会员名称');
					return;
				}	
				
				if(!memberType.val()){
					Util.msg.tip('请选择会员类型');
					return;
				}	
				
				Util.LM.show();
				
				if($('#memberSendMsgPrint_input_memberAdd').attr('checked')){
					setcookie(document.domain+'_chargeSms', true);
				}else{
					delcookie(document.domain+'_chargeSms');
				}
				
				$.post('../OperateMember.do', {
					dataSource : 'insert',
					name : memberName.val(),
					mobile : memberMobile.val(),
					memberTypeId : memberType.val(),
					sex : memberSex.val(),
					memberCard :memberCard.val(),
					birthday : birthday.val() ? birthday.val().format('Y-m-d') : '',
					firstCharge : firstCharge.val(),
					firstActualCharge : firstActualCharge.val(),
					rechargeType : rechargeType.val(),
					referrer : referrer.val(),
					isPrint : $('#memberPrintRecharge_input_memberAdd').attr('checked')?true:false,
					sendSms : $('#memberSendMsgPrint_input_memberAdd').attr('checked')?true:false
				}, function(jr){
					Util.LM.hide();
					if(jr.success){
						self.find('[id=cancel_a_memberAdd]').click();
						//更新短信
						Util.sys.checkSmStat();
						
						Util.msg.alert({
							topTip : true,
							msg : jr.msg
						});
					}else{
//						Util.msg.alert({
//							renderTo : 'tableSelectMgr',
//							msg : jr.msg
//						});	
						Util.msg.alert({
							topTip : true,
							msg : jr.msg
						});
					}
					
				});
				
			});
			
			
			//取消按钮
			self.find('[id=cancel_a_memberAdd]').click(function(){
				_addMemberPopup.close();
			});
		}
	});
	
	this.open = function(afterOpen){
		_addMemberPopup.open(function(self){
			//关闭列表
			$('#frontPageMemberOperation').popup('close');
			
			//充值金额
			$('#memberFirst_input_memberAdd').on('keyup', function(){
				var chargeMoney = $('#memberFirst_input_memberAdd').val();
				var actualChargeMoney = $('#memberActual_input_memberAdd');
				actualChargeMoney.val(Math.round(chargeMoney * ts.member.chargeRate));
			});	
			
			
			if(getcookie(document.domain+'_chargeSms') == 'true'){
				$('#memberSendMsgPrint_input_memberAdd').attr('checked', true).checkboxradio("refresh");
			}else{
				$('#memberSendMsgPrint_input_memberAdd').attr('checked', false).checkboxradio("refresh");
			}
			
			//判断是否有短信模块
			if(Util.sys.smsModule){
				$('#memberSendCmp_div_memberAdd').show();
				$('#memberSendWord_font_memberAdd').html('发送充值信息'+(Util.sys.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Util.sys.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Util.sys.smsCount+'条, 请及时充值</font>)'));
				$('#memberSendWord_font_memberAdd').trigger('refresh');
			}
			
			//打开的时候加载数据
			Util.LM.show();
			$.ajax({
				url : '../QueryMemberType.do',
				type : 'post',
				async : false,
				data : {dataSource : 'normal'},
				success : function(jr, status, xhr){
					if(jr.success){
						Util.LM.hide();
						var html = [];
						var weixin;
						for (var i = 0; i < jr.root.length; i++) {
							if(jr.root[i].name == "微信会员"){
								weixin = jr.root[i];
								continue;
							}
							
							html.push('<option value={id} data-attrVal={attrVal} data-chargeRate={chargeRate}>{name}</option>'.format({
								id : jr.root[i].id,
								attrVal : jr.root[i].attributeValue,
								chargeRate : jr.root[i].chargeRate,
								name : jr.root[i].name
							}));
							
						}
						//加上微信会员选项
						html.unshift('<option value={id} data-attrVal={attrVal} data-chargeRate={chargeRate}>{name}</option>'.format({
							id : weixin.id,
							attrVal : weixin.attributeValue,
							chargeRate : weixin.chargeRate,
							name : weixin.name
						}));
						
						
						$('#memberType_select_memberAdd').html(html.join(""));
						$('#memberType_select_memberAdd').val(weixin.id);
						$('#memberType_select_memberAdd').selectmenu('refresh'); 
						
					}else{
						Util.msg.tip(jr.msg);
					}
				},
				error : function(request, status, err){
					Util.msg.tip(jr.msg);
				}
			});
			
			
			$.ajax({
				url : '../QueryStaff.do',
				type : 'post',
				dataType : 'json',
				success : function(data, status, xhr){
					if(data.success){
						var html = ['<option></option>'];
						for(var i = 0; i < data.root.length; i++){
							html.push('<option value={id}>{name}</option>'.format({
								id : data.root[i].staffID,
								name : data.root[i].staffName
							}));
						}
						$('#memberReferrer_select_memberAdd').html(html.join("")).selectmenu('refresh');
			            
					}else{
						Util.msg.tip('获取餐厅员工信息失败, 请联系客服员.');
					}
				},
				error : function(request, status, err){
					Util.LM.hide();
					Util.msg.tip('获取餐厅员工信息失败, 请联系客服员.');
				}
			});
			
		});
	}
	
	this.close = function(afterOpen, timeout){
		_addMemberPopup.close(afterOpen, timeout);
	};


}


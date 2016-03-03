

function MemberRechargePopup(){
	
		//读取会员信息
	function readMemberMsg(memberType){
		var memberInfo = $('#loadMember_input_recharge');//获取输入框
		if(!memberInfo.val()){
			Util.msg.alert({msg:'请填写会员相关信息', topTip:true});
			memberInfo.focus();
			return;
		}
		if(memberType){
//			$('#charge_searchMemberType').popup('close');
			$('#searchMemberType_div_recharge').hide();
			$('#searchMemberTypeCmp_ul_recharge').hide();
		}else{
			memberType = '';
		}
		Util.LM.show();
		$.ajax({
			url : "../QueryMember.do",
			type : 'post',
			data : {
				dataSource :'normal',
				sType : memberType,
				forDetail : true,
				memberCardOrMobileOrName : memberInfo.val()
			},
			async : true,
			dataType : 'json',
			success : function(response, status, xhr){
				Util.LM.hide();
				if(response.success){
					if(response.root.length == 1){
						Util.msg.alert({msg:'会员信息读取成功.', topTip:true});
//						ts.member.rechargeMember = response.root[0];
//						ts.member.loadMemberInfo4Charge(response.root[0]);
						_rechargeMember = response.root[0];
						writeMemberMsg(response.root[0]);
					}else if(response.root.length > 1){
						Util.msg.alert({msg:'检查到有多个账号.', topTip:true});
						$('#searchMemberType_div_recharge').show();
						$('#searchMemberTypeCmp_ul_recharge').show();
						$('#searchMemberTypeCmp_ul_recharge').find('li').each(function(index, element){
							$(element).click(function(){
								readMemberMsg($(element).val());
							});
						});
//						$('#charge_searchMemberType').css({top:$('#loadMember_button_recharge').position().top - 270, left:$('#loadMember_button_recharge').position().left-300});
					}else{
						Util.msg.alert({msg:'该会员信息不存在, 请重新输入条件后重试.', topTip:true});
						memberInfo.select();
						memberInfo.focus();
					}
				}else{
					Util.msg.alert({
						msg : response.msg
//						renderTo : 'tableSelectMgr'
					});
				}
			},
			error : function(request, status, err){
				Util.msg.alert({msg:'读取错误.', topTip:true});
			}
		});
	}
	
	var _rechargePopup = null;
	//保持充值会员对象
	var _rechargeMember = null;
	//充值比率
	var memberRate = null;
	_rechargePopup = new JqmPopup({
		loadUrl : './popup/member/recharge.html',
		pageInit : function(self){
			
			self.find('[id=container_div_recharge]').trigger('create').trigger('refresh');
			//会员读取按钮
			self.find('[id=loadMember_button_recharge]').click(function(){
//				ts.member.readMemberByCondtion4Charge();
				readMemberMsg();
			});
			//充值按钮
			self.find('[id=toRecharge_a_recharge]').click(function(){
//				ts.member.rechargeControlCenter();
				meberRechargeCheck();
			});
			//取消按钮
			self.find('[id=canelRecharge_a_recharge]').click(function(){
				closeMemberPopup();
			});
			//金额联动
			self.find('[id=PayMannerMoney_input_recharge]').focus(function(){
				$('#PayMannerMoney_input_recharge').keyup(function(){
					getCharge($('#PayMannerMoney_input_recharge'), $('#rechargeMoney_input_recharge'), memberRate);
				});
			});
		}
	});
	this.open = function(afterOpen){
		_rechargePopup.open(afterOpen);
	};
	this.close = function(afterClose, timeout){
		_rechargePopup.close(afterClose, timeout);
	};
	

	
	//显示会员信息
	function writeMemberMsg(member){
		member = member == null || typeof member == 'undefined' ? {} : member;
		var memberType = member.memberType ? member.memberType : {};
		
		$('#PayMannerMoney_input_recharge').val('');
		$('#rechargeMoney_input_recharge').val('');
		
		$('#baseBalance_label_recharge').text(typeof member.baseBalance != 'undefined'?member.baseBalance:'----');
		$('#extraBalance_label_recharge').text(typeof member.extraBalance != 'undefined'?member.extraBalance:'----');
		$('#totalBalance_label_recharge').text(typeof member.totalBalance != 'undefined'?member.totalBalance:'----');
		$('#memberName_label_recharge').text(member.name?member.name:'----');
		$('#memberType_label_recharge').text(memberType.name?memberType.name:'----');
		$('#memberSex_label_recharge').text(member.sexText?member.sexText:'----');	
		
		$('#memberMobileNum_label_recharge').text(member.mobile?member.mobile:'----');
		$('#memberCard_label_recharge').text(member.memberCard?member.memberCard:'----');	
		$('#weixinMemberCard_label_recharge').text(member.weixinCard?member.weixinCard:'----');
		
		if(!jQuery.isEmptyObject(member)){
			//充值比率
			ts.member.chargeRate = member.memberType.chargeRate;
			$('#PayMannerMoney_input_recharge').focus();		
		}
	}
	
	
	//充值功能
	function meberRechargeCheck(_c){
		_c = _c == null || typeof _c == 'undefined' ? {} : _c;
		if(_rechargeMember == null || typeof _rechargeMember == 'undefined'){
			Util.msg.tip('未读取会员信息, 请先刷卡.');
			return;
		}
		if(_rechargeMember.memberType.attributeValue != 0){
			Util.msg.tip('积分属性会员不允许充值, 请重新刷卡.');
			return;
		}
	
		var rechargeMoney = $('#rechargeMoney_input_recharge');
		var rechargeType = $('#rechargeType_select_recharge');
		var payMannerMoney = $('#PayMannerMoney_input_recharge');
		
		if(!rechargeMoney.val()){
			Util.msg.tip('请输入充值金额');
			return;
		}
	
		if(!payMannerMoney.val()){
			Util.msg.tip('请输入账户充额');
			return;		
		}
		//保存缓存
		if($('#sendReChargeMsg_check_recharge').attr('checked')){
			setcookie(document.domain+'_chargeSms', true);
		}else{
			delcookie(document.domain+'_chargeSms');
		}
		
		if($('#printRecharge_checkbox_recharge').attr('checked')){
			setcookie(document.domain+'_chargeSms', true);
		}else{
			delcookie(document.domain+'_chargeSms');
		}
		
		Util.LM.show();
		
		$.post('../OperateMember.do', {
			dataSource : 'charge',
			memberID : _rechargeMember.id,
			rechargeMoney : rechargeMoney.val(),
			rechargeType : rechargeType.val(),
			payMannerMoney : payMannerMoney.val(),
			isPrint : $('#printRecharge_checkbox_recharge').attr('checked') ? true : false,
			sendSms : $('#sendReChargeMsg_check_recharge').attr('checked') ? true : false,
			orientedPrinter : getcookie(document.domain + '_printers')
		}, function(response){
			Util.LM.hide();
			if(response.success){
				closeMemberPopup();
				//更新短信
				Util.sys.checkSmStat();
				Util.msg.alert({
					topTip : true,
					msg : response.msg
				});
			}else{
				Util.msg.alert({
//					renderTo : 'tableSelectMgr',
					msg : response.msg
				});
			}		
		});
	}
	
	//关闭popup框
	function closeMemberPopup(){
		_rechargePopup.close(); //关闭充值popup
		$('#shadowForPopup').hide(); //关闭遮罩层
		writeMemberMsg(); //
		$('#loadMember_input_recharge').val(''); //清空input框
		_rechargeMember = null;
	}
	
	//金额联动
	function getCharge(basePay, getBalance, rate){
		if(basePay.val()){
			if(rate){
				getBalance.val(parseInt(basePay.val()) * parseInt(rate));
			}else{
				getBalance.val(parseInt(basePay.val()));
			}
		}else{
			getBalance.val(0);
		}
	}

}



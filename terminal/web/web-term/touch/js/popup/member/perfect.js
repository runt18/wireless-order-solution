function PerfectMemberPopup(param){
	
	param = param || {
		selectedOrder : null,   //order对象
		selectedMember : null,	//member对象
		memberName : null,   
		postBound : null		//绑定后回调函数
	};
	
	var _selectedMember = param.selectedMember;  //member对象
	var _selectedOrder = null;
	if(param.selectedOrder){
		_selectedOrder = param.selectedOrder;   //order对象
	}
	
	var _perfectMemberPopup = null;
	_perfectMemberPopup = new JqmPopup({
		loadUrl : './popup/member/perfect.html',
		pageInit : function(self){
			loadMemberBind();
			
			//绑定确定按钮
			self.find('[id=confirm_a_perfect]').click(function(){
				checkMemberExist(self);
			});
			
			//绑定取消按钮
			self.find('[id=cancel_a_perfect]').click(function(){
				_perfectMemberPopup.close();
			});
		}
	});
	
	this.open = function(afterOpen){
		_perfectMemberPopup.open();
	};
	
	this.close = function(afterOpen, timeout){
		_perfectMemberPopup.close();
	}; 
	
	
	//加载初始化
	function loadMemberBind(){
		//在页面打开的时候加载微信会员类型
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
					for(var i = 0; i < jr.root.length; i++){
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
					
					$('#perfectMemberType_select_perfect').html(html.join(""));
					$('#perfectMemberType_select_perfect').val(weixin.name);
					$('#perfectMemberName_input_perfect').val(param.memberName);
					$('#perfectMemberType_select_perfect').selectmenu('refresh');
					
					
				}else{
					Util.msg.tip(jr.msg);
				}
			},
			error : function(request, status, err){
				Util.msg.tip(request.msg);
			}
		});
	}
	
	
	//绑定的时候查找会员手机和会员卡号是否存在
	function checkMemberExist(self){
		var mobile = $('#perfectMemberMobile_input_perfect').val();;
		var card = $('#perfectMemberCard_input_perfect').val();
		
		if(!mobile && !card){
			Util.msg.tip('请填写手机号或实体卡号');
			return;
		}
		
		Util.LM.show();
		$.post('../OperateMember.do', {
			dataSource : 'checkMember',
			name : $('#perfectMemberName_input_perfect').val(),
			mobile : mobile,
			card : card,
			sex : $('input[name="perfectMemberSex_input_perfect"]:checked').val(),
			birthday : $('#perfectMemberBirthday_input_perfect').val(),
			type : $('#perfectMemberType_select_perfect').val()			
		}, function(result){
			Util.LM.hide();
			if(result.success){
				if(result.root.length > 0){
					$('#confirmMember_div_perfect').slideDown("slow");
					$('#confirm_a_perfect span span').text('确认并绑定');
					$('#confirm_a_perfect').trigger('create').trigger('refresh');
					$('#confirm_a_perfect').on('click',confirmBindMember);
					showMemberDetail(result.root[0]);
				}else{
					confirmBindMember();
				}
			}else{
				Util.msg.tip(result.msg);
			}
		}, 'json');
	}
	
	//加载对比会员数据
	function showMemberDetail(m){
		$('#confirmMemberName_td_perfect').html(m.name);
		$('#confirmMembeMobile_td_perfect').html(m.mobile ? m.mobile : "----");
		$('#confirmMembeCard_td_perfect').html(m.memberCard ? m.memberCard : "----");
		$('#confirmMembeSex_td_perfect').html(m.sexText);
		$('#confirmMembeBirthday_td_perfect').html(m.birthdayForamt ? m.birthdayForamt : "----");
		$('#confirmMembeType_td_perfect').html(m.memberType.name);
		
		//设置数据到上方
		$('#perfectMemberName_input_perfect').val(m.name);
		$('#perfectMemberMobile_input_perfect').val(m.mobile);
		$('#perfectMemberCard_input_perfect').val(m.memberCard);
		$('#perfectMemberBirthday_input_perfect').val(m.birthdayFormat);
		
		$('input[name="perfectMemberSex_input_perfect"]').each(function(index, element){
			if(this.value == m.sexValue){
				$(this).attr("checked", true).checkboxradio("refresh");
			}else{
				$(this).attr("checked", false).checkboxradio("refresh");
			}
		});
		
		$('#perfectMemberType_select_perfect').val(m.memberType.id);		
		$('#perfectMemberType_select_perfect').select('refresh');
		
		$('#perfectMemberMobile_input_perfect').attr("disabled", "disabled").parent().addClass('ui-disabled');
		$('#perfectMemberCard_input_perfect').attr("disabled", "disabled").parent().addClass('ui-disabled');
	}
	
	//确认绑定
	function confirmBindMember(){

		var mobile = $('#perfectMemberMobile_input_perfect').val();
		var card = $('#perfectMemberCard_input_perfect').val();
		
		if(!mobile && !card){
			Util.msg.tip('请填写手机号或实体卡号');
			return;
		}
		
		Util.LM.show();
		$.post('../OperateMember.do', {
			dataSource : 'bindWxMember',
			id : _selectedMember,
			orderId : _selectedOrder,
			name : $('#perfectMemberName_input_perfect').val(),
			mobile : mobile,
			card : card,
			sex : $('input[name="perfectMemberSex_input_perfect"]:checked').val(),
			birthday : $('#perfectMemberBirthday_input_perfect').val(),
			type : $('#perfectMemberType_select_perfect').val()			
		}, function(result){
			Util.LM.hide();
			if(result.success){
				if(param.postBound && typeof param.postBound == 'function'){
					param.postBound();
				}
				Util.msg.tip(result.msg);
				_perfectMemberPopup.close();
			}else{
				Util.msg.tip(result.msg);
			}
		}, 'json');
	}
	
	
}
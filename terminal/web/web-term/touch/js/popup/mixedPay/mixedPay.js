function createMixPayPopup(param){
	
	param = param || {
		left : function(){},     //左边按钮的方法
		middle : function(){},	 //中间按钮的方法
		right : function(){},	 //右边按钮的方法
		orderMessage : null      //账单信息
	}
	
	var _payTypeData = null;   //支付类型数据
	
	var _mixPayPopup = null;
	_mixPayPopup = new JqmPopup({
		loadUrl : './popup/mixedPay/mixedPay.html',
		pageInit : function(self){
			//左边按钮的方法
			self.find('[id="left_a_mixedPay"]').click(function(){
				if(param.left && typeof param.left == 'function'){
					var mixedIncome = '';
					for (var i = 0; i < _payTypeData.length; i++) {
						var checked = $('#chbForPayType' + _payTypeData[i].id).attr('checked');
						if(checked && $('#numForPayType' + _payTypeData[i].id).val()){
							if(mixedIncome.length != 0){
								mixedIncome += '&';
							}
							mixedIncome += (_payTypeData[i].id + ',' + $('#numForPayType' + _payTypeData[i].id).val());  
						}
					}
					param.left(mixedIncome);
				}
			});
		
			
			//中间按钮的方法
			self.find('[id="middle_a_mixedPay"]').click(function(){
				if(param.middle && typeof param.middle == 'function'){
					var mixedIncome = '';
					for (var i = 0; i < _payTypeData.length; i++) {
						var checked = $('#chbForPayType' + _payTypeData[i].id).attr('checked');
						if(checked && $('#numForPayType' + _payTypeData[i].id).val()){
							if(mixedIncome.length != 0){
								mixedIncome += '&';
							}
							mixedIncome += (_payTypeData[i].id + ',' + $('#numForPayType' + _payTypeData[i].id).val());  
						}
					}
					param.middle(mixedIncome);
				}
			});
			
			//右边按钮的方法
			self.find('[id="right_a_mixedPay"]').click(function(){
				if(param.right && typeof param.right == 'function'){
					param.right();
				}else{
					_mixPayPopup.close();
				}
			});
		
		}
	});
	
	this.open = function(afterOpen){
		_mixPayPopup.open(function(self){
			$.ajax({
				url : "../QueryPayType.do",
				type : 'post',
				data : {
					dataSource : 'exceptMember'
				},
				success : function(jr, status, xhr){
					if(jr.success){
						var eachMaxType = '<tr>' +
							'<td><label><input data-theme="e" id={checkboxId} data-for={numberfieldId} type="checkbox" name="mixPayCheckbox">{name}</label></td>'+
							'<td style="padding-right: 10px;"><input data-theme="c" id={numberfieldId} class="mixPayInputFont numberInputStyle" disabled="disabled" ></td>'+
							'</tr>';
						
						var html = [];
						var checkBoxes = [];
						_payTypeData = jr.root;
						for(var i = 0; i < _payTypeData.length; i++){
							var checkBoxId = "chbForPayType" + _payTypeData[i].id;
							var numberfieldId = "numForPayType" + _payTypeData[i].id;
							checkBoxes.push(checkBoxId);
							html.push(eachMaxType.format({
								name : _payTypeData[i].name,
								checkboxId : checkBoxId,
								numberfieldId : numberfieldId
							}));
						}
						
						self.find('[id="mixedPay_tbl_mixPay"]').html(html.join('')).trigger('create');
						
						//混合结账中每个CheckBox按钮的事件
						for(var i = 0; i < checkBoxes.length; i++){
							$('#' + checkBoxes[i]).click(function(){
								
								var curCheckbox = $(this);
								var numForAlias = $("#" + curCheckbox.attr('data-for'));
								
								if(curCheckbox.attr('checked')){
									
									var mixedPayMoney = param.orderMessage.actualPrice;
									for (var i = 0; i < _payTypeData.length; i++) {
										var checked = $('#chbForPayType' + _payTypeData[i].id).attr('checked');
										var money = $('#numForPayType' + _payTypeData[i].id).val();
										if(checked && money){
											mixedPayMoney = (mixedPayMoney * 10000 - parseInt(money) * 10000) / 10000; 
										}
									}
									
									numForAlias.val(mixedPayMoney < 0 ? 0 : mixedPayMoney);							
									
									numForAlias.removeAttr("disabled"); 
									numForAlias.parent().removeClass('ui-disabled');
							
									numForAlias.focus();
									numForAlias.select();
									
								}else{
									
									numForAlias.attr("disabled", true); 
									numForAlias.parent().addClass('ui-disabled');
							
									numForAlias.val('');		
								}	
							});
						}
						
					}
				},
				error : function(request, status, err){
				}
			}); 	
		});
	}
	
	this.close = function(afterClose, timeout){
		_mixPayPopup.close();
	}
	
	
}
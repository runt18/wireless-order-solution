function CreateWxPaymentPopup(param){
	
	param = param || {
		pay : function(inputValue){},     //支付
		right : function(){},               //右边方法
		title : null,                        //标题
	    content : null,                       //内容
	    leftText : null,                    //左边按钮内容
	    rightText : null	 				//右边按钮内容
	}
	
	var _wxPay = null;
	
	_wxPay = new JqmPopup({
		loadUrl : './popup/wxPayment/wxPayment.html',
		pageInit : function(self){
			//确认按钮
			self.find('[id="confirm_a_wxPayment"]').click(function(){
				if(param.pay && typeof param.pay == 'function'){
					param.pay($('#wxPay_input_wxPayment').val());
				}
			});
			
			//读取会员
			self.find('[id="wxPay_input_wxPayment"]').on('keypress', function(event){
				if(event.keyCode == "13"){
					if(param.pay && typeof param.pay == 'function'){
						param.pay($('#wxPay_input_wxPayment').val());
					}
				}
			});
			
			//提示内容
			if(param.content){
				self.find('[id="wxPay_input_wxPayment"]').hide();
				self.find('[id="payTip_div_wxPayment"]').show();
				self.find('[id="payTip_div_wxPayment"]').html(param.content);
			}
			
			
			//标题
			if(param.title){
				self.find('[id="WxTitle_div_wxPayment"]').html(param.title);
			}
			
			//左边按钮text
			if(param.leftText){
				self.find('[id="confirm_a_wxPayment"] span span').text(param.leftText);
			}
			
			//右边按钮text
			if(param.rightText){
				self.find('[id="cancel_a_wxPayment"] span span').text(param.rightText);
			}
			
			//取消按钮
			self.find('[id="cancel_a_wxPayment"]').click(function(){
				if(param.right && typeof param.right == 'function'){
					param.right();
				}else{
					
				}
				_wxPay.close();
			});
		}
	});
	
	
	this.open = function(afterOpen){
		_wxPay.open(function(self){
			self.find('[id="wxPay_input_wxPayment"]').focus();
		});
		
		if(afterOpen && typeof afterOpen == 'function'){
			afterOpen();
		}
	}
	
	
	this.close = function(afterClose, timeout){
		_wxPay.close();
		
		if(afterClose && typeof afterClose == 'function'){
			if(timeout){
				setTimeout(afterClose, timeout);
			}else{
				afterClose();
			}
		}
	}
	
}
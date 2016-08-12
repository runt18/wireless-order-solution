define(function(require, exports, module){
	function ScanUseCouponPopup(param){
		var thiz = this;
		var _self = null;
		
		param = param || {
			confirm : function(couponId, self){}
		}
		
		var _scanUseCouponPopup = null;
		_scanUseCouponPopup = new JqmPopup({
			loadUrl : './popup/scanUseCoupon/scanUseCoupon.html',
			pageInit : function(self){
				_self = self;
				//确定按钮
				self.find('[id="left_a_scanUseCoupon"]').click(function(){
					if(param.confirm){
						param.confirm(self.find('[id="scan_input_scanUseCoupon"]').val(), self);
						
					}
				});
				
				self.find('[id=scan_input_scanUseCoupon]').on('keypress', function(event){
					if(event.keyCode == "13"){
						if(param.confirm){
							param.confirm(self.find('[id="scan_input_scanUseCoupon"]').val(), self);
							
						}
					}
				});
				
				//取消按钮
				self.find('[id="right_a_askTable"]').click(function(){
					thiz.close();
				});
				
			}
		});
			
		this.open = function(afterOpen){
			_scanUseCouponPopup.open(function(){
				_self.find('[id="scan_input_scanUseCoupon"]').focus();
			});
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		}

		this.close = function(afterClose, timeout){
			_scanUseCouponPopup.close();
			
			if(afterClose && typeof afterClose == 'function'){
				if(timeout){
					setTimeout(afterClose, timeout);
				}else{
					afterClose();
				}
			}
		}
	}
	
	exports.newInstance = function(param){
		return new ScanUseCouponPopup(param);
	};
})
	

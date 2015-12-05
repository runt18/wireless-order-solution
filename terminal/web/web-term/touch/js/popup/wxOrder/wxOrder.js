function WxOrderPopup(param){
	
	param = param || {
		left : function(){},     //左边按钮的方法
		right : function(){}	//右边按钮的方法
	}
	
	var _self = this;
	var _wxOrderPopup = null;
	_wxOrderPopup = new JqmPopup({
		loadUrl : './popup/wxOrder/wxOrder.html',
		pageInit : function(self){
			//右边按钮
			self.find('[id=right_a_wxOrder]').click(function(){
				_self.close();
			});
			
			//左边按钮
			self.find('[id=left_a_wxOrder]').click(function(){
				if(param.left){
					param.left();
				}
			});
			
		}
	});
	
	this.open = function(afterOpen){
		_wxOrderPopup.open(function(self){
			//数字键盘绑定填写台号框 
			NumKeyBoardAttacher.instance().attach(self.find('[id=code_input_wxOrder]')[0]);
			
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
			
			self.find('[id=code_input_wxOrder]').focus();
		});
	}
	
	
	this.close = function(afterClose, timeout){
		_wxOrderPopup.close(function(self){
			//数字键盘绑定填写台号框 
			NumKeyBoardAttacher.instance().detach(self.find('[id=code_input_wxOrder]')[0]);
	
			if(afterClose && typeof afterClose == 'function'){
				if(timeout){
					setTimeout(afterClose, timeout);
				}else{
					afterClose();
				}
			}	
		});
	}
	
}
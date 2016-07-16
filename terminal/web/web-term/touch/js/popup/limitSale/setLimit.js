define(function(require, exports, module){
	
	function SetLimitPopup(param){
		
		param = param || {
			food : null,
			title : null,
			postConfirm : function(){}
		}
		
		var _setLimitPopup = null;
		
		var _thiz = this;
		
		_setLimitPopup = new JqmPopup({
			loadUrl : './popup/limitSale/setLimit.html',
			pageInit : function(self){
				
				if(param.title){
					self.find('[id="title_div_setLimit"]').html(param.title);
				}
				
				if(param.food){
					self.find('[id="setLimit_num_setLimit"]').val(param.food.foodLimitAmount);					
				}
				
				//重置按钮
				self.find('[id="reset_a_setLimit"]').click(function(){
					if(param.food){
						self.find('[id="setLimit_num_setLimit"]').val(param.food.foodLimitRemain);					
					}
				});
				
				
				//取消按钮
				self.find('[id="right_a_setLimit"]').click(function(){
					_thiz.close();
				});
				
				
				//修改按钮
				self.find('[id="left_a_setLimit"]').click(function(){
					//TODO
					$.ajax({
						url : '../OperateSellOutFood.do',
						data : {
							dataSource : 'setFoodLimit',
							foodId : param.food.id,
							remain : self.find('[id="setLimit_num_setLimit"]').val()
						},
						type : 'post',
						dataType : 'json',
						beforeSent : function(){
							Util.LM.show();
						},
						success : function(jr){
							Util.msg.tip(jr.msg);
							if(jr.success){
								_thiz.close();
							}
						},
						error : function(){
						
						},
						complete : function(){
							Util.LM.hide();
							if(param.postConfirm && typeof param.postConfirm == 'function'){
								param.postConfirm();
							}
						}
					})
					
				});
			
			}
		});
		
		this.open = function(afterOpen){
			_setLimitPopup.open();
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		}		
		
		this.close = function(afterClose, timeout){
			_setLimitPopup.close();
			
			if(afterClose && typeof afterClose == 'function'){
				if(afterClose && typeof afterClose == 'function'){
					if(timeout){
						setTimeout(afterClose, timeout);
					}else{
						afterClose();
					}					
				}			
			}
		}
		
	}
	
	exports.newInstance = function(param){
		return new SetLimitPopup(param);
	};
	
});
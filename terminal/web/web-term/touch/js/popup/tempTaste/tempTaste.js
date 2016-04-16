define(function(require, exports, modele){
	function TempTastePopup(param){
		param = param || {
			selectedFood : null,     //当前选中的菜品
			confirm : function(selectedFood, name, price){}   //确认按钮
		}
		
		var _tempTastePopup = null;
		var _self = null;
		var thiz = this;
		
		_tempTastePopup = new JqmPopupDiv({
			loadUrl : './popup/tempTaste/tempTaste.html',
			pageInit : function(self){
				
				_self = self;
				
				initTempTaste();
							
				//保存按钮
				self.find('[id="saveTempTaste_a_tempTaste"]').click(function(){
					var name = self.find('[id="tempTasteName_input_tempTaste"]').val();
					var price = self.find('[id="tempTastePrice_input_tempTaste"]').val();
					
					_tempTastePopup.close();
					param.confirm(param.selectedFood, name, price);
				});	
				
				//关闭按钮
				self.find('[id="closeTempTaste_a_tempTaste"]').click(function(){
					thiz.close();
				});
				
				//名称清空
				self.find('[id="delName_a_tempTaste"]').click(function(){
					self.find('[id="tempTasteName_input_tempTaste"]').val('');
				});
				
				//价钱清空
				self.find('[id="delPrice_a_tempTaste"]').click(function(){
					self.find('[id="tempTastePrice_input_tempTaste"]').val('');
				});
				
			}
		});
		
		function initTempTaste(){
			if(param.selectedFood.hasTempTaste()){
				_self.find('[id="tempTasteName_input_tempTaste"]').val(param.selectedFood.tasteGroup.tmpTaste.name);
				_self.find('[id="tempTastePrice_input_tempTaste"]').val(param.selectedFood.tasteGroup.tmpTaste.price);
			}else{
				_self.find('[id="tempTasteName_input_tempTaste"]').val('');
				_self.find('[id="tempTastePrice_input_tempTaste"]').val('');
			}
		}
		
		
		
		this.open = function(afterOpen){
			_tempTastePopup.open(function(){
				HandWritingAttacher.instance().attach(_self.find('[id="tempTasteName_input_tempTaste"]')[0]);
				NumKeyBoardAttacher.instance().attach(_self.find('[id="tempTastePrice_input_tempTaste"]')[0]);	
			});
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		}
		
		this.close = function(afterClose, timeout){
			_tempTastePopup.close(function(){
				HandWritingAttacher.instance().detach(_self.find('[id="tempTasteName_input_tempTaste"]')[0]);
				NumKeyBoardAttacher.instance().detach(_self.find('[id="tempTastePrice_input_tempTaste"]')[0]);
			});
			
			if(afterClose &&　typeof afterClose == 'function'){
				if(timeout){
					setTimeout(afterOpen, timeout);
				}else{
					afterClose();
				}
			}
			
		}
	}
	
	exports.newInstance = function(param){
		return new TempTastePopup(param);
	}
	
});
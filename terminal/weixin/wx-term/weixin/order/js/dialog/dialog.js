function DialogPopup(param){
	
	var _self = this;
	var _dialogDiv = null;
	param = param || {
		titleText : null,      //标题内容
		titleClass : null,     //标题样式
		content : null,       //内容
		contentCallback : function(){},   //内容的回调
		leftText : null,      //左边按钮文字
		leftClass : null,     //左边按钮的样式
		left : function(){},  //左边按钮的方法
		rightText : null,	  //右边按钮文字
		rightClass : null,    //右边按钮的样式
		right : function(){}  //右边按钮的方法
	}
	
	
	function _init(){
		_dialogDiv = $('<div data-type="shadow_div_dialog" class="div-mask-dialogPopup">'
							+'<div class="dialog-dialogPopup">'
								+ '<div class="dialog-title-dialogPopup"><font data-type="dialogTitle_div_dialogPopup"></font><div data-type="dialogClose_div_dialogPopup" style="width:30px;height:30px;background-color:red;float:right;font-size:19px;" class="dialog-close-dialogPopup">×</div></div>'
								+ '<div data-type="dialogContent_div_dialogPopup" class="dialog-msg-dialogPopup"></div>'
								+ '<div style="height:20px;"></div>'
								+ '<div data-type="dialogButton_div_dialogPopup" class="dialog-buttons-dialogPopup">'
									+  '<button data-type="left_button_dialogPopup" class="orange">确定</button><button class="orange" style="display:none;" data-type="right_button_dialogPopup">取消</button>'
								+ '</div>'
							+ '</div>'
						+ '</div>');
		 
		if(_dialogDiv){
			
		}
		$('body').append(_dialogDiv);
			
		_dialogDiv.css({
			'display' : 'block'
		})		
		
		if(param.left && typeof param.left == 'function' && param.right && typeof param.right == 'function'){
			_dialogDiv.find('[data-type="dialogButton_div_dialogPopup"]').removeClass('dialog-buttons-dialogPopup').addClass('dialog-button-dialogPopup')
			_dialogDiv.find('[data-type="dialogButton_div_dialogPopup"]').find('button:first').after('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
			_dialogDiv.find('[data-type="right_button_dialogPopup"]').show();
		}
		
		//点击shadow关闭
		_dialogDiv.click(function(){
			_self.close();
			
		});
		
		_dialogDiv.find('.dialog-dialogPopup').click(function(e){
			e.stopPropagation();
		});
		
		
		//右上角的关闭按钮
		_dialogDiv.find('[data-type="dialogClose_div_dialogPopup"]').click(function(){
			_self.close();
		})
		
		//contentdMember
		_dialogDiv.find('[data-type="dialogContent_div_dialogPopup"]').html(param.content);
		
		//contentCallback
		if(param.contentCallback){
			param.contentCallback(_dialogDiv);
		}
		
		
		//标题的文字
		if(param.titleText){
			_dialogDiv.find('[data-type="dialogTitle_div_dialogPopup"]').text(param.titleText);
		}
		
		//左边按钮的文字
		if(param.leftText){
			_dialogDiv.find('[data-type="left_button_dialogPopup"]').text(param.leftText);
		}
		
		//左边按钮的回调方法
		if(param.left && typeof param.left == 'function'){
			_dialogDiv.find('[data-type="left_button_dialogPopup"]').click(function(){
				param.left(_dialogDiv);
			});
		}
		
		//右边按钮的文字
		if(param.rightText){
			_dialogDiv.find('[data-type="right_button_dialogPopup"]').text(param.rightText);
		}
		
		//右边按钮的方法
		if(param.right && typeof param.right == 'function'){
			_dialogDiv.find('[data-type="right_button_dialogPopup"]').click(function(){
				param.right();
			});
		}
		
		
	}
	
	
	this.open = function(){
		_init();
	};
	
	
	this.close = function(afterClose, timeout){
		_dialogDiv.remove();
		if(afterClose && typeof afterClose == 'function'){
			if(timeout){
				setTimeout(afterClose, timeout)
			}else{
				afterClose();
			}
		}
	};
	
	
	
	
}
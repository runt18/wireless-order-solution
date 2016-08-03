function WeDialogPopup(param){
	
	var _self = this;
	var _dialogDiv = null;
	param = param || {
		titleText : null,      				//标题内容
		titleClass : null,     				//标题样式
		content : null,       				//内容
		contentCallback : function(container){},   	//内容的回调
		leftText : null,      				//左边按钮文字
		leftClass : null,     				//左边按钮的样式
		left : function(_dialogDiv){},  	//左边按钮的方法
		rightText : null,	  				//右边按钮文字
		rightClass : null,    				//右边按钮的样式
		right : function(_dialogDiv){},  	//右边按钮的方法
		afterClose : function(){},   		//点击阴影和关闭的回调函数
		dismissible : false
	}
	
	
	function _init(){
		_dialogDiv = $('<div class="weui_dialog_confirm" id="dialog1" style="z-index:600000;display: block;">'
							+'<div class="weui_mask" id="weDialogMask_mask_weDialog"></div>'
								+ '<div class="weui_dialog" id="dialogContainer_div_weDialog">'
								+		'<div class="weui_dialog_hd"><strong class="weui_dialog_title" data-type="dialogTitle_div_dialogPopup"></strong></div>'
								+ 		'<div class="weui_dialog_bd" id="dialogContent_div_dialogPopup" data-type="dialogContent_div_dialogPopup"></div>'
								+ 		'<div class="weui_dialog_ft">'
               					+			 '<a href="javascript:;" class="weui_btn_dialog default" data-type="left_button_WeDialogPopup" style="display:none;"></a>'
                				+			 '<a href="javascript:;" class="weui_btn_dialog primary" data-type="right_button_WeDialogPopup" style="display:none;"></a>'
            					+ 		'</div>'
            					+ '</div>'
						+ '</div>'
							);
		$('body').append(_dialogDiv);
		
		_dialogDiv.find('[id=weDialogMask_mask_weDialog]').css({
			'z-index' : '4000'
		});
		
		_dialogDiv.find('[id=dialogContainer_div_weDialog]').css({
			'z-index' : '5000'
		});
		if(param.left && typeof param.left == 'function' && param.right && typeof param.right == 'function'){
			_dialogDiv.find('[data-type="dialogButton_div_dialogPopup"]').removeClass('dialog-buttons-dialogPopup').addClass('dialog-button-dialogPopup')
			_dialogDiv.find('[data-type="dialogButton_div_dialogPopup"]').find('button:first').after('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
			_dialogDiv.find('[data-type="right_button_WeDialogPopup"]').show();
		}
		
		//点击shadow关闭
		if(!param.dismissible){
			_dialogDiv.find('[id=weDialogMask_mask_weDialog]').click(function(){
				_self.close();
			});
		}
		
		_dialogDiv.find('.dialog-dialogPopup').click(function(e){
			e.stopPropagation();
		});
		
		
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
			_dialogDiv.find('[data-type="left_button_WeDialogPopup"]').text(param.leftText);
			_dialogDiv.find('[data-type="left_button_WeDialogPopup"]').css('display', 'inline-block');
		}
		
		//左边按钮的回调方法
		if(param.left && typeof param.left == 'function'){
			_dialogDiv.find('[data-type="left_button_WeDialogPopup"]').click(function(){
				param.left(_dialogDiv);
			});
		}
		
		//右边按钮的文字
		if(param.rightText){
			_dialogDiv.find('[data-type="right_button_WeDialogPopup"]').text(param.rightText);
			_dialogDiv.find('[data-type="right_button_WeDialogPopup"]').css('display', 'inline-block');
		}
		
		//右边按钮的方法
		if(param.right && typeof param.right == 'function'){
			_dialogDiv.find('[data-type="right_button_WeDialogPopup"]').click(function(){
				param.right(_dialogDiv);
			});
		}
	}
	
	this.open = function(afterOpen){
		_init();
		if(afterOpen){
			afterOpen();
		}
	};
	
	
	this.close = function(afterClose, timeout){
		_dialogDiv.remove();
		if(afterClose && typeof afterClose == 'function'){
			if(timeout){
				setTimeout(afterClose, timeout)
			}else{
				afterClose();
			}
			
		}else if(param.afterClose && typeof param.afterClose == 'function'){
			param.afterClose();
			
		}
	};
	
}
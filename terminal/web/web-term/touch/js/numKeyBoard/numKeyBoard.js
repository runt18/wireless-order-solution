function NumKeyBoard(param){
	//在div上增加numberKeyBoard
	var keyBoard = $("<div/>");
	keyBoard.addClass('keys');
	keyBoard.css({
		'height' : '210px',
		'width' : '300px',
		'margin-top' : '-20px'
	}); 
	
	//数字键盘的增加
	var numKeys = new Array("7", "8", "9", "0", "4", "5", "6", ".", "1", "2", "3", "C");
	
	var keys = "";
	for(var i = 0; i < numKeys.length; i++){
		var eachKeys = '<span>' + numKeys[i] + '</span>';
		if(i % 4 == 0){
			keys += '<br>';
		}
		keys += eachKeys;
	}	
	keyBoard.append(keys);
	$(param.renderTo).append(keyBoard);
	
	$(param.renderTo).find('span').each(function(index, element){
		element.onclick = function(e){
			if(param.result){ 
				var value = $(element).text();
				if(isNaN(value)){
					param.result(value);
				}else{ 
					param.result(parseInt(value)); 
				}
			}
		};
	});
	
	
	
}

	
var NumKeyBoardAttacher = (function(){
	
	function Singleton(){
		
		var attachInputs = [];
		var activeInput = null;
		var container = null;
		var numKeyBoard = null;
		var isMouseOver = false;
		this.attach = function(attachTo, postInput){
			//检查是否有重复控件
			var isExist = attachInputs.some(function(item, index, array){
				return item.attachObj.id == attachTo.id;
			});
			
			if(!isExist){
				var attachInput = {
					attachObj : attachTo,
					postInput : postInput,
					focusFn : function(){
						if(container == null){
							container = $('<div/>');
							container.addClass('ui-overlay-shadow ui-corner-all');
							container.css({
								'float' : 'right',
								'margin-top' : '25%',
								'right' : '0px',
								'top' : 'initial',
								'bottom' : '0',
								'position' : 'absolute',
								'z-index' : '24000'
							});
							
							var header = $('<div/>');
							header.addClass("ui-corner-top ui-header ui-bar-b");
							header.css({
								'line-height' : '45px',
								'font-size' : '20px',
								'text-align' : 'center'
							});
							header.text("数字键盘");
							
							var body = $('<div/>');
							body.addClass('calculator');
							body.css({
								'overflow-y' : 'auto'
							});
							
							container.on('mouseover', function(){
								isMouseOver = true;
							});
							
							container.on('mouseout', function(){
								isMouseOver = false;
							});
							
							container.append(header).append(body);
							$('body').append(container);
							
						numKeyBoard = new NumKeyBoard({
								renderTo : body[0],
								result : function(value){
									if(value == 'C'){
										$(activeInput.attachObj).val('');
										$(activeInput.attachObj).focus();
									}else{
										//获取当前选中的文字
										var getSelected = function(){
											var t = '';
											if(window.getSelection) {
											    t = window.getSelection();
											} else if(document.getSelection) {
											    t = document.getSelection();
											} else if(document.selection) {
											    t = document.selection.createRange().text;
											}
											return t;
										};
										if(getSelected().toString() != ""){
											var s = $(activeInput.attachObj).val();
  											$(activeInput.attachObj).val(s.replace(getSelected().toString(), value));
  											$(activeInput.attachObj).focus();
										}else{
											$(activeInput.attachObj).val($(activeInput.attachObj).val() + value);
											$(activeInput.attachObj).focus();
										}
									}
									if(activeInput.postInput){
										activeInput.postInput($(activeInput.attachObj).val());
									}
								}
							});
						}
						for(var i = 0; i < attachInputs.length; i++){
	        				if(attachInputs[i].attachObj.id == attachTo.id){
	        					activeInput = attachInputs[i];
	        					break;
	        				}
	        			}
					},
					blurFn : function(){
						if(isMouseOver){
							 $(activeInput.attachObj).focus();
						}else{
							if(container){
								container.remove();
								container = null;
								numKeyBoard = null;
							}
							activeInput = null;
						}
					}
				
				};
				
				attachInputs.push(attachInput);
				
				$(attachTo).on('focus', attachInput.focusFn);
				
				$(attachTo).on('blur', attachInput.blurFn);
			}
			return this;
		};
		
		this.detach = function(detachFrom){
			//如果detach的输入框是当前激活的，则删除数字键盘
			if(activeInput && activeInput.attachObj.id == detachFrom.id){
				if(container){
					container.remove();
					container = null;
					numKeyBoard = null;
				}
				activeInput = null;
			}
			//删除focus和blur事件的处理函数
			for(var i = 0; i < attachInputs.length; i++){
				if(attachInputs[i].attachObj.id == detachFrom.id){
					$(attachInputs[i].attachObj).off('focus', attachInputs[i].focusFn);
					$(attachInputs[i].attachObj).off('blur', attachInputs[i].blurFn);
				}
			}
			//删除数组中的Input组件
			attachInputs = attachInputs.filter(function(item, index, array){
				return detachFrom.id != item.attachObj.id;
			});
			return this;
		};
	}
	
	//实例容器
	var instance = null;
	
	var _static = {
		
		//获取实例的方法
		//返回Singleton的实例
		instance : function(){
			if(instance == null){
				instance = new Singleton();
			}
			return instance;
		}
	};
	return _static;
})();

function NumKeyBoardPopup(param){
	var _param = param || {
		header : '',
		rightText : '',
		right : function(){},
		leftText : '',
		left : function(){},
		middleText : '',
		middle : function(){},
		last : function(){},
		hasComment : false
	};
	//创建数字键盘
	function createNumberKeyBoard(input){
		//createNumKeyBoard一声明就执行
		 new NumKeyBoard({
			renderTo : document.getElementById('content_div_numKbPopup'),
			result : function(value){
				input.focus();
				//获取当前选中的文字
				function getSelected(){
					var t = '';
					if(window.getSelection) {
					    t = window.getSelection();
					} else if(document.getSelection) {
					    t = document.getSelection();
					} else if(document.selection) {
					    t = document.selection.createRange().text;
					}
					return t;
				}
				if(value == '+'){
					if(input.val() == ""){
						input.val(input.val() + 1);
					}else{
						input.val(parseFloat(input.val()) + 1);
					}					
				}else if(value == '-'){
					if(input.val() == ""){
						input.val("");
					}else{
						input.val(parseFloat(input.val()) - 1);
					}	
				}else if(value == 'C'){
					input.val('');
					input.focus();
				}else{
					var s = input.val();			
					if(getSelected().toString() != ""){
						input.val(s.replace(getSelected().toString(), value));
					}else{
						input.val(s + value);
					}
				}		
			}
		});
	}

	var _self;
	var numberPopup = null;
	numberPopup = new JqmPopup({
		loadUrl : './popup/keyboard/number.html',
		pageInit : function(self){
			_self = self;
			//创建数字键盘
			//createNumKeyBoard一声明就执行
			createNumberKeyBoard(self.find('[id = input_input_numKbPopup]'));
			
			if(_param.hasComment === true){
				var remark = '<div>'+
								'<input id="inputTableOpenCommon" placeholder="开台备注" data-type="num" class="countInputStyle" >'+
							'</div>';
				self.find('[id=content_div_numKbPopup]').append(remark);
				self.find('[id=content_div_numKbPopup]').trigger('create');
				NumKeyBoardAttacher.instance().attach(self.find('[id=inputTableOpenCommon]')[0]);
			}
			
			self.find('[id=header_div_numKbPopup] h3').html(_param.header);
			
			if(_param.rightText){
				self.find('[id=right_a_numKbPopup] span span').text(_param.rightText);
			}
			
			if(_param.middleText){
				self.find('[id=middle_a_numKbPopup] span span').text(_param.middleText);
			}
			
			if(_param.leftText){
				self.find('[id=left_a_numKbPopup] span span').text(_param.leftText);
			}
			
			//绑定取消按钮
			$('#right_a_numKbPopup').click(function(){
				 _param.right(); 
			 });
			 
			 //绑定确定按钮
			 self.find('[id=left_a_numKbPopup]').click(function(){
				 _param.left();
			 });
			 
			 if(_param.middle){
				 //绑定点菜按钮
				 self.find('[id=middle_a_numKbPopup]').click(function(){
					 _param.middle();
				 });
			 }
			 
			 if(_param.last){
				//绑定查看预订按钮
				 self.find('[id=last_a_numKbPopup]').click(function(){
					 _param.last();
				 });  
			 }
			
		}
	});	
	
	this.open = function(afterOpen){
		numberPopup.open(afterOpen);
	};
	
	this.close = function(afterOpen, timeout){
		if(_param.hasComment === true){
			NumKeyBoardAttacher.instance().detach(_self.find('[id=inputTableOpenCommon]')[0]);
		}
		numberPopup.close(afterOpen, timeout);
	};
}


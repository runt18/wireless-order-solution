define(function(require, exports, module){
	function AskTablePopup(param){
	
		param = param || {
			tables : [],				//餐台列表
			title : '',					//标题
			leftText : '',				//左Button文字
			left : null,				//左Button方法
			middleText : '',			//中间Button文字
			middle : null,				//中间Button方法
			rightText : '',				//右Button文字
			right : null,				//右Button方法
			tableSelect : null,			//点击匹配餐台的方法
			suffixSelect : null         //后缀的方法
		};
		
		var _self = this;
		
		var _popupInstance = null;
		_popupInstance = new JqmPopup({
			loadUrl : './popup/table/ask.html',
			pageInit : function(self){
				//设置标题
				if(param.title){
					self.find('[id=title_div_askTable]').html(param.title);
				}
				//设置右Button文字
				if(param.rightText){
					self.find('[id=right_a_askTable]').html(param.rightText);
				}
				//设置右Button方法
				if(param.right){
					self.find('[id=right_a_askTable]').click(param.right);
				}else{
					self.find('[id=right_a_askTable]').click(function(){
						_self.close();
					});
				}
				
				//设置左button文字
				if(param.leftText){
					self.find('[id=left_a_askTable] span span').text(param.leftText);
				}
				
				//设置左button方法
				if(param.left){
					self.find('[id=left_a_askTable]').click(function(){
						param.left();
					});
				}
				
				//设置中间的文字
				if(param.middleText){
					self.find('[id=middle_a_askTable] span span').text(param.middleText);
				}
				
				//设置中间的方法
				if(param.middle){
					self.find('[id=middle_a_askTable]').click(function(){
						param.middle();
					});
				}
				
				//后缀的方法
				if(param.suffixSelect && typeof param.suffixSelect == 'function'){
					self.find('[id=suffix_div_ask] a').each(function(index, element){
						element.onclick = function(){
							param.suffixSelect($(element).text());
						}
					});
				}
				
			}
		});	
		
		this.open = function(afterOpen){
			_popupInstance.open(function(self){
				//数字键盘绑定填写台号框
				NumKeyBoardAttacher.instance().attach(self.find('[id=left_input_askTable]')[0], function(inputVal){
					matchTable(inputVal, self);	
				});
				
				//数字键盘绑定填写要转去的台号输入框
				NumKeyBoardAttacher.instance().attach(self.find('[id=tranNum_input_ask]')[0]);
				NumKeyBoardAttacher.instance().attach(self.find('[id=foodAmountText_input_ask]')[0]);
				
				//数字键盘绑定'开台备注'
				NumKeyBoardAttacher.instance().attach(self.find('[id=apartComment_input_ask]')[0]);
				
				//键盘输入匹配
				self.find('[id=left_input_askTable]').on('keyup', function(){
					if(self.find('[id=left_input_askTable]').val()){
						matchTable(self.find('[id=left_input_askTable]').val(), self);
					}
					
				});
				
				if(afterOpen && typeof afterOpen == 'function'){
					afterOpen();
				}
				self.find('[id=left_input_askTable]').focus();
			});
		};
		
		this.close = function(afterClose, timeout){
			_popupInstance.close(function(self){
				NumKeyBoardAttacher.instance().detach(self.find('[id=left_input_askTable]')[0]);
				
				NumKeyBoardAttacher.instance().detach(self.find('[id=tranNum_input_ask]')[0]);
				NumKeyBoardAttacher.instance().detach(self.find('[id=foodAmountText_input_ask]')[0]);
				
				NumKeyBoardAttacher.instance().detach(self.find('[id=apartComment_input_ask]')[0]);
				
				//删除keypress事件
				self.find('[id=left_input_askTable]').off('keyup');
				
				if(afterClose && typeof afterClose == 'function'){
					if(timeout){
						setTimeout(afterClose, timeout);
					}else{
						afterClose();
					}
				}
			});
		};
		
		this.prefect = function(){
			//查台
			return param.tables.getByAlias($('#left_input_askTable').val());
		};
		
		
		function matchTable(inputVal, self){
			if(param.tables){
				self.find('[id=left_input_askTable]')[0].focus();
				//显示匹配的餐台
				function showMatchedTable(matchedTables){
					var tableCmpTemplate = 
						'<a data-role="button" data-corners="false" data-inline="true" class="tableCmp" data-index={dataIndex} data-value={id} data-theme={theme}>' +
							'<div style="height: 70px;">{name}<br>{alias}' +
								'<div class="{tempPayStatusClass}">{tempPayStatus}</div>'+
								'<div class="bookTableStatus">{bookTableStatus}</div>'+
							'</div>'+
						'</a>';
					var html = [];
					for (var i = 0; i < matchedTables.length; i++) {
						
						var aliasOrName;
						if(matchedTables[i].categoryValue == 1){
							aliasOrName = matchedTables[i].alias;
						}else{
							aliasOrName = '<font color="green">' + matchedTables[i].categoryText +'</font>';
						}
						html.push(tableCmpTemplate.format({
							dataIndex : i,
							id : matchedTables[i].id,
							alias : aliasOrName,
							theme : matchedTables[i].statusValue == '1' ? "e" : "c",
							name : matchedTables[i].name,
							tempPayStatus : matchedTables[i].isTempPaid ? '暂结' : '&nbsp;&nbsp;',
							bookTableStatus : matchedTables[i].isBook ? '订' : '',
							tempPayStatusClass : navigator.userAgent.indexOf("Firefox") >= 0 ? 'tempPayStatus4Moz' : 'tempPayStatus'
						}));	
					}
					self.find('[id=matchedTables_div_askTable]').html(html.join(''));
					self.find('[id=matchedTables_div_askTable] a').buttonMarkup('refresh');
					self.find('[id=matchedTables_div_askTable] a').each(function(index, element){
						$(element).click(function(){
							if(param.tableSelect){
								param.tableSelect(matchedTables[$(element).attr('data-index')]);
							}
						});
					});
				}
				
				//显示匹配条件的餐台
				showMatchedTable(param.tables.getByFuzzy(inputVal).slice(0, 8));
			}
		}
	}
	
	exports.newInstance = function(param){
		return new AskTablePopup(param);
	}
});



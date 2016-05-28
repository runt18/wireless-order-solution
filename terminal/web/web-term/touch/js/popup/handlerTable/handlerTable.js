define(function(require, exports, module){	
	function CreateHandlerTable(param){
		param = param || {
			title : null,       //标题
			type : null,        //控件类型
			leftText : null,   //左边的文字
			rightText : null,   //右边的文字
			left : function(selectedTable){},      //左边的方法
			right : function(selectedTable){}      //右边的方法
		}
		
		//选择的餐桌
		var _selectedTable = [];
		
		var _handlerTable = null;
		_handlerTable = new JqmPopupDiv({
			loadUrl : './popup/handlerTable/handlerTable.html',
			pageInit : function(self){
				//关闭
				self.find('[id="close_a_handlerTable"]').click(function(){
					_handlerTable.close();
				});
				
				//标题
				if(param.title){
					self.find('[id="title_span_handlerTable"]').text(param.title);
				}
					
				//左边按钮
				if(param.leftText){
					self.find('[id="left_a_handlerTable"] span span').html(param.leftText);
				}
				
				
				function initTables(tables){
					//餐台
					var tableTemplet = '<a data-role="button" style="margin-top:11px;margin-left:11px;" data-type="tablesClick" data-corners="false" data-inline="true" class="tableCmp" data-index={dataIndex} data-value={id} data-theme={theme}>' +
						'<div style="height: 70px;">{name}<br>{alias}' +
							'<div class="{tempPayStatusClass}">{tempPayStatus}</div>'+
							'<div class="bookTableStatus">{bookTableStatus}</div>'+
						'</div>'+
					'</a>';
					
					var html = [];
					for (var i = 0; i < tables.length; i++) {
						tables[i] = new WirelessOrder.TableWrapper(tables[i]);
						var aliasOrName;
						if(tables[i].isNormal()){//一般台
							aliasOrName = tables[i].alias;
						}else if(tables[i].isJoin()){//搭台
							var begin = tables[i].name.indexOf("(");
							var end = tables[i].name.indexOf(")");
							aliasOrName = '<font color="green">' + tables[i].name.substring(begin+1, end) +'</font>';
						}else{
							aliasOrName = '<font color="green">'+ tables[i].categoryText +'</font>';
						}		
						html.push(tableTemplet.format({
							dataIndex : i,
							id : tables[i].id,
							alias : aliasOrName,
							theme : tables[i].isBusy() ? "e" : "c",
							name : tables[i].name == "" || typeof tables[i].name != 'string' ? tables[i].alias + "号桌" : tables[i].name,
							tempPayStatus : tables[i].isTempPaid? '暂结' : '&nbsp;&nbsp;',
							bookTableStatus : tables[i].isBook ? '订' : '',
							tempPayStatusClass : navigator.userAgent.indexOf("Firefox") >= 0?'tempPayStatus4Moz':'tempPayStatus'		
						}));				
					}
					$('#handlerTableChoose_div_handleTable').html(html.join("")).trigger('create');
					
					$('#handlerTableChoose_div_handleTable').find('[data-type="tablesClick"]').each(function(index, element){
						element.onclick = function(){
							for(var i= 0; i < _selectedTable.length; i++){
								if($(element).attr('data-value') == _selectedTable[i].id){
									_selectedTable.splice(i, 1);
								}
								initTables(_selectedTable);
							}
						}
					});
				}
				
				//左边按钮方法
				self.find('[id="left_a_handlerTable"]').click(function(){
					var askTable = require('../table/ask.js');
					var askTablePopup = askTable.newInstance({
						title : '选择餐桌',
						tables : WirelessOrder.tables,
						tableSelect : function(selectedTable){
							var hasTables = true;
							for(var i = 0; i < _selectedTable.length; i++){
								if(_selectedTable[i].id == selectedTable.id){
									_selectedTable.splice(i, 1);
									hasTables = false;
									break;
								}
							}
							
							if(hasTables){
								if(param.type && param.type == "multiOpenTable"){
									if(WirelessOrder.tables.getById(selectedTable.id).statusValue == 1){
										Util.msg.tip("此餐台已使用, 不能选择");
										return;
									}
								}else if(param.type && param.type == "spellingTable"){
									if(WirelessOrder.tables.getById(selectedTable.id).statusValue == 0){
										Util.msg.tip("此餐台未开台, 不能选择");
										return;
									}
								}
								_selectedTable.push(WirelessOrder.tables.getById(selectedTable.id));
							}
							initTables(_selectedTable);
							askTablePopup.close();
						}
					});
					
					askTablePopup.open(function(){
						$('#left_a_askTable').hide();
						$('#middle_a_askTable').css('width', '48%');
						$('#right_a_askTable').css('width', '50%');
					});
				});
				
				//右边按钮
				if(param.rightText){
					self.find('[id="right_a_handlerTable"] span span').html(param.rightText);
				}
				
				if(param.right && typeof param.right == 'function'){
					self.find('[id="right_a_handlerTable"]').click(function(){
						param.right(_selectedTable);
					});
				}
				
			}
		});
		
		
		this.open = function(afterOpen){
			_handlerTable.open();
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		}
		
		this.close = function(afterClose, timeout){
			_handlerTable.close();
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
		return new CreateHandlerTable(param);
	};
});	
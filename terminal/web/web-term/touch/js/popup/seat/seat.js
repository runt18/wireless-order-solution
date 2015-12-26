function CreateInSeatDiv(param){
	
	param = param || {
		//FIXME  要传出book
		right : function(tables){},  //右边的方法	
		book : '',             //预订数据
	};
	
	//已定餐台
	var _bookTables = [];
	//入座餐台
	var _seatTables = [];
	var _seatDiv = null;
	_seatDiv = new JqmPopupDiv({
		loadUrl : './popup/seat/seat.html',
		pageInit : function(self){
			//关闭按钮
			self.find('[id="close_a_seat"]').click(function(){
				_seatDiv.close();
				$('#shadowForPopup').hide();
			});
			
			//选择餐桌按钮
			self.find('[id="left_a_seat"]').click(function(){
				var askTablePopup = null;
				askTablePopup = new AskTablePopup({
					title : '入座选桌',
					tables : WirelessOrder.tables,
					tableSelect : function(selectedTable){
						var hasTables = true;
						
						for(var i = 0; i < _seatTables.length; i++){
							if(_seatTables[i].id == selectedTable.id){
								_seatTables.splice(i, 1);
								hasTables = false;
								break;
							}
						}
						
						if(hasTables){
							if(WirelessOrder.tables.getById(selectedTable.id).isBusy()){
								Util.msg.tip("此餐台已使用, 不能选择");
								return;
							}
							_seatTables.push(WirelessOrder.tables.getById(selectedTable.id));
						}
						
						initTables(_seatTables, 'seat');
						askTablePopup.close();
					}
				});
				
				askTablePopup.open(function(){
					$('#left_a_askTable').hide();
					$('#middle_a_askTable').css('width', '48%');
					$('#right_a_askTable').css('width', '50%');
				});
				
			});
			
			//入座的回调函数
			self.find('[id="right_a_seat"]').click(function(){
				param.right(_seatTables);
			});
			
			
		}
	})
	
	this.open = function(afterOpen){
		_seatDiv.open(function(self){
			initTables(param.book.tables);
		});
		$('#shadowForPopup').show();
		if(afterOpen && typeof afterOpen == 'function'){
			afterOpen();
		}
	}
	
	this.close = function(afterClose, timeout){
		_seatDiv.close();
		$('#shadowForPopup').hide();
	}
	
	//入座餐桌
	function initTables(tables, type){
		var html = [];
		for (var i = 0; i < tables.length; i++) {
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
			html.push(tableCmpTemplet.format({
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
		if(type == "seat"){
			$('#seatTable_div_seat').html(html.join("")).trigger('create');
		}else{
			$('#bookTable_div_seat').html(html.join("")).trigger('create');
		}
		
	}
	
}
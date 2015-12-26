function CreateAddBookInfo(param){
	param = param || {
		title : '',   //标题
		bookFoods : function(tables){},	//菜品预订的方法
		confirm : function(tables){},	//确认的方法
		cancel : function(){},   //取消的方法
		type : '',                //打开预订信息的类型
		book : '',                //预订的信息
		isNeedFoot : true            //是否需要脚部
	}
	
	var _self = this;
	var _tables = [];
	var _addBookInfo = null;
	_addBookInfo = new JqmPopupDiv({
		loadUrl : './popup/books/books.html',
		pageInit : function(self){
			//右上角的关闭按钮
			self.find('[id="close_a_books"]').click(function(){
				_self.close();
			});
			
			
			//标题
			if(param.title){
				self.find('[id="title4AddBook"]').text(param.title);
			}
			
			//取消按钮
			self.find('[id="cancel_a_books"]').click(function(){
					_self.close();
			});
			
			//左边按钮方法
			self.find('[id="left_a_books"]').click(function(){
				var askTablePopup = null;
				askTablePopup = new AskTablePopup({
					title : '选择餐桌',	
					tables : WirelessOrder.tables,
					tableSelect : function(selectedTable){
						var hasTables = true;
						for(var i = 0; i < _tables.length; i++){
							if(_tables[i].id == selectedTable.id){
								_tables.splice(i, 1);
								if(_tables.length == 0){
									self.find('[id="bookTableList_tr_books"]').hide();
								}
								hasTables = false;
								break;
							}
						}
						
						if(hasTables){
							_tables.push(WirelessOrder.tables.getById(selectedTable.id));
						}
							//显示预订添加界面餐台
						self.find('[id="bookTableList_tr_books"]').show();
						initTables(_tables);
						askTablePopup.close();
					}
				});
				askTablePopup.open(function(){
					$('#left_a_askTable').hide();
					$('#middle_a_askTable').css('width', '48%');
					$('#right_a_askTable').css('width', '50%');
				});
			});
			
			//中间的方法
			self.find('[id="bookFoods_a_books"]').click(function(){
				if(param.bookFoods && typeof param.bookFoods == 'function'){
					param.bookFoods(_tables);
				}
			});
			
			//右边的方法
			self.find('[id="confirm_a_books"]').click(function(){
				if(param.confirm && typeof param.confirm == 'function'){
					param.confirm(_tables);
				}
			});
			
		}
	});
	
	
	this.open = function(afterOpen){
		
		_addBookInfo.open(function(self){	
			//加载经手人
			$.post('../QueryStaff.do', function(data){
				if(data.success){
					var html = [];
					var option = '<option value="{id}">{name}</option>';
					data.root.forEach(function(e){
						html.push(option.format({
							id : e.staffID,
							name : e.staffName
						}));
					});
					self.find('[id="staff_select_books"]').html(html.join("")).selectmenu("refresh");
					
				}
			}, 'json');
			
			//时分插件
			self.find('[id="add_bookTimeBox"]').html('<input id="bookTime_input_books" class="bookTime" >').trigger('create');
			self.find('.bookTime').timepicki();	
			
			var now = new Date();
			self.find('[id="bookDate_input_books"]').val(now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate());
			self.find('[id="bookMoney_input_books"]').val(0);
			
			if(param.isNeedFoot == true || typeof param.isNeedFoot == 'undefined'){
				self.find('[id="bookInfoFoot_div_books"]').show();
			}else{
				self.find('[id="bookInfoFoot_div_books"]').hide();
			}
			
			
			if(param.type){
				$.post('../OperateBook.do', {
					dataSource : 'getByCond',
					bookId : param.book.id,
					detail : true
				}, function(data){
					if(data.success){
						self.find('[id="bookDate_input_books"]').text(data.root[0].bookDate.substring(0, 11));
						self.find('[id="bookTime_input_books"]').val(data.root[0].bookDate.substring(11, 16));
						self.find('[id="bookPerson_input_books"]').val(data.root[0].member);
						self.find('[id="bookPhone_input_books"]').val(data.root[0].tele);
						self.find('[id="bookPersonAmount_input_books"]').val(data.root[0].amount);
						self.find('[id="bookCate_select_books"]').val(data.root[0].category).selectmenu("refresh");
						self.find('[id="bookReserved_input_books"]').val(data.root[0].reserved);
						self.find('[id="staff_select_books"]').val(data.root[0].staffId).selectmenu("refresh");
						self.find('[id="bookMoney_input_books"]').val(data.root[0].money);
						self.find('[id="staff_select_books"]').selectmenu("refresh");
						self.find('[id="bookCate_select_books"]').selectmenu("refresh");
						if(data.root[0].tables.length > 0){
							self.find('[id="bookTableList_tr_books"]').show();
							_tables = data.root[0].tables;
							initTables(_tables);
						}else{
							self.find('[id="bookTableList_tr_books"]').hide();
						}
						
						if(data.root[0].order && data.root[0].order.orderFoods.length > 0){
							//查看预订菜品	
							var bookOrderFoodListCmpTemplet = '<tr class="{isComboFoodTd}">'
									+ '<td>{dataIndex}</td>'
									+ '<td ><div class={foodNameStyle}>{name}</div></td>'
									+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
									+ '<td><div style="height: 25px;overflow: hidden;">{tastePref}</div></td>'
									+ '<td>{unitPrice}</td>'
									+ '</tr>';	
							var orderFoods = data.root[0].order.orderFoods;
							
							var html = [];
							for (var i = 0; i < orderFoods.length; i++) {
								html.push(bookOrderFoodListCmpTemplet.format({
									dataIndex : i + 1,
									id : orderFoods[i].id,
									name : orderFoods[i].foodName,
									count : orderFoods[i].count,
									isWeight : (orderFoods[i].status & 1 << 7) != 0 ? 'initial' : 'none',
									tastePref : orderFoods[i].tasteGroup.tastePref,
									unitPrice : orderFoods[i].unitPrice.toFixed(2) + (orderFoods[i].isGift?'&nbsp;[<font style="font-weight:bold;">已赠送</font>]':''),
									isComboFoodTd : "",
									foodNameStyle : "commonFoodName"
								}));
							}
							
							self.find('[id="foodList_tbody_books"]').html(html.join('')).trigger('create');
							
							//显示预订界面菜品显示
							self.find('[id="bookOrderFoodList_td_books"]').show();				
						}
					}
				});
			}
//			$('#shadowForPopup').show();
		})
		
		if(afterOpen && typeof afterOpen == 'function'){
			afterOpen();
		}
		
	};
	
	this.close = function(afterClose, timeout){
		_addBookInfo.close();
//		$('#shadowForPopup').hide();
		if(afterClose && typeof afterClose == 'function'){
			if(timeout){
				setTimeout(afterClose, timeout);
			}else{
				afterClose();
			}
		}	
	};
	
	function initTables(tables){
		//餐台
		var tableTemplet = '<a data-role="button" data-type="tablesClick" data-corners="false" data-inline="true" class="tableCmp" data-index={dataIndex} data-value={id} data-theme={theme}>' +
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
		$('#tableList_td_books').html(html.join("")).trigger('create');
		
		$('#tableList_td_books').find('[data-type="tablesClick"]').each(function(index, element){
			element.onclick = function(){
				for(var i= 0; i < _tables.length; i++){
					if($(element).attr('data-value') == _tables[i].id){
						_tables.splice(i, 1);
					}
					initTables(_tables);
				}
			}
		});
	}
}
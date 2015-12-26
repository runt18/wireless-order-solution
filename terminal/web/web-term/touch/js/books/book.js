//进入订单列表Entry
var books = {
	entry : function(){},    //book入口
	bookFoods : []
};
books.entry = function(){
	//去已预订界面
	location.href="#bookOrderListMgr";
	
};
$(function(){
	//预订列表
	var bookList = null;
	
	//预订列表匹配
	var bookSearch = {
		file : null,
		fileValue : null,
		init : function(c){
			this.file = document.getElementById(c.file);
			if(typeof this.file.oninput != 'function'){
				this.file.oninput = function(e){
					bookSearch.fileValue = bookSearch.file.value;
					var data = null, temp = null;
					if(bookSearch.fileValue.trim().length > 0){
						data = [];
						temp = bookList;
						for(var i = 0; i < temp.length; i++){
							if((temp[i].tele).indexOf(bookSearch.fileValue.trim()) != -1){
								data.push(temp[i]);
							}
						}				
						if(data != null){
							loadBookListData(data);			
						}
						
						data = null;
						temp = null;					
					}else{
						loadBookListData(bookList);	
					}
					

				};
			}
			return this.file;
		},
		valueBack : function(){
			if(this.file.value){
				this.file.value = this.file.value.substring(0, this.file.value.length - 1);
				this.file.oninput(this.file);			
			}

			this.file.focus();
		},
		select : function(){
			this.file.select();
		},
		clear : function(){
			this.file.value = '';
			this.file.oninput(this.file);
			this.file.select();
		},
		callback : function(){
			co.s.clear();
		},
		onInput : function(){
			this.file.oninput(this.file);		
		},	
		fireEvent : function(){
			bookSearch.onInput();
		}
	};
	
	/**
	 * 预订列表名称匹配
	 */
	var bookSearchByName = {
		file : null,
		fileValue : null,
		init : function(c){
			this.file = document.getElementById(c.file);
			if(typeof this.file.oninput != 'function'){
				this.file.oninput = function(e){
					bookSearchByName.fileValue = bookSearchByName.file.value;
					var data = null, temp = null;
					if(bookSearchByName.fileValue.trim().length > 0){
						data = [];
						temp = bookList;
						for(var i = 0; i < temp.length; i++){
							if((temp[i].member).indexOf(bookSearchByName.fileValue.trim()) != -1){
								data.push(temp[i]);
							}
						}				
						if(data != null){
							loadBookListData(data);			
						}
						
						data = null;
						temp = null;					
					}else{
						loadBookListData(bookList);	
					}
				};
			}
			return this.file;
		},
		valueBack : function(){
			if(this.file.value){
				this.file.value = this.file.value.substring(0, this.file.value.length - 1);
				this.file.oninput(this.file);			
			}

			this.file.focus();
		},
		select : function(){
			this.file.select();
		},
		clear : function(){
			this.file.value = '';
			this.file.oninput(this.file);
			this.file.select();
		},
		callback : function(){
			co.s.clear();
		},
		onInput : function(){
			this.file.oninput(this.file);		
		},	
		fireEvent : function(){
			bookSearchByName.onInput();
		}
	};



	//查询列表
	function searchBookList(c){
		Util.LM.show();
		
		var name = $('#searchBookPerson_input_tableSelect').val();
		var phone = $('#searchBookPhone_input_tableSelect').val();
		var status = $('#searchBookStatus').val();
		var bookDate = $('input[name=bookDateType]:checked').val();
		var begin, end;
		
		if(c && c.begin){
			begin = c.begin;
			end = c.end;
		}else{
			$('#conditionDayBegin_td_tableSelect').hide();
			$('#conditionDayEnd_td_tableSelect').hide();
		}
		
		$.post('../OperateBook.do', {
			dataSource : 'getByCond',
			name : name,
			phone : phone,
			status : status,
			bookDate : bookDate,
			beginDate : begin,
			endDate : end
		}, function(data){
			Util.LM.hide();
			
			if(data.success){
				bookList = data.root;
				//加载预订数据
				loadBookListData(data.root);
			}
			
		}, 'json');	
	};
	
	//加载预订数据
	function loadBookListData(data){
		var bookListTemplate = '<tr>' +
								'<td>{list}</td>' +
								'<td>{bookDate}</td>' +
								'<td>{region}</td>' +
								'<td>{member}</td>' +
								'<td>{tele}</td>' +
								'<td>{amount}</td>' +
								'<td>{status}</td>' +
								'<td>{staff}</td>' +
								'<td><a data-type="lookOut" data-value={bookId} href="#">{lookout}</a></td>' +
								'<td><div data-role="controlgroup" data-type="horizontal"><a href="#" data-role="button" data-value={bookId} data-type="change" data-theme="b">{confirmOrUpdate}</a><a data-role="button" data-theme="b" data-value={bookId} data-type="inSeat">入座</a><a data-role="button" data-theme="b" data-value={bookId} data-type="delete" href="#">删除</a></div></td>' +
								'</tr>';
	
		var html = [];
		for (var i = 0; i < data.length; i++) {
			var region = [];
			if(data[i].tables.length > 0){
				for (var j = 0; j < data[i].tables.length; j++) {
					region.push(data[i].tables[j].name);
				}
				region = region.join(",");
			}else{
				region = data[i].region;
			}
			
			html.push(bookListTemplate.format({
				list : i+1,
				index : i,
				bookId : data[i].id,
				bookDate : data[i].bookDate,
				lookout : data[i].isExpired ? '<span style="color:red">查看(超时)</span>' : '查看',
				region : region,
				member : data[i].member,
				tele : data[i].tele,
				amount : data[i].amount,
				status : data[i].status == 1? '<span style="color:green">'+ data[i].statusDesc +' ('+ data[i].sourceDesc +')</span>' : data[i].statusDesc,
				confirmOrUpdate : data[i].status == 1? '确认' : '修改',
				staff : data[i].staff
			}));
		}
		$('#bookOrderListBody').html(html.join("")).trigger('create');
		
		
		//预订列表详情点击事件
		$('#bookOrderListBody').find('[data-type="lookOut"]').each(function(index, element){
			element.onclick = function(){
				var book;
				for(var i = 0; i < bookList.length; i++){
					if($(element).attr('data-value') == bookList[i].id){
						book = bookList[i];
					}
				}
				
				var lookBookInfo = new CreateAddBookInfo({
					title : '查看预订--' + book.sourceDesc,
					type : 'look',
					book : book,
					isNeedFoot : false
				});																																	
				lookBookInfo.open();
			}
		});
		
		//预订详情的入座
		$('#bookOrderListBody').find('[data-type="inSeat"]').each(function(index, element){
			element.onclick = function(){
				var book;
				for(var i = 0; i < bookList.length; i++){	
					if($(element).attr('data-value') == bookList[i].id){
						book = bookList[i];
					}
				}
				
				if(book.status != 2){
					Util.msg.tip("预订不是【已确认】状态, 不能入座");
					return;
				}
	
				if(book.isExpired){
					Util.msg.tip("预订已过期, 不能入座");
					return;
				}
				
				var inSeatPopup = new CreateInSeatDiv({
					book : book,
					seat : function(tables){
						if(tables.length == 0){
							Util.msg.tip("请选择餐台");
							return;
						}
						
						$.post('../OperateBook.do', {
							dataSource : 'getByCond',
							bookId : book.id,
							detail : 'true'
						}, function(data){
							if(data.success){
								//进入点菜界面
								of.entry({
									initFoods : data.root[0].order ? data.root[0].order.orderFoods : null,
									table : tables[0],
									comment : '',
									orderFoodOperateType : 'bookSeat',
									commit : function(selectedFoods){
										
										var bookOrders = [];
										for(var i = 0; i < tables.length; i++){
											var orderDataModel = {};
											orderDataModel.tableID = tables[i].id;
											orderDataModel.orderFoods = selectedFoods.slice(0);
											orderDataModel.categoryValue = tables[i].categoryValue;
											bookOrders.push(JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)));
										}
										
										Util.LM.show();
										$.post('../OperateBook.do', {
											dataSource : 'seat',
											bookId : book.id,
											bookOrders : bookOrders.join("<li>")
										}, function(data){
											Util.LM.hide();
											if(data.success){
												Util.msg.tip(data.msg);
												books.entry();
											}else{
												Util.msg.tip(data.msg);
											}
										}, 'json');
										
									}
								});	
								inSeatPopup.close();
							}else{''
								Util.msg.tip(data.msg);
							} 		
						});
					}
					
				});
				inSeatPopup.open();
			}
		});
		
		//预订详情的修改
		$('#bookOrderListBody').find('[data-type="change"]').each(function(index, element){
			element.onclick = function(){
				var book;
				for(var i = 0; i < bookList.length; i++){
					if($(element).attr('data-value') == bookList[i].id){
						book = bookList[i];
					}
				}
				
				if(book.status == '3'){
					Util.msg.tip("已入座,不能修改");
					return;
				}
				
				var changeBookInfo = new CreateAddBookInfo({
					title : '确认预订',
					book : book,
					type : 'change',
					bookFoods : function(tables){
						var bookTables = [];
						//关闭shadow
						$('#shadowForPopup').hide();
						//进入点菜界面
						if(book.tables.length > 0){
							bookTables = book.tables;
						}else{
							bookTables = tables;
						}
						of.entry({
							table :bookTables.length > 0 ? bookTables[0] : {name : '预订台', alias: ''},
							comment : '',
							//不清空已点菜
							orderFoodOperateType : 'addBook'
						});	
					},
					confirm : function(tables){
						var bookDate = $('#bookDate_input_books').val();
						var time = $('#bookTime_input_books').val();
						var member = $('#bookPerson_input_books').val();
						var tele = $('#bookPhone_input_books').val();
						var amount = $('#bookPersonAmount_input_books').val();
						var cate = $('#bookCate_select_books').val();
						var reserved = $('#bookReserved_input_books').val();
						var staff = $('#staff_select_books').val();
						var money = $('#bookMoney_input_books').val();
						
					
						if(!bookDate || !reserved || !member || !tele || !amount || !money){
							Util.msg.tip("请填写完整信息");
							return;
						}
						
						if(tables.length == 0){
							Util.msg.tip("请选择餐台");
							return;		
						}
						
						if(time.indexOf('PM') > 0){
							var hourString = time.substring(0, time.indexOf(':'));
							var hour = parseInt(hourString)+12;
							var minute = time.substr(time.indexOf(':')+1, 2);
							time = hour + ":" + minute + ':' + "59";
						}else if(time.indexOf('AM') > 0){
							var hour = time.substring(0, time.indexOf(':'));
							var minute = time.substr(time.indexOf(':')+1, 2);	
							time = hour + ":" + minute + ':' + "59";
						}else{
							time += ":59";
						}
						
						var tablesId = [];
						for (var i = 0; i < tables.length; i++) {
							tablesId.push(tables[i].id);
						}
						
						
						var orderFoods = [];
						for (var i = 0; i < books.bookFoods.length; i++) {
							orderFoods.push(JSON.stringify(books.bookFoods[i]));
						}
						
						Util.LM.show();
						$.post('../OperateBook.do', {
							dataSource : 'update',
							bookId : book.id,
							bookDate : bookDate + " " + time,
							member : member,
							tele : tele,
							amount : amount,
							cate : cate,
							reserved : reserved,
							staff : staff,
							money : money,
							comment : '',
							tables : tablesId.join("&"),
							orderFoods : orderFoods.join("&")
						}, function(data){
							Util.LM.hide();
							if(data.success){
								Util.msg.tip(data.msg);
								changeBookInfo.close();
								searchBookList();
							}else{
								Util.msg.tip(data.msg);
							}
						});
					}
				});
				changeBookInfo.open();
			}
		});
		
		//预订详情的删除
		$('#bookOrderListBody').find('[data-type="delete"]').each(function(index, element){
			element.onclick = function(){
				var book;
				for(var i = 0; i < bookList.length; i++){
					if($(element).attr('data-value') == bookList[i].id){
						book = bookList[i];
					}
				}
				Util.msg.alert({
					title : '提示',
					msg : '是否删除该预订?',
					buttons : 'YESBACK',
					renderTo : 'bookOrderListMgr',
					certainCallback : function(btn){
						if(btn == 'yes'){
							Util.LM.show();
							$.post('../OperateBook.do', {
								dataSource : 'delete',
								bookId : book.id
							}, function(data){
								Util.LM.hide();
								Util.msg.tip(data.msg);
								searchBookList();
							}, 'json');
						}
					}
				});
			}
		});
		
	}
	
	//初始化日期
	function initDay(){
		var now = new Date();
		
		var today = now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate();
		
		now.setDate(now.getDate() + 1);
		var tomorrow = now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate();

		now.setDate(now.getDate() + 1);
		var afterday = now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate();
		
		$('#bookDate_today').attr('value', today);
		$('#bookDate_tomorrow').attr('value', tomorrow);
		$('#bookDate_afterday').attr('value', afterday);
	}

	
	$('#bookOrderListMgr').on('pageshow', function(){
		
		//用户自定义日期
		$('#conditionDayBeginDay_input_tableSelect').on('change', function(){
			if($('#conditionDayBeginDay_input_tableSelect').val() && $('#conditionDayEndDay_input_tableSelect').val() ){
				searchBookList({begin:$('#conditionDayBeginDay_input_tableSelect').val(), end:$('#conditionDayEndDay_input_tableSelect').val()});
			}
		});	
		$('#conditionDayEndDay_input_tableSelect').on('change', function(){
			if($('#conditionDayBeginDay_input_tableSelect').val() && $('#conditionDayEndDay_input_tableSelect').val() ){
				searchBookList({begin:$('#conditionDayBeginDay_input_tableSelect').val(), end:$('#conditionDayEndDay_input_tableSelect').val()});
			}
		});	
		
		//订单状态选择
		$('#searchBookStatus').on('change', function(){
			searchBookList();
		});
		
		
		//条件筛选初始化
		bookSearch.init({file : 'searchBookPhone_input_tableSelect'});	
		bookSearchByName.init({file : 'searchBookPerson_input_tableSelect'});	
		
		//查找订单
		searchBookList();
		
		//初始化日期
		initDay();
		
		
	});
	
	
	$('#bookOrderListMgr').on('pageinit', function(){
		//预订返回按钮
		$('#bookBack_a_tableSelect').click(function(){
			//返回到餐桌界面
			ts.loadData();
			
			delete ts.bookTable4Search;
			
			$('#searchBookPerson_input_tableSelect').val("");
			$('#searchBookPhone_input_tableSelect').val("");
			$('#searchBookStatus').val(-1).selectmenu("refresh");
			$('input[name="bookDateType"]:checked').removeAttr("checked").checkboxradio("refresh");
			$("input[name=bookDateType]:eq(0)").attr("checked",'checked').checkboxradio("refresh");	
		});
		
		//预订刷新
		$('#bookRefresh_a_tableSelect').click(function(){
			$('#searchBookPerson_input_tableSelect').val("");
			$('#searchBookPhone_input_tableSelect').val("");
			$('#searchBookStatus').val(-1).selectmenu("refresh");
			$('input[name="bookDateType"]:checked').removeAttr("checked").checkboxradio("refresh");
			$("input[name=bookDateType]:eq(0)").attr("checked",'checked').checkboxradio("refresh");
			
			//去已预订界面
			location.href="#bookOrderListMgr";
		});
		
		//选择日期搜索
		$('#daySelect_fieldset_tableSelect').find('input[name="bookDateType"]').each(function(index, element){
			element.onclick = function(){
				if($(element).attr('data-type') == 'condition'){
					$('#conditionDayBeginDay_input_tableSelect').val('');
					$('#conditionDayEndDay_input_tableSelect').val('');
					$('#conditionDayBegin_td_tableSelect').show();
					$('#conditionDayEnd_td_tableSelect').show();
				}else{
					searchBookList();
				}
			}
		});
		
		//添加
		$('#addBooksInfo').click(function(){
			var addBookInfo = new CreateAddBookInfo({
				bookFoods : function(tables){
					//关闭shadow
					$('#shadowForPopup').hide();
					//进入点菜界面
					of.entry({
						table : tables.length > 0 ? tables[0] : {name : '预订台', alias: ''},
						comment : '',
						//不清空已点菜
						orderFoodOperateType : 'addBook'
					});	
				},
				confirm : function(tables){
					var bookDate = $('#bookDate_input_books').val();
					var time = $('#bookTime_input_books').val();
					var member = $('#bookPerson_input_books').val();
					var tele = $('#bookPhone_input_books').val();
					var amount = $('#bookPersonAmount_input_books').val();
					var cate = $('#bookCate_select_books').val();
					var reserved = $('#bookReserved_input_books').val();
					var staff = $('#staff_select_books').val();
					var money = $('#bookMoney_input_books').val();
					
				
					if(!bookDate || !reserved || !member || !tele || !amount || !money){
						Util.msg.tip("请填写完整信息");
						return;
					}
					
					if(tables.length == 0){
						Util.msg.tip("请选择餐台");
						return;		
					}
					
					if(time.indexOf('PM') > 0){
						var hourString = time.substring(0, time.indexOf(':'));
						var hour = parseInt(hourString)+12;
						var minute = time.substr(time.indexOf(':')+1, 2);
						time = hour + ":" + minute + ':' + "59";
					}else if(time.indexOf('AM') > 0){
						var hour = time.substring(0, time.indexOf(':'));
						var minute = time.substr(time.indexOf(':')+1, 2);	
						time = hour + ":" + minute + ':' + "59";
					}else{
						time += ":59";
					}
					
					var tablesId = [];
					for (var i = 0; i < tables.length; i++) {
						tablesId.push(tables[i].id);
					}
					
					var orderFoods = [];
					for (var i = 0; i < books.bookFoods.length; i++) {
						orderFoods.push(JSON.stringify(books.bookFoods[i]));
					}
					
					Util.LM.show();
					$.post('../OperateBook.do', {
						dataSource : 'insert',
						bookDate : bookDate + " " + time,
						member : member,
						tele : tele,
						amount : amount,
						cate : cate,
						reserved : reserved,
						staff : staff,
						money : money,
						comment : '',
						tables : tablesId.join("&"),
						orderFoods : orderFoods.join("&")
					}, function(data){
						Util.LM.hide();
						if(data.success){
							Util.msg.tip(data.msg);
							addBookInfo.close();
							searchBookList();
						}
					});
				}
			});
			addBookInfo.open();
		});

		//预订选好了
		$('#addBookOrderFood').click(function(){
				
				if(of.newFood.length == 0){
					Util.msg.alert({
						msg : '请选择菜品',
						renderTo : 'orderFoodMgr'
					});		
					return ;
				}else{
					books.bookFoods = of.newFood.slice(0);
				}
				
				//返回预订界面
				history.back();
				
				var html = [];
				var bookOrderFoodListCmpTemplet = '<tr class="{isComboFoodTd}">'
							+ '<td>{dataIndex}</td>'
							+ '<td ><div class={foodNameStyle}>{name}</div></td>'
							+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
							+ '<td><div style="height: 25px;overflow: hidden;">{tastePref}</div></td>'
							+ '<td>{unitPrice}</td>'
							+ '</tr>';
							
				books.bookFoods.forEach(function(e, index){
					html.push(bookOrderFoodListCmpTemplet.format({
						dataIndex : index + 1,
						id : e.id,
						name : e.name,
						count : e.count,
						isWeight : e.isWeight() ? 'initial' : 'none',
						tastePref : e.tasteGroup.tastePref,
						unitPrice : e.unitPrice.toFixed(2) + (e.isGift ? '&nbsp;[<font style="font-weight:bold;">已赠送</font>]' : ''),
						isComboFoodTd : '',
						foodNameStyle : 'commonFoodName'
					}));
				});
				
				$('#foodList_tbody_books').html(html.join('')).trigger('create');
				
				//显示预订界面菜品显示
				$('#bookOrderFoodList_td_books').show();	
		});
	});
});
wxOrder = {
	entry : function(){
		location.href="#wxOrderWin_div_wxOrder";     //入口
	}    
}

$(function(){
	var wxOrderList = null;
	var WxStatus = {
			COMMITTED : { val : 2, desc : '待确认'},
			ORDER_ATTACHED : { val : 3, desc : '已下单'}
		};
		
	
	$('#searchWxOrderNumber_input_wxOrder').on('keyup', function(){
		if($('#searchWxOrderNumber_input_wxOrder').val() != ''){
			var data = [];
			var temp = wxOrderList;
			for(var i = 0; i < temp.length; i++){
				if(temp[i].code.toString().indexOf($('#searchWxOrderNumber_input_wxOrder').val()) != -1){
					data.push(temp[i]);
				}
			}
			loadWXOrderList(data);
		}else{
			loadWXOrderList(wxOrderList);
		}
		
	});
	
	//查询列表
	function searchList(){
		var status = $('#searchWxOrderStatus_select_wxOrder').val();
		
		Util.LM.show();
		$.ajax({
			url : '../QueryWxOrder.do',
			type : 'post',
			dataType : 'json',
			data : {
				dataSource : 'getByCond',
				detail : true,
				status : status
			},
			success : function(data){
				Util.LM.hide();
				if(data.success){
					wxOrderList = data.root;
					loadWXOrderList(data.root);
				}else{
					Util.msg.tip(data.msg);					
				}
			}
		});
	}
	
	function loadWXOrderList(data){
		var bookListTemplate = '<tr>' +
									'<td>{list}</td>' +
									'<td>{wxOrderNumber}</td>' +
									'<td>{wxOrderTime}</td>' +	
									'<td>{memberName}</td>' +
									'<td>{tel}</td>' +
									'<td>{tableName}</td>' +
									'<td>{status}</td>' +
									'<td>' +
										'<div data-role="controlgroup" data-type="horizontal">' +
											'<a href="#" data-table={tableId} data-role="button" data-value={wxOrderNumber} data-status={orderStatus} data-type="wxOrderConfirm_a_wxOrder" data-theme="b">{confirm}</a>' +
											'<a href="#"  data-role="button" data-number={wxOrderNumber} data-value={wxOrderId} data-status={orderStatus} data-type="wxOrderDelete_a_wxOrder" data-theme="b">{del}</a>' +
										'</div>' +
									'</td>' +
								'</tr>';
		
		var html = [];
		for(var i = 0; i < data.length; i++){
			html.push(bookListTemplate.format({
				list : i+1,
				wxOrderId : data[i].id,
				wxOrderNumber : data[i].code,
				wxOrderTime : data[i].date,
				orderStatus : data[i].statusVal,
				confirm : data[i].statusVal == WxStatus.COMMITTED.val ? '下单' : '账单已处理(' + data[i].orderId + ')',
				del : '删除', 
				memberName : data[i].member.name,
				tel : data[i].member.mobile,
				tableName : data[i].table ? data[i].table.name : '---',
				status : data[i].statusDesc,
				tableId : data[i].table ? data[i].table.id : '---'
			}));
		}
		$('#wxOrderList_tbody_wxOrder').html(html.join("")).trigger('create');
		
		//删除操作
		$('#wxOrderList_tbody_wxOrder').find('[data-type="wxOrderDelete_a_wxOrder"]').each(function(index, element){
			element.onclick = function(){
				var id = $(element).attr('data-value');
				
				Util.msg.alert({
					title : '提示',
					msg : '是否删除订单编号为 ' + $(element).attr('data-number') + '的微定账单?',
					buttons : 'YESBACK',
					renderTo : 'bookOrderListMgr',
					certainCallback : function(btn){
						if(btn == 'yes'){
							Util.LM.show();
							$.ajax({
								url : '../QueryWxOrder.do',
								type : 'post',
								async : false,
								dataType : 'json',
								data : {
									dataSource : 'delectById',
									id : id
								},
								success : function(result, status, xhr){
									if(result.success){
										Util.msg.tip(result.msg);
										Util.LM.hide();
										searchList();
									}else{
										Util.msg.tip(result.msg);
									}
								}
							});
						}
					}
				});
				
				
				
			}
			
			
			
		});	
		
		//下单操作
		$('#wxOrderList_tbody_wxOrder').find('[data-type="wxOrderConfirm_a_wxOrder"]').each(function(index, element){
			element.onclick = function(){
				if($(element).attr('data-status') == WxStatus.COMMITTED.val){
					var code = $('#wxOrderList_tbody_wxOrder').find('[data-type="wxOrderConfirm_a_wxOrder"]').attr('data-value');
					$.ajax({
						url : '../QueryWxOrder.do',
						type : 'post',
						async : false,
						dataType : 'json',
						data : {
							dataSource : 'getByCond',
							code : code,
							detail : true
						},
						success : function(result, status, xhr){
							if(result.success){
								var askPopup = new AskTablePopup({
									tables : WirelessOrder.tables,
									title : '请选择餐桌',
									leftText : '确定',
									left : function(){
										var inputVal  = $('#left_input_askTable').val();
										if(inputVal == ""){
											Util.msg.tip("请选择餐桌");
										}else{
											//结账
											var perfectMatched = askPopup.prefect();
											if(perfectMatched){
												askPopup.close(function(){
													of.entry({
														orderFoodOperateType : 'normal',
														initFoods : result.root[0].foods,
														wxCode : result.root[0].code,
														table : perfectMatched
													});
												}, 200);
											}else{
												Util.msg.tip('没有此餐台,请重新输入');
											}
										}
									},
									tableSelect : function(selectedTable){
										askPopup.close(function(){
											of.entry({
												orderFoodOperateType : 'normal',
												initFoods : result.root[0].foods,
												table : selectedTable,
												wxCode : result.root[0].code
											});
										}, 200);
									}
				
								});
								
								askPopup.open(function(self){
									$('#middle_a_askTable').hide();
									$('#left_a_askTable').css('width', '48%');
									$('#right_a_askTable').css('width', '50%');
									if(result.root[0].table){
										$('#addCmp_div_askTable').show();
									}
									var tableTemplate = 
										'<a data-role="button" data-corners="false" data-inline="true" class="tableCmp"  data-value={id} data-theme={theme}>' +
											'<div style="height: 70px;">{name}<br>{alias}' +
												'<div class="{tempPayStatusClass}">{tempPayStatus}</div>'+
												'<div class="bookTableStatus">{bookTableStatus}</div>'+
											'</div>'+
										'</a>';
									var html = [];
									var aliasOrName;
									if(result.root[0].table){
										if(result.root[0].table.categoryValue == 1){
											aliasOrName = result.root[0].table.alias;
										}else{
											aliasOrName = '<font color="green">' + result.root[0].table.categoryText +'</font>';
										}
										
										html.push(tableTemplate.format({
											id : result.root[0].table.id,
											alias : aliasOrName,
											theme : result.root[0].table.statusValue == '1' ? "e" : "c",
											name : result.root[0].table.name,
											tempPayStatus : result.root[0].table.isTempPaid ? '暂结' : '&nbsp;&nbsp;',
											bookTableStatus :result.root[0].table.isBook ? '订' : '',
											tempPayStatusClass : navigator.userAgent.indexOf("Firefox") >= 0 ? 'tempPayStatus4Moz' : 'tempPayStatus'
										}));	
										$('#tables_div_askTable').html(html.join(''));
										$('#tables_div_askTable a').buttonMarkup('refresh');
										$('#tables_div_askTable a').each(function(index, element){
											element.onclick = function(){
												askPopup.close(function(){
													of.entry({
														orderFoodOperateType : 'normal',
														initFoods : result.root[0].foods,
														wxCode : result.root[0].code,
														table : result.root[0].table
													});
												}, 200);
											}
										});
									}
								});
							}else{
								Util.msg.tip(result.msg);
							}
						}
					});
				}else{
					var table= null;
					for(var i = 0; i < data.length; i++){
						if(data[i].table){
							if(data[i].table.id == $(element).attr('data-table')){
								table = data[i].table;
							}
						}
					}
					uo.entry({table : table});
				}
			}
		});
	}
	
	
	$('#wxOrderWin_div_wxOrder').on('pageshow', function(){
		//查询列表
		searchList();
		
		//订单状态选择
		$('#searchWxOrderStatus_select_wxOrder').on('change', function(){
			searchList();
		});
	});
	
	
	$('#wxOrderWin_div_wxOrder').on('pageinit', function(){
		//返回
		$('#wxOrderBack_a_wxOrder').click(function(){
			//返回到餐桌界面
			ts.loadData();
			$('#searchwxOrderNumber_input_wxOrder').val("");
			$('#searchWxOrderStatus_select_wxOrder').val(2).selectmenu("refresh");
		});
		
		//刷新
		$('#wxOrderRefresh_a_wxOrder').click(function(){
			$('#searchWxOrderNumber_input_wxOrder').val("");
			$('#searchWxOrderStatus_select_wxOrder').val(2).selectmenu("refresh");
			searchList();
		});
		
	});
	
});
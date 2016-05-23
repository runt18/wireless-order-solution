define(function(require, exports, module){
	function wxOrderListPopup(wxOrderList){
		
		//获取微信订单里面的所有会员并按照会员id来分开会员所点的菜品
		var memberWxOrderList = {};
		var memberAmount = 0;
		var start = 0;
		var limit = 7;
		
//		var wxOrders = uo.order.weixinOrder;
		var wxOrders = wxOrderList;
//		wxOrders.forEach(function(el, index){
//			if(!memberWxOrderList[el.member.id]){
//				el.foods.forEach(function(foodOrder){
//					foodOrder['date'] = el.date;
//				});
//				memberWxOrderList[el.member.id] = {
//					member : el.member,
//					foods : el.foods
//				};	
//				memberAmount++;
//			}else{
//				memberWxOrderList[el.member.id][foods] = memberWxOrderList[el.member.id][foods].concat(el.foods);
//			}
//		});
		
		wxOrders.forEach(function(el, index){
			memberWxOrderList[el.id] = el;
		});
		
		
		//memberWxOrderList保存有 foods
		var memberBtnTemplate = '<a data-role="button" data-inline="true" class="comboFoodPopTopBtn " data-value={id} name="wxOrderMemberBtn" ' +
					   			'data-theme="{theme}" style="margin:0;height:43px;line-height:43px;border-radius:0;" >' +
					   			'<div>{name}</div>' +
					   			'</a>';

		var wxOrderFoodTemplate = '<tr class="ui-bar-d">'+
		            	  			  '<th style="width: 25px;"></th>'+
		           		  			  '<th style="width: 100px;">{foodName}</th>'+
		            	  			  '<th style="width: 100px;">{amount}</th>'+
		           					  '<th style="width: 100px;">{taste}</th>'+
		            				  '<th style="width: 100px;">{price}</th>'+
		            				  '<th style="width: 100px;">{time}</th>'+
		           					  '<th style="width: 100px;">{waiter}</th>'+
		         				  '</tr>';			   			
		var wxOrderPopup;

		wxOrderPopup = new JqmPopup({
			content : '<div id="wxOrderListPopup_div_checkOut" data-role="popup" data-theme="c" data-dismissible="false" style="" class="ui-overlay-shadow ui-corner-all" align="center">'+
	    			  	  '<div id="wxOrderListHeader_div_checkOut" data-role="header" data-theme="b" class="uui-corner-top ui-header ui-bar-b" style="height:45px;">'+
	  					  	  '<div id="wxOrderMemberList_div_checkOut" style="width:;float:left;">' +
	  					  	  '<div id="wxOrderMemberBtnList_div_checkOut" style="width:;float:left;">' +
	  					  	  '</div>'+
	  					  	  '</div>'+
	    			  	  	  '<span id="closeBtnOfWxOrderList_span_checkOut" class="popupWinCloseBtn ui-btn ui-shadow ui-btn-up-b" style="float:right;height:43px;line-height:43px;">X</span>'+
	       				  '</div>'+
						  '<table  style="max-height:330px;overflow-y:auto;" data-role="table"  data-mode="columntoggle" class="ui-body-d ui-shadow table-stripe ui-responsive infoTableMgr" >'+
	       				  	'<thead>'+
		         		  		'<tr class="ui-bar-d">'+
		            	  			'<th style="width: 25px;"></th>'+
		           		  			'<th style="width: 150px;">菜名</th>'+
		            	  			'<th style="width: 100px;">数量</th>'+
		           					'<th style="width: 100px;">口味</th>'+
		            				'<th style="width: 100px;">单价</th>'+
		            				'<th style="width: 170px;">时间</th>'+
		           					'<th style="width: 100px;">点菜会员</th>'+
		         				'</tr>'+
	        				'</thead>'+
	        				'<tbody id="memberWxOrderListBody_tbody_checkOut">'+
	        				'</tbody>'+
	     				'</table>' +
					  '</div>',
			pageInit : function(self){
				$('#wxOrderListPopup_div_checkOut').find('[id="closeBtnOfWxOrderList_span_checkOut"]').click(function(){
					wxOrderPopup.close();
				});
				
				var html = [];
				for(var member in memberWxOrderList){
					var wxOrderMemberObj = memberWxOrderList[member];
					html.push(memberBtnTemplate.format({
						id : 'wxOrderMember_' + member,
						theme : 'b',
						name : wxOrderMemberObj.member.name + '(' + wxOrderMemberObj.code + ')'
					}));
				}
				
				if(wxOrderList.length > limit){
						$('#wxOrderListPopup_div_checkOut').find('[id=wxOrderMemberList_div_checkOut]').append('<a name="previousPage_a_checkOut" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true">L</a>' +
						'<a name="nextPage_a_checkOut" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true">R</a>').trigger('create');
				}
				
				function initHeader(){
					var appendHtml = html.slice(start, (start + limit) > html.length ? html.length : (start + limit) );
					$('#wxOrderListPopup_div_checkOut').find('[id=wxOrderMemberBtnList_div_checkOut]').html(appendHtml.join('')).trigger('create');
					
					$('#wxOrderListPopup_div_checkOut').find('[name=wxOrderMemberBtn]').each(function(index, el){
						el.onclick = function(){
							$('#wxOrderListPopup_div_checkOut').find('[name=wxOrderMemberBtn]').each(function(index, btnEl){
								if(btnEl.getAttribute('data-value') != el.getAttribute('data-value')){
									$(btnEl).attr('data-theme', 'b').removeClass('ui-btn-up-e').addClass('ui-btn-up-b');
								}else{
									$(btnEl).attr('data-theme', 'e').addClass('ui-btn-up-e');
								}
							});
							
							var foodList = [];
							var memberWxOrder = memberWxOrderList[el.getAttribute('data-value').split('_')[1]];
							memberWxOrder.foods.forEach(function(ele, eindex){
								 foodList.push(wxOrderFoodTemplate.format({
							 		foodName : ele.foodName,
							 		amount : ele.count,
							 		taste : ele.tasteGroup.tastePref,
							 		price : ele.totalPrice,
							 		time : memberWxOrder.date,
							 		waiter : memberWxOrder.member.name
							 	}));
							 });
							 $('#wxOrderListPopup_div_checkOut').find('[id=memberWxOrderListBody_tbody_checkOut]').html(foodList.join(''));
							
						}
					});
				}
				
				initHeader();
				
				//上一页
				$('#wxOrderListPopup_div_checkOut').find('[name="previousPage_a_checkOut"]').click(function(){
					if(start > 0){
						start -= limit;
					}
					initHeader();
					console.log(1);
				});
				
				//下一页
				$('#wxOrderListPopup_div_checkOut').find('[name="nextPage_a_checkOut"]').click(function(){
					if(start < html.length){
						start += limit;
					}
					initHeader();
					console.log(1);
				});
				
				self.find('[name=wxOrderMemberBtn]')[0].onclick();
				
			}
		});
		
		this.open = function(afterOpen){
			wxOrderPopup.open();
			if(afterOpen){
				afterOpen($('#wxOrderListPopup_div_checkOut'));
			}
		}
		
		this.close = function(afterClose){
			wxOrderPopup.close();
			if(afterClose){
				afterClose($('#wxOrderListPopup_div_checkOut'));
			}
		}
	}
	
	exports.newInstance = function(wxOrderList){
		return new wxOrderListPopup(wxOrderList);
	}
	
});
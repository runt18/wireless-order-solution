function DetailPopup(param){
	
	param = param || {
		order : '',  //账单
		table : '',   //餐桌
		detailType : ''  //明细类型
	};
	
	var _detailPopup = null;
	//明细数据
	var _orderFoodDetail = [];
	//总价
	var _detailTotalPrice = null;
	
	_detailPopup = new JqmPopup({
		loadUrl : './popup/detail/detail.html',
		pageInit : function(self){
			self.find('input[name=lookupType]').attr('checked', false);
			//右上角的关闭按钮
			self.find('[id=detailPopup_a_detail]').click(function(){
				_detailPopup.close();
			});
			
			//全部按钮
			self.find('input[data-type=allDetail_input_detail]').click(function(){
				filterDetail(DetailPopup.DetailType.ALL.val, self);
			});
			
			//赠送按钮
			self.find('input[data-type=giftDetail_input_detail]').click(function(){
				filterDetail(DetailPopup.DetailType.GIFT.val, self);
			});
			
			//折扣
			self.find('input[data-type=discountDetail_input_detail]').click(function(){
				filterDetail(DetailPopup.DetailType.DISCOUNT.val, self);
			});
			
			//转菜
			self.find('input[data-type=transDetail_input_detail]').click(function(){
				filterDetail(DetailPopup.DetailType.TRANS.val, self);
			});
			
			//退菜
			self.find('input[data-type=cancelDetail_input_detail]').click(function(){
				filterDetail(DetailPopup.DetailType.CANCEL.val, self);
			});
			
			//根据param决定调用那个按钮
			if(param.detailType == DetailPopup.DetailType.CANCEL.val){
				self.find('input[data-type=cancelDetail_input_detail]').click();
				
			}else if(param.detailType == DetailPopup.DetailType.DISCOUNT){
				self.find('input[data-type=discountDetail_input_detail]').click();
				
			}else if(param.detailType == DetailPopup.DetailType.GIFT){
				self.find('input[data-type=giftDetail_input_detail]').click();
			}
			
			 
		}
	});
	
	this.open = function(afterOpen){
		_detailPopup.open(function(self){
			Util.LM.show();
			 $.ajax({
				 url : '../QueryDetail.do',
				 type : 'post',
				 data : {
					 queryType : 'TodayByTbl',
					 tableID : param.table.id,
					 orderID : param.order.id
				 },
				 async : false,
				 dataType : 'json',
				 success : function(jr, status, xhr){
					 Util.LM.hide();
					 if(jr.success){
						 _orderFoodDetail = jr.root;
						 _detailTotalPrice = jr.other.detailTotalPrice;
					 }else{
						 Util.msg.tip(jr.msg);
					 }
				 }
			 });
			 
			 if(param.detailType){
				 filterDetail(param.detailType, self);
			 }else{
				 filterDetail(DetailPopup.DetailType.ALL.val, self);
			 }
		
		});
	};
	
	this.close = function(afterOpen, timeout){
		_detailPopup.close();
	};
	
	
	//筛选对应的detail
	function filterDetail(type, self){
		if(type == DetailPopup.DetailType.ALL.val){
			self.find('input[data-type=allDetail_input_detail]').attr('checked', true);
			
		}else if(type == DetailPopup.DetailType.CANCEL.val){
			self.find('input[data-type=cancelDetail_input_detail]').attr('checked', true);
			
		}else if(type == DetailPopup.DetailType.DISCOUNT.val){
			self.find('input[data-type=discountDetail_input_detail]').attr('checked', true);
			
		}else if(type == DetailPopup.DetailType.GIFT.val){
			self.find('input[data-type=giftDetail_input_detail]').attr('checked', true);
			
		}else if(type == DetailPopup.DetailType.TRANS.val){
			self.find('input[data-type=transDetail_input_detail]').attr('checked', true);
			
		}
		self.find('input[name=lookupType]').checkboxradio('refresh');
		
		//筛选出结果
		var filter = [];
		for(var i = 0; i < _orderFoodDetail.length; i++){
			
			if(type == DetailPopup.DetailType.ALL.val){
				filter.push(_orderFoodDetail[i]);
			}else if(type == DetailPopup.DetailType.CANCEL.val && _orderFoodDetail[i].operationValue == 2){
				filter.push(_orderFoodDetail[i]);
				
			}else if(type == DetailPopup.DetailType.DISCOUNT.val && _orderFoodDetail[i].discount < 1){
				filter.push(_orderFoodDetail[i]);
				
			}else if(type == DetailPopup.DetailType.GIFT.val && _orderFoodDetail[i].isGift){
				filter.push(_orderFoodDetail[i]);
				
			}else if(type == DetailPopup.DetailType.TRANS.val && _orderFoodDetail[i].operationValue == 3){
				filter.push(_orderFoodDetail[i]);
			}
			
		}
		
		//账单详细
		var orderDetailTemplet = '<tr>'
			+ '<td>{dataIndex}</td>'
			+ '<td ><div style="height: 30px;overflow: hidden;">{name}</div></td>'
			+ '<td>{unitPrice}</td>'
			+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
			+ '<td><div style="height: 30px;overflow: hidden;">{tastePref}</div></td>'
			+ '<td>{tastePrice}</td>'
			+ '<td>{isGift}</td>'
			+ '<td>{discount}</td>'
			+ '<td>{kitchenName}</td>'
			+ '<td>{operation}</td>'
			+ '<td>{orderDateFormat}</td>'
			+ '<td>{waiter}</td>'
			+ '<td>{cancelReason}</td>'
			+ '</tr>';
		//遍历筛选出来的数据显示出来
		var filterFood = '';
		for(var j = 0, index = 1; j < filter.length; j ++){
			filterFood += orderDetailTemplet.format({
				dataIndex : index,
				id : filter[j].id,
				name : filter[j].foodName,
				count : filter[j].count,
				isWeight : (filter[j].status & 1 << 7) != 0 ? 'initial' : 'none',
				isGift : filter[j].isGift?'是':'否',	
				discount : filter[j].discount,
				tastePref : filter[j].tasteGroup.tastePref,
				tastePrice : filter[j].tasteGroup.tastePrice,
				unitPrice : (filter[j].unitPrice + filter[j].tasteGroup.tastePrice).toFixed(2),
				cancelReason : filter[j].cancelReason.reason ? filter[j].cancelReason.reason:'',
				totalPrice : filter[j].totalPrice.toFixed(2),
				orderDateFormat : filter[j].orderDateFormat.substring(11),
				kitchenName : filter[j].kitchen.name,
				operation : filter[j].operation,
				waiter : filter[j].waiter 
			});	
			index ++;
			
		}
		//将筛选出来的加上去
		self.find('[id=foodDetail_tbody_detail]').html(filterFood).trigger('create').trigger('refresh');
		
		//设置总价
		self.find('[id=totalPrice_label_detail]').text(_detailTotalPrice);
		//设置账单号
		self.find('[id=orderDetail_span_detail]').html('查看账单信息 -- 账单号:<font color="#f7c942">' + param.order.id + '</font> ');
		//设置餐桌号
		self.find('[id=tableDetail_span_detail]').html('餐桌号:<font color="#f7c942">' + param.table.alias + '</font>&nbsp;' + (param.table.name?'<font color="#f7c942" >(' + param.table.name +')</font>' :''));
	}
	
}

DetailPopup.DetailType = {
		ALL : {val : 1 , desc : '全部'},
		CANCEL : {val : 2, desc : '退菜'},
		DISCOUNT : {val : 3, desc : '折扣'},
		GIFT : {val : 4, desc : '赠送'},
		TRANS : {val : 5, desc : '转菜'}
};
define(function(require, exports, module){

	function limitSalePopup(param){
		
		param = param || {
			postClick : function(food){}         //点击菜品的回调函数
		}
		
		
		var _limitSale = null;
		//菜品分页
		var foodPaging = null;
		
		var thiz = this;
		
		_limitSale = new JqmPopupDiv({
			loadUrl : './popup/limitSale/limitSale.html',
			pageInit : function(self){
				
				initLimitFood();
				
				if(document.body.clientWidth < 1200){
					$(self).css('left', '11%');
				}else{
					$(self).css('left', '25%');
				}
				
				//取消按钮
				self.find('[id="close_a_limitSale"]').click(function(){
					thiz.close();				
				});
				
				//上一页
				self.find('[id="previousPage_a_limitSale"]').click(function(){
					foodPaging.prev();
				});
					
				//下一页
				self.find('[id="nextPage_a_limitSale"]').click(function(){
					foodPaging.next();
				});
				
				//一键重置
				self.find('[id="allResrt_a_limitSale"]').click(function(){
					Util.msg.alert({
						msg : '是否重置限量菜品?',
						renderTo : 'limitSale_div_limitSale',
						buttons : 'yesback',
						certainCallback : function(){
							Util.LM.show();
							$.post('../OperateSellOutFood.do', {dataSource : 'resetFoodLimit'}, function(rt){
								if(rt.success){
									thiz.reload();
									Util.msg.tip(rt.msg);
									Util.LM.hide();
								}else{
									Util.msg.alert({
										renderTo : 'stopSellMgr',
										msg : '重置失败'
									});
								}
							}).error(function() {
								Util.LM.hide();
								Util.msg.alert({
									msg : '操作失败, 请联系客服',
									renderTo : 'orderFoodListMgr'
								});		
							});
							
						},
						returnCallback : function(){
						}
					});
					
				});
			
			}
		});
		
		this.open = function(afterOpen){
	
			_limitSale.open();
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		}
		
		this.close = function(afterClose, timeout){

			_limitSale.close();
			
			if(afterClose && typeof afterClose == 'function'){
				if(timeout){
					setTimeout(afterClose, timeout);
				}else{
					afterClose();
				}
			}
		}
		
		this.reload = function(){
			initLimitFood();
		}
		
		function initLimitFood(){
			$.ajax({
				url : '../QueryMenu.do',
				data : {
					dataSource : 'limit'
				},
				dataType : 'json',
				type : 'post',
				success : function(jr){
					if(jr.success){
						
						//创建菜品分页的控件
						if(foodPaging == null){
							//菜品列表
							var foodCmpTemplate = '<a data-role="button" data-corners="false" data-inline="true" style="height:90px;" class="food-style" data-index={dataIndex} data-value={id}>' +
													'<div style="height: 75%;">{name}<br>￥{unitPrice}' +
														'<div class="food-status-font {commonStatus} style="height:25%;" >' +
															'<font color="orange">{weigh}</font>' +
															'<font color="blue">{currPrice}</font>' +
															'<font color="FireBrick">{sellout}</font>' +
															'<font color="green">{gift}</font>' +
														'</div>'+
														'<div class="food-status-limit {limitStatus}">' +
															'<font color="orange">限: {foodLimitAmount}</font><br>' +
															'<font color="green">剩: {foodLimitRemain}</font>' +
														'</div>'+								
													'</div>'+
												  '</a>';
							
							foodPaging = new WirelessOrder.Padding({
								renderTo : $('#foodList_div_limitSale'),
								displayTo : $('#foodPadding_div_limitSale'),
								itemLook : function(index, item){
									return foodCmpTemplate.format({
										dataIndex : index,
										id : item.id,
										name : item.name.substring(0, 10),
										unitPrice : item.hasFoodUnit() ? item.getFoodUnit()[0].price : item.unitPrice,
										sellout : item.isSellout() ? '停' : '',
										currPrice : item.isCurPrice() ? '时' : '',		
										gift : item.isAllowGift() ? '赠' : ''	,
										weigh : item.isWeight() ? '称' : '',
										commonStatus : item.isLimit() ? 'none' : '',
										limitStatus : item.isLimit() ? '' : 'none',
										foodLimitAmount : item.foodLimitAmount,
										foodLimitRemain : item.foodLimitRemain
									});
								},
								itemClick : function(index, item){
									if(param.postClick && typeof param.postClick == 'function'){
										param.postClick(item);
									}
								},
								onPageChanged : function(){
									setTimeout(function(){
										$(".food-status-font").css("position", "absolute");
									}, 250);	
								}
							});		
						}
						foodPaging.data(new WirelessOrder.FoodList(jr.root));
					}
				}
			})
		}
	}
	
	exports.newInstance = function(param){
		return new limitSalePopup(param);
	};
	
})
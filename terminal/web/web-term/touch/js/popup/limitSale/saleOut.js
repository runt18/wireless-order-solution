define(function(require, exports, module){
	
	function SaleOutPopup(){
	
		var _saleOutPopup = null;
		var thiz = this;
		var _data = null;
		var _self = null;
		var _foodpaging = null;
		
		_saleOutPopup = new JqmPopupDiv({
			loadUrl : './popup/limitSale/saleOut.html',
			pageInit : function(self){
				_self = self;
				
				//开售的点击
				self.find('[id="sale_a_saleOut"]').click(function(){
					self.find('a').attr('data-theme', 'b').removeClass('ui-btn-up-e').addClass('ui-btn-up-b');
					self.find('[id="sale_a_saleOut"]').attr('data-theme', 'e').addClass('ui-btn-up-e');
					$('#saleInput_input_saleOut').val('');
					initFood('onSale');
				});
				
				//估清的点击
				self.find('[id="saleOut_a_saleOut"]').click(function(){
					self.find('a').attr('data-theme', 'b').removeClass('ui-btn-up-e').addClass('ui-btn-up-b');
					self.find('[id="saleOut_a_saleOut"]').attr('data-theme', 'e').addClass('ui-btn-up-e');
					$('#saleInput_input_saleOut').val('');
					initFood('stop');
				})
				
				//上一页
				self.find('[id="previousPage_a_saleOut"]').click(function(){
					_foodpaging.prev();
				});
					
				//下一页
				self.find('[id="nextPage_a_saleOut"]').click(function(){
					_foodpaging.next();
				});
				
				//取消按钮
				self.find('[id="close_a_saleOut"]').click(function(){
					thiz.close();				
				});
				
				//删除按钮
				self.find('[id="delete_a_saleOut"]').click(function(){
					$('#saleInput_input_saleOut').val('');
					$('#saleInput_input_saleOut').trigger('input');
				});
				
				self.find('[id="sale_a_saleOut"]').click();
				
				self.find('[id="saleInput_input_saleOut"]').on("input", function(){
					search(self.find('[id="saleInput_input_saleOut"]').val());
				});
				
			}
		});
		
		this.open = function(afterOpen){
	
			_saleOutPopup.open(function(){
				HandWritingAttacher.instance().attach($('#saleInput_input_saleOut')[0]);
			});
			
			
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		}
		
		this.close = function(afterClose, timeout){
			HandWritingAttacher.instance().detach($('#saleInput_input_saleOut')[0]);
			_saleOutPopup.close();
			
			if(afterClose && typeof afterClose == 'function'){
				if(timeout){
					setTimeout(afterClose, timeout);
				}else{
					afterClose();
				}
			}
		}
		
		this.reload = function(){
			_self.find('[id="sale_a_saleOut"]').click();
		}
		
		function initFood(dataSource){
			$.ajax({
				url : '../QueryMenu.do',
				type : 'post',
				dataType : 'json',
				data : {
					dataSource : dataSource
				},
				success : function(data){
					if(data.success){
						_data = new WirelessOrder.FoodList(data.root);
						
						search();
					}else{
						Util.msg.tip('加载失败,请联系客服');
					}
				
				},
				error : function(){
					Util.msg.tip('加载失败s,请联系客服');
				}
			});
		}
		
		function search(value){
			var data = null;
			if(value){
				data = _data.getByName(value);
			}else{
				data = _data;
			}
			
			//菜品列表
			var foodCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="food-style" data-index={dataIndex} data-value={id}>' +
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
			
			 _foodpaging = new WirelessOrder.Padding({
				renderTo : $('#foodList_div_saleOut'),
				displayTo : $('#foodPadding_div_saleOut'),
				itemLook : function(index, item){
					return foodCmpTemplet.format({
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
					var dataSource = null;
					var msg = null;
					if(item.isSellout()){
						dataSource = 'deSellOut';
						msg = '是否开售【'+ item.name +'】菜品?';
					}else{
						dataSource = 'sellOut';
						msg = '是否沽清【'+ item.name +'】菜品?';
					}
					
					Util.msg.alert({
						msg : msg,
						renderTo : 'saleOut_div_saleOut',
						buttons : 'yesback',
						certainCallback : function(){
							
							Util.LM.show();
							$.ajax({
								url : '../OperateSellOutFood.do',
								type : 'post',
								data : {
									dataSource : dataSource,
									foodIds : item.id
								},
								success : function(data, status, xhr) {
									Util.LM.hide();
									Util.msg.tip(data.msg);
									thiz.reload();
								},
								error : function(request, status, err) {
									Util.LM.hide();
									Util.msg.tip('数据操作失误, 请刷新页面后重试');
								}
							});
						},
						returnCallback : function(){
						}
					});
					
				},
				onPageChanged : function(){
					setTimeout(function(){
						$(".food-status-font").css("position", "absolute");
					}, 250);	
				}
			});		
		
			_foodpaging.data(data);
		}
		
		
	
	}
	
	exports.newInstance = function(param){
		return new SaleOutPopup();
	};
	
});
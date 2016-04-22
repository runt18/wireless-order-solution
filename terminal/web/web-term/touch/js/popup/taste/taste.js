define(function(require, exports, module){

	function TastePopup(param){
	
		param = param || {
			selectedFood : null,      								//点中的菜品的信息,用来加载套菜和口味
			postTasteChanged : function(taste, selectedFood){}, 
			postUnitClick : function(selectedFood, taste){}			//点击单位的回调
		}
		
		var _tastePopup = null;
		var _selectedFood = null;
		var _self = null;
		
		_tastePopup = new JqmPopupDiv({
			loadUrl : './popup/taste/taste.html',
			isShadow : 'no',
			pageInit : function(self){
				
				self.find('[id="close_a_taste"]').click(function(){
					_tastePopup.close();				
				});
				
				_self = self;
				//选中的菜品
				_selectedFood = param.selectedFood;
				
			}
		});
		
		this.open = function(afterOpen){
			_tastePopup.open(function(){
				initFood();
			});
			
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		};
		
		this.close = function(afterClose, timeout){
			_tastePopup.close();
			
			if(afterClose &&　typeof afterClose == 'function'){
				if(timeout){
					setTimeout(afterOpen, timeout);
				}else{
					afterClose();
				}
			}
		};
		
		   
		
		
		//加载菜品信息
		function initFood(){
			_selectedFood.popTastes.forEach(function(popTasteId, index){
			})
			$.post('../QueryFoodTaste.do', {foodID : _selectedFood.id}, function(jr){
				if(jr.success){
					var commonTastes = jr.root;
					var multiPrice = WirelessOrder.foods.getById(_selectedFood.id).multiUnitPrice;
					
					var foodGroups = [];
					
					foodGroups.push({
						id : _selectedFood.id,
						name : _selectedFood.name,
						isComboFood : false,
						src : _selectedFood
					});
					
					if(_selectedFood.isCombo()){
						for(var i = 0; i < _selectedFood.combo.length; i++){
							_selectedFood.combo[i].comboFood.isComboFood = true;
							foodGroups.push({
								id : _selectedFood.combo[i].comboFood.id,
								name : _selectedFood.combo[i].comboFood.name,
								isComboFood : true,
								src : _selectedFood.combo[i]
							});
						}
					}
					
					initFoodGroupCmp(foodGroups, commonTastes, multiPrice);
					
					if($("#orderPinyinCmp").is(":hidden") && $("#orderHandCmp").is(":hidden")){
						_self.css({top : 'initial', bottom : '90px'});
					}else{
						_self.css({top : 'initial', bottom : '48.5%'});
					}
					
				}
			});
		}
		
		function initFoodGroupCmp(foodGroups, tastes, multiPrice, start){
			//套菜组
			var comboFoodGroupCmpTemplet = '<a data-role="button" data-inline="true" class="comboFoodPopTopBtn" data-value={id} data-index={index} ' +
										'data-theme="{theme}">' +
										'<div>{name}</div>' +
										'</a>';
			var start;
			
			if(start){
				start = start;
			}else{
				var start = 0;
			}
			
			var foodGroupsLimit =  foodGroups.length > 6 ? 5 : 6;
			var limit = foodGroups.length >= start + foodGroupsLimit ? foodGroupsLimit :  foodGroupsLimit - (start + foodGroupsLimit - foodGroups.length);
			
			var html = [];
			if(foodGroups.length > 0){
				for(var i = 0; i < limit; i++){
					var theme = "b";
					if(!foodGroups[start + i].isComboFood){
						theme = "e";
					}
				
					html.push(comboFoodGroupCmpTemplet.format({
						index : i,
						id : foodGroups[start + i].id,
						name : foodGroups[start + i].name,
						isComboFood : foodGroups[start + i].isComboFood,
						theme : theme
					}));
				}
			}
			
			if(foodGroups.length > 5){
				html.push('<a id="previousPage_a_taste" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="tasteGroupPage">L</a>' +
				'<a id="nextPage_a_taste" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="tasteGroupPage">R</a>');
			}
			
			
			_self.find('[id="tasteName_div_taste"]').html(html.join("")).trigger('create');	
			
				//上一页
			_self.find('[id="previousPage_a_taste"]').click(function(){
				start -= foodGroupsLimit;
				if(start < 0){
					start += foodGroupsLimit;
					return;
				}
				initFoodGroupCmp(foodGroups, tastes, multiPrice, start);
			});
			
			//下一页
			_self.find('[id="nextPage_a_taste"]').click(function(){
				start += foodGroupsLimit;
				if(start > foodGroups.length){
					start -= foodGroupsLimit;
					return;
				}
				
				initFoodGroupCmp(foodGroups, tastes, multiPrice, start);
			});
			
			//点击口味组
			_self.find('[id="tasteName_div_taste"] .comboFoodPopTopBtn').each(function(index, element){
				element.onclick = function(){
					if($(element).attr('data-theme') == "e"){
						$(element).attr('data-theme', 'e').addClass('ui-btn-up-e');
					}else{
						_self.find('[id="tasteName_div_taste"] .comboFoodPopTopBtn').attr('data-theme', 'b').removeClass('ui-btn-up-e').addClass('ui-btn-up-b');
						$(element).attr('data-theme', 'e').addClass('ui-btn-up-e');
					}
						
					var chooseFood = null;
					
					for(var i = 0; i < foodGroups.length; i++){
						if(foodGroups[i].id === parseInt($(element).attr('data-value'))){
							chooseFood = foodGroups[i];
						}
					}
					
					initCommonTaste(chooseFood);
					
					initFoodUnit(chooseFood);
					
					
				}
			});
			
			_self.find('[id="tasteName_div_taste"] .comboFoodPopTopBtn')[0].click();
		
		}
			
		function initCommonTaste(chooseFood){
			$.post('../QueryFoodTaste.do',  {foodID : chooseFood.id}, function(jr){
				if(jr.success){
					var tastes = jr.root;
					
					_self.find('[id="normalTaste_div_taste"]').html('');
					var html = [];
					var tasteCmpTemplet = '<a data-type="normalTaste" data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id} data-theme={theme}><div>{name}<br>{price}</div></a>';
					if(tastes.length > 0){
						
						for(var i = 0; i < tastes.length; i++){
							var theme = "c";
							if(chooseFood.src.hasTasteGroup()){
								if(chooseFood.src.tasteGroup.hasNormalTaste()){
									for(var j = 0; j < chooseFood.src.tasteGroup.normalTasteContent.length; j++){
										if(tastes[i].taste.id === chooseFood.src.tasteGroup.normalTasteContent[j].id){
											theme = "e"
										}
									}
								}
							}
								
							html.push(tasteCmpTemplet.format({
								index : i,
								id : tastes[i].taste.id,
								name : tastes[i].taste.name,
								price : tastes[i].taste.calcValue == 1 ? (tastes[i].taste.rate * 100) + '%' : ('￥'+ tastes[i].taste.price),
								theme : theme
							}));
						}
						
						html.push('<a data-role="button" data-corners="false" data-inline="true" class="moreTasteCmp" data-type="moreTaste" data-theme="b">更多口味</a>' +
						'<a data-role="button" data-corners="false" data-inline="true" data-type="handWritingTaste" class="moreTasteCmp" data-theme="b">手写口味</a>');
						
						
					}else{
						html.push('<a data-role="button" data-corners="false" data-inline="true" data-type="moreTaste" class="moreTasteCmp" data-theme="b">更多口味</a>' +
						'<a data-role="button" data-corners="false" data-inline="true" data-type="handWritingTaste" class="moreTasteCmp" data-theme="b">手写口味</a>');
					}
					
					_self.find('[id="normalTaste_div_taste"]').html(html.join("")).trigger('create');	
					_self.find('[id="normalTasteCollapsible_div_taste"]').trigger("expand");
					
					//点击更多口味
					_self.find('[data-type="moreTaste"]').click(function(){
						
						_tastePopup.close(function(){
							var more = require('../moreTaste/moreTaste');
							
							var moreTaste = null;
							moreTaste = more.newInstance({
								selectedFood : chooseFood.src,
								postTasteClick : function(taste, chooseFood){
									chooseFood.addTaste(taste);
									param.postTasteChanged(taste, chooseFood);
								},
								postTasteCancel : function(taste, chooseFood){
									chooseFood.removeTaste(taste);
									param.postTasteChanged(taste, chooseFood);
								}
							});
							
							moreTaste.open();
						});
						
					});
					
					//点击手写口味
					_self.find('[data-type="handWritingTaste"]').click(function(){
						var temp = require('../tempTaste/tempTaste');
						
						var tempTaste = null;
						tempTaste = temp.newInstance({
							selectedFood : chooseFood.src,
							confirm : function(chooseFood, name, price){
								chooseFood.setTempTaste(name, price);
								param.postTasteChanged();
							}
						});
						tempTaste.open();
						
					});
					
					//点击常用口味
					_self.find('[data-type="normalTaste"]').each(function(index, element){
						element.onclick = function(){
							if($(element).attr('data-theme') == 'e'){
								$(element).attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
								
								for(var i = 0; i < tastes.length; i++){
									if(parseInt($(element).attr('data-value')) === tastes[i].taste.id){
										chooseFood.src.removeTaste(tastes[i].taste);
										param.postTasteChanged(tastes[i].taste, chooseFood.src);
									}
								}
							}else{
								$(element).attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
								
								for(var i = 0; i < tastes.length; i++){
									if(parseInt($(element).attr('data-value')) === tastes[i].taste.id){
										chooseFood.src.addTaste(tastes[i].taste);
										param.postTasteChanged(tastes[i].taste, chooseFood.src);
									}
								}
							}
							$(element).buttonMarkup( "refresh" );
						}       
					});
				}else{
					Util.msg.tip(jr.msg);
				}
			});	
			
		}
		
		
		
		function initFoodUnit(chooseFood){
			var multiPrice = WirelessOrder.foods.getById(chooseFood.id).multiUnitPrice;		 
			var selectedFoodId = null;
			var selectedFood = null;
			//多单位
			var multiPriceCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="multiPriceCmp" data-index={index} data-value={id} data-theme={theme}><div>{multiPrice}</div></a>';
			
			var hasMultiPrice = null;
			if(multiPrice.length > 0){
				var html = [];
				var chooseUnit;
				var hasLight = false;
				for(var i = 0; i < multiPrice.length; i++){
					if(chooseFood.src.hasFoodUnit() && !hasLight){
						if(chooseFood.src.foodUnit.id == multiPrice[i].id){
							hasLight = true;
							chooseUnit = multiPrice[i];
						}
						
					}else{
						if(!hasLight){
							chooseUnit = multiPrice[0];
						}					
					}
										
					
					
					html.push(multiPriceCmpTemplet.format({
						index : i,
						id : multiPrice[i].id,
						multiPrice : '¥' + multiPrice[i].price + " / " + multiPrice[i].unit,
						theme : multiPrice[i].id == (chooseUnit ? chooseUnit.id : chooseUnit) ? "e" : "c"
					}));
					
				}
				
				param.postUnitClick(chooseFood.src);
				_self.find('[id="foodUnit_div_taste"]').html(html.join("")).trigger('create');	
				_self.find('[id="foodUnitCollapsible_div_taste"]').show();
				_self.find('[id="foodUnitCollapsible_div_taste"]').trigger('expand');
				
				//foodunit的点击事件
				_self.find('[id="foodUnit_div_taste"] a').each(function(index, element){
					element.onclick = function(){
						if($(element).attr('data-theme') == "e"){
							$(element).attr('data-theme', 'e').addClass('ui-btn-up-e');
						}else{
							_self.find('[id="foodUnit_div_taste"] a').attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
							$(element).attr('data-theme', 'e').addClass('ui-btn-up-e');
							for(var i = 0; i < multiPrice.length; i++){
								if(multiPrice[i].id == $(element).attr('data-value')){
									chooseFood.src.setFoodUnit(multiPrice[i]);
									param.postUnitClick(chooseFood.src);
								}
							}	
						}
					}
				});
				
				
				
			}else{
				//没有单位
				_self.find('[id="foodUnit_div_taste"]').html('');	
				_self.find('[id="foodUnitCollapsible_div_taste"]').hide();
			}
			
		}
		
		
//			
	}
	
	
	exports.newInstance = function(param){
		return new TastePopup(param);	
	}
});
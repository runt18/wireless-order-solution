define(function(require, exports, module){
	function MoreTastePopup(param){
		param = param || {
			selectedFood : selectedFood,               				   //选中的菜品	
			postTasteClick : function(taste, selectedFood){},          //点击口味的回调事件
			postTasteCancel : function(taste, selectedFood){}          //取消点击口味的回调事件
		};
	
		var _moreTastePopup = null;
		var _self  = null;
		var thiz = this;
		var _tastePaging = null;
		var _allTastes = [];
		_moreTastePopup = new JqmPopupDiv({
			loadUrl : './popup/moreTaste/moreTaste.html',
			pageInit : function(self){
				
				_self = self;
				
				if(document.body.clientWidth < 1200){
					$(_self).css('left', '11%');
				}else{
					$(_self).css('left', '25%');
				}
				
				//取消按钮
				self.find('[id="close_a_moreTaste"]').click(function(){
					thiz.close();				
				});
				
				//上一页
				self.find('[id="previousPage_a_moreTaste"]').click(function(){
					_tastePaging.prev();
				});
					
				//下一页
				self.find('[id="nextPage_a_moreTaste"]').click(function(){
					_tastePaging.next();
				});
				
				initTastesGroups();
				
			}
			
		});
		
		this.open = function(afterOpen){
			_moreTastePopup.open();
		};
	
		this.close = function(afterClose, timeout){
			_moreTastePopup.close();
		};
		
		function initTastesGroups(){
			var tastesGroups = null;
			var data = [];
			if(Wireless.Tastes.length > 0){
				data.push({
					id : Wireless.Tastes[0].cateValue,
					name : Wireless.Tastes[0].cateText,
					items : []
				});
			}
			
			var has = true, temp = {};
			for(var i = 0; i < Wireless.Tastes.length; i++){
				
				_allTastes.push(Wireless.Tastes[i]);
				
				has = false;
				for(var k = 0; k < data.length; k++){
					if(Wireless.Tastes[i].cateValue == data[k].id){
						data[k].items.push(Wireless.Tastes[i]);
						has = true;
						break;
					}
				}
				if(!has){
					temp = {
						id : Wireless.Tastes[i].cateValue,
						name : Wireless.Tastes[i].cateText,
						items : []
					};
					temp.items.push(Wireless.Tastes[i]);
					data.push(temp);
				}
			}	
			
			tastesGroups = data;
			
			ininTasteMenu(tastesGroups);
		}
		
		//初始化口味组
		function ininTasteMenu(tastesGroups, startIndex){
			var start;
			
			if(startIndex){
				start = startIndex;
			}else{
				start = 0;
			}
			var pageLimit = tastesGroups.length > 7 ? 6 : 7;
			//口味组
			var tasteGroupCmpTemplet = '<a data-role="button" data-inline="true" class="tastePopTopBtn" data-value={id} data-index={index} data-theme="{theme}" data-type="tastesGroupsCmp">{name}</a>';
			var limit = tastesGroups.length >= start + pageLimit ? pageLimit : pageLimit - (start + pageLimit - tastesGroups.length);
			
			var html = [];
			
			if(tastesGroups.length > 0){
				for(var i = 0; i < limit; i++){
					html.push(tasteGroupCmpTemplet.format({
						index : i,
						id : tastesGroups[start + i].id,
						name : tastesGroups[start + i].name,
						theme : start + i == 0 ? "e" : "b"
					}));
				}
			}
			
			if(tastesGroups.length > 7){
				html.push('<a data-type="prePage_a_moreTaste" data-role="button" data-icon="arrow-l" data-iconpos="notext" data-inline="true" class="tasteGroupPage">L</a>' +
				'<a data-type="next_a_moreTaste" data-role="button" data-icon="arrow-r" data-iconpos="notext" data-inline="true" class="tasteGroupPage">R</a>');
			}
			
			_self.find('[id="tasteGroupCmp_div_moreTaste"]').html(html.join('')).trigger('create');
			
		
			
			//口味组的点击事件
			_self.find('[data-type="tastesGroupsCmp"]').each(function(index, element){
				element.onclick = function(){
						
					if($(element).attr('data-theme') == "e"){
						$(element).attr('data-theme', 'e').addClass('ui-btn-up-e');
					}else{
						_self.find('[data-type="tastesGroupsCmp"]').attr('data-theme', 'b').removeClass('ui-btn-up-e').addClass('ui-btn-up-b');
						$(element).attr('data-theme', 'e').addClass('ui-btn-up-e');
					}
					
					
					var chooseTaste = null;
					
					for(var i = 0; i < tastesGroups.length; i++){
						if(tastesGroups[i].id === parseInt($(element).attr('data-value'))){
							chooseTaste = tastesGroups[i].items;
						}
					}
						
					//口味列表
					var tasteCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="tasteCmp" data-index={index} data-value={id} data-theme={theme}><div>{name}<br>{price}</div></a>';
					_tastePaging = new WirelessOrder.Padding({
						data : chooseTaste,
						renderTo : _self.find('[id="tastesCmp_div_moreTaste"]'), 
						displayTo : _self.find('[id="tastePagingDesc_div_moreTaste"]'),
						itemLook : function(index, item){
							var theme = "c";
							
							if(param.selectedFood){
								if(param.selectedFood.hasTasteGroup()){
									if(param.selectedFood.tasteGroup.hasNormalTaste()){
										for(var i = 0; i < param.selectedFood.tasteGroup.normalTasteContent.length; i++){
											if(item.id == param.selectedFood.tasteGroup.normalTasteContent[i].id){
												theme = "e";
												break;
											}
										}								
									}
								}
							}
							
							return tasteCmpTemplet.format({
								index : index,
								id : item.id,
								name : item.name,
								price :  item.calcValue == 1 ? ( item.rate * 100) + '%' : ('￥'+  item.price),
								theme : theme
							});
						},
						itemClick : function(pageNo, item, element){
							//每个口味的点击事件
							if($(element).attr('data-theme') == 'e'){
								$(element).attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
								
								if(param.postTasteCancel && typeof param.postTasteClick == 'function'){
									if(param.selectedFood){
										param.postTasteCancel(item, param.selectedFood);	
									}else{
										param.postTasteCancel(item, null);	
									}
								}
								
							}else{
								$(element).attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
								
								if(param.postTasteClick && typeof param.postTasteClick == 'function'){
									if(param.selectedFood){
										param.postTasteClick(item, param.selectedFood);	
									}else{
										param.postTasteClick(item, null);	
									}
								}
							}
							$(element).buttonMarkup( "refresh" );
						}
					});
				};
			});
			
			_self.find('[data-type="tastesGroupsCmp"]')[0].click();
			
			
			//下一页
			_self.find('[data-type="next_a_moreTaste"]').click(function(){
				start += pageLimit;
				if(start > tastesGroups.length){
					start -= pageLimit;
					return;
				}
				ininTasteMenu(tastesGroups, start);
			});
			
			//上一页
			_self.find('[data-type="prePage_a_moreTaste"]').click(function(){
				start -= pageLimit;
				if(start < 0){
					start += pageLimit;
					return;
				}
				ininTasteMenu(tastesGroups, start);
			});
		}
		
	
	}
	
	exports.newInstance = function(param){
		return new MoreTastePopup(param);
	};
});
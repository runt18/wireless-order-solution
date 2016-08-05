(function(global){
	
	if(typeof global.CreateHead === 'undefined'){
		global.CreateTabPanel = function(tabs){
			tabs = tabs || tabs[{
				tab : null,
				initTab : 0,                     //默认点击的tab的下标
				onActive : function(){},            //tab点击的时候的回调函数
				onDeactive : function(){}			//tab切换的时候的回调函数
			}];
		
			var _tabContainer = null;
			
			var _activeTab = null;      //当前活动的tab
			
			this.open = function(afterOpen){
				createContainer();
				
				if(afterOpen && typeof afterOpen == 'function'){
					afterOpen();
				}
			}
			
			
			this.close = function(afterClose, timeout){
				if(_container){
					_container[0].remove();
					_container = null;
				}
				
				if(afterClose && typeof afterClose == 'function'){
					if(timeout){
						setTimeout(afterClose, timeout)
					}else{
						afterClose();
					}
				}
			}
		
			function createContainer(){
				_tabContainer = $('<div/>'); 
				
				var headContainer = $('<div/>')
				headContainer.addClass('tabs');
				headContainer.css({
				
				});
				
				var headWidth = (document.documentElement.clientWidth / tabs.length) - 0.2;
				
				var head = ' <a data-type="tab" href="#" style="width:'+ headWidth +'px;" data-value={index} hidefocus="true">{name}</a>';
				
				
				var height = document.documentElement.clientHeight - 40;
				var width = document.documentElement.clientWidth;
				var container = '<div id="xxxxx" style="overflow:auto;height:'+ height +'px; width:'+ width +'px;"></div>'
				
				
				var tabPart = [];
				if(tabs.length > 0){
					for(var i = 0; i < tabs.length; i++){
						tabPart.push(head.format({
							index : i,
							name : tabs[i].tab
						}));
					}
				}
				
				for(var i = 0; i < tabPart.length; i++){
					headContainer.append(tabPart[i]);
				}
				
				_tabContainer.append(headContainer).append(container);
				$('body').append(_tabContainer);
				
				
				//tab绑定click事件
				$('body').find('[data-type="tab"]').each(function(index, element){
					element.onclick = function(){
						if($(element).hasClass('active solid')){
							$(element).addClass('active solid');
						}else{
							$('body').find('[data-type="tab"]').removeClass('active solid');
							$(element).addClass('active solid');	
						}
						
						//清空
						$(container).html('');
						
						if(_activeTab){
							//先deActive
							_activeTab.onDeactive($(container));
						}
						
						_activeTab = tabs[$(element).attr('data-value')];
						
						_activeTab.onActive($(container));
						
					}
				});
				
				
				//默认第一个tab选中状态
				$('body').find('[data-type="tab"]')[0].click();
			}
		}
	}
	
})(this);



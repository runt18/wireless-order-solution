
WirelessOrder.Padding = function(c){
	
	c = c || {
		data : [],									//需要分页的数据							
		renderTo : null,							//分页渲染的目标div
		displayTo : null,							//显示分页信息的div
		itemLook : function(index, item){},			//分页中每个item的显示样式
		itemClick : function(index, item, element){},		//分页中每个item的click函数
		onPageChanged : function(pageNo, items){},	//页数变化时的回调函数
		limit : null                                //限制个数
	};
	
	var _self = this;
	var _data = null;
	var _length = 0;
	var _start = 0;
	var _limit = 0;
	
	function init(data){
		
		_data = data || [];
		_length = _data.length;
		
		 //第一次加载不成功,延迟来一直加载达到菜品显示出来为止
		(function(){
			//console.log($('#foodsCmp')[0].clientHeight);
			if(c.renderTo[0].clientHeight != 0){
				var ch = c.renderTo[0].clientHeight;
				var cw = c.renderTo[0].clientWidth;
				
				if(c.limit){
					_limit = c.limit;
				}else{
					//根据屏幕大小计算显示的个数
					_limit = parseInt(ch / (65 + 18)) * parseInt(cw / (100 + 12));
				}
				
				//显示第一页
				_self.first();
			}else{
				setTimeout(arguments.callee, 500);
			}
		})();
		
	};
	
	function changePage(){
		if(_length == 0){
			c.renderTo.html('').trigger('create');
			
		}else if(_length > 0){
			var html = [];
			var limit = _length >= _start + _limit ? _limit : _limit - (_start + _limit - _length);
			
			var itemsToThisPage = [];
			for(var i = 0; i < limit; i++){
				itemsToThisPage.push(_data[_start + i]);
				html.push(c.itemLook(i, _data[_start + i]));
			}
			
			c.renderTo.html(html.join('')).trigger('create');
			
			if(c.itemClick && typeof c.itemClick == 'function'){
				c.renderTo.children().each(function(index, element){
					element.onclick = function(){
						var index = parseInt($(element).attr('data-index'));
						c.itemClick(index, itemsToThisPage[index], element);
					};
				});
			}
			
			//显示导航信息
			if(c.displayTo){
				var displayMsg = '共{0}项, 第{1}/{2}页, 每页{3}项';
				c.displayTo.html(displayMsg.format(
					_length,
					parseInt(_start / _limit) + 1,
					parseInt(_length / _limit) + (_length % _limit == 0 ? 0 : 1),
					_limit
				));
			}
			
			if(c.onPageChanged && typeof c.onPageChanged == 'function'){
				c.onPageChanged(parseInt(_start / _limit), itemsToThisPage);
			}
			
		}
	}
	
	//更新data
	this.data = function(data){
		init(data);
	};
	
	//第一页
	this.first = function(){
		_start = 0;
		changePage();
	};
	
	//最后一页
	this.last = function(){
		_start = _length - _length % _limit;
		changePage();
	};
	
	//下一页
	this.next = function(c){
		if(_start + _limit <= (_length - 1)){
			_start += _limit;
			changePage();
		}
	};
	
	//上一页
	this.prev = function(){
		if(_start - _limit >= 0){
			_start -= _limit;
			changePage();
		}
	};
	
	init(c.data);
};



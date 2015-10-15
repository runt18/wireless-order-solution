/**
 * 
 */
 
function JqmPopup(o){
	
	var _popupId = null;
	var _o = o || {
		loadUrl : null,		//加载内容的URL
		content : null,		//直接的加载内容
		pageInit : null		//内容加载后的callback
	};
	
	function _init(body){
		if(body){
			
			var root = $(body).find('div').first().parent();
			if(!root.attr('id')){
				root.attr('id', 'popup-' + new Date().getTime());
			}
			
			//把消息框加入指定page底部
			$.mobile.activePage.append(root);
			
			_popupId = root.attr('id');
			//动态创建组件
			$('#' + _popupId).trigger("create");
			//声明为popup
			$('#' + _popupId).popup();
			//添加弹出样式
			$('#' + _popupId).parent().addClass("pop").addClass("in");
			
			if(_o.pageInit){
				_o.pageInit($('#' + _popupId));
			}
		}
	}
	
	function _open(pageOpen){
		//添加open callback
		if(pageOpen){
			$('#' + _popupId).on('popupafteropen', pageOpen);
		}
		//弹出组件
		$('#' + _popupId).popup('open');
	}
	
	this.open = function(pageOpen){
		if(_popupId){
			_open(pageOpen);

		}else{
			if(_o.loadUrl){
				$('<div/>').load(_o.loadUrl, function(response, status, xhr){
					_init(response);
					_open(pageOpen);
				});
				
			}else if(o.content){
				_init(o.content);
				_open(pageOpen);
			}
			
		}

	};
	
	this.close = function(pageClose){
		//添加close callback
		if(pageClose){
			$('#' + _popupId).on('popupafterclose', pageClose);
		}
		//弹出组件
		$('#' + _popupId).popup('close');
		//移除Popup
		$('#' + _popupId).parent().remove();
		$('#' + _popupId).remove();
		_popupId = null;
	};
}



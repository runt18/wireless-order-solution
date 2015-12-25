(function(){
	
	JqmPopupDiv = function(o){
		var _popupDivId = null;
		var _o = o || {
			loadUrl : null,		//加载内容的URL
			content : null,		//直接的加载内容
			pageInit : null		//内容加载后的callback
		}
		
		function _init(body){
			if(body){
				
				var root = $(body).find('div').first().parent();
				if(!root.attr('id')){
					root.attr('id', 'popupDiv-' + new Date().getTime());
				}
				_popupDivId = root.attr('id');
				
				//把消息框加入指定page底部
				$.mobile.activePage.append(root);
				//刷新div
				root.trigger('create').trigger('refresh');
				
				if(_o.pageInit){
					_o.pageInit($('#' + _popupDivId));
				}
				
			}
		}
		
		this.open = function(afterOpen){
			$('<div/>').load(_o.loadUrl, function(response, status, xhr){
				if(xhr.status == '200'){
					_init(response);
					if(afterOpen && typeof afterOpen == 'function'){
						afterOpen($('#' + _popupDivId));
					}
				}else{
					alert('无法打开页面\r\n' + 'url : ' + _o.loadUrl + ' ,status : ' + xhr.status + ' ,statusText : ' + xhr.statusText);
				}
			});
		}
		
		this.close = function(afterClose){
			//移除Popup
			$('#' + _popupDivId).remove();
			if(afterClose && typeof afterClose == 'function'){
				afterClose($('#' + popupDivId));
			}
			_popupDivId = null;
		}
	}
})();
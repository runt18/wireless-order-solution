(function(){
	var _loadedUrlCache = {};
	
	JqmPopupDiv = function(o){
		var _popupDivId = null;
		var _shadowId = null;
		var _o = o || {
			loadUrl : null,		//加载内容的URL
			content : null,		//直接的加载内容
			pageInit : null, 	//内容加载后的callback
			isShadow : null	
		}
		
		function _init(body){
			if(body){
				
				var root = $(body).find('div').first().parent();
				if(!root.attr('id')){
					root.attr('id', 'popupDiv-' + new Date().getTime());
				}
				_popupDivId = root.attr('id');
				
				
				if(_o.isShadow != 'no' && typeof _o.isShadow == 'undefined'){
					var shadow = $('<div/>')
					shadow.attr('id', 'divShadow-' + new Date().getTime())
					_shadowId = shadow.attr('id');
					shadow.css({
						'z-index' : '1101',
						'width' : '100%',
						'height' : '100%',
						'opacity' : '0',
						'position' : 'absolute',
						'top'  : '0',
						'left' : '0',
						'background' : '#DDD'
					});
					
					$.mobile.activePage.append(root).append(shadow);//刷新div
					shadow.trigger('create').trigger('refresh');
					
				}else{
					$.mobile.activePage.append(root);
				}
				root.trigger('create').trigger('refresh');
				
				
				
				if(_o.pageInit){
					_o.pageInit($('#' + _popupDivId));
				}
				
			}
		}
		
		function _open(afterOpen){
			//添加open callback
			if(afterOpen){
				if(afterOpen && typeof afterOpen == 'function'){
					afterOpen($('#' + _popupDivId));
				}
			}
		}
		
		
		this.open = function(afterOpen){
			if(_o.loadUrl){
				if(_loadedUrlCache[_o.loadUrl]){
					_init(_loadedUrlCache[_o.loadUrl]);
					_open(afterOpen);
				}else{
					$('<div/>').load(_o.loadUrl, function(response, status, xhr){
						_loadedUrlCache[_o.loadUrl] = response;
						if(xhr.status == '200'){
							_init(response);
							_open(afterOpen);
						}else{
							alert('无法打开页面\r\n' + 'url : ' + _o.loadUrl + ' ,status : ' + xhr.status + ' ,statusText : ' + xhr.statusText);
						}
					});
				}
				
			}else if(o.content){
				_init(o.content);
			}
		}
		
		this.close = function(afterClose){
			//移除Popup
			$('#' + _popupDivId).remove();
			$('#' + _shadowId).remove();
			if(afterClose && typeof afterClose == 'function'){
				afterClose($('#' + _popupDivId));
			}
			_popupDivId = null;
		}
	}
})();
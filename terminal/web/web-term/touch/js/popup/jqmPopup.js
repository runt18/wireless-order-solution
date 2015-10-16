/**
 * 
 */
(function(){
	
	var _loadedUrlCahe = {};
	
	JqmPopup = function(o){
		
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
				_popupId = root.attr('id');
				
				//把消息框加入指定page底部
				$.mobile.activePage.append(root);

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
				$('#' + _popupId).on('popupafteropen', function(){
					pageOpen($('#' + _popupId));
				});
			}
			//弹出组件
			$('#' + _popupId).popup('open');
		}
		
		this.open = function(pageOpen){
			if(_popupId){
				_open(pageOpen);

			}else{
				if(_o.loadUrl){
					if(_loadedUrlCahe[_o.loadUrl]){
						_init(_loadedUrlCahe[_o.loadUrl]);
						_open(pageOpen);
					}else{
						$('<div/>').load(_o.loadUrl, function(response, status, xhr){
							_loadedUrlCahe[_o.loadUrl] = response;
							if(xhr.status == '200'){
								_init(response);
								_open(pageOpen);
							}else{
								alert('无法打开页面\r\n' + 'url : ' + _o.loadUrl + ' ,status : ' + xhr.status + ' ,statusText : ' + xhr.statusText);
							}
						});
					}
					
				}else if(o.content){
					_init(o.content);
					_open(pageOpen);
				}
				
			}

		};
		
		this.close = function(pageClose){
			//添加close callback
			if(pageClose){
				$('#' + _popupId).on('popupafterclose', function(){
					pageClose($('#' + _popupId));
				});
			}
			//弹出组件
			$('#' + _popupId).popup('close');
			//移除Popup
			$('#' + _popupId).parent().remove();
			$('#' + _popupId).remove();
			_popupId = null;
		};
	}	
})();




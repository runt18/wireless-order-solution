var wxLoadDialog = (function(){

	function Singleton(){
	
		var _container = null;
		
		this.show = function(){
			if(!_container){
				_container = '<div id="loadingToast" class="weui_loading_toast" style="z-index: 6000;">'
			    +'<div class="weui_mask_transparent"></div>'
			    +' <div class="weui_toast">'
			    +'<div class="weui_loading">'  
			    +'<div class="weui_loading_leaf weui_loading_leaf_0"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_1"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_2"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_3"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_4"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_5"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_6"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_7"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_8"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_9"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_10"></div>'
	            +'<div class="weui_loading_leaf weui_loading_leaf_11"></div>'
			    +'</div>' 
			       + '<p class="weui_toast_content">数据加载中</p>'  
			  +'</div>';
			  
			 $('body').append($(_container));
			}
		}
		
		this.hide = function(){
			if(_container){
				$('body').find('[id="loadingToast"]').remove();	
				_container = null;
			}
		}
	
	}
	
	function Success(){
		var _container = null;
		
		this.show = function(){
			if(!_container){
				_container = '<div id="toast" style="">'
				                +'<div class="weui_mask_transparent"></div>'
				                +'<div class="weui_toast">'
				                    +'<i class="weui_icon_toast"></i>'
				                    +'<p class="weui_toast_content">成功</p>'
				                +'</div>'
			           		 +'</div>';
			           		 
			  $('body').append($(_container));
				
			}
		}
		
		this.hide = function(){
			if(_container){
				$('body').find('[id="toast"]').remove();	
				_container = null;
			}
		}
		
	}
	
	//实例容器
	var instance = null;
	var success = null;
	
	var _static = {
		
		//获取实例的方法
		//返回Singleton的实例
		instance : function(){
			if(instance == null){
				instance = new Singleton();
			}
			return instance;
		},
		success : function(){
			if(success == null){
				success = new Success();
			}
			return success;
		}
	};
	return _static;
})();
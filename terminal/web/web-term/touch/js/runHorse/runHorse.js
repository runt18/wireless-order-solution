function CreateRunHorse(param){
	
	param = param || {
		css : null,            //要显示的样式 	
		size : null            //消息中心最大显示多少条信息
	}
	
	var containerId = null;
	
	//加载跑马灯
	function initRunHorse(word){
		var container = null;
		 
		container = $('<div/>');
		container.addClass('box-horizontal');
		container.css({
			'top' : '0',
			'left' : '30%',
			'z-index' : '10000',
			'position' : 'absolute'
		});
		
		container.attr('id', 'runHorse' + new Date().getTime());
		containerId = container.attr('id');
		
		//TODO   跑马灯
		var showWord = '<marquee behavior="slide" width="200">'+ word +'</marquee>';
		
		container.append(showWord);
		
		$('body').append(container);
	}
	
	
	this.open = function(afterOpen){
		
		//FIXME  如果id存在的时候要remove在重新生成,不存在就直接加载, 现在的问题是containerId一直是null,需要调试
		if(containerId){
			$('#' + containerId).remove();
			initRunHorse('我刚被删掉了,重新来过');
		}else{
			initRunHorse('我存在哟!你打我啊');
		}
	}	
	
	this.close = function(afterClose, timeout){
		
		//TODO
		if(containerId){
			$('#' + containerId).remove();
		}
	}

	
}

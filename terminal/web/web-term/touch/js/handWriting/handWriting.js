/**
 * 
 */
 
function HandWritingPanel(param){
	
	//在div上增加canvas
	var canvas = document.createElement("canvas");
	canvas.style.border = "3px green solid";
	canvas.style.background = "white";
	canvas.height = param.renderTo.clientHeight;
	canvas.width = param.renderTo.clientWidth;
	param.renderTo.appendChild(canvas);

	var ctx = canvas.getContext("2d");
	ctx.lineWidth = 6;//画笔粗细
	ctx.strokeStyle = "#000000";//画笔颜色
	
	//是否支持触摸
	var touchable = 'createTouch' in document;
	if (touchable) {
	    canvas.addEventListener('touchstart', onTouchStart, false);
	    canvas.addEventListener('touchmove', onTouchMove, false);
		canvas.addEventListener('touchend', onTouchEnd, false);
		canvas.addEventListener('touchout', onTouchOut, false);
	}else{
	    canvas.addEventListener('mousedown', onMouseDown, false);
	    canvas.addEventListener('mousemove', onMouseMove, false);
		canvas.addEventListener('mouseup', onMouseUp, false);
		canvas.addEventListener('mouseout', onMouseOut, false);
	}

	//上一次触摸坐标
	var lastX = 0;
	var lastY = 0;
	var bihua = [];
	//画圆
	function drawRound(x, y){
	    ctx.fillStyle = "#000000";
	    ctx.beginPath();
	    ctx.arc(x, y, 3, 0, Math.PI * 2, true);
	    ctx.closePath();
	    ctx.fill();
		bihua.push(x + "a" + y + "a");
		
	}
	//画线
	function drawLine(startX, startY, endX, endY){
	    ctx.beginPath();
	    ctx.lineCap = "round";
	    ctx.moveTo(startX, startY);
	    ctx.lineTo(endX, endY);
	    ctx.stroke();
		bihua.push(endX + "a" + endY + "a");
	}
	
	var drawing = false;
	
	function onMouseOut(event){
		//console.log('mouse out');
		drawing = false;
	}
	
	function onMouseUp(event) {
		//console.log('mouse up');
	    //ev = event || window.event; 
		//var mousePos = mousePosition(event);
		drawing = false; 
		bihua.push("s");
		senddata();
	}
	
	function onMouseDown(event) {
		//console.log('mouse down');
	    //ev = event || window.event; 
		//var mousePos = mousePosition(event);
		drawing = true; 
		event.preventDefault();
	    lastX = event.clientX;
	    lastY = event.clientY;
	    var top = getY(canvas);    
	    var left = getX(canvas);    
		lastX = lastX - left + document.body.scrollLeft;
		lastY = lastY - top + document.body.scrollTop;
	    drawRound(lastX, lastY);
	
	}
	
	function onMouseMove(event) {
		//console.log('mouse move');
	    //ev = event || window.event; 
		//var mousePos = mousePosition(event); w
		if(drawing){
			//lastX=event.clientX;
	    	//lastY=event.clientY;
	    	//drawRound(lastX,lastY);
			try{
	     	 	//event.preventDefault();
				var top = getY(canvas);    
				var left = getX(canvas);    
	     	 	drawLine(lastX, lastY, event.clientX - left + document.body.scrollLeft, event.clientY - top + document.body.scrollTop );
	     	 	lastX = event.clientX;
	    	  	lastY = event.clientY;
				lastX = lastX - left + document.body.scrollLeft;
				lastY = lastY -top + document.body.scrollTop;
				
	   		 } catch(err){
	   	     	alert( err.description);
	   		 }
		}
	}
	
	//触摸开始事件
	function onTouchOut(event){		
		drawing = false;
	}
	
	function onTouchStart(event) {
	    event.preventDefault();
	    lastX=event.touches[0].clientX;
	    lastY=event.touches[0].clientY;
		var top,left;    
	    top = getY(canvas);    
	    left = getX(canvas);    
		lastX = lastX - left + document.body.scrollLeft;
		lastY = lastY -top + document.body.scrollTop;
	    drawRound(lastX,lastY);
	}
	
	//触摸结束
	function onTouchEnd(event) {
	    bihua.push("s");
		senddata();
	}
	
	//触摸滑动事件
	function onTouchMove(event) {
	    try{
		  	var top = getY(canvas);    
			var left = getX(canvas);
	    	event.preventDefault();
	      	drawLine(lastX, lastY, event.touches[0].clientX - left + document.body.scrollLeft, event.touches[0].clientY - top + document.body.scrollTop);
	      	lastX = event.touches[0].clientX;
	      	lastY = event.touches[0].clientY;
			lastX = lastX - left + document.body.scrollLeft;
			lastY = lastY - top + document.body.scrollTop;
	    }
	    catch(err){
	        alert( err.description);
	    }
	
	}
	
	function getX(obj){
		var parObj = obj;  
		var left = obj.offsetLeft;  
	 	while(parObj = parObj.offsetParent){  
	  		left += parObj.offsetLeft;  
		}  
 		return left;  
	}  
  
	function getY(obj){  
		var parObj = obj;  
		var top = obj.offsetTop;  
		while(parObj = parObj.offsetParent){  
 			top += parObj.offsetTop;  
 		}  
	 	return top;  
	}  

	function senddata() {
		var lg = "zh-cn";//选择语言
		$.post("http://118.244.232.180/json/hd_json.php?key=c4ca4238a0b923820dcc509a6f75849b", {
					bh : lg + bihua.join("")
				}, function(data) {
					if(param.result){
						//Remove the duplicated result.
						param.result(data.split(" ").filter(function(item, pos, self){
							return self.indexOf(item) == pos && item.trim().length != 0 && item != "\"" ;
						}));
					}
				});
	}
	
	//public methods
	this.rewrite = function(){
		drawing = false;
		ctx.clearRect(0, 0, param.renderTo.clientWidth, param.renderTo.clientHeight);
        bihua = [];       
  	};
}

var HandWritingAttacher = (function () {

    //参数：传递给单例的一个参数集合
    function Singleton() {

        var attachInputs = [];
        var activeInput = null;
        var container = null;
        var word = null;
        var panel = null;
        var _handWritingPanel = null;
        var isMouseOver = false;
        this.attach = function(attachTo, onWordSelected, param){
        	//检查是否有重复控件
			var isExist = attachInputs.some(function(item, index, array){
				return item.attachObj.id == attachTo.id;
			});
			
			if(!isExist){
				var attachInput = {
					attachObj : attachTo,
					onWordSelected : onWordSelected,
	        		focusFn : function(){
	        			if(panel == null){
	        				container = $('<div/>');
	        				//TODO
	        				container.css(param || {
	        					'width' : '300px',
	        					'height' : '300px',
	        					'float' : 'right',
	        					'margin-right' : '0px'
	        				});   
	        				
	        				word = $('<div/>'); 	
	        				word.css({
	        					'width' : '40px',
	        					'height' : '300px',
	        					'float' : 'left',
	        					'margin-left' : '50px',
	        					'margin-top' : '-15px'
	        				});
	        				
	        				panel = $('<div/>');
	        				panel.css({
	        					'width' : '410px',
	        					'height' : '300px',
	        					'float' : 'right',
	        					'margin-top' : '25%',
	        					'right' :  '9px',
	        					'top' : 'initial',
	        					'bottom' : '0',  
	        					'position' : 'absolute',	 
	        					'z-index' : '24400'
	        					       				
	        				});
	        				
	        				panel.on('mouseover', function(){	
        						isMouseOver = true;	   
				        	});
				        	
				        	panel.on('mouseout', function(){
				        		isMouseOver = false;	
				        	});
	        				
	        				panel.append(word).append(container);	        			
	        				$('body').append(panel);
	        				
					  		_handWritingPanel = new HandWritingPanel({ 
								renderTo : container[0] ,
						   	   	result : function(data){
						   	    	var temp = data.slice(0, 4);
						   	   		var zifu = "";
						   	   		for (var i=0; i < temp.length; i++) {
										var eachCharactar = '<input type="button" style="width:60px;height:60px;font-size:26px;" value="' + temp[i] + '">';		
										if(i % 1 == 0){
											zifu += '<br>';
										}
										zifu += eachCharactar;
									};   	
									zifu +=	'<input type="button" style="width:60px;height:60px;font-size:20px;" value="重写">';
						   			word.html(zifu);		
						   			word.find('input').each(function(index, element){						   				
							   				element.onclick = function(){
							   					if(element.value == '重写'){
						   							_handWritingPanel.rewrite();	
						   						}else{
													$(activeInput.attachObj).val($(activeInput.attachObj).val() + element.value);	
													_handWritingPanel.rewrite();	
													if(activeInput.onWordSelected){
														activeInput.onWordSelected(activeInput.attachObj, element.value);
													}
												 }
											};	
									});			   			
						   		}
						   	});
	        			}
	        			for(var i = 0; i < attachInputs.length; i++){
	        				if(attachInputs[i].attachObj.id == attachTo.id){
	        					activeInput = attachInputs[i];
	        					break;
	        				}
	        			}
	        			
	        		},
	        		blurFn : function(e){
	        			 if(isMouseOver){
	        				 $(activeInput.attachObj).focus();
	        			 }else{
	        				 if(panel){
								 panel.remove();
								 panel = null;
								 container = null;
								 word = null;
								 _handWritingPanel = null;
							 }
						 	 activeInput = null;
	        			 }
	        		}
				};
				
	        	attachInputs.push(attachInput);
	        	
	        	$(attachTo).on('focus', attachInput.focusFn);
	        	
	        	$(attachTo).on('blur', attachInput.blurFn);
	        	
	        	
			}
			
        	return this;
        };
        
        this.detach = function(detachFrom){
			//删除focus和 blur事件的处理函数
			for(var i = 0; i < attachInputs.length; i++){
				if(attachInputs[i].attachObj.id == detachFrom.id){
					$(attachInputs[i].attachObj).off('focus', attachInputs[i].focusFn);
					$(attachInputs[i].attachObj).off('blur', attachInputs[i].blurFn);	
				}
			}
			//删除数组中的Input组件
			attachInputs = attachInputs.filter(function(item, index, array){
				return detachFrom.id != item.attachObj.id;
			});
			
			return this;
        };
        
    }

    //实例容器
    var instance = null;

    var _static = {

        //获取实例的方法
        //返回Singleton的实例
        instance : function () {
            if (instance == null) {
                instance = new Singleton();
            }
            return instance;
        }
    };
    return _static;
})();


/**
 * 
 */
$(function(){
	var handWriting = new HandWritingPanel(
		{ renderTo : document.getElementById('handWritingPanel'),
		  result : function(data){
//		  	for(var i = 0; i < data.length; i++){
//		  		data[i]
//		  	}
			console.log(data);	
		}}
		);
	$('#buttonRewrite').click(function(){
		handWriting.rewrite();
	});
});
 
function HandWritingPanel(param){
	
	//在div上增加canvas
	var canvas = document.createElement("canvas");
	canvas.style.border = "3px green solid";
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
	}else{
	    canvas.addEventListener('mousedown', onMouseDown, false);
	    canvas.addEventListener('mousemove', onMouseMove, false);
		canvas.addEventListener('mouseup', onMouseUp, false);
	}

	//上一次触摸坐标
	var lastX;
	var lastY;
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
	    ctx.moveTo(startX,startY);
	    ctx.lineTo(endX,endY);
	    ctx.stroke();
		bihua.push(endX + "a" + endY + "a");
	}
	
	var drawing = false;
	function onMouseUp(event) {
	    //ev = event || window.event; 
		//var mousePos = mousePosition(event);
		drawing = false; 
		bihua.push("s");
		senddata();
	}
	
	function onMouseDown(event) {
	    //ev = event || window.event; 
		//var mousePos = mousePosition(event);
		drawing = true; 
	    lastX = event.clientX;
	    lastY = event.clientY;
	    var top = getY(canvas);    
	    var left = getX(canvas);    
		lastX = lastX - left + document.body.scrollLeft;
		lastY = lastY - top + document.body.scrollTop;
	    drawRound(lastX, lastY);
	
	}
	
	function onMouseMove(event) {
	    //ev = event || window.event; 
		//var mousePos = mousePosition(event); 
		if(drawing){
			//lastX=event.clientX;
	    	//lastY=event.clientY;
	    	//drawRound(lastX,lastY);
			try{
	     	 	//event.preventDefault();
				var top = getY(canvas);    
				var left = getX(canvas);    
	     	 	drawLine(lastX,lastY,event.clientX - left + document.body.scrollLeft, event.clientY - top + document.body.scrollTop );
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

	function YBZ_DeleteTheSameChar(str) {
		var newStr = "";
		for (var i = 0; i < 28; i++) {
			if (newStr.indexOf(str.charAt(i)) == -1) {
				newStr += "<input type='button' value='" + str.charAt(i)
						+ "' onclick='javascript:showmsg(this.value);'/>";
			}
		}
		return newStr;
	}
	
	function senddata() {
		var lg = "zh-cn";//选择语言
		$.post("http://www.yibizi.com/json/hd_json.php", {
					bh : lg + bihua.join("")
				}, function(data) {
					if(param.result){
						//Remove the duplicated result.
						param.result(data.split(" ").filter(function(item, pos, self){
							return self.indexOf(item) == pos;
						}))
					}
				});
	}
	
	//public methods
	this.rewrite = function(){
		//TODO
		ctx.clearRect(0,0,300,200);
        bihua = [];
  	}
}


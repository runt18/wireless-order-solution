
$.ajaxSetup({
	contentType:"application/x-www-form-urlencoded;charset=utf-8",
	global : true,
	complete:function(XMLHttpRequest,textStatus){ 
		//通过XMLHttpRequest取得响应头
		if(XMLHttpRequest.getResponseHeader("session_status")){ 
//			logout();
		} 
	}
});

/**
 * 
 * @param args
 * @returns {String}
 */
String.prototype.format = function(args){
    var result = this;
    if (arguments.length > 0){    
        if (arguments.length == 1 && typeof args == "object"){
            for(var key in args) {
                if(args[key] != undefined){
                    var reg = new RegExp("({" + key + "})", "g");
                    result = result.replace(reg, args[key]);
                }
            }
        }else{
        	for(var i = 0; i < arguments.length; i++){
        		if (arguments[i] != undefined) {
        			var reg= new RegExp("({)" + i + "(})", "g");
        			result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
};
/**
 * 
 */
String.prototype.trim = function(){
	return this.replace(/(^\s*)|(\s*$)/g, ""); 
};

//工具包
var Util = {
	sys : {}
};

//外卖模式
Util.to = {};

//外卖模式上翻下翻
/*Util.to.scroll = function(c){
	if(c == null || typeof c.otype != 'string'){
		return;
	}
	var dom = getDom(c.content);
	if(c.otype.toLowerCase() == 'up'){
		if(dom.scrollTop < 50){
			$('html, body').animate({scrollTop: 0}, 'fast');
		}else{
			$('html, body').animate({scrollTop: dom.scrollTop - (typeof c.size == 'number' ? c.size : 50 * 3)}, 'fast');
		}
	}else if(c.otype.toLowerCase() == 'down'){
		if(dom.scrollHeight == dom.scrollTop){
			
		}else if(dom.scrollHeight - dom.scrollTop < 50){
			$('body').animate({scrollTop: dom.scrollHeight}, 'fast');
		}else{
			$('body').animate({scrollTop: dom.scrollTop + (typeof c.size == 'number' ? c.size : 50 * 3)}, 'fast');
		}
	}
};*/
Util.to.scroll = function(c){
	if(!c){
		c = {};
	}
	var dom = document.getElementById(c.content);
//	console.log('top, hei'+dom.scrollTop+',' + dom.scrollHeight)
	if(c.otype.toLowerCase() == 'up'){
		if(dom.scrollTop < 50){
			$(dom).animate({scrollTop: 0}, 'fast');
		}else{
			$(dom).animate({scrollTop: dom.scrollTop - (typeof c.size == 'number' ? c.size : 60 * 3)}, 'fast');
		}
	}else if(c.otype.toLowerCase() == 'down'){
		if(dom.scrollHeight == dom.scrollTop){
			
		}else if(dom.scrollHeight - dom.scrollTop < 50){
			$(dom).animate({scrollTop: dom.scrollHeight}, 'fast');
		}else{
			$(dom).animate({scrollTop: dom.scrollTop + (typeof c.size == 'number' ? c.size : 60 * 3)}, 'fast');
		}
	}
};

//新版消息弹框
Util.msg = {
	event : [],
	interval : [],
	fireEvent : function(btn, id){
		for(var i = 0; i < this.event.length; i++){
			if(this.event[i].id == id && typeof this.event[i].fn == 'function'){
				this.event[i].fn(btn);
				break;
			}
		}
	},
	clearEvent : function(id){
		for(var i = 0; i < this.event.length; i++){
			if(this.event[i].id == id){
				this.event.splice(i, 1);
				break;
			}
		}
	},
	clearInterval : function(id){
		for(var i = 0; i < this.interval.length; i++){
			if(this.interval[i].id == id){
				clearInterval(this.interval[i].interval);
				this.interval.splice(i, 1);
				break;
			}
		}
	},
	createContent : function(c){
		var id = this.createId();
		var hasBack = (typeof c.buttons == 'string' && c.buttons.toUpperCase() == 'YESBACK') ? true : false;
		
		var content = '<div data-role="popup" id="'+id+'" data-theme="c" data-dismissible="false" style="min-width:200px;max-width:400px;" class="ui-corner-all">'+
						    '<div data-role="header" class="ui-corner-top ui-header ui-bar-b" data-theme="b"><h1 class="ui-title" role="heading" aria-level="1">'+(typeof c.title != 'string' || $.trim(c.title).length == 0 ? '温馨提示' : c.title)+'</h1></div>'+
						    '<div data-type="time" style="text-align: center;color: red;"></div>'+
						    '<div data-role="content" data-theme="d" class="ui-corner-bottom ui-content" align="center">'+
						    	'<h2 class="ui-title">'+c.msg+'</h2>'+
						        '<a onclick="Util.msg.save({event:\'yes\', id:\''+id+'\'})" data-role="button" ' +(hasBack ? 'data-inline="true"': "") +' data-theme="b" >'+ (typeof c.btnEnter != 'undefined'? c.btnEnter : "确定") +'</a>'+
						        (hasBack ? '<a href="javascript:Util.msg.hide({event:\'back\', id:\''+id+'\', callback:' + c.returnCallback +'})" data-role="button" data-theme="c" data-inline="true" >取消</a>' : '')+
						    '</div>'+
						'</div>	';
		return {
			id : id,
			content : content
		};
	},
	createTopTip : function(c){
		var id = this.createId();
		var content = '<div id="'+ id +'" class="ui-content-all toptip slidedown in" ><p>'+ c.msg +'</p></div>';
		$('body').append(content);
		$('#'+id).css('left', (document.body.clientWidth - $('#'+id).width()) / 2);
		setTimeout(function(){
			$('#'+id).addClass('reverse').addClass('out');
			setTimeout(function(){
				$('#'+id).remove();	
			}, 200);	
		}, 2500);		
	},
	createId : function(){
		var id = null;
		var dom = null;
		while(true){
			id = 'divMsg-' + parseInt(Math.random() * 10000);
			dom = document.getElementById(id);
			if(!dom)
				break;
		}
		dom = null;
		return id;
	},
	save : function(c){
		//关闭消息框
		$('#'+c.id).popup({
			afterclose : function(){
				//销毁dom
				$('#'+c.id).remove();
				$('#'+c.id).parent().remove();
				
				Util.msg.fireEvent(c.event, c.id);
				Util.msg.clearEvent(c.id);
				Util.msg.clearInterval(c.id);					
			}
		});
		$('#'+c.id).popup('close');
	
	},
	hide : function(c){
		//关闭消息框
		$('#'+c.id).popup('close');
		
		//销毁dom
		$('#'+c.id).remove();
		$('#'+c.id).parent().remove();
		
		if(typeof c.callback == 'function'){
			c.callback();
		}
	},
	alert : function(c){
		
		if(c.topTip){
			this.createTopTip(c);
		}else{
			var content = this.createContent({
				title : c.title,
				msg : c.msg,
				buttons : c.buttons, //yes or yesback
				btnEnter : c.btnEnter,
				time : c.time,
				returnCallback : c.returnCallback,
				certainCallback : c.certainCallback
			});
			//把消息框加入指定page底部
			$('#' + c.renderTo).append(content.content);
			//动态创建组件
			$('#' + content.id).trigger("create");
			//声明为popup
			$('#' + content.id).popup();
			//添加弹出样式
			$('#' + content.id).parent().addClass("pop").addClass("in");
			//弹出组件
			$('#' + content.id).popup('open');
			
			if(typeof c.fn == 'function'){
				this.event.push({
					id : content.id,
					fn : c.fn
				});
			}
			
			if(typeof c.certainCallback == 'function'){
				this.event.push({
					id : content.id,
					fn : c.certainCallback
				});
			}		
			
			if(typeof c.time == 'number'){
				var to = null, t = c.time;
				to = setInterval(function(){
					if(t == 0){
						//销毁
						Util.msg.save({id: content.id});
						to = null;
						return;
					}
					$('#'+content.id+' > div[data-type=time]').html(t + ' 秒后自动关闭.');
					t--;
				}, 1000);
				this.interval.push({
					id : content.id,
					interval : to
				});
			}			
		}
	}
	,tip : function(msg){
		this.alert({
			msg : msg,
			topTip : true
		});
	}
};


Util.LM = {
	show : function(c){
		var msg = "请求发送中...";
		if(c && c.msg){
			msg = c.msg;
		}
	    $.mobile.loadingMessageTextVisible = true;  
	    $.mobile.showPageLoadingMsg( 'a', msg);		
	},
	hide : function(){
		$.mobile.hidePageLoadingMsg();
	}
};

/**
 * 记录系统属性
 */
Util.sys.smsModule = false;
Util.sys.smsCount = 0;
Util.sys.checkSmStat = function(){
	$.ajax({
		url : '../QueryModule.do',
		type : 'post',
//		async:false,
		data : {
			dataSource : 'checkModule',
			code : 4000
		},
		success : function(jr, status, xhr){
			if(jr.success){
				Util.sys.smsModule = true;
				Util.sys.smsCount = jr.code;
			}else{
				Util.sys.smsModule = false;
			}
		},
		error : function(request, status, err){}
	}); 	
};

/**
 * cookies操作
 */
function setcookie(name,value, path, time){  
	var exp= new Date(), pathData = "";
	
	//js要设置毫秒, java则为秒
	if(time){
	    exp.setTime(exp.getTime() + time); 
	}else{
	    exp.setTime(exp.getTime() + 30*365 * 24*60*60*1000); 		
	}
 
    //不设置path默认使用当前相对路径, 会导致ajax不能上传cookie
    if(path){
    	pathData = "path=" + path + ";";
    }else{
    	pathData = "path=/web-term/touch/;";
    }
	document.cookie = name + "=" + escape (value) + ";" + pathData +"expires = " + exp.toGMTString();    
}

function getcookie(name){  
    var arr = document.cookie.match(new RegExp("(^| )"+name+"=([^;]*)(;|$)"));   
    if(arr != null){  
        return unescape(arr[2]);  
    }else{  
        return "";  
    }  
}

function delcookie(name, path){  
    var exp = new Date();   
    //-1:关闭浏览器后删除, 0:立即删除, >0:不删除
    exp.setTime(0);  
    var cval=getcookie(name);  
    if(cval!=null) document.cookie= name + "="+cval+";path=" + (path ? path : "/web-term/touch/") + ";expires="+exp.toGMTString();  
}

/**
 * 克隆对象
 * @param myObj
 * @returns
 */
Util.clone = function(myObj){
	  if(typeof(myObj) != 'object') return myObj;
	  if(myObj == null) return myObj;
	  
	  var myNewObj = new Object();
	  
	  for(var i in myObj)
	    myNewObj[i] = Util.clone(myObj[i]);
	  
	  return myNewObj;
}

/**
 * 只允许输入数字
 */
function intOnly(){
	  var codeNum=event.keyCode;
	  if(codeNum==8||codeNum==37||codeNum==39|| codeNum==110 || (codeNum>=48&&codeNum<=57)){
	    event.returnValue=codeNum;
	  }else{
	    event.returnValue=false;
	  }
}

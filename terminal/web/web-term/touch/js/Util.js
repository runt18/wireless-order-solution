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
//FIXME 临时加的判断是否沽清页面, 数据重构后删除
Util.sellOutCond = false;

//外卖模式
Util.to = {};

/**
 * 分页工具
 * @param c
 */
Util.padding = function(c){
	if(c == null || typeof c.renderTo == 'undefined' || typeof c.templet != 'function'){
		return;
	}
	var obj = {
		renderTo : c.renderTo,
		templet : c.templet,
		dom : null,
		start : 0,
		limit : 20,
		data : [],
		length : 0,
		pageData : [],
		displayMsg : typeof c.displayMsg != 'undefined' ? c.displayMsg : '共{0}项, 第{1}/{2}页, 每页{3}项',
		displayId : c.displayId,
		isEmpty : function(){
			return this.data == null || this.data.length <= 0;
		},
		getData : function(){
			return this.data;
		},
		getPageData : function(){
			return this.pageData;
		},
		clear : function(){
			this.start = 0;
			this.limit = 20;
			this.data = [];
			this.length = 0;
			this.pageData = [];
		},
		clearContent : function(){
			if(this.dom){
				this.dom.innerHTML = '&nbsp;';
			}
			this.showMsg();
		},
		showMsg : function(){
			if(this.displayId != null && this.displayId != ''){
				var md = getDom(this.displayId);
				if(md){
					if(this.length > 0){
						md.innerHTML = this.displayMsg.format(this.length,
							parseInt(this.start / this.limit) + 1,
							parseInt(this.length / this.limit) + (this.length % this.limit == 0 ? 0 : 1),
							this.limit
						);
					}else{
						md.innerHTML = '&nbsp;';
					}
				}
				md = null;
			}
		},
		init : function(ic){
			ic = ic == null ? {data:[]} : ic;
			//
			this.dom = getDom(this.renderTo);
			this.clearContent();
//			console.log(this.displayId )
			this.showMsg();
			//
			var ch = this.dom.clientHeight, cw = this.dom.clientWidth;
			this.limit = parseInt((ch / (70 + 5 + 3 * 2))) * parseInt((cw / (90 + 5 + 3 * 2)));
			//
			this.data = ic.data == null || typeof ic.data == 'undefined' ? [] : ic.data;
			this.length = this.data.length;
			if(typeof ic.callback == 'function'){
				ic.callback();
			}
		},
		initContent : function(c){
			this.pageData = [];
			this.clearContent();
			if(!this.isEmpty()){
				var html = '';
				var start = this.start, limit = this.data.length >= this.start + this.limit ? this.limit : this.limit - (this.start + this.limit -this.data.length);
				var temp = null;

				for(var i = 0; i < limit; i++){
					temp = this.data[start+i];
					//FIXME 判断是否为沽清页面, 是就不用验证菜品状态
//					if(Util.sellOutCond){
//					if(true){
					this.pageData.push(temp);
					if(temp != null){
						html += this.templet({
							dataIndex : i,
							data : temp
						});	
					}
//					}else{
//						if((temp.status & 1 << 2) == 0){
//							this.pageData.push(temp);
//							if(temp != null){
//								html += this.templet({
//									dataIndex : i,
//									data : temp
//								});	
//							}
//	
//						}
//					}


				}
				temp = null;
				
				this.dom.innerHTML = html;
			}
			this.showMsg();
		},
		getFirstPage : function(){
			this.start = 0;
			this.initContent();
		},
		getLastPage : function(){
			this.start = this.data.length - this.data.length % this.limit;
			this.initContent();
		},
		getNextPage : function(){
			this.start += this.limit;
			if(this.start > this.data.length){
				this.start -= this.limit;
				return;
			}
			this.initContent();
		},
		getPreviousPage : function(){
			this.start -= this.limit;
			if(this.start < 0){
				this.start += this.limit;
				return;
			}
			this.initContent();
		}
	};
	obj.init({
		data : c.data,
		callback : c.callback
	});
	return obj;
};

/**
 * 
 * @param c
 */
Util.scroll = function(c){
	if(c == null || typeof c.otype != 'string'){
		return;
	}
	var dom = getDom(c.content);
	if(c.otype.toLowerCase() == 'up'){
		if(dom.scrollTop < 50){
			dom.scrollTop = 0;
		}else{
			dom.scrollTop = dom.scrollTop - (typeof c.size == 'number' ? c.size : 50 * 3);
		}
	}else if(c.otype.toLowerCase() == 'down'){
		if(dom.scrollHeight == dom.scrollTop){
			
		}else if(dom.scrollHeight - dom.scrollTop < 50){
			dom.scrollTop = dom.scrollHeight;
		}else{
			dom.scrollTop = dom.scrollTop + (typeof c.size == 'number' ? c.size : 50 * 3);
		}
	}
};


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

//新版分页
Util.to.padding = function(c){
	if(c == null || typeof c.renderTo == 'undefined' || typeof c.templet != 'function'){
		return;
	}
	var obj = {
		renderTo : c.renderTo,
		templet : c.templet,
		dom : null,
		start : 0,
		limit : c.limit,
		data : [],
		length : 0,
		pageData : [],
		displayMsg : typeof c.displayMsg != 'undefined' ? c.displayMsg : '共{0}项, 第{1}/{2}页, 每页{3}项',
		displayId : c.displayId,
		isEmpty : function(){
			return this.data == null || this.data.length <= 0;
		},
		getData : function(){
			return this.data;
		},
		getPageData : function(){
			return this.pageData;
		},
		clear : function(){
			this.start = 0;
			this.limit = 20;
			this.data = [];
			this.length = 0;
			this.pageData = [];
		},
		clearContent : function(){
			if(this.dom){
				this.dom.html('');
			}
			this.showMsg();
		},
		pagedCallBack : c.pagedCallBack,//执行分页后调用
		showMsg : function(){
			if(this.displayId != null && this.displayId != ''){
				var md = $('#'+this.displayId);
				if(md){
					if(this.length > 0){
						md.html(this.displayMsg.format(this.length,
							parseInt(this.start / this.limit) + 1,
							parseInt(this.length / this.limit) + (this.length % this.limit == 0 ? 0 : 1),
							this.limit
						));
					}else{
						md.html('');
					}
				}
				md = null;
			}
		},
		init : function(ic){
			ic = ic == null ? {data:[]} : ic;
			
//			console.log('renddiv')
//			console.log(this.renderTo)
			
			this.dom = $('#'+this.renderTo);
			
			this.clearContent();
			//重置右下角分页导航信息
			this.showMsg();			
			//
			var ch = this.dom[0].clientHeight, cw = this.dom[0].clientWidth;
			
//			console.log(this.dom)
//			console.log('ch+cw:' +ch + ',' +cw)
//			console.log('limit:'+this.limit)
			
			if(!this.limit){
				this.limit = parseInt(ch / (65 + 18)) * parseInt(cw / (100 + 12));
			}
			//
			this.data = ic.data == null || typeof ic.data == 'undefined' ? [] : ic.data;
			this.length = this.data.length;
			if(typeof ic.callback == 'function'){
				ic.callback();
			}
		},
		initContent : function(c){
			c = c || {};
			this.pageData = [];
			this.clearContent();
			
/*			console.log('limit:'+this.limit)
			console.log('padata')
			console.log(this.data)*/
			
			if(!this.isEmpty()){
				var html = [];
				var start = this.start, limit = this.data.length >= this.start + this.limit ? this.limit : this.limit - (this.start + this.limit -this.data.length);
				
				var temp = null;
				for(var i = 0; i < limit; i++){
					temp = this.data[start+i];
					this.pageData.push(temp);
					if(temp != null){
						html.push(this.templet({
							index : i,
							data : temp
						}));	
					}
				}
				temp = null;
/*				console.log('html')
				console.log(html)*/
				
				this.dom.html(html.join("")).trigger('create');
				
				if(c.around){
					this.dom.buttonMarkup( "refresh" );
				}
				
				if(typeof this.pagedCallBack == 'function'){
					this.pagedCallBack();
				}
				
			}
		},
		getFirstPage : function(c){
			this.start = 0;
			this.initContent(c);
		},
		getLastPage : function(){
			this.start = this.data.length - this.data.length % this.limit;
			this.initContent();
		},
		getNextPage : function(c){
			this.start += this.limit;
			if(this.start > (this.data.length-1)){
				this.start -= this.limit;
				return;
			}
			this.initContent(c);
		},
		getPreviousPage : function(c){
			this.start -= this.limit;
			if(this.start < 0){
				this.start += this.limit;
				return;
			}
			this.initContent(c);
		}
	};
	obj.init({
		data : c.data,
		callback : c.callback
	});
	return obj;
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

/**
 * 
 * @param c
 * 	renderTo
 * 	type
 */
Util.dialongDisplay = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	var el = $('#'+c.renderTo), lm = $('div[for='+c.renderTo+']');
	if(!el){return;}
	if(lm.length <= 0){
		el.before('<div for="'+c.renderTo+'" style="opacity:0; position: absolute; top:0; left:0; width: 100%; height: 100%; background: #DDD;"></div>');
		lm = $('div[for='+c.renderTo+']');
	}
	if($.trim(c.type) == 'show'){
		el.css({
			zIndex: 99999,
			position: 'absolute',
			top: '50%',
			left: '50%',
			margin: '-{0}px 0 0 -{1}px'.format(el.height() / 2, el.width() / 2)
		});
		if(el.hasClass('dialong-hide')){
			el.removeClass('dialong-hide');			
		}
		if(lm.hasClass('dialong-lm-hide')){
			lm.removeClass('dialong-lm-hide');			
		}
		if(lm.hasClass('dialong-lm-hide-top')){
			lm.removeClass('dialong-lm-hide-top');			
		}
		if(typeof c.isTop == 'boolean' && typeof c.isTop){
			lm.removeClass('dialong-lm-show');
			lm.addClass('dialong-lm-show-top');
		}else{
			lm.removeClass('dialong-lm-show-top');
			lm.addClass('dialong-lm-show');
		}
		el.addClass('dialong-show');
	}else if($.trim(c.type) == 'hide'){
		el.addClass('dialong-hide');
		if(typeof c.isTop == 'boolean' && typeof c.isTop){
			lm.addClass('dialong-lm-hide-top');
		}else{
			lm.addClass('dialong-lm-hide');
		}
		if(typeof c.remove == 'boolean' && c.remove){
			var interval = null;
			interval = setInterval(function(){
				el[0].parentNode.removeChild(el[0]);
				lm[0].parentNode.removeChild(lm[0]);				
				clearInterval(interval);
				interval = null;
			}, 500);
		}
	}
};
/**
 * 旧版消息弹框
 */
//Util.msg = {
//	event : [],
//	interval : [],
//	fireEvent : function(btn, id){
//		for(var i = 0; i < this.event.length; i++){
//			if(this.event[i].id == id && typeof this.event[i].fn == 'function'){
//				this.event[i].fn(btn);
//				break;
//			}
//		}
//	},
//	clearEvent : function(id){
//		for(var i = 0; i < this.event.length; i++){
//			if(this.event[i].id == id){
//				this.event.splice(i, 1);
//				break;
//			}
//		}
//	},
//	clearInterval : function(id){
//		for(var i = 0; i < this.interval.length; i++){
//			if(this.interval[i].id == id){
//				clearInterval(this.interval[i].interval);
//				this.interval.splice(i, 1);
//				break;
//			}
//		}
//	},
//	createContent : function(c){
//		var id = this.createId();
//		var content = '<div id="'+id+'" class="box-vertical msg-base">'
//			+ '<div data-type="title">'+(typeof c.title != 'string' || $.trim(c.title).length == 0 ? '温馨提示' : c.title)+'</div>'
//			+ '<div data-type="content">'+c.msg+'</div>'
//			+ (typeof c.time == 'number' ? '<div data-type="time">&nbsp;</div>' : '')
//			+ '<div data-type="button" class="box-horizontal">'
//				+ '<div class="div-full"></div>'
//				+ '<div class="button-base" style="width:150px;" onClick="Util.msg.save({event:\'yes\', id:\''+id+'\', callback:' + c.certainCallback +'})">'+ (typeof c.btnEnter != 'undefined'? c.btnEnter : "确定") +'</div>'
//				+ (typeof c.buttons == 'string' && c.buttons.toUpperCase() == 'YESBACK' ? '<div class="button-base" style="width:150px; margin-left:20px;" onClick="Util.msg.hide({event:\'back\', id:\''+id+'\', callback:' + c.returnCallback +'})">返回</div>' : '')
//				+ '<div class="div-full"></div>'
//			+ '</div>'
//			+ '</div>';
//		return {
//			id : id,
//			content : content
//		};
//	},
//	createId : function(){
//		var id = null;
//		var dom = null;
//		while(true){
//			id = 'divMsg-' + parseInt(Math.random() * 10000);
//			dom = getDom(id);
//			if(!dom)
//				break;
//		}
//		dom = null;
//		return id;
//	},
//	save : function(c){
//		this.hide({
//			event : 'yes',
//			id : c.id
//		});
//		
//		if(typeof c.callback == 'function'){
//			c.callback();
//		}
//	},
//	hide : function(c){
//		Util.dialongDisplay({
//			renderTo : c.id,
//			type : 'hide',
//			remove : true
//		});
//		this.fireEvent(c.event, c.id);
//		this.clearEvent(c.id);
//		this.clearInterval(c.id);
//		
//		if(typeof c.callback == 'function'){
//			c.callback();
//		}
//	},
//	alert : function(c){
//		var content = this.createContent({
//			title : c.title,
//			msg : c.msg,
//			fn : c.fn,
//			buttons : c.buttons,
//			btnEnter : c.btnEnter,
//			time : c.time,
//			returnCallback : c.returnCallback,
//			certainCallback : c.certainCallback
//		});
//		document.body.insertAdjacentHTML('beforeEnd', content.content);
//		Util.dialongDisplay({
//			renderTo : content.id,
//			type : 'show'
//		});
//		if(typeof c.fn == 'function'){
//			this.event.push({
//				id : content.id,
//				fn : c.fn
//			});
//		}
//		if(typeof c.time == 'number'){
//			var to = null, t = c.time;
//			to = setInterval(function(){
//				if(t == 0){
//					Util.msg.clearInterval(c.id);
//					Util.msg.hide({
//						event : 'yes', 
//						id : content.id
//					});
//					to = null;
//					return;
//				}
//				$('#'+content.id+' > div[data-type=time]').html(t + ' 秒后自动关闭.');
//				t--;
//			}, 1000);
//			this.interval.push({
//				id : content.id,
//				interval : to
//			});
//		}
//	}
//};
//

/**
 * 
 */
Util.toggleDisplay = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	var el = $('#'+c.el);
	if(!el){return;}
	if($.trim(c.type) == 'show'){
		if(el.hasClass(c.hideCls)){
			el.removeClass(c.hideCls);
		}
		el.addClass(c.showCls);
	}else if($.trim(c.type) == 'hide'){
		el.addClass(c.hideCls);
	}
};
/**
 * 
 */
Util.toggleContentDisplay = function(c){
	Util.toggleDisplay({
		showCls : 'content-show',
		hideCls : 'content-hide',
		el : c.renderTo || c.el,
		type : c.type
	});
};
/**
 * 
 */
Util.toggleToolbarDisplay = function(c){
	Util.toggleDisplay({
		showCls : 'toolbar-show',
		hideCls : 'toolbar-hide',
		el : c.el,
		type : c.type
	});
};

/**
 * 
 */
/*Util.LM = (function(){
	var $ = {
		isDisplay : false,
		id : 'lm-content-'+parseInt(Math.round(1-1000)),
		dom : null
	};
	var initDocument = function(){
		var el = '<div id="'+$.id+'" data-type="lm-content-circular">'
				 	+ '<div data-type="lm-content-circular-1"></div>'
				 	+ '<div data-type="lm-content-circular-2"></div>'
				 	+ '<div data-type="lm-content-circular-3"></div>'
				 	+ '<div data-type="lm-content-circular-4"></div>'
				 	+ '<div data-type="lm-content-circular-5"></div>'
				 	+ '<div data-type="lm-content-circular-6"></div>'
				 	+ '<div data-type="lm-content-circular-7"></div>'
				 	+ '<div data-type="lm-content-circular-8"></div>'
				 + '</div>';
		
		document.body.insertAdjacentHTML('beforeEnd', el);
		$.dom = document.getElementById($.id);
		
		$.isInit = true;
	};
	$.show = function(){
		if(!this.dom || !document.getElementById(this.id)){
			initDocument();
		}
		if(!this.isDisplay){
			Util.dialongDisplay({
				renderTo : this.id,
				type : 'show'
			});
			this.isDisplay=true;
		}
	};
	$.hide = function(){
		if(this.dom && this.isDisplay){
			Util.dialongDisplay({
				renderTo : this.id,
				type : 'hide'
			});
			this.isDisplay=false;
		}
	};
	return $;
})();*/

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

//从url获取当前桌信息
function Util_urlParaQuery() {
	var name, value, i, key = 0;
	var str = location.href;
	
	if(str.indexOf("#") > 0){
		str = str.substring(0,str.indexOf("#"));
	}
	var num = str.indexOf("?");
	if(num > 0){
		str = str.substr(num + 1);
		var arrtmp = str.split("&");
		for (i = 0; i < arrtmp.length; i++) {
			num = arrtmp[i].indexOf("=");
			if (num > 0) {
				name = arrtmp[i].substring(0, num);
				value = arrtmp[i].substr(num + 1);
				this[name] = value;
			}
		}
	}
}

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
 * 检查是否为小数
 * @param num
 * @returns {Boolean}
 */
function checkDot(num){
	if(!isNaN(num)){
		num = num + ""; 
	}
	var dot = num.indexOf(".");
	if(dot != -1){
	    var dotCnt = num.substring(dot+1,dot+2);
	    var dotCnt2 = num.substring(dot+2);
	    if(dotCnt >= 1 || dotCnt2 >= 1){
	        return true;
	    }else{
	    	return false;
	    }
	}else{
		return false;
	}
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

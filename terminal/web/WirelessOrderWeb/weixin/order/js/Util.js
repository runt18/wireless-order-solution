String.prototype.format = function(args){
    var result = this;
    if (arguments.length > 0){    
        if (arguments.length == 1 && typeof args == 'object'){
            for(var key in args) {
                if(args[key] != undefined){
                    var reg = new RegExp('({' + key + '})', 'g');
                    result = result.replace(reg, args[key]);
                }
            }
        }else{
        	for(var i = 0; i < arguments.length; i++){
        		if (arguments[i] != undefined) {
        			var reg= new RegExp('({)' + i + '(})', 'g');
        			result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
};
String.prototype.trim = function(){
	return this.replace(/(^\s*)|(\s*$)/g, ''); 
};
String.prototype.isEmpty = function(){
	return this.trim().length == 0;
};
Date.prototype.format = function(fmt) {
	var o = {
		'M+': this.getMonth() + 1,
		'd+': this.getDate(),
		'h+': this.getHours(),
		'm+': this.getMinutes(),
		's+': this.getSeconds(),
		'q+': Math.floor((this.getMonth() + 3) / 3),
		'S': this.getMilliseconds()
	};
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length));
    for (var k in o)
    if (new RegExp('(' + k + ')').test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)));
    return fmt;
};
Object.clone = function(obj){
	if(typeof obj !== 'object'){
		return obj;   
	}
	 var clone = {}; 
	 if(obj.constructor == Array){   
		 clone = [];
	 }
	 for(var i in obj){  
		 clone[i] = Object.clone(obj[i]);
	 }
	 return clone;
};
var Util = {
	mp : { oid : 0, fid : 0 },
	getDom : function(id){ return document.getElementById(id); },
	hparam : [],
	initParams : function(){
		var str = location.href;
		var temp = str.indexOf('?');
		str = str.substr(temp + 1);
		this.hparam = str.split('&');
		this.mp.oid = Util.getParam('m');
		this.mp.fid = Util.getParam('r');
	},
	getParam : function(key){
		var temp;
		for (var i = 0; i < this.hparam.length; i++) {
			temp = this.hparam[i].split('=');
			if (temp.length > 0 && temp[0].trim() == key) {
				return temp[1].trim();
			}
		}
	},
	lineTD : function(line, t){
		var sc='m-b-line-show', hc='m-b-line-hide';
		if(t=='hide'){
			if(line.hasClass(sc)) line.removeClass(sc);
			line.addClass(hc);
		}else if(t=='show'){
			if(line.hasClass(hc)) line.removeClass(hc);
			line.addClass(sc);
		}
	},
	URLTemplet : './{0}?m={1}&r={2}&time={3}',
	defineURL : function(page){
		this.mp.oid = 'o_da4uFcIRO1-WkbnEfebmstqFQw'; this.mp.fid = 'gh_cbad03f831ab';
		return this.URLTemplet.format(page, this.mp.oid, this.mp.fid, new Date().getTime());
	},
	skip : function(page){
		window.location.href = this.defineURL(page);
	},
	lbar : function(addr, cb){
		$.ajax({
			url : addr + 'mbar.html',
			success : function(html){
				if(typeof cb == 'function'){ cb(html); }
			}
		});
	}
};
Util.initParams();
/**
 * Enpty:
 * Util.lm.show();
 */
Util.lm = {
	box : '',
	img : 'images/loading.gif',
	id : 'div-loadmask-m-ld',
	templet : '<div id={id} class="div-mask div-mask-ld"><div class="img"><img src="{img}" style="border:0;margin:5px 0 0 6px;"></div></div>',
	init : function(){
		if(!this.box){
			document.body.insertAdjacentHTML('afterBegin', this.templet.format({id:Util.lm.id, img:Util.lm.img}));
			this.box = Util.getDom(this.id);
		}
	},
	show : function(){
		this.init();
		this.box.style.display = 'block';
	},
	hide : function(){
		this.init();
		this.box.style.display = 'none';
	}
};
/**
 * Enpty:
 * Util.dialog.show({
 * 	title:title, msg:msg, [callback:callback]
 * });
 */
Util.dialog = {
	box : '',
	id : 'div-loadmask-m-dialog',
	tid : 'div-loadmask-m-dialog-t',
	mid : 'div-loadmask-m-dialog-m',
	templet : '<div id="{id}" class="div-mask div-mask-dialog"><div class="dialog">'
		+ '<div id={tid} class="dialog-title">{title}</div>'
		+ '<div id={mid} class="dialog-msg">{msg}</div>'
		+ '<div class="dialog-button">'
			+ '<button onclick="Util.dialog.event(\'yes\');">确定</button>&nbsp;&nbsp;<button onclick="Util.dialog.event(\'cancel\');">取消</button>'
		+ '</div>'
		+ '</div>'
		+ '</div>',
	init : function(c){
		if(!this.box){
			document.body.insertAdjacentHTML('afterBegin', this.templet.format({
				id: Util.dialog.id,
				tid: Util.dialog.tid,
				mid: Util.dialog.mid
			}));
			this.box = Util.getDom(this.id);
		}
		Util.getDom(Util.dialog.tid).innerHTML = typeof c.title == 'string' ? c.title : '温馨提示';
		Util.getDom(Util.dialog.mid).innerHTML = typeof c.msg == 'string' ? c.msg : '';
	},
	event : function(btn){
		this.hide();
		if(typeof this.defineConfig.callback == 'function'){
			this.defineConfig.callback(btn, this.defineConfig);
		}
	},
	show : function(c){
		this.defineConfig = null;
		this.defineConfig = c ? c : {};
		this.init(this.defineConfig);
		this.box.style.display = 'block';
		var view = this.box.firstChild;
		view.style.marginTop = parseInt(view.offsetHeight / 2 * -1) + 'px';
	},
	hide : function(){
		if(this.box){ this.box.style.display = 'none'; }
	}
};
document.addEventListener('WeixinJSBridgeReady', function onBridgeReady() {
//	WeixinJSBridge.call('hideToolbar');
});
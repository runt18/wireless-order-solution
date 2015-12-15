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
	mp : { 
		oid : 0, 
		fid : 0,
		e : 0,
		params : null
	},
	getDom : function(id){ 
		return document.getElementById(id); 
	},
	parseUrl : function(url){
	    var a = document.createElement('a');
	    a.href = url;
	    return {
	        source: url,
	        protocol: a.protocol.replace(':', ''),
	        host: a.hostname,
	        port: a.port,
	        query: a.search,
	        params: (function () {
	            var ret = {},
	            seg = a.search.replace(/^\?/, '').split('&'),
	            len = seg.length, i = 0, s;
	            for (; i < len; i++) {
	                if (!seg[i]) { 
	                	continue; 
	                }
	                s = seg[i].split('=');
	                ret[s[0]] = s[1];
	            }
	            return ret;
	 
	        })(),
	        file: (a.pathname.match(/\/([^\/?#]+)$/i) || [, ''])[1],
	        hash: a.hash.replace('#', ''),
	        path: a.pathname.replace(/^([^\/])/, '/$1'),
	        relative: (a.href.match(/tps?:\/\/[^\/]+(.+)/) || [, ''])[1],
	        segments: a.pathname.replace(/^\\/, '').split('/')
	    };
	},
	initParams : function(){
		var requestUrl = this.parseUrl(location.href);
		this.mp.oid = requestUrl.params.m;
		this.mp.fid = requestUrl.params.r;
		this.mp.extra = requestUrl.params.e;
		this.mp.params = requestUrl.params;
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
	jump : function(page, extra){
		var paramFormat = 'm={0}&r={1}&time={2}' + (extra ? '&e=' + extra : '');
		if(page.indexOf('?') > 0){
			window.location.href = page + '&' + paramFormat.format(this.mp.oid, this.mp.fid, new Date().getTime());
		}else{
			window.location.href = page + '?' + paramFormat.format(this.mp.oid, this.mp.fid, new Date().getTime());
		}
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
 * js原生ajax
 */

/*
* Name: xhr,AJAX封装函数
* Description: 一个ajax调用封装类,仿jquery的ajax调用方式
*/
/*var xhr = function () {
    var 
    ajax = function  () {
        return ('XMLHttpRequest' in window) ? function  () {
                return new XMLHttpRequest();
            } : function  () {
            return new ActiveXObject("Microsoft.XMLHTTP");
        }
    }(),
    formatData= function (fd) {
        var res = '';
        for(var f in fd) {
            res += f+'='+fd[f]+'&';
        }
        return res.slice(0,-1);
    },
    AJAX = function(ops) {
        var     
        root = this,
        req = ajax();

        root.url = ops.url;
        root.type = ops.type || 'responseText';
        root.method = ops.method || 'GET';
        root.async = ops.async || true;     
        root.data = ops.data || {};
        root.complete = ops.complete || function  () {};
        root.success = ops.success || function(){};
        root.error =  ops.error || function (s) { alert(root.url+'->status:'+s+'error!')};
        root.abort = req.abort;
        root.setData = function  (data) {
            for(var d in data) {
                root.data[d] = data[d];
            }
        }
        root.send = function  () {
            var datastring = formatData(root.data),
            sendstring,get = false,
            async = root.async,
            complete = root.complete,
            method = root.method,
            type=root.type;
            if(method === 'GET') {
                root.url+='?'+datastring;
                get = true;
            }
            req.open(method,root.url,async);
            if(!get) {
                req.setRequestHeader("Content-type","application/x-www-form-urlencoded");
                sendstring = datastring;
            }      

            //在send之前重置onreadystatechange方法,否则会出现新的同步请求会执行两次成功回调(chrome等在同步请求时也会执行onreadystatechange)
            req.onreadystatechange = async ? function  () {
                // console.log('async true');
                if (req.readyState ==4){
                    complete();
                    if(req.status == 200) {
                        root.success(req[type]);
                    } else {
                        root.error(req.status);
                    }                   
                }
            } : null;
            req.send(sendstring);
            if(!async) {
                //console.log('async false');
                complete();
                root.success(req[type]);
            }
        }
        root.url && root.send();        
    };
    return function(ops) {return new AJAX(ops);}    
}();



*/






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
//	btn : {yesno : '<button onclick="Util.dialog.event(\'yes\');">确定</button>&nbsp;&nbsp;<button onclick="Util.dialog.event(\'cancel\');">取消</button>', yes : '<button onclick="Util.dialog.event(\'yes\');">确定</button>'},
	templet : '<div id="{id}" class="div-mask div-mask-dialog"><div class="dialog">'
		+ '<div id={tid} class="dialog-title">{title}</div>'
		+ '<div id={mid} class="dialog-msg">{msg}</div>'
		+ '<div class="dialog-button">'
			+  '<button onclick="Util.dialog.event(\'yes\');">{leftText}</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<button id="dialog_cancel" onclick="Util.dialog.event(\'cancel\');">取消</button>'
		+ '</div>'
		+ '</div>'
		+ '</div>',
	init : function(c){
		if(!this.box){
			document.body.insertAdjacentHTML('afterBegin', this.templet.format({
				id: Util.dialog.id,
				tid: Util.dialog.tid,
				mid: Util.dialog.mid,
				leftText : c.leftText ? c.leftText : '确定'
			}));
			this.box = Util.getDom(this.id);
		}
		Util.getDom(Util.dialog.tid).innerHTML = typeof c.title == 'string' ? c.title : '温馨提示';
		Util.getDom(Util.dialog.mid).innerHTML = typeof c.msg == 'string' ? c.msg : '';
		if(c.dialogInit){
			c.dialogInit(this.box.firstChild);
		}
		
		if(c.btn && c.btn == 'yes'){
			$('#dialog_cancel').hide();
		}else{
			$('#dialog_cancel').show();
		}
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
		if(c.top){
			view.style.marginTop = parseInt(c.top) + 'px';
		}else{
			view.style.marginTop = parseInt(view.offsetHeight / 2 * -1) + 'px';
		}
		
		$('#div-loadmask-m-dialog').height(document.body.clientHeight + 15);
	}, 
	hide : function(){
		if(this.box){ this.box.style.display = 'none'; }
	}
};

function checkDot(c)
{
	var r= /^[+-]?[1-9]?[0-9]*\.[0-9]*$/;
	return r.test(c);
}

//document.addEventListener('WeixinJSBridgeReady', function onBridgeReady() {
////	WeixinJSBridge.call('hideToolbar');
//});

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



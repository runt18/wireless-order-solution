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
String.prototype.trim = function(){
	return this.replace(/(^\s*)|(\s*$)/g, ""); 
};
String.prototype.isEmpty = function(){
	return this.trim().isEmpty();
};
var Util = {
	mp : { oid : 0, fid : 0 },
	getDom : function(id){ return document.getElementById(id); },
	hparam : [],
	initParams : function(){
		var str = location.href;
		var temp = str.indexOf("?");
		str = str.substr(temp + 1);
		this.hparam = str.split("&");
		this.mp.oid = Util.getParam('m');
		this.mp.fid = Util.getParam('r');
	},
	getParam : function(key){
		var temp;
		for (var i = 0; i < this.hparam.length; i++) {
			temp = this.hparam[i].split("=");
			if (temp.length > 0 && temp[0].trim() == key) {
				return temp[1].trim();
			}
		}
	},
	lineTD : function(line, t){
		var sc='m-b-line-show', hc="m-b-line-hide";
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
	html : function(addr, cb){
		$.ajax({
			url : addr + 'mbar.html',
			success : function(html){
				if(typeof cb == 'function'){ cb(html); }
			}
		});
	}
};
Util.initParams();
document.addEventListener('WeixinJSBridgeReady', function onBridgeReady() {
	//WeixinJSBridge.call('hideToolbar');
});
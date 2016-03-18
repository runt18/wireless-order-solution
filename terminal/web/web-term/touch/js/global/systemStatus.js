(function(){
	var systemStatus = {
		POS : {val : 1, desc : 'pos'},
		TOUCH : { val : 2, desc : 'touch'},
		TRY : {val : 3, desc : 'try'},
		FASTFOOD : {val : 4, desc : 'fastFood'}
	}
	
	//获得url内的参数
	function parseUrl(url){
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
	};
	
	//1:pos端	2:touch		3:试用 
	var status = parseInt(parseUrl(window.location.href).params.status);
	
	WirelessOrder.systemStatus = {
		isPos : function(){//stauts=1
			return status === systemStatus.POS.val;
		},
		isTouch : function(){//stauts=2
			return status === systemStatus.TOUCH.val;
		},
		isTry : function(){//stauts=3
			return status === systemStatus.TRY.val;
		},
		isFastFood : function(){//stauts=4
			return status === systemStatus.FASTFOOD.val;
		},
		val : status
	}
	
	
})();

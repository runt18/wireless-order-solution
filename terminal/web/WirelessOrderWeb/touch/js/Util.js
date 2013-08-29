// 工具包
var Util = {};
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
		displayMsg : typeof c.displayMsg != 'undefined' ? c.displayMsg : '共{0}项, 每{1}/{2}页, 每页{3}项',
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
			this.showMsg();
			//
			var ch = this.dom.clientHeight, cw = this.dom.clientWidth;
			this.limit = parseInt((ch / (70 + 5 + 3 * 2))) * parseInt((cw / (90 + 5 + 3 * 2)));
			//
			this.data = typeof ic.data == 'undefined' ? [] : ic.data;
			this.length = this.data.length;
		},
		initContent : function(c){
			this.pageData = [];
			this.clearContent();
			if(!this.isEmpty()){
				var html = '';
				var start = this.start, limit = this.start > this.data.length && this.start + this.limit > this.data.length ? this.start + this.limit - this.data.length : this.limit;
				var temp = null;
				for(var i = 0; i < limit; i++){
					temp = this.data[start+i];
					this.pageData.push(temp);
					if(temp != null){
						html += this.templet({
							dataIndex : i,
							data : temp
						});				
					}
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
		data : c.data
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
/**
 * 
 * @param c
 */
Util.dialongDisplay = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	var el = $('#'+c.renderTo), lm = $('div[for='+c.renderTo+']');
	if(!el){return;}
	if(lm.length <= 0){
		el.before('<div for="'+c.renderTo+'" style="opacity:0; position: absolute; width: 100%; height: 100%; background: #DDD;"></div>');
		lm = $('div[for='+c.renderTo+']');
	}
	if($.trim(c.type) == 'show'){
		el.css({
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
		el.addClass('dialong-show');
		lm.addClass('dialong-lm-show');
	}else if($.trim(c.type) == 'hide'){
		el.addClass('dialong-hide');
		lm.addClass('dialong-lm-hide');
	}
};
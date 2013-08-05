Ext.onReady(function() {
	Ext.BLANK_IMAGE_URL = "../../extjs/resources/images/default/s.gif";
	Ext.QuickTips.init();
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
});
Ext.override(Ext.tree.TreeNodeUI, {
	onDblClick : function(e){
		e.preventDefault();
		if(this.disabled){
			return;
		}
		if(this.checkbox){
			this.toggleCheck();
		}
		if(!this.animating && this.node.hasChildNodes()){
			var isExpand = this.node.ownerTree.doubleClickExpand;
			if(isExpand){
				this.node.toggle();
			};
		}
		this.fireEvent("dblclick", this.node, e);
	}    
});
// GridPanel默认分页条数
var GRID_PADDING_LIMIT_10 = 10;
var GRID_PADDING_LIMIT_20 = 20;
var GRID_PADDING_LIMIT_30 = 30;
var GRID_PADDING_LIMIT_50 = 50;
// 操作类型
Ext.ux.otype = {
	'insert' : 'INSERT', 'INSERT' : 'INSERT',
	'update' : 'UPDATE', 'UPDATE' : 'UPDATE',
	'select' : 'SELECT', 'SELECT' : 'SELECT',
	'delete' : 'DELETE', 'DELETE' : 'DELETE',
	'set' : 'SET', 'SET' : 'SET',
	'get' : 'GET', 'GET' : 'GET'
};
//从url获取当前桌信息
function URLParaQuery() {
	var name, value, i;
	var str = location.href;
	var num = str.indexOf("?");
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
//修正日期控件在IE8显示不完全的问题
Ext.override(Ext.menu.Menu, {
	autoWidth : function() {
		this.width += "px";
	}
});
// 表格中的checkbox
Ext.grid.CheckColumn = function(config) {
	Ext.apply(this, config);
	if (!this.id) {
		this.id = Ext.id();
	}
	this.renderer = this.renderer.createDelegate(this);
};

Ext.grid.CheckColumn.prototype = {
	init : function(grid) {
		this.grid = grid;
		this.grid.on('render', function(){
			var view = this.grid.getView();
			view.mainBody.on('mousedown', this.onMouseDown, this);
		}, this);
	},

	onMouseDown : function(e, t) {
		if (t.id == this.id) {
			e.stopEvent();
			var index = this.grid.getView().findRowIndex(t);// 行号
			var cindex = this.grid.getView().findCellIndex(t);// 列好
			var record = this.grid.store.getAt(index);// 行记录
			var field = this.grid.colModel.getDataIndex(cindex);// 列名
			var value = !record.data[this.dataIndex];// 点击后，获得当前checkbox值的相反值
			record.set(this.dataIndex, value);// 设定checkbox被选择时候的值
			// 事件的参数
			var e = {
				grid : this.grid,
				record : record,
				field : field,
				originalValue : record.data[this.dataIndex],
				value : !record.data[this.dataIndex],
				row : index,
				column : cindex
			};

			// <SPAN style="COLOR: #ff0000">afterEdit事件</SPAN>
			this.grid.fireEvent("afteredit", e); // 申请事件，参数

		}
	},

	renderer : function(v, p, record) {
		p.css += ' x-grid3-check-col-td';
		return '<div id="' + this.id + '" class="x-grid3-check-col'
				+ (v ? '-on' : '') + '">&#160;</div>';
	}
};

//获取操作人姓名, 此函数要求页面上有operatorName,restaurantID全局变量；有id为optName的div
function getOperatorName(pin, actionPath) {
	Ext.Ajax.request({
		url : actionPath + "QueryStaff.do",
		params : {
			"restaurantID" : restaurantID,
			"type" : 0,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var staffData = [];
			var operatorName = "";
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (resultJSON.msg == "normal") {
					staffData = rootData;

					for ( var i = 0; i < staffData.length; i++) {
						// find the name
						if (staffData[i].staffID == pin) {
							operatorName = staffData[i].staffName;
						}
					}
					// update the page
					document.getElementById("optName").innerHTML = operatorName;
				} else {
					Ext.MessageBox.show({
						msg : resultJSON.msg,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			}
		},
		failure : function(response, options) {
		}
	});
};
/**
 * 初始化页面布局
 * 
 * @param west 
 * 	viewport layout for west region
 * @param center
 * 	viewport layout for center region
 * @param east
 * 	viewport layout for east region
 */
function initMainView(west, center, east){
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
	    	bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		(west != null && typeof(west) != 'undefined' ? west : {}),
		(center != null && typeof(center) != 'undefined'  ? center : {}),
		(east != null && typeof(east) != 'undefined'  ? east : {}),
		{
			region : 'south',
			height : 30,
			frame : true,
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
}
/**
 * 绑定跳转地址
 * 
 * @param id
 * @param sbg
 * @param bg
 * @param href
 * 	typeof: string, function
 * @returns {active}
 * 	if the activity is not empty, return to their own
 */
function bindActiveEvent(id, sbg, bg, href){
	var active = document.getElementById(id);
	if(active!=null){
		active.onmouseover = function(){
			this.style.background = sbg;
		};
		active.onmouseout = function(){
			this.style.background = bg;
		};
		active.onclick = function(){
			if(typeof href == 'string'){
				location.href = href;
			}else if(typeof href == 'function'){
				href();
			}
		};
	}
	return active;
}

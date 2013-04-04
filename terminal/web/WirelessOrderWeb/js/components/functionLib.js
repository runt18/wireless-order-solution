// GridPanel默认分页条数
var GRID_PADDING_LIMIT_10 = 10;
var GRID_PADDING_LIMIT_20 = 20;
var GRID_PADDING_LIMIT_50 = 50;

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

// 获取操作人姓名
// 此函数要求页面上有operatorName,restaurantID全局变量；有id为optName的div
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
				if (rootData[0].message == "normal") {
					staffData = rootData.slice(0);

					for ( var i = 0; i < staffData.length; i++) {
						// find the name
						if (staffData[i].pin == pin) {
							operatorName = staffData[i].staffName;
						}
					}
					// update the page
					document.getElementById("optName").innerHTML = operatorName;
				} else {
					Ext.MessageBox.show({
						msg : rootData[0].message,
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

// 修正日期控件在IE8显示不完全的问题
Ext.override(Ext.menu.Menu, {
	autoWidth : function() {
		this.width += "px";
	}
});
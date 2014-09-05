Ext.onReady(function() {
	Ext.BLANK_IMAGE_URL = "../../extjs/resources/images/default/s.gif";
	Ext.form.Field.prototype.msgTarget = 'side';  
	Ext.QuickTips.init();
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	
	Ext.Ajax.timeout = 120000;
});

/**
 * Ext.Ajax判断session超时后跳转
 */
Ext.Ajax.on('requestcomplete',checkUserSessionStatus, this);     
function checkUserSessionStatus(conn,response,options){     
   //Ext重新封装了response对象
    if(response.getResponseHeader && response.getResponseHeader("session_status")){ 
		var interval = 3;
		var action = '<br>&nbsp;&nbsp;&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转';
		Ext.MessageBox.show({
			title : '提示',
			msg : '操作已超时, 请重新登陆' + action,
			buttons : Ext.Msg.OK,
			icon : Ext.MessageBox.WARNING,
			closable : false,
			fn : function(btn){
				if(btn == 'ok'){
					location.href = response.getResponseHeader("root_path") + '/pages/Login.html';
				}
			}
		});

		new Ext.util.TaskRunner().start({
			run: function(){
				if(interval < 1){
					location.href = response.getResponseHeader("root_path") + '/pages/Login.html';								
				}
				Ext.getDom('returnInterval').innerHTML = interval;
				interval--;
			},
			interval : 1000
		});  
    }     
} 

Ext.override(Ext.tree.TreeEventModel, {
	delegateClick : function(e, t){
        if(!this.beforeEvent(e)){
            return;
        }

        if(e.getTarget('input[type=checkbox]', 1)){
            this.onCheckboxClick(e, this.getNode(e));
        }
        else if(e.getTarget('.x-tree-ec-icon', 1)){
            this.onIconClick(e, this.getNode(e));
        }
        else if(this.getNodeTarget(e)){
        	if(this.tree.getSelectionModel().selNode){
        		if(this.tree.getSelectionModel().selNode.ui != "null"){
        			this.tree.getSelectionModel().selNode.ui.onSelectedChange(false);
        		}
        	}
			this.tree.getSelectionModel().selNode = this.getNode(e);
            this.onNodeClick(e, this.getNode(e));
            
        }
    }
});
/*Ext.override(Ext.tree.TreeEventModel, {
	onNodeClick : function(e, node) {
		this.tree.getSelectionModel().select(node);
		node.ui.onClick(e);
	}
});*/
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

Ext.override(Ext.grid.CellSelectionModel, {
    onEditorKey : function(field, e) {
        var smodel = this;
        var k = e.getKey(), newCell=null, g = smodel.grid, ed = g.activeEditor;
        switch(k){
            case e.TAB:
                 e.stopEvent();
                 ed.completeEdit();
                 if (e.shiftKey) {
                     newCell = g.walkCells(ed.row, ed.col-1, -1, smodel.acceptsNav, smodel);
                 } else {
                     newCell = g.walkCells(ed.row, ed.col+1, 1, smodel.acceptsNav, smodel);
                 }
                 if (ed.col == 1) {
                     if (e.shiftKey) {
                         newCell = g.walkCells(ed.row, ed.col+1, -1, smodel.acceptsNav, smodel);
                     } else {
                         newCell = g.walkCells(ed.row, ed.col+1, 1, smodel.acceptsNav, smodel);
                     }
                 }
                break;
            case e.UP:
                 e.stopEvent();
                 ed.completeEdit();
                 newCell = g.walkCells(ed.row-1, ed.col, -1, smodel.acceptsNav, smodel);
                break;
            case e.DOWN:
                 e.stopEvent();
                 ed.completeEdit();
                 g.startEditing(ed.row+1, ed.col);
                 e.stopEvent(); 
                break;
            case e.LEFT:
                 e.stopEvent();
                 ed.completeEdit();
                 newCell = g.walkCells(ed.row, ed.col-1, -1, smodel.acceptsNav, smodel);
                 break;
             case e.RIGHT:
                 e.stopEvent();
                 ed.completeEdit();
                 newCell = g.walkCells(ed.row, ed.col+1, 1, smodel.acceptsNav, smodel);
                   break;
        }
       if (newCell) {
            g.startEditing(newCell[0], newCell[1]);
       }
     }
});
// GridPanel默认分页条数
var GRID_PADDING_LIMIT_10 = 10;
var GRID_PADDING_LIMIT_20 = 20;
var GRID_PADDING_LIMIT_25 = 25;
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
	var name, value, i, key = 0;
	var str = location.href;
	var num = str.indexOf("?");
	if(num > 0){
		str = str.substr(num + 1);
		$.ajax({
            cache: false,
            async: false,   
            dataType: 'json', 
            type: 'post',
            url: '../../QueryDynamicKey.do',
            success: function (jr){ 
				key = jr;
            }
        });
        str = strDecode(str, key);
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
function getOperatorName(actionPath, callback) {
	Ext.Ajax.request({
		url : actionPath + "QueryStaff.do",
		params : {
			"type" : 0,
			"isPaging" : false,
			"isName" : true
		},
		success : function(response, options) {
			var jr = Ext.util.JSON.decode(response.responseText);
			if(jr.success){
				if(jr.other.staff != null){
					document.getElementById("optName").innerHTML = jr.other.staff.staffName;
					document.getElementById("restaurantName").innerHTML = jr.other.restaurant.name;
					
					if(typeof callback == 'function'){
						callback(jr.other.staff);
					}
				}
			}else{
				jr.icon = Ext.MessageBox.OK;
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) {
		}
	});
	
	
};
/**
 * 验证员工权限
 * @param actionPath 	相对路径
 * @param code 			权限码
 * @param callback 		验证成功后执行的操作
 */
function verifyStaff(actionPath, code, callback){
	Ext.Ajax.request({
		url : actionPath + "VerifyStaff.do",
		params : {
			restaurantID : restaurantID,
			code : code
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(typeof callback == 'function'){
				callback(jr);
			}else if(typeof callback == 'string'){
				if(jr.success){
					location.href = callback;
				}else{
					jr['icon'] = Ext.MessageBox.WARNING;
					Ext.ux.showMsg(jr);
				}
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
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
			html : '<h4 id="restaurantName" style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
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

function promotion_showFloatOptionForMult(node, mult){
		for (var j = 0; j < mult.length; j++) {
			if(node.attributes.status == mult[j].status){
				if(!$("#div_floatBar").status || $("#div_floatBar").status != node.attributes.status){
					$('#div_floatBar').html("");
					for (var i = 0; i < mult[j].option.length; i++) {
						if(i > 0){
							$("#div_floatBar").append('|&nbsp;');
						}
						$("#div_floatBar").append('<a href="javascript:void(0)" onclick='+mult[j].option[i].fn+'>'+ mult[j].option[i].name +'</a>&nbsp;');
					}	
					$("#div_floatBar").status = node.attributes.status; 
					
					break;
				}

			}
		}
}

function showFloatOption(obj_b){
	//记录节点的位置和鼠标位置
	var nodex=0;
	var offset, liOffset;
	//把bar加到tree上
	$("#"+obj_b.treeId).mouseover(function(){
		//生成浮动bar
		
		if(!obj_b.mult){//有多种浮动框情况时不执行单一浮动框
			if($("#div_floatBar").find("a").length == 0){
				for (var i = 0; i < obj_b.option.length; i++) {
					if(i > 0){
						$("#div_floatBar").append('|&nbsp;');
					}
					$("#div_floatBar").append('<a href="javascript:void(0)" onclick='+obj_b.option[i].fn+'>'+ obj_b.option[i].name +'</a>&nbsp;');
				}
			}		
		}


		liOffset = $("#"+obj_b.treeId).find("ul").offset();
		nodey = liOffset.top;
		barY = ($("#"+obj_b.treeId).find("ul").height() + nodey);
		
		$("#"+obj_b.treeId).find(".x-tree-node-leaf, .floatBarStyle").mouseover(function(){
			floatBarNodeId = $(this).attr("ext:tree-node-id");
			offset = $(this).find("a").offset();
			nodex = offset.left - 18;
			barX = (offset.left + $(this).find("a").width() + 100);
			if(obj_b.mult){
				promotion_showFloatOptionForMult(Ext.getCmp(obj_b.treeId).getNodeById(floatBarNodeId), obj_b.mult);
			}
			
			if($('#div_floatBar').html()){
				$('#div_floatBar').css({left:offset.left+$(this).find("a").width(), top:(Ext.isIE?(offset.top-12):(offset.top-2))});
				$('#div_floatBar').show();			
			}else{
				$('#div_floatBar').hide();		
			}

			
			
			
		});
		
		$(document).mousemove(function(event){
			if(event.clientX > barX || event.clientX < nodex || event.clientY <=nodey || event.clientY >barY){
				$('#div_floatBar').hide();
				$('#div_floatBar').html("");
				floatBarNodeId ="";
			}
		});

	});
}

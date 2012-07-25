
var cancelledFoodWin = null;
var cancelledFoodMune = null;
var cancelledFoodGrid = null;
var cancelledFoodQType = 0;
var cancelledFoodOType = 0;
var cancelledFoodDeptID = '';

cancelledFoodInit = function(){	
	
	if(!cancelledFoodMune){		
		var bfsmTop = new Ext.Panel({
			id : 'cancelledFoodQTL',
		    xtype : 'panel',	
		    border : false,
		    tbar : new Ext.Toolbar({		    	
		    	height : 26,
		    	items : [
		    	'->', {
			  	    xtype : 'radio',
			  	    name : 'queryType',
			  	    id : 'ridaoQueryTypeDept',
			  	    boxLabel : '部门',
			  	    width : 65,
			  	    inputValue : 0,
			  	    checked : true,
			  	    listeners : {
			  	    	
			  	   	}
			  	}, 
//			  	{
//			  	    xtype : 'radio',
//			  	    name : 'queryType',
//			  	    boxLabel : '原因',
//			  	    width : 65,
//			  	    inputValue : 0,
//			  	    listeners : {
//			  	    	check : function(e){
//			  	    		if(e.getValue()){
//			  	    			
//			  	    		}
//			  	    	}
//			  	   	}
//			  	}, 
			  	{
			  	    xtype : 'radio',
			  	    name : 'queryType',
			  	    id : 'ridaoQueryTypeDetail',
			  	    boxLabel : '明细',
			  	    width : 95,
			  	    inputValue : 0,
			  	    listeners : {
			  	    	
			  	   	}
			  	}]
		    }),
		    listeners : {
		    	
		    }
		});
		
		var bfsmTree = new Ext.tree.TreePanel({
			id : 'cancelledFoodDeptTree',
			border : false,
			rootVisible : true,
			height : 410,
			loader : new Ext.tree.TreeLoader({
				dataUrl : '../../QueryDeptTree.do?time='+new Date(),
				baseParams : {
					'restaurantID' : restaurantID
				}
			}),
			root : new Ext.tree.AsyncTreeNode({
				expanded : true,
				text : '全部菜品',
		        leaf : false,
		        deptID : '-1'
			}),
			listeners : {
				click : function(e){
					if(cancelledFoodQType == 1){
						
					}else if(cancelledFoodQType == 2){
						if(e.attributes.deptID == '' || parseInt(e.attributes.deptID) == -1){
							cancelledFoodDeptID = '';
						}else{
							cancelledFoodDeptID = e.attributes.deptID;
						}
						Ext.getDom('cancelledFoodShowType').innerHTML = e.text;
					}
				}
			}
		});
		
		cancelledFoodMune = new Ext.Panel({		
			region : 'west',
			width : 200,
			items : [bfsmTop, bfsmTree]
		});
	}
	
	if(!cancelledFoodGrid){
		
		var cancelledFoodTbar = new Ext.Toolbar({
			height : 26,
			items : [
			    {xtype:'tbtext', text:String.format(Ext.ux.txtFormat.typeName, 'cancelledFoodShowType', '----')}, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},	
				{xtype:'tbtext',text:'日期:'},
			    {
			    	xtype : 'datefield',
			    	format : 'Y-m-d',
					id : 'cancelledFoodBegDate',
					width : 100,
			    	readOnly : true,
			    	listeners : {
			    		blur : function(){
			    			Ext.ux.checkDuft(true, 'cancelledFoodBegDate', 'cancelledFoodEndDate');
			    		}
			    	}
			    }, 
			    { xtype : 'tbtext', text : '&nbsp;&nbsp;至:&nbsp;&nbsp;'},
			    {
			    	xtype : 'datefield',
			    	format : 'Y-m-d',
					id : 'cancelledFoodEndDate',
					width : 100,
			    	readOnly : true,
			    	listeners : {
			    		blur : function(){
			    			Ext.ux.checkDuft(false, 'cancelledFoodBegDate', 'cancelledFoodEndDate');
			    		}
			    	}
			    },
			    { xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'},
			    {
			    	xtype : 'radio',
			    	name : 'orderType',
			    	id : 'radioOederTypeCount',
			    	hideLabel : true,
			    	boxLabel : '按数量排序',
			    	checked : true,
			    	listeners : {
			    		check : function(e){
			    			if(e.getValue()){
			    				cancelledFoodOType = 0;
			    			}
			    		}
			    	}
			    },
			    { xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    {
			    	xtype : 'radio',
			    	name : 'orderType',
			    	id : 'radioOederTypePrice',
			    	hideLabel : true,
			    	boxLabel : '按金额排序',
			    	listeners : {
			    		check : function(e){
			    			if(e.getValue()){
			    				cancelledFoodOType = 1;
			    			}
			    		}
			    	}
			    },
			    '->',
			    {
			    	text : '搜索',
			    	iconCls : 'btn_search',
			    	handler : function(e){
			    		var bd = Ext.getCmp('cancelledFoodBegDate').getValue();
						var ed = Ext.getCmp('cancelledFoodEndDate').getValue();
						if(bd == '' && ed == ''){
							Ext.getCmp('cancelledFoodEndDate').setValue(new Date());
							Ext.ux.checkDuft(false, 'cancelledFoodBegDate', 'cancelledFoodEndDate');
						}else if(bd != '' && ed == ''){
							Ext.ux.checkDuft(true, 'cancelledFoodBegDate', 'cancelledFoodEndDate');
						}else if(bd == '' && ed != ''){
							Ext.ux.checkDuft(false, 'cancelledFoodBegDate', 'cancelledFoodEndDate');
						}
			    		
			    		var gs = cancelledFoodGrid.getStore();
			    		gs.baseParams['beginDate'] = Ext.getCmp('cancelledFoodBegDate').getRawValue();
						gs.baseParams['endDate'] = Ext.getCmp('cancelledFoodEndDate').getRawValue();
						gs.baseParams['otype'] = cancelledFoodOType;
						gs.baseParams['qtype'] = cancelledFoodQType;
						gs.baseParams['deptID'] = cancelledFoodDeptID;
			    		gs.removeAll();
						gs.load({params:{start:0,limit:15}});
						
						cancelledFoodSetColumn();
			    	}
			    }
			]
		});
		
		var cmData = [[true, false, false, true],
		              ['日期','orderDateFormat',130], ['菜名','foodName',130],
		               ['部门','deptName',80], ['账单号', 'orderID', 80],
		              ['单价','price',80,'right','Ext.ux.txtFormat.gridDou'],
		              ['退菜数量','count',90,'right','Ext.ux.txtFormat.gridDou'], 
		              ['退菜金额','totalPrice',90,'right','Ext.ux.txtFormat.gridDou'],		              
		              ['操作人','waiter',80], ['退菜原因','reason',165]];
		var url = '../../QueryCancelledFood.do?tiem='+new Date();
		var readerData = ['orderDateFormat','foodName','deptName','orderID','price','count','totalPrice','waiter','reason'];
		var baseParams = [['pin', pin], ['qtype', cancelledFoodQType], ['otype', cancelledFoodOType]];
		var pageSize = 15;
		var id = 'cancelledFood_grid';
		var title = '';
		var height = '';
		var width = '';
		var groupName = '';
		
		cancelledFoodGrid = createGridPanel(id,title,height,width,url,cmData,readerData,baseParams,pageSize,groupName,cancelledFoodTbar);
		cancelledFoodGrid.region = 'center';
		cancelledFoodGrid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = cancelledFoodGrid.getView().getRow(store.getCount()-1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				sumRow.style.color = 'green';
				for(var i = 0; i < cancelledFoodGrid.getColumnModel().getColumnCount(); i++){
					var sumRowCell = cancelledFoodGrid.getView().getCell(store.getCount()-1, i);
					sumRowCell.style.fontSize = '15px';
					sumRowCell.style.fontWeight = 'bold';
				}
				if(cancelledFoodQType == 0){
					cancelledFoodGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '';
					cancelledFoodGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '汇总';
				}else if(cancelledFoodQType == 2){
					cancelledFoodGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
					cancelledFoodGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '';
					cancelledFoodGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '';
					cancelledFoodGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '';
					cancelledFoodGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '';
				}
			}
		});
		
	}
};

cancelledFood = function(){
	
	cancelledFoodInit();
	
	if(!cancelledFoodWin){
	cancelledFoodWin = new Ext.Window({
		title : '退菜统计',
		layout : 'border',
		closeAction : 'hide',
		resizable : false,
		modal : true,
		closable : false,
		constrainHeader : true,
		draggable : false,
		width : 1200,
		height : 500,
		items : [cancelledFoodMune, cancelledFoodGrid],
		buttons : [
		    {
				text : '打印',
				disabled : true,
				handler : function(){
					
				}
			},
			{
				text : '退出',
				handler : function(){
					cancelledFoodWin.hide();
				}
			}
			],
			listeners : {
				show : function(){
					var dept = Ext.getDom('ridaoQueryTypeDept');
					var detail = Ext.getDom('ridaoQueryTypeDetail');
					
					dept.onclick = function(){
						cancelledFoodQType = 0;
						cancelledFoodDeptID = '';
						Ext.getDom('cancelledFoodShowType').innerHTML = '部门分类';
	  	    			Ext.getCmp('cancelledFoodDeptTree').setDisabled(true);    			
//	  	    			Ext.getCmp('radioOederTypeCount').setDisabled(true);
//	  	    			Ext.getCmp('radioOederTypePrice').setDisabled(true);
					};
					
					detail.onclick = function(){
						cancelledFoodQType = 2;
						cancelledFoodDeptID = '';
						Ext.getDom('cancelledFoodShowType').innerHTML = '全部菜品';
	  	    			Ext.getCmp('cancelledFoodDeptTree').setDisabled(false);
	  	    			Ext.getCmp('cancelledFoodDeptTree').root.select();
//	  	    			Ext.getCmp('radioOederTypeCount').setDisabled(false);
//	  	    			Ext.getCmp('radioOederTypePrice').setDisabled(false);
					};
					
					dept.click();
					
					Ext.getCmp('cancelledFoodBegDate').setValue();
					Ext.getCmp('cancelledFoodEndDate').setValue();
					Ext.getCmp('radioOederTypeCount').setValue(true);
					
					cancelledFoodGrid.getStore().removeAll();
					
					cancelledFoodSetColumn();
				}
			}
		});
	}
	
	cancelledFoodWin.show();
	
};

cancelledFoodSetColumn = function(){
	var grid = Ext.getCmp('cancelledFood_grid');
	var colHide = true;				
	if(cancelledFoodQType == 0){
		colHide = true;
	}else if(cancelledFoodQType == 2){
		colHide = false;
	}
	grid.getColumnModel().setHidden(1, colHide);
	grid.getColumnModel().setHidden(2, colHide);
	grid.getColumnModel().setHidden(4, colHide);
	grid.getColumnModel().setHidden(5, colHide);
	grid.getColumnModel().setHidden(8, colHide);
	grid.getColumnModel().setHidden(9, colHide);
	grid.getColumnModel().setColumnWidth(1, 130);
	grid.getColumnModel().setColumnWidth(2, 130);
	grid.getColumnModel().setColumnWidth(3, 80);
	grid.getColumnModel().setColumnWidth(4, 80);
	grid.getColumnModel().setColumnWidth(5, 80);
	grid.getColumnModel().setColumnWidth(6, 90);
	grid.getColumnModel().setColumnWidth(7, 90);
	grid.getColumnModel().setColumnWidth(8, 80);
	grid.getColumnModel().setColumnWidth(9, 165);
};



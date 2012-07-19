
var backFoodWin = null;
var backFoodMune = null;
var backFoodGrid = null;
var backFoodQType = 0;
var backFoodOType = 0;
var backFoodDeptID = '';

backFoodInit = function(){	
	
	if(!backFoodMune){		
		var bfsmTop = new Ext.Panel({
			id : 'backFoodQTL',
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
			id : 'backFoodDeptTree',
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
					if(backFoodQType == 1){
						
					}else if(backFoodQType == 2){
						if(e.attributes.deptID == '' || parseInt(e.attributes.deptID) == -1){
							backFoodDeptID = '';
						}else{
							backFoodDeptID = e.attributes.deptID;
						}
						Ext.getDom('backFoodShowType').innerHTML = e.text;
					}
				}
			}
		});
		
		backFoodMune = new Ext.Panel({		
			region : 'west',
			width : 200,
			items : [bfsmTop, bfsmTree]
		});
	}
	
	if(!backFoodGrid){
		
		var backFoodTbar = new Ext.Toolbar({
			height : 26,
			items : [
			    {xtype:'tbtext', text:String.format(Ext.ux.txtFormat.typeName, 'backFoodShowType', '----')}, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},	
				{xtype:'tbtext',text:'日期:'},
			    {
			    	xtype : 'datefield',
			    	format : 'Y-m-d',
					id : 'backFoodBegDate',
					width : 100,
			    	readOnly : true,
			    	listeners : {
			    		blur : function(){
			    			Ext.ux.checkDuft(true, 'backFoodBegDate', 'backFoodEndDate');
			    		}
			    	}
			    }, 
			    { xtype : 'tbtext', text : '&nbsp;&nbsp;至:&nbsp;&nbsp;'},
			    {
			    	xtype : 'datefield',
			    	format : 'Y-m-d',
					id : 'backFoodEndDate',
					width : 100,
			    	readOnly : true,
			    	listeners : {
			    		blur : function(){
			    			Ext.ux.checkDuft(false, 'backFoodBegDate', 'backFoodEndDate');
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
			    				backFoodOType = 0;
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
			    				backFoodOType = 1;
			    			}
			    		}
			    	}
			    },
			    '->',
			    {
			    	text : '搜索',
			    	iconCls : 'btn_search',
			    	handler : function(e){
			    		var bd = Ext.getCmp('backFoodBegDate').getValue();
						var ed = Ext.getCmp('backFoodEndDate').getValue();
						if(bd == '' && ed == ''){
							Ext.getCmp('backFoodEndDate').setValue(new Date());
							Ext.ux.checkDuft(false, 'backFoodBegDate', 'backFoodEndDate');
						}else if(bd != '' && ed == ''){
							Ext.ux.checkDuft(true, 'backFoodBegDate', 'backFoodEndDate');
						}else if(bd == '' && ed != ''){
							Ext.ux.checkDuft(false, 'backFoodBegDate', 'backFoodEndDate');
						}
			    		
			    		var gs = backFoodGrid.getStore();
			    		gs.baseParams['beginDate'] = Ext.getCmp('backFoodBegDate').getRawValue();
						gs.baseParams['endDate'] = Ext.getCmp('backFoodEndDate').getRawValue();
						gs.baseParams['otype'] = backFoodOType;
						gs.baseParams['qtype'] = backFoodQType;
						gs.baseParams['deptID'] = backFoodDeptID;
			    		gs.removeAll();
						gs.load({params:{start:0,limit:15}});
						
						backFoodSetColumn();
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
		var url = '../../QueryBackFood.do?tiem='+new Date();
		var readerData = ['orderDateFormat','foodName','deptName','orderID','price','count','totalPrice','waiter','reason'];
		var baseParams = [['pin', pin], ['qtype', backFoodQType], ['otype', backFoodOType]];
		var pageSize = 15;
		var id = 'backFood_grid';
		var title = '';
		var height = '';
		var width = '';
		var groupName = '';
		
		backFoodGrid = createGridPanel(id,title,height,width,url,cmData,readerData,baseParams,pageSize,groupName,backFoodTbar);
		backFoodGrid.region = 'center';
		backFoodGrid.getStore().on('load', function(store, records, options){
			if(store.getCount() > 0){
				var sumRow = backFoodGrid.getView().getRow(store.getCount()-1);	
				sumRow.style.backgroundColor = '#EEEEEE';			
				sumRow.style.color = 'green';
				for(var i = 0; i < backFoodGrid.getColumnModel().getColumnCount(); i++){
					var sumRowCell = backFoodGrid.getView().getCell(store.getCount()-1, i);
					sumRowCell.style.fontSize = '15px';
					sumRowCell.style.fontWeight = 'bold';
				}
				if(backFoodQType == 0){
					backFoodGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '';
					backFoodGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '汇总';
				}else if(backFoodQType == 2){
					backFoodGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
					backFoodGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '';
					backFoodGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '';
					backFoodGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '';
					backFoodGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '';
				}
			}
		});
		
	}
};

backFood = function(){
	
	backFoodInit();
	
	if(!backFoodWin){
	backFoodWin = new Ext.Window({
		title : '退菜汇总',
		layout : 'border',
		closeAction : 'hide',
		resizable : false,
		modal : true,
		closable : false,
		constrainHeader : true,
		draggable : false,
		width : 1200,
		height : 500,
		items : [backFoodMune, backFoodGrid],
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
					backFoodWin.hide();
				}
			}
			],
			listeners : {
				show : function(){
					var dept = Ext.getDom('ridaoQueryTypeDept');
					var detail = Ext.getDom('ridaoQueryTypeDetail');
					
					dept.onclick = function(){
						backFoodQType = 0;
						backFoodDeptID = '';
						Ext.getDom('backFoodShowType').innerHTML = '部门分类';
	  	    			Ext.getCmp('backFoodDeptTree').setDisabled(true);    			
//	  	    			Ext.getCmp('radioOederTypeCount').setDisabled(true);
//	  	    			Ext.getCmp('radioOederTypePrice').setDisabled(true);
					};
					
					detail.onclick = function(){
						backFoodQType = 2;
						backFoodDeptID = '';
						Ext.getDom('backFoodShowType').innerHTML = '全部菜品';
	  	    			Ext.getCmp('backFoodDeptTree').setDisabled(false);
	  	    			Ext.getCmp('backFoodDeptTree').root.select();
//	  	    			Ext.getCmp('radioOederTypeCount').setDisabled(false);
//	  	    			Ext.getCmp('radioOederTypePrice').setDisabled(false);
					};
					
					dept.click();
					
					Ext.getCmp('backFoodBegDate').setValue();
					Ext.getCmp('backFoodEndDate').setValue();
					Ext.getCmp('radioOederTypeCount').setValue(true);
					
					backFoodGrid.getStore().removeAll();
					
					backFoodSetColumn();
				}
			}
		});
	}
	
	backFoodWin.show();
	
};

backFoodSetColumn = function(){
	var grid = Ext.getCmp('backFood_grid');
	var colHide = true;				
	if(backFoodQType == 0){
		colHide = true;
	}else if(backFoodQType == 2){
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



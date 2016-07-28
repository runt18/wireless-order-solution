(function(){
	Ext.stockDistributionAction = {
		stockSubType : {
			//库单的类型
			values : {
				STOCK_IN : {val : 1, desc : "采购"},
				STOCK_IN_TRANSFER : {val : 2, desc : "领料"},
				SPILL : {val : 3, desc : "其他入库"},
				STOCK_OUT : {val : 4, desc : "退货"},
				STOCK_OUT_TRANSFER : {val : 5, desc : "退料"},
				DAMAGE : {val : 6, desc : "其他出库"},
				MORE : {val : 7, desc : "盘盈"},
				LESS : {val : 8, desc : "盘亏"},
				CONSUMPTION : {val : 9, desc : "消耗"},
				INIT : {val : 10, desc : "初始化"},
				DISTRIBUTION_SEND : {val : 11, desc : '配送发货'},
				DISTRIBUTION_RECEIVE : {val : 12, desc : '配送收货'},
				DISTRIBUTION_RETURN : {val : 13, desc : '配送退货'},
				DISTRIBUTION_RECOVERY : {val : 14, desc : '配送回收'},
				DISTRIBUTION_APPLY : {val : 15, desc : '配送申请'}
			},
			//使用值来获取类型
			valueOf : function(value){
				var type;
				for(var key in Ext.stockDistributionAction.stockSubType.values){
					if(Ext['stockDistributionAction']['stockSubType']['values'][key]['val'] == value){
						type = Ext['stockDistributionAction']['stockSubType']['values'][key];
					}
				}
				
				if(!type){
					console.log('配送类型输入的值找不到');
				}
				
				return type;
			}
		},
		cateType : {
			GOOD : {val : 1, desc : '商品'},
			MATERIAL : {val : 2, desc : '原料'}
		},
		actionType : {
			INSERT :　{dataSource : 'insert', desc : '添加'},
			UPDATE : {dataSource : 'update', desc : '修改'},
			DELETE : {dataSource : 'delete', desc : '删除'},
			GETBYCOND : {dataSource : 'getByCond', desc : '查询'},
			AUDIT : {dataSource : 'audit', desc : '审核'},
			REAUDIT : {dataSource : 'reAudit', desc : '反审核'}
		},
		stockActionStatus : {
			SINGLE : {val : 1, desc : "未绑定"},
			MARRIED : {val : 2, desc : "已绑定"}
		},
		stockType : {
			STOCK_IN : {val : 1, desc : "入库"},
			STOCK_OUT : {val : 2, desc : "出库"},
			STOCK_APPLY: {val : 3, desc : "申请"}
		},
		newInstance : function(param){
			return new init(param);
		}
	};
	
	//js缓存 只要js没关闭都保存   静态私有变量
	var cookieData;
	
	if(!cookieData){
		cookieData = {};
		Ext.Ajax.request({
			url : '../../QueryMaterial.do',
			params : {
				dataSource : 'normal',
				restaurantID : restaurantID,
				cateType : Ext.stockDistributionAction.cateType.GOOD.val
			},
			success : function(data){
				var jr = Ext.decode(data.responseText);
				cookieData.good = jr.root;
			},
			failure : function(res){
				Ext.ux.example('错误提示', Ext.decode(res.responseText).msg);
			}
		});
		
		Ext.Ajax.request({
			url : '../../QueryMaterial.do',
			params : {
				dataSource : 'normal',
				restaurantID : restaurantID,
				cateType : Ext.stockDistributionAction.cateType.MATERIAL.val
			},
			success : function(data){
				var jr = Ext.decode(data.responseText);
				cookieData.material = jr.root;
			},
			failure : function(res){
				Ext.ux.example('错误提示', Ext.decode(res.responseText).msg);
			}
		});
	}
	
	function init(param){
		var _param = param || {
			subType : null,					//业务类型
			cateType : null,				//货品类型
			oriId : null,					//原始单号
			oriDate : null,					//货单时间
			comment : null,					//备注
			appover : null,					//审核人
			appoverDate : null,				//审核时间
			operator : null,				//操作者
			operateDate : null,				//操作时间
			associateId : null,				//关联单号
			callback : null,				//确认回调
			isAssociate : false,			//是否使用关联按钮
			stockActionId : null,			//库单号！注意库单号和配送单号不同  【单店单号】
			stockInRestaurant : null,		//入货门店
			stockInRestaurantText : null,	//入货门店名称
			stockOutRestaurant : null,		//发货门店
			stockOutRestaurantText : null,	//发货门店名称
			details : null,					//货品明细
			id : null,						//配送单号
			actualPrice : null,				//实际价格
			isOnlyShow : null,				//禁止库单修改标志位
			stockType : null,				//货单类型
			checkWithOutMsg : false,		//防止查看账单的错误提示
			isOnlyRestaurant : false,		//单店模式标志位
			deptOut : null,					//出库仓
			deptIn : null,					//入库仓
			supplier : null,				//供应商
			expandBtns : null				//扩展按钮
		};
		var _self = this;
		
		if(_param.subType && typeof _param.subType != 'number'){
			console.log('Ext.stockDistributionAction sub_type 配置错误');
		}
		
		var radioGroup = new Ext.form.RadioGroup({
			fieldLabel : '货品类别',
			columns : 2,
			columnWidth : 0.8,
			style : {
				'text-align' : 'center',
				'padding-left' : '14%',
				'padding-top' : '10px'
			},
			items : [{boxLabel : '商品', name : 'cateType', inputValue : '1'},
					 {boxLabel : '原料', name : 'cateType', inputValue : '2'}]
		});
		
		var cateTypeWinId =  'cateTypeWin_' + new Date().getTime();
		var cateTypeWin;
		cateTypeWin = new Ext.Window({
			height : 100,
			width : 300,
			title : _param.subType ? Ext.stockDistributionAction.stockSubType.valueOf(_param.subType).desc : '配送申请',
			resizable : false,
			modal : true,
			closable : false,
			layout : 'column',
			items : [radioGroup],
			bbar : ['->', {
				text : '确认',
				iconCls : 'btn_save',
				handler : function(){
					if(radioGroup.getValue() == null){
						Ext.example.msg('错误提示', '必须选择货品类型');
					}else{
						cateTypeWin.hide();
						_param.cateType = radioGroup.getValue().inputValue;
						container.show();
						beforeShow();
					}
				}
			}, {
				text : '取消',
				iconCls : 'btn_close',
				handler : function(){
					_self.close();
				}
			}]
		});
		
		var deptIn
		deptIn = new Ext.form.ComboBox({
			width : 110,
			readOnly : false,
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : '入库仓不能为空',
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			listeners : {
				render : function(){
				}				
			}
		});
		
		var deptOut;
		deptOut = new Ext.form.ComboBox({
			width : 110,
			readOnly : false,
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : '出库仓不能为空',
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			listeners : {
				render : function(){
				}				
			}
		});
		
		var supplierCombo;
		supplierCombo = new Ext.form.ComboBox({
			width : 110,
			readOnly : false,
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : '供应商不能为空',
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			listeners : {
				render : function(){
				}				
			}
		});
		
		var stockInRestaurant;
		stockInRestaurant = new Ext.form.ComboBox({
			width : 110,
			readOnly : false,
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : '收货门店不能为空',
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			listeners : {
				render : function(){
				}				
			}
		});
		
		var stockOutRestaurant;
		stockOutRestaurant = new Ext.form.ComboBox({
			width : 120,
			readOnly : false,
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : '出货门店不能为空',
			store : new Ext.data.SimpleStore({
				fields : ['id', 'name']
			}),
			listeners : {
				render : function(){
				}				
			}
		});
		
		var oriDate;
		oriDate = new Ext.form.DateField({
			width : 120,
			format : 'Y-m-d',
			value : new Date()	
		});
		
		var oriId;
		oriId = new Ext.form.TextField({
			width : 120
		});
		
		var comment;
		comment = new Ext.form.TextField({
			width : 730
		});
		
		var appover;
		appover = new Ext.form.TextField({
			width : 120,
			disabled : true
		});
		
		var appoverDate;
		appoverDate = new Ext.form.TextField({
			width : 120,
			disabled : true
		});
		
		var operator;
		operator = new Ext.form.TextField({
			width : 120,
			disabled : true
		});
		
		var operateDate;
		operateDate = new Ext.form.TextField({
			width : 120,
			disabled : true
		});
		
		var associateId;
		associateId = new Ext.form.TextField({
			width : 120,
			disabled : true
		});
		
		var cateId;
		cateId = new Ext.form.ComboBox({
			width : 120,
			hidden : true
		});
		
		var material;
		material = new Ext.form.ComboBox({
			xtype : 'combo',
			forceSelection : true,
			listWidth : 250,
			loadMask : {
				msg : '数据加载中,请稍后....'
			},
			height : 200,
			maxHeight : 300,
			store : new Ext.data.JsonStore({
				fields : MaterialRecord.getKeys()
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			tpl:'<tpl for=".">' 
				+ '<div class="x-combo-list-item" style="height:18px;">'
				+ '{id} -- {name} -- {pinyin}'
				+ '</div>'
				+ '</tpl>',
			listeners : {
				beforequery : function(e){
					var combo = e.combo; 
					if(!e.forceAll){ 
						var value = e.query; 
						combo.store.filterBy(function(record,id){
							return record.get('name').indexOf(value) != -1 
									|| (record.get('id')+'').indexOf(value) != -1 
									|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
						}); 
						combo.expand(); 
						combo.select(0, true);
						return false; 
					}
				},
				select : function(store,data,index){
					var has = false;
	    			for(var i=0; i < gridPanel.store.getCount(); i++){
	    				if(gridPanel.store.getAt(i).get('material.id') == data.data.id){
	    					gridPanel.store.getAt(i).set('amount', gridPanel.store.getAt(i).get('amount') + 1);
	    					has = true;
	    					break;
	    				}
	    			}
	    			
	    			if(!has){
	    				var record = new StockDetailRecord({
							material : data.data,
							id : data.data.id,
							'material.id' : data.data.id,
							'material.cateName' : data.data.cateName,
							'material.name' : data.data.name,
							amount : 1,
							price : data.data.price,
							materialAssociateId : data.json.associateId
						});
						gridPanel.store.add(record);
	    			}else{
	    				gridPanel.store.fireEvent('load');
	    			}
				}
			}
		});
		
		var columnModel = new Ext.grid.ColumnModel([
			new Ext.grid.RowNumberer(),
			{header:'货品名称',dataIndex:'material.name'},
		   	{header:'数量',dataIndex:'amount',align : 'right',
				editor: new Ext.form.NumberField({
			    	allowNegative: false,
    	 			selectOnFocus:true,
				    enableKeyEvents: true,
				    listeners : {
				    	change : function(){
				    		gridPanel.store.fireEvent('load');
				    	}
				    }
				})},
		   {header:'单价',dataIndex:'price',align : 'right',
		   		editor: new Ext.form.NumberField({
					allowNegative: false,
					selectOnFocus:true,
					enableKeyEvents: true,
					listeners : {
				    	change : function(){
				    		gridPanel.store.fireEvent('load');
				    	}
				    }
				})},
		   {header:'总价',dataIndex:'totalPrice',align : 'right', renderer : function(data, cls, json){
				return json.data.amount * json.data.price;
		   	}},
		   {header:'操作',align : 'center',renderer : function(){
		   		return '<a href="javascript:void(0);" data-type="deleteDetail_a_stockDistributionAction">删除</a>';
		   }}
		]);
		
		
		var tbar = new Ext.Toolbar({
			height : 26,
			items : [{
				xtype : 'tbtext',
				text : '&nbsp;货品类型：&nbsp;',
				hidden : true
			}, cateId, {
				xtype : 'tbtext',
				text : '&nbsp;货品选择：&nbsp;'
			}, material]
		});
		
		var totalAmount;
		totalAmount = new Ext.form.TextField({
			width : 100,
			disabled : true,
			height : 30,
			style : {
				'font-size' : '18px',
				'color' : '#000'
			}
		});
		
		var totalMoney;
		totalMoney = new Ext.form.TextField({
			width : 100,
			disabled : true,
			style : {
				'font-size' : '18px',
				'color' : '#000'
			}
		});
		
		var actualMoney;
		actualMoney = new Ext.form.TextField({
			width : 100,
			id : 'actualMoney_createAction',
			style : {
				'font-size' : '18px',
				'color' : 'red'
			}
		});
		
		var gridPanel = new Ext.grid.EditorGridPanel({
			title : '货品列表',
			cm : columnModel,
			store : new Ext.data.JsonStore({
				fields : StockDetailRecord.getKeys()
			}),
			viewConfig : {
	    		forceFit : true
		    },
		    height : 300,
		    autoScroll : true,
		    tbar : tbar,
		    bbar : [{
		    	xtype : 'tbtext',
				text : '总数量小计：',
				height : 30,
				style : {
					'line-height' : '30px',
					'font-size' : '20px',
					'font-weight' : 'bold',
					'margin-left' : '120px'
				}
		    }, totalAmount, {
		    	xtype : 'tbtext',
		    	text　: '总金额：',
		    	height : 30,
				style : {
					'line-height' : '30px',
					'font-size' : '20px',
					'font-weight' : 'bold'
				}
		    }, totalMoney, {
		    	xtype : 'tbtext',
		    	text : '实际金额：',
		    	height : 30,
				style : {
					'line-height' : '30px',
					'font-size' : '20px',
					'font-weight' : 'bold'
				}
		    }, actualMoney]
		});
		
		function createAssociateWin(){
			var colModel;
			colModel = new Ext.grid.ColumnModel([
				new Ext.grid.RowNumberer(),
				{
					header : '日期',
					dataIndex : 'stockAction.oriStockDateFormat'
				},
				{
					header : '库单号',
					dataIndex : 'id'		
				}, {
					header : '关联单号',
					dataIndex : 'associateId',
					renderer : function(data){
						return data > 0 ? data : '----';
					}
				},
				{
					header : '库单类型',
					dataIndex : 'stockAction.subTypeText'
				},
				{
					header : '出货门店',
					dataIndex : 'stockOutRestaurant.name'
				},
				{
					header : '收货门店',
					dataIndex : 'stockInRestaurant.name'
				},
				{
					header : '库单状态',
					dataIndex : 'statusText'		
				},
				{
					header : '操作',
					dataIndex : 'operation',
					renderer : function(){
						return '<a href="javascript:void(0);" data-type="associateStockDistribution_a_CreateAction">引用</a>';
					}
				}
			]);
			
			var store;
			store = new Ext.data.Store({
				proxy : new Ext.data.HttpProxy({url : '../../OperateStockDistribution.do'}),
				reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [{
					name : 'stockAction.oriStockDateFormat'
				},{
					name : 'id'
				},{
					name : 'associateId'
				},{
					name : 'stockAction.subTypeText'
				},{
					name : 'stockOutRestaurant.name'
				},{
					name : 'stockInRestaurant.name'
				},{
					name : 'statusText'
				}]),
				baseParams : {
					start : 0,
					limit : 12,
					dataSource : 'getByCond',
					isWithOutSum : true,
					subType : (_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val ? Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_APPLY.val : _param.subType - 1),
					cateType : _param.cateType,
					restaurantId : (_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val) ?
									stockOutRestaurant.getValue() : '' ,
					isGroupDistirbution : (_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val) ?
										  true : false,
					isHistory : true,
					isWidthOutUnAudit : true
				}
			});
			
			
			var pagingbar
			pagingbar = new Ext.PagingToolbar({
				pageSize : 12,
				store : store,
				displayInfo : true,
				displayMsg : '显示第{0} 条到{1} 条记录，共{2}条',
				emptyMsg : '没有记录'
			}); 
			
			var dateBegin = new Ext.form.DateField({
				id : 'beginDate_combo_createAssociateWin',
				xtype : 'datefield',
				format : 'Y-m-d',
				maxValue : new Date(),
				readOnly : false,
				allowBlank : false
			});
			
			var dateEnd = new Ext.form.DateField({
				id : 'beginEnd_combo_createAssociateWin',
				xtype : 'datefield',
				format : 'Y-m-d',
				maxValue : new Date(),
				readyOnly : false,
				allowBlank : false
			});
			
			var dateCombo;
			dateCombo = Ext.ux.createDateCombo({
				beginDate : dateBegin,
				endDate : dateEnd,
				callback : function(){
					Ext.getCmp('searchBtn_createAction').handler();
				}
			});
				
			var associateStockActionGridPanel;
			associateStockActionGridPanel = new Ext.grid.GridPanel({
				frame : false,
				height : 400,
				style : {
					'width' : '100%'
				},
				viewConfig : {
					forceFit : true
				},
				cm : colModel,
				loadMask : {
					msg : '数据加载中,请稍后....'
				},
				store : store,
				tbar : [{
					xtype : 'tbtext',
					text : '&nbsp;&nbsp;&nbsp;&nbsp;日期：'
				},
				dateCombo,'　',
				dateBegin,{
					xtype : 'tbtext',
					text : '&nbsp;&nbsp;至&nbsp;&nbsp;'
				},
				dateEnd,{
					xtype : 'tbtext',
					text : '&nbsp;&nbsp;&nbsp;&nbsp;绑定状态'
				},{
					xtype : 'combo',
					id : 'associate_createAssociateWin',
					readOnly : false,
					forceSelection : true,
					width : 80,
					value : 1,
					store : new Ext.data.SimpleStore({
						fields : ['value', 'text']
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					listeners : {
						select : function(thiz){
							Ext.getCmp('searchBtn_createAction').handler();
						},
						render : function(thiz){
							var data = [];
							data.push([-1, '全部']);
							for(var key in Ext.stockDistributionAction.stockActionStatus){
								var status = Ext.stockDistributionAction.stockActionStatus[key]
								data.push([status.val, status.desc]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					}
				}, '->', {
					xtype : 'button',
					iconCls : 'btn_search',
					id : 'searchBtn_createAction',
					text : '搜索',
					handler : function(){
						var assosicateStore = associateStockActionGridPanel.store;
						assosicateStore.baseParams['beginDate'] = Ext.getCmp('beginDate_combo_createAssociateWin').getValue();
						assosicateStore.baseParams['endDate'] = Ext.getCmp('beginEnd_combo_createAssociateWin').getValue();
						assosicateStore.baseParams['distributionStatus'] = Ext.getCmp('associate_createAssociateWin').getValue();
						assosicateStore.load();
					}
				}],
				bbar : pagingbar,
				keys : {
					key : 13, //enter键
					scope : this,
					fn : function(){
						Ext.getCmp('searchBtn_createAction').handler();
					}
				},
				listeners : {
					render : function(){
						store.load();
					}
				}
			});
			
			dateCombo.setValue(1);
			dateCombo.fireEvent('select');
			associateStockActionGridPanel.store.on('load', function(){
				$('[data-type=associateStockDistribution_a_CreateAction]').click(function(){
					var selectCol = associateStockActionGridPanel.getSelectionModel().getSelected();
					var restaurantId;
					//收货和回收引用的单据是夸门店的  所以需要传入前一单的门店来查询库单
					if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val){
						restaurantId = selectCol.json.stockOutRestaurant.id;
					}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val){
						restaurantId = selectCol.json.stockInRestaurant.id;
					}
					
					if(selectCol){
						var details;	
						$.ajax({
							url : '../../OperateStockDistribution.do',
							type : 'post',
							dataType : 'json',
							data : {
								id : selectCol.json.id,
								containsDetail : true,
								dataSource : 'getByCond',
								restaurantId : restaurantId,
								isHistory : true,
								beginDate : Ext.getCmp('beginDate_combo_createAssociateWin').value,
								endDate : Ext.getCmp('beginEnd_combo_createAssociateWin').value
							},
							success : function(data, status, req){
								details = data.root[0].stockAction.stockDetails;
									gridPanel.store.removeAll();		    			
					    			if(details && details.length > 0){
					    				details.forEach(function(el, index){
					    					gridPanel.store.add(new StockDetailRecord({
						    					material : el,
						    					id : el.stockActionId,
						    					'material.id' : el.materialId,
						    					'material.cateName' : '',
						    					'material.name' : el.materialName,
						    					amount : el.amount,
						    					price : el.price
						    				}));
					    				});
					    			}else{
					    				Ext.example.msg('错误提示', '该单剧没有材料存在');
					    				return;
					    			}
					    			//收货单的关联号为发货单的单号  其余都为发货单的单号
					    			if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val){
					    				associateId.setValue(selectCol.json.id);
					    			}else{
					    				associateId.setValue(selectCol.json.associateId);
					    			}
					    			
					    			if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val){
					    				stockOutRestaurant.setValue(restaurantId);
					    			}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val){
					    				stockInRestaurant.setValue(restaurantId);
					    			}
					    			
					    			associateWin.hide();
					    			$('#' + associateWin.id).remove();
					    			Ext.example.msg('成功提示', '引用成功');
//						    			
							},
							error : function(req, status, err){
								Ext.example.msg('错误提示', '单据读取失败');
							}
						});
		    		}else{
		    			Ext.example.msg('错误提示', '请选择单据');
		    		}
				});
			});
			
			var associateWin;
			associateWin = new Ext.Window({
				title : '引用【' + Ext.stockDistributionAction.stockSubType.valueOf(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val ? Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_APPLY.val : _param.subType - 1).desc + '】单',
				height : 460,
				width : 900,
		    	resizable : false,
				modal : true,
				closable : false,
				items : [associateStockActionGridPanel],
				bbar : ['->', {
					text : '取消',
					iconCls : 'btn_close',
					handler : function(){
						var id = associateWin.id;
						associateWin.close();
						$('#' + id).remove();
					}
				}]
			});
			
			associateWin.show();
			Ext.getCmp('searchBtn_createAction').handler();
		}
		
		var expandBtn;
		expandBtn = new Ext.Button({
			text : '引用',
			hidden : _param.isAssociate ? false : true,
			iconCls : 'btn_edit',
			handler : function(){
				createAssociateWin();
			}
		});
		
		var container;
		var containerId = 'createAction_' + new Date().getTime();
		container = new Ext.Window({
			id : containerId,
			title : _param.subType ? (Ext.stockDistributionAction.stockSubType.valueOf(_param.subType).desc + '单') + (_param.isOnlyRestaurant ? (_param.stockActionId ? '【' + _param.stockActionId + '】' : '') : '') : '配送申请单',
			height : 500,
			width : 900,
	    	resizable : false,
			modal : true,
			closable : false,
			items : [{
				xtype : 'panel',
				title : '货单基础信息',
				height : 143,
				width : 900,
		    	frame : true,
				items : [{
					xtype : 'container',
					width : 900,
					layout : 'column',
					style : {
						'margin' : '5px 0'						
					},
					items : [{
						xtype : 'tbtext',
						text : !_param.isOnlyRestaurant ? '&nbsp;收货门店：&nbsp;' : '&nbsp;入库仓：&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						},
						id : 'firstLabelField_text_createAction'
					}, stockInRestaurant, deptIn, {
						xtype : 'tbtext',
						text : '&nbsp;&nbsp;供应商：&nbsp;&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						},
						id : 'thirdLabelField_text_createAction'
					}, supplierCombo, {
						xtype : 'tbtext',
						text : !_param.isOnlyRestaurant ? '&nbsp;出货门店：&nbsp;' : '&nbsp;出库仓：&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						},
						id : 'secondLabelField_text_createAction'
					}, stockOutRestaurant, deptOut,{
						xtype : 'tbtext',
						text : '&nbsp;原始单号：&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						}
					}, oriId, {
						xtype : 'tbtext',
						text : (_param.isOnlyRestaurant ? '&nbsp;库单日期：&nbsp;' : ((_param.subType % 2) ? '&nbsp;发货日期：&nbsp;' : '&nbsp;收货日期：&nbsp;')),
						width : 80,
						style : {
							'margin-left' : '10px'					
						}
					}, oriDate]
					}, {
						xtype : 'container',
						layout : 'column',
						style : {
							'margin' : '5px 0'						
						},
						items : [{
						xtype : 'tbtext',
						text : '&nbsp;备注：&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						}
					}, comment]
					}, {
						xtype : 'container',
						layout : 'column',
						items : [{
						xtype : 'tbtext',
						text : '&nbsp;审核人：&nbsp;',
						width : 80,
						style : {
							'margin' : '5px 0'						
						},
						style : {
							'margin-left' : '10px'					
						}
					}, appover, {
						xtype : 'tbtext',
						text : '&nbsp;审核时间：&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						}
					}, appoverDate, {
						xtype : 'tbtext',
						text : '&nbsp;制单人：&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						}
					}, operator, {
						xtype : 'tbtext',
						text : '&nbsp;制单时间：&nbsp;',
						width : 80,
						style : {
							'margin-left' : '10px'					
						}
					}, operateDate]
					}, {
						xtype : 'container',
						layout : 'column',
						style : {
							'margin' : '5px 0'					
						},
						items : [{
							xtype : 'tbtext',
							text : '&nbsp;关联单号&nbsp;',
							width : 80,
							style : {
								'margin-left' : '10px'					
							}		
						}, associateId]
					}
					]
				}, {
					xtype : 'panel',
					items : [gridPanel]
				}],
			bbar : ['->', expandBtn, {
				text : '确认',
				iconCls : 'btn_save',
				disabled : _param.isOnlyShow,
				handler : function(){
					if(_param.callback){
						if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val){
							if(!associateId.getValue()){
								Ext.example.msg('错误提示', '未引用单据');
								return;
							}
						}
						
						var detail = '';
						for(var i = 0; i < gridPanel.getStore().getCount(); i++){
							var temp = gridPanel.getStore().getAt(i);
							if(i>0){
								detail+='<sp>';
							}
							detail+=(temp.get('material.id')+'<spst>'+temp.get('price')+'<spst>'+temp.get('amount'));
						}
						_param.callback({
							stockInRestaurant : stockInRestaurant.getValue(),
							stockOutRestaurant : stockOutRestaurant.getValue(),
							oriId : oriId.getValue(),
							oriDate : oriDate.getValue(),
							comment : comment.getValue(),
							subType : _param.subType,
							detail : detail,
							cateType : _param.cateType,
							associateId : associateId.getValue(),
							stockActionId : _param.stockActionId,
							id : _param.id,
							actualPrice : Ext.getCmp('actualMoney_createAction').getValue(),
							stockType : _param.stockType ? _param.stockType : '',
							deptOut : deptOut.getValue(),
							deptIn : deptIn.getValue(),
							supplier : supplierCombo.getValue()
						});
					}
				}
			}, {
				text : '取消',
				iconCls : 'btn_close',
				handler : function(){
					_self.close();
				}
			}]
		});
		
		function setMes(){
			if(_param.expandBtns){
//				console.log(_param.expandBtns instanceof Array);
				if(_param.expandBtns instanceof Array){
					_param.expandBtns.forEach(function(el, index){
						container.toolbars[0].insertButton(index + 1, el);
					});	
				}else{
					container.toolbars[0].insertButton(1, _param.expandBtns);
				}
//				container.toolbars[0].insertButton(1, testBtn);
				container.toolbars[0].doLayout();
			}
			
			if(_param.oriId){
				oriId.setValue(_param.oriId);
			}
			
			if(_param.oriDate){
				oriDate.setValue(_param.oriDate);
			}
			
			if(_param.comment){
				comment.setValue(_param.comment);
			}
			
			if(_param.appover){
				appover.setValue(_param.appover);
			}
			
			if(_param.appoverDate && _param.appover){
				appoverDate.setValue(_param.appoverDate);
			}
			
			if(_param.operator){
				operator.setValue(_param.operator);		
			}
			
			if(_param.operateDate){
				operateDate.setValue(_param.operateDate);
			}
			
			if(_param.associateId){
				associateId.setValue(_param.associateId);
			}
			
			if(_param.details){
				_param.details.forEach(function(el, index){
					var record = new StockDetailRecord({
						id : el.id,
						'material.id' : el.materialId,
						'material.name' : el.materialName,
						amount : el.amount,
						price : el.price
					});
					gridPanel.store.add(record);
				});
			}
			
			if(_param.actualPrice){
				setTimeout(function(){
					Ext.getCmp('actualMoney_createAction').setValue((_param.actualPrice).toFixed(2));
				}, 300);
			}
		}
		
		
		function NewDate(str) { 
			str = str.split('-'); 
			var date = new Date(); 
			date.setUTCFullYear(str[0], str[1] - 1, str[2]); 
			date.setUTCHours(0, 0, 0, 0); 
			return date; 
		} 
		
		function getCurrentDay(){
			Ext.Ajax.request({
				url : '../../OperateMonthlyBalance.do',
				params : {dataSource : 'getCurrentMonthly'},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					if(jr.other.minDay){
						oriDate.setMinValue(NewDate(jr.other.minDay));
					}
					if(NewDate(jr.other.currentDay) < new Date()){
						oriDate.setValue(NewDate(jr.other.currentDay));
						oriDate.setMaxValue(NewDate(jr.other.currentDay));
					}else{
						oriDate.setMaxValue(new Date());
					}
				},
				failure : function(res, opt){
					Ext.ux.showMsg(Ext.decode(res.responseText));
				}
				
			});
		}
		
		function beforeShow(){
			
			if(!_param.isOnlyRestaurant){
				deptIn.hide();
				deptOut.hide();
				supplierCombo.hide();
				//解决Ext 内部text不能hide bug
				Ext.getCmp('thirdLabelField_text_createAction').setWidth(0);
				Ext.getCmp('thirdLabelField_text_createAction').setHeight(0);
				
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getByCond',
						id : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						var data = [];
						//配送发货
						if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val){
							if(jr.root[0].typeVal != '2'){
								if(!_param.checkWithOutMsg){
									Ext.ux.showMsg({
										msg : '总店以外的门店不能建立发货单',
										title : '错误提醒',
										callBack : _self.close,
										code : '5199'
									});
								}
								
								stockInRestaurant.disable();
								stockOutRestaurant.disable();
							}else{
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
								
								stockInRestaurant.store.loadData(data);
								stockOutRestaurant.store.loadData([[jr.root[0]['id'], jr.root[0]['name'] + '(总店)']]);
								stockOutRestaurant.setValue(jr.root[0]['id']);
								stockOutRestaurant.disable();
							}
						}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_APPLY.val){
							if(jr.root[0].typeVal == '3'){
								stockInRestaurant.store.loadData([[jr.root[0]['id'], jr.root[0]['name']]]);
								stockInRestaurant.setValue(jr.root[0]['id']);
								stockInRestaurant.disable();
	
								Ext.Ajax.request({
									url : '../../OperateRestaurant.do',
									params : {
										dataSource : 'getGroupRestaurant'
									},
									success : function(res, opt){
										var jr = Ext.decode(res.responseText);
										stockOutRestaurant.store.loadData([[jr.root[0]['id'], jr.root[0]['name']]]);
										stockOutRestaurant.setValue(jr.root[0]['id']);
										stockOutRestaurant.disable();
									},
									failure : function(res, opt){
										Ext.example.msg('错误提示', Ext.decode(res.responseText).msg);
									}
								});
							}else{
								if(!_param.checkWithOutMsg){
									Ext.ux.showMsg({
										msg : '总店不能建立该单',
										title : '错误提醒',
										callBack : _self.close,
										code : '5199'
									});
								}
								stockInRestaurant.disable();
								stockOutRestaurant.disable();
							}
						}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val){
							if(jr.root[0].typeVal == '3'){
								stockOutRestaurant.store.loadData([[jr.root[0]['id'], jr.root[0]['name']]]);
								stockOutRestaurant.setValue(jr.root[0]['id']);
								stockOutRestaurant.disable();
	
								Ext.Ajax.request({
									url : '../../OperateRestaurant.do',
									params : {
										dataSource : 'getGroupRestaurant'
									},
									success : function(res, opt){
										var jr = Ext.decode(res.responseText);
										stockInRestaurant.store.loadData([[jr.root[0]['id'], jr.root[0]['name']]]);
										stockInRestaurant.setValue(jr.root[0]['id']);
										stockInRestaurant.disable();
									},
									failure : function(res, opt){
										Ext.example.msg('错误提示', Ext.decode(res.responseText).msg);
									}
								});
							}else{
								if(!_param.checkWithOutMsg){							
									Ext.ux.showMsg({
										msg : '总店不能建立退货单',
										title : '错误提醒',
										callBack : _self.close,
										code : '5199'
									});
								}
								stockInRestaurant.disable();
								stockOutRestaurant.disable();
							}
						}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val){
							if(jr.root[0].typeVal != '2'){
								if(!_param.checkWithOutMsg){
									Ext.ux.showMsg({
										msg : '总店以外的门店不能建立回收单',
										title : '错误提醒',
										callBack : _self.close,
										code : '5199'
									});
								}
								stockInRestaurant.disable();
								stockOutRestaurant.disable();
							}else{
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
								
								stockOutRestaurant.store.loadData(data);
								stockInRestaurant.store.loadData([[jr.root[0]['id'], jr.root[0]['name'] + '(总店)']]);
								stockInRestaurant.setValue(jr.root[0]['id']);
								stockInRestaurant.disable();
								stockOutRestaurant.disable();
							}
						}
						
						if(_param.stockInRestaurant){
							if(stockInRestaurant.store.data.length){
								stockInRestaurant.setValue(_param.stockInRestaurant);
							}else{
								stockInRestaurant.setValue(_param.stockInRestaurantText);
							}
						}
						
						if(_param.stockOutRestaurant){
							if(stockOutRestaurant.store.data.length){
								stockOutRestaurant.setValue(_param.stockOutRestaurant);
							}else{
								stockOutRestaurant.setValue(_param.stockOutRestaurantText);
							}
						}
						
					},
					failure : function(res, opt){
						Ext.example.msg('错误提示', Ext.decode(res.responseText).msg);
					}
				});
			}else{
				stockInRestaurant.hide();
				stockOutRestaurant.hide();
				supplierCombo.hide();
				$.ajax({
					url : '../../OperateDept.do',
					type : 'post',
					dataType : 'json',
					data : {
						dataSource : 'getByCond',
						inventory : true
					},
					success : function(data){
						var depts = [];
						for(var i = 0; i < data.root.length; i++){
							depts.push([data['root'][i]['id'], data['root'][i]['name']]);
						}
						deptIn.store.loadData(depts);
						deptOut.store.loadData(depts);
						if(_param.subType == Ext.stockDistributionAction.stockSubType.values.STOCK_OUT_TRANSFER.val){
							deptIn.setValue(252);
							deptIn.disable();
						}else{
							deptOut.setValue(252);
							deptOut.disable();
						}
						
						if(_param.deptIn || typeof _param.deptIn == 'number'){
							deptIn.setValue(_param.deptIn);
						}
						
						if(_param.deptOut || typeof _param.deptOut == 'number'){
							deptOut.setValue(_param.deptOut);
						}
					},
					error : function(){
					
					}
				});
				
				$.ajax({
					url : '../../QuerySupplier.do',
					type : 'post',
					dataType : 'json',
					data : {
					
					},
					success : function(data){
						var suppliers = [];
						for(var i = 0; i < data.root.length; i++){
							suppliers.push([data.root[i].supplierID, data.root[i].name]);
						}
						supplierCombo.store.loadData(suppliers);
						if(_param.supplier){
							supplierCombo.setValue(_param.supplier);
						}
					},
					error : function(){
					
					}
				});
				
				if(_param.subType == Ext.stockDistributionAction.stockSubType.values.STOCK_OUT.val){
					supplierCombo.show();
					deptIn.hide();
					Ext.getCmp('firstLabelField_text_createAction').setWidth(0);
					Ext.getCmp('firstLabelField_text_createAction').setHeight(0);
				}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.STOCK_IN.val){
					supplierCombo.show();
					deptOut.hide();
					Ext.getCmp('secondLabelField_text_createAction').setWidth(0);
					Ext.getCmp('secondLabelField_text_createAction').setHeight(0);
				}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.STOCK_OUT_TRANSFER.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.STOCK_IN_TRANSFER.val){
					Ext.getCmp('thirdLabelField_text_createAction').setWidth(0);
					Ext.getCmp('thirdLabelField_text_createAction').setHeight(0);
				}else if(_param.subType == Ext.stockDistributionAction.stockSubType.values.DAMAGE.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.CONSUMPTION.val || _param.subType == Ext.stockDistributionAction.stockSubType.values.LESS.val){
					deptIn.hide();
					Ext.getCmp('firstLabelField_text_createAction').setWidth(0);
					Ext.getCmp('firstLabelField_text_createAction').setHeight(0);
					Ext.getCmp('thirdLabelField_text_createAction').setWidth(0);
					Ext.getCmp('thirdLabelField_text_createAction').setHeight(0);
				}else{
					deptOut.hide();
					Ext.getCmp('secondLabelField_text_createAction').setWidth(0);
					Ext.getCmp('secondLabelField_text_createAction').setHeight(0);
					Ext.getCmp('thirdLabelField_text_createAction').setWidth(0);
					Ext.getCmp('thirdLabelField_text_createAction').setHeight(0);
				}
			}
			
			if(_param.cateType == Ext.stockDistributionAction.cateType.GOOD.val){
				material.store.loadData(cookieData.good);
			}else if(_param.cateType == Ext.stockDistributionAction.cateType.MATERIAL.val){
				material.store.loadData(cookieData.material);
			}else{
				alert('菜品读取失败');
			}
			
			//gridPanel 里面的删除功能
			gridPanel.store.on('load', function(){
						
				setTimeout(function(){
					var countTotalAmount = 0;
					var countTotalMoney = 0;
					for(var i = 0; i < gridPanel.store.getCount(); i++){
						countTotalAmount += gridPanel.store.getAt(i).get('amount');
						countTotalMoney += gridPanel.store.getAt(i).get('amount') * gridPanel.store.getAt(i).get('price');
					}
					
					totalAmount.setValue(countTotalAmount);
					totalMoney.setValue(countTotalMoney.toFixed(2));
					actualMoney.setValue(countTotalMoney.toFixed(2));
					
					$('[data-type=deleteDetail_a_stockDistributionAction]').each(function(index, el){
						el.onclick = function(){
							gridPanel.store.removeAt(gridPanel.getSelectionModel().selection.cell[0]);
						}
					});
				}, 0);
			});
			
			gridPanel.store.on('add', function(){
				gridPanel.store.fireEvent('load');
			});
			
			gridPanel.store.on('remove', function(){
				gridPanel.store.fireEvent('load');
			});
			
			getCurrentDay();
			//设置弹框的值
			setMes();
		}
		
		this.open = function(beforeOpen, afterOpen){
			if(beforeOpen && typeof beforeOpen == 'function'){
				beforeOpen($('#' + containerId));
			}
			
			if(_param.cateType){
				container.show();
				beforeShow();
			}else{
				cateTypeWin.show();
			}
			
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen($('#' + containerId));
			}		
			
			return this;
		}		
		
		this.close = function(afterClose){
			cateTypeWin.hide();
			container.hide();
			$('#' + containerId).remove();
			$('#' + cateTypeWinId).remove();
			if(afterClose && typeof afterClose == 'function'){
				afterClose();
			}
			
			return this;
		}
		
		this.callback = function(callback){
			if(callback && typeof callback == 'function'){
				callback($('#' + containerId));
			}
		}
		
		this.gridPanel = gridPanel;
		
	}
	
})();
getClientTypeTrigger = function(c){
	 var trigger = new Ext.form.TriggerField({
		editable : false,
	    fieldLabel : c.fieldLabel,
	    id : c.id,
	    readOnly : false,
	    allowBlank : typeof c.allowBlank == 'boolean' ? c.allowBlank : false,
	    width : c.width == null || typeof c.width == 'undefined' ? '' : c.width,
	    blankText : '该项不能为空.',
	    validator : function(v){
			if(Ext.util.Format.trim(v).length > 0){
				return true;
			}else{
				return '该项不能为空.';
			}
		},
		hideOnClick : false,
	    onTriggerClick : function(){
	    	if(!this.disabled){
	    		this.menu.show(this.el, 'tl-bl?');
	    	}
	    },
	    listeners : {
	    	render : function(){
	    		this.setValue = function(v){
	    			this.tree.setValue(v);
	    		};
	    		this.getValue = function(){
	    			return this.value;
	    		};
	    		this.reload = function(){
	    			if(typeof this.tree.getRootNode() != 'undefined'){
	    				this.tree.getRootNode().reload();
	    			}
	    		};
	    		this.hasNode = function(){
	    			if(typeof this.tree.getRootNode() != 'undefined'){
	    				return this.tree.getRootNode().childNodes.length > 0;
	    			}else{
	    				return false;
	    			}
	    		};
	    		this.tree = new Ext.tree.TreePanel({
	 	    		width : 200,
		    	   	height : 280,
		    	   	autoScroll : true,
	 	    		rootVisible : typeof c.rootVisible == 'boolean' ? c.rootVisible : true,
	 	    		frame : true,
	 	    		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
	 	    		loader : new Ext.tree.TreeLoader({
	 	    			dataUrl : '../../QueryClientTypeTree.do',
	 	    			baseParams : {
	 	    				restaurantID : restaurantID
	 	    			}
	 	    		}),
	 	    		root : new Ext.tree.AsyncTreeNode({
	 	    			text : typeof c.rootText == 'string' ? c.rootText : '全部类型', 
	 	    			leaf : false,
	 	    			border : true,
	 	    			clientTypeID : -1,
	 	    			expanded : true
	 	    		}),
	 	    		listeners : {
	 	    			load : function(){
 	    					this.ownerCt.isLoad = true;
 	    				},
	 	    			render : function(){
	 	    				this.bindValue = function(c){
	 	    					this.ownerCt.setRawValue(c.text);
		    	    			this.ownerCt.value = c.value;
	 	    				};
	 	    				this.findChildNode = function(node, val){
	 	    					if(node.attributes.clientTypeID == val){
	 	    						node.select();
	 	    						this.bindValue({
			    	    				text : node.text,
			    	    				value : node.attributes.clientTypeID
			    	    			});
	 	    					}else{
	 	    						for(var i = 0; i < node.childNodes.length; i++){
	 	    							var rn = node.childNodes[i];
	 	    							if(rn.attributes.clientTypeID == val){
	 	    								rn.select();
	 	    								this.bindValue({
	 	    									text : rn.text,
	 	    									value : rn.attributes.clientTypeID
	 	    								});
	 	    								break;
	 	    							}else if(rn.childNodes.length > 0){
	 	    								this.findChildNode(rn, val);
	 	    							}
	 	    						}
	 	    					}
	 	    				};
	 	    				this.checkValue = function(node, vn){
	 	    					if(node.parentNode != null){
	 	    						var pn = node.parentNode;
	 	    						if(pn.attributes.clientTypeID == vn.attributes.clientTypeID){
	 	    							return true;
	 	    						}else{
	 	    							return this.checkValue(pn, vn);
	 	    						}
	 	    					}
	 	    				};
	 	    				this.setValue = function(val){
	 	    					this.getSelectionModel().clearSelections();
	 	    					if(val == null || typeof val == 'undefined'){
	 	    						this.snode = null;
	 	    						this.bindValue({
			    	    				text : null,
			    	    				value : null
			    	    			});
	 	    					}else if(typeof val == 'string' && Ext.util.Format.trim(val).length > 0){
	 	    						this.snode = null;
	 	    						this.findChildNode(this.getRootNode(), val);
	 	    					}else if(typeof val == 'number'){
	 	    						this.snode = null;
	 	    						this.findChildNode(this.getRootNode(), val);
	 	    					}else if(typeof val == 'object'){
	 	    						this.snode = val;
	 	    						this.findChildNode(this.getRootNode(), val.attributes.clientParentTypeID);
	 	    					}
	 	    				};
	 	    				this.getValue = function(){
	 	    					return this.ownerCt.value;
	 	    				};
	 	    			},
	 	    			click : function(e){
	 	    				if(this.snode != null && typeof this.snode != 'undefined'){
	 	    					if(eval(e.attributes.clientTypeID != this.snode.attributes.clientTypeID) && this.checkValue(e, this.snode) != true){
			    	    			this.bindValue({
			    	    				text : e.text,
			    	    				value : e.attributes.clientTypeID
			    	    			});
			    	    			this.ownerCt.menu.hide();
		    	    			}else{
		    	    				Ext.example.msg('提示', '不能选择自身或其子类型.');
		    	    			}
	 	    				}else{
	 	    					this.bindValue({
		    	    				text : e.text,
		    	    				value : e.attributes.clientTypeID
		    	    			});
		    	    			this.ownerCt.menu.hide();
	 	    				}
	 	    			}
	 	    		}
	    		});
	  	    	this.tree.ownerCt = this;
	  	    	this.menu = new Ext.menu.Menu({
	   	    		items : [new Ext.menu.Adapter(this.tree)]
	   	    	});
	   	    	this.menu.ownerCt = this;
	   	    	this.menu.render(document.body);
	    	}
	    }
	 });
	 // 设置自定义属性
	 if(typeof c.moption == 'object'){
		 for(var key in c.moption){
			 trigger[key] = c.moption[key];
		 }
	 }
	 return trigger;
};

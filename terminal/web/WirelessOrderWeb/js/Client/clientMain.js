Ext.onReady(function() {
	var center = new Ext.Panel({
		region : 'center',
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : ['->', new Ext.ux.ImageButton({
				imgPath : '../../images/UserLogout.png',
				imgWidth : 50,
				imgHeight : 50,
				tooltip : '返回',
				handler : function(e){
					location.href = '../PersonLogin.html?restaurantID=' + restaurantID + '&isNewAccess=false' + '&pin=' + pin;
				}
			}), {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			}, new Ext.ux.ImageButton({
				imgPath : '../../images/ResLogout.png',
				imgWidth : 50,
				imgHeight : 50,
				tooltip : '登出',
				handler : function(e){
					
				}
			}), {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			}]
		}),
		items : [{
			contentEl : 'protal'
		}]
	});
	
	initMainView(null, center,null);
	getOperatorName(pin, '../../');
	
	bingActiveEvent();
});

function bingActiveEvent(){
	// ---------------------------------
//	bindActiveEvent('clientMgr', 
//			'url(../../images/clientMgr_select.png) no-repeat 50%',
//			'url(../../images/clientMgr.png) no-repeat 50%',
//			"SupplierManagement.html?pin=" + pin + "&restaurantID=" + restaurantID
//	);
	// ---------------------------------
	bindActiveEvent('memberTypeMgr', 
			'url(../../images/memberTypeMgr_select.png) no-repeat 50%',
			'url(../../images/memberTypeMgr.png) no-repeat 50%',
			"MemberTypeManagement.html?pin=" + pin + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('memberMgr', 
			'url(../../images/memberMgr_select.png) no-repeat 50%',
			'url(../../images/memberMgr.png) no-repeat 50%',
			"MemberManagement.html?pin=" + pin + "&restaurantID=" + restaurantID
	);
};


var paySubmit = function(submitType) {

	var Request = new URLParaQuery();
	Ext.Ajax.request({
		url : "../PayOrder.do",
		params : {
			"pin" : Request["pin"],
			"tableID" : Request["tableNbr"],
			"actualPrice" : checkOutForm.findById("actualCount").getValue(),
			"payType" : payType,
			"discountType" : discountType,
			"payManner" : submitType,
			"memberID" : actualMemberID,
			"comment" : checkOutForm.findById("remark").getValue()
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var dataInfo = resultJSON.data;
				Ext.MessageBox.show({
					msg : dataInfo,
					width : 300,
					buttons : Ext.MessageBox.OK,
					fn : function() {
						var Request = new URLParaQuery();
						location.href = "TableSelect.html?pin=" + Request["pin"];
					}
				});
			} else {
				var dataInfo = resultJSON.data;
				Ext.MessageBox.show({
					msg : dataInfo,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
		}
	});
};

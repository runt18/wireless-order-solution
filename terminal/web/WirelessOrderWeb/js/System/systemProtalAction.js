var protalFuncReg = function() {
	$("#passwordConfig").each(
			function() {
				$(this).bind(
						"click",
						function() {
							// 密码設置
							passwordConfigWin.show();
						});
			});

};
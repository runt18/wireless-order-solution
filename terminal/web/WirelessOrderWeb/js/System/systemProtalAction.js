var protalFuncReg = function() {
	$("#passwordConfig").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (!isPrompt) {
								// 密码設置
								passwordConfigWin.show();
								isPrompt = true;
							}
						});
			});

};
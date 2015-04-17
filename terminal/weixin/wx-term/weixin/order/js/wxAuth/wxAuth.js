//从普通url获取当前桌信息
function common_urlParaQuery() {
	var name, value, i, key = 0;
	var str = location.href;
	
	if(str.indexOf("#") > 0){
		str = str.substring(0,str.indexOf("#"));
	}
	var num = str.indexOf("?");
	if(num > 0){
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
}

	var Request = new common_urlParaQuery();
	var rid = Request["rid"];
	
	
	$.ajax({
		url : '../../WxAuthLogin.do',
		type : 'post',
		data : { rid : rid },
		success : function(result){
			location.href = result;
		},
		error : function(xhr){
			location.href = xhr.responseText;
		}
	});
	
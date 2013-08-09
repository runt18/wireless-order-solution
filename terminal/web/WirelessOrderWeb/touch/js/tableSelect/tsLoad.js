//function addRegions(){
//	var xmlhttp;
//	if(window.XMLHttpRequest){
//		xmlhttp = new XMLHttpRequest();
//	}else{
//		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
//	}
//
//	xmlhttp.onreadystatechange = function(){
//		if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
//			var responseText = xmlhttp.responseText;
//			//把json格式的字符串解析成javascript对象
//			var region = eval("(" + responseText + ")");
//			var regionHtml = "";
//			for(x in region){			
//				regionHtml += "<div class='button-base' id='"+region[x].regionId+
//				"' style='margin-bottom: 2px;' onclick='addTables(this)'>"+region[x].regionName+"</div>";
//			}
//			$("#divShowRegion").html(regionHtml);
//		}
//	};
//	xmlhttp.open("POST", "/WirelessOrderWeb/QueryRegion.do", true);
//	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//	xmlhttp.send("pin=217&dataSource=tree&random="+Math.random());
//}

function addRegions(){
	$.post("/WirelessOrderWeb/QueryRegion.do",{pin: 217, dataSource: 'tree'}, function(result){
		var region;
	    region = eval("(" + result + ")");
		var regionHtml = "";
		for(x in region){			
			regionHtml += "<div class='button-base' id='"+region[x].regionId+
			"' style='margin-bottom: 2px;' onclick='addTables(this)'>"+region[x].regionName+"</div>";
		}
		$("#divShowRegion").html(regionHtml);
	});
}













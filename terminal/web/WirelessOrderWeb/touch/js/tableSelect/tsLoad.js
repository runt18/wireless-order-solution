function addRegions(){
	var xmlhttp;
	if(window.XMLHttpRequest){
		xmlhttp = new XMLHttpRequest();
	}else{
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}

	xmlhttp.onreadystatechange = function(){
		if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
			var responseText = xmlhttp.responseText;
			//把json格式的字符串解析成javascript对象
				alert("H");
			var region = eval("(" + responseText + ")");
			for(x in region){			
				document.getElementById("divToolRight").innerHTML += 
				"<div class='button-base' id='"+region[x].regionId+
				"' style='margin-bottom: 2px;'>"+region[x].regionName+"</div>";
			}								
		}
	}
	xmlhttp.open("POST", "/WirelessOrderWeb/QueryRegion.do?flag=normal", true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
	xmlhttp.send("pin=217&dataSource=tree");
}
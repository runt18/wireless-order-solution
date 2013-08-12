function addRegions(){
	$.get("/WirelessOrderWeb/QueryRegion.do",{pin: 217, dataSource: 'tree'}, function(result){
		var region;
	    region = eval("(" + result + ")");
		var regionHtml = "";
		for(x in region){			
			regionHtml += "<div class='button-base regionSelect' id='region"+region[x].regionId+
			"' style='margin-bottom: 2px;' onclick='addTables(this)'>"+region[x].regionName+"</div>";
		}
		$("#divShowRegion").html(regionHtml);
	});
}













function editMaterial(id, alias_id, name, stock, price, warning_threshold, danger_threshold) {
    var editType = "addMaterial";
    var title = "添加食材";
    var stockTitle = "数量";
    if (id != "") {
        editType = "editMaterial";
        title = "修改食材";
        stockTitle = "库存";
    }
    var aliasId = "";
    if (id == "") {
        aliasId = '<input type="text" id="alias_id" name="alias_id" value="' + alias_id + '" size="25" height="20" onfocus="this.select()" ' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" />';
        	                            
    }
    else {
        aliasId = '<input type="hidden" id="alias_id" name="alias_id" value="' + alias_id + '"/>' + alias_id + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="materialForm" action="material.php"  method="post" onkeydown="editMaterialKeyDown(&quot;' + stockTitle + '&quot;)">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<div class="add_foot_Content" style="height:230px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1" style="padding-left:32px">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;编号：' + aliasId + '</div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;名称：<input type="text" id="name" name="name" value="' + name + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + stockTitle + '：<input type="text" id="stock" name="stock" value="' + stock + '" size="25" height="20" onfocus="this.select()"' +
	                             ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;价格：<input type="text" id="price" name="price" value="' + price + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1" style="padding-left:32px">预警阀值：<input type="text" id="warning_threshold" name="warning_threshold" value="' + warning_threshold + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                               '<div class="pop_Content1" style="padding-left:32px">危险阀值：<input type="text" id="danger_threshold" name="danger_threshold" value="' + danger_threshold + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitMaterialData(&quot;' + stockTitle + '&quot;)">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';    
    showMessageBox(content, 342, 350);
    if (id == "") {
        document.getElementById("alias_id").focus();
    }
    else {
        document.getElementById("name").focus();
    }    
}

function submitMaterialData(stockTitle) {   
    var alias_id = document.getElementById("alias_id").value;
    var name = document.getElementById("name").value;
    var stock = document.getElementById("stock").value;
    var price = document.getElementById("price").value;
    var warning_threshold = document.getElementById("warning_threshold").value;
    var danger_threshold = document.getElementById("danger_threshold").value;
    if (alias_id == undefined || alias_id == null || alias_id == "") {
        alert("编号不能为空！");
        document.getElementById("alias_id").focus();
        return;
    }
    if (name == undefined || name == null || name == "") {
        alert("名称不能为空！");
        document.getElementById("name").focus();
        return;
    }
    if (stock == undefined || stock == null || stock == "") {
        alert(stockTitle + "不能为空！");
        document.getElementById("stock").focus();
        return;
    }
    if (price == undefined || price == null || price == "") {
        alert("价格不能为空！");
        document.getElementById("price").focus();
        return;
    }
    if (warning_threshold == undefined || warning_threshold == null || warning_threshold == "") {
        alert("预警阀值不能为空！");
        document.getElementById("warning_threshold").focus();
        return;
    }
    if (danger_threshold == undefined || danger_threshold == null || danger_threshold == "") {
        alert("危险阀值不能为空！");
        document.getElementById("danger_threshold").focus();
        return;
    }       
    document.materialForm.submit();
}

function editMaterialKeyDown(stockTitle) {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitMaterialData(stockTitle);
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function deleteMaterial(id) {
    if (confirm("确认删除？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "material.php";
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "id";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "deleteMaterial";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }
}

function showHideCondition(select) {
    var option = select.options[select.selectedIndex];
    document.getElementById("condition_type").style.display = "none";
    document.getElementById("keyword").style.display = "inline";
    document.getElementById("keyword").value = "";
    if (option.value == "stock" || option.value == "price" || option.value == "warning_threshold" || option.value == "danger_threshold") {
        document.getElementById("condition_type").style.display = "inline";
    }   
}
function initializeMaterial() {
    var keyword_type = document.getElementById("keyword_type");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var condition_type = document.getElementById("condition_type");
    var condition_type_value = document.getElementById("condition_type_value").value;
    var keyword = document.getElementById("keyword");
    var keyword_value = document.getElementById("keyword_value").value;
    for (var i = 0; i < keyword_type.options.length; i++) {
        if (keyword_type.options[i].value == keyword_type_value) {
            keyword_type.options[i].selected = true;
            break;
        }
    }
    for (var i = 0; i < condition_type.options.length; i++) {
        if (condition_type.options[i].value == condition_type_value) {
            condition_type.options[i].selected = true;
            break;
        }
    }  
    keyword.value = keyword_value;
    if (keyword_type_value == "stock" || keyword_type_value == "price" || keyword_type_value == "warning_threshold" || keyword_type_value == "danger_threshold") {
        document.getElementById("condition_type").style.display = "inline";
    }   
}

function inWarehouse(id, name) {
    var editType = "inWarehouse";
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">入库 - ' + name + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="materialForm" action="material.php"  method="post" onkeydown="inWarehouseKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<div class="add_foot_Content" style="height:160px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">数量：<input type="text" id="amount" name="amount" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46||event.keyCode==45"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1">价格：<input type="text" id="price" name="price" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46||event.keyCode==45"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1">日期：<input type="text" id="date" name="date" size="25" height="20" style="width:136px"  onclick="javascript:ShowCalendar(this.id)" onfocus="this.select()" /></div>' +  
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitInWarehouseData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("amount").focus();
}

function inWarehouseKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitInWarehouseData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function submitInWarehouseData() {
    var amount = document.getElementById("amount").value;
    var price = document.getElementById("price").value;
    var date = document.getElementById("date").value;
    if (amount == undefined || amount == null || amount == "") {
        alert("数量不能为空！");
        document.getElementById("amount").focus();
        return;
    }
    if (price == undefined || price == null || price == "") {
        alert("价格不能为空！");
        document.getElementById("price").focus();
        return;
    }
    if (date == undefined || date == null || date == "") {
        alert("日期不能为空！");
        document.getElementById("date").focus();
        return;
    }   
    document.materialForm.submit();
}

function viewDetail(id, name) {
    var editType = "viewDetail";
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">入库明细 - 请选择日期区间</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="materialForm" action="material.php"  method="post" onkeydown="editMaterialKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<input type="hidden" name="name" value="' + name + '" />' +
	                      '<div class="add_foot_Content" style="height:130px;text-align:center">' +
	                        '<div class="pop_Content">' +	                         
	                           '<div class="pop_Content1" style="padding-left:0px;text-align:center">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:100px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:100px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitViewDetail()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
}
function viewDetailKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitViewDetail();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}
function submitViewDetail() {
    document.materialForm.submit();
}

function viewInWarehouse(id, name, dateFrom, dateTo) {
    var content = ' <div class="add_foot" style="height:550px;width:100%">' +
                              '<div class="title" style="width:100%">' +
	                          '<div class="title_left" style="width:92%"><font class="font" style="width:200px">入库明细 - ' + name + '</font></div>' +
	                          '<div class="title_right" style="width:8%;float:left" ></div>' +
	                          '</div>' +
	                          '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' +
	                              '<iframe src="viewInWarehouse.php?id=' + id + '&dateFrom=' + dateFrom + '&dateTo=' + dateTo + '" scrolling="no" style="width:100%;height:100%" />' +
	                          '</div>' +
						   '</div>';
    showMessageBox(content, 450, 350);
}
function viewMaterialStat(statType) {
    if (statType == "daily") {
        title = "日结汇总";
    }
    else {
        title = "月结汇总";
    }
    var content = ' <div id="div_add_foot" class="add_foot" style="height:400px;width:100%">' +
                              '<div class="title" style="width:100%">' +
	                          '<div id="div_title_left" class="title_left" style="width:565px"><font id="titleName" class="font" style="width:260px">' + title + '</font></div>' +
	                          '<div class="title_right" style="width:35px;float:left" ></div>' +
	                          '</div>' +
	                          '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' +
	                              '<iframe src="searchCondition.php?table=material&value=id&name=name&target=materialStat.php&statType=' + statType + '&msg=食材" scrolling="no" style="width:100%;height:100%" />' +
	                          '</div>' +
						   '</div>';
    showMessageBox(content, 600, 535);

}

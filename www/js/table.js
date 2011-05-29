function move(sign) {
    var tleft = left;
    switch (sign) {
        case "right_btn":
            {
                var len = $("#list .item").length * 800;
                if ((len - Math.abs(left)) > 800) {
                    tleft -= 800;
                }
                else {
                    tleft = left;
                }
                break;
            }
        case "left_btn":
            {
                if (left == 0) {
                    tleft = 0;
                }
                else {
                    tleft += 800;
                }
                break;
            }
    }

    if ((tleft - left) != 0) {
        $("#list").animate({
            left: tleft + "px"
        }, 500, function() {
            left = tleft;

        });
    }
    else {
        $("#list").animate({
            left: 0 + "px"
        }, 500, function() {
            left = 0;

        });
    }

}

function deleteTable(tableCode) {
    var content = ' <div class="add_foot">' +
            '<div class="title">' +
                '<div class="title_left"><font class="font">删除餐桌</font></div>' +
                '<div class="title_right"></div>' +
            '</div>' +
                  '<div class="add_foot_Content">' +
                    '<div class="pop_Content">' +
                        '<div class="pop_Content1">餐桌号：<input type="text" id="tableCode" size="25" value="' + tableCode + '" height="20"/></div>' +
                    '</div>' +
                    '<span class="pop_action-span"><a href="#" onclick="confirmDelete(document.getElementById(&quot;tableCode&quot;).value,&quot;table.php&quot;)">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                    '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                  '</div>' +
            '</div>';
    showMessageBox(content, 342, 350);
}

function confirmDelete(id, target) {
    if (confirm("确认删除" + id + "号台？")) {
        var formDelete = document.createElement("form");
        formDelete.action = target;
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "deleteId";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "deleteTable";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }

}

function editTable(id, alias_id, name, target) {
    var editType = "addTable";
    var title = "添加餐桌";    
    if (id != "") {
        editType = "editTable";
        title = "修改餐桌";
    }
    var aliasId = "";
    if (id == "") {
        aliasId = '<input type="text" id="alias_id" name="alias_id" value="' + alias_id + '" size="25" height="20" onfocus="this.select()" ' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" />'
    }
    else {
        aliasId = '<input type="hidden" id="alias_id" name="alias_id" value="' + alias_id + '"/>' + alias_id + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="tableForm" action="' + target + '"  method="post" onkeydown="editTableKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<div class="add_foot_Content" style="height:180px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">编号：' + aliasId + '</div>' +
	                            '<div class="pop_Content1">名称：<input type="text" id="name" name="name" value="' + name + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitTableData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
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

function submitTableData() {
    var alias_id = document.getElementById("alias_id").value;
    var name = document.getElementById("name").value;
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
    document.tableForm.submit();
}

function editTableKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitTableData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}


function showMenu(id) {
    var content = ' <div class="add_foot">' +
            '<div class="title">' +
                '<div class="title_left"><font class="font" style="width:100%">操作（' + id + '号台）</font></div>' +
                '<div class="title_right"></div>' +
            '</div>' +
            '<form name="tableForm" action="table.php"  method="post">' +

                  '<div class="add_foot_Content" style="text-align:center;">' +
				    '<table width="100%" border=0>' +
			        '<tr><td>' +
                    '<div class="pop_action-span1" style="margin-right: 110px;text-align:center;"><a href="#" onclick="confirmDelete(&quot;' + id + '&quot;,&quot;table.php&quot;)">删&nbsp;&nbsp;&nbsp;&nbsp;除</a></div>' +
					'</td></tr>' +
			        '<tr><td>' +
                    '<div class="pop_action-span1" style="margin-right: 110px;text-align:center;"><a href="#">下&nbsp;&nbsp;&nbsp;&nbsp;单</a></div>' +
					'</td></tr>' +
  				    '<tr><td>' +
                    '<div class="pop_action-span1" style="margin-right: 110px;text-align:center;"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></div>' +
  					'</td></tr></table>' +
                  '</div>' +
            '</form>';
    '</div>'
    showMessageBox(content, 342, 350);
}

//function submitTableData() {
//    var tableCode = document.getElementById("tableCode").value;  

//    if (tableCode == undefined || tableCode == null || tableCode == "") {
//        alert("餐桌号不能为空！");
//        return;      
//    }
//    document.tableForm.submit();
//}

function statTable(totalNumber, isPaidNumber) {
    document.getElementById("total_Table").innerText = totalNumber;
    document.getElementById("used_Table").innerText = totalNumber - isPaidNumber;
    document.getElementById("idle_Table").innerText = isPaidNumber;
}

function addTableKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitTableData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}
function showHideCondition(select) {
    //    alert(select);
    var option = select.options[select.selectedIndex];
    document.getElementById("condition_type").style.display = "none";
    document.getElementById("keyword").style.display = "inline";
    document.getElementById("keyword").value = "";
    if (option.value == "status") {
        document.getElementById("condition_type").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }
}
function initializeTable() {
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
    if (keyword_type_value == "status") {
        document.getElementById("condition_type").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }
}
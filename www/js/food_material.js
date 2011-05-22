function editFoodMaterial(food_id, material_id, material_name, consumption, materials) {
    var editType = "addFoodMaterial";
    var title = "添加关联食材";
    if (material_name != "") {
        editType = "editFoodMaterial";
        title = "修改关联食材";
    }   
    var material = "";
    if (material_name == "") {
        var ms = "";
        var arr1 = materials.split("@");
        for (var i = 0; i < arr1.length; i++) {
            var arr2 = arr1[i].split("|");
            ms += (' <option value="' + arr2[0] + '">' + arr2[1] + '</option>');
        }
        material = '<select id="sel_material" name="sel_material" style="width:85px;">' +
                                ms +
								'</select>'
    }
    else {
        material = material_name;
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="foodMaterialForm" action="food_material.php?id=' + food_id + '"  method="post" onkeydown="editFoodMaterialKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="food_id" value="' + food_id + '" />' +
	                      '<input type="hidden" name="material_id" value="' + material_id + '" />' +
	                      '<div class="add_foot_Content" style="height:180px;text-align:center">' +
	                        '<div class="pop_Content">' +	                           
	                            '<div class="pop_Content1">食材：' + material + '</div>' +
	                            '<div class="pop_Content1">消耗：<input type="text" id="consumption" name="consumption" value="' + consumption + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitFoodMaterialData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    if (material_name == "") {
        if (material_id != "") {
            document.getElementById("sel_material").value = material_id;
        }
        document.getElementById("sel_material").focus();
    }
    else {
        document.getElementById("consumption").focus();
    }
}

function editFoodMaterialKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitFoodMaterialData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function submitFoodMaterialData() {   
    var consumption = document.getElementById("consumption").value;  
    if (consumption == undefined || consumption == null || consumption == "") {
        alert("消耗数量不能为空！");
        document.getElementById("consumption").focus();
        return;
    }
    var id = parseInt(consumption);
    if (id < 0) {
        alert("消耗数量只能输入大于0的数字！");
        document.getElementById("consumption").focus();
        return;
    }
    var sel_material = document.getElementById("sel_material");
    if (sel_material != undefined && sel_material != null) {
        document.getElementById("material_id").value = document.getElementById("sel_material").value;
    }
    document.foodMaterialForm.submit();
}

function deleteFoodMaterial(food_id,material_id) {
    if (confirm("确认删除？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "food_material.php?id=" + food_id;
        formDelete.method = "post";
        var f_id = document.createElement("input");
        f_id.name = "food_id";
        f_id.value = food_id;
        f_id.type = "hidden";
        formDelete.appendChild(f_id);
        var m_id = document.createElement("input");
        m_id.name = "material_id";
        m_id.value = material_id;
        m_id.type = "hidden";
        formDelete.appendChild(m_id);
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "deleteFoodMaterial";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }
}


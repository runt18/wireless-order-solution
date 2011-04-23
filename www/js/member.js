function editMember(id, alias_id, name, birth, tele, exchange_rate, discount_type) {
    var editType = "addMember";
    var title = "添加会员";
    if (id != "") {
        editType = "editMember";
        title = "修改会员";
    }
    var aliasId = "";
    if (id == "") {
        aliasId = '<input type="text" id="alias_id" name="alias_id" value="' + alias_id + '" size="25" height="20" onfocus="this.select()" ' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" />';
        exchange_rate = "0.00";
        discount_type = "0";
    }
    else {
        aliasId = '<input type="hidden" id="alias_id" name="alias_id" value="' + alias_id + '"/>' + alias_id + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="memberForm" action="member.php"  method="post" onkeydown="editMemberKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<div class="add_foot_Content" style="height:230px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">编号：' + aliasId + '</div>' +
	                            '<div class="pop_Content1">姓名：<input type="text" id="name" name="name" value="' + name + '" onfocus="this.select()" size="25" height="20" /></div>' +
	                            '<div class="pop_Content1">出生日期：<input type="text" id="birth" name="birth" value="' + birth + '" size="25" height="20" onclick="javascript:ShowCalendar(this.id)" onfocus="this.select()" style="position: relative; right: -14px; width: 150px;"/></div>' +
	                            '<div class="pop_Content1">电话：<input type="text" id="tele" name="tele" value="' + tele + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1">兑换折扣：<input type="text" id="exchange_rate" name="exchange_rate" value="' + exchange_rate + '" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1">折扣方式：<select style="width: 90px;position: relative; right: -2px;" id="discount_type" name="discount_type" ' +
	                            '><option value="0">会员折扣1</option><option value="1">会员折扣2</option></select></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitMemberData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
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
    document.all.discount_type.value = discount_type;
}

function submitMemberData() {
    var alias_id = document.getElementById("alias_id").value;
    var name = document.getElementById("name").value;
    var birth = document.getElementById("birth").value;
    var tele = document.getElementById("tele").value;
    var exchange_rate = document.getElementById("exchange_rate").value;
    if (alias_id == undefined || alias_id == null || alias_id == "") {
        alert("编号不能为空！");
        return;
    }
    if (name == undefined || name == null || name == "") {
        alert("姓名不能为空！");
        return;
    }
    if (birth == undefined || birth == null || birth == "") {
        alert("出生日期不能为空！");
        return;
    }
    if (tele == undefined || tele == null || tele == "") {
        alert("电话不能为空！");
        return;
    }
    if (exchange_rate == undefined || exchange_rate == null || exchange_rate == "") {
        alert("兑换折扣！");
        return;
    }
    //    var dis = parseFloat(discount);
    //    if (dis > 1 || dis < 0) {
    //        alert("一般折扣的输入范围是0~1")
    //        return;
    //    }

    document.memberForm.submit();
}

function editMemberKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitMemberData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function deleteMember(id) {
    if (confirm("确认删除？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "member.php";
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "id";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "deleteMember";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }

}

function showHideCondition(select) {
    var option = select.options[select.selectedIndex];
    document.getElementById("condition_type").style.display = "none";
    document.getElementById("discount_type").style.display = "none";
    document.getElementById("keyword").style.display = "inline";
    document.getElementById("keyword").value = "";
    if (option.value == "balance" || option.value == "expenditure") {
        document.getElementById("condition_type").style.display = "inline";
    }
    if (option.value == "discount_type") {
        document.getElementById("discount_type").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }
}
function initializeMember() {
    var keyword_type = document.getElementById("keyword_type");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var condition_type = document.getElementById("condition_type");
    var condition_type_value = document.getElementById("condition_type_value").value;
    var discount_type = document.getElementById("discount_type");
    var discount_type_value = document.getElementById("discount_type_value").value;
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
    for (var i = 0; i < discount_type.options.length; i++) {
        if (discount_type.options[i].value == discount_type_value) {
            discount_type.options[i].selected = true;
            break;
        }
    }
    keyword.value = keyword_value;
    if (keyword_type_value == "balance" || keyword_type_value == "expenditure") {
        document.getElementById("condition_type").style.display = "inline";
    }
    if (keyword_type_value == "discount_type") {
        document.getElementById("discount_type").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }
}

function recharge(id, name) {
    var editType = "recharge";
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">会员充值 - ' + name + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="memberForm" action="member.php"  method="post" onkeydown="rechargeKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<div class="add_foot_Content" style="height:130px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">金额：<input type="text" id="money" name="money" onfocus="this.select()" size="25" height="20"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitRechargeData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("money").focus();
}

function rechargeKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitRechargeData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function submitRechargeData() {
    var money = document.getElementById("money").value;
    if (money == undefined || money == null || money == "") {
        alert("冲值金额不能为空！");
        return;
    }
    var m = parseFloat(money);
    if (m <= 0) {
        alert("冲值金额必须大于0！")
        return;
    }
    document.memberForm.submit();
}

function viewDetail(id, name) {
    var editType = "viewDetail";
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">明细 - 请选择类型和区间</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="memberForm" action="member.php"  method="post" onkeydown="editMemberKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<input type="hidden" name="name" value="' + name + '" />' +
	                      '<div class="add_foot_Content" style="height:130px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                          '<div class="pop_Content1" style="padding-left:0px;text-align:center"><input type="radio" checked="checked" name="viewType" value="recharge" />冲值&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="viewType" value="expenditure" />消费</div>' +
	                           '<div class="pop_Content1" style="padding-left:0px;text-align:center">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:100px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:100px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitViewDetail()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
}
function submitViewDetail() {   
    document.memberForm.submit();
}

function viewRecharge(id,name,dateFrom,dateTo) {
    var content = ' <div class="add_foot" style="height:550px;width:100%">' +
                              '<div class="title" style="width:100%">' +
	                          '<div class="title_left" style="width:92%"><font class="font" style="width:200px">充值明细 - ' + name + '</font></div>' +
	                          '<div class="title_right" style="width:8%;float:left" ></div>' +
	                          '</div>' +
	                          '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' +
	                              '<iframe src="viewRecharge.php?id=' + id + '&dateFrom=' + dateFrom + '&dateTo=' + dateTo + '" scrolling="no" style="width:100%;height:100%" />' +
	                          '</div>' +
						   '</div>';
    showMessageBox(content, 450, 350);


}


function viewExpenditure(id, name, dateFrom, dateTo) {
    var content = ' <div class="add_foot" style="height:550px;width:100%">' +
                              '<div class="title" style="width:100%">' +
	                          '<div class="title_left" style="width:92%"><font class="font" style="width:200px">消费明细 - ' + name + '</font></div>' +
	                          '<div class="title_right" style="width:8%;float:left" ></div>' +
	                          '</div>' +
	                          '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' +
	                              '<iframe src="viewExpenditure.php?id=' + id + '&dateFrom=' + dateFrom + '&dateTo=' + dateTo + '" scrolling="no" style="width:100%;height:100%" />' +
	                          '</div>' +
						   '</div>';
    showMessageBox(content, 450, 350);

}

function viewRechargeKeyDown() {
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        parent.closeWindow();
    }
}
function viewExpenditureKeyDown() {
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        parent.closeWindow();
    }
}
function showHideCondition(select,withClear) {
    var condition_type = document.getElementById("condition_type");
    condition_type.style.display = "none";
    var status_type = document.getElementById("status_type");
    status_type.style.display = "none";
    var keyword = document.getElementById("keyword");    
    keyword.style.display = "none";
    keyword.onclick = null;
//    keyword.onblur = null;
    var option = select.options[select.selectedIndex];
    if (option.value == "is_status") {
        status_type.style.display = "inline";
    }
    else if (option.value == "is_expire_date") {
        condition_type.style.display = "inline";
        keyword.style.display = "inline";
        keyword.onclick = showCal;
//        keyword.onblur = hidCal;
    }
    else {
        keyword.style.display = "inline";
    }
    if (withClear) {
        keyword.value = "";
    }
}
function showCal() {
    ShowCalendar('keyword');
}
//function hidCal() {
//    if(document.all.Calendar)
//    {
//        document.all.Calendar.style.visibility='hidden';
//    }
//}

function ininTerminalOriginal() {
    var keyword_type = document.getElementById("keyword_type");
    var condition_type = document.getElementById("condition_type");
    var status_type = document.getElementById("status_type");
    var keyword = document.getElementById("keyword");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var condition_type_value = document.getElementById("condition_type_value").value;
    var status_type_value = document.getElementById("status_type_value");
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
    for (var i = 0; i < status_type.options.length; i++) {
        if (status_type.options[i].value == status_type_value) {
            status_type.options[i].selected = true;
            break;
        }
    }
    keyword.value = keyword_value;
    showHideCondition(keyword_type, false);
}

function statTerminal(totalWork, totalIdle) {
    var useRate = 0;
    if ((totalWork + totalIdle) != 0) {
        useRate = totalWork / (totalWork + totalIdle)*100;
    }
    var totalIdleMonth = totalIdle/3600/24/30;
    var totalWorkMonth = totalWork/3600/24/30;
    document.getElementById("terminalStat").innerText = "空闲(月)：" + totalIdleMonth.toFixed(1) + "   使用(月)：" + totalWorkMonth.toFixed(1) + "  使用率：" + useRate.toFixed(1) + "%";
}

function addTerminal(pin, new_pin, model_name,owner_name) {
    var editType = "addTerminal";
    var title = "添加终端";    
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:160px">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="terminalForm" action="admin_terminal.php"  method="post" onkeydown="addTerminalKeyDown()">' +
	                        '<input type="hidden" name="editType" value="' + editType + '" />' +	                      	     	                	                          
	                      '<div class="add_foot_Content" >' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">PIN：&nbsp;&nbsp;<input type="text" id="new_pin" name="new_pin" value="' + new_pin + '" size="25" height="20" onfocus="this.select()" /></div>' +
	                            '<div class="pop_Content1">型号：<input type="text" id="model_name" name="model_name" value="' + model_name + '" size="25" height="20" onfocus="this.select()" /></div>' +                           
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitTerminalData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("new_pin").focus();     
}

function editTerminal(pin, new_pin, model_name,owner_name) {
    var editType = "editTerminal";
    var title = '修改（' + pin + '）';
//    var pinedit = "";       
//    if (pin != "") {
//        editType = "editTerminal";
//        title = '修改（' + pin + '）';
//        pinedit = '<input type="hidden" name="pin" value="' + new_pin + '" /> <div class="pop_Content1">PIN：' + new_pin + '</div>';       
//    }
//    else
//    {
//        pinedit = '<div class="pop_Content1">PIN：&nbsp;&nbsp;<input type="text" id="new_pin" name="new_pin" value="' + new_pin + '" size="25" height="20" onfocus="this.select()" /></div>';  
//    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:160px">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="terminalForm" action="admin_terminal.php"  method="post" onkeydown="addTerminalKeyDown()">' +
	                        '<input type="hidden" name="editType" value="' + editType + '" />' +	     	                
	                         '<input type="hidden" name="pin" value="' + pin + '" />' +	  
	                         '<input type="hidden" name="new_pin" value="' + new_pin + '" />'  +            
	                      '<div class="add_foot_Content" >' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">PIN：&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + new_pin + '</div>' +
//	                            '<div class="pop_Content1">PIN：&nbsp;&nbsp;<input type="text" id="new_pin" name="new_pin" value="' + new_pin + '" size="25" height="20" onfocus="this.select()" /></div>' +
                                '<div class="pop_Content1">持有人：<input type="text" id="owner_name" name="owner_name" value="' + owner_name + '" size="25" height="20" onfocus="this.select()" style="width: 150px;"/></div>' +                           
	                            '<div class="pop_Content1">型号：<input type="text" id="model_name" name="model_name" value="' + model_name + '" size="25" height="20" onfocus="this.select()" style="position: relative; right: -14px; width: 150px;"/></div>' +                           	                            
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitTerminalData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
//    if(pin != "")
//    {
         document.getElementById("owner_name").focus();
//    }
//    else
//    {
//        document.getElementById("new_pin").focus();    
//    }   
}

function submitTerminalData() {
    var pin = document.getElementById("new_pin").value;
    var model_name = document.getElementById("model_name").value;

    if (pin == undefined || pin == null || pin == "") {
        alert("PIN码不能为空！");
        return;
    }
    if (model_name == undefined || model_name == null || model_name == "") {
        alert("型号不能为空！");
        return;
    }  
    document.terminalForm.submit();
}
function addTerminalKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitTerminalData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}


function editUsingTerminal(pin,new_pin, restaurant_id, model_name,expire_date,owner_name) {       
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:160px">修改（' + pin + '）</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="terminalForm" action="admin_terminal.php"  method="post" onkeydown="addTerminalKeyDown()">' +
	                        '<input type="hidden" name="editType" value="editUsingTerminal" />' +	
	                        '<input type="hidden" name="pin" value="' + pin + '" />' +	 
	                         '<input type="hidden" name="new_pin" value="' + new_pin + '" />' +	                      
	                      '<div class="add_foot_Content" style="height:200px;" >' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">PIN：&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + new_pin + '</div>' +
	                           '<input type="text" id="new_pin" name="new_pin" value="' + new_pin + '" size="25" height="20" style="display:none;" />' +
	                            '<div class="pop_Content1">餐厅编号：<input type="text" id="restaurant_id" name="restaurant_id" value="' + restaurant_id + '" size="25" height="20" onfocus="this.select()" style="width:150px;"onkeypress="return event.keyCode>=48&&event.keyCode<=57" style="ime-mode:Disabled" /></div>' + 
	                            '<div class="pop_Content1">持有人：<input type="text" id="owner_name" name="owner_name" value="' + owner_name + '" size="25" height="20" onfocus="this.select()" style="position: relative; right: -14px; width: 150px;"/></div>' +                           
	                            '<div class="pop_Content1">型号：<input type="text" id="model_name" name="model_name" value="' + model_name + '" size="25" height="20" onfocus="this.select()" style="position: relative; right: -28px; width: 150px;"/></div>' + 
	                            '<div class="pop_Content1">有效期：<input type="text" id="expire_date" name="expire_date" value="' + expire_date + '" size="25" height="20" onclick="javascript:ShowCalendar(this.id)" onfocus="this.select()" style="position: relative; right: -14px; width: 150px;"/></div>' +                           
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitUsingTerminalData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
//    document.getElementById("new_pin").focus();
    document.getElementById("restaurant_id").focus();
}
function submitUsingTerminalData() {
    var new_pin = document.getElementById("new_pin").value;
    var restaurant_id = document.getElementById("restaurant_id").value;    
    var model_name = document.getElementById("model_name").value;
    var expire_date = document.getElementById("expire_date").value;

    if (new_pin == undefined || new_pin == null || new_pin == "") {
        alert("PIN码不能为空！");
        return;
    }
    if (restaurant_id == undefined || restaurant_id == null || restaurant_id == "") {
        alert("餐厅编号不能为空！");
        return;
    }   
    if (model_name == undefined || model_name == null || model_name == "") {
        alert("型号不能为空！");
        return;
    }  
    if (expire_date == undefined || expire_date == null || expire_date == "") {
        alert("有效期不能为空！");
        return;
    }
    if(restaurant_id < 11)
    {
        alert("餐厅编号只能输入大于10的整数！");
        return;
    }
    document.terminalForm.submit();
}

function installTerminal(pin, restaurant_id,expire_date) {   
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                         '<div class="title_left"><font class="font" style="width:160px">挂载（' + pin + '）</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="terminalForm" action="admin_terminal.php"  method="post" onkeydown="installTerminalKeyDown()">' +
	                        '<input type="hidden" name="editType" value="installTerminal" />' +	 
	                         '<input type="hidden" name="pin" value="' + pin + '" />' +	                        
	                      '<div class="add_foot_Content" >' +
	                        '<div class="pop_Content">' +
	                             '<div class="pop_Content1">餐厅编号：<input type="text" id="restaurant_id" name="restaurant_id" value="' + restaurant_id + '" size="25" height="20" onfocus="this.select()" onkeypress="return event.keyCode>=48&&event.keyCode<=57" style="ime-mode:Disabled" /></div>' + 
	                             '<div class="pop_Content1">有效期：<input type="text" id="expire_date" name="expire_date" value="' + expire_date + '" size="25" height="20" onclick="javascript:ShowCalendar(this.id)" onfocus="this.select()" style="right: -13px; position: relative;"/></div>' +                           
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitInstallTerminalData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("restaurant_id").focus();
}

function submitInstallTerminalData() {  
    var restaurant_id = document.getElementById("restaurant_id").value;      
    var expire_date = document.getElementById("expire_date").value;
    
    if (restaurant_id == undefined || restaurant_id == null || restaurant_id == "") {
        alert("餐厅编号不能为空！");
        return;
    }    
    if (expire_date == undefined || expire_date == null || expire_date == "") {
        alert("有效期不能为空！");
        return;
    }
    if(restaurant_id < 11)
    {
        alert("餐厅编号只能输入大于10的整数！");
        return;
    }
    document.terminalForm.submit();
}

function installTerminalKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitInstallTerminalData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}


function setOwner(pin,owner_name)
{
      var content = ' <div class="add_foot">' +
            '<div class="title">' +
                 '<div class="title_left"><font class="font" style="width:160px">设定持有人（' + pin + '）</font></div>' +                
                '<div class="title_right"></div>' +
            '</div>' +
            '<form name="ownerForm" action="terminal.php"  method="post" onkeydown="setOwnerKeyDown()">' +	 
                 '<input type="hidden" name="editType" value="setOwner" />' +          
                  '<input type="hidden" name="pin" value="' + pin + '" />' +          
                  '<div class="add_foot_Content">' + 
                    '<div class="pop_Content">' +                        
                        '<div class="pop_Content1">持有人：<input type="text" id="owner_name" name="owner_name" size="25" value="' + owner_name + '"height="20" onfocus="this.select()" /></div>' +	                           
                    '</div>' +
                    '<span class="pop_action-span"><a href="#" onclick="ownerForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                    '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                  '</div>' +
            '</form>' +
            '</div>';
    showMessageBox(content,342,350);
    document.getElementById("owner_name").focus();
}
function setOwnerKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        ownerForm.submit();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}


function viewTerminal(pin,restaurant_id,model_name,expire_date,status,entry_date,work_month,idle_month,discard_date,use_rate,owner_name)
{
    var hidden = "";
    if(discard_date == "")
    {
        hidden = ' style="display:none"';
    }
    var content = ' <div class="add_foot" style="width:300px">' +
                        '<div class="title" style="width:302px">' +
                             '<div class="title_left" style="width:272px;"><font class="font" style="width:160px">查看（' + pin + '）</font></div>' +    	                        
	                        '<div class="title_right"></div>' +
	                    '</div>' +	                	                    
	                      '<div class="add_foot_Content" style="height:350px;text-align:center;width:300px">' +
	                        '<div class="pop_Content" style="width:300px" >' +
	                            '<div class="pop_Content1">PIN：' + pin + '</div>' +
	                            '<div class="pop_Content1">餐厅编号：' + restaurant_id + '</div>' +
	                            '<div class="pop_Content1">型号：' + model_name + '</div>' +
	                            '<div class="pop_Content1">有效期：' + expire_date + '</div>' +
	                            '<div class="pop_Content1">状态：' + status + '</div>' +
	                            '<div class="pop_Content1">持有人：' + owner_name + '</div>' +
	                            '<div class="pop_Content1">添加日期：' + entry_date + '</div>' +
	                            '<div class="pop_Content1">使用时间：' + work_month + '月</div>' +
	                            '<div class="pop_Content1">空闲时间：' + idle_month + '月</div>' +
	                            '<div class="pop_Content1"' + hidden + '>废弃日期：' + discard_date + '</div>' +
	                            '<div class="pop_Content1">使用率：' + use_rate + '%</div>' +
	                        '</div>' +	                        
	                        '<span class="pop_action-span1"><a href="#" onclick="closeWindow()" style="width:100px;margin-top:-10px;margin-left: -60px;">确&nbsp;&nbsp;&nbsp;&nbsp;定</a></span>' +
	                      '</div>' +	         
	                '</div>';
    showMessageBox(content, 400, 400);    
}

function changeTerminalStatus(pin,destinationStatus,desc)
{
    if(confirm("确定"+desc+"终端？"))
    {        
            var formDelete = document.createElement("form");
            formDelete.action = "admin_terminal.php";
            formDelete.method = "post";
            var deleteId = document.createElement("input");
            deleteId.name = "pin";
            deleteId.value = pin;
            deleteId.type = "hidden";
            formDelete.appendChild(deleteId);
            var editType = document.createElement("input");
            editType.name = "editType";
            editType.value = destinationStatus;
            editType.type = "hidden";
            formDelete.appendChild(editType);
            document.body.appendChild(formDelete);
            formDelete.submit();                           
    }
    
}

function deleteTerminal(pin)
{
    if(confirm("确认删除？"))
    {
        var formDelete = document.createElement("form");
            formDelete.action = "admin_terminal.php";
            formDelete.method = "post";
            var deleteId = document.createElement("input");
            deleteId.name = "pin";
            deleteId.value = pin;
            deleteId.type = "hidden";
            formDelete.appendChild(deleteId);
            var editType = document.createElement("input");
            editType.name = "editType";
            editType.value = "deleteTerminal";
            editType.type = "hidden";
            formDelete.appendChild(editType);
            document.body.appendChild(formDelete);
            formDelete.submit();          
    }
    
}
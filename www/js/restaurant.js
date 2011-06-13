function viewRestaurant(id,account,restaurant_name,record_alive,order_num,terminal_num,food_num,table_num,order_paid,table_using) {
//    alert(order_num);
    var ra = "永久";
    if(record_alive != "0")
    {
        ra = record_alive + "天";
    }
    var order_notPaid = parseInt(order_num)-parseInt(order_paid);
    var table_notUsing = parseInt(table_num)-parseInt(table_using);
    var content = ' <div class="add_foot" style="width:400px">' +
                        '<div class="title" style="width:400px">' +
	                        '<div class="title_left" style="width:370px"><font class="font" style="width:350px;">查看（' + restaurant_name + '）</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +	                	                    
	                      '<div class="add_foot_Content" style="height:290px;text-align:center;width:398px">' +
	                        '<div class="pop_Content" style="width:398px" >' +
	                            '<div class="pop_Content1">编号：' + id + '</div>' +
	                            '<div class="pop_Content1">餐厅名：' + restaurant_name + '</div>' +
	                            '<div class="pop_Content1">帐户名：' + account + '</div>' +
	                            '<div class="pop_Content1">帐单有效期：' + ra + '</div>' +
	                            '<div class="pop_Content1">帐单数量：' + order_num + '（总数），' + order_paid + '（已结帐），' + order_notPaid + '（未结帐）</div>' +
	                            '<div class="pop_Content1">终端数量：' + terminal_num + '</div>' +
	                            '<div class="pop_Content1">菜谱数量：' + food_num + '</div>' +
	                            '<div class="pop_Content1">餐台数：' + table_num + '（总数），' + table_using + '（就餐），' + table_notUsing + '（空闲）</div>' +
	                        '</div>' +	                        
	                        '<span class="pop_action-span"><a href="#" onclick="closeWindow()" style="width:100px; position: relative; left: 90px;">确&nbsp;&nbsp;&nbsp;&nbsp;定</a></span>' +
	                      '</div>' +	         
	                '</div>';
    showMessageBox(content, 400, 400);
}

function editRestaurant(id, account, restaurant_name,tele1,tele2,address, restaurant_info, record_alive,random_num,old_account) {
    var editType = "addRestaurant";
    var title = "添加餐厅";   
//    var isDisable = "";    
    if(id != "")
    {
        editType = "editAdminRestaurant";
        title = "修改（" + restaurant_name + "）";
//        isDisable = 'disabled = "true"';
    }    
    else
    {
        record_alive = "90";
        random_num = "";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">' + title + '</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="restaurantForm" action="admin_restaurant.php"  method="post" onkeydown="editRestaurantKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<input type="hidden" name="random_num" value="' + random_num + '" />' +
	                      '<input type="hidden" name="old_account" value="' + old_account + '" />' +
	                      '<div class="add_foot_Content" style="height:400px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                            '<div class="pop_Content1">帐户名：<input type="text" id="account" name="account" value="' + account + '" size="25" height="20" onfocus="this.select()" style="position: relative; right: -30px; width: 120px;"/></div>' +
	                            '<div class="pop_Content1">新密码：<input type="password" id="newPassword" name="newPassword" value="' + random_num +'" onfocus="this.select()"  style="position: relative; right: -30px; width: 120px;"/></div>' +
	                            '<div class="pop_Content1">确认新密码：<input type="password" id="confirmPassword" name="confirmPassword" value="' + random_num +'" onfocus="this.select()"  style="position: relative; right: -2px; width: 120px;"/></div>' +
	                            '<div class="pop_Content1">餐厅名：<input type="text" id="restaurant_name" name="restaurant_name" value="' + restaurant_name + '" onfocus="this.select()" style="position: relative; right: -30px; width: 120px;"/></div>' +
	                            '<div class="pop_Content1">电话1：<input type="text" id="tele1" name="tele1" value="' + tele1 + '" onfocus="this.select()" size="25" height="20" style="position: relative; right: -35px; width: 120px;"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1">电话2：<input type="text" id="tele2" name="tele2" value="' + tele2 + '" onfocus="this.select()" size="25" height="20" style="position: relative; right: -35px; width: 120px;"' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                            '<div class="pop_Content1">地址：<input type="text" id="address" name="address" value="' + address + '" onfocus="this.select()" style="position: relative; right: -43px; width: 120px;"/></div>' +
	                            '<div class="pop_Content1">帐单有效期：<select style="width: 70px;position: relative; right: -2px;" id="record_alive" name="record_alive" value="' + record_alive + 
	                            '"><option value="90">90天</option><option value="180">180天</option><option value="360">360天</option><option value="0">永久</option></select></div>' +
	                            '<div class="pop_Content1">餐厅信息：</div>' +
	                            '<div class="pop_Content1"><textarea id="restaurant_info" name="restaurant_info" style="height:80px;width:220px" onkeydown="cancelSubmit()">' + restaurant_info.replace(/<br \/>/g,"\r\n") + '</textarea></div>' +	                         
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitRestaurantData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';
    
    showMessageBox(content, 342, 350);
//    if(id != "")
//    {
//        document.getElementById("newPassword").focus();        
//    }
//    else
//    {
        document.getElementById("account").focus();     
//    }    
    document.all.record_alive.value=record_alive;
}

function editRestaurantKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitRestaurantData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function deleteRestaurant(id)
{
    if(confirm("删除餐厅时，会删除与之有关的菜谱、帐单和餐台数据，是否继续？"))
    {                    
            var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font">输入密码</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="restaurantForm" action="admin_restaurant.php"  method="post">' +
	                      '<input type="hidden" name="editType" value="deleteRestaurant" />' +	    
	                      '<input type="hidden" name="id" value="' + id + '" />' +                 
	                      '<div class="add_foot_Content" style="height:100px;text-align:center">' +
	                        '<div class="pop_Content">' +	                       
	                           '<div class="pop_Content1" style="position: relative; left: 20px;">密码：<input type="password" id="password" name="password"  /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="restaurantForm.submit()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';
            showMessageBox(content, 342, 350);      
            document.getElementById("password").focus();  
    }
    
}

function submitRestaurantData()
{
    var account = document.getElementById("account").value;
    var newPassword = document.getElementById("newPassword").value;
    var confirmPassword = document.getElementById("confirmPassword").value;
    var restaurant_name = document.getElementById("restaurant_name").value;       
    var restaurant_info = document.getElementById("restaurant_info").value;
    
    if (account == undefined || account == null || account == "") {
        alert("帐户名不能为空！");
        return;
    }
    if (newPassword == undefined || newPassword == null || newPassword == "") {
        alert("新密码不能为空！");
        return;
    }
    if (newPassword.length > 30) {
        alert("新密码的最大长度不能超过30个英文字符！");
        return;
    }
    if (confirmPassword != newPassword) {
        alert("两次输入的新密码不一致！");
        return;
    }  
    if (restaurant_name == undefined || restaurant_name == null || restaurant_name == "") {
        alert("餐厅名不能为空！");
        return;
    }    
    document.restaurantForm.submit();
}
function editInfo(restaurant_info)
{
     var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font">编辑信息</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="restaurantForm" action="admin_restaurant.php"  method="post" onkeydown="editInfoKeyDown()">' +
	                      '<input type="hidden" name="editType" value="editInfo" />' +	                     
	                      '<div class="add_foot_Content" style="height:180px;text-align:center">' +
	                        '<div class="pop_Content">' +	                       
	                            '<div class="pop_Content1"><textarea id="restaurant_info" name="restaurant_info" style="height:80px;width:230px" onkeydown="cancelSubmit()">' + restaurant_info.replace(/<br \/>/g,"\r\n") + '</textarea></div>' +	                         
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitEditInfo()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';
    showMessageBox(content, 342, 350);
    document.getElementById("restaurant_info").focus(); 
}
function submitEditInfo()
{
    document.restaurantForm.submit();
}

function editInfoKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitEditInfo();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function ininRestaurantOriginal() {
    var keyword_type = document.getElementById("keyword_type"); 
    var keyword = document.getElementById("keyword");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var keyword_value = document.getElementById("keyword_value").value;
    for (var i = 0; i < keyword_type.options.length; i++) {
        if (keyword_type.options[i].value == keyword_type_value) {
            keyword_type.options[i].selected = true;
            break;
        }
    }   
    keyword.value = keyword_value;    
}
function cancelSubmit()
{
    if (event.keyCode == 13) {
        window.event.cancelBubble = true;
    }
}
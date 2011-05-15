function moveAllItem(sourceId, destinationId) {
    var source = document.getElementById(sourceId);
    var destination = document.getElementById(destinationId);
    for (var i = 0; i < source.options.length; i++) {
        var op = document.createElement("option");
        op.text = source.options[i].text;
        op.value = source.options[i].value;
        try {
            destination.add(op, null);
        }
        catch (ex) {
            destination.add(op);
        }
    }
    for (var i = source.options.length - 1; i >= 0; i--) {
        source.remove(i);
    }
}
function moveSelectedItem(sourceId, destinationId) {
    var source = document.getElementById(sourceId);
    var destination = document.getElementById(destinationId);
    var addList = new Array();
    for (var i = 0; i < source.options.length; i++) {
        var option = source.options[i];
        if (option.selected) {
            var op = document.createElement("option");
            op.text = option.text;
            op.value = option.value;
            addList.push(i);
            try {
                destination.add(op, null);
            }
            catch (ex) {
                destination.add(op);
            }
        }
    }
    for (var i = addList.length - 1; i >= 0; i--) {
        source.remove(addList[i]);
    }
}
function getSelectedItem(id, formId) {
    //    debugger;
    var form = document.getElementById(formId);
    var list = document.getElementById(id);
    var ids = "";
    for (var i = 0; i < list.options.length; i++) {
        if (i != 0) {
            ids += ",";
        }
        var option = list.options[i];
        ids += option.value;
    }
    var hidIds = document.createElement("input");
    hidIds.name = "ids";
    hidIds.value = ids;
    hidIds.type = "hidden";
    form.appendChild(hidIds);
    return ids;
}

function submitSearch(msg) {
    if (getSelectedItem("selectedList", "searchForm") == "") {
        alert("请至少选择一种" + msg + "进行统计！");
        event.returnValue = false;
        event.cancel = true;
        return;
    }
    document.getElementById("searchForm").submit();
}
function iframeKeyDown() {
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        parent.closeWindow();
    }
}

function preview(oper) {
    if (oper < 10) {
        bdhtml = window.document.body.innerHTML; //获取当前页的html代码
        sprnstr = "<!--startprint" + oper + "-->"; //设置打印开始区域
        eprnstr = "<!--endprint" + oper + "-->"; //设置打印结束区域
        prnhtml = bdhtml.substring(bdhtml.indexOf(sprnstr) + 18); //从开始代码向后取html

        prnhtml = prnhtml.substring(0, prnhtml.indexOf(eprnstr)); //从结束代码向前取html
        window.document.body.innerHTML = prnhtml;
        window.print();
        window.document.body.innerHTML = bdhtml;


    } else {
        window.print();
    }

}

function printsetup() {
    // 打印页面设置 
    wb.execwb(8, 1);
}
function printpreview() {
    // 打印页面预览 

    wb.execwb(7, 1);

} 


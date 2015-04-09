
/*$(function(){
	Util.lm.show();

	$.ajax({
		url : '../../WXInterface.do',
		dataType : 'json',
		data : {
			dataSource : 'getConfig',
			url: location.href.split('#')[0],
			fid : Util.mp.fid
		},
		success : function(data, status, xhr){
			wx.config({
			    debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
			    appId: data.other.appId, // 必填，公众号的唯一标识
			    timestamp: data.other.timestamp, // 必填，生成签名的时间戳
			    nonceStr: data.other.nonceStr, // 必填，生成签名的随机串
			    signature: data.other.signature,// 必填，签名，见附录1
			    jsApiList: ["scanQRCode"] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
			});		
			Util.lm.hide();
		},
		error : function(xhr, errorType, error){
			alert('操作失误, 请重新进入');
		}
	});	
	
});*/

$.post('../../WXOperateMember.do', {
	dataSource:'inpour',
	oid : Util.mp.oid,
	fid : Util.mp.fid,
	orderId : 3064505
}, function(result){
	if(result.success){
//		document.getElementById('div4ScanMsg').innerHTML('会员支付成功!');
		$('#div4ScanMsg').html('会员支付成功!');
	}   	    	
});


/*var ajax = xhr({
    url:'../../WXInterface.do',
    data:{
    	"dataSource":"getConfig","url":location.href.split('#')[0]
    },
    method:'POST',
    success: function  (data) {
    	alert(typeof data)
        var obj = JSON.parse(data);
    	console.log(obj)
        //....
    }
});*/



// config信息验证后会执行ready方法，所有接口调用都必须在config接口获得结果之后，config是一个客户端的异步操作，所以如果需要在页面加载时就调用相关接口，则须把相关接口放在ready函数中调用来确保正确执行。对于用户触发时才调用的接口，则可以直接调用，不需要放在ready函数中。
wx.ready(function(){
	wx.scanQRCode({
	    needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
	    scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
	    success: function (res) {
/*	    	xhr({
	    	    url:'../../WXOperateMember.do',
	    	    data:{
		    		dataSource:'inpour',
		    		oid : Util.mp.oid,
		    		fid : Util.mp.fid,
		    		orderId : 4393328
	    	    },
	    	    method:'POST',
	    	    success: function  (data) {
	    	        var obj = JSON.parse(data);
		    		if(obj.success){
		    			document.getElementById('div4ScanMsg').innerHTML('会员支付成功!');
		    		}
	    	    }
	    	});*/
	    	$('#div4ScanMsg').html('正在处理信息...');
	    	$.post('../../WXOperateMember.do', {
	    		dataSource:'inpour',
	    		oid : Util.mp.oid,
	    		fid : Util.mp.fid,
	    		orderId : 4393330
	    	}, function(result){
	    		if(result.success){
//	    			document.getElementById('div4ScanMsg').innerHTML('会员支付成功!');
	    			$('#div4ScanMsg').html('会员支付成功!');
	    		}   	    	
	    	});
	    	
	    	var result = res.resultStr; // 当needResult 为 1 时，扫码返回的结果
		}
	});
});
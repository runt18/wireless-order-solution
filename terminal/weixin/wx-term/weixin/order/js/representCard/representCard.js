$(function(){
	initRepresentCard();
	function initRepresentCard(){
		$.ajax({
			url : '../../WxOperateRestaurant.do',
			type : 'post',
			data : {
				dataSource : 'representActive',
				fid : Util.mp.fid
			},
			datatype : 'json',
			success : function(data, status, req){
				if(data.success){
					showPoster(data);
				}else{
					alert('页面读取失败');
				}
			},
			error : function(req, status, err){
				console.log('error');
			}
		});
		
		//请求生成的二维码
		$.ajax({
			url : '../../WxOperateRepresent.do',
			type : 'post',
			data : {
				dataSource : 'qrCode',
				fid : Util.mp.fid,
				oid : Util.mp.oid
			},
			datatype : 'json',
			success : function(data, status, req){
				if(data.success){
					$("#qrCode_div_representCard").attr("src", "http://qr.liantu.com/api.php?text=" + data.msg );
				}else{
					alert(data.msg);
				}
			},
			error : function(req, status, err){
				console.log(err);
			}
		});
	}
	
	function showPoster(data){
		var backgroundHeight = $(window).height();
		var backgroundWidth = $(window).width();
		
		$('#background_div_representCard').css({
			'height' : backgroundHeight,
			'width' : backgroundWidth,
//			'background-color' : 'red',
			'position' : 'relative',
			'left' : '0px',
			'right' : '0px',
			'padding-top' : '5%',
			'background-image' : 'url("' + (data.root[0].image ? data.root[0].image.image : 'http://digie-image-test.oss.aliyuncs.com/WxRepresent/40/20160416154639937.jpg') + '")',
			'background-size' : '100% 100%'
		});
//
//		//背景图片
//		var backgroundDom = $('<div/>');
//		backgroundDom.css({
//			'height' : backgroundHeight,
//			'width' : backgroundWidth,
////			'background-color' : 'red',
//			'position' : 'relative',
//			'left' : '0px',
//			'right' : '0px',
//			'padding-top' : '5%',
//			'background-image' : 'url("images/represent-background.jpg")',
//			'background-size' : '100% 100%'
//		});
//		
//		
		//二维码
		var qrCode = $('#qrCode_div_representCard');
		qrCode.css({
			'width' : '550px',
			'position' : 'absolute',
			'left' : '0px',
			'bottom' : '0px'
		});
//		
//		
//		//活动标题
//		var activeTitle = $('<h1/>');
//		
		$('#title_h1_representCard').html(data.root[0].title);
//		
		$('#title_h1_representCard').css({
			'color' : 'rgb(224, 11, 19)',
			'font-family' : '微软雅黑',
			'width' : '100%',
			'text-align' : 'center',
			'margin' : '12% auto',
			'font-size' : '85px'
		});
//		
//		var descrition = $('<p/>');
//		
		$('#descrition_p_representCard').html(data.root[0].slogon);
		$('#descrition_p_representCard').css({
			'width' : '80%',
			'text-align' : 'center',
			'margin' : '0 auto',
			'font-size' : '50px',
			'font-family' : '微软雅黑',
			'color' : 'rgb(255, 127, 127)',
			'margin-top' : '20%',
			'font-weight' : 'bold'
		});
//		
//		backgroundDom.append(activeTitle).append(descrition).append(qrCode);
//		
//		$('body').append(backgroundDom);
	}
})
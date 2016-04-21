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
			'background-image' : 'url("' + data.root[0].image.image + '")',
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
			'height' : '520px',
			'width' : '550px',
			'background' : 'url("images/qrCode.jpg")',
			'background-size' : '100% 100%',
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
$(function(){
	initRepresentCard();
	function initRepresentCard(){
		//获取饭店的代言设置
		$.ajax({
			url : '../../WxOperateRepresent.do',
			type : 'post',
			data : {
				dataSource : 'getByCond',
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
		
		$.ajax({
			url : '../../WxOperateRestaurant.do',
			type : 'post',
			data : {
				dataSource : 'detail',
				fid : Util.mp.fid
			},
			datatype : 'json',
			success : function(data, status, err){
				if(data.success){
					$('#restaurantTips_span_representCard').html('【' + data.root[0].name + '】');
					$('#restaurantDecoration_span_representCard').html('成为【' + data.root[0].name + '】会员');
				}				
			},
			error : function(req, status, err){
				console.log(err);
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
					$("#qrCode_div_representCard").attr('src', 'http://qr.liantu.com/api.php?text=' + data.msg );
				}else{
					$("#qrCode_div_representCard").attr('src', '../order/images/qrCode.jpg' );
					alert(data.msg);
				}
			},
			error : function(req, status, err){
				console.log(err);
			}
		});
		
		//获取推荐人的信息
		$.ajax({
			url : '../../WxOperateRepresent.do',
			type : 'post',
			data : {
				dataSource : 'referrer',
				fid : Util.mp.fid,
				oid : Util.mp.oid
			},
			datatype : 'json',
			success : function(data, status, req){
				if(data.success){
					
					if(data.root[0].headimgurl){
						$('#headingPhoto_img_representCard').attr('src', data.root[0].headimgurl);
					}else{
						$('#headingPhoto_img_representCard').attr('src', 'http://wx.qlogo.cn/mmopen/dmwVvwWRJuBdrMQylJiaxqAMjxT9bDcViaQ4Q6ybvyUUnKQKJLiakaiaDsibhgWeOquBFvNHvasOic2afurSKwFeia7sfrORo1vdY7f/0');
					}
					
					if(data.root[0].nickname){
						$('#friendDecoration_span_represetnCard').html('好友【' + data.root[0].nickname + '】推荐你');
					}else{
						$('#friendDecoration_span_represetnCard').html('来自好友【wode】的推荐');
					}
					
					
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
		var containerHeight = document.documentElement.clientHeight;
		var containerWidth = document.documentElement.clientWidth;
		
		
		
		$('#container_div_representCard').css({
			'height' : containerHeight * 0.95,
			'width' : containerWidth
		});
		
		
		//活动海报
		$('#background_div_representCard').css({
			'height' : '75%',
			'width' : '100%',
//			'background-color' : 'red',
			'position' : 'relative',
			'left' : '0px',
			'right' : '0px',
			'padding-top' : '5%',
			'background-image' : 'url("' + (data.root[0].image ? data.root[0].image.image : 'http://digie-image-test.oss.aliyuncs.com/WxRepresent/40/20160416154639937.jpg') + '")',
			'background-size' : '100% 100%',
			'margin-bottom' : '2%'
		});
		
		//活动二维码
		var qrCode = $('#qrCode_div_representCard');
		qrCode.css({
			'width' : '30%',
			'position' : 'absolute',
			'left' : '0px',
			'border-right' : '1px solid #666',
			'padding' : '0 10%'
		});
		
		
		//活动标题
		$('#title_h1_representCard').html(data.root[0].title);

		$('#title_h1_representCard').css({
			'color' : 'rgb(224, 11, 19)',
			'font-family' : '微软雅黑',
			'width' : '100%',
			'text-align' : 'center',
			'margin' : '12% auto',
			'font-size' : '85px'
		});

		//活动详情
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
		
		//推荐人头像设置
		$('#headingPhoto_img_representCard').css({
			'width' : '25%',
			'padding' : '0 11%',
			'border-radius' : '50%'
		});
		
		$('#decoration_span_represetnCard').css({
			'font-size' : '214%',
			'width' : '50%',
			'color' : '#666'
		});
		
		$('#tips_span_representCard').css({
			'font-size' : '214%',
			'width' : '50%',
			'color' : '#666'	
		});
	}
})
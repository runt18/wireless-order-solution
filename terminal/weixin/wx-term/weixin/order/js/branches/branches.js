$(function(){
	var branches = {
		RESTAURANT : {val : 1, text : '餐厅'},
		GROUP : {val : 2, text : '集团'},
		BRANCE : {val : 3, text : '门店'}
	}
	
	$.ajax({
		url : '../../WxOperateRestaurant.do',
		type : 'post',
		data : {
			dataSource : 'detail',
			fid : Util.mp.fid,
			sessionId : Util.mp.params.sessionId
		},
		dataType : 'json',
		success : function(data){
			var redirectUrl = Util.mp.params.redirect_url + '?sessionId=' + Util.mp.params.sessionId;
			if((data.root[0].typeVal == branches.GROUP.val)){
				
				if(Util.mp.params.branchId){
					redirectUrl += '&branchId=' + Util.mp.params.branchId;
					window.location.href = redirectUrl;
					
				}else{
					var branchesTemplet =  '<div class="main-box" data-value={breanchesId} data-type="braches_div_branches">'
								+'<ul class="m-b-list">'
									+'<li style="line-height: 40px;width:98%;">'
										+'<h3><i class="foundicon-home fcolor6"></i>&nbsp;&nbsp;{restaurant}</h3>'	
										+'<div style="margin-top:-3px;color: #666;">地址: {address}</div>'
										+'<div style="margin-top:-3px;color: #666;">电话: {telephone}</div>'  
									+"</li>"
								+"</ul>"	
							+"</div>";
					
					var html = [];
					html.push(branchesTemplet.format({
						breanchesId : data.root[0].id,
						restaurant : data.root[0].name + '<font color="red">(总店)</font>',
						address : data.root[0].address,
						telephone : data.root[0].tele1
					}));
					
					
					for(var i = 0; i < data.root[0].branches.length; i++){
						html.push(branchesTemplet.format({
							breanchesId : data.root[0].branches[i].id,
							restaurant : data.root[0].branches[i].name,
							address : data.root[0].branches[i].address,
							telephone : data.root[0].branches[i].tele1
						}));
					}
					$('#branches_div_branches').html(html.join(''));	
					$('#branchesWin_div_branches').show();
					
					//各个门店的点击事件
					$('#branches_div_branches').find('[data-type="braches_div_branches"]').each(function(index, element){
	//					element.onmousedown = function(){
	//						$(element).css({
	//							'border-color' : '#26A9D0'
	//						});
	//					};
	//					element.onmouseup = function(){						
	//						$(element).css({
	//							'border-color' : 'darkgray'
	//						});
	//					};
						element.onclick = function(){
							$(element).css({
							    'background-color': '#ddd'
							});
							if(Util.mp.params.sessionId){
								window.location.href = redirectUrl + '&branchId=' + $(element).attr('data-value');
							}else{
								Util.jump(Util.mp.params.redirect_url, $(element).attr('data-value'));
							}
						};
					});
				}

				
			}else{
				
				redirectUrl += '&branchId=' + data.root[0].id;
				
				if(Util.mp.params.tableId){
					redirectUrl += '&tableId=' + Util.mp.params.tableId;
				}
				
				window.location.href = redirectUrl;
			}
		}
	});

	
	
	
});
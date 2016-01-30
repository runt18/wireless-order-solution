$(function(){
	$.ajax({
		url : '../../WxOperateRestaurant.do',
		type : 'post',
		data : {
			dataSource : 'detail',
			fid : Util.mp.fid
		},
		dataType : 'json',
		success : function(data){
			if(data.root[0].branches){
				console.log(data.root[0].branches); 
				var branchesTemplet =  '<div class="main-box" data-value={breanchesId} data-type="braches_div_branches">'
							+'<ul class="m-b-list">'
								+'<li style="line-height: 40px;width:80%;">'
									+'<h4><i class="foundicon-home fcolor6"></i>&nbsp;&nbsp;{restaurant}</h4>'	
									+"<p>地址：{address}</p>"	
									+"<p>电话: {telephone}</p>"	  
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
					element.onclick = function(){
//						Util.mp.params.redirecturl
						console.log($(element).attr('data-value'));
						Util.jump(Util.mp.params.redirecturl, $(element).attr('data-value'));
//						Util.jump('book.html', $(element).attr('data-value'));
					}
				});
				
			}else{
				Util.jump(Util.mp.params.redirecturl);
			}
		}
	});

	
	
	
});
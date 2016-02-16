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
			fid : Util.mp.fid
		},
		dataType : 'json',
		success : function(data){
			if(data.root[0].typeVal == branches.GROUP.val){
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
						console.log(Util.mp.params.redirect_url);
						Util.jump(Util.mp.params.redirect_url, $(element).attr('data-value'));
//						Util.jump('book.html', $(element).attr('data-value'));
					}
				});
				
			}else{
				Util.jump(Util.mp.params.redirect_url);
			}
		}
	});

	
	
	
});
var Templet = {
	mainBox : '<div class="box-s-f-b" onclick="linkToDazhong({promotionId})">'
		+ '<div>'
			+ '<img border="0" style="width:100%; height: 100%;" src="{img}"/>'
		+ '</div>'
		+ '<div class="box-horizontal">'
			+ '<div class="div-full" >{name}</div>'
			+ '<div style="width: 50px; text-align: right; color: #0000FF;">¥:{unitPrice}</div>'
		+ '</div>'
		+ '<div>{desc}</div>'
//		+ '<div class="box-horizontal">'
//			+ '<div style="-webkit-box-flex: 1; text-align: center; border-right: 1px solid #ddd; ">分享&nbsp;8888</div>'
//			+ '<div style="-webkit-box-flex: 1; text-align: center; ">赞&nbsp;8888</div>'
//		+ '</div>'
		+ '</div>'
};

function linkToDazhong(id){
	window.location.href = "http://t.dianping.com/deal/" + id;
}

$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	$.ajax({
		url : '../../WXQueryDianping.do',
		dataType : 'json',
		data : {
			dataSource : 'getAllGroupBuying',
			fid :Util.mp.params.r
		},
		success : function(data, status, xhr){
			var html = [];
			for(var i = 0; i < data.deals.length; i++){
				var temp = data.deals[i];
				html.push(Templet.mainBox.format({
					promotionId : temp.deal_id.substring(2),
					img : temp.image_url,
					name : temp.title,
					unitPrice : temp.current_price,
					desc : temp.description 
				}));
			}
			$('#divInsertBefore').before(html.join(''));
		},
		error : function(xhr, errorType, error){
//			alert('error');
		}
	});
});
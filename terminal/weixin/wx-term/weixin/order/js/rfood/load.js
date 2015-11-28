var Templet = {
	mainBox : '<div class="box-s-f-b">'
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
$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	$.ajax({
		url : '../../WxQueryFood.do',
		dataType : 'json',
		data : {
			dataSource : 'isRecommend',
			fid : Util.mp.params.r
		},
		success : function(data, status, xhr){
			var html = [];
			for(var i = 0; i < data.root.length; i++){
				var temp = data.root[i];
				html.push(Templet.mainBox.format({
					img : temp.img.thumbnail,
					name : temp.name,
					unitPrice : temp.unitPrice.toFixed(2),
					desc : temp.desc == null || temp.desc.isEmpty() ? '暂无简介' : temp.desc 
				}));
			}
			$('#divInsertBefore').before(html.join(''));
		},
		error : function(xhr, errorType, error){
			alert('error');
		}
	});
});
function ConsumeDetail(){
	var _self = this;
	var _consumeDetail = new JqmPopup({
		loadUrl : './popup/consumeDetail/consumeDetail.html',
		pageInit : function(self){
			//操作类型筛选
			self.find('input[name=memberConsumeType]').each(function(index, element){
				element.onclick = function(){
					searchMemberDetail(self);
				}
			});
			
			//搜索功能
			self.find('[id=searchMember_button_consumeDetail]').click(function(){
				searchMemberDetail(self);
			});
			
			//enter键
			self.find('[id=memberName_input_consumeDetail]').keyup(function(e){
				//keyCode 13 为enter
				if(e.keyCode == 13){
					searchMemberDetail(self);
				}
			});
			
			//关闭按钮
			self.find('[id=colseDetail_a_consumeDetail]').click(function(){
				_self.close();
			});
			
		}
	});
	this.open = function(afterOpen){
		_consumeDetail.open(function(self){
			//初始搜索
			searchMemberDetail(self);
		});
		
		if(afterOpen && typeof afterOpen == 'function'){
			afterOpen();
		}
	}
	
	this.close = function(afterClose, timeOut){
		_consumeDetail.close();
		if(afterClose && typeof afterClose == 'function'){
			if(timeOut){
				setTimeout(afterOpen, timeOut);
			}else{
				afterClose();
			}
		}
	}
	
	//查询会员消费明细		
	function searchMemberDetail(self){
		//会员消费明细
		var memberConsumeTrTemplet = '<tr>'
				+ '<td>{dataIndex}</td>'
				+ '<td>{orderId}</td>'
				+ '<td>{operateDateFormat}</td>'
				+ '<td>{memberName}</td>'
				+ '<td>{memberType}</td>'
				+ '<td>{otype}</td>'
				+ '<td class="text_right">{money}</td>'
				+ '<td class="text_right">{deltaPoint}</td>'
				+ '<td>{staffName}</td>'
				+ '<td>{comment}</td>'
				+ '</tr>';
		
		Util.LM.show();
		var operateType = null;
		var detailOperates = self.find('input[name=memberConsumeType]'); 
		for (var i = 0; i < detailOperates.length; i++) {
			if($(detailOperates[i]).attr("checked")){
				operateType = $(detailOperates[i]).attr("data-value");
				break;
			}
		}
		
		//member手机号名字等
		var searchMemberName = self.find('[id=memberName_input_consumeDetail]').val();
		
		$.ajax({
			url : '../QueryMemberOperation.do',
			type : 'post',
			dataType : 'json',
			data : {
				isPaging : false,
				dataSource : 'today',
				fuzzy : searchMemberName,
				operateType : operateType		
			},
			success : function(result, status, xhr){
				Util.LM.hide();
				if(result.success){
					var html = [];
					for(var i = 0, index = 1; i < result.root.length; i++){
						html.push(memberConsumeTrTemplet.format({
							dataIndex : index,
							orderId : result.root[i].orderId != 0 ? result.root[i].orderId : '----',
							operateDateFormat : result.root[i].operateDateFormat,
							memberName : result.root[i].member.name,
							memberType : result.root[i].member.memberType.name,
							otype : result.root[i].operateTypeText,
							money : result.root[i].payMoney ? result.root[i].payMoney : (result.root[i].deltaTotalMoney ? result.root[i].deltaTotalMoney : 0),	
							deltaPoint : result.root[i].deltaPoint > 0 ? '+'+result.root[i].deltaPoint : result.root[i].deltaPoint,
							staffName : result.root[i].staffName,
							comment : result.root[i].comment
						}));	
						index ++;
					}	
					
					self.find('[id=memberConsumeDetailBody_tbody_consumeDetail]').html(html.join("")).trigger('create');
				}else{
					Util.msg.tip(result.msg);
				}
			},
			error : function(request, status, err){
				Util.LM.hide();
				Util.msg.tip(request.msg);
			}
		});		
	}
	
}
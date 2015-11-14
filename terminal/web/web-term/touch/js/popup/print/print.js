function PrintBindPopup(){
	
	var _printBindPopup = null;
	_printBindPopup = new JqmPopup({
		loadUrl : './popup/print/print.html',
		pageInit : function(self){
			
			
			//确定按钮
			self.find('[id=confirm_a_print]').click(function(){
				var printers = [];
				self.find('[id=print_div_print] .printClass').each(function(index, element){
					if(element.checked){
						printers.push($(element).attr('print_id'));
					}
				});
				setcookie(document.domain + '_printers', printers.join(','), "/");
				
				_printBindPopup.close();
			});
			
			//取消按钮
			self.find('[id=cancel_a_print]').click(function(){
				_printBindPopup.close();
			});
			
		}
	});
	
	this.open = function(afterOpen){
		_printBindPopup.open(function(self){
			Util.LM.show();
			$.post('../OperatePrinter.do',{
				dataSource : 'getByCond',
				isEnabled : true,		//可用的打印机
				oriented : 2			//面向特定的打印机
			},function(jr){
				Util.LM.hide();
				if(jr.success){
					var prints1 = "";
					var prints2 = "";
					var printCookie = getcookie(document.domain + '_printers').split(',');
					
					for(var i = 0; i < jr.root.length; i++){
						var eachPrint = '<tr>'
										+'<td style="width:350px">'
										+'<label style="height:50px"><input $(checked) type="checkbox" class="printClass" data-theme="e" print_id="' + jr.root[i].printerId + '">'+ jr.root[i].name + '$(alias)' +'</label>'
										 + '</td>'
										 + '</tr>';
						
						for(var j = 0; j < printCookie.length; j++){
							if(parseInt(printCookie[j]) == jr.root[i].printerId){
								eachPrint = eachPrint.replace('$(checked)', 'checked');
							}
						}
						
						if(jr.root[i].alias){
							eachPrint = eachPrint.replace('$(alias)', jr.root[i].alias);
						}else{
							eachPrint = eachPrint.replace('$(alias)', '');
						}
						
						
						if(i % 2 == 0){
							prints1 += eachPrint;
						}else if(i % 2 == 1){
							prints2 += eachPrint;
						}
					}
					
					self.find('[id=printTal1_table_print]').append(prints1);
					self.find('[id=printTal1_table_print]').trigger('create').trigger('refresh');
					
					self.find('[id=printTal2_table_print]').append(prints2);
					self.find('[id=printTal2_table_print]').trigger('create').trigger('refresh');
				}
			}, 'json');
			
			
			
			
		});
	}
	
	
	this.close = function(afterOpen){
		_printBindPopup.close();
	}
	
	
	
}
define(function(require, exports, module){
	
	function DiagPrinterPopup(){

		var _self = this;
		//建立jqmPopup
		var _diagPrinterPopup = new JqmPopup({
			loadUrl : './popup/diagPrinter/diagPrinter.html',
			pageInit : function(self){
				//关闭按钮
				self.find('[id=closePrintConnection_a_checkPrinter]').click(function(){
					_self.close();
				});
				//刷新按钮
				self.find('[id=refresh_a_checkPrinter]').click(function(){
					Util.LM.show();
					loadPrinterMsg(self);
				});
			}
		
		});
		
		this.open = function(afterOpen){
			_diagPrinterPopup.open(function(self){
				Util.LM.show();
				//信息显示&&对应errorType 的提示及对应功能
				loadPrinterMsg(self);
			});
			if(afterOpen && typeof afterOpen == 'funciton'){
				aferOpen();
			}
		}
		
		this.close = function(afterClose, timeOut){
			_diagPrinterPopup.close();
			if(afterClose && typeof afterClose == 'function'){
				if(timeOut && typeof timeOut == 'number'){
					setTimeout(afterClose, timeOut);
				}else{
					afterClose();
				}
			}
		}
		
		//加入显示打印机信息
		function getPrinterMsg(printer, index){
			var details = printer.driver && printer.ping;
			
			var index = index + 1;
			var name = printer.printerName + (printer.printerAlias ? '('+printer.printerAlias+')' : '');
			var driver = printer.driver ? '<font color="green">正常</font>' : '<a href="javascript:void(0);" data-type="driver" style="color:red; font-weight:bold;">失败</a>';
			var port = printer.printerPort ? printer.printerPort : '----';
			var gateway = printer.gateway ? printer.gateway : '----';
			var ping = printer.ping ? '<font color="green">正常</font>' : '<a href="javascript:void(0);" data-type="ping" style="color:red; font-weight:bold;">失败</a>';
			var coverOpen = !details ?  "----" : printer.coverOpen ? '<a href="javascript:void(0);">未关闭</a>' : '<font color="green">正常</font>';
			var paperEnd = !details ?  "----" : printer.paperEnd ? '<a href="javascript:void(0);">已用完</a>' : '<font color="green">正常</font>';
			var cutterError = !details ?  "----" : printer.paperEnd ? '<a href="javascript:void(0);">已损坏</a>' : '<font color="green">正常</font>';
			
			var container = '<tr>' +
							'<td>' + index + '</td>' + 
							'<td>' + name + '</td>' + 
							'<td>' + driver + '</td>' + 
							'<td>' + port + '</td>' + 
							'<td>' + gateway + '</td>' + 
							'<td>' + ping + '</td>' + 
							'<td>' + coverOpen + '</td>' + 
							'<td>' + paperEnd + '</td>' + 
							'<td>' + cutterError + '</td>' + 
							'</tr>';
			
			return container;
		}
		
		//读取打印机的信息
		function loadPrinterMsg(self){
			$.ajax({
				url : '../PrinterDiagnosis.do',
				type : 'post',
				dataType : 'json',
				success : function(response, status, xhr){
					Util.LM.hide();
					if(response.success){
						self.find('[id=printerConnectionCount_span_checkPrinter]').text(response.root[0].printers.length);
						if(response.root[0].connectionAmount == 0){
							self.find('[id=printerServiceState_span_checkPrinter]').html('<a href="javascript:void(0);"><span style="color:red; font-weight:bold;">未打开 (所有打印机不出单, 点击解决)</span></a>');
							self.find('[id=printerServiceState_span_checkPrinter]').attr('value', 1);
						}else if(response.root[0].connectionAmount == 1){
							self.find('[id=printerServiceState_span_checkPrinter]').html('<span style="color:green;">正常</span>');
							self.find('[id=printerServiceState_span_checkPrinter]').attr('value', 2);
						}else{
							self.find('[id=printerServiceState_span_checkPrinter]').html('<a href="javascript:void(0);"><span style="color:red; font-weight:bold;">服务重叠 (所有打印机出重单, 点击解决)</span></a>');
							self.find('[id=printerServiceState_span_checkPrinter]').attr('value', 3);
						}
						self.find('[id=printerServiceState_span_checkPrinter]')[0].onclick = function(){
							alert(0);
							var val = self.find('[id=printerServiceState_span_checkPrinter]').attr('value');
							if(val == 1){
								//未开启error
								showError('./popup/diagPrinter/printerDisOpenError.html', 'close_a_disOpenError');
							}else if(val == 3){
								//重复绑定error
								showError('./popup/diagPrinter/printerBindRepeatError.html', 'close_a_bindReapeatError');
							}
						};
						
						//显示打印机状态
						var tbody = self.find('[id=printerConnectionList_tbody_checkPrinter]');
						tbody.html('');
						for(var i = 0; i < response.root[0].printers.length; i++){
							tbody.append(getPrinterMsg(response.root[0].printers[i], i));
						}
						
						//驱动error
						self.find('[data-type=driver]').each(function(index, element){
							element.onclick = function(){
								showError('./popup/diagPrinter/printerDriverError.html', 'close_a_driverError');
							}
						});
						
						//网络error
						self.find('[data-type=ping]').each(function(index, element){
							element.onclick = function(){
								showError('./popup/diagPrinter/printerNetworkError.html', 'close_a_networkError');
							}
						});
					}else{
						self.find('[id=printerServiceState_span_checkPrinter]').html('<a href="javascript:void(0);" style="color:red; ">未打开 (所有打印机不出单, 点击解决)</a>');
						self.find('[id=printerConnectionCount_span_checkPrinter]').text(0);
						self.find('[id=printerConnectionList_tbody_checkPrinter]').html('');
						self.find('[id=printerServiceState_span_checkPrinter]')[0].onclick = function(){
								//未开启error
								showError('./popup/diagPrinter/printerDisOpenError.html', 'close_a_disOpenError');
							}
					}
				},
				error : function(request, status, err){
					Util.LM.hide();
					Util.msg.tip('诊断出错,请联系客服');
				}
				
			});
		}
		
		//错误解决方法提示框弹出
		function showError(url, closeBtnId){
			var errorPopupDiv = new JqmPopupDiv({
				loadUrl : url,
				pageInit : function(self){
					self.find('[id=' + closeBtnId + ']').click(function(){
						errorPopupDiv.close();
					});;

				}
			});
			errorPopupDiv.open();
		}
	}
	
	
	exports.newInstance = function(){
		return new DiagPrinterPopup();
	}
});
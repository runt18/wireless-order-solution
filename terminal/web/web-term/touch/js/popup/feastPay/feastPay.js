define(function(require, exports, module){
	function FeastPaidPopup(param){
		param = param || {
			confirm : function(deptFeast){}   //确定按钮
		}
		
		//入账的部门
		var _dept = [];
		//选择的部门
		var _selectedDept = [];
		//入账的部门和对应的金额
		var _deptFeast = [];
		
		var _feastPay = null;
		_feastPay = new JqmPopupDiv({
			loadUrl : './popup/feastPay/feastPay.html',
			pageInit : function(self){
				//取消按钮
				self.find('[id="cancel_a_feastPay"]').click(function(){
					_feastPay.close();
				});
				
				//确认按钮
				if(param.confirm && typeof param.confirm == 'function'){
					self.find('[id="confirm_a_feastPay"]').click(function(){
						Util.LM.show();
						$.post('../FeastOrder.do', {deptFeasts : _deptFeast.join('&')}, function(result){
							Util.LM.hide();
							param.confirm(result);
						}, "json");
					});
				}
				
			}
		})
		
		//加载部门
		function initDept(dept){
			//酒席入账部门
			var feastPayDeptTemplet = '<tr data-value={id}>' +
					'<td><a data-role="button" data-theme="e" >{deptName}</a></td>' +
					'<td style="padding-right: 10px;"><input id={inputId} data-value={id} data-type="feastPayInput" class="mixPayInputFont numberInputStyle" onkeypress="intOnly()"></td>'+
					'<td> <a data-role="button" data-value="{deptId}" data-text={deptName} data-icon="delete" data-iconpos="notext" data-theme="b" data-iconshadow="false" data-inline="true" data-type="removeFeast">D</a></td>'+
					'</tr>';
				
			var html = feastPayDeptTemplet.format({
				inputId : 'feastInput' + new Date().getTime(),
				id : dept.id,
				deptName : dept.name,
				deptId : dept.id
			});
			$('#feastPayWinTable_table_feastPay').append(html).trigger('create');
			
			//部门收益移除
			$('#feastPayWinTable_table_feastPay').find('[data-type="removeFeast"]').each(function(index, element){
				element.onclick = function(){
					Util.msg.alert({
						msg : '是否去除 <font color="green">' + $(element).attr('data-text') +'</font> 收益?',
						renderTo : 'tableSelectMgr',
						buttons : 'yesback',
						certainCallback : function(){
							for(var i = 0; i < _selectedDept.length; i++){
								if($(element).attr('data-value') == _selectedDept[i].id){
									_selectedDept.splice(i, 1);
								}
							}
								
							$(element).parent().parent().remove();
							calcFeastPay();
						}
					});
				}
			});
			
			//金额跟随键盘输入变化
			$('#feastPayWinTable_table_feastPay').find('[data-type="feastPayInput"]').each(function(index, element){
				NumKeyBoardAttacher.instance().attach(element, function(){
					calcFeastPay();
				});
				if(dept.id == $(element).attr('data-value')){
					$(element).focus();
				}
				element.oninput = function(){
					calcFeastPay();
				}
			});
		}
		
		//计算总金额
		function calcFeastPay(){
			var totalMoney = 0;
			_deptFeast = [];
			var dept = $('#feastPayWinTable_table_feastPay').find('[data-type="feastPayInput"]');
			for(var i = 0; i < dept.length; i++){
				if($(dept[i]).val()){
					totalMoney += parseFloat($(dept[i]).val());
					_deptFeast.push($(dept[i]).attr('data-value') + "," + $(dept[i]).val());
				}
			}
			
			$('#feastPayTotalPrice_label_feastPay').text(totalMoney);
		}
		
		
		this.open = function(afterOpen){
			_feastPay.open(function(){
				$.post('../OperateDept.do',{dataSource : 'getByCond'}, function(result){
					_dept = result.root;
					
					var html = [];
					
					for(var i = 0; i < _dept.length; i++){
						html.push('<li class="popupButtonList" data-icon="false"><a data-type="feaseDept" data-value={id}>{name}</a></li>'.format({
							id : _dept[i].id,
							name : _dept[i].name
						}));
					}
					$('#departmentsListCmp_ul_feastPay').html(html.join('')).listview('refresh');	
					
					//绑定各部门的click
					$('#departmentsListCmp_ul_feastPay').find('[data-type="feaseDept"]').each(function(index, element){
						element.onclick = function(){
							var deptId = $(element).attr('data-value');
							
							for(var i = 0; i < _selectedDept.length; i++){
								if(_selectedDept[i].id == deptId){
									Util.msg.tip("此部门已添加");
									return;
								}
							}
							
							$('#popupDepartmentsCmp').popup('close');
							
							setTimeout(function(){
								for(var i = 0; i < _dept.length; i++){
									if(_dept[i].id == deptId){
										_selectedDept.push(_dept[i]);
										initDept(_dept[i]);
									}
								}
							}, 100);
						}
					});
					
				});
			});
			if(afterOpen && typeof afterOpen == 'function'){
				afterOpen();
			}
		}
		
		this.close = function(afterClose, timeout){
			//金额跟随键盘输入变化
			$('#feastPayWinTable_table_feastPay').find('[data-type="feastPayInput"]').each(function(index, element){
				NumKeyBoardAttacher.instance().detach(element);
			});
			_feastPay.close();
			if(afterClose && typeof afterClose == 'function'){
				if(timeout){
					setTimeout(afterClose, timeout);
				}else{
					afterClose();
				}
			}
		}
		
	}
	
	exports.newInstance = function(param){
		return new FeastPaidPopup(param);
	};
});

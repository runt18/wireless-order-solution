//餐桌选择包
var	ts = {
	table : {},
	member : {}
};



$(function(){
	
	//消息中心跑马灯
	var runHorseDiv;
	//作为收银端或触摸屏时, 餐台列表的高度
	var	tableListHeight = 86;
	//餐台刷新的定时器Id
	var tableRefreshTimeoutId = null;
	
	//刷新是返回到'#tableSelectMgr'
	$(window).on('unload', function(){
		$.ajax({
			url : '../VerifyLogin.do',
			async : false,
			success : function(data, status, xhr){
				if(data.success){
					location.href = '#tableSelectMgr';
					location.reload(true);
				}
			}
		});
	});
	
	//改变窗口时
	$(window).resize(function(){
		//餐厅选择界面高度
		$('#tableAndRegionsCmp').height(document.body.clientHeight - tableListHeight);	
		//点菜界面高度
		$('#orderFoodCenterCmp').height(document.body.clientHeight - 210);
		document.getElementById('foodsCmp_div_orderFood').style.height = (document.body.clientHeight - 210)+'px';		
		//沽清菜界面高度
		$('#stopSellCmp').height(document.body.clientHeight - 125);	
		document.getElementById('foods4StopSellCmp').style.height = (document.body.clientHeight - 210)+'px';
		document.getElementById('divFoods4StopSellCmp').style.height = (document.body.clientHeight - 210)+'px';
		//已点菜界面高度
		$('#orderFoodListCmp').height(document.body.clientHeight - 125);
		//预订列表
		$('#bookOrderListCmp').height(document.body.clientHeight - 170);	
		//微信预订列表
		$('#wxBookOrderListCmp_div_wxBook').height(document.body.clientHeight - 170);
		//结账界面高度 & 菜品列表高度
		$('#paymentCmp').height(document.body.clientHeight - 86);	
		$('#payment_orderFoodListCmp').height(document.body.clientHeight - 126);	
	});
	
	//退出餐台选择界面
	$('#tableSelectMgr').on('pagehide', function(){
		
		//删除刷新餐台的定时器
		if(tableRefreshTimeoutId){
			clearTimeout(tableRefreshTimeoutId);
		}
		
	});

	//退出餐台选择界面
	$('#tableSelectMgr').on('pagebeforehide', function(){
		//隐藏跑马灯
		if(runHorseDiv){
			runHorseDiv.hide();
		}
	});
	
	//进入餐台选择界面
	$('#tableSelectMgr').on('pagebeforeshow', function(){
		$.ajax({
			url : '../VerifyLogin.do',
			success : function(data, status, xhr){
				if(data.success){
					//定时器，定时刷新餐桌选择页面数据
					(function refreshTable(){
						initTableData();
						tableRefreshTimeoutId = setTimeout(arguments.callee, 15 * 60 * 1000);
					})();
				}
			}
		});
	});
	
	//进入餐台选择界面
	$('#tableSelectMgr').on('pageshow', function(){

		//清除快捷键
		$(document).off('keydown');
		//设置快捷键
		$(document).on('keydown', function(event){
			if(event.which == '107'){//加号
				$('#searchTable_a_tableSelect').click();
		    } 	
		});
		
		//刷新微信预订单
		(function refreshWeixinBook(){
			$.post('../OperateBook.do', {dataSource: 'getByCond', status:1}, function(data){
				if(data.success){
					if(data.root && data.root.length > 0){
						$('#bookAmount_div_tableSelect').html(data.root.length);
						$('#bookAmount_div_tableSelect').show();
					}else{
						$('#bookAmount_div_tableSelect').hide();
					}
				}
			}, 'json');	
		})();
		
		//刷新微信订单
		(function refreshWxBook(){
			
			$.ajax({
				url : '../QueryWxOrder.do',
				type : 'post',
				dataType : 'json',
				data : {
					dataSource : 'getByCond',
					status : '2'
				},
				success : function(data){
					Util.LM.hide();
					if(data.success){
						if(data.root && data.root.length > 0){
							$('#wxbookAmount_div_tableSelect').html(data.root.length);
							$('#wxbookAmount_div_tableSelect').show();
						}else{
							$('#wxbookAmount_div_tableSelect').hide();
						}
					}
				}
			});
			
		})();
		
		//显示跑马灯
		if(runHorseDiv){
			runHorseDiv.show();	
		}
		
	});
	
	//进入餐桌初始化
	$('#tableSelectMgr').on('pageinit', function(){
		
		//建立跑马灯   只有在pos端的情况下才创建
		if(WirelessOrder.systemStatus.isPos()){ 
			runHorseDiv = new CreateRunHorse();
			runHorseDiv.open();
		}
	
		//初始化窗口大小
		$(window).resize();
	
		//餐台分页
		ts.padding = new WirelessOrder.Padding({
			renderTo : $('#divTableShowForSelect'),
			displayTo : $('#divDescForTableSelect-padding-msg'),
			itemLook : function(index, item){
				var aliasOrName;
				if(item.categoryValue == 1){//一般台
					aliasOrName = item.alias;
				}else if(item.categoryValue == 3){//搭台
					var begin = item.name.indexOf("(");
					var end = item.name.indexOf(")");
					aliasOrName = '<font color="green">' + item.name.substring(begin+1, end) +'</font>';
				}else{
					aliasOrName = '<font color="green">'+ item.categoryText +'</font>';
				}
				
				var tempPaid;
				if(item.isTempPaid && item.isTempPaidTimeout){
					tempPaid = '<font color="red">暂结</font>';
				}else if(item.isTempPaid && !item.isTempPaidTimeout){
					tempPaid = '<font>暂结</font>';
				}else{
					tempPaid = '&nbsp;&nbsp;';
				}
				var tableCmpTemplet = '<a data-role="button" data-corners="false" data-inline="true" class="tableCmp" data-index={dataIndex} data-value={id} data-theme={theme}>' +
					'<div style="height: 70px;">{name}<br>{alias}' +
						'<div class="{tempPayStatusClass}">{tempPayStatus}</div>'+
						'<div class="bookTableStatus">{bookTableStatus}</div>'+
					'</div>'+
				'</a>';
				return tableCmpTemplet.format({
					dataIndex : index,
					id : item.id,
					alias : aliasOrName,
					theme : item.statusValue == '1' ? "e" : "c",
					name : item.name,
					tempPayStatus : tempPaid,
					bookTableStatus : item.isBook? '订' : '',
					tempPayStatusClass : navigator.userAgent.indexOf("Firefox") >= 0?'tempPayStatus4Moz':'tempPayStatus'
				});	
			},
			itemClick : function(index, item){
				updateTable({
					id : item.id,
					alias : item.tableAlias
				});
			}
		});
		
		//餐台上一页
		$('#prevTablePage_a_tableSelect').click(function(){
			ts.padding.prev();
		});
		
		//餐台下一页
		$('#nextTablePage_a_tableSelect').click(function(){
			ts.padding.next();
		});
	
		Util.LM.show();		
		
		$.ajax({
			url : '../VerifyLogin.do',
			success : function(data, status, xhr){
				if(data.success){
					
					WirelessOrder.login = new WirelessOrder.Staff(data.root[0]);
					
					$('#loginStaffName_div_tableSelect').html('操作人: <font color="green">'+ WirelessOrder.login.staffName + '</font>');
					
					Util.LM.show();
					
					//加载基础数据
					initTableData();
					initFoodData();
					
				}else{	
					Util.msg.tip('请先登录');
					setTimeout(function(){
						location.href = 'verifyLogin.jsp?status=' + WirelessOrder.systemStatus.val;
					}, 2000);
				}
				Util.LM.hide();	
			},
			error : function(request, status, error){
				Util.LM.hide();	
				Util.msg.tio('操作有误,请刷新页面');
				setTimeout(function(){
					location.href = 'verifyLogin.jsp?status=' +  WirelessOrder.systemStatus.val;
				}, 2000);
			}
		});
		
		
		//获取系统相关属性
		Util.sys.checkSmStat();
		
		//pos端 && 体验端 && touch端
		if(WirelessOrder.systemStatus.isPos()){//pos端
			//日结,交班等
			$('#divPosOperation').show();
			$('#btnOrderAndPay').show();
			//下单并结账
			$('#orderPay_li_orderFood').show();
			//已点菜结账按钮
			$('#payOrder_a_checkOut').show();
			//收银端餐台列表高度
			tableListHeight = 130;	
			//快餐模式的现金找零
			$('#cashReceives_a_payment').hide();
		}else if(WirelessOrder.systemStatus.isTry()){//try端
			//日结,交班等
			$('#divPosOperation').show();
			$('#btnOrderAndPay').show();
			//下单并结账
			$('#orderPay_li_orderFood').hide();
			//已点菜结账按钮
			$('#payOrder_a_checkOut').show();		
			//收银端餐台列表高度
			tableListHeight = 130;	
			//try端不可进入后台
			$('#btnToBasicPage').hide();
			//快餐模式的现金找零
			$('#cashReceives_a_payment').hide();
			
		}else if(WirelessOrder.systemStatus.isTouch()){//touch端
			//日结,交班等
			$('#divPosOperation').hide();
			$('#btnOrderAndPay').hide();
			//下单并结账
			$('#orderPay_li_orderFood').hide();
			//已点菜结账按钮
			$('#payOrder_a_checkOut').hide();
			//快餐模式的结账按钮
			$('#cashReceives_a_payment').hide();
			//收银端餐台列表高度
			tableListHeight = 86;
		}else if(WirelessOrder.systemStatus.isFastFood()){//快餐模式
			//日结,交班等
			$('#divPosOperation').show();
			$('#btnOrderAndPay').show();
			//下单并结账
			$('#orderPay_li_orderFood').show();
			//已点菜结账按钮
			$('#payOrder_a_checkOut').show();
			//快餐模式按钮
			$('#fastFood_li_tableSelect').show();
			//快餐模式的现金找零
			$('#cashReceives_a_payment').show();
			//快餐模式的结账按钮
			$('#fastPay_a_orderFood').show();
			$('#fastPay_a_orderFood').text('快速结账');
			
			//收银端餐台列表高度
			tableListHeight = 130;	
		}
		
		//空闲状态的餐台
		$('#idleTable_li_tableSelect').click(function(){
			$('#labTableStatus .ui-btn-text').text($(this).text());
			$('#labTableStatus').attr('table-status', WirelessOrder.TableList.Status.IDLE.val);
			$('#popupAllStatusCmp').popup('close');
			showTable();
		});
		
		//就餐状态的餐台
		$('#busyTable_li_tableSelect').click(function(){
			$('#labTableStatus .ui-btn-text').text($(this).text());
			$('#labTableStatus').attr('table-status', WirelessOrder.TableList.Status.BUSY.val);
			$('#popupAllStatusCmp').popup('close');
			showTable();
		});
		
		//全部状态的餐台
		$('#allTable_li_tableSelect').click(function(){
			$('#labTableStatus .ui-btn-text').text($(this).text());
			$('#labTableStatus').removeAttr('table-status');
			$('#popupAllStatusCmp').popup('close');
			showTable();
		});
		
		var CommitTypeEnum = {
			Daily : {
				type : 1,
				title : '日结'
			},
			Phrase : {
				type : 0,
				title : '交班'
			},
			Person : {
				type : 2,
				title : '交款'
			}
		};
		
		//日结处理函数
		function dailyHandler(commitType){
			var dailyPopup = null;
			dailyPopup = new JqmPopup({
				loadUrl : './popup/daily/daily.html',
				pageInit : function(self){
					//关闭
			 		self.find('[id=close_a_daily]').click(function(){
			 			dailyPopup.close();
			 		});
			 		//预打
			 		self.find('[id=prePrint_a_daily]').click(function(){
			 			prePrint();
			 		});
			 		//日结
			 		self.find('[id=confirm_a_daily]').click(function(){
			 			submitDailyOperation();
			 		});
				}
			});
			
			//打开日结Popup
			dailyPopup.open(function(){
				showDailyInfo(commitType);
			});
			
			//交班、日结的时间段
			var dutyRange = {
				onDutyFormat : null,
				offDutyFormat : null	
			};
			
			//交款&交班&日结打印
			function dailyOperationDaYin(printType, appendMsg){
				Util.LM.show();
				$.post('../PrintOrder.do',{
					onDuty : dutyRange.onDutyFormat,
					offDuty : dutyRange.offDutyFormat,
					printType : printType,
					orientedPrinter : getcookie(document.domain + '_printers')			//特定打印机打印
				}, function(resultJSON) {
					Util.LM.hide();
					if(resultJSON.success){
						Util.msg.alert({
							msg : resultJSON.msg + (appendMsg ? ('<br/>' + appendMsg) : ''),
							topTip : true
						});			
					}else{
						Util.msg.alert({
							msg : resultJSON.msg,
							renderTo : 'tableSelectMgr'
						});				
					}
			
				});
			}
			
			//预打
			function prePrint(){
				if(commitType == CommitTypeEnum.Person){
					//交款
					dailyOperationDaYin(12);
				}else{
					dailyOperationDaYin(5);
				}
			}
			
			//显示日结信息
			function showDailyInfo(settleType){
				
				var memberTitle = '<tr>' 
								+ '<th class="table_title text_center">会员操作</th>'
								+ '<th class="table_title text_center">现金</th>'
								+ '<th class="table_title text_center">刷卡</th>'
								+ '<th class="table_title text_center">账户实充/扣额</th>'
								+ '</tr>',
								
					memberTrModel = '<tr>'
								+ '<th>会员充值</th>'
								+ '<td class="text_right">{0}</td>'
								+ '<td class="text_right">{1}</td>'
								+ '<td class="text_right">{2}</td>'
								+ '</tr>'
								+ '<tr>'
								+ '<th>会员退款</th>'
								+ '<td class="text_right">{3}</td>'
								+ '<td class="text_right">{4}</td>'
								+ '<td class="text_right">{5}</td>'
								+ '</tr>',
								
					deptTrModel = '<tr>'
								+ '<th class="table_title text_center">部门汇总</th>'
								+ '<th class="table_title text_center">折扣总额</th>'
								+ '<th class="table_title text_center">赠送总额</th>'
								+ '<th class="table_title text_center">应收总额</th>'
								+ '</tr>',
								
					trModel = '<tr>'
								+ '<th>{0}</th>'
								+ '<td class="text_right">{1}</td>'
								+ '<td class="text_right">{2}</td>'
								+ '<td class="text_right">{3}</td>'
								+ '</tr>',
					
					trPayIncomeModel = '<tr>'
								+ '<th>{0}</th>'
								+ '<td class="text_right">{1}</td>'
								+ '<td class="text_right">{2}</td>'
								+ '<td class="text_right">{3}</td>'
								+ '</tr>';
				
				//设置标题
				$('#title4DailyInfoTable').html('<font color="#f7c942">' + commitType.title + '</font> -- ' + commitType.title + '人 : '+ WirelessOrder.login.staffName);
				$('#confirm_a_daily .ui-btn-text').html(commitType.title);
				
				$.post('../QueryDailySettleByNow.do',{queryType : settleType.type}, function(jr){
					if(jr.success){
						var business = jr.other.business;
						var deptStat = business.deptStat;
						dutyRange.onDutyFormat = business.paramsOnDuty;
						dutyRange.offDutyFormat = business.paramsOffDuty;
						
						var trContent = '';
						for(var i = 0; i < deptStat.length; i++){
							var temp = deptStat[i];
							trContent += (trModel.format(
									temp.dept.name, 
									temp.discountPrice.toFixed(2), 
									temp.giftPrice.toFixed(2), 
									temp.income.toFixed(2)
								)
							);
						}
						
						var memberTrDate = memberTrModel.format(business.memberChargeByCash.toFixed(2), 
																business.memberChargeByCard.toFixed(2), 
																business.memberAccountCharge.toFixed(2),
																business.memberRefund.toFixed(2), 
																0.00, 
																business.memberAccountRefund.toFixed(2));
						var table = '<table border="1" class="tb_base">{0}{1}</table><br><table border="1" class="tb_base">{2}{3}</table>'.format(memberTitle, memberTrDate, deptTrModel, trContent);
						
						//是否有预订金额
						if(business.bookIncome > 0){
							table += '<br><table border="1" class="tb_base"><tr><th class="table_title text_center">预订总金额:</th><th class="table_title text_center">'+ business.bookIncome +'</th></tr></table>';
						}
						
						$('#memberIncome_div_daily').html(table);
						
						$('#startDate_td_daily').html(business.paramsOnDuty);
						$('#endDate_td_daily').html(business.paramsOffDuty);
			
						$('#orderAmount_td_daily').html(business.orderAmount);
						
						$('#eraseAmount_td_daily').html(business.eraseAmount);
						$('#eraseIncome_td_daily').html(business.eraseIncome.toFixed(2));
						
						$('#discountAmount_td_daily').html(business.discountAmount);
						$('#discountIncome_td_daily').html(business.discountIncome.toFixed(2));
						
						$('#giftAmount_td_daily').html(business.giftAmount);
						$('#giftIncome_td_daily').html(business.giftIncome.toFixed(2));
						
						$('#couponAmount_td_daily').html(business.couponAmount);
						$('#couponIncome_td_daily').html(business.couponIncome.toFixed(2));
						
						$('#cancelAmount_td_daily').html(business.cancelAmount);
						$('#cancelIncome_td_daily').html(business.cancelIncome.toFixed(2));
						
						$('#repaidAmount_td_daily').html(business.paidAmount);
						$('#repaidIncome_td_daily').html(business.paidIncome.toFixed(2));
						
						$('#serviceAmount_td_daily').html(business.serviceAmount);
						$('#serviceIncome_td_daily').html(business.serviceIncome.toFixed(2));
						
						
						var trPayTypeContent = ['<tr>'
						  + '<th class="table_title text_center">收款方式</th>'
						  + '<th class="table_title text_center">账单数</th>'
						  + '<th class="table_title text_center">应收总额</th>'
						  + '<th class="table_title text_center">实收总额</th>'
						  + '</tr>'];								
						//输出付款方式集合
						var totalCount = 0, totalShouldPay = 0, totalActual = 0;
						for(var i = 0; i < business.paymentIncomes.length; i++){
							var temp = business.paymentIncomes[i];
							totalCount += temp.amount;
							totalShouldPay += temp.total;
							totalActual += temp.actual;
							
							trPayTypeContent.push(trPayIncomeModel.format(
									temp.payType, 
									temp.amount, 
									temp.total.toFixed(2), 
									temp.actual.toFixed(2)
								)
							);
							
						}
						//汇总
						trPayTypeContent.push(trPayIncomeModel.format(
							'总计', 
							totalCount, 
							totalShouldPay.toFixed(2), 
							totalActual.toFixed(2)
						));
						$('#payIncome_table_daily').html(trPayTypeContent.join(""));
					}
				});
			}
			
			//交班, 日结, 交款操作
			function submitDailyOperation(){
				
				if(commitType == CommitTypeEnum.Phrase){
					//交班
					$.post('../DoShift.do', function(resultJSON){
						if (resultJSON.success) {
							dailyPopup.close();
							dutyRange = resultJSON.other.dutyRange;
							dailyOperationDaYin(4, resultJSON.msg);
						} else {
							dailyPopup.close(function(){
								Util.msg.alert({
									msg : resultJSON.msg,
									renderTo : 'tableSelectMgr'
								});
							}, 200);
						}		
					});		
				}else if(commitType == CommitTypeEnum.Daily){
					// 未交班帳單檢查
					$.post('../DailySettleCheck.do', function(resultJSON){
						if (resultJSON.success) {
							//日结
							$.post('../DailySettleExec.do', function(data){
								if (data.success) {
									dailyPopup.close();
									dutyRange = data.other.dutyRange;
									dailyOperationDaYin(6, data.msg);
								} else {
									dailyPopup.close(function(){
										Util.msg.alert({
											msg : data.msg,
											renderTo : 'tableSelectMgr'
										});
									}, 200);
								}		
							});			
						} else {
							dailyPopup.close(function(){
								Util.msg.alert({
									msg : resultJSON.msg,
									renderTo : 'tableSelectMgr',				
									buttons : 'YESBACK',
									certainCallback : function(btn){
										if(btn == 'yes'){
											$.post('../DailySettleExec.do', function(data){
												if (data.success) {
													dailyPopup.close();
													dutyRange = data.other.dutyRange;
													dailyOperationDaYin(6, data.msg);
												} else {
													Util.msg.alert({
														msg : data.msg,
														renderTo : 'tableSelectMgr'
													});
												}		
											});
										}
									}
								});
							}, 200);
		
						}		
					});		
				}else if(commitType == CommitTypeEnum.Person){
					//交款
					$.post('../DoPayment.do', function(resultJSON){
						if (resultJSON.success) {
							dailyPopup.close();
							dutyRange = resultJSON.other.dutyRange;
							dailyOperationDaYin(12, resultJSON.msg);
						} else {
							dailyPopup.close(function(){
								Util.msg.alert({
									msg : resultJSON.msg,
									renderTo : 'tableSelectMgr'
								});
							}, 200);
						}		
					});		
				}
			}
		}
		
		//日结
		$('#dailySettle_a_tableSelect').click(function(){
			dailyHandler(CommitTypeEnum.Daily);
		});
		
		//交班
		$('#phraseSettle_a_tableSelect').click(function(){
			dailyHandler(CommitTypeEnum.Phrase);
		});
		
		//交班
		$('#personSettle_a_tableSelect').click(function(){
			dailyHandler(CommitTypeEnum.Person);
		});
		
		//快速发券
		$('#fastIssue_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use(['readMember', 'issueCoupon'], function(readPopup, issuePopup){
					var fastIssuePopup = null;
					fastIssuePopup = readPopup.newInstance({
						confirm : function(member){
							if(member){
								fastIssuePopup.close(function(){
									var issueCouponPopup = issuePopup.newInstance({
										title : '快速发放优惠券',
										memberName : member.name,
										issueMode : issuePopup.IssueMode.FAST,
										issueTo : member.id
									});
									issueCouponPopup.open();
								}, 200);
							}else{
								Util.msg.tip('请注入会员!');
							}
						}
					});
					fastIssuePopup.open();
					
				});
			}, 100);
		});
		
		//快速用券
		$('#fastUse_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use(['readMember','useCoupon'], function(readPopup, usePopup){
					var fastUsePopup = null;
					fastUsePopup = readPopup.newInstance({
						confirm : function(member){
							if(member){
								fastUsePopup.close(function(){
									var useCouponPopup = null;
									useCouponPopup = usePopup.newInstance({
										title : '快速使用优惠券',
										memberName : member.name,
										issueMode : usePopup.UseMode.FAST,
										useTo  : member.id,
										useCuoponMethod : function(coupons){
											$.post('../OperateCoupon.do', {
													dataSource : 'coupon', 
													coupons : coupons.join(','), 
													useTo : member.id, 
													useMode : usePopup.UseMode.FAST.mode 
												}, function(response, status,xhr){
													if(response.success){
														Util.msg.tip('使用成功!');
														useCouponPopup.close();
													}else{
														Util.msg.tip(response.msg);
													}
											}, 'json');
										}
									});
									useCouponPopup.open();
								}, 200);
							}else{
								Util.msg.tip('请注入会员!');
							}
						}
					});
					
					fastUsePopup.open();
				});
			}, 300);
		});
		
		//添加会员
		$('#addMember_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use('addMember', function(popup){
					popup.newInstance().open();
				});
			}, 200);
		});
		
		//微信会员绑定
		$('#memberWxBind_li_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use(['readMember', 'perfectMemberMsg'], function(readPopup, prefectPopup){
					var memberWxReadPopup = null;
					memberWxReadPopup = readPopup.newInstance({
						confirm : function(member){
							if(member && member.isRaw){
								memberWxReadPopup.close(function(){
									var memberWxBindePopup = null;
									memberWxBindPopup = prefectPopup.newInstance({
										memberName : member.name,
										selectedMember : member.id,
										postBound : function(){
											memberWxBindPopup.close();
										}
									});
									memberWxBindPopup.open();
								}, 200);
							}
						}
						
					});
					memberWxReadPopup.open();
				});
			}, 200);
		});
		
		
		//打印机诊断
		$('#diagPrinter_a_tableSelect').click(function(){
			seajs.use('diagPrinter', function(popup){
				popup.newInstance().open();
			});
		});
		
		
		
		//打印机绑定按钮
		$('#printBind_a_tableSelect').click(function(){
			$('#tableSelectOtherOperateCmp').popup('close');
			setTimeout(function(){
				var printBindPopup = new PrintBindPopup();
				printBindPopup.open();
			}, 300);
			
		});
		
		//消费明细
		$('#consumeDetail_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use('consumeDetail', function(popup){
					popup.newInstance().open();
				});
			},300);
		});
		
		
		//查台按钮
		$('#searchTable_a_tableSelect').click(function(){
			var askTablePopup = null;
			askTablePopup = new AskTablePopup({
				tables : WirelessOrder.tables,
				title : '查台',
				middleText : '点菜(+)',
				middle : function(){
					var prefectMatched = askTablePopup.prefect();
					if(prefectMatched){
						askTablePopup.close(function(){
							updateTable({
								id : prefectMatched.id,
								alias : !prefectMatched.id ? prefectMatched.alias : ''
							});	
						}, 200);
					}else{
						Util.msg.tip('没有此餐台,请重新输入');
					}
					
				},
				left : function(){
					//结账
					var perfectMatched = askTablePopup.prefect();
					
					if(perfectMatched){
						if(perfectMatched.statusValue == WirelessOrder.TableList.Status.BUSY.val){
							askTablePopup.close(function(){
								$('#tableSelect_div_askTable').off('keydown');
								updateTable({
									toPay : true,
									id : perfectMatched.id,
									alias : !perfectMatched.id ? perfectMatched.alias : ''
								});
							}, 200);
						}else{
							Util.msg.tip('餐台是空闲状态，不能结账');
						}
					}else{
						Util.msg.tip('没有此餐台,请重新输入');
					}
				},
				tableSelect : function(selectedTable){
					askTablePopup.close(function(){
						updateTable({
							id : selectedTable.id,
							alias : selectedTable.alias
						});	
					}, 200);
				}
			});
			
			askTablePopup.open(function(self){
				
				$('#tableSelect_div_askTable').on('keydown', function(event){
					if(event.keyCode == '107'){
						//快捷键'+'
						$('#middle_a_askTable').click();
					}else if(event.keyCode == '13'){
						//快捷键'Enter'
						$('#left_a_askTable').click();
					}
					//取消事件的冒泡行为
					event.stopPropagation();
				});
			});
		});
	
		//快餐模式按钮
		$('#fastFood_li_tableSelect').click(function(){
			of.entry({orderFoodOperateType : 'fast'});
		});
		
		//会员查询
		$('#searchMember_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use('readMember', function(popup){
					var readPopup = null;
					readPopup = popup.newInstance({
						confirm : function(){
							readPopup.close();
						}
					});
					readPopup.open();
				});
			}, 300);
		});
		
		//会员充值
		$('#memberRecharge_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use('recharge', function(popup){
					popup.newInstance().open();
				});
			},300);
		});
		
		//补发实体卡
		$('#patchCard_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use('patchCard', function(popup){
					popup.newInstance().open();
				});
			},300);
		});

		//补发电子卡
		$('#patchWxCard_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				seajs.use('patchWxCard', function(popup){
					popup.newInstance().open();
				});
			},300);
		});
		
		
		//转台
		$('#tranTable_a_tableSelect').click(function(){
			var askTablePopup = new AskTablePopup({
				title : '转台',
				middle : function(){
					var sourceTable = null;
					var sourceAlias = $('#left_input_askTable').val();
					var destAliasd = $('#tranNum_input_ask').val();
					
					sourceTable = WirelessOrder.tables.getByAlias(sourceAlias);
					
					var destTable = WirelessOrder.tables.getByAlias(destAliasd);
					
					if(!sourceTable || !destTable){
						Util.msg.tip('查找餐台出错,请检查台号是否正确');
						return;
					}
					
					Util.LM.show();
					
					$.post('../OperateTable.do', {
						dataSource : 'transTable',
						oldTableId : sourceTable.id,
						newTableId : destTable.id
					}, function(data){
						Util.LM.hide();
						if(data.success){
							askTablePopup.close();
							initTableData();
							Util.msg.tip(data.msg);
							ts.loadData();
						}else{
							Util.msg.tip(data.msg);
						}
					}).error(function(){
						Util.LM.hide();
						Util.msg.tip('操作失败,请刷新页面重试');
					});
				}
			});
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#tranNum_td_ask').show();
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
		});
		
		//拆台
		$('#apartTable_a_tableSelect').click(function(){
			var _selectedTable = null;
			var askTablePopup = new AskTablePopup({
				tables : WirelessOrder.tables,
				title : '拆台',
				middle : function(){
					Util.msg.tip('请选中一张餐桌或者编号');
				},
				tableSelect : function(selectedTable){
					$('#matchedTables_div_askTable').hide();
					$('#suffix_div_ask').show();
					_selectedTable = selectedTable;
				},
				suffixSelect : function(suffixValue){
					Util.LM.show();
					var suffix = suffixValue;
					$.post('../OperateTable.do', {
						dataSource : 'apartTable',
						tableID : _selectedTable.id,
						suffix : suffix,
						comment : $("#apartComment_input_ask").val()
					}, function(result){
						Util.LM.hide();
						if(result.success){
							askTablePopup.close(function(){
								uo.entry({
									table : result.root[0]
								});
							}, 200);
						}else{
							Util.msg.tip(result.msg);
						}
					}).error(function(){
						Util.LM.hide();
						Util.msg.tip('操作失败, 请刷新页面重试');		
					});		
				}
				
			});
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#apartComment_tr_ask').show();
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
		});
					
		//当日账单
		$('#todayBill_a_tableSelect').click(function(){
			//新页面打开账单管理
			window.open("../pages/FrontBusiness_Module/Bills.html");      
		});
		
		//注销
		$('#logout_a_tableSelect').click(function(){
			Util.LM.show();
			$.ajax({
				url : '../LoginOut.do',
				success : function(data, status, xhr){
					location.href = 'verifyLogin.jsp?status=' + WirelessOrder.systemStatus.val;
					//location.reload();
				}
			});	
			if(runHorseDiv){
				runHorseDiv.close();
			}
		});
		
		
		//微信预定
		$('#WxOrder_a_tableSelect').click(function(){
			wxOrder.entry();
		});
		
		//预订按钮
		$('#book_a_tableSelect').click(function(){
			books.entry();
		});
		
		//多台开席
		$('#multiOpen_li_tableSelect').click(function(){
			$('#tableSelectOtherOperateCmp').popup('close');
			setTimeout(function(){
				var handlerTable = new CreateHandlerTable({
					type : 'multiOpenTable',
					title : '多台开席选台',
					right : function(selectedTable){
						if(selectedTable.length == 0){
							Util.msg.tip("请选择餐台");
							return;
						}

						handlerTable.close(function(){
							//进入点菜界面
							of.entry({
								table : selectedTable[0],
								comment : '',
								orderFoodOperateType : 'multiOpenTable',
								commit : function(selectedFoods){
									if(selectedFoods.length == 0){
										Util.msg.tip("请选择菜品");		
										return ;
									}
									
									var orderFoods = [];
									for (var i = 0; i < selectedTable.length; i++) {
										var orderDataModel = {};
										orderDataModel.tableID = selectedTable[i].id;
										orderDataModel.orderFoods = selectedFoods.slice(0);
										orderDataModel.categoryValue =  selectedTable[i].categoryValue;	
										orderFoods.push(JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)));
									}
									
									Util.LM.show();
									$.post('../OperateOrderFood.do', {
										dataSource : 'multiOpenTable',
										multiTableOrderFoods : orderFoods.join("<li>")
									}, function(data){
										Util.LM.hide();
										if(data.success){
											Util.msg.tip(data.msg);
											//清空已选餐台
											ts.loadData();
										}else{
											Util.msg.tip(data.msg);
										}
									}, 'json');
								}
							});	
						}, 300);
					}
				});
				handlerTable.open();
			}, 200);
		});
		
		//多台并台
		$('#MultiPayTable_li_tableSelect').click(function(){
			$('#tableSelectOtherOperateCmp').popup('close');
			setTimeout(function(){
				var handlerTable = new CreateHandlerTable({
					title : '拼台(菜品将合并到第一张餐台)',
					type : 'spellingTable',
					right : function(selectedTable){
						if(selectedTable.length == 0){
							Util.msg.tip("请选择餐台");
							return;
						}
						var multiTables = [];
						for (var i = 0; i < selectedTable.length; i++) {
							multiTables.push(selectedTable[i].id);
						}

						$.post('../OperateTable.do', {
							dataSource : 'mergeTable',
							tables : multiTables.join(",")
						}, function(rt){
							if(rt.success){
								Util.msg.tip("并台成功, 已合并到 "+ selectedTable[0].name);
								//关闭选台
								handlerTable.close();
								//进入已点菜界面
								uo.entry({
									table : selectedTable[0]
								});
							}else{
								Util.msg.tip('tableSelectMgr');
							}
						}, 'json');
					}
				});
				handlerTable.open();
			}, 300);
		});
		
		//酒席入账
		$('#feastPay_li_tableSelect').click(function(){
			$('#tableSelectOtherOperateCmp').popup('close');
			setTimeout(function(){
				var feastPay = new FeastPaidPopup({
					confirm : function(result){
						if(result.success){
							feastPay.close();
							//先跳转到结账界面再操作
							updateTable({
								toPay : true,
								id : result.other.tableId
							});	
							//刷新餐台数据
							initTableData();
						}else{
							Util.msg.tip(result.msg);
						}
					}
				});
				feastPay.open();
			}, 300);
		});
		
	});

	//更新菜品列表
	function initFoodData(){

		//加载口味列表
		$.ajax({
			url : '../OperateTaste.do',
			type : 'post',
			data : {
				dataSource : 'getByCond'
			},
			dataType : 'json',
			success : function(data){
				if(data.success){
					Wireless.Tastes = new WirelessOrder.TasteList(data.root);
				}else{
					Util.msg.tip(data.msg);
				}
			}
		});
		
		//加载菜品列表
		$.ajax({
			url : '../QueryMenu.do',
			type : 'post',
			async: false,
			data : {
				dataSource : 'foodList'
			},
			success : function(data, status, xhr){
				
				var foods = [];
				var depts = [];
				var kitchens = [];
				
				var deptNodes = data.root;
				for (var i = 0; i < deptNodes.length; i++) {
					depts.push(deptNodes[i].deptNodeKey);
					for (var j = 0; j < deptNodes[i].deptNodeValue.length; j++) {
						var kitNode = deptNodes[i].deptNodeValue[j];
						kitNode.kitchenNodeKey.foods = kitNode.kitchenNodeValue.foodList;
						
						foods = foods.concat(kitNode.kitchenNodeValue.foodList);
						
						kitchens.push(kitNode.kitchenNodeKey);
					}
				}
				
				WirelessOrder.foods = new WirelessOrder.FoodList(foods);
				WirelessOrder.kitchens = kitchens;
				WirelessOrder.depts = depts;
				
				//加载临时厨房
				$.post('../QueryMenu.do', {dataSource:'isAllowTempKitchen'}, function(data){
					of.tempKitchens = data.root;
				});
				
			},
			error : function(request, status, err){
				Util.msg.alert({
					msg : request.msg,
					renderTo : 'tableSelectMgr'
				});
			}
		}); 
		
	}
	
	
	/**
	 * 根据alias或id返回table的最新状态
	 * toPay : 是否去结账界面
	 */
	function updateTable(c){
		var table = null;
		$.ajax({
			url : '../QueryTable.do',
			type : 'post',
			data : {
				tableID : !c.alias || c.alias == 0 ? c.id : '', 
				alias : c.alias
			},
	//		async : false,
			success : function(data, status, xhr){
				if(data.success && data.root.length > 0){
					table = data.root[0];
					c.table = table;
					if(c.toPay){
						//关闭选台
						pm.entry(c);
					}else{
						handleTableForTS(c);
					}
				}else{
					Util.msg.alert({
						msg : '没有此餐台, 请重新输入',
						topTip : true
					});				
				}
	
			},
			error : function(request, status, err){
				Util.msg.alert({
					title : '温馨提示',
					msg : err, 
					renderTo : 'orderFoodListMgr',
					time : 2
				});
			}
		});
	}

	/**
	 * 当选中餐桌时，依据餐桌状态处理餐桌
	 */
	function handleTableForTS(c){
		var table = c.table;
		if(table != null){
			//判断是否为已点菜餐桌
			if(table.statusValue == WirelessOrder.TableList.Status.BUSY.val){	
				//判断餐桌是否已经改变状态
				if(c.event && $(c.event).attr('data-theme') != 'e'){
					initTableData();
				}
				//去已点菜界面
				uo.entry({
					table : table
				});
	
			}else{
				ts.table = table;
				//判断餐桌是否已经改变状态
				if(c.event && $(c.event).attr('data-theme') == 'e'){ 
					initTableData();
				}
				
				if(ts.table.isBook){
					var customerPopup = null;
					customerPopup = new NumKeyBoardPopup({
						header : '请输入人数--' + c.table.name,
						leftText : '开台',
						rightText : '查看预订',
						left : function(){
								var customNum = $('#input_input_numKbPopup').val();
								
								if(isNaN(customNum)){
									Util.msg.alert({
										msg : '请填写正确的人数',
										topTip : true
									});			
									$('#input_input_numKbPopup').focus();
									return;
								}else if(customNum <= 0){
									Util.msg.alert({
										msg : '就餐人数不能少于0',
										topTip : true
									});
									$('#input_input_numKbPopup').focus();
									return;
								}
								
								Util.LM.show();
								
								var orderDataModel = {};
								orderDataModel.tableID = ts.table.id;
								orderDataModel.customNum = customNum;
								orderDataModel.orderFoods = [];
								orderDataModel.categoryValue =  ts.table.categoryValue;
								orderDataModel.comment = $('#inputTableOpenCommon').val();
								
								$.post('../InsertOrder.do', {
									commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
									type : 1,
									notPrint : false
								}, function(result){
									Util.LM.hide();
									if (result.success) {
										initTableData();
										Util.msg.alert({
											msg : '开台成功!',
											topTip : true
										});
									}else{
										Util.msg.alert({
											msg : result.msg,
											topTip : true
										});			
									}
								});
								customerPopup.close();	
						},
						right : function(){
							customerPopup.close(function(){
								books.entry();
							}, 200);
							
						},
						middle : function(){
								var customNum = $('#input_input_numKbPopup').val();
								var comment = $('#inputTableOpenCommon').val();
								if(isNaN(customNum)){
									Util.msg.alert({
										msg : '请填写正确的人数',
										topTip : true
									});			
									$('#input_input_numKbPopup').focus();
									return;
								}else if(customNum <= 0){
									Util.msg.alert({
										msg : '就餐人数不能少于0',
										topTip : true
									});
									$('#input_input_numKbPopup').focus();
									return;
								}
								
								ts.renderToCreateOrder(ts.table.alias, customNum, comment);
								customerPopup.close();
							},
						hasComment : true
					});
					
					customerPopup.open(function(self){
						self.find('[id=input_input_numKbPopup]').val(1);
						self.find('[id=left_a_numKbPopup]').css({
							'width' : '32%',
							'float' : 'left'
						});
						self.find('[id=middle_a_numKbPopup]').css({
							'width' : '32%',
							'float' : 'left'
						});
						self.find('[id=right_a_numKbPopup]').css({
							'width' : '34%',
							'height' : '10%',
							'float' : 'left'
						});
						
						setTimeout(function(){
							self.find('[id=input_input_numKbPopup]').select();
						}, 200);
						
					});
				}else{
					var customerPopup = null;
					customerPopup = new NumKeyBoardPopup({
						header : '请输入人数--' + c.table.name,
						leftText : '开台',
						left : function(){
							var customNum = $('#input_input_numKbPopup').val();
							
							if(isNaN(customNum)){
								Util.msg.alert({
									msg : '请填写正确的人数',
									topTip : true
								});			
								$('#input_input_numKbPopup').focus();
								return;
							}else if(customNum <= 0){
								Util.msg.alert({
									msg : '就餐人数不能少于0',
									topTip : true
								});
								$('#input_input_numKbPopup').focus();
								return;
							}
							
							Util.LM.show();
							
							var orderDataModel = {};
							orderDataModel.tableID = ts.table.id;
							orderDataModel.customNum = customNum;
							orderDataModel.orderFoods = [];
							orderDataModel.categoryValue =  ts.table.categoryValue;
							orderDataModel.comment = $('#inputTableOpenCommon').val();
							
							$.post('../InsertOrder.do', {
								commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
								type : 1,
								notPrint : false
							}, function(result){
								Util.LM.hide();
								if (result.success) {
									initTableData();
									Util.msg.alert({
										msg : '开台成功!',
										topTip : true
									});
								}else{
									Util.msg.alert({
										msg : result.msg,
										topTip : true
									});			
								}
							});
							customerPopup.close();	
						},
						right : function(){
							customerPopup.close();
						},
						middle : function(){
								var customNum = $('#input_input_numKbPopup').val();
								var comment = $('#inputTableOpenCommon').val();
								if(isNaN(customNum)){
									Util.msg.alert({
										msg : '请填写正确的人数',
										topTip : true
									});			
									$('#input_input_numKbPopup').focus();
									return;
								}else if(customNum <= 0){
									Util.msg.alert({
										msg : '就餐人数不能少于0',
										topTip : true
									});
									$('#input_input_numKbPopup').focus();
									return;
								}
								
								ts.renderToCreateOrder(ts.table.alias, customNum, comment);
								customerPopup.close();
							},
						hasComment : true
					});
					
					customerPopup.open(function(self){
						self.find('[id=input_input_numKbPopup]').val(1);
						self.find('[id=left_a_numKbPopup]').css({
							'width' : '32%',
							'float' : 'left'
						});
						self.find('[id=middle_a_numKbPopup]').css({
							'width' : '32%',
							'float' : 'left'
						});
						self.find('[id=right_a_numKbPopup]').css({
							'width' : '34%',
							'height' : '10%',
							'float' : 'left'
						});
						
						setTimeout(function(){
							self.find('[id=input_input_numKbPopup]').select();
						}, 200);
						
					});
				}
			}
		}
	}

	/**
	 * 初始化餐桌信息，保存到tables数组中
	 * freeTables存放空闲餐桌，busyTables存放就餐餐桌
	 */
	function initTableData(){
		
		//显示区域
		function showRegion(){
			//添加区域信息
			var html = [];
			html.push('<a data-role="button" data-inline="true" data-type="region" class="regionBtn">全部区域</a>');
			WirelessOrder.regions.forEach(function(e){
				html.push('<a data-role="button" data-inline="true" data-type="region" class="regionBtn" region-id="' + e.id + '">'+ e.name +'</a>');
			});
			
			$('#divSelectRegionForTS').html(html.join("")).trigger('create').trigger('refresh').find('.regionBtn').each(function(index, element){
				element.onclick = function(){
					//恢复所有区域按钮为未选中状态
					$('#divSelectRegionForTS .regionBtn').attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c').removeAttr('region-selected');
					//设置点击的区域按钮是选中状态
					$(element).attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e').attr('region-selected', true);
					//显示所选区域的餐台
					showTable();
				};
			});
		}
	
		Util.LM.show();
		//加载区域
		$.post('../OperateRegion.do', {dataSource : 'getByCond'}, function(response, status, xhr){
			if(status == 'success'){
				if(response.success){
					WirelessOrder.regions = response.root;
					//显示区域
					showRegion();
				}else{
					Util.msg.tip(result.msg);
				}
			}
		}, 'json');
		
		
		// 加载餐台数据
		$.post('../QueryTable.do', null, function(data, status, xhr){
			if(status == 'success'){
				if(data.success){
					
					WirelessOrder.tables = new WirelessOrder.TableList(data.root);
					
					//设置各状态数量
					$('#idleTableAmount_label_tableSelect').text(WirelessOrder.tables.getIdleAmount());
					$('#busyTableAmount_label_tableSelect').text(WirelessOrder.tables.getBusyAmount());
					$('#idleTableAmount_font_tableSelect').text(WirelessOrder.tables.getIdleAmount());
					$('#busyTableAmount_font_tableSelect').text(WirelessOrder.tables.getBusyAmount());
					$('#tmpPaidTableAmount_font_tableSelect').text(WirelessOrder.tables.getTmpPaidAmount());
					
					showTable();
					
				}else{
					Util.msg.alert({
						title : data.title,
						msg : data.msg,
						renderTo : 'tableSelectMgr',
						time : 2
					});
				}
			}
			Util.LM.hide();
		});	
		
	}



	/**
	 * 显示餐桌
	 * @param {object} temp 需要显示的餐桌数组
	 */
	function showTable(){
		
		var tableStatus = null;
		//取得当前的餐台状态条件
		var tableStatusVal = $('#labTableStatus').attr('table-status');
		if(tableStatusVal){
			if(tableStatusVal == WirelessOrder.TableList.Status.IDLE.val){
				tableStatus = WirelessOrder.TableList.Status.IDLE;
			}else if(tableStatusVal == WirelessOrder.TableList.Status.BUSY.val){
				tableStatus = WirelessOrder.TableList.Status.BUSY;
			}
		}
		
		//取得当前的选中区域
		var regionId = null;
		$('#divSelectRegionForTS .regionBtn').each(function(index, element){
			if($(element).attr('region-selected')){
				regionId = $(element).attr('region-id');
			}
		});
	
		var result = WirelessOrder.tables;
		if(tableStatus){
			result = result.getByStatus(tableStatus);
		}
		if(regionId){
			result = result.getByRegion(regionId);
		}
		
		if(result.length != 0){
			ts.padding.data(result);
		}else{
			$("#divTableShowForSelect").html("");
		}	
	}
});	

/** 
 * 通过其他界面返回餐台选择
 */
ts.loadData = function(){
	location.href = '#tableSelectMgr';
};


window.onload = function(){
	//沽清搜索
	$('#searchSelloutFoodInput').focus(function(){
		focusInput = this.id;
		if(this.id == 'searchSelloutFoodInput'){
			ss.s.fireEvent();
		}		
	});
	
	//渲染会员读取窗口
	$('#lookupOrderDetail').trigger('create').trigger('refresh');
    
	//积分消费读卡
    $('#txtMember4PointConsume').on('keypress',function(event){
        if(event.keyCode == "13")    
        {
        	ts.member.readMemberByCondtion4PointConsume();
        }
    });	    
    
    //会员消费详情
    $('#consumeDetail_memberName').on('keypress',function(event){
        if(event.keyCode == "13")    
        {
        	ts.member.searchMemberDetail();
        }
    });
    
};

/**
 * 去沽清界面
 */
ts.stopSellMgr = function(){
	location.href = '#stopSellMgr';
	ss.entry();
};

//进入点菜界面
ts.renderToCreateOrder = function(tableNo, customNum, comment){
	if(tableNo > 0){
		
		setTimeout(function(){
			var tableToAlias = WirelessOrder.tables.getByAlias(tableNo);
			//同时操作餐台时,选中状态没变化的餐桌处理
			//直接写台豪点菜时判断是否已点菜, 是则先给co.order.orderFoods赋值
			if(tableToAlias.statusValue == 1){
				of.entry({
					table : tableToAlias,
					orderFoodOperateType : 'normal'
				});
			}else{
				of.entry({
					table : tableToAlias,
					customNum : customNum,
					comment : comment,
					orderFoodOperateType : 'normal'
				});
			}				
		}, 250);
		
	
	}else{
		Util.msg.alert({
			msg : '没有该餐桌，请重新输入一个桌号.', 
			topTip : true
		});
	}
};


/**
 * 打开 & 关闭会员积分消费
 */
ts.member.openMemberPointConsumeWin = function(){
	$('#frontPageMemberOperation').popup('close');
	
	setTimeout(function(){
		$('#memberPointConsume').show();
		$('#shadowForPopup').show();
		
		$('#txtMember4PointConsume').focus();		
	}, 250);
};

ts.member.closeMemberPointConsumeWin = function(){
	
	$('#memberPointConsume').hide();
	$('#shadowForPopup').hide();
	
	ts.member.loadMemberInfo4PointConsume();
	
	$('#txtMember4PointConsume').val('');
};


/**
 * 积分消费读取会员
 */
ts.member.readMemberByCondtion4PointConsume = function(stype){
	var memberInfo = $('#txtMember4PointConsume');
	
	if(!memberInfo.val()){
		Util.msg.alert({msg:'请填写会员相关信息', topTip:true});
		memberInfo.focus();
		return;
	}
	
	if(stype){
		$('#pointConsume_searchMemberType').popup('close');
	}else{
		stype = '';
	}
	Util.LM.show();
	$.ajax({
		url : "../QueryMember.do",
		type : 'post',
		data : {
			dataSource:'normal',
			sType: stype,
			forDetail : true,
			memberCardOrMobileOrName:memberInfo.val()
		},
//		async : false,
		dataType : 'json',
		success : function(jr, status, xhr){
			Util.LM.hide();
			if(jr.success){
				if(jr.root.length == 1){
					Util.msg.alert({msg:'会员信息读取成功.', topTip:true});
					ts.member.pointConsumeMember = jr.root[0];
					ts.member.loadMemberInfo4PointConsume(jr.root[0]);
				}else if(jr.root.length > 1){
					$('#pointConsume_searchMemberType').popup('open');
					$('#pointConsume_searchMemberType').css({top:$('#btnReadMember4PointConsume').position().top - 270, left:$('#btnReadMember4PointConsume').position().left-300});
				}else{
					Util.msg.alert({msg:'该会员信息不存在, 请重新输入条件后重试.', renderTo : 'tableSelectMgr', fn : function(){
						memberInfo.focus();
					}});
				}
			}else{
				Util.msg.alert({
					msg : jr.msg,
					renderTo : 'tableSelectMgr'
				});
			}
		},
		error : function(request, status, err){
		}
	}); 		
};


/**
 * 积分消费加载会员信息
 */
ts.member.loadMemberInfo4PointConsume = function(member){
	member = member == null || typeof member == 'undefined' ? {} : member;
	var memberType = member.memberType ? member.memberType : {};
	
	$('#numConsumePointForConsumePoint').val('');
	
	$('#numMemberPointForConsumePoint').text(member.point?member.point:'----');
	$('#numMemberNameForConsumePoint').text(member.name?member.name:'----');
	$('#numMemberTypeForConsumePoint').text(memberType.name?memberType.name:'----');
	
	if(!jQuery.isEmptyObject(member)){
		$('#numConsumePointForConsumePoint').focus();		
	}
};

/**
 * 积分消费操作
 */
ts.member.memberPointConsumeAction = function(){
	if(!ts.member.pointConsumeMember){
		Util.msg.alert({msg : '请先输入手机号码或会员卡号读取会员信息.', topTip:true});
		return;
	}
	
	var point = $('#numConsumePointForConsumePoint');
	if(!point.val()){
		Util.msg.alert({msg : '请输入要消费的积分.', topTip:true});
		point.focus();
		return;
	}else if(point.val() > ts.member.pointConsumeMember.point){
		Util.msg.alert({msg:'请输入小于当前积分的消费积分的数值.', renderTo:'tableSelectMgr', fn:function(){
			point.focus();
		}});
		return;
	}
	
	$.post('../OperateMember.do', {
		dataSource : 'consumePoint',
		memberId : ts.member.pointConsumeMember.id,
		point : point.val()		
	}, function(jr){
		if(jr.success){
			Util.msg.alert({
				title : '消费成功',
				msg : '<font size=4>原有积分: ' + ts.member.pointConsumeMember['point'] + '</font>'
					+'<br><font size=4 color="red">消费积分: ' + point.val()	 + '</font>'
					+'<br><font size=4 color="green">当前积分: ' + (ts.member.pointConsumeMember['point'] - point.val()) + '</font>',
				renderTo : 'tableSelectMgr'
			});
			
/*			Util.msg.alert({
				msg : '会员积分消费成功',
				topTip : true
			});*/
			
			ts.member.closeMemberPointConsumeWin();
		}else{
			Util.msg.alert({
				msg : jr.msg,
				renderTo : 'tableSelectMgr'
			});
		}		
	});
};

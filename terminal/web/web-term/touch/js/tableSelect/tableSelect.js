var Request = new Util_urlParaQuery();
//1:pos端	2:touch		3:试用 
var systemStatus = Request["status"]?parseInt(Request["status"]):2;


	
//餐桌选择包
var	ts = {
		table : {},
		tf : {},
		member : {},
		commitTableOrTran : 'table',
		bookChoosedTable : [],
		multiOpenTableChoosedTable : [],
		multiPayTableChoosedTable : []
	},
	/**
	 * 元素模板
	 */
	//餐台
	tableCmpTemplet = '<a onclick="{click}" data-role="button" data-corners="false" data-inline="true" class="tableCmp" data-index={dataIndex} data-value={id} data-theme={theme}>' +
//	'<div>{name}<br>{alias}</div></a>';
		'<div style="height: 70px;">{name}<br>{alias}' +
			'<div class="{tempPayStatusClass}">{tempPayStatus}</div>'+
			'<div class="bookTableStatus">{bookTableStatus}</div>'+
		'</div>'+
	'</a>',
	
	//会员消费明细
	memberConsumeTrTemplet = '<tr>'
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
			+ '</tr>',
	//酒席入账部门
	feastPayDeptTemplet = '<tr id={tr4Feast}>' +
			'<td><a data-role="button" data-theme="e" >{deptName}</a></td>' +
			'<td style="padding-right: 10px;"><input id={numberfieldId} class="mixPayInputFont numberInputStyle" onkeypress="intOnly()"></td>'+
			'<td> <a data-role="button" data-for="{deptId}" data-text={deptName} data-icon="delete" data-iconpos="notext" data-theme="b" data-iconshadow="false" data-inline="true" onclick="ts.feastPayRemoveAction({event:this})">D</a></td>'+
		'</tr>',
	//查看预订菜品	
	bookOrderFoodListCmpTemplet = '<tr class="{isComboFoodTd}">'
			+ '<td>{dataIndex}</td>'
			+ '<td ><div class={foodNameStyle}>{name}</div></td>'
			+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
			+ '<td><div style="height: 25px;overflow: hidden;">{tastePref}</div></td>'
			+ '<td>{unitPrice}</td>'
			+ '</tr>';		



$(function(){
	
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
		document.getElementById('foodsCmp').style.height = (document.body.clientHeight - 210)+'px';		
		//沽清菜界面高度
		$('#stopSellCmp').height(document.body.clientHeight - 125);	
		document.getElementById('foods4StopSellCmp').style.height = (document.body.clientHeight - 210)+'px';
		document.getElementById('divFoods4StopSellCmp').style.height = (document.body.clientHeight - 210)+'px';
		//已点菜界面高度
		$('#orderFoodListCmp').height(document.body.clientHeight - 125);
		//预订列表
		$('#bookOrderListCmp').height(document.body.clientHeight - 170);	
		//结账界面高度 & 菜品列表高度
		$('#paymentCmp').height(document.body.clientHeight - 86);	
		$('#payment_orderFoodListCmp').height(document.body.clientHeight - 126);	
	});
	
	//退出餐台选择界面
	$('#tableSelectMgr').on('pagehide', function(){
		//删除刷新餐台的定时器
		if(tableRefreshTimeoutId){
			clearTimeout(tableRefreshTimeoutId);
			console.log('clear initTableData');
		}
	});
	
	//进入餐台选择界面
	$('#tableSelectMgr').on('pageshow', function(){
		//清除快捷键
		$(document).off('keydown');
		//设置快捷键
		$(document).on('keydown', function(event){
			if(event.which == "107"){//加号
				$('#searchTable_a_tableSelect').click();
		    } 	
		});
		
		$.ajax({
			url : '../VerifyLogin.do',
			success : function(data, status, xhr){
				if(data.success){
					//定时器，定时刷新餐桌选择页面数据
					(function refreshTable(){
						initTableData();
						console.log('initTableData');
						tableRefreshTimeoutId = setTimeout(arguments.callee, 15 * 60 * 1000);
					})();
				}
			}
		});

	});
	
	//进入餐桌初始化
	$('#tableSelectMgr').on('pageinit', function(){
		
		//初始化窗口大小
		$(window).resize();
	
		/**
		 * 餐桌分页包
		 */
		ts.tp = new Util.to.padding({
			renderTo : 'divTableShowForSelect',
			displayId : 'divDescForTableSelect-padding-msg',
			templet : function(c){
				var aliasOrName;
				if(c.data.categoryValue == 1){//一般台
					aliasOrName = c.data.alias;
				}else if(c.data.categoryValue == 3){//搭台
					var begin = c.data.name.indexOf("(");
					var end = c.data.name.indexOf(")");
					aliasOrName = '<font color="green">' + c.data.name.substring(begin+1, end) +'</font>';
				}else{
					aliasOrName = '<font color="green">'+ c.data.categoryText +'</font>';
				}
				return tableCmpTemplet.format({
					dataIndex : c.index,
					id : c.data.id,
					click : 'ts.selectTable({event : this, id : '+ c.data.id +',tableAlias :'+ c.data.alias +'})',
					alias : aliasOrName,
					theme : c.data.statusValue == '1' ? "e" : "c",
					name : c.data.name == "" || typeof c.data.name != 'string' ? c.data.alias + "号桌" : c.data.name,
					tempPayStatus : c.data.isTempPaid? '暂结' : '&nbsp;&nbsp;',
					bookTableStatus : c.data.isBook? '订' : '',
					tempPayStatusClass : navigator.userAgent.indexOf("Firefox") >= 0?'tempPayStatus4Moz':'tempPayStatus'
				});				
			}
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
						location.href = 'verifyLogin.jsp?status=' + systemStatus;
					}, 2000);
				}
				Util.LM.hide();	
			},
			error : function(request, status, error){
				Util.LM.hide();	
				Util.msg.tio('操作有误,请刷新页面');
				setTimeout(function(){
					location.href = 'verifyLogin.jsp?status=' + systemStatus;
				}, 2000);
			}
		});
		
		
		//获取系统相关属性
		Util.sys.checkSmStat();
		
		//刷新微信预订单
		ts.refreshWeixinBook();
		
		//用户自定义日期
		$('#conditionDayBeginDay').bind('change', function(){
			if($('#conditionDayBeginDay').val() && $('#conditionDayEndDay').val() ){
				ts.searchBookList({begin:$('#conditionDayBeginDay').val(), end:$('#conditionDayEndDay').val()});
			}
		});	
		$('#conditionDayEndDay').bind('change', function(){
			if($('#conditionDayBeginDay').val() && $('#conditionDayEndDay').val() ){
				ts.searchBookList({begin:$('#conditionDayBeginDay').val(), end:$('#conditionDayEndDay').val()});
			}
		});	
		
		//pos端 && 体验端 && touch端
		if(systemStatus == 1){//pos端
			//日结,交班等
			$('#divPosOperation').show();
			$('#btnOrderAndPay').show();
			//下单并结账
			$('#orderPay_li_orderFood').show();
			//已点菜结账按钮
			$('#payOrder_a_checkOut').show();
			//收银端餐台列表高度
			tableListHeight = 130;	
	
		}else if(systemStatus == 3){//try端
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
			
		}else if(systemStatus == 2){//touch端
			//日结,交班等
			$('#divPosOperation').hide();
			$('#btnOrderAndPay').hide();
			//下单并结账
			$('#orderPay_li_orderFood').hide();
			//已点菜结账按钮
			$('#payOrder_a_checkOut').hide();
			//收银端餐台列表高度
			tableListHeight = 86;
		}else if(systemStatus == 4){//快餐模式
			//日结,交班等
			$('#divPosOperation').show();
			$('#btnOrderAndPay').show();
			//下单并结账
			$('#orderPay_li_orderFood').show();
			//已点菜结账按钮
			$('#payOrder_a_checkOut').show();
			//快餐模式按钮
			$('#fastFood_li_tableSelect').show();
			//快餐模式的结账按钮
			$('#fastPay_a_orderFood').show();
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
		
		var dailyPopup = null;
		//日结处理函数
		function dailyHandler(commitType){
			if(dailyPopup == null){
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
			}
			
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
				var fastIssuePopup = null;
				fastIssuePopup = new MemberReadPopup({
					confirm : function(member){
						if(member){
							fastIssuePopup.close(function(){
								var issueCouponPopup = new IssueCouponPopup({
									title : '快速发放优惠券',
									memberName : member.name,
									issueMode : IssueCouponPopup.IssueMode.FAST,
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
			}, 100);
		});
		
		//快速用券
		$('#fastUse_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				var fastUsePopup = null;
				fastUsePopup = new MemberReadPopup({
					confirm : function(member){
						if(member){
							fastUsePopup.close(function(){
								var useCouponPopup = null;
								useCouponPopup = new UseCouponPopup({
									title : '快速使用优惠券',
									memberName : member.name,
									issueMode : UseCouponPopup.UseMode.FAST,
									useTo  : member.id,
									useCuoponMethod : function(coupons){
										$.post('../OperateCoupon.do', {
												dataSource : 'coupon', 
												coupons : coupons.join(','), 
												useTo : member.id, 
												useMode : UseCouponPopup.UseMode.FAST.mode 
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
			}, 100);
		});
		
		//添加会员
		$('#addMember_a_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				var addMemberPopup = new  AddMemberPopup();
				addMemberPopup.open();
			}, 200);
		});
		
		//微信会员绑定
		$('#memberWxBind_li_tableSelect').click(function(){
			$('#frontPageMemberOperation').popup('close');
			setTimeout(function(){
				var memberWxReadPopup = null;
				memberWxReadPopup = new MemberReadPopup({
					confirm : function(member){
						if(member && member.isRaw){
							memberWxReadPopup.close(function(){
								var memberWxBindPopup = null;
								memberWxBindPopup = new PerfectMemberPopup({
									memberName : member.name,
									selectedMember : member.id,
									postBound : function(){
										memberWxBindPopup.close();
									}
								});
								memberWxBindPopup.open();
							}, 200);
						}else{
							Util.msg.tip('会员资料已完善');
						}
					}
				});
				memberWxReadPopup.open();
			}, 200);
		});
		
		//打印机绑定按钮
		$('#printBind_a_tableSelect').click(function(){
			$('#tableSelectOtherOperateCmp').popup('close');
			setTimeout(function(){
				var printBindPopup = new PrintBindPopup();
				printBindPopup.open();
			}, 300);
			
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
				var searchMemberPopup = null;
				searchMemberPopup = new MemberReadPopup({
					confirm : function(){
						searchMemberPopup.close();
					}
				});
				searchMemberPopup.open();
			}, 300);
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
					
	
		//多台开席
		$('#multiTables_a_tableSelect').click(function(){
			var _selectedTable = null;
			var askTablePopup = null;
			askTablePopup = new AskTablePopup({
				title : '多台开席选台',
				tables : WirelessOrder.tables,
				tableSelect : function(selectedTable){
					_selectedTable = selectedTable;
					var table = null;
					for (var i = 0; i < ts.multiOpenTableChoosedTable.length; i++) {
						if(ts.multiOpenTableChoosedTable[i].id == _selectedTable.id){
							table = true;
							ts.multiOpenTableChoosedTable.splice(i, 1);
							break;
						}
					}
					//选择餐台
					if(table == null){
						table = WirelessOrder.tables.getById(_selectedTable.id);
						if(table.statusValue == 1){
							Util.msg.tip("此餐台已使用, 不能选择");
							return;
						}
						ts.multiOpenTableChoosedTable.push(table);
					}
					askTablePopup.close();
					//刷新已点餐台
					ts.loadBookChoosedTable({renderTo : 'multiOpenTableHadChoose', tables:ts.multiOpenTableChoosedTable});	
	
				},
				middle : function(){
					Util.msg.tip('请选择一张餐桌');
				}
			});
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
		});
		
		//拼台
		$('#combineTable_a_tableSelect').click(function(){
			var _selectedTable = null;
			var askTablePopup = null;
			askTablePopup = new AskTablePopup({
				title : '拼台',
				tables : WirelessOrder.tables,
				tableSelect : function(selectedTable){
					_selectedTable = selectedTable;
					var table = null;
					
					for (var i = 0; i < ts.multiPayTableChoosedTable.length; i++) {
						if(ts.multiPayTableChoosedTable[i].id == _selectedTable.id){
							table = true;
							ts.multiPayTableChoosedTable.splice(i, 1);
							break;
						}
					}
					
					//选择餐台
					if(table == null){
						table = WirelessOrder.tables.getById(_selectedTable.id);
						if(table.statusValue == 0){
							Util.msg.tip("此餐台未点餐, 不能选择");
							return;
						}
						ts.multiPayTableChoosedTable.push(table);
					}
					
					askTablePopup.close();
					//刷新已点餐台
					ts.loadBookChoosedTable({renderTo : 'multiPayTableHadChoose', tables:ts.multiPayTableChoosedTable});	
					
				},
				middle : function(){
					Util.msg.tip('请选择一张餐桌');
				}
			});
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
		});
		
		//预订添加餐桌
		$('#addBookTable_a_tableSelect').click(function(){
			var _selectedTable = null;
			var askTablePopup = null;
			askTablePopup = new AskTablePopup({
				title : '选择餐桌',	
				tables : WirelessOrder.tables,
				tableSelect : function(selectedTable){
					_selectedTable = selectedTable;
					var table = null;
					
					for (var i = 0; i < ts.bookChoosedTable.length; i++) {
						if(ts.bookChoosedTable[i].id == _selectedTable.id){
							table = true;
							ts.bookChoosedTable.splice(i, 1);
							break;
						}
					}
					
					//选择餐台
					if(table == null){
						table = WirelessOrder.tables.getById(_selectedTable.id);
						
						ts.bookChoosedTable.push(table);
					}
					//刷新已点餐台
					ts.loadBookChoosedTable({renderTo : 'add_bookTableList', tables:ts.bookChoosedTable, otype:'display'});
					
					//显示预订添加界面餐台
					$('#box4BookTableList').show();
					askTablePopup.close();
				}
			});
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
			
		});
		
		//入座选餐桌
		$('#bookChoose_a_tableSelect').click(function(){
			var _selectedTable = null;
			var askTablePopup = null;
			askTablePopup = new AskTablePopup({
				title : '入座选桌',
				tables : WirelessOrder.tables,
				tableSelect : function(selectedTable){
					_selectedTable = selectedTable;
					var table = null;
					if(typeof _selectedTable.otype == 'undefined' || _selectedTable.otype == 'commit'){
						
						for (var i = 0; i < ts.bookChoosedTable.length; i++) {
							if(ts.bookChoosedTable[i].id == _selectedTable.id){
								table = true;
								break;
							}
						}
						//选择餐台
						if(table == null){
							table = WirelessOrder.tables.getById(_selectedTable.id);
							if(table.statusValue == 1){
								Util.msg.tip("此餐台已使用, 不能选择");
								return;
							}
							ts.bookChoosedTable.push(table);
						}			
						//去除已订餐台
						for (var i = 0; i < ts.bookOperateTable.oldTables.length; i++) {
							if(ts.bookOperateTable.oldTables[i].id == _selectedTable.id){
								ts.bookOperateTable.oldTables.splice(i, 1);
								break;
							}
						}			
					}else if(c.otype == 'display'){
						for (var i = 0; i < ts.bookChoosedTable.length; i++) {
							if(ts.bookChoosedTable[i].id == _selectedTable.id){
								table = ts.bookChoosedTable[i];
								ts.bookChoosedTable.splice(i, 1);
								break;
							}
						}
						//退回原餐台
						ts.bookOperateTable.oldTables.push(table);
					}
	
					//选台后关闭
					askTablePopup.close();
					//刷新入座餐台
					ts.loadBookChoosedTable({renderTo : 'bookTableHadChoose', tables:ts.bookChoosedTable, otype : 'display'});
					//刷新预订餐台
					ts.loadBookChoosedTable({renderTo : 'bookTableToChoose', tables:ts.bookOperateTable.oldTables, otype : 'commit'});		
	
					
				}
			});
			
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
		});
	
		//当日账单
		$('#todayBill_a_tableSelect').click(function(){
			//新页面打开账单管理
			window.open("../pages/FrontBusiness_Module/Bills.html");      
			ts.searchBookList();
		});
		
		//注销
		$('#logout_a_tableSelect').click(function(){
			Util.LM.show();
			$.ajax({
				url : '../LoginOut.do',
				success : function(data, status, xhr){
					location.href = 'verifyLogin.jsp?status=' + systemStatus;
					//location.reload();
				}
			});	
		});
		
	});

	//更新菜品列表
	function initFoodData(){
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
				
				//加载所有口味
				$.ajax({
					url : '../QueryMenu.do',
					type : 'post',
					data : {
						dataSource : 'tastes'
					},
					success : function(result, status, xhr){
						var tastes = result;
						
						of.allTastes = [];
						
						var data = [];
						if(tastes.root.length > 0){
							data.push({
								id : tastes.root[0].taste.cateValue,
								name : tastes.root[0].taste.cateText,
								items : []
							});
						}
						var has = true, temp = {};
						for(var i = 0; i < tastes.root.length; i++){
							
							of.allTastes.push(tastes.root[i]);
							
							has = false;
							for(var k = 0; k < data.length; k++){
								if(tastes.root[i].taste.cateValue == data[k].id){
									data[k].items.push(tastes.root[i]);
									has = true;
									break;
								}
							}
							if(!has){
								temp = {
										id : tastes.root[i].taste.cateValue,
										name : tastes.root[i].taste.cateText,
										items : []
								};
								temp.items.push(tastes.root[i]);
								data.push(temp);
							}
						}	
						
						of.tasteGroups = data;
						of.tasteGroups.unshift({
							id : -10,
							name : '常用口味',
							items : of.allTastes		
						});
					},
					error : function(request, status, err){
						Util.msg.alert({
							msg : request.msg,
							renderTo : 'tableSelectMgr'
						});
					}
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
	
});	

/** 
 * 通过其他界面返回餐台选择
 */
ts.loadData = function(){
	location.href = '#tableSelectMgr';
	ts.commitTableOrTran = null;
	initTableData();
};

//设置搜索出来的餐台升序排序, 按名称长短
ts.searchTableCompareByName = function (obj1, obj2) {
    var val1 = obj1.name.length;
    var val2 = obj2.name.length;
    if (val1 > val2) {
        return 1;
    } else if (val1 < val2) {
        return -1;
    } else {
        return 0;
    }            
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
	
	//会员充值读卡
    $('#txtMemberCharge4Read').on('keypress',function(event){
        if(event.keyCode == "13")    
        {
        	ts.member.readMemberByCondtion4Charge();
        }
    });	
    
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
 * 预订列表匹配
 */
ts.bookSearch = {
	file : null,
	fileValue : null,
	init : function(c){
		this.file = document.getElementById(c.file);
		if(typeof this.file.oninput != 'function'){
			this.file.oninput = function(e){
				ts.bookSearch.fileValue = ts.bookSearch.file.value;
				var data = null, temp = null;
				if(ts.bookSearch.fileValue.trim().length > 0){
					data = [];
					temp = ts.bookList;
					for(var i = 0; i < temp.length; i++){
						if((temp[i].tele).indexOf(ts.bookSearch.fileValue.trim()) != -1){
							data.push(temp[i]);
						}
					}				
					if(data != null){
						ts.loadBookListData(data);			
					}
					
					data = null;
					temp = null;					
				}else{
					ts.loadBookListData(ts.bookList);	
				}
				

			};
		}
		return this.file;
	},
	valueBack : function(){
		if(this.file.value){
			this.file.value = this.file.value.substring(0, this.file.value.length - 1);
			this.file.oninput(this.file);			
		}

		this.file.focus();
	},
	select : function(){
		this.file.select();
	},
	clear : function(){
		this.file.value = '';
		this.file.oninput(this.file);
		this.file.select();
	},
	callback : function(){
		co.s.clear();
	},
	onInput : function(){
		this.file.oninput(this.file);		
	},	
	fireEvent : function(){
		ts.bookSearch.onInput();
	}
};

/**
 * 预订列表名称匹配
 */
ts.bookSearchByName = {
	file : null,
	fileValue : null,
	init : function(c){
		this.file = document.getElementById(c.file);
		if(typeof this.file.oninput != 'function'){
			this.file.oninput = function(e){
				ts.bookSearchByName.fileValue = ts.bookSearchByName.file.value;
				var data = null, temp = null;
				if(ts.bookSearchByName.fileValue.trim().length > 0){
					data = [];
					temp = ts.bookList;
					for(var i = 0; i < temp.length; i++){
						if((temp[i].member).indexOf(ts.bookSearchByName.fileValue.trim()) != -1){
							data.push(temp[i]);
						}
					}				
					if(data != null){
						ts.loadBookListData(data);			
					}
					
					data = null;
					temp = null;					
				}else{
					ts.loadBookListData(ts.bookList);	
				}
				

			};
		}
		return this.file;
	},
	valueBack : function(){
		if(this.file.value){
			this.file.value = this.file.value.substring(0, this.file.value.length - 1);
			this.file.oninput(this.file);			
		}

		this.file.focus();
	},
	select : function(){
		this.file.select();
	},
	clear : function(){
		this.file.value = '';
		this.file.oninput(this.file);
		this.file.select();
	},
	callback : function(){
		co.s.clear();
	},
	onInput : function(){
		this.file.oninput(this.file);		
	},	
	fireEvent : function(){
		ts.bookSearchByName.onInput();
	}
};


/**
 * 搜索出来的结果点击直接提交
 */
ts.toOrderFoodOrTransFood = function(c){
	if(ts.commitTableOrTran == 'table'){
		ts.renderToCreateOrder(c.alias, 1);
	}else if(ts.commitTableOrTran == 'trans'){
		uo.transFood({id:c.id});
	}else if(ts.commitTableOrTran == 'allTrans'){
		uo.transFood({id:c.id, allTrans : -1});
	}else if(ts.commitTableOrTran == 'transTable'){
		ts.transTable({alias:c.alias});
	}else if(ts.commitTableOrTran == 'lookup'){
		updateTable({
			id : c.id,
			alias : c.alias
		});		
	}else if(ts.commitTableOrTran == 'apartTable'){
		$('#divSelectTablesForTs').hide();
		$('#divSelectTablesSuffixForTs').show();
		ts.table.id = c.id;
	}else if(ts.commitTableOrTran == 'bookTableChoose'){//预订入座选台
		var table = null;
		if(typeof c.otype == 'undefined' || c.otype == 'commit'){
			
			for (var i = 0; i < ts.bookChoosedTable.length; i++) {
				if(ts.bookChoosedTable[i].id == c.id){
					table = true;
					break;
				}
			}
			//选择餐台
			if(table == null){
				table = WirelessOrder.tables.getById(c.id);
				if(table.statusValue == 1){
					Util.msg.tip("此餐台已使用, 不能选择");
					return;
				}
				ts.bookChoosedTable.push(table);
			}			
			//去除已订餐台
			for (var i = 0; i < ts.bookOperateTable.oldTables.length; i++) {
				if(ts.bookOperateTable.oldTables[i].id == c.id){
					ts.bookOperateTable.oldTables.splice(i, 1);
					break;
				}
			}			
		}else if(c.otype == 'display'){
			for (var i = 0; i < ts.bookChoosedTable.length; i++) {
				if(ts.bookChoosedTable[i].id == c.id){
					table = ts.bookChoosedTable[i];
					ts.bookChoosedTable.splice(i, 1);
					break;
				}
			}
			//退回原餐台
			ts.bookOperateTable.oldTables.push(table);
		}

		//选台后关闭
		uo.closeTransOrderFood();
		//刷新入座餐台
		ts.loadBookChoosedTable({renderTo : 'bookTableHadChoose', tables:ts.bookChoosedTable, otype : 'display'});
		//刷新预订餐台
		ts.loadBookChoosedTable({renderTo : 'bookTableToChoose', tables:ts.bookOperateTable.oldTables, otype : 'commit'});		
		
	}else if(ts.commitTableOrTran == 'addBookTableChoose'){//添加预订选台
		var table = null;
		for (var i = 0; i < ts.bookChoosedTable.length; i++) {
			if(ts.bookChoosedTable[i].id == c.id){
				table = true;
				ts.bookChoosedTable.splice(i, 1);
				break;
			}
		}
		
		//选择餐台
		if(table == null){
			table = WirelessOrder.tables.getById(c.id);
			
			ts.bookChoosedTable.push(table);
		}
		//刷新已点餐台
		ts.loadBookChoosedTable({renderTo : 'add_bookTableList', tables:ts.bookChoosedTable, otype:'display'});
		
		//显示预订添加界面餐台
		$('#box4BookTableList').show();
		
		//选台后关闭
		uo.closeTransOrderFood();
	}else if(ts.commitTableOrTran == 'multiOpenTableChoose'){//多台开席选台
		var table = null;
			
		for (var i = 0; i < ts.multiOpenTableChoosedTable.length; i++) {
			if(ts.multiOpenTableChoosedTable[i].id == c.id){
				table = true;
				ts.multiOpenTableChoosedTable.splice(i, 1);
				break;
			}
		}
		
		//选择餐台
		if(table == null){
			table = WirelessOrder.tables.getById(c.id);
			if(table.statusValue == 1){
				Util.msg.tip("此餐台已使用, 不能选择");
				return;
			}
			ts.multiOpenTableChoosedTable.push(table);
		}
		//选台后关闭
		uo.closeTransOrderFood();
		//刷新已点餐台
		ts.loadBookChoosedTable({renderTo : 'multiOpenTableHadChoose', tables:ts.multiOpenTableChoosedTable});	

		
	}else if(ts.commitTableOrTran == 'multiPayTableChoose'){//多台并台选台
		var table = null;
			
		for (var i = 0; i < ts.multiPayTableChoosedTable.length; i++) {
			
			if(ts.multiPayTableChoosedTable[i].id == c.id){
				table = true;
				ts.multiPayTableChoosedTable.splice(i, 1);
				break;
			}
		}
		
		//选择餐台
		if(table == null){
			table = WirelessOrder.tables.getById(c.id);
			if(table.statusValue == 0){
				Util.msg.tip("此餐台未点餐, 不能选择");
				return;
			}
			ts.multiPayTableChoosedTable.push(table);
		}
		//选台后关闭
		uo.closeTransOrderFood();
		//刷新已点餐台
		ts.loadBookChoosedTable({renderTo : 'multiPayTableHadChoose', tables:ts.multiPayTableChoosedTable});	

		
	}
	$('#divSelectTablesForTs').html("");
};

var printerConnectionTemplet = '<tr>' +
		'<td>{index}</td>' +
		'<td>{name}</td>' +
		'<td >{driver}</td>' +
		'<td>{port}</td>' +
		'<td>{gateway}</td>' +
		'<td >{ping}</td>' +
		'<td >{coverOpen}</td>' +
		'<td >{paperEnd}</td>' +
		'<td >{cutterError}</td>' +
	'</tr>';

/**
 * 打开远程诊断
 */
ts.displayPrintConnection = function(){
	Util.LM.show();
	$.ajax({
		url : '../PrinterDiagnosis.do',
		type : 'post',
		dataType : 'json',
//		data : { pin : 29 },
		success : function(result){
			Util.LM.hide();
			if(result.success){
				$('#printerConnectionCount').text(result.root[0].printers.length);
/*				var root =[{
					printerName : '192.168.1.202',
					printerAlias : '中厨',
					printerPort : '192.168.1.202',
					driver : false,
					ping : false,
					coverOpen : true,
					paperEnd : true,
					cutterError : true
				}];*/
				
				if(result.root[0].connectionAmount == 1){
					$('#printerServiceState').html('<font color="green">正常</font>');
				}else if(result.root[0].connectionAmount == 2){
					$('#printerServiceState').html('<a href="#printerServiceOpenTwiceCmp" data-rel="popup" data-transition="pop">服务重叠 (所有打印机出重单, 点击解决)</a>');
				}else{
					$('#printerServiceState').html('<a href="#printerServiceUnopenCmp" data-rel="popup" data-transition="pop">未打开 (所有打印机不出单, 点击解决)</a>');
				}
				
				var html = [];
				for (var i = 0; i < result.root[0].printers.length; i++) {
					var printer = result.root[0].printers[i];
					var details = printer.driver && printer.ping;
					html.push(printerConnectionTemplet.format({
						index : i+1,
						name : printer.printerName + (printer.printerAlias?'('+printer.printerAlias+')':''),
						driver : printer.driver ? '<font color="green">正常</font>' : '<a href="#printerDriverErrorCmp" data-rel="popup" data-transition="pop">失败</a>', 
						port : printer.printerPort?printer.printerPort:'----',
						gateway : printer.gateway?printer.gateway:'----',
						ping : printer.ping ? '<font color="green">正常</font>' : '<a href="#printerPingErrorCmp" data-rel="popup" data-transition="pop">失败</a>',
						coverOpen : !details? "----" : printer.coverOpen ? '<a href="#">未关闭</a>' : '<font color="green">正常</font>',
						paperEnd : 	!details? "----" : printer.paperEnd ? '<a href="#">已用完</a>' : '<font color="green">正常</font>',
						cutterError : 	!details? "----" : printer.paperEnd ? '<a href="#">已损坏</a>' : '<font color="green">正常</font>'
					}));
				}
				$('#printerConnectionList').html(html.join('')).trigger('create').trigger('refresh');
				
			
			}else{
				$('#printerServiceState').html('<a href="#printerServiceUnopenCmp" data-rel="popup" data-transition="pop">未打开 (所有打印机不出单, 点击解决)</a>');
				$('#printerConnectionCount').text(0);
				$('#printerConnectionList').html('');
			}
			
			$('#printerConnectionCmp').show();
			$('#shadowForPopup').show();	
		},
		error : function(result){
			Util.LM.hide();
			Util.msg.alert({
				renderTo : 'tableSelectMgr',
				msg : '诊断出错, 请联系客服'
			});
		}
	});
};

/**
 * 关闭远程诊断
 */
ts.closePrintConnection = function(){
	$('#printerConnectionCmp').hide();
	$('#shadowForPopup').hide();
};

/**
 * 关闭开台
 */
ts.closeTableWithPeople = function(){
	//人数输入框设置默认
	$('#inputTableOpenCommon').val("");
	$('#input_input_numKbPopup').val(1);
	
};


/**
 * 选中一张餐桌
 * @param c
 */
ts.selectTable = function(c){
	updateTable({
		id : c.id,
		alias : c.tableAlias,
		event : c.event
	});
};

/**
 *桌号输入页面的确定按钮
 */
ts.submitForSelectTableNumTS = function(){
	//获得餐桌号
	var tableNo;
	var peopleNo = 1;
	if($("#left_input_askTable").val()){
		tableNo = parseInt($("#left_input_askTable").val());
	}else{
		tableNo = -1;
	}
	
	ts.renderToCreateOrder(tableNo, peopleNo);	
};

/**
 * 桌号人数界面的点菜按钮
 */
ts.createOrderForShowMessageTS = function(){
	var tableNo;
	var peopleNo;
	tableNo = parseInt($("#txtTableNumForTS").val());
	if($("#openTablePeople").val()){
		peopleNo = parseInt($("#txtPeopleNumForSM").val());
	}else{
		peopleNo = 1;
	}	
	renderToCreateOrder(tableNo, peopleNo);
};

/**
 * 去沽清界面
 */
ts.stopSellMgr = function(){
	location.href = '#stopSellMgr';
	ss.entry();
};

//进入点菜界面
ts.renderToCreateOrder = function(tableNo, peopleNo, comment){
	if(tableNo > 0){
		//关闭台操作popup
		uo.closeTransOrderFood();
		//关闭开台
		ts.closeTableWithPeople();
		
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
				tableToAlias.customNum = peopleNo;
				of.entry({
					table : tableToAlias,
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
 * 根据餐桌alias，返回餐桌对象
 * @param {int} tableAlias
 * @returns {object} 
 */
//function getTableByAlias(tableAlias){
//	for(x in WirelessOrder.tables){
//		if(WirelessOrder.tables[x].alias == tableAlias){
//			return WirelessOrder.tables[x];		
//		}
//	}
//}

/**
 * 根据餐桌id，返回餐桌对象
 * @param {int} tableId
 * @returns {object} 
 */
//function getTableById(tableId){
//	for(x in WirelessOrder.tables){
//		if(WirelessOrder.tables[x].id == tableId){
//			return WirelessOrder.tables[x];		
//		}
//	}
//}



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
									ts.closeTableWithPeople();
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
							ts.checkBookTable();
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
						}
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
					
					var remark = '<div>'+
								'<input id="inputTableOpenCommon" placeholder="开台备注" data-type="num" class="countInputStyle" >'+
							'</div>';
					self.find('[id=content_div_numKbPopup]').append(remark);
					self.find('[id=content_div_numKbPopup]').trigger('create');
					
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
								ts.closeTableWithPeople();
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
						}
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
					
					var remark = '<div>'+
								'<input id="inputTableOpenCommon" placeholder="开台备注" data-type="num" class="countInputStyle" >'+
							'</div>';
					self.find('[id=content_div_numKbPopup]').append(remark);
					self.find('[id=content_div_numKbPopup]').trigger('create');
					
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
	$.post('../QueryRegion.do', {dataSource : 'normal'}, function(response, status, xhr){
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
				
				Util.LM.hide();
			}else{
				Util.LM.hide();
				Util.msg.alert({
					title : data.title,
					msg : data.msg,
					renderTo : 'tableSelectMgr',
					time : 2
				});
			}
		}
	});	
	
	//刷新餐台是刷新预订
	ts.refreshWeixinBook();
	
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
		ts.tp.init({
		    data : result
		});
		ts.tp.getFirstPage();
		
	}else{
		$("#divTableShowForSelect").html("");
	}	
}




/**
 * 打开会员充值
 */
ts.member.openMemberChargeWin = function(){
	$('#frontPageMemberOperation').popup('close');
	//充值金额
	$('#rd_numPayMannerMoney').on('keyup', function(){
		var chargeMoney = $('#rd_numPayMannerMoney').val();
		var actualChargeMoney = $('#rd_numRechargeMoney');
		actualChargeMoney.val(Math.round(chargeMoney * ts.member.chargeRate));
	});	
	
	if(getcookie(document.domain+'_chargeSms') == 'true'){
		$('#chbSendCharge').attr('checked', true).checkboxradio("refresh");
	}else{
		$('#chbSendCharge').attr('checked', false).checkboxradio("refresh");
	}
	
	if(Util.sys.smsModule){
		$('#td4ChbSendCharge').show();
		$('#lab4SendSms').html('发送充值信息'+(Util.sys.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Util.sys.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Util.sys.smsCount+'条, 请及时充值</font>)'));
	}
	
	$('#memberChargeWin').show();
	$('#shadowForPopup').show();
	
	setTimeout(function(){
		$('#txtMemberCharge4Read').focus();
	}, 250);
};

/**
 * 关闭会员充值
 */
ts.member.closeMemberChargeWin = function(){
	$('#memberChargeWin').hide();
	$('#shadowForPopup').hide();
	
	ts.member.loadMemberInfo4Charge();
	$('#txtMemberCharge4Read').val('');
	
	delete ts.member.rechargeMember;
};

/**
 * 充值读取会员
 */
ts.member.readMemberByCondtion4Charge = function(stype){
	var memberInfo = $('#txtMemberCharge4Read');
	
	if(!memberInfo.val()){
		Util.msg.alert({msg:'请填写会员相关信息', topTip:true});
		memberInfo.focus();
		return;
	}
	
	if(stype){
		$('#charge_searchMemberType').popup('close');
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
					ts.member.rechargeMember = jr.root[0];
					ts.member.loadMemberInfo4Charge(jr.root[0]);
				}else if(jr.root.length > 1){
					$('#charge_searchMemberType').popup('open');
					$('#charge_searchMemberType').css({top:$('#btnReadMember4Charge').position().top - 270, left:$('#btnReadMember4Charge').position().left-300});
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
 * 充值时加载会员信息
 */
ts.member.loadMemberInfo4Charge = function(member){
	member = member == null || typeof member == 'undefined' ? {} : member;
	var memberType = member.memberType ? member.memberType : {};
	
	$('#rd_numPayMannerMoney').val('');
	$('#rd_numRechargeMoney').val('');
	
	$('#rd_numBaseBalance').text(typeof member.baseBalance != 'undefined'?member.baseBalance:'----');
	$('#rd_numExtraBalance').text(typeof member.extraBalance != 'undefined'?member.extraBalance:'----');
	$('#rd_numTotalBalance').text(typeof member.totalBalance != 'undefined'?member.totalBalance:'----');
	$('#rd_txtMemberName').text(member.name?member.name:'----');
	$('#rd_txtMmeberType').text(memberType.name?memberType.name:'----');
	$('#rd_txtMemberSex').text(member.sexText?member.sexText:'----');	
	
	$('#rd_numMemberMobileForRecharge').text(member.mobile?member.mobile:'----');
	$('#rd_numMemberCardForRecharge').text(member.memberCard?member.memberCard:'----');	
	$('#rd_numWeixinMemberCard').text(member.weixinCard?member.weixinCard:'----');
	
	if(!jQuery.isEmptyObject(member)){
		//充值比率
		ts.member.chargeRate = member.memberType.chargeRate;
		$('#rd_numPayMannerMoney').focus();		
	}

};

/**
 * 充值操作
 * @param _c
 */
ts.member.rechargeControlCenter = function(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	if(ts.member.rechargeMember == null || typeof ts.member.rechargeMember == 'undefined'){
		Util.msg.tip('未读取会员信息, 请先刷卡.');
		return;
	}
	
	if(ts.member.rechargeMember.memberType.attributeValue != 0){
		Util.msg.alert({
			renderTo : 'tableSelectMgr',
			msg : '积分属性会员不允许充值, 请重新刷卡.'
		});
		return;
	}
	
	var rechargeMoney = $('#rd_numRechargeMoney');
	var rechargeType = $('#rd_comboRechargeType');
	var payMannerMoney = $('#rd_numPayMannerMoney');
//	var comment = $('#rd_txtRechargeComment');
	
	if(!rechargeMoney.val()){
		Util.msg.alert({
			topTip : true,
			msg : '请输入充值金额'
		});
		return;
	}
	
	if(!payMannerMoney.val()){
		Util.msg.alert({
			topTip : true,
			msg : '请输入账户充额'
		});
		return;		
	}
	//设置cookie
	if($('#chbSendCharge').attr('checked')){
		setcookie(document.domain+'_chargeSms', true);
	}else{
		delcookie(document.domain+'_chargeSms');
	}
	
	Util.LM.show();
	
	$.post('../OperateMember.do', {
		dataSource : 'charge',
		memberID : ts.member.rechargeMember.id,
		rechargeMoney : rechargeMoney.val(),
		rechargeType : rechargeType.val(),
		payMannerMoney : payMannerMoney.val(),
		isPrint : $('#chbPrintRecharge').attr('checked')?true:false,
		sendSms : $('#chbSendCharge').attr('checked')?true:false	
	}, function(jr){
		Util.LM.hide();
		if(jr.success){
			ts.member.closeMemberChargeWin();
			//更新短信
			Util.sys.checkSmStat();
			
			Util.msg.alert({
				topTip : true,
				msg : jr.msg
			});
		}else{
			Util.msg.alert({
				renderTo : 'tableSelectMgr',
				msg : jr.msg
			});
		}		
	});
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

/**
 * 查询会员消费明细
 */
ts.member.searchMemberDetail = function(){
	Util.LM.show();
//	var memberType = $('#consumeDetail_memberType').val();
	var operateType = -1;
	var detailOpes = $('input[name=memberConsumeType]'); 
	for (var i = 0; i < detailOpes.length; i++) {
		
		if($(detailOpes[i]).attr("checked")){
			operateType = $(detailOpes[i]).attr("data-value");
			break;
		}
	}
	var name = $('#consumeDetail_memberName').val();
	
	$.ajax({
		url : '../QueryMemberOperation.do',
		type : 'post',
		dataType : 'json',
		data : {
			isPaging:false,
			dataSource:'today',
			fuzzy: name,
			operateType:operateType		
		},
		success : function(result, status, xhr){
			Util.LM.hide();
			if(result.success){
				var html = [];
				for(var i = 0, index = 1; i < result.root.length; i++){
					html.push(memberConsumeTrTemplet.format({
						dataIndex : index,
						orderId : result.root[i].orderId != 0?result.root[i].orderId:'----',
						operateDateFormat : result.root[i].operateDateFormat,
						memberName : result.root[i].member.name,
						memberType : result.root[i].member.memberType.name,
						otype : result.root[i].operateTypeText,
						money : result.root[i].payMoney?result.root[i].payMoney:(result.root[i].deltaTotalMoney?result.root[i].deltaTotalMoney:0),	
						deltaPoint : result.root[i].deltaPoint > 0? '+'+result.root[i].deltaPoint : result.root[i].deltaPoint,
						staffName : result.root[i].staffName,
						comment : result.root[i].comment
					}));	
					index ++;
				}	
				
				$('#front_memberConsumeDetailBody').html(html.join("")).trigger('create');
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				renderTo : 'tableSelectMgr',
				msg : request.msg
			});
		}
	});		
}; 

/**
 * 打开会员消费明细
 */
ts.member.openMemberConsumeDetailWin = function(){
	$('#frontPageMemberOperation').popup('close');
	
	$.ajax({
		url : '../QueryMemberType.do',
		type : 'post',
		async:false,
		data : {dataSource : 'normal'},
		success : function(jr, status, xhr){
			if(jr.success){
				var html = ['<option value=-1 >全部</option>'];
				for (var i = 0; i < jr.root.length; i++) {
					html.push('<option value={id} >{name}</option>'.format({
						id : jr.root[i].id,
						attrVal : jr.root[i].attributeValue,
						chargeRate : jr.root[i].chargeRate,
						name : jr.root[i].name
					}));
				}
				$('#consumeDetail_memberType').html(html.join("")).selectmenu('refresh');
			}else{
				Util.msg.alert({
					renderTo : 'tableSelectMgr',
					msg : jr.msg
				});
			}
		},
		error : function(request, status, err){
			Util.msg.alert({
				renderTo : 'tableSelectMgr',
				msg : request.msg
			});
		}
	});	
	
	ts.member.searchMemberDetail();
	
	$('#memberConsumeDetailWin').show();
	$('#shadowForPopup').show();
};

/**
 * 关闭会员消费明细
 */
ts.member.closeMemberConsumeDetailWin = function(){
	$('#memberConsumeDetailWin').hide();
	$('#shadowForPopup').hide();
	
	$('#consumeDetail_memberName').val('');
	$('#front_memberConsumeDetailBody').html('');
};

/**
 * 酒席分部门入账
 */
ts.displayFeastPayWin = function(){
	$('#tableSelectOtherOperateCmp').popup('close');
	$.post('../QueryDept.do', {dataSource:'normal'}, function(result){
		ts.depts4Feast = result.root;
		
		var html=[];
		for (var i = 0; i < ts.depts4Feast.length; i++) {
			html.push('<li class="popupButtonList" data-icon="false"><a onclick="ts.addFeastDepartment({index})">{name}</a></li>'.format({
				index : i,
				name : ts.depts4Feast[i].name
			}));
		}
		
		$('#departmentsListCmp').html(html.join('')).listview('refresh');		
	});
	
	$('#feastPayWin').show();
	$('#shadowForPopup').show();
	
};

/**
 * 关闭酒席入账
 */
ts.closeFeastPayWin = function(){
	$('#feastPayWin').hide();
	$('#shadowForPopup').hide();	
	
	//清空操作
	$('#feastPayWinTable').html('');
	$('#feastPayTotalPrice').text(0);	
	ts.addFeastDepartment.depts.length = 0;
};

/**
 * 计算酒席入账总金额
 */
ts.calcFeastPay = function(){
	var totalMoney = 0;
	//清空酒席记录再重新赋值
	ts.addFeastDepartment.deptFeast.length = 0;
	
	for (var i = 0; i < ts.addFeastDepartment.depts.length; i++) {
		if($('#numForFeast'+ts.addFeastDepartment.depts[i]).val()){
			totalMoney += parseFloat($('#numForFeast'+ts.addFeastDepartment.depts[i]).val());
			//console.log(typeof $('#numForFeast'+ts.addFeastDepartment.depts[i]).val())
			ts.addFeastDepartment.deptFeast.push(ts.addFeastDepartment.depts[i] + "," + $('#numForFeast'+ts.addFeastDepartment.depts[i]).val());
		}
	}
	
	$('#feastPayTotalPrice').text(totalMoney);	
};

/**
 * 添加入账部门
 */
ts.addFeastDepartment = function(index){
	
	var dept = ts.depts4Feast[index], fieldId = "numForFeast" + dept.id; 
	
	for (var i = 0; i < ts.addFeastDepartment.depts.length; i++) {
		if(ts.addFeastDepartment.depts[i] == dept.id){
			Util.msg.tip("此部门已添加");
			return;
		}
	}
	
	ts.addFeastDepartment.depts.push(dept.id);
	
	var html = feastPayDeptTemplet.format({
		numberfieldId : fieldId,
		deptName : dept.name,
		deptId : dept.id,
		tr4Feast : 'tr4Feast'+dept.id
	});
	
	$('#feastPayWinTable').append(html).trigger('create');
	
	//找零快捷键
    $('#'+fieldId).on('keyup', function(btn){
    	ts.calcFeastPay();
	});	
	
	$('#popupDepartmentsCmp').popup('close');
	
};

//记录添加的部门
ts.addFeastDepartment.depts = [];
//记录添加的部门和金额
ts.addFeastDepartment.deptFeast = [];

/**
 * 移除部门收益
 */
ts.feastPayRemoveAction = function(c){
	var curField = $(c.event);
	var numForAlias = parseInt(curField.attr('data-for'));
	var name = curField.attr('data-text');
	
	Util.msg.alert({
		msg : '是否去除 <font color="green">' + name +'</font> 收益?',
		renderTo : 'tableSelectMgr',
		buttons : 'yesback',
		certainCallback : function(){
			for (var i = 0; i < ts.addFeastDepartment.depts.length; i++) {
				if(numForAlias == ts.addFeastDepartment.depts[i]){
					ts.addFeastDepartment.depts.splice(i, 1);
					break;
				}
			}

			$('#tr4Feast'+numForAlias).remove();		
			
			ts.calcFeastPay();
		}
	});
};

/**
 * 确定酒席入账
 */
ts.doFeastOrder = function(){
	Util.LM.show();
	$.post('../FeastOrder.do', {deptFeasts : ts.addFeastDepartment.deptFeast.join('&')}, function(result){
		Util.LM.hide();
		if(result.success){
			ts.closeFeastPayWin();
			//先跳转到结账界面再操作
			updateTable({
				toPay : true,
				id : result.other.tableId
			});	
			//刷新餐台数据
			initTableData();
		}else{
			Util.msg.alert({
				msg : result.msg,
				renderTo : 'tableSelectMgr'
			});
		}
	}, "json");
};


//预订=============================================
/**
 * 刷新微信预订单
 */
ts.refreshWeixinBook = function(){
	$.post('../QueryBook.do', {dataSource: 'normal', status:1}, function(data){
		if(data.success){
			if(data.root && data.root.length > 0){
				$('#amount4Book').html(data.root.length);
				$('#amount4Book').show();
			}else{
				$('#amount4Book').hide();
			}
		}
	}, 'json');	
};

/**
 * 加载预订数据
 */
ts.loadBookListData = function(data){
	var bookListTemplate = '<tr>' +
		'<td>{list}</td>' +
		'<td>{bookDate}</td>' +
		'<td>{region}</td>' +
		'<td>{member}</td>' +
		'<td>{tele}</td>' +
		'<td>{amount}</td>' +
		'<td>{status}</td>' +
		'<td>{staff}</td>' +
		'<td><a href="javascript: ts.addBookInfo({type:\'look\', index:{index}})">{lookout}</a></td>' +
		'<td><div data-role="controlgroup" data-type="horizontal"><a href="#" data-role="button" data-theme="b" onclick="ts.addBookInfo({type:\'confirm\', index:{index}})">{confirmOrUpdate}</a><a data-role="button" data-theme="b" onclick="ts.bookOperateTable({bookId:{bookId}, index:{index}})">入座</a><a data-role="button" data-theme="b" onclick="ts.deleteBook({index:{index}})">删除</a></div></td>' +
	'</tr>';
	
	var html = [];
	for (var i = 0; i < data.length; i++) {
		var region = [];
		if(data[i].tables.length > 0){
			for (var j = 0; j < data[i].tables.length; j++) {
				region.push(data[i].tables[j].name);
			}
			region = region.join(",");
		}else{
			region = data[i].region;
		}
		
		html.push(bookListTemplate.format({
			list : i+1,
			index : i,
			bookId : data[i].id,
			bookDate : data[i].bookDate,
			lookout : data[i].isExpired ? '<span style="color:red">查看(超时)</span>' : '查看',
			region : region,
			member : data[i].member,
			tele : data[i].tele,
			amount : data[i].amount,
			status : data[i].status == 1? '<span style="color:green">'+ data[i].statusDesc +' ('+ data[i].sourceDesc +')</span>' : data[i].statusDesc,
			confirmOrUpdate : data[i].status == 1? '确认' : '修改',
			staff : data[i].staff
		}));
	}
	$('#bookOrderListBody').html(html.join("")).trigger('create');
};

/**
 * 查询列表
 */
ts.searchBookList = function(c){
	Util.LM.show();
	
	var name = $('#searchBookPerson').val();
	var phone = $('#searchBookPhone').val();
	var status = $('#searchBookStatus').val();
	var bookDate = $('input[name=bookDateType]:checked').val();
	var begin, end;
	
	if(c && c.begin){
		begin = c.begin;
		end = c.end;
	}else{
		$('#conditionDayBegin').hide();
		$('#conditionDayEnd').hide();
	}
	
	$.post('../QueryBook.do', {
		dataSource : 'normal',
		name : name,
		phone : phone,
		status : status,
		bookDate : bookDate,
		beginDate : begin,
		endDate : end, 
		tableId : ts.bookTable4Search || ""
	}, function(data){
		Util.LM.hide();
		
		if(data.success){
			ts.bookList = data.root;
			ts.loadBookListData(data.root);
		}
		
	}, 'json');	
};

/**
 * 打开日期条件筛选
 */
ts.openConditionDay = function(){
	$('#conditionDayBeginDay').val('');
	$('#conditionDayEndDay').val('');
	$('#conditionDayBegin').show();
	$('#conditionDayEnd').show();
};


/**
 * 进入订单列表Entry
 */
ts.bookListEntry = function(){
	
	//条件筛选初始化
	ts.bookSearch.init({file : 'searchBookPhone'});	
	ts.bookSearchByName.init({file : 'searchBookPerson'});	
	
	//去已预订界面
	location.href="#bookOrderListMgr";
	
	ts.searchBookList();
	
	var now = new Date();
	
	var today = now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate();
	ts.bookListEntry.today = today;
	
	now.setDate(now.getDate() + 1);
	var tomorrow = now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate();

	now.setDate(now.getDate() + 1);
	var afterday = now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate();
	
	$('#bookDate_today').attr('value', today);
	$('#bookDate_tomorrow').attr('value', tomorrow);
	$('#bookDate_afterday').attr('value', afterday);
	
	//加载经手人
	$.post('../QueryStaff.do', function(data){
		if(data.success){
			var html = [];
			var option = '<option value="{id}">{name}</option>';
			data.root.forEach(function(e){
				html.push(option.format({
					id : e.staffID,
					name : e.staffName
				}));
			});
			$('#add_staff').html(html.join("")).selectmenu("refresh");
			
		}
	}, 'json');

};

/**
 * 刷新预订
 */
ts.refreshBookList = function(){
	$('#searchBookPerson').val("");
	$('#searchBookPhone').val("");
	$('#searchBookStatus').val(-1).selectmenu("refresh");
	$('input[name="bookDateType"]:checked').removeAttr("checked").checkboxradio("refresh");
	$("input[name=bookDateType]:eq(0)").attr("checked",'checked').checkboxradio("refresh");
	
	ts.bookListEntry();
};

/**
 * 预订返回
 */
ts.bookListBack = function(){
	//返回到餐桌界面
	ts.loadData();
	
	delete ts.bookTable4Search;
	
	$('#searchBookPerson').val("");
	$('#searchBookPhone').val("");
	$('#searchBookStatus').val(-1).selectmenu("refresh");
	$('input[name="bookDateType"]:checked').removeAttr("checked").checkboxradio("refresh");
	$("input[name=bookDateType]:eq(0)").attr("checked",'checked').checkboxradio("refresh");	
};

/**
 * 打开预订入座
 */
ts.bookOperateTable = function(c){
	if(ts.bookList[c.index].status != 2){
		Util.msg.tip("预订不是【已确认】状态, 不能入座");
		return;
	}
	
	if(ts.bookList[c.index].isExpired){
		Util.msg.tip("预订已过期, 不能入座");
		return;
	}
	
	//清空菜品
	of.newFood.length = 0;
	var book = ts.bookList[c.index];
	$.ajax({
		url : '../QueryBook.do',
		type : 'post',
		dataType : 'json',
		async : false,
		data :{
			dataSource : 'checkout',
			id : book.id
		},
		success : function(data){
			if(data.success){
				book = data.root[0];
				if(data.root[0].order && data.root[0].order.orderFoods.length > 0){
					of.newFood = data.root[0].order.orderFoods;
				}else{
					of.newFood.length = 0;
				}
			}
		}
	});
	
	var tables = book.tables;
	//用于操作餐台点击
	ts.bookOperateTable.oldTables = book.tables; 
	
	//初始化已选餐台
	ts.bookChoosedTable.length = 0;
	ts.currentBookId = c.bookId;
	
	ts.loadBookChoosedTable({renderTo : 'bookTableToChoose', tables:tables, otype : 'commit'});
	
	ts.commitTableOrTran = 'bookTableChoose';
	
	$('#bookOperateChooseTable').show();
	$('#shadowForPopup').show();
};

/**
 * 关闭预订入座
 */
ts.closeBookOperateTable = function(){
	$('#bookOperateChooseTable').hide();
	$('#shadowForPopup').hide();
	$('#bookTableHadChoose').html('');
}; 

/**
 * 删除预订
 */
ts.deleteBook = function(c){
	Util.msg.alert({
		title : '提示',
		msg : '是否删除该预订?',
		buttons : 'YESBACK',
		renderTo : 'bookOrderListMgr',
		certainCallback : function(btn){
			if(btn == 'yes'){
				Util.LM.show();
				$.post('../OperateBook.do', {
					dataSource : 'delete',
					bookId : ts.bookList[c.index].id
				}, function(data){
					Util.LM.hide();
					Util.msg.tip(data.msg);
					ts.bookListEntry();
				}, 'json');
			}
		}
	});	
};

/**
 * 加载已选餐台
 */
ts.loadBookChoosedTable = function(c){
	var html = [];
	for (var i = 0; i < c.tables.length; i++) {
		var aliasOrName;
		if(c.tables[i].categoryValue == 1){//一般台
			aliasOrName = c.tables[i].alias;
		}else if(c.tables[i].categoryValue == 3){//搭台
			var begin = c.tables[i].name.indexOf("(");
			var end = c.tables[i].name.indexOf(")");
			aliasOrName = '<font color="green">' + c.tables[i].name.substring(begin+1, end) +'</font>';
		}else{
			aliasOrName = '<font color="green">'+ c.tables[i].categoryText +'</font>';
		}		
		html.push(tableCmpTemplet.format({
			dataIndex : i,
			id : c.tables[i].id,
			alias : aliasOrName,
			theme : c.tables[i].statusValue == '1' ? "e" : "c",
			name : c.tables[i].name == "" || typeof c.tables[i].name != 'string' ? c.tables[i].alias + "号桌" : c.tables[i].name,
			tempPayStatus : c.tables[i].isTempPaid? '暂结' : '&nbsp;&nbsp;',
			bookTableStatus : c.tables[i].isBook? '订' : '',
			tempPayStatusClass : navigator.userAgent.indexOf("Firefox") >= 0?'tempPayStatus4Moz':'tempPayStatus'		
		}));				
	}
	$('#'+c.renderTo).html(html.join("")).trigger('create');	
};

/**
 * 入座点菜
 */
ts.bookTableOrderFood = function(){

	if(ts.bookChoosedTable.length == 0){
		Util.msg.tip("请选择餐台");
		return;
	}
	
	setTimeout(function(){
		//关闭选台
		ts.closeBookOperateTable();				
	}, 500);

	//进入点菜界面
	of.entry({
		table : ts.bookChoosedTable[0],
		comment : '',
		orderFoodOperateType : 'bookSeat'
	});	
};

/**
 * 入座落单
 */
ts.bookTableCommitOrderFood = function(){
	
	if(of.newFood.length == 0){
		Util.msg.alert({
			msg : "请选择菜品",
			renderTo : 'orderFoodMgr'
		});		
		return ;
	}
	
	var bookOrderFoods = [];
	for (var i = 0; i < ts.bookChoosedTable.length; i++) {
		orderDataModel.tableID = ts.bookChoosedTable[i].id;
		orderDataModel.orderFoods = of.newFood.slice(0);
		orderDataModel.categoryValue =  ts.bookChoosedTable[i].categoryValue;	
		bookOrderFoods.push(JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)));
	}
	
	Util.LM.show();
	$.post('../OperateBook.do', {
		dataSource : 'seat',
		bookId : ts.currentBookId,
		bookOrderFoods : bookOrderFoods.join("<li>")
	}, function(data){
		Util.LM.hide();
		if(data.success){
			Util.msg.tip(data.msg);
			ts.bookListEntry();
		}else{
			Util.msg.alert({
				renderTo : 'orderFoodMgr',
				msg : data.msg
			});
		}
	}, 'json');
};


/**
 * 打开手动预订页面
 */
ts.addBookInfo = function(c){
	//时分插件
	$('#add_bookTimeBox').html('<input id="add_bookTime" class="bookTime" >').trigger('create');
	$(".bookTime").timepicki();	
	
	$('#add_bookDate').val(ts.bookListEntry.today);
	$('#add_bookMoney').val(0);
	if(c.type == "add"){
		ts.addBookInfo.otype = "insert";
		
		//清空菜品
		of.newFood.length = 0;
		//清空已选餐台
		ts.bookChoosedTable.length = 0;
		
		$('#title4AddBook').text("填写预订");
		$('#footer4AddBook').show();
	}else if(c.type == "look" || c.type == "confirm"){
		
		var book = ts.bookList[c.index];
		
		ts.addBookInfo.otype = "update";
		ts.addBookInfo.id = book.id;
		
		$.ajax({
			url : '../QueryBook.do',
			type : 'post',
			dataType : 'json',
			async : false,
			data :{
				dataSource : 'checkout',
				id : book.id
			},
			success : function(data){
				if(data.success){
					book = data.root[0];
				}
			}
		});
		
		if(c.type == "look" ){
			$('#footer4AddBook').hide();
			$('#title4AddBook').text("查看预订 -- "+ book.sourceDesc);
		}else{
			if(book.status == 3){
				Util.msg.tip("入座状态不能做确认操作");
				return;
			}
			$('#footer4AddBook').show();
			$('#title4AddBook').text("确认预订");
		}
		
		$('#add_bookDate').val(book.bookDate.substring(0, 11));
		$('#add_bookTime').val(book.bookDate.substring(11, 16));
		$('#add_bookPerson').val(book.member);
		$('#add_bookPhone').val(book.tele);
		$('#add_bookAmount').val(book.amount);
		$('#cm_bookCate').val(book.category).selectmenu("refresh");
		$('#cm_bookReserved').val(book.reserved);
		$('#add_staff').val(book.staffId != -1?book.staffId :"").selectmenu("refresh");;
		$('#add_bookMoney').val(book.money);
		
		if(book.tables.length > 0){
			ts.bookChoosedTable = book.tables;
			$('#box4BookTableList').show();
			ts.loadBookChoosedTable({renderTo : 'add_bookTableList', tables:ts.bookChoosedTable});
		}
		
		if(book.order && book.order.orderFoods.length > 0){
			var orderFoods = book.order.orderFoods;
			
			var html = [];
			for (var i = 0; i < orderFoods.length; i++) {
				html.push(bookOrderFoodListCmpTemplet.format({
					dataIndex : i + 1,
					id : orderFoods[i].id,
					name : orderFoods[i].foodName,
					count : orderFoods[i].count,
					isWeight : (orderFoods[i].status & 1 << 7) != 0 ? 'initial' : 'none',
					tastePref : orderFoods[i].tasteGroup.tastePref,
					unitPrice : orderFoods[i].unitPrice.toFixed(2) + (orderFoods[i].isGift?'&nbsp;[<font style="font-weight:bold;">已赠送</font>]':''),
					isComboFoodTd : "",
					foodNameStyle : "commonFoodName"
				}));
			}
			
			$('#bookOrderFoodListBody').html(html.join('')).trigger('create');
			
			//显示预订界面菜品显示
			$('#box4BookOrderFoodList').show();				
		}
	}
	
	
	$('#addBookInfo').show();
	$('#shadowForPopup').show();
};

/**
 * 关闭手动预订页面
 */
ts.closeAddBookInfo = function(){
	$('#addBookInfo').hide();
	$('#shadowForPopup').hide();
	//清空选台 & 显示
	ts.bookChoosedTable.length = 0;	
	$('#box4BookTableList').hide();
	$('#add_bookTableList').html('');	
	
	//清空菜品
	of.newFood.length = 0;
	$('#box4BookOrderFoodList').hide();
	$('#bookOrderFoodListBody').html('');
	
	//清空input
	$('#add_bookDate').val("");
	$('#add_bookTimeBox').html("");
	$('#add_bookPerson').val("");
	$('#add_bookPhone').val("");
	$('#add_bookAmount').val("");
//	$('#cm_bookCate').val();
	$('#cm_bookReserved').val("");
//	$('#add_staff').val();
	$('#add_bookMoney').val("");	
};

/**
 * 打开点菜页面
 */
ts.toOrderFoodPage = function(){
	//关闭shadow
	$('#shadowForPopup').hide();
	//进入点菜界面
	of.entry({
		table : ts.bookChoosedTable.length > 0 ? ts.bookChoosedTable[0] : {name : '预订台', alias: ''},
		comment : '',
		//不清空已点菜
		orderFoodOperateType : 'addBook'
	});	
};

/**
 * 预定菜完毕返回添加预订
 */
ts.bookFoodChooseFinish = function(){
	
	if(of.newFood.length == 0){
		Util.msg.alert({
			msg : "请选择菜品",
			renderTo : 'orderFoodMgr'
		});		
		return ;
	}
	
	//返回预订界面
	history.back();
	
	var html = [];
	for (var i = 0; i < of.newFood.length; i++) {
		html.push(bookOrderFoodListCmpTemplet.format({
			dataIndex : i + 1,
			id : of.newFood[i].id,
			name : of.newFood[i].name,
			count : of.newFood[i].count,
			isWeight : (of.newFood[i].status & 1 << 7) != 0 ? 'initial' : 'none',
			tastePref : of.newFood[i].tasteGroup.tastePref,
			unitPrice : of.newFood[i].unitPrice.toFixed(2) + (of.newFood[i].isGift?'&nbsp;[<font style="font-weight:bold;">已赠送</font>]':''),
			isComboFoodTd : "",
			foodNameStyle : "commonFoodName"
		}));
	}
	
	$('#bookOrderFoodListBody').html(html.join('')).trigger('create');
	
	//显示预订界面菜品显示
	$('#box4BookOrderFoodList').show();	
};	

/**
 * 添加预订数据提交
 */
ts.commitAddBook = function(){
	var bookDate = $('#add_bookDate').val();
	var time = $('#add_bookTime').val();
	var member = $('#add_bookPerson').val();
	var tele = $('#add_bookPhone').val();
	var amount = $('#add_bookAmount').val();
	var cate = $('#cm_bookCate').val();
	var reserved = $('#cm_bookReserved').val();
	var staff = $('#add_staff').val();
	var money = $('#add_bookMoney').val();
	

	if(!bookDate || !reserved || !member || !tele || !amount || !money){
		Util.msg.tip("请填写完整信息");
		return;
	}
	
	if(ts.bookChoosedTable.length == 0){
		Util.msg.tip("请选择餐台");
		return;		
	}
	
	if(time.indexOf('PM') > 0){
		var hourString = time.substring(0, time.indexOf(':'));
		var hour = parseInt(hourString)+12;
		var minute = time.substr(time.indexOf(':')+1, 2);
		time = hour + ":" + minute + ':' + "59";
	}else if(time.indexOf('AM') > 0){
		var hour = time.substring(0, time.indexOf(':'));
		var minute = time.substr(time.indexOf(':')+1, 2);	
		time = hour + ":" + minute + ':' + "59";
	}else{
		time += ":59";
	}
	
	var tables = [];
	for (var i = 0; i < ts.bookChoosedTable.length; i++) {
		tables.push(ts.bookChoosedTable[i].id);
	}
	
	var orderFoods = [];
	for (var i = 0; i < of.newFood.length; i++) {
		orderFoods.push(JSON.stringify(of.newFood[i]));
	}
	
	Util.LM.show();
	$.post('../OperateBook.do', {
		dataSource : ts.addBookInfo.otype,
		bookId : ts.addBookInfo.id,
		bookDate : bookDate + " " + time,
		member : member,
		tele : tele,
		amount : amount,
		cate : cate,
		reserved : reserved,
		staff : staff,
		money : money,
		comment : '',
		tables : tables.join("&"),
		orderFoods : orderFoods.join("&")
	}, function(data){
		Util.LM.hide();
		if(data.success){
			ts.addBookInfo.otype = '';
			delete ts.addBookInfo.id;
			Util.msg.tip(data.msg);
			ts.closeAddBookInfo();
			ts.searchBookList();
			
			ts.refreshWeixinBook();
		}
	});
};


/**
 * 已点菜界面查看预订
 */
ts.checkBookTable = function(){
	
	//去已点菜界面
	if($.mobile.activePage.attr( "id" ) == 'tableSelectMgr'){//餐台界面中使用
		ts.bookTable4Search = ts.table.id;
	}else if($.mobile.activePage.attr( "id" ) == 'orderFoodListMgr'){//已点菜界面使用
		ts.bookTable4Search = uo.table.id;
		$('#updateFoodOtherOperateCmp').popup('close');
	}	
	ts.bookListEntry();
};

//==============end 预订

//===============并台start
/**
 * 打开多台开席
 */
ts.openMultiPayTableCmp = function(){
	//初始化选择餐台
	ts.multiPayTableChoosedTable.length = 0;
	//关闭更多
	$('#tableSelectOtherOperateCmp').popup('close');
	setTimeout(function(){
		$('#multiPayTableCmp').show();
		$('#shadowForPopup').show();
	}, 250);
};

/**
 * 关闭预订入座
 */
ts.closeMultiPayTableCmp = function(){
	$('#multiPayTableCmp').hide();
	$('#shadowForPopup').hide();
	$('#multiPayTableHadChoose').html('');
};

/**
 * 设置多台开席入座选台
 */
ts.openMultiPayTable = function(){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	
	ts.commitTableOrTran = 'multiPayTableChoose';
	
	$("#txtTableNumForTS").val("");
	$("#txtTableComment").val("");
	
	$('#transSomethingTitle').html("请输入桌号，确认添加");
	
	//打开控件
	uo.openTransOrderFood();		
};

/**
 * 多台并台
 */
ts.multiPayTableOrderFood = function(){

	if(ts.multiPayTableChoosedTable.length == 0){
		Util.msg.tip("请选择餐台");
		return;
	}
	var multiTables = [];
	for (var i = 0; i < ts.multiPayTableChoosedTable.length; i++) {
		multiTables.push(ts.multiPayTableChoosedTable[i].id);
	}

	$.post('../OperateTable.do', {
		dataSource : 'mergeTable',
		tables : multiTables.join(",")
	}, function(rt){
		if(rt.success){
			Util.msg.tip("并台成功, 已合并到 "+ ts.multiPayTableChoosedTable[0].name);
			//关闭选台
			ts.closeMultiPayTableCmp();		
			//进入已点菜界面
			uo.entry({
				table : ts.multiPayTableChoosedTable[0]
			});
		}else{
			Util.msg.alert({
				renderTo : 'tableSelectMgr',
				msg : rt.msg
			});
		}
	}, 'json');
};


//===============end并台

//=================多台开席start

/**
 * 打开多台开席
 */
ts.openMultiOpenTableCmp = function(){
	//初始化选择餐台
	ts.multiOpenTableChoosedTable.length = 0;
	//关闭更多
	$('#tableSelectOtherOperateCmp').popup('close');
	setTimeout(function(){
		$('#multiOpenTableCmp').show();
		$('#shadowForPopup').show();
	}, 250);
};

/**
 * 关闭预订入座
 */
ts.closeMultiOpenTableCmp = function(){
	$('#multiOpenTableCmp').hide();
	$('#shadowForPopup').hide();
	$('#multiOpenTableHadChoose').html('');
};

/**
 * 多台点菜
 */
ts.multiOpenTableOrderFood = function(){
	if(ts.multiOpenTableChoosedTable.length == 0){
		Util.msg.tip("请选择餐台");
		return;
	}
	
	setTimeout(function(){
		//关闭选台
		ts.closeMultiOpenTableCmp();				
	}, 500);

	//进入点菜界面
	of.entry({
		table : ts.multiOpenTableChoosedTable[0],
		comment : '',
		orderFoodOperateType : 'multiOpenTable'
	});	
};

/**
 * 多台开席落单
 */
ts.multiOpenTableCommitOrderFood = function(){
	
	if(of.newFood.length == 0){
		Util.msg.alert({
			msg : "请选择菜品",
			renderTo : 'orderFoodMgr'
		});		
		return ;
	}
	
	var orderFoods = [];
	for (var i = 0; i < ts.multiOpenTableChoosedTable.length; i++) {
		orderDataModel.tableID = ts.multiOpenTableChoosedTable[i].id;
		orderDataModel.orderFoods = of.newFood.slice(0);
		orderDataModel.categoryValue =  ts.multiOpenTableChoosedTable[i].categoryValue;	
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
			ts.multiOpenTableChoosedTable.length = 0;
			ts.loadData();
		}else{
			Util.msg.alert({
				renderTo : 'orderFoodMgr',
				msg : data.msg
			});
		}
	}, 'json');
};

//===============end 多台开席














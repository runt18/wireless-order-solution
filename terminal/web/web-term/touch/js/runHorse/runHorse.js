function CreateRunHorse(param){
	
	param = param || {
		//要显示的样式 	
		css : {
			'top' : '12px',
			'left' : '20%',
			'z-index' : '10000',
			'position' : 'absolute',
			'cursor' : 'pointer'
		},            
		size : 20            //消息中心最大显示多少条信息
	}
	
	//socket
	var _messageSocket = null;
	//跑马灯载体
	var _container = null;
	//socket接受的新消息
	var _activeMsg = null;	//activeMsg
	//信息存储器
	var _messages = [];		//messages
	//消息中心载体
	var _displayPanel = null;
	//语音播报对象
	var _audio = null;
	//判断控件是否隐藏
	var isHide = false;
	
	var _self = this;
	
	//跑马灯初始化
	function init(){
		var restaurantId = null;
		//获取店铺ID
		$.ajax({
			url : '../OperateRestaurant.do',
			type : 'post',
			datatype : 'json',
			data : {
				dataSource : 'getByCond',
				byId : true
			},
			success : function(data, status, xhr){
				restaurantId = data.root[0].id;
				//建立连接
				var hostname = null;
				var port = null
				hostname = window.location.hostname;
				port = window.location.port; 
				if(hostname === 'e-tones.net' || hostname === 'lb.e-tones.net'){
					hostname = 'wx.e-tones.net';
				}
				_messageSocket = new ReconnectingWebSocket('ws://' + hostname + ':' + port + '/wx-term/ws/wx-waiter/' + restaurantId, null, {debug : false});  
		  
				//webSocket的onmessage事件
				_messageSocket.onmessage = function(e){
					_activeMsg = JSON.parse(e.data);
					
					if(_activeMsg){
					
					//保存数据入数组
						if(_messages.length >= param.size){
							_messages.push(_activeMsg);
							_messages.shift();
							
						}else{
							_messages.push(_activeMsg);
						}
						
						//展示跑马灯及效果
						if(_container){
							$(_container).remove();
						}
						showActiveMsg();
						
						if(isHide){
							$(_container).css('display', 'none');
						}else{
							$(_container).css('display', 'block');
						}
						
						//语音播报
						play(_activeMsg.content);
					}
					
					//将得到的信息保存在sessionStorage
					if(window.sessionStorage){
						sessionStorage.setItem('clientOrderMsgs', JSON.stringify(_messages));
					}
					
					//实时更新消息中心数据
					msgManagerShower();
					
					
					$(_container).select(function(){
						_self.close();
					});
				}
			}
		});
		
	}
	
	//渲染跑马灯样式及提示新的信息
	function showActiveMsg(){
		//建立载体
		var container = $('<div/>');
		container.addClass('box-horizontal');
		container.css(param.css);
		
		container.attr('id', 'runHorse' + new Date().getTime());
		container.attr('data-type', 'runHorse');
		_container = container;
		
		//展示形式
		var showWord = null;
		
		if(_activeMsg){
			showWord = _activeMsg.content;
			
		}else{
			showWord = '';
		}
		//展示端
		var runHorseShower = $('<marquee loop="5" width="380" style="color:yellow;font-size:22px;font-family:"微软雅黑";">'+ showWord +'</marquee>');
		
		runHorseShower.attr('id', 'shower' + new Date().getTime());
		
		//消息中心提示
		var tips = $('<span style="display:inline-block;color:wheat;border:1px solid wheat;font-size:18px;text-shadow:0px 0px 40px;position:relative;top:-7px;">共(<span data-type="showMsgCount">0</span>)条消息</span>');
		
		container.append(runHorseShower).append(tips);
		
		container.find('[data-type=showMsgCount]').text(_messages.length);
		
		$('body').append(container);
		
		//开关消息中心
		container.click(function(){
			msgManagerShower().slideToggle();
			setTimeout(function(){
				runHorseShower.html('');
			}, 2000);
		});
	}
	
	
	//返回消息中心dom元素及消息中心样式
	function msgManagerShower(){
		//初始化消息中心的样式
		if(!_displayPanel ){
			var msgManagerContainer = $('<div>');
			msgManagerContainer.attr('id', 'msgManager' + new Date().getTime());
			msgManagerContainer.css({
				'display': 'none',
				'top' : '50px',
				'left' : '25%',
				'width' : '450px',
				'height' : '400px',
				'background' : '#fff',
				'overflow-y' : 'scroll',
				'border' : '3px soild #000',
				'position' : 'absolute',
				'box-shadow' : '4px 4px 21px #000',
				'z-index' : '2'
			});
			_displayPanel = msgManagerContainer;
			//消息显示栏
			var table = $('<table style="width:100%;"><thead class="ui-bar-d"><tr><td style="width:18%;height:36px;">时间</td><td style="width:64%;height:36px;">内容</td><td style="width:18%;height:36px;">类型</td></tr></thead><tbody></tbody></table>');
			var closeMsgShower = $('<span style="display:block;width:28px;height:28px;line-height:28px;text-align:center;background:red;color:#fff;position:absolute;border-radius:14px;top:0;right:0;font-weight:normal;cursor:pointer;">X</span>');
			msgManagerContainer.append(table);
			msgManagerContainer.append(closeMsgShower);
			//关闭消息中心按钮设置
			closeMsgShower.click(function(){
				msgManagerContainer.slideUp();
			});
			$('body').append(msgManagerContainer);
		}
		
		
		if(_messages.length != 0 ){
			//消息中心内容填写
//			$('#' + _displayPanelId).html('');
			$(_displayPanel).find('tbody').html('');
			_messages.forEach(function(element, index){
				var showWord = element.typeText + ':' + element.content + ',' + element.hour;
				if(index == (_messages.length - 1)){
					$(_displayPanel).find('tbody').prepend('<tr style="color:red;"><td style="height:36px;border:1px solid #666;">' + element.hour + '</td><td style="height:36px;border:1px solid #666;">' + element.content + '</td><td style="height:36px;border:1px solid #666;">' + element.typeText + '</td></tr>');
//					$('#' + _displayPanelId).prepend('<p style="border-bottom:1px solid #ccc;width:95%;text-align:center;color:red;">' + showWord + '</p>');
				}else{
					$(_displayPanel).find('tbody').prepend('<tr><td style="height:36px;border:1px solid #666;">' + element.hour + '</td><td style="height:36px;border:1px solid #666;">' + element.content + '</td><td style="height:36px;border:1px solid #666;">' + element.typeText + '</td></tr>');
//					$('#' + _displayPanelId).prepend('<p style="border-bottom:1px solid #ccc;width:95%;text-align:center;">' + showWord + '</p>');
				}
			});		
		}
		
		return $(_displayPanel);
	}
	
	
	//语音播报
	var session = null;
//	$.getScript('http://blog.faultylabs.com/files/md5.js').done(function(){
//		$.getScript('http://webapi.openspeech.cn/socket.io/socket.io.js').done(function(){
//			$.getScript('http://webapi.openspeech.cn/fingerprint.js').done(function(){
//				$.getScript('http://webapi.openspeech.cn/tts.min.js').done(function(){
//					session = new IFlyTtsSession({
//				        'url' : 'http://webapi.openspeech.cn/',							
//				        'interval' : '30000', 
//					      'disconnect_hint' : 'disconnect',
//					      'sub' : 'tts'
//					});
//				});
//			});
//		});
//	});
	$.getScript('http://blog.faultylabs.com/files/md5.js', function(){
		$.getScript('http://webapi.openspeech.cn/socket.io/socket.io.js', function(){
			$.getScript('http://webapi.openspeech.cn/fingerprint.js', function(){
				$.getScript('http://webapi.openspeech.cn/tts.min.js', function(){
					session = new IFlyTtsSession({
				        'url' : 'http://webapi.openspeech.cn/',							
				        'interval' : '30000', 
					      'disconnect_hint' : 'disconnect',
					      'sub' : 'tts'
					});
				});
			});
		});
	});
	//语音播报函数
	function play(content) {
		if(session){
			var appid = "568f230c";                             
			var timestamp = new Date().toLocaleTimeString();                     
			var expires = 60000;                          	
			
			var signature = faultylabs.MD5(appid + '&' + timestamp + '&' + expires + '&' + "3444ec156adf96e8");		
			
			var params = { "params" : "vcn = vixy, aue = speex-wb;7, ent = intp65, spd = 50, vol = 50, tte = utf8, caller.appid=" + appid + ",timestamp=" + timestamp + ",expires=" + expires, "signature" : signature, "gat" : "mp3"};	
			session.start(params, content, function (err, obj){
				if(err) {
					alert("语音合成发生错误，错误代码 ：" + err);
				} else {
					if(_audio != null){
						_audio.pause();
					}
					_audio = new Audio();
					_audio.src = '';
					_audio.play();
					_audio.src = "http://webapi.openspeech.cn/" + obj.audio_url;
					_audio.play();
				}
			});
		}else{
			//TODO
			console.log('语音播报文件还没导入');
		}
	};
	
	
	this.open = function(afterOpen){
		
		//用于刷新后拿去本地数据
		if(sessionStorage.getItem('clientOrderMsgs')){
			_messages = JSON.parse(sessionStorage.getItem('clientOrderMsgs'));
			showActiveMsg();
		}
		
		init();
	}	
	
	this.close = function(afterClose, timeout){
		
		if(_container){
			$(_container).remove();
		}
		if(_displayPanel){
			$(_displayPanel).remove();
			_displayPanel = null;
		}
		_messageSocket.close();
	}

	this.hide = function(){
		$(_container).hide();
		$(_displayPanel).hide();
		isHide = true;
	}
	
	this.show = function(){
		$(_container).show();
		isHide = false;
	}
}

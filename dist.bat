@Rem copy the files to make the distribution folder for digi-e

@echo off

Rem delete the old distribution folder before releasing
IF EXIST dist rmdir /s/q dist > nul

Rem copy the db files
IF EXIST db\scripts GOTO db_exist
IF NOT EXIST db goto db_file
:db_exist
	@echo copying the db script files...
	@xcopy /s/y db\scripts dist\db\scripts\ > nul

	@copy db\releasenote.txt dist\db\ > nul

	GOTO www_files

db_not_exist:
	@echo the db files missing
	@pause
	GOTO end


Rem copy the www files
:www_files
IF EXIST www GOTO www_exist
IF NOT EXIST www GOTO www_not_exist
:www_exist
	@echo copying the index.html...
	@xcopy /s/y www\index.html dist\www\ > nul
	
	@echo copying the favicon...
	@xcopy /s/y www\images\favicon.ico dist\www\ > nul

	@echo copying the login.php...
	@xcopy /s/y www\login.php dist\www\digi-e\ > nul
	
	GOTO web_term_files

:www_not_exist
	@echo the www files missing
	@pause
	GOTO end

Rem copy the web-term files
:web_term_files
IF EXIST terminal\web\target\web-term GOTO web_term_exist
IF NOT EXIST terminal\web\target\web-term GOTO web_term_not_exist
:web_term_exist
	@echo copying the web-term files...
	@xcopy /s/y terminal\web\target\web-term dist\www\web-term\ > nul

	GOTO wx_term_files

:web_term_not_exist
	@echo the web term files missing
	@pause
	GOTO end

Rem copy the wx-term files
:wx_term_files
IF EXIST terminal\weixin\target\wx-term GOTO wx_term_exist
IF NOT EXIST terminal\weixin\target\wx-term GOTO wx_term_not_exist
:wx_term_exist
	@echo copying the wx-term files...
	@xcopy /s/y terminal\weixin\target\wx-term dist\www\wx-term\ > nul

	GOTO android_apk

:wx_term_not_exist
	@echo the wx term files missing
	@pause
	GOTO end
	
Rem copy the AndroidManifest.xml and apk file
:android_apk
IF EXIST terminal\android\phone\bin\WirelessOrderTerminal_Android.apk GOTO apk_exist
IF NOT EXIST terminal\android\phone\bin\WirelessOrderTerminal_Android.apk GOTO apk_not_exist
:apk_exist
	@echo copying the android apk files...
	IF NOT EXIST dist\www\ota\android\phone mkdir dist\www\ota\android\phone
	@copy terminal\android\phone\version.php dist\www\ota\android\phone > nul
	@copy terminal\android\phone\AndroidManifest.xml dist\www\ota\android\phone > nul
	@copy terminal\android\phone\bin\WirelessOrderTerminal_Android.apk dist\www\ota\android\phone > nul
	GOTO android_eMenu_apk
:apk_not_exist
	@echo the android phone apk file missing
	@pause
	GOTO end
	
Rem copy the AndroidManifest.xml and apk file
:android_pad_apk
IF EXIST terminal\android\pad\bin\WirelessOrderTerminal_Pad.apk GOTO pad_apk_exist
IF NOT EXIST terminal\android\pad\bin\WirelessOrderTerminal_Pad.apk GOTO pad_apk_not_exist
:pad_apk_exist
	@echo copying the android pad apk files...
	IF NOT EXIST dist\www\ota\android\pad mkdir dist\www\ota\android\pad
	@copy terminal\android\pad\version.php dist\www\ota\android\pad > nul
	@copy terminal\android\pad\AndroidManifest.xml dist\www\ota\android\pad > nul
	@copy terminal\android\pad\bin\WirelessOrderTerminal_Pad.apk dist\www\ota\android\pad > nul
	GOTO android_eMenu_apk
:pad_apk_not_exist
	@echo the android pad apk file is missing
	@pause
	GOTO end
	
Rem copy the AndroidManifest.xml and apk file
:android_eMenu_apk
IF EXIST terminal\android\eMenu\bin\WirelessOrderMenu_Pad.apk GOTO eMenu_apk_exist
IF NOT EXIST terminal\android\eMenu\bin\WirelessOrderMenu_Pad.apk GOTO eMenu_apk_not_exist
:eMenu_apk_exist
	@echo copying the android eMenu apk files...
	IF NOT EXIST dist\www\ota\android\eMenu mkdir dist\www\ota\android\eMenu
	@copy terminal\android\eMenu\version.php dist\www\ota\android\eMenu > nul
	@copy terminal\android\eMenu\AndroidManifest.xml dist\www\ota\android\eMenu > nul
	@copy terminal\android\eMenu\bin\WirelessOrderMenu_Pad.apk dist\www\ota\android\eMenu > nul
	GOTO pserver_files
:eMenu_apk_not_exist
	@echo the android eMenu apk file is missing
	@pause
	GOTO end	
	
:pserver_files
IF EXIST pserver\setup_nsis\pserver.exe GOTO pserver_exist
IF NOT EXIST pserver\setup_nsis\pserver.exe GOTO pserver_not_exist
:pserver_exist
	@echo copying the pserver setup program...
	IF NOT EXIST dist\www\pserver mkdir dist\www\pserver
	@copy pserver\setup_nsis\pserver.exe dist\www\pserver\ > nul
	@copy pserver\setup_nsis\version.php dist\www\pserver\ > nul
	@copy pserver\releasenote.txt dist\www\pserver\ > nul
	@echo copying the pserver help files...
	@copy pserver\setup_nsis\help.html dist\www\pserver\ > nul
	IF NOT EXIST dist\www\pserver\images mkdir dist\www\pserver\images
	@copy pserver\setup_nsis\images\*.* dist\www\pserver\images\ > nul
	GOTO socket_jar

:pserver_not_exist
	@echo the pserver setup program missing
	@pause
	GOTO end

:socket_jar
IF EXIST server\target\wireless_order_socket.jar GOTO socket_exist
IF NOT EXIST server\target\wireless_order_socket.jar GOTO socket_not_exist
:socket_exist
	@echo copying the wireless order socket jar file...
	IF NOT EXIST dist\socket mkdir dist\socket
	@copy server\target\wireless_order_socket.jar dist\socket > nul
	@copy server\conf\conf.xml dist\socket > nul
	@copy server\releasenote.txt dist\socket > nul
	IF NOT EXIST dist\socket\lib mkdir dist\socket\lib
	@copy server\target\lib dist\socket\lib > nul
	GOTO socket_scripts

:socket_not_exist
	@echo the wireless order socket missing
	@pause
	GOTO end

:socket_scripts
IF EXIST server\scripts GOTO sscripts_exist
IF NOT EXIST server\scripts GOTO sscripts_not_exist
:sscripts_exist
	@echo copying the socket scripts...
	IF NOT EXIST dist\socket\scripts mkdir dist\socket\scripts
	@copy server\scripts\*.* dist\socket\scripts > nul
	GOTO ptemp_files

:sscripts_not_exist
	@echo the socket scripts missing
	@pause
	GOTO end

:ptemp_files
IF EXIST server\ptemp GOTO ptemp_exist
IF NOT EXIST server\ptemp GOTO ptemp_not_exist
:ptemp_exist
	@echo copying the socket print templates...
	IF NOT EXIST dist\socket\ptemp mkdir dist\socket\ptemp
	@copy server\ptemp\*.* dist\socket\ptemp > nul
	GOTO end

:ptemp_not_exist
	@echo the socket print templates missing
	@pause
	GOTO end

:end

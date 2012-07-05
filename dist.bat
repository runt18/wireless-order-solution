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
	@echo copying the index.php...
	@xcopy /s/y www\index.php dist\www\ > nul
	
	@echo copying the favicon...
	@xcopy /s/y www\images\favicon.ico dist\www\ > nul

	@echo copying the www files...
	@xcopy /s/y www dist\www\digi-e\ > nul
	@copy www\releasenote.txt dist\www\digi-e\ > nul
	@del dist\www\digi-e\index.php > nul

	GOTO web_term_files

:www_not_exist
	@echo the www files missing
	@pause
	GOTO end

Rem copy the web-term files
:web_term_files
IF EXIST terminal\web\WirelessOrderWeb GOTO web_term_exist
IF NOT EXIST terminal\web\WirelessOrderWeb GOTO web_term_not_exist
:web_term_exist
	@echo copying the web-term files...
	@xcopy /s/y terminal\web\WirelessOrderWeb dist\www\web-term\ > nul

	GOTO cod_45_files

:web_term_not_exist
	@echo the web term files missing
	@pause
	GOTO end

Rem copy the bb45 terminal cod files
:cod_45_files
IF EXIST terminal\blackberry\bb45\deliverables\Web\4.5.0\WirelessOrderTerminal.cod GOTO cod_45_exist
IF NOT EXIST terminal\blackberry\deliverables\Web\4.5.0\WirelessOrderTerminal.cod GOTO cod_45_not_exist
:cod_45_exist
	@echo copying the terminal cod for blackberry 4.5 files...
	IF NOT EXIST dist\www\ota\bb45 mkdir dist\www\ota\bb45
	@copy terminal\blackberry\bb45\deliverables\Web\4.5.0\WirelessOrderTerminal*.cod dist\www\ota\bb45 > nul
	@copy terminal\blackberry\bb45\deliverables\Web\4.5.0\WirelessOrderTerminal.jad dist\www\ota\bb45 > nul
	@copy terminal\blackberry\bb45\deliverables\Web\4.5.0\WirelessOrderTerminal_BB45.jad dist\www\ota\bb45 > nul
	@copy terminal\blackberry\bb45\deliverables\Web\4.5.0\WirelessOrderTerminal.jar dist\www\ota\bb45 > nul
	@copy terminal\blackberry\bb45\deliverables\Web\4.5.0\WirelessOrderTerminal_BB45.jar dist\www\ota\bb45 > nul
	@copy terminal\blackberry\version.php dist\www\ota\bb45 > nul
	call terminal\blackberry\replace.bat .\dist\www\ota\bb45\version.php ("WirelessOrderTerminal.jad") ("WirelessOrderTerminal_BB45.jad")
	@copy terminal\blackberry\releasenote.txt dist\www\ota\bb45 > nul
	set pwd=%CD%
	cd %CD%\dist\www\ota\bb45
	%pwd%\terminal\blackberry\UpdateJad -n WirelessOrderTerminal_BB45.jad WirelessOrderTerminal.jad 
	cd %pwd%
	del dist\www\ota\bb45\WirelessOrderTerminal.jad > nul
	GOTO bb45_help

:cod_45_not_exist
	@echo the terminal cod for blackberry 4.5 file missing
	@pause
	GOTO end

Rem copy the terminal help files
:bb45_help
IF EXIST terminal\blackberry\bb45\help GOTO bb45_help_exist
IF NOT EXIST terminal\blackberry\bb45\help GOTO bb45_help_not_exist
:bb45_help_exist
	@echo copying the bb45 help files...
	@xcopy /s/y terminal\blackberry\bb45\help dist\www\help\bb45\ > nul
	GOTO cod_50_files

bb45_help_not_exist:
	@echo the terminal help files missing
	@pause
	GOTO end

Rem copy the bb50 terminal cod files
:cod_50_files
IF EXIST terminal\blackberry\bb45\deliverables\Web\4.5.0\WirelessOrderTerminal.cod GOTO cod_50_exist
IF NOT EXIST terminal\blackberry\deliverables\Web\4.5.0\WirelessOrderTerminal.cod GOTO cod_50_not_exist
:cod_50_exist
	@echo copying the terminal cod for blackberry 5.0 files...
	IF NOT EXIST dist\www\ota\bb50 mkdir dist\www\ota\bb50
	@copy terminal\blackberry\bb50\deliverables\Web\4.5.0\WirelessOrderTerminal*.cod dist\www\ota\bb50 > nul
	@copy terminal\blackberry\bb50\deliverables\Web\4.5.0\WirelessOrderTerminal.jar dist\www\ota\bb50 > nul
	@copy terminal\blackberry\bb50\deliverables\Web\4.5.0\WirelessOrderTerminal.jad dist\www\ota\bb50 > nul
	@copy terminal\blackberry\bb50\deliverables\Web\5.0.0\WirelessOrderTerminal*.cod dist\www\ota\bb50 > nul
	@copy terminal\blackberry\bb50\deliverables\Web\5.0.0\WirelessOrderTerminal_BB50.jad dist\www\ota\bb50 > nul
	@copy terminal\blackberry\bb50\deliverables\Web\5.0.0\WirelessOrderTerminal_BB50.jar dist\www\ota\bb50 > nul
	@copy terminal\blackberry\version.php dist\www\ota\bb50 > nul
	call terminal\blackberry\replace.bat .\dist\www\ota\bb50\version.php ("WirelessOrderTerminal.jad") ("WirelessOrderTerminal_BB50.jad")
	@copy terminal\blackberry\releasenote.txt dist\www\ota\bb50 > nul
	set pwd=%CD%
	cd %CD%\dist\www\ota\bb50
	%pwd%\terminal\blackberry\UpdateJad -n WirelessOrderTerminal_BB50.jad WirelessOrderTerminal.jad 
	cd %pwd%
	del dist\www\ota\bb50\WirelessOrderTerminal.jad > nul
	GOTO android_apk

:cod_50_not_exist
	@echo the terminal cod for blackberry 5.0 file missing
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
	GOTO android_pad_apk
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
	GOTO pserver_files
:pad_apk_not_exist
	@echo the android pad apk file is missing
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
IF EXIST server\deliverables\wireless_order_socket.jar GOTO socket_exist
IF NOT EXIST server\deliverables\wireless_order_socket.jar GOTO socket_not_exist
:socket_exist
	@echo copying the wireless order socket jar file...
	IF NOT EXIST dist\socket mkdir dist\socket
	@copy server\deliverables\wireless_order_socket.jar dist\socket > nul
	@copy server\conf\conf.xml dist\socket > nul
	@copy server\releasenote.txt dist\socket > nul
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

@echo off 
setlocal enabledelayedexpansion 

rem 请输入要操作的文件名称(包括扩展名)：
set file=%1 
 
set "file=%file:"=%" 
for %%i in ("%file%") do set file=%%~fi 
echo. 

rem 请输入即将被替换的内容：
set replaced=%2 
 
echo. 

rem 请输入替换字符串：
set all=%3 
 
for /f "delims=" %%i in ('type "%file%"') do ( 
set str=%%i 
rem echo %%i
set "str=!str:%replaced%=%all%!"
rem echo !str!
echo !str!>>"%file%"_tmp.txt 
) 
rem copy "%file%" "%file%"_bak.txt >nul 2>nul 
move "%file%"_tmp.txt "%file%" 

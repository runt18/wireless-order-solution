@echo off 
setlocal enabledelayedexpansion 

rem ������Ҫ�������ļ�����(������չ��)��
set file=%1 
 
set "file=%file:"=%" 
for %%i in ("%file%") do set file=%%~fi 
echo. 

rem �����뼴�����滻�����ݣ�
set replaced=%2 
 
echo. 

rem �������滻�ַ�����
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

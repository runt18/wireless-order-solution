; Script generated by the HM NIS Edit Script Wizard.

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "e点通打印服务"
!define PRODUCT_VERSION "1.0.4"
!define PRODUCT_PUBLISHER "广州智易科技有限公司"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\gui.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON"${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

; Language Selection Dialog Settings
!define MUI_LANGDLL_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_LANGDLL_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "NSIS:Language"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
;!insertmacro MUI_PAGE_LICENSE "..\..\..\path\to\licence\YourSoftwareLicence.txt"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
;!define MUI_FINISHPAGE_RUN "$INSTDIR\gui.exe"
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION RunOnStartUp
!define MUI_FINISHPAGE_RUN_TEXT "设置开机启动"
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "SimpChinese"

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "pserver.exe"
InstallDir "$PROGRAMFILES\Digi-e\e点通打印服务"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show
BrandingText " "

Function .onInit
  ReadRegStr $R0 ${PRODUCT_UNINST_ROOT_KEY} \
  "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" \
  "UninstallString"
  StrCmp $R0 "" done

  ;Run the uninstaller silently
  ClearErrors
  ;Call "uninst/s" to uninstall the previous version silently if using "pserver/S"
  IfSilent +1 +3
    ExecWait '"$R0"/s _?=$INSTDIR'
    Goto done

  ;Prompt the user if using "pserver"
  ReadRegStr $R1 ${PRODUCT_UNINST_ROOT_KEY} \
  "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}" \
  "DisplayName"
  MessageBox MB_ICONINFORMATION|MB_OK "检测到你的电脑已安装$R1，安装向导将会自动卸载原有版本。"
  ;and then call "uninst" to uninstall the pervious version normally
  ExecWait '$R0 _?=$INSTDIR'
  
  IfErrors no_remove_uninstaller done
    ;You can either use Delete /REBOOTOK in the uninstaller or add some code
    ;here to remove the uninstaller. Use a registry key to check
    ;whether the user has chosen to uninstall. If you are using an uninstaller
    ;components page, make sure all sections are uninstalled.

  no_remove_uninstaller:

done:
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Section "MainSection" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File "..\gui\out\rel\gui.exe"
  CreateDirectory "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}"
  CreateShortCut "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}\e点通打印服务.lnk" "$INSTDIR\gui.exe"
  File "..\gui\out\rel\protocol.dll"
  File "..\gui\out\rel\pserver.dll"
  File "..\releasenote.txt"
  File "uninst.ico"
  File "rn.ico"
  CreateShortCut "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}\版本历史.lnk" "$INSTDIR\releasenote.txt" "" "$INSTDIR\rn.ico"
SectionEnd

Section -AdditionalIcons
  CreateShortCut "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}\卸载.lnk" "$INSTDIR\uninst.exe" "" "$INSTDIR\uninst.ico"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "${PRODUCT_DIR_REGKEY}" "" "$INSTDIR\gui.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\gui.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
  IfSilent +1 +2
    Call RunOnStartUp
  IfSilent +1 +2
    Exec "$INSTDIR\gui.exe"

SectionEnd


Function un.onUninstSuccess
  Call un.GetParameters
  Pop $R0
  StrCmp $R0 '/s' done do_unist
  do_unist:
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) 已成功地从你的计算机移除。"
  done:
FunctionEnd

Function un.onInit
  !insertmacro MUI_UNGETLANGUAGE
  Call un.GetParameters
  Pop $R0
  StrCmp $R0 '/s' done do_unist
  ;not prompt the msg if uninstalling silently
  do_unist:
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "你确实要完全移除 $(^Name) ，其及所有的组件？" IDYES +2
  Abort
  done:
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\releasenote.txt"
  Delete "$INSTDIR\pserver.dll"
  Delete "$INSTDIR\protocol.dll"
  Delete "$INSTDIR\gui.exe"
  Delete "$INSTDIR\uninst.ico"
  Delete "$INSTDIR\rn.ico"

  Delete "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}\卸载.lnk"
  Delete "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}\版本历史.lnk"
  Delete "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}\e点通打印服务.lnk"

  RMDir "$SMPROGRAMS\e点通打印服务 ${PRODUCT_VERSION}"
  RMDir "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  DeleteRegValue HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Run\" "PServer"
  SetAutoClose true
SectionEnd

;Set the pserver to run on start up
Function RunOnStartUp
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Run" "PServer" "$INSTDIR\gui.exe"
FunctionEnd

 ; GetParameters
 ; input, none
 ; output, top of stack (replaces, with e.g. whatever)
 ; modifies no other variables.

Function un.GetParameters

  Push $R0
  Push $R1
  Push $R2
  Push $R3

  StrCpy $R2 1
  StrLen $R3 $CMDLINE

  ;Check for quote or space
  StrCpy $R0 $CMDLINE $R2
  StrCmp $R0 '"' 0 +3
    StrCpy $R1 '"'
    Goto loop
  StrCpy $R1 " "

  loop:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 $R1 get
    StrCmp $R2 $R3 get
    Goto loop

  get:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 " " get
    StrCpy $R0 $CMDLINE "" $R2

  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0

FunctionEnd
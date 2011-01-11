DIM objShell
set objShell=wscript.createObject("wscript.shell")
uninstCmd="cmd.exe /c msiexec /x " & WScript.Arguments(0) & " /qr"
iReturn=objShell.Run(uninstCmd, 0, TRUE)


Displays a message box with buttons `buttons` containing the text `message`. 

*   __buttons__: One of the following `OK`, `OKCANCEL`, `ABORTRETRYIGNORE`, `RETRYCANCEL`, `YESNO`, or `YESNOCANCEL`
*   __message__: Message text
*   __options__: `|` separated list of zero or more options: `ICONEXCLAMATION`, `ICONINFORMATION`, `ICONQUESTION`, `ICONSTOP`, `USERICON`, `TOPMOST`, `SETFOREGROUND`, `RIGHT`, `RTLREADING`, `DEFBUTTON1`, `DEFBUTTON2`, `DEFBUTTON3`, and `DEFBUTTON4`. Refer to the NSIS MessageBox instruction documentation for details.
*   __sd__: Silent installer default return. Use empty string (or simply omit the argument) if message box is shown in silent install.

Function will return name of the button user selected: `OK`, `CANCEL`, `ABORT`, `RETRY`, `IGNORE`, `YES`, or `NO`.

    If MsgBox("YESNO", "Are you sure?") == "YES"
        ...

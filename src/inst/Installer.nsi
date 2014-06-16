################
# Global Defines
################

!ifndef BRIDLE_VERSION
    !error "BRIDLE_VERSION not defined"
!endif

!ifndef BRIDLE_HOME
    !error "BRIDLE_HOME not defined"
!endif

!define PRODUCT_NAME        "BridleNSIS"
!define PRODUCT_REG_PATH    "Software\${PRODUCT_NAME}"
!define VERSION_REG_PATH    "${PRODUCT_REG_PATH}\${BRIDLE_VERSION}"
!define WIN_UNINST_REG_PATH "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}-${BRIDLE_VERSION}"
!define UNINSTALLER_EXE     "Uninstall.exe"

##################
# Global Variables
##################

Var replace_instdir ; Will contain instdir of Bridle's current version if found
Var replace_version ; Will contain version of Bridle in current instdir if found

###############
# Include Files
###############

!include "LogicLib.nsh"
!include "Gui.nsh"

####################
# Installer Settings
####################

Name                  "${PRODUCT_NAME} v${BRIDLE_VERSION}"
OutFile               "${BRIDLE_HOME}\${PRODUCT_NAME}-${BRIDLE_VERSION}.exe"
InstallDir            "$PROGRAMFILES\${PRODUCT_NAME}"
InstallDirRegKey      HKLM "${VERSION_REG_PATH}" "InstallDir"
RequestExecutionLevel admin
ShowInstDetails       show

VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName"     "${PRODUCT_NAME}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright"  "Copyright © 2014 Henri Kor"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "${PRODUCT_NAME} Setup"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion"     "${BRIDLE_VERSION}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductVersion"  "${BRIDLE_VERSION}"
VIProductVersion "${BRIDLE_VERSION}.0"

##############################
# Installer callback Functions
##############################

Function .onInit

    old_install_dir = ReadRegStr("HKLM", \
                                 ${PRODUCT_REG_PATH} + "\" + ${BRIDLE_VERSION}, \
                                 "InstallDir")

    If old_install_dir != ""

        If "CANCEL" == MsgBox("OKCANCEL", \
                              "BridleNSIS version " + ${BRIDLE_VERSION} + " is already installed to " + old_install_dir + ".$\n$\n \
                                   Click OK to replace it with new version or Cancel to quit the installer.", \
                              "ICONQUESTION|DEFBUTTON2")

            Abort ; User want's to quit

        EndIf

        ; Mark existing version to be removed
        global.replace_instdir = old_install_dir

        ; New copy will be installed to the same directory by default
        instdir = old_install_dir

    EndIf

FunctionEnd

##################
# Custom Functions
##################

Function UninstallOldVersion(version, directory)

    DetailPrint("Uninstall old version " + version + " at " + directory)
    SetDetailsPrint none
  
    uninst_exe = ReadRegStr("HKLM", \
                            "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}-" + version, \
                            "UninstallString")

    temp_file = GetTempFileName()

    If FileCopy(uninst_exe, temp_file) <> 0
        SetDetailsPrint lastused
        Abort("Old version uninstaller not found.")
    EndIf
    
    result = ExecWait(temp_file + " /S _?=" + directory)

    Delete(temp_file)

    If result <> 0
        SetDetailsPrint lastused
        Abort("Old version uninstall returned " + result + ".")
    EndIf

    SetDetailsPrint lastused

FunctionEnd

##################
# Install Sections
##################

Section "Main"

    If replace_instdir != ""
        ; Current version is installed to somewhere else
        UninstallOldVersion(${BRIDLE_VERSION}, replace_instdir)
    EndIf
    
    If replace_version != ""
        ; Some other version is in instdir already
        UninstallOldVersion(replace_version, instdir)
    EndIf
    
    SetOutPath "$INSTDIR"
    File "${BRIDLE_HOME}\${PRODUCT_NAME}-${BRIDLE_VERSION}.jar"
    File "${BRIDLE_HOME}\LICENSE"
    File "${BRIDLE_HOME}\Manual.html"
    File "${BRIDLE_HOME}\Release Notes.html"

    SetOutPath "$INSTDIR\Example"
    File /r "${BRIDLE_HOME}\Example\*.nsi"
    File /r "${BRIDLE_HOME}\Example\*.nsh"
    File    "${BRIDLE_HOME}\Example\MakeInstaller.bat"
    
    WriteRegStr("HKLM",   ${VERSION_REG_PATH},    "InstallDir",      instdir)
    
    WriteRegStr("HKLM",   ${PRODUCT_REG_PATH},    "",                "$INSTDIR\${PRODUCT_NAME}-${BRIDLE_VERSION}.jar")
    
    WriteRegStr("HKLM",   ${WIN_UNINST_REG_PATH}, "DisplayName",     ${PRODUCT_NAME})
    WriteRegStr("HKLM",   ${WIN_UNINST_REG_PATH}, "DisplayVersion",  ${BRIDLE_VERSION})
    WriteRegStr("HKLM",   ${WIN_UNINST_REG_PATH}, "Publisher",       "Henri Kor")
    WriteRegStr("HKLM",   ${WIN_UNINST_REG_PATH}, "UninstallString", instdir + "\" + ${UNINSTALLER_EXE})
    
    WriteRegDWORD("HKLM", ${WIN_UNINST_REG_PATH}, "EstimatedSize",    SectionGetSize(0))
    WriteRegDWORD("HKLM", ${WIN_UNINST_REG_PATH}, "NoModify",         1)
    WriteRegDWORD("HKLM", ${WIN_UNINST_REG_PATH}, "NoRepair",         1)
    
    WriteUninstaller "${UNINSTALLER_EXE}"

SectionEnd

####################
# Uninstall Sections
####################

Section "Uninstall"

    DeleteRegKey          HKLM "${WIN_UNINST_REG_PATH}"
    DeleteRegKey          HKLM "${VERSION_REG_PATH}"
    DeleteRegValue        HKLM "${PRODUCT_REG_PATH}" ""
    DeleteRegKey /ifempty HKLM "${PRODUCT_REG_PATH}"

    Delete "$INSTDIR\${PRODUCT_NAME}-${BRIDLE_VERSION}.jar"
    Delete "$INSTDIR\LICENSE"
    Delete "$INSTDIR\Release Notes.html"
    Delete "$INSTDIR\Manual.html"
    Delete "$INSTDIR\${UNINSTALLER_EXE}"

    RMDir /r "$INSTDIR\Example"
    RMDir    "$INSTDIR"

SectionEnd

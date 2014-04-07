##############
# GUI Settings
##############

!include MUI2.nsh

###############
# Install Pages
###############

!insertmacro MUI_PAGE_LICENSE "${BRIDLE_HOME}\LICENSE"
!define MUI_PAGE_CUSTOMFUNCTION_LEAVE DirectoryPageLeave
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

#################
# Uninstall Pages
#################

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

##################
# Language Support
##################

!insertmacro MUI_LANGUAGE "English"

#######################
# Page Custom Functions
#######################

## Check if selected instdir is not already reserved for old Bridle version
Function DirectoryPageLeave()

    index = 0
    version = EnumRegKey("HKLM", ${PRODUCT_REG_PATH}, index)
    
    Do Until version == ""
        
        folder = ReadRegStr("HKLM", ${PRODUCT_REG_PATH} + "\" + version, "InstallDir")
        
        If folder == global.instdir And version != ${BRIDLE_VERSION}
        
            If "CANCEL" == MsgBox("OKCANCEL", \
                                  "Selected folder already contains BridleNSIS version " + version + ".$\n \
                                       Click OK to replace it with new version or Cancel to change the install folder.", \
                                  "ICONQUESTION")
    
                Abort ; Go back to the page
    
            EndIf

            ; Mark old version in 'instdir' to be replaced
            global.replace_version = version
            
            Break ; Exit loop
        
        EndIf
        
        index = IntOp(index, "+", 1)
        version = EnumRegKey("HKLM", ${PRODUCT_REG_PATH}, index)
    
    Loop

FunctionEnd

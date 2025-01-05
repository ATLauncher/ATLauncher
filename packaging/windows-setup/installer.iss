#define MyAppName "ATLauncher"
#define MyAppURL "https://atlauncher.com"
#define MyAppVersion "1.2.0.0"

[Setup]
AppId={{2F5FDA11-45A5-4CC3-8E51-5E11E2481697}
AppName={#MyAppName}
AppVerName={#MyAppName}
AppPublisher={#MyAppName}
AppVersion={#MyAppVersion}
VersionInfoVersion={#MyAppVersion}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={userappdata}\{#MyAppName}
DisableDirPage=auto
DisableWelcomePage=no
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
LicenseFile=..\..\LICENSE
PrivilegesRequired=lowest
SetupIconFile=..\..\src\main\resources\assets\image\icon.ico
WizardImageFile=wizardimage.bmp
Compression=lzma
SolidCompression=yes
OutputBaseFilename={#MyAppName}-setup-{#MyAppVersion}
UninstallDisplayIcon={app}\{#MyAppName}.exe
UninstallDisplayName={#MyAppName} Setup
WizardStyle=modern
ChangesAssociations=yes

[Run]
Filename: {tmp}\7za.exe; Parameters: "x ""{tmp}\jre.zip"" -o""{app}\"" * -r -aoa"; Flags: runhidden runascurrentuser; Components: java
Filename: {app}\{#MyAppName}.exe; Description: {cm:LaunchProgram,{#MyAppName}}; Flags: nowait postinstall skipifsilent

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: checkedonce

[Files]
Source: "7za.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "{tmp}\{#MyAppName}.exe"; DestDir: "{app}"; Flags: external ignoreversion
Source: "{tmp}\jre.zip"; DestDir: "{tmp}"; Flags: external deleteafterinstall; Components: java

[Components]
Name: "atlauncher"; Description: "ATLauncher"; ExtraDiskSpaceRequired: 20000000; Types: full compact custom; Flags: fixed
Name: "java"; Description: "Install Java 17 (For ATLauncher Only)"; ExtraDiskSpaceRequired: 129016602; Types: full; Flags: disablenouninstallwarning

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"
Name: "{userdesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"; Tasks: desktopicon
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[InstallDelete]
Type: filesandordirs; Name: "{app}\jre"; Components: java

[UninstallDelete]
Type: filesandordirs; Name: "{app}\jre"; Components: java

[Code]
var
  DownloadPage: TDownloadWizardPage;

procedure InitializeWizard;
begin
  DownloadPage := CreateDownloadPage(SetupMessage(msgWizardPreparing), SetupMessage(msgPreparingDesc), nil);
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if (CurStep = ssPostInstall) then begin
    if WizardIsComponentSelected('java') then begin
      if not RenameFile(ExpandConstant('{app}') + '\jdk-17.0.3+7-jre', ExpandConstant('{app}/jre')) then begin
        MsgBox('Failed to rename jre directory. Please try again or uncheck the "Install Java" option', mbError, MB_OK);
        WizardForm.Close;
      end
    end
  end
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  if (CurPageID = wpSelectComponents) and not WizardIsComponentSelected('java') then
  begin
    Result := SuppressibleMsgBox('The option to install Java was not selected. Letting the launcher install and use it''s own version of Java is highly recommended to avoid issues in the future when we update the application.' + #13#10 + #13#10 + 'Installing this will not install it globally on your system, only for ATLauncher to use.' + #13#10 + #13#10 + 'Are you sure you want to continue without installing Java?', mbConfirmation, MB_YESNO, IDNO) = IDYES;
  end else if CurPageID = wpReady then begin
    DownloadPage.Clear;

    DownloadPage.Add('https://download.nodecdn.net/containers/atl/ATLauncher.exe', '{#MyAppName}.exe', '');

    if WizardIsComponentSelected('java') then begin
      if IsWin64 then begin
        DownloadPage.Add('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jre_x64_windows_hotspot_17.0.3_7.zip', 'jre.zip', 'd77745fdb57b51116f7b8fabd7d251067edbe3c94ea18fa224f64d9584b41a97');
      end else begin
        DownloadPage.Add('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.3_7.zip', 'jre.zip', 'e29e311e4200a32438ef65637a75eb8eb09f73a37cef3877f08d02b6355cd221');
      end;
    end;

    DownloadPage.Show;
    try
      try
        DownloadPage.Download;
        Result := True;
      except
          SuppressibleMsgBox(AddPeriod(GetExceptionMessage), mbCriticalError, MB_OK, IDOK);
        Result := False;
      end;
    finally
      DownloadPage.Hide;
    end;
  end else
    Result := True;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  if CurUninstallStep = usPostUninstall then
  begin
    if MsgBox('Do you want to delete all the launchers data (instances, downloads, saves, etc)?', mbConfirmation, MB_YESNO) = IDYES then begin
        if DelTree(ExpandConstant('{app}/'), True, True, True) then
        begin
        end else
        begin
            MsgBox('Error deleting user data. Please delete it manually.', mbError, MB_OK);
        end;
    end;
  end;
end;

#define MyAppName "ATLauncher"
#define MyAppURL "https://atlauncher.com"
#define MyAppVersion "1.3.0.0"

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
AlwaysShowComponentsList=no
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
Filename: {tmp}\7za.exe; Parameters: "x ""{tmp}\jre.zip"" -o""{app}\"" * -r -aoa"; Flags: runhidden runascurrentuser
Filename: {app}\{#MyAppName}.exe; Description: {cm:LaunchProgram,{#MyAppName}}; Flags: nowait postinstall skipifsilent

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: checkedonce

[Files]
Source: "7za.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "{tmp}\{#MyAppName}.exe"; DestDir: "{app}"; Flags: external ignoreversion
Source: "{tmp}\jre.zip"; DestDir: "{tmp}"; Flags: external deleteafterinstall

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"
Name: "{userdesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"; Tasks: desktopicon
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[InstallDelete]
Type: filesandordirs; Name: "{app}\jre"

[UninstallDelete]
Type: filesandordirs; Name: "{app}\jre"

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
    if not RenameFile(ExpandConstant('{app}') + '\jdk-17.0.3+7-jre', ExpandConstant('{app}/jre')) then begin
      MsgBox('Failed to rename jre directory. Please try again', mbError, MB_OK);
      WizardForm.Close;
    end
  end
end;

function NextButtonClick(CurPageID: Integer): Boolean;
var
  Retry: Boolean;
  Answer: Integer;
begin
  if CurPageID = wpReady then begin
    try
      // The launcher download must complete
      repeat
        DownloadPage.Clear;
        DownloadPage.Add('https://download.nodecdn.net/containers/atl/ATLauncher.exe', '{#MyAppName}.exe', '');
        DownloadPage.Show;
        try
          DownloadPage.Download;
          Result := True;
          Retry := False;
        except
          Answer := SuppressibleMsgBox(AddPeriod(GetExceptionMessage), mbCriticalError, MB_RETRYCANCEL, IDRETRY);
          Retry := (Answer = IDRETRY);
          Result := (Answer <> IDCANCEL);
        end;
      until not Retry;
      
      if not Result then Exit;

      // Now do the download for the JRE, but make it optional and okay if it fails
      repeat
        DownloadPage.Clear;
        if IsWin64 then begin
          DownloadPage.Add('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jre_x64_windows_hotspot_17.0.3_7.zip', 'jre.zip', 'd77745fdb57b51116f7b8fabd7d251067edbe3c94ea18fa224f64d9584b41a97');
        end else begin
          DownloadPage.Add('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.3_7.zip', 'jre.zip', 'e29e311e4200a32438ef65637a75eb8eb09f73a37cef3877f08d02b6355cd221');
        end;
        try
          DownloadPage.Download;
          Result := True;
          Retry := False;
        except
          Answer := SuppressibleMsgBox(AddPeriod(GetExceptionMessage), mbCriticalError, MB_ABORTRETRYIGNORE, IDIGNORE);
          Retry := (Answer = IDRETRY);
          Result := (Answer <> IDABORT);
        end;
      until not Retry;
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

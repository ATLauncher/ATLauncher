#define MyAppName "ATLauncher"
#define MyAppURL "https://atlauncher.com"
#define MyAppVersion "1.0.0.0"

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
OutputBaseFilename={#MyAppName}-setup
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
Name: "java"; Description: "Install Java"; ExtraDiskSpaceRequired: 99401728; Types: full

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
      if not RenameFile(ExpandConstant('{app}/jdk8u312-b07-jre'), ExpandConstant('{app}/jre')) then begin
        MsgBox('Failed to rename jre directory. Please try again or uncheck the "Install Java" option', mbError, MB_OK);
        WizardForm.Close;
      end
    end
  end
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  if CurPageID = wpReady then begin
    DownloadPage.Clear;

    DownloadPage.Add('https://download.nodecdn.net/containers/atl/ATLauncher.exe', '{#MyAppName}.exe', '');

    if WizardIsComponentSelected('java') then begin
      if IsWin64 then begin
        DownloadPage.Add('https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x64_windows_hotspot_8u312b07.zip', 'jre.zip', 'a4623365d70e7bc969e84b7f29b6b2eecb6c0686863ed67651506e2b5adf43b0');
      end else begin
        DownloadPage.Add('https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x86-32_windows_hotspot_8u312b07.zip', 'jre.zip', '789cfa21858156084020ee885ade643556dc57ddec2b44745b5489edb7945b64');
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

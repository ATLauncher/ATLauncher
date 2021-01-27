#define MyAppName "ATLauncher"
#define MyAppURL "https://atlauncher.com"

[Setup]
AppId={{2F5FDA11-45A5-4CC3-8E51-5E11E2481697}
AppName={#MyAppName}
AppVerName={#MyAppName}
AppPublisher={#MyAppName}
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
SetupIconFile=..\..\src\main\resources\assets\image\Icon.ico
WizardImageFile=wizardimage.bmp
Compression=lzma
SolidCompression=yes
OutputBaseFilename={#MyAppName}-setup
UninstallDisplayIcon={app}\{#MyAppName}.exe
UninstallDisplayName={#MyAppName}
WizardStyle=modern

[Run]
Filename: {tmp}\7za.exe; Parameters: "x ""{tmp}\runtime.zip"" -o""{app}\runtimes\"" * -r -aoa"; Flags: runhidden runascurrentuser; Components: java
Filename: {tmp}\7za.exe; Parameters: "x ""{tmp}\jre.zip"" -o""{app}\"" * -r -aoa"; Flags: runhidden runascurrentuser; Components: java
Filename: {app}\{#MyAppName}.exe; Description: {cm:LaunchProgram,{#MyAppName}}; Flags: nowait postinstall skipifsilent

[Files]
Source: "7za.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "{tmp}\{#MyAppName}.exe"; DestDir: "{app}"; Flags: external
Source: "{tmp}\runtime.zip"; DestDir: "{tmp}"; Flags: external deleteafterinstall; Components: java
Source: "{tmp}\jre.zip"; DestDir: "{tmp}"; Flags: external deleteafterinstall; Components: java

[Components]
Name: "atlauncher"; Description: "ATLauncher"; ExtraDiskSpaceRequired: 20000000; Types: full compact custom; Flags: fixed
Name: "java"; Description: "Install Java"; ExtraDiskSpaceRequired: 249643008; Types: full

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

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
    if WizardIsComponentSelected('java') then begin
      if not RenameFile(ExpandConstant('{app}/jdk8u282-b08-jre'), ExpandConstant('{app}/jre')) then begin
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
        DownloadPage.Add('https://download.nodecdn.net/containers/atl/runtimes/jre-win-64-1.8.0_51.zip', 'runtime.zip', '6521919e3b1480c278a12bedcabe2e494f8138ef38ef63fe74f595f5f72a1337');
        DownloadPage.Add('https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u282-b08/OpenJDK8U-jre_x64_windows_hotspot_8u282b08.zip', 'jre.zip', '58f2bbf0e5abc6dee7ee65431fd2fc95cdb2c3d10126045c5882f739dda79c3b');
      end else begin
        DownloadPage.Add('https://download.nodecdn.net/containers/atl/runtimes/jre-win-32-1.8.0_51.zip', 'runtime.zip', '745810d17c51e5399dbe7c2fa7b5d5d63e399c541b9af992c930591bb2a09a24');
        DownloadPage.Add('https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u282-b08/OpenJDK8U-jre_x86-32_windows_hotspot_8u282b08.zip', 'jre.zip', 'f7748dbdfc904a7e713aebec1aabdaf7b0a18b90517adec55b93f921dce8dd65');
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

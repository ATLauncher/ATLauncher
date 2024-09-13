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
#include "lib/JsonHelpers.pas"

var
  DownloadPage: TDownloadWizardPage;
  FallbackUrl, FallbackHash, URL, HASH: WideString;

procedure GetJreInfo(
  Out URL : WideString;
  Out HASH : WideString;
  Out FOLDER : WideString
  );
  var
    WinHttpReq: Variant;
    Json, OS: string;
    JsonParser: TJsonParser;
    JsonRoot, BundledJreObject, OSObject: TJsonObject;
begin
  if IsWin64 then 
    begin
      OS := 'windowsx64'
      FallbackUrl := 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x64_windows_hotspot_17.0.9_9.zip'
      FallbackHash := '6c491d6f8c28c6f451f08110a30348696a04b009f8c58592191046e0fab1477b'
    end
  else
    begin
      OS := 'windowsx86'
      FallbackUrl := 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.9_9.zip'
      FallbackHash := '2f9fe8b587400e89cd3ef33b71e0517ab99a12a5ee623382cbe9f5078bf2b435'
  end;
  WinHttpReq := CreateOleObject('WinHttp.WinHttpRequest.5.1');
  WinHttpReq.Open('GET', 'https://download.nodecdn.net/containers/atl/launcher/json/config.json', False);
  WinHttpReq.Send('');
  if WinHttpReq.Status == 200 then
  begin
  Json := WinHttpReq.ResponseText
  if ParseJsonAndLogErrors(JsonParser, Json) then
    begin
      JsonRoot := GetJsonRoot(JsonParser.Output);
      if FindJsonObject(JsonParser.Output, JsonRoot, 'bundledJre', BundledJreObject) and
        FindJsonObject(JsonParser.Output, BundledJreObject, OS, OSObject) and
        FindJsonString(JsonParser.Output, OSObject, 'url', URL) and
        FindJsonString(JsonParser.Output, OSObject, 'hash', HASH) and 
        FindJsonString(JsonParser.Output, OSObject, 'folcer',FOLDER) then
        begin
          MsgBox(URL, mbInformation, MB_OK)
          MsgBox(HASH, mbInformation, MB_OK)
        end
         else
        begin
          URL := FallbackUrl
          HASH := FallbackHash
          FOLDER := 'jdk-17.0.9+9-jre'
        end;
        ClearJsonParser(JsonParser);
    end;
  end;
end;

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
      GetJreInfo(URL,HASH)
      DownloadPage.Add(URL,'jre.zip',HASH);
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

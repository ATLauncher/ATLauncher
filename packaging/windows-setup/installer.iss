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
#include "lib/JsonHelpers.pas"
const
CONFIGURL = 'https://download.nodecdn.net/containers/atl/launcher/json/config.json';
FALLBACKx86 = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.9_9.zip';
FALLBACKx64 = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jre_x64_windows_hotspot_17.0.9_9.zip';

var
  DownloadPage: TDownloadWizardPage;
  FallbackUrl, FallbackHash, Url, Hash, Folder: WideString;

procedure GetJreInfo;
  var
    WinHttpReq: Variant;
    Json, OS: string;
    JsonParser: TJsonParser;
    JsonRoot, BundledJreObject, OSObject: TJsonObject;
begin
  if IsWin64 then
    begin
      OS := 'windowsx64'
      FallbackUrl := FALLBACKx64
      FallbackHash := '6c491d6f8c28c6f451f08110a30348696a04b009f8c58592191046e0fab1477b'
    end
  else
    begin
      OS := 'windowsx86'
      FallbackUrl := FALLBACKx86
      FallbackHash := '2f9fe8b587400e89cd3ef33b71e0517ab99a12a5ee623382cbe9f5078bf2b435'
    end;
  Try
    WinHttpReq := CreateOleObject('WinHttp.WinHttpRequest.5.1');
    WinHttpReq.Open('GET', CONFIGURL, False);
    WinHttpReq.Send('');
    if WinHttpReq.Status = 200 then
     begin
        Json := WinHttpReq.ResponseText
        if ParseJsonAndLogErrors(JsonParser, Json) then
          begin
            JsonRoot := GetJsonRoot(JsonParser.Output);
            if not FindJsonObject(JsonParser.Output, JsonRoot, 'bundledJre', BundledJreObject) or
            not FindJsonObject(JsonParser.Output, BundledJreObject, OS, OSObject) or
            not FindJsonString(JsonParser.Output, OSObject, 'url', Url) or
            not FindJsonString(JsonParser.Output, OSObject, 'hash', Hash) or
            not FindJsonString(JsonParser.Output, OSObject, 'folder',Folder) then
            begin
              RaiseException('Failed to read from ' + CONFIGURL + ', falling back to defaults')
            end;
        end;
      ClearJsonParser(JsonParser)
      end
      else
        begin
          RaiseException('Failed to read from ' + CONFIGURL + ', falling back to defaults')
      end;
  Except
    MsgBox(GetExceptionMessage,mbError,MB_OK)
    Url := FallbackUrl
    Hash := FallbackHash
    Folder := 'jdk-17.0.9+9-jre'
 end;
end;

procedure InitializeWizard;
begin
  DownloadPage := CreateDownloadPage(SetupMessage(msgWizardPreparing), SetupMessage(msgPreparingDesc), nil);
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if (CurStep = ssPostInstall) then begin
    if not RenameFile(ExpandConstant('{app}') + '\' + Folder, ExpandConstant('{app}/jre')) then begin
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
        GetJreInfo;
        DownloadPage.Add(Url, 'jre.zip', Hash);
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

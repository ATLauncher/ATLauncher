//Taken from Martin Prikryl's answer for https://stackoverflow.com/questions/34290115/how-to-parse-a-json-string-in-inno-setup
#include "JsonParser.pas"

function GetJsonRoot(Output: TJsonParserOutput): TJsonObject;
begin
  Result := Output.Objects[0];
end;

function FindJsonValue(
  Output: TJsonParserOutput; Parent: TJsonObject; Key: TJsonString;
  var Value: TJsonValue): Boolean;
var
  I: Integer;
begin
  for I := 0 to Length(Parent) - 1 do
  begin
    if Parent[I].Key = Key then
    begin
      Value := Parent[I].Value;
      Result := True;
      Exit;
    end;
  end;

  Result := False;
end;

function FindJsonObject(
  Output: TJsonParserOutput; Parent: TJsonObject; Key: TJsonString;
  var Object: TJsonObject): Boolean;
var
  JsonValue: TJsonValue;
begin
  Result :=
    FindJsonValue(Output, Parent, Key, JsonValue) and
    (JsonValue.Kind = JVKObject);

  if Result then
  begin
    Object := Output.Objects[JsonValue.Index];
  end;
end;

function FindJsonNumber(
  Output: TJsonParserOutput; Parent: TJsonObject; Key: TJsonString;
  var Number: TJsonNumber): Boolean;
var
  JsonValue: TJsonValue;
begin
  Result :=
    FindJsonValue(Output, Parent, Key, JsonValue) and
    (JsonValue.Kind = JVKNumber);

  if Result then
  begin
    Number := Output.Numbers[JsonValue.Index];
  end;
end;

function FindJsonString(
  Output: TJsonParserOutput; Parent: TJsonObject; Key: TJsonString;
  var Str: TJsonString): Boolean;
var
  JsonValue: TJsonValue;
begin
  Result :=
    FindJsonValue(Output, Parent, Key, JsonValue) and
    (JsonValue.Kind = JVKString);
  if Result then
  begin
    Str := Output.Strings[JsonValue.Index];
  end;
end;

function ParseJsonAndLogErrors(
  var JsonParser: TJsonParser; const Source: WideString): Boolean;
var
  I: Integer;
begin
  ParseJson(JsonParser, Source);

  Result := (Length(JsonParser.Output.Errors) = 0);
  if not Result then
  begin
    Log('Error parsing JSON');
    for I := 0 to Length(JsonParser.Output.Errors) - 1 do
    begin
      Log(JsonParser.Output.Errors[I]);
    end;
  end;
end;

program 现场系统;

uses
  Forms,
  xianchang in 'xianchang.pas' {formMain};

{$R *.res}

begin
  Application.Initialize;
  Application.MainFormOnTaskbar := True;
  Application.Title := '现场系统';
  Application.CreateForm(TformMain, formMain);
  Application.Run;
end.

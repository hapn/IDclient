program �ֳ�ϵͳ;

uses
  Forms,
  xianchang in 'xianchang.pas' {formMain};

{$R *.res}

begin
  Application.Initialize;
  Application.MainFormOnTaskbar := True;
  Application.Title := '�ֳ�ϵͳ';
  Application.CreateForm(TformMain, formMain);
  Application.Run;
end.

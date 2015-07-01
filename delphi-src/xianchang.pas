unit xianchang;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, ShellAPI, pngimage, ExtCtrls, GIFImg;

type
  TformMain = class(TForm)
    Image1: TImage;
    Image2: TImage;
    procedure FormCreate(Sender: TObject);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

var
  formMain: TformMain;

implementation

{$R *.dfm}

procedure TformMain.FormCreate(Sender: TObject);
var

   dwExStyle : DWORD;
begin
  SetWindowLong(Application.Handle, GWL_EXSTYLE, dwExStyle);

  TGIFImage(Image2.Picture.Graphic).AnimationSpeed := 200;
  TGIFImage(Image2.Picture.Graphic).Animate := true;

  ShellExecute(Handle, 'open', PChar('bin\xianchang.exe'), PChar(''), nil, SW_SHOW);
end;

end.

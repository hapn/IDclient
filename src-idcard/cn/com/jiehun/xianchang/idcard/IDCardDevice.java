package cn.com.jiehun.xianchang.idcard;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

interface IDCardDevice extends Library {
	IDCardDevice sdtapi = (IDCardDevice) Native
			.loadLibrary((Platform.isWindows() ? ".\\dll\\sdtapi" : "c"),
					IDCardDevice.class);

	int InitComm(int port);

	int Authenticate();

	int GetSAMIDToStr(byte[] samid);

	int ReadBaseInfos(byte[] Name, byte[] Gender, byte[] Folk, byte[] BirthDay,
			byte[] Code, byte[] Address, byte[] Agency, byte[] ExpireStart,
			byte[] ExpireEnd);

	int ReadBaseInfosPhoto(byte[] Name, byte[] Gender, byte[] Folk,
			byte[] BirthDay, byte[] Code, byte[] Address, byte[] Agency,
			byte[] ExpireStart, byte[] ExpireEnd, String dir);
	
	IDCardDevice msgapi = (IDCardDevice) Native
			.loadLibrary((Platform.isWindows() ? "user32" : "c"),
					IDCardDevice.class);
	
	Boolean MessageBeep(short uType);

	public final static int DEFAULT_PORT = 1001;
}

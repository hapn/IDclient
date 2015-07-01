package cn.com.jiehun.xianchang.iccard;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

interface ICCardDevice extends Library {
	ICCardDevice sdtapi = (ICCardDevice) Native
			.loadLibrary((Platform.isWindows() ? ".\\dll\\mwrf32" : "c"),
					ICCardDevice.class);

	// 连接设备
	int usb_ic_init();

	// // 复位
	// byte[] rf_rest(int icdev,byte[] time);
	// 中指卡操作
	short rf_halt(int icdev);

	// 关闭连接
	short usb_ic_exit(int icdev);

	// 蜂鸣
	short rf_beep(int icdev, int time);

	// 寻卡请求
	short rf_request(int icdev, byte b_mode, byte[] b_tagType);

	// 防止卡冲突 返回卡的序列号
	short rf_anticoll(int icdev, byte bcnt, byte[] snr);

	// 取UL卡的序列号
	short rf_get_snr(int icdev, byte[] snr);

	// 从多个卡中选取一个给定序列号的卡
	short rf_select(int icdev, byte[] snr, byte[] _size);

	// 取得读写器硬件版本号
	short rf_get_status(int icdev, byte[] b_status);

	// 读取软件版本号
	short lib_ver(byte[] buff);

	// 读取卡中数据
	short rf_read_hex(int icdev, byte[] addr, byte[] data);

	short rf_read(int icdev, int addr, byte[] data);

	// int rf_read_hex(int icdev, byte[] addr, byte[] data);
	// int rf_read(int icdev, byte[] addr, byte[] data);
	// 往卡里写数据
	short rf_write_hex(int icdev, byte[] addr, byte[] data);
}
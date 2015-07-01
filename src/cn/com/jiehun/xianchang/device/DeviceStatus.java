package cn.com.jiehun.xianchang.device;

import java.util.Date;

import cn.com.jiehun.xianchang.config.SysConfig;
import cn.com.jiehun.xianchang.util.Md5Util;

public class DeviceStatus {
	public static Boolean ICCardConnected = false;
	public static Boolean IDCardConnected = false;
	public static String ICCardNumber;
	public static String IDCardNumber;
	
	private static String encryMask;
	/**
	 * 加密字符串
	 * @param time
	 * @param orig
	 * @return
	 */
	public static String encryStr(int time, String orig) {
		if (encryMask == null) {
			encryMask = SysConfig.getInstance().getEncryMask();
		}
		
		return Md5Util.getMD5Str(time + "_" + encryMask + orig); 
	}
}

package cn.com.jiehun.xianchang.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class SysConfig {
	private static Properties properties = new Properties();
	private HashMap<String, String> options = new HashMap<String, String>();
	private final String HOMEPAGE = "homepage";
	private final String ENCRY_MASK = "encrymask";
	private final String JAVA_DIR = "javadir";
	private final String TOOLBAR = "toolbar";
	private final String STATUSBAR = "statusbar";
	private final String ICCARD_CMD = "iccard_cmd";
	private final String IDCARD_CMD = "idcard_cmd";
	private final String CLOSE_BEFORE_NEW = "close_before_new"; // 打开新窗口时先关闭掉之前的
	private static final String PROPERTIES_FILE_NAME = "sysconfig.properties";
	
	private final String USE_ICCARD = "use_iccard";
	private final String USE_IDCARD = "use_idcard";

	private static SysConfig sysConfig;

	private SysConfig() {
		
	}

	public static SysConfig getInstance() {
		if (sysConfig == null) {
			sysConfig = new SysConfig();
		}
		return sysConfig;
	}

	/**
	 * 初始化properties，即载入数据
	 */
	private void initProperties() {
		try {
			InputStream ips = new FileInputStream(PROPERTIES_FILE_NAME);
			properties.load(ips);
			ips.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Boolean getToolbar() {
		String show = this.getString(TOOLBAR, "1");
		return show.equals("1");
	}
	
	public void setToolbar(Boolean visible) {
		this.saveString(TOOLBAR, visible ? "1" : "0");
	}
	
	public Boolean getStatusbar() {
		String show = this.getString(STATUSBAR, "1");
		return show.equals("1");
	}
	
	public void setStatusbar(Boolean visible) {
		this.saveString(STATUSBAR, visible ? "1" : "0");
	}
	
	public String getICCardCmd() {
		return this.getString(ICCARD_CMD, "iccard.jar -nd");
	}
	
	public String getIDCardCmd() {
		return this.getString(IDCARD_CMD, "idcard.jar -nd");
	}
	
	public String getJavaDir() {
		return this.getString(JAVA_DIR);
	}
	
	public String getHomepageUrl() {
		return this.getString(HOMEPAGE, "");
	}
	
	public String getEncryMask() {
		return this.getString(ENCRY_MASK);
	}

	public void setHomepageUrl(String homepageUrl) {
		this.saveString(HOMEPAGE, homepageUrl);
	}
	
	public boolean getCloseBeforeNew() {
		return this.getString(CLOSE_BEFORE_NEW).equals("1") ? true : false;
	}
	
	public void setCloseBeforeNew(Boolean closeBeforeNew) {
		this.saveString(CLOSE_BEFORE_NEW, closeBeforeNew ? "1" : "0");
	}
	
	public boolean getUseICCard() {
		return this.getString(USE_ICCARD).equals("0") ? false : true;
	}
	
	public void setUseICCard(Boolean useICCard) {
		this.saveString(USE_ICCARD, useICCard ? "1" : "0");
	}
	
	public boolean getUseIDCard() {
		return this.getString(USE_IDCARD).equals("0") ? false : true;
	}
	
	public void setUseIDCard(Boolean useIDCard) {
		this.saveString(USE_IDCARD, useIDCard ? "1" : "0");
	}
	
	private void saveString(String name, String value) {
		if (properties.isEmpty()) {
			initProperties();
		}
		// 修改值
		properties.setProperty(name, value);
		this.options.put(name, value);
		// 保存文件
		try {
			FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE_NAME);
			properties.store(fos, "the system configs");
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getString(String name) {
		return getString(name, "");
	}

	private String getString(String name, String defVal) {
		if (this.options.containsKey(name)) {
			return this.options.get(name);
		}
		
		if (properties.isEmpty()) {
			initProperties();
		}
		String ret = properties.getProperty(name);
		if (ret == null) {
			return defVal;
		}
		return ret;
	}
}
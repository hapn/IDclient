package cn.com.jiehun.xianchang.idcard;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IDCardHandler {
	private static Level LOGGER_LEVEL;

	private Logger log = Logger.getLogger(IDCardHandler.class.getName());
	private OnReadActionListener listener;
	
	private static long READ_INTERVAL = 500;
	private static long SINGLE_DELAY = 5000;
	private static long FAILED_RETRY_TIME = 3000;

	public void setOnReadListener(OnReadActionListener listener) {
		this.listener = listener;
	}

	public static char[] tagtype = new char[10];

	public IDCardHandler() {
		log = Logger.getLogger(IDCardHandler.class.getName());
		log.setLevel(LOGGER_LEVEL);
	}


	public static String formatString(byte[] orig) {
		try {
			return new String(orig, "gbk").replace("\0", "").replace(" ", "");
		} catch (UnsupportedEncodingException e) {
			return new String(orig).replace("\0", "").replace(" ", "");
		}
	}

	private byte name[] = new byte[51];
	private byte sex[] = new byte[3];
	private byte folk[] = new byte[10];
	private byte birth[] = new byte[9];
	private byte code[] = new byte[19];
	private byte addr[] = new byte[71];
	private byte agency[] = new byte[31];
	private byte expirestart[] = new byte[9];
	private byte expireend[] = new byte[9];

	private Boolean connected = false;
	private String lastCode;
	private Date lastTime;
	private final int checkConnectStatusLimit = 5;
	private int checkNum = 0;

	@SuppressWarnings("static-access")
	public void run() {
		// 检测是否有相册目录，没有则创建
		String dir = System.getProperty("user.dir");
		File file = new File(dir + "\\photo");
		if (!file.exists()) {
			file.mkdirs();
		}
		dir = file.getAbsolutePath() + "\\";
		
		IDCardDevice device = IDCardDevice.sdtapi;
		while (true) {
			int ret = device.InitComm(IDCardDevice.DEFAULT_PORT);
			log.info("idcard.initcommon:" + ret);

			if (ret != 1) {
				if (connected) {
					connected = false;
					log.info("idcard.disconnected");
					if (this.listener != null) {
						this.listener.onDisConnected(new CardEvent(null));
					}
				}
				try {
					new Thread().sleep(FAILED_RETRY_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			connected = true;
			checkNum = 0;
			log.info("idcard.connected");
			this.listener.onConnected(new CardEvent(new IDInfo()));

			while (true) {
				ret = device.Authenticate();
				log.info("idcard.authenticate:" + ret);
				if (ret == 1) {
					ret = device.ReadBaseInfosPhoto(name, sex, folk, birth,
							code, addr, agency, expirestart, expireend,
							dir);
					log.info("idcard.readbaseinfosphoto:" + ret);
					if (ret == 1) {
						String curCode = formatString(code);
						Date now = new Date();
						if (curCode.equals(lastCode)) {
							if (lastTime != null
									&& now.getTime() - lastTime.getTime() < SINGLE_DELAY) {
								try {
									new Thread().sleep(READ_INTERVAL);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								continue;
							}
						}
						lastTime = now;
						lastCode = curCode;

						IDInfo info = new IDInfo();
						info.setName(formatString(name));
						info.setSex(formatString(sex));
						info.setFolk(formatString(folk));
						info.setBirth(formatString(birth));
						info.setCode(curCode);
						info.setAddress(formatString(addr));
						info.setAgency(formatString(agency));
						info.setExpirestart(formatString(expirestart));
						info.setExpireend(formatString(expireend));
						info.setPhotoPath("\\\\photo\\\\photo.bmp");

						IDCardDevice.msgapi.MessageBeep((short)1);
						if (this.listener != null) {
							this.listener.onReadCard(new CardEvent(info));
						}
					}
				} else {
					checkNum++;
					if (checkNum > checkConnectStatusLimit) {
						checkNum = 0;
						ret = device.InitComm(IDCardDevice.DEFAULT_PORT);
						if (ret != 1) {
							if (connected) {
								connected = false;
								log.info("idcard.disconnected");
								if (this.listener != null) {
									this.listener.onDisConnected(new CardEvent(
											new IDInfo()));
								}
								break;
							}
						}
					}
				}

				try {
					new Thread().sleep(READ_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public class CardEvent extends EventObject {
		private IDInfo IDInfo;

		public CardEvent(IDInfo IDInfo) {
			super(IDInfo);
			this.IDInfo = IDInfo;
		}

		public IDInfo getIDInfo() {
			return this.IDInfo;
		}

	}

	public static class IDInfo {
		private String name;
		private String sex;
		private String folk;
		private String birth;
		private String code;
		private String address;
		private String agency;
		private String expirestart;
		private String expireend;

		private String photoPath;
		
		public IDInfo() {
			
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSex() {
			return sex;
		}

		public void setSex(String sex) {
			this.sex = sex;
		}

		public String getFolk() {
			return folk;
		}

		public void setFolk(String folk) {
			this.folk = folk;
		}

		public String getBirth() {
			return birth;
		}

		public void setBirth(String birth) {
			this.birth = birth;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String add) {
			this.address = add;
		}

		public String getAgency() {
			return agency;
		}

		public void setAgency(String agency) {
			this.agency = agency;
		}

		public String getExpirestart() {
			return expirestart;
		}

		public void setExpirestart(String expirestart) {
			this.expirestart = expirestart;
		}

		public String getExpireend() {
			return expireend;
		}

		public void setExpireend(String expireend) {
			this.expireend = expireend;
		}

		public String getPhotoPath() {
			return photoPath;
		}

		public void setPhotoPath(String photoPath) {
			this.photoPath = photoPath;
		}
	}

	public interface OnReadActionListener {
		public void onReadCard(CardEvent evt);

		public void onConnected(CardEvent evt);

		public void onDisConnected(CardEvent evt);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0 && "-nd".equals(args[0])) {
			LOGGER_LEVEL = Level.WARNING;
		} else {
			LOGGER_LEVEL = Level.INFO;
		}
		
		if (args.length > 1) {
			long time = Long.valueOf(args[1]);
			if (time > 0) {
				READ_INTERVAL = time;
			}
		}
		
		if (args.length > 2) {
			long time = Long.valueOf(args[2]);
			if (time > 0) {
				SINGLE_DELAY = time;
			}
		}
		
		if (args.length > 3) {
			long time = Long.valueOf(args[3]);
			if (time > 0) {
				FAILED_RETRY_TIME = time;
			}
		}

		IDCardHandler handler = new IDCardHandler();
		handler.setOnReadListener(new OnReadActionListener() {
			@Override
			public void onReadCard(final CardEvent evt) {
				IDInfo info = evt.getIDInfo();
				String rootPath = new String();
				File directory = new File("");// 设定为当前文件夹
				try {
					rootPath = directory.getCanonicalPath();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("b"); // 开始标记
				System.out.println("code:" + info.getCode());
				System.out.println("name:" + info.getName());
				System.out.println("sex:" + info.getSex());
				System.out.println("address:" + info.getAddress());
				System.out.println("folk:" + info.getFolk());
				System.out.println("agency:" + info.getAgency());
				System.out.println("birthday:" + info.getBirth());
				System.out.println("expire_start:" + info.getExpirestart());
				System.out.println("expire_end:" + info.getExpirestart());
				System.out.println("photo:" + rootPath.replace("\\", "\\\\") + info.getPhotoPath());
				System.out.println("e"); // 结束标记
			}

			@Override
			public void onConnected(CardEvent evt) {
				System.out.println("1");
			}

			@Override
			public void onDisConnected(CardEvent evt) {
				System.out.println("0");
			}
		});
		handler.run();
	}

}

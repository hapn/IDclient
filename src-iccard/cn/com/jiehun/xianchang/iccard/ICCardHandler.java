package cn.com.jiehun.xianchang.iccard;

import java.util.Date;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ICCardHandler {
	private static Level LOGGER_LEVEL;
	
	private Logger log;
	private OnReadActionListener listener;

	public static byte[] tagtype = new byte[10];
	private Boolean isConnected = false;
	
	private static long READ_INTERVAL = 500;
	private static long SINGLE_DELAY = 5000;
	private static long FAILED_RETRY_TIME = 3000;
	
	public ICCardHandler() {
		log = Logger.getLogger(ICCardHandler.class.getName());
		log.setLevel(LOGGER_LEVEL);
	}
	
	/**
	 * 设置事件侦听器
	 * 
	 * @param listener
	 */
	public void setOnReadListener(OnReadActionListener listener) {
		this.listener = listener;
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}

	@SuppressWarnings("static-access")
	public void run() {
		ICCardDevice device = ICCardDevice.sdtapi;
		int HANDLE = 0;
		while (true) {
			HANDLE = device.usb_ic_init();
			log.info("iccard.usb_ic_init:" + HANDLE);
			if (HANDLE > 0) {
				isConnected = true;
				log.info("iccard.connected:" + HANDLE);
				if (this.listener != null) {
					this.listener.onConnected(new CardEvent(""));
				}
			} else {
				if (isConnected) {
					isConnected = false;
					log.info("iccard.disconnected:" + HANDLE);
					if (this.listener != null) {
						this.listener.onDisConnected(new CardEvent(""));
					}
					HANDLE = 0;
				}
				try {
					new Thread().sleep(FAILED_RETRY_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			// RF500Device.rf_beep(HANDLE, (char) 20);
			byte B_SNR[] = new byte[8];
			String lastCard = "";
			Date lastTime = null;
			while (true) {
				device.rf_halt(HANDLE);
				int ret = device.rf_request(HANDLE, (byte) 0, tagtype);
				log.info("iccard.rf_request:" + ret);
				if (ret == 0) { // UL
					ret = device.rf_read(HANDLE, 0, B_SNR);
					log.info("iccard.rf_read:" + ret);
					if (ret != 0) {
						continue;
					}
					String cardNumber = bytesToHexString(B_SNR);

					Date now = new Date();
					if (cardNumber.equals(lastCard)) {
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
					lastCard = cardNumber;

					device.rf_beep(HANDLE, (char) 5);
					if (this.listener != null) {
						this.listener.onReadCard(new CardEvent(cardNumber));
					}
				} else if (ret < 0) {
					break;
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
		private String cardNumber;

		public CardEvent(String number) {
			super(number);
			this.cardNumber = number;
		}

		public String getCardNumber() {
			return this.cardNumber;
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
		if (args.length > 0 && "-nd".equals(args[0]) ) {
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
		
		ICCardHandler handler = new ICCardHandler();
		handler.setOnReadListener(new OnReadActionListener() {
			@Override
			public void onReadCard(final CardEvent evt) {
				System.out.println(evt.getCardNumber());
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

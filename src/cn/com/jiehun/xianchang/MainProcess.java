package cn.com.jiehun.xianchang;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import cn.com.jiehun.xianchang.config.SysConfig;
import cn.com.jiehun.xianchang.device.DeviceStatus;
import cn.com.jiehun.xianchang.ui.MainFrame;
import cn.com.jiehun.xianchang.ui.MainFrame.CardEventListener;

public class MainProcess {
	public static void main(String[] args) {
		// 先删除正在执行的读卡器程序
		try {
			runtime.exec("TASKKILL /IM iccard.exe /F");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			runtime.exec("TASKKILL /IM idcard.exe /F");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final MainProcess main = new MainProcess();
		if (SysConfig.getInstance().getUseICCard()) {
			main.startICCardProcess();
		}
		if (SysConfig.getInstance().getUseIDCard()) {
			main.startIDCardProcess();
		}
		
		main.initUI();
	}

	
	private final String JAR_COMMAND = SysConfig.getInstance().getJavaDir();
	// private final String JAR_COMMAND = "java -jar ";
	private final String ICCARD_JAR = SysConfig.getInstance().getICCardCmd(); // 不使用debug
	private final String IDCARD_JAR = SysConfig.getInstance().getIDCardCmd(); // 不使用debug
	private MainFrame mainFrame;
	private Logger log = Logger.getLogger(MainProcess.class.getName());
	private Process pICCard;
	private Process pIDCard;
	private static Runtime runtime = Runtime.getRuntime();
	private HashMap<String, String> idInfo;

	private String[] testICCard = new String[] { "04F5A7DE89860280",
			"0409C247118A0281", "0493E1FE59AC0280", "043A893F098A0280",
			"04A37D5259AC0280", "04B07F4349990180", "047927D2D1F60180",
			"04357CC5D14E0280", "046CC020C9AE0280", "04B7CAF129AC0280",
			"0498796D198A0280" };
	private java.util.List<HashMap<String, String>> testIDs;

	public MainProcess() {
		initTestIDs();

		runtime.addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (pICCard != null) {
					pICCard.destroy();
					log.info("iccard:Progress " + pICCard.toString()
							+ " destroyed");
				}
				if (pIDCard != null) {
					pIDCard.destroy();
					log.info("idcard:Progress " + pIDCard.toString()
							+ " destroyed");
				}
			}
		});
	}

	private void initTestIDs() {
		testIDs = new ArrayList<HashMap<String, String>>();

		String[] codes = new String[] { "11022919900801669X",
				"110229199008012314", "110101200801013439",
				"110229199008018150", "110229198508011364",
				"110229198508018400", "110104200101018940",
				"110104200101012282", "110104198601011745",
				"110104198601012764" };
		String[] names = new String[] { "夏毅书", "宁雄曙", "蒋韦飘", "池舍", "谢嘉翼",
				"吴耀建", "车飞", "石兼一", "方赫妡", "卫凉樱" };
		int len = codes.length;
		for (String code : codes) {
			int index = (int) (Math.random() * len);
			HashMap<String, String> info = new HashMap<String, String>();
			info.put("name", names[index]);
			info.put("code", code);
			info.put("birthday", code.substring(6, 8));
			info.put("sex",
					Integer.valueOf(code.substring(16, 17)) % 2 == 0 ? "女"
							: "男");
			info.put("address", "北京市西城区");
			testIDs.add(info);
		}
	}

	private void initUI() {
		NativeInterface.open();
		UIUtils.setPreferredLookAndFeel();
		mainFrame = new MainFrame();
		
		mainFrame.setCardEventListener(new CardEventListener() {

			@Override
			public void onTestIDCard(EventObject obj) {
				int index = (int) (Math.random() * testIDs.size());
				final HashMap<String, String> idInfo = testIDs.get(index);
				DeviceStatus.IDCardNumber = idInfo.get("code");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						mainFrame.setIDCard(idInfo);
					}
				});

			}

			@Override
			public void onTestICCard(EventObject obj) {
				int index = (int) (Math.random() * testICCard.length);
				final String card = testICCard[index];
				DeviceStatus.ICCardNumber = card;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						mainFrame.setICCard(card);
					}
				});
			}

			@Override
			public void onRestartIDCard(EventObject obj) {
				if (pIDCard != null) {
					pIDCard.destroy();
				}
			}

			@Override
			public void onRestartICCard(EventObject obj) {
				if (pICCard != null) {
					pICCard.destroy();
				}
			}
		});
		// swing的事件处理队列，用来处理UI的变化
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainFrame.InitComponents();
				
				// 清除掉delphi进程
				try {
					new Thread().sleep(1000);
					runtime.exec("taskkill /im 现场系统.exe /F");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		NativeInterface.runEventPump();
	}

	/**
	 * 开始IC卡的进程
	 */
	private void startICCardProcess() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						pICCard = runtime.exec(ICCARD_JAR,
								new String[] { "-nd" });
						log.info("iccard:Progress " + pICCard.toString()
								+ " start");
						BufferedReader br = new BufferedReader(
								new InputStreamReader(new BufferedInputStream(
										pICCard.getInputStream())));

						String s;
						Thread errThread = new Thread(new Runnable() {
							@Override
							public void run() {
								BufferedReader errbr = new BufferedReader(
										new InputStreamReader(
												pICCard.getErrorStream()));
								try {
									String line;
									while ((line = errbr.readLine()) != null) {
										System.out.println(line);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
						errThread.start();
						while ((s = br.readLine()) != null) {
							log.info("iccard.get string:" + s);

							if (s.equals("1")) {
								DeviceStatus.ICCardConnected = true;
								if (mainFrame != null) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											mainFrame.setICCardConnectStatus();
										}
									});
								}
							} else if (s.equals("0")) {
								DeviceStatus.ICCardConnected = false;
								if (mainFrame != null) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											mainFrame.setICCardConnectStatus();
										}
									});
								}
							} else if (!s.equals("9")) {
								DeviceStatus.ICCardNumber = s;
								if (mainFrame != null) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											mainFrame
													.setICCard(DeviceStatus.ICCardNumber);
										}
									});
								}
							}
						}
						;
						log.info("iccard:Progress " + pICCard.toString()
								+ " exit");

						if (errThread.isAlive()) {
							errThread.interrupt();
						}
						pICCard.waitFor();
						pICCard.destroy();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * 开始ID卡的进程
	 */
	private void startIDCardProcess() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						pIDCard = runtime.exec(IDCARD_JAR);
						
						log.info("idcard:Progress " + pIDCard.toString()
								+ " start");

						BufferedInputStream in = new BufferedInputStream(
								pIDCard.getInputStream());
						BufferedReader br = new BufferedReader(
								new InputStreamReader(in, "gbk"));

						Thread errThread = new Thread(new Runnable() {
							@Override
							public void run() {
								BufferedReader errbr = new BufferedReader(
										new InputStreamReader(pICCard
												.getErrorStream()));
								try {
									String line;
									while ((line = errbr.readLine()) != null) {
										System.out.println(line);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
						errThread.start();

						String s;
						while ((s = br.readLine()) != null) {
							// s = new String(s.getBytes("utf-8"));
							log.info("idcard.get string:" + s);

							if (s.equals("1")) {
								DeviceStatus.IDCardConnected = true;
								if (mainFrame != null) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											mainFrame.setIDCardConnectStatus();
										}
									});
								}
							} else if (s.equals("0")) {
								DeviceStatus.IDCardConnected = false;
								if (mainFrame != null) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											mainFrame.setIDCardConnectStatus();
										}
									});
								}
							} else if (s.equals("b")) {
								idInfo = new HashMap<String, String>();
							} else if (s.equals("e")) {
								if (!idInfo.isEmpty()
										&& idInfo.containsKey("code")) {
									DeviceStatus.IDCardNumber = idInfo
											.get("code");

									if (mainFrame != null) {
										SwingUtilities
												.invokeLater(new Runnable() {
													public void run() {
														mainFrame
																.setIDCard(idInfo);
													}
												});
									}
								}
							} else {
								if (s.indexOf(':') > -1) {
									String[] arr = s.split(":", 2);
									if (arr.length == 2) {
										idInfo.put(arr[0], arr[1]);
									}
								}
							}
						}
						log.info("idcard:Progress " + pIDCard.toString()
								+ " exit");
						new Thread().sleep(1000);

						if (errThread.isAlive()) {
							errThread.interrupt();
						}
						pIDCard.waitFor();
						pIDCard.destroy();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}

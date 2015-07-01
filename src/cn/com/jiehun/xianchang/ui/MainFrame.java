package cn.com.jiehun.xianchang.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Date;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import cn.com.jiehun.xianchang.config.SysConfig;
import cn.com.jiehun.xianchang.device.DeviceStatus;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	JLabel txtICCardStatus;
	JLabel txtICCardNumber;
	JLabel txtIDCardStatus;
	JLabel txtIDCardNumber;

	JLabel txtCardNumber;
	JWebBrowser webBrowser;
	JToolBar statusBar;
	
	JWebBrowser newWebBrowser;
	
	private CardEventListener cardEventListener;

	public interface CardEventListener extends EventListener {
		public void onRestartICCard(EventObject obj);

		public void onRestartIDCard(EventObject obj);

		public void onTestICCard(EventObject obj);

		public void onTestIDCard(EventObject obj);
	}

	public MainFrame() {

	}

	public void InitComponents() {
		Image img = Toolkit.getDefaultToolkit().getImage(".\\favicon.gif");
		this.setIconImage(img);
		// Create and set up the window.
		this.setMinimumSize(new Dimension(800, 600));
		this.setLocationRelativeTo(null);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setTitle("中国婚博会现场系统");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setBackground(Color.WHITE);

		this.setJMenuBar(buildMenuBar());
		// mainContainer.add(initToolBar(), BorderLayout.NORTH);

		this.add(initBrowser(), BorderLayout.CENTER);
		// mainContainer.add(initBrowser(), BorderLayout.CENTER);

		this.add(initStatusBar(), BorderLayout.SOUTH);

		this.pack();
		this.setVisible(true);
	}

	public void setCardEventListener(CardEventListener listener) {
		this.cardEventListener = listener;
	}
	/**
	 * 重启
	 */
	private void restart() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
					Runtime.getRuntime().exec("xianchang.exe");
				} catch (IOException e) {
					e.printStackTrace();
				}
            }    
        });
        System.exit(0);
	}

	/**
	 * 创建菜单
	 * 
	 * @return
	 */
	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("文件(F)");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

//		JMenuItem restartMenuItem = new JMenuItem("重启(E)", KeyEvent.VK_E);
//		fileMenu.add(restartMenuItem);
//		restartMenuItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent evt) {
//				int response = JOptionPane.showConfirmDialog(MainFrame.this, "你确定要重启吗？", "确认", JOptionPane.YES_NO_OPTION);
//				if (response == JOptionPane.NO_OPTION) {
//					return;
//				}
//				restart();
//			}
//		});
		
		JMenuItem exitMenuItem = new JMenuItem("退出(E)", KeyEvent.VK_E);
		fileMenu.add(exitMenuItem);
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				System.exit(0);
			}
		});
		

		JMenu toolMenu = new JMenu("工具(T)");
		toolMenu.setMnemonic(KeyEvent.VK_T);
		JMenuItem item = new JMenuItem("返回首页(H)", KeyEvent.VK_H);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String homepage = SysConfig.getInstance().getHomepageUrl();
				webBrowser.navigate(homepage);
			}
		});
		toolMenu.add(item);

		item = new JMenuItem("设为首页(S)", KeyEvent.VK_O);
		item.addActionListener(new ActionListener() {

			@SuppressWarnings("static-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				SysConfig.getInstance().setHomepageUrl(
						webBrowser.getResourceLocation());
				new JOptionPane().showMessageDialog(null, "设置成功");
			}
		});
		toolMenu.add(item);

		toolMenu.insertSeparator(toolMenu.getItemCount());

		JCheckBoxMenuItem jmi = new JCheckBoxMenuItem("工具栏(T)");
		jmi.setMnemonic(KeyEvent.VK_T);
		jmi.setSelected(SysConfig.getInstance().getToolbar());
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBoxMenuItem item = ((JCheckBoxMenuItem) e
						.getSource());

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						item.setSelected(item.isSelected());
						if (webBrowser != null) {
							webBrowser.setButtonBarVisible(item.isSelected());
							webBrowser.setLocationBarVisible(item.isSelected());
						}
						SysConfig.getInstance().setToolbar(item.isSelected());
					}
				});

			}
		});
		toolMenu.add(jmi);

		jmi = new JCheckBoxMenuItem("状态栏(S)");
		jmi.setMnemonic(KeyEvent.VK_S);
		jmi.setSelected(SysConfig.getInstance().getStatusbar());
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBoxMenuItem item = ((JCheckBoxMenuItem) e
						.getSource());

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						item.setSelected(item.isSelected());
						statusBar.setVisible(item.isSelected());
						SysConfig.getInstance().setStatusbar(item.isSelected());
					}
				});

			}
		});
		toolMenu.add(jmi);
		
		toolMenu.insertSeparator(toolMenu.getItemCount());

		jmi = new JCheckBoxMenuItem("使用IC卡(C)");
		jmi.setMnemonic(KeyEvent.VK_C);
		jmi.setSelected(SysConfig.getInstance().getUseICCard());
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBoxMenuItem item = ((JCheckBoxMenuItem) e
						.getSource());

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						item.setSelected(item.isSelected());
						SysConfig.getInstance().setUseICCard(item.isSelected());
						
						int response = JOptionPane.showConfirmDialog(MainFrame.this, "需要重启后才能生效，你确定继续吗？", "确认", JOptionPane.YES_NO_OPTION);
						if (response == JOptionPane.NO_OPTION) {
							return;
						}
						restart();
					}
				});

			}
		});
		toolMenu.add(jmi);
		
		jmi = new JCheckBoxMenuItem("使用身份证(D)");
		jmi.setMnemonic(KeyEvent.VK_D);
		jmi.setSelected(SysConfig.getInstance().getUseIDCard());
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBoxMenuItem item = ((JCheckBoxMenuItem) e
						.getSource());

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						item.setSelected(item.isSelected());
						SysConfig.getInstance().setUseIDCard(item.isSelected());
						
						int response = JOptionPane.showConfirmDialog(MainFrame.this, "需要重启后才能生效，你确定继续吗？", "确认", JOptionPane.YES_NO_OPTION);
						if (response == JOptionPane.NO_OPTION) {
							return;
						}
						restart();
					}
				});

			}
		});
		toolMenu.add(jmi);
//		item = new JMenuItem("IC卡重启");
//		item.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				if (cardEventListener != null) {
//					cardEventListener.onRestartICCard(new EventObject(arg0
//							.getSource()));
//				}
//			}
//		});
//		toolMenu.add(item);
//
//		item = new JMenuItem("身份证重启");
//		item.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				if (cardEventListener != null) {
//					cardEventListener.onRestartIDCard(new EventObject(arg0
//							.getSource()));
//				}
//			}
//		});
//		toolMenu.add(item);
		
		toolMenu.insertSeparator(toolMenu.getItemCount());

		item = new JMenuItem("IC卡测试");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (cardEventListener != null) {
					cardEventListener.onTestICCard(new EventObject(arg0
							.getSource()));
				}
			}
		});
		toolMenu.add(item);

		item = new JMenuItem("身份证测试");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (cardEventListener != null) {
					cardEventListener.onTestIDCard(new EventObject(arg0
							.getSource()));
				}
			}
		});
		toolMenu.add(item);

		menuBar.add(toolMenu);

		JMenu helpMenu = new JMenu("帮助(H)");
		helpMenu.setMnemonic(KeyEvent.VK_H);

		item = new JMenuItem("关于(A)", KeyEvent.VK_A);
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				AboutDialog dlg = new AboutDialog();
				dlg.Init();
				dlg.setModal(true);
				dlg.setResizable(false);
				dlg.setLocationRelativeTo(null);
				dlg.setVisible(true);
			}
		});
		helpMenu.add(item);

		menuBar.add(helpMenu);

		return menuBar;
	}

	private static final String CMD_BACK_MAIN_FRAME = "backMain";

	private JWebBrowser initBrowser() {
		webBrowser = new JWebBrowser();

		Boolean toolbarVisible = SysConfig.getInstance().getToolbar();
		webBrowser.setBarsVisible(false);
		webBrowser.setLocationBarVisible(toolbarVisible);
		webBrowser.setButtonBarVisible(toolbarVisible);
		webBrowser.setStatusBarVisible(false);

		String homepage = SysConfig.getInstance().getHomepageUrl();
		if (homepage != null && !"".equals(homepage)) {
			webBrowser.navigate(homepage);
		}

		webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
			@Override
			public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
				e.getNewWebBrowser().addWebBrowserListener(
						new WebBrowserAdapter() {
							@Override
							public void locationChanging(
									WebBrowserNavigationEvent e) {
								GraphicsEnvironment ge = GraphicsEnvironment
										.getLocalGraphicsEnvironment();
								GraphicsDevice[] gs = ge.getScreenDevices();

								GraphicsDevice g = null;
								for (int i = gs.length - 1; i >= 0; i--) {
									if (gs[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
										g = gs[i];
										break;
									}
								}
								Rectangle rect = g.getDefaultConfiguration()
										.getBounds();

								Window wnd = (Window) e.getWebBrowser().getWebBrowserWindow();

								if (!rect.contains(wnd.getX(), wnd.getY())) {
									wnd.setBounds(rect);
								}
								e.getWebBrowser().setBarsVisible(false);
								Point mousepoint = MouseInfo.getPointerInfo()
										.getLocation();
								if (!rect.contains(mousepoint)) {
									try {
										Robot robot = new Robot();
										robot.mouseMove(wnd.getX() + wnd.getWidth() * 2 / 3, wnd.getY() + wnd.getHeight() * 2 / 3);
										//新开窗口并将鼠标移动后，增加一个定时器，1秒后让鼠标点击一次新开的窗口
										Timer timer = new Timer(); 
										timer.schedule(new TimerTask() { 
											public void run() {
												Robot robot;
												try {
													robot = new Robot();
													robot.mousePress(InputEvent.BUTTON1_MASK);
													robot.mouseRelease(InputEvent.BUTTON1_MASK);
												} catch (AWTException e) {
													// TODO 自动生成的 catch 块
													e.printStackTrace();
												}
											}
										}, 1000);
									} catch (AWTException e1) {
										e1.printStackTrace();
									}
								}
							}
							@Override
							public void commandReceived(WebBrowserCommandEvent e) {
								switch(e.getCommand()) {
								case CMD_BACK_MAIN_FRAME: // 将鼠标焦点转移会主窗口
									Rectangle rect = MainFrame.this.getBounds();
									Object[] params = e.getParameters();
									try {
										Robot robot = new Robot();
										robot.mouseMove((int)(rect.getX() + rect.getWidth() * 2/ 3), (int)(rect.getY() + rect.getHeight() * 2 /3));
										robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
										
										if (params.length > 0) {
											String callback = params[0].toString();
											if (!"".equals(callback)) {
												webBrowser.executeJavascript(callback);
											}
										}
									} catch (AWTException e1) {
										e1.printStackTrace();
									}
									break;
								}
							}
						});
				// 正常的电脑是不会再次跑到这个地方来的，只有异常的电脑才会再次光临，因此可以直接写死这一块
				// e.consume();
				if (newWebBrowser != null) {
					newWebBrowser.getWebBrowserWindow().dispose();
				}
				newWebBrowser = e.getNewWebBrowser();
			}
			
			@Override
			public void windowClosing(WebBrowserEvent e) {
				if (newWebBrowser != null) {
					newWebBrowser = null;
				}
			}
			
			public void locationChanged(WebBrowserNavigationEvent e) {
				System.gc();
			}
		});

		return webBrowser;
	}

	private JToolBar initStatusBar() {
		statusBar = new JToolBar();
		statusBar.setEnabled(false);

		statusBar.add(new JLabel("IC卡识别仪状态："));

		txtICCardStatus = new JLabel();
		statusBar.add(txtICCardStatus);
		statusBar.addSeparator();

		statusBar.add(new JLabel("IC卡卡号："));
		txtICCardNumber = new JLabel();
		txtICCardNumber.setText("                ");
		statusBar.add(txtICCardNumber);
		statusBar.addSeparator();

		statusBar.add(new JLabel("身份证识别仪状态："));
		txtIDCardStatus = new JLabel();
		statusBar.add(txtIDCardStatus);
		statusBar.addSeparator();

		statusBar.add(new JLabel("身份证号码："));
		txtIDCardNumber = new JLabel();
		txtIDCardNumber.setText("                  ");
		statusBar.add(txtIDCardNumber);
		statusBar.addSeparator();

		setICCardConnectStatus();
		setICCardNumber();

		setIDCardConnectStatus();
		setIDCardNumber();

		statusBar.setVisible(SysConfig.getInstance().getStatusbar());

		return statusBar;
	}

	public void setICCardConnectStatus() {
		System.out.println("mainframe:iccard" + DeviceStatus.ICCardConnected);

		if (!DeviceStatus.ICCardConnected) {
			txtICCardStatus.setForeground(Color.GRAY);
			txtICCardStatus.setText("未连接");
		} else {
			txtICCardStatus.setForeground(Color.RED);
			txtICCardStatus.setText("已连接");
		}
	}

	public void setICCardNumber() {
		if (DeviceStatus.ICCardNumber != null
				&& !"".equals(DeviceStatus.ICCardNumber)) {
			txtICCardNumber.setText(DeviceStatus.ICCardNumber);
		}
	}

	public void setIDCardConnectStatus() {
		System.out.println("mainframe:idcard" + DeviceStatus.IDCardConnected);
		if (!DeviceStatus.IDCardConnected) {
			txtIDCardStatus.setForeground(Color.GRAY);
			txtIDCardStatus.setText("未连接");
		} else {
			txtIDCardStatus.setForeground(Color.RED);
			txtIDCardStatus.setText("已连接");
		}
	}

	public void setIDCardNumber() {
		if (DeviceStatus.IDCardNumber != null
				&& !"".equals(DeviceStatus.IDCardNumber)) {
			txtIDCardNumber.setText(DeviceStatus.IDCardNumber);
		}
	}

	public void setICCard(String number) {
		int timestamp = Integer.valueOf((new Date().getTime() / 1000) + "");
		String token = DeviceStatus.encryStr(timestamp, "_iccard_" + number);
		setICCardNumber();
		String script = "CardMonitor.add('iccard', '" + number + "', {_time:"
				+ timestamp + ",_valid_code:'" + token + "'})";
		webBrowser.executeJavascript(script);
	}

	public void setIDCard(HashMap<String, String> info) {
		int timestamp = Integer.valueOf((new Date().getTime() / 1000) + "");
		String code = info.get("code");
		String token = DeviceStatus.encryStr(timestamp,
				"_idcard_" + info.get("code"));

		setIDCardNumber();

		String script = "CardMonitor.add('idcard', '" + code + "', {";
		for (String key : info.keySet()) {
			script += key + ":'" + info.get(key) + "',";
		}
		script += "_time:" + timestamp + ",_valid_code:'" + token + "'})";
		webBrowser.executeJavascript(script);
	}

}
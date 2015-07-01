package cn.com.jiehun.xianchang.ui;

import javax.swing.JDialog;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	public void Init() {
		this.setSize(500, 400);
		this.setTitle("关于中国婚博会现场系统");
		JLabel label = new JLabel();
		
		label = new JLabel();
		label.setText("<html><div style=\"padding:10px;\">"
				+ "<h1 style=\"margin:0 0 50px;text-align:center;font-size:24px;font-weight:bold;\">关于中国婚博会现场系统</h1>"
				+ "<p style=\"font-size:14px;\">该系统集成了IC卡识别仪和身份证识别仪两种系统于一体，内置一个web浏览器，将识别到卡号信息通过js接口传递给浏览器，从而实现数据的读取。通过这样的理念，也成功地实现识别器和实际业务的分离，任何基于该类卡片识别的系统都可以集成到这款软件上。 </p>"
				+ "<p style=\"text-align:right;margin-top:40px;\">"
				+ "作者：Ronnie Deng<br/>"
				+ "邮箱:comdeng@gmail.com"
				+ "</p>"
				+ "</div></html>");
		this.add(label);
	}
}
package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.ResourceBundleControl;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Notification implements Streamable {
	protected static final Logger logger = LoggerFactory.getLogger(Streamable.class);
	private static final ResourceBundle codeResourceBundle = getBundle("org.fastcatsearch.notification.message.FastcatSearchNotificationCode");
	private static final ResourceBundle formatResourceBundle = getBundle("org.fastcatsearch.notification.message.FastcatSearchNotificationFormat");

	private static Map<String, String> codeDefinitionMap = new TreeMap<String, String>();
	static {
		Enumeration<String> e = codeResourceBundle.getKeys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			String value = codeResourceBundle.getString(key);
			codeDefinitionMap.put(key, value);

		}
	}
	// 알림 발생 노드.
	protected Node origin;
	// 리소스 파일에 정의된 알림코드.
	protected String messageCode;
	protected long time;

	public Notification() {
	}

	public Notification(String messageCode) {
		this.messageCode = messageCode;
		this.origin = ServiceManager.getInstance().getService(NodeService.class).getMyNode();
		this.time = System.currentTimeMillis();
	}

	public Node origin() {
		return origin;
	}

	public String messageCode() {
		return messageCode;
	}

	public long time() {
		return time;
	}

	public abstract String toMessageString();

	protected String getFormattedMessage(Object... params) {
		if (formatResourceBundle != null) {
			try {
				return MessageFormat.format(formatResourceBundle.getString(messageCode), params);
			} catch (MissingResourceException e) {
				e.printStackTrace();
				return params.toString();
			}
		} else {
			return params.toString();
		}
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		origin = new Node();
		origin.readFrom(input);
		messageCode = input.readString();
		time = input.readLong();
		logger.debug("read Notification messageCode = {}, origin={}", messageCode, origin);
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		origin.writeTo(output);
		logger.debug("write Notification messageCode = {}, origin={}", messageCode, origin);
		output.writeString(messageCode);
		output.writeLong(time);
	}

	public static Map<String, String> getNotificationCodeDefinition() {
		return codeDefinitionMap;
	}
	
	
	private static ResourceBundle getBundle(String className) {
		//FIXME:리소스번들을 유연하게 사용할 수 있도록 수정요.
		//우선은 오류나지 않도록 수정.
		ResourceBundle bundle = null;
		String[] encodings = { "UTF-8", "ko_KR", "en_US" };
		for(String encoding : encodings) {
			if(bundle==null) {
				try {
				bundle = ResourceBundle.getBundle(className,
					new ResourceBundleControl(Charset.forName(encoding)));
				} catch (NullPointerException ignore) {
				} catch (MissingResourceException ignore) {
				} catch (IllegalArgumentException ignore) {
				}
			}
			if(bundle != null) { break; }
		}
		if(bundle==null) {
			bundle = ResourceBundle.getBundle(className);
		}
		
		return bundle;
	}
}

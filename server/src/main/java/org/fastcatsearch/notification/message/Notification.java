package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
	private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.fastcatsearch.notification.message.FastcatSearchNotificationCode_ko_KR", new ResourceBundleControl(Charset.forName("UTF-8")));
	
	//알림 발생 노드.
	protected Node origin;
	//리소스 파일에 정의된 알림코드.
	protected String messageCode;
	
	public Notification(){ }
	
	public Notification(String messageCode) {
		this.messageCode = messageCode;
		this.origin = ServiceManager.getInstance().getService(NodeService.class).getMyNode();
	}
	
	public abstract String toMessageString();
	
	protected String getFormattedMessage(Object... params){
		if (resourceBundle != null) {
			try{
				return MessageFormat.format(resourceBundle.getString(messageCode), params);
			}catch(MissingResourceException e){
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
		logger.debug("read Notification messageCode = {}, origin={}", messageCode, origin);
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		origin.writeTo(output);
		logger.debug("write Notification messageCode = {}, origin={}", messageCode, origin);
		output.writeString(messageCode);
	}
}

package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.ResourceBundleControl;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.service.ServiceManager;

public abstract class Notification implements Streamable {

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
		ResourceBundle resourceBundle = ResourceBundle.getBundle("org.fastcatsearch.notification.message.FastcatSearchNotificationCode_ko_KR", new ResourceBundleControl(Charset.forName("UTF-8")));
		if (resourceBundle != null) {
			try{
				return MessageFormat.format(resourceBundle.getString(messageCode), params);
			}catch(MissingResourceException e){
				e.printStackTrace();
				return params.toString();
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void readFrom(StreamInput input) throws IOException {
		origin = new Node();
		origin.readFrom(input);
		messageCode = input.readString();
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		origin.writeTo(output);
		output.writeString(messageCode);
	}
}

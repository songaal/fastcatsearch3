package org.fastcatsearch.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class EmailSender {
	
	/////////////////////////////////////
	// START OF PROPERTIES
	/////////////////////////////////////
	public static class MailProperties {
		protected Properties properties;
		protected EmailPasswordAuthenticator emailPasswordAuthenticator;

		public MailProperties() {
			properties = new Properties();
		}

		public MailProperties(Properties properties){
			this.properties = properties;
		}
		public void add(String key, String value) {
			properties.put(key, value);
		}

		public Properties properties() {
			return properties;
		}

		public Authenticator authenticator() {
			return emailPasswordAuthenticator;
		}

		public void setAuthentication(String id, String password) {
			emailPasswordAuthenticator = new EmailPasswordAuthenticator(id, password);
		}

		class EmailPasswordAuthenticator extends Authenticator {

			private String id;
			private String pw;

			public EmailPasswordAuthenticator(String id, String pw) {
				this.id = id;
				this.pw = pw;
			}

			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(id, pw);
			}
		}
	}

	public static class GmailProperties extends MailProperties {
		public GmailProperties(){
			this(false);
		}
		public GmailProperties(boolean debug) {
			properties.put("mail.smtp.port", "587");
			properties.put("mail.smtp.host", "smtp.gmail.com");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
			properties.put("mail.debug", debug);
		}
	}

	// ///////////////////////////////////
	// END OF PROPERTIES
	// ///////////////////////////////////

	private final MailProperties mailProperties;

	public EmailSender(MailProperties mailProperties) {
		this.mailProperties = mailProperties;
	}

	public void sendHTML(String fromAddress, List<String> recipientToList, String subject, String text) throws IOException {
		send(fromAddress, recipientToList, null, null, subject, text, "text/html", null);
	}

	public void sendText(String fromAddress, List<String> recipientToList, String subject, String text) throws IOException {
		send(fromAddress, recipientToList, null, null, subject, text, "text/plain", null);
	}
	
	public void send(String fromAddress, List<String> recipientToList, String subject, String text, String mimeType) throws IOException {
		send(fromAddress, recipientToList, null, null, subject, text, mimeType, null);
	}
	
	public void send(String fromAddress, List<String> recipientToList, List<String> recipientCCList, List<String> recipientBCCList, String subject, String text, String mimeType, List<File> files) throws IOException {

		Session session = Session.getInstance(mailProperties.properties(), mailProperties.authenticator());

		try {
			MimeMessage msg = new MimeMessage(session);
			MimeMultipart mmp = new MimeMultipart();
			MimeBodyPart mbp = new MimeBodyPart();
			mbp.setContent(text, mimeType+";\n\tcharset=\"UTF-8\"");
			mbp.setHeader("Content-Transfer-Encoding", "base64");
			mmp.addBodyPart(mbp);
			
			if (files != null) {
				for (int i = 0; i < files.size(); i++) {
					mbp = new MimeBodyPart();
					File addFile = (File) files.get(i);
					if (addFile.exists()) {
						FileDataSource fds = new FileDataSource(addFile);
						mbp.setDataHandler(new DataHandler(fds));
						mbp.setFileName(MimeUtility.encodeText(addFile.getName(), "UTF-8", "Q"));
						mmp.addBodyPart(mbp);
					} else {
						throw new IOException("첨부파일을 찾을 수 없습니다. " + addFile.getAbsolutePath());
					}
				}
			}
			msg.setContent(mmp);
			msg.setFrom(fromAddress);
			if(recipientToList != null){
				for (int i = 0; i < recipientToList.size(); i++) {
					msg.addRecipients(Message.RecipientType.TO, recipientToList.get(i));
				}
			}
			if(recipientCCList != null){
				for (int i = 0; i < recipientCCList.size(); i++) {
					msg.addRecipients(Message.RecipientType.CC, recipientCCList.get(i));
				}
			}
			if(recipientBCCList != null){
				for (int i = 0; i < recipientBCCList.size(); i++) {
					msg.addRecipients(Message.RecipientType.BCC, recipientBCCList.get(i));
				}
			}
			
			msg.setSubject(MimeUtility.encodeText(subject, "UTF-8", "Q"));
			msg.setSentDate(new Date());
			Transport.send(msg);
		} catch (Exception e) {
			throw new IOException("메일전송실패", e);
		}
	}
}
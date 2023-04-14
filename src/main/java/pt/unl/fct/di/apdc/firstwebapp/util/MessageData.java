package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class MessageData {
	public String messageID;
	public String username;
	public String nameOfUser;
	public String content;
	public long creationDate;

	public MessageData() {

	}

	public MessageData(String username, String nameOfUser, String content) {
		this.messageID = UUID.randomUUID().toString();
		this.username = username;
		this.nameOfUser = nameOfUser;
		this.content = content;
		this.creationDate = System.currentTimeMillis();
	}

	public boolean isMessageTooLong() {
		return content.length() > 280;
	}

}

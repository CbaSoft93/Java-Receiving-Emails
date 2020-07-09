package cl.cbasoft.jre;

import java.net.Socket;
import java.util.UUID;

import tech.blueglacier.email.Email;

public class OnMail {
	
	public boolean newConnection(Socket socket) {
		return true;
	}
	
	public boolean domain(String domain) {
		return true;
	}

	public boolean from(String from) {
		return true;
	}

	public boolean rcpt(String rcpt) {
		return true;
	}
	
	public String newEmail(Email email) {
		return UUID.randomUUID().toString();
	}
}

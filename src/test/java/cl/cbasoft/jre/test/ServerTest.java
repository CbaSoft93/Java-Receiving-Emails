package cl.cbasoft.jre.test;

import java.net.Socket;
import java.util.UUID;

import cl.cbasoft.jre.OnMail;
import cl.cbasoft.jre.SMTPServer;
import tech.blueglacier.email.Email;

public class ServerTest {

	public static void main(String[] args) throws Exception {
		while (true) {
			try {
				SMTPServer.Start(
					new OnMail() {
						
						@Override
						public boolean newConnection(Socket socket) {
							System.out.println(socket.getRemoteSocketAddress());
							return true;
						}
						
						@Override
						public boolean domain(String domain) {
							System.out.println(domain);
							return true;
						}
						
						@Override
						public boolean from(String from) {
							System.out.println(from);
							return true;
						}
						
						@Override
						public boolean rcpt(String rcpt) {
							System.out.println(rcpt);
							return true;
						}
						
						@Override
						public String newEmail(Email email) {
							System.out.println("FROM		: " + email.getFromEmailHeaderValue());
							System.out.println("TO			: " + email.getToEmailHeaderValue());
							System.out.println("SUBJECT		: " + email.getEmailSubject());
							System.out.println("ATTACHMENTS	: " + email.getAttachments().size());
							return UUID.randomUUID().toString();
						}
					}
				);
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(5000);
			}
		}
	}
}

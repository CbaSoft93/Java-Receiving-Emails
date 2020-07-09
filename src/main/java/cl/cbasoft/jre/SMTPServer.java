package cl.cbasoft.jre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SMTPServer {
		
	public static void Start(final OnMail onMail) throws IOException {
		ServerSocket server = new ServerSocket(25);
		try {
			while (true) {
				final Socket socket = server.accept();
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean permit = onMail.newConnection(socket);
						if (permit) {
							NewConnection(socket, onMail);
						}
					}
				}).start();
			}
		} finally {
			server.close();
		}
	}
	
	private static void NewConnection(Socket socket, OnMail onMail) {
		OutputStream outputStream;
		InputStream inputStream;

		BufferedWriter bufferedWriter;
		BufferedReader bufferedReader;
		try {
			outputStream = socket.getOutputStream();
			inputStream  = socket.getInputStream();

			bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			
			WriteLine(220, "READY FOR YOU", bufferedWriter);
			EHLO(onMail, bufferedReader, bufferedWriter);
			
			String nextLine = bufferedReader.readLine();
			if (!nextLine.toUpperCase().startsWith("MAIL FROM:")) {
				throw new SMTPException(501, "FROM IS NOT VALID", bufferedWriter);
			}
			
			boolean isFromValid = onMail.from(nextLine);
			if (!isFromValid) {
				throw new SMTPException(501, "FROM IS NOT VALID", bufferedWriter);
			}
			SMTPServer.WriteLine(250, "OK MY FRIEND", bufferedWriter);
			SMTPInput.New(onMail, bufferedReader, bufferedWriter);
			
		} catch (Exception ex) {
			if (ex instanceof SMTPException) {
				WriteSTMPException(ex);
			} else {
				ex.printStackTrace();			
			}
		} finally {
			CloseSocket(socket);
		}
	}
	
	private static void EHLO(OnMail onMail, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
		String ehlo  = bufferedReader.readLine();
		boolean isOK = true;
		
		if (ehlo == null || ehlo.isEmpty()) {
			isOK = false;
		} else {
			ehlo = ehlo.toLowerCase();
			if (!ehlo.startsWith("ehlo") && !ehlo.startsWith("helo")) {
				isOK = false;
			} else {
				String[] params = ehlo.split("\\s+");
				if (params.length < 2) {
					isOK = false;
				}

				String domain = params[1];
				isOK = onMail.domain(domain);
			}
		}
		if (isOK) {
			WriteLine(250, "OK MY FRIEND", bufferedWriter);
		} else {
			throw new SMTPException(501, "SORRY I DON'T UNDERSTAND YOU :(", bufferedWriter);
		}
	}
		
	public static void WriteLine(int code, String data, BufferedWriter bufferedWriter) throws IOException {
		bufferedWriter.write(code + " " + data);
		bufferedWriter.write("\r\n");
		bufferedWriter.flush();
	}
	
	private static void WriteSTMPException(Exception ex) {
		try {
			SMTPException smtpEx = (SMTPException) ex;
			smtpEx.write();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
	}
		
 	private static void CloseSocket(Socket socket) {
		try {
			socket.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

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

import cl.cbasoft.jre.db.MongoDB;
import cl.cbasoft.jre.db.Validator;

public class SMTPServer {

	public static void main(String[] args) throws Exception {
		StartDBConnecion();
		
		while (true) {
			try {
				StartServer();
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(5000);
			}
		}
	}
	
	private static void StartDBConnecion() throws Exception {
		MongoDB.MONGO_URI = System.getenv("MONGO_URI");
		if (MongoDB.MONGO_URI == null)
			throw new Exception("MONGO_URI IS NULL");
	}
	
	private static void StartServer() throws IOException {
		ServerSocket server = new ServerSocket(25);
		try {
			while (true) {
				final Socket socket = server.accept();
				new Thread(new Runnable() {
					@Override
					public void run() {
						NewConnection(socket);
					}
				}).start();
			}
		} finally {
			server.close();
		}
	}
	
	private static void NewConnection(Socket socket) {
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
			EHLO(bufferedReader, bufferedWriter);
			
			String nextLine = bufferedReader.readLine();
			if (!nextLine.toUpperCase().startsWith("MAIL FROM:")) {
				throw new SMTPException(501, "FROM IS NOT VALID", bufferedWriter);
			}
			
			boolean isFromValid = Validator.ValidateFrom(nextLine);
			if (!isFromValid) {
				throw new SMTPException(501, "FROM IS NOT VALID", bufferedWriter);
			}
			SMTPServer.WriteLine(250, "OK MY FRIEND", bufferedWriter);
			SMTPInput.New(bufferedReader, bufferedWriter);
			
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
	
	private static void EHLO(BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
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
				isOK = Validator.ValidateDomain(domain);
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

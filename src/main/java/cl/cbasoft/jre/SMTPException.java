package cl.cbasoft.jre;
import java.io.BufferedWriter;
import java.io.IOException;

public class SMTPException extends RuntimeException {

	private static final long serialVersionUID = 6593L;

	private int code;
	private String menssage;
	private BufferedWriter bufferedWriter;
	
	public SMTPException(int code, String message, BufferedWriter bufferedWriter) {
		this.code = code;
		this.menssage = message;
		this.bufferedWriter = bufferedWriter;
	}
	
	public void write() throws IOException {
		bufferedWriter.write(code + " " + menssage);
		bufferedWriter.write("\r\n");
		bufferedWriter.flush();
	}
	
	@Override
	public String getMessage() {
		return menssage + " (" + code + ")";
	}
}

package cl.cbasoft.jre;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.bson.types.ObjectId;

import cl.cbasoft.jre.db.Validator;
import cl.cbasoft.jre.db.entity.DBEmail;
import tech.blueglacier.email.Email;
import tech.blueglacier.parser.CustomContentHandler;

public class SMTPInput {

	public BufferedReader bufferedReader;
	public BufferedWriter bufferedWriter;
	
	public static void New(BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
		new SMTPInput(bufferedReader, bufferedWriter);
	}
	
	private SMTPInput(BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
		this.bufferedReader = bufferedReader;
		this.bufferedWriter = bufferedWriter;
		listen();
	}
	
	private void listen() throws IOException {
		String commandLine;
		while ((commandLine = bufferedReader.readLine()) != null) {
			commandLine = commandLine.toUpperCase();
			
			if (commandLine.startsWith("RCPT TO:")) {
				RCPT(commandLine);
			} else if (commandLine.equals("DATA")) {
				DATA();
			} else if (commandLine.equals("QUIT")) {
				QUIT();
				break;
			} else {
				throw new SMTPException(501, "SORRY I DON'T UNDERSTAND YOU :(", bufferedWriter);
			}
		}
	}
	
	private void RCPT(String rcpt) throws IOException {
		boolean ok  = Validator.ValidateRcpt(rcpt);
		if (ok) {
			SMTPServer.WriteLine(250, "OK MY FRIEND", bufferedWriter);
		} else {
			throw new SMTPException(501, "RCPT IS NOT VALID", bufferedWriter);
		}
	}
	
	private void DATA() throws IOException {
		SMTPServer.WriteLine(354, "End data with <CR><LF>.<CR><LF>", bufferedWriter);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] endsBytes = new byte[5];
		while (true) {
			byte newByte = (byte) bufferedReader.read();

			endsBytes[0] = endsBytes[1];
			endsBytes[1] = endsBytes[2];
			endsBytes[2] = endsBytes[3];
			endsBytes[3] = endsBytes[4];
			endsBytes[4] = newByte;
			
			byte b1 = endsBytes[0];
			byte b2 = endsBytes[1];
			byte b3 = endsBytes[2];
			byte b4 = endsBytes[3];
			byte b5 = endsBytes[4];

			baos.write(newByte);
			if (b1 == 13 && b2 == 10 && b3 == 46 && b4 == 13 && b5 == 10) {
				break;
			}
		}
		baos.close();
		
		CustomContentHandler contentHandler 		= new CustomContentHandler();
		MimeConfig mime4jParserConfig 				= MimeConfig.DEFAULT;
		BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();		
		MimeStreamParser mime4jParser 				= new MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder);
		mime4jParser.setContentDecoding(true);
		mime4jParser.setContentHandler(contentHandler);
		
		try {
			mime4jParser.parse(baos.toInputStream());
			Email email = contentHandler.getEmail();
			ObjectId emailId = DBEmail.Storage(email);
			
			SMTPServer.WriteLine(250, "OK ID: " + emailId, bufferedWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new SMTPException(501, "SORRY I DON'T UNDERSTAND YOU :(", bufferedWriter);
		}
	}

	private void QUIT() throws IOException {
		SMTPServer.WriteLine(221, "Good bye and have a good trip", bufferedWriter);
	}
}

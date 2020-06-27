package cl.cbasoft.jre.test;

import java.net.URL;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.bson.types.ObjectId;

import cl.cbasoft.jre.db.MongoDB;
import cl.cbasoft.jre.db.entity.DBEmail;
import tech.blueglacier.parser.CustomContentHandler;

public class EmailStorageTest {

	public static void main(String[] args) throws Exception {
		MongoDB.MONGO_URI = System.getenv("MONGO_URI");
		System.out.println(MongoDB.MONGO_URI);
		
		URL url = new URL("https://raw.githubusercontent.com/ram-sharma-6453/email-mime-parser/master/src/test/resources/emailWithAttachedEmails.eml");
		
		CustomContentHandler contentHandler 		= new CustomContentHandler();
		MimeConfig mime4jParserConfig 				= MimeConfig.DEFAULT;
		BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();		
		MimeStreamParser mime4jParser 				= new MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder);
		mime4jParser.setContentDecoding(true);
		mime4jParser.setContentHandler(contentHandler);
		
		mime4jParser.parse(url.openStream());
		ObjectId idEmail = DBEmail.Storage(contentHandler.getEmail());
		System.out.println("OK ID: " + idEmail);
	}
	
}

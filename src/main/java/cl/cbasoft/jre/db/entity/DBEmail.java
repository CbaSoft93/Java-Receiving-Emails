package cl.cbasoft.jre.db.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;

import cl.cbasoft.jre.db.MongoDB;
import tech.blueglacier.email.Attachment;
import tech.blueglacier.email.Email;

public class DBEmail {

	private static final MongoCollection<DBEmail> COLLECTION = MongoDB.Collection("Emails", DBEmail.class);
	
	@BsonId
	public ObjectId 	_id;
	public boolean 		read;
	public Date	  		date;
	public String 		from;
	public List<String>	to;
	public String 		subject;
	public int 			attachmentsCount;
	public byte[] 		plainTextBody;
	public byte[]		htmlBody;

	public List<DBAttachment> attachments;
	
	public DBEmail() { }
	
	public DBEmail(Email email) throws IOException {
		this.read 	 = false;
		this.date 	 = new Date();
		this.from 	 = email.getFromEmailHeaderValue();
		this.to	  	 = Arrays.asList(email.getToEmailHeaderValue().split(","));
		this.subject = email.getEmailSubject();
		this.attachmentsCount = email.getAttachments().size();
		
		for (int i = this.to.size() -1 ; i >= 0; i--) {
			this.to.set(
				i, 
				this.to.get(i).trim()
			);
		}
		
		Attachment plainText = email.getPlainTextEmailBody();
		if (plainText != null) {
			this.plainTextBody = MongoDB.ToByte(plainText.getIs());
		}
		
		Attachment html = email.getHTMLEmailBody();
		if (html != null) {
			this.htmlBody = MongoDB.ToByte(html.getIs());
		}
		
		if (email.getAttachments() != null) {
			this.attachments = new ArrayList<>();
			
			for (Attachment attachment : email.getAttachments()) {
				DBAttachment dbAttachment = new DBAttachment(attachment);
				this.attachments.add(dbAttachment);
			}
		}
	}
	
	public static ObjectId Storage(Email email) throws IOException {
		DBEmail dbEntity = new DBEmail(email);
		COLLECTION.insertOne(dbEntity);
		return dbEntity._id;
	}
}

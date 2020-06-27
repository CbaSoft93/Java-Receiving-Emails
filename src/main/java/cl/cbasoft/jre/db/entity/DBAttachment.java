package cl.cbasoft.jre.db.entity;

import java.io.IOException;

import org.bson.types.ObjectId;

import cl.cbasoft.jre.db.MongoDB;
import tech.blueglacier.email.Attachment;

public class DBAttachment {
	
	public ObjectId		idAttachment;
	public String 		name;
	public long 		size;
	public byte[] 		data;

	public DBAttachment(Attachment attachment) throws IOException {
		this.idAttachment = new ObjectId();
		
		this.name = attachment.getAttachmentName();
		this.size = attachment.getAttachmentSize();
		this.data = MongoDB.ToByte(attachment.getIs());
	}
	
}

package cl.cbasoft.jre.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDB {

	public static String MONGO_URI;
	private static MongoClient CLIENT;
	private static MongoDatabase DB;
	
	private synchronized static MongoClient Start() {
		if (CLIENT != null)
			return CLIENT;
		
		List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
		conventions.add(new NullConvention());
		
		CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
			PojoCodecProvider.builder().conventions(conventions).automatic(true).build()
		);
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
			MongoClientSettings.getDefaultCodecRegistry(), 
            pojoCodecRegistry
        );		

		ConnectionString connectionString  = new ConnectionString(MONGO_URI);
		MongoClientSettings clientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .codecRegistry(codecRegistry)
            .build();
		  
		CLIENT = MongoClients.create(clientSettings);
		DB = CLIENT.getDatabase(connectionString.getDatabase());
		return CLIENT;
	}
	
	public synchronized static MongoDatabase DB() {
		if (DB == null)
			Start();
		return DB;
	}
	
	public static MongoCollection<Document> Collection(String name) {
		return DB().getCollection(name);
	}
	
	public static <T> MongoCollection<T> Collection(String name, Class<T> clazz) {
		return DB().getCollection(name, clazz);
	}	

	public static ClientSession StartSession() {
		return Start().startSession();
	}
	
	public static byte[] ToByte(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (inputStream.read(buffer) != -1) {
			baos.write(buffer);
		}
		baos.flush();
		return baos.toByteArray();
	}
}

package cl.cbasoft.jre.db;

import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;

class NullConvention implements Convention {
	
	@Override
	public void apply(ClassModelBuilder<?> classModelBuilder) {
		classModelBuilder.getPropertyModelBuilders().forEach(pmb -> {
			pmb.propertySerialization(value -> true);
		});
	}

}

package openml.xstream;

import openml.xml.Authenticate;
import openml.xml.DataSetDescription;
import openml.xml.ApiError;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XstreamXmlMapping {

	public static XStream getInstance() {
		XStream xstream = new XStream(new DomDriver("UFT-8", new NoNameCoder()));
		
		xstream.alias("oml:data_set_description", DataSetDescription.class);
		xstream.aliasAttribute(DataSetDescription.class, "oml", "xmlns:oml");
		xstream.aliasField("oml:id", DataSetDescription.class, "id");
		xstream.aliasField("oml:name", DataSetDescription.class, "name");
		xstream.aliasField("oml:version", DataSetDescription.class, "version");
		xstream.aliasField("oml:description", DataSetDescription.class, "description");
		xstream.aliasField("oml:format", DataSetDescription.class, "format");
		xstream.aliasField("oml:upload_date", DataSetDescription.class, "upload_date");
		xstream.aliasField("oml:licence", DataSetDescription.class, "licence");
		xstream.aliasField("oml:url", DataSetDescription.class, "url");
		xstream.aliasField("oml:md5_checksum", DataSetDescription.class, "md5_checksum");
		
		xstream.alias("oml:error", ApiError.class);
		xstream.aliasAttribute(ApiError.class, "oml", "xmlns:oml");
		xstream.aliasField("oml:code", ApiError.class, "code");
		xstream.aliasField("oml:message", ApiError.class, "message");
		xstream.aliasField("oml:additional_information", ApiError.class, "additional_information");
		
		xstream.alias("oml:authenticate", Authenticate.class);
		xstream.aliasField("oml:session_hash", Authenticate.class, "sessionHash");
		xstream.aliasField("oml:valid_until", Authenticate.class, "validUntil");
		
		
		return xstream;
	}
}

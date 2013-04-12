package openml.xml;

public class ApiError {
	private final String oml = "http://open-ml.org/openml";
	
	private String code;
	private String message;
	private String additional_information;
	
	public String getOml() {
		return oml;
	}
	public String getCode() {
		return code;
	}
	public String getMessage() {
		return message;
	}
	public String getAdditional_information() {
		return additional_information;
	}
}

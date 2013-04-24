package openml.xml;

import openml.constants.Constants;

public class Implementation {
	private final String oml = Constants.OPENML_XMLNS;
	
	private String name;
	private String version;
	private String description;
	private String[] creator;
	private String[] contributor;
	private String licence;
	private String language;
	private String full_description;
	private String installation_notes;
	private String dependencies;
	private Bibliographical_reference[] bibliographical_reference;
	private Parameter parameter;
	private String source_format;
	private String binary_format;
	private String source_md5;
	private String binary_md5;
	
	public Implementation( String name, String version, String description ) {
		this.name = name;
		this.version = version;
		this.description = description;
	}
	
	public Implementation(String name, String version, String description,
			String[] creator, String[] contributor, String licence,
			String language, String full_description,
			String installation_notes, String dependencies) {
		super();
		this.name = name;
		this.version = version;
		this.description = description;
		this.creator = creator;
		this.contributor = contributor;
		this.licence = licence;
		this.language = language;
		this.full_description = full_description;
		this.installation_notes = installation_notes;
		this.dependencies = dependencies;
	}



	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}

	public String[] getCreator() {
		return creator;
	}

	public String[] getContributor() {
		return contributor;
	}

	public String getLicence() {
		return licence;
	}

	public String getLanguage() {
		return language;
	}

	public String getFull_description() {
		return full_description;
	}

	public String getInstallation_notes() {
		return installation_notes;
	}

	public String getDependencies() {
		return dependencies;
	}

	public Bibliographical_reference[] getBibliographical_reference() {
		return bibliographical_reference;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public String getSource_format() {
		return source_format;
	}

	public String getBinary_format() {
		return binary_format;
	}

	public String getSource_md5() {
		return source_md5;
	}

	public String getBinary_md5() {
		return binary_md5;
	}

	public String getOml() {
		return oml;
	}

	public class Bibliographical_reference {
		private String citation;
		private String url;
		
		public String getCitation() {
			return citation;
		}
		public String getUrl() {
			return url;
		}
	}
	
	public class Parameter {
		private String name;
		private String data_type;
		private String default_value;
		private String description;
		
		public String getName() {
			return name;
		}
		public String getData_type() {
			return data_type;
		}
		public String getDefault_value() {
			return default_value;
		}
		public String getDescription() {
			return description;
		}
	}
}

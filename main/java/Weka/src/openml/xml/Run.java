package openml.xml;

import openml.constants.Constants;

public class Run {

	private final String oml = Constants.OPENML_XMLNS;
	private int task_id;
	private String implementation_id;
	private Parameter_setting[] parameter_settings;
	
	public Run( int task_id, String implementation_id, String parameters ) {
		this.task_id = task_id;
		this.implementation_id = implementation_id;
		
		// TODO: We must do something better than this. 
		System.out.println("receiving: " + parameters);
		String[] parts = parameters.split(" ");
		parameter_settings = new Parameter_setting[parts.length/2];
		for(int i = 1; i < parts.length; ++i) {
			parameter_settings[i/2] = new Parameter_setting(parts[i-1], parts[i], implementation_id);
		}
	}
	
	public String getOml() {
		return oml;
	}

	public int getTask_id() {
		return task_id;
	}

	public String getImplementation_id() {
		return implementation_id;
	}

	public static class Parameter_setting {
		private String name;
		private String value;
		private String component;
		
		public Parameter_setting(String name, String value, String component) {
			this.name = name;
			this.component = component;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		public String getComponent() {
			return component;
		}
		public String getValue() {
			return value;
		}
	}
}

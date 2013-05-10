package openml.xml;

import openml.constants.Constants;
import openml.io.ApiConnector;

public class Task {
	private final String oml = Constants.OPENML_XMLNS;
	
	private Integer task_id;
	private String task_type;
	private Input[] inputs;
	private Output[] outputs;
	
	public String getOml() {
		return oml;
	}

	public Integer getTask_id() {
		return task_id;
	}

	public String getTask_type() {
		return task_type;
	}

	public Input[] getInputs() {
		return inputs;
	}

	public Output[] getOutputs() {
		return outputs;
	}

	public class Input {
		private String name;
		private Data_set data_set;
		private Estimation_procedure estimation_procedure;
		private Evaluation_measures evaluation_measures;
		
		public String getName() {
			return name;
		}

		public Data_set getData_set() {
			return data_set;
		}
		
		public Estimation_procedure getEstimation_procedure() {
			return estimation_procedure;
		}

		public Evaluation_measures getEvaluation_measures() {
			return evaluation_measures;
		}
		
		public class Data_set {
			private Integer data_set_id;
			private String target_feature;
			private DataSetDescription dsdCache;
			
			public Integer getData_set_id() {
				return data_set_id;
			}
			public String getTarget_feature() {
				return target_feature;
			}
			public DataSetDescription getDataSetDescription() throws Exception {
				if(dsdCache == null) {
					dsdCache = ApiConnector.openmlDataDescription(data_set_id);
				}
				return dsdCache;
			}
		}
		
		public class Estimation_procedure {
			private String type;
			private String data_splits_url;
			private Parameter[] parameters;
			
			public String getType() {
				return type;
			}

			public String getData_splits_url() {
				return data_splits_url;
			}

			public Parameter[] getParameters() {
				return parameters;
			}

			public class Parameter {
				private String name;
				private String value;
				
				public String getName() {
					return name;
				}
				public String getValue() {
					return value;
				}
			}
		}
		
		public class Evaluation_measures {
			private String[] evaluation_measure;

			public String[] getEvaluation_measure() {
				return evaluation_measure;
			}
		}
	}
	
	public class Output {
		private String name;
		private Predictions predictions;
		
		public String getName() {
			return name;
		}

		public Predictions getPredictions() {
			return predictions;
		}
		
		public class Predictions {
			private String format;
			private Feature[] features;
			
			public String getFormat() {
				return format;
			}

			public Feature[] getFeatures() {
				return features;
			}

			public class Feature {
				private String name;
				private String type;
				
				public String getName() {
					return name;
				}
				public String getType() {
					return type;
				}
			}
		}
	}
}

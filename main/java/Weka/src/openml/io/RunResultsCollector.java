package openml.io;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import openml.algorithms.TaskInformation;
import openml.xml.Run;
import openml.xml.Task;
import openml.xml.Task.Output.Predictions.Feature;

import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class RunResultsCollector extends Observable implements Serializable {

	private static final long serialVersionUID = 4789455211L;
	
	private final Map<String, OpenmlExecutedTask> currentlyCollecting;

	public RunResultsCollector() {
		currentlyCollecting = new HashMap<String, OpenmlExecutedTask>();
	}

	public void acceptResults(Task t, Integer repeat, Integer fold,
			String implementation, String version, String options, Integer[] rowids,
			FastVector predictions) throws Exception {
		String implementationId = implementation + "(" + version + ")";
		String key = t.getTask_id() + "_" + implementationId + "_" + options;
		if( currentlyCollecting.containsKey(key) == false)
			currentlyCollecting.put(key, new OpenmlExecutedTask(t, implementationId, options) );
		currentlyCollecting.get(key).addBatch(fold, repeat, rowids, predictions);
		
		if(currentlyCollecting.get(key).complete()) {
			notifyObservers(currentlyCollecting.get(key));
			currentlyCollecting.remove(key);
		}
	}

	public class OpenmlExecutedTask {

		private Instances predictions;
		private int nrOfResultBatches;
		private final int nrOfExpectedResultBatches;
		private String[] classnames;
		private Run run;

		public OpenmlExecutedTask(Task t, String implementation, String options) throws Exception {
			classnames = TaskInformation.getClassNames(t);
			nrOfExpectedResultBatches = TaskInformation.getNumberOfFolds(t)
					* TaskInformation.getNumberOfRepeats(t);
			nrOfResultBatches = 0;
			FastVector attInfo = new FastVector();
			for (Feature f : TaskInformation.getPredictions(t).getFeatures()) {
				if (f.getName().equals("confidence.classname")) {
					for (String s : TaskInformation.getClassNames(t)) {
						attInfo.addElement(new Attribute("confidence." + s));
					}
				} else if (f.getName().equals("prediction")) {
					FastVector values = new FastVector(classnames.length);
					for (String classname : classnames)
						values.addElement(classname);
					attInfo.addElement(new Attribute(f.getName(), values));
				} else {
					attInfo.addElement(new Attribute(f.getName()));
				}
			}
			predictions = new Instances("openml_task_" + t.getTask_id()
					+ "_predictions", attInfo, 0);
			
			run = new Run(t.getTask_id(), implementation, options);
		}

		public void addBatch(int fold, int repeat, Integer[] rowids,
				FastVector batchPredictions) {
			nrOfResultBatches += 1;
			for (int i = 0; i < rowids.length; ++i) {
				Prediction current = (Prediction) batchPredictions.elementAt(i);
				double[] values = new double[predictions.numAttributes()];
				values[predictions.attribute("row_id").index()] = rowids[i];
				values[predictions.attribute("fold").index()] = fold;
				values[predictions.attribute("repeat").index()] = repeat;
				values[predictions.attribute("prediction").index()] = current.predicted();
				if(current instanceof NominalPrediction) {
					double[] confidences = ((NominalPrediction) current).distribution();
					for(int j = 0; j < confidences.length; ++j) {
						values[predictions.attribute("confidence."+classnames[j]).index()] = confidences[j];
					}
				}
				
				predictions.add(new Instance(1.0D, values));
			}
		}
		
		public Run getRun() {
			return run;
		}
		
		public Instances getPredictions() {
			return predictions;
		}

		public boolean complete() {
			return nrOfResultBatches == nrOfExpectedResultBatches;
		}

	}
}

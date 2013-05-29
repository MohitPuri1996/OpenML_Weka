package openml.experiment;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

import openml.algorithms.Conversion;
import openml.algorithms.TaskInformation;
import openml.io.ApiConnector;
import openml.io.ApiSessionHash;
import openml.xml.Run;
import openml.xml.Task;
import openml.xml.Task.Output.Predictions.Feature;
import openml.xstream.XstreamXmlMapping;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstancesResultListener;

public class TaskResultListener extends InstancesResultListener {

	private static final long serialVersionUID = 7230120341L;

	/** List of OpenML tasks currently being solved. Folds/repeats are gathered */
	private final Map<String, OpenmlExecutedTask> currentlyCollecting;

	/**
	 * List of OpenML tasks that reported back with errors. Will be send to
	 * server with error message
	 */
	private final ArrayList<String> tasksWithErrors;

	/** Credentials for sending results to server */
	private ApiSessionHash ash;

	/** boolean checking whether correct credentials have been stored */
	private boolean credentials = false;

	public TaskResultListener() {
		super();

		currentlyCollecting = new HashMap<String, OpenmlExecutedTask>();
		tasksWithErrors = new ArrayList<String>();
		ash = null;
	}

	public boolean acceptCredentials(String username, String password) {
		ash = new ApiSessionHash();
		try {
			credentials = ash.set(username, password);
			return credentials;
		} catch (ParseException e) {
			e.printStackTrace();
			credentials = false;
			return false;
		}
	}

	public boolean gotCredentials() {
		return credentials;
	}

	public void acceptResultsForSending(Task t, Integer repeat, Integer fold,
			String implementation, String version, String options,
			Integer[] rowids, FastVector predictions) throws Exception {
		String implementationId = implementation + "(" + version + ")";
		String key = t.getTask_id() + "_" + implementationId + "_" + options;
		if (currentlyCollecting.containsKey(key) == false)
			currentlyCollecting.put(key, new OpenmlExecutedTask(t,
					implementationId, null, options));
		currentlyCollecting.get(key)
				.addBatch(fold, repeat, rowids, predictions);

		if (currentlyCollecting.get(key).complete()) {
			sendTask(currentlyCollecting.get(key));
			currentlyCollecting.remove(key);
		}
	}

	public void acceptErrorResult(Task t, String implementation,
			String version, String error_message, String options)
			throws Exception {
		String implementationId = implementation + "(" + version + ")";
		String key = t.getTask_id() + "_" + implementationId + "_" + options;

		if (tasksWithErrors.contains(key) == false) {
			tasksWithErrors.add(key);
			sendTaskWithError(new OpenmlExecutedTask(t, implementationId,
					error_message, options));
		}
	}

	private void sendTask(OpenmlExecutedTask oet) throws Exception {
		XStream xstream = XstreamXmlMapping.getInstance();
		File tmpPredictionsFile;
		File tmpDescriptionFile;
		
		ashUpdate();
		
		tmpPredictionsFile = Conversion.instancesToTempFile(
				oet.getPredictions(), "weka_generated_predictions");
		tmpDescriptionFile = Conversion.stringToTempFile(
				xstream.toXML(oet.getRun()), "weka_generated_run");
		Map<String, File> output_files = new HashMap<String, File>();
		output_files.put("predictions", tmpPredictionsFile);
		ApiConnector.openmlRunUpload(tmpDescriptionFile, output_files,
				ash.getSessionHash());

	}

	private void sendTaskWithError(OpenmlExecutedTask oet) throws Exception {
		XStream xstream = XstreamXmlMapping.getInstance();
		File tmpDescriptionFile;
		
		ashUpdate();
		
		tmpDescriptionFile = Conversion.stringToTempFile(
				xstream.toXML(oet.getRun()), "weka_generated_run");
		ApiConnector.openmlRunUpload(tmpDescriptionFile, new HashMap<String, File>(), ash.getSessionHash());
	}

	private void ashUpdate() throws Exception {
		if (ash == null)
			throw new Exception("No credentials provided yet. ");
		if (ash.isValid() == false)
			ash.update();
		if (ash.isValid() == false)
			throw new Exception("Credentials not valid. ");
	}

	private class OpenmlExecutedTask {

		private Instances predictions;
		private int nrOfResultBatches;
		private final int nrOfExpectedResultBatches;
		private String[] classnames;
		private Run run;

		public OpenmlExecutedTask(Task t, String implementation,
				String error_message, String options) throws Exception {
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
			
			run = new Run(t.getTask_id(), implementation, error_message,
					options);
		}

		public void addBatch(int fold, int repeat, Integer[] rowids,
				FastVector batchPredictions) {
			nrOfResultBatches += 1;
			for (int i = 0; i < rowids.length; ++i) {
				Prediction current = (Prediction) batchPredictions.elementAt(i);
				double[] values = new double[predictions.numAttributes()];
				values[predictions.attribute("row_id").index()] = rowids[i];
				values[predictions.attribute("fold").index()] = fold;
				values[predictions.attribute("repeat").index()] = repeat - 1; // 1-based
																				// =>
																				// 0-based
				values[predictions.attribute("prediction").index()] = current
						.predicted();
				if (current instanceof NominalPrediction) {
					double[] confidences = ((NominalPrediction) current)
							.distribution();
					for (int j = 0; j < confidences.length; ++j) {
						values[predictions.attribute(
								"confidence." + classnames[j]).index()] = confidences[j];
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

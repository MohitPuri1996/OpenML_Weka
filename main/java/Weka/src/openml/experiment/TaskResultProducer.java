package openml.experiment;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import openml.algorithms.InstancesHelper;
import openml.algorithms.TaskInformation;
import openml.io.ApiConnector;
import openml.io.ApiSessionHash;
import openml.io.RunResultsCollector;
import openml.io.RunResultsSubmitter;
import openml.xml.DataSetDescription;
import openml.xml.Task;
import openml.xml.Task.Input.Data_set;
import openml.xml.Task.Input.Estimation_procedure;

import weka.core.Instances;
import weka.core.Utils;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.OutputZipper;

public class TaskResultProducer extends CrossValidationResultProducer {

	private static final long serialVersionUID = 1L;

	private static final String FOLDS_FILE_TRAIN = "TRAIN";
	private static final String FOLDS_FILE_TEST = "TEST";
	
	public static final String TASK_FIELD_NAME = "OpenML_Task_id";

	/** The task to be run */
	protected Task m_Task;

	/** Instances file with splits in it **/
	protected Instances m_Splits;
	
	/** Collecting results before sending to server */
	protected RunResultsCollector m_ResultsCollector;
	

	/** Sending results to server */
	protected RunResultsSubmitter m_ResultsSubmitter;
	
	/** Credentials for sending results to server */
	private ApiSessionHash ash;
	
	public boolean acceptCredentials(String username, String password) {
		ash = new ApiSessionHash();
		try {
			boolean succes = ash.set(username, password);
			if(succes)
				m_ResultsSubmitter.acceptSessionHash(ash);
			return succes;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	public TaskResultProducer() {
		super();
		
		m_SplitEvaluator = new TaskSplitEvaluator();
		m_ResultsSubmitter = new RunResultsSubmitter();
		m_ResultsCollector = new RunResultsCollector(m_ResultsSubmitter);
	}

	public void setTask(Task t) throws Exception {
		m_Task = t;

		Data_set ds = TaskInformation.getSourceData(m_Task);
		Estimation_procedure ep = TaskInformation
				.getEstimationProcedure(m_Task);

		DataSetDescription dsd = ds.getDataSetDescription();
		m_Instances = dsd.getDataset();
		InstancesHelper.setTargetAttribute(m_Instances, ds.getTarget_feature());
		m_Splits = ApiConnector.getDatasetFromUrl(ep.getData_splits_url());
	}

	/**
	 * Gets the names of each of the columns produced for a single run. This
	 * method should really be static.
	 * 
	 * @return an array containing the name of each column
	 */
	@Override
	public String[] getKeyNames() {

		String[] keyNames = m_SplitEvaluator.getKeyNames();
		// Add in the names of our extra key fields
		String[] newKeyNames = new String[keyNames.length + 4];
		newKeyNames[0] = DATASET_FIELD_NAME;
		newKeyNames[1] = RUN_FIELD_NAME;
		newKeyNames[2] = FOLD_FIELD_NAME;
		newKeyNames[3] = TASK_FIELD_NAME;
		System.arraycopy(keyNames, 0, newKeyNames, 4, keyNames.length);
		return newKeyNames;
	}

	/**
	 * Gets the data types of each of the columns produced for a single run.
	 * This method should really be static.
	 * 
	 * @return an array containing objects of the type of each column. The
	 *         objects should be Strings, or Doubles.
	 */
	@Override
	public Object[] getKeyTypes() {

		Object[] keyTypes = m_SplitEvaluator.getKeyTypes();
		// Add in the types of our extra fields
		Object[] newKeyTypes = new String[keyTypes.length + 4];
		newKeyTypes[0] = new String();
		newKeyTypes[1] = new String();
		newKeyTypes[2] = new String();
		newKeyTypes[3] = new String();
		System.arraycopy(keyTypes, 0, newKeyTypes, 4, keyTypes.length);
		return newKeyTypes;
	}

	@Override
	public void doRun(int run) throws Exception {
		int attTypeIndex = m_Splits.attribute("type").index();
		int attRowidIndex = m_Splits.attribute("rowid").index();
		int attFoldIndex = m_Splits.attribute("fold").index();
		int attRepeatIndex = m_Splits.attribute("repeat").index();
		
		if (getRawOutput()) {
			if (m_ZipDest == null) {
				m_ZipDest = new OutputZipper(m_OutputFile);
			}
		}

		if (m_Instances == null) {
			throw new Exception("No Instances set");
		}

		if (m_Task == null) {
			throw new Exception("No task set");
		}

		// creating al empty copies for each fold
		Instances[] trainingSets = new Instances[m_NumFolds];
		Instances[] testSets = new Instances[m_NumFolds];
		Map<Integer, Integer[]> rowids = new HashMap<Integer, Integer[]>();

		for (int i = 0; i < m_NumFolds; ++i) {
			trainingSets[i] = new Instances(m_Instances, 0, 0);
			testSets[i] = new Instances(m_Instances, 0, 0);
		}
		for (int i = 0; i < m_Splits.numInstances(); ++i) {
			int repeat = (int) m_Splits.instance(i).value(attRepeatIndex);
			if (repeat == run - 1) { // 1based - 0based correction
				int fold = (int) m_Splits.instance(i).value(attFoldIndex);
				String type = m_Splits.attribute(attTypeIndex).value(
						(int) m_Splits.instance(i).value(attTypeIndex));
				int rowid = Integer
						.parseInt(m_Splits.attribute(attRowidIndex)
								.value((int) m_Splits.instance(i).value(
										attRowidIndex)));

				if (type.equals(FOLDS_FILE_TRAIN)) {
					trainingSets[fold].add(m_Instances.instance(rowid));
				} else if (type.equals(FOLDS_FILE_TEST)) {
					testSets[fold].add(m_Instances.instance(rowid));
					if(rowids.containsKey(fold)) {
						rowids.put( fold, ArrayUtils.addAll( rowids.get(fold), rowid ) );
					} else {
						Integer[] n = {rowid};
						rowids.put( fold, n );
					}
				}
			}
		}

		for (int fold = 0; fold < m_NumFolds; fold++) {
			// Add in some fields to the key like run and fold number, dataset
			// name
			Object[] seKey = m_SplitEvaluator.getKey();
			Object[] key = new Object[seKey.length + 4];
			key[0] = Utils.backQuoteChars(m_Instances.relationName());
			key[1] = "" + run;
			key[2] = "" + (fold + 1);
			key[3] = "" + m_Task.getTask_id();
			System.arraycopy(seKey, 0, key, 4, seKey.length);
			if (m_ResultListener.isResultRequired(this, key)) {
				try {
					Object[] seResults = m_SplitEvaluator.getResult(
							trainingSets[fold], testSets[fold]);
					Object[] results = new Object[seResults.length + 1];
					results[0] = getTimestamp();
					System.arraycopy(seResults, 0, results, 1, seResults.length);

					m_ResultListener.acceptResult(this, key, results);
					// TODO: do a better check
					if(m_ResultListener instanceof TaskResultListener)
						// TODO: do better than just key[4], key[5] and key[6]
						m_ResultsCollector.acceptResults(m_Task, run, fold, (String) key[4], (String) key[5], (String) key[6], rowids.get(fold), ((TaskSplitEvaluator) m_SplitEvaluator).recentPredictions());
				} catch (Exception ex) {
					// Save the train and test datasets for debugging purposes?
					throw ex;
				}
			}
		}
	}
}

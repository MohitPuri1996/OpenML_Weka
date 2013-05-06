package openml.experiment;

import openml.algorithms.InstancesHelper;
import openml.io.ApiConnector;
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
	
	/** The task to be run */
	protected Task m_Task;
	
	/** Instances file with splits in it **/
	protected Instances m_Splits;
	
	public void setTask (Task t) throws Exception {
		m_Task = t;
		
		// TODO: pick right input
		Data_set ds = t.getInputs()[0].getData_set();
		Estimation_procedure ep = t.getInputs()[1].getEstimation_procedure();
		
		DataSetDescription dsd = ApiConnector.openmlDataDescription(ds.getData_set_id());
		m_Instances = ApiConnector.getDatasetFromUrl(dsd.getUrl());
		InstancesHelper.setTargetAttribute(m_Instances, ds.getTarget_feature());
		m_Splits = ApiConnector.getDatasetFromUrl(ep.getData_splits_url());
	}
	
	@Override
	public void doRun(int run) throws Exception {
		System.out.println("TaskResultProducer run #" + run );
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
		
		if(m_Task == null) {
			throw new Exception("No task set");
		}
		
		// creating al empty copies for each fold
		Instances[] trainingSets = new Instances[m_NumFolds];
		Instances[] testSets = new Instances[m_NumFolds];
		
		for(int i = 0; i < m_NumFolds; ++i) {
			trainingSets[i] = new Instances(m_Instances, 0, 0);
			testSets[i] = new Instances(m_Instances, 0, 0);
		}
		for(int i = 0; i < m_Splits.numInstances(); ++i) {
			int repeat = (int) m_Splits.instance(i).value(attRepeatIndex);
			if(repeat == run - 1) { // 1based - 0based correction
				int fold = (int) m_Splits.instance(i).value(attFoldIndex);
				String type = m_Splits.attribute(attTypeIndex).value((int) m_Splits.instance(i).value(attTypeIndex));
				int rowid = Integer.parseInt( m_Splits.attribute(attRowidIndex).value((int) m_Splits.instance(i).value(attRowidIndex)));
				
				if(type.equals(FOLDS_FILE_TRAIN)) {
					trainingSets[fold].add(m_Instances.instance(rowid));
				} else if(type.equals(FOLDS_FILE_TEST)) {
					testSets[fold].add(m_Instances.instance(rowid));
				}
			}
		}
		
		for (int fold = 0; fold < m_NumFolds; fold++) {
			// Add in some fields to the key like run and fold number, dataset
			// name
			Object[] seKey = m_SplitEvaluator.getKey();
			Object[] key = new Object[seKey.length + 3];
			key[0] = Utils.backQuoteChars(m_Instances.relationName());
			key[1] = "" + run;
			key[2] = "" + (fold + 1);
			System.arraycopy(seKey, 0, key, 3, seKey.length);
			if (m_ResultListener.isResultRequired(this, key)) {
				try {
					Object[] seResults = m_SplitEvaluator
							.getResult(trainingSets[fold], testSets[fold]);
					Object[] results = new Object[seResults.length + 1];
					results[0] = getTimestamp();
					System.arraycopy(seResults, 0, results, 1, seResults.length);
					
					m_ResultListener.acceptResult(this, key, results);
				} catch (Exception ex) {
					// Save the train and test datasets for debugging purposes?
					throw ex;
				}
			}
		}
	}
}

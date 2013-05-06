package openml.experiment;

import openml.xml.Task;

import weka.core.Instances;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.OutputZipper;

public class TaskResultProducer extends CrossValidationResultProducer {

	private static final long serialVersionUID = 1L;
	
	/** The task to be run */
	protected Task m_Task;
	
	@Override
	public void doRun(int run) throws Exception {
		System.out.println("TaskResultProducer run #" + run );
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
		// Randomize on a copy of the original dataset
		Instances runInstances = new Instances(m_Instances);
		
		/*for (int fold = 0; fold < m_NumFolds; fold++) {
			// Add in some fields to the key like run and fold number, dataset
			// name
			Object[] seKey = m_SplitEvaluator.getKey();
			Object[] key = new Object[seKey.length + 3];
			key[0] = Utils.backQuoteChars(m_Instances.relationName());
			key[1] = "" + run;
			key[2] = "" + (fold + 1);
			System.arraycopy(seKey, 0, key, 3, seKey.length);
			if (m_ResultListener.isResultRequired(this, key)) {
				Instances train = runInstances
						.trainCV(m_NumFolds, fold, random);
				Instances test = runInstances.testCV(m_NumFolds, fold);
				try {
					Object[] seResults = m_SplitEvaluator
							.getResult(train, test);
					Object[] results = new Object[seResults.length + 1];
					results[0] = getTimestamp();
					System.arraycopy(seResults, 0, results, 1, seResults.length);
					
					m_ResultListener.acceptResult(this, key, results);
				} catch (Exception ex) {
					// Save the train and test datasets for debugging purposes?
					throw ex;
				}
			}
		}*/
	}
}

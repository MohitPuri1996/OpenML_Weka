package openml.experiment;

import java.lang.reflect.Array;

import javax.swing.DefaultListModel;

import openml.algorithms.InstancesHelper;
import openml.io.ApiConnector;
import openml.xml.ApiError;
import openml.xml.DataSetDescription;
import openml.xml.Task;
import openml.xml.Task.Input.Data_set;

import weka.core.Instances;
import weka.experiment.Experiment;

public class TaskBasedExperiment extends Experiment {

	private static final long serialVersionUID = 1L;

	/** An array of the Tasks to be executed */
	protected DefaultListModel<Integer> m_Tasks = new DefaultListModel<Integer>();

	/**
	 * boolean to specify whether this is a plain dataset based experiment, or
	 * an OpenML specific task based experiment
	 */
	protected boolean datasetBasedExperiment = true;

	/** The task currently being used */
	protected Task m_CurrentTask;

	public TaskBasedExperiment(Experiment exp) {
		this.m_ResultListener = exp.getResultListener();
		this.m_ResultProducer = exp.getResultProducer();
		this.m_RunLower = exp.getRunLower();
		this.m_RunUpper = exp.getRunUpper();
		this.m_Datasets = exp.getDatasets();
		this.m_UsePropertyIterator = exp.getUsePropertyIterator();
		this.m_PropertyArray = exp.getPropertyArray();
		this.m_Notes = exp.getNotes();
		// this.m_AdditionalMeasures =
		// this.m_ClassFirst = exp.classFirst(flag)
		this.m_AdvanceDataSetFirst = exp.getAdvanceDataSetFirst();

	}
	
	public void setMode(boolean datasetBasedExperiment) {
		this.datasetBasedExperiment = datasetBasedExperiment;
	}

	public DefaultListModel<Integer> getTasks() {
		return m_Tasks;
	}

	@Override
	public void initialize() throws Exception {
		try {
			super.initialize();
		} catch(Exception e) {
			if(e.getMessage().equals("No datasets have been specified") && datasetBasedExperiment == false ) {
				// TODO: Some better catching!
			} else {
				throw e;
			}
		}
		m_CurrentTask = null;

		if (getTasks().size() == 0 && datasetBasedExperiment == false) {
			throw new Exception("No tasks have been specified");
		}
	}

	@Override
	public void nextIteration() throws Exception {
		if (m_UsePropertyIterator) {
			if (m_CurrentProperty != m_PropertyNumber) {
				setProperty(0, m_ResultProducer);
				m_CurrentProperty = m_PropertyNumber;
			}
		}
		
		if(m_CurrentTask == null) {
			Object objTask = ApiConnector.openmlTasksSearch( getTasks().elementAt(m_DatasetNumber) );
			if(objTask instanceof ApiError) {
				throw new Exception("An error has occured while getting task " + getTasks().elementAt(m_DatasetNumber) );
			}
			m_CurrentTask = (Task) objTask;
			
			Data_set ds = m_CurrentTask.getInputs()[0].getData_set();
			DataSetDescription dsd = ApiConnector.openmlDataDescription(ds.getData_set_id());
			Instances instDataset = ApiConnector.getDatasetFromUrl(dsd.getUrl());
			InstancesHelper.setTargetAttribute(instDataset, ds.getTarget_feature());
			
			m_ResultProducer.setInstances(instDataset);
		}
		
		m_ResultProducer.doRun(m_RunNumber);
		
		advanceCounters();
	}

	@Override
	public void advanceCounters() {
		Integer subjectsInExperiment = (datasetBasedExperiment) ? getDatasets()
				.size() : getTasks().size();

		if (m_AdvanceDataSetFirst) {
			m_RunNumber++;
			if (m_RunNumber > getRunUpper()) {
				m_RunNumber = getRunLower();
				m_DatasetNumber++;
				m_CurrentInstances = null;
				m_CurrentTask = null;
				if (m_DatasetNumber >= subjectsInExperiment) {
					m_DatasetNumber = 0;
					if (m_UsePropertyIterator) {
						m_PropertyNumber++;
						if (m_PropertyNumber >= Array
								.getLength(m_PropertyArray)) {
							m_Finished = true;
						}
					} else {
						m_Finished = true;
					}
				}
			}
		} else { // advance by custom iterator before data set
			m_RunNumber++;
			if (m_RunNumber > getRunUpper()) {
				m_RunNumber = getRunLower();
				if (m_UsePropertyIterator) {
					m_PropertyNumber++;
					if (m_PropertyNumber >= Array.getLength(m_PropertyArray)) {
						m_PropertyNumber = 0;
						m_DatasetNumber++;
						m_CurrentInstances = null;
						m_CurrentTask = null;
						if (m_DatasetNumber >= subjectsInExperiment) {
							m_Finished = true;
						}
					}
				} else {
					m_DatasetNumber++;
					m_CurrentInstances = null;
					m_CurrentTask = null;
					if (m_DatasetNumber >= subjectsInExperiment) {
						m_Finished = true;
					}
				}
			}
		}
	}
}

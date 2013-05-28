package openml.experiment;

import java.lang.reflect.Array;

import javax.swing.DefaultListModel;

import openml.algorithms.InstancesHelper;
import openml.algorithms.TaskInformation;
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
	protected DefaultListModel<Task> m_Tasks = new DefaultListModel<Task>();

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

	public DefaultListModel<Task> getTasks() {
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
			m_CurrentTask = getTasks().elementAt(m_DatasetNumber);
			
			Data_set ds = TaskInformation.getSourceData(m_CurrentTask);
			Instances instDataset = TaskInformation.getSourceData(m_CurrentTask).getDataSetDescription().getDataset();
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

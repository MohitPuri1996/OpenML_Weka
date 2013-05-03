package openml.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;

import openml.algorithms.Conversion;
import openml.experiment.TaskBasedExperiment;
import weka.core.Utils;
import weka.core.ClassDiscovery.StringCompare;
import weka.core.converters.ConverterUtils;
import weka.core.converters.Saver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.experiment.Experiment;
import weka.gui.JListHelper;
import weka.gui.ViewerDialog;
import weka.gui.experiment.DatasetListPanel;
import weka.gui.experiment.Messages;

public class TaskListPanel extends DatasetListPanel {

	private static final long serialVersionUID = 1L;

	/** The experiment to set the dataset or task list of. */
	protected Experiment m_Exp;

	private boolean datasetBased = true;

	public TaskListPanel(Experiment exp) {
		this();
		setExperiment(exp);
	}

	public TaskListPanel() {
		super();
	}

	/**
	 * sets the state of the buttons according to the selection state of the
	 * JList.
	 * 
	 * @param e
	 *            the event
	 */
	private void setButtons(ListSelectionEvent e) {
		if ((e == null) || (e.getSource() == m_List)) {
			m_DeleteBut.setEnabled(m_List.getSelectedIndex() > -1);
			m_EditBut.setEnabled(datasetBased
					&& m_List.getSelectedIndices().length == 1);
			m_UpBut.setEnabled(JListHelper.canMoveUp(m_List));
			m_DownBut.setEnabled(JListHelper.canMoveDown(m_List));
		}
	}

	/**
	 * Tells the panel to act on a new experiment.
	 * 
	 * @param exp
	 *            a value of type 'TaskBasedExperiment'
	 */
	public void setExperiment(Experiment exp) {
		m_Exp = exp;
		m_List.setModel(datasetBased ? m_Exp.getDatasets() : m_Exp.getTasks());
		m_AddBut.setEnabled(true);
		setButtons(null);
	}

	public void setMode(boolean datasetBased) {
		this.datasetBased = datasetBased;

		if (datasetBased) {
			m_relativeCheck.setEnabled(true);
			m_List.setModel(m_Exp.getDatasets());
			setBorder(BorderFactory
					.createTitledBorder(Messages.getInstance().getString(
							"DatasetListPanel_RelativeCheck_SetBorder_Text")));
		} else {
			m_relativeCheck.setEnabled(false);
			m_List.setModel(m_Exp.getTasks());
			setBorder(BorderFactory.createTitledBorder("Tasks"));
		}
	}

	/**
	 * Handle actions when buttons get pressed.
	 * 
	 * @param e
	 *            a value of type 'ActionEvent'
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_AddBut) {
			if (datasetBased)
				actionPerformedDatasetBasedAdd(e);
			else
				actionPerformedTaskBasedAdd(e);
		} else if (e.getSource() == m_DeleteBut) {
			// Delete the selected files
			if (datasetBased) {
				int[] selected = m_List.getSelectedIndices();
				if (selected != null) {
					for (int i = selected.length - 1; i >= 0; i--) {
						int current = selected[i];
						m_Exp.getDatasets().removeElementAt(current);
						if (m_Exp.getDatasets().size() > current) {
							m_List.setSelectedIndex(current);
						} else {
							m_List.setSelectedIndex(current - 1);
						}
					}
				}
			} else {
				int[] selected = m_List.getSelectedIndices();
				if (selected != null) {
					for (int i = selected.length - 1; i >= 0; i--) {
						int current = selected[i];
						m_Exp.getTasks().removeElementAt(current);
						if (m_Exp.getTasks().size() > current) {
							m_List.setSelectedIndex(current);
						} else {
							m_List.setSelectedIndex(current - 1);
						}
					}
				}
			}
			setButtons(null);
		} else if (e.getSource() == m_EditBut) {
			if (datasetBased)
				actionPerformedDatasetBasedEdit(e);
			else
				actionPerformedTaskBasedEdit(e);
		} else if (e.getSource() == m_UpBut) {
			JListHelper.moveUp(m_List);
		} else if (e.getSource() == m_DownBut) {
			JListHelper.moveDown(m_List);
		}
	}

	public void actionPerformedDatasetBasedAdd(ActionEvent e) {
		boolean useRelativePaths = m_relativeCheck.isSelected();
		// Let the user select an arff file from a file chooser
		int returnVal = m_FileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (m_FileChooser.isMultiSelectionEnabled()) {
				File[] selected = m_FileChooser.getSelectedFiles();
				for (int i = 0; i < selected.length; i++) {
					if (selected[i].isDirectory()) {
						Vector files = new Vector();
						getFilesRecursively(selected[i], files);

						// sort the result
						Collections.sort(files, new StringCompare());

						for (int j = 0; j < files.size(); j++) {
							File temp = (File) files.elementAt(j);
							if (useRelativePaths) {
								try {
									temp = Utils.convertToRelativePath(temp);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							m_Exp.getDatasets().addElement(temp);
						}
					} else {
						File temp = selected[i];
						if (useRelativePaths) {
							try {
								temp = Utils.convertToRelativePath(temp);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						m_Exp.getDatasets().addElement(temp);
					}
				}
				setButtons(null);
			} else {
				if (m_FileChooser.getSelectedFile().isDirectory()) {
					Vector files = new Vector();
					getFilesRecursively(m_FileChooser.getSelectedFile(), files);

					// sort the result
					Collections.sort(files, new StringCompare());

					for (int j = 0; j < files.size(); j++) {
						File temp = (File) files.elementAt(j);
						if (useRelativePaths) {
							try {
								temp = Utils.convertToRelativePath(temp);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						m_Exp.getDatasets().addElement(temp);
					}
				} else {
					File temp = m_FileChooser.getSelectedFile();
					if (useRelativePaths) {
						try {
							temp = Utils.convertToRelativePath(temp);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					m_Exp.getDatasets().addElement(temp);
				}
				setButtons(null);
			}
		}
	}

	public void actionPerformedDatasetBasedEdit(ActionEvent e) {
		// Delete the selected files
		int selected = m_List.getSelectedIndex();
		if (selected != -1) {
			ViewerDialog dialog = new ViewerDialog(null);
			String filename = m_List.getSelectedValue().toString();
			int result;
			try {
				DataSource source = new DataSource(filename);
				result = dialog.showDialog(source.getDataSet());
				// nasty workaround for Windows regarding locked files:
				// if file Reader in Loader is not closed explicitly, we
				// cannot
				// overwrite the file.
				source = null;
				System.gc();
				// workaround end
				if ((result == ViewerDialog.APPROVE_OPTION)
						&& (dialog.isChanged())) {
					result = JOptionPane
							.showConfirmDialog(
									this,
									Messages.getInstance()
											.getString(
													"DatasetListPanel_ActionPerformed_Result_JOptionPaneShowConfirmDialog_Text"));
					if (result == JOptionPane.YES_OPTION) {
						Saver saver = ConverterUtils.getSaverForFile(filename);
						saver.setFile(new File(filename));
						saver.setInstances(dialog.getInstances());
						saver.writeBatch();
					}
				}
			} catch (Exception ex) {
				JOptionPane
						.showMessageDialog(
								this,
								Messages.getInstance()
										.getString(
												"DatasetListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_First")
										+ filename
										+ Messages
												.getInstance()
												.getString(
														"DatasetListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_Second")
										+ ex.toString(),
								Messages.getInstance()
										.getString(
												"DatasetListPanel_ActionPerformed_Error_JOptionPaneShowMessageDialog_Text_Third"),
								JOptionPane.INFORMATION_MESSAGE);
			}
		}
		setButtons(null);
	}

	public void actionPerformedTaskBasedAdd(ActionEvent e) {
		String s = (String) JOptionPane.showInputDialog(this,
				"A comma-seperated list of the task id's from OpenML.org:",
				"OpenML Task id's", JOptionPane.PLAIN_MESSAGE);
		try {
			int[] input_task_ids = Conversion.commaSeperatedStringToIntArray(s);
			for (int i = 0; i < input_task_ids.length; ++i) {
				if (m_Exp.getTasks().contains(input_task_ids[i]) == false) {
					m_Exp.getTasks().addElement(input_task_ids[i]);
				}
			}
		} catch (NumberFormatException nfe) {
			JOptionPane
					.showMessageDialog(
							this,
							"Please insert a comma seperated list of task_id's. These are all numbers. ",
							"Wrong input", JOptionPane.ERROR_MESSAGE);
		} catch (NullPointerException npe) {
			// catch quietly. User probably pressed cancel.
		}
	}

	public void actionPerformedTaskBasedEdit(ActionEvent e) {
		System.out.println("TODO, function not yet implemented.");
	}
}

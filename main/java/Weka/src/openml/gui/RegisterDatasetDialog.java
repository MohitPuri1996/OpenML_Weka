package openml.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import com.thoughtworks.xstream.XStream;

import openml.algorithms.Conversion;
import openml.algorithms.Hashing;
import openml.constants.Constants;
import openml.io.ApiConnector;
import openml.io.ApiSessionHash;
import openml.xml.ApiError;
import openml.xml.DataSetDescription;
import openml.xml.UploadDataSet;
import openml.xstream.XstreamXmlMapping;

import weka.core.Instances;

public class RegisterDatasetDialog extends JDialog implements Observer {

	private static final long serialVersionUID = 1L;
	private final Instances dataset;
	private final ApiSessionHash apiSessionHash;
	private final JFrame parent;
	
	private boolean requestedSubmission;

	private static final JLabel[] label = { new JLabel("Name: "),
			new JLabel("Version: "), new JLabel("Description: "),
			new JLabel("Creator(s): "), new JLabel("Contributor(s): "),
			new JLabel("Collection date: "), new JLabel("Language: "),
			new JLabel("Licence: "), new JLabel("Row containing ID: ") };

	private static final String[] tooltip = {
			"Every dataset needs a good name, not conflicting with existing datasets on the server. Name-version combinations should be unique.",
			"Every dataset needs a version, the default is 1.0. ",
			"Description of what this dataset is about. Required!",
			"Optional. The persons/institutions that created the dataset. One per line. ",
			"Optional. Datasets can have contributors, e.g. people that reformatted the data. One per line.",
			"Optional. When was this data first collected? Free format, to be read only by humans.",
			"Option, in English. Starts with 1 upper case latter, rest lower case.",
			"Optional. Default is none, meanining Public Domain or \"dont know / care\".",
			"Optional. The attribute that reflects the row id for each row in tabular data. If not set, we will assume that there is no row-id column and that the rows should be ordered in the order they appear in the dataset (from 0 to (n.obs - 1))." };

	private static final JTextComponent[] basicTextComponents = {
			new JTextField(),
			new JTextField(),
			new JTextArea(6, 0),
			new JTextArea(3, 0),
			new JTextArea(3, 0),
			new JTextField(),
			new JTextField(), 
			new JTextField(), 
			new JTextField() };
	
	private static final JComponent[] textComponent = {
		basicTextComponents[0], 
		basicTextComponents[1], 
		new JScrollPane(basicTextComponents[2], ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
		new JScrollPane(basicTextComponents[3], ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
		new JScrollPane(basicTextComponents[4],ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
				basicTextComponents[5], 
				basicTextComponents[6], 
				basicTextComponents[7], 
				basicTextComponents[8]
	};
	
	public RegisterDatasetDialog(JFrame parent, Instances dataset, ApiSessionHash apiSessionHash ) {
		super(parent, "Register dataset on OpenML.org");
		this.requestedSubmission = false;
		this.dataset = dataset;
		this.parent = parent;
		this.apiSessionHash = apiSessionHash;
		apiSessionHash.addObserver(this);
		
		add(getContents());
		setResizable(false);
		pack();
	}

	private JPanel getContents() {
		JPanel contents = new JPanel();
		contents.setLayout(new BorderLayout());
		contents.setPreferredSize(new Dimension(640, 480));
		contents.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

		contents.add(getInputPanel(),BorderLayout.CENTER);
		contents.add(getButtonPanel(),BorderLayout.SOUTH);
		return contents;
	}

	private JPanel getInputPanel() {
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

		for (int i = 0; i < label.length; ++i) {
			textComponent[i].setBorder(BorderFactory.createEtchedBorder());
			textComponent[i].setAlignmentX(JComponent.LEFT_ALIGNMENT);
			basicTextComponents[i].setText("");
			label[i].setToolTipText(tooltip[i]);
			label[i].setAlignmentX(JComponent.LEFT_ALIGNMENT);
			inputPanel.add(label[i]);
			inputPanel.add(textComponent[i]);
		}
		return inputPanel;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton cancel = new JButton("Cancel");
		JButton submit = new JButton("Submit");

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				requestedSubmission = false;
				dispose();
			}
		});
		
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				registerDataset();
			}
		});

		buttonPanel.add(cancel);
		buttonPanel.add(submit);
		return buttonPanel;
	}
	
	private void registerDataset() {
		if( apiSessionHash.isValid() ) {
			requestedSubmission = false;
			File tmpDatasetFile;
			File tmpDescriptionFile;
			try {
				XStream xstream = XstreamXmlMapping.getInstance();
				tmpDatasetFile = Conversion.instancesToTempFile(dataset, "weka_tmp_dataset");
				
				DataSetDescription dsd = new DataSetDescription(
					basicTextComponents[0].getText().equals("") ? null : basicTextComponents[0].getText(), 
					basicTextComponents[1].getText().equals("") ? null : basicTextComponents[1].getText(), 
					basicTextComponents[2].getText().equals("") ? null : basicTextComponents[2].getText(), 
					basicTextComponents[3].getText().equals("") ? null : basicTextComponents[3].getText().split("\n"), 
					basicTextComponents[4].getText().equals("") ? null : basicTextComponents[4].getText().split("\n"), 
					Constants.DATASET_FORMAT, 
					basicTextComponents[5].getText().equals("") ? null : basicTextComponents[5].getText(), 
					basicTextComponents[6].getText().equals("") ? null : basicTextComponents[6].getText(), 
					basicTextComponents[7].getText().equals("") ? null : basicTextComponents[7].getText(), 
					basicTextComponents[8].getText().equals("") ? null : basicTextComponents[8].getText(), 
					Hashing.md5(tmpDatasetFile));
				
				tmpDescriptionFile = Conversion.stringToTempFile(xstream.toXML(dsd), "weka_tmp_description");
				String result = ApiConnector.openmlDataUpload(tmpDescriptionFile, tmpDatasetFile, apiSessionHash.getSessionHash());
				Object resultObject = xstream.fromXML(result);
				if(resultObject instanceof ApiError) {
					ApiError error = (ApiError) resultObject;
					JOptionPane.showMessageDialog(parent,
							error.getMessage(),
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				} else {
					UploadDataSet uds = (UploadDataSet) resultObject;
				
					JOptionPane.showMessageDialog(parent, "Dataset succesfully registered with id " + uds.getId() );
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(parent,
						"An unexpected IO Exception has occured. ",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
			} catch (NoSuchAlgorithmException e) {
				JOptionPane.showMessageDialog(parent,
						"An unexpected Exception has occured. ",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
			}
			
			
		} else {
			requestedSubmission = true;
			AuthenticateDialog ad = new AuthenticateDialog(parent, apiSessionHash);
			ad.setVisible(true);
		}
	}

	public void update(Observable arg0, Object arg1) {
		if(requestedSubmission)
			registerDataset();
	}
}

package openml.gui;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import openml.io.ApiSessionHash;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class RegisterImplementationDialog extends JDialog implements Observer {

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
			new JTextField(), new JTextField(), new JTextArea(6, 0),
			new JTextArea(3, 0), new JTextArea(3, 0), new JTextField(),
			new JTextField(), new JTextField(), new JTextField() };

	private static final JComponent[] textComponent = {
			basicTextComponents[0],
			basicTextComponents[1],
			new JScrollPane(basicTextComponents[2],
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			new JScrollPane(basicTextComponents[3],
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			new JScrollPane(basicTextComponents[4],
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			basicTextComponents[5], basicTextComponents[6],
			basicTextComponents[7], basicTextComponents[8] };
	
	public RegisterImplementationDialog(JFrame parent, Classifier implementation, ApiSessionHash apiSessionHash) {
		super(parent, "Register implementation on OpenML.org");
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
		return new JPanel();
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

}

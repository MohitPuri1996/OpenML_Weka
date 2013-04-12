package openml.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import openml.io.ApiSessionHash;

import weka.core.Instances;

public class RegisterDatasetDialog extends JDialog implements Observer {

	private static final long serialVersionUID = 1L;
	private final Instances dataset;
	private final ApiSessionHash apiSessionHash;
	private final JFrame parent;

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

	private static final JComponent[] textComponent = {
			new JTextField(),
			new JTextField(),
			new JScrollPane(new JTextArea(6, 0),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			new JScrollPane(new JTextArea(3, 0),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			new JScrollPane(new JTextArea(3, 0),
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
			new JTextField(),
			new JTextField(), 
			new JTextField(), 
			new JTextField() };
	
	public RegisterDatasetDialog(JFrame parent, Instances dataset, ApiSessionHash apiSessionHash ) {
		super(parent, "Register dataset on OpenML.org");
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
			System.out.println("registering dataset");
		} else {
			AuthenticateDialog ad = new AuthenticateDialog(parent, apiSessionHash);
			ad.setVisible(true);
		}
	}

	public void update(Observable arg0, Object arg1) {
		System.out.println("update function invoked");
		registerDataset();
	}
}

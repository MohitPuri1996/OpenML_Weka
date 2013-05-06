package openml.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import openml.io.ApiConnector;
import openml.io.ApiSessionHash;
import openml.xml.Implementation;
import openml.xml.UploadImplementation;
import openml.xstream.XstreamXmlMapping;
import weka.classifiers.Sourcable;

public class RegisterImplementationDialog extends JDialog implements Observer {

	private static final long serialVersionUID = 1L;
	private final File sourceFile;
	private final File binaryFile;
	private final ApiSessionHash apiSessionHash;
	private final JFrame parent;
	
	private boolean requestedSubmission;
	
	private static final JLabel[] label = {
		new JLabel("Name: "),
		new JLabel("Version: "),
		new JLabel("Description: "),
		new JLabel("Creators: "),
		new JLabel("Contributors: "),
		new JLabel("Licence: "),
		new JLabel("Language: "),
		new JLabel("Full description: "),
		new JLabel("Installation notes: "),
		new JLabel("Dependencies: "),
	};

	private static final String[] tooltip = {
		"Every implementation needs a good name, not conflicting with existing implementations on the server. Name-version combinations should be unique. ",
		"Every implementation needs a version, the default is 1.0.",
		"User description of the implementation. Required!",
		"Optional. The persons/institutions that created the implementation.",
		"Optional. Minor contributors to the workflow.",
		"Optional. Default is none, meanining Public Domain or \"don't know / care\"",
		"Optional. Default is English. Starts with 1 upper case latter, rest lower case",
		"Optional. Full description of the workflow, e.g.  man pages filled in by tool. This is a much more elaborate description that given in the 'description' field. It may include information about all components of the workflow.",
		"Optional. Technical info. How to run the workflow, are there additional hints?",
		"Optional. Dependencies of this implementations: environments, software packages or libraries necessary to run it. Don't forget version numbers for libraries. Free form, BUT every tool should specify a suitable format here and stick to it. Use comma separation for multiple dependencies",
	};

	private static final JTextComponent[] basicTextComponents = {
		new JTextField(),
		new JTextField(),
		new JTextArea(3, 0),
		new JTextArea(3, 0),
		new JTextArea(3, 0),
		new JTextField(),
		new JTextField(),
		new JTextArea(3, 0),
		new JTextArea(3, 0),
		new JTextField(),
	};

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
		new JScrollPane(basicTextComponents[7], ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
		new JScrollPane(basicTextComponents[8],ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
		basicTextComponents[9], 
	};
	
	public RegisterImplementationDialog(JFrame parent, File sourceFile, File binaryFile, ApiSessionHash apiSessionHash) {
		super(parent, "Register implementation on OpenML.org");
		this.requestedSubmission = false;
		this.sourceFile = sourceFile;
		this.binaryFile = binaryFile;
		this.parent = parent;
		this.apiSessionHash = apiSessionHash;
		apiSessionHash.addObserver(this);
		
		add(getContents());
		setResizable(false);
		pack();
	}
	
	public RegisterImplementationDialog(JFrame parent, Sourcable classifier, String className, ApiSessionHash apiSessionHash) throws Exception {
		super(parent, "Register implementation on OpenML.org");
		this.requestedSubmission = false;
		this.sourceFile = Conversion.stringToTempFile(classifier.toSource(className), "wekaImplementationSource");
		this.binaryFile = null;
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
				requestedSubmission = true;
				registerImplementation();
			}
		});

		buttonPanel.add(cancel);
		buttonPanel.add(submit);
		return buttonPanel;
	}
	
	private void registerImplementation() {
		if( apiSessionHash.isValid() ) {
			requestedSubmission = false;
			
			File tmpDescriptionFile;
			try {
				XStream xstream = XstreamXmlMapping.getInstance();
				
				Implementation imp = new Implementation(
					basicTextComponents[0].getText().equals("") ? null : basicTextComponents[0].getText(), 
					basicTextComponents[1].getText().equals("") ? null : basicTextComponents[1].getText(), 
					basicTextComponents[2].getText().equals("") ? null : basicTextComponents[2].getText(), 
					basicTextComponents[3].getText().equals("") ? null : basicTextComponents[3].getText().split("\n"), 
					basicTextComponents[4].getText().equals("") ? null : basicTextComponents[4].getText().split("\n"), 
					basicTextComponents[5].getText().equals("") ? null : basicTextComponents[5].getText(), 
					basicTextComponents[6].getText().equals("") ? null : basicTextComponents[6].getText(), 
					basicTextComponents[7].getText().equals("") ? null : basicTextComponents[7].getText(), 
					basicTextComponents[8].getText().equals("") ? null : basicTextComponents[8].getText(),
					basicTextComponents[9].getText().equals("") ? null : basicTextComponents[8].getText() );
				
				String implementationXml = xstream.toXML(imp);
				System.out.println(implementationXml);
				tmpDescriptionFile = Conversion.stringToTempFile(implementationXml, "weka_tmp_implementation");
				
				UploadImplementation ui = ApiConnector.openmlImplementationUpload(tmpDescriptionFile, sourceFile, binaryFile, apiSessionHash.getSessionHash());
				JOptionPane.showMessageDialog(parent, "Implementation succesfully registered with id " + ui.getId() );
				/*Object resultObject = xstream.fromXML(result);
				System.out.println(result);
				if(resultObject instanceof ApiError) {
					ApiError error = (ApiError) resultObject;
					JOptionPane.showMessageDialog(parent,
							error.getMessage(),
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				} else {
					UploadImplementation ui = (UploadImplementation) resultObject;
				
					JOptionPane.showMessageDialog(parent, "Implementation succesfully registered with id " + ui.getId() );
				}*/
			} catch (IOException e) {
				JOptionPane.showMessageDialog(parent,
						"An unexpected IO Exception has occured. ",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
			} /*catch (NoSuchAlgorithmException e) {
				JOptionPane.showMessageDialog(parent,
						"An unexpected Exception has occured. ",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
			}*/ catch (Exception e) {
				JOptionPane.showMessageDialog(parent,
						e.getMessage(),
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
			}
			
		} else {
			requestedSubmission = true;
			AuthenticateDialog ad = new AuthenticateDialog(parent, apiSessionHash);
			ad.setVisible(true);
		}
	}
	
	public void update(Observable o, Object arg) {
		if(requestedSubmission)
			registerImplementation();
	}

}

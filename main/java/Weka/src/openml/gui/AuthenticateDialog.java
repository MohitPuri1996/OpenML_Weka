package openml.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import openml.io.ApiConnector;
import openml.io.ApiSessionHash;
import openml.xml.Authenticate;

public class AuthenticateDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private ApiSessionHash sessionHash;
	private final JFrame parent;

	private static final JLabel[] label = {
		new JLabel("Username: "), 
		new JLabel("Password: ")
	};
	
	private static final String[] tooltip = {
		"Your username on OpenML.org",
		"Your password on OpenML.org. Will not be stored in Weka."
	};
	
	private static final JTextComponent[] textComponent = {
		new JTextField(),
		new JPasswordField(),
	};
	
	public AuthenticateDialog(JFrame parent, ApiSessionHash sessionHash ) {
		super(parent, "Authenticate on OpenML.org");
		this.parent = parent;
		this.sessionHash = sessionHash;
		add(getContents());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
	}
	
	private JPanel getContents() {
		JPanel contents = new JPanel();
		contents.setLayout(new BorderLayout());
		contents.setPreferredSize(new Dimension(320, 110));
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
			textComponent[i].setText("");
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
				setVisible(false);
				dispose();
			}
		});
		
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				/*try {
					Authenticate auth = ApiConnector.openmlAuthenticate(textComponent[0].getText(),textComponent[1].getText());
					sessionHash.set(null, auth.getSessionHash(), auth.getValidUntil());
					
					/*Object xml = xstream.fromXML(response);
					if( xml instanceof ApiError ) {
						ApiError error = (ApiError) xml;
						JOptionPane.showMessageDialog(parent,
								error.getMessage(),
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
					} else {
						// TODO: set also username. 
						Authenticate auth = (Authenticate) xml;
						sessionHash.set(null, auth.getSessionHash(), auth.getValidUntil());
					}*/
				/*} catch (IOException e) {
					JOptionPane.showMessageDialog(parent,
							"An unexpected IO Exception has occured. ",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				} catch (NoSuchAlgorithmException e) {
					JOptionPane.showMessageDialog(parent,
							"An unexpected Exception has occured. ",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				} catch (ParseException e) {
					JOptionPane.showMessageDialog(parent,
							"An unexpected Parse Exception has occured. ",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(parent,
							e.getMessage(),
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
				} 
				
				setVisible(false);
				dispose();*/
			}
		});

		buttonPanel.add(cancel);
		buttonPanel.add(submit);
		return buttonPanel;
	}
	
}
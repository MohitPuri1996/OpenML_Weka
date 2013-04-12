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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.thoughtworks.xstream.XStream;

import openml.io.ApiConnector;
import openml.io.ApiSessionHash;
import openml.xml.ApiError;
import openml.xml.Authenticate;
import openml.xstream.XstreamXmlMapping;

public class AuthenticateDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private ApiSessionHash sessionHash;

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
		this.sessionHash = sessionHash;
		add(getContents());
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
				try {
					XStream xstream = XstreamXmlMapping.getInstance();
					String response = ApiConnector.openmlAuthenticate(textComponent[0].getText(),textComponent[1].getText());
					System.out.println(response);
					Object xml = xstream.fromXML(response);
					if( xml instanceof ApiError ) {
						// TODO: present error dialog. 
					} else {
						// TODO: set also username. 
						Authenticate auth = (Authenticate) xml;
						sessionHash.set(null, auth.getSessionHash(), auth.getValidUntil());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				dispose();
			}
		});

		buttonPanel.add(cancel);
		buttonPanel.add(submit);
		return buttonPanel;
	}
	
}
package openml.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import openml.algorithms.Conversion;
import openml.gui.AuthenticateDialog;
import openml.io.RunResultsCollector.OpenmlExecutedTask;
import openml.xml.UploadRun;
import openml.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStream;

public class RunResultsSubmitter implements Observer, Serializable {

	private static final long serialVersionUID = 8129374529493626L;
	private ApiSessionHash ash;
	private List<OpenmlExecutedTask> queue;
	private boolean authDialogOpened;

	public RunResultsSubmitter() {
		ash = new ApiSessionHash();
		queue = new ArrayList<OpenmlExecutedTask>();
		authDialogOpened = false;
	}

	public void acceptResult(OpenmlExecutedTask oet) {
		queue.add(oet);
		if (ash.countObservers() == 0)
			ash.addObserver(this);
		sendTask();
	}

	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof ApiSessionHash) {

			authDialogOpened = false;
			if (queue.size() > 0)
				sendTask();

		}
	}

	private void sendTask() {
		if (ash.isValid()) {
			try {
				XStream xstream = XstreamXmlMapping.getInstance();
				File tmpPredictionsFile;
				File tmpDescriptionFile;
				OpenmlExecutedTask oet = queue.get(0);
				tmpPredictionsFile = Conversion.instancesToTempFile(
						oet.getPredictions(), "weka_generated_predictions");
				tmpDescriptionFile = Conversion.stringToTempFile(
						xstream.toXML(oet.getRun()), "weka_generated_run");
				Map<String, File> output_files = new HashMap<String, File>();
				output_files.put("predictions", tmpPredictionsFile);
				UploadRun ur = ApiConnector.openmlRunUpload(tmpDescriptionFile,
						output_files, ash.getSessionHash());
				System.out.println(xstream.toXML(ur));
				queue.remove(0);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				if (e.getMessage().length() >= 12) {
					if (e.getMessage().substring(0, 12).equals("ApiError 205")) {
						System.out.println("Unknown implementation! ");
					}
				}
				e.printStackTrace();
				queue.remove(0);
			}

			if (queue.size() > 0)
				sendTask();
		} else {
			if (authDialogOpened == false) {
				AuthenticateDialog ad = new AuthenticateDialog(new JFrame(),
						ash);
				ad.setVisible(true);
				authDialogOpened = true;
			}
		}
	}
}

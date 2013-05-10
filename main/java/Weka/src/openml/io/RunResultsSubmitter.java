package openml.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import com.thoughtworks.xstream.XStream;

import openml.algorithms.Conversion;
import openml.gui.AuthenticateDialog;
import openml.io.RunResultsCollector.OpenmlExecutedTask;
import openml.xstream.XstreamXmlMapping;

public class RunResultsSubmitter implements Observer, Serializable {
	
	private static final long serialVersionUID = 8129374529493626L;
	private transient ApiSessionHash ash;
	private transient OpenmlExecutedTask oet;
	
	public RunResultsSubmitter() {
		ash = new ApiSessionHash();
		ash.addObserver(this);
	}
	
	public void update(Observable arg0, Object arg1) {
		//TODO: Here is something wrong. Apparently, due to the copy, the call is never received. 
		if(arg0 instanceof RunResultsCollector){
			oet = (OpenmlExecutedTask) arg1;
			sendTask();
		} else if(arg0 instanceof ApiSessionHash) {
			sendTask();
		}
	}
	
	private void sendTask() {
		if( ash.isValid() ) {
			try {
				XStream xstream = XstreamXmlMapping.getInstance();
				File tmpPredictionsFile;
				File tmpDescriptionFile;
				tmpPredictionsFile = Conversion.instancesToTempFile(oet.getPredictions(), "weka_generated_predictions");
				tmpDescriptionFile = Conversion.stringToTempFile( xstream.toXML(oet.getRun()), "weka_generated_run" );
				Map<String, File> output_files = new HashMap<String, File>();
				output_files.put("predictions", tmpPredictionsFile);
				ApiConnector.openmlRunUpload(tmpDescriptionFile, output_files, ash.getSessionHash());
			} catch( IOException e ) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// TODO: new JFrame. Can we do this, or do we register a "real" frame?
			AuthenticateDialog ad = new AuthenticateDialog(new JFrame(), ash);
			ad.setVisible(true);
		}
	}
}

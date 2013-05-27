package openml.io;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import openml.algorithms.Conversion;
import openml.io.RunResultsCollector.OpenmlExecutedTask;
import openml.xml.UploadRun;
import openml.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStream;

public class RunResultsSubmitter implements Serializable {

	private static final long serialVersionUID = 8129374529493626L;
	private ApiSessionHash ash;

	public RunResultsSubmitter() {
		ash = null;
	}
	
	public void acceptSessionHash(ApiSessionHash ash) {
		this.ash = ash;
	}

	public void sendTask(OpenmlExecutedTask oet) throws Exception {
		if(ash == null) {
			throw new Exception("No credentials provided yet. ");
		}
		if (ash.isValid() == false) {
			ash.update();
		}
		
		if(ash.isValid() == false ) {
			throw new Exception("Credentials not valid. ");
		}
		
		try {
			XStream xstream = XstreamXmlMapping.getInstance();
			File tmpPredictionsFile;
			File tmpDescriptionFile;
			tmpPredictionsFile = Conversion.instancesToTempFile(
					oet.getPredictions(), "weka_generated_predictions");
			tmpDescriptionFile = Conversion.stringToTempFile(
					xstream.toXML(oet.getRun()), "weka_generated_run");
			Map<String, File> output_files = new HashMap<String, File>();
			output_files.put("predictions", tmpPredictionsFile);
			UploadRun ur = ApiConnector.openmlRunUpload(tmpDescriptionFile,
					output_files, ash.getSessionHash());
			System.out.println(xstream.toXML(ur));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			if (e.getMessage().length() >= 12) {
				if (e.getMessage().substring(0, 12).equals("ApiError 205")) {
					System.out.println("Unknown implementation! ");
				}
			}
			e.printStackTrace();
		}
	}
}

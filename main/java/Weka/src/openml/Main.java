package openml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import openml.gui.RegisterDatasetDialog;
import openml.io.ApiConnector;
import openml.io.ApiSessionHash;
import openml.xml.ApiError;
import openml.xml.DataSetDescription;
import openml.xstream.XstreamXmlMapping;
import weka.core.Instances;

import com.thoughtworks.xstream.XStream;

public class Main {

	public static void main(String[] args) throws IOException{
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
		
		
		
		JFrame f = new JFrame();
		f.setLayout(new BorderLayout());
        f.setLocationByPlatform(true);
		f.setSize(new Dimension(640,480));
		f.setVisible(true);
		
		ApiSessionHash ash = new ApiSessionHash();
		JDialog regDataSet = new RegisterDatasetDialog(f,datasetFromApi(61),ash);
		
		regDataSet.setVisible(true);
	}
	
	private static Instances datasetFromApi(int did) throws IOException {
		XStream xstream = XstreamXmlMapping.getInstance();
		String iris_xml = ApiConnector.openmlDataDescription(did);
		
		Object apiRepsonse = xstream.fromXML(iris_xml);
		
		if( ! (apiRepsonse instanceof DataSetDescription) ) {
			ApiError e = (ApiError) apiRepsonse;
			throw new RuntimeException("Error " + e.getCode() + ". " + e.getMessage() );
		}
		
		DataSetDescription d = (DataSetDescription) apiRepsonse;
		return ApiConnector.getDatasetFromUrl(d.getUrl());
	}
}
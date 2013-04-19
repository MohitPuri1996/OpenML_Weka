package openml.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import openml.constants.Constants;

import weka.core.Instances;

public class Conversion {

	public static File instancesToTempFile( Instances dataset, String filename ) throws IOException {
		File file = File.createTempFile(filename, Constants.DATASET_FORMAT);
		BufferedWriter br = new BufferedWriter(new FileWriter(file));
		br.write(dataset.toString());
		br.close();
		file.deleteOnExit();
		return file;
	}
	
	public static File stringToTempFile( String string, String filename ) throws IOException {
		File file = File.createTempFile(filename, Constants.DATASET_FORMAT);
		BufferedWriter br = new BufferedWriter(new FileWriter(file));
		br.write(string);
		br.close();
		file.deleteOnExit();
		return file;
	}
}

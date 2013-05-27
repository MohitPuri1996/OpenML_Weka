package openml.algorithms;

import weka.classifiers.Classifier;

public class WekaAlgorithm {

	public static String getVersion(String algorithm) {
		String version = "undefined";
		try {
			Classifier classifier = (Classifier) Class.forName(algorithm).newInstance();
			version = classifier.getRevision();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}
}

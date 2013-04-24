package openml.algorithms;

import weka.core.Instances;

public class InstancesHelper {

	public static void setTargetAttribute( Instances instances, String classAttribute ) throws Exception {
		for(int i = 0; i < instances.numAttributes(); ++i ) {
			if(instances.attribute(i).name().equals(classAttribute)) {
				instances.setClassIndex(i);
				return;
			}
		}
		throw new Exception("classAttribute " + classAttribute + " non-existant on dataset. ");
	}
}

package openml.algorithms;

import openml.xml.Task;
import openml.xml.Task.Input.Estimation_procedure;

public class TaskInformation {

	public static int getNumberOfRepeats( Task t ) throws Exception {
		Estimation_procedure ep = getEstimationProcedure(t);
		for(int i = 0; i < ep.getParameters().length; ++i) {
			if(ep.getParameters()[i].getName().equals("number_repeats") ) {
				return Integer.parseInt(ep.getParameters()[i].getValue());
			}
		}
		throw new Exception("Tasks estimation procedure does not contain \"number_repeats\"");
	}
	
	private static Estimation_procedure getEstimationProcedure( Task t ) throws Exception {
		for( int i = 0; i < t.getInputs().length; ++i ) {
			System.out.println("name: " + t.getInputs()[i].getName());
			if(t.getInputs()[i].getName().equals("estimation_procedure") ) {
				return t.getInputs()[i].getEstimation_procedure();
			}
		}
		throw new Exception("Task does not define an estimation procedure. ");
	}
}

package openml.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import openml.xml.Run.Parameter_setting;

public class OptionParser {

	public static Parameter_setting[] parseParameters(String algorithm, String parameters) {
		Map<String,Parameter_setting> aps = new HashMap<String,Parameter_setting>();
		if(parameters.contains("--")) {
			aps.putAll(parseSimpleParameterString(algorithm, parameters.substring(0, parameters.indexOf("--")-1)));
			if(aps.containsKey(algorithm + "_W")) {
				String baseAlgorithm = aps.get(algorithm + "_W").getValue();
				String algorithmVersion = baseAlgorithm + "(" + WekaAlgorithm.getVersion(baseAlgorithm) + ")";
				aps.putAll(parseSimpleParameterString(algorithmVersion, parameters.substring(parameters.indexOf("--")+2)));
			}
		} else {
			aps.putAll(parseSimpleParameterString(algorithm, parameters));
		}
		Collection<Parameter_setting> returnValue = aps.values();
		return returnValue.toArray(new Parameter_setting[returnValue.size()]);
	}
	
	private static Map<String,Parameter_setting> parseSimpleParameterString(String algorithm, String parameters) {
		//System.out.println("Parsing: " + parameters);
		Map<String,Parameter_setting> aps = new HashMap<String,Parameter_setting>();
		String[] parametersSplitted = splitSafe('-', parameters); 
		
		for(int i = 0; i < parametersSplitted.length; ++i) {
			String[] currentParam = parametersSplitted[i].split(" ");
			if(currentParam.length == 2) {
				aps.put(algorithm + "_" + currentParam[0], new Parameter_setting(algorithm, currentParam[0], currentParam[1]));
			} else if(currentParam.length == 1) {
				aps.put(algorithm + "_" + currentParam[0], new Parameter_setting(algorithm, currentParam[0], "true"));
			}
		}
		return aps;
	}
	
	/*
	 * Splits a string (usually on delimiter "-" (hyphen)) but only if the next character is a letter
	 */
	private static String[] splitSafe( char delimiter, String subject ) {
		ArrayList<String> result = new ArrayList<String>();
		int cursor = -1;
		for(int i = 0; i < subject.length(); ++i) {
			if(subject.charAt(i) == delimiter && isNumeric(subject.charAt(i+1)) == false ) {
				if(cursor >= 0 )
					result.add(subject.substring(cursor+1,i).trim());
				cursor = i;
			}
		}
		if(cursor+1 < subject.length()) {
			result.add(subject.substring(cursor+1,subject.length()).trim());
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	private static boolean isNumeric( char c) {
		return (c >= '0' && c <= '9');
	}
}

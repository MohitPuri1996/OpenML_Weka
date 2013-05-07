package openml.algorithms;

public class ArrayUtils {

	public static String[] addAll( String[] a, String[] b ) {
		String[] res = new String[a.length+b.length];
		for(int i = 0; i < a.length + b.length; ++i) {
			if( i < a.length )
				res[i] = a[i];
			else 
				res[i] = b[i-a.length];
		}
		return res;
	}
}

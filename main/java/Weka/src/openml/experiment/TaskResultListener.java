package openml.experiment;

import weka.core.Attribute;
import weka.core.Instance;
import weka.experiment.InstancesResultListener;
import weka.experiment.ResultProducer;

public class TaskResultListener extends InstancesResultListener {

	private static final long serialVersionUID = -942137631914454114L;

	@Override
	public void acceptResult(ResultProducer rp, Object[] key, Object[] result)
			throws Exception {

		for( Object k : key ) {
			System.out.println((String) k );
		}
		System.out.println("---");
		for( Object k : result ) {
			System.out.println((String) k );
		}
		if (m_RP != rp) {
			throw new Error("Unrecognized ResultProducer sending results!!");
		}

		Instance newInst = new Instance(m_AttributeTypes.length);
		for (int i = 0; i < m_AttributeTypes.length; i++) {
			Object val = null;
			if (i < key.length) {
				val = key[i];
			} else {
				val = result[i - key.length];
			}
			if (val == null) {
				newInst.setValue(i, Instance.missingValue());
			} else {
				switch (m_AttributeTypes[i]) {
				case Attribute.NOMINAL:
					String str = (String) val;
					Double index = (Double) m_NominalIndexes[i].get(str);
					if (index == null) {
						index = new Double(m_NominalStrings[i].size());
						m_NominalIndexes[i].put(str, index);
						m_NominalStrings[i].addElement(str);
					}
					newInst.setValue(i, index.doubleValue());
					break;
				case Attribute.NUMERIC:
					double dou = ((Double) val).doubleValue();
					newInst.setValue(i, (double) dou);
					break;
				default:
					newInst.setValue(i, Instance.missingValue());
				}
			}
		}
		System.out.println(newInst);
		m_Instances.addElement(newInst);
	}
}

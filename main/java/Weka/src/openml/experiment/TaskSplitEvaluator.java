package openml.experiment;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Summarizable;
import weka.experiment.ClassifierSplitEvaluator;

public class TaskSplitEvaluator extends ClassifierSplitEvaluator {

	private static final long serialVersionUID = -73425852822213L;

	/** The length of a key */
	protected static final int KEY_SIZE = 4;

	private static final String SCHEMA_FIELD_NAME = "Scheme";
	private static final String SCHEMA_VERSION_FIELD_NAME = "Scheme_version";
	private static final String SCHEMA_OPTIONS_FIELD_NAME = "Scheme_options";
	private static final String SCHEMA_VERSION_ID_FIELD_NAME = "Scheme_version_ID";

	private transient FastVector recentPredictions;

	/**
	 * Gets the results for the supplied train and test datasets. Now performs a
	 * deep copy of the classifier before it is built and evaluated (just in
	 * case the classifier is not initialized properly in buildClassifier()).
	 * 
	 * @param train
	 *            the training Instances.
	 * @param test
	 *            the testing Instances.
	 * @return the results stored in an array. The objects stored in the array
	 *         may be Strings, Doubles, or null (for the missing value).
	 * @throws Exception
	 *             if a problem occurs while getting the results
	 */
	public Object[] getResult(Instances train, Instances test) throws Exception {

		if (train.classAttribute().type() != Attribute.NOMINAL) {
			throw new Exception("Class attribute is not nominal!");
		}
		if (m_Template == null) {
			throw new Exception("No classifier has been specified");
		}
		int addm = (m_AdditionalMeasures != null) ? m_AdditionalMeasures.length
				: 0;
		int overall_length = RESULT_SIZE + addm;
		overall_length += NUM_IR_STATISTICS;
		overall_length += NUM_WEIGHTED_IR_STATISTICS;
		if (getAttributeID() >= 0)
			overall_length += 1;
		if (getPredTargetColumn())
			overall_length += 2;

		ThreadMXBean thMonitor = ManagementFactory.getThreadMXBean();
		boolean canMeasureCPUTime = thMonitor.isThreadCpuTimeSupported();
		if (canMeasureCPUTime && !thMonitor.isThreadCpuTimeEnabled())
			thMonitor.setThreadCpuTimeEnabled(true);

		Object[] result = new Object[overall_length];
		Evaluation eval = new Evaluation(train);
		m_Classifier = Classifier.makeCopy(m_Template);
		double[] predictions;
		long thID = Thread.currentThread().getId();
		long CPUStartTime = -1, trainCPUTimeElapsed = -1, testCPUTimeElapsed = -1, trainTimeStart, trainTimeElapsed, testTimeStart, testTimeElapsed;

		// training classifier
		trainTimeStart = System.currentTimeMillis();
		if (canMeasureCPUTime)
			CPUStartTime = thMonitor.getThreadUserTime(thID);
		m_Classifier.buildClassifier(train);
		if (canMeasureCPUTime)
			trainCPUTimeElapsed = thMonitor.getThreadUserTime(thID)
					- CPUStartTime;
		trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;

		// testing classifier
		testTimeStart = System.currentTimeMillis();
		if (canMeasureCPUTime)
			CPUStartTime = thMonitor.getThreadUserTime(thID);
		predictions = eval.evaluateModel(m_Classifier, test);
		// ADDED BY JvR: the only "new thing" to this overriden function.
		recentPredictions = eval.predictions();
		if (canMeasureCPUTime)
			testCPUTimeElapsed = thMonitor.getThreadUserTime(thID)
					- CPUStartTime;
		testTimeElapsed = System.currentTimeMillis() - testTimeStart;
		thMonitor = null;

		m_result = eval.toSummaryString();
		// The results stored are all per instance -- can be multiplied by the
		// number of instances to get absolute numbers
		int current = 0;
		result[current++] = new Double(train.numInstances());
		result[current++] = new Double(eval.numInstances());
		result[current++] = new Double(eval.correct());
		result[current++] = new Double(eval.incorrect());
		result[current++] = new Double(eval.unclassified());
		result[current++] = new Double(eval.pctCorrect());
		result[current++] = new Double(eval.pctIncorrect());
		result[current++] = new Double(eval.pctUnclassified());
		result[current++] = new Double(eval.kappa());

		result[current++] = new Double(eval.meanAbsoluteError());
		result[current++] = new Double(eval.rootMeanSquaredError());
		result[current++] = new Double(eval.relativeAbsoluteError());
		result[current++] = new Double(eval.rootRelativeSquaredError());

		result[current++] = new Double(eval.SFPriorEntropy());
		result[current++] = new Double(eval.SFSchemeEntropy());
		result[current++] = new Double(eval.SFEntropyGain());
		result[current++] = new Double(eval.SFMeanPriorEntropy());
		result[current++] = new Double(eval.SFMeanSchemeEntropy());
		result[current++] = new Double(eval.SFMeanEntropyGain());

		// K&B stats
		result[current++] = new Double(eval.KBInformation());
		result[current++] = new Double(eval.KBMeanInformation());
		result[current++] = new Double(eval.KBRelativeInformation());

		// IR stats
		result[current++] = new Double(eval.truePositiveRate(m_IRclass));
		result[current++] = new Double(eval.numTruePositives(m_IRclass));
		result[current++] = new Double(eval.falsePositiveRate(m_IRclass));
		result[current++] = new Double(eval.numFalsePositives(m_IRclass));
		result[current++] = new Double(eval.trueNegativeRate(m_IRclass));
		result[current++] = new Double(eval.numTrueNegatives(m_IRclass));
		result[current++] = new Double(eval.falseNegativeRate(m_IRclass));
		result[current++] = new Double(eval.numFalseNegatives(m_IRclass));
		result[current++] = new Double(eval.precision(m_IRclass));
		result[current++] = new Double(eval.recall(m_IRclass));
		result[current++] = new Double(eval.fMeasure(m_IRclass));
		result[current++] = new Double(eval.areaUnderROC(m_IRclass));

		// Weighted IR stats
		result[current++] = new Double(eval.weightedTruePositiveRate());
		result[current++] = new Double(eval.weightedFalsePositiveRate());
		result[current++] = new Double(eval.weightedTrueNegativeRate());
		result[current++] = new Double(eval.weightedFalseNegativeRate());
		result[current++] = new Double(eval.weightedPrecision());
		result[current++] = new Double(eval.weightedRecall());
		result[current++] = new Double(eval.weightedFMeasure());
		result[current++] = new Double(eval.weightedAreaUnderROC());

		// Timing stats
		result[current++] = new Double(trainTimeElapsed / 1000.0);
		result[current++] = new Double(testTimeElapsed / 1000.0);
		if (canMeasureCPUTime) {
			result[current++] = new Double(
					(trainCPUTimeElapsed / 1000000.0) / 1000.0);
			result[current++] = new Double(
					(testCPUTimeElapsed / 1000000.0) / 1000.0);
		} else {
			result[current++] = new Double(Instance.missingValue());
			result[current++] = new Double(Instance.missingValue());
		}

		// sizes
		ByteArrayOutputStream bastream = new ByteArrayOutputStream();
		ObjectOutputStream oostream = new ObjectOutputStream(bastream);
		oostream.writeObject(m_Classifier);
		result[current++] = new Double(bastream.size());
		bastream = new ByteArrayOutputStream();
		oostream = new ObjectOutputStream(bastream);
		oostream.writeObject(train);
		result[current++] = new Double(bastream.size());
		bastream = new ByteArrayOutputStream();
		oostream = new ObjectOutputStream(bastream);
		oostream.writeObject(test);
		result[current++] = new Double(bastream.size());

		// IDs
		if (getAttributeID() >= 0) {
			String idsString = "";
			if (test.attribute(m_attID).isNumeric()) {
				if (test.numInstances() > 0)
					idsString += test.instance(0).value(m_attID);
				for (int i = 1; i < test.numInstances(); i++) {
					idsString += "|" + test.instance(i).value(m_attID);
				}
			} else {
				if (test.numInstances() > 0)
					idsString += test.instance(0).stringValue(m_attID);
				for (int i = 1; i < test.numInstances(); i++) {
					idsString += "|" + test.instance(i).stringValue(m_attID);
				}
			}
			result[current++] = idsString;
		}

		if (getPredTargetColumn()) {
			if (test.classAttribute().isNumeric()) {
				// Targets
				if (test.numInstances() > 0) {
					String targetsString = "";
					targetsString += test.instance(0).value(test.classIndex());
					for (int i = 1; i < test.numInstances(); i++) {
						targetsString += "|"
								+ test.instance(i).value(test.classIndex());
					}
					result[current++] = targetsString;
				}

				// Predictions
				if (predictions.length > 0) {
					String predictionsString = "";
					predictionsString += predictions[0];
					for (int i = 1; i < predictions.length; i++) {
						predictionsString += "|" + predictions[i];
					}
					result[current++] = predictionsString;
				}
			} else {
				// Targets
				if (test.numInstances() > 0) {
					String targetsString = "";
					targetsString += test.instance(0).stringValue(
							test.classIndex());
					for (int i = 1; i < test.numInstances(); i++) {
						targetsString += "|"
								+ test.instance(i).stringValue(
										test.classIndex());
					}
					result[current++] = targetsString;
				}

				// Predictions
				if (predictions.length > 0) {
					String predictionsString = "";
					predictionsString += test.classAttribute().value(
							(int) predictions[0]);
					for (int i = 1; i < predictions.length; i++) {
						predictionsString += "|"
								+ test.classAttribute().value(
										(int) predictions[i]);
					}
					result[current++] = predictionsString;
				}
			}
		}

		if (m_Classifier instanceof Summarizable) {
			result[current++] = ((Summarizable) m_Classifier).toSummaryString();
		} else {
			result[current++] = null;
		}

		for (int i = 0; i < addm; i++) {
			if (m_doesProduce[i]) {
				try {
					double dv = ((AdditionalMeasureProducer) m_Classifier)
							.getMeasure(m_AdditionalMeasures[i]);
					if (!Instance.isMissingValue(dv)) {
						Double value = new Double(dv);
						result[current++] = value;
					} else {
						result[current++] = null;
					}
				} catch (Exception ex) {
					System.err.println(ex);
				}
			} else {
				result[current++] = null;
			}
		}

		if (current != overall_length) {
			throw new Error("Results didn't fit RESULT_SIZE");
		}
		return result;
	}

	/**
	 * Gets the data types of each of the key columns produced for a single run.
	 * The number of key fields must be constant for a given SplitEvaluator.
	 * 
	 * @return an array containing objects of the type of each key column. The
	 *         objects should be Strings, or Doubles.
	 */
	public Object[] getKeyTypes() {

		Object[] keyTypes = new Object[KEY_SIZE];
		keyTypes[0] = "";
		keyTypes[1] = "";
		keyTypes[2] = "";
		keyTypes[3] = "";
		return keyTypes;
	}

	/**
	 * Gets the names of each of the key columns produced for a single run. The
	 * number of key fields must be constant for a given SplitEvaluator.
	 * 
	 * @return an array containing the name of each key column
	 */
	public String[] getKeyNames() {

		String[] keyNames = new String[KEY_SIZE];
		keyNames[0] = SCHEMA_FIELD_NAME;
		keyNames[1] = SCHEMA_VERSION_FIELD_NAME;
		keyNames[2] = SCHEMA_OPTIONS_FIELD_NAME;
		keyNames[3] = SCHEMA_VERSION_ID_FIELD_NAME;
		return keyNames;
	}

	/**
	 * Gets the key describing the current SplitEvaluator. For example This may
	 * contain the name of the classifier used for classifier predictive
	 * evaluation. The number of key fields must be constant for a given
	 * SplitEvaluator.
	 * 
	 * @return an array of objects containing the key.
	 */
	public Object[] getKey() {

		Object[] key = new Object[KEY_SIZE];
		key[0] = m_Template.getClass().getName();
		key[1] = m_Template.getRevision();
		key[2] = m_ClassifierOptions;
		key[3] = m_ClassifierVersion;
		return key;
	}

	public FastVector recentPredictions() throws Exception {
		if (recentPredictions != null)
			return recentPredictions;
		throw new Exception("No predictions set by SplitEvaluator. ");
	}
}

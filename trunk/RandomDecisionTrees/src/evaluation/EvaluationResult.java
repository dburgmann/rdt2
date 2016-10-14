package evaluation;

import java.util.List;

import evaluation.measure.Measure;
import rdt.essentials.RDTException;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.CollectorType;
import weka.core.Instance;

/**
 * Class which represents the results of an evaluation of a collector. During the initialization
 * you can specify which measures should be evaluated in this EvaluationResult. Afterwards you can
 * simple call the method "checkPrediction(Instance, Collector)" to update the measures. The 
 * instances contains the ground truth values for the prediction and the collector is used to
 * generate the prediction. Each learning task which is performed by a model should have its 
 * own EvaluationResult during the evaluation process.
 * 
 * @author MK
 */
public class EvaluationResult {
	
	/**
	 * The measures which are evaluated in this EvaluationResult
	 */
	private List<Measure> measures;
	
	/**
	 * The type of the collector which is evaluated in this EvaluationResult
	 */
	private CollectorType ct;
	
	/**
	 * Time used to train the model.
	 */
	private long buildTime;
	
	/**
	 * Time used to test the model.
	 */
	private long testTime;
	
	
	/**
	 * Creates a new EvaluationResult for the given collector-type. The given measures
	 * will be evaluated for this collector.
	 * 
	 * @param ct the collector-type
	 * @param measure the measures which will be evaluated in this EvaluationResult
	 */
	public EvaluationResult(CollectorType ct, List<Measure> measures){
		this.measures = measures;
		this.ct = ct;
	}
	
	/**
	 * Updates all measures with the given instance and the collector which is evaluated in
	 * this EvaluationResult. The information of the collector is used to generate the prediction.
	 * 
	 * @param inst the instance which contains the ground truth values for the class/labels
	 * @param c the collector which is used to produce the prediction
	 */
	public void checkPrediction(Instance inst, Collector c) throws RDTException{
		int[] attributeIds = c.getUsedAttributes();
		double[] truth = new double[attributeIds.length];
		double[] prediction = c.getPrediction();
		
		for(int i=0; i<truth.length; i++){
			truth[i] = inst.value(attributeIds[i]);
		}
		
		for(Measure m : measures){
			m.update(truth, prediction);
		}
	}
	
	/**
	 * Returns a short summary of the results.
	 * 
	 * @return a short summary of the results
	 */
	public String getResults(){
		StringBuilder sb = new StringBuilder();
		
		for(Measure m : measures){
			sb.append(m.toString());
			sb.append("\n");
		}
		
		return ct + "\n" + sb.toString();
	}
	
	/**
	 * Returns the value of the i-th measure.
	 * 
	 * @param i the number of the measure in the list
	 * @return the value of the i-th measure
	 */
	public double getValue(int i){
		return measures.get(i).getValue();
	}
	
	/**
	 * Returns all the measures which are evaluated in this EvaluationResult. 
	 * 
	 * @return all measures which are evaluated in this EvaluationResult
	 */
	public List<Measure> getMeasure(){
		return measures;
	}
	
	/**
	 * Returns the collector-type for which this EvaluationResult have been created.
	 * 
	 * @return the collector-type for which this EvaluationResult have been created
	 */
	public CollectorType getCollectorType(){
		return ct;
	}

	/**
	 * Returns the time which has been used to train the model (the time which has been
	 * used for the evaluation is not included).
	 * 
	 * @return the time which has been used to train the model
	 */
	public long getBuildTime() {
		return buildTime;
	}

	/**
	 * Sets the time which has been used to train the model (the time which has been
	 * used for the evaluation is not included).
	 * 
	 * @param buildTime the time which has been used to train the model
	 */
	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
	}

	/**
	 * Returns the time which has been used to evaluate the model.
	 * 
	 * @return the time which has been used to evaluate the model
	 */
	public long getTestTime() {
		return testTime;
	}

	/**
	 * Sets the time which has been used to evaluate the model (the time which was
	 * used for the training is not included).
	 * 
	 * @param testTime the time which has been used to evaluate the model
	 */
	public void setTestTime(long testTime) {
		this.testTime = testTime;
	}
	
	/**
	 * Creates a new EvaluationResult-array for the given collectors (learning tasks).
	 * 
	 * @param cp the different collectors which have been used in the model
	 * @return a new EvaluationResult-array for the given collectors
	 */
	public static EvaluationResult[] initEvaluationResult(CollectorPreferences cp) throws RDTException{
		
		Collector[] cols = cp.getNewCollectors();
		
		EvaluationResult[] er = new EvaluationResult[cols.length];
		for(int i=0; i<cols.length; i++){
			er[i] = new EvaluationResult(cols[i].getType(), cols[i].getMeasures());
		}
		return er;
	}
	
}

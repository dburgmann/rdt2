package evaluation.measure;

/**
 * Measure Interface. A measure is used in the evaluation process to determine the effectiveness
 * of a learning algorithm. For example a measure can be accuracy, recall or precision. A measure
 * has to be updated with each prediction of the model which is evaluated. To update the measure 
 * it is necessary to know the correct values (ground truth) and the predicted values (prediction).
 * 
 * @author MK
 */
public interface Measure {

	/**
	 * Updates the measure with the ground truth and the prediction. 
	 *  
	 * @param truth the ground truth (correct values)
	 * @param pred the predicted values (predicted values)
	 */
	public abstract void update(double[] truth, double[] pred);
	
	/**
	 * @return the name of the measure
	 */
	public abstract String getName();
	
	/**
	 * @return the ideal value for the measure
	 */
	public abstract double getIdealValue();
	
	/**
	 * @return the value of the measure
	 */
	public abstract double getValue();
	
}

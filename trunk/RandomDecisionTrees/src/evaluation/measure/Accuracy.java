package evaluation.measure;

/**
 * Class for the measure accuracy. Accuracy measures the amount of correct classified instances.
 * This implementation is currently only usable for the evaluation of a classification task.
 * 
 * @author MK
 */
public class Accuracy implements Measure{

	/**
	 * Counter for the correct predictions.
	 */
	private double countCorrect;
	
	/**
	 * Counter for the number of updates.
	 */
	private double numUpdates;
	
	/**
	 * Create a new Accuracy-measure
	 */
	public Accuracy(){
		this.countCorrect = 0;
		this.numUpdates = 0;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("Accuracy: ");
		sb.append(countCorrect/numUpdates);
		
		return sb.toString();
	}
	
	@Override
	public void update(double[] truth, double[] pred) {
		if(truth[0] == pred[0]){
			countCorrect++;
		}
		numUpdates++;
	}

	@Override
	public double getIdealValue() {
		return 1;
	}

	@Override
	public double getValue() {
		return (countCorrect/numUpdates);
	}

	@Override
	public String getName() {
		return "Accuracy";
	}

}

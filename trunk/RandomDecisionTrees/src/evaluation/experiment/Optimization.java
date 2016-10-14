package evaluation.experiment;

import evaluation.dataset.Dataset;
import rdt.model.ModelType;
import rdt.tree.collector.CollectorPreferences;

/**
 * Under construction...
 * 
 * @author MK
 */
public abstract class Optimization implements Experiment{

	
	protected abstract ModelType getModelType();
	protected abstract Object[][] getParameters();
	protected abstract int numFolds();
	
	
	@Override
	public void runExperiment(Dataset dataset) throws Exception {
		CollectorPreferences cp = dataset.getCollectorPreferences();
		
		

		
		Object[][] params = getParameters();
		int[] currentState = new int[params.length];

		
		
		
		
		
		
		
		
	}
	
	private void recursiveExecution(Object[][] params, int[] currentState){
		
	}
	
	
	
	private int[] updateState(int[] currentState){
		return null;
	}
	
	private int computeNumberOfIterations(Object[][] parameters){
		
		int numberIterations = 1;
		
		for(int i=0; i<parameters.length; i++){	
			numberIterations *= parameters[i].length;
		}
		
		return numberIterations;
	}

}

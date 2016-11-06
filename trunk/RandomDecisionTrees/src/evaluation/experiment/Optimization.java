package evaluation.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import evaluation.Evaluation;
import evaluation.EvaluationResultsSummary;
import evaluation.dataset.Dataset;
import evaluation.engine.Models;
import evaluation.measure.Measure;
import rdt.essentials.RDTException;
import rdt.model.Model;
import rdt.model.ModelType;
import rdt.tree.collector.CollectorPreferences;

/**
 * Class to run an optimization. The algorithm will evaluate each possible parameter 
 * combination which is specified in the method "getParameters()" and save the best
 * parameter-combinations for each evaluation measure into a file.
 * For each parameter-combination a cross-validation is performed on the trainings-set
 * of the dataset. The test-set of the dataset will not be used. 
 * 
 * The results of these experiments will be saved at the location 
 * "results/optimization/EXPERIMENT_NAME/DATASET_NAME".
 * 
 * Take a look at some experiments which implements this abstract class to get a better 
 * understanding how this class works.
 * 
 * IMPORTANT: Currently this experiment is only designed for the use of one collector in
 * the models.
 * 
 * @author MK
 */
public abstract class Optimization implements Experiment{

	
	/**
	 * Returns the type of the models which will be evaluated in this experiment.
	 * 
	 * @return the type of the models which will be evaluated in this experiment
	 */
	protected abstract ModelType getModelType();
	
	/**
	 * Returns an array for each parameter of the specific model. The first dimension defines 
	 * the parameter and the second dimension defines the different values for that parameter
	 * that will be used in this experiment. 
	 * 
	 * @return the parameter values for the model
	 */
	protected abstract Object[][] getParameters();
	
	/**
	 * Returns the number of folds. This method is only used if you perform a
	 * cross-validation.
	 * 
	 * @return the number of folds
	 */
	protected abstract int getNumFolds();
	
	/**
	 * Returns the number of rounds each evaluation with specific parameter values will be
	 * repeated. For example a 10-fold cross-validation will be repeated five times if we
	 * set the value for the rounds to five. All the results will be averaged.
	 * 
	 * @return the number of rounds each evaluation with specific parameter values
	 */
	protected abstract int getRounds();
	
	/**
	 * Returns the name/type of the experiment. This method is compulsory to save the results
	 * in the correct folder which will be created during the runtime.
	 * 
	 * @return the name/type of the experiment
	 */
	protected abstract ExperimentType getExperimentType();
	
	private Double[] bestValues;
	private String[] data;
	
	@Override
	public void runExperiment(Dataset dataset) throws Exception {
		CollectorPreferences cp = dataset.getCollectorPreferences();
		
		//TODO Here we have the assumption that only one collector has been used in in the model!!!
		bestValues = new Double[cp.getCollectors().get(0).getMeasures().size()];
		data = new String[cp.getCollectors().get(0).getMeasures().size() + 1];	//+1 cause of header
		
		Object[][] params = getParameters();
		int[] curState = new int[params.length];

		int numIterations = computeNumberOfIterations(params);
		int curIteration = 1;
		boolean finishFlag = false;
		
		System.out.println("Evaluation-Start");
		System.out.println("Experiment-Type: " + getExperimentType().name());
		System.out.println("Dataset: " + dataset.getDatasetType().name());
		
		while(!finishFlag){
			
			Object[] paramsForState = getParamsForState(params, curState);
			
			printSomeInformation(paramsForState, curIteration, numIterations);
			
			runModelWithParams(dataset, paramsForState);
			
			//Update curState
			for(int j=curState.length-1; 0<=j; j--){
				if((curState[j]+1) < params[j].length){
					curState[j]++;
					break;
				}else{
					if(j==0){
						finishFlag = true;
					}
					curState[j] = 0;
				}
			}
			curIteration++;
		}
		
		updateResults(dataset, true);
	}
	
	
	private void printSomeInformation(Object[] params, int curIteration, int numIterations) throws RDTException{
		
		String[] paramNames = Models.getModelParameterNames(getModelType());
		
		StringBuilder sb = new StringBuilder();
		sb.append(curIteration);
		sb.append("/");
		sb.append(numIterations);
		sb.append("; ");
		sb.append("model=");
		sb.append(getModelType().name());
		
		for(int j=0; j<params.length; j++){
			sb.append("; ");
			sb.append(paramNames[j]).append("=").append(params[j]);
		}
		
		sb.append("; ");
		sb.append((new Date(System.currentTimeMillis()).toString()));
		
		System.out.println(sb.toString());
	}
	
	private void runModelWithParams(Dataset dataset, Object[] paramsForState) throws IOException, RDTException, Exception {

		Model model = Models.getModel(getModelType(), dataset.getCollectorPreferences(), paramsForState);

		EvaluationResultsSummary ers =  Evaluation.evaluateCV(model, dataset.getCVInstances(), getNumFolds(), getRounds());
		
		//TODO Here we have the assumption that only one collector has been used in in the model!!!
		double[] avgValues = ers.getAveragedValues()[0];
		List<Measure> measures = ers.getAllResults().get(0)[0].getMeasure();
		boolean updated = false;
		
		for(int i=0; i<avgValues.length; i++){
			
			if(bestValues[i] == null){
				saveValues(paramsForState, ers, i);
				bestValues[i] = avgValues[i];
				updated = true;
				continue;
			}
			
			double idealValue = measures.get(i).getIdealValue();
			double bestDiff = Math.abs(idealValue - bestValues[i]);
			double curDiff = Math.abs(idealValue - avgValues[i]);
			
			if(bestDiff > curDiff){
				saveValues(paramsForState, ers, i);
				bestValues[i] = avgValues[i];
				updated = true;
			}
		}
		
		if(updated){
			updateResults(dataset, false);
		}		
	}
	
	private void saveValues(Object[] paramsForState, EvaluationResultsSummary ers, int index) throws RDTException {
		
		//TODO Here we have the assumption that only one collector has been used in in the model!!!
		String[] measureNames = ers.getMeasureNames()[0];
		String[] paramNames = Models.getModelParameterNames(getModelType());
		
		double[] avgValues = ers.getAveragedValues()[0];
		List<Measure> measures = ers.getAllResults().get(0)[0].getMeasure();

		//print header
		if(data[0] == null){
			StringBuilder sb = new StringBuilder();
					
			sb.append(",");
					
			for(int i=0; i<paramNames.length; i++){
				sb.append("PARAM:").append(paramNames[i]).append(",");
			}
					
			for(int i=0; i<measureNames.length; i++){
				sb.append("MEASURE:").append(measureNames[i]);
				if(i != (measureNames.length-1)){
					sb.append(",");
				}
			}
					
			sb.append(",MEASURE:Build_time,MEASURE:Test_time");

			data[0] = sb.toString();
		}	
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Best_");
		sb.append(measures.get(index).getName());
		sb.append(",");
		
		for(int i=0; i<paramsForState.length; i++){
			sb.append(paramsForState[i]);
			sb.append(",");
		}
		
		for(int j=0; j<avgValues.length; j++){
			sb.append(String.format("%.4f", Math.round(avgValues[j]*10000)/10000.0).replaceAll(",", "."));
			sb.append(",");
		}
		
		sb.append((ers.getAveragedBuildTime()/1000.0));
		sb.append(",");
		sb.append((ers.getAveragedTestTime()/1000.0));
		
		data[index+1] = sb.toString();
		
	}

	private void updateResults(Dataset dataset, boolean lastRound) throws IOException, RDTException{
		ModelType model = getModelType();
		
		String fileName = model.name();
		StringBuilder path = new StringBuilder();
	
		path.append("results/optimization/");
		path.append(getExperimentType().name());
		path.append("/");
		path.append(dataset.getDatasetType().name());
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<data.length; i++){
			
			sb.append(data[i]);
			
			if((i+1) < data.length){
				sb.append("\n");
			}
		}
		
		if(lastRound){
			System.out.println("CREATE FINAL RESULT-FILE AND DELETE TEMPORARY RESULT-FILE!");
			String tmpFileName = fileName + "_tmp";
			new File(path.toString() + "/" + tmpFileName.toString().replace(".", "_") + ".csv").delete();
		}else{
			System.out.println("UPDATE TEMPORARY RESULT-FILE!");
			fileName += "_tmp";
		}
		
		//Save content
		File f = new File(path.toString());
		f.mkdirs();
		f = new File(path.toString() + "/" + fileName.toString().replace(".", "_") + ".csv");
		f.createNewFile();
		FileWriter fw = new FileWriter(f.getPath());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(sb.toString());
		bw.close();
	}
	
	
	
	private Object[] getParamsForState(Object[][] params, int[] curState){
		Object[] p = new Object[params.length];
		for(int j=0; j<p.length; j++){
			p[j] = params[j][curState[j]];
		}
		return p;
	}
	

	
	private int computeNumberOfIterations(Object[][] parameters){
		
		int numberIterations = 1;
		
		for(int i=0; i<parameters.length; i++){	
			numberIterations *= parameters[i].length;
		}
		
		return numberIterations;
	}

}

package evaluation.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import evaluation.EvaluationResultsSummary;
import evaluation.Evaluation;
import evaluation.EvaluationType;
import evaluation.dataset.Dataset;
import evaluation.engine.Models;
import rdt.essentials.RDTException;
import rdt.model.Model;
import rdt.model.ModelType;

/**
 * Class for an experiment in which different models with specific parameter values are 
 * evaluated. This experiment is designed that the results of each model can be visualized
 * in a line plot. To perform such a visualization we need one parameter which is shared 
 * along all the evaluated models which can be plotted on the x-axis of the graph. For
 * example if we want to compare two ensembles then we can use the number of trees as the
 * variable parameter. By using a variable parameter we can evaluate how the model
 * is affected by the change of one parameter.
 * The results of these experiments will be saved at the location 
 * "results/visualization/EXPERIMENT_NAME/DATASET_NAME". You can use the visualizer which
 * is implemented in python to automatically visualize the results. The visualizer creates
 * an pdf-file in the same folder.
 * 
 * Take a look at some experiments which implements this abstract class to get a better 
 * understanding how this class works.
 * 
 * IMPORTANT: Currently this experiment is only designed for the use of one collector in
 * the models.
 * 
 * @author MK
 */
public abstract class CompareExperiment implements Experiment{

	/**
	 * Returns the types of the models which will be evaluated in this experiment.
	 * 
	 * @return the types of the models which will be evaluated in this experiment
	 */
	protected abstract ModelType[] getModelTypes();
	
	/**
	 * Returns an array for each parameter of the specific model. The first dimension
	 * defines the model, the second dimension defines the parameter and the last 
	 * dimension defines the different values for that parameter that will be used in
	 * this experiment. 
	 * 
	 * @return the parameter values for each model
	 */
	protected abstract Object[][][] getParameters();
	
	/**
	 * Returns the focus of each model as an array. The focus is the index of the parameter
	 * which will be mapped on the x-axis of the plot, so it is the variable parameter.
	 * 
	 * @return the focus of each model
	 */
	protected abstract int[] getFocuses();
	
	/**
	 * Returns the type of the evaluation. For example if the type is
	 * TEST_SET then a test-set evaluation will be performed.
	 * 
	 * @return the type of the evaluation
	 */
	protected abstract EvaluationType getEvaluationType();
	
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
	
	@Override
	public void runExperiment(Dataset dataset) throws Exception {
		ModelType[] modelTypes = getModelTypes();
		Object[][][] params = getParameters();
		int[] focuses = getFocuses();
		
		Set<ModelType> useEnumerations = computeUseEnumerations(modelTypes);
		
		int numIterations = computeNumberOfIterations(modelTypes, params, focuses);
		int curIteration = 1;
		
		System.out.println("Evaluation-Start");
		System.out.println("Experiment-Type: " + getExperimentType().name());
		System.out.println("Dataset: " + dataset.getDatasetType().name());
		
		for(int i=0; i<modelTypes.length; i++){
			ModelType model = modelTypes[i];
			int focus = focuses[i];
			int[] curState = new int[params[i].length];
			boolean finishFlag = false;
			
			while(!finishFlag){
				
				Object[][] paramsForState = getParamsForState(params[i], curState, focus);
				
				printSomeInformation(model, focus, paramsForState, curIteration, numIterations);
				
				runSingleModel(dataset, model, paramsForState, focus, useEnumerations, i, params);
				
				//Update curState
				for(int j=curState.length-1; 0<=j; j--){
					if(focus == j){
						if(focus == 0){
							finishFlag = true;
						}
						continue;
					}
					
					if((curState[j]+1) < params[i][j].length){
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
		}
		
	}
	
	private Set<ModelType> computeUseEnumerations(ModelType[] modelTypes) {
		Set<ModelType> set = new HashSet<ModelType>();
		for(int i=0; i<modelTypes.length; i++){
			for(int j=i; j<modelTypes.length; j++){
				if(modelTypes[i] == modelTypes[j] && i != j){
					set.add(modelTypes[i]);
				}
			}
		}
		return set;
	}
	
	private void printSomeInformation(ModelType model, int focus, Object[][] params, int curIteration, int numIterations) throws RDTException{
		
		String[] paramNames = Models.getModelParameterNames(model);
		
		StringBuilder sb = new StringBuilder();
		sb.append(curIteration);
		sb.append("/");
		sb.append(numIterations);
		sb.append("; ");
		sb.append("model=");
		sb.append(model.name());
		
		for(int j=0; j<params.length; j++){
			if(j != focus){
				sb.append("; ");
				sb.append(paramNames[j]).append("=").append(params[j][0]);
			}else{
				sb.append("; ");
				sb.append(paramNames[j]).append("=").append(Arrays.toString(params[j]));
			}
		}
		
		sb.append("; ");
		sb.append((new Date(System.currentTimeMillis()).toString()));
		
		System.out.println(sb.toString());
	}
	
	private Object[][] getParamsForState(Object[][] params, int[] curState, int focus){
		Object[][] p = new Object[params.length][];
		for(int j=0; j<p.length; j++){
			if(j == focus){
				p[j] = params[j];
			}else{
				p[j] = new Object[] {params[j][curState[j]]};
			}
		}
		return p;
	}
	
	private void runSingleModel(Dataset dataset, ModelType modelType, Object[][] params, int focus, Set<ModelType> useEnumerations, int number, Object[][][] allParams) throws IOException, Exception{
		
		EvaluationResultsSummary[] ers = new EvaluationResultsSummary[params[focus].length];
		
		for(int i=0; i<params[focus].length; i++){
			Object[] p = new Object[params.length];
			for(int j=0; j<p.length; j++){
				if(j == focus){
					p[j] = params[j][i];
				}else{
					p[j] = params[j][0];
				}
			}

			System.out.println("\t" + (i+1) + "/" + params[focus].length + " parameters = " + Arrays.toString(p) + " " + (new Date(System.currentTimeMillis()).toString()));
			
			ers[i] = doIteration(dataset, modelType, p);
			
			saveResults(dataset, modelType, params, focus, ers, useEnumerations, number, allParams);
		}
	}
	
	private EvaluationResultsSummary doIteration(Dataset dataset, ModelType modelType, Object[] params) throws IOException, Exception{
		
		Model model = Models.getModel(modelType, dataset.getCollectorPreferences(), params);
		EvaluationResultsSummary cer;
		
		switch(getEvaluationType()){
		case CROSS_VALIDATION:
			cer = Evaluation.evaluateCV(model, dataset.getCVInstances(), getNumFolds(), getRounds());
			break;
		case TEST_SET:
			cer = Evaluation.evaluateTestSet(model, dataset.getTrainInstances(), dataset.getTestInstances(), getRounds());
			break;
		default:
			throw new RDTException("EvaluationType does not exist!");
		}
		
		return cer;
	}
	
	private void saveResults(Dataset dataset, ModelType model, Object[][] params, int focus, EvaluationResultsSummary[] ers, Set<ModelType> useEnumerations, int number, Object[][][] allParams) throws IOException, RDTException{
		
		String[] paramNames = Models.getModelParameterNames(model);
		
		StringBuilder path = new StringBuilder();
		StringBuilder fileName = new StringBuilder();
		
		path.append("results/visualization/");
		path.append(getExperimentType().name());
		path.append("/");
		path.append(dataset.getDatasetType().name());
		
		fileName.append(model.name());
		if(useEnumerations.contains(model)){
			fileName.append("_");
			fileName.append(number);
		}
		
		for(int i=0; i<allParams[number].length; i++){
			if(allParams[number][i].length > 1 && i != focus){
				fileName.append("_");
				fileName.append(paramNames[i]).append("=");
				fileName.append(params[i][0]);
			}
		}
		
		//TODO Here we have the assumption that only one collector has been used in in the model!!!
		String[] measureNames = ers[0].getMeasureNames()[0];
		StringBuilder sb = new StringBuilder();
		sb.append("PARAM:").append(paramNames[focus]).append(",");
		
		//print header
		for(int i=0; i<paramNames.length; i++){
			if(i == focus){
				continue;
			}
			sb.append("PARAM:").append(paramNames[i]).append(",");
		}
		
		for(int i=0; i<measureNames.length; i++){
			sb.append("MEASURE:").append(measureNames[i]);
			if(i != (measureNames.length-1)){
				sb.append(",");
			}
		}
		
		sb.append(",MEASURE:Build_time,MEASURE:Test_time");
		sb.append("\n");		
		
		for(int i=0; i<ers.length; i++){
			
			if(ers[i] == null){
				continue;
			}
			
			sb.append(params[focus][i]);
			sb.append(",");
			
			for(int j=0; j<params.length; j++){
				
				if(j == focus){
					continue;
					
				}else{
					sb.append(params[j][0]);
					sb.append(",");
				}
				
			}

			double[] avgValues = ers[i].getAveragedValues()[0];
			
			for(int j=0; j<avgValues.length; j++){
				sb.append(String.format("%.4f", Math.round(avgValues[j]*10000)/10000.0).replaceAll(",", "."));
				sb.append(",");
			}
			
			sb.append(((double)ers[i].getAveragedBuildTime()/1000.0));
			sb.append(",");
			sb.append(((double)ers[i].getAveragedTestTime()/1000.0));
			sb.append("\n");
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
	
	private int computeNumberOfIterations(ModelType[] modelTypes, Object[][][] params, int[] focuses) throws RDTException{
		int numIterations = 0;
		
		for(int i=0; i<params.length; i++){
			int focus = focuses[i];
			int tmp = 1;
			for(int j=0; j<params[i].length; j++){
				if(j!=focus){
					tmp *= params[i][j].length;
				}
			}
			numIterations += tmp;
		}
		return numIterations;
	}

}

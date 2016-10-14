package evaluation;

import java.util.LinkedList;
import java.util.List;

import evaluation.measure.Measure;

/**
 * Class which summarizes the EvaluationResults of multiple evaluations. If you run multiple 
 * evaluations with the same collectors and you want to combine the results of the evaluations
 * you can use this class. This class simple computes the averaged values and the standard 
 * deviation for each specific collector and their measures by only adding the EvaluationResults
 * of each run.
 * 
 * @author MK
 */
public class EvaluationResultsSummary {
	
	/**
	 * The list which contains all the EvaluationResult-arrays which have been
	 * added to this EvaluationResultsSummary.
	 */
	private List<EvaluationResult[]> results;
	
	
	/**
	 * Creates a new EvaluationResultsSummary with an empty list of results.
	 */
	public EvaluationResultsSummary(){
		this.results = new LinkedList<EvaluationResult[]>();
	}
	
	/**
	 * Add the given EvaluationResult-array to the result-list.
	 * 
	 * @param the EvaluationResult-array which will be added
	 */
	public void addResult(EvaluationResult[] result){
		results.add(result);
	}
	
	/**
	 * Returns the averaged values of the measures which are used for each 
	 * collector. The first dimension of the array identifies the collector and the
	 * second dimension of the array identifies the measure of that specific collector.
	 * 
	 * @return the averaged values of the measures for each collector
	 */
	public double[][] getAveragedValues(){
		return computeAverage();
	}
	
	
	/**
	 * Returns the standard deviation of the measures which are used for each 
	 * collector. The first dimension of the array identifies the collector and the
	 * second dimension of the array identifies the measure of that specific collector.
	 * 
	 * @return the standard deviation of the measures for each collector
	 */
	public double[][] getStandardDeviation(){
		return computeStandardDeviation(computeAverage());
	}
	
	/**
	 * Returns a list of EvaluationResult-arrays. Each EvaluationResult-array contains the
	 * results of one specific evaluation which have been added to this EvaluationResultArray.
	 * 
	 * @return a list of EvaluationResult-arrays which have been added to this EvaluationResultArray
	 */
	public List<EvaluationResult[]> getAllResults(){
		return results;
	}
	
	/**
	 * Returns the averaged time which have been used to train the model (time to evaluate the
	 * model is not included).
	 * 
	 * @return the averaged time which have been used to train the model
	 */
	public double getAveragedBuildTime(){
		double buildTime = 0;
		
		for(EvaluationResult[] result : results){
			buildTime += result[0].getBuildTime();
		}
		return (buildTime/results.size());
	}
	
	/**
	 * Returns the averaged time which have been used to evaluate the model (time to train the
	 * model is not included).
	 * 
	 * @return the averaged time which have been used to evaluate the model
	 */
	public double getAveragedTestTime(){
		double testTime = 0;
		
		for(EvaluationResult[] result : results){
			testTime += result[0].getTestTime();
		}
		return (testTime/results.size());
	}
	
	/**
	 * Returns the measure names of the measures which are used for each collector. The
	 * first dimension of the array identifies the collector and the second dimension of 
	 * the array identifies the measure of that specific collector.
	 * 
	 * @return the name of the measures for each collector
	 */
	public String[][] getMeasureNames(){
		
		EvaluationResult[] result = results.get(0);
		String[][] names = new String[result.length][];
			
		for(int i=0; i<result.length; i++){
			List<Measure> measures = result[i].getMeasure();
			String[] tmp = new String[measures.size()];
			for(int j=0; j<measures.size(); j++){
				tmp[j] = measures.get(j).getName();
			}
			names[i] = tmp;
		}
		return names;
	}
	
	/**
	 * This method computes the average value of all measures.
	 * 
	 * @return the average value of all measures
	 */
	private double[][] computeAverage(){
		
		int numCollectors = results.get(0).length;
		
		double[][] avgResults = new double[numCollectors][];
		for(int i=0; i<numCollectors; i++){
			List<Measure> measures = results.get(0)[i].getMeasure();
			avgResults[i] = new double[measures.size()];
		}
		for(int i=0; i<results.size(); i++){
			for(int j=0; j<numCollectors; j++){
				List<Measure> measures = results.get(i)[j].getMeasure();	
				for(int k=0; k<measures.size(); k++){
					avgResults[j][k] += measures.get(k).getValue();
				}
			}
		}
		for(int i=0; i<avgResults.length; i++){
			for(int j=0; j<avgResults[i].length; j++){
				avgResults[i][j] /= results.size();
			}
		}
		
		return avgResults;
	}
	
	/** 
	 * This method computes the standard deviation of the value of all measures.
	 * 
	 * @return the standard deviation of the value of all measures
	 */
	private double[][] computeStandardDeviation(double[][] avgResults){
		
		int numCollectors = results.get(0).length;
		
		double[][] standardDeviation = new double[numCollectors][];
		
		for(int i=0; i<numCollectors; i++){
			List<Measure> measures = results.get(0)[i].getMeasure();
			standardDeviation[i] = new double[measures.size()];
		}
		for(int i=0; i<results.size(); i++){
			for(int j=0; j<numCollectors; j++){
				List<Measure> measures = results.get(i)[j].getMeasure();	
				for(int k=0; k<measures.size(); k++){
					double avgValue = avgResults[j][k];
					double curValue = measures.get(k).getValue();
					double diff = curValue - avgValue;	
					standardDeviation[j][k] += (diff * diff);
				}
			}
		}
		for(int i=0; i<standardDeviation.length; i++){
			for(int j=0; j<standardDeviation[i].length; j++){
				standardDeviation[i][j] /= results.size();
				standardDeviation[i][j] = Math.sqrt(standardDeviation[i][j]);
			}
		}
		
		return standardDeviation;
	}
	
	/**
	 * Returns a short summary of the results.
	 * 
	 * @return a short summary of the results
	 */
	public String getSummary(){
		StringBuilder sb = new StringBuilder();
		
		EvaluationResult[] firstResult = results.get(0);
		String[][] measureNames = getMeasureNames();
		double[][] averagedValues = getAveragedValues();
		double[][] standardDeviation = getStandardDeviation();
		double averagedBuildTime = getAveragedBuildTime();
		double averagedTestTime = getAveragedTestTime();
		
		for(int i=0; i<averagedValues.length; i++){
			sb.append(firstResult[i].getCollectorType().name()).append("\n");
			for(int j=0; j<averagedValues[i].length; j++){
				sb.append(measureNames[i][j]).append(": ");
				sb.append(String.format("%.4f", Math.round(averagedValues[i][j]*10000)/10000.0).replaceAll(",", "."));
				if(results.size() > 1){
					sb.append(" +/- ");
					sb.append(String.format("%.4f", Math.round(standardDeviation[i][j]*10000)/10000.0).replaceAll(",", "."));
				}
				sb.append("\n");
			}
			sb.append("Time to build: ");
			sb.append(((double)averagedBuildTime/1000.0)+" sec");
			sb.append("\n");
			sb.append("Time to test: ");
			sb.append(((double)averagedTestTime/1000.0)+" sec");
			sb.append("\n");
		}
		return sb.toString();
	}
}

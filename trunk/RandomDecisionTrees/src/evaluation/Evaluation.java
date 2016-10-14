package evaluation;

import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.model.Model;
import rdt.tree.collector.Collector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class to perform an evaluation of a model. The evaluation-methods either return
 * an array of EvaluationResult or an EvaluationResultSummary. The methods which 
 * return the array of EvaluationResult only perform the evaluation once. For each 
 * collector which was used in the model exists one EvaluationResult which evaluates
 * one specific learning task. 
 * The methods which return an EvaluationResultSummary perform the evaluation
 * multiple times. The parameter "rounds" indicates how many repetitions will be
 * performed. The EvaluationResultSummary class is a special class which summarizes
 * the results of the different rounds.
 * 
 * @author MK
 */
public class Evaluation {
	
	/**
	 * Performs a cross-validation with the given parameters.
	 * 
	 * @param model the model which will be evaluated
	 * @param insts the instances which will be used for the cross-validation
	 * @param folds the number of folds
	 * @return the results of the cross-validation (each EvaluationResult in the array
	 * represents one result of one  collector which was used in the model)
	 */
	public static EvaluationResult[] evaluateCV(Model model, RDTInstances insts, int folds) throws Exception {
		if(folds < 2){
			throw new RDTException("The number of folds must be greater than 1");
		}

		EvaluationResult[] er = EvaluationResult.initEvaluationResult(model.getCollectorPreferences());

		
		long buildTime = 0;
		long testTime = 0;
		long currentTime = 0;
		Instances workingSet = new Instances(insts.getDataSet());
		
		for(int i=0; i<folds; i++){
			
			RDTInstances train = new RDTInstances(workingSet.trainCV(folds, i), insts.getAttributes(), insts.getRestrictedAttributes(), insts.getFreeAttributes(), insts.getPath());
			Instances test = workingSet.testCV(folds, i);
			
			currentTime = System.currentTimeMillis();
			model.build(train);
			buildTime += System.currentTimeMillis() - currentTime;
			
			currentTime = System.currentTimeMillis();
			for(Instance inst : test){
				Collector[] collectors = model.predict(inst);
				
				for(int j=0; j<collectors.length; j++){
					er[j].checkPrediction(inst, collectors[j]);
				}
			}
			testTime += System.currentTimeMillis() - currentTime;
		}	
		
		for(int i=0; i<er.length; i++){
			er[i].setBuildTime(buildTime);
			er[i].setTestTime(testTime);
		}
		
		return er;
	}
	
	/**
	 * Performs a cross-validation with the given parameters.
	 * 
	 * @param model the model which will be evaluated
	 * @param insts the instances which will be used for the cross-validation
	 * @param folds the number of folds
	 * @param rounds the number of repetitions 
	 * @return the results of the cross-validation (each EvaluationResultSummary summarizes the
	 * results of the different rounds 
	 */
	public static EvaluationResultsSummary evaluateCV(Model model, RDTInstances insts, int folds, int rounds) throws Exception{
		EvaluationResultsSummary cmr = new EvaluationResultsSummary();
		
		for(int i=0; i<rounds; i++){
			cmr.addResult(evaluateCV(model, insts, folds));
		}
		
		return cmr;
	}
	
	/**
	 * Performs a Leave-One-Out-Cross-Validation with the given parameters.
	 * 
	 * @param model the model which will be evaluated
	 * @param insts the instances which will be used for the cross-validation
	 * @return the results of the cross-validation (each EvaluationResult in the array
	 * represents one result of one  collector which was used in the model)
	 */
	public static EvaluationResult[] evaluateLeaveOneOut(Model model, RDTInstances insts) throws Exception {
		return evaluateCV(model, insts, insts.getNumInstances());
	}
	
	/**
	 * Performs a test-set-evaluation with the given parameters.
	 * 
	 * @param model the model which will be evaluated
	 * @param trainInstances the instances which will be used to train the model
	 * @param testInstances the instances which will be used to evaluate the model
	 * @return the results of the test-set-evaluation (each EvaluationResult in the array
	 * represents one result of one  collector which was used in the model)
	 */
	public static EvaluationResult[] evaluateTestSet(Model model, RDTInstances trainInstances, RDTInstances testInstances) throws Exception{
		long currentTime = 0;
		long buildTime = 0;
		long testTime = 0;
		EvaluationResult[] er = EvaluationResult.initEvaluationResult(model.getCollectorPreferences());
		
		currentTime = System.currentTimeMillis();
		model.build(trainInstances);
		buildTime = System.currentTimeMillis() - currentTime;
		
		currentTime = System.currentTimeMillis();
		for(Instance inst : testInstances.getDataSet()){
				
			Collector[] collectors = model.predict(inst);
				
			for(int i=0; i<collectors.length; i++){
				er[i].checkPrediction(inst, collectors[i]);
			}	
		}
		testTime = System.currentTimeMillis() - currentTime;

		for(int i=0; i<er.length; i++){
			er[i].setBuildTime(buildTime);
			er[i].setTestTime(testTime);
		}
		
		return er;
	}
	
	/**
	 * Performs a test-set-evaluation with the given parameters.
	 * 
	 * @param model the model which will be evaluated
	 * @param trainInstances the instances which will be used to train the model
	 * @param testInstances the instances which will be used to evaluate the model
	 * @param rounds the number of repetitions 
	 * @return  the results of the test-set-evaluation (each EvaluationResultSummary summarizes the
	 * results of the different rounds
	 */
	public static EvaluationResultsSummary evaluateTestSet(Model model, RDTInstances trainInstances, RDTInstances testInstances, int rounds) throws Exception{
		EvaluationResultsSummary cmr = new EvaluationResultsSummary();
		
		for(int i=0; i<rounds; i++){
			cmr.addResult(evaluateTestSet(model, trainInstances, testInstances));
		}
		
		return cmr;
	}
}

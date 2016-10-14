package evaluation.engine;

import rdt.essentials.RDTException;
import rdt.model.BatchEnsemble;
import rdt.model.Model;
import rdt.model.ModelType;
import rdt.model.MultilabelChainEnsemble;
import rdt.model.MultilabelChainEnsemble.ChainType;
import rdt.model.MultilabelChainEnsemble.PredictType;
import rdt.model.QuantilEnsemble;
import rdt.model.SparseMultilabelChainEnsemble;
import rdt.tree.collector.CollectorPreferences;

/**
 * Class to dynamically create models with specific values for the parameters during the runtime. If 
 * you want to use the specific pre-defined experiments (e.g. CompareExperiment) you have to add the
 * model in the methods "getModel(...)" and "getModelParameterNames(...)". 
 * 
 * @author MK
 */
public class Models {

	/**
	 * Returns the specific model with the given parameter values and the collector-preferences. The
	 * number of values for parameters and the type of the values must exactly match the model 
	 * parameters.
	 * 
	 * @param type the type of the model which will be created
	 * @param cp the information about the learning tasks
	 * @param params the values of the parameters of the model
	 * @return the specific model with the given parameter values and the collector-preferences
	 */
	public static Model getModel(ModelType type, CollectorPreferences cp, Object[] params) throws RDTException{
		switch(type){
		case BATCH_ENSEMBLE: return getBatchEnsemble(cp, params);
		case QUANTIL_ENSEMBLE: return getQuantilEnsemble(cp, params);
		case MULTILABEL_CHAIN_ENSEMBLE: return getMultilabelChainEnsemble(cp, params);
		case SPARSE_MULTILABEL_CHAIN_ENSEMBLE: return getSparseMultilabelChainEnsemble(cp, params);
		default: throw new RDTException("Model does not exist in class Models for ModelType " + type.name() + "!");
		}
	}
	
	/**
	 * Returns the names of the parameters of a specific model.
	 * 
	 * @param type the type of the model
	 * @return the names of the parameters of a specific model
	 */
	public static String[] getModelParameterNames(ModelType type) throws RDTException{
		switch(type){
		case BATCH_ENSEMBLE: return getBatchEnsembleParameterNames();
		case QUANTIL_ENSEMBLE: return getQuantilEnsembleParameterNames();
		case MULTILABEL_CHAIN_ENSEMBLE: return getMultilabelChainEnsembleParameterNames();
		case SPARSE_MULTILABEL_CHAIN_ENSEMBLE: return getSparseMultilabelChainEnsembleParameterNames();
		default: throw new RDTException("Model-parameter-names does not exist in class Models for ModelType " + type.name() + "!");
		}
	}



	/*
	 * BatchEnsemble
	 */
	private static Model getBatchEnsemble(CollectorPreferences cp, Object[] params) throws RDTException {
		if(params.length != 4){
			throw new RDTException("Wrong parameters for model BatchEnsemble!");
		}
		int numTrees = (int) params[0];
		int maxDeep = (int) params[1];
		int maxS = (int) params[2];
		long randomSeed = (long) params[3];
		return new BatchEnsemble(cp, numTrees, maxDeep, maxS, randomSeed);
	}
	private static String[] getBatchEnsembleParameterNames(){
		return new String[]{"numTrees", "maxDeep", "maxS", "randomSeed"};
	}
	
	
	/*
	 * QuantilEnsemble
	 */
	private static Model getQuantilEnsemble(CollectorPreferences cp, Object[] params) throws RDTException {
		if(params.length != 7){
			throw new RDTException("Wrong parameters for model QuantilEnsemble!");
		}
		int numTrees = (int) params[0];
		int maxDeep = (int) params[1];
		int maxValues = (int) params[2];
		boolean randomQuantil = (boolean) params[3];
		boolean conceptDrift = (boolean) params[4];
		boolean interpolate = (boolean) params[5];
		long randomSeed = (long) params[6];
		return new QuantilEnsemble(cp, numTrees, maxDeep, maxValues, randomQuantil, conceptDrift, interpolate, randomSeed);
	}
	private static String[] getQuantilEnsembleParameterNames(){
		return new String[]{"numTrees", "maxDeep", "maxValues", "randomQuantil", "conceptDrift", "interpolate", "randomSeed"};
	}
	
	

	/*
	 * MultilabelChainEnsemble
	 */
	private static Model getMultilabelChainEnsemble(CollectorPreferences cp, Object[] params) throws RDTException {
		if(params.length != 9){
			throw new RDTException("Wrong parameters for model MultilabelChainEnsemble!");
		}
		int numTrees = (int) params[0];
		int maxDeep = (int) params[1];
		int maxS = (int) params[2];
		long randomSeed = (long) params[3];
		double percentageLabels = (double) params[4];
		double percentageActiveLabels = (double) params[5];
		int numChainPredict = (int) params[6];
		PredictType predictType = (PredictType) params[7];
		ChainType chainType = (ChainType) params[8];
		return new MultilabelChainEnsemble(cp, numTrees, maxDeep, maxS, randomSeed, percentageLabels, percentageActiveLabels, numChainPredict, predictType, chainType);
	}
	private static String[] getMultilabelChainEnsembleParameterNames(){
		return new String[]{"numTrees", "maxDeep", "maxS", "randomSeed", "percentageLabels", "percentageActiveLabels", "numChainPredict", "predictType", "chainType"};
	}
	

	/*
	 * SparseMultilabelChainEnsemble
	 */
	private static Model getSparseMultilabelChainEnsemble(CollectorPreferences cp, Object[] params) throws RDTException {
		if(params.length != 9){
			throw new RDTException("Wrong parameters for model MultilabelChainEnsembleSparse!");
		}
		int numTrees = (int) params[0];
		int maxDeep = (int) params[1];
		int maxS = (int) params[2];
		long randomSeed = (long) params[3];
		double percentageLabels = (double) params[4];
		double percentageActiveLabels = (double) params[5];
		int numChainPredict = (int) params[6];
		PredictType predictType = (PredictType) params[7];
		ChainType chainType = (ChainType) params[8];
		return new SparseMultilabelChainEnsemble(cp, numTrees, maxDeep, maxS, randomSeed, percentageLabels, percentageActiveLabels, numChainPredict, predictType, chainType);
	}
	private static String[] getSparseMultilabelChainEnsembleParameterNames(){
		return new String[]{"numTrees", "maxDeep", "maxS", "randomSeed", "percentageLabels", "percentageActiveLabels", "numChainPredict", "predictType", "chainType", "numAttrs"};
	}
	
	
	
	
}

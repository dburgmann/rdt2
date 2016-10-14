package rdt.model;

import rdt.essentials.RDTException;
import rdt.tree.builder.SparseMultilabelChainTreeBuilder;
import rdt.tree.collector.CollectorPreferences;

/**
 * Class to represent an ensemble of MultilabelChainTrees. A SparseMultilabelChainEnsemble contains trees
 * which have been built with the SparseMultilabelChainTreeBuilder. The SparseMultilabelChainTreeBuilder
 * uses special splitters which can handle sparse data well. This model can only be applied on datasets
 * which have only binary attributes. All the other functionalities of the MultilabelChainEnsemble are
 * still the same.
 * 
 * @author MK
 */
public class SparseMultilabelChainEnsemble extends MultilabelChainEnsemble{

	/**
	 * Creates a new SparseMultilabelChainEnsemble with the given information about the learning tasks,
	 * the number of trees, the maximal depth of the trees, the minimum number of instances to 
	 * create a splitter, a seed for the random number generator, the percentage of label-splitters
	 * in the ensemble, the percentage of the label-splitters which are activated, the number of 
	 * how many labels should be predicted with the chain method, the type of prediction and the 
	 * type how to predict the next label.
	 * 
	 * IMPORTANT
	 * "percentageActiveLabels" only exists to do some experiments. To ensure the functionality of
	 * the chain methods you have to set the value of this variable equal to 1.
	 * "numChainPredict only" exists to do some experiments. To ensure the functionality of the
	 * chain methods you have to set the value of this variable higher than the number of labels
	 * of the dataset.
	 * 
	 * @param cp the information about the learning tasks
	 * @param numTrees the number of trees
	 * @param maxDeep the maximal depth of the trees
	 * @param maxS the minimum number of instances to create a splitter
	 * @param randomSeed the seed for the random number generator
	 * @param percentageLabels the percentage of label-splitters in the ensemble
	 * @param percentageActiveLabels the percentage of the label-splitters which are activated
	 * @param numChainPredict the number of how many labels should be predicted with the chain
	 * method (ONLY for specific experiments should the value be chosen below the number of labels)
	 * @param predictType the type of prediction
	 * @param chainType the type how to predict the next label
	 */
	public SparseMultilabelChainEnsemble(CollectorPreferences cp, int numTrees, int maxDeep, int maxS, long randomSeed,
			double percentageLabels, double percentageActiveLabels, int numChainPredict, PredictType predictType,
			ChainType chainType) throws RDTException {
		super(cp, numTrees, maxDeep, maxS, randomSeed, percentageLabels, percentageActiveLabels, numChainPredict, predictType,
				chainType);
		this.treeBuilder = new SparseMultilabelChainTreeBuilder(cp, maxDeep, maxS, randomSeed, percentageLabels);
	}


}

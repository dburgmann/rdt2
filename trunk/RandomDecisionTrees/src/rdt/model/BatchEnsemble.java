package rdt.model;

import rdt.essentials.RDTException;
import rdt.tree.builder.BatchTreeBuilder;
import rdt.tree.collector.CollectorPreferences;

/**
 * Class to represent an ensemble of random decision trees. The trees have been built 
 * with the BatchTreeBuilder (batch learning).
 * 
 * @author MK
 */
public class BatchEnsemble extends Ensemble{

	/**
	 * Creates a new BatchEnsemble with the given number of trees, maximal depth, the 
	 * information about the learning tasks, the minimum number of instances to create 
	 * an inner node and a random seed to initialize the random number generator.
	 * 
	 * @param cp the information about the learning tasks
	 * @param numTrees the number of trees in the ensemble
	 * @param maxDeep the maximum depth of the trees
	 * @param maxS the minimum number of instances to create a inner-node
	 * @param randomSeed a seed to initialize the random number generator
	 */
	public BatchEnsemble(CollectorPreferences cp, int numTrees, int maxDeep, int maxS, long randomSeed) throws RDTException {
		super(cp, new BatchTreeBuilder(cp, maxDeep, maxS, randomSeed), numTrees);
	}

}

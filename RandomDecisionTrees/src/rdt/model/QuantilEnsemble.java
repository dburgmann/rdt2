package rdt.model;

import rdt.essentials.RDTException;
import rdt.tree.Tree;
import rdt.tree.builder.QuantilTreeBuilder;
import rdt.tree.builder.UpdateableTreeBuilder;
import rdt.tree.collector.CollectorPreferences;
import weka.core.Instance;

/**
 * 
 * Class to represent a quantil-ensemble. A quantil-ensemble contains trees which have been 
 * built with the QuantilTreeBuilder. The QuantilEnsemble is a incremental learning algorithm
 * and implements the UpdateAbleModel-interface. Furthermore is it possible that multiple
 * QunatilEnsemble's can be merged together if they are built for the same learning task and 
 * if they have been initialized with the same parameters. The only difference between the 
 * ensembles which can be merged together is that they have been built on different instances
 * which have the same attributes. 
 * 
 * 
 * 
 * TODO: Update comments!
 * 
 * @author MK 
 */
public class QuantilEnsemble extends Ensemble implements UpdateableModel{

	private UpdateableTreeBuilder utb;
	
	public QuantilEnsemble(CollectorPreferences cp, int numTrees, int maxDeep, int maxValues, boolean randomQuantil, boolean conceptDrift, boolean interpolate, long randomSeed) throws RDTException {
		super(cp, new QuantilTreeBuilder(cp, maxDeep, maxValues, randomQuantil, conceptDrift, interpolate, randomSeed), numTrees);
	
		 this.utb = (UpdateableTreeBuilder) treeBuilder;
			
	}

	@Override
	public void update(Instance inst) throws RDTException {
		for(Tree t : trees){
			utb.update(t, inst);
		}
	}
	
	
	public void merge(QuantilEnsemble otherEnsemble){
		//TODO
	}

}

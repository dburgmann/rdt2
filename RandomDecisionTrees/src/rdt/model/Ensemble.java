package rdt.model;

import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.Tree;
import rdt.tree.Tree.PredictionCombination;
import rdt.tree.builder.TreeBuilder;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.CombineCollectors;
import weka.core.Instance;

/**
 * Class to represent a model which is an ensemble of trees. A tree-builder is used
 * to create the trees. To perform a prediction the predict(...) method of all trees
 * are called and these predictions are merged to produce the final prediction.
 * 
 * @author MK
 */
public abstract class Ensemble implements Model{
	/**
	 * This variable contains all the information about the learning tasks which 
	 * will be performed by the model. These preferences contain a set of 
	 * collectors, each of them represents a specific learning task.
	 */
	protected CollectorPreferences cp;
	
	/**
	 * Contains all the trees which have been built with the tree-builder.
	 */
	protected Tree[] trees;
	
	/**
	 * The tree-builder is used to create the trees for the ensemble.
	 */
	protected TreeBuilder treeBuilder;
	

	/**
	 * Create a new Ensemble with the specified learning tasks, which are given by the 
	 * CollectorPreferences, the given tree-builder which will be used to build to trees
	 * and the given number of trees in the ensemble.
	 * 
	 * @param cp the information about the learning tasks
	 * @param treeBuilder the tree-builder to create the trees
	 * @param numTrees the number of trees which will be created
	 */
	public Ensemble(CollectorPreferences cp, TreeBuilder treeBuilder, int numTrees){
		this.cp = cp;
		this.treeBuilder = treeBuilder;
		this.trees = new Tree[numTrees];
	}

	/**
	 * @return the information about the learning tasks
	 */
	public CollectorPreferences getCollectorPreferences(){
		return cp;
	}
	
	/**
	 * Returns all the trees of the ensemble.
	 * 
	 * @return all trees of the ensemble
	 */
	public Tree[] getTrees(){
		return trees;
	}
	
	@Override
	public void build(RDTInstances insts) throws RDTException {
		for(int i=0; i<trees.length; i++){
			trees[i] = treeBuilder.buildTree(insts);
		}
	}

	@Override
	public Collector[] predict(Instance inst) throws RDTException {
		
		// Just in case: Set all the values for the restricted attributes to missing. Furthermore
		// we store the values of the restricted attributes to reassign them to the instance afterwards.
		int[] restrictedAttributes = cp.getAllResrictedAttributeIds();
		double[] restrictedValues = new double[restrictedAttributes.length];
		for(int i=0; i<restrictedAttributes.length; i++){
			restrictedValues[i] = inst.value(restrictedAttributes[i]);
			inst.setMissing(restrictedAttributes[i]);
		}
		
		CombineCollectors mc = new CombineCollectors(cp.getNumCollectors(), trees.length);
		
		for(Tree tree : trees){			
			mc.addCollectors(tree.predict(inst));
		}
		
		
		// Reassign the values of the restricted attributes.
		for(int i=0; i<restrictedValues.length; i++){
			inst.setValue(restrictedAttributes[i], restrictedValues[i]);
		}
		
		return mc.combine(PredictionCombination.MERGE);
	}
	

	
}

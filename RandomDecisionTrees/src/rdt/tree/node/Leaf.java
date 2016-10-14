package rdt.tree.node;

import rdt.essentials.RDTException;
import rdt.tree.Tree.PredictionCombination;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CombineCollectors;
import weka.core.Instance;

/**
 * Class for a leaf. A leaf is a node in a decision tree which do not have any child-nodes.
 * A leaf contains an array of collectors. Each collector represents one learning task and 
 * can be used to create a prediction for that learning task. It is possible to add or remove
 * instances from the collectors. These instances are processed by the collector to update
 * internal statistics with which the prediction can be generated. A leaf contains multiple
 * collectors in order to perform multiple learning tasks in the tree at the same time.
 * For example it is possible to predict the value for a nominal attribute and the value for
 * a numeric attribute in the same leaf by using the ClassificationCollector and the
 * RegressionCollector.
 * 
 * @author MK
 */
public class Leaf extends Node{
	
	/**
	 * The collectors of this leaf
	 */
	private Collector[] collectors;
	
	/**
	 * Creates a new Leaf with the given collectors
	 */
	public Leaf(Collector[] collectors){
		this.collectors = collectors;
	}
	
	/**
	 * Adds the given instance to all collectors
	 * 
	 * @param inst the instance which will be added
	 */
	public void addToCollectors(Instance inst){
		for(Collector c : collectors){
			c.addInstance(inst);
		}
	}
	
	/**
	 * Adds the collectors of the given leaf to this leaf
	 * 
	 * @param otherLeaf the leaf which contains the collectors which will be added
	 * @throws RDTException 
	 */
	public void addToCollectors(Leaf otherLeaf) throws RDTException{
		CombineCollectors cc = new CombineCollectors(collectors.length, 2);
		
		cc.addCollectors(collectors);
		cc.addCollectors(otherLeaf.getCollectors());
		
		collectors = cc.combine(PredictionCombination.ADD);
	}
	
	/**
	 * Removes the given instance from all collectors
	 * 
	 * @param inst, the instance which will be removed
	 */
	public void removeFromCollectors(Instance inst){
		for(Collector c : collectors){
			c.removeInstance(inst);
		}
	}
	
	/**
	 * Returns an array of all collectors
	 * 
	 * @return The collectors
	 */
	public Collector[] getCollectors(){
		return collectors;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.LEAF;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Leaf");
		
		for(Collector c : collectors){
			sb.append("\n");
			sb.append(c.toString());
		}
		return sb.toString();
	}
	
}

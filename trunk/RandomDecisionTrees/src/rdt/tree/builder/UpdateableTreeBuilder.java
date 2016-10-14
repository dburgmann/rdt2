package rdt.tree.builder;

import rdt.essentials.RDTException;
import rdt.tree.Tree;
import weka.core.Instance;

/**
 * UpdateableTreeBuilder Interface. For incremental learning it is necessary to update the
 * decision tree with each instance. A tree-builder can update trees by using the method which
 * is provided in this interface.
 * 
 * @author MK
 */
public interface UpdateableTreeBuilder extends TreeBuilder{
	
	/**
	 * Updates the given tree with the given instance.
	 * 
	 * @param tree the tree which will be updated
	 * @param inst the instance which will be used to update the tree
	 */
	public void update(Tree tree, Instance inst) throws RDTException;
}

package rdt.tree.builder;

import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.Tree;

/**
 * TreeBuilder Interface. A tree-builder is used to build trees.
 * 
 * @author MK
 */
public interface TreeBuilder {
	
	/**
	 * Builds a new tree by using the information of the given instances.
	 * 
	 * @param trainInstances the instances which will be used to construct the tree
	 * @return the constructed tree
	 */
	public Tree buildTree(RDTInstances trainInstances) throws RDTException;

	/**
	 * Returns the type of the tree-builder.
	 * 
	 * @return the type of the TreeBuilder
	 */
	public TreeBuilderType getType();
	

	
}

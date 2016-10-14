package rdt.tree.builder;

/**
 * Enumeration for all different kinds of tree-builders. The tree-builder-type is used
 * to easily identify the type of the tree-builder by only using the getType()-method
 * of the tree-builder. With this we can avoid using instanceof during the runtime.
 * 
 * @author MK
 */
public enum TreeBuilderType {
	
	
	//Batch-Algorithm
	BATCH, 	 
	SPECIAL_CV,
	MULTILABEL_CHAIN,
	SPARSE_MULTILABEL_CHAIN,
	
	//Online-Algorithm
	QUANTIL,		

}
package rdt.tree.node;

/**
 * Node Interface. The node is an essential element of a decision tree. A decision tree is a collection
 * of nodes which are connected with each other in a structured way. A node can be either a inner-node
 * which has a couple of child-nodes or a leaf which contains the prediction (in our case collectors).
 * 
 * @author MK
 */
public abstract class Node {

	/**
	 * The parent-node of the tree. The parent-node of a root is null.
	 */
	private Node parent;
	
	/**
	 * Returns the type of the node.
	 * 
	 * @return the node-type
	 */
	public abstract NodeType getNodeType();
	
	/**
	 * Returns the parent-node of the node.
	 * 
	 * @return the parent
	 */
	public Node getParent(){
		return parent;
	}
	
	/**
	 * Sets the given node as the parent-node.
	 * 
	 * @param newParent, the given node
	 */
	public void setParent(Node newParent){
		parent = newParent;
	}
	
}

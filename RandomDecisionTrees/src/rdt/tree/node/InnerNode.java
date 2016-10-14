package rdt.tree.node;

import rdt.essentials.RDTException;
import rdt.tree.splitter.Splitter;
import weka.core.Instance;

/**
 * Class for a inner-node. An inner-node is a node in a decision tree which has child-nodes.
 * It contains an array of child-nodes and a splitter. The splitter is used to forwarded 
 * incoming instances to the correct child-node.
 * 
 * @author MK
 */
public class InnerNode extends Node{
	
	/**
	 * The child-nodes of this node
	 */
	private Node[] children;
	
	/**
	 * The splitter which is used to assign incoming instances to the child-nodes
	 */
	private Splitter splitter;

	/**
	 * Creates a new InnerNode without a splitter.
	 */
	public InnerNode(){
		this.children = null;
		this.splitter = null;
	}
	
	/**
	 * Creates a new InnerNode with the given splitter.
	 * 
	 * @param splitter the splitter which will be used in this inner-node
	 */
	public InnerNode(Splitter splitter){
		this.children = new Node[splitter.getNumberOfChilds()];
		this.splitter = splitter;
	}
	
	/**
	 * Sets the given splitter as the splitter of this node
	 * 
	 * @param splitter, the given splitter
	 */
	public void setSplitter(Splitter splitter){
		this.splitter = splitter;
		this.children = new Node[splitter.getNumberOfChilds()];
	}
	
	/**
	 * Returns the child to which the given instance will be forwarded.
	 * 
	 * @param inst, the instance
	 * @return the child
	 */
	public Node getChildAccordingTo(Instance inst) throws RDTException{
		return children[splitter.determineChild(inst)];
	}
	
	/**
	 * Sets the given node as the i-th child-node.
	 * 
	 * @param i the index of the child-node
	 * @param node the new child-node
	 */
	public void setChild(int i, Node node){
		children[i] = node;
	}
	
	/**
	 * Returns the i-th child-node.
	 * 
	 * @param i the index of the child-node
	 * @return the i-th child-node
	 */
	public Node getChild(int i){
		return children[i];
	}

	/**
	 * Returns all children.
	 * 
	 * @return all children
	 */
	public Node[] getChildren(){
		return children;
	}
	
	/**
	 * Sets all children.
	 * 
	 * @param newChildren the new children
	 */
	public void setChildren(Node[] newChildren){
		children = newChildren;
	}
	
	/**
	 * Returns the number of child-nodes.
	 * 
	 * @return the number of child-nodes
	 */
	public int getNumberOfChildren(){
		return children.length;
	}

	/**
	 * Returns the splitter of the node.
	 * 
	 * @return the splitter of the node
	 */
	public Splitter getSplitter(){
		return splitter;
	}
	
	@Override
	public NodeType getNodeType() {
		return NodeType.INNER_NODE;
	}

	@Override
	public String toString() {
		return "InnerNode\n" + splitter.toString();
	}

}

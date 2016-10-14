package rdt.tree;

import rdt.essentials.RDTException;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.node.InnerNode;
import rdt.tree.node.Leaf;
import rdt.tree.node.Node;
import rdt.tree.node.NodeType;
import rdt.tree.splitter.QuantilSplitter;

/**
 * 
 * TODO: Update comments!
 * 
 * @author MK 
 */
public class QuantilTree extends Tree{
	
	public QuantilTree(Node root, CollectorPreferences cp) throws RDTException {
		super(root, cp);
	}
	
	public void simpleMerge(QuantilTree qt) throws RDTException{		
		simpleMergeRecursively(getRoot(), qt.getRoot());	
	}
	
	private void simpleMergeRecursively(Node node, Node otherNode) throws RDTException{
		
		if(node.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) node;
			if(otherNode.getNodeType() == NodeType.INNER_NODE){
				InnerNode otherIn = (InnerNode) otherNode;
				for(int i=0; (i<in.getNumberOfChildren() && i<otherIn.getNumberOfChildren()); i++){
					simpleMergeRecursively(in.getChild(i), otherIn.getChild(i));
				}
				merge(in, otherIn);
			}else if(otherNode.getNodeType() == NodeType.LEAF){
				Leaf otherLeaf = (Leaf) otherNode;
				merge(in, otherLeaf);
			}else{
				throw new RDTException("Unknown node-type: " + otherNode.getNodeType());
			}
		}else if(node.getNodeType() == NodeType.LEAF){
			Leaf leaf = (Leaf) node;
			if(otherNode.getNodeType() == NodeType.LEAF){
				Leaf otherLeaf = (Leaf) otherNode;
				merge(leaf, otherLeaf);
			}else if(otherNode.getNodeType() == NodeType.INNER_NODE){
				InnerNode otherIn = (InnerNode) otherNode;
				merge(leaf, otherIn);
			}else{
				throw new RDTException("Unknown node-type: " + otherNode.getNodeType());
			}
		}else{
			throw new RDTException("Unknown node-type: " + otherNode.getNodeType());
		}
	}

	private void merge(InnerNode in, InnerNode otherIn) throws RDTException{
		QuantilSplitter us = (QuantilSplitter)in.getSplitter();
		QuantilSplitter otherUs = (QuantilSplitter)otherIn.getSplitter();
		
		if(us.getNumberOfChilds() < otherUs.getNumberOfChilds()){
			Node[] children = in.getChildren();
			Node[] otherChildren = otherIn.getChildren();
			Node[] newChildren = new Node[otherIn.getNumberOfChildren()];
			
			for(int i=0; i<children.length; i++){
				newChildren[i] = children[i];
			}
			
			for(int i=children.length; i<otherChildren.length; i++){
				newChildren[i] = otherChildren[i];
				newChildren[i].setParent(in);
			}
			in.setChildren(newChildren);
		}
		us.merge(otherUs);
	}
	
	private void merge(Leaf leaf, Leaf otherLeaf) throws RDTException{
		leaf.addToCollectors(otherLeaf);
	}
	
	private void merge(InnerNode in, Leaf otherLeaf){
		//Nothing to do here
	}
	
	private void merge(Leaf leaf, InnerNode otherIn){
	
		Node parent = leaf.getParent();
		if(parent == null){
			root = otherIn;
		}else{
			InnerNode in = (InnerNode) parent;
			for(int i=0; i<in.getNumberOfChildren(); i++){
				if(in.getChild(i).equals(leaf)){
					in.setChild(i, otherIn);
					break;
				}
			}
			otherIn.setParent(in);
		}
		
	}

}

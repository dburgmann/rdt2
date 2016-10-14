package rdt.tree;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import rdt.essentials.RDTException;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.CombineCollectors;
import rdt.tree.node.InnerNode;
import rdt.tree.node.Leaf;
import rdt.tree.node.Node;
import rdt.tree.node.NodeType;
import weka.core.Instance;

/**
 * Class which represents a tree. A tree has three basic functionalities. The 
 * first functionality is to make an prediction for an instance. In this case 
 * the instance will be forwarded from the root of the tree to a leaf. If the
 * instances passes a splitter which can not handle this instance the instance
 * will be forwarded to all children of the node of the splitter and the predictions
 * will be combined afterwards. There are two ways to combine a prediction ADD and 
 * MERGE. If ADD is selected the collectors will  be added together and if MERGE
 * is selected the collectors will be merged together (see Collector.add(...) and
 * Collector.merge(...) for more information).
 * If one of the collectors in a leaf cannot make a valid prediction the closest 
 * leaf to the current leaf will be searched and the collector of that leaf will
 * be used. If that collector cannot make a valid prediction as well the procedure
 * will be repeated until a valid prediction for the specific collector can be found.
 * The second functionality is to add an instance to the tree. Just like making a
 * prediction the instance is forwarded to a leaf where the instance is added to 
 * the collectors of the leaf. If the instance passes a splitter which cannot handle 
 * the instance, the weight of the instance will be divided by the number of the 
 * child-nodes and the instance is forwarded to all child-nodes. While adding an 
 * instance to the tree the inner-nodes will not be modified.
 * The last functionality is to remove an instance from the tree. It is the same
 * procedure as to add an instance except that the instance will removed from the
 * collectors instead of adding the instance.
 * 
 * @author MK
 */
public class Tree{

	/**
	 * The root of the tree.
	 */
	protected Node root;
	
	/**
	 * The information about the learning tasks which can be performed by this tree.
	 */
	protected CollectorPreferences cp;
	
	/**
	 * These sets are used to perform an efficient prediction if a leaf can not make a
	 * valid prediction. In these maps all the nodes are stored which have been visited
	 * to find a valid prediction.
	 */
	protected HashSet<Node> invalidParents;
	protected HashSet<Node> invalidChilds;
	

	/**
	 * Enumeration to define the state of adding or removing a instance to or of the tree.
	 */
	protected enum Action{ADD, REMOVE};
	
	/**
	 * Enumeration to define if the collectors are added or merged if we have to combine
	 * the predictions.
	 */
	public enum PredictionCombination{ADD, MERGE};
	
	/**
	 * The current state. ADD means that we want to add an instance to the tree and 
	 * REMOVE means that we want to remove the instance from the tree.
	 */
	protected Action curAction;
	
	/**
	 * The current prediction combination type. If it is ADD then the collectors will
	 * bee added together if the collectors have to be combined and if it is MERGE
	 * the collectors will be merged together.
	 */
	protected PredictionCombination curPredictionCombination;
	
	/**
	 * Creates a new Tree with the given root and the information about the learning
	 * tasks.
	 * 
	 * @param root the root of the tree
	 * @param cp the information about the learning tasks
	 */
	public Tree(Node root, CollectorPreferences cp) throws RDTException{
		if(root == null){
			throw new RDTException("Tree created with a root which is null!");
		}
		this.root = root;
		this.cp = cp;
		this.curPredictionCombination = PredictionCombination.MERGE;
	}
	
	/**
	 * Adds the given instance to the tree.
	 * 
	 * @param inst the instance which will be added to the tree
	 */
	public void addInstance(Instance inst) throws RDTException{
		curAction = Action.ADD;
		trainOrRemoveRecursively(root, inst);
	}
	
	/**
	 * Removes the given instance from the tree.
	 * 
	 * @param inst the instance which will be removed from the tree
	 */
	public void remove(Instance inst) throws RDTException{
		curAction = Action.REMOVE;
		trainOrRemoveRecursively(root, inst);
	}
	
	/**
	 * Searches recursively the leaf(s) for the given instance and add or removes the 
	 * instance from the collectors of the leaf according to the current action.
	 * 
	 * @param currentNode the node which is examined in this step
	 * @param inst the instance which will be added or removed
	 */	
	private void trainOrRemoveRecursively(Node currentNode, Instance inst) throws RDTException{
		if(currentNode.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) currentNode;
			
			//Handle missing values
			if(!in.getSplitter().canHandle(inst)){
		
				double oldWeight = inst.weight();
				inst.setWeight(oldWeight / in.getNumberOfChildren());
				
				for(int i=0; i<in.getNumberOfChildren(); i++){
					trainOrRemoveRecursively(in.getChild(i), inst);
				}
				inst.setWeight(oldWeight);
				
			}else{
				trainOrRemoveRecursively(in.getChildAccordingTo(inst), inst);
			}
		}else{
			if(currentNode.getNodeType() == NodeType.LEAF){
				Leaf leaf = (Leaf) currentNode;
				
				if(curAction == Action.ADD){
					leaf.addToCollectors(inst);
				}else if(curAction == Action.REMOVE){
					leaf.removeFromCollectors(inst);
				}else{
					throw new RDTException("Unknown action: " + curAction);
				}
			}else{
				throw new RDTException("Unknown node-type: " + currentNode.getNodeType());
			}
		}
	}
	
	/**
	 * Makes a prediction for the given instance. The prediction is represented as an
	 * array of collectors. Each collector represents one specific learning-task, which
	 * have been specified in the initialization process. 
	 * 
	 * @param inst the instance for which the prediction will be computed
	 * @return An array of collectors which contain the prediction for each specific
	 * learning task.
	 */
	public Collector[] predict(Instance inst) throws RDTException{
		invalidParents = new HashSet<Node>();
		invalidChilds = new HashSet<Node>();
		return predictRecursively(root, inst);
	}
	
	/**
	 * Determines the prediction for the given instance recursively. If the
	 * instances passes a splitter which can not handle this instance the instance
	 * will be forwarded to all children of the node of the splitter and the predictions
	 * will be combined afterwards (according to the current PredictionCombination-state).
	 * If a leaf is reached which can not make a valid prediction the closest
	 * leaf will be determined which can make a valid prediction by using the
	 * getValidPredictionFromParent(...) method.
	 * 
	 * @param currentNode the node which is examined currently
	 * @param inst the instance for which we need the prediction
	 * @return the prediction for the instance
	 */
	protected Collector[] predictRecursively(Node currentNode, Instance inst) throws RDTException {
		if(currentNode.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) currentNode;
			
			//Handle missing values
			if(!in.getSplitter().canHandle(inst)){
				
				CombineCollectors cc = new CombineCollectors(cp.getNumCollectors(), in.getNumberOfChildren());
				
				for(int i=0; i<in.getNumberOfChildren(); i++){
					cc.addCollectors(predictRecursively(in.getChild(i), inst));
				}
				
				return cc.combine(curPredictionCombination);
			}else{
				return predictRecursively(in.getChildAccordingTo(inst), inst);
			}
		}else{
			if(currentNode.getNodeType() == NodeType.LEAF){
				Leaf l = (Leaf) currentNode;
				Collector[] collectors = l.getCollectors();
				
				if(canMakePrediction(collectors)){
					return collectors;
				}
				
				Collector[] newCollectors = new Collector[collectors.length];
				
				for(int i=0; i<collectors.length; i++){
					if(!collectors[i].canMakePrediction()){
						if(invalidParents.contains(l.getParent())){
							return null;
						}
						invalidChilds.add(l);
						invalidParents.add(l.getParent());						
						newCollectors[i] = getValidPredictionFromParent(l.getParent(), i, inst);
						invalidParents.remove(l.getParent());
						invalidChilds.remove(l);

					}else{
						newCollectors[i] = collectors[i];
					}
				}
				return newCollectors;
			}else{
				throw new RDTException("Unknown node-type: " + currentNode.getNodeType());
			}
		}
	}
	
	/**
	 * Checks if all given collectors can make a valid prediction.
	 * 
	 * @param collectors the collectors which will be checked
	 * @return true if all collectors can make a valid prediction otherwise false
	 */
	protected boolean canMakePrediction(Collector[] collectors){
		for(Collector c : collectors){
			if(!c.canMakePrediction()){
				return false;
			}
		}
		return true;
	}

	/**
	 * Searches the nearest valid prediction for the given collector (defined by the 
	 * collector-id) by going one step back in the tree and combining the predictions
	 * of all child nodes from the parent node.
	 * If every child node of the parent node can not make a valid prediction this 
	 * method will be called recursively with the parent node of the current parent
	 * node.
	 * This method uses two global HashMaps to store the nodes which already have been
	 * visited to speed up the progress.
	 * 
	 * @param parent the current parent
	 * @param collectorId the position in the collector-array of the collector for
	 * which we need a valid prediction
	 * @param inst the instance for which we need the prediction
	 * @return the collector which can make a valid prediction
	 */
	protected Collector getValidPredictionFromParent(Node parent, int collectorId, Instance inst) throws RDTException {		
		if(parent == null){
			throw new RDTException("No valid prediction for collector " + collectorId + " was found in the tree. Maybe you forgot to add some instances to the tree (train the tree).");
		}
		
		if(parent.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) parent;
			
			List<Collector> validCollectors = new LinkedList<Collector>();
			
			for(int i=0; i<in.getNumberOfChildren(); i++){
				Node child = in.getChild(i);
	
				if(invalidChilds.contains(child)){
					continue;
				}
	
				Collector[] collectors = predictRecursively(child, inst);
				if(collectors != null && collectors[collectorId].canMakePrediction()){
					validCollectors.add(collectors[collectorId]);
				}
			}
			
			if(validCollectors.size() == 0){
				
				invalidChilds.add(parent);
				invalidParents.add(parent.getParent());
				Collector c = getValidPredictionFromParent(parent.getParent(), collectorId, inst);
				invalidParents.remove(parent.getParent());
				invalidChilds.remove(parent);
				
				return c;
			}	
			
			CombineCollectors cc = new CombineCollectors(1, validCollectors.size());
			
			for(Collector c : validCollectors){
				cc.addCollectors(new Collector[]{c});
			}
			
			return cc.combine(curPredictionCombination)[0];
		}else{
			throw new RDTException("Unknowen parent-node-type: " + parent.getNodeType());
		}
	}
	
	/**
	 * Returns the root of the tree.
	 * 
	 * @return the root of the tree
	 */
	public Node getRoot(){
		return root;
	}
	
	/** 
	 * Returns a list which contains all nodes of the tree.
	 * 
	 * @return a list which contains all nodes of the tree
	 */
	public List<Node> getAllNodes(){
		List<Node> nodes = new LinkedList<Node>();
		addAllNodesToList(nodes, root);
		return nodes;	
	}
	
	/**
	 * Adds all nodes recursively to the given list.
	 * 
	 * @param nodes the current list of nodes
	 * @param node the node which is currently examined
	 */
	private void addAllNodesToList(List<Node> nodes, Node node){
		nodes.add(node);
		if(node.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) node;
			for(int i=0; i<in.getNumberOfChildren(); i++){
				addAllNodesToList(nodes, in.getChild(i));
			}
		}
	}
	
	/**
	 * Sets the way how the collectors will be combined (ADD/MERGE) if the collectors
	 * of different leafs have to be combined (e.g. if splitter tests on missing value
	 * then the collectors of all child nodes have to be combined). 
	 * 
	 * @param tmp the type how to combine the collectors
	 */
	public void setPredcitionCombination(PredictionCombination tmp){
		curPredictionCombination = tmp;
	}
	
}
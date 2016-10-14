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
import rdt.tree.splitter.InstanceBasedNumericSplitter;
import rdt.tree.splitter.SplitterType;
import weka.core.Instance;

/**
 * Class which represents a SpecialCVTree. This tree differs from the normal tree only in the
 * way that it has additional methods to perform the special cross-validation. While performing
 * the special cross-validation it is not allowed to use splitters which have been built with the
 * given instance (instance for which the prediction will be created). In this case the splitters
 * will be skipped by adding the predictions of all children together.
 * 
 * @author MK
 */
public class SpecialCVTree extends Tree{
	
	/**
	 * Creates a new SpecialCVTree with the given root and the information about the learning
	 * tasks.
	 * 
	 * @param root the root of the tree
	 * @param cp the information about the learning tasks
	 */
	public SpecialCVTree(Node root, CollectorPreferences cp) throws RDTException {
		super(root, cp);
	}

	/**
	 * Makes a prediction for the given instance. While performing the prediction all the
	 * splitters are skipped which have been build with the given instance. The prediction 
	 * is represented as an array of collectors. Each collector represents one specific 
	 * learning-task, which have been specified in the initialization process. 
	 * 
	 * @param inst the instance for which the prediction will be computed
	 * @return An array of collectors which contain the prediction for each specific
	 * learning task.
	 */
	public Collector[] predictSpecialCV(Instance inst) throws RDTException{
		invalidParents = new HashSet<Node>();
		invalidChilds = new HashSet<Node>();
		
		return predictRecursivelySpecialCV(root, inst);
	}
	
	/**
	 * Determines the prediction for the given instance recursively. Furthermore all the 
	 * splitters are skipped which have been built with the given instance.  If the
	 * instances passes a splitter which can not handle this instance or the splitter was
	 * built with the given instance then the instance will be forwarded to all children 
	 * of the node of the splitter and the predictions will be combined afterwards. If 
	 * the splitter was skipped then the predictions will be added together otherwise they
	 * will be merged.
	 * If a leaf is reached which can not make a valid prediction the closest
	 * leaf will be determined which can make a valid prediction by using the
	 * getValidPredictionFromParent(...) method.
	 * 
	 * @param currentNode the node which is examined currently
	 * @param inst the instance for which we need the prediction
	 * @return the prediction for the instance
	 */
	private Collector[] predictRecursivelySpecialCV(Node currentNode, Instance inst) throws RDTException {
		if(currentNode.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) currentNode;
			
			
			if(in.getSplitter().getType() == SplitterType.INSTANCE_BASED_NUMERIC){
				
				InstanceBasedNumericSplitter ibns = (InstanceBasedNumericSplitter) in.getSplitter();
				
				if(ibns.getInstance() == inst){
					CombineCollectors mc = new CombineCollectors(cp.getNumCollectors(), in.getNumberOfChildren());
					
					for(int i=0; i<in.getNumberOfChildren(); i++){
						Collector[] collectors = predictRecursivelySpecialCV(in.getChild(i), inst);							
						mc.addCollectors(collectors);
					}
					
					return mc.combine(PredictionCombination.ADD);		
				}
			
			}

			if(!in.getSplitter().canHandle(inst)){
				
				CombineCollectors cc = new CombineCollectors(cp.getNumCollectors(), in.getNumberOfChildren());
				
				for(int i=0; i<in.getNumberOfChildren(); i++){
					cc.addCollectors(predictRecursivelySpecialCV(in.getChild(i), inst));
				}
				
				return cc.combine(curPredictionCombination);
			}else{
				return predictRecursivelySpecialCV(in.getChildAccordingTo(inst), inst);
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
						newCollectors[i] = getValidPredictionFromParentSpecialCV(l.getParent(), i, inst);
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
	protected Collector getValidPredictionFromParentSpecialCV(Node parent, int collectorId, Instance inst) throws RDTException {		
		if(parent == null){
			throw new RDTException("No valid prediction for collector " + collectorId + " was found in the tree");
		}
		
		if(parent.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) parent;
			
			List<Collector> validCollectors = new LinkedList<Collector>();
			
			for(int i=0; i<in.getNumberOfChildren(); i++){
				Node child = in.getChild(i);
	
				if(invalidChilds.contains(child)){
					continue;
				}
	
				Collector[] collectors = predictRecursivelySpecialCV(child, inst);
				if(collectors != null && collectors[collectorId].canMakePrediction()){
					validCollectors.add(collectors[collectorId]);
				}
			}
			
			if(validCollectors.size() == 0){
							
				invalidChilds.add(parent);
				invalidParents.add(parent.getParent());
				Collector c = getValidPredictionFromParentSpecialCV(parent.getParent(), collectorId, inst);
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
}

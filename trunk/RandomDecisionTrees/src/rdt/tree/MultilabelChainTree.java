package rdt.tree;

import java.util.HashSet;
import java.util.List;

import rdt.essentials.RDTException;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.CombineCollectors;
import rdt.tree.node.InnerNode;
import rdt.tree.node.Leaf;
import rdt.tree.node.Node;
import rdt.tree.node.NodeType;
import rdt.tree.splitter.ActiveSplitter;
import rdt.tree.splitter.SplitterType;
import weka.core.Instance;

/**
 * Class which represents a MultilabelChainTree. This tree differs from the normal tree only in
 * the way that it contains information about the label-splitters (splitters which test on a 
 * label-attribute) and information about the attribute-ids of the splitters which have been
 * used for the last prediction (This information can be used to speed up the prediction-process
 * of the multilabel-chain algorithm). 
 * 
 * @author MK
 */
public class MultilabelChainTree extends Tree{
	
	/**
	 * Temporary set for storing the attribute-ids of the label attributes which are used
	 * in the splitters while a prediction is performed.
	 */
	private HashSet<Integer> checkedRestrictedAttributeIds;
	
	/**
	 * All the splitters of the tree which test on a label-attribute.
	 */
	private List<ActiveSplitter> labelSplitters;
	
	/**
	 * Creates a new MultilabelChainTree with the given root, the information about the learning
	 * tasks and all the splitters of the tree which test on a label-attribute.
	 * 
	 * @param root the root of the tree
	 * @param cp the information about the learning tasks
	 * @param labelSplitters the splitters of the tree which test on a label-attribute 
	 */
	public MultilabelChainTree(Node root, CollectorPreferences cp, List<ActiveSplitter> labelSplitters) throws RDTException {
		super(root, cp);
		this.labelSplitters = labelSplitters;
	}

	
	@Override
	public Collector[] predict(Instance inst) throws RDTException{
		invalidParents = new HashSet<Node>();
		invalidChilds = new HashSet<Node>();

		checkedRestrictedAttributeIds = new HashSet<Integer>();
		
		return predictRecursively(root, inst);
	}
	
	@Override
	protected Collector[] predictRecursively(Node currentNode, Instance inst) throws RDTException {
		if(currentNode.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) currentNode;

			
			
			//************MODIFY START************
			// If the splitter is an active-splitter (restricted attribute is used),
			// then add the used attribute to the set
			
			if(in.getSplitter().getType() == SplitterType.FULLNOMINAL_ACTIVE || in.getSplitter().getType() == SplitterType.LABEL){
				for(int i=0; i<in.getSplitter().getUsedAttributeIds().length; i++){
					checkedRestrictedAttributeIds.add(in.getSplitter().getUsedAttributeIds()[i]);
				}
			}
			//*************MODIFY END*************
			
			
			
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
	 * Returns a set of attribute-ids. This set includes the attribute-ids of all label-splitters
	 * (splitters which test on a label-attribute) which have been used during the last prediction.
	 * 
	 * @return all label-attributes which have been used in the splitters during the last prediction
	 */
	public HashSet<Integer> getCheckedRestrictedAttributeIds(){
		return checkedRestrictedAttributeIds;
	}
	
	/**
	 * Returns all the splitters of the tree which test on a label-attribute.
	 * 
	 * @return all the splitters of the tree which test on a label-attribute
	 */
	public List<ActiveSplitter> getLabelSplitters(){
		return labelSplitters;
	}
	
}

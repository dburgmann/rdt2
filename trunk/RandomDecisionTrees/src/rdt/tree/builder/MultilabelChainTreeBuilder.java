package rdt.tree.builder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import rdt.essentials.RDTAttribute;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.MultilabelChainTree;
import rdt.tree.Tree;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.node.Node;
import rdt.tree.node.InnerNode;
import rdt.tree.splitter.ActiveSplitter;
import rdt.tree.splitter.FullNominalActivateSplitter;
import rdt.tree.splitter.Splitter;
import weka.core.Instance;

/**
 * Class for a multilabel-chain-tree-builder which builds the trees in batch-mode. 
 * This tree-builder builds the trees in the exact same way as the BatchTreeBuilder.
 * The only difference is that this tree-builder is able to build splitters which test
 * on label attributes. The amount of label-splitters is defined by the parameter 
 * percentageLabels (e.g. 10% of all splitters are label-splitters)
 * 
 * @author MK
 */
public class MultilabelChainTreeBuilder extends BatchTreeBuilder{

	/**
	 * The percentage of label-splitters which should be contained in the tree which will be built.
	 */
	private double percentageLabels;
	
	/**
	 * All splitters which are testing on labels.
	 */
	protected List<ActiveSplitter> labelSplitters;
	
	/**
	 * This array contains only the attributes which are labels.
	 */
	protected RDTAttribute[] restrAttrs;

	
	/**
	 * Creates a new MultilabelChainTreeBuilder with the given information about the learning
	 * tasks, the maximum depth of the trees, the minimum number of instances to create an 
	 * inner-node, the random seed and the percentage of label-splitters which should be 
	 * contained in the tree.
	 * 
	 * @param cp information about the learning tasks
	 * @param maxDeep the maximum depth of the trees which will be built
	 * @param maxS  the minimum number of instances to create an inner-node
	 * @param randomSeed the random seed
	 * @param percentageLabels the percentage of label-splitters which should be contained in the tree
	 */
	public MultilabelChainTreeBuilder(CollectorPreferences cp, int maxDeep, int maxS, long randomSeed, double percentageLabels) throws RDTException {
		super(cp, maxDeep, maxS, randomSeed);
		
		this.percentageLabels = percentageLabels;
	}
	
	/**
	 * Creates a new splitter which tests on a label attribute. If no attribute can be used
	 * in this splitter because all the label attribute have been tested in the tree before
	 * this method will return null.
	 * 
	 * @param ions the instances which can be used to determine the split-attribute
	 * @return the created splitter or null if no splitter can be created
	 */
	protected Splitter createLabelSplitter(List<Instance> ions) throws RDTException {
		
		RDTAttribute attr = getRandomAttribute(helpSet, restrAttrs);
		
		if(attr == null){
			return null;
		}
		
		FullNominalActivateSplitter splitter = new FullNominalActivateSplitter(attr.getAttributeId(), attr.getNumValues());
		
		labelSplitters.add(splitter);
		
		return splitter;
	}
	
	/**
	 * Returns the random number generator.
	 * 
	 * @return the random number generator
	 */
	public Random getRandom(){
		return random;
	}
	
	@Override
	public Tree buildTree(RDTInstances trainInstances) throws RDTException {
		attributes = trainInstances.getAttributes();
		freeAttrs = trainInstances.getFreeAttributes();
		restrAttrs = trainInstances.getRestrictedAttributes();
		labelSplitters = new LinkedList<ActiveSplitter>();
		helpSet = new HashSet<Integer>();
		
		return new MultilabelChainTree(buildNodesRecursively(transformToList(trainInstances), 0, null), cp, labelSplitters);
	}
	
	@Override
	protected Node buildNode(List<Instance> ions, Node parentNode) throws RDTException{
		double randomPercentage = random.nextDouble();
		Splitter splitter;
		
		if(randomPercentage < percentageLabels){
			//Case 1 create a label-splitter
			splitter = createLabelSplitter(ions);
			if(splitter == null){
				splitter = createSplitter(ions);
			}
			
			
		}else{
			//Case 2 create a normal splitter
			splitter = createSplitter(ions);
			if(splitter == null){
				splitter = createLabelSplitter(ions);
			}
		}
		
		if(splitter == null){
			return createLeaf(ions);
		}
		
		InnerNode in = new InnerNode();
		in.setParent(parentNode);
		in.setSplitter(splitter);
		return in;
	}
	
	@Override
	public TreeBuilderType getType() {
		return TreeBuilderType.MULTILABEL_CHAIN;
	}

}

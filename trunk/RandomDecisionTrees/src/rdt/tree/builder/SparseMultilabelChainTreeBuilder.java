package rdt.tree.builder;

import java.util.List;

import rdt.essentials.RDTException;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.splitter.LabelSplitter;
import rdt.tree.splitter.SparseSplitter;
import rdt.tree.splitter.Splitter;
import weka.core.Instance;

/**
 * Class for a SparseMultilabelChainTreeBuilder. This tree-builder is specialized to build trees 
 * for sparse datasets. Currently the tree-builder can only handle binary nominal attributes.
 * The idea of this tree-builder is to use the SparseSplitter to find good attributes to split.
 * This tree-builder has still the same functionality as the normal MultilabelChainTreeBuilder.
 * 
 * @author MK
 */
public class SparseMultilabelChainTreeBuilder extends MultilabelChainTreeBuilder{
	
	/**
	 * Creates a new SparseMultilabelChainTreeBuilder with the given information about the learning
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
	public SparseMultilabelChainTreeBuilder(CollectorPreferences cp, int maxDeep, int maxS, long randomSeed, double percentageLabels) throws RDTException {
		super(cp, maxDeep, maxS, randomSeed, percentageLabels);
	}

	@Override
	protected Splitter createSplitter(List<Instance> ions) throws RDTException {
				
		Splitter splitter = new SparseSplitter(ions, random, freeAttrs, helpSet);
		
		if(splitter.getUsedAttributeIds().length == 0){
			return null;
		}

		return splitter;
	}
	
	@Override
	protected Splitter createLabelSplitter(List<Instance> ions) throws RDTException {
		LabelSplitter splitter = new LabelSplitter(ions, random, restrAttrs, helpSet);
		
		if(splitter.getUsedAttributeIds().length == 0){
			return null;
		}
		
		labelSplitters.add(splitter);
		
		return splitter;
	}
	
	@Override
	public TreeBuilderType getType() {
		return TreeBuilderType.SPARSE_MULTILABEL_CHAIN;
	}
}

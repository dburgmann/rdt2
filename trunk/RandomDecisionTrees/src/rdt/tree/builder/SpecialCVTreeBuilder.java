package rdt.tree.builder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import rdt.essentials.RDTAttribute;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.SpecialCVTree;
import rdt.tree.Tree;
import rdt.tree.builder.TreeBuilderType;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.splitter.InstanceBasedNumericSplitter;
import rdt.tree.splitter.Splitter;
import weka.core.Instance;

/**
 * Class for a tree-builder which builds trees which can be used for the special cross-validation.
 * The only difference is that instance-based splitters are used for the numeric attributes. These 
 * splitters additionally stores the instance which have been used to create the splitter. (e.g. if 
 * the value of an instance was used as a threshold then this instance will be stored in the splitter
 * as well). With this information it is possible to skip the splitters which have been created with
 * a particular instance which is necessary for the special cross-validation. Furthermore SpecialCVTree's
 * are created which have special functionalities to perform the fast cross-validation.
 * 
 * @author MK
 */
public class SpecialCVTreeBuilder extends BatchTreeBuilder {
	
	/**
	 * Create a new SpecialCVTreeBuilder with the given maximal depth, the information about the learning
	 * tasks, the minimum number of instances to create an inner node and a random seed to initialize 
	 * the random number generator.
	 * 
	 * @param cp the information about the learning tasks
	 * @param maxDeep the maximal depth of the trees which will be built
	 * @param maxS the minimum number of instances to create an inner node
	 * @param randomSeed a seed to initialize the random number generator
	 */
	public SpecialCVTreeBuilder(CollectorPreferences cp, int maxDeep, int maxS, long randomSeed) throws RDTException {
		super(cp, maxDeep, maxS, randomSeed);
	}

	@Override
	protected Splitter createNumericSplitter(RDTAttribute attr, List<Instance> ions) throws RDTException{		
		Instance inst = ions.get(random.nextInt(ions.size()));
		
		//handle missing values
		if(inst.isMissing(attr.getAttributeId())){
			Iterator<Instance> iter = ions.iterator();
			do{
				if(iter.hasNext()){
					inst = iter.next();
				}else{
					return null;
				}
			}while(inst.isMissing(attr.getAttributeId()));
		}
		
		double threshold = inst.value(attr.getAttributeId());
		InstanceBasedNumericSplitter ns = new InstanceBasedNumericSplitter(attr.getAttributeId(), threshold);
		ns.setInstance(inst);
		return ns;
	}

	@Override
	public TreeBuilderType getType() {
		return TreeBuilderType.SPECIAL_CV;
	}

	@Override
	public Tree buildTree(RDTInstances trainInstances) throws RDTException {
		attributes = trainInstances.getAttributes();
		freeAttrs = trainInstances.getFreeAttributes();
		helpSet = new HashSet<Integer>();
		
		return new SpecialCVTree(buildNodesRecursively(transformToList(trainInstances), 0, null), cp);
	}

}
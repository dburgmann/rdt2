package rdt.tree.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import rdt.essentials.RDTAttribute;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.Tree;
import rdt.tree.builder.TreeBuilder;
import rdt.tree.builder.TreeBuilderType;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.node.InnerNode;
import rdt.tree.node.Leaf;
import rdt.tree.node.Node;
import rdt.tree.node.NodeType;
import rdt.tree.splitter.ConstructableSplitter;
import rdt.tree.splitter.FullNominalSplitter;
import rdt.tree.splitter.NumericSplitter;
import rdt.tree.splitter.Splitter;
import weka.core.Instance;

/**
 * Class for a tree-builder which builds the trees in batch-mode. This tree-builder builds
 * the trees recursively. That means that after a new inner node is created the splitter of 
 * the new created inner node is used to assign the instances to the child-nodes. In the child-
 * nodes the process of creating a new inner node is repeated with the assigned instances until
 * the stopping criteria is met (maximum depth is reached or not enough instances are available
 * to create  a inner-node). 
 * For the creation of each splitter in the inner nodes an attribute is chosen randomly. If the 
 * random attribute is nominal then a FullNominalSplitter is created. A FullNominalSplitter has 
 * for every value of the attribute one child-node. If the random attribute is numeric then a
 * NumericSplitter is created. To determine the threshold we choose an instance randomly and take
 * the value of that instance of the specific attribute as the threshold.
 * This tree-builder has two parameters. The first parameter is used to define the maximum depth
 * of the trees which will be build. If the tree-builder reaches the maximum depth leafs will be
 * created (no matter how many instances have reached this node). If the maximum depth is set to 0
 * then the tree will only consist of one leaf without any inner nodes. The second parameter maxS 
 * is used to define the minimum number of instances to create an inner node. If less than maxS 
 * instances are available then a leaf will be created. Whenever a leaf is created all the available
 * instances are transfered to the collectors of the leaf which will extract the relevant information
 * about the learning tasks.
 * 
 * @author MK
 */
public class BatchTreeBuilder implements TreeBuilder {
	
	/**
	 * Contains the information about the collectors which will be placed in the leafs.
	 */
	protected CollectorPreferences cp;
	
	/**
	 * The maximal depth of the tree. If the maximal depth is reached leafs will be created
	 * instead of inner nodes.
	 */
	protected int maxDeep;
	
	/**
	 * The generator for random numbers.
	 */
	protected Random random;
	
	/**
	 * The seed for the generator for random numbers
	 */
	protected long randomSeed;
	
	
	/**
	 * The number of instances which are necessary to build a inner-node. If less than maxS
	 * instances are available a leaf will be created.
	 */
	protected int maxS;
	
	/**
	 * The attributes of the training-instances. This array also contains the restricted attributes.
	 * You can check the attribute with attributes[i].isRestricted(), if you want to know if you
	 * can use the attribute for building the trees
	 */
	protected RDTAttribute[] attributes;
	
	/**
	 * This array contains only the attributes which are not labels.
	 */
	protected RDTAttribute[] freeAttrs;
	
	/**
	 * Contains all the nominal attribute-ids which have been tested in the nodes from the root to the 
	 * current node. 
	 */
	protected Set<Integer> helpSet;
	
	/**
	 * Create a new BatchTreeBuilder with the given maximal depth, the information about the learning
	 * tasks, the minimum number of instances to create an inner node and a random seed to initialize 
	 * the random number generator.
	 * 
	 * @param cp the information about the learning tasks
	 * @param maxDeep the maximal depth of the trees which will be built
	 * @param maxS the minimum number of instances to create an inner node
	 * @param randomSeed a seed to initialize the random number generator
	 */
	public BatchTreeBuilder(CollectorPreferences cp, int maxDeep, int maxS, long randomSeed) throws RDTException{
		this.maxDeep = maxDeep;
		this.cp = cp;
		this.maxS = maxS;
		this.randomSeed = randomSeed;
		this.random = new Random(randomSeed);
	}
	
	/**
	 * Transforms the given instances into a list of instances.
	 * 
	 * @return a list of instances
	 */
	protected List<Instance> transformToList(RDTInstances insts){
		List<Instance> ions = new LinkedList<Instance>();
		
		for(Instance i : insts.getDataSet()){
			ions.add(i);
		}
		return ions;
	}
	
	/**
	 * Builds recursively the tree and returns the root of the tree. In each call of this method
	 * the stopping criteria is checked. If the stopping criteria is not fulfilled a new inner-
	 * node will be created, otherwise a leaf will be created. If a new inner-node is created 
	 * the splitter of the newly created inner-node will be used to assign the instances to the
	 * child-nodes. For the child-nodes this method will be called again. If it is not possible 
	 * to assign an instance to a child-node (e.g. instance has a missing value) then the weight
	 * of the instance will be divided by the number of children and the instance will be forwarded
	 * to all children.
	 * 
	 * @param ions the instances which are available to build the current node
	 * @param currentDepth the current depth of the tree
	 * @param parentNode the parent node
	 * @return the root of the tree
	 */
	protected Node buildNodesRecursively(List<Instance> ions, int currentDepth, Node parentNode) throws RDTException{
		
		if(currentDepth >= maxDeep || ions.size() <= maxS){
			return createLeaf(ions);
		}else{
			currentDepth++;
			
			Node node = buildNode(ions, parentNode); 

			if(node.getNodeType() == NodeType.INNER_NODE){
				InnerNode in = (InnerNode) node;
				List<List<Instance>> newIons = null;
				HashMap<Instance, Double> oldWeights = null;
				
				if(in.getSplitter().isConstructable()){
					ConstructableSplitter splitter = (ConstructableSplitter) in.getSplitter();
					newIons = splitter.construct(ions);
					if(newIons == null){
						return createLeaf(ions);
					}
					in.setSplitter(splitter);
				}
				
				for(int i=0; i<in.getSplitter().getUsedAttributeIds().length; i++){
					RDTAttribute attr =  attributes[in.getSplitter().getUsedAttributeIds()[i]];
					if(attr.isNominal()){
						helpSet.add(in.getSplitter().getUsedAttributeIds()[i]);
					}
				}
				
				if(newIons == null){
					newIons = new LinkedList<List<Instance>>();
					
					for(int j=0; j<in.getNumberOfChildren(); j++){
						newIons.add(new LinkedList<Instance>());
					}
					
					oldWeights = new HashMap<Instance, Double>();
					
					for(Instance inst : ions){
						if(!in.getSplitter().canHandle(inst)){
							
							double oldWeight = inst.weight();
							oldWeights.put(inst, oldWeight);
							inst.setWeight(oldWeight / in.getNumberOfChildren());
							
							for(int i=0; i<in.getNumberOfChildren(); i++){
								newIons.get(i).add(inst);
							}
							
						}else{
							int accordingChild = in.getSplitter().determineChild(inst);
							newIons.get(accordingChild).add(inst);
						}
						
					}
				}
				
				
				for(int j=0; j<in.getSplitter().getNumberOfChilds(); j++){
					Node temp = buildNodesRecursively(newIons.get(j), currentDepth, node);
					in.setChild(j, temp);
					temp.setParent(in);
				}
				
				//reset instance weights
				if(oldWeights != null){
					for(Entry<Instance, Double> entry : oldWeights.entrySet()){
						entry.getKey().setWeight(entry.getValue());
					}
				}
				
				
				for(int i=0; i<in.getSplitter().getUsedAttributeIds().length; i++){
					helpSet.remove(in.getSplitter().getUsedAttributeIds()[i]);
				}
				
				
			}else{
				if(node.getNodeType() != NodeType.LEAF){
					throw new RDTException("Unkown node-type: " + node.getNodeType());
				}
			}
			return node;
		}
	}

	/**
	 * Tries to build a new inner-node. For building the splitter a random attribute is
	 * chosen by calling the method getNotUsedAttribute(...). If this method returns null
	 * a leaf will be created. Otherwise a nominal or numeric splitter splitter will be
	 * created and the newly created inner-node with this splitter will be returned.
	 * 
	 * @param ions the list of instances which can be used to create the splitter
	 * @param parentNode the parent node
	 * @return the newly created node (leaf or inner-node)
	 */
	protected Node buildNode(List<Instance> ions, Node parentNode) throws RDTException{
		
		InnerNode in = new InnerNode();
		in.setParent(parentNode);
		Splitter splitter = createSplitter(ions);
		
		if(splitter == null){
			return createLeaf(ions);
		}
		
		in.setSplitter(splitter);
		return in;
	}
	
	protected Splitter createSplitter(List<Instance> ions) throws RDTException{
		Splitter splitter = null;
		
		int count = 0;
		do{
			RDTAttribute attr = getRandomAttribute(helpSet, freeAttrs);
			
			if(attr == null){
				return null;
			}
			
			if(attr.isNominal()){
				splitter = createNominalSplitter(attr, ions);
			}else if(attr.isNumeric()){
				splitter = createNumericSplitter(attr, ions);
			}else{
				throw new RDTException("Unknown attribute-type!");
			}
			
			count++;
		
		}while(splitter == null && count < attributes.length);
		
		return splitter;
	}
	
	/**
	 * Creates a new leaf with new collectors which are provided by the collector-preferences.
	 * 
	 * @return a new leaf
	 */
	protected Leaf createLeaf(List<Instance> insts) throws RDTException{		
		Leaf leaf = new Leaf(cp.getNewCollectors());
		
		for(Instance inst : insts){
			leaf.addToCollectors(inst);
		}

		return leaf;
	}
	
	/**
	 * Returns a random attribute from the given attribute-array which attribute-id is not
	 * in the given set of attribute-ids.
	 * 
	 * @param usedAttrIds the set of attribute-ids
	 * @param attrs the attribute-array from which the random attribute will be chosen
	 * @return the random attribute or null if all attributes in the given array are contained in the set as well
	 */
	protected RDTAttribute getRandomAttribute(Set<Integer> usedAttrIds, RDTAttribute[] attrs){
		int t0 = random.nextInt(attrs.length);
		
		int t1 = t0;
		while (usedAttrIds.contains(attrs[t0].getAttributeId())) {
			t0++;
			t0 %= attrs.length;
			if (t0 == t1) {
				return null;
			}
		}	
		return attributes[attrs[t0].getAttributeId()];
	}
	
	/**
	 * Creates a new nominal splitter for the given attribute. The information given by the instances
	 * can be used to build the splitter.
	 * 
	 * @param attr the attribute for which the splitter will be created
	 * @param ions the instances which can be used to build the nominal splitter
	 * @return a nominal splitter for the given attribute
	 */
	protected Splitter createNominalSplitter(RDTAttribute attr, List<Instance> ions){
		return new FullNominalSplitter(attr.getAttributeId(), attr.getNumValues());
	}
	
	/**
	 * Creates a new numeric splitter for the given attribute. The information given by the instances
	 * can be used to build the splitter.
	 * 
	 * @param attr the attribute for which the splitter will be created
	 * @param ions the instances which can be used to build the numeric splitter
	 * @return a numeric splitter for the given attribute
	 */
	protected Splitter createNumericSplitter(RDTAttribute attr, List<Instance> ions) throws RDTException{		
		Instance inst = ions.get(random.nextInt(ions.size()));
		
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
		NumericSplitter ns = new NumericSplitter(attr.getAttributeId(), threshold);
		return ns;
	}

	@Override
	public TreeBuilderType getType() {
		return TreeBuilderType.BATCH;
	}

	@Override
	public Tree buildTree(RDTInstances trainInstances) throws RDTException {
		attributes = trainInstances.getAttributes();
		freeAttrs = trainInstances.getFreeAttributes();
		helpSet = new HashSet<Integer>();
		
		return new Tree(buildNodesRecursively(transformToList(trainInstances), 0, null), cp);
	}
	
	public Random getRandom(){
		return random;
	}

}
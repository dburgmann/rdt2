package rdt.tree.builder;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import rdt.essentials.RDTAttribute;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.QuantilTree;
import rdt.tree.Tree;
import rdt.tree.builder.TreeBuilder;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.node.InnerNode;
import rdt.tree.node.Leaf;
import rdt.tree.node.Node;
import rdt.tree.node.NodeType;
import rdt.tree.splitter.QuantilSplitter;
import rdt.tree.splitter.Splitter;
import rdt.tree.splitter.UpdateableFullNominalSplitter;
import rdt.tree.splitter.UpdateableSplitter;
import weka.core.Instance;

/**
 * 
 * TODO: Update comments!
 * 
 * @author MK
 */
public class QuantilTreeBuilder implements TreeBuilder, UpdateableTreeBuilder{

	private CollectorPreferences cp;
	private int maxHeight;
	private Random random;
	
	protected RDTAttribute[] curAttr;
	
	private int maxValues;
	private boolean conceptDrift;
	private boolean randomQuantil;
	private boolean interpolate;
	
	private double[] quantilValues;
	
	public QuantilTreeBuilder(CollectorPreferences cp, int maxDeep, int maxValues, boolean randomQuantil, boolean conceptDrift, boolean interpolate, long randomSeed) throws RDTException {
		this.cp = cp;
		this.maxHeight = maxDeep;
		this.maxValues = maxValues;
		this.random = new Random(randomSeed);
		this.conceptDrift = conceptDrift;
		this.randomQuantil = randomQuantil;
		this.interpolate = interpolate;
	}	
	
	private void initQuantilValues(){
		quantilValues = new double[maxHeight];
		
		for(int i=0; i<quantilValues.length; i++){
			if(randomQuantil){
				quantilValues[i] = random.nextDouble();
			}else{
				quantilValues[i] = 0.5;
			}
		}
	}
	
	protected Splitter createSplitter(int currentHeight) throws RDTException{
		Splitter splitter;
		
		if(curAttr[currentHeight].isNominal()){
			splitter = new UpdateableFullNominalSplitter(curAttr[currentHeight].getAttributeId());
		}else if(curAttr[currentHeight].isNumeric()) {
			splitter = new QuantilSplitter(curAttr[currentHeight].getAttributeId(), quantilValues[currentHeight], maxValues, conceptDrift, interpolate);
		}else{
			throw new RDTException("Unknown attribute-type!");
		}
		
		return splitter;
	}
	
	@Override
	public TreeBuilderType getType() {
		return TreeBuilderType.QUANTIL;
	}
	
	protected void buildWithInstance(Node node, Instance inst, int currentHeight) throws RDTException {
		if(node.getNodeType() == NodeType.INNER_NODE){
			
			InnerNode in = (InnerNode) node;
			if(in.getSplitter() instanceof UpdateableSplitter){
				UpdateableSplitter us = (UpdateableSplitter) in.getSplitter();
				
				if(us.canHandle(inst)){
					updateInnerNode(us, inst, in);
					currentHeight++;
					buildWithInstance(in.getChildAccordingTo(inst), inst, currentHeight);
				}else{
					if(us instanceof UpdateableFullNominalSplitter){
						updateInnerNode(us, inst, in);
						currentHeight++;
						buildWithInstance(in.getChildAccordingTo(inst), inst, currentHeight);
					}else if(us instanceof QuantilSplitter){
						double oldWeight = inst.weight();
						inst.setWeight(oldWeight/us.getNumberOfChilds());
						for(int i=0; i<us.getNumberOfChilds(); i++){
							currentHeight++;
							buildWithInstance(in.getChildAccordingTo(inst), inst, currentHeight);
						}
						inst.setWeight(oldWeight);
					}else{
						throw new RDTException("Only updateable nominal or quantil splitters are allowed in this tree!");
					}	
				}	
			}else{
				throw new RDTException("Only updateable splitters are allowed in this tree!");
			}
			
		}else if(node.getNodeType() == NodeType.LEAF){
			if(currentHeight >= curAttr.length){
				Leaf leaf = (Leaf) node;
				leaf.addToCollectors(inst);
				/*if(!buildUntrainedTrees){
					leaf.addToCollectors(inst);
				}*/
				
			}else{
				InnerNode parent = (InnerNode) node.getParent();
				InnerNode in = createInnerNode(parent, currentHeight);
				
				for(int i=0; i<parent.getNumberOfChildren(); i++){
					if(node.equals(parent.getChild(i))){
						parent.setChild(i, in);
						break;
					}
				}
				
				if(in.getSplitter() instanceof UpdateableSplitter){
					UpdateableSplitter us = (UpdateableSplitter) in.getSplitter();
					
					if(us.canHandle(inst)){
						updateInnerNode(us, inst, in);
						currentHeight++;
						buildWithInstance(in.getChildAccordingTo(inst), inst, currentHeight);
					}else{
						if(us instanceof UpdateableFullNominalSplitter){
							updateInnerNode(us, inst, in);
							currentHeight++;
							buildWithInstance(in.getChildAccordingTo(inst), inst, currentHeight);
						}else if(us instanceof QuantilSplitter){
							double oldWeight = inst.weight();
							inst.setWeight(oldWeight/us.getNumberOfChilds());
							for(int i=0; i<us.getNumberOfChilds(); i++){
								currentHeight++;
								buildWithInstance(in.getChildAccordingTo(inst), inst, currentHeight);
							}
							inst.setWeight(oldWeight);
						}else{
							throw new RDTException("Only updateable nominal or quantil splitters are allowed in this tree!");
						}
					}		
				}else{
					throw new RDTException("Only updateable splitters are allowed in this tree!");
				}
			}
			
		}else{
			throw new RDTException("Unkown node-type: " + node.getNodeType());
		}
		
	}
	
	protected void updateInnerNode(UpdateableSplitter us, Instance inst, InnerNode in) throws RDTException{
		if(us.update(inst)){
			Node[] newChildren = new Node[us.getNumberOfChilds()];
			Node[] oldChildren = in.getChildren();
			for(int i=0; i<oldChildren.length; i++){
				newChildren[i] = oldChildren[i];
			}
			for(int i=oldChildren.length; i<newChildren.length; i++){
				newChildren[i] = new Leaf(cp.getNewCollectors());
				newChildren[i].setParent(in);
			}
			in.setChildren(newChildren);
		}		
	}
	
	protected InnerNode createInnerNode(Node parent, int currentHeight) throws RDTException{
		InnerNode in = new InnerNode(createSplitter(currentHeight));
		in.setParent(parent);
		
		for(int i=0; i<in.getNumberOfChildren(); i++){
			Leaf leaf = new Leaf(cp.getNewCollectors());
			leaf.setParent(in);
			in.setChild(i, leaf);
		}
		
		return in;
	}
	
	protected RDTAttribute[] computeAttributesForLevel(RDTAttribute[] attributes) {
		
		List<RDTAttribute> attrList = new LinkedList<RDTAttribute>();
		
		for(int i=0; i<maxHeight; i++){
			RDTAttribute choosenAttr = null;
			int attrId =random.nextInt(attributes.length);
			int firstAttrId = attrId;
			while(choosenAttr == null){
				RDTAttribute randomAttr = attributes[attrId];
				if(!randomAttr.isRestricted()){
					if((randomAttr.isNumeric()) || (randomAttr.isNominal() && !attrList.contains(randomAttr))){
						choosenAttr = randomAttr;
						continue;
					}
				}
				attrId = (attrId + 1) % attributes.length;
				
				if(firstAttrId == attrId){
					return attrList.toArray(new RDTAttribute[attrList.size()]);
				}
			}
			attrList.add(choosenAttr);
		}
		return attrList.toArray(new RDTAttribute[attrList.size()]);
	}

	@Override
	public Tree buildTree(RDTInstances trainInstances) throws RDTException {
		
		QuantilTree qt;
		
		initQuantilValues();
		curAttr = computeAttributesForLevel(trainInstances.getAttributes());
		
		if(maxHeight == 0){
			qt = new QuantilTree(new Leaf(cp.getNewCollectors()), cp);
		}else{
			qt = new QuantilTree(createInnerNode(null, 0), cp);
		}
		
		for(Instance inst : trainInstances.getDataSet()){	
			update(qt, inst);
		}
		
		
		return qt;
	}

	@Override
	public void update(Tree tree, Instance inst) throws RDTException {
		buildWithInstance(tree.getRoot(), inst, 0);
	}

}

package rdt.tree.splitter;

import rdt.essentials.RDTException;
import weka.core.Instance;

/**
 * Class for a full nominal splitter which is updateable. During the update it is possible 
 * that the number of child-nodes can change (e.g. the instance contains an attribute value
 * which has not been seen before, so the splitter decides to have a new child-node for 
 * this value). Furthermore we have the problem that we do not know how to handle missing 
 * values because the number of child-nodes can change over time. As a result of this there
 * exists exactly one child-node which represents the missing value. All instances which
 * have a missing value for that instance will be forwarded to this child-node. In 
 * addition this splitter can be merged with another UpdateableFullNominalSplitter. 
 * 
 * @author MK
 */
public class UpdateableFullNominalSplitter implements UpdateableSplitter{
	
	/**
	 * The id of the nominal attribute which will be used to determine the child-node 
	 * for an instance.
	 */
	private int attributeId;
	
	/**
	 * The number of children to which the splitter can assign instances.
	 */
	private int numberOfChilds;
	
	/**
	 * Creates a new UpdateableFullNominalSplitter for a nominal attribute which 
	 * is specified by the given attribute-id.
	 * 
	 * @param attributeId the attribute-id of the nominal attribute
	 */
	public UpdateableFullNominalSplitter(int attributeId){
		this.attributeId = attributeId;
		this.numberOfChilds = 0;
	}
	
	/**
	 * Merges the splitter with the given splitter. The statistics of the current splitter will 
	 * be updated with the statistics of the given splitter. After this method the current splitter
	 * represents the fusion of both splitters.
	 * 
	 * @param otherSplitter the splitter with which the current splitter will be merged
	 */
	public void merge(UpdateableFullNominalSplitter other) throws RDTException {
		int[] otherAttrId = other.getUsedAttributeIds();
		
		if(otherAttrId.length == 1 && otherAttrId[0] == attributeId){
			if(numberOfChilds < other.getNumberOfChilds()){
				numberOfChilds = other.getNumberOfChilds();
			}
		}else{
			throw new RDTException("Can not merge splitters with different attributes!");
		}
	}
	
	@Override
	public boolean update(Instance inst) {
		if(inst.isMissing(attributeId)){
			if(numberOfChilds == 0){
				numberOfChilds = 2;
				return true;
			}
			return false;
		}
		
		if(inst.value(attributeId) + 3 > numberOfChilds){
			numberOfChilds = (int) (inst.value(attributeId) + 3);
			return true;
		}
		return false;
	}
	
	@Override
	public int determineChild(Instance inst) throws RDTException {
		if(inst.isMissing(attributeId)){
			return 0;
		}
		
		if(inst.value(attributeId) + 3 > numberOfChilds){
			return numberOfChilds-1;
		}
		return (int) inst.value(attributeId) + 1;
	}

	@Override
	public boolean canHandle(Instance inst) {
		return !inst.isMissing(attributeId);
	}

	@Override
	public int getNumberOfChilds() {
		return numberOfChilds;
	}

	@Override
	public int[] getUsedAttributeIds() throws RDTException {
		return new int[]{attributeId};
	}
	
	@Override
	public String toString() {
		return "UpdateableNominalSplitter\n"
				+ "Attribute: " + attributeId;
	}

	@Override
	public SplitterType getType() {
		return SplitterType.FULLNOMINAL_UPDATEABLE;
	}

	@Override
	public boolean isUpdateable() {
		return true;
	}

	@Override
	public boolean isConstructable() {
		return false;
	}

}

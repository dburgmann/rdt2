package rdt.tree.splitter;

import rdt.essentials.RDTException;
import rdt.tree.splitter.SplitterType;
import weka.core.Instance;

/**
 * Class for an instance-based-numeric-splitter. This splitter is equal to the normal 
 * numeric-splitter except it also contains the instance which was used to create the threshold
 * for this splitter. (This splitter is necessary to perform the fast leave-one-out evaluation,
 * because we have to check if we are allowed to use the splitter)
 * 
 * @author MK
 */
public class InstanceBasedNumericSplitter extends NumericSplitter{
	
	/**
	 * The id of the numeric attribute which is checked in this splitter
	 */
	private int attributeId;
	
	/**
	 * The threshold for the numeric attribute
	 */
	private double threshold;
	
	/**
	 * The instance which was used to create this splitter
	 */
	private Instance usedInst = null;
	
	/**
	 * Creates a new object of this class for the given attribute-id and the given
	 * threshold
	 * 
	 * @param attributeId, the given attribute-id
	 * @param threshold, the given threshold
	 */
	public InstanceBasedNumericSplitter(int attributeId, double threshold){
		super(attributeId, threshold);
		this.attributeId = attributeId;
		this.threshold = threshold;
	}
	
	/**
	 * Sets the instance which is used to create this splitter
	 * 
	 * @param inst, the instance which will be set
	 */
	public void setInstance(Instance inst){
		usedInst = inst;
	}
	
	/**
	 * Returns the instance which was used to create this splitter
	 * 
	 * @return the instance
	 */
	public Instance getInstance(){
		return usedInst;
	}
	
	@Override
	public int determineChild(Instance inst) throws RDTException {
		
		double value = inst.value(attributeId);
		if(value <= threshold){
			return 0;
		}
		return 1;
	}

	@Override
	public String toString() {
		return "NumericSplitter\n"
				+ "Attribute: " + attributeId + "\n"
						+ "Threshold: " + threshold + "\n"
						+ "Instance: yes";
	}

	@Override
	public boolean canHandle(Instance inst) {
		return !inst.isMissing(attributeId);
	}

	@Override
	public int[] getUsedAttributeIds() {
		return new int[]{attributeId};
	}

	@Override
	public int getNumberOfChilds() {
		return 2;
	}

	@Override
	public SplitterType getType(){
		return SplitterType.INSTANCE_BASED_NUMERIC;
	}
	
}

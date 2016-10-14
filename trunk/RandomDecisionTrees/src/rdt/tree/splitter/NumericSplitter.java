package rdt.tree.splitter;

import rdt.essentials.RDTException;
import rdt.tree.splitter.Splitter;
import weka.core.Instance;

/**
 * Class for a numeric-splitter. This splitter assigns the incoming instances to two
 * child-nodes according if the value of the instance is greater or smaller than the
 * pre-defined threshold.
 * 
 * @author MK
 */
public class NumericSplitter implements Splitter{
	
	/**
	 * The id of the numeric attribute which is checked in this splitter.
	 */
	private int attributeId;
	
	/**
	 * The threshold for the numeric attribute.
	 */
	private double threshold;
	
	/**
	 * Creates a new NumericSplitter for a numeric attribute which is specified by the given
	 * attribute-id. 
	 * 
	 * @param attributeId the attribute-id of the numeric attribute
	 * @param threshold the threshold to 
	 */
	public NumericSplitter(int attributeId, double threshold){
		this.attributeId = attributeId;
		this.threshold = threshold;
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
						+ "Threshold: " + threshold;
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
	public SplitterType getType() {
		return SplitterType.NUMERIC;
	}

	@Override
	public boolean isUpdateable() {
		return false;
	}

	@Override
	public boolean isConstructable() {
		return false;
	}

}

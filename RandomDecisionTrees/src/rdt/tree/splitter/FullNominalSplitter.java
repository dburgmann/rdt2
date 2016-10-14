package rdt.tree.splitter;

import rdt.essentials.RDTException;
import rdt.tree.splitter.Splitter;
import weka.core.Instance;

/**
 * Class for a full nominal splitter. This splitter assigns the incoming instances to the 
 * child-nodes according to value of the nominal attribute. Each unique value of the attribute
 * has its own child-node.
 * 
 * @author MK
 */
public class FullNominalSplitter implements Splitter{
	
	/**
	 * The id of the nominal attribute which will be used to determine the child-node 
	 * for an instance.
	 */
	protected int attributeId;
	
	/**
	 * The number of children to which the splitter can assign instances.
	 */
	protected int numValues;
	
	
	/**
	 * Creates a new FullNominalSplitter for a nominal attribute which is specified
	 * by the given attribute-id.
	 * 
	 * @param attributeId the attribute-id of the nominal attribute
	 * @param numValues the number of unique values of the attribute
	 */
	public FullNominalSplitter(int attributeId, int numValues){
		this.attributeId = attributeId;
		this.numValues = numValues;
	}
	
	@Override
	public int determineChild(Instance inst) throws RDTException {		
		return (int) inst.value(attributeId);
	}

	@Override
	public boolean canHandle(Instance inst) {
		return !inst.isMissing(attributeId);
	}

	@Override
	public int[] getUsedAttributeIds() throws RDTException {
		return new int[]{attributeId};
	}

	@Override
	public int getNumberOfChilds() {
		return numValues;
	}
	
	@Override
	public String toString(){
		return "FullNominalSplitter: Attribute: " + attributeId;
	}

	@Override
	public SplitterType getType() {
		return SplitterType.FULLNOMINAL;
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

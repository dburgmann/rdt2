package rdt.tree.splitter;

import rdt.tree.splitter.SplitterType;
import weka.core.Instance;

/**
 * Class for a full nominal splitter which can be activated. This splitter has the same 
 * functionality as the full nominal splitter except it can be activated or deactivated. 
 * If the splitter is deactivated it cannot process/handle any instance and if the 
 * splitter is activated it works as a full nominal splitter.
 * 
 * @author MK
 */
public class FullNominalActivateSplitter extends FullNominalSplitter implements ActiveSplitter{

	/**
	 * Determines whether the splitter is active or not
	 */
	private boolean active;
	
	/**
	 * Creates a new ActiveFullNominalSplitter for a nominal attribute which is specified
	 * by the given attribute-id. Initially the splitter is activated.
	 * 
	 * @param attributeId the attribute-id of the nominal attribute
	 * @param numValues the number of unique values of the attribute
	 */
	public FullNominalActivateSplitter(int attributeId, int numValues) {
		super(attributeId, numValues);
		
		this.active = true;
	}
	
	@Override
	public boolean canHandle(Instance inst) {
		return !inst.isMissing(attributeId) && active;
	}
	
	@Override
	public SplitterType getType(){
		return SplitterType.FULLNOMINAL_ACTIVE;	
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
		
	}

	@Override
	public boolean isActive() {
		return active;
	}
}

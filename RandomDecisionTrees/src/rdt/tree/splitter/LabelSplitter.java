package rdt.tree.splitter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rdt.essentials.RDTAttribute;
import rdt.essentials.RDTException;
import weka.core.Instance;

/**
 * Class for a LabelSplitter. A LabelSplitter is used in the MultilabelChainTreeBuilder and is a special
 * splitter which uses a label-attribute to split the instances. This splitter chooses the attribute 
 * itself. If after the creation the splitter does not have any used-attribute-ids then the creation
 * of the splitter failed. This means the splitter was not able to find an attribute to split.
 * Label attributes are often sparse. As a result of this an instance is picked randomly and all 
 * label-attributes are checked. If the instance has the value 1 for a label attribute and the attribute
 * is not tested in one of the splitters in the tree before then this attribute will be an candidate. 
 * After all label-attributes are checked we randomly select one of the candidates to be the 
 * attribute which will be used in this splitter. If no candidate was found then the procedure will
 * be repeated another instance and so on. 
 * 
 * @author MK
 */
public class LabelSplitter implements Splitter, ActiveSplitter{

	/**
	 * The attribute-id which will be used in this splitter. This array can contain 0 elements if
	 * the procedure to find a label attribute to split failed.
	 */
	private int[] usedAttrIds;
	
	/**
	 * This variable indicates if the splitter is active. If the splitter is active it will work 
	 * as a normal splitter. If the splitter is inactive then the splitter cannot handle any instances.
	 */
	private boolean active;
	
	/**
	 * Creates a new LabelSplitter with the given parameters.
	 * 
	 * @param ions the instances which can be used to determine the attribute and the split-value
	 * @param random the random number generator to generate some random numbers
	 * @param restrAttrs the attributes which can be selected as the attribute for this splitter
	 * @param usedAttrs the attribute-ids which have been used in the tree before
	 */
	public LabelSplitter(List<Instance> ions, Random random, RDTAttribute[] restrAttrs, Set<Integer> usedAttrs){

		this.active = true;
		
		int selectedId = determineAttribute(ions, random, restrAttrs, usedAttrs, 0);
		
		if(selectedId == -1){
			this.usedAttrIds = new int[0];
		}else{
			this.usedAttrIds = new int[]{selectedId};
		}
	}
	
	/**
	 * Tries to find an attribute for the splitter recursively. This method returns -1 if no proper attribute can be
	 * found.
	 * 
	 * @param ions the instance which can be used to determine the attribute
	 * @param random the random number generator to generate some random numbers
	 * @param attrs the attributes which can be selected
	 * @param usedAttrs the attribute-ids which have been used in the tree before
	 * @param round the current round of the recursive call
	 * @return the attribute-id of the attribute which will be tested in this splitter or -1 if no attribute was found
	 */
	private int determineAttribute(List<Instance> ions, Random random, RDTAttribute[] attrs, Set<Integer> usedAttrs, int round){
		
		if(ions.size() < round){
			return -1;
		}
		
		Instance inst = ions.get(random.nextInt(ions.size()));
		
		List<Integer> candidates = new LinkedList<Integer>();

		for(int i=0; i<attrs.length; i++){
			if(inst.value(attrs[i].getAttributeId()) == 1 && !usedAttrs.contains(attrs[i].getAttributeId())){
				candidates.add(attrs[i].getAttributeId());
			}
		}
		
		if(candidates.size() == 0){
			round++;
			return determineAttribute(ions, random, attrs, usedAttrs, round);
		}
		
		return candidates.get(random.nextInt(candidates.size()));
	}

	@Override
	public int determineChild(Instance inst) throws RDTException {
		return (int) inst.value(usedAttrIds[0]);
	}

	@Override
	public boolean canHandle(Instance inst) {
		return !inst.isMissing(usedAttrIds[0]) && active;
	}

	@Override
	public int getNumberOfChilds() {
		return 2;
	}

	@Override
	public int[] getUsedAttributeIds() throws RDTException {
		return usedAttrIds;
	}

	@Override
	public boolean isUpdateable() {
		return false;
	}

	@Override
	public boolean isConstructable() {
		return false;
	}

	@Override
	public SplitterType getType() {
		return SplitterType.LABEL;
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

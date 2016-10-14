package rdt.tree.splitter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rdt.essentials.RDTAttribute;
import rdt.essentials.RDTException;
import weka.core.Instance;

/**
 * Class for a SparseSplitter. A SparseSplitter can be used in trees for datasets which are sparse.
 * This splitter can only handle binary nominal attributes. This splitter chooses the attribute 
 * itself. If after the creation the splitter does not have any used-attribute-ids then the creation
 * of the splitter failed. This means the splitter was not able to find an attribute to split.
 * As a result of the sparse data the split-attribute is selected the following way. An instance is 
 * picked randomly and all attributes are checked. If the instance has the value 1 for an attribute
 * and the attribute is not tested in one of the splitters in the tree before then this attribute 
 * will be an candidate. After all attributes are checked we randomly select one of the candidates
 * to be the attribute which will be used in this splitter. If no candidate was found then the procedure
 * will be repeated another instance and so on. If no attribute can be found the used-attributes will be
 * an empty array.
 * 
 * @author MK
 */
public class SparseSplitter implements Splitter{

	/**
	 * The attribute-id which will be used in this splitter. This array can contain 0 elements if
	 * the procedure to find an attribute to split failed.
	 */
	private int[] usedAttrIds;

	
	/**
	 * Creates a new SparseSplitter with the given parameters.
	 * 
	 * @param ions the instances which can be used to determine the attribute and the split-value
	 * @param random the random number generator to generate some random numbers
	 * @param freeAttrs the attributes which can be selected as the attribute for this splitter
	 * @param usedAttrs the attribute-ids which have been used in the tree before
	 */
	public SparseSplitter(List<Instance> ions, Random random, RDTAttribute[] freeAttrs, Set<Integer> usedAttrs){
		int selectedId = determineAttribute(ions, random, freeAttrs, usedAttrs, 0);
		
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
		
		List<Integer> selectedAttr = new LinkedList<Integer>();

		for(int i=0; i<attrs.length; i++){
			if(inst.value(attrs[i].getAttributeId()) == 1 && !usedAttrs.contains(attrs[i].getAttributeId())){
				selectedAttr.add(attrs[i].getAttributeId());
			}
		}
		
		if(selectedAttr.size() == 0){
			round++;
			return determineAttribute(ions, random, attrs, usedAttrs, round);
		}
		
		return selectedAttr.get(random.nextInt(selectedAttr.size()));
	}
	
	@Override
	public int determineChild(Instance inst) throws RDTException {
		
		if(inst.value(usedAttrIds[0]) == 1){
			return 1;
		}
		return 0;
		
	}

	@Override
	public boolean canHandle(Instance inst) {	
		return !inst.isMissing(usedAttrIds[0]);
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
	public SplitterType getType() {
		return SplitterType.SPARSE;
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

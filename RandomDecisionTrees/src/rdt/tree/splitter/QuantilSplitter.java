package rdt.tree.splitter;

import java.util.Random;

import rdt.essentials.RDTException;
import rdt.util.SortedLinkedList;
import weka.core.Instance;

/**
 * Class for a quantil-splitter which is updateable. The idea of this splitter is that
 * it collects values of a numeric attribute during the incremental learning and uses
 * these to determine the split value. The split value is calculated by the pre-defined 
 * quantil of the collected values. If the quantil is 0.5 (median) the splitter picks the
 * threshold which splits the collected values in two equal sized partitions. To avoid 
 * memory problems during the runtime, the number of collected values has to be specified
 * during the initialization of the splitter. Furthermore this splitter supports concept
 * drift. If concept drift is activated the collected values will be replaced randomly 
 * by the values of the incoming instances. If interpolate is activated the splitter tries
 * to determine the split value by using interpolation between the closest values in the 
 * collected list according to the computed quantil. 
 *  
 * This splitter can only assign instances to two child-nodes (greater or smaller than
 * the threshold). In addition this splitter can be merged with another QuantilSplitter. 
 * 
 * @author MK
 */
public class QuantilSplitter implements Splitter, UpdateableSplitter{
	
	/**
	 * A list of the collected values which is sorted in an ascending order.
	 */
	private SortedLinkedList collectedValues;
	
	/**
	 * The id of the numeric attribute for which the values are collected and the 
	 * threshold is computed.
	 */
	private int attributeId;
	
	/**
	 * The quantil which will be used in this splitter (0.5 = median)
	 */
	private double quantil;
	
	/**
	 * The computed threshold for the numeric attribute
	 */
	private double threshold;
	
	/**
	 * The maximum number of values which will be collected in the list
	 */
	private int maxValues;
	
	/**
	 * A random-number-generator for the functionality of concept-drift
	 */
	private Random random;
	
	/**
	 * Specifies if concept drift should be performed or not.
	 */
	private boolean conceptDrift;
	
	/**
	 * Specifies if interpolation should be performed or not.
	 */
	private boolean interpolate;

	
	/**
	 * Creates a new QuantilSplitter for a numeric attribute which is specified by the given
	 * attribute-id. 
	 * 
	 * @param attributeId the attribute-id of the numeric attribute
	 * @param quantil the value for the quantil (have to be between 0 and 1)
	 * @param maxValues the number of values which will be collected
	 * @param conceptDrift if concept drift should be performed this boolean should be true
	 * @param interpolate if interpolation should be performed this boolean should be true
	 */
	public QuantilSplitter(int attributeId, double quantil, int maxValues, boolean conceptDrift, boolean interpolate) throws RDTException{
		if(quantil > 1 && quantil < 0){
			throw new RDTException("The quantil must be between 0 and 1!");
		}
		this.attributeId = attributeId;
		this.quantil = quantil;
		this.threshold = 0;
		this.conceptDrift = conceptDrift;
		this.interpolate = interpolate;
		
		this.collectedValues = new SortedLinkedList();
		this.maxValues = maxValues;
		this.random = new Random();
	}
	
	/**
	 * Merges the splitter with the given splitter. The statistics of the current splitter will 
	 * be updated with the statistics of the given splitter. After this method the current splitter
	 * represents the fusion of both splitters.
	 * 
	 * @param otherSplitter the splitter with which the current splitter will be merged
	 */
	public void merge(QuantilSplitter otherSplitter) throws RDTException {
		if(otherSplitter.getUsedAttributeIds()[0] == attributeId){
			if(quantil == otherSplitter.getQuantil()){
				collectedValues.addAll(otherSplitter.getValues());
				int pos = (int) Math.round(collectedValues.size() * quantil);
				if(pos == collectedValues.size()){
					pos--;
					if(collectedValues.size() == 0){
						threshold = 0;
					}else{
						threshold = collectedValues.get(pos);	
					}
				}else{
					threshold = collectedValues.get(pos);	
				}				
			}else{
				throw new RDTException("Can not merge splitters with different quantils!");
			}	
		}else{
			throw new RDTException("Can not merge splitters with different attributes!");
		}	
	}
	
	public SortedLinkedList getValues(){
		return collectedValues;
	}
	
	public double getQuantil(){
		return quantil;
	}
	
	@Override
	public boolean update(Instance inst) {
		if(collectedValues.size()<maxValues){
			double value = inst.value(attributeId);
			collectedValues.add(value);
			
			if(interpolate){
				if(collectedValues.size()%2 == 0){

					int pos1 = (int) (collectedValues.size() * quantil);
					pos1--;
					if(pos1 < 0){
						pos1 = 0;
					}
					int pos2 = pos1 + 1;
						
					double value1 = collectedValues.get(pos1);
					double value2 = collectedValues.get(pos2);
					
					threshold = (value2 - value1) * quantil + value1;
					
				}else{
					int pos = (int) (collectedValues.size() * quantil);
					if(pos >= collectedValues.size()){
						pos--;
					}
					threshold = collectedValues.get(pos);	
				}
			}else{
				int pos = (int) (collectedValues.size() * quantil);
				if(pos >= collectedValues.size()){
					pos--;
				}
				threshold = collectedValues.get(pos);
			}
			
		}else{
			if(conceptDrift){
				int randomNumber = random.nextInt(maxValues);
				
				collectedValues.remove(randomNumber);
				double value = inst.value(attributeId);
				collectedValues.add(value);
				
				if(interpolate){
					if(collectedValues.size()%2 == 0){

						int pos1 = (int) (collectedValues.size() * quantil);
						pos1--;
						if(pos1 < 0){
							pos1 = 0;
						}
						int pos2 = pos1 + 1;
							
						double value1 = collectedValues.get(pos1);
						double value2 = collectedValues.get(pos2);
						
						threshold = (value2 - value1) * quantil + value1;
						
					}else{
						int pos = (int) (collectedValues.size() * quantil);
						if(pos >= collectedValues.size()){
							pos--;
						}
						threshold = collectedValues.get(pos);	
					}
				}else{
					int pos = (int) (collectedValues.size() * quantil);
					if(pos >= collectedValues.size()){
						pos--;
					}
					threshold = collectedValues.get(pos);
				}
			}
	
		}
		return false;
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
	public boolean canHandle(Instance inst) {
		return !inst.isMissing(attributeId) && inst.attribute(attributeId).isNumeric();
	}

	@Override
	public int getNumberOfChilds() {
		return 2;
	}

	@Override
	public int[] getUsedAttributeIds() throws RDTException {
		return new int[]{attributeId};
	}
	
	@Override
	public String toString() {
		return "QuantilNumericSplitter\n"
				+ "Attribute: " + attributeId + "\n"
						+ "Quantil: " + quantil + "\n"
						+ "Split-Value: " + threshold;
	}

	@Override
	public SplitterType getType() {
		return SplitterType.QUANTIL;
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

package rdt.tree.collector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import evaluation.measure.Measure;
import evaluation.measure.MulanMeasure;
import rdt.essentials.RDTException;
import rdt.util.LPConverter;
import weka.core.Attribute;
import weka.core.Instance;

/**
 * Class for a collector which can perform multilabel-classification by using the label-powerset
 * method. The label-powerset method transforms the multilabel-classification into a standard
 * classification by assigning one class for each label-combination. For example a multilabel-task
 * with 5 labels will have 2^5 = 32 classes. This class uses a LPConverter to transform the 
 * labels into class and back. One LPConverter can be used in all collectors of an ensemble.
 * With this converter the collector works similar to a classification collector. Additionally
 * the distribution of the labels is saved which can be used to perform a better prediction.
 * 
 * @author MK
 */
public class LPMultilabelCollector implements Collector{

	/**
	 * The attribute-ids of the labels.
	 */
	private int[] attributeIds;
	
	/**
	 * In this map the class-distribution is stored. The key contains the class-value and 
	 * the value contains how many times the corresponding class-value has been seen. 
	 */
	private HashMap<Integer, Double> classCounts;
	
	/**
	 * The distribution of the labels. Each entry in the array represents the count of positive
	 * values of a specific label. 
	 */
	private double[] labelCount;
	
	/**
	 * The converter to convert a double-array into a class-value and a class-value into a
	 * double-array.
	 */
	private LPConverter converter;
	
	/**
	 * The number of instances which have been seen by the collector. This value is important
	 * to calculate how many labels have to be predicted.
	 */
	private double numInst;
	
	
	/**
	 * Creates a new LPMultilabelConverter for the given attribute-ids and with the given LPConverter.
	 * 
	 * @param attributeIds the attribute-ids of the labels
	 * @param converter the converter to convert the label-combination into a class-value and back
	 */
	public LPMultilabelCollector(int[] attributeIds, LPConverter converter) {
		this.attributeIds = attributeIds;
		this.classCounts = new HashMap<Integer, Double>();
		this.labelCount = new double[attributeIds.length];
		this.numInst = 0;
		this.converter = converter;
	}		
	
	/**
	 * Creates a new LPMultilabelConverter for the given attribute-ids, the class-counts, the label-count,
	 * the number of seen instances and the LPConverter.
	 * 
	 * @param attributeIds  the attribute-ids of the labels
	 * @param newClassCounts the distribution of the class-values
	 * @param labelCount the distribution of the labels
	 * @param newNumInst the number of instances which have been seen by the collector
	 * @param converter the converter to convert the label-combination into a class-value and back
	 */
	public LPMultilabelCollector(int[] attributeIds, HashMap<Integer, Double> newClassCounts, double[] labelCount, double newNumInst, LPConverter converter) {
		this.attributeIds = attributeIds;
		this.classCounts = newClassCounts;
		this.labelCount = labelCount;
		this.numInst = newNumInst;
		this.converter = converter;
	}

	/**
	 * Returns the distribution of the labels.
	 * 
	 * @return the distribution of the labels
	 */
	public double[] getLabelCount(){
		return labelCount;
	}
	
	/**
	 * Returns the converter which converts the label-combination in a class-value and back.
	 * 
	 * @return the converter
	 */
	public LPConverter getConverter(){
		return converter;
	}

	@Override
	public void addInstance(Instance inst) {
		double[] values = new double[attributeIds.length];
		for(int i=0; i<attributeIds.length; i++){
			if(!inst.isMissing(attributeIds[i])){
				labelCount[i] += (inst.value(attributeIds[i]) * inst.weight());
			}
			values[i] = inst.value(attributeIds[i]);
		}
		
		int hashValue = converter.put(values);
		
		if(classCounts.containsKey(hashValue)){
			double count = classCounts.get(hashValue);
			count += inst.weight();
			classCounts.put(hashValue, count);
		}else{
			classCounts.put(hashValue, inst.weight());
		}
		
		numInst += inst.weight();
	}
	


	@Override
	public void removeInstance(Instance inst) {
		double[] values = new double[attributeIds.length];
		for(int i=0; i<attributeIds.length; i++){
			if(!inst.isMissing(attributeIds[i])){
				labelCount[i] -= (inst.value(attributeIds[i]) * inst.weight());
			}
			values[i] = inst.value(attributeIds[i]);
		}
		
		int hashValue = converter.put(values);
		
		if(classCounts.containsKey(hashValue)){
			double count = classCounts.get(hashValue);
			
			if(count > inst.weight()){
				count -= inst.weight();
				classCounts.put(hashValue, count);
			}else{
				classCounts.remove(hashValue);
			}
		}
		
		if(numInst > inst.weight()){
			numInst -= inst.weight();
		}else{
			numInst = 0;
		}
	}

	@Override
	public void removeAllInstances() {
		classCounts = new HashMap<Integer, Double>();	
		labelCount = new double[attributeIds.length];
		numInst = 0;
	}

	@Override
	public CollectorType getType() {
		return CollectorType.LP_MULTILABEL;
	}

	@Override
	public Collector merge(Collector[] collectors) {
		HashMap<Integer, Double> newClassCounts = new HashMap<Integer, Double>(classCounts);
		double[] newLabelCount = Arrays.copyOf(labelCount, labelCount.length);
		double newNumInst = numInst;
		
		for(Entry<Integer, Double> entry : classCounts.entrySet()){
			newClassCounts.put(entry.getKey(), (entry.getValue()/numInst));
		}
		for(int i=0; i<newLabelCount.length; i++){
			newLabelCount[i] = labelCount[i]/numInst;
		}

		for(Collector c : collectors){
			if(c != null && c.getType() == CollectorType.LP_MULTILABEL){
				LPMultilabelCollector cc = (LPMultilabelCollector) c;

				
				double[] temp = cc.getLabelCount();
				for(int i=0; i<temp.length; i++){
					newLabelCount[i] += (temp[i]/cc.getNumInst());
				}
				
				newNumInst += cc.getNumInst();
				
				HashMap<Integer, Double> tempCounts = cc.getClassCounts();
				for(Entry<Integer, Double> entry : tempCounts.entrySet()){
					if(newClassCounts.containsKey(entry.getKey())){
						double oldValue = newClassCounts.get(entry.getKey());
						double addValue = (entry.getValue()/cc.getNumInst());
						double newValue = oldValue + addValue;
						
						newClassCounts.replace(entry.getKey(), newValue);					
					}else{
						newClassCounts.put(entry.getKey(), (entry.getValue()/cc.getNumInst()));
					}
				}
				
			}
		}
		
		newNumInst = (newNumInst/(collectors.length+1));
		
		for(Entry<Integer, Double> entry : newClassCounts.entrySet()){
			int key = entry.getKey();
			double value = entry.getValue();
		
			double newValue = (value/(collectors.length+1));
			newValue = newValue * newNumInst;
			
			newClassCounts.replace(key, newValue);
		}
		
		for(int i=0; i<newLabelCount.length; i++){
			newLabelCount[i] /= (collectors.length+1);
			newLabelCount[i] *= newNumInst;
		}
		
		return new LPMultilabelCollector(attributeIds, newClassCounts, newLabelCount, newNumInst, converter);
	}

	@Override
	public Collector add(Collector[] collectors) {
		HashMap<Integer, Double> newClassCounts = new HashMap<Integer, Double>(classCounts);
		double[] newLabelCount = Arrays.copyOf(labelCount, labelCount.length);
		double newNumInst = numInst;

		for(Collector c : collectors){
			if(c != null && c.getType() == CollectorType.LP_MULTILABEL){
				LPMultilabelCollector cc = (LPMultilabelCollector) c;

				double[] temp = cc.getLabelCount();
				
				for(int i=0; i<temp.length; i++){
					newLabelCount[i] += temp[i];
				}
				
				newNumInst += cc.getNumInst();
				
				HashMap<Integer, Double> tempCounts = cc.getClassCounts();
				for(Entry<Integer, Double> entry : tempCounts.entrySet()){
					if(newClassCounts.containsKey(entry.getKey())){
						newClassCounts.replace(entry.getKey(), (newClassCounts.get(entry.getKey()) + entry.getValue()));
					}else{
						newClassCounts.put(entry.getKey(), entry.getValue());
					}
				}		
			}
		}
		
		return new LPMultilabelCollector(attributeIds, newClassCounts, newLabelCount, newNumInst, converter);
	}

	private HashMap<Integer, Double> getClassCounts() {
		return classCounts;
	}


	@Override
	public double[] getPrediction() throws RDTException {

		double maxCount = -Double.MAX_VALUE;
		Integer predictedClass = -1;
		
		for(Entry<Integer, Double> entry : classCounts.entrySet()){
			if(entry.getValue() > maxCount){
				predictedClass = entry.getKey();
				maxCount = entry.getValue();
			}
		}

		return converter.getValues(predictedClass);
	}

	@Override
	public boolean canMakePrediction() {
		return (numInst > 0);
	}

	@Override
	public boolean canHandle(Instance inst) {
		for(int i=0; i<attributeIds.length; i++){
			Attribute attr = inst.attribute(attributeIds[i]);	
			if(!(attr.isNominal() && attr.numValues() == 2)){
				return false;
			}
		}
		return true;
	}

	@Override
	public int[] getUsedAttributes() {
		return attributeIds;
	}

	@Override
	public double getNumInst() {
		return numInst;
	}

	@Override
	public List<Measure> getMeasures() {
		List<Measure> measures = new LinkedList<Measure>();
		
		// add example-based measures
		measures.add(new MulanMeasure(new mulan.evaluation.measure.SubsetAccuracy(), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.HammingLoss(), false));
		/*measures.add(new MulanMeasure(new mulan.evaluation.measure.ExampleBasedPrecision(), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.ExampleBasedRecall(), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.ExampleBasedFMeasure(), false));*/
		measures.add(new MulanMeasure(new mulan.evaluation.measure.ExampleBasedAccuracy(), false));
		//measures.add(new MulanMeasure(new mulan.evaluation.measure.ExampleBasedSpecificity(), false));
		
        // add label-based measures
		measures.add(new MulanMeasure(new mulan.evaluation.measure.MicroPrecision(attributeIds.length), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.MicroRecall(attributeIds.length), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.MicroFMeasure(attributeIds.length), false));
		//measures.add(new MulanMeasure(new mulan.evaluation.measure.MicroSpecificity(attributeIds.length), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.MacroPrecision(attributeIds.length), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.MacroRecall(attributeIds.length), false));
		measures.add(new MulanMeasure(new mulan.evaluation.measure.MacroFMeasure(attributeIds.length), false));
		//measures.add(new MulanMeasure(new mulan.evaluation.measure.MacroSpecificity(attributeIds.length), false));
		
		return measures;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("LP-Collector:\n");
		
		for(Entry<Integer,Double> entry : classCounts.entrySet()){
			
			try {
				sb.append(entry.getValue()).append(" : ").append(Arrays.toString(converter.getValues(entry.getKey()))).append("\n");
			} catch (RDTException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}

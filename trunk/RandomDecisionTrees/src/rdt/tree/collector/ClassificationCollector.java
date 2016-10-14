package rdt.tree.collector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import evaluation.measure.Accuracy;
import evaluation.measure.Measure;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorType;
import weka.core.Instance;

/**
 * Class for a collector which can perform classification. During the training-process the
 * distribution of the values of the class attribute is created by using the instances which
 * have been added to this collector. In order to perform a prediction the majority class
 * will be selected.
 * 
 * @author MK
 */
public class ClassificationCollector implements Collector{

	/**
	 * The id of the class attribute.
	 */
	private int attributeId;
	
	/**
	 * In this map the class-distribution is stored. The key contains the class-value and 
	 * the value contains how many times the corresponding class-value has been seen. 
	 */
	private HashMap<Integer, Double> classCounts;
	
	/**
	 * The number of instances which have been seen by the collector.
	 */
	private double numInst;
	
	/**
	 * Creates a new ClassificationCollector for the class-attribute which is given by the
	 * attribute-id.
	 * 
	 * @param attributeId the attribute id of the class-attribute
	 */
	public ClassificationCollector(int attributeId){
		this.attributeId = attributeId;
		this.classCounts = new HashMap<Integer, Double>();
		this.numInst = 0;
	}
	
	/**
	 * Creates a new ClassificationCollector for the class-attribute which is given by the
	 * attribute-id, the given class-distribution and the given number of instances which
	 * have been seen by the collector.
	 * 
	 * @param attributeId the attribute id of the class-attribute
	 * @param classCounts the distribution of the class-values
	 * @param numInst the number of instances which have been seen by the collector
	 */
	public ClassificationCollector(int attributeId, HashMap<Integer, Double> classCounts, double numInst){
		this.attributeId = attributeId;
		this.classCounts = classCounts;
		this.numInst = numInst;
	}


	/**
	 * @return the distribution of the class-values.
	 */
	public HashMap<Integer, Double>  getClassCounts(){
		return classCounts;
	}
	
	/**
	 * @return the number of instances which have been seen by the collector
	 */
	public double getNumInst(){
		return numInst;
	}
	
	@Override
	public String toString(){
		return Double.toString(numInst);/*"Classification-Collector: " + classCounts.toString();*/
		
	}
	
	@Override
	public void addInstance(Instance inst) {
		if(classCounts.containsKey((int)inst.value(attributeId))){
			classCounts.replace((int) inst.value(attributeId), (classCounts.get((int)inst.value(attributeId)) + inst.weight()));		
		}else{
			classCounts.put((int) inst.value(attributeId), inst.weight());
		}
		
		numInst += inst.weight();
	}

	@Override
	public void removeInstance(Instance inst) {
		if(classCounts.containsKey((int)inst.value(attributeId))){
			classCounts.replace((int) inst.value(attributeId), (classCounts.get((int)inst.value(attributeId)) - inst.weight()));
			numInst -= inst.weight();
		}
	}

	@Override
	public void removeAllInstances() {
		classCounts = new HashMap<Integer, Double>();
		numInst = 0;
	}

	@Override
	public CollectorType getType() {
		return CollectorType.CLASSIFICATION;
	}

	@Override
	public Collector merge(Collector[] collectors) {

		HashMap<Integer, Double> newClassCounts = new HashMap<Integer, Double>(classCounts);
		double newNumInst = numInst;
		
		for(Entry<Integer, Double> entry : classCounts.entrySet()){
			newClassCounts.put(entry.getKey(), (entry.getValue()/numInst));
		}

		for(Collector c : collectors){
			if(c != null && c.getType() == CollectorType.CLASSIFICATION){
				ClassificationCollector cc = (ClassificationCollector) c;

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
		
		return new ClassificationCollector(attributeId, newClassCounts, newNumInst);
	}

	@Override
	public double[] getPrediction() {
		
		double maxCount = -Double.MAX_VALUE;
		int predictedClass = -1;
		
		for(Entry<Integer, Double> entry : classCounts.entrySet()){
			if(entry.getValue() > maxCount){
				predictedClass = entry.getKey();
				maxCount = entry.getValue();
			}
		}

		return new double[]{predictedClass};
	}

	@Override
	public boolean canMakePrediction() {
		return numInst > 0;
	}

	@Override
	public boolean canHandle(Instance inst) {
		if(inst.attribute(attributeId).isNominal()){
			return true;
		}
		return false;
	}

	@Override
	public int[] getUsedAttributes() {
		return new int[]{attributeId};
	}
	
	@Override
	public List<Measure> getMeasures() {
		List<Measure> measures = new LinkedList<Measure>();
		measures.add(new Accuracy());
		return measures;
	}

	@Override
	public Collector add(Collector[] collectors) {
		HashMap<Integer, Double> newClassCounts = new HashMap<Integer, Double>(classCounts);
		double newNumInst = numInst;

		for(Collector c : collectors){
			if(c != null && c.getType() == CollectorType.CLASSIFICATION){
				ClassificationCollector cc = (ClassificationCollector) c;

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
		
		return new ClassificationCollector(attributeId, newClassCounts, newNumInst);
	}

}

package rdt.tree.collector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import evaluation.measure.Measure;
import evaluation.measure.MulanMeasure;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorType;
import weka.core.Attribute;
import weka.core.Instance;

/**
 * Class for a collector which can perform multilabel-classification. During the training-process
 * the distribution of the labels is created by using the instances which have been added to this
 * collector. In order to perform a prediction the number of seen instances and the number of 
 * positive class-labels (distribution of labels) is used to calculate how many labels should
 * be predicted (regression). After that the computed amount of labels with the highest probability
 * in the label-distribution are selected which will be predicted as positive. All other labels
 * will be predicted as negative.  
 * 
 * @author MK
 */
public class MultilabelCollector implements Collector{
	

	/**
	 * The attribute-ids of the labels.
	 */
	protected int[] attributeIds;
	
	/**
	 * The distribution of the labels. Each entry in the array represents the count of positive
	 * values of a specific label. 
	 */
	protected double[] labelCount;
	
	/**
	 * The number of instances which have been seen by the collector. This value is important
	 * to calculate how many labels have to be predicted.
	 */
	protected double numInst;
	
	/**
	 * Creates a new MultilabelCollector for the labels which are given by there attribute-ids.
	 * 
	 * @param attributeIds the attribute-ids of the labels
	 */
	public MultilabelCollector(int[] attributeIds){
		this.attributeIds = attributeIds;
		this.labelCount = new double[attributeIds.length];
		this.numInst = 0;
	}
	
	/**
	 * Creates a new MultilabelCollector for the labels which are given by there attribute-ids,
	 * the given distribution of the labels and the given number of instances which have been 
	 * seen by the collector.
	 * 
	 * @param attributeIds the attribute-ids of the labels
	 * @param labelCount the distribution of the labels
	 * @param numInst the number of instances which have been seen by the collector
	 */
	public MultilabelCollector(int[] attributeIds, double[] labelCount, double numInst){
		this.attributeIds = attributeIds;
		this.labelCount = labelCount;
		this.numInst = numInst;
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
	 * Calculates how many labels should be predicted.
	 * 
	 * @return the number of labels which will be predicted
	 */
	public int computeNumberOfPredictedLabels(){
		double sum = 0;
		for(int i=0; i<labelCount.length; i++){
			sum += labelCount[i];
		}
		return (int) Math.round(sum/((double)numInst));
	}

	@Override
	public double getNumInst(){
		return numInst;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		StringBuilder sb2 = new StringBuilder("{");
		for(int i=0; i<labelCount.length; i++){
			sb.append(labelCount[i]);
			sb2.append(labelCount[i]/numInst);
			if(i < labelCount.length-1){
				sb.append(", ");
				sb2.append(", ");
			}
		}
		sb.append("}");
		sb2.append("}");
		
		return "MultiLabelCollector: \nNumInst: " + numInst + "\nNumOfPredLabels: " + computeNumberOfPredictedLabels() + "\n" 
				+ sb.toString() + "\n"
				+sb2.toString();
	}
	
	@Override
	public void addInstance(Instance inst) {
		for(int i=0; i<attributeIds.length; i++){
			if(!inst.isMissing(attributeIds[i])){
				labelCount[i] += (inst.value(attributeIds[i]) * inst.weight());
			}
		}
		numInst += inst.weight();
	}

	@Override
	public void removeInstance(Instance inst) {
		for(int i=0; i<attributeIds.length; i++){
			if(!inst.isMissing(attributeIds[i])){
				labelCount[i] -= (inst.value(attributeIds[i]) * inst.weight());
			}
		}
		numInst -= inst.weight();
		
		if(numInst<0){
			numInst = 0;
		}
	}

	@Override
	public void removeAllInstances() {
		labelCount = new double[attributeIds.length];
		numInst = 0;
	}

	@Override
	public CollectorType getType() {
		return CollectorType.MULTILABEL;
	}

	@Override
	public Collector merge(Collector[] collectors) {

		double[] newLabelCount = Arrays.copyOf(labelCount, labelCount.length);
		double newNumInst = numInst;
		
		for(int i=0; i<newLabelCount.length; i++){
			newLabelCount[i] = labelCount[i]/numInst;
		}
		
		for(Collector c : collectors){
			
			if(c.getType() == CollectorType.MULTILABEL){
				MultilabelCollector mlc = (MultilabelCollector) c;
				
				newNumInst += mlc.getNumInst();
				double[] temp = mlc.getLabelCount();
				
				for(int i=0; i<temp.length; i++){
					newLabelCount[i] += (temp[i]/mlc.getNumInst());
				}
			}
		}
		
		newNumInst /= (collectors.length+1);
		
		for(int i=0; i<newLabelCount.length; i++){
			newLabelCount[i] /= (collectors.length+1);
			newLabelCount[i] *= newNumInst;
		}
		
		return new MultilabelCollector(attributeIds, newLabelCount, newNumInst);
	}

	@Override
	public double[] getPrediction() {
		int numPredictedClasses = computeNumberOfPredictedLabels();
		
		int[] temp = weka.core.Utils.stableSort(labelCount);
		double[] predictedClasses = new double[labelCount.length];
		for(int i=0; i<numPredictedClasses; i++){
			predictedClasses[temp[temp.length - i - 1]] = 1;
		}
	
		return predictedClasses;
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
	public Collector add(Collector[] collectors) {
		double[] newLabelCount = Arrays.copyOf(labelCount, labelCount.length);
		double newNumInst = numInst;
		
		for(Collector c : collectors){
			
			if(c.getType() == CollectorType.MULTILABEL){
				MultilabelCollector mlc = (MultilabelCollector) c;
				
				newNumInst += mlc.getNumInst();
				double[] temp = mlc.getLabelCount();
				
				for(int i=0; i<temp.length; i++){
					newLabelCount[i] += temp[i];
				}
			}
		}
		return new MultilabelCollector(attributeIds, newLabelCount, newNumInst);
	}

}

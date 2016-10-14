package rdt.tree.collector;

import java.util.Arrays;

import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorType;

/**
 * Class for a collector which can perform multilabel-classification. This collector is very
 * similar to the standard multilabel-collector. The only difference is that we do not have to 
 * calculate the number of labels which will be predicted. In order to perform a prediction we 
 * predict all labels which have probability of 50% or higher in the label-distribution.
 * 
 * @author MK
 */
public class PMultilabelCollector extends MultilabelCollector{
	
	/**
	 * Creates a new PMultilabelCollector for the labels which are given by there attribute-ids.
	 * 
	 * @param attributeIds the attribute-ids of the labels
	 */
	public PMultilabelCollector(int[] attributeIds) {
		super(attributeIds);
	}

	/**
	 * Creates a new PMultilabelCollector for the labels which are given by there attribute-ids,
	 * the given distribution of the labels and the given number of instances which have been 
	 * seen by the collector.
	 * 
	 * @param attributeIds, the attribute-ids of the labels
	 * @param labelCount, an array containing the count of each label
	 * @param numInst, the number of train-instances which were seen in the train-instances
	 */
	public PMultilabelCollector(int[] attributeIds, double[] newLabelCount,
			double newNumInst) {
		super(attributeIds, newLabelCount, newNumInst);
	}

	@Override
	public double[] getPrediction() {
		double[] predictedClasses = new double[labelCount.length];
		for(int i=0; i<labelCount.length; i++){
			double relevance = labelCount[i]/numInst;
			if(relevance >= 0.5){
				predictedClasses[i] = 1;
			}
		}
		return predictedClasses;
	}
	
	@Override
	public Collector add(Collector[] collectors) {
		double[] newLabelCount = Arrays.copyOf(labelCount, labelCount.length);
		double newNumInst = numInst;
		
		for(Collector c : collectors){
			
			if(c.getType() == CollectorType.P_MULTILABEL){
				PMultilabelCollector mlc = (PMultilabelCollector) c;
				
				newNumInst += mlc.getNumInst();
				double[] temp = mlc.getLabelCount();
				
				for(int i=0; i<temp.length; i++){
					newLabelCount[i] += temp[i];
				}
			}
		}
		return new PMultilabelCollector(attributeIds, newLabelCount, newNumInst);
	}
	
	@Override
	public Collector merge(Collector[] collectors) {

		double[] newLabelCount = Arrays.copyOf(labelCount, labelCount.length);
		double newNumInst = numInst;
		
		for(int i=0; i<newLabelCount.length; i++){
			newLabelCount[i] = labelCount[i]/numInst;
		}
		
		for(Collector c : collectors){
			
			if(c.getType() == CollectorType.P_MULTILABEL){
				PMultilabelCollector mlc = (PMultilabelCollector) c;
				
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
		
		return new PMultilabelCollector(attributeIds, newLabelCount, newNumInst);
	}
	
	@Override
	public String toString(){
		StringBuilder sb2 = new StringBuilder("{");
		for(int i=0; i<labelCount.length; i++){
			sb2.append(labelCount[i]/numInst);
			if(i < labelCount.length-1){
				sb2.append(", ");
			}
		}
		sb2.append("}");
		
		return "PMultiLabelCollector: NumInst: " + numInst + "    :    "+ sb2.toString();
	}
	
	@Override
	public CollectorType getType() {
		return CollectorType.P_MULTILABEL;
	}
	
}

package rdt.tree.collector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import evaluation.measure.Measure;
import evaluation.measure.MulanMeasure;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorType;
import rdt.util.Pair;
import weka.core.Attribute;
import weka.core.Instance;

/**
 * DEPRECATED! Use the new LPCollector!
 * 
 * Class for a collector which can perform multilabel-classification by using the label-powerset
 * method. The label-powerset method transforms the multilabel-classification into a standard
 * classification by assigning classes for each label-combination. For example a multilabel-task
 * with 5 labels will have 2^5 = 32 classes.
 * 
 * This collector is working properly but it needs a better implementation. The challenge is to find
 * a good way to store all the different class-values and their count. (e.g. HashMaps do not work well
 * because usually there a lot of collectors in the trees of an ensemble (e.g. >1000). This does not
 * scale well if we have many instances and many labels) Because of that I do not provide any documentary
 * in this class.
 * 
 * @author MK
 */
public class LPMultilabelCollector_OLD implements Collector{
	
	private HashSet<Integer> set;	
	private List<Pair<double[], Double>> classCounts;
	private int[] attributeIds;
	private double numInst;
	
	public LPMultilabelCollector_OLD(int[] attributeIds) {
		this.attributeIds = attributeIds;
		this.numInst = 0;
		this.set = new HashSet<Integer>();
		this.classCounts = new LinkedList<Pair<double[], Double>>();
	}

	public LPMultilabelCollector_OLD(int[] attributeIds, HashSet<Integer> set, List<Pair<double[], Double>> classCounts, double numInst) {
		this.attributeIds = attributeIds;
		this.numInst = numInst;
		this.set = set;
		this.classCounts = classCounts;
	}

	@Override
	public double[] getPrediction() {
		double maxCount = -Double.MAX_VALUE;
		double[] predictedClasses = new double[attributeIds.length];
		
		for(Pair<double[], Double> temp : classCounts){
			if(temp.getSecond() > maxCount){
				predictedClasses = temp.getFirst();
				maxCount = temp.getSecond();	
			}
		}
		return predictedClasses;
	}

	@Override
	public void addInstance(Instance inst) {
		double[] values = new double[attributeIds.length];
		
		for(int i=0; i<attributeIds.length; i++){
			if(!inst.isMissing(attributeIds[i])){
				values[i] = inst.value(attributeIds[i]);
			}else{
				System.err.println("LPMultilabelCollector.addInstance(...) failure: instance with missing labels is added!");
			}
		}
		
		if(set.contains(Arrays.hashCode(values))){
			for(Pair<double[], Double> temp : classCounts){
				if(Arrays.hashCode(temp.getFirst()) == Arrays.hashCode(values)){
					temp.setSecond(temp.getSecond() + inst.weight());
					break;
				}
			}
		}else{
			classCounts.add(new Pair<double[], Double>(values, inst.weight()));
			set.add(Arrays.hashCode(values));
		}
		
		numInst += inst.weight();
	}

	@Override
	public void removeInstance(Instance inst) {
		double[] values = new double[attributeIds.length];
		
		for(int i=0; i<attributeIds.length; i++){
			if(!inst.isMissing(attributeIds[i])){
				values[i] = inst.value(attributeIds[i]);
			}else{
				System.err.println("LPMultilabelCollector.addInstance(...) failure: instance with missing labels is added!");
			}
		}
		
		if(set.contains(Arrays.hashCode(values))){
			double computedNumInst = 1;
			int i=0;
			for(Pair<double[], Double> temp : classCounts){
				if(Arrays.hashCode(temp.getFirst()) == Arrays.hashCode(values)){
					temp.setSecond(temp.getSecond() - inst.weight());	
					computedNumInst = temp.getSecond();
					break;
				}
				i++;
			}
			if(computedNumInst <= 0){
				set.remove(Arrays.hashCode(values));
				classCounts.remove(i);
			}
			numInst -= inst.weight();
		}
	}

	@Override
	public void removeAllInstances() {
		this.numInst = 0;
		this.set = new HashSet<Integer>();
		this.classCounts = new LinkedList<Pair<double[], Double>>();
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
	public CollectorType getType() {
		return CollectorType.LP_MULTILABEL_OLD;
	}

	@Override
	public Collector merge(Collector[] collectors) {
		List<Pair<double[], Double>> newClassCounts = new LinkedList<Pair<double[], Double>>(classCounts);
		HashSet<Integer> newSet = new HashSet<Integer>(set);
		double newNumInst = numInst;
		
		for(Pair<double[], Double> temp : newClassCounts){
			temp.setSecond(temp.getSecond()/newNumInst);	
		}
		
		for(Collector c : collectors){
			if(c != null && c.getType() == CollectorType.LP_MULTILABEL_OLD){
				LPMultilabelCollector_OLD lpmc = (LPMultilabelCollector_OLD) c;

				newNumInst += lpmc.getNumInst();
				
				List<Pair<double[], Double>> tempCounts = lpmc.getClassCounts();
				for(Pair<double[], Double> temp : tempCounts){
					if(newSet.contains(Arrays.hashCode(temp.getFirst()))){
						
						double oldValue = 0;
						for(Pair<double[], Double> temp2 : newClassCounts){
							if(Arrays.hashCode(temp2.getFirst()) == Arrays.hashCode(temp.getFirst())){
								oldValue = temp2.getSecond();
								break;
							}
						}
						
						double addValue = (temp.getSecond()/lpmc.getNumInst());
						double newValue = oldValue + addValue;
						
						for(Pair<double[], Double> temp2 : newClassCounts){
							if(Arrays.hashCode(temp2.getFirst()) == Arrays.hashCode(temp.getFirst())){
								temp2.setSecond(newValue);
								break;
							}
						}
						
						
					}else{						
						newClassCounts.add(new Pair<double[], Double>(temp.getFirst(), temp.getSecond()));
						newSet.add(Arrays.hashCode(temp.getFirst()));
					}
				}
				
			}
		}
		
		newNumInst = (newNumInst/(collectors.length+1));
		
		for(Pair<double[], Double> temp : newClassCounts){
			
			double newValue = (temp.getSecond()/(collectors.length+1));
			newValue = newValue * newNumInst;
			
			temp.setSecond(newValue);
		}
		
		
		return new LPMultilabelCollector_OLD(attributeIds, newSet, newClassCounts, newNumInst);
	
	}

	@Override
	public Collector add(Collector[] collectors) {
		
		List<Pair<double[], Double>> newClassCounts = new LinkedList<Pair<double[], Double>>(classCounts);
		HashSet<Integer> newSet = new HashSet<Integer>(set);
		double newNumInst = numInst;
		
		for(Collector c : collectors){
			if(c != null && c.getType() == CollectorType.LP_MULTILABEL_OLD){
				LPMultilabelCollector_OLD lpmc = (LPMultilabelCollector_OLD) c;

				newNumInst += lpmc.getNumInst();
				
				List<Pair<double[], Double>> tempCounts = lpmc.getClassCounts();
				for(Pair<double[], Double> temp : tempCounts){
					if(newSet.contains(Arrays.hashCode(temp.getFirst()))){
						
						double oldValue = 0;
						for(Pair<double[], Double> temp2 : newClassCounts){
							if(Arrays.hashCode(temp2.getFirst()) == Arrays.hashCode(temp.getFirst())){
								oldValue = temp2.getSecond();
								break;
							}
						}
						
						double addValue = temp.getSecond();
						double newValue = oldValue + addValue;
						
						for(Pair<double[], Double> temp2 : newClassCounts){
							if(Arrays.hashCode(temp2.getFirst()) == Arrays.hashCode(temp.getFirst())){
								temp2.setSecond(newValue);
								break;
							}
						}				
					}else{						
						newClassCounts.add(new Pair<double[], Double>(temp.getFirst(), temp.getSecond()));
						newSet.add(Arrays.hashCode(temp.getFirst()));
					}
				}		
			}
		}
		
		return new LPMultilabelCollector_OLD(attributeIds, newSet, newClassCounts, newNumInst);
	}
	
	public List<Pair<double[], Double>> getClassCounts() {
		return classCounts;
	}

	public double getNumInst() {
		return numInst;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("LP-Collector_OLD:\n");
		
		for(Pair<double[],Double> entry : classCounts){
			sb.append(entry.getSecond()).append(" : ").append(Arrays.toString(entry.getFirst())).append("\n");
		}
		return sb.toString();
	}

}

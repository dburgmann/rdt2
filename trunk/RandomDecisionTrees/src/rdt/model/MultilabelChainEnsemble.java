package rdt.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.MultilabelChainTree;
import rdt.tree.Tree;
import rdt.tree.Tree.PredictionCombination;
import rdt.tree.builder.MultilabelChainTreeBuilder;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.CollectorType;
import rdt.tree.collector.CombineCollectors;
import rdt.tree.collector.LPMultilabelCollector;
import rdt.tree.collector.MultilabelCollector;
import rdt.tree.collector.PMultilabelCollector;
import rdt.tree.splitter.ActiveSplitter;
import rdt.util.FisherYates;
import rdt.util.LPConverter;
import weka.core.Instance;

/**
 * Class to represent an ensemble of MultilabelChainTrees. A MultilabelChainEnsemble contains trees
 * which have been built with the MultilabelChainTreeBuilder. The idea of this model is to use some of
 * the splitters in the trees to test on the label attributes. In order to perform a prediction the
 * instance will be passed to the trees multiple times. Each time one of the labels will be predicted.
 * During the first time we do not know any values of the labels and all the the splitters which will
 * be tested are skipped (instance will be moved to all the child-nodes and the prediction will 
 * be combined (added) afterwards). After this the resulting prediction will be analyzed and one of
 * the labels will be chosen for which the value will be predicted. This method will be repeated
 * until all labels are predicted under the circumstances that in the following iterations the 
 * predicted labels can be used in the label-splitters. There multiple ways to predict the 
 * value for one of the labels which is determined by the current PredictType and ChainType.
 * The ChainType defines which values of the labels we want to predict first. For example if LOWEST
 * is chosen as the ChainType then the algorithm tries to predict the labels with the value 0 first 
 * before the algorithm tries to predict the labels with the value 1. In order to do that the results
 * of the collectors will be analyzed and the label with highest probability to have the value 0 will
 * be predicted in this round. If we only have some labels left which have a very high probability to
 * have the value 1 then predict the value 1 for the label with the highest probability. Depending on
 * which PredictType is chosen this method will perform differently but in general it tries to predict
 * the labels with the value 0 first. If we choose HIGHEST as the ChainType then the algorithm will try
 * too predict the labels with the value 1 first in the same manner as the LOWEST variant.
 * Nonetheless the ChainTypes HIGHEST and LOWEST do not perform very good. That is why the ChainType
 * HYBRID was implemented. The HYBRID tries to predict the next label with the highest probability to
 * have a certain value. For example if we have two labels. One of the labels have the probability of
 * 20% and the the other label has the probability of 75% to have the value 1. In this case the label
 * with the probability of 20% will be predicted with the value 0 because it is closer to 0% than 75%
 * is to 100%. If the value of the other label is 85% then this label will be predicted with the
 * value 1. Depending on which PredictType is chosen this method will perform differently as well.
 * It turns out that HYBRID is the best way to predict the next label. 
 * Furthermore you can choose the PredictType. The PredictType defines the way how the value for the
 * next label will be chosen under the circumstances of the ChainType. Currently there only exist
 * two way LABEL_CHAIN and PERCENTAGE_CHAIN which use the described chain method. LABEL_NORMAL and
 * PERCENTAGE_NORMAL do not use the chain method and only exist to do some particular experiments.
 * The PERCENTAGE_CHAIN method uses the probability of the labels to choose the value for the next
 * label. Labels which have a probability lower than 50% will be predicted with the value 0 and 
 * labels with a probability higher or equal than 50% will be predicted with the value 1. In 
 * combination with the HYBRID ChainType the next label will be predicted which is closer to 0% or
 * to 100% probability. If the probability is closer to 100% then this label will be predicted 
 * with the value 1 and if it is closer to 0% then this label will be predicted with the value 0.
 * The method LABEL_CHAIN works similar to the PERCENTAGE_CHAIN method. The only difference is
 * that LABEL_CHAIN method takes the averaged number of predicted labels into consideration. A 
 * label can only be predicted with the value 1 if the number of predicted labels with the value 1
 * do not exceed the average number of predicted labels.
 * 
 * The parameters "numChainPredict" and "percentageActivatedLabels" only exist to perform some
 * particular experiments. "numChainPredict" should have a value greater than the number of labels
 * and "percentageActivatedLabels" should have the value 1. In this case the algorithm works in
 * the right way. Furthermore the ChainType GIVEN_CHAIN exists for some specific experiments as well.
 * If you choose this method you can define a particular order in which the labels will be predicted.
 * 
 * IMPORTANT: The LP_CHAIN method is currently under construction and only works with the ChainType
 * HYBRID.
 * 
 * @author MK 
 */
public class MultilabelChainEnsemble extends Ensemble{

	/**
	 * The type how the the next label is chosen. The following types are possible:
	 * - LOWEST 		(predict the next label with the lowest probability)
	 * - HIGHEST		(predict the next label with the highest probability)
	 * - HYBRID			(predict the next label which probability is closer to 0% or 100%)
	 * - GIVEN_CHAIN	(use a predefined order to predict the labels)
	 */
	public enum ChainType {
		LOWEST,
		HIGHEST,
		HYBRID,
		GIVEN_CHAIN
	}
	
	/**
	 * The type of prediction, which is used. The following types are possible:
	 * - PERCENTAGE_NORMAL	(performs standard prediction with the PMultilabelCollector (not a chain method))
	 * - PERCENTAGE_CHAIN	(performs the chain-method and chooses the next label only according to the probability)
	 * - LABEL_NORMAL		(performs standard prediction with the MultilabelCollector (not a chain method))
	 * - LABEL_CHAIN		(performs the chain-method and chooses the next label according to the probability and
	 * 						the averaged number of predicted labels)
	 * - LP_NORMAL			(performs standard prediction with the LPMultilabelCollector (not a chain method))
	 * - LP_CHAIN			(TODO under construction)
	 */
	public enum PredictType {
		PERCENTAGE_NORMAL,
		PERCENTAGE_CHAIN,
		LABEL_NORMAL,
		LABEL_CHAIN,
		LP_NORMAL,
		LP_CHAIN,
	}
	
	/**
	 * This array contains the number of labels of each collector which is used in this model.
	 */
	private int[] maxLabels;
	
	/**
	 * This variable contains the number of labels of the collector which has the most labels.
	 */
	private int mostLabels;
	
	/**
	 * The PredictType which is currently used (see the description of the enumeration PredictType) 
	 */
	private PredictType predictType;
	
	/**
	 * The ChainType which is currently used (see the description of the enumeration ChainType) 
	 */
	private ChainType chainType;
	
	/**
	 * This variable only exists to do some experiments. To ensure the functionality of the
	 * chain methods you have to set the value of this variable higher than the number of 
	 * labels of the dataset.
	 * With this variable you can define how many labels should be predicted with the chain
	 * method. For example if you choose the value ten then the first ten labels will be
	 * predicted by the chain method and the rest of the labels will be predicted in one
	 * iteration afterwards, by using the normal prediction of the corresponding collector.
	 */
	private int numChainPredict;
	
	/**
	 * This variable only exists to do some experiments. To ensure the functionality of the
	 * chain methods you have to set the value of this variable equal to 1.
	 * With this variable it is possible to deactivate a certain amount of the label-splitters
	 * permanently. For example if the value is 0.5 than only the half of all label-splitters
	 * will be able to work in the trees.
	 */
	private double percentageActivatedLabels;
	
	/**
	 * This array contains all splitters which test on a label attribute.
	 */
	private ActiveSplitter[] activateSplitters;
	
	/**
	 * Contains all the MultilabelChainTrees of the ensemble.
	 */
	private MultilabelChainTree[] mTrees;
	
	/**
	 * The way how the predictions (collectors) are combined if a splitter tests on a missing
	 * value. You can add or merge collectors. In this special case of performing the chain
	 * method we choose to use ADD to combine the collectors because we want to achieve that
	 * the label-splitter does not have any influence on the prediction.
	 */
	private PredictionCombination predictionCombination;
	
	/**
	 * The random number generator to produce some random numbers.
	 */
	private Random random;
	
	/**
	 * In this variable the attribute-id of the last predicted attribute is stored.
	 */
	private int lastPredictedAttributeID = -1;
	
	
	/**
	 * Creates a new MultilabelChainEnsemble with the given information about the learning tasks,
	 * the number of trees, the maximal depth of the trees, the minimum number of instances to 
	 * create a splitter, a seed for the random number generator, the percentage of label-splitters
	 * in the ensemble, the percentage of the label-splitters which are activated, the number of 
	 * how many labels should be predicted with the chain method, the type of prediction and the 
	 * type how to predict the next label.
	 * 
	 * IMPORTANT
	 * "percentageActiveLabels" only exists to do some experiments. To ensure the functionality of
	 * the chain methods you have to set the value of this variable equal to 1.
	 * "numChainPredict only" exists to do some experiments. To ensure the functionality of the
	 * chain methods you have to set the value of this variable higher than the number of labels
	 * of the dataset.
	 * 
	 * @param cp the information about the learning tasks
	 * @param numTrees the number of trees
	 * @param maxDeep the maximal depth of the trees
	 * @param maxS the minimum number of instances to create a splitter
	 * @param randomSeed the seed for the random number generator
	 * @param percentageLabels the percentage of label-splitters in the ensemble
	 * @param percentageActiveLabels the percentage of the label-splitters which are activated
	 * @param numChainPredict the number of how many labels should be predicted with the chain
	 * method (ONLY for specific experiments should the value be chosen below the number of labels)
	 * @param predictType the type of prediction
	 * @param chainType the type how to predict the next label
	 */
	public MultilabelChainEnsemble(CollectorPreferences cp, int numTrees, int maxDeep, int maxS, long randomSeed, double percentageLabels, double percentageActiveLabels, int numChainPredict, PredictType predictType, ChainType chainType) throws RDTException {
		super(cp, new MultilabelChainTreeBuilder(cp, maxDeep, maxS, randomSeed, percentageLabels), numTrees);
		
		this.predictType = predictType;
		this.numChainPredict = numChainPredict;
		this.chainType = chainType;
		this.percentageActivatedLabels = percentageActiveLabels;		
		this.predictionCombination = PredictionCombination.ADD;
		this.random = new Random(randomSeed);
		
		int[][] restrictedAttributes = cp.getResrictedAttributeIds();
		
		cp.removeAllCollectors();
		
		switch(predictType){
		
			case LABEL_NORMAL:
			case LABEL_CHAIN:
				for(int i=0; i<restrictedAttributes.length; i++){
					cp.addCollector(new MultilabelCollector(restrictedAttributes[i]));
				}
				break;
			
			case PERCENTAGE_NORMAL:
			case PERCENTAGE_CHAIN:
				for(int i=0; i<restrictedAttributes.length; i++){
					cp.addCollector(new PMultilabelCollector(restrictedAttributes[i]));
				}
				break;
				
			case LP_NORMAL:
			case LP_CHAIN:
				LPConverter converter = new LPConverter();
				for(int i=0; i<restrictedAttributes.length; i++){
					cp.addCollector(new LPMultilabelCollector(restrictedAttributes[i], converter));
				}
				break;
				
			default:
				throw new RDTException("Multilabel-Trees can only be used with Multilabel-Collectors!");	
		}
		
		//compute maxLables
		List<Collector> collectors = cp.getCollectors();
		this.maxLabels = new int[collectors.size()];
		int i=0;
		for(Collector c : collectors){
			if(c.getType() == CollectorType.MULTILABEL || c.getType() == CollectorType.P_MULTILABEL || c.getType() == CollectorType.LP_MULTILABEL){
				maxLabels[i] = c.getUsedAttributes().length;
				if(mostLabels < maxLabels[i]){
					mostLabels = maxLabels[i];
				}
			}else{
				throw new RDTException("Multilabel-Trees can only be used with Multilabel-Collectors!");
			}
			i++;
		}
	}

	
	/**
	 * With this method you can change some parameters of this ensemble on the fly. You do not 
	 * need to rebuild the ensemble if you change one or more of these parameters.
	 * 
	 * @param chainType the type, which describes how the next label is chosen
	 * @param numChainPredict the number of labels which will be predicted with the chain-method 
	 * @param percentageActivatedLabels the percentage of active label-tests in the trees
	 */
	public void setVariables(ChainType chainType, int numChainPredict, double percentageActivatedLabels) throws RDTException{
		this.chainType = chainType;
		this.numChainPredict = numChainPredict;
		this.percentageActivatedLabels = percentageActivatedLabels;
		
		this.activateSplitters();
	}
	
	/**
	 * This method activates a certain amount of label-splitters according to the value of the variable
	 * percentageActivatedLabels.
	 */
	private void activateSplitters(){
		int numActivatedSplitters = (int) (activateSplitters.length * percentageActivatedLabels);
		
		for(int i=0; i<activateSplitters.length; i++){
			if(i < numActivatedSplitters){
				activateSplitters[i].setActive(true);
			}else{
				activateSplitters[i].setActive(false);
			}
		}
	}
	
	@Override
	public void build(RDTInstances insts) throws RDTException {
		super.build(insts);
		
		List<ActiveSplitter> allActiveSplitters = new LinkedList<ActiveSplitter>();
		
		mTrees = new MultilabelChainTree[trees.length];
		
		int i=0;
		for(Tree t : trees){
			MultilabelChainTree mt = (MultilabelChainTree) t;
			mTrees[i] = mt;
			mTrees[i].setPredcitionCombination(predictionCombination);
			allActiveSplitters.addAll(mt.getLabelSplitters());
			i++;
		}
		
		activateSplitters = new ActiveSplitter[allActiveSplitters.size()];
		allActiveSplitters.toArray(activateSplitters);
		
		FisherYates.shuffleArray(random, activateSplitters);
		
		activateSplitters();
	}
	
	

	@Override
	public Collector[] predict(Instance inst) throws RDTException {
		//Save value and set missing values for restricted attributes
		int[] restrictedAttributes = cp.getAllResrictedAttributeIds();
		double[] restrictedValues = new double[restrictedAttributes.length];
		for(int i=0; i<restrictedAttributes.length; i++){
			restrictedValues[i] = inst.value(restrictedAttributes[i]);
			inst.setMissing(restrictedAttributes[i]);
		}
		
		
		Collector[] prediction;
		
		if(predictType == PredictType.LABEL_NORMAL || predictType == PredictType.PERCENTAGE_NORMAL || predictType == PredictType.LP_NORMAL){
			prediction = super.predict(inst);
		}else if(predictType == PredictType.LABEL_CHAIN || predictType == PredictType.PERCENTAGE_CHAIN || predictType == PredictType.LP_CHAIN){
			prediction = predictChain(inst);
		}else{
			throw new RDTException("MultilabelChain: Type for prediction is unknown!");
		}
		
		
		//Restore values of restricted attributes
		for(int i=0; i<restrictedValues.length; i++){
			inst.setValue(restrictedAttributes[i], restrictedValues[i]);
		}
		
		
		
		return prediction;
	}
	
	/**
	 * Performs the prediction with the chain method for the given instance.
	 * 
	 * @param inst the instance for which the prediction will be created
	 * @return the prediction for the instance
	 */
	private Collector[] predictChain(Instance inst) throws RDTException{	
		//checking, if all values are missing
		for(int id : cp.getAllResrictedAttributeIds()){
			if(!inst.isMissing(id)){
				throw new RDTException("Predicting an instance with already given classes is not possible in a Multilabel-Tree");
			}
		}
		
		int numPredictedLabels = 0;
		int[] numPositiveLabels = new int[maxLabels.length];
		Collector[][] savedCols = new Collector[trees.length][];
		boolean first = true;
		
		while(numPredictedLabels < mostLabels && numPredictedLabels <= numChainPredict){
			CombineCollectors cc = new CombineCollectors(cp.getNumCollectors(),trees.length);
			
			int z=0;
			for(MultilabelChainTree t : mTrees){
				
				if(first){
					savedCols[z] = t.predict(inst);
				}else{
					//Only predict new collectors if the new predicted label is tested in a label-test on the path of the last prediction
					if(t.getCheckedRestrictedAttributeIds().contains(lastPredictedAttributeID)){
						savedCols[z] = t.predict(inst);
					}
				}
				
				cc.addCollectors(savedCols[z]);
				
				z++;
			}
			first = false;
			Collector[] collectors = cc.combine(PredictionCombination.MERGE);
			
			int i=0;
			for(Collector col : collectors){
				

				if(numPredictedLabels < maxLabels[i]){
					
					switch(predictType){
					
					case LABEL_CHAIN:
						MultilabelCollector mc = (MultilabelCollector) col;
						numPositiveLabels[i] += predictNextClass(mc, inst, numPositiveLabels[i], numPredictedLabels);
						break;
						
					case PERCENTAGE_CHAIN:
						PMultilabelCollector pmc = (PMultilabelCollector) col;
						numPositiveLabels[i] += predictNextClass(pmc, inst, numPositiveLabels[i], numPredictedLabels);
						break;
					
					case LP_CHAIN:
						LPMultilabelCollector tmp = (LPMultilabelCollector)col;
						
						predictNextClass(tmp, inst);
						
						
						break;
						
					default:
						throw new RDTException("Predict-Type does not exist!");					
					}	
					
				}
				
				if(numPredictedLabels == numChainPredict && (numPredictedLabels+1) < maxLabels[i]){
					int[] attrIds = col.getUsedAttributes();
					
					double[] values = col.getPrediction();
					for(int j=0; j<attrIds.length; j++){
						if(inst.isMissing(attrIds[j])){
							inst.setValue(attrIds[j], values[j]);
						}
					}
				}
			}
			numPredictedLabels++;
		}
		
		//Create a new collector with the values of the instance
		MultilabelCollector[] finalCollectors = new MultilabelCollector[cp.getNumCollectors()];
		int s = 0;
		for(Collector c : cp.getCollectors()){
			int[] attrIds = c.getUsedAttributes();
			double[] newLabelCount = new double[attrIds.length];
			
			for(int i=0; i<attrIds.length; i++){
				if(inst.value(attrIds[i]) == 1){
					newLabelCount[i] = 1;
				}
			}
			
			finalCollectors[s] = new MultilabelCollector(attrIds, newLabelCount, 1);
			s++;
		}
				
		//remove the predicted values from the instance
		for(int id : cp.getAllResrictedAttributeIds()){
			inst.setMissing(id);
		}
				
		return finalCollectors;
	}
	
	/**
	 * Predicts the next label for the LP_CHAIN method for the instance and with the current prediction.
	 * 
	 * IMPROTANT: This method is under construction!
	 * 
	 * @param col the current prediction
	 * @param inst the instance for which the next label will be predicted
	 */
	private void predictNextClass(LPMultilabelCollector col, Instance inst) throws RDTException{
		int[] attrIds = col.getUsedAttributes();
		double[] labelCount = col.getLabelCount();
		double[] prediction = col.getPrediction();
		double numInst = col.getNumInst();
		
		//System.out.println(Arrays.toString(labelCount));
		//System.out.println(Arrays.toString(prediction));
		
		double highest = 0;
		int highestIndex = -1;
		double lowest = Double.MAX_VALUE;
		int lowestIndex = -1;
		
		for(int i=0; i<prediction.length; i++){
			if(inst.isMissing(attrIds[i])){
				if(prediction[i] == 1.0){
					if(labelCount[i] > highest){
						highest = labelCount[i];
						highestIndex = i;
					}
				}else{
					if(labelCount[i] < lowest){
						lowest = labelCount[i];
						lowestIndex = i;
					}
				}
			}
		}
		
		/*if(highestIndex>=0){
			System.out.println(attrIds[highestIndex] + "\tVALUE: " + highest);
		}
		
		if(lowestIndex>=0){
			System.out.println(attrIds[lowestIndex] + "\tVALUE: " + lowest);
		}
		System.out.println(numInst);
		*/
		if((numInst-highest) <= lowest){
			inst.setValue(attrIds[highestIndex], 1.0);
			lastPredictedAttributeID = attrIds[highestIndex];
		}else{
			inst.setValue(attrIds[lowestIndex], 0.0);
			lastPredictedAttributeID = attrIds[lowestIndex];
		}
		
		/*System.out.println(lastPredictedAttributeID);
		System.out.println(inst.hashCode());
		
		double[] values = new double[attrIds.length];
		
		for(int i=0; i<values.length; i++){
			values[i] = inst.value(attrIds[i]);
		}
		
		System.out.println(Arrays.toString(values));
		
		System.out.println();
		//System.out.println(inst);
		
		/*int[] attrIds = col.getUsedAttributes();
		double[] labelCount = col.getLabelCount();
		double[] prediction = col.getPrediction();
		double numInst = col.getNumInst();
		
		int indexBiggest = -1;
		int indexSmallest = -1;
		for(int i=0; i<labelCount.length; i++){
			if(inst.isMissing(attrIds[i]) && (indexBiggest == -1 || labelCount[i] >= labelCount[indexBiggest])){
				indexBiggest = i;
			}
			if(inst.isMissing(attrIds[i]) && (indexSmallest == -1 || labelCount[i] < labelCount[indexSmallest])){
				indexSmallest = i;
			}
		}
		double percentageBiggest = labelCount[indexBiggest] / numInst;
		double percentageSmallest = labelCount[indexSmallest] / numInst;
		
		System.out.println(percentageBiggest);
		System.out.println(percentageSmallest);
		
		double difference = 1 - percentageBiggest;
		double percentage;
		int attrId;
		boolean biggest;
		
		if(difference < percentageSmallest){
			percentage = percentageBiggest;
			attrId = attrIds[indexBiggest];
			
			
			
			
			
			biggest = true;
		}else{
			percentage = percentageSmallest;
			attrId = attrIds[indexSmallest];
			biggest = false;
		}
	
		lastPredictedAttributeID = attrId;*/
		
		
		//System.exit(0);
	}
	
	/**
	 * Predicts the next class for the LABEL_CHAIN and PERCENTAGE_CHAIN method for the instance
	 * and with the current prediction. Furthermore the method requires some meta-information like 
	 * the number of already predicted labels and the number of positive predicted labels.
	 * 
	 * @param col the current prediction
	 * @param inst the instance for which the next label will be predicted
	 * @param numPositiveLabels the number of positive labels which are already predicted
	 * @param numPredictedLabels, the number of labels which are already predicted
	 * @return 1 if the label which is predicted is positive and 0 if the label is negative 
	 */
	private int predictNextClass(MultilabelCollector col, Instance inst, int numPositiveLabels, int numPredictedLabels) throws RDTException{
		int[] attrIds = col.getUsedAttributes();
		double[] labelCount = col.getLabelCount();
		int numLabels = col.computeNumberOfPredictedLabels();
		double numInst = col.getNumInst();
		
		int indexBiggest = -1;
		int indexSmallest = -1;
		for(int i=0; i<labelCount.length; i++){
			if(inst.isMissing(attrIds[i]) && (indexBiggest == -1 || labelCount[i] >= labelCount[indexBiggest])){
				indexBiggest = i;
			}
			if(inst.isMissing(attrIds[i]) && (indexSmallest == -1 || labelCount[i] < labelCount[indexSmallest])){
				indexSmallest = i;
			}
		}
		double percentageBiggest = labelCount[indexBiggest] / numInst;
		double percentageSmallest = labelCount[indexSmallest] / numInst;

		if(chainType == ChainType.HYBRID){
			return predictNextClassHybrid(inst, attrIds, percentageBiggest, indexBiggest, percentageSmallest, indexSmallest, numPredictedLabels, numPositiveLabels, numLabels);	
		}else if(chainType == ChainType.HIGHEST){
			return predictNextClassHighest(inst, attrIds, percentageBiggest, indexBiggest, indexSmallest, numPositiveLabels, numLabels);
		}else if(chainType == ChainType.LOWEST){
			return predictNextClassLowest(inst, attrIds, indexBiggest, percentageSmallest, indexSmallest, numPredictedLabels, numLabels);
		}else if(chainType == ChainType.GIVEN_CHAIN){
			return predictNextClassChainOrder(inst, attrIds, labelCount, numInst, numPredictedLabels);
		}else{
			throw new RDTException("MultilabelChain: ChainType is unknown!");
		}
	
	}

	/**
	 * Predicts the next class for the LABEL_CHAIN and PERCENTAGE_CHAIN method with the ChainType LOWEST and
	 * some additional information.
	 * 
	 * @param inst the instance for which the next label will be predicted
	 * @param attrIds the attribute-ids of the labels of the collector
	 * @param indexBiggest the index of the label with the highest probability
	 * @param percentageSmallest the probability of the label with the lowest probability
	 * @param indexSmallest the index of the label with the lowest probability
	 * @param numPredictedLabels the number of labels which are already predicted with the chain-method 
	 * @param numLabels the averaged number of positive labels of the collector
	 * @return 1 if the label which is predicted is positive and 0 if the label is negative 
	 */
	private int predictNextClassLowest(Instance inst, int[] attrIds, int indexBiggest, double percentageSmallest, int indexSmallest, int numPredictedLabels, int numLabels) throws RDTException {
		if(predictType == PredictType.LABEL_CHAIN){
			if((attrIds.length - numPredictedLabels) > numLabels){
				lastPredictedAttributeID = attrIds[indexSmallest];
				inst.setValue(attrIds[indexSmallest], 0);
				return 0;
			}else{
				lastPredictedAttributeID = attrIds[indexBiggest];
				inst.setValue(attrIds[indexBiggest], 1);
				return 1;
			}
			
		}else if(predictType == PredictType.PERCENTAGE_CHAIN){
			if(percentageSmallest < 0.5){
				lastPredictedAttributeID = attrIds[indexSmallest];
				inst.setValue(attrIds[indexSmallest], 0);
				return 0;
			}else{
				lastPredictedAttributeID = attrIds[indexBiggest];
				inst.setValue(attrIds[indexBiggest], 1);
				return 1;
			}	
			
		}else{
			throw new RDTException("MultilabelChain: Type of Prediction is unknown!");
		}
	}
	
	/**
	 * Predicts the next class for the LABEL_CHAIN and PERCENTAGE_CHAIN method with the ChainType HIGHEST and
	 * some additional information.
	 * 
	 * @param inst the instance for which the next label will be predicted
	 * @param attrIds the attribute-ids of the labels of the collector
	 * @param percentageBiggest, the probability of the label with the highest probability
	 * @param indexBiggest the index of the label with the highest probability
	 * @param indexSmallest the index of the label with the lowest probability
	 * @param numPositiveLabels the number of positive labels which are already predicted
	 * @param numLabels the averaged number of positive labels of the collector
	 * @return 1 if the label which is predicted is positive and 0 if the label is negative 
	 */
	private int predictNextClassHighest(Instance inst, int[] attrIds, double percentageBiggest, int indexBiggest, int indexSmallest, int numPositiveLabels, int numLabels) throws RDTException{
		
		if(predictType == PredictType.LABEL_CHAIN){
			if(numPositiveLabels < numLabels){
				lastPredictedAttributeID = attrIds[indexBiggest];
				inst.setValue(attrIds[indexBiggest], 1);
				return 1;
			}else{
				lastPredictedAttributeID = attrIds[indexSmallest];
				inst.setValue(attrIds[indexSmallest], 0);
				return 0;
			}
			
		}else if(predictType == PredictType.PERCENTAGE_CHAIN){
			if(percentageBiggest >= 0.5){
				lastPredictedAttributeID = attrIds[indexBiggest];
				inst.setValue(attrIds[indexBiggest], 1);
				return 1;
			}else{
				lastPredictedAttributeID = attrIds[indexSmallest];
				inst.setValue(attrIds[indexSmallest], 0);
				return 0;
			}	
			
		}else{
			throw new RDTException("MultilabelChain: Type of Prediction is unknown!");
		}
	}
	
	/**
	 * Predicts the next class for the LABEL_CHAIN and PERCENTAGE_CHAIN method with the ChainType HYBRID and
	 * some additional information.
	 * 
	 * @param inst the instance for which the next label will be predicted
	 * @param attrIds the attribute-ids of the labels of the collector
	 * @param percentageBiggest, the probability of the label with the highest probability
	 * @param indexBiggest the index of the label with the highest probability
	 * @param percentageSmallest the probability of the label with the lowest probability
	 * @param indexSmallest the index of the label with the lowest probability
	 * @param numPredictedLabels the number of labels which are already predicted with the chain-method 
	 * @param numPositiveLabels the number of positive labels which are already predicted
	 * @param numLabels the averaged number of positive labels of the collector
	 * @return 1 if the label which is predicted is positive and 0 if the label is negative 
	 */
	private int predictNextClassHybrid(Instance inst, int[] attrIds, double percentageBiggest, int indexBiggest, double percentageSmallest, int indexSmallest, int numPredictedLabels, int numPositiveLabels, int numLabels) throws RDTException{
		double difference = 1 - percentageBiggest;
		double percentage;
		int attrId;
		boolean biggest;
		
		if(difference < percentageSmallest){
			percentage = percentageBiggest;
			attrId = attrIds[indexBiggest];
			biggest = true;
		}else{
			percentage = percentageSmallest;
			attrId = attrIds[indexSmallest];
			biggest = false;
		}
	
		lastPredictedAttributeID = attrId;
		
		if(predictType == PredictType.LABEL_CHAIN){

			if(biggest){
				if(numPositiveLabels < numLabels){
					inst.setValue(attrId, 1);
					return 1;
				}else{
					inst.setValue(attrId, 0);
					return 0;
				}
			}else{
				if((attrIds.length-numPredictedLabels) > (numLabels-numPositiveLabels)){
					inst.setValue(attrId, 0);
					return 0;
				}else{
					inst.setValue(attrId, 1);
					return 1;
				}
			}		
			
		}else if(predictType == PredictType.PERCENTAGE_CHAIN){			
			
			if(percentage < 0.5){
				inst.setValue(attrId, 0);
				return 0;
			}else{
				inst.setValue(attrId, 1);
				return 1;
			}
		}else{
			throw new RDTException("MultilabelChain: Type for prediction is unknown!");
		}
	}
	
	
	//***************************************
	//*** ONLY FOR PREDEFINED CHAIN-ORDER ***
	//***************************************
	
	/**
	 * The oder of the labels, which will be predicted with the chain-method
	 */
	private int[] chainOrder;
	
	/**
	 * Predicts the next class for the LABEL_CHAIN and PERCENTAGE_CHAIN method with the ChainType GIVEN_CHAIN and
	 * some additional information.
	 * 
	 * @param inst the instance for which the next label will be predicted
	 * @param attrIds the attribute-ids of the labels of the collector
	 * @param labelCount the distribution of the labels of the collector	 
	 * @param numPredictedLabels the number of labels which are already predicted with the chain-method 
	 * @return 1 if the label which is predicted is positive and 0 if the label is negative 
	 */
	private int predictNextClassChainOrder(Instance inst, int[] attrIds, double[] labelCount, double numInst, int numPredictedLabels) throws RDTException{
		
		int numPredict =chainOrder[numPredictedLabels];
		double percentage = labelCount[numPredict]/numInst;
		int attrId = attrIds[numPredict];
		
		lastPredictedAttributeID = attrId;
		
		if(percentage < 0.5){
			inst.setValue(attrId, 0);
			return 0;
		}else{
			inst.setValue(attrId, 1);
			return 1;
		}
		
	}
	
}

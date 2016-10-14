package rdt.tree.collector;

import java.util.List;

import evaluation.measure.Measure;
import rdt.essentials.RDTException;
import weka.core.Instance;

/**
 * Collector Interface. The collector is an essential element for a decision tree in this framework.
 * Each collector represents a specific learning task. For example the ClassificationCollector can 
 * predict the value for a nominal attribute and the MultilabelCollector can predict the values of
 * multiple nominal attributes. An array of collectors is contained in each leaf of a tree. In order
 * to make a prediction with a collector the collector has to capture information about the 
 * learning task it fulfills. This is done by adding all the instances which are assigned to a leaf
 * to the collectors. The collectors extract the relevant information (e.g. value of the class) from
 * the instances and update their statistics (e.g. distribution of class values). These statistics 
 * can be used to perform a prediction (e.g. predicting the majority class in the collector).
 * It is also possible to create a collector which collects any other information about the incoming 
 * instances (it does not have to be a learning task). For example we can create a collector which
 * only stores the incoming instances in a list.  
 * 
 * @author MK
 */
public interface Collector {
	
	/**
	 * Adds the given instance to the collector.
	 * 
	 * @param inst the instance which will be added
	 */
	public void addInstance(Instance inst);

	/**
	 * Removes the given instance from the collector.
	 * 
	 * @param inst the instance which will be removed
	 */
	public void removeInstance(Instance inst);
	
	/**
	 * Removes all instances from the collector.
	 */
	public void removeAllInstances();
	
	/**
	 * Returns the type of the collector.
	 * 
	 * @return the type of the collector
	 */
	public CollectorType getType();
	
	/**
	 * Creates a new collector by merging the statistics of the current collector and the
	 * statistics of the collectors of the same type in the given collector-array. Each
	 * collector has the same weight regardless if they are based on one or thousands of
	 * instances (Collectors are normalized according to their number of seen instances
	 * before they will be merged).
	 * 
	 * IMPORTANT: The collector which calls this method and the collectors which are contained 
	 * in the collector-array will not be modified (It is not allowed to modify these collectors
	 * because this will result in manipulating the collectors in a leaf). While merging the 
	 * collectors a new collector-instance have to be created. 
	 * 
	 * @param collectors an array of collectors which will be merged with the current collector
	 * @return a new collector-instance containing the merged content of all collector
	 */
	public Collector merge(Collector[] collectors);
	
	/**
	 * Creates a new collector by adding the statistics of the current collector and the
	 * statistics of the collectors of the same type in the given collector-array together.
	 * Collectors which contain a huge number of seen instances will have a higher weight
	 * than collectors which only have seen a few instances (Collectors are not normalized
	 * before they will be combined).
	 * 
	 * IMPORTANT: The collector which calls this method and the collectors which are contained 
	 * in the collector-array will not be modified (It is not allowed to modify these collectors
	 * because this will result in manipulating the collectors in a leaf). While adding the 
	 * collectors a new collector-instance have to be created. 
	 * 
	 * @param collectors an array of collectors which will be added the new collector
	 * @return a new collector-instance containing the merged content of all collectors of the same type
	 */
	public Collector add(Collector[] collectors);
	
	/**
	 * Returns an array of values which are the prediction for the corresponding attribute-ids.
	 * The value with the index 0 contains the prediction for the attribute-id with the index 0
	 * of the array which can be obtained by calling the method getUsedAttributes().
	 * 
	 * @return an array of values which are the prediction for the corresponding attribute-ids.
	 */
	public double[] getPrediction() throws RDTException;
	
	/**
	 * Checks if the collector is able to make a prediction.
	 * 
	 * @return true, if the collector can make a prediction otherwise false
	 */
	public boolean canMakePrediction();
	
	/**
	 * Checks if the collector is able to process the instance.
	 * 
	 * @param inst, the given instance
	 * @return true, if the collector can handle the given instance
	 */
	public boolean canHandle(Instance inst);

	/**
	 * Returns an array of attribute-ids for which the collector performs predictions. 
	 * 
	 * @return an array of attribute-ids for which the collector performs predictions
	 */
	public int[] getUsedAttributes();
	
	/**
	 * Returns the number of instances which have been seen by the collector.
	 * 
	 * @return the number of instances which have been seen by the collector
	 */
	public double getNumInst();
	
	/**
	 * Returns a list of measures which can be used for the evaluation. You have to
	 * implement this method if you want to use the evaluation provided in this 
	 * framework.
	 * 
	 * @return a list of measures
	 */
	public List<Measure> getMeasures();
}

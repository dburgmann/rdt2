package rdt.model;

import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.Collector;
import rdt.tree.collector.CollectorPreferences;
import weka.core.Instance;

/**
 * Interface for a model. A model is a machine learning algorithm which can perform
 * predictions. The algorithm uses the instances which are provided through the 
 * build(...) method to create an intern model. 
 * 
 * @author MK
 */
public interface Model{
	
	/**
	 * Builds the model with the given instances. 
	 * 
	 * @param insts the instances which will be sued to build the model
	 */
	public void build(RDTInstances insts) throws RDTException;

	
	/**
	 * Makes a prediction for the given instance. The prediction is represented as an
	 * array of collectors. Each collector represents one specific learning-task, which
	 * have been specified in the initialization process of this instance.
	 * 
	 * @param inst the instance for which the prediction will be computed
	 * @return An array of collectors which contain the prediction for each specific
	 * learning task.
	 */
	public Collector[] predict(Instance inst) throws RDTException;

	/**
	 * Returns the information about the learning tasks which are performed by the model.
	 * 
	 * @return the information about the learning tasks which are performed by the model
	 */
	public CollectorPreferences getCollectorPreferences();

}

package evaluation.dataset;

import java.io.IOException;

import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.CollectorPreferences;

/**
 * Class which represents a dataset. Each dataset contains training-instances,
 * test-instances and cv-instances (usually cv-instances are all available
 * instances). Furthermore a dataset contains also the information about the
 * learning task (CollectorPreferences) (e.g. a multilabel-dataset contains
 * a MultilabelCollector in the CollectorPreferences).
 * 
 * @author MK
 */
public abstract class Dataset {
	
	/**
	 * Returns the training-instances of the dataset.
	 * 
	 * @return the training-instances of the dataset
	 */
	public abstract RDTInstances getTrainInstances() throws IOException, RDTException;
	
	/**
	 * Returns the test-instances of the dataset.
	 * 
	 * @return the test-instances of the dataset
	 */
	public abstract RDTInstances getTestInstances() throws IOException, RDTException;
	
	/**
	 * Returns all instances of the dataset.
	 * 
	 * @return all instances of the dataset
	 */
	public abstract RDTInstances getCVInstances() throws IOException, RDTException;
	
	/**
	 * Returns the attribute-ids of the atributes which are restricted (used in collectors).
	 * 
	 * @return the attribute-ids of the atributes which are restricted (used in collectors)
	 */
	public abstract int[] getRestrictedAttributeIds();
	
	/**
	 * Returns the collector-preferences of the dataset.
	 * 
	 * @return the collector-preferences of the dataset
	 */
	public abstract CollectorPreferences getCollectorPreferences();
	
	/**
	 * Returns the type of the dataset.
	 * 
	 * @return the type of the dataset
	 */
	public abstract DatasetType getDatasetType();
	
	/**
	 * Returns the name of the dataset.
	 * 
	 * @return the name of the dataset
	 */
	public abstract String getName();
}

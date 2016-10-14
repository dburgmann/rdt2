package evaluation.experiment;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;

/**
 * Interface which represents an experiment. By implementing the method "getDatasets()" you
 * can specify on which datasets the experiment can be run. In the "runExperiment(...)"
 * you can implement the logic of the experiment. Nearly everything can be implemented in
 * this method.
 * 
 * @author MK
 */
public interface Experiment {

	/**
	 * Returns the dataset-types which can be used for this experiment.
	 * 
	 * @return the dataset-types which can be used for this experiment
	 */
	public DatasetType[] getDatasets();
	
	/**
	 * Starts the experiment with the given dataset.
	 * 
	 * @param dataset the dataset on which the experiment will be run
	 */
	public void runExperiment(Dataset dataset) throws Exception;
}

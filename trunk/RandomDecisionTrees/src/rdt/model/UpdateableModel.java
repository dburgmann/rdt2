package rdt.model;

import rdt.essentials.RDTException;
import weka.core.Instance;

/**
 * Interface for a model which can be updated. This interface can be used for models which perform
 * incremental learning.
 * 
 * @author MK
 */
public interface UpdateableModel extends Model{

	/**
	 * Updates the model with the given instance.
	 * 
	 * @param inst the instance which will be sued to update the model.
	 */
	public void update(Instance inst) throws RDTException;
}

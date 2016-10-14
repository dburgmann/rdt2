package rdt.tree.splitter;

import weka.core.Instance;

/**
 * UpdateableSplitter Interface. For incremental learning it is necessary to update the
 * decision tree with each instance. A splitter can be updated by using this interface.
 * 
 * @author MK
 */
public interface UpdateableSplitter extends Splitter{

	/**
	 * Updates the splitter with the given instance. This method will return true,
	 * if the number of child-nodes to which the splitter can assign instances
	 * had changed through the update.
	 * 
	 * @param inst the instance which will be used to update the splitter
	 * @return true if the number of child-nodes had changed through the update otherwise false
	 */
	public boolean update(Instance inst);
}

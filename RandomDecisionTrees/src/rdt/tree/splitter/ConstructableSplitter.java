package rdt.tree.splitter;

import java.util.List;

import weka.core.Instance;

/**
 * ConstructableSplitter Interface. This interface can be used for splitters which will be used in
 * the BatchTreeBuilder. Normally a splitter has to create a split-value and according to this
 * split-value the instances will be directed to the child-nodes. With this interface it is possible
 * to combine the creation of the split-value and the forwarding of the instances to the child-nodes.
 * In order to do this the method "construct(List<Instance>)" can be used. This method has to return
 * for each child-node a list of instances (the instances which will be forwarded to the specific
 * child-node). It possible to capture some statistics in the method "construct(List<Instance>)"
 * while going through the instances and this information can be used to choose a better 
 * threshold. The only disadvantage is that you have to find a way to separate the instances at the
 * same time. Theoretically if you go through all instances once it has the same complexity as the 
 * normal method. 
 * 
 * @author MK
 */
public interface ConstructableSplitter extends Splitter{

	/**
	 * Constructs the splitter by using the given instances. During the process of the
	 * construction the instances are assigned to the specific child-nodes and will be
	 * returned as some lists of instances. Each list contains the instances which belong
	 * to a specific child-node.
	 * 
	 * @param ions the instances which can be used to construct the splitter
	 * @return some lists of instances, each list contains the instances for a specific child-node
	 */
	public List<List<Instance>> construct(List<Instance> ions);
}

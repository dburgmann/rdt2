package rdt.tree.splitter;

import rdt.essentials.RDTException;
import weka.core.Instance;

/**
 * Splitter Interface. The splitter is an essential element of a decision tree and is contained in
 * each inner-node. With a splitter it is decided to which child-node the incoming instance will be
 * forwarded.
 * 
 * @author MK
 */
public interface Splitter{
	
	
	/**
	 * Determines the number of the child-node to which the instance should be forwarded.
	 * 
	 * @param inst the instance which will be forwarded
	 * @return the number of the child-node to which the instance will be forwarded
	 */
	public int determineChild(Instance inst) throws RDTException;
	
	
	/**
	 * Checks if this splitter can process the given instance. (e.g. If the the splitter
	 * checks on a missing value of the given instance this method return false) 
	 * 
	 * @param inst the instance which will be checked, if it can be processed by the splitter
	 * @return true if a child-node can be determined for the instance otherwise false
	 */
	public boolean canHandle(Instance inst);
	
	
	/**
	 * Returns the number of child-nodes to which the splitter can assign instances.
	 * 
	 * @return the number of child-nodes
	 */
	public int getNumberOfChilds();
	
	
	/**
	 * Returns all the attributes which are used to determine the child-node for a
	 * given instance.
	 * 
	 * @return the attributes which are used by the splitter
	 */
	public int[] getUsedAttributeIds() throws RDTException;
	
	/**
	 * Returns true if the splitter can be updated otherwise false.
	 * 
	 * @return true if the splitter can be updated otherwise false
	 */
	public boolean isUpdateable();
	
	/**
	 * Returns true if the splitter can be updated otherwise false.
	 * 
	 * @return true if the splitter can be updated otherwise false
	 */
	public boolean isConstructable();
	
	/**
	 * Returns the type of the splitter.
	 * 
	 * @return the type of the splitter
	 */
	public SplitterType getType();
}


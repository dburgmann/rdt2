package rdt.tree.splitter;

/**
 * ActiveSplitter Interface. An which implements this interface can be activated or 
 * deactivated. If the splitter is activated is has the usual functionality which
 * has been implement. If the splitter is deactivated it cannot handle any instance
 * which means that every instance will be forwarded to all child-nodes of the 
 * inner-node which contains this splitter.
 * Make sure that you override the canHandle(Instance)-method with the described
 * logic (usually you only have to add "... && active")
 * 
 * @author MK
 */
public interface ActiveSplitter extends Splitter{

	/**
	 * Sets the active-state of the splitter. If the active-state is false the splitter
	 * will be deactivated and if the active-state is true the splitter will be activated.
	 * 
	 * @param active the new active-state of the splitter
	 */
	public void setActive(boolean active);
	
	/**
	 * Returns the current active-state of the splitter.
	 * 
	 * @return the current active-state of the splitter
	 */
	public boolean isActive();
	
}

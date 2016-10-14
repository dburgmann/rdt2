package rdt.tree.collector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rdt.essentials.RDTException;
import rdt.tree.collector.ClassificationCollector;
import rdt.tree.collector.MultilabelCollector;
import rdt.tree.collector.PMultilabelCollector;

/**
 * Class to specify the learning tasks. In order to perform multiple learning tasks we need
 * a way to specify which learning tasks should be performed. This class contains all the 
 * information about the learning tasks which will be performed by the model. These preferences
 * contain a variable amount of  collectors, each of them represents a specific learning task. 
 * Furthermore we are able to create new instances of these collectors in the getNewCollectors()
 * method. I have decided to create new collectors like it is done in the getNewCollectors() method
 * because we have to call this method many times while constructing a tree/ensemble and the
 * cloning of the collectors costs a lot more time than producing new instances of the 
 * collectors. The drawback of this method is that we have to append the getNewCollectors()
 * method if we want to add a new collector to the framework.
 * 
 * @author MK
 */
public class CollectorPreferences {
	
	/**
	 * The list of collectors (different learning tasks)
	 */
	private List<Collector> collectors;
	
	/**
	 * Creates a new CollectorPreferences without any specified learning tasks.
	 */
	public CollectorPreferences(){
		this.collectors = new LinkedList<Collector>();
	}
	
	/**
	 * Adds the given collector (learning task) to the preferences.
	 * 
	 * @param c the collector which will be added
	 */
	public void addCollector(Collector c){
		collectors.add(c);
	}
	
	/**
	 * Removes all collectors from the preferences.
	 */
	public void removeAllCollectors(){
		collectors = new LinkedList<Collector>();
	}
	
	/**
	 * Returns an array of newly created collectors of the collectors which have been added
	 * to the collector-preferences before. For example this method is used to create a new
	 * array of collectors for a leaf.
	 * 
	 * @return an array of newly created collectors
	 */
	public Collector[] getNewCollectors() throws RDTException{
		Collector[] newCollectors = new Collector[collectors.size()];
		
		int i=0;
		for(Collector c : collectors){
			if(c.getType() == CollectorType.CLASSIFICATION){
			
				newCollectors[i] = new ClassificationCollector(c.getUsedAttributes()[0]);
				
			}else if(c.getType() == CollectorType.MULTILABEL){
				
				newCollectors[i] = new MultilabelCollector(c.getUsedAttributes());
				
			}else if(c.getType() == CollectorType.P_MULTILABEL){
				
				newCollectors[i] = new PMultilabelCollector(c.getUsedAttributes());
				
			}else if(c.getType() == CollectorType.LP_MULTILABEL){
				
				LPMultilabelCollector lpCol = (LPMultilabelCollector) c;
				newCollectors[i] = new LPMultilabelCollector(c.getUsedAttributes(), lpCol.getConverter());
				
			}else if(c.getType() == CollectorType.LP_MULTILABEL_OLD){
				
				newCollectors[i] = new LPMultilabelCollector_OLD(c.getUsedAttributes());
				
			}else{
				throw new RDTException("Unknown CollectorType in CollectorPreferences.getNewCollectors()");
			}
			i++;
		}
		return newCollectors;
	}
	
	/**
	 * Returns the number of collectors (number of different learning tasks)
	 * 
	 * @return the number of collectors (number of different learning tasks)
	 */
	public int getNumCollectors(){
		return collectors.size();
	}
	
	/**
	 * Returns the collectors which have been added.
	 * 
	 * @return the collectors which have been added
	 */
	public List<Collector> getCollectors(){
		return collectors;
	}
	
	/**
	 * Returns an array with all attribute-ids which are used in the collectors (restricted attributes).
	 * 
	 * @return an array with all attribute-ids which are used in the collectors (restricted attributes)
	 */
	public int[] getAllResrictedAttributeIds(){
		Set<Integer> ids = new HashSet<Integer>();
		
		for(Collector c : collectors){
			int[] temp = c.getUsedAttributes();
			for(Integer i : temp){
				ids.add(i);
			}
		}
		
		int[] ret = new int[ids.size()];
		
		int j=0;
		for(Integer i : ids){
			ret[j] = i;
			j++;
		}
		
		return ret;
	}
	
	/**
	 * Returns an two-dimensional array with the used attribute-ids of each collectors (restricted attributes).
	 * 
	 * @return an two-dimensional array with the used attribute-ids of each collectors (restricted attributes)
	 */
	public int[][] getResrictedAttributeIds(){
		
		int[][] array = new int[collectors.size()][];
		
		
		for(int i=0; i<array.length; i++){
			array[i] = collectors.get(i).getUsedAttributes();
		}
		
		return array;
	}
	
	/**
	 * Checks if this and the given CollectorPreferences are the same (equal).
	 * 
	 * @param other the other CollectorPreferences which will be checked
	 * @return true if the collector-preferences are equal otherwise false
	 */
	public boolean equals(CollectorPreferences other){
		
		List<Collector> otherCollectors = other.getCollectors();
		
		if(collectors.size() != otherCollectors.size()){
			return false;
		}
		
		Iterator<Collector> iter = otherCollectors.iterator();
		Collector otherC = iter.next();
		for(Collector c : collectors){
			if(c.getType() != otherC.getType()){
				return false;
			}
			if(c.getUsedAttributes().length != otherC.getUsedAttributes().length){
				return false;
			}
			for(int i=0; i<c.getUsedAttributes().length; i++){
				if(c.getUsedAttributes()[i] != otherC.getUsedAttributes()[i]){
					return false;
				}
			}
			if(iter.hasNext()){
				otherC = iter.next();
			}
		}
		return true;
	}
}

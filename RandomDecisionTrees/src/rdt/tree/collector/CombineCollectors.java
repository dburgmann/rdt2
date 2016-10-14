package rdt.tree.collector;

import rdt.essentials.RDTException;
import rdt.tree.Tree.PredictionCombination;

/**
 * Class to combine collectors easily. This framework supports the realization of multiple
 * learning tasks in the same tree. As a result of this the leafs contain multiple collectors,
 * each representing one particular learning task. So if we want to make a prediction we will
 * return the array of collectors of the leaf to which the instance have been forwarded. 
 * Problems occur if we have to combine two or more arrays of collectors because we have to 
 * combine the collectors for each specific learning task. To make this easy I implemented
 * this class, where we only have to submit the arrays of the collectors (e.g. the collector
 * arrays of two different leafs). After we submitted all arrays of collectors we can combine
 * the collector array by calling the method combine(...). Here we need to specify if want to
 * add or merge the collectors (see the description of the methods add(...) and merge(...) of 
 * the collector interface). 
 * 
 * IMPORTANT: All collector arrays should contain the same amount of collectors and each 
 * index of the collector arrays should have the same collector type. Otherwise the combination
 * will not work properly.
 * 
 * @author MK
 */
public class CombineCollectors {

	/**
	 * Temporary variable to store the current position in the matrix. We need this
	 * variable to know where we have to add the next array of collectors into
	 * the matrix.
	 */
	private int col;
	
	/**
	 * The first array of collectors which have been added. This array will be used to
	 * call the add(...) or merge(...) method to combine the collectors.
	 */
	private Collector[] firstCollectors;
	
	/**
	 * Contains all other arrays of collectors which have been added after the first one.
	 * A matrix of collectors which will be merged with the array firstCollectors.
	 * Each row represents an array of collectors and each column represents one particular
	 * type of collectors.
	 */
	private Collector[][] matrix;
	
	/**
	 * Creates a new CombineCollectors. It is necessary to specify how many collector arrays
	 * we will be combined (numArrays) and how many collectors are contained in each collector 
	 * array (numCollectors). 
	 * 
	 * IMPORTANT: All collector arrays should contain the same amount of collectors and each 
	 * index of the collector arrays should have the same collector type. Otherwise the combination
	 * will not work.
	 * 
	 * @param numCollectors the number of collectors in the collector array
	 * @param numArrays the total number of collector arrays which will be combined
	 */
	public CombineCollectors(int numCollectors, int numArrays) {
		this.matrix = new Collector[numCollectors][numArrays-1];
		this.col = 0;
	}
	
	/**
	 * Adds the given array of collectors.
	 * 
	 * @param collectors the array of collectors which will be added
	 */
	public void addCollectors(Collector[] collectors){
		if(firstCollectors == null){
			firstCollectors = collectors;
		}else{
			for(int row=0; row<collectors.length; row++){
				matrix[row][col] = collectors[row];
			}
			col++;
		}
	}
	
	/**
	 * Adds or merges the collected arrays of collectors together and returns an array of
	 * collectors which contain the combination of the the collected collectors. During 
	 * this procedure the collected collectors will not be modified. The returned 
	 * array only contains newly created collectors.
	 * 
	 * @return the combined collectors
	 */
	public Collector[] combine(PredictionCombination pc) throws RDTException{
		Collector[] mergedCollectors = new Collector[firstCollectors.length];
		
		for(int i=0; i<matrix.length; i++){
			if(pc == PredictionCombination.MERGE){
				mergedCollectors[i] = firstCollectors[i].merge(matrix[i]);
			}else if(pc == PredictionCombination.ADD){
				mergedCollectors[i] = firstCollectors[i].add(matrix[i]);
			}else{
				throw new RDTException("Unknwn PredictionCombination-type!");
			}
		}
		
		return mergedCollectors;
	}
}

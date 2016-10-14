package rdt.util;

import java.util.HashMap;

import rdt.essentials.RDTException;

/**
 * Class to perform the Label-Powerset method effectively. The idea is to use this class to
 * store the double-arrays for a specific key in a HashMap. Under the assumption that the 
 * double-array only contains the values 0 and 1 it is possible to create a hash-function
 * with less collisions. The specific key can be used in the model as a single class. With this
 * approach we do not have to compute the hash-value for each double-array over and over again.
 * We can simply use the key to identify a specific class combination and if we want to get
 * the specific class-combination for a key, we can get the double array by calling the 
 * method "getValues(int)".
 * 
 * Furthermore one LPConverter can be used for all LPCollectors in a specific ensemble.
 * 
 * @author MK
 */
public class LPConverter {

	/**
	 * This map contains the double-array for the corresponding hash-value. 
	 */
	private HashMap<Integer, double[]> map;
	
	/**
	 * Creates a new LPConverter with an empty map.
	 */
	public LPConverter(){
		this.map = new HashMap<Integer, double[]>();
	}
	
	/**
	 * If the double-array does not exist in the map it will be added. This method will
	 * return the hash-value of the double-array.
	 * 
	 * @param values the double array which will be added and for which the hash-value will be computed
	 * @return the hash-value of the double array
	 */
	public int put(double[] values){
		
		int hashValue = computeHash(values);
		
		if(!map.containsKey(hashValue)){
			map.put(hashValue, values);
		}
		
		return hashValue;
	}
	
	/**
	 * Returns the double array for a specific hash-value.
	 * 
	 * @param hashValue the specific hash-value
	 * @return the corresponding double array
	 */
	public double[] getValues(Integer hashValue) throws RDTException{
		
		if(map.containsKey(hashValue)){
			return map.get(hashValue);
		}
		
		throw new RDTException("Value not found!");
	}
	
	/**
	 * Computes a hash value for the given array. This method works differently than the 
	 * method Arrays.hashCode(double[]) because we assume that the array contains only
	 * the values 0 and 1. With this implementation we can achieve a more collision 
	 * resistant hash function for these specific arrays.
	 * 
	 * @param array the array for which the hash-value will be computed
	 * @return the hash value
	 */
	private int computeHash(double[] array){
		
		int hash = 0;
		
		for(int i=0; i<array.length; i++){				
			hash = 37 * hash + ((int) array[i]);
		}
		
		return hash;
	}
}

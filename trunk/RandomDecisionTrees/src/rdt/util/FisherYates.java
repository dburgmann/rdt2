package rdt.util;

import java.util.Random;

/**
 * Class with methods for shuffling the elements of an array by using the Fisher-Yates algorithm. 
 * 
 * @author MK
 */
public class FisherYates {

	
	/**
	 * Shuffles the elements in the given array randomly by using the Fisher-Yates algorithm. 
	 * 
	 * @param random the generator to produce random numbers
	 * @param array the array which elements will be shuffled
	 */
	public static void shuffleArray(Random random, Object[] array){
		for (int i = array.length - 1; i > 0; i--){
	    	int index = random.nextInt(i+1);
	    	Object a = array[index];
	    	array[index] = array[i];
	    	array[i] = a;
		}
	}
	
	/**
	 * Shuffles the elements in the given array randomly by using the Fisher-Yates algorithm. 
	 * 
	 * @param random the generator to produce random numbers
	 * @param array the array which elements will be shuffled
	 */
	public static void shuffleArray(Random random, int[] array){
		for (int i = array.length - 1; i > 0; i--){
	    	int index = random.nextInt(i+1);
	    	int a = array[index];
	    	array[index] = array[i];
	    	array[i] = a;
		}
	}
}

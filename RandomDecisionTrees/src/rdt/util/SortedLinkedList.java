package rdt.util;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class represents a LinkedList<Double> which is always sorted in a ascending order.
 * New elements are added at the correct position in the list which satisfy the ascending order.
 *
 * @author MK
 */
public class SortedLinkedList extends LinkedList<Double>{
	private static final long serialVersionUID = 3315865309986036736L;

	@Override
	public boolean add(Double newValue){
		
		ListIterator<Double> iter = listIterator();
		
		while(iter.hasNext()){
			double value = iter.next();
			if(value > newValue){
				iter.previous();
				iter.add(newValue);
				return true;
			}
		}
		
		iter.add(newValue);
		return true;
	}
	
}

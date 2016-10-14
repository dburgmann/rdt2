package rdt.util;

/**
 * Class to represent a pair of values.
 * 
 * @author MK
 */
public class Pair<F, S> {
    
	/**
	 * First member of the pair
	 */
	private F first;
	
	/**
	 * Second member of the pair
	 */
    private S second;

    
    /**
     * Creates a new pair with the given values.
     * 
     * @param first value for the first member of the pair
     * @param second value for the second member of the pair
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
}
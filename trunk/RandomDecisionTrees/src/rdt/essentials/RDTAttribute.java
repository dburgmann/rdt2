package rdt.essentials;

import weka.core.Attribute;

/**
 * Class for RDTAttribute. This class represents an attribute in the framework and it is build on
 * the attribute-class of the weka library. The only difference is that a RDTAttribute can be 
 * restricted (the attribute is used in one of the collectors e.g the class-attribute for a 
 * classification task). Restricted attributes should not be tested in the splitters of a decision
 * tree because they contain the the information which we want to predict (with the exception of
 * special cases e.g MultilabelChainTree)
 * 
 * @author MK
 */
public class RDTAttribute {
	
	/**
	 * Stores an instance of the attribute of the weak library.
	 */
	private Attribute attr;
	
	/**
	 * Indicates whether the attribute is restricted or not.
	 */
	private boolean isRestricted;
	
	/**
	 * Creates a new RDTAttribute with the given attribute of the WEKA library. An attribute
	 * is restricted if it is used in one of the collectors. As a result of this the attribute
	 * is used for a learning task and should not be used to create a decision tree (with the 
	 * exception of special cases.
	 * 
	 * @param attr the attribute of the WEKA library
	 * @param isRestricted true if the attribute is used in one of the collectors
	 */
	public RDTAttribute(Attribute attr, boolean isRestricted){
		this.attr = attr;
		this.isRestricted = isRestricted;
	}
	
	/**
	 * Returns the attribute-id.
	 * 
	 * @return the attribute-id
	 */
	public int getAttributeId(){
		return attr.index();
	}
	
	/**
	 * Returns true if the attribute is nominal otherwise false.
	 * 
	 * @return true if the attribute is nominal otherwise false
	 */
	public boolean isNominal(){
		return attr.isNominal();
	}
	
	/**
	 * Returns true if the attribute is numeric otherwise false.
	 * 
	 * @return true if the attribute is numeric otherwise false
	 */
	public boolean isNumeric(){
		return attr.isNumeric();
	}
	
	/**
	 * Returns true if the attribute is used in a collector otherwise false.
	 * 
	 * @return true if the attribute is used in a collector otherwise false
	 */
	public boolean isRestricted(){
		return isRestricted;
	}
	
	/**
	 * Returns the number of attribute values. Returns 0 for attributes that
	 * are not either nominal, string, or relation-valued.
	 * 
	 * @return the number of values of the attribute
	 */
	public int getNumValues(){
		return attr.numValues();
	}
	
	/**
	 * Returns the attribute-instance which is used by WEKA.
	 * 
	 * @return the attribute-instance which is used by WEKA
	 */
	public Attribute getWekaAttribute(){
		return attr;
	}

	@Override
	public String toString(){
		return attr.toString();
	}
	
}

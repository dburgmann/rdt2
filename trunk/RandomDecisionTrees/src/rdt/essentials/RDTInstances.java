package rdt.essentials;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Class for RDTInstances. This class represents instances in the framework and it is build on
 * the instances-class of the weka library. The only difference is that a RDTInstances can contain 
 * restricted attributes (attributes which are used in the collectors (e.g. the class-attribute for
 * a classification task)). 
 * 
 * @author MK
 */
public class RDTInstances {

	/**
	 * The instances (weka library) which have been read from the file.
	 */
	private Instances insts;
	
	/**
	 * Contains all the available attributes of the dataset which have been read.
	 */
	private RDTAttribute[] attributes;
	
	/**
	 * Contains all the attributes of the dataset which are not restricted (not used in the collectors).
	 */
	private RDTAttribute[] freeAttributes;
	
	/**
	 * Contains all the attributes of the dataset which are restricted (used in the collectors).
	 */
	private RDTAttribute[] restrictedAttributes;
	
	/**
	 * Contains the path to the file which contains the dataset which have been read.
	 */
	private String path;
	
	
	/**
	 * Creates a new RDTInstances with the instances which will be read from the file which is
	 * given by the path and the restricted attribute-ids (attribute-ids of the attributes which
	 * are used in the collectors).
	 * 
	 * @param path the location of the file from which the instances will be read
	 * @param restrictedAttributesIndex the attribute-ids of the attributes which are restricted 
	 */
	public RDTInstances(String path, int[] restrictedAttributesIndex) throws IOException, RDTException{
		Reader reader = new FileReader(path);
		insts = new Instances(reader);
		
		this.path = path;
		this.init(insts, restrictedAttributesIndex);
	}
	
	/**
	 * Creates a new RDTInstances with the given instances of the weka library and the restricted 
	 * attribute-ids (attribute-ids of the attributes which are used in the collectors). By using
	 * this constructor the path will be set to null. 
	 * 
	 * @param insts the instances of the weka library
	 * @param restrictedAttributesIndex the attribute-ids of the attributes which are restricted 
	 */
	public RDTInstances(Instances insts, int[] restrictedAttributesIndex) throws IOException, RDTException{
		this.insts = insts;
		this.path = null;//"path_not_set";
		
		this.init(insts, restrictedAttributesIndex);
	}
	
	/**
	 * Creates a new RDTInstances with the given instances of the weka library and the restricted 
	 * attribute-ids (attribute-ids of the attributes which are used in the collectors). By using
	 * this constructor the path will be set to the given path.
	 * 
	 * @param insts the instances of the weka library
	 * @param restrictedAttributesIndex the attribute-ids of the attributes which are restricted 
	 * @param path the path to the file of which the instances have been read
	 */
	public RDTInstances(Instances insts, int[] restrictedAttributesIndex, String path) throws IOException, RDTException{
		this.insts = insts;
		this.path = path;
		
		this.init(insts, restrictedAttributesIndex);
	}
	
	/**
	 * 
	 * Creates a new RDTInstances with the given instances of the weka library, the given 
	 * information about the attributes. By using this constructor the path will be set to
	 * the given path.
	 * 
	 * @param insts the instances of the weka library
	 * @param attributes all attributes of the dataset
	 * @param restricted the attribute-ids which are restricted (attributes are used in the collectors)
	 * @param freeAttributes the attribute-ids which are not restricted (attributes are not used in the collectors)
	 * @param path the path to the file of which the instances have been read
	 */
	public RDTInstances(Instances insts, RDTAttribute[] attributes, RDTAttribute[] restrictedAttributes, RDTAttribute[] freeAttributes, String path) throws IOException, RDTException{
		this.insts = insts;
		this.attributes = attributes;
		this.restrictedAttributes = restrictedAttributes;
		this.freeAttributes = freeAttributes;
		this.path = path;
	}
	
	/**
	 * Initializes this class with given instances of the weka library and an array of the attribute-ids
	 * which are restricted (used in the collectors).
	 * 
	 * @param insts the instances from the weka library
	 * @param restrictedAttributesIndex the array of attribute-ids which are restricted
	 */
	private void init(Instances insts, int[] restrictedAttributesIndex) throws RDTException{
		for(int i=0; i<restrictedAttributesIndex.length; i++){
			if(restrictedAttributesIndex[i] >= insts.numAttributes()){
				throw new RDTException("Given attribute-index " + restrictedAttributesIndex[i] + " out of bounds!");
			}
		}
		freeAttributes = new RDTAttribute[insts.numAttributes()- restrictedAttributesIndex.length];
		restrictedAttributes = new RDTAttribute[restrictedAttributesIndex.length];
		
		
		Arrays.sort(restrictedAttributesIndex);	
	
		this.attributes = new RDTAttribute[insts.numAttributes()];
		
		int l = 0;
		for(int i=0; i<insts.numAttributes(); i++){
			if(l<restrictedAttributesIndex.length && restrictedAttributesIndex[l] == i){
				attributes[i] = new RDTAttribute(insts.attribute(i), true);
				restrictedAttributes[l] = attributes[i];
				l++;
			}else{
				attributes[i] = new RDTAttribute(insts.attribute(i), false);
				freeAttributes[i-l] = attributes[i];
			}
		}
	}
	
	
	/**
	 * Checks if the attribute with the given attribute-id is nominal.
	 * 
	 * @param attributeId the attribute-id of the attribute which will be checked
	 * @return true if the attribute is nominal otherwise false
	 */
	public boolean isNominal(int attributeId){
		return insts.attribute(attributeId).isNominal();
	}
	
	/**
	 * Checks if the attribute with the given attribute-id is numeric.
	 * 
	 * @param attributeId the attribute-id of the attribute which will be checked
	 * @return true if the attribute is numeric otherwise false
	 */
	public boolean isNumeric(int attributeId){
		return insts.attribute(attributeId).isNumeric();
	}
	
	/**
	 * Returns the instances-instance which is used by WEKA.
	 * 
	 * @return the instances-instance which is used by WEKA
	 */
	public Instances getDataSet(){
		return insts;
	}

	/**
	 * Returns the number of instances.
	 * 
	 * @return the number of instances
	 */
	public int getNumInstances() {
		return insts.size();
	}

	/**
	 * Returns all the attributes of the instances.
	 * 
	 * @return all attributes
	 */
	public RDTAttribute[] getAttributes(){
		return attributes;
	}
	
	/**
	 * Returns the attributes which are restricted (used in the collectors).
	 * 
	 * @return the attributes which are restricted (used in the collectors)
	 */
	public RDTAttribute[] getRestrictedAttributes(){
		return restrictedAttributes;
	}
	
	
	/**
	 * Returns the attributes which are not restricted (not used in the collectors).
	 * 
	 * @return the attributes which are not restricted (not used in the collectors)
	 */
	public RDTAttribute[] getFreeAttributes(){
		return freeAttributes;
	}
	
	/**
	 * Checks if the instances and the given instances use the same attributes.
	 * 
	 * @param insts the given instances
	 * @return true if both instances use the same attributes otherwise false
	 */
	public boolean useSameAttributes(RDTInstances insts){
		
		RDTAttribute[] temp = insts.getAttributes();
		
		for(int i=0; i<attributes.length; i++){
			if(attributes[i].isRestricted() != temp[i].isRestricted() ||
					attributes[i].isNominal() != temp[i].isNominal() ||
					attributes[i].isNumeric() != temp[i].isNumeric() ||
					attributes[i].getAttributeId() != temp[i].getAttributeId() ||
					attributes[i].getNumValues() != temp[i].getNumValues()){
				return false;
			}
		}
		return true;		
	}
	
	/**
	 * Combines the instances with the given instances. All the given instances will be added
	 * to this RDTInstances-instance. The attributes of both RDTInstances have to be the
	 * same otherwise an exception will be thrown.
	 * 
	 * @param temp, the given instances
	 */
	public void addInstances(RDTInstances temp) throws RDTException{
		if(useSameAttributes(temp)){
			for(Instance inst : temp.getDataSet()){
				insts.add(inst);
			}
		}else{
			throw new RDTException("Can not add instances with different attributes!");
		}
	}
	
	/**
	 * Returns the path of the arff-file.
	 * 
	 * @return the path of the arff-file
	 */
	public String getPath(){
		return this.path;
	}
	
}

package rdt.tree.splitter;

/**
 * Enumeration for all different kinds of splitters. The splitter-type is used
 * to easily identify the type of the splitter by only using the getType()-method
 * of the splitter. With this we can avoid using instanceof during the runtime.
 * 
 * @author MK
 */
public enum SplitterType {
	
	NUMERIC,
	QUANTIL,
	INSTANCE_BASED_NUMERIC,
	
	FULLNOMINAL,
	FULLNOMINAL_ACTIVE,
	FULLNOMINAL_UPDATEABLE,
	
	SPARSE,
	LABEL,
	
};
package rdt.tree.collector;

/**
 * Enumeration for all different kinds of collectors. The collector-type is used
 * to easily identify the type of the collector by only using the getType()-method
 * of the collector. With this we can avoid using instanceof during the runtime.
 * 
 * @author MK
 */
public enum CollectorType {
	DUMMY,
	CLASSIFICATION,
	CLASSIFICATION_FADING,
	REGRESSION,
	MULTILABEL,
	P_MULTILABEL,
	LP_MULTILABEL,
	SPECIAL_MULTILABEL,
	SPECIAL2_MULTILABEL,
	LP_MULTILABEL_OLD
	}

package rdt.model;

/**
 * Enumeration for all different kinds of models. The model-type is used
 * to easily identify the type of the model by only using the getType()-method
 * of the model. With this we can avoid using instanceof during the runtime.
 * 
 * @author MK
 */
public enum ModelType {

	BATCH_ENSEMBLE,
	QUANTIL_ENSEMBLE,
	MULTILABEL_CHAIN_ENSEMBLE,
	SPARSE_MULTILABEL_CHAIN_ENSEMBLE,
	
}

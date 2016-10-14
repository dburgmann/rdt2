package evaluation.dataset;

/**
 * Enumeration for all different kinds of datasets. The datset-type is used
 * to easily identify the type of the dataset by only using the getType()-method
 * of the model.
 * 
 * @author MK
 */
public enum DatasetType {
	
	
	//Numeric-Classification
	CLASSIFICATION_ABALONE,
	CLASSIFICATION_BREAST_W,
	CLASSIFICATION_HEART_STATLOG,
	CLASSIFICATION_IONOSPHERE,
	CLASSIFICATION_OPTDIGITS,
	CLASSIFICATION_PAGE_BLOCKS,
	CLASSIFICATION_SEGMENT,
	CLASSIFICATION_SONAR,
	CLASSIFICATION_SPECTROMETER,
	CLASSIFICATION_VEHICLE,
	CLASSIFICATION_VOWEL,
	CLASSIFICATION_WAVEFORM,
	CLASSIFICATION_YEAST,
	CLASSIFICATION_GLASS,
	CLASSIFICATION_DIABETES,
	CLASSIFICATION_CREDITG,
	CLASSIFICATION_EYE_STATE,
	CLASSIFICATION_LETTER_RECOGNITION,
	
	//Nominal-Classification
	CLASSIFICATION_SOLAR_M,
	CLASSIFICATION_SOLAR_C,
	CLASSIFICATION_ANNEAL,
	CLASSIFICATION_CREDIT_A,
	CLASSIFICATION_HEART_C,
	CLASSIFICATION_LABOR,
	CLASSIFICATION_LYMPH,
	CLASSIFICATION_SOYBEAN,
	CLASSIFICATION_SPLICE,
	CLASSIFICATION_CAR,
	CLASSIFICATION_MUSHROOM,
	
	//Mixed-Classification
	CLASSIFICATION_THYROID,
	CLASSIFICATION_AIRLINE,
	CLASSIFICATION_ELECTRICITY,
	
	//Numeric-Multilabel
	MULTILABEL_CAL500,
	MULTILABEL_EMOTIONS,
	MULTILABEL_MEDIAMILL,
	MULTILABEL_SCENE,
	MULTILABEL_YEAST,
	
	//Nominal-Multilabel
	MULTILABEL_BIBTEX,
	MULTILABEL_ENRON,
	MULTILABEL_DELICIOUS,
	MULTILABEL_MEDICAL,
	MULTILABEL_GENBASE,
	MULTILABEL_COREL5K,
	MULTILABEL_COREL16K,
	MULTILABEL_TMC2007,
	
	//Mixed Multilabel
	MULTILABEL_BIRDS,
	MULTILABEL_FLAGS,

	//PCA-Multilabel-Datasets
	MULTILABEL_ENRON_PCA,
	MULTILABEL_MEDICAL_PCA,
	//MULTILABEL_TMC2007_PCA,
	MULTILABEL_COREL5K_PCA,
	MULTILABEL_BIBTEX_PCA,
	//MULTILABEL_DELICIOUS_PCA,
	
}

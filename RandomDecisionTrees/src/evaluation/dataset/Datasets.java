package evaluation.dataset;

import java.util.HashMap;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import evaluation.dataset.classification.mixed.Classification_Abalone;
import evaluation.dataset.classification.mixed.Classification_Airline;
import evaluation.dataset.classification.mixed.Classification_Anneal;
import evaluation.dataset.classification.mixed.Classification_CreditG;
import evaluation.dataset.classification.mixed.Classification_Credit_A;
import evaluation.dataset.classification.mixed.Classification_Electricity;
import evaluation.dataset.classification.mixed.Classification_Heart_C;
import evaluation.dataset.classification.mixed.Classification_Labor;
import evaluation.dataset.classification.mixed.Classification_Spectrometer2;
import evaluation.dataset.classification.mixed.Classification_Thyroid;
import evaluation.dataset.classification.mixed.Classification_Vowel;
import evaluation.dataset.classification.nominal.Classification_Car;
import evaluation.dataset.classification.nominal.Classification_Lymph;
import evaluation.dataset.classification.nominal.Classification_Mushroom;
import evaluation.dataset.classification.nominal.Classification_SolarC;
import evaluation.dataset.classification.nominal.Classification_SolarM;
import evaluation.dataset.classification.nominal.Classification_Soybean;
import evaluation.dataset.classification.nominal.Classification_Splice;
import evaluation.dataset.classification.numeric.Classification_Breast_W;
import evaluation.dataset.classification.numeric.Classification_Diabetes;
import evaluation.dataset.classification.numeric.Classification_Eye_State;
import evaluation.dataset.classification.numeric.Classification_Glass;
import evaluation.dataset.classification.numeric.Classification_Heart_Statlog;
import evaluation.dataset.classification.numeric.Classification_Ionosphere;
import evaluation.dataset.classification.numeric.Classification_Letter_Recognition;
import evaluation.dataset.classification.numeric.Classification_Optdigits;
import evaluation.dataset.classification.numeric.Classification_PageBlocks;
import evaluation.dataset.classification.numeric.Classification_Segment;
import evaluation.dataset.classification.numeric.Classification_Sonar;
import evaluation.dataset.classification.numeric.Classification_Vehicle;
import evaluation.dataset.classification.numeric.Classification_Waveform;
import evaluation.dataset.classification.numeric.Classification_Yeast;
import evaluation.dataset.multilabel.mixed.Multilabel_Birds;
import evaluation.dataset.multilabel.mixed.Multilabel_Flags;
import evaluation.dataset.multilabel.nominal.Multilabel_Corel5k;
import evaluation.dataset.multilabel.nominal.Multilabel_TMC2007;
import evaluation.dataset.multilabel.nominal.Multilabel_Bibtex;
import evaluation.dataset.multilabel.nominal.Multilabel_Corel16k;
import evaluation.dataset.multilabel.nominal.Multilabel_Delicious;
import evaluation.dataset.multilabel.nominal.Multilabel_Enron;
import evaluation.dataset.multilabel.nominal.Multilabel_Genbase;
import evaluation.dataset.multilabel.nominal.Multilabel_Medical;
import evaluation.dataset.multilabel.numeric.Multilabel_CAL500;
import evaluation.dataset.multilabel.numeric.Multilabel_Emotions;
import evaluation.dataset.multilabel.numeric.Multilabel_Mediamill;
import evaluation.dataset.multilabel.numeric.Multilabel_Scene;
import evaluation.dataset.multilabel.numeric.Multilabel_Yeast;
import evaluation.dataset.multilabel.pca.Multilabel_Corel5k_PCA;
import evaluation.dataset.multilabel.pca.Multilabel_Bibtex_PCA;
import evaluation.dataset.multilabel.pca.Multilabel_Enron_PCA;
import evaluation.dataset.multilabel.pca.Multilabel_Medical_PCA;
import rdt.essentials.RDTException;

/**
 * Class to retrieve datasets by their dataset-type. You only have to call the
 * method "getDataset(DatasetType)" to get a specific dataset. Make sure that you 
 * add all the new datasets you wnat to use in the method "initDataset()".
 * 
 * @author MK
 */
public class Datasets {

	/**
	 * This map contains all the available datasets.
	 */
	private static HashMap<DatasetType, Dataset> datasets;
	
	
	/**
	 * Returns the dataset-object for a specific dataset-type.
	 * 
	 * @param type the specific dataset-type
	 * @return the dataset-object
	 */
	public static Dataset getDataset(DatasetType type) throws RDTException{
		if(datasets == null){
			Datasets.initDataset();
		}
		
		if(datasets.containsKey(type)){
			return datasets.get(type);
		}
		throw new RDTException("Dataset not found!");
	}
	
	/**
	 * Returns all the available datasets.
	 * 
	 * @return all the available datasets
	 */
	public static HashMap<DatasetType, Dataset> getAllDatasets(){
		return datasets;
	}
	
	/**
	 * Initializes the map of datasets. 
	 */
	private static void initDataset() throws RDTException{
		datasets = new HashMap<DatasetType, Dataset>();
		datasets.put(DatasetType.CLASSIFICATION_CAR, new Classification_Car());
		datasets.put(DatasetType.CLASSIFICATION_CREDITG, new Classification_CreditG());
		datasets.put(DatasetType.CLASSIFICATION_DIABETES, new Classification_Diabetes());
		datasets.put(DatasetType.CLASSIFICATION_VOWEL, new Classification_Vowel());
		datasets.put(DatasetType.CLASSIFICATION_ABALONE, new Classification_Abalone());
		datasets.put(DatasetType.CLASSIFICATION_BREAST_W, new Classification_Breast_W());
		datasets.put(DatasetType.CLASSIFICATION_DIABETES, new Classification_Diabetes());
		datasets.put(DatasetType.CLASSIFICATION_GLASS, new Classification_Glass());
		datasets.put(DatasetType.CLASSIFICATION_HEART_STATLOG, new Classification_Heart_Statlog());
		datasets.put(DatasetType.CLASSIFICATION_IONOSPHERE, new Classification_Ionosphere());
		datasets.put(DatasetType.CLASSIFICATION_OPTDIGITS, new Classification_Optdigits());
		datasets.put(DatasetType.CLASSIFICATION_PAGE_BLOCKS, new Classification_PageBlocks());
		datasets.put(DatasetType.CLASSIFICATION_SEGMENT, new Classification_Segment());
		datasets.put(DatasetType.CLASSIFICATION_SONAR, new Classification_Sonar());
		datasets.put(DatasetType.CLASSIFICATION_SPECTROMETER, new Classification_Spectrometer2());
		datasets.put(DatasetType.CLASSIFICATION_VEHICLE, new Classification_Vehicle());
		datasets.put(DatasetType.CLASSIFICATION_WAVEFORM, new Classification_Waveform());
		datasets.put(DatasetType.CLASSIFICATION_YEAST, new Classification_Yeast());
		datasets.put(DatasetType.CLASSIFICATION_SOLAR_M, new Classification_SolarM());
		datasets.put(DatasetType.CLASSIFICATION_SOLAR_C, new Classification_SolarC());
		datasets.put(DatasetType.CLASSIFICATION_ANNEAL, new Classification_Anneal());
		datasets.put(DatasetType.CLASSIFICATION_CREDIT_A, new Classification_Credit_A());
		datasets.put(DatasetType.CLASSIFICATION_HEART_C, new Classification_Heart_C());
		datasets.put(DatasetType.CLASSIFICATION_LABOR, new Classification_Labor());
		datasets.put(DatasetType.CLASSIFICATION_LYMPH, new Classification_Lymph());
		datasets.put(DatasetType.CLASSIFICATION_SOYBEAN, new Classification_Soybean());
		datasets.put(DatasetType.CLASSIFICATION_SPLICE, new Classification_Splice());
		datasets.put(DatasetType.CLASSIFICATION_AIRLINE, new Classification_Airline());
		datasets.put(DatasetType.CLASSIFICATION_ELECTRICITY, new Classification_Electricity());
		datasets.put(DatasetType.CLASSIFICATION_EYE_STATE, new Classification_Eye_State());
		datasets.put(DatasetType.CLASSIFICATION_LETTER_RECOGNITION, new Classification_Letter_Recognition());
		datasets.put(DatasetType.CLASSIFICATION_MUSHROOM, new Classification_Mushroom());
		datasets.put(DatasetType.CLASSIFICATION_THYROID, new Classification_Thyroid());
		datasets.put(DatasetType.MULTILABEL_CAL500, new Multilabel_CAL500());
		datasets.put(DatasetType.MULTILABEL_EMOTIONS, new Multilabel_Emotions());
		datasets.put(DatasetType.MULTILABEL_MEDIAMILL, new Multilabel_Mediamill());
		datasets.put(DatasetType.MULTILABEL_SCENE, new Multilabel_Scene());
		datasets.put(DatasetType.MULTILABEL_YEAST, new Multilabel_Yeast());
		datasets.put(DatasetType.MULTILABEL_BIBTEX, new Multilabel_Bibtex());
		datasets.put(DatasetType.MULTILABEL_DELICIOUS, new Multilabel_Delicious());
		datasets.put(DatasetType.MULTILABEL_ENRON, new Multilabel_Enron());
		datasets.put(DatasetType.MULTILABEL_MEDICAL, new Multilabel_Medical());
		datasets.put(DatasetType.MULTILABEL_COREL5K, new Multilabel_Corel5k());
		datasets.put(DatasetType.MULTILABEL_FLAGS, new Multilabel_Flags());
		datasets.put(DatasetType.MULTILABEL_TMC2007, new Multilabel_TMC2007());
		datasets.put(DatasetType.MULTILABEL_BIRDS, new Multilabel_Birds());
		datasets.put(DatasetType.MULTILABEL_COREL16K, new Multilabel_Corel16k());
		datasets.put(DatasetType.MULTILABEL_GENBASE, new Multilabel_Genbase());
		datasets.put(DatasetType.MULTILABEL_BIBTEX_PCA, new Multilabel_Bibtex_PCA());
		datasets.put(DatasetType.MULTILABEL_COREL5K_PCA, new Multilabel_Corel5k_PCA());
		datasets.put(DatasetType.MULTILABEL_ENRON_PCA, new Multilabel_Enron_PCA());
		datasets.put(DatasetType.MULTILABEL_MEDICAL_PCA, new Multilabel_Medical_PCA());
		
	}
}

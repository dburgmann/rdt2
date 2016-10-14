package evaluation.dataset.classification.mixed;

import java.io.IOException;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.ClassificationCollector;
import rdt.tree.collector.CollectorPreferences;

public class Classification_Electricity extends Dataset{

	@Override
	public RDTInstances getTrainInstances() throws IOException, RDTException {
		return new RDTInstances("data/classification/electricityMOA/elecNormNew.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getTestInstances() throws IOException, RDTException {
		return new RDTInstances("data/classification/electricityMOA/elecNormNew.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getCVInstances() throws IOException, RDTException {
		return new RDTInstances("data/classification/electricityMOA/elecNormNew.arff", getRestrictedAttributeIds());
	}

	@Override
	public int[] getRestrictedAttributeIds() {
		return new int[]{8};
	}

	@Override
	public CollectorPreferences getCollectorPreferences() {
		CollectorPreferences cp = new CollectorPreferences();
		cp.addCollector(new ClassificationCollector(getRestrictedAttributeIds()[0]));
		return cp;
	}

	@Override
	public DatasetType getDatasetType() {
		return DatasetType.CLASSIFICATION_ELECTRICITY;
	}
	
	@Override
	public String getName() {
		return "Electricity";
	}

}

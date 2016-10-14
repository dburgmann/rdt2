package evaluation.dataset.classification.nominal;

import java.io.IOException;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.ClassificationCollector;
import rdt.tree.collector.CollectorPreferences;

public class Classification_Car extends Dataset{

	@Override
	public RDTInstances getTrainInstances() throws IOException, RDTException {
		return new RDTInstances("data/classification/car.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getTestInstances() throws IOException, RDTException {
		return new RDTInstances("data/classification/car.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getCVInstances() throws IOException, RDTException {
		return new RDTInstances("data/classification/car.arff", getRestrictedAttributeIds());
	}

	@Override
	public int[] getRestrictedAttributeIds() {
		return new int[]{6};
	}

	@Override
	public CollectorPreferences getCollectorPreferences() {
		CollectorPreferences cp = new CollectorPreferences();
		cp.addCollector(new ClassificationCollector(getRestrictedAttributeIds()[0]));
		return cp;
	}

	@Override
	public DatasetType getDatasetType() {
		return DatasetType.CLASSIFICATION_CAR;
	}
	
	@Override
	public String getName() {
		return "Car";
	}

}

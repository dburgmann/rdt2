package evaluation.dataset.multilabel.mixed;

import java.io.IOException;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.MultilabelCollector;

public class Multilabel_Birds extends Dataset{

	private int[] rA;
	
	public Multilabel_Birds(){
		rA = new int[19];
		for(int i=0; i<rA.length; i++){
			rA[i] = i + 260;
		}
	}
	
	@Override
	public RDTInstances getTrainInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/birds/birds-train.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getTestInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/birds/birds-test.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getCVInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/birds/birds.arff", getRestrictedAttributeIds());
	}	

	@Override
	public int[] getRestrictedAttributeIds() {
		return rA;
	}

	@Override
	public CollectorPreferences getCollectorPreferences() {
		CollectorPreferences cp = new CollectorPreferences();
		cp.addCollector(new MultilabelCollector(getRestrictedAttributeIds()));
		return cp;
	}
	
	@Override
	public String getName() {
		return "Birds";
	}

	@Override
	public DatasetType getDatasetType() {
		return DatasetType.MULTILABEL_BIRDS;
	}
}

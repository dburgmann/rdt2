package evaluation.dataset.multilabel.nominal;

import java.io.IOException;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.MultilabelCollector;

public class Multilabel_Enron extends Dataset{

	private int[] rA;
	
	public Multilabel_Enron(){
		rA = new int[53];
		for(int i=0; i<rA.length; i++){
			rA[i] = i + 1001;
		}
	}
	
	@Override
	public RDTInstances getTrainInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/enron/enron-train.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getTestInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/enron/enron-test.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getCVInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/enron/enron.arff", getRestrictedAttributeIds());
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
	public DatasetType getDatasetType() {
		return DatasetType.MULTILABEL_ENRON;
	}
	
	@Override
	public String getName() {
		return "enron";
	}
}

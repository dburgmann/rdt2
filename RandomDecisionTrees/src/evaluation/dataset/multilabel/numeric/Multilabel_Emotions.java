package evaluation.dataset.multilabel.numeric;

import java.io.IOException;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.MultilabelCollector;

public class Multilabel_Emotions extends Dataset{

	private int[] rA;
	
	public Multilabel_Emotions(){
		rA = new int[6];
		for(int i=0; i<rA.length; i++){
			rA[i] = i + 72;
		}
	}
	
	@Override
	public RDTInstances getTrainInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/Emotions/emotions-train.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getTestInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/Emotions/emotions-test.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getCVInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/Emotions/emotions.arff", getRestrictedAttributeIds());
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
		return "Emotions";
	}

	@Override
	public DatasetType getDatasetType() {
		return DatasetType.MULTILABEL_EMOTIONS;
	}
}

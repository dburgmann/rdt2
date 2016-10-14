package evaluation.dataset.multilabel.pca;

import java.io.IOException;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import rdt.essentials.RDTException;
import rdt.essentials.RDTInstances;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.MultilabelCollector;

public class Multilabel_Bibtex_PCA extends Dataset{

	private int[] rA;
	
	public Multilabel_Bibtex_PCA(){
		rA = new int[159];
		for(int i=0; i<rA.length; i++){
			rA[i] = i + 200;
		}
	}
	
	@Override
	public RDTInstances getTrainInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/bibtex/pca/pca_bibtex-train.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getTestInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/bibtex/pca/pca_bibtex-test.arff", getRestrictedAttributeIds());
	}

	@Override
	public RDTInstances getCVInstances() throws IOException, RDTException {
		return new RDTInstances("data/multilabel/bibtex/pca/pca_bibtex.arff", getRestrictedAttributeIds());
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
		return DatasetType.MULTILABEL_BIBTEX_PCA;
	}
	
	@Override
	public String getName() {
		return "bibtex_PCA";
	}
}

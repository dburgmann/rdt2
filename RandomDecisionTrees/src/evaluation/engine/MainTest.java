package evaluation.engine;

import java.io.IOException;

import evaluation.EvaluationResultsSummary;
import evaluation.Evaluation;
import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import evaluation.dataset.Datasets;
import rdt.essentials.RDTException;
import rdt.model.BatchEnsemble;
import rdt.model.MultilabelChainEnsemble;
import rdt.model.MultilabelChainEnsemble.ChainType;
import rdt.model.MultilabelChainEnsemble.PredictType;
import rdt.model.QuantilEnsemble;
import rdt.model.SparseMultilabelChainEnsemble;
import rdt.tree.Tree.PredictionCombination;
import rdt.tree.collector.CollectorPreferences;
import rdt.tree.collector.LPMultilabelCollector;
import rdt.tree.collector.LPMultilabelCollector_OLD;
import rdt.tree.collector.MultilabelCollector;
import rdt.tree.collector.PMultilabelCollector;
import rdt.util.LPConverter;
import rdt.util.TreeVisualizer;
import weka.core.Instance;

/**
 * Exists only for some tests.
 * 
 * @author MK
 */
public class MainTest {

	public static void main(String[] args) throws IOException, Exception {
		
		Dataset dataset = Datasets.getDataset(DatasetType.MULTILABEL_COREL5K);		
		
		CollectorPreferences cp = dataset.getCollectorPreferences();
		
		//CollectorPreferences cp = new CollectorPreferences();
		//cp.addCollector(new LPMultilabelCollector(dataset.getRestrictedAttributeIds(), new LPConverter()));
		//cp.addCollector(new LPMultilabelCollector_OLD(dataset.getRestrictedAttributeIds()));
		
		int numTrees = 200;
		int maxDeep = 30;
		int maxS = 2;
		long randomSeed = 2;
		double percentageLabels = 0.1;
		double percentageActiveLabel = 1;
		int numChainPredict = 1000000;
		PredictType pt = PredictType.LP_CHAIN;
		ChainType ct = ChainType.HYBRID;
		int numAttrs = 1000;
		
		//BatchEnsemble model = new BatchEnsemble(cp, numTrees, maxDeep, maxS, randomSeed);

		SparseMultilabelChainEnsemble model = new SparseMultilabelChainEnsemble(cp, numTrees, maxDeep, maxS, randomSeed, percentageLabels, percentageActiveLabel, numChainPredict, pt, ct);
		
		//MultilabelChainEnsemble model = new MultilabelChainEnsemble(cp, numTrees, maxDeep, maxS, randomSeed, percentageLabels, percentageActiveLabel, numChainPredict, pt, ct);
		
		
		
		
		int maxValues = 10;
		boolean randomQuantil = false;
		boolean conceptDrift = false;
		boolean interpolate = false;
		
		
		//QuantilEnsemble model = new QuantilEnsemble(cp, numTrees, maxDeep, maxValues, randomQuantil, conceptDrift, interpolate, randomSeed);
		
		EvaluationResultsSummary cmr = Evaluation.evaluateTestSet(model, dataset.getTrainInstances(), dataset.getTestInstances(), 1);
		//EvaluationResultsSummary cmr = Evaluation.evaluateCV(model, dataset.getCVInstances(), 5, 1);
		
		//TreeVisualizer.visualize(model.getTrees()[0]);
		
		System.out.println(cmr.getSummary());
		
	}

}

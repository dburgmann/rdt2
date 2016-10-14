package evaluation.experiment.experiments;

import evaluation.EvaluationType;
import evaluation.dataset.DatasetType;
import evaluation.experiment.CompareExperiment;
import evaluation.experiment.ExperimentType;
import rdt.model.ModelType;
import rdt.model.MultilabelChainEnsemble.ChainType;
import rdt.model.MultilabelChainEnsemble.PredictType;

public class SparseMultilabelChainExperiment extends CompareExperiment {
	
	@Override
	protected ModelType[] getModelTypes() {
		return new ModelType[]{
				ModelType.MULTILABEL_CHAIN_ENSEMBLE
		};
	}

	@Override
	protected Object[][][] getParameters() {
		return new Object[][][]{
			//Model 1
			{
				new Integer[]{300},		//Trees
				new Integer[]{40},				//maxDeep
				new Integer[]{3},				//maxS
				new Long[]{(long) 1},			//randomSeed
				new Double[]{0.0, 0.05, 0.1, 0.2, 0.3},					//percentageLabels
				new Double[]{1.0}, 				//percentageActiveLabels
				new Integer[]{10000},				//numChainPredict
				new PredictType[]{PredictType.PERCENTAGE_CHAIN, PredictType.LABEL_CHAIN, PredictType.LP_CHAIN},				
				new ChainType[]{ChainType.HYBRID},	
				
			},
	
		};
	}

	@Override
	public DatasetType[] getDatasets() {
		return new DatasetType[]{
				//DatasetType.MULTILABEL_ENRON,
				DatasetType.MULTILABEL_MEDICAL
		};
	}

	@Override
	protected int[] getFocuses() {
		return new int[]{4};
	}

	@Override
	protected EvaluationType getEvaluationType() {
		return EvaluationType.CROSS_VALIDATION;
	}

	@Override
	protected int getNumFolds() {
		return 5;
	}

	@Override
	protected int getRounds() {
		return 1;
	}

	@Override
	protected ExperimentType getExperimentType() {
		return ExperimentType.SPARSE_EXPERIMENT;
	}


}

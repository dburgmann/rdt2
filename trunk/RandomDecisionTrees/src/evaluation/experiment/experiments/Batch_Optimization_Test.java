package evaluation.experiment.experiments;

import evaluation.EvaluationType;
import evaluation.dataset.DatasetType;
import evaluation.experiment.CompareExperiment;
import evaluation.experiment.ExperimentType;

import rdt.model.ModelType;
import rdt.model.MultilabelChainEnsemble.ChainType;
import rdt.model.MultilabelChainEnsemble.PredictType;

public class Batch_Optimization_Test extends CompareExperiment {
	
	@Override
	protected ModelType[] getModelTypes() {
		return new ModelType[]{
				ModelType.SPARSE_MULTILABEL_CHAIN_ENSEMBLE,
		};
	}

	@Override
	protected Object[][][] getParameters() {
		return new Object[][][]{
			//Model 1
			{
				new Integer[]{50},		//Trees
				new Integer[]{20},				//maxDeep
				new Integer[]{5},				//maxS
				new Long[]{(long) 1},			//randomSeed	
				new Double[]{0.2},					//percentageLabels
				new Double[]{1.0}, 				//percentageActiveLabels
				new Integer[]{10000},				//numChainPredict
				new PredictType[]{PredictType.PERCENTAGE_CHAIN},				
				new ChainType[]{ChainType.HYBRID},	
				new Integer[]{10},
				
			},
		
		};
	}

	@Override
	public DatasetType[] getDatasets() {
		return new DatasetType[]{
				DatasetType.MULTILABEL_ENRON
		};
	}

	@Override
	protected int[] getFocuses() {
		return new int[]{1};
	}

	@Override
	protected EvaluationType getEvaluationType() {
		return EvaluationType.CROSS_VALIDATION;
	}

	@Override
	protected int getNumFolds() {
		return 10;
	}

	@Override
	protected int getRounds() {
		return 1;
	}

	@Override
	protected ExperimentType getExperimentType() {
		return ExperimentType.BATCH_OPTIMIZATION_TEST;
	}


}

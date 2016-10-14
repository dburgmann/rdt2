package evaluation.engine;

import java.util.LinkedList;
import java.util.List;

import evaluation.dataset.Dataset;
import evaluation.dataset.DatasetType;
import evaluation.dataset.Datasets;
import evaluation.experiment.Experiment;
import evaluation.experiment.experiments.Batch_Optimization_Test;
import evaluation.experiment.experiments.SparseMultilabelChainExperiment;
import evaluation.experiment.experiments.TestExperiment;

/**
 * This class represents the main-class to start an experiment. You can simply add your newly
 * created experiment in the switch. By using the arguments you can choose your experiment. The 
 * first argument is the keyword for the experiment which is specified in the switch. Furthermore
 * you can add some more arguments to specify on which dataset the experiment will be run (each 
 * experiment contains a list of datasets on which the experiment can be run). To run an experiment
 * on a specific dataset you only have to add the index of the dataset in the list as an argument.
 * For example if you want to run the experiment only on the first dataset of the list you can 
 * write "test 0" if you named the experiment "test" in the switch. It is also possible to run
 * the experiment on two or more datasets by adding the specific indexes of the datasets to the 
 * arguments (e.g. "test 0 3 4") (the datasets will be processed sequentially). If you do not 
 * specifiy any datasets in the arguments then the experiment will be run on all datasets.
 * 
 * @author MK
 */
public class ExperimentEngine {

	
	public static void main(String[] args) throws Exception {

		//For testing 
		args = new String[]{"sparse"};
		
		
		
		List<Experiment> experiments = new LinkedList<Experiment>();
		
		if(args.length == 0){
			System.out.println("No parameters!");
			System.exit(0);
		}
		

		
		
		
		
		switch(args[0]){
		
		//example for experiment
		case "test":
			TestExperiment test = new TestExperiment();
			experiments.add(test);
			break;
		case "sparse":
			experiments.add(new SparseMultilabelChainExperiment());
			break;
		case "batch_opt":
			experiments.add(new Batch_Optimization_Test());
			break;

		default:
			System.out.println("Unknown parameter!");
			System.exit(0);
		}
		
		
		//For each added experiment
		for(Experiment exp : experiments){
		
			//Now get the datasets which are given in the args with index >= 1
			//If no more arguments are given all datasets of the experiments will be evaluated
			DatasetType[] datasetTypes = exp.getDatasets();
			if(args.length > 1){
				DatasetType[] temp = new DatasetType[args.length-1];
				for(int j=1; j<args.length; j++){
					int i = Integer.parseInt(args[j]);
					temp[j-1] = datasetTypes[i];	
				}
				datasetTypes = temp;
			}
			
			//Perform experiment for each chosen dataset
			for(DatasetType type : datasetTypes){
				Dataset dataset = Datasets.getDataset(type);
				exp.runExperiment(dataset);
			}
		}
	}
}

package evaluation.measure;

import mulan.classifier.MultiLabelOutput;
import mulan.evaluation.GroundTruth;

/**
 * Class for measures which are imported from the mulan library. This class works as a wrapper
 * for the mulan-measures. 
 * 
 * @author MK
 */
public class MulanMeasure implements Measure{

	/**
	 * Stores an instance of the measure of the mulan library.
	 */
	private mulan.evaluation.measure.Measure mulanMeasure;
	
	/**
	 * This boolean indicates if the values of the attributes which are evaluated 
	 * are numeric or nominal
	 */
	private boolean isNumeric;
	
	/**
	 * Create a new MulanMeasure with the given measure of the mulan library.
	 * 
	 * @param mulanMeasure the measure from the mulan library
	 * @param isNumeric this indicates if the values of the attributes which are evaluated 
	 * are numeric or nominal
	 */
	public MulanMeasure(mulan.evaluation.measure.Measure mulanMeasure, boolean isNumeric){
		this.mulanMeasure = mulanMeasure;
		this.isNumeric = isNumeric;
	}

	@Override
	public void update(double[] truth, double[] pred) {
		GroundTruth gt;
		MultiLabelOutput mlo;
		
		if(isNumeric){
			gt = new GroundTruth(truth);
			mlo = new MultiLabelOutput(pred);
		}else{
			boolean[] truthBool = new boolean[truth.length];
			boolean[] predBool = new boolean[pred.length];
			
			for(int i=0; i<truth.length; i++){
				if(truth[i] == 1){
					truthBool[i] = true;
				}else{
					truthBool[i] = false;
				}
			}
			
			for(int i=0; i<pred.length; i++){
				if(pred[i] == 1){
					predBool[i] = true;
				}else{
					predBool[i] = false;
				}
			}
			
			gt = new GroundTruth(truthBool);
			mlo = new MultiLabelOutput(predBool);
		}

		mulanMeasure.update(mlo, gt);
	}
	@Override
	public String toString(){
		return mulanMeasure.toString();
	}

	@Override
	public double getIdealValue() {
		return mulanMeasure.getIdealValue();
	}

	@Override
	public double getValue() {
		return mulanMeasure.getValue();
	}

	@Override
	public String getName() {
		return mulanMeasure.getName();
	}
	
}

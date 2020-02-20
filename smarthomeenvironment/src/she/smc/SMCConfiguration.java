package she.smc;

import java.util.Arrays;

public class SMCConfiguration {

	private int features[];
	private double featureFailureRates[];
	private int pAdd;
	private Requiremnts requirements = new Requiremnts();

	// São os 3 QAs, custo, taxa de falha e tempo de feature
	public class Requiremnts {
		private double cost;
		private double failureRate;
		private double featureTime;
	}
	
	public double getFailureRate() {
		return requirements.failureRate;
	}
	
	public void setFailureRate(double failureRate) {
		if(requirements == null) {
			requirements = new Requiremnts();
			requirements.failureRate = failureRate;
		} else {
			requirements.failureRate = failureRate;
		}
	}
	
	public double getCost() {
		return requirements.cost;
	}
	
	public void setCost(double cost) {
		if(requirements == null) {
			requirements = new Requiremnts();
			requirements.cost = cost;
		} else {
			requirements.cost = cost;
		}
	}
	
	public double getFeatureTime() {
		return requirements.featureTime;
	}
	
	public void setFeatureTime(double featureTime) {
		if(requirements == null) {
			requirements = new Requiremnts();
			requirements.featureTime = featureTime;
		} else {
			requirements.featureTime = featureTime;
		}
	}

	public int[] getFeatures() {
		return features;
	}
	
	public void setFeatures(int[] features) {
		this.features = features;
	}

	public void setFeatureFailureRates(double[] featureFailureRates) {
		this.featureFailureRates = featureFailureRates;
	}

	public void setpAdd(int pAdd) {
		this.pAdd = pAdd;
	}

	public Requiremnts getRequirements() {
		return requirements;
	}

	public void setRequirements(Requiremnts requirements) {
		this.requirements = requirements;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(features) + "," + getFailureRate() + "," + getCost() + "," + getFeatureTime();
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(features) + Arrays.hashCode(featureFailureRates) + pAdd;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SMCConfiguration) {
			SMCConfiguration configuration = (SMCConfiguration) obj;
			return Arrays.equals(features, configuration.features) && Arrays.equals(featureFailureRates, configuration.featureFailureRates)
					&& pAdd == configuration.pAdd;
		}
		return false;
	}
}

package she.simulation;

import java.util.Random;

import she.adaptation.periodic.InformationProvider;
import she.enumeration.TypeFeature;

public class Feature {


	private int id;

	private TypeFeature type;

	private double failureRate;

	private double cost;

	private double responseTime;

	private int queueLength;

	public Feature(int id, TypeFeature type, double failureRate, double cost, double responseTime, int queueLength) {
		super();
		this.id = id;
		this.type = type;
		this.failureRate = failureRate;
		this.cost = cost;
		this.responseTime = responseTime;
		this.queueLength = queueLength;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	public TypeFeature getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(TypeFeature type) {
		this.type = type;
	}

	/**
	 * @return the failureRate
	 */
	public double getFailureRate() {
		return failureRate;
	}

	/**
	 * @param failureRate the failureRate to set
	 */
	public void setFailureRate(double failureRate) {
		this.failureRate = failureRate;
		InformationProvider.featureFailureRates[(getType().getId() - 1)][id] = failureRate;
	//	InformationProvider.serviceFailureRates[type-1][id] = failureRate;
	}

	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * @return the responseTime
	 */
	public double getResponseTime() {
		return responseTime;
	}

	/**
	 * @param responseTime the responseTime to set
	 */
	public void setResponseTime(double responseTime) {
		this.responseTime = responseTime;
		InformationProvider.serviceTimes[(getType().getId() - 1)][id] = responseTime;
	}

	/**
	 * @return the queueLength
	 */
	public int getQueueLength() {
		return queueLength;
	}

	/**
	 * @param queueLength the queueLength to set
	 */
	public void setQueueLength(int queueLength) {
		this.queueLength = queueLength;
		InformationProvider.queueLength[(getType().getId() - 1)][id] = queueLength;
	}
	
	
	//--------------------------------------------------------------------- Modificar
	

	static Random random = new Random();

	int invocations = 0;

	// standard deviation
	//taxa de desvio padrão
	static public double sd = 0.05;

	// TODO: Analisar a questão da quantidade de invocações, pra colocar um peso na
	// taxa de falha.
	static public int MinInvocationsForSD = 10;

	static public int FeatureQueueRefreshPeriod = 15;

	double originalFrate;

	

	public boolean invoke() {
		invocations++;
		// change queue length after 15 invocations
		if (invocations % FeatureQueueRefreshPeriod == 0) {
			setQueueLength(random.nextInt(5) + 1);
		}
		// update failure rate according to standard deviation after 10 invocations
		if (invocations > MinInvocationsForSD) {
			invocations = 0;
			double failure;
			do {
				failure = random.nextGaussian() * sd + originalFrate;
			} while (failure < 0);
			setFailureRate(failure);
		}

		return random.nextDouble() >= getFailureRate();
	}
	

}

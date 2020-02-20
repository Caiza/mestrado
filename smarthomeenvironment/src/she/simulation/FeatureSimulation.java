package she.simulation;

import java.util.Random;

import she.enumeration.TypeFeature;

public class FeatureSimulation extends Feature {

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

	public FeatureSimulation(int id, TypeFeature type, double failureRate, double cost, double responseTime,
			int queueLength) {
		super(id, type, failureRate, cost, responseTime, queueLength);
		originalFrate = failureRate;
	}

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

package she.simulation;



import java.text.DecimalFormat;
import java.util.Arrays;

import she.adaptation.periodic.InformationProvider;
import she.adaptation.periodic.Window;
import she.smc.SMCChecker;
import she.smc.SMCConfiguration;

public class FeedbackLoopSimulation {
	
	int invocations;
	double cost, failureRate, featureTime;
	public static int AdaptationPeriod = 500;
	public static int SlidingWindowSize = 1000;
	public static int SlidingWindowAvgSize = 100;
	Window windowFR = new Window(SlidingWindowSize, SlidingWindowAvgSize); //FR failureRate
	Window windowCost = new Window(SlidingWindowSize, SlidingWindowAvgSize);
	Window windowFT = new Window(SlidingWindowSize, SlidingWindowAvgSize);//FT featureTime
	

	double[][] featureFailureRates = { { 0.11, 0.04, 0.18, 0.08, 0.14 }, { 0.12, 0.07, 0.18, 0.10, 0.15 },
			{ 0.01, 0.03, 0.05, 0.07, 0.02 } };
	
	double removeP = 0.50;
	
	/**
	 * This method starts the adaptation engine
	 */
	public void start() {
		if (!SMCChecker.useFeatureTime) System.out.println("SNo,[ADD,REM,DOW], Invocation Result, Cost, AvgFailureRate, AvgCost");
		else System.out.println("SNo,[ADD,REM,DOW], Invocation Result, Cost, FeatureTime, AvgFailureRate, AvgCost, AvgFeatureTime");
	}
	
	/**
	 * This method stops the adaptation engine and remove any configuration
	 * associated with it.
	 */
	public void stop() {
		
	}
	
	public void monitor(boolean result, double iCost, double iFeatureTime) {
		
		invocations++;
		windowFR.add(result ? 1 : 0);
		failureRate = windowFR.isAveragePossible() ? 1 - windowFR.getAverage() : 0;
		
		windowCost.add(iCost);
		this.cost = windowCost.getAverage();
		
		if(SMCChecker.useFeatureTime) {
			windowFT.add(iFeatureTime);
			featureTime = windowFT.getAverage();
		}
		
		if(!SMCChecker.useFeatureTime) {
			System.out.format("%d, %s, %s, %f, %f, %f\\n", invocations, 
					Arrays.toString(Start.currentFeatures), result?"Success":"Failed", iCost, failureRate, cost);
		}else {
			System.out.format("%d, %s, %s, %f, %f, %f, %f, %f\n", invocations, Arrays.toString(Start.currentFeatures), 
					result?"Success": "Failed", iCost, iFeatureTime, failureRate, cost, featureTime);
		}
		analyzePeriodic();
	}

	private void analyzePeriodic() {
		if(invocations % AdaptationPeriod == 0) {
			adaptationRequired();
		}
	}

	private void adaptationRequired() {
		SMCConfiguration[] configurations = invokeSMC();
		SMCConfiguration bestConfiguration;
		if(SMCChecker.useFeatureTime) {
			bestConfiguration = planner3Req(configurations);
		}else {
			bestConfiguration = planner2Rew(configurations);
		}
		if(bestConfiguration != null) {
			exectuor(bestConfiguration);
		}else {
			System.out.println(" Planner couldn't find a best configuration ");
		}
		
		
	}

	private void exectuor(SMCConfiguration smcConfiguration) {
		Start.currentFeatures = smcConfiguration.getFeatures();
		
	}

	private SMCConfiguration planner2Rew(SMCConfiguration[] configurations) {
		double bestCost = Double.MAX_VALUE, bestFR = Double.MAX_VALUE;
		SMCConfiguration config;
		double R1 = 0.15;
		double R2 = 8.0;
		int bestIndex = -1;
		for(int i = 0; i < configurations.length; i++) {
			config= configurations[i];
			if(config.getFailureRate() < R1 && config.getCost() < R2) {
				if(config.getFailureRate() < bestFR 
						|| (config.getFailureRate() == bestFR && config.getCost() < bestCost) ) {
					bestIndex = i;
					bestFR = config.getFailureRate();
					bestCost = config.getCost();
				}
			}
		}
		if(bestIndex != -1) {
			return configurations[bestIndex];
		}
		return null;
	}

	private SMCConfiguration planner3Req(SMCConfiguration[] configurations) {
		double bestCost = Double.MAX_VALUE, bestFR = Double.MAX_VALUE, bestFT = Double.MAX_VALUE;
		SMCConfiguration config;
		//valores dos requisitos
		double R1 = 0.15;
		double R2 = 8.0;
		double R3 = 60;
		int bestIndex = -1;
		for(int i = 0; i < configurations.length; i ++) {
			config = configurations[i];
			if(config.getFailureRate() < R1 && config.getCost() < R2 && config.getFeatureTime() < R3) {
				if(config.getFeatureTime() < bestFT 
						|| (config.getFeatureTime() == bestFT && config.getFailureRate() < bestFR)) {
					bestIndex = i;
					bestFR = config.getFailureRate();
					bestCost = config.getCost();
					bestFT = config.getFeatureTime();
				}
			}
		}
		if(bestIndex != -1) {
			return configurations[bestIndex];
		}
		return null;
	}

	private SMCConfiguration[] invokeSMC() {
		double[][] failureRates = new double[3][5];
		for(int i = 0; i < 3; i ++) {
			for(int j = 0; j < 5; j++) {
				failureRates[i][j] = InformationProvider.featureFailureRates[i][j];
			}
		}
		int removeP = (int) Math.round(InformationProvider.removeP * 100);
		if(!SMCChecker.useFeatureTime) {
			SMCChecker.setInitialData(removeP, 20, 100 - removeP, 88, failureRates);
		}
		else {
			SMCChecker.setInitialData(removeP, 20, 100 - removeP, 88, failureRates, InformationProvider.queueLength);
		}
		
		SMCConfiguration[] configurations = SMCChecker.checkAllConfiguration();
		return configurations;
	}
	
	DecimalFormat decimalFormat = new DecimalFormat("0.00");
	
	public String getFeatureString() {
		int[] cFeatures = Start.currentFeatures;
		
		double[][] failureRates = InformationProvider.featureFailureRates;
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < cFeatures.length; i ++) {
			sb.append(cFeatures[i]);
			sb.append(",");
		}
		
		for(int i = 0; i < cFeatures.length; i++) {
			sb.append(decimalFormat.format(failureRates[i][cFeatures[i] - 1]));
			sb.append(",");
		}
		return sb.toString();
	}
	

}

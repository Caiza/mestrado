package she.simulation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider.Service;
import java.util.Properties;
import java.util.Random;

import javax.script.Invocable;

import org.omg.CORBA.portable.InvokeHandler;

import she.adaptation.periodic.InformationProvider;
import she.enumeration.TypeFeature;
import she.smc.SMCChecker;

public class Start {

	FeatureSimulation installFeature;
	FeatureSimulation uninstallFeature;

	final int IMPL = 5;
	int numberOfSimulations = 10000;

	public static int[] currentFeatures = { 1, 1, 1 };

	FeedbackLoopSimulation feedbackloop;
	// ADD -> adcionar feature, REM -> remover feature, DOW -> download da feature
	// MAS --> ADD
	// DS --> DOW
	// REM --> REM
	static final int ADD = 1, REM = 2, DOW = 3;
	Feature[] addFeature;
	Feature[] downloadFeature;
	Feature[] removeFeature;

	double sd = 0.10;
	int MinInvocationsForSD = 400;

	public static void main(String[] args) {

		Start start = new Start();
		// Load properties
		start.readProperties();

		start.initializeSHE();
		start.startSimulation();

	}

	private void startSimulation() {
		feedbackloop.start();
		Random rand = new Random();

		for (int i = 0; i < numberOfSimulations; i++) {
			if (i % MinInvocationsForSD == 0) {
				double failure;
				do {
					failure = rand.nextGaussian() * sd + 0.25;
				} while (failure < 0);
				InformationProvider.removeP = failure;
			}
			
			if(rand.nextDouble() >= InformationProvider.removeP) {
				if(invoke(ADD)) {
					if(rand.nextDouble() >= InformationProvider.downloadP) {
						invoke(DOW);
					}else {
						invoke(REM);
					}
				}
			}else {
				invoke(REM);
			}
		}
	}

	double cost = 0;
	double featureTime;

	private boolean invoke(int featureType) {
		Feature feature = getFeature(featureType);
		boolean result = feature.invoke();

		if (result) {
			cost = feature.getCost();
			featureTime = feature.getResponseTime();
		} else {
			feedbackloop.monitor(result, cost, 0);
			cost = 0;
			featureTime = 0;
		}
		if (result && (featureType == REM || featureType == DOW)) {
			feedbackloop.monitor(result, cost, featureTime);
			cost = 0;
			featureTime = 0;
		}
		return result;
	}

	private Feature getFeature(int featureType) {

		if (featureType == REM) {
			return removeFeature[currentFeatures[REM] - 1];
		} else if (featureType == ADD) {
			return addFeature[currentFeatures[ADD - 1] - 1];
		}
		return downloadFeature[currentFeatures[DOW - 1] - 1];
	}

	private void initializeSHE() {
		downloadFeature = new Feature[IMPL];
		addFeature = new Feature[IMPL];
		removeFeature = new Feature[IMPL];
	
		
		downloadFeature[0] = new Feature(0, TypeFeature.DOWNLOAD, 0.11, 4.0, 5.7, 3);
		downloadFeature[1] = new Feature(1, TypeFeature.DOWNLOAD, 0.04, 12.0, 7.3, 2);
		downloadFeature[2] = new Feature(2, TypeFeature.DOWNLOAD, 0.18, 2.0, 3.8, 5);
		downloadFeature[3] = new Feature(3, TypeFeature.DOWNLOAD, 0.08, 3.0, 9.5, 1);
		downloadFeature[4] = new Feature(4, TypeFeature.DOWNLOAD, 0.14, 5.0, 18.6, 4);
		
		addFeature[0] = new Feature(0, TypeFeature.INSTALL, 0.12, 4.0, 11, 1);
		addFeature[1] = new Feature(1, TypeFeature.INSTALL, 0.07, 14.0, 9.4, 4);
		addFeature[2] = new Feature(2, TypeFeature.INSTALL, 0.18, 2.0, 20, 2);
		addFeature[3] = new Feature(3, TypeFeature.INSTALL, 0.10, 6.0, 8, 6);
		addFeature[4] = new Feature(4, TypeFeature.INSTALL, 0.15, 3.0, 9, 3);
		
		removeFeature[0] = new Feature(0, TypeFeature.UNISTALL, 0.01, 5.0, 8, 1);
		removeFeature[1] = new Feature(1, TypeFeature.UNISTALL, 0.03, 3.0, 7.7, 3);	
		removeFeature[2] = new Feature(2, TypeFeature.UNISTALL, 0.05, 2.0, 11, 5);	
		removeFeature[3] = new Feature(3, TypeFeature.UNISTALL, 0.07, 1.0, 10, 2);
		removeFeature[4] = new Feature(4, TypeFeature.UNISTALL, 0.02, 4.0, 15, 4);	
		
		//Initialize feedbackloop
		feedbackloop =  new FeedbackLoopSimulation();

	}

	private void readProperties() {

		try {
			Properties prop = new Properties();
			// criar propiedades
			String propFileName = System.getProperty("user.dir") + "/app.properties";

			InputStream inputStream = new FileInputStream(propFileName);

			prop.load(inputStream);

			// get the property value and print it out
			SMCChecker.command = prop.getProperty("verifyPath") + " -u %s";
			SMCChecker.useFeatureTime = prop.getProperty("useFeatureTime").contentEquals("true");
			SMCChecker.relativeSEM = Integer.parseInt(prop.getProperty("relativeSEM"));
			SMCChecker.simulationsSEM5 = Integer.parseInt(prop.getProperty("simulationsSEM5"));
			SMCChecker.simulationsSEM10 = Integer.parseInt(prop.getProperty("simulationsSEM10"));
			numberOfSimulations = Integer.parseInt(prop.getProperty("MAXInvocations"));
			FeedbackLoopSimulation.AdaptationPeriod = Integer.parseInt(prop.getProperty("AdaptationPeriod"));
			FeedbackLoopSimulation.SlidingWindowSize = Integer.parseInt(prop.getProperty("SlidingWindow_Size"));
			FeedbackLoopSimulation.SlidingWindowAvgSize = Integer.parseInt(prop.getProperty("SlidingWindow_MinSizeForAverage"));
			FeatureSimulation.sd = Double.parseDouble(prop.getProperty("FRates_SD"));
			FeatureSimulation.MinInvocationsForSD = Integer.parseInt(prop.getProperty("Frates_MinInvocationsForSD"));
			sd = Double.parseDouble(prop.getProperty("Emergency_SD"));
			MinInvocationsForSD = Integer.parseInt(prop.getProperty("Emergency_MinInvocationsForSD"));
			FeatureSimulation.FeatureQueueRefreshPeriod = Integer.parseInt(prop.getProperty("ServiceQueueRefreshPeriod"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

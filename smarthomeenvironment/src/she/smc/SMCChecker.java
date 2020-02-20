package she.smc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class SMCChecker {

	// Paths dos modelos realizado no Uppaal
	public static Path modelPathFrate = Paths.get(System.getProperty("user.dir") + "/models/sheFR.xml");
	public static Path modelPathCost = Paths.get(System.getProperty("user.dir") + "/models/sheCost.xml");
	public static Path modelPathFeatureTime = Paths.get(System.getProperty("user.dir") + "/models/sheTime.xml");

	static String modelFrate, modelCost, modelFeatureTime;
	public static boolean useFeatureTime = false;

	// Path do verifyta no meu computador
	//public static String command = "/Users/arguedes/Desktop/TAS/uppaal/uppaal-4.1.19/bin-Win32/verifyta -u %s";
	public static String command = "/Users/cfortuna/Desktop/uppaal/bin-Win32/verifyta -u %s";

	static int TYPES = 3;

	// We added uncertainty to probabilities of the Feature failure rates and
	// invoked requests based on a normal distribution
	// with a standard deviation of 0.05 and 0.10 respectively.
	public static int IMPL = 5;
	public static int relativeSEM = 10;

	// The simulation queries to estimate failure rates and cost with an accuracy of
	// RSEM 10% and 5% respectively.
	public static int simulationsSEM5 = 50;
	public static int simulationsSEM10 = 125;

	// Taxa de falha
	public static double failureRates[][] = new double[][] { { 11, 4, 18, 7, 14 }, { 12, 7, 18, 10, 15 },
			{ 1, 3, 5, 7, 1 } };

	public static int pAdd = 50;
	public static int pRemove = 50;
	public static int pDownload = 80;
	public static int pNoDownload = 20;

	private static String executeCommand(String command) {
		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			if (output.length() == 0) {
				while ((line = errorReader.readLine()) != null) {
					output.append(line + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return output.toString();
	}

	public static String getCommand(String modelPath) {
		return String.format(command, modelPath);
	}

	/*
	 * This method parse the following line -- States explored : 147698 states ---
	 * acredito q sejam os estados do uppaal
	 */
	final static String STATES_EXPLORED = "-- States explored : ";

	static long getStatesExplored(String string) {
		int start = string.indexOf(STATES_EXPLORED);
		if (start != -1) {
			start += STATES_EXPLORED.length();
			int end = string.indexOf(" states", start);
			String runs = string.substring(start, end);
			return Long.parseLong(runs);
		} else {
			throw new RuntimeException("Couldn't parse states explored");
		}
	}

	/*
	 * This method parse the following line -- CPU user time used : 571 ms
	 */
	final static String CPU_TIME = "-- CPU user time used : ";

	static long getCPUtime(String string) {

		int start = string.indexOf(CPU_TIME);
		if (start != -1) {
			start += CPU_TIME.length();
			int end = string.indexOf(" ms", start);
			String runs = string.substring(start, end);
			return Long.parseLong(runs);
		} else {
			throw new RuntimeException("Couldn't parse CPU time ");
		}
	}

	/*
	 * This method parse the following line -- Resident memory used : 6164 KiB
	 */

	final static String RESIDENT_MEM = "-- Resident memory used : ";

	static long getResidentMen(String string) {

		int start = string.indexOf(RESIDENT_MEM);
		if (start != -1) {
			start += RESIDENT_MEM.length();
			int end = string.indexOf(" KiB", start);
			String runs = string.substring(start, end);
			return Long.parseLong(runs);
		} else {
			throw new RuntimeException("Couldn't parse resident memory");
		}
	}

	static double getSimulatedValue(String string) {
		// find the last pair
		int startingIndex = string.lastIndexOf("(") + 1;
		int endIndex = string.lastIndexOf(")");
		String pair = string.substring(startingIndex, endIndex);
		String[] splitPair = pair.split(",");
		double value = Double.parseDouble(splitPair[1]);
		return value;
	}

	static String changeInvocationProbabilities(String file, int removeSensor, int noDownloadFeature, int addSensor,
			int downloadFeature) {
		String startText = "//&lt;probability&gt;";
		String endText = "//&lt;/probability&gt;";
		String newText = String.format("\nconst int p_ADD = %d, p_REMOVE = %d, p_DOWNLOAD = %d, p_NODOWNLOAD = %d;\n",
				addSensor, removeSensor, downloadFeature, noDownloadFeature);
		file = changeText(file, startText, endText, newText);
		return file;

	}

	private static String changeText(String file, String startString, String endString, String newText) {

		if (file.contains(startString)) {
			int startIndex = file.indexOf(startString) + startString.length();
			int endIndex = file.indexOf(endString);
			String oldText = file.substring(startIndex, endIndex);
			file = file.replace(oldText, newText);
			return file;
		} else {
			throw new RuntimeException("StartString:" + startString + " not found!");
		}
	}

	static String changeCurrentFeatures(String file, int Features[]) {

		String startText = "//&lt;feature-type-selection&gt;";
		String endText = "//&lt;/feature-type-selection&gt;";
		StringBuilder newText = new StringBuilder();
		newText.append("\nint cFeatures[s_type] = {");
		for (int i = 0; i < Features.length - 1; i++) {
			newText.append(Features[i] + ",");
		}
		newText.append(Features[Features.length - 1] + "};\n");
		
		file = changeText(file, startText, endText, newText.toString());
		return file;
	}

	static String changeFeatureTimes(String file, double[][] FeatureTimes) {

		String startText = "//&lt;response-time&gt;";
		String endText = "//&lt;/response-time&gt;";

		StringBuilder str = new StringBuilder("{");
		for (int i = 0; i < 3; i++) {
			str.append("{");
			for (int j = 0; j < 5; j++) {
				str.append(FeatureTimes[i][j]);
				if (j != 4) {
					str.append(",");
				}
				str.append("}");
				if (i != 2) {
					str.append(",");
				}
			}
		}
		str.append("}");
		String newText = String.format("\nconst double sRT[s_type][s_imp] =%s;\n", str.toString());
		file = changeText(file, startText, endText, newText);
		return file;
	}

	static String changeFailureRates(String file, double[][] failureRates) {

		String startText = "//&lt;failure-rates&gt;";
		String endText = "//&lt;/failure-rates&gt;";

		StringBuilder str = new StringBuilder("{");
		for (int i = 0; i < 3; i++) {
			str.append("{");
			for (int j = 0; j < 5; j++) {
				str.append(failureRates[i][j]);
				if (j != 4)
					str.append(",");
			}
			str.append("}");
			if (i != 2)
				str.append(",");
		}
		str.append("}");
		String newText = String.format("\nconst double sFailureRates[s_type][s_imp] =%s;\n", str.toString());
		file = changeText(file, startText, endText, newText);
		return file;
	}

	static String changeQueueLength(String file, int[][] queueLengths) {

		String startText = "//&lt;queue-length&gt;";
		String endText = "//&lt;/queue-length&gt;";

		StringBuilder str = new StringBuilder("{");
		for (int i = 0; i < 3; i++) {
			str.append("{");
			for (int j = 0; j < 5; j++) {
				str.append(queueLengths[i][j]);
				if (j != 4)
					str.append(",");
			}
			str.append("}");
			if (i != 2)
				str.append(",");
		}
		str.append("}");
		String newText = String.format("\nconst int queueLength[s_type][s_imp] =%s;\n", str.toString());
		file = changeText(file, startText, endText, newText);
		return file;
	}

	static String changeSimulationSimultaneousSensor(String file) {

		String startText = "//&lt;simulation-simultaneous-sensor&gt;";
		String endText = "//&lt;/simulation-simultaneous-sensor&gt;";

		Random rand = new Random();
		int upperbound = 9;

		StringBuilder str = new StringBuilder("{");

		for (int i = 0; i < 99; i++) {

			str.append(rand.nextInt(upperbound) + ",");
		}

		str.append(rand.nextInt(upperbound) + "};\n");

		String newText = String.format("\nconst int sss[MAX_SSS] = %s\n", str.toString());
		file = changeText(file, startText, endText, newText);
		return file;
	}

	static ExecutorService cachedPool = Executors.newCachedThreadPool();

	public static SMCConfiguration[] checkAllConfiguration() {

		int max = (int) Math.pow(IMPL, TYPES);
		SMCConfiguration[] configurations = new SMCConfiguration[max];

		int index = 0;
		for (int i = 1; i <= IMPL; i++) {
			for (int j = 1; j <= IMPL; j++) {
				for (int k = 1; k <= IMPL; k++) {
					configurations[index++] = checkConfiguration(i, j, k);
				}
			}
		}
		return configurations;
	}

	private static SMCConfiguration checkConfiguration(int addSensorFeature, int removeSensorFeature,
			int downloadSensorFeature) {
		if (!useFeatureTime) {
			return check2Requeriments(addSensorFeature, removeSensorFeature, downloadSensorFeature);
		}
		return check3Requeriments(addSensorFeature, removeSensorFeature, downloadSensorFeature);
	}

	private static SMCConfiguration check2Requeriments(int addSensorFeature, int removeSensorFeature,
			int downloadSensorFeature) {

		SMCConfiguration configuration = new SMCConfiguration();
		configuration.setFeatures(new int[] { addSensorFeature, removeSensorFeature, downloadSensorFeature });

		double cost = 0;
		double failureRate = 0;

		configuration.setpAdd(pAdd);
		try {
			readModels();
			modelFrate = changeCurrentFeatures(modelFrate,
					new int[] { addSensorFeature, removeSensorFeature, downloadSensorFeature });
			modelCost = changeCurrentFeatures(modelCost,
					new int[] { addSensorFeature, removeSensorFeature, downloadSensorFeature });

			Files.write(modelPathFrate, modelFrate.getBytes(Charset.defaultCharset()));
			Files.write(modelPathCost, modelCost.getBytes(Charset.defaultCharset()));

			LinkedList<ExecuteCommand> commands = new LinkedList<ExecuteCommand>();
			String command1 = getCommand(modelPathFrate.toString());
			String command2 = getCommand(modelPathCost.toString());

			commands.add(new ExecuteCommand(command1));
			commands.add(new ExecuteCommand(command2));

			List<Future<String>> results = cachedPool.invokeAll(commands);

			// Split result
			String[] formulaFR = results.get(0).get().split("Verifying formula ");
			String[] formulaCost = results.get(1).get().split("Verifying formula ");

			// parsing values and time
			R1.addAndGet(getCPUtime(formulaFR[1]));
			R2.addAndGet(getCPUtime(formulaCost[1]));

			failureRate = getSimulatedValue(formulaFR[1]);
			cost = getSimulatedValue(formulaCost[1]);

			configuration.setCost(cost);
			configuration.setFailureRate(failureRate);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return configuration;
	}

	private static SMCConfiguration check3Requeriments(int addSensorFeature, int removeSensorFeature,
			int downloadSensorFeature) {
		SMCConfiguration configuration = new SMCConfiguration();
		configuration.setFeatures(new int[] { addSensorFeature, removeSensorFeature, downloadSensorFeature });

		double cost = 0;
		double failureRate = 0;
		double featureTime = 0;

		configuration.setpAdd(pAdd);

		try {
			readModels();
			modelFrate = changeCurrentFeatures(modelFrate,
					new int[] { addSensorFeature, removeSensorFeature, downloadSensorFeature });
			modelCost = changeCurrentFeatures(modelCost,
					new int[] { addSensorFeature, removeSensorFeature, downloadSensorFeature });
			modelFeatureTime = changeCurrentFeatures(modelFeatureTime,
					new int[] { addSensorFeature, removeSensorFeature, downloadSensorFeature });

			Files.write(modelPathFrate, modelFrate.getBytes(Charset.defaultCharset()));
			Files.write(modelPathCost, modelCost.getBytes(Charset.defaultCharset()));
			Files.write(modelPathFeatureTime, modelFeatureTime.getBytes(Charset.defaultCharset()));

			LinkedList<ExecuteCommand> commands = new LinkedList<ExecuteCommand>();
			String command1 = getCommand(modelPathFrate.toString());
			String command2 = getCommand(modelPathCost.toString());
			String command3 = getCommand(modelPathFeatureTime.toString());

			commands.add(new ExecuteCommand(command1));
			commands.add(new ExecuteCommand(command2));
			commands.add(new ExecuteCommand(command3));

			List<Future<String>> results = cachedPool.invokeAll(commands);

			// Split result
			String[] formulaFR = results.get(0).get().split("Verifying formula ");
			String[] formulaCost = results.get(1).get().split("Verifying formula ");
			String[] formulaFeatureTime = results.get(2).get().split("Verifying formula ");

			// parsing values and time
			R1.addAndGet(getCPUtime(formulaFR[1]));
			R2.addAndGet(getCPUtime(formulaCost[1]));
			R3.addAndGet(getCPUtime(formulaFeatureTime[1]));

			failureRate = getSimulatedValue(formulaFR[1]);
			cost = getSimulatedValue(formulaCost[1]);
			featureTime = getSimulatedValue(formulaFeatureTime[1]);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		configuration.setCost(cost);
		configuration.setFailureRate(failureRate);
		configuration.setFeatureTime(featureTime);

		return configuration;
	}

	static AtomicLong R1 = new AtomicLong(), R2 = new AtomicLong(), R3 = new AtomicLong();

	private static void readModels() throws IOException {
		if (modelFrate == null || modelCost == null) {
			modelFrate = new String(Files.readAllBytes(modelPathFrate), Charset.defaultCharset());
			modelCost = new String(Files.readAllBytes(modelPathCost), Charset.defaultCharset());
			if (useFeatureTime)
				modelFeatureTime = new String(Files.readAllBytes(modelPathFeatureTime), Charset.defaultCharset());

			modelFrate = setSimulations(modelFrate);
			modelCost = setSimulations(modelCost);
			if (useFeatureTime)
				modelFeatureTime = setSimulations(modelFeatureTime);
		}
	}

	private static String setSimulations(String model) {
		String str = "simulate 1[";
		int start = model.lastIndexOf(str) + str.length();
		int end = model.indexOf("]", start);
		String s = model.substring(start, end);
		int simulations = relativeSEM == 5 ? simulationsSEM5 : simulationsSEM10;
		model = model.replace(s, "&lt;=" + simulations);
		return model;
	}

	public static void setInitialData(int p_Remove, int p_NoDownload, int p_Add, int p_Download,
			double[][] p_FailureRates) {
		pRemove = p_Remove;
		pNoDownload = p_NoDownload;
		pAdd = p_Add;
		pDownload = p_Download;
		failureRates = p_FailureRates;

		try {
			readModels();

			modelFrate = changeSimulationSimultaneousSensor(modelFrate);
			modelCost = changeSimulationSimultaneousSensor(modelCost);

			modelFrate = changeInvocationProbabilities(modelFrate, p_Remove, pNoDownload, pAdd, pDownload);
			modelCost = changeInvocationProbabilities(modelCost, p_Remove, pNoDownload, pAdd, pDownload);
			modelFrate = changeFailureRates(modelFrate, p_FailureRates);

			Files.write(modelPathFrate, modelFrate.getBytes(Charset.defaultCharset()));
			Files.write(modelPathCost, modelCost.getBytes(Charset.defaultCharset()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		R1.set(0);
		R2.set(0);

	}

	public static void setInitialData(int p_Remove, int p_NoDownload, int p_Add, int p_Download,
			double[][] p_FailureRates, int[][] queueLength) {
		setInitialData(p_Remove, p_NoDownload, p_Add, p_Download, p_FailureRates);
		try {

			modelFeatureTime = changeSimulationSimultaneousSensor(modelFeatureTime);

			modelFeatureTime = changeInvocationProbabilities(modelFeatureTime, p_Remove, pNoDownload, pAdd, pDownload);
			modelFeatureTime = changeQueueLength(modelFeatureTime, queueLength);
			Files.write(modelPathFeatureTime, modelFeatureTime.getBytes(Charset.defaultCharset()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		R3.set(0);
	}

	public static void calculatePropertiesR2() {

		// String modelPath = "/Users/caiza/Documents/model/sheCost.xml";
		// String modelPath = "/Users/caiza/Documents/model/sheTime.xml";
		String modelPath = "/Users/caiza/Documents/model/sheFR.xml";

		for (int i = 1; i <= 1000; i++) {
			// long start = System.currentTimeMillis();
			String result = executeCommand(getCommand(modelPath));
			// long end = System.currentTimeMillis();
			// Sytem.out.println(end-start);
			String[] formulas = result.split("Verifying formula ");

			// For Simulation
			System.out.format("%d, %f, %d, %d, %d\n", i, getSimulatedValue(formulas[1]), getCPUtime(formulas[1]),
					getResidentMen(formulas[1]), getStatesExplored(formulas[1]));
			// System.out.format("%f\n", getSimulatedValue(formulas[1]));
		}
	}

	// Scalability 2 : multiple concrete feature
	public static void checkMultipleConcreteFeatures() {
		for (int imp = 2; imp <= 20; imp++) {
			for (int count = 0; count < 30; count++)
				IMPL = imp;
			long start = System.currentTimeMillis();
			for (int i = 1; i <= IMPL; i++) {
				for (int j = 1; j <= IMPL; j++) {
					for (int k = 1; k <= IMPL; k++) {
						checkConfiguration(i, j, k);
					}
				}
			}
			long end = System.currentTimeMillis() - start;
			System.out.format("%d,%d,%d,%d,%d,%d\n", R1, R2, end, end - (R1.get() + R2.get()), imp, 125);
			R1.set(0);
			R2.set(0);
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < 20; i++) {
			SMCConfiguration[] configurations = checkAllConfiguration();
			System.out.println(R1.get() + R2.get() + R3.get());
			R1.set(0);
			R2.set(0);
			R3.set(0);
		}

	}

}

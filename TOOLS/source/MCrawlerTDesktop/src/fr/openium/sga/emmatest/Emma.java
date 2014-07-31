package fr.openium.sga.emmatest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import kit.Command.DeletCommand;
import kit.Command.PullCommand;
import kit.Config.Config;
import kit.Scenario.Scenario;
import kit.Scenario.ScenarioData;
import kit.Scenario.ScenarioGenerator;
import kit.Utils.SgUtils;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import fr.openium.JunitTestCasesGenerator;
import fr.openium.sga.ConfigApp;
import fr.openium.sga.Main;
import fr.openium.sga.ThreadSleeper;
import fr.openium.sga.Utils.ActivityCoverageUtils;
import fr.openium.sga.bissimulation.SgaGraph;
import fr.openium.sga.dot.model.Refinement;
import fr.openium.sga.reporter.ModelReporter;
import fr.openium.sga.result.CrawlResult;
import fr.openium.sga.strategy.AbstractStrategy;
import fr.openium.sga.strategy.StrategyFactory;
import fr.openium.specification.xml.StreamException;

/**
 * Launch a test project with Emma
 * 
 * -arv /Users/Stassia/Documents/Scen-genWorkSpace/sgd/TestData/testData.xml
 * coverage results and analyze them on runtime
 * 
 * 
 * @author Stassia
 * 
 */
public class Emma {
	public static final long TIME_LIMITE = 2000000;
	private static AbstractStrategy mcurrent_strategy;
	private static File path_file_directory;
	private static File model_directory;
	private static File out_directory;
	public static Long exec_time;
	public static Long device_exec_time = 0L;
	public static long init_time;

	/**
	 * help
	 */
	public static void help() {
		info("Please, enter arguments as follow [options][arguments]: \n -p: projectPath argument \n -tp test project path argument \n -tpackage test project package name argument \n -sdk sdk path \n -coverage minimum code coverage to reach \n -out output directory path -arv test data path (xml file) \n -strategy 0 (normal) or 1(fourmy) \n -thread the number of emulator in parallel ");

	}

	/**
	 * @param args
	 *            -p project under test
	 * 
	 *            -tp the test project
	 * 
	 *            -sdk sdk path
	 * @throws Exception
	 * 
	 */
	public static void main(String[] args) throws Exception {
		SgdEnvironnement env = init_environment_(args);

		if (env == null) {
			return;
		}

		if (!env.checkParameters()) {
			info("test finished due to unvalid parameters");
			System.exit(-1);
		}

		mcurrent_strategy = StrategyFactory.getNewStrategy(env);
		out_directory = new File(env.getOutDirectory());
		model_directory = new File(env.getScenarioDirectory());
		path_file_directory = new File(env.getPathDirectory());

		Long time = System.currentTimeMillis();
		init_time = time;
		mResult = mcurrent_strategy.getResult();
		if (ConfigApp.DEBUG) {
			System.out.println("Test execution time : "
					+ (System.currentTimeMillis() - time) / 1000 + " sec ");
		}
		exec_time = (System.currentTimeMillis() - time) / 1000;
		try {
			display_result();
		} catch (NullPointerException e) {
			System.err
					.println("No result to display, please check error on the device \n Please, check if the test package's name matches \n (2) SgdInstrumentationTestRunner may not be setted \n aut may not be installed \n generic class may not be created \n (5) a right version of sga.apk may not be installed ");
			// System.exit(-1);
		}

		// System.exit(-1);

		/**
		 * save model
		 */
		save_model();

		/**
		 * display each path
		 */

		// save_and_display_paths(true, true);

		/**
		 * vue de l'ensemble des chemin
		 */
		if (ConfigApp.DEBUG) {
			System.out.println("Test execution time : "
					+ (System.currentTimeMillis() - time) / 1000 + " sec ");
		}
		// display_all_bissimiled_path();

		if (ConfigApp.DEBUG) {
			System.out.println("Test execution time : "
					+ (System.currentTimeMillis() - time) / 1000 + " sec ");
		}

		save_and_display_paths(true, false);
		/**
		 * generate test cases for each path
		 */
		

		generateJunitModelValidation(env);
		display_recap(env);

		generateCrashTest(env,mResult.getScenarioData());

		if (ConfigApp.DEBUG) {
			System.out.println("end");
		}
		System.exit(-1);
	}
	public static void generateCrashTest(SgdEnvironnement env) throws CloneNotSupportedException
	{
		generateCrashTest(env,env.getModel());
	}
			
	public static ArrayList<String> generateCrashTest(SgdEnvironnement env,
			ScenarioData treeModel)
			throws CloneNotSupportedException {

		File outputFile = new File(env.getOutDirectory() + File.separator
				+ Config.JUNITDIRECTORY);
		if (!outputFile.exists()) {
			outputFile.mkdirs();
		}
		
		return generateCrashTest(env, outputFile, treeModel);

	}

	/**
	 */
	private static void generateJunitModelValidation(SgdEnvironnement env) {
		File outputFile = new File(env.getOutDirectory() + File.separator
				+ Config.JUNITDIRECTORY);
		if (!outputFile.exists()) {
			outputFile.mkdirs();
		}
		if (env.getLauncherActivityOfAppUnderTest() == null) {
			throw new NullPointerException("activityLauncher is not setted");
		}
		JunitTestCasesGenerator gen = new JunitTestCasesGenerator(null,
				outputFile);
		for (ScenarioData path : mResult.getPathList()) {
			gen.addPaths(path);
		}
		/**
		 * pack.cl#test_x
		 */
		HashSet<String> testName = gen.generate(env.getTestProjectPackage(),
				"ModelValidationTest", env.getLauncherActivityOfAppUnderTest(),
				outputFile);
		// "fr.openium.converterexample.MainActivity"
		info(" model validation is available in: " + outputFile.toString());
		info(" list of test: " + testName);
	}

	private static void display_recap(SgdEnvironnement env)
			throws FileNotFoundException, SAXException, IOException,
			StreamException, ParserConfigurationException,
			CloneNotSupportedException {
		info("Exec time: " + exec_time);
		// info("Device exec time: " + device_exec_time);

		if (mResult == null || mResult.getScenarioData() == null
				|| mResult.getScenarioData().getTransitions() == null) {
			info("Test number : mResult is Null");
			return;
		} else
			info("Test number : "
					+ mResult.getScenarioData().getTransitions().size());
		if (!mResult.getScenarioData().getTransitions().isEmpty()) {
			info("State number before biss: "
					+ mResult.getInigraph().getVertices().size());
			info("State number after biss: "
					+ mResult.getFinalGraph().getVertices().size());
		}

		ActivityCoverageUtils util = new ActivityCoverageUtils(
				env.getManifestfilePath(), env.getFinalModelPath());
		info("Activity coverage: " + util.getActivityCoverage());
		ModelReporter report = new ModelReporter(env, mResult, "" + exec_time);
		report.generate();
	}

	private static void display_result() {
		mResult.getInigraph().display();
		mResult.getFinalGraph().display();
	}

	@SuppressWarnings("unused")
	private static void display_all_bissimiled_path() {
		Refinement ref = new Refinement(mResult.getPathList());
		SgaGraph graph[] = ref.computeBissModel();
		graph[0].display();
		graph[1].display();
	}

	private static void save_and_display_paths(boolean display, boolean save) {
		if (!display && !save) {
			return;
		}

		if (!path_file_directory.exists()) {
			path_file_directory.mkdirs();
		}
		for (ScenarioData path : mResult.getPathList()) {
			if (display) {
				display(path);
			}
			if (save) {
				save_path(path);

			}
			/**
			 * save path list
			 */

		}

	}

	/**
	 * @param path
	 */
	private static void save_path(ScenarioData path) {
		if (path != null && path.getInitialState() != null) {
			new ScenarioGenerator(out_directory.getPath() + File.separator
					+ ConfigApp.PATH + "_").generateXml(path);
			savegeneric_file(new File(out_directory.getPath() + File.separator
					+ ConfigApp.PATH + "_"), path_file_directory, ".xml");
		}
	}

	/**
	 * @param path
	 */
	private static void display(ScenarioData path) {
		Refinement ref = new Refinement(path);
		SgaGraph graph = ref.generate_graphe(path);
		graph.display();
	}

	private static void save_model() {
		if (!model_directory.exists()) {
			model_directory.mkdirs();
		}
		if (mResult.getScenarioData() != null) {
			new ScenarioGenerator(out_directory.getPath()
					+ ConfigApp.OutXMLPath).generateXml(mResult
					.getScenarioData());
			savegeneric_file(new File(out_directory.getPath()
					+ ConfigApp.OUTPATH), model_directory, ".xml");
		}
	}

	public static SgdEnvironnement init_environment_(String[] args)
			throws FileNotFoundException {
		SgdEnvironnement env = null;
		Emma.info("ARGS LENGTH:" + args.length);
		if ((args.length - 1) > 8 && pair(args.length - 1)) {
			env = new SgdEnvironnement();
			for (int i = 1; i < args.length - 1;) {
				if (args[i].equals("-strategy")) {
					env.setStrategyType(args[i + 1]);
					info("strategy " + env.getStrategyType());
				}
				if (args[i].equals("-p")) {
					env.setProjectPath(args[i + 1]);
					info("Project Path " + env.getProjectPath());
				}
				if (args[i].equals("-tp")) {
					env.setTestProjectPath(args[i + 1]);
					info("Test Project Path " + env.getTestProjectPath());
				}
				if (args[i].equals("-tpackage")) {
					env.setTestProjectPackage(args[i + 1]);
					info("Test Project Path Package"
							+ env.getTestProjectPackage());
				}
				if (args[i].equals("-sdk")) {
					env.setTestSdk(args[i + 1]);
					info("sdk " + env.getSdkPath());
				}
				if (args[i].equals("-coverage")) {
					env.setCoverageLimit(Long.parseLong(args[i + 1]));
					info("coverage limitation " + env.getCoverageLimit());
				}
				if (args[i].equals("-arv")) {
					env.setAllTestDataPath(args[i + 1]);
					info("TestData path " + env.getAllTestDataPath());
				}
				if (args[i].equals("-trv")) {
					env.setTestedDataPath(args[i + 1]);
				}
				if (args[i].equals("-out")) {
					env.setOutDirectory(args[i + 1]);
					info("out directory" + env.getOutDirectory());
					env.setCoveragePath();
					info("coverage directory" + env.getCoveragePath());
				}
				/**
				 * ajout de l'emulateur
				 */
				if (args[i].equals("-emu")) {
					env.setDevice(args[i + 1]);
					info("emulateur" + env.getDevice());
				}

				if (args[i].equals("-pmd")) {
					env.setPmdDir(args[i + 1]);
					info("pmd" + env.getmPmd());
				}
				/**
				 * thread number
				 */
				if (args[i].equals("-thread")) {
					env.setThread_number(Integer.parseInt(args[i + 1]));
					info("thread number" + env.getThread_number());
				}

				// if (args[i].equals("-fourmy")) {
				// env.setFourmyStrategy((args[i + 1]));
				// info("fourmy strategy " + env.getFourmyStrategy());
				// }

				if (args[i].equals("-pApk")) {
					env.setProjectApk((args[i + 1]));
					info("Project apk ");
					info(env.getProjectApk().getPath());
				}

				if (args[i].equals("-pPackage")) {
					env.setProjectPackage((args[i + 1]));
					info("Project package " + env.getProjectPackape());
				}

				if (args[i].equals("-launcherActivity")) {
					env.setLauncherActivityOfAppUnderTest((args[i + 1]));
					info("launcherActivity "
							+ env.getLauncherActivityOfAppUnderTest());
				}

				if (args[i].equals("-maxEvent")) {
					env.setStressMaxEvent((args[i + 1]));
					info("max event " + env.getStressMaxEvent());
				}
				if (args[i].equals("-class")) {
					env.setTargetClass(args[i + 1]);
					info("Target test class" + env.getTargetClass());
				}

				if (args[i].equals("-stopError")) {
					env.setErrorIndicator(args[i + 1]);
					info("Error indicator " + env.getErrorIndicator());
				}

				if (args[i].equals("-model")) {
					info("Model path " + args[i + 1]);
					env.setModel(args[i + 1]);
					info("Model to handle " + env.getModel().toString());
				}

				if (args[i].equals("-cp")) {
					env.setCP(args[i + 1]);
					info("cp to commit " + env.getCp());
				}
				if (args[i].equals("-db")) {
					env.setDbName(args[i + 1]);
					info("db to commit " + env.getDbPath());
				}

				if (args[i].equals("-maxTime")) {
					env.setMaxTime(args[i + 1]);
					info("maxTime " + env.getMaxTime());
				}
				if (args[i].equals("-bruteDico")) {
					env.setDicoPath(args[i + 1]);
					info("Path to dico File" + env.getDicoPath());
				}
				i++;
			}
		} else {

			info("THE NUMBER OF REQUEST DOES NOT MATCH:");
			info("Your request:");
			for (int i = 0; i < args.length - 1; i++) {
				info(args[i]);
			}
			help();
			return null;
		}
		return env;
	}

	public static CrawlResult mResult;

	public static void savegeneric_file(File file, File directory) {
		savegeneric_file(file, directory, null);
	}

	private static boolean pair(int length) {
		return Main.pair(length);
	}

	protected static void refine_save_Scenario(String outDirectory) {
		File Outdirectory = new File(outDirectory);
		File ScenarioDirectory = new File(Outdirectory + ConfigApp.SCENARII);
		File[] scenarioDataFiles = ScenarioDirectory.listFiles();
		Refinement refinement_operation = new Refinement(scenarioDataFiles);
		SgaGraph[] result = refinement_operation.computeBissModel();
		result[1].display();
		result[0].display();
		/**
		 * enregistrer sous forme dot
		 */
		try {
			FileUtils.writeStringToFile(new File(Outdirectory
					+ ConfigApp.SCENARIO_REFINED_FILE), result[1].toDot(),
					Config.UTF8, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		savegeneric_file(new File(Outdirectory
				+ ConfigApp.SCENARIO_REFINED_FILE), new File(Outdirectory
				+ ConfigApp.DOT_DIRECTORY));

	}

	public static void info(Object logs) {
		if (logs instanceof List) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Iterator<Object> it = ((List) logs).iterator();
			if (!it.hasNext()) {
				return;
			}
			do {
				fr.openium.sga.Main.info(((String) it.next()));
			} while (it.hasNext());

		}
		fr.openium.sga.Main.info(logs.toString());

	}

	protected static void error(String string) {
		Main.errorLog(string);
	}

	/**
	 * set a maximum value
	 */
	public static boolean ERROR = false;

	public static AbstractStrategy getStrategy() {
		return mcurrent_strategy;
	}

	public static CrawlResult getResult() {
		return mResult;
	}

	public static void delete_File_onDevice(String valueInTestResults,
			SgdEnvironnement env, ThreadSleeper sleeper) {

		info("delete file :"
				+ new DeletCommand(env.getSdkPath() + File.separator
						+ ConfigApp.ADBPATH, env.getDevice(),
						ConfigApp.DEVICETESTRESULTS + "/" + valueInTestResults)
						.execute());
		sleeper.sleepMedium();
	}

	public static void pull(String valueInTestResults, SgdEnvironnement env) {
		pull(valueInTestResults, env, env.getOutDirectory());
	}

	/**
	 * 
	 * @param valueInTestResults
	 *            : path stored in ConfigApp.DEVICETESTRESULTS
	 * @param env
	 *            :
	 * @param outDirectory
	 *            : the destination directory
	 */
	public static void pull(String valueInTestResults, SgdEnvironnement env,
			String outDirectory) {
		pull(valueInTestResults, outDirectory, env.getAdb(), env.getDevice());
	}

	/**
	 * 
	 * @param valueInTestResults
	 * @param outDirectory
	 * @param adb
	 * @param device
	 */
	public static void pull(String valueInTestResults, String outDirectory,
			String adb, String device) {
		File out = new File(outDirectory);
		PullCommand pull = new PullCommand(adb, device,
				ConfigApp.DEVICETESTRESULTS + "/" + valueInTestResults,
				out.getPath());
		pull.execute();
		new ThreadSleeper().sleepMedium();
	}

	public static boolean limit_time_isReached(Long initTime, Long timLimit) {
		return (System.currentTimeMillis() - initTime) > timLimit;
	}

	public static void save_generic_ok(SgdEnvironnement envToCheck, File ok) {
		File okDirectory = new File(envToCheck.getOutDirectory()
				+ ConfigApp.OkDirectory);
		savegeneric_file(ok, okDirectory);

	}

	public static void save_genericTime(SgdEnvironnement envToCheck) {
		pull(ConfigApp.TIME, envToCheck);
		savegeneric_file(new File(envToCheck.getOutDirectory() + File.separator
				+ ConfigApp.TimeDirectory + File.separator + ConfigApp.TIME),
				new File(envToCheck.getOutDirectory() + File.separator
						+ ConfigApp.TimeDirectory));

	}

	public static void save_generic_rv_done(SgdEnvironnement envToCheck) {
		File rv_done_directory = new File(envToCheck.getAllTestDataPath())
				.getParentFile();
		File rv_done = new File(envToCheck.getOutDirectory() + ConfigApp.RVDONE);
		pull(ConfigApp.RVDONE, envToCheck);
		savegeneric_file(rv_done, rv_done_directory);

	}

	/**
	 * @param file
	 * @param mTree_directory
	 * @param extension
	 */
	public static void savegeneric_file(File file, File directory, String ext) {
		if (!file.exists()) {
			info(" File does not exist " + file.getPath());
			return;
		}
		if (!directory.exists()) {
			if (ConfigApp.DEBUG) {
				System.out.println("savegeneric_file");
				System.out.println("create Directory : " + directory.getPath());

			}
			directory.mkdirs();
		}
		try {
			if (directory.listFiles().length != 0) {

				int i = 0;
				do {
					if (new File(directory + File.separator + file.getName()
							+ i + ((ext == null) ? "" : ext)).exists()) {
						i++;
					} else {
						if (file.renameTo(new File(file.getAbsolutePath() + i
								+ ((ext == null) ? "" : ext)))) {
							file = new File(file.getAbsolutePath() + i
									+ ((ext == null) ? "" : ext));
						}
						break;
					}
				} while (true);

			}
			FileUtils.copyFileToDirectory(file, directory, true);
		} catch (IOException e) {
			throw new Error(" [error]: File " + file.getName()
					+ " is not saved, " + e.getMessage());

		} finally {
			info(" File " + file.getName() + " is available in: "
					+ directory.getPath());
			file.delete();
		}
	}

	/**
	 * @param string
	 * @param outDirectory
	 * @param ext
	 */
	public static void savegeneric_file(String fileNme, String outDirectory,
			String ext) {
		savegeneric_file(new File(fileNme), new File(outDirectory), ext);

	}

	public static void save_non_generic(File file, File directory) {
		if (!file.exists()) {
			info(" File does not exist " + file.getPath());
			return;
		}
		if (!directory.exists()) {
			directory.mkdirs();
		}
		try {
			FileUtils.copyFileToDirectory(file, directory, true);
		} catch (IOException e) {
			throw new Error(" [error]: Scenario is not saved, "
					+ e.getMessage());

		} finally {
			info(" File " + file.getName() + " vailable in: "
					+ directory.getPath());
			file.delete();
		}
	}

	public static String getTime() {

		return "" + (System.currentTimeMillis() - init_time);
	}

	public static ArrayList<String> generateCrashTest(SgdEnvironnement env,
			File outputFile, ScenarioData treeModel) throws CloneNotSupportedException {
		
		ArrayList<String> testName = new ArrayList<String>();
		if (treeModel == null) {
			return testName;
		}
		if (env.getLauncherActivityOfAppUnderTest() == null) {
			throw new NullPointerException("activityLauncher is not setted");
		}

		/**
		 * cas de test pour chaque crash
		 */

		HashSet<String> errorNumber = SgUtils.get_error_number(treeModel);
		if (errorNumber.isEmpty()) {
			info("No JUNIT for crash testing is generated");

			return testName;
		}

		Iterator<String> errors = errorNumber.iterator();
		if (errors.hasNext()) {
			String currentErrors;
			do {

				currentErrors = errors.next();

				/**
				 * add path
				 */
				ScenarioData path = SgUtils.getConcretePath(
						treeModel.getState(currentErrors, Scenario.DEST),
						treeModel);
				if (path == null || path.getTransitions() == null
						|| path.getTransitions().isEmpty()) {
					continue;
				}

				JunitTestCasesGenerator gen = new JunitTestCasesGenerator(null,
						outputFile);
				gen.addPaths(path);

				/**
				 * pack.cl#test_x
				 */
				testName.addAll(gen.generate(env.getTestProjectPackage(),
						"CrashTest" + currentErrors,
						env.getLauncherActivityOfAppUnderTest(), outputFile));

			} while (errors.hasNext());

		}
		info(" Crash test may be available in: " + outputFile.toString());
		info(" list of test case to reach error: " + testName);
		info(testName);
		return testName;
	}

}
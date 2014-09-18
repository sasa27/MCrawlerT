package kit.Config;

import java.io.File;

import android.annotation.SuppressLint;

@SuppressLint("SdCardPath")
public class Config {
	public static final String CURRENT_DIRECTORY = new File("")
			.getAbsolutePath();
	public static final String OkPath = "/OK";
	public static final String TimeDirectory = "/Time";
	public static final String TIME = "time";
	public static final String BRUTE_TIME = "brute_time";
	public static final String OkDirectory = "/OKdirectory";
	public static final String OutXMLPath = "/out.xml";
	public static final String SCENARII = "/Scenario";
	public static final String OUTPATH = "/out";
	public static final String ACTION = "sga.intent.action.LAUNCHTEST";
	public static final String ACTIVITYLAUNCHER = "fr.openium.sga/.MainActivity";
	public static final String SERVICELAUNCHER = "fr.openium.sga/.CoverageService";
	public static final String TESTRESULTS = "/testResults";
	@SuppressLint("SdCardPath")
	public static final String DEVICETESTRESULTS = "/mnt/sdcard/testResults";
	public static final String DEVICECOVERAGE = "/mnt/sdcard/coverage";
	public static final String COVERAGEPATH = "/bin/coverage.xml";
	public static final String COVERAGEXML = "coverage.xml";
	public static final String BUILDXML = "/build.xml";
	public static final String RV = "/testResults/rv";
	public static final String TESTDATA = "/TestData";
	public static final String TESTDATA_ALL_RV = "/TestData/all_rv";
	public static final String TESTDATA_TESTED_RV = "/TestData/tested_rv";
	public static final String TESTDATA_TO_PUSH = "/TestData/rv";
	public static final String EMMAJAR = "emma.jar";
	public static final String ADBPATH = "platform-tools/adb";
	public static final String EMMAJARPATH = "/tools/lib/emma.jar";
	public static final String JAVA = "java";
	public static final String JAVA_CP = "java -cp ";
	public static final String EMMA = " emma";
	public static final String COMA = ",";
	public static final String EMMAXMLREPORT = EMMA
			+ " report -r xml,html -in ";
	public static final String COVERAGE = "coverage";
	public static final String DEVICE_COVERAGE_DIRECTORY = DEVICETESTRESULTS
			+ File.separator + COVERAGE;
	public static final String DESKTOP_COVERAGE_DIRECTORY = File.separator
			+ COVERAGE;
	public static final String COVERAGEEM = "bin/coverage.em";
	public static final String TESTDATA_Directory = "/TestData";
	public static final String RVDONE = "/rv_done";
	public static final String UTF8 = "UTF-8";
	public static final String TESTDATA_XML = "testData.xml";
	public static final String DOT = "dot";
	public static final String XMLEXTENSION = "xml";
	public static final boolean DEBUG = true;
	public static final String DOT_EXEC = "//opt//local//bin//dot";
	public static final String SMS_OUT_URI = "content://sms/inbox";
	public static final String SMS_SENT_URI = "content://sms/sent";
	public static final String SMS_IN_URI = "content://sms/out";
	public static final String SMS_OUTBOX_URI = "content://sms/outbox";
	public static final String SMS_URI = "content://sms";
	public static final String MENU = "menu";
	public static final boolean TAKE_ACCOUNT_TEXTVIEWVALUE = false;
	public static final String CRASH_FILE = "crash.xml";
	public static final String JUNITDIRECTORY = "Junit";
	public static final String EVENTS = "events";
	public static final String EVENTS_DELIMITERS = "*";
	public static String ANT_EMMA_COMMAND = "clean emma debug install";
	public static String RED_WIDGET = "red";
	public static String error = "error";
	public static String EXECUTED_EVENTS = "events_done";
	public static final String CP_LIST_FILE = "cp";
	public static final String DB = "db";
	public static final String TEST_TABLE = "TestTable";
	public static final String TR_OUT = "tr_out";
	public static final String ROBOTIUM_SCREENSHOTS = "Robotium-ScreenShots";
	public static final String INTENT_XML = "intent.xml" + "";

}
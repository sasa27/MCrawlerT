package org.totschnig.myexpenses.test;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.MyExpenses;
import org.totschnig.myexpenses.R;

import com.jayway.android.robotium.solo.Solo;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.ListAdapter;

/**
 * For the moment just an experiment in how to test the UI
 * http://robotium.googlecode.com/files/robotium-solo-3.1.jar
 * @author Michael Totschnig
 *
 */
public class MyExpensesTest extends
  ActivityInstrumentationTestCase2 <MyExpenses> {
  private MyExpenses mActivity;
  private ListAdapter mAdapter;
  private Instrumentation mInstrumentation = null;
  private Solo solo;
  private SharedPreferences settings;
  private static final String TEST_ID = "functest";

  

  public MyExpensesTest() {
    super("org.totschnig.myexpenses",MyExpenses.class);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MyApplication app = (MyApplication) getInstrumentation().getTargetContext().getApplicationContext();
    settings = app.getSharedPreferences(TEST_ID,Context.MODE_PRIVATE);
    app.setSettings(settings);
    app.setDatabaseName(TEST_ID);
    setActivityInitialTouchMode(false); 
    mActivity = getActivity();
    mInstrumentation = getInstrumentation();
  } // end of setUp() method definition
  
  /**
   * currently works only if locale is en
   * @throws Exception
   */
  public void testMainScreen() throws Exception {
    mAdapter = mActivity.getListAdapter();
    solo = new Solo(mInstrumentation, getActivity());
    //Help dialog opened
    assertTrue(solo.searchText(mActivity.getString(R.string.menu_help)));
    solo.clickOnButton(mActivity.getString(R.string.close));
    assertTrue(mAdapter != null);
    assertEquals(0,mAdapter.getCount());
    //reset should not be visible
    //getInstrumentation().waitForIdleSync();
    solo.sendKey(Solo.MENU);
    assertFalse(solo.searchText(mActivity.getString(R.string.menu_reset)));
    //mInstrumentation.waitForIdleSync();
    if (!mInstrumentation.invokeMenuActionSync(mActivity, MyExpenses.INSERT_TA_ID, 0))
      throw new Exception();
    solo.enterText((EditText) solo.getView(R.id.Amount),"123.45");
    solo.clickOnButton(mActivity.getString(R.string.done));
    assertTrue(solo.searchText("123.45"));
    //since we have now a transaction, reset should be visible
    solo.sendKey(Solo.MENU);
    assertTrue(solo.searchText(mActivity.getString(R.string.menu_reset)));
    //mInstrumentation.waitForIdleSync();
    if (!mInstrumentation.invokeMenuActionSync(mActivity, MyExpenses.SELECT_ACCOUNT_ID, 0))
      throw new Exception();
    //mInstrumentation.waitForIdleSync();
    //in order to use invokeMenuActionSync here, I'd need to know how to get at the activity we arein
    //if (!mInstrumentation.invokeMenuActionSync(?, SelectAccount.INSERT_ACCOUNT_ID, 0))
    //  throw new Exception();
    solo.pressMenuItem(0);
    solo.enterText((EditText) solo.getView(R.id.Label),"Testing account");
    solo.enterText((EditText) solo.getView(R.id.Description),"Created with Robotium");
    solo.enterText((EditText) solo.getView(R.id.Opening_balance),"456.59");
    solo.clickOnButton(mActivity.getString(R.string.done));
    assertTrue(solo.searchText("456.59"));
    solo.goBack();
    //since we have now two accounts transfer should be visible
    solo.sendKey(Solo.MENU);
    assertTrue(solo.searchText(mActivity.getString(R.string.menu_insert_transfer)));
//    getInstrumentation().waitForIdleSync();
//    assertTrue(solo.searchText(mActivity.getString(R.string.warning_reset_account)));
  }
  protected void tearDown() throws Exception {
    solo.finishOpenedActivities();
    Editor editor = settings.edit();
    editor.clear();
    editor.commit();
    mActivity.deleteDatabase(TEST_ID);
  }
}

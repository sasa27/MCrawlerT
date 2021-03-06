package fr.openium.function;

import java.util.ArrayList;
import java.util.HashMap;

import kit.TestRunner.ListViewElement;
import kit.TestRunner.MenuView;
import kit.TestRunner.SgdView;
import kit.TestRunner.SgdViewFactory;
import kit.Utils.SgUtils;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.robotium.solo.Solo;

import fr.openium.variable.MCrawlerVariable.Action;
import fr.openium.variable.MCrawlerVariable.State;
import fr.openium.variable.MCrawlerVariable.Widget;

public class MCrawlerFunctions {
	private static Solo mSolo;
	private static Context mContext;

	public MCrawlerFunctions() {
	}

	/**
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean isEqualState(State s1, State s2) {
		return SgUtils.isEqualState(s1, s2);
	}

	private static void test_event(HashMap<String, String> event) {
		ArrayList<View> allWidgetInCurrentActivity = mSolo.getCurrentViews();
		if (event.isEmpty()) {
			return;
		}
		String name = event.keySet().iterator().next();
		Log.d("name of widget", name);
		for (View v : allWidgetInCurrentActivity) {
			SgdView view_to_handle = SgdViewFactory.createView(mContext, mSolo.getCurrentActivity(), v, null,
					mSolo);

			/**
			 * case of menu
			 */
			if (SgUtils.isMenu(v) && name.equals("" + (MenuView.class.cast(v)).getIndex())) {
				view_to_handle.performAction(false);
				break;
			}
			/**
			 * case list Element
			 */
			if (SgUtils.isListElementView(v) && name.equals((ListViewElement.class.cast(v)).getIndex())) {
				view_to_handle.performAction(false);
				break;
			}
			String ressource_name = null;
			try {
				ressource_name = mContext.getResources().getResourceName(v.getId());
			} catch (Exception ex) {
				continue;
			}
			if (name.equals(ressource_name)) {
				Log.d("widget Matched", name);
				view_to_handle.performAction(false);
				break;
			}
		}
	}

	/**
	 * @param l
	 * @param a1
	 * @param a2
	 */
	public static void performAction(Context ctx, Solo solo, long l, Action... act) {
		mContext = ctx;
		mSolo = solo;
		fillEditext(act);
		for (Action act1 : act) {
			HashMap<String, String> event1 = SgUtils.get_event(act1);
			test_event(event1);
		}

	}

	/**
	 * @param act
	 */
	private static void fillEditext(Action[] act) {
		HashMap<String, String> seq = new HashMap<String, String>();
		for (Action action : act) {
			Widget wig = (Widget) action.getWidget();
			if (wig.getType().equalsIgnoreCase(EditText.class.getSimpleName())) {
				seq.put(wig.getName(), wig.getValue());
			}
		}
		fillEditTextWithValues(seq);
	}

	private static void fillEditTextWithValues(HashMap<String, String> sequence) {
		ArrayList<EditText> edits = mSolo.getCurrentViews(EditText.class);
		for (EditText edit : edits) {
			String name = mContext.getResources().getResourceName(edit.getId());
			String value_to_insert = sequence.get(name);
			mSolo.clearEditText(edit);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mSolo.enterText(edit, value_to_insert);
		}

	}
}

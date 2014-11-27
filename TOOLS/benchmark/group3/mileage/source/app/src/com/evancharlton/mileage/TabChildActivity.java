package com.evancharlton.mileage;

import android.app.Activity;
import android.widget.Spinner;

public class TabChildActivity extends Activity {

	protected void setVehicleSelection(Spinner spinner) {
		Mileage parent = (Mileage) getParent();
		if (parent != null) {
			int position = parent.getSelectedVehicleIndex();
			if (position >= 0 && position < spinner.getCount()) {
				spinner.setSelection(parent.getSelectedVehicleIndex());
			}
		}
	}

	protected void updateVehicleSelection(int position) {
		Mileage parent = (Mileage) getParent();
		if (parent != null) {
			parent.setSelectedVehicleIndex(position);
		}
	}
}

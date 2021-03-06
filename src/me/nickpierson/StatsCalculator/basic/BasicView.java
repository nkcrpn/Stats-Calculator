package me.nickpierson.StatsCalculator.basic;

import java.util.HashMap;

import me.nickpierson.StatsCalculator.utils.Constants;
import me.nickpierson.StatsCalculator.utils.DefaultAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.nickpierson.me.StatsCalculator.R;
import com.thecellutioncenter.mvplib.DataActionHandler;

public abstract class BasicView extends DataActionHandler {

	public enum Types {
		DONE_PRESSED, EDITTEXT_CLICKED, MENU_SAVE, SAVE_LIST, LOAD_LIST, MENU_LOAD_OR_DELETE, DELETE_LIST, MENU_REFERENCE;
	}

	public enum Keys {
		LIST_NAME;
	}

	protected RelativeLayout view;
	protected FrameLayout flFrame;
	protected ListView lvResults;
	protected TableLayout tlKeypad;
	protected EditText etInput;
	protected Activity activity;
	protected DefaultAdapter resultsAdapter;

	public BasicView(Activity activity, DefaultAdapter adapter) {
		this.activity = activity;
		view = (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.basic, null);
		tlKeypad = (TableLayout) LayoutInflater.from(activity).inflate(R.layout.keypad, null);
		flFrame = (FrameLayout) view.findViewById(R.id.basic_flContent);
		resultsAdapter = adapter;
		etInput = (EditText) view.findViewById(R.id.basic_etInput);

		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			etInput.setMaxLines(2);
		}

		etInput.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				event(Types.EDITTEXT_CLICKED);
				return false;
			}
		});
	}

	public void showResults(HashMap<String, String> results) {
		resultsAdapter.setResults(results);
		resultsAdapter.notifyDataSetChanged();
		showResults();
	}

	public void showKeypad() {
		flFrame.removeAllViews();
		flFrame.addView(tlKeypad);
	}

	public void showToast(String message) {
		Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
	}

	public void showErrorToast(int errorItem) {
		Toast.makeText(activity, String.format(Constants.DESCRIPTIVE_NUMBER_ERROR, errorItem), Toast.LENGTH_SHORT).show();
	}

	public void selectInput(String string) {
		int startIndex = etInput.getText().toString().indexOf(string);

		/* Just in case */
		if (startIndex >= 0) {
			etInput.setSelection(startIndex, startIndex + string.length());
		}
	}

	@SuppressLint("NewApi")
	public void showSaveListPopup() {
		AlertDialog.Builder alertBuilder;
		View alertView;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			alertBuilder = new AlertDialog.Builder(activity, getDialogTheme());
			alertView = LayoutInflater.from(activity).inflate(R.layout.save_list_dialog, null);
		} else {
			alertBuilder = new AlertDialog.Builder(activity);
			alertView = LayoutInflater.from(activity).inflate(R.layout.save_list_dialog_old, null);
		}

		final EditText etName = (EditText) alertView.findViewById(R.id.save_list_etListName);

		alertBuilder.setView(alertView);
		alertBuilder.setPositiveButton(R.string.save, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				eventWithName(Types.SAVE_LIST, etName.getText().toString());
			}
		});
		alertBuilder.setNegativeButton(R.string.cancel, null);

		alertBuilder.show();
	}

	@SuppressLint("NewApi")
	public void showLoadListPopup(final String[] savedLists) {
		AlertDialog.Builder alertBuilder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			alertBuilder = new AlertDialog.Builder(activity, getDialogTheme());
		} else {
			alertBuilder = new AlertDialog.Builder(activity);
		}

		alertBuilder.setSingleChoiceItems(savedLists, 0, null);
		alertBuilder.setPositiveButton(R.string.load, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ListView lv = ((AlertDialog) dialog).getListView();
				String name = (String) lv.getAdapter().getItem(lv.getCheckedItemPosition());
				eventWithName(Types.LOAD_LIST, name);
			}
		});
		alertBuilder.setNegativeButton(R.string.delete, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ListView lv = ((AlertDialog) dialog).getListView();
				String name = (String) lv.getAdapter().getItem(lv.getCheckedItemPosition());
				eventWithName(Types.DELETE_LIST, name);
			}
		});
		alertBuilder.setNeutralButton(R.string.cancel, null);

		alertBuilder.show();
	}

	private void eventWithName(Types type, String name) {
		HashMap<Enum<?>, String> result = new HashMap<Enum<?>, String>();
		result.put(Keys.LIST_NAME, name);
		dataEvent(type, result);
	}

	public void menuSaveList() {
		event(Types.MENU_SAVE);
	}

	public void menuLoadList() {
		event(Types.MENU_LOAD_OR_DELETE);
	}

	public void donePress() {
		event(Types.DONE_PRESSED);
	}

	public boolean isKeyPadVisible() {
		return tlKeypad.isShown();
	}

	public View getView() {
		return view;
	}

	public void setScrollPosition(int position) {
		lvResults.setSelectionFromTop(position, 0);
	}

	public int getScrollPosition() {
		return lvResults.getFirstVisiblePosition();
	}

	public String getInput() {
		return etInput.getText().toString();
	}

	public void setInputText(String list) {
		etInput.setText(list);
	}

	public HashMap<String, String> getResults() {
		return resultsAdapter.getResults();
	}

	public abstract void showResults();

	public abstract int getDialogTheme();
}
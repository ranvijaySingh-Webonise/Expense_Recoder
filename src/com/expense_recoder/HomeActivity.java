package com.expense_recoder;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.expense_recoder.database.DataBaseHelper;
import com.expense_recoder.database.DatabaseOperation;
import com.expense_recoder.interfaces.HorizontalScrollViewListener;
import com.expense_recoder.interfaces.ScrollViewListener;
import com.expense_recoder.util.Constants;
import com.expense_recoder.util.LOG;

public class HomeActivity extends Activity implements OnEditorActionListener,OnClickListener,ScrollViewListener,HorizontalScrollViewListener { 

	private static String strTitle = "No title";
	private static int nameId = 0;
	private static int eventId = 0;
	private EditText editTextTitle;
	private TextView textViewTitle;
	private RecordManager mRecordManager;
	protected String strDialogString;
	private LinearLayout linearLayoutName;
	private LinearLayout linearLayoutEvent;
	private DatabaseOperation mDataOperation;
	private ObservableScrollView observableScrollViewEvent = null;
	private ObservableScrollView observableScrollViewData = null;
	private ObservableHorizontalScrollView observableHorizontalScrollViewName = null;
	private ObservableHorizontalScrollView observableHorizontalScrollViewData = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		initializeComponents();
	}

	private void initializeComponents() {
		nameId = 0;
		eventId = 0;
		Constants.IS_ADDING_FIRST_TIME = true;
		observableScrollViewEvent = (ObservableScrollView)findViewById(R.id.observableScrollViewEvent);
		observableScrollViewData= (ObservableScrollView)findViewById(R.id.observableScrollViewData);
		observableScrollViewEvent.setScrollViewListener(this);
		observableScrollViewData.setScrollViewListener(this);
		observableHorizontalScrollViewName = (ObservableHorizontalScrollView) findViewById(R.id.horizontalScrollViewName);
		observableHorizontalScrollViewData=(ObservableHorizontalScrollView) findViewById(R.id.horizontalScrollViewData);
		observableHorizontalScrollViewName.setHorizontalScrollViewListener(this);
		observableHorizontalScrollViewData.setHorizontalScrollViewListener(this);
		
		editTextTitle = (EditText) findViewById(R.id.editTextTitle);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		editTextTitle.setOnEditorActionListener(this);
		textViewTitle.setOnClickListener(this);
		mRecordManager = new RecordManager(this);
		mDataOperation = new DatabaseOperation(this);
		mDataOperation.close();
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
		strTitle = v.getText().toString();
		if (!strTitle.equals("")) {
			setTitleAsTextView(strTitle);
		} else {
			strTitle = "No title";
			setTitleAsTextView(strTitle);
		}
		return true;
	}

	private void setTitleAsTextView(String strTitle) {
		textViewTitle.setText(strTitle);
		textViewTitle.setVisibility(View.VISIBLE);
		editTextTitle.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textViewTitle:
			textViewTitle.setVisibility(View.GONE);
			editTextTitle.setVisibility(View.VISIBLE);
			editTextTitle.setHint(strTitle);
			break;
		default:
			showDialogBox(v);
			break;
		}
	}

	private void showDialogBox(final View view) {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_box);
		dialog.setTitle(getResources().getString(R.string.app_name));
		EditText editTextDialog = (EditText)dialog.findViewById(R.id.editTextDialog);
		editTextDialog.setHint(((TextView)view).getText());
		Button buttonDialog = (Button)dialog.findViewById(R.id.buttonDialogOk);
		buttonDialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText editTextDialog = (EditText)dialog.findViewById(R.id.editTextDialog);
				String strEditText = editTextDialog.getText().toString();
				if(strEditText.equals("")) {
				} else { 
					((TextView)view).setText(editTextDialog.getText().toString());
				}
				dialog.dismiss();
			}
		});
		dialog.show();		
	}

	public void onClickAddName(View view) {
		LOG.i("clicked", "add name " + nameId);
		linearLayoutName = (LinearLayout)findViewById(R.id.linearLayoutName);
		linearLayoutName.addView(getTextView(nameId,Constants.NAME));
		nameId++;
		mRecordManager.addEntry(eventId, nameId);
	}
	
	public void onClickAddEvent(View view) {
		LOG.i("clicked", "add event " + eventId);
		linearLayoutEvent = (LinearLayout)findViewById(R.id.linearLayoutEvent);
		linearLayoutEvent.addView(getTextView(eventId, Constants.EVENT));
		eventId++;
		mRecordManager.addEntry(eventId, nameId);
	}

	private TextView getTextView(int id, String name) {
		TextView textView = new TextView(this);
		textView.setId(id);
		textView.setGravity(1);
		textView.setPadding(10, 8, 10, 8);
		textView.setTextSize(Constants.TEXT_SIZE_GENERAL);
		textView.setHint(name);
		textView.setOnClickListener(this);
		return textView;
	}

	public void onClickSubstractName(View view) {
		if (nameId > 0) {
			nameId--;
			deleteLastName(nameId); 
			LOG.i("clicked", "substract name " + nameId);
			mRecordManager.deleteEntry(eventId, nameId);
		}
	}

	public void deleteLastName(int name_id) {
		TextView textView =(TextView)linearLayoutName.getChildAt(name_id);
		linearLayoutName.removeView(textView);
	}

	public void onClickSubstractEvent(View view) {
		if (eventId > 0) {
			eventId--;
			mRecordManager.deleteEntry(eventId, nameId);
			deleteLastEvent(eventId);
			LOG.i("clicked", "substract event " + eventId);
		}
	}

	public void deleteLastEvent(int event_id) { 
		TextView textView =(TextView)linearLayoutEvent.getChildAt(event_id); 
		linearLayoutEvent.removeView(textView);
	}
	
	public void onClickSave(View view) {
		if(nameId > 0 && eventId > 0) {
			if(strTitle.equals("") || strTitle.equals("No title")) {
				Toast.makeText(this, getResources().getString(R.string.give_occasion_name),Toast.LENGTH_LONG).show();
			} else {
				mDataOperation.open();
				String strTableName = strTitle.replace(" ", "_");
				if(mDataOperation.isTableAlreadyCreated(strTableName)) {
					LOG.i("table check", "table already exists");
					clearAllTheDataWithTripName(strTableName);
				}
				Toast.makeText(this, "Saving data.", Toast.LENGTH_SHORT).show();
				for (int i = 0; i < eventId; i++) {
					String [] strArrayOccasion = getOccasionRow(strTableName,i);
					mDataOperation.insertIntoTableOccasion(strArrayOccasion);
				}
				mDataOperation.close();
			}
		} else {
			Toast.makeText(this, "There is not data to save.", Toast.LENGTH_SHORT).show();
		}
	}

	private void clearAllTheDataWithTripName(String strTableName) {
		String [] strArrayReceivedData = mDataOperation.getAllRowsWithTripName(DataBaseHelper.DATABASE_TABLE_OCCASION,strTableName);
		for (int i = 0; i < strArrayReceivedData.length; i++) {
			mDataOperation.deleteAllRowWithTripId(strArrayReceivedData[i]);
			LOG.v("delete","deleted all record with trip id: "+strArrayReceivedData[i]);
		}
	}

	private String[] getOccasionRow(String strTripName, int row) {
		String [] strArrayOccasion = new String[4];
		TextView mTextView = (TextView) linearLayoutEvent.getChildAt(row);
		String strEventName = mTextView.getText().toString();
		int intLastId = mDataOperation.getLastIdFromOccasion();
		Log.v("last index is : ",intLastId+"");
		
		strArrayOccasion[0] = strTripName+intLastId;
		strArrayOccasion[1] = strTripName;
		if (!strEventName.equals("")) {
			strArrayOccasion[2] = strEventName+intLastId;
		}
		strArrayOccasion[3] = strEventName;
		for (int column = 0; column < nameId; column++) {
			mDataOperation.insertIntoTableRecord(getRecordRow(strArrayOccasion,row, column));
		}
		return strArrayOccasion;
	}

	private String[] getRecordRow(String[] strArrayOccasion,int row, int column) {
		TextView mTextView = (TextView)linearLayoutName.getChildAt(column);
		String [] strArrayRecord = new String[4];
		strArrayRecord[0] = strArrayOccasion[0];
		strArrayRecord[1] = strArrayOccasion[2];		
		strArrayRecord[2] = mTextView.getText().toString();
		strArrayRecord[3] = mRecordManager.getElementAt(row,column);
		return strArrayRecord;
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}


	@Override
	public void onScrollChanged(ObservableHorizontalScrollView scrollView, int x, int y,int oldX, int oldY) {
		if (scrollView == observableHorizontalScrollViewName) {
			observableHorizontalScrollViewData.scrollTo(x, y);
		} else if(scrollView == observableHorizontalScrollViewData){
			observableHorizontalScrollViewName.scrollTo(x, y);
		}
	}
	
	@Override
	public void onScrollChanged(ObservableScrollView scrollView, int x, int y,int oldX, int oldY) {
		if (scrollView == observableScrollViewEvent) {
			observableScrollViewData.scrollTo(x, y);
		} else if(scrollView == observableScrollViewData){
			observableScrollViewEvent.scrollTo(x, y);
		}
	}
}

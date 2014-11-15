package com.sensoro.qqsearch;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener, TextWatcher, PopupWindow.OnDismissListener {
	private Context context;
	private ListView listView;
	private View headerView;
	private TextView searchTextView;

	// show and hide
	private RelativeLayout mainLayout;
	private RelativeLayout titleBarLayout;
	private int moveHeight;
	private int statusBarHeight;

	// search popupwindow
	private PopupWindow popupWindow;
	private View searchView;
	private EditText searchEditText;
	private TextView cancelTextView;
	private TextView closeTextView;
	private ListView filterListView;
	private View alphaView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initCtrl();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.search_tv:
			showSearchBar(view);
			break;
		case R.id.popup_window_tv_close:
			searchEditText.setText("");
			break;
		case R.id.popup_window_tv_cancel:
			dismissPopupWindow();
			break;
		case R.id.popup_window_v_alpha:
			dismissPopupWindow();
			break;

		default:
			break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> viewGroup, View view, int position, long arg3) {
		switch (viewGroup.getId()) {
		case R.id.lv:
			if (position == 0) {
				showSearchBar(viewGroup);
			}
			break;
		case R.id.popup_window_lv:
			Toast.makeText(context, "" + position, Toast.LENGTH_LONG).show();
			break;

		default:
			break;
		}
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		if (arg0.toString().equals("")) {
			alphaView.setVisibility(View.VISIBLE);
			filterListView.setVisibility(View.GONE);
		} else {
			alphaView.setVisibility(View.GONE);
			filterListView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void afterTextChanged(Editable arg0) {

	}

	@Override
	public void onDismiss() {
		resetUI();
	}

	private void initCtrl() {
		context = this;
		listView = (ListView) findViewById(R.id.lv);
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		headerView = layoutInflater.inflate(R.layout.header_listview_search, null);
		listView.addHeaderView(headerView);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n" }));
		listView.setOnItemClickListener(this);
		searchTextView = (TextView) headerView.findViewById(R.id.search_tv);
		searchTextView.setOnClickListener(this);

		mainLayout = (RelativeLayout) findViewById(R.id.main);
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar_layout);

		LayoutInflater popupWindowInflater = LayoutInflater.from(context);
		searchView = popupWindowInflater.inflate(R.layout.popup_window_search, null);
		searchEditText = (EditText) searchView.findViewById(R.id.popup_window_et_search);
		searchEditText.setFocusable(true);
		searchEditText.addTextChangedListener(this);
		cancelTextView = (TextView) searchView.findViewById(R.id.popup_window_tv_cancel);
		cancelTextView.setOnClickListener(MainActivity.this);
		closeTextView = (TextView) searchView.findViewById(R.id.popup_window_tv_close);
		closeTextView.setOnClickListener(MainActivity.this);
		Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
		closeTextView.setTypeface(font);
		filterListView = (ListView) searchView.findViewById(R.id.popup_window_lv);
		filterListView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14" }));
		filterListView.setOnItemClickListener(MainActivity.this);
		alphaView = searchView.findViewById(R.id.popup_window_v_alpha);
		alphaView.setOnClickListener(MainActivity.this);

		popupWindow = new PopupWindow(searchView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable()); // For BackPressed can be used for popupwindow.
		popupWindow.setOnDismissListener(this);
	}

	private void getStatusBarHeight() {
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
	}

	private void showSearchBar(final View v) {
		getStatusBarHeight();
		moveHeight = titleBarLayout.getHeight();
		Animation translateAnimation = new TranslateAnimation(0, 0, 0, -moveHeight);
		translateAnimation.setDuration(300);
		mainLayout.startAnimation(translateAnimation);
		translateAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
				mainLayout.setAnimation(anim);
				titleBarLayout.setVisibility(View.GONE);
				titleBarLayout.setPadding(0, -moveHeight, 0, 0);

				popupWindow.showAtLocation(mainLayout, Gravity.CLIP_VERTICAL, 0, statusBarHeight);
				// openKeyboard();
			}
		});

	}

	private void openKeyboard() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 0);
	}

	private void dismissPopupWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}

	private void resetUI() {
		titleBarLayout.setPadding(0, 0, 0, 0);
		titleBarLayout.setVisibility(View.VISIBLE);
		Animation translateAnimation = new TranslateAnimation(0, 0, -moveHeight, 0);
		translateAnimation.setDuration(300);
		mainLayout.startAnimation(translateAnimation);
		translateAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
				mainLayout.setAnimation(anim);
				// titleBarLayout.setPadding(0, 0, 0, 0);

			}
		});
	}

}

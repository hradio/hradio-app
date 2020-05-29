package lmu.hradio.hradioshowcase.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Slide;
import androidx.transition.Transition;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.hradio.prudac.model.PrivacyParameters;
import com.hradio.prudac.model.Report;
import com.hradio.prudac.model.Survey;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.omri.radio.Radio;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDabEdi;
import org.omri.radioservice.RadioServiceType;
import org.omri.tuner.TunerType;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.hradio.httprequestwrapper.dtos.recommendation.Recommender;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.timeshiftplayer.SkipItem;
import eu.hradio.timeshiftplayer.TimeshiftPlayer;
import lmu.hradio.hradioshowcase.BuildConfig;
import lmu.hradio.hradioshowcase.R;
import lmu.hradio.hradioshowcase.database.Database;
import lmu.hradio.hradioshowcase.error.GeneralError;
import lmu.hradio.hradioshowcase.listener.OnEPGUpdateListener;
import lmu.hradio.hradioshowcase.listener.OnManagerErrorListener;
import lmu.hradio.hradioshowcase.listener.OnTunerScanListener;
import lmu.hradio.hradioshowcase.listener.PlayBackDelegate;
import lmu.hradio.hradioshowcase.listener.PlayBackListener;
import lmu.hradio.hradioshowcase.listener.PlayRadioServiceListener;
import lmu.hradio.hradioshowcase.listener.PodcastSearchResultListener;
import lmu.hradio.hradioshowcase.listener.State;
import lmu.hradio.hradioshowcase.listener.TrackLikeService;
import lmu.hradio.hradioshowcase.manager.LocationReader;
import lmu.hradio.hradioshowcase.manager.MainAppController;
import lmu.hradio.hradioshowcase.manager.prudac.PrudacRestClient;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionPlayerTyp;
import lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProviderType;
import lmu.hradio.hradioshowcase.model.state.PlayBackState;
import lmu.hradio.hradioshowcase.model.substitiution.SubstitutionItem;
import lmu.hradio.hradioshowcase.model.view.ImageData;
import lmu.hradio.hradioshowcase.model.view.RadioServiceViewModel;
import lmu.hradio.hradioshowcase.model.view.TextData;
import lmu.hradio.hradioshowcase.spotify.SpotifyProvider;
import lmu.hradio.hradioshowcase.util.ColorUtils;
import lmu.hradio.hradioshowcase.util.DataCollectionHelper;
import lmu.hradio.hradioshowcase.util.DeviceUtils;
import lmu.hradio.hradioshowcase.util.Parser;
import lmu.hradio.hradioshowcase.util.SharedPreferencesHelper;
import lmu.hradio.hradioshowcase.view.component.LockableDragBottomSheetBehavior;
import lmu.hradio.hradioshowcase.view.fragment.ContentFragment;
import lmu.hradio.hradioshowcase.view.fragment.FavoritesFragment;
import lmu.hradio.hradioshowcase.view.fragment.LauncherFragment;
import lmu.hradio.hradioshowcase.view.fragment.RadioServiceListFragment;
import lmu.hradio.hradioshowcase.view.fragment.SettingsFragment;
import lmu.hradio.hradioshowcase.view.fragment.WebViewFragment;
import lmu.hradio.hradioshowcase.view.fragment.car.CarFragment;
import lmu.hradio.hradioshowcase.view.fragment.dialog.CollectUserDataDialogFragment;
import lmu.hradio.hradioshowcase.view.fragment.dialog.SelectPodcastDialogFragment;
import lmu.hradio.hradioshowcase.view.fragment.player.MiniMusicPlayerFragment;
import lmu.hradio.hradioshowcase.view.fragment.player.RadioPlayerFragment;
import lmu.hradio.hradioshowcase.view.fragment.player.RadioPlayerPlaybackFragment;
import lmu.hradio.hradioshowcase.view.fragment.playlist.PlaylistsPagerFragment;
import lmu.hradio.hradioshowcase.view.fragment.questionaire.QuestionaireFragment;
import lmu.hradio.hradioshowcase.view.fragment.search.SearchFragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;
import static lmu.hradio.hradioshowcase.PermissionRequestCodes.REQUEST_LOCATION_PERMISSION;
import static lmu.hradio.hradioshowcase.PermissionRequestCodes.REQUEST_STORAGE_PERMISSION;
import static lmu.hradio.hradioshowcase.manager.substitution.SubstitutionProviderType.None;
import static lmu.hradio.hradioshowcase.util.DeviceUtils.isTablet;

public class MainActivity extends AppCompatActivity implements
		PlayBackDelegate, RadioPlayerFragment.OnLoadProgrammePodcastListener,
		WebViewFragment.TimeShiftHolder,
		State.Holder, QuestionaireFragment.OnSurveyCompletedListener,
		CollectUserDataDialogFragment.OnCollectUserDataInteractionListener,
		OnManagerErrorListener, CarFragment.OnCarModeClosedListener,
		SearchFragment.OnSearchRequestListener,
		PlayRadioServiceListener,
		MiniMusicPlayerFragment.OnFullScreenClickedListener,
		RadioPlayerPlaybackFragment.SkipItemProvider,
		RadioPlayerFragment.OnMinimizeClickListener,
		PodcastSearchResultListener,
		SelectPodcastDialogFragment.PodcastListInteractionListener,
		RadioPlayerFragment.OnLockBottomSheetListener, OnTunerScanListener, PlayBackListener, SettingsFragment.SettingsActivity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String PLAYLISTS_TAG = PlaylistsPagerFragment.class.getSimpleName();
	private static final String SERVICE_LIST_TAG = RadioServiceListFragment.class.getSimpleName();
	private static final String SEARCH_TAG = SearchFragment.class.getSimpleName();
	private static final String MINI_PLAYER_TAG = MiniMusicPlayerFragment.class.getSimpleName();
	private static final String PLAYER_TAG = RadioPlayerFragment.class.getSimpleName();
	private static final String CAR_TAG = CarFragment.class.getSimpleName();
	private static final String FAVORITES_TAG = FavoritesFragment.class.getSimpleName();
	private static final String WEB_VIEW_TAG = WebViewFragment.class.getSimpleName();
	private static final String LAUNCHER_FRAGMENT_TAG = LauncherFragment.class.getSimpleName();
	private static final String CONTENT_FRAGMENT_TAG = ContentFragment.class.getSimpleName();
	private static final String SETTINGS_FRAGMENT_TAG = SettingsFragment.class.getSimpleName();
	private static final String CURRENT_FRAGMENT_TAG = "current";
	private static final String TITLE_TAG = "title";
	private static final String PODCAST_TAG = "podcasts";
	private static final String QUESTIONAIRE_TAG = "questionaire";
	private static final String IS_PLAYER_SHOWEN_TAG = "player_visible_tag";
	private static final String IS_PROGRESS_SHOWEN_TAG = "progress-visible";
	private static final String PROGRESS_TAG = "progress";

	private static final int FILE_CHOOSE_REQUEST_CODE = 1231;

	private String currentFragmentTag = CURRENT_FRAGMENT_TAG;
	private MainAppController mainAppController;

	private LockableDragBottomSheetBehavior mBottomSheetBehavior;

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@BindView(R.id.loading_indicator_progress_bar)
	ProgressBar progressBar;

	@BindView(R.id.loading_indicator_text_view)
	TextView progressText;

	@BindView(R.id.loading_indicator)
	ConstraintLayout loadingIndicator;

	@BindView(R.id.container)
	CoordinatorLayout mainLayout;

	@Override
	public void onBackPressed() {
		if(isInCarMode) {
			toggleCarMode();
		} else if (mBottomSheetBehavior != null && mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			hidePlayer();
		}
		else if (currentFragmentTag.equals(SETTINGS_FRAGMENT_TAG)) {
			this.switchToSearch();
		}
		else if (currentFragmentTag.equals(PLAYLISTS_TAG)) {
			this.switchToSearch();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterListeners();
		if (isFinishing()) {
			getController().killService(this.getApplicationContext(), this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == REQUEST_CODE) {
			SpotifyProvider manager = (SpotifyProvider) getController().getSubstitutionManager(SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION);
			//This calls must be performed in activity
			AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
			manager.authenticated(response, this);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			if (LAUNCHER_FRAGMENT_TAG.equals(currentFragmentTag))
				tryRestoreFromLastState(null);
		} else if (requestCode == FILE_CHOOSE_REQUEST_CODE && intent != null) {
			Uri selectedfile = intent.getData();
			if (selectedfile != null) {
				try {
					String content = Parser.parseUri(selectedfile, this);
					SharedPreferencesHelper.putWebConfig(this, content);
					getController().tryLoadConfig(this);
				} catch (IOException e) {
					onError(new GeneralError(GeneralError.INVALID_WEBVIEW_CONFIG));
				}
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		if(intent != null) {
			Uri data = intent.getData();
			if (data != null) {
				String dec = Uri.decode(data.getEncodedQuery());
				Uri linkUri = Uri.parse("http://removeMe.irt.de?" + Uri.decode(data.getEncodedQuery()));
				mShareServiceUrl = linkUri.getQueryParameter("s");
				mShareSbtToken = linkUri.getQueryParameter("x");
				String wantedUts = linkUri.getQueryParameter("t");
				if(wantedUts != null) {
					try {
						mShareWantedUts = Long.parseLong(wantedUts);
					} catch (NumberFormatException numExc) {
						if (BuildConfig.DEBUG) numExc.printStackTrace();
					}
				}

				mShareLink = mShareServiceUrl + "?wantedUts=" + mShareWantedUts;
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private String mShareLink = null;
	private String mShareServiceUrl = null;
	private String mShareSbtToken = null;
	private long mShareWantedUts = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean darkMode = SharedPreferencesHelper.getBoolean(this, "dark_mode", true);
		if (darkMode) {
			setTheme(R.style.Theme_App_Dark);
		} else {
			setTheme(R.style.Theme_App_Light);
		}

		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);

		if (mainAppController == null) {
			mainAppController = MainAppController.getInstance(this, () -> initialize(savedInstanceState));
		} else {
			initialize(savedInstanceState);
		}
	}

	private void initialize(Bundle savedInstanceState) {
		int providerInt = SharedPreferencesHelper.getInt(this, SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE, 0);
		SubstitutionProviderType provider = SubstitutionProviderType.values()[providerInt];
		getController().setPrimarySubstitution(provider, this);
		switch (provider) {
			case Spotify:
				if(Build.VERSION.SDK_INT < 21) {
					onError(new GeneralError(GeneralError.API_SPOTIFY_ERROR));
					tryRestoreFromLastState(savedInstanceState);
					return;
				}
				if (DeviceUtils.isAppInstalled(this, "com.spotify.music")) {
					if (getController().getSubstitutionManager(SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION).isAuthenticated()) {
						tryRestoreFromLastState(savedInstanceState);
					} else {
						replaceFragment(R.id.content_container, new LauncherFragment(), LAUNCHER_FRAGMENT_TAG);
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
						getController().getSubstitutionManager(SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION).initialize(this);
					}
				} else {
					tryRestoreFromLastState(savedInstanceState);
					openSpotifyNotInstalledDialog();
				}
				break;
			case None:
				tryRestoreFromLastState(savedInstanceState);
				break;
		}

        if(mShareServiceUrl != null) {
            getController().getAllRadioServices(new OnSearchResultListener<List<RadioServiceViewModel>>() {
                @Override
                public void onResult(List<RadioServiceViewModel> radioServiceViewModels) {
                    for(RadioServiceViewModel vm : radioServiceViewModels) {
                        if(BuildConfig.DEBUG)Log.d(TAG, "AllServiceresult: " + vm.getServiceLabel() + ", ServiceUrl:  " + mShareServiceUrl);
                        for(RadioService rSrv : vm.getRadioServices()) {
                            if(rSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
                                if(BuildConfig.DEBUG)Log.d(TAG, "AllServiceresult: " + ((RadioServiceDabEdi)rSrv).getUrl());
                                if(((RadioServiceDabEdi)rSrv).getUrl().equals(mShareServiceUrl)) {
                                    if(BuildConfig.DEBUG)Log.d(TAG, "AllServiceresult found saved service");
                                    if(mShareWantedUts > 0) {
                                        ((RadioServiceDabEdi) rSrv).setInitialTimePosix(mShareWantedUts);
                                    }
                                    if(mShareSbtToken != null) {
                                        ((RadioServiceDabEdi) rSrv).setInitialSbtToken(mShareSbtToken);
                                    }
                                    getController().startService(vm, MainActivity.this);
                                    return;
                                }
                            }
                        }
                    }
                }
            });
        }
	}

	void getCdtsStatus() {
		String cdtsUserName = SharedPreferencesHelper.getString(getApplicationContext(), SharedPreferencesHelper.CDTS_USERNAME);
		if(BuildConfig.DEBUG) Log.d(TAG, "CDTS checking status for username: " + cdtsUserName);
		if(!cdtsUserName.isEmpty()) {
			Thread cdtsThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						URL cdtsUrl = new URL("https://lmu.hradio.eu:8080/api/v1/kvs/" + cdtsUserName);
						HttpURLConnection cdtsConn = (HttpURLConnection) cdtsUrl.openConnection();
						cdtsConn.setDoInput(true);
						int responseCode = cdtsConn.getResponseCode();
						if (BuildConfig.DEBUG) Log.d(TAG, "CDTS GET ResponseCode: " + responseCode);

						cdtsConn.disconnect();

						if (responseCode >= 400) {
							createCdts(cdtsUserName);
						}

						if (responseCode == 200) {
							String result;
							BufferedInputStream bis = new BufferedInputStream(cdtsConn.getInputStream());
							ByteArrayOutputStream buf = new ByteArrayOutputStream();
							int result2 = bis.read();
							while (result2 != -1) {
								buf.write((byte) result2);
								result2 = bis.read();
							}
							result = buf.toString();

							if (BuildConfig.DEBUG) Log.d(TAG, "CDTS GET TimeshiftToken: " + result);

							JSONObject cdtsObj = new JSONObject(result);
							String streamUrl = cdtsObj.optString("url");
							String timeshiftToken = cdtsObj.optString("tst");
							long validTill = cdtsObj.optLong("vt");

							if(!streamUrl.isEmpty() && !timeshiftToken.isEmpty()) {
								getController().getAllRadioServices(new OnSearchResultListener<List<RadioServiceViewModel>>() {
									@Override
									public void onResult(List<RadioServiceViewModel> radioServiceViewModels) {
										for (RadioServiceViewModel vm : radioServiceViewModels) {
											if (BuildConfig.DEBUG) Log.d(TAG, "CDTS AllServiceresult: " + vm.getServiceLabel() + ", ServiceUrl:  " + mShareServiceUrl);
											for (RadioService rSrv : vm.getRadioServices()) {
												if (rSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
													if (BuildConfig.DEBUG) Log.d(TAG, "CDTS AllServiceresult: " + ((RadioServiceDabEdi) rSrv).getUrl());
													if (((RadioServiceDabEdi) rSrv).getUrl().equals(streamUrl)) {
														if (BuildConfig.DEBUG) Log.d(TAG, "CDTS AllServiceresult found saved service");

														((RadioServiceDabEdi) rSrv).setInitialSbtToken(timeshiftToken);

														getController().startService(vm, MainActivity.this);
														return;
													}
												}
											}
										}
									}
								});
							}
						}
					} catch (MalformedURLException malUrlExc) {
						if (BuildConfig.DEBUG)
							Log.e(TAG, "MalformedURLException: " + malUrlExc.getMessage());
						malUrlExc.printStackTrace();
					} catch (IOException ioExc) {
						if (BuildConfig.DEBUG) ioExc.printStackTrace();
					} catch(JSONException jsonExc) {
						if(BuildConfig.DEBUG)jsonExc.printStackTrace();
					}
				}
			});

			cdtsThread.start();
		}
	}

	void createCdts(String cdtsUsername) {
		if(BuildConfig.DEBUG)Log.d(TAG, "CDTS creating new store");
		Thread cdtsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL cdtsUrl = new URL("https://lmu.hradio.eu:8080/api/v1/kvs?key=" + cdtsUsername);
					HttpURLConnection cdtsConn = (HttpURLConnection)cdtsUrl.openConnection();
					cdtsConn.setRequestMethod("POST");
					cdtsConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
					cdtsConn.setRequestProperty("Accept","application/json");
					cdtsConn.setDoOutput(true);
					cdtsConn.setDoInput(true);

					JSONObject cdtsPostObj = new JSONObject();
					cdtsPostObj.put("tst", "testToken");
					cdtsPostObj.put("url", "https://bredi.irt.de:443/services/6");
					cdtsPostObj.put("vt", 0);

					DataOutputStream cdtsOutStream = new DataOutputStream(cdtsConn.getOutputStream());
					cdtsOutStream.writeBytes(cdtsPostObj.toString());
					cdtsOutStream.flush();
					cdtsOutStream.close();

					int responseCode = cdtsConn.getResponseCode();
					if(BuildConfig.DEBUG)Log.d(TAG, "CDTS POST ResponseCode: " + responseCode);

					cdtsConn.disconnect();
				} catch(MalformedURLException malUrlExc) {
					if(BuildConfig.DEBUG)Log.e(TAG, "MalformedURLException: " + malUrlExc.getMessage()); malUrlExc.printStackTrace();
				}  catch(JSONException jsonExc) {
					if(BuildConfig.DEBUG)jsonExc.printStackTrace();
				} catch(IOException ioExc) {
					if(BuildConfig.DEBUG)ioExc.printStackTrace();
				}
			}
		});

		cdtsThread.start();
	}

	void updateCdts(String tst, String url) {
		String cdtsUserName = SharedPreferencesHelper.getString(getApplicationContext(), SharedPreferencesHelper.CDTS_USERNAME);
		if(BuildConfig.DEBUG)Log.d(TAG, "CDTS updating store with token: " + tst + " and URL: " + url);

		if(!cdtsUserName.isEmpty()) {
			Thread cdtsThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						URL cdtsUrl = new URL("https://lmu.hradio.eu:8080/api/v1/kvs/" + cdtsUserName);
						HttpURLConnection cdtsConn = (HttpURLConnection) cdtsUrl.openConnection();
						cdtsConn.setRequestMethod("PUT");
						cdtsConn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
						cdtsConn.setRequestProperty("Accept", "application/json");
						cdtsConn.setDoOutput(true);
						cdtsConn.setDoInput(true);

						JSONObject cdtsPutObj = new JSONObject();
						cdtsPutObj.put("tst", tst);
						cdtsPutObj.put("url", url);
						cdtsPutObj.put("vt", 0);

						DataOutputStream cdtsOutStream = new DataOutputStream(cdtsConn.getOutputStream());
						cdtsOutStream.writeBytes(cdtsPutObj.toString());
						cdtsOutStream.flush();
						cdtsOutStream.close();

						cdtsConn.connect();

						int responseCode = cdtsConn.getResponseCode();

						if (BuildConfig.DEBUG) Log.d(TAG, "CDTS PUT ResponseCode: " + responseCode);

						if (responseCode == 400) {
							if (BuildConfig.DEBUG) Log.d(TAG, "CDTS update value exceeds 1024 character limit: " + responseCode);
						}

						if (responseCode == 404) {
							if (BuildConfig.DEBUG) Log.d(TAG, "CDTS store for " + cdtsUserName + " does not exist: " + responseCode);
						}

						if (responseCode == 200) {
							String result;
							BufferedInputStream bis = new BufferedInputStream(cdtsConn.getInputStream());
							ByteArrayOutputStream buf = new ByteArrayOutputStream();
							int result2 = bis.read();
							while (result2 != -1) {
								buf.write((byte) result2);
								result2 = bis.read();
							}
							result = buf.toString();

							if (BuildConfig.DEBUG)
								Log.d(TAG, "CDTS updated successfully to: " + result);
						}

						cdtsConn.disconnect();
					} catch (MalformedURLException malUrlExc) {
						if (BuildConfig.DEBUG)
							Log.e(TAG, "MalformedURLException: " + malUrlExc.getMessage());
						malUrlExc.printStackTrace();
					} catch (IOException ioExc) {
						if (BuildConfig.DEBUG) ioExc.printStackTrace();
					} catch (JSONException jsonExc) {
						if (BuildConfig.DEBUG) jsonExc.printStackTrace();
					}
				}
			});

			cdtsThread.start();
		}
	}

	void deleteCdts() {
		String cdtsUserName = SharedPreferencesHelper.getString(getApplicationContext(), SharedPreferencesHelper.CDTS_USERNAME);
		if(!cdtsUserName.isEmpty()) {
			if (BuildConfig.DEBUG) Log.d(TAG, "CDTS deleting store");
			Thread cdtsThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						URL cdtsUrl = new URL("https://lmu.hradio.eu:8080/api/v1/kvs/" + cdtsUserName);
						HttpURLConnection cdtsConn = (HttpURLConnection) cdtsUrl.openConnection();
						cdtsConn.setRequestMethod("DELETE");
						cdtsConn.setDoInput(true);

						int responseCode = cdtsConn.getResponseCode();
						if (BuildConfig.DEBUG) Log.d(TAG, "CDTS DELETE ResponseCode: " + responseCode);

						if (responseCode == 200) {
							String result;
							BufferedInputStream bis = new BufferedInputStream(cdtsConn.getInputStream());
							ByteArrayOutputStream buf = new ByteArrayOutputStream();
							int result2 = bis.read();
							while (result2 != -1) {
								buf.write((byte) result2);
								result2 = bis.read();
							}
							result = buf.toString();

							SharedPreferencesHelper.put(getApplicationContext(), SharedPreferencesHelper.CDTS_USERNAME, "");
							if (BuildConfig.DEBUG) Log.d(TAG, "CDTS deleted successfully: " + result);
						}

						cdtsConn.disconnect();
					} catch (MalformedURLException malUrlExc) {
						if (BuildConfig.DEBUG) Log.e(TAG, "CDTS MalformedURLException: " + malUrlExc.getMessage());
						malUrlExc.printStackTrace();
					} catch (IOException ioExc) {
						if (BuildConfig.DEBUG) ioExc.printStackTrace(); Log.e(TAG, "CDTS IOException: " + ioExc.getMessage());
					}
				}
			});

			cdtsThread.start();
		}
	}

	public MainAppController getController() {
		if (mainAppController == null)
			mainAppController = MainAppController.getInstance(this, () -> {
			});
		return mainAppController;
	}

	private void initViewElements() {
		setSupportActionBar(toolbar);
		hideNavView(false);
		hideToolbar(false);
		registerListeners();
		this.mBottomSheetBehavior = (LockableDragBottomSheetBehavior) BottomSheetBehavior.from(findViewById(R.id.drag_up_container));
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

			TypedValue typedValue = new TypedValue();
			Resources.Theme theme = this.getTheme();
			theme.resolveAttribute(R.attr.colorNavigationBar, typedValue, true);
			getWindow().setNavigationBarColor(typedValue.data);

		}
		mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
					hideMiniPlayer(!getController().isServiceRunning(), true);
					unregisterPlayerFragmentListeners();
				} else if(newState == BottomSheetBehavior.STATE_EXPANDED){
					registerPlayerFragmentListeners();
					hideMiniPlayer(true, true);
					addPlayerFragment();
				} else if(newState == BottomSheetBehavior.STATE_DRAGGING){
					hideMiniPlayer(true, true);
					unregisterPlayerFragmentListeners();
				}

			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				if (slideOffset > 0.5f) {
					hideNavView(true);
					if (getSupportActionBar() != null) getSupportActionBar().hide();
				} else {
					hideNavView(false);
					if (getSupportActionBar() != null) getSupportActionBar().show();
				}
			}
		});
	}


	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	private void openSpotifyNotInstalledDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
		builder.setMessage(getString(R.string.spotify_app_not_installed_description))
				.setTitle(R.string.spotify_app_not_installed_title)
				.setPositiveButton(R.string.install_spotify, (dialogInterface, i) -> DeviceUtils.openPlaystoreEntry(this, "com.spotify.music"))
				.setNegativeButton(R.string.disable_substitution, (dialogInterface, i) -> enableSubstitutionProvider(None));
		builder.create().show();
	}

	private void enableSubstitutionProvider(SubstitutionProviderType provider) {
		getController().setPrimarySubstitution(provider, this);

		switch (provider) {
			case Spotify:
				if (DeviceUtils.isAppInstalled(this, "com.spotify.music")) {
					if (!getController().getSubstitutionManager(SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION).isAuthenticated()) {
						if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
							hidePlayer();
						}
						hideMiniPlayer(true, false);
						hideNavView(true);
						hideToolbar(true);
						replaceFragment(R.id.content_container, new LauncherFragment(), LAUNCHER_FRAGMENT_TAG);
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
						getController().getSubstitutionManager(SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION).initialize(this);
					}
				} else {
					openSpotifyNotInstalledDialog();
				}
				break;
			case None:
				break;
		}

	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENT_FRAGMENT_TAG, currentFragmentTag);
		outState.putString(TITLE_TAG, this.toolbar.getTitle().toString());
		outState.putInt(IS_PROGRESS_SHOWEN_TAG, loadingIndicator.getVisibility());
		if (progressText.getVisibility() == VISIBLE) {
			outState.putInt(PROGRESS_TAG, progressBar.getProgress());
		}

		if (mBottomSheetBehavior != null) {
			outState.putBoolean(IS_PLAYER_SHOWEN_TAG, mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED);
		}

		Fragment miniMusicPlayerFragment = getSupportFragmentManager().findFragmentByTag(MINI_PLAYER_TAG);
		Fragment current = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
		if (miniMusicPlayerFragment != null) {
			getSupportFragmentManager().putFragment(outState, MINI_PLAYER_TAG, miniMusicPlayerFragment);
		}
		if (current != null) {
			getSupportFragmentManager().putFragment(outState, currentFragmentTag, current);
		}
	}

	@Override
	public List<SkipItem> getSkipContent() {
		return getController().getSkipItems();
	}

	@Override
	public void onSkipItemClicked(SkipItem item) {
		getController().skipToItem(item);
	}

	@Override
	public boolean onSkipItemLongClicked(SkipItem item) {
		RadioService curSrv = getController().getCurrentTimeShiftPlayer().getRadioService();
		if(curSrv != null) {
			if(curSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
				RadioServiceDabEdi curEdiSrv = (RadioServiceDabEdi)curSrv;
				if(BuildConfig.DEBUG)Log.d(TAG, "LongClick curSrvUrl: " + curEdiSrv.getUrl());
				if(BuildConfig.DEBUG)Log.d(TAG, "LongClick ShareUrl: " + "http://rs.irt.de?s=" + Uri.encode(curEdiSrv.getUrl()) + "&t=" + item.getSbtRealTime());

				long valid = item.getSbtRealTime() + curEdiSrv.getSbtMax();
				String validTill = "";
				if(valid > 0) {
					Date valDate = new Date(valid);
					SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY - HH:mm:ss", Locale.getDefault());
					validTill = format.format(valDate);
				}

				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				String subjectString = "";
				if(!validTill.isEmpty()) {
					subjectString = getString(R.string.share_item_subject_date, validTill, item.getSkipTextual().getText());
				} else {
					subjectString = getString(R.string.share_item_subject, item.getSkipTextual().getText());
				}
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, subjectString);

				//Toggle
				sendIntent.putExtra(Intent.EXTRA_TEXT, "http://rs.irt.de?s=" + Uri.encode(((RadioServiceDabEdi)curSrv).getUrl()) + "&t=" + item.getSbtRealTime());
				//Token
				sendIntent.setType("text/plain");

				startActivity(Intent.createChooser(sendIntent, getString(R.string.share_text)));
				return true;
			}
		}

		return false;
	}

	@Override
	public void shareSbtToken() {
		RadioService curSrv = getController().getCurrentTimeShiftPlayer().getRadioService();
		if(curSrv != null) {
			if(curSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
				RadioServiceDabEdi curEdiSrv = (RadioServiceDabEdi)curSrv;

				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);

				String subject;
				if(getController().getCurrentEpg() != null) {
					subject = getString(R.string.share_session_programme, getController().getCurrentEpg().getCurrentRunningProgramme().getQualifiedName());
				} else {
					subject = getString(R.string.share_session_wo_programme);
				}
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "http://rs.irt.de?s=" + Uri.encode(((RadioServiceDabEdi)curSrv).getUrl()) + "&x=" + curEdiSrv.getSbtToken());
				sendIntent.setType("text/plain");

				startActivity(Intent.createChooser(sendIntent, getString(R.string.share_text)));
			}
		}
	}

	@Override
	public long getTotalDuration() {
		return getController().getTotalTimeShiftDuration();
	}

	@Override
	public SkipItem getCurrentSkipItem() {
		return getController().getCurrentSkipItem();
	}

	@Override
	public void onMinimizeClicked() {
		hidePlayer();
	}

	@Override
	public void onMiniPlayerFullscreenClicked() {
		if(getController().getAppState().getPlayBackState().isRunning() && !isInCarMode)
			switchToPlayer();
	}


	@Override
	public void onRadioServiceSelected(RadioServiceViewModel radioService) {
		showProgressIndicator(true);
		getController().startService(radioService, this);
		invalidateOptionsMenu();
	}

	/**
	 * Restores saved state if not null, performs regular initialization else
	 *
	 * @param savedInstanceState - the saved state
	 */
	private void tryRestoreFromLastState(Bundle savedInstanceState) {
		initViewElements();
	    checkForUserDataCollection();

		if (savedInstanceState != null) {

			int progressVisibility = savedInstanceState.getInt(IS_PROGRESS_SHOWEN_TAG, GONE);
			int progress = savedInstanceState.getInt(PROGRESS_TAG, 0);
			if (progressVisibility == VISIBLE) {
				boolean indeterminate = progress == 0;
				showProgressIndicator(indeterminate);
				if (!indeterminate) {
					tunerScanProgress(progress);
				}
			}

			// try to restore fragment state
			currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);

			if (QUESTIONAIRE_TAG.equals(currentFragmentTag)) {
				showQuestionaire(true);
				return;
			}

			if (WEB_VIEW_TAG.equals(currentFragmentTag)) {
				toggleWebView(true);
				return;
			}

			if (!getSupportFragmentManager().getFragments().isEmpty()) {
				Fragment current = getSupportFragmentManager().getFragment(savedInstanceState, currentFragmentTag);
				replaceFragment(R.id.content_container, current, currentFragmentTag);
			}

			//reconnect to spotify since remote connection linked to activity lifecycle
			if (getController().isSubstitution()) {
				getController().refreshSubstitutionState(this, () -> {
				});
			}

			isInCarMode = CAR_TAG.equals(currentFragmentTag);
			if(BuildConfig.DEBUG)Log.d(TAG, "isInCarMode: " + isInCarMode );
			if (isInCarMode) {
				mainLayout.setFitsSystemWindows(false);
				hideToolbar(true);
				hideNavView(true);
				hideMiniPlayer(true, false);
				hidePlayer();
				return;
			} else {
				mainLayout.setFitsSystemWindows(true);
				hideToolbar(false);
				hideNavView(false);
			}

			if (getController().isServiceRunning() && !currentFragmentTag.equals(CAR_TAG) && !savedInstanceState.getBoolean(IS_PLAYER_SHOWEN_TAG, false)) {
				hideMiniPlayer(false, false);
			}

			toolbar.setTitle(savedInstanceState.getString(TITLE_TAG, getController().getRunningServiceLabel()));
			if (savedInstanceState.getBoolean(IS_PLAYER_SHOWEN_TAG, false)) {
				switchToPlayer();
			}

		} else {
			Database.getInstance().readFavorites(favs -> {
				if (!favs.isEmpty()) {
					switchToFavorites();
				} else if(!Radio.getInstance().getRadioServices().isEmpty()) {
					if(BuildConfig.DEBUG)Log.d(TAG, "Switching to servicelist");
					switchToServiceList();
				} else {
					if(BuildConfig.DEBUG)Log.d(TAG, "Switching to search: " + Radio.getInstance().getRadioServices().size());
					switchToSearch();
				}
				if (getIntent().getBooleanExtra("FROM-NOTIFICATION", false) && getController().isServiceRunning()) {
					switchToPlayer();
				}
			}, this);
		}
	}

	/**
	 * Checks if preference for data collection is present.
	 * Open preferences dialog if not.
	 */
	private void checkForUserDataCollection() {
		if (!DataCollectionHelper.hasUserDataCollectionPreference(this)) {
			showDataCollectionDialog();
		}
	}

	/**
	 * Actually shows the data collection preferences dialog
	 */
	private void showDataCollectionDialog() {
		FragmentManager fm = getSupportFragmentManager();
		CollectUserDataDialogFragment collectUserDataDialogFragment = CollectUserDataDialogFragment.newInstance();
		collectUserDataDialogFragment.show(fm, SharedPreferencesHelper.ALLOW_DATA_COLLECTION_KEY);
	}

	@Override
	public void onPodcastsReceived(byte[] sourceCover, SubstitutionItem[] podcastSubstitutions) {
		hideProgressIndicator();
		FragmentManager fm = getSupportFragmentManager();
		SelectPodcastDialogFragment podcastDialogFragment = (SelectPodcastDialogFragment) fm.findFragmentByTag(PODCAST_TAG);
		if (podcastDialogFragment == null) {
			podcastDialogFragment = SelectPodcastDialogFragment.newInstance(podcastSubstitutions);
			podcastDialogFragment.show(fm, PODCAST_TAG);
			podcastDialogFragment.setOnDismissListener(() -> {
				if(isInCarMode) {
					getWindow().getDecorView().setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_LAYOUT_STABLE
									| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
									| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_FULLSCREEN
									| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
				}
			});
		} else {
			podcastDialogFragment.addPodcasts(podcastSubstitutions);
		}
	}


	@Override
	public void onPodcastSelected(SubstitutionItem podcast) {
		showProgressIndicator(true);
		getController().playPodcast(podcast, this);
	}

	private void showQuestionaire(boolean recursiveCall) {
		PrudacRestClient.getAllNewSurveys(surveys -> {
			if (!surveys.isEmpty()) {
				Survey survey = surveys.remove(0);
				Report report = Report.buildReport(survey, new PrivacyParameters(0, 1, 1));
				Fragment fragment = QuestionaireFragment.newInstance(survey, report);
				replaceFragment(R.id.content_container, fragment, QUESTIONAIRE_TAG);
				hideMiniPlayer(true, false);
				hideToolbar(true);
				hideNavView(true);
			} else {
				if (!recursiveCall)
					this.onError(new GeneralError(GeneralError.PRUDAC_ERROR));
			}

		}, this, this);
	}

	@Override
	public void onComplete(Report report, Survey survey) {
		PrudacRestClient.sendReport(report, survey, this);
		hideNavView(false);
		hideToolbar(false);
		switchToFavorites();
		showQuestionaire(true);
	}

	/**
	 * replace container with fragment
	 *
	 * @param containerID - the containers layout id
	 * @param fragment    - the new fragment
	 * @param tag         - the transaction tag
	 */
	private void replaceFragment(int containerID, Fragment fragment, String tag) {
		//check for content container
		if (containerID == R.id.content_container)
			currentFragmentTag = tag;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(containerID, fragment, tag);
		ft.commitAllowingStateLoss();
	}

	/**
	 * switch to radio player fragment view
	 */
	private void switchToPlayer() {
		if (!isInCarMode) {

			Objects.requireNonNull(getSupportActionBar()).hide();
			hideNavView(true);
			hideToolbar(true);
			hideMiniPlayer(true, false);
			addPlayerFragment();
			if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
				mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		}
	}

	private void addPlayerFragment(){
		Fragment radioPlayerFragment = getSupportFragmentManager().findFragmentByTag(PLAYER_TAG);
		if (radioPlayerFragment == null) {
			radioPlayerFragment = RadioPlayerFragment.newInstance();
			replaceFragment(R.id.drag_content_container, radioPlayerFragment, PLAYER_TAG);

		}
	}

	private void unregisterPlayerFragmentListeners(){
		RadioPlayerFragment radioPlayerFragment = (RadioPlayerFragment) getSupportFragmentManager().findFragmentByTag(PLAYER_TAG);
		if (radioPlayerFragment != null) {
			radioPlayerFragment.unregisterListeners();
		}
	}

	private void registerPlayerFragmentListeners(){
		RadioPlayerFragment radioPlayerFragment = (RadioPlayerFragment) getSupportFragmentManager().findFragmentByTag(PLAYER_TAG);
		if (radioPlayerFragment != null) {
			radioPlayerFragment.registerListeners();
		}
	}

	private void hidePlayer(){
		removePlayerFragment();
		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
	}

	private void removePlayerFragment(){
		Fragment radioPlayerFragment = getSupportFragmentManager().findFragmentByTag(PLAYER_TAG);
		if (radioPlayerFragment != null) {
			getSupportFragmentManager().beginTransaction().remove(radioPlayerFragment).commitAllowingStateLoss();
		}
	}

	/**
	 * switch to service search fragment
	 */
	private void switchToSearch() {
		if(BuildConfig.DEBUG)Log.d(TAG, "switchToSearch isTablet: " + isTablet(this));
	/*	if (isTablet(this)) {
			getController().getSearchResultState(res ->
					runOnUiThread(() -> {
						if (res == null || res.isEmpty()) {
							loadContentFragmentWithSearch();
						} else {
							loadContentFragment();
						}
					}));
		} else {*/
			Fragment searchFragment = getSupportFragmentManager().findFragmentByTag(SEARCH_TAG);
			if (searchFragment == null)
				searchFragment = SearchFragment.newInstance();
			toolbar.setTitle(R.string.search_text);
			hideNavView(false);
			hideMiniPlayer(!getController().isServiceRunning(), false);
			replaceFragment(R.id.content_container, searchFragment, SEARCH_TAG);
	//	}
	}

	/**
	 * switch to service search fragment
	 */
	private void switchToPlaylists() {
		if (SharedPreferencesHelper.getInt(this, SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE, 0) != 0) {
			Fragment playLists = getSupportFragmentManager().findFragmentByTag(PLAYLISTS_TAG);
			if (playLists == null)
				playLists = PlaylistsPagerFragment.newInstance();
			toolbar.setTitle(R.string.playlists_text);
			hideMiniPlayer(!getController().isServiceRunning(), false);
			replaceFragment(R.id.content_container, playLists, PLAYLISTS_TAG);
		} else {
			onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
		}
	}


	/**
	 * switch to sender list fragment
	 */
	private void switchToServiceList() {
		if(BuildConfig.DEBUG)Log.d(TAG, "switchToServiceList isTablet: " + isTablet(this));
		if (isTablet(this)) {
			loadContentFragment();
		} else {
			if (currentFragmentTag.equals(SERVICE_LIST_TAG)) {
				RadioServiceListFragment serviceListFragment = (RadioServiceListFragment) getSupportFragmentManager().findFragmentByTag(SERVICE_LIST_TAG);
				if (serviceListFragment != null)
					serviceListFragment.refresh(this);
				return;
			}
			RadioServiceListFragment serviceListFragment = (RadioServiceListFragment) getSupportFragmentManager().findFragmentByTag(SERVICE_LIST_TAG);
			if (serviceListFragment == null)
				serviceListFragment = RadioServiceListFragment.newInstance();

			toolbar.setTitle(R.string.toolbar_title_list);
			getLastSearchResultState(serviceListFragment::onServiceUpdate);

			replaceFragment(R.id.content_container, serviceListFragment, SERVICE_LIST_TAG);
			hideMiniPlayer(!getController().isServiceRunning(), false);
		}
	}

	private void switchToFavorites() {
		if(BuildConfig.DEBUG)Log.d(TAG, "switchToFavorites isTablet: " + isTablet(this));
		if (isTablet(this)) {
			loadContentFragment();
		} else {
			if (currentFragmentTag.equals(FAVORITES_TAG))
				return;
			Fragment favoritesFragment = getSupportFragmentManager().findFragmentByTag(FAVORITES_TAG);
			if (favoritesFragment == null)
				favoritesFragment = FavoritesFragment.newInstance();
			toolbar.setTitle(R.string.title_favorites);
			replaceFragment(R.id.content_container, favoritesFragment, FAVORITES_TAG);
			hideMiniPlayer(!getController().isServiceRunning(), false);
		}
	}

	private void loadContentFragment() {
		if (currentFragmentTag.equals(CONTENT_FRAGMENT_TAG))
			return;
		Fragment contentFragment = getSupportFragmentManager().findFragmentByTag(CONTENT_FRAGMENT_TAG);
		if (contentFragment == null)
			contentFragment = ContentFragment.newInstance();
		toolbar.setTitle(R.string.title_content);
		replaceFragment(R.id.content_container, contentFragment, CONTENT_FRAGMENT_TAG);
		hideMiniPlayer(!getController().isServiceRunning(), false);
	}

	private void loadContentFragmentWithSearch() {
		Fragment contentFragment = getSupportFragmentManager().findFragmentByTag(CONTENT_FRAGMENT_TAG);
		if (contentFragment == null)
			contentFragment = ContentFragment.newInstance();
		Bundle bundle = new Bundle();
		bundle.putBoolean(SEARCH_TAG, true);
		contentFragment.setArguments(bundle);
		toolbar.setTitle(R.string.title_content);
		replaceFragment(R.id.content_container, contentFragment, CONTENT_FRAGMENT_TAG);
		hideMiniPlayer(!getController().isServiceRunning(), false);
	}



	void toggleWebView(boolean toggle) {
		if (!toggle) {
			hideNavView(false);
			hideMiniPlayer(!getController().getAppState().getPlayBackState().isRunning(), false);
			switchToFavorites();
		} else {
			if(Build.VERSION.SDK_INT == 19) {
				hidePlayer();
				String webAppUrl = getController().getAppState().getPlayBackState().getRunningService().getWebAppUrl(); //"http://mpattest.irt.de/hradio/radioWeb/";//
				if (webAppUrl != null && !webAppUrl.isEmpty()) {
					hideMiniPlayer(true, false);
					hideNavView(true);
					Fragment webView = WebViewFragment.newInstance(webAppUrl);
					toolbar.setTitle(R.string.title_webview);
					replaceFragment(R.id.content_container, webView, WEB_VIEW_TAG);
				}
			} else{
				onError(new GeneralError(GeneralError.API_ERROR));
			}
		}
	}

	/**
	 * change visibility of mini player fragment, by removing/adding it to container
	 *
	 * @param hide - visibility flag
	 */
	private void hideMiniPlayer(boolean hide, boolean animated) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment miniMusicPlayerFragment = getSupportFragmentManager().findFragmentByTag(MINI_PLAYER_TAG);
		if (miniMusicPlayerFragment == null && hide)
			return;
		if (hide) {
			ft.remove(miniMusicPlayerFragment);
			mBottomSheetBehavior.setPeekHeight(0);
			FrameLayout layout = findViewById(R.id.content_container);
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
			params.setMargins(0, 0, 0, 0);
			layout.setLayoutParams(params);
		} else {
			if (miniMusicPlayerFragment == null)
				miniMusicPlayerFragment = MiniMusicPlayerFragment.newInstance();

			if (animated) {
				Slide slide = new Slide();
				slide.setSlideEdge(Gravity.BOTTOM);
				slide.addListener(new Transition.TransitionListener() {
					@Override
					public void onTransitionStart(@NonNull Transition transition) {

					}

					@Override
					public void onTransitionEnd(@NonNull Transition transition) {
						setPeakHeightToMiniPlayerSize();

					}

					@Override
					public void onTransitionCancel(@NonNull Transition transition) {

					}

					@Override
					public void onTransitionPause(@NonNull Transition transition) {

					}

					@Override
					public void onTransitionResume(@NonNull Transition transition) {

					}
				});
				miniMusicPlayerFragment.setEnterTransition(slide);
			} else {
				setPeakHeightToMiniPlayerSize();
			}
			ft.replace(R.id.mini_player_container, miniMusicPlayerFragment, MINI_PLAYER_TAG).addToBackStack(MINI_PLAYER_TAG);

		}
		ft.commitAllowingStateLoss();

	}

	private void setPeakHeightToMiniPlayerSize() {
		float miniPlayerHeight = getResources().getDimension(R.dimen.miniplayer_height);
		float navBarHeight = getResources().getDimension(R.dimen.bottom_navigation_view_height);
		float peakHeight = navBarHeight + miniPlayerHeight;
		mBottomSheetBehavior.setPeekHeight((int) peakHeight);
		FrameLayout layout = findViewById(R.id.content_container);
		ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
		params.setMargins(0, 0, 0, (int) miniPlayerHeight);
		layout.setLayoutParams(params);
	}

	@Override
	public void onError(@NonNull GeneralError error) {
		runOnUiThread(() -> {
			switch (error.getErrorCode()) {
				case GeneralError.SPOTIFY_TOKEN_ERROR:
					hideProgressIndicator();
					getController().getSubstitutionManager(SubstitutionPlayerTyp.SPOTIFY_SUBSTITUTION).initialize(this);
					break;
				case GeneralError.EPG_ERROR:
					makeSnackbar(R.string.no_epg_found);
					break;
				case GeneralError.LOCATION_ERROR_PERMISSION_DENIED:
					makeSnackbar(R.string.error_reading_location_permission_denied, R.string.request_permission_settings, v -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION));
					break;
				case GeneralError.LOCATION_ERROR_DISABLED:
					makeSnackbar(R.string.error_reading_location_disabled, R.string.open_gps_settings, v -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
					break;
				case GeneralError.EXO_ERROR_LOAD:
					hideProgressIndicator();
					makeSnackbar(R.string.exo_error_load);
					break;
				case GeneralError.EXO_BUFFER_ERROR:
					hideProgressIndicator();
					makeSnackbar(R.string.exo_error_buffer);
					break;
				case GeneralError.EXO_ERROR_PLAYBACK:
					hideProgressIndicator();
					makeSnackbar(R.string.exo_error_playback);
					break;
				case GeneralError.SUBSTITUTION_ERROR:
					hideProgressIndicator();
					makeSnackbar(R.string.spotify_error, R.string.disable_substitution, (v) -> enableSubstitutionProvider(None));
					break;
				case GeneralError.NETWORK_ERROR:
					hideProgressIndicator();
					makeSnackbar(R.string.network_error);
					break;
				case GeneralError.CONTROLLS_ERROR:
					hideProgressIndicator();
					makeSnackbar(R.string.controls_error);
					break;
				case GeneralError.TUNER_ERROR:
					hideProgressIndicator();
					makeSnackbar(R.string.tuner_error);
					break;
				case GeneralError.SERVICE_SEARCH:
					hideProgressIndicator();
					makeSnackbar(R.string.search_error);
					break;
				case GeneralError.RECOMMENDER:
					if (BuildConfig.DEBUG) Log.d("RECOMMENDER", "No recommendations found");
					break;
				case GeneralError.USER_DATA_COLLECTION:
					if (BuildConfig.DEBUG)
						Log.d("USER_DATA_COLLECTION", error.fillInStackTrace().toString());
					break;
				case GeneralError.TIMESHIFT:
					hideProgressIndicator();
					//makeSnackbar(R.string.timeshift_error);
					break;
				case GeneralError.SUBSTITUTION_DISABLED:
					hideProgressIndicator();
					makeSnackbar(R.string.no_substitution_provider, R.string.settings, (v) -> openSettingsScreen());
					break;
				case GeneralError.NO_DAB_FM_TUNER:
					hideProgressIndicator();
					makeSnackbar(R.string.no_dab_fm_tuner);
					break;
				case GeneralError.NO_IP_TUNER:
					hideProgressIndicator();
					makeSnackbar(R.string.no_ip_tuner);
					break;
				case GeneralError.TUNER_NOT_AVAILABLE:
					hideProgressIndicator();
					makeSnackbar(R.string.tuner_not_available);
					break;
				case GeneralError.SEEKING_NOT_SUPPORTED:
					hideProgressIndicator();
					makeSnackbar(R.string.seeking_not_supported);
					break;
				case GeneralError.PRUDAC_ERROR:
					hideProgressIndicator();
					makeSnackbar(R.string.prudac_error);
					break;
				case GeneralError.FAVORITE_NOT_AVAILABLE_ERROR:
					hideProgressIndicator();
					makeSnackbar(R.string.favorite_not_available_error);
					break;
				case GeneralError.STORAGE_PERMISSION_DENIED:
					hideProgressIndicator();
					makeSnackbar(R.string.storage_permission_denied);
					break;
				case GeneralError.PLAY_WHEN_SCANNING:
					makeSnackbar(R.string.play_when_scanning_description);
                /*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.play_when_scanning_title)
                            .setMessage(R.string.play_when_scanning_description)
                            .setNegativeButton(R.string.decline, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(R.string.play, (dialogInterface, i) -> {
                                Bundle bundle = error.getExtra();
                                if(bundle != null && bundle.get(GeneralError.SERIALIZABLE_EXTRA) != null){
                                    getController().stopScanningAndPlay((RadioServiceImpl)bundle.get(GeneralError.SERIALIZABLE_EXTRA));
                                }
                            });
                    builder.create().show(); */
					break;
				case GeneralError.API_ERROR:
					makeSnackbar(R.string.api_level_to_low);
					break;
				case GeneralError.API_SPOTIFY_ERROR:
					enableSubstitutionProvider(None);
					makeSnackbar(R.string.api_level_to_low_for_spotify);
					break;
			}
		});
	}


	private void makeSnackbar(int messageID) {
		makeSnackbar(messageID, -1, null);
	}


	private void makeSnackbar(int messageID, int actionId, View.OnClickListener actionListener) {
		Snackbar snackbar = Snackbar.make(findViewById(R.id.container), messageID, (actionId == -1) ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG);
		if (actionId != -1) {
			snackbar.setAction(actionId, actionListener);
		}
		snackbar.show();
	}


	@Override
	public void declineDataCollection() {
		DataCollectionHelper.disableDataCollection(this);
	}

	@Override
	public void acceptDataCollection(int genderIndex, int ageIndex, boolean trackLocation, boolean trackCountry) {
		DataCollectionHelper.allowDataCollection(genderIndex, ageIndex, trackLocation, trackCountry, this, error -> {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "User location error with code " + error.getErrorCode());
		});
	}

	private static final int EXTERNAL_STORAGE_PERMISSION = 1409;

	private Runnable externalStorageCallback;

	private void openSearchResultDialogMap(List<RadioServiceViewModel> searchResult) {

       /* if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            MapDialogFragment mapDialogFragment = MapDialogFragment.newInstance(searchResult);
            FragmentManager fm = getSupportFragmentManager();
            mapDialogFragment.show(fm, MapDialogFragment.class.getSimpleName());
        } else {
            externalStorageCallback = () -> {
                MapDialogFragment mapDialogFragment = MapDialogFragment.newInstance(searchResult);
                FragmentManager fm = getSupportFragmentManager();
                mapDialogFragment.show(fm, MapDialogFragment.class.getSimpleName());
            };
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION);

        } */

	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		// If request is cancelled, the result arrays are empty.
		if (requestCode == REQUEST_LOCATION_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				LocationReader.getInstance().readUserLocation(this, null);
				return;
			}
			LocationReader.getInstance().firePendingCallbacks(null, new GeneralError(GeneralError.LOCATION_ERROR_PERMISSION_DENIED));
		} else if (requestCode == EXTERNAL_STORAGE_PERMISSION) {
			if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				if (externalStorageCallback != null) externalStorageCallback.run();
			}
		} else if (requestCode == REQUEST_STORAGE_PERMISSION) {
			if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				getController().tryLoadConfig(this);
			} else {
				onError(new GeneralError(GeneralError.STORAGE_PERMISSION_DENIED));
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);

		// MenuItem spotifyItem = menu.findItem(R.id.spotify_menu);
		@ColorInt int color = ColorUtils.resolveAttributeColor(R.attr.colorPrimaryText, this);

		//Carmode disabled for now
		/*
		MenuItem item = menu.findItem(R.id.car_mode_menu);
		if (item != null) {
			int iconResource = isInCarMode ? R.drawable.outline_directions_car_white_24 : R.drawable.baseline_directions_car_white_24;
			Drawable drawable = ContextCompat.getDrawable(this, iconResource);
			if (drawable != null) {
				DrawableCompat.setTint(drawable, color);
				item.setIcon(drawable);
			}
		}
		*/
		MenuItem itemWebToggle = menu.findItem(R.id.radio_web_toggle_menu);
		Drawable drawableToggle = ContextCompat.getDrawable(this, R.drawable.baseline_web_white_24);
		if (itemWebToggle != null && drawableToggle != null) {
			DrawableCompat.setTint(drawableToggle, color);
			itemWebToggle.setIcon(drawableToggle);
			RadioServiceViewModel vm = getController().getAppState().getPlayBackState().getRunningService();
			itemWebToggle.setVisible( Build.VERSION.SDK_INT >= 19 && vm != null && vm.getWebAppUrl() != null && !vm.getWebAppUrl().isEmpty());
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void closeCarMode() {
		toggleCarMode();
	}

	@Override
	public void getLastSearchResultState(OnSearchResultListener<List<RadioServiceViewModel>> resultCallback) {
		getController().getSearchResultState(resultCallback);
	}

	@Override
	public PlayBackState getState() {
		return getController().getAppState().getPlayBackState();
	}

	@Override
	public void getRecommendations(OnTunerScanListener listener) {
		getController().getRecommendations(listener, this);
	}

	/**
	 * get playback controls delegate
	 */
	public PlayBackDelegate getPlayBackDelegate() {
		return this;
	}

	private void unregisterListeners() {
		getController().unregisterOnErrorListener(this);
		getController().unregisterPodcastSearchResultListener(this);
		getController().unregisterPlayBackListener(this);
		getController().unregisterTunerScanListeener(this);
	}

	private void registerListeners() {
		getController().registerTunerScanListeener(this);
		getController().registerOnErrorListener(this);
		getController().registerPodcastSearchResultListener(this);
		getController().registerPlayBackListener(this);
		BottomNavigationView navView = findViewById(R.id.nav_view);
		if (navView != null)
			navView.setOnNavigationItemSelectedListener(item -> {
				switch (item.getItemId()) {
					case R.id.navigation_list:
						switchToServiceList();
						return true;
					case R.id.navigation_search:
						switchToSearch();
						return true;
					case R.id.navigation_favorites:
						switchToFavorites();
						return true;
				}
				return false;
			});

		toolbar.setOnMenuItemClickListener(item -> {
			switch (item.getItemId()) {
				case R.id.settings:
					this.openSettingsScreen();
					break;
				case R.id.questionaire_menu:
					this.showQuestionaire(false);
					break;
					/* Car mode disabled for now
				case R.id.car_mode_menu:
					this.toggleCarMode();
					int iconResource = isInCarMode ? R.drawable.outline_directions_car_white_24 : R.drawable.baseline_directions_car_white_24;
					Drawable drawable = ContextCompat.getDrawable(this, iconResource);
					if (drawable != null) {
						TypedValue typedValue = new TypedValue();
						Resources.Theme theme = this.getTheme();
						theme.resolveAttribute(R.attr.colorPrimaryText, typedValue, true);
						@ColorInt int color = typedValue.data;
						DrawableCompat.setTint(drawable, color);
						item.setIcon(drawable);
					}
					break;
					*/

				case R.id.playlist_menu:
					boolean enabled = SharedPreferencesHelper.getInt(this, SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE, 0) != 0;
					if (enabled)
						this.switchToPlaylists();
					else
						makeSnackbar(R.string.no_substitution_provider, R.string.settings, (v) -> openSettingsScreen());
					break;

				case R.id.radio_web_toggle_menu:
					toggleWebView(!currentFragmentTag.equals(WEB_VIEW_TAG));
					break;
				case R.id.cdts_menu:
					String cdtsUsername = SharedPreferencesHelper.getString(this, SharedPreferencesHelper.CDTS_USERNAME);
					if(!cdtsUsername.isEmpty()) {
						getCdtsStatus();
					} else {
						makeSnackbar(R.string.cdts_error_no_name_set, R.string.settings, (v) -> openSettingsScreen());
					}

					break;
               /* case R.id.spotify_menu:
                    boolean spotifyEnabled = SharedPreferencesHelper.isSpotifyEnabled(this);
                    enableSubstitutionProvider(!spotifyEnabled);
                    break;


                case R.id.switch_theme:
                    boolean darkMode = SharedPreferencesHelper.getBoolean(this, "dark_mode", true);
                    SharedPreferencesHelper.put(this, "dark_mode", !darkMode);
                    recreate();
                    break; */
			}
			return true;
		});
	}

	private void openSettingsScreen() {
		hidePlayer();
		SettingsFragment fragment = SettingsFragment.newInstance();
		replaceFragment(R.id.content_container, fragment, SettingsFragment.class.getSimpleName());
		hideNavView(true);
		hideMiniPlayer(true, true);
		toolbar.setTitle(R.string.settings);
	}

	private void hideToolbar(boolean hide) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				toolbar.setVisibility(hide ? GONE : VISIBLE);
			}
		});
	}

	private void hideNavView(boolean hide) {
		BottomNavigationView navView = findViewById(R.id.nav_view);
		if (navView != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					navView.setVisibility(hide ? GONE : VISIBLE);
				}
			});
		}
	}

	private boolean isInCarMode;

	@SuppressLint("SourceLockedOrientationActivity")
	private void toggleCarMode() {
		if (isInCarMode) {
			isInCarMode = false;
			this.switchToFavorites();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			hideNavView(false);
			hideToolbar(false);
		} else {
			isInCarMode = true;
			hideNavView(true);
			hideMiniPlayer(true, false);
			hideToolbar(true);
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(CAR_TAG);
			if (fragment == null)
				fragment = CarFragment.newInstance();
			replaceFragment(R.id.content_container, fragment, CAR_TAG);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}

		this.recreate();
	}

	public void registerOnMetaDataUpdateListener(OnEPGUpdateListener listener) {
		getController().registerMetaDataUpdateListener(listener);
	}

	public void unregisterOnMetaDataUpdateListener(OnEPGUpdateListener listener) {
		getController().unregisterMetaDataUpdateListener(listener);
	}

	@Override
	public void lockBottomSheet(boolean lock) {
		mBottomSheetBehavior.setDragEnabled(!lock);
	}

	@Override
	public TimeshiftPlayer getTimeShiftPlayer() {
		return getController().getCurrentTimeShiftPlayer();
	}

	private void showProgressIndicator(boolean indeterminate) {
		runOnUiThread(() -> {
			loadingIndicator.setVisibility(VISIBLE);
			progressBar.setVisibility(VISIBLE);
			progressBar.setIndeterminate(indeterminate);
			if (indeterminate)
				progressText.setVisibility(GONE);
			else
				progressText.setVisibility(VISIBLE);

		});
	}

	private void hideProgressIndicator() {
		runOnUiThread(() -> {
			loadingIndicator.setVisibility(GONE);
			progressBar.setVisibility(GONE);
			progressText.setVisibility(GONE);
		});
	}


	@Override
	public void tunerScanStarted() {
		showProgressIndicator(false);
	}

	@Override
	public void tunerScanProgress(int percentScanned) {
		runOnUiThread(() -> {
			showProgressIndicator(false);
			progressBar.setProgress(percentScanned);
			progressText.setText(String.format(getString(R.string.progress_percentage), percentScanned));
		});
	}

	@Override
	public void tunerScanFinished() {
		if(BuildConfig.DEBUG)Log.d(TAG, "tunerScanFinished");
		hideProgressIndicator();

		Fragment fragment = getSupportFragmentManager().findFragmentByTag(SERVICE_LIST_TAG);
		if (fragment instanceof RadioServiceListFragment) {
			runOnUiThread(() -> getController().getAllRadioServices(((RadioServiceListFragment) fragment)::onServiceUpdate));
		}
	}

	@Override
	public void searchFor(Map<String, String> params, List<TunerType> selectedTuners) {

		Fragment fragment = getSupportFragmentManager().findFragmentByTag(SERVICE_LIST_TAG);
		if (fragment instanceof RadioServiceListFragment) {
			((RadioServiceListFragment) fragment).clearServiceList();
		}
		showProgressIndicator(false);
		switchToServiceList();

		getController().searchForServices(this, params, selectedTuners);
	}

	@Override
	public void onResult(List<RadioServiceViewModel> services) {
		runOnUiThread(() -> {
			hideProgressIndicator();
			if (DeviceUtils.isTablet(this)) {
				Fragment fragment = getSupportFragmentManager().findFragmentByTag(CONTENT_FRAGMENT_TAG);
				if (fragment instanceof ContentFragment) {
					runOnUiThread(() -> getController().getAllRadioServices(((ContentFragment) fragment)::onServiceUpdate));
				}
			} else {
				Fragment fragment = getSupportFragmentManager().findFragmentByTag(SERVICE_LIST_TAG);
				if (fragment instanceof RadioServiceListFragment) {

					((RadioServiceListFragment) fragment).onServiceUpdate(services);
				}
			}
			for (RadioServiceViewModel service : services)
				if (service.getSource() != null && !service.getSource().isEmpty()) {
					openSearchResultDialogMap(services);
					return;
				}
		});
	}

	@Override
	public void onServiceFound(RadioServiceViewModel service, RadioService type) {
		if (DeviceUtils.isTablet(this)) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(CONTENT_FRAGMENT_TAG);
			if (fragment instanceof ContentFragment) {
				runOnUiThread(() -> ((ContentFragment) fragment).onServiceFound(service, type));
			}
		} else {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(SERVICE_LIST_TAG);
			if (fragment instanceof RadioServiceListFragment) {
				runOnUiThread(() -> ((RadioServiceListFragment) fragment).onServiceFound(service, type));
			}
		}
	}

	@Override
	public void started() {
		if(BuildConfig.DEBUG)Log.d(TAG, "CDTS updating for new started service");
		if(getController().getAppState().getPlayBackState().getRunningService() != null) {
			if(BuildConfig.DEBUG)Log.d(TAG, "CDTS updating for new started service: " + getController().getAppState().getPlayBackState().getRunningService().getServiceLabel());

			for(RadioService srv : getController().getAppState().getPlayBackState().getRunningService().getRadioServices()) {
				if(srv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
					RadioServiceDabEdi ediSrv = (RadioServiceDabEdi)srv;
					if(ediSrv.sbtEnabled()) {
						if(BuildConfig.DEBUG)Log.d(TAG, "CDTS started service is SBT enabled");
						updateCdts(ediSrv.getSbtToken(), ediSrv.getUrl());
					} else {
						if(BuildConfig.DEBUG)Log.d(TAG, "CDTS started service is NOT SBT enabled");
					}

					break;
				}
			}
		} else {
			if(BuildConfig.DEBUG)Log.d(TAG, "CDTS started service null");
		}

		runOnUiThread(()-> {
			hideProgressIndicator();
			if (!isInCarMode && mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
				this.switchToPlayer();
				if(getController().getAppState().getPlayBackState().getRunningService() != null) {
					this.toolbar.setTitle(getController().getAppState().getPlayBackState().getRunningService().getServiceLabel());
				}
			}
		});
	}

	@Override
	public void stopped() {
		hideProgressIndicator();
	}

	@Override
	public void paused() {
		hideProgressIndicator();
	}

	@Override
	public void playProgress(long current, long total) {
		//  hideProgressIndicator();
	}

	@Override
	public void playProgressRealtime(long realTimePosix, long streamTimePosix, long curPos, long totalDuration) {
		//if(BuildConfig.DEBUG)Log.d(TAG, "SbtRealTime: " + new Date(realTimePosix) + " : " + new Date(streamTimePosix));
	}

	@Override
	public void sbtSeeked() {

	}

	@Override
	public void textualContent(TextData content) {
	}

	@Override
	public void visualContent(ImageData visual) {
	}

	@Override
	public void started(SubstitutionItem substitution) {
		hideProgressIndicator();
	}

	@Override
	public void stopped(SubstitutionItem substitution) {
		hideProgressIndicator();
	}

	@Override
	public void playProgress(SubstitutionItem substitution, long current, long total) {
	}

	@Override
	public void skipItemAdded(SkipItem skipItem) {
	}

	@Override
	public void itemStarted(SkipItem skipItem) {
	}


	@Override
	public void onPlayClicked() {
		showProgressIndicator(true);
		getController().onPlayClicked();
	}

	@Override
	public void skipItemRemoved(SkipItem skipItem) {

	}

	@Override
	public void onPauseClicked() {
		showProgressIndicator(true);
		getController().onPauseClicked();
	}

	@Override
	public void onSkipNext(Activity activity) {
		getController().onSkipNext(activity);
	}

	@Override
	public void onSubstitutePodcast(Activity activity) {
		showProgressIndicator(true);
		getController().onSubstitutePodcast(activity);
	}

	@Override
	public void onSkipBack() {
		getController().onSkipBack();
	}

	@Override
	public void onJumpToLive() {
		getController().onJumpToLive();
	}

	@Override
	public void seekTo(long progress) {
		getController().seekTo(progress);
	}

	@Override
	public void registerPlayBackListener(PlayBackListener listener) {
		getController().registerPlayBackListener(listener);
	}

	@Override
	public void unregisterPlayBackListener(PlayBackListener listener) {
		getController().unregisterPlayBackListener(listener);
	}

	@Override
	public TrackLikeService getTrackLikeService() {
		return getController().getTrackLikeService();
	}

	@Override
	public void retrieveAvailableRecommenders(OnSearchResultListener<Recommender[]> resultListener) {
		getController().retrieveAvailableRecommenders(resultListener);
	}

	@Override
	public void openPreferenceView() {
		this.showDataCollectionDialog();
	}

	@Override
	public void openWeburlConfigFileChooser() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			Intent intent = new Intent()
					.setType("*/*")
					.setAction(Intent.ACTION_GET_CONTENT);

			startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_web_url_config)), FILE_CHOOSE_REQUEST_CODE);
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
		}

	}

	@Override
	public void getAllPlayLists(OnSearchResultListener<List<TrackLikeService.PlayList>> resultListener) {
		if (getTrackLikeService() != null)
			getTrackLikeService().getAllPlaylists(resultListener);
		else
			onError(new GeneralError(GeneralError.SUBSTITUTION_ERROR));
	}

	@Override
	public void removeSubstitutionPlayList() {
		getController().removeSubstitutionPlayList();
	}

	@Override
	public void setSubstitutionPlayList(TrackLikeService.PlayList playList) {
		getController().setSubstitutionPlayList(playList);
	}

	@Override
	public void updateSubstitutionProvider() {
		SubstitutionProviderType substitutionProviderType = SubstitutionProviderType.values()[SharedPreferencesHelper.getInt(this, SharedPreferencesHelper.SUBSTITUTION_PROVIDER_TYPE, 0)];
		enableSubstitutionProvider(substitutionProviderType);
	}

	@Override
	public void loadPodcasts(List<String> urls) {
		getController().loadPodcasts(this, urls);
	}

	@Override
	public void cdtsUsernameUpdated() {
		if(BuildConfig.DEBUG)Log.d(TAG, "CDTS Username updated");
		getCdtsStatus();
	}

	@Override
	public void cdtsUsernameDeleted() {
		if(BuildConfig.DEBUG)Log.d(TAG, "Deleting CDTS Username");
		deleteCdts();
	}
}

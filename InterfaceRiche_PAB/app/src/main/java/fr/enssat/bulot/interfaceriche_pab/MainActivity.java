package fr.enssat.bulot.interfaceriche_pab;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private Uri mVideoUri;

    private WebView mWebView;

    private MapView mMapView;
    private JSONArray mWaypoints;
    private static final String MAPVIEW_BUNDLE_KEY = "macle";
    private static final String JSON_LAT = "lat";
    private static final String JSON_LNG = "lng";
    private static final String JSON_LABEL = "label";
    private static final String JSON_TIMESTAMP = "timestamp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.mapview);
        mMapView.onCreate(mapViewBundle);

        initVideoView();
        initChapters();
        initImb();

        initMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    private void initVideoView() {
        mVideoView = (VideoView)findViewById(R.id.videoview);
        try {
            MediaController mediacontroller = new MediaController(this);
            mediacontroller.setAnchorView(mVideoView);
            mVideoUri = Uri.parse(getString(R.string.video_url));
            mVideoView.setMediaController(mediacontroller);
            mVideoView.setVideoURI(mVideoUri);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mVideoView.start();
            }
        });
    }

    private View.OnClickListener chaptersListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = (int)v.getTag();
            mVideoView.seekTo(position);
        }
    };

    private void initChapters(){
        InputStream inputStream = getResources().openRawResource(R.raw.chapters);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
            JSONArray jArray = jObject.getJSONArray("Chapters");
            mWaypoints = jObject.getJSONArray("Waypoints");
            int pos = 0;
            String title = "";
            LinearLayout chapters = (LinearLayout)findViewById(R.id.chapters);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            for (int i = 0; i < jArray.length(); i++) {
                pos = jArray.getJSONObject(i).getInt("pos");
                title = jArray.getJSONObject(i).getString("title");
                Button button = new Button(this);
                button.setTag(pos * 1000);
                button.setText(title);
                button.setLayoutParams(layoutParams);
                button.setOnClickListener(chaptersListener);
                chapters.addView(button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initImb() {
        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(getString(R.string.imdb_url));
    }

    private void initMap(){
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    long lat = 0;
                    long lng = 0;
                    String label = "";
                    int timestamp = 0;
                    for (int i = 0; i < mWaypoints.length(); i++) {
                        lat = mWaypoints.getJSONObject(i).getLong(JSON_LAT);
                        lng = mWaypoints.getJSONObject(i).getLong(JSON_LNG);
                        label = mWaypoints.getJSONObject(i).getString(JSON_LABEL);
                        timestamp = mWaypoints.getJSONObject(i).getInt(JSON_TIMESTAMP);
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat,lng))
                                .title(label));
                        marker.setTag(timestamp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int timestamp = (int)marker.getTag();
                        mVideoView.seekTo(timestamp * 1000);
                        return false;
                    }
                });
            }
        });
    }
}

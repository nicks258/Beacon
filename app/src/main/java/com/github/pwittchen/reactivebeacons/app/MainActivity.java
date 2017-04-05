/*
 * Copyright (C) 2015 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pwittchen.reactivebeacons.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.pwittchen.reactivebeacons.R;
import com.github.pwittchen.reactivebeacons.library.rx2.Beacon;
import com.github.pwittchen.reactivebeacons.library.rx2.Proximity;
import com.github.pwittchen.reactivebeacons.library.rx2.ReactiveBeacons;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.logging.Logger;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends Activity implements BeaconConsumer,RangeNotifier {
  private static final boolean IS_AT_LEAST_ANDROID_M =
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000;
  private static final String ITEM_FORMAT = "MAC: %s, RSSI: %d\ndistance: %.2fm, proximity: %s\n%s";
  private BeaconManager mBeaconManager;
  private String beaconName ="";
  private ReactiveBeacons reactiveBeacons;
  private Disposable subscription;
  private String TAG = "MainActivity";
  private ListView lvBeacons;
  private Map<String, Beacon> beacons;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    lvBeacons = (ListView) findViewById(R.id.lv_beacons);
    reactiveBeacons = new ReactiveBeacons(this);
    beacons = new HashMap<>();
  }

  @Override protected void onResume() {
    super.onResume();
    mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
    // Detect the URL frame:
    mBeaconManager.getBeaconParsers().add(new BeaconParser().
            setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
    mBeaconManager.bind(this);
    if (!canObserveBeacons()) {
      return;
    }

    startSubscription();
  }

  private void startSubscription() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      requestCoarseLocationPermission();
      return;
    }

    subscription = reactiveBeacons.observe()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Beacon>() {
          @Override public void accept(@NonNull Beacon beacon) throws Exception {
            beacons.put(beacon.device.getAddress(), beacon);
//            refreshBeaconList();
          }
        });
  }

  private boolean canObserveBeacons() {
    if (!reactiveBeacons.isBleSupported()) {
      Toast.makeText(this, "BLE is not supported on this device", Toast.LENGTH_SHORT).show();
      return false;
    }

    if (!reactiveBeacons.isBluetoothEnabled()) {
      reactiveBeacons.requestBluetoothAccess(this);
      return false;
    } else if (!reactiveBeacons.isLocationEnabled(this)) {
      reactiveBeacons.requestLocationAccess(this);
      return false;
    } else if (!isFineOrCoarseLocationPermissionGranted() && IS_AT_LEAST_ANDROID_M) {
      requestCoarseLocationPermission();
      return false;
    }

    return true;
  }

//  private void refreshBeaconList() {
//    List<String> list = new ArrayList<>();
//
//    for (Beacon beacon : beacons.values()) {
//      list.add(getBeaconItemString(beacon));
//    }
//
//    int itemLayoutId = android.R.layout.simple_list_item_1;
//    lvBeacons.setAdapter(new ArrayAdapter<>(this, itemLayoutId, list));
//  }

  private String getBeaconItemString(Beacon beacon) {
    String mac = beacon.device.getAddress();
    int rssi = beacon.rssi;
    double distance = beacon.getDistance();
    Proximity proximity = beacon.getProximity();
    String name = beacon.device.getName();
    Log.i("Name ->" ,beaconName);
    return String.format(ITEM_FORMAT, mac, rssi, distance, proximity, beaconName);
  }

//  @Override protected void onPause() {
//    super.onPause();
//    if (subscription != null && !subscription.isDisposed()) {
//      subscription.dispose();
//    }
//  }

  @Override public void onRequestPermissionsResult(int requestCode,
      @android.support.annotation.NonNull String[] permissions,
      @android.support.annotation.NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    final boolean isCoarseLocation = requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION;
    final boolean permissionGranted = grantResults[0] == PERMISSION_GRANTED;

    if (isCoarseLocation && permissionGranted && subscription == null) {
      startSubscription();
    }
  }

  private void requestCoarseLocationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(new String[] { ACCESS_COARSE_LOCATION },
          PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
    }
  }

  private boolean isFineOrCoarseLocationPermissionGranted() {
    boolean isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    boolean isFineLocationPermissionGranted = isGranted(ACCESS_FINE_LOCATION);
    boolean isCoarseLocationPermissionGranted = isGranted(ACCESS_COARSE_LOCATION);

    return isAndroidMOrHigher && (isFineLocationPermissionGranted
        || isCoarseLocationPermissionGranted);
  }

  private boolean isGranted(String permission) {
    return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
  }



  public void onBeaconServiceConnect() {
    Region region = new Region("all-beacons-region", null, null, null);
    try {
      mBeaconManager.startRangingBeaconsInRegion(region);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    mBeaconManager.setRangeNotifier(this);
  }

  @Override
  public void didRangeBeaconsInRegion(Collection<org.altbeacon.beacon.Beacon> beacons, Region region) {
    final List<String> list = new ArrayList<>();

    for (org.altbeacon.beacon.Beacon beacon : beacons) {
      String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
      Log.d(TAG, "I see a beacon transmitting a url: " + url +
              " approximately " + beacon.getDistance() + " meters away.");
      DefaultHttpClient httpClient = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet("https://www.youtube.com");
      ResponseHandler<String> resHandler = new BasicResponseHandler();
      try {
        String page = httpClient.execute(httpGet, resHandler);
        Log.i("WEBSITE INFO->>",page);
      } catch (IOException e) {
        e.printStackTrace();
      }
      list.add(url);
    }

    final int itemLayoutId = android.R.layout.simple_list_item_1;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        lvBeacons.setAdapter(new ArrayAdapter<>(MainActivity.this, itemLayoutId, list));

      }
    });
//    lvBeacons.setAdapter(new ArrayAdapter<>(this, itemLayoutId, list));
//    for (org.altbeacon.beacon.Beacon beacon : beacons) {
//      if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
//        // This is a Eddystone-URL frame
//        String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
////                String beaconName = beacon.getBluetoothName();
//
//
////        Logger.i("yoooo"+modalClass.getID());
////        Logger.i(beaconName);
//// TODO AUTOMATIC FUNCTION
//// runOnUiThread(new Runnable() {
////          @Override
////          public void run() {
//////            beaconTv = (TextView) findViewById(R.id.beacon);
//////            beaconTv.setText(beaconName);
////          }
////        });
//
//        String beaconDetail = beacon.getParserIdentifier()+ " " + beacons.toString() + " " +
//                beacon.describeContents() + " " + beacon.getServiceUuid()+ " " + beacon.getId1() ;
////        Logger.i(beaconDetail);

//        Identifier namespaceId = beacon.getId1();
//        beaconName = url;
////                Identifier instanceId = beacon.getId2();
//        Log.d(TAG, "I see a beacon transmitting namespace id: "+namespaceId+
//                " and instance id: "+
//                " approximately "+beacon.getDistance()+" meters away.");
//      }
//    }
  }



  @Override
  public void onPause() {
    super.onPause();
    mBeaconManager.unbind(this);
  }
}

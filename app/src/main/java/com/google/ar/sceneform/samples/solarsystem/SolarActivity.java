/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.solarsystem;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.csv.*;


/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
public class SolarActivity extends AppCompatActivity {

  private static final int RC_PERMISSIONS = 0x123;
  private boolean installRequested;

  private GestureDetector gestureDetector;
  private Snackbar loadingMessageSnackbar = null;
  private static Snackbar cityInfoSnackbar = null;

  private ArSceneView arSceneView;

  private ModelRenderable sunRenderable;
  private ModelRenderable mercuryRenderable;
  private ModelRenderable venusRenderable;
  private ModelRenderable earthRenderable;
  private ModelRenderable lunaRenderable;
  private ModelRenderable marsRenderable;
  private ModelRenderable markerRenderable;
  private ModelRenderable jupiterRenderable;
  private ModelRenderable saturnRenderable;
  private ModelRenderable uranusRenderable;
  private ModelRenderable neptuneRenderable;
  private ViewRenderable solarControlsRenderable;

  private final SolarSettings solarSettings = new SolarSettings();

  // True once scene is loaded
  private boolean hasFinishedLoading = false;

  // True once the scene has been placed.
  private boolean hasPlacedSolarSystem = false;

  // Astronomical units to meters ratio. Used for positioning the planets of the solar system.
  private static final float AU_TO_METERS = 0.5f;

  public static final float EARTH_RADIUS = 0.53f;

  public static String cityInfo = "";

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    if (!DemoUtils.checkIsSupportedDeviceOrFinish(this)) {
      // Not a supported device.
      return;
    }

    setContentView(R.layout.activity_solar);
    arSceneView = findViewById(R.id.ar_scene_view);

    cityInfoSnackbar = Snackbar.make(
            SolarActivity.this.findViewById(android.R.id.content),
            "",
            Snackbar.LENGTH_INDEFINITE);

    // Build all the planet models.
    CompletableFuture<ModelRenderable> sunStage =
        ModelRenderable.builder().setSource(this, Uri.parse("earth_obj.sfb")).build();
//    CompletableFuture<ModelRenderable> mercuryStage =
//        ModelRenderable.builder().setSource(this, Uri.parse("Mercury.sfb")).build();
//    CompletableFuture<ModelRenderable> venusStage =
//        ModelRenderable.builder().setSource(this, Uri.parse("Venus.sfb")).build();
//    CompletableFuture<ModelRenderable> earthStage =
//        ModelRenderable.builder().setSource(this, Uri.parse("Earth.sfb")).build();
    CompletableFuture<ModelRenderable> lunaStage =
        ModelRenderable.builder().setSource(this, Uri.parse("Luna.sfb")).build();
    CompletableFuture<ModelRenderable> marsStage =
        ModelRenderable.builder().setSource(this, Uri.parse("Mars.sfb")).build();
//    CompletableFuture<ModelRenderable> jupiterStage =
//        ModelRenderable.builder().setSource(this, Uri.parse("Jupiter.sfb")).build();
//    CompletableFuture<ModelRenderable> saturnStage =
//        ModelRenderable.builder().setSource(this, Uri.parse("Saturn.sfb")).build();
//    CompletableFuture<ModelRenderable> uranusStage =
//        ModelRenderable.builder().setSource(this, Uri.parse("Uranus.sfb")).build();
//    CompletableFuture<ModelRenderable> neptuneStage =
//        ModelRenderable.builder().setSource(this, Uri.parse("Neptune.sfb")).build();

    CompletableFuture<ModelRenderable> markerStage =
            ModelRenderable.builder().setSource(this, Uri.parse("model.sfb")).build();

    // Build a renderable from a 2D View.
//    CompletableFuture<ViewRenderable> solarControlsStage =
//        ViewRenderable.builder().setView(this, R.layout.solar_controls).build();

    CompletableFuture.allOf(
            sunStage,
//            mercuryStage,
//            venusStage,
//            earthStage,
            lunaStage,
            marsStage,
            markerStage
//            jupiterStage,
//            saturnStage,
//            uranusStage,
//            neptuneStage,
//            solarControlsStage
    )
        .handle(
            (notUsed, throwable) -> {
              // When you build a Renderable, Sceneform loads its resources in the background while
              // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
              // before calling get().

              if (throwable != null) {
                DemoUtils.displayError(this, "Unable to load renderable", throwable);
                return null;
              }

              try {
                sunRenderable = sunStage.get();
//                mercuryRenderable = mercuryStage.get();
//                venusRenderable = venusStage.get();
//                earthRenderable = earthStage.get();
                lunaRenderable = lunaStage.get();
                marsRenderable = marsStage.get();
                markerRenderable = markerStage.get();
//                jupiterRenderable = jupiterStage.get();
//                saturnRenderable = saturnStage.get();
//                uranusRenderable = uranusStage.get();
//                neptuneRenderable = neptuneStage.get();
//                solarControlsRenderable = solarControlsStage.get();

                // Everything finished loading successfully.
                hasFinishedLoading = true;

              } catch (InterruptedException | ExecutionException ex) {
                DemoUtils.displayError(this, "Unable to load renderable", ex);
              }

              return null;
            });

    // Set up a tap gesture detector.
    gestureDetector =
        new GestureDetector(
            this,
            new GestureDetector.SimpleOnGestureListener() {
              @Override
              public boolean onSingleTapUp(MotionEvent e) {
                onSingleTap(e);
                return true;
              }

              @Override
              public boolean onDown(MotionEvent e) {
                return true;
              }
            });

    // Set a touch listener on the Scene to listen for taps.
    arSceneView
        .getScene()
        .setOnTouchListener(
            (HitTestResult hitTestResult, MotionEvent event) -> {
              // If the solar system hasn't been placed yet, detect a tap and then check to see if
              // the tap occurred on an ARCore plane to place the solar system.
              if (!hasPlacedSolarSystem) {
                return gestureDetector.onTouchEvent(event);
              }

              // Otherwise return false so that the touch event can propagate to the scene.
              return false;
            });

    // Set an update listener on the Scene that will hide the loading message once a Plane is
    // detected.
    arSceneView
        .getScene()
        .addOnUpdateListener(
            frameTime -> {
              if (loadingMessageSnackbar == null) {
                return;
              }

              Frame frame = arSceneView.getArFrame();
              if (frame == null) {
                return;
              }

              if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                return;
              }

              for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                if (plane.getTrackingState() == TrackingState.TRACKING) {
                  hideLoadingMessage();
                }
              }
            });

    // Lastly request CAMERA permission which is required by ARCore.
    DemoUtils.requestCameraPermission(this, RC_PERMISSIONS);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (arSceneView == null) {
      return;
    }

    if (arSceneView.getSession() == null) {
      // If the session wasn't created yet, don't resume rendering.
      // This can happen if ARCore needs to be updated or permissions are not granted yet.
      try {
        Session session = DemoUtils.createArSession(this, installRequested);
        if (session == null) {
          installRequested = DemoUtils.hasCameraPermission(this);
          return;
        } else {
          arSceneView.setupSession(session);
        }
      } catch (UnavailableException e) {
        DemoUtils.handleSessionException(this, e);
      }
    }

    try {
      arSceneView.resume();
    } catch (CameraNotAvailableException ex) {
      DemoUtils.displayError(this, "Unable to get camera", ex);
      finish();
      return;
    }

    if (arSceneView.getSession() != null) {
      showLoadingMessage();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (arSceneView != null) {
      arSceneView.pause();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (arSceneView != null) {
      arSceneView.destroy();
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
    if (!DemoUtils.hasCameraPermission(this)) {
      if (!DemoUtils.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        DemoUtils.launchPermissionSettings(this);
      } else {
        Toast.makeText(
                this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
            .show();
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      // Standard Android full-screen functionality.
      getWindow()
          .getDecorView()
          .setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  private void onSingleTap(MotionEvent tap) {
    if (!hasFinishedLoading) {
      // We can't do anything yet.
      return;
    }

    Frame frame = arSceneView.getArFrame();
    if (frame != null) {
      if (!hasPlacedSolarSystem && tryPlaceSolarSystem(tap, frame)) {
        hasPlacedSolarSystem = true;
      }
    }
  }

  private boolean tryPlaceSolarSystem(MotionEvent tap, Frame frame) {
    if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
      for (HitResult hit : frame.hitTest(tap)) {
        Trackable trackable = hit.getTrackable();
        if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
          // Create the Anchor.
          Anchor anchor = hit.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arSceneView.getScene());
          Node solarSystem = createSolarSystem();
          anchorNode.addChild(solarSystem);
          return true;
        }
      }
    }

    return false;
  }

//  private void addMarker(City city, Node sunVisual) {
//    Node newCity = new Node();
//    newCity.setParent(sunVisual);
//    newCity.setRenderable(markerRenderable);
//    newCity.setLocalPosition(new Vector3((float) city.x, 0.5f + (float) city.y, (float) city.z));
//    newCity.setLocalScale(new Vector3(0.02f, 0.02f, 0.02f));
//  }

  private Node createSolarSystem() {
    Log.i("SOHACKS", "createSolarSystem function");
    Reader csv_file = null;
    List<CSVRecord> records = new ArrayList<>();

    try {
//      csv_file = new FileReader("../../../../../../../../../sampledata/data/worldcities.csv");
      csv_file = new InputStreamReader(getAssets().open("worldcities.csv"));
      records = CSVFormat.EXCEL.withHeader().parse(csv_file).getRecords();
      Log.i("CSV STATUS", "Found csv file successfully");
      Log.i("SOHACKS", "Found csv file successfully");
    }
    catch (Exception e) {
        Log.i("SOHACKS", "ERRROR");
        if (e.equals(FileNotFoundException.class)) {
        throw new IllegalArgumentException("File not found");
      }else if (e.equals(IOException.class)) {
        throw new IllegalArgumentException("CSV format cannot be passed");
      }
    }

    Node base = new Node();

//    Node sun = new Node();
//    sun.setParent(base);
//    sun.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));

    Node sunVisual = new Node();
    sunVisual.setParent(base);
    sunVisual.setRenderable(sunRenderable);
    sunVisual.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));
    sunVisual.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

    Node luna = new Node();
    luna.setParent(sunVisual);
    luna.setRenderable(lunaRenderable);
    luna.setLocalPosition(new Vector3(1.0f, 0.0f, 0.0f));
    luna.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));

    Node mars = new Node();
    mars.setParent(sunVisual);
    mars.setRenderable(marsRenderable);
    mars.setLocalPosition(new Vector3(0.0f, 0.5f, EARTH_RADIUS));
    mars.setLocalScale(new Vector3(0.01f, 0.01f, 0.01f));

//    for (City c:cities) {
//      addMarker(c, sunVisual);
//    }

    ArrayList<City> cities = new ArrayList<>();
    for (CSVRecord record : records) {
      if (record.get("capital").equals("primary")) {
        try {
          // Create the planet and position it relative to the sun.
          City newCity =
                  new City(
                          this, markerRenderable, cityInfoSnackbar, record.get("city"), Double.parseDouble(record.get("lat")), Double.parseDouble(record.get("lng")), Long.parseLong(record.get("population")), (double)EARTH_RADIUS, 0.1f);
          newCity.setParent(sunVisual);
          newCity.setLocalPosition(new Vector3((float) newCity.getX(), (float) newCity.getY()+0.5f, (float) newCity.getZ()));
          //cities.add(new City(record.get("city"), Double.parseDouble(record.get("lat")), Double.parseDouble(record.get("lng")), Double.parseDouble(record.get("population")), EARTH_RADIUS));
        }
        catch (Exception e){


        }

      }
    }

//    Node solarControls = new Node();
//    solarControls.setParent(sun);
//    solarControls.setRenderable(solarControlsRenderable);
//    solarControls.setLocalPosition(new Vector3(0.0f, 0.25f, 0.0f));

//    View solarControlsView = solarControlsRenderable.getView();
//    SeekBar orbitSpeedBar = solarControlsView.findViewById(R.id.orbitSpeedBar);
//    orbitSpeedBar.setProgress((int) (solarSettings.getOrbitSpeedMultiplier() * 10.0f));
//    orbitSpeedBar.setOnSeekBarChangeListener(
//        new SeekBar.OnSeekBarChangeListener() {
//          @Override
//          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            float ratio = (float) progress / (float) orbitSpeedBar.getMax();
//            solarSettings.setOrbitSpeedMultiplier(ratio * 10.0f);
//          }
//
//          @Override
//          public void onStartTrackingTouch(SeekBar seekBar) {}
//
//          @Override
//          public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//
//    SeekBar rotationSpeedBar = solarControlsView.findViewById(R.id.rotationSpeedBar);
//    rotationSpeedBar.setProgress((int) (solarSettings.getRotationSpeedMultiplier() * 10.0f));
//    rotationSpeedBar.setOnSeekBarChangeListener(
//        new SeekBar.OnSeekBarChangeListener() {
//          @Override
//          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            float ratio = (float) progress / (float) rotationSpeedBar.getMax();
//            solarSettings.setRotationSpeedMultiplier(ratio * 10.0f);
//          }
//
//          @Override
//          public void onStartTrackingTouch(SeekBar seekBar) {}
//
//          @Override
//          public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
//
//    // Toggle the solar controls on and off by tapping the sun.
////    sunVisual.setOnTapListener(
////        (hitTestResult, motionEvent) -> solarControls.setEnabled(!solarControls.isEnabled()));
//
//    createPlanet("Mercury", sun, 0.4f, 47f, mercuryRenderable, 0.019f, 0.03f);
//
//    createPlanet("Venus", sun, 0.7f, 35f, venusRenderable, 0.0475f, 2.64f);
//
//    Node earth = createPlanet("Earth", sun, 1.0f, 29f, earthRenderable, 0.05f, 23.4f);
//
//    createPlanet("Moon", earth, 0.15f, 100f, lunaRenderable, 0.018f, 6.68f);
//
//    createPlanet("Mars", sun, 1.5f, 24f, marsRenderable, 0.0265f, 25.19f);
//
//    createPlanet("Jupiter", sun, 2.2f, 13f, jupiterRenderable, 0.16f, 3.13f);
//
//    createPlanet("Saturn", sun, 3.5f, 9f, saturnRenderable, 0.1325f, 26.73f);
//
//    createPlanet("Uranus", sun, 5.2f, 7f, uranusRenderable, 0.1f, 82.23f);
//
//    createPlanet("Neptune", sun, 6.1f, 5f, neptuneRenderable, 0.074f, 28.32f);

    return base;
  }

  private Node createPlanet(
      String name,
      Node parent,
      float auFromParent,
      float orbitDegreesPerSecond,
      ModelRenderable renderable,
      float planetScale,
      float axisTilt) {
    // Orbit is a rotating node with no renderable positioned at the sun.
    // The planet is positioned relative to the orbit so that it appears to rotate around the sun.
    // This is done instead of making the sun rotate so each planet can orbit at its own speed.
    RotatingNode orbit = new RotatingNode(solarSettings, true, false, 0);
    orbit.setDegreesPerSecond(orbitDegreesPerSecond);
    orbit.setParent(parent);

    // Create the planet and position it relative to the sun.
    Planet planet =
        new Planet(
            this, name, planetScale, orbitDegreesPerSecond, axisTilt, renderable, solarSettings);
    planet.setParent(orbit);
    planet.setLocalPosition(new Vector3(auFromParent * AU_TO_METERS, 0.0f, 0.0f));

    return planet;
  }

  private void showLoadingMessage() {
    if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
      return;
    }

    loadingMessageSnackbar =
        Snackbar.make(
            SolarActivity.this.findViewById(android.R.id.content),
            R.string.plane_finding,
            Snackbar.LENGTH_INDEFINITE);
    loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
    loadingMessageSnackbar.show();
  }

  private void hideLoadingMessage() {
    if (loadingMessageSnackbar == null) {
      return;
    }

    loadingMessageSnackbar.dismiss();
    loadingMessageSnackbar = null;
  }

  public static void updateInfoWindow(String s) {
    if (cityInfoSnackbar == null) {
      return;
    }

    cityInfo = s;
    cityInfoSnackbar.setText(cityInfo);

    cityInfoSnackbar.getView().setBackgroundColor(0xbf323232);
    cityInfoSnackbar.show();
  }

  public void hideInfoWindow() {
    if (cityInfoSnackbar == null) {
      return;
    }
    cityInfoSnackbar.dismiss();
    cityInfoSnackbar = null;
  }
}

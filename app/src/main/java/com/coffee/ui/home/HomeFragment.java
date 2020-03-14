package com.coffee.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.coffee.LocationListener;
import com.coffee.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback {


    MapView mMapView;
    GoogleMap googleMap ;



    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {


           googleMap.clear();

           //aici bagi markeru
            if (LocationListener.location != null) {
                LatLng Loc = new LatLng(LocationListener.location.getLatitude(), LocationListener.location.getLongitude());
                LocationListener myLoc = new LocationListener();
                LocationManager lm = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);


                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 3 , myLoc);
                Marker marker =
                        googleMap.addMarker(new MarkerOptions().position(Loc).title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .draggable(false).visible(true));
            }

        }
    }
    @SuppressLint("WrongViewCast")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.user_list_map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(Objects.requireNonNull(HomeFragment.this.getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*double latitude = 17.385044;
        double longitude = 78.486671;
        // create marker
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude, longitude)).title("Hello Maps");

        // Changing marker icon
        marker.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));*/

        // adding marker

        /*googleMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(17.385044, 78.486671)).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

        mMapView.getMapAsync(this);
        CheckUserPermsions();
         //latitude and longitude



        // Perform any camera updates here
        return v;

    }

    //access to permissions
    public void CheckUserPermsions(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(HomeFragment.this.getContext()), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }

        runlistener();// init the contact list

    }
    //get access to user permission
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runlistener();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( HomeFragment.this.getContext(),"No can do" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @SuppressLint("MissingPermission")
    void runlistener() {
        LocationListener myLoc = new LocationListener();
        LocationManager lm = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);


        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 3 , myLoc);
        /*MyThread MyT= new MyThread();
        MyT.start();*/
    }
    public class MyThread extends Thread{
        public void run(){
            /*while(true){

                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        googleMap.clear();
                        if (LocationListener.location != null) {
                            LatLng Loc = new LatLng(LocationListener.location.getLatitude(), LocationListener.location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(Loc).title("Your Location"));

                        }
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }*/
        }
    }






    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


}


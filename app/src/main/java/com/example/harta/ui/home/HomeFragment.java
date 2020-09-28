package com.example.harta.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.harta.Cafenea;
import com.example.harta.Cafenele;
import com.example.harta.MapStateManager;
import com.example.harta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    static ArrayList<Marker> chunk = new ArrayList<>();
    public
    double latitudine;
    static CameraPosition pozitie;
    double longitudine;
    String fileName = "yourFileName";
    boolean json;
    boolean fisier;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "Update";
    @SuppressLint("StaticFieldLeak")
    static ViewFlipper viewFlipper;
    ArrayList<Cafenea> cache = new ArrayList<>();
    static ArrayList<Marker> mrk = new ArrayList<>();
    static LocationCallback locationCallback;
    static ArrayList<Cafenea> rezultat;
    static Boolean requestingLocationUpdates = true;
    static MapStateManager mgr;
    CameraPosition position;
    // short delay = 150;
    //Variabile globale
    private
    static GoogleMap googleMap;
    Marker srch = null;
    ListView lv;
    TextView negasit;
    MapView mMapView;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    fetchLastLocation fll = new fetchLastLocation();


    ValueEventListener cacheEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Cafenea cafenea = postSnapshot.getValue(Cafenea.class);
                    assert cafenea != null;

                    try {
                        if (cafenea.getAddress().contains(getCityName(currentLocation.getLatitude(), currentLocation.getLongitude()))) {
                            cache.add(cafenea);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };
    ValueEventListener CameraEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                //Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Cafenea cafenea = postSnapshot.getValue(Cafenea.class);
                    assert cafenea != null;

                    try {
                        if (cafenea.getAddress().contains(getCityName(pozitie.target.latitude, pozitie.target.longitude))) {
                            chunk.add(googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                    .title(cafenea.getName())
                                    .snippet(cafenea.getAddress())
                                    .icon(BitmapDescriptorFactory.defaultMarker
                                            (BitmapDescriptorFactory.HUE_AZURE))));

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    private String getCityName(double latitude, double longitude) throws IOException {
        String myCity;
        Geocoder geocoder = new Geocoder(HomeFragment.this.requireContext(), Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        myCity = addresses.get(0).getLocality();
        return myCity;
    }

    public void verificareJson() throws IOException {
        FileInputStream fis = HomeFragment.this.requireContext().openFileInput(fileName);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(isr);
        File file = HomeFragment.this.requireContext().getFileStreamPath(fileName);
        if (file.exists()) {
            fisier = true;
        }
        if (bufferedReader.readLine() != null) {
            json = true;
        }
        Log.e("Fisier Exista", "" + fisier);
        Log.e("Fisier e scris", "" + json);
    }

    public void Scriere(String fileName) {


        databaseCafea.addListenerForSingleValueEvent(cacheEventListener);
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            HomeFragment.this.getContext();
            FileOutputStream fos = null;
            try {
                fos = HomeFragment.this.requireContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.e("CACHE", "" + cache.size());

            assert fos != null;
            OutputStreamWriter out = new OutputStreamWriter(fos);
            JsonWriter writer = new JsonWriter(out);
            //set indentation for pretty print
            writer.setIndent("\t");
            //start writing
            try {
                writer.beginObject(); //{
                writer.name("Cafenele").beginArray();
                for (Cafenea cafenea : cache) {
                    Log.e("Cache", "" + cafenea.getName());
                    writer.beginObject(); //{

                    writer.name("Address").value(cafenea.getAddress()); // "id": 123
                    writer.name("Latitude").value(cafenea.getLatitude()); // "name": "David"
                    writer.name("Longitude").value(cafenea.getLongitude()); // "permanent": false
                    writer.name("id").value(cafenea.getId());
                    writer.name("name").value(cafenea.getName());// "address": {
                    writer.endObject(); // }
                }
                writer.endArray(); // ]
                writer.endObject(); // }
                writer.flush();

                //close writer
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }, 650);
    }


    public void read_file(@NonNull Context context, String filename, ArrayList<Cafenea> cache) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            Gson g = new Gson();
            Cafenele cafenele = g.fromJson(String.valueOf(sb), Cafenele.class);

            if (!cache.isEmpty()) {
                cache.clear();
            }
            cache.addAll(cafenele.getCafenele());
            Log.e("Marime cache", "" + cache.size());
            Log.e("Citire", "" + sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mrk.clear();
            if (dataSnapshot.exists()) {
                //Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Cafenea cafenea = postSnapshot.getValue(Cafenea.class);
                    assert cafenea != null;
                    if (currentLocation != null && cafenea.getLatitude() <= currentLocation.getLatitude() + 0.007 && cafenea.getLatitude() >= currentLocation.getLatitude() - 0.007) {
                        if (cafenea.getLongitude() <= currentLocation.getLongitude() + 0.007 && cafenea.getLongitude() >= currentLocation.getLongitude() - 0.007) {
                            mrk.add(googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                    .title(cafenea.getName())
                                    .snippet(cafenea.getAddress())
                                    .icon(BitmapDescriptorFactory.defaultMarker
                                            (BitmapDescriptorFactory.HUE_AZURE))));
                        }
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };
    DatabaseReference databaseCafea = FirebaseDatabase.getInstance().getReference("Cafenea");

    private void removeAllMarkers(ArrayList<Marker> AllMarkers) {
        for (Marker mLocationMarker : AllMarkers) {
            mLocationMarker.remove();
        }
        AllMarkers.clear();
    }

    SearchView searchView;

    //De aici incepe aplicata.Locul unde se creeaza fragmentul cu toate utilitatile sale(Main)

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        try {
            verificareJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        if (currentLocation == null && mMapView != null) {
            new Thread(fll).start();
            mMapView.getMapAsync(this);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeFragment.this.requireContext());

        ListenerForSingeAndMGR();


        if (!fisier && !json) {
            Scriere(fileName);
            read_file(HomeFragment.this.requireContext(), fileName, cache);
        }

        Back back = new Back();
        new Thread(back).start();

        lv = view.findViewById(R.id.result_list);
        Button center = view.findViewById(R.id.Center);
        center.setOnClickListener(this);
        searchView = view.findViewById(R.id.sv_location);
        updateValuesFromBundle(savedInstanceState);
        searchView.setQueryHint("Cauta ceva:");
        viewFlipper = view.findViewById(R.id.view_flipper);
        Button previous = view.findViewById(R.id.previous);
        previous.setOnClickListener(this);
        Button next = view.findViewById(R.id.next);
        next.setOnClickListener(this);
        negasit = view.findViewById(R.id.GasitNimic);

        //Apelare functii de cautare
        SetOnQuery();

        return view;
    }


    public void ListenerForSingeAndMGR() {
        if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!json) {
                databaseCafea.addListenerForSingleValueEvent(valueEventListener);
            } else {
                read_file(HomeFragment.this.requireContext(), fileName, cache);
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    for (Cafenea cafenea : cache) {
                        if (currentLocation != null && cafenea.getLatitude() <= currentLocation.getLatitude() + 0.007 && cafenea.getLatitude() >= currentLocation.getLatitude() - 0.007) {
                            if (cafenea.getLongitude() <= currentLocation.getLongitude() + 0.007 && cafenea.getLongitude() >= currentLocation.getLongitude() - 0.007) {
                                mrk.add(googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                        .title(cafenea.getName())
                                        .snippet(cafenea.getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker
                                                (BitmapDescriptorFactory.HUE_AZURE))));
                            }
                        }

                        //Log.e("Citire",""+cafenea.getLatitude()+" "+ cafenea.getLongitude());
                    }
                }, 500);
            }

        } else {
            Toast.makeText(HomeFragment.this.requireContext(), "Please enable location access", Toast.LENGTH_SHORT).show();
        }

        mgr = new MapStateManager(HomeFragment.this.requireContext());
        if (mgr.getSavedCameraPosition() != null) {
            position = mgr.getSavedCameraPosition();
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            }
        };
    }

    public void SetOnQuery() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                rezultat = new ArrayList<>();
                if (currentLocation != null) {
                    new FirebaseUserSearch().execute();
                } else {
                    Toast.makeText(HomeFragment.this.requireContext(), "Cannot detect user location", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new Async(HomeFragment.this).execute();
        mMapView.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    //Locul butoanelor
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous: {
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), R.anim.right);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();
                break;
            }
            case R.id.next: {
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_in_left);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_out_right);
                viewFlipper.showNext();
                break;
            }
            case R.id.Center: {
                if (currentLocation != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().
                            target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).
                            tilt(0).
                            zoom(16).
                            bearing(0).
                            build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    Toast.makeText(HomeFragment.this.getContext(), "Can't find location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Face update uri in timp real
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /* 60 secs */
        long UPDATE_INTERVAL = 60000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        /* 5 secs */
        long FASTEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        MapStateManager mgr = new MapStateManager(HomeFragment.this.requireContext());
        if (googleMap != null) {
            mgr.saveMapState(googleMap);
            stopLocationUpdates();
        }
    }

    //In caz ca se schimba limba/se roteste sa nu se distruga appul.E apelat in main(CreateView)

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
        UpdateUi();

    }

    public void UpdateUi() {
        if (isVisible()) {
            if (null != currentLocation) {
                currentLocation.getLongitude();
                currentLocation.getLatitude();
            }

        }
    }

    //------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;
        pozitie = googleMap.getCameraPosition();
        latitudine = pozitie.target.latitude;
        longitudine = pozitie.target.longitude;

        MapRdy mrd = new MapRdy();

        new Thread(mrd).start();

        Runnable r = new Runnable() {
            Handler rr = new Handler();

            @Override
            public void run() {
                rr.post(() -> googleMap.setOnCameraMoveListener(() -> {
                    //RAMAS : TREBUIE ORDONATE AMBELE LISTE INAINTE DE COMPARARE
                    pozitie = googleMap.getCameraPosition();
                    latitudine = pozitie.target.latitude;
                    longitudine = pozitie.target.longitude;

                    if (pozitie.zoom > 16.0 && chunk.isEmpty()) {
                        if (!json) {
                            databaseCafea.addListenerForSingleValueEvent(CameraEventListener);
                        } else {
                            for (Cafenea cafenea : cache) {
                                chunk.add(mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                        .title(cafenea.getName())
                                        .snippet(cafenea.getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker
                                                (BitmapDescriptorFactory.HUE_AZURE))));
                                Log.e("chunk", "" + chunk.get(0).getPosition().longitude);
                            }

                        }


                    } else if (pozitie.zoom < 15.0) {
                        removeAllMarkers(chunk);


                    }

                }));
            }
        };
        Thread map = new Thread(r);
        new Thread(map).start();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //OnBackPressedCallBack - setare functie buton de back
    class Back implements Runnable {
        Handler sh = new Handler();
        @Override
        public void run() {
            sh.post(() -> {
                OnBackPressedCallback callback = new OnBackPressedCallback(
                        true // default to enabled
                ) {
                    @Override
                    public void handleOnBackPressed() {
                        if (srch != null) {
                            srch.remove();
                            srch = null;
                        } else {
                            viewFlipper.showPrevious();
                        }

                    }
                };
                requireActivity().getOnBackPressedDispatcher().addCallback(
                        getViewLifecycleOwner(), // LifecycleOwner
                        callback);
            });
        }
    }
//70 ms


    static class Async extends AsyncTask<Void, Void, Void> {
        private final WeakReference<HomeFragment> wk;
        FusedLocationProviderClient fusedLocationProviderClient;
        Location currentLocation;
        CameraPosition position;
        int delay = 100;

        public Async(HomeFragment context) {
            this.wk = new WeakReference<>(context);
        }

        public int setDelay(int delay) {
            if (currentLocation == null) {
                delay += 20;
                final Handler handler = new Handler();
                int finalDelay = delay;
                handler.postDelayed(() -> setDelay(finalDelay), 0);
            } else {
                delay = 0;
            }
            return delay;
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... voids) {
            HomeFragment activity = wk.get();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity.requireContext());

            @SuppressLint("MissingPermission") final Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentLocation = task.getResult();

                }
            }).addOnFailureListener(e -> {
                Log.d("MapDemoActivity", "Error trying to get last GPS location");
                e.printStackTrace();
            });

            mgr = new MapStateManager(activity.requireContext());
            if (mgr.getSavedCameraPosition() != null) {
                position = mgr.getSavedCameraPosition();
            }

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                }
            };
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HomeFragment activity = wk.get();
            Handler ol = new Handler();
            ol.postDelayed(() -> {
                if (currentLocation != null && mgr.getSavedCameraPosition() != null && mgr.getSavedCameraPosition().target.latitude <= currentLocation.getLatitude() + 0.25 && mgr.getSavedCameraPosition().target.latitude >= currentLocation.getLatitude() - 0.25 && mgr.getSavedCameraPosition().target.longitude <= currentLocation.getLongitude() + 0.25 && mgr.getSavedCameraPosition().target.longitude >= currentLocation.getLongitude() - 0.25 && position != null) {
                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                    googleMap.moveCamera(update);
                    googleMap.setMapType(mgr.getSavedMapType());
                } else if (currentLocation != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), 15));
                } else {
                    Toast.makeText(activity.requireContext(), "S-a miscat prea incet", Toast.LENGTH_SHORT).show();
                }
            }, setDelay(delay));
        }

    }


    class MapRdy implements Runnable {
        Handler mr = new Handler();

        @Override
        public void run() {
            mr.post(() -> {
                // For showing a move to my location button
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    new Async(HomeFragment.this).execute();
                } else {
                    Toast.makeText(HomeFragment.this.requireContext(), "Please enable location access", Toast.LENGTH_SHORT).show();
                }
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            });
        }
    }



    @SuppressLint("StaticFieldLeak")
    class FirebaseUserSearch extends AsyncTask<Void, Void, Void> {
        Handler search = new Handler();

        private String getCityName(double latitude, double longitude) throws IOException {
            String myCity;
            Geocoder geocoder = new Geocoder(HomeFragment.this.requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                myCity = addresses.get(0).getLocality();
                return myCity;
            } else {
                return null;
            }

        }

        private void firebaseUserSearch(final String searchText, final Location currentLocation, DatabaseReference databaseCafea) throws IOException {
            final ArrayList<Cafenea> cautare = new ArrayList<>();
            if (!json) {
                Log.e("Baza de date", "Se cauta");
                Query query = databaseCafea.orderByChild("name");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                try {
                                    new Cafenea();
                                    Cafenea caf;
                                    caf = issue.getValue(Cafenea.class);
                                    assert caf != null;
                                    if (getCityName(latitudine, longitudine) == null) {
                                        if (latitudine == 0 && caf.getAddress().contains(Objects.requireNonNull(getCityName(currentLocation.getLatitude(), currentLocation.getLongitude())))) {
                                            if (cautare.size() <= 30) {
                                                cautare.add(issue.getValue(Cafenea.class));
                                                if (cautare.size() == 30) {
                                                    for (Cafenea cafenea : cautare) {
                                                        if (cafenea.getName().toLowerCase().contains(searchText)) {
                                                            rezultat.add(cafenea);
                                                        }
                                                    }
                                                    cautare.clear();
                                                }
                                            }
                                        }

                                    } else if (caf.getAddress().contains(Objects.requireNonNull(getCityName(latitudine, longitudine)))) {
                                        if (cautare.size() <= 30) {
                                            cautare.add(issue.getValue(Cafenea.class));
                                            if (cautare.size() == 30) {
                                                for (Cafenea cafenea : cautare) {
                                                    if (cafenea.getName().toLowerCase().contains(searchText)) {
                                                        rezultat.add(cafenea);
                                                    }
                                                }
                                                cautare.clear();
                                            }
                                        }
                                    } else {
                                        Toast.makeText(HomeFragment.this.requireContext(), "Get closer I can't see", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (rezultat.isEmpty()) {
                                negasit.setVisibility(View.VISIBLE);
                            } else {
                                negasit.setVisibility(View.GONE);
                            }
                            cautare.clear();
                        }
                        UsersAdapter adapter = new UsersAdapter(HomeFragment.this.requireContext(), rezultat);
                        lv.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            } else {
                read_file(HomeFragment.this.requireContext(), fileName, cache);
                Log.e("cache", "" + cache.size());
                for (Cafenea caf : cache) {
                    if (getCityName(latitudine, longitudine) == null) {
                        if (latitudine == 0 && caf.getAddress().contains(Objects.requireNonNull(getCityName(currentLocation.getLatitude(), currentLocation.getLongitude())))) {
                            if (caf.getName().toLowerCase().contains(searchText)) {
                                rezultat.add(caf);
                            }
                        }
                    } else if (caf.getAddress().contains(Objects.requireNonNull(getCityName(latitudine, longitudine)))) {
                        if (caf.getName().toLowerCase().contains(searchText)) {
                            rezultat.add(caf);
                        }
                    } else {
                        Toast.makeText(HomeFragment.this.requireContext(), "Get closer I can't see", Toast.LENGTH_SHORT).show();
                    }
                    if (rezultat.isEmpty()) {
                        negasit.setVisibility(View.VISIBLE);
                    } else {
                        negasit.setVisibility(View.GONE);
                    }
                    cautare.clear();

                }

                UsersAdapter adapter = new UsersAdapter(HomeFragment.this.requireContext(), rezultat);
                lv.setAdapter(adapter);


            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                getCityName(currentLocation.getLatitude(), currentLocation.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            search.post(() -> {
                String location = searchView.getQuery().toString().toLowerCase();
                try {
                    firebaseUserSearch(location, currentLocation, databaseCafea);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        super.onSaveInstanceState(savedInstanceState);
    }




//--------------------------------------------------------------------------------------------------------------------

    //Format afisare rezultate cautare
    public class UsersAdapter extends ArrayAdapter<Cafenea> {
        public UsersAdapter(@NonNull Context context, ArrayList<Cafenea> users) {
            super(context, 0, users);
        }
        @NonNull
        @Override
        //Creare butoane si elemente de afisare
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Cafenea user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout, parent, false);
            }
            // Lookup view for data population
            final View finalConvertView = convertView;
            final TextView user_name = finalConvertView.findViewById(R.id.nume_text);
            final TextView adress = finalConvertView.findViewById(R.id.adresa_text);
            Button Locatie = finalConvertView.findViewById(R.id.buttonLocatie);
            assert user != null;
            user_name.setText(user.getName());
            adress.setText(user.getAddress());

            Locatie.setOnClickListener(view -> {
                if (srch != null) {
                    srch.remove();
                }
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();
                for (Cafenea cafenea : rezultat) {
                    if (user_name.getText().equals(cafenea.getName()) && adress.getText().equals(cafenea.getAddress())) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(cafenea.getLatitude(),
                                        cafenea.getLongitude()), 20));
                        srch = googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                .title(cafenea.getName())
                                .snippet(cafenea.getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker
                                        (BitmapDescriptorFactory.HUE_AZURE)));
                    }
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }

    class fetchLastLocation implements Runnable {
        @Override
        public void run() {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeFragment.this.requireContext());

            @SuppressLint("MissingPermission") final Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentLocation = task.getResult();

                }
            }).addOnFailureListener(e -> {
                Log.d("MapDemoActivity", "Error trying to get last GPS location");
                e.printStackTrace();
            });
        }
    }
}

//ZONA COMENTARII:
/* @Override
            public void run() {
                Iterator<Cafenea> iter
                        = chunk.iterator();
                Iterator<Marker> etcetera
                        = cache.iterator();
                rr.post(() -> googleMap.setOnCameraMoveListener(() -> {
                        RAMAS : TREBUIE ORDONATE AMBELE LISTE INAINTE DE COMPARARE
                        pozitie = googleMap.getCameraPosition();
                        databaseCafea.addListenerForSingleValueEvent(CameraEventListener);
                        if (pozitie.zoom > 18.0 && pozitie.zoom < 20) {
        if (!cache.isEmpty()) {
        if (cache.size() < chunk.size()) {
        for (int i = 0; i < chunk.size(); i++) {
        if (i < cache.size()) {
        if (!cache.get(i).getSnippet().equals(chunk.get(i).getAddress())) {
        cache.get(i).setPosition(new LatLng(chunk.get(i).getLatitude(), chunk.get(i).getLongitude()));
        cache.get(i).setTitle(chunk.get(i).getName());
        cache.get(i).setSnippet(chunk.get(i).getAddress());
        }
        }
        if (i > cache.size()) {
        cache.add(googleMap.addMarker(new MarkerOptions()
        .position(new LatLng(chunk.get(i).getLatitude(), chunk.get(i).getLongitude()))
        .title(chunk.get(i).getName())
        .snippet(chunk.get(i).getAddress())
        .icon(BitmapDescriptorFactory.defaultMarker
        (BitmapDescriptorFactory.HUE_AZURE))));
        }

        }
        } else if (cache.size() > chunk.size() && chunk.size() != 0) {
       /* for (int y = 0; y < cache.size(); y++) {
        if (y < chunk.size()) { //CHUNK.GET(Y) DA EROAREA INDEX:0, SIZE:0
        if (!cache.get(y).getSnippet().equals(chunk.get(y).getAddress())) {
        cache.get(y).setPosition(new LatLng(chunk.get(y).getLatitude(), chunk.get(y).getLongitude()));
        cache.get(y).setTitle(chunk.get(y).getName());
        cache.get(y).setSnippet(chunk.get(y).getAddress());
        }

        } else {
        cache.get(y).remove();
        }
        }
        }
        } else {
        for (int i = 0; i < chunk.size(); i++) {
        cache.add(googleMap.addMarker(new MarkerOptions()
        .position(new LatLng(chunk.get(i).getLatitude(), chunk.get(i).getLongitude()))
        .title(chunk.get(i).getName())
        .snippet(chunk.get(i).getAddress())
        .icon(BitmapDescriptorFactory.defaultMarker
        (BitmapDescriptorFactory.HUE_AZURE))));
        }

        }
        if (!chunk.isEmpty()) {
        chunk.clear();
        }
        } else if (pozitie.zoom < 16.0) {
        removeAllMarkers(cache);
        cache.clear();
        }
        }));
        }
        };
        Thread map = new Thread(r);
        new Thread(map).start();
        }
        */
/* googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currentLocation.getLatitude(),
                        currentLocation.getLongitude()), 15));*/
/*
public CameraPosition savePos(){
    final CameraPosition[] test = new CameraPosition[1];
    googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {


           test[0] = googleMap.getCameraPosition();

        }
    });
    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(test[0]));

    return test[0];
}
public void SetPos(CameraPosition LastViewed){
        LastViewed = savePos();

}
 */






package ru.allfound.googleapiexample.activity;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.allfound.googleapiexample.R;
import ru.allfound.googleapiexample.model.Marker;
import ru.allfound.googleapiexample.sqlite.DatabaseHandler;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    /** The "no data" message. */
    private final static String MSG_NO_DATA = "No data";

    /** The Google Map object. */
    private GoogleMap mMap = null;

    /** Location manager */
    LocationManager mLocManager = null;

    AlertDialog dialogAddMarker;
    SupportMapFragment mapFragment;

    private ArrayList<Marker> markers;
    private DatabaseHandler databaseHandler;

    /**
     * {@inheritDoc}
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Location manager instance
        mLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Receive Google Map object
        mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markers = new ArrayList<>();
        databaseHandler = new DatabaseHandler(getApplicationContext());
    }

    private class AsyncLoadFromBD extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            markers = (ArrayList<Marker>) databaseHandler.fetchMarkers();
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadMarkers();
        }
    }

    private void loadMarkers() {
        for(int i = 0; i < markers.size(); i++ ) {
            LatLng point = new LatLng(markers.get(i).getLatitude(), markers.get(i).getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(markers.get(i).getName())
                    .snippet(markers.get(i).getAddress()));
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        // Request address by location
        if (point != null) {
            createDialogAddMarker(point);
            dialogAddMarker.show();
            //Toast.makeText(this, "tapped, point=" + point
            //        + ", Address: " + getAddressByLoc(point.latitude, point.longitude), Toast.LENGTH_SHORT).show();
        }
    }

    private void createDialogAddMarker(final LatLng point) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_add_marker, null);

        TextView etLatitude = (TextView) view.findViewById(R.id.tvLatitude);
        TextView etLongitude = (TextView) view.findViewById(R.id.tvLongitude);
        TextView tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        final EditText etName = (EditText) view.findViewById(R.id.etName);

        final String currentAddress = getAddressByLoc(point.latitude, point.longitude);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title(etName.getText().toString())
                                .snippet(currentAddress));
                        Marker marker = new Marker();
                        marker.setLatitude(point.latitude);
                        marker.setLongitude(point.longitude);
                        marker.setName(etName.getText().toString());
                        marker.setAddress(currentAddress);
                        databaseHandler.addMarker(marker);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialogAddMarker.cancel();
                    }
                });

        etLatitude.setText(String.format(getString(R.string.latitudeString),
                Double.toString(point.latitude)));
        etLongitude.setText(String.format(getString(R.string.longitudeString),
                Double.toString(point.longitude)));
        tvAddress.setText(String.format(getString(R.string.addressString),
                currentAddress));

        dialogAddMarker = builder.create();
        dialogAddMarker.setTitle(getString(R.string.addMarkerText));
    }

    /**
     * Get address string by location
     * */
    private String getAddressByLoc(double latitude, double longitude) {

        // Create geocoder
        final Geocoder geo = new Geocoder(this);

        // Try to get addresses list
        List<Address> list = null;
        try {
            list = geo.getFromLocation(latitude, longitude, 5);
        } catch (IOException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        // If list is empty, return "No data" string
        if (list.isEmpty()) return MSG_NO_DATA;

        // Get first element from List
        Address a = list.get(0);

        // Get a Postal Code
        final int index = a.getMaxAddressLineIndex();
        String postal = null;
        if (index >= 0) {
            postal = a.getAddressLine(index);
        }

        // Make address string
        StringBuilder builder = new StringBuilder();
        final String sep = ", ";
        builder.append(postal);
        if (a.getCountryName() != null) {
            builder.append(sep).append(a.getCountryName());
        }
        if (a.getAdminArea() != null) {
            builder.append(sep).append(a.getAdminArea());
        }
        if (a.getThoroughfare() != null) {
            builder.append(sep).append(a.getThoroughfare());
        }
        if (a.getSubThoroughfare() != null) {
            builder.append(sep).append(a.getSubThoroughfare());
        }
        return builder.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be
     * used.
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable the my-location layer in the map
        mMap.setMyLocationEnabled(true);

        // Disable my-location button
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMapClickListener(this);

        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);

        AsyncLoadFromBD asyncLoadFromBD = new AsyncLoadFromBD();
        asyncLoadFromBD.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item != null ? item.getItemId() : 0;

        // Map type - Normal
        if (id == R.id.menu_map_mode_normal) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        // Map type - Satellite
        if (id == R.id.menu_map_mode_satellite) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            return true;
        }
        // Map type - Terrain
        if (id == R.id.menu_map_mode_terrain) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            return true;
        }

        // Delete markers
        if (id == R.id.menu_delete_markers) {
            markers.clear();
            databaseHandler.deleteMarkers();
            mMap.clear();
            return true;
        }

        // My Location
        if (id == R.id.menu_map_location && mMap.isMyLocationEnabled()) {

            // Get last know location
            final Location loc = mLocManager.getLastKnownLocation(
                    LocationManager.PASSIVE_PROVIDER);

            // If location available
            if (loc != null) {
                // Create LatLng object for Maps
                LatLng target = new LatLng(loc.getLatitude(), loc.getLongitude());
                // Defines a camera move. An object of this type can be used to modify a map's camera
                // by calling moveCamera()
                CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(target, 10F);
                // Move camera to point with Animation
                mMap.animateCamera(camUpdate);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
    }
}
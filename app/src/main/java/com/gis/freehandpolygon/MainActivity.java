package com.gis.freehandpolygon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolygonClickListener {
    private GoogleMap map;
    ArrayList<LatLng> latLngArrayListPolygon = new ArrayList<>();
    ArrayList<Double> distancesFromMidPointsOfPolygonEdges = new ArrayList<>();
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;
    private boolean markerClicked=false;
    private Polygon polygon;
    private PolygonOptions polygonOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);
        map.setOnPolygonClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        map.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)).position(latLng));
        if (markerClicked) {
            if (polygon != null) {
                polygon.remove();
                polygon = null;
            }
            adjustPolygonWithRespectTo(latLng);
            PolygonOptions polygonOptions = null;
            for (int i = 0; i < latLngArrayListPolygon.size(); i++)
                if (i == 0)
                    polygonOptions = new PolygonOptions().add(latLngArrayListPolygon.get(0));
                else
                    polygonOptions.add(latLngArrayListPolygon.get(i));
            polygonOptions.strokeColor(Color.BLACK);
            polygonOptions.strokeWidth(5f);
            //polygonOptions.fillColor(shadeColor);
            polygon = map.addPolygon(polygonOptions);
            polygon.setClickable(true);
        } else {
            if (polygon != null) {
                polygon.remove();
                polygon = null;
            }
            polygonOptions = new PolygonOptions().add(latLng);
            latLngArrayListPolygon.add(latLng);
            markerClicked = true;
        }
    }




    private void adjustPolygonWithRespectTo(LatLng point) {
        double minDistance = 0;

        if (latLngArrayListPolygon.size() > 2) {
            distancesFromMidPointsOfPolygonEdges.clear();
            //midPointsOfPolygonEdges?.removeAll()

            for (int i = 0; i < latLngArrayListPolygon.size(); i++) {
                // 1. Find the mid points of the edges of polygon
                ArrayList<LatLng> list = new ArrayList<>();

                if (i == (latLngArrayListPolygon.size() - 1)) {
                    list.add(latLngArrayListPolygon.get(latLngArrayListPolygon.size() - 1));
                    list.add(latLngArrayListPolygon.get(0));
                } else {
                    list.add((latLngArrayListPolygon.get(i)));
                    list.add((latLngArrayListPolygon.get(i + 1)));
                }


                LatLng midPoint = computeCentroid(list);

                // 2. Calculate the nearest coordinate by finding distance between mid point of each edge and the coordinate to be drawn
                Location startPoint = new Location("");
                startPoint.setLatitude(point.latitude);
                startPoint.setLongitude(point.longitude);
                Location endPoint = new Location("");
                endPoint.setLatitude(midPoint.latitude);
                endPoint.setLongitude(midPoint.longitude);
                double distance = startPoint.distanceTo(endPoint);

                distancesFromMidPointsOfPolygonEdges.add(distance);
                if (i == 0) {
                    minDistance = distance;
                } else {

                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
                //midPointsOfPolygonEdges?.append(midPoint)
            }

            // 3. The nearest coordinate = the edge with minimum distance from mid point to the coordinate to be drawn
            int position = minIndex(distancesFromMidPointsOfPolygonEdges);


            // 4. move the nearest coordinate at the end by shifting array right
            int shiftByNumber = (latLngArrayListPolygon.size() - position - 1);

            if (shiftByNumber != latLngArrayListPolygon.size()) {
                latLngArrayListPolygon = rotate(latLngArrayListPolygon, shiftByNumber);
            }
        }

        // 5. Now add coordinated to be drawn
        latLngArrayListPolygon.add(point);
    }

    public static int minIndex(ArrayList<Double> list) {
        return list.indexOf(Collections.min(list));
    }

    public static <T> ArrayList<T> rotate(ArrayList<T> aL, int shift) {
        if (aL.size() == 0)
            return aL;

        T element = null;
        for (int i = 0; i < shift; i++) {
            // remove last element, add it to front of the ArrayList
            element = aL.remove(aL.size() - 1);
            aL.add(0, element);
        }

        return aL;
    }

    private LatLng computeCentroid(List<LatLng> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();

        for (LatLng point : points) {
            latitude += point.latitude;
            longitude += point.longitude;
        }

        return new LatLng(latitude / n, longitude / n);
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        Toast.makeText(this,"Polygon clicked",Toast.LENGTH_LONG).show();
    }
}

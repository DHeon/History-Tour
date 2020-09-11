package com.example.hya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    LocationManager lm;
    Location location;
    DBHelper dbhelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},200);
        }
        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //기본
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {  //맵이 준비되면 작동.
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        mMap.setOnMarkerClickListener(this);
        LatLng seoul = new LatLng(37.5642135, 127.0016985);//서울에 맞춤.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));  //서울로 카메라 옮김.

        try{
            SQLiteDatabase db = dbhelper.getReadableDatabase();  //데이터베이스를 읽음.
            Cursor exist = db.rawQuery("select exists (select num from main)",null);  //데이터가 존재하는가?
            exist.moveToFirst();  //첫행으로
            if(!exist.getString(0).equals("0")){ //0이면 존재안함 1이면 존재함.
                Cursor nomi = db.rawQuery("select num from main", null); //main으로부터 num을 선택함.
                nomi.moveToLast();
                int i = nomi.getInt(0); //마지막행의 num을 읽음.
                Toast.makeText(this,i+"",Toast.LENGTH_LONG).show();
                for(int k = 1; k<=i; k++){  //마지막행까지 돌면서 좌표찍고 좌표에 마커에 찍고 이름정함.
                    Cursor cursor = db.rawQuery("select lon, lat , title from main where num="+k+"",null);
                    cursor.moveToFirst();
                    LatLng lng = new LatLng(Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(0)));
                    MarkerOptions marker = new MarkerOptions();
                    Marker a = mMap.addMarker(marker.position(lng)); //Marker 객체 가져옴.
                    a.setTitle(cursor.getString(2)); //마커에 이름정함.
                    a.showInfoWindow(); //앱 들어갔을 때 기본적으로 이름이 떠있음.
                }
            }
        }catch(SQLiteException | NullPointerException e){
            e.printStackTrace();
        }
    }

    public void mylocation(View view) { //버튼을 누르면 내 위치에 마커를 찍음.
        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        LatLng lng = new LatLng(lat, lon);  //좌표 가져오기.
        MarkerOptions marker = new MarkerOptions();
        Marker a = mMap.addMarker(marker.position(lng));
        a.setTitle("당신의 위치!");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lng,16));
    }

    Marker nm;
    @Override
    public boolean onMarkerClick(Marker marker){ //마커 클릭시 이벤트.
        Intent intent = new Intent(getApplicationContext(), SubActivity.class);

        intent.putExtra("La",marker.getPosition().latitude);
        intent.putExtra("Lo",marker.getPosition().longitude);  //경도랑 위도 데이터 넘김.
        startActivityForResult(intent,1); //작동.
        nm = marker;//?
        // Toast.makeText(this,string,Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){  //sub으로부터 데이터 받아옴.
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            String d = data.getStringExtra("key");  //제목을 받아옴.
            Toast.makeText(this, d, Toast.LENGTH_LONG).show();
            nm.setTitle(d);  //제목 정함.
        }
    }

}

package com.example.hya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private LocationManager lm;
    private Location location;
    private DBHelper dbhelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //기본
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {  //맵이 준비되면 작동.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        LatLng seoul = new LatLng(37.5642135, 127.0016985);//서울에 맞춤.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));  //서울로 카메라 옮김.
        mMap.setMyLocationEnabled(true); //내 위치 버튼 활성화
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this); //내 위치 클릭
        mMap.setMinZoomPreference(7f);
        try {
            SQLiteDatabase db = dbhelper.getReadableDatabase();  //데이터베이스를 읽음.
            Cursor exist = db.rawQuery("select exists (select cch from checking)", null);  //데이터가 존재하는가?
            exist.moveToFirst();  //첫행으로
            if (!exist.getString(0).equals("1")) { //0이면 존재안함 1이면 존재함.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("사용 방법");
                builder.setMessage("오른쪽 위에 보이는 버튼을 누르면 현재 내 위치로 화면이 이동하면서 내 위치인 파란 마커가 생깁니다.\n파란 마커를 클릭하면 장소를 표시하는 빨간 마커가 생깁니다.\n빨간 마커를 눌러 현재 내가 있는 곳의 설명을 쓸 수 있습니다.");
                builder.setPositiveButton("닫기",null);
                builder.setNegativeButton("다시는 안보기", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SQLiteDatabase db = dbhelper.getWritableDatabase();
                        db.execSQL("insert into checking (cch) values (?)",new Integer[]{1});
                    }
                });
                builder.setCancelable(false);
                AlertDialog ad = builder.create();
                ad.show();
            }

        } catch (SQLiteException | NullPointerException e) {
            e.printStackTrace();
        }

        try {
            SQLiteDatabase db = dbhelper.getReadableDatabase();  //데이터베이스를 읽음.
            Cursor exist = db.rawQuery("select exists (select num from main)", null);  //데이터가 존재하는가?
            exist.moveToFirst();  //첫행으로
            if (!exist.getString(0).equals("0")) { //0이면 존재안함 1이면 존재함.
                Cursor nomi = db.rawQuery("select num from main", null); //main으로부터 num을 선택함.
                nomi.moveToLast();
                int i = nomi.getInt(0); //마지막행의 num을 읽음.
                for (int k = 1; k <= i; k++) {  //마지막행까지 돌면서 좌표찍고 좌표에 마커에 찍고 이름정함.
                    Cursor cursor = db.rawQuery("select lon, lat , title from main where num=" + k + "", null);
                    cursor.moveToFirst();
                    LatLng lng = new LatLng(Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(0)));
                    MarkerOptions marker = new MarkerOptions();
                    Marker a = mMap.addMarker(marker.position(lng)); //Marker 객체 가져옴.
                    a.setTitle(cursor.getString(2)); //마커에 이름정함.
                    a.showInfoWindow(); //앱 들어갔을 때 기본적으로 이름이 떠있음.
                }
            }
        } catch (SQLiteException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    protected double longitude;
    protected double latitude;

    private Marker nm;

    @Override
    public boolean onMarkerClick(Marker marker) { //마커 클릭시 이벤트.
        Intent intent = new Intent(getApplicationContext(), SubActivity.class);

        intent.putExtra("La", marker.getPosition().latitude);
        intent.putExtra("Lo", marker.getPosition().longitude);  //경도랑 위도 데이터 넘김.
        startActivityForResult(intent, 1); //작동.
        nm = marker;
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  //sub으로부터 데이터 받아옴.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String d = data.getStringExtra("key");  //제목을 받아옴.
            Toast.makeText(this, d, Toast.LENGTH_LONG).show();
            nm.setTitle(d);  //제목 정함.
            nm.showInfoWindow();
        }
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        try {
            float dis = 10000000;
            SQLiteDatabase db = dbhelper.getReadableDatabase();  //데이터베이스를 읽음.
            Cursor exist = db.rawQuery("select exists (select num from main)", null);  //데이터가 존재하는가?
            exist.moveToFirst();  //첫행으로
            if (!exist.getString(0).equals("0")) { //0이면 존재안함 1이면 존재함.
                Cursor nomi = db.rawQuery("select num from main", null); //main으로부터 num을 선택함.
                nomi.moveToLast();
                int i = nomi.getInt(0); //마지막행의 num을 읽음.
                for (int k = 1; k <= i; k++) {  //마지막행까지 돌면서 좌표찍고 좌표에 마커에 찍고 이름정함.
                    Cursor cursor = db.rawQuery("select lon, lat , title from main where num=" + k + "", null);
                    cursor.moveToFirst();
                    LatLng lng = new LatLng(Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(0)));
                    Location compare = new Location(LocationManager.NETWORK_PROVIDER);
                    compare.setLongitude(lng.longitude);
                    compare.setLatitude(lng.latitude);
                    float temp = location.distanceTo(compare);
                    if(dis > temp) dis = temp;
                }
            }
            if(dis > 15f){
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                LatLng lng2 = new LatLng(latitude, longitude);  //좌표 가져오기.
                MarkerOptions marker = new MarkerOptions();
                marker.position(lng2);
                marker.title("마커");
                mMap.addMarker(marker);
            }
        } catch (SQLiteException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private final long FINISH_TIME = 2000;
    private long backTime = 0;
    @Override
    public void onBackPressed(){
        long temp = System.currentTimeMillis();
        long inter = temp - backTime;
        if(0<= inter && FINISH_TIME >= inter){
            moveTaskToBack(true);
            finishAndRemoveTask();
            android.os.Process.killProcess(android.os.Process.myPid());
        }else{
            backTime = temp;
            Toast.makeText(getApplicationContext(), "한 번 더 뒤로가기를 눌러주세요", Toast.LENGTH_LONG).show();
        }
    }
}

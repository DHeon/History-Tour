package com.example.hya;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class SubActivity extends AppCompatActivity {
    String name;
    EditText tv;
    EditText con;

    Intent intent2;

    ImageView i;
    String la;
    String lo;

    DBHelper helper;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.subactivity);

        tv = (EditText) findViewById(R.id.tt);
        con = (EditText) findViewById(R.id.contents);
        i = (ImageView)findViewById(R.id.image);


        intent2 = getIntent();
        la = Double.toString(intent2.getExtras().getDouble("La"));
        lo = Double.toString(intent2.getExtras().getDouble("Lo"));  //위도 경도 가져옴.

        helper = new DBHelper(this);  //데이터 베이스 가져옴.
        String sm = la+" " + lo;
        Toast.makeText(this,sm,Toast.LENGTH_LONG).show();

        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor exist2 = db.rawQuery("select exists (select image from main where lat="+la+" and lon="+lo+")", null);
            exist2.moveToFirst();
            if(!exist2.getString(0).equals("0")){
                Cursor cursor = db.rawQuery("select image from main where lat="+la+" and lon="+lo+"", null);
                cursor.moveToLast();
                if(cursor.getString(0) == null){
                    Glide.with(this).load(R.drawable.upload).into(i);
                }else{
                    Bitmap bm = StringToBitmap(cursor.getString(0));
                    i.setImageBitmap(bm);
                }

            }
            Cursor exist = db.rawQuery("select exists (select title, content from main where lat="+la+" and lon="+lo+")",null);
            exist.moveToFirst();
            if(!exist.getString(0).equals("0")){
                Cursor cursor = db.rawQuery("select title, content from main where lat="+la+" and lon="+lo+"",null);
                cursor.moveToLast();
                tv.setText(cursor.getString(0));
                con.setText(cursor.getString(1));
            }

        }catch(SQLiteException e){
            e.printStackTrace();
        }//데이터베이스에서 제목이랑 내용 불러옴-
    }
    String en;
    public void getC(View view){
        String contents  = con.getText().toString();
        name = tv.getText().toString();
        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteDatabase db2 = helper.getReadableDatabase();
        if(en != null){
            Cursor exist = db2.rawQuery("select exists (select lon, lat from main where lon="+lo+" and lat="+la+")",null);
            exist.moveToFirst();
            if(!exist.getString(0).equals("0")){
                db.execSQL("update main SET title=?,content=?,image=? where lon="+lo+" and lat="+la+"",new String[]{name,contents,en});
            }else{
                db.execSQL("insert into main (lon, lat, title, content, image) values (?,?,?,?,?)",new String[]{lo,la,name,contents,en});
            }
        }else{
            Cursor exist = db2.rawQuery("select exists (select lon, lat from main where lon="+lo+" and lat="+la+")",null);
            exist.moveToFirst();
            if(!exist.getString(0).equals("0")){
                db.execSQL("update main SET title=?, content=? where lon="+lo+" and lat="+la+"",new String[]{name,contents});
            }else{
                db.execSQL("insert into main (lon, lat, title, content) values (?,?,?,?)",new String[]{lo,la,name,contents});
            }
        }
        Intent intent = new Intent();
        intent.putExtra("key",name);
        setResult(RESULT_OK,intent);
        finish();
    } //버튼을 누르면 데이터베이스에 저장되고 제목이 main클래스로 보냄.
    private static int PICK_IMAGE_REQUEST = 1;

    public void loadImage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE_REQUEST);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
                //data에서 절대경로로 이미지를 가져옴
                uri = data.getData();
                en = uri.toString();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                //이미지가 한계이상(?) 크면 불러 오지 못하므로 사이즈를 줄여 준다.
                int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);
                String s = BitmapToString(scaled);
                en = s;
                i.setImageBitmap(scaled);
            } else {
                Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_LONG).show();
            }


        }catch(Exception e){

        }
    }

    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        return temp;
    }
}

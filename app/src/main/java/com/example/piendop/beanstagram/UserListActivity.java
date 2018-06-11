package com.example.piendop.beanstagram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    /******************IMPORT PHOTO********************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.share_menu,menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1 ){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getPhoto();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.share){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }else{
                getPhoto();
            }
        }else if(item.getItemId()==R.id.logout){
            ParseUser.logOut();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent,1);
    }

    /**************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        //LIST ARRAY TO CONTAIN DATA
        final ArrayList<String> usernames = new ArrayList<>();

        //set array adapter to display data on list view
        //list view
        final ListView listView = findViewById(R.id.userListView);
        //set array adapter for list view
        final ArrayAdapter arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.black_text, R.id.textView, usernames);
        addDataToListViewAndDisplay(usernames, listView, arrayAdapter);
        showUserFeedAndImage(usernames, listView);


    }

    private void showUserFeedAndImage(final ArrayList<String> usernames, ListView listView) {
        /**********SHOW USER FEED AND IMAGE***********************/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),UserFeedActivity.class);
                intent.putExtra("username",usernames.get(i));
                startActivity(intent);
            }
        });
    }

    private void addDataToListViewAndDisplay(final ArrayList<String> usernames, final ListView listView, final ArrayAdapter arrayAdapter) {
        //get parse user list by parse query
        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        //find all users that is not user currently logged in
        parseQuery.whereNotEqualTo("username",ParseUser.getCurrentUser().getUsername());

        parseQuery.addAscendingOrder("username");

        parseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(objects!=null&&objects.size()>0){
                    for(ParseUser user: objects){
                        usernames.add(user.getUsername());
                    }
                    listView.setAdapter(arrayAdapter);
                }else{
                    e.printStackTrace();
                }
            }
        });
    }

    /**********************ACTION AFTER RECEIVE IMAGE***********************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode == RESULT_OK && data!=null){

            //uri link image with app
            Uri selectedImage  = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);

                Log.i("Photo","Received");
                /****UPLOAD PHOTO TO PARSE SERVER***/
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);

                byte[] byteArray = stream.toByteArray();

                ParseFile file = new ParseFile("image.png",byteArray);

                ParseObject object = new ParseObject("Image");

                object.put("image",file);

                object.put("username",ParseUser.getCurrentUser().getUsername());

                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null){
                            Toast.makeText(getApplicationContext(),"Image uploaded",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Image could not be uploaded - please try again later",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

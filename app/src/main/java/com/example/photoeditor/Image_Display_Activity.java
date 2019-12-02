package com.example.photoeditor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.MenuItem;

public class Image_Display_Activity extends AppCompatActivity {

    static Bitmap bm = BitmapFactory.decodeFile(MainActivity.mCurrentPhotoPath);
    static float vH = 0, vW = 0;
    static BitmapFactory.Options bmOptions;
    private final static String TAG = "DEBUG_BOTTOM_NAV_UTIL";
    static PhotoView imageDisplay;
    static float iHeight = 0;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_image__display_);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        imageDisplay = (PhotoView) findViewById(R.id.imageDisplay);

        final float targetW = getIntent().getExtras().getInt("width");
        final float targetH = getIntent().getExtras().getInt("height");

        // Get the dimensions of the bitmap
        bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(MainActivity.mCurrentPhotoPath, bmOptions);
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;
        {
            vH = targetH * (0.89f);
            vW = (targetH * (0.89f) / (bm.getHeight())) * (bm.getWidth());
            if (vW > targetW) {
                vW = targetW;
                vH = (targetW / (bm.getWidth())) * (bm.getHeight());
            }
        }

        // Determine how much to scale down the image
        float scaleFactor = Math.min(photoW / vW, photoH / vH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;
        bmOptions.inPurgeable = true;
        bm = rotImage(BitmapFactory.decodeFile(MainActivity.mCurrentPhotoPath, bmOptions));
        bmOptions.inJustDecodeBounds = true;
        iHeight = bmOptions.outHeight;

        //set scaled image to imageDisplay
        imageDisplay.setImageBitmap(bm);


        final BottomNavigationView optionNavigationView = (BottomNavigationView) findViewById(R.id.optionNavigation);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) optionNavigationView.getChildAt(0);
        try {//Set shifting mode of bottom navigation view as false to see all titles
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // Set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.d(TAG, "Unable to change value of shift mode");
        }

        //Start respective activities when option is chosen from bottom navigation view
        optionNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_addText:
                        Intent addTextIntent = new Intent(Image_Display_Activity.this, Add_Text_Activity.class);
                        addTextIntent.putExtra("height", targetH);
                        addTextIntent.putExtra("width", targetW);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this, new Pair<View, String>(findViewById(R.id.imageDisplay), (getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, addTextIntent, options.toBundle());
                        break;
                    case R.id.action_draw:
                        Intent drawIntent = new Intent(Image_Display_Activity.this, Draw_Activity.class);
                        drawIntent.putExtra("height", targetH);
                        drawIntent.putExtra("width", targetW);
                        ActivityOptionsCompat optionsDraw = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this, new Pair<View, String>(findViewById(R.id.imageDisplay), (getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, drawIntent, optionsDraw.toBundle());
                        break;
                    case R.id.action_addEmoji:
                        Intent emojiIntent = new Intent(Image_Display_Activity.this, Emoji_Activity.class);
                        emojiIntent.putExtra("height", targetH);
                        emojiIntent.putExtra("width", targetW);
                        ActivityOptionsCompat optionsEmoji = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this, new Pair<View, String>(findViewById(R.id.imageDisplay), (getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, emojiIntent, optionsEmoji.toBundle());
                        break;
                    case R.id.action_rotateCrop:
                        Intent rotCropIntent = new Intent(Image_Display_Activity.this, Rotate_Crop_Activity.class);
                        rotCropIntent.putExtra("height", targetH);
                        rotCropIntent.putExtra("width", targetW);
                        ActivityOptionsCompat rotCropOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this, new Pair<View, String>(findViewById(R.id.imageDisplay), (getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, rotCropIntent, rotCropOptions.toBundle());
                        break;
                    case R.id.action_tune:
                        Intent tuneIntent = new Intent(Image_Display_Activity.this, Tune_Activity.class);
                        tuneIntent.putExtra("height", targetH);
                        tuneIntent.putExtra("width", targetW);
                        tuneIntent.putExtra("iHeight", iHeight);
                        ActivityOptionsCompat tuneOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this, new Pair<View, String>(findViewById(R.id.imageDisplay), (getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this, tuneIntent, tuneOptions.toBundle());
                        break;

                }
                return true;
            }
        });

        ImageView saveDisplayImage = (ImageView) findViewById(R.id.saveImageDisplay);
        saveDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ImageView cancelDisplayImage = (ImageView) findViewById(R.id.cancelImageDisplay);
        cancelDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });


    }

    //Function to save image
    private void saveImage() throws Exception {
        FileOutputStream fOut = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName, ".png", file2);

        try {
            fOut = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri cUri = Uri.fromFile(file);
        mediaScanIntent.setData(cUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(), "Image Saved to Pictures", Toast.LENGTH_SHORT).show();
    }

    //rotate image if it is incorrectly oriented
    private Bitmap rotImage(Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(MainActivity.mCurrentPhotoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix matrix = new Matrix();

            if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return true;
    }

//    String type = "image/*";
//    String filename = "/myPhoto.jpg";
//    String mediaPath = Environment.getExternalStorageDirectory() + filename;

//    private void createInstagramIntent(String type, String mediaPath) {
//
//
//        Intent share = new Intent(Intent.ACTION_SEND);
//        share.setType(type);
//        File media = new File(mediaPath);
//        Uri uri = Uri.fromFile(media);
//        share.putExtra(Intent.EXTRA_STREAM, uri);
//        startActivity(Intent.createChooser(share, "Share to"));
//
//    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                    shareContent();
//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                sharingIntent.setType("image/png");
//                String shareBodyText = "Pesan terkirim";
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject here");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
//                startActivity(Intent.createChooser(sharingIntent, "Sharing Via"));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareContent(){

        Bitmap bitmap = getBitmapFromView(imageDisplay);
        try {
            File file = new File(this.getExternalCacheDir(),"logicchip.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, "Test");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }





}
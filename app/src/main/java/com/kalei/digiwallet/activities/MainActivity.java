package com.kalei.digiwallet.activities;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageSegMode;
import com.kalei.digiwallet.R;
import com.kalei.digiwallet.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends FragmentActivity {
    private static ImageView imageView;
    private TextView mResultTextView;
    protected String _path;
    // protected static Bitmap bit;
    static File myDir;
    protected static Bitmap mImageBitmap;

    private static final String TAG = "dw";
    private final int START_CODE = 101;
    private String mDirPath = null;
    private Uri mOutPutUri = null;
    private static final String lang = "eng";
    private String mPath = null;
    boolean useStatic = false;

    public void getPicture() {
        android.util.Log.i(TAG, "mDirPath: " + mDirPath + " mPath: " + mPath);
        if (useStatic) {
            processImage(mPath, 0);
        } else {
            if (mDirPath != null && mDirPath.length() > 0) {
                mOutPutUri = Uri.fromFile(new File(mPath));
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mOutPutUri);
                startActivityForResult(intent, START_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                int rotation = -1;
                long fileSize = new File(mPath).length();
                android.util.Log.i(TAG, "fileSize " + fileSize);

                //Suppose Device Supports ExifInterface
                ExifInterface exif;
                try {
                    exif = new ExifInterface(mPath);
                    int exifOrientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    switch (exifOrientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotation = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotation = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotation = 270;
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        case ExifInterface.ORIENTATION_UNDEFINED:
                            rotation = 0;
                            break;
                    }
                    android.util.Log.i(TAG, "Exif:rotation " + rotation);

                    if (rotation != -1) {
                        processImage(mPath, rotation);
                    } else {
                        //Device Does Not Support ExifInterface
                        Cursor mediaCursor = getContentResolver().query(mOutPutUri,
                                new String[]{MediaStore.Images.ImageColumns.ORIENTATION,
                                        MediaStore.MediaColumns.SIZE},
                                null, null, null);
                        if (mediaCursor != null && mediaCursor.getCount() != 0) {
                            while (mediaCursor.moveToNext()) {
                                long size = mediaCursor.getLong(1);
                                android.util.Log.i(TAG, "Media:size " + size);
                                if (size == fileSize) {
                                    rotation = mediaCursor.getInt(0);
                                    break;
                                }
                            }
                            android.util.Log.i(TAG, "Media:rotation " + rotation);
                            processImage(mPath, rotation);
                        } else {
                            android.util.Log.i(TAG, "Android Problem");
//                            txtGotTime.setText("Android Problem");
                        }
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                android.util.Log.i(TAG, "RESULT_CANCELED");
//                txtGotTime.setText("RESULT_CANCELED");
            }
        }
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private void processImage(final String filePath, final int rotation) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        if (bitmap != null) {
            bitmap = toGrayscale(bitmap);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            try {
                FileOutputStream mFileOutStream = new FileOutputStream(mPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (useStatic) {
                bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.test)).getBitmap();
            }
            TessBaseAPI baseApi = new TessBaseAPI();
//            baseApi.setDebug(true);
            baseApi.init(mDirPath, lang);
            baseApi.setPageSegMode(TessBaseAPI.OEM_TESSERACT_ONLY);
            baseApi.setPageSegMode(PageSegMode.PSM_AUTO_OSD);
            String whiteList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789,.-";
            String blackList = "@#$%*()+~`/&";
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whiteList);
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, blackList);
            baseApi.setImage(ReadFile.readBitmap(toGrayscale(bitmap)));
            String recognizedText = baseApi.getUTF8Text();
            android.util.Log.i(TAG, "recognizedText: 1 " + recognizedText);
            baseApi.end();
            if (lang.equalsIgnoreCase("eng")) {
                recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
            }
            mResultTextView.setText("text is: " + recognizedText);
            android.util.Log.i(TAG, "recognizedText: 2 " + recognizedText.trim());
//            txtGotTime.setText(recognizedText.trim());
        }
    }

    private void saveImageAndroid(final Bitmap passedBitmap) {
        try {
            FileOutputStream mFileOutStream = new FileOutputStream(mDirPath + File.separator + "savedAndroid.jpg");
            passedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultTextView = (TextView) findViewById(R.id.txt_results);
        mDirPath = Utils.getDataPath();
        mPath = mDirPath + File.separator + "test.jpg";
        android.util.Log.i(TAG, "mDirPath: " + mDirPath + " mPath: " + mPath);

        if (!(new File(mDirPath + File.separator + "tessdata" + File.separator + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata" + File.separator + lang + ".traineddata");
                OutputStream out = new FileOutputStream(mDirPath + File.separator
                        + "tessdata" + File.separator + lang + ".traineddata");
                byte[] buf = new byte[8024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                android.util.Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        } else {
            processImage(mDirPath + File.separator + "six.jpg", 0);
        }

        this.imageView = (ImageView) this.findViewById(R.id.img_photo);
        Button photoButton = (Button) this.findViewById(R.id.img_shutter);

        _path = Environment.getExternalStorageDirectory() + "/images/test.bmp";

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CALL THE PICTURE
                dispatchTakePictureIntent();
            }
        });
    }

    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
        mImageBitmap = (Bitmap) extras.get("data");
        imageView.setImageBitmap(mImageBitmap);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

        imageView.setImageBitmap(bitmap);

        //_path = path to the image to be OCRed
        ExifInterface exif;
        try {
            exif = new ExifInterface(_path);

            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
            if (rotate != 0) {
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);
                // Rotating Bitmap & convert to ARGB_8888, required by tess
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

            TessBaseAPI baseApi = new TessBaseAPI();
            // DATA_PATH = Path to the storage
            // lang for which the language data exists, usually "eng"
            baseApi.init(_path, "eng");  //THIS SHOULD BE DATA_PATH ?
            baseApi.setImage(bitmap);
            String recognizedText = baseApi.getUTF8Text();
            Log.i(TAG, "Recognized Text = " + recognizedText);
            baseApi.end();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent() {
        getPicture();
    }

    protected static void identifyunicode() {
        // DATA_PATH = Path to the storage
        // lang for which the language data exists, usually "eng"

    /*
     * TessBaseAPI baseApi = new TessBaseAPI();
     * baseApi.init(myDir.toString(), "eng"); // myDir + //
     * "/tessdata/eng.traineddata" // must be present baseApi.setImage(bit);
     * String recognizedText = baseApi.getUTF8Text(); // Log or otherwise //
     * display this // string... baseApi.end();
     */
    }
}
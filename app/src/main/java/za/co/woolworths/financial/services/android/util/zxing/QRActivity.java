package za.co.woolworths.financial.services.android.util.zxing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.zxing.MultiFormatReader;
import com.pacific.mvc.Activity;
import com.trello.rxlifecycle.ActivityEvent;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class QRActivity extends Activity<QRModel> {
    public static final int CODE_PICK_IMAGE = 0x100;
    private BaseCameraManager cameraManager;
    private final int ZBAR_PERMS_REQUEST_CODE = 12345678;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this, R.color.black);
        setContentView(R.layout.activity_qr);
        setupToolbar();

        if (hasPermissions()) {
            if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
                cameraManager = new CameraManager(getApplication());
            } else {
                cameraManager = new CameraManager(getApplication());
            }
            model = new QRModel(new QRView(this));
            model.onCreate();

            cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
                @Override
                public void onResult(QRResult qrResult) {
                    model.resultDialog(qrResult);
                }
            });
        } else {
            requestPerms();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.releaseCamera();
        cameraManager.shutdownExecutor();
    }


    public boolean hasPermissions() {
        int res;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.CAMERA};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, ZBAR_PERMS_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_PICK_IMAGE) {
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), columns, null, null, null);
            if (cursor.moveToFirst()) {
                Observable
                        .just(cursor.getString(cursor.getColumnIndex(columns[0])))
                        .observeOn(Schedulers.from(cameraManager.getExecutor()))
                        .compose(this.<String>bindUntilEvent(ActivityEvent.PAUSE))
                        .map(new Func1<String, QRResult>() {
                            @Override
                            public QRResult call(String str) {
                                return QRUtils.decode(str, new MultiFormatReader());
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<QRResult>() {
                            @Override
                            public void call(QRResult qrResult) {
                                model.resultDialog(qrResult);
                            }
                        });
            }
            cursor.close();
        }
    }

    public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
        if (cameraManager.getExecutor().isShutdown()) return;
        Observable
                .just(surfaceHolder)
                .compose(this.<SurfaceHolder>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(Schedulers.from(cameraManager.getExecutor()))
                .map(new Func1<SurfaceHolder, Object>() {
                    @Override
                    public Object call(SurfaceHolder holder) {
                        cameraManager.setRotate(getWindowManager().getDefaultDisplay().getRotation());
                        cameraManager.connectCamera(holder);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        model.setEmptyViewVisible(false);
                        cameraManager.startCapture();
                    }
                });
    }

    public void onSurfaceDestroyed() {
        cameraManager.releaseCamera();
    }

    public void restartCapture() {
        cameraManager.startCapture();
    }

    public void setHook(boolean hook) {
        cameraManager.setHook(hook);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;
        switch (requestCode) {
            case ZBAR_PERMS_REQUEST_CODE:
                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }
        if (allowed) {
            //user granted all permissions we can perform our task.
            if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
                cameraManager = new CameraManager(getApplication());
            } else {
                cameraManager = new CameraManager(getApplication());
            }
            model = new QRModel(new QRView(this));
            model.onCreate();

            cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
                @Override
                public void onResult(QRResult qrResult) {
                    model.resultDialog(qrResult);
                }
            });

        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Camera Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        WTextView mTextToolbar = (WTextView) findViewById(R.id.toolbarText);
        mTextToolbar.setText(getString(R.string.scan_product));
        mTextToolbar.setGravity(Gravity.LEFT);
        mTextToolbar.setTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayUseLogoEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDefaultDisplayHomeAsUpEnabled(false);
            ab.setHomeAsUpIndicator(R.drawable.close_white);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

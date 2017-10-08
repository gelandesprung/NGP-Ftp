package yanchao.bj.ngp.ui;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.functions.Consumer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import yanchao.bj.ngp.ftpserver.FtpService;
import yanchao.bj.ngp.ftpserver.FtpService.MyBinder;
import yanchao.bj.ngp.ftpserver.IFtp;
import yanchao.bj.ngp.R;
import yanchao.bj.ngp.utils.qrcode.QRCodeFactory;

public class MainActivity extends AppCompatActivity {

    private TextView ip;
    private Button ftp;
    private IFtp ftpServer;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ftpServer = ((MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ip = (TextView) findViewById(R.id.ip);
        ftp = (Button) findViewById(R.id.ftp_manager);
        mContext = this;
        ftp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ftpServer != null) {
                    if (ftpServer.ftpStatus()) {
                        if (ftpServer.stopFtp()) {
                            ftp.setText(R.string.start_ftp);
                            ftp.setSelected(false);
                            ip.setCompoundDrawables(null,null,null,null);
                        }
                    } else {
                        if (ftpServer.startFtp()) {
                            ftp.setText(R.string.stop_ftp);
                            ftp.setSelected(true);
                            Bitmap qr = QRCodeFactory
                                    .createQRImage("ftp://admin:123456@" + ip + ":2121", 100, 100);
                            Drawable qrDrawable = new BitmapDrawable(getResources(),qr);
                            qrDrawable.setBounds(0,0,132,132);
                            ip.setCompoundDrawables(null,qrDrawable,null,null);
                        }
                    }
                }
            }
        });
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE,
                permission.ACCESS_WIFI_STATE, permission.ACCESS_NETWORK_STATE, permission.INTERNET)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            ip.setText(getIPAddress(mContext));
                            startService(new Intent(MainActivity.this, FtpService.class));
                            bindService(new Intent(MainActivity.this, FtpService.class),
                                    conn,
                                    BIND_AUTO_CREATE);
                        } else {
                            exitForError();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        exitForError();
                    }
                });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void exitForError() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_denied)
                .setNegativeButton(R.string.sure, exitFromApp())
                .create().show();
    }

    private DialogInterface.OnClickListener exitFromApp() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                            en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                                enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()
                                    && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     */
    public String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}

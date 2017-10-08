package yanchao.bj.ngp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public class FtpService extends Service implements IFtp {

    private FtpServer server;

    public FtpService() {
    }

    public class MyBinder extends Binder {

        public IFtp getService() {
            return FtpService.this;
        }
    }

    private MyBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("service", "onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initFtp();
    }

    private void initFtp() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        //设置监听端口
        factory.setPort(2121);

        //替换默认监听
        serverFactory.addListener("default", factory.createListener());

        //用户名
        BaseUser user = new BaseUser();
        user.setName("admin");
        //密码 如果不设置密码就是匿名用户
        user.setPassword("123456");
        //用户主目录
        user.setHomeDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());

        List<Authority> authorities = new ArrayList<Authority>();
        //增加写权限
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);

        /**
         * 也可以使用配置文件来管理用户
         */
//      PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
//      userManagerFactory.setFile(new File("users.properties"));
//      serverFactory.setUserManager(userManagerFactory.createUserManager());
        //增加该用户
        try {
            serverFactory.getUserManager().save(user);
            server = serverFactory.createServer();
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand");
        if (server == null) {
            initFtp();
        }
        return Service.START_NOT_STICKY;
    }

    public FtpServer getServer() {
        return server;
    }

    @Override
    public boolean startFtp() {
        if (server != null) {
            try {
                if (server.isSuspended()) {
                    server.resume();
                } else {
                    server.start();
                }
                return true;
            } catch (FtpException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean stopFtp() {
        if (server != null) {
            server.suspend();
        }
        return server.isSuspended();
    }

    @Override
    public boolean ftpStatus() {
        if (server == null) {
            initFtp();
        }
        if (server.isStopped() || server.isSuspended()) {
            return false;
        }
        return true;
    }
}

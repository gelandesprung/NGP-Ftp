package yanchao.bj.ngp.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import yanchao.bj.ngp.R;

public class FTPClientActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private List<View> mViewList;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    return true;
            }
            return false;
        }
    };
    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    /**
     * 下载页面的刷新监听函数
     */
    private OnRefreshListener onDownloadViewSwipe = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            mFtpClient
                    .map(new Function<FTPClient, FTPFile[]>() {
                        @Override
                        public FTPFile[] apply(FTPClient ftpClient) throws Exception {
                            return ftpClient.listFiles();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(refreshRemoteView);
        }
    };
    /**
     * 上传页面的刷新监听函数
     */
    private OnRefreshListener onUploadViewSwipe = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            Observable.create(new ObservableOnSubscribe<File[]>() {
                @Override
                public void subscribe(ObservableEmitter<File[]> e) throws Exception {
                    File rootDir = new File(mUploadWorkingDirectory);
                    e.onNext(rootDir.listFiles());
                }
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe(refreshLocalView);
        }
    };

    private Context mContxt;

    private RecyclerView mDownloadRecyclerView;
    private RecyclerView mUploadRecyclerView;
    private SwipeRefreshLayout mDownloadSwipe;
    private SwipeRefreshLayout mUploadSwipe;
    private String mDownloadWorkingDirectory;
    private String mUploadWorkingDirectory;//local
    private DocumentListAdapter<FTPFile> mDownloadAdapter;
    private DocumentListAdapter<File> mUploadAdapter;
    private List<FTPFile> mDownloadData = new ArrayList<>();
    private List<File> mUploadData = new ArrayList<>();

    private Observable<FTPClient> mFtpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftpclient);

        mContxt = this;

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager.addOnPageChangeListener(onPageChangeListener);

        mViewList = new ArrayList<>(2);
        View downloadView = LayoutInflater.from(this).inflate(R.layout.download_view, null);
        mDownloadRecyclerView = downloadView.findViewById(R.id.ftp_client_list);
        mDownloadSwipe = downloadView.findViewById(R.id.swipe);
        mViewList.add(downloadView);
        View uploadView = LayoutInflater.from(this).inflate(R.layout.download_view, null);
        mUploadRecyclerView = uploadView.findViewById(R.id.ftp_client_list);
        mUploadSwipe = uploadView.findViewById(R.id.swipe);
        mViewList.add(mUploadRecyclerView);

        mDownloadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUploadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemDecoration commonDecoration = new CommonDecoration();
        mDownloadRecyclerView.addItemDecoration(commonDecoration);
        mUploadRecyclerView.addItemDecoration(commonDecoration);
        mDownloadRecyclerView.setHasFixedSize(true);
        mUploadRecyclerView.setHasFixedSize(true);

        mDownloadSwipe.setOnRefreshListener(onDownloadViewSwipe);
        mUploadSwipe.setOnRefreshListener(onUploadViewSwipe);

        mDownloadAdapter = new DocumentListAdapter<FTPFile>(mContxt, R.layout.document_item, mDownloadData) {
            @Override
            public void convert(DocumentViewHolder holder, FTPFile file) {
                TextView fileName = (TextView) holder.get(R.id.docment_name);
                fileName.setText(file.getName());
                ImageView header = (ImageView) holder.get(R.id.header);
                if (file.isDirectory()) {
                    header.setImageResource(R.mipmap.directory_icon);
                } else {
                    String name = file.getName();
                    if (name.endsWith(".doc")) {
                        header.setImageResource(R.mipmap.doc_file);
                    } else if (name.endsWith(".txt")) {
                        header.setImageResource(R.mipmap.txt_file);
                    } else if (name.endsWith(".zip")) {
                        header.setImageResource(R.mipmap.zip_file);
                    } else if (name.endsWith(".jpg") || name.endsWith(".png")) {
                        header.setImageResource(R.mipmap.jpg_file);
                    } else {
                        header.setImageResource(R.mipmap.document);
                    }
                }
            }
        };
        mUploadAdapter = new DocumentListAdapter<File>(mContxt, R.layout.document_item, mUploadData) {
            @Override
            public void convert(DocumentViewHolder holder, File file) {
                TextView fileName = (TextView) holder.get(R.id.docment_name);
                fileName.setText(file.getName());
                ImageView header = (ImageView) holder.get(R.id.header);
                if (file.isDirectory()) {
                    header.setImageResource(R.mipmap.directory_icon);
                } else {
                    String name = file.getName();
                    if (name.endsWith(".doc")) {
                        header.setImageResource(R.mipmap.doc_file);
                    } else if (name.endsWith(".txt")) {
                        header.setImageResource(R.mipmap.txt_file);
                    } else if (name.endsWith(".zip")) {
                        header.setImageResource(R.mipmap.zip_file);
                    } else if (name.endsWith(".jpg") || name.endsWith(".png")) {
                        header.setImageResource(R.mipmap.jpg_file);
                    } else {
                        header.setImageResource(R.mipmap.document);
                    }
                }
            }
        };
        mDownloadRecyclerView.setAdapter(mDownloadAdapter);
        mUploadRecyclerView.setAdapter(mUploadAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initData() {
        if (mUploadWorkingDirectory == null) {
            mUploadWorkingDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        mUploadData = Arrays.asList(new File(mUploadWorkingDirectory).listFiles());
        if (mDownloadWorkingDirectory == null) {
            if (mFtpClient == null) {
                mFtpClient = createFTPClient();
            }
            mFtpClient.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<FTPClient>() {
                        @Override
                        public void accept(FTPClient ftpClient) throws Exception {
                            mDownloadWorkingDirectory = ftpClient.printWorkingDirectory();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(mContxt, throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private Observable<FTPClient> createFTPClient() {
        return Observable.create(new ObservableOnSubscribe<FTPClient>() {
            @Override
            public void subscribe(ObservableEmitter<FTPClient> e) throws Exception {
                FTPClient ftpClient = new FTPClient();
                FTPClientConfig config = new FTPClientConfig();
                config.setServerTimeZoneId("asia/shanghai");
                ftpClient.configure(config);
                ftpClient.enterLocalPassiveMode();
                try {
                    ftpClient.connect("192.168.1.74", 2121);
                    ftpClient.login("admin", "123456");
                    int errorCode = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(errorCode)) {
                        ftpClient.disconnect();
                        e.onError(new Exception(getString(R.string.login_fail_tip)));
                    }
                    ftpClient.enterLocalPassiveMode();
                    e.onNext(ftpClient);
                    e.onComplete();
                } catch (IOException exception) {
                    e.onError(exception);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private Observer<FTPFile[]> refreshRemoteView = new Observer<FTPFile[]>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(FTPFile[] ftpFiles) {
            mDownloadData.clear();
            mDownloadData.addAll(Arrays.asList(ftpFiles));
            mDownloadAdapter.notifyDataSetChanged();
            mDownloadSwipe.setRefreshing(false);
        }

        @Override
        public void onError(Throwable e) {
            mDownloadSwipe.setRefreshing(false);
            Toast.makeText(mContxt, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {

        }
    };
    private Observer<File[]> refreshLocalView = new Observer<File[]>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(File[] files) {
            mUploadData.clear();
            mUploadData.addAll(Arrays.asList(files));
            mUploadAdapter.notifyDataSetChanged();
            mUploadSwipe.setRefreshing(false);
        }

        @Override
        public void onError(Throwable e) {
            mDownloadSwipe.setRefreshing(false);
            Toast.makeText(mContxt, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {

        }
    };
}

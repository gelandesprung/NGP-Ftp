package yanchao.bj.ngp.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
import yanchao.bj.ngp.utils.OnItemClickListener;

public class FTPClientActivity extends AppCompatActivity {

    private static final String TAG = "FTPClient";

    private static final int PAGE_UPLOAD = 0;
    private static final int PAGE_DOWNLOAD = 1;
    private ViewPager viewPager;
    private List<View> mViewList;
    private BottomNavigationView mNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener = new BottomNavigationView
            .OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_upload:
                    viewPager.setCurrentItem(PAGE_UPLOAD);
                    return true;
                case R.id.navigation_download:
                    viewPager.setCurrentItem(PAGE_DOWNLOAD);
                    return true;
            }
            return false;
        }
    };


    private Context mContxt;

    private RecyclerView mDownloadRecyclerView;
    private RecyclerView mUploadRecyclerView;
    private SwipeRefreshLayout mDownloadSwipe;
    private SwipeRefreshLayout mUploadSwipe;
    private String mDownloadWorkingDirectory = "";
    private File mUploadWorkingDirectory;//local
    private String mDownloadRootDirectory;
    private String mUploadRootDirectory;//local
    private DocumentListAdapter<FTPFile> mDownloadAdapter;
    private DocumentListAdapter<File> mUploadAdapter;
    private List<FTPFile> mDownloadData = new ArrayList<>();
    private List<File> mUploadData = new ArrayList<>();

    private Observable<FTPClient> mFtpClient;
    private PagerAdapter viewPagerAdapter;
    private Toolbar toolbar;
    private TextView mPathExhibition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftpclient);

        mContxt = this;

        toolbar = (Toolbar) findViewById(R.id.client_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(navigateBackClickListener);
        mPathExhibition = (TextView) findViewById(R.id.path_exhibition);

        mNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager.addOnPageChangeListener(onPageChangeListener);

        mViewList = new ArrayList<>(2);
        View downloadView = LayoutInflater.from(this).inflate(R.layout.download_view, null);
        mDownloadRecyclerView = downloadView.findViewById(R.id.ftp_client_list);
        mDownloadSwipe = downloadView.findViewById(R.id.swipe);
        View uploadView = LayoutInflater.from(this).inflate(R.layout.download_view, null);
        mUploadRecyclerView = uploadView.findViewById(R.id.ftp_client_list);
        mUploadSwipe = uploadView.findViewById(R.id.swipe);
        mViewList.add(0, uploadView);
        mViewList.add(1, downloadView);

        mDownloadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUploadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemDecoration commonDecoration = new CommonDecoration();
        mDownloadRecyclerView.addItemDecoration(commonDecoration);
        mUploadRecyclerView.addItemDecoration(commonDecoration);
        mDownloadRecyclerView.setHasFixedSize(true);
        mUploadRecyclerView.setHasFixedSize(true);

        mDownloadSwipe.setOnRefreshListener(onDownloadViewSwipe);
        mUploadSwipe.setOnRefreshListener(onUploadViewSwipe);

        mDownloadAdapter = new DocumentListAdapter<FTPFile>(mContxt, R.layout.document_item,
                mDownloadData) {
            @Override
            public void convert(DocumentViewHolder holder, FTPFile file) {
                Log.d(TAG, "convert: file = " + file.getName());
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
        mUploadAdapter = new DocumentListAdapter<File>(mContxt, R.layout.document_item,
                mUploadData) {
            @Override
            public void convert(DocumentViewHolder holder, File file) {
                Log.d(TAG, "convert: file = " + file.getName());
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
        mDownloadAdapter.setItemClickListener(onDownloadItemClicked);
        mUploadAdapter.setItemClickListener(onUploadItemClicked);
        mDownloadRecyclerView.setAdapter(mDownloadAdapter);
        mUploadRecyclerView.setAdapter(mUploadAdapter);

        viewPagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = mViewList.get(position);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        };
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        refresh();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        mFtpClient.subscribe(FTPClient::disconnect);
        mViewList.clear();
    }

    private void initData() {
        Log.d(TAG, "initData");
        mUploadRootDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (mUploadWorkingDirectory == null) {
            mUploadWorkingDirectory = new File(mUploadRootDirectory);
        }
        mDownloadRootDirectory = "/";
        if (mDownloadWorkingDirectory == null || mFtpClient == null) {
            if (mFtpClient == null) {
                mFtpClient = createFTPClient();
            }
            mFtpClient.observeOn(AndroidSchedulers.mainThread()).subscribe(ftpClient ->
            {
                mDownloadWorkingDirectory = ftpClient.printWorkingDirectory();
                mDownloadRootDirectory = ftpClient.printWorkingDirectory();
            }, throwable ->
                    Toast.makeText(mContxt, throwable.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private OnClickListener navigateBackClickListener = v -> dispatcheBackEvent();

    private void dispatcheBackEvent() {
        Log.d(TAG, "dispatcheBackEvent " + mUploadWorkingDirectory.getAbsolutePath());
        switch (viewPager.getCurrentItem()) {
            case PAGE_UPLOAD:
                Observable.just(mUploadWorkingDirectory.getParentFile())
                        .map(file -> {
                            mUploadWorkingDirectory = file;
                            return mUploadWorkingDirectory.listFiles();
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(refreshLocalView);
                break;
            case PAGE_DOWNLOAD:
                mFtpClient
                        .map(ftpClient -> {
                            ftpClient.changeWorkingDirectory(mDownloadWorkingDirectory);
                            if (ftpClient.changeToParentDirectory()) {
                                mDownloadWorkingDirectory = ftpClient.printWorkingDirectory();
                            }
                            return ftpClient.listFiles();
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(refreshRemoteView);
                break;
            default:
                break;
        }
    }

    private OnItemClickListener<FTPFile> onDownloadItemClicked = new OnItemClickListener<FTPFile>() {
        @Override
        public void onItemClickListener(View view, FTPFile itemData) {
            Log.d(TAG, "mDownloadWorkingDirectory =" + mDownloadWorkingDirectory
                    + " onDownloadItemClicked: " + itemData.getName());
            Observable.just(itemData)
                    .filter(FTPFile::isDirectory)
                    .flatMap(ftpFile -> mFtpClient)
                    .doOnNext(ftpClient -> {
                        ftpClient.changeWorkingDirectory(
                                mDownloadWorkingDirectory + File.separator + itemData.getName());
                        mDownloadWorkingDirectory = ftpClient.printWorkingDirectory();

                    })
                    .map(FTPClient::listFiles)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(refreshRemoteView);
        }

        @Override
        public void onItemLongClickListener(View view, FTPFile itemData) {

        }
    };
    private OnItemClickListener<File> onUploadItemClicked = new OnItemClickListener<File>() {
        @Override
        public void onItemClickListener(View view, File itemData) {
            Log.d(TAG, "mUploadWorkingDirectory =" + mUploadWorkingDirectory
                    + " onUploadItemClicked: " + itemData.getName());

            Observable.just(itemData).filter(File::isDirectory)
                    .map(file -> {
                        File child = new File(
                                mUploadWorkingDirectory.getAbsolutePath() + File.separator + file.getName());
                        if (child.exists() && child.isDirectory()) {
                            mUploadWorkingDirectory = child.getAbsoluteFile();
                        }
                        return mUploadWorkingDirectory.listFiles();
                    }).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(refreshLocalView);
        }

        @Override
        public void onItemLongClickListener(View view, File itemData) {

        }
    };

    /**
     * 触发刷新动作,刷新当前页面的数据
     */
    private void refresh() {
        Log.d(TAG, "refresh");
        switch (viewPager.getCurrentItem()) {
            case PAGE_UPLOAD:
                onUploadViewSwipe.onRefresh();
                break;
            case PAGE_DOWNLOAD:
                onDownloadViewSwipe.onRefresh();
                break;
            default:
                onUploadViewSwipe.onRefresh();
                break;
        }
    }

    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case PAGE_DOWNLOAD:
                    mNavigationView.setSelectedItemId(R.id.navigation_download);
                    if (mDownloadWorkingDirectory.equals(mDownloadRootDirectory)) {
                        toolbar.setNavigationIcon(null);
                        mPathExhibition.setText(mDownloadRootDirectory);
                    } else {
                        mPathExhibition.setText(mDownloadWorkingDirectory);
                        toolbar.setNavigationIcon(R.drawable.arrow_back_black_24dp);
                    }
                    break;
                default:
                    mNavigationView.setSelectedItemId(R.id.navigation_upload);
                    if (mUploadWorkingDirectory.getAbsolutePath().equals(mUploadRootDirectory)) {
                        toolbar.setNavigationIcon(null);
                        mPathExhibition.setText(mUploadRootDirectory);
                    } else {
                        mPathExhibition.setText(mUploadWorkingDirectory.getAbsolutePath());
                        toolbar.setNavigationIcon(R.drawable.arrow_back_black_24dp);
                    }
                    break;
            }
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
            mFtpClient.doOnNext(ftpClient -> {
                Log.d(TAG, "refresh------" + ftpClient.printWorkingDirectory());
                ftpClient.changeWorkingDirectory(mDownloadWorkingDirectory);
            })
                    .map(FTPClient::listFiles)
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
            Observable.create((ObservableOnSubscribe<File[]>) e -> {
                File rootDir = mUploadWorkingDirectory;
                e.onNext(rootDir.listFiles());
                e.onComplete();
            }).subscribeOn(AndroidSchedulers.mainThread()).subscribe(refreshLocalView);
        }
    };

    private Observable<FTPClient> createFTPClient() {
        Log.d(TAG, "createFTPClient");
        return Observable.create((ObservableOnSubscribe<FTPClient>) e -> {
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
            } catch (IOException exception) {
                e.onError(exception);
            }
        }).subscribeOn(Schedulers.io());
    }

    private Observer<FTPFile[]> refreshRemoteView = new Observer<FTPFile[]>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(FTPFile[] ftpFiles) {
            Log.d(TAG, "refreshRemoteView ");
            if (mDownloadWorkingDirectory.equals(mDownloadRootDirectory) || mDownloadWorkingDirectory.isEmpty()) {
                toolbar.setNavigationIcon(null);
                mPathExhibition.setText(mDownloadRootDirectory);
            } else {
                mPathExhibition.setText(mDownloadWorkingDirectory);
                toolbar.setNavigationIcon(R.drawable.arrow_back_black_24dp);
            }
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
            Log.d(TAG, "refreshLocalView workingdir=" + mUploadWorkingDirectory.getAbsolutePath());
            if (mUploadWorkingDirectory.getAbsolutePath().equals(mUploadRootDirectory)) {
                toolbar.setNavigationIcon(null);
                mPathExhibition.setText(mUploadRootDirectory);
            } else {
                mPathExhibition.setText(mUploadWorkingDirectory.getAbsolutePath());
                toolbar.setNavigationIcon(R.drawable.arrow_back_black_24dp);
            }
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

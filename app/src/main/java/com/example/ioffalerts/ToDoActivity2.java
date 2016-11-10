package com.example.ioffalerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import static junit.framework.Assert.*;

import com.google.android.gms.maps.GoogleMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class ToDoActivity2 extends Activity {
    /**
     * Create Google Map Variable
     */
    private GoogleMap mMap;
    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    /**
     * Mobile Service Table used to access data
     */
    private MobileServiceTable<Devices> mToDoTable;

    //Offline Sync
    /**
     * Mobile Service Table used to access and Sync data
     */
    //private MobileServiceSyncTable<ToDoItem> mToDoTable;

    /**
     * Adapter to sync the items list with the view
     */
    private ToDoItemAdapter mAdapter;

    String currentString;
    String currentTime, currentProb;
    ProgressBar mProgressBar;
    TextView view;
    TextView view2;
    TextView view3;
    TextView view4;
    ImageView images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_2);


        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Home");
        Bundle bundle = getIntent().getExtras();
        currentString = bundle.getString("currentString");
        currentTime = bundle.getString("currentTime");
        currentProb = bundle.getString("currentProb");
        view = (TextView) findViewById(R.id.textView1);
        view2 = (TextView) findViewById(R.id.textView2);
        view3 = (TextView) findViewById(R.id.textView3);
        view4 = (TextView) findViewById(R.id.textView4);
        view.setText(currentString);
        images = (ImageView) findViewById(R.id.DeviceMap);







        // Initialize the progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar2);


        mProgressBar.setVisibility(ProgressBar.GONE);


        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://ioffalerts.azurewebsites.net",
                    this).withFilter(new ProgressFilter());

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            // Get the Mobile Service Table instance to use

            mToDoTable = mClient.getTable(Devices.class);

            // Offline Sync
            //mToDoTable = mClient.getSyncTable("ToDoItem", ToDoItem.class);

            //Init local storage
            initLocalStore().get();

            // Load the items from the Mobile Service
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Creates a dialog and shows it
     *
     * @param message The dialog message
     * @param title   The dialog title
     */


    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }


    /**
     * Initialize local storage
     *
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */


    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("text", ColumnDataType.String);
                    tableDefinition.put("complete", ColumnDataType.Boolean);

                    localStore.defineTable("ToDoItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }


    /**
     * Run an ASync task on the corresponding executor
     *
     * @param task
     * @return *
     **/


    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */


    private void createAndShowDialogFromTask(final Exception exception,final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, title);
            }
        });
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */


    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }

    /**
     * Refresh the list with the items in the Mobile Service Table
     */

    private List<Devices> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return mToDoTable.where().field("deviceName").
                eq(val(currentString)).execute().get();
    }


    /**
     * Refresh the list with the items in the Table
     */

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<Devices> results = refreshItemsFromMobileServiceTable();

                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            for (Devices item : results) {
                                view.setText(item.getDeviceName());
                                view2.setText(currentTime);
                                view3.setText(currentProb);
                                view4.setText(item.getInstructLocation());
                                try{
                                    String uri = "@drawable/" +item.getMapLocation();
                                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                                    Drawable res = getResources().getDrawable(imageResource);
                                    images.setImageDrawable(res);
                                }catch(Exception e){
                                    createAndShowDialogFromTask(e, "Image Error ");
                                }
                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }



}



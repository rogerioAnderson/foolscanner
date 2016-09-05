package com.coded2.foolsscanner;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;

import java.util.List;
import java.util.Random;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class
MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView scannerView;
    private final int PERMISSION_CAMERA=0;
    private FloatingActionButton fab;

    private boolean checkCameraPermission(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        boolean cameraPermission = permissionCheck == PackageManager.PERMISSION_GRANTED;

        if(!cameraPermission){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},PERMISSION_CAMERA);
        }

        return cameraPermission;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case PERMISSION_CAMERA:
                if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
                    qrScanner(null);
                return;
            default:
                break;
        }

    }

    public void qrScanner(View view){


        if(checkCameraPermission()){
            scannerView = new ZXingScannerView(this);
            setContentView(scannerView);
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }
    }

    @Override
    public void onBackPressed() {
        if(scannerView!=null){
            initContentVIew();
            scannerView.stopCamera();
            scannerView=null;
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentVIew();
    }

    private void initContentVIew() {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        boolean foolMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_long_press),false);

        if(foolMode){
            String foolMessage = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_fool_message),getString(android.R.string.unknownName));
            final String[] arrayMessage = foolMessage.split(";");
            final double seed = Math.floor(Math.random()*arrayMessage.length);

            fab.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    qrScanner(v);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showResult(ParsedResultType.TEXT,arrayMessage[(int) seed]);
                            initContentVIew();
                        }
                    }, 7000);
                    return true;
                }
            });
        }


        ListView view = (ListView) findViewById(R.id.listView);
        List<History> records = History.list(this);
        final HistoryAdapter adapter = new HistoryAdapter(this,android.R.layout.simple_list_item_1,records);
        assert view != null;
        view.setAdapter(adapter);

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                History historyItem = adapter.getItem(position);
                showItem(historyItem,adapter);
            }
        });
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
            Intent it = new Intent(this,SettingsActivity.class);
            startActivity(it);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result result) {
        ParsedResult parsedResult = ResultParser.parseResult(result);
        scannerView.stopCamera();
        scannerView=null;
        showResult(parsedResult.getType(),parsedResult.getDisplayResult());
        initContentVIew();
    }

    private void showResult(ParsedResultType type, String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.result));

        builder.setMessage(result);

        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
        History history = new History();
        history.type=type.toString();
        history.content = result;
        history.save(this);
    }

    private void showItem(final History history, final HistoryAdapter adapter) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.detail_dialog, null);

        TextView value = (TextView) view.findViewById(R.id.value);

        value.setText(history.content);

        Button btnDelete = (Button) view.findViewById(R.id.delete_btn);

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.remove(history);
                alertDialog.dismiss();
            }
        });


        Button btnClipoard = (Button) view.findViewById(R.id.clipboard_btn);

        btnClipoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cpmanager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText(null,history.content);
                cpmanager.setPrimaryClip(data);
                Toast.makeText(MainActivity.this, R.string.copied_to_clipboard,Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });


        Button btnShare = (Button) view.findViewById(R.id.share_btn);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(history.content);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void share(String text){
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, getString(R.string.select_an_action)));

    }
}
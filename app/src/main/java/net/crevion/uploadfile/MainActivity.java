package net.crevion.uploadfile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView textViewStatus;
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private final static String BASE_URL = "http://192.168.1.14:3000"; // Change this to your server host

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 999);
            }
        });

        Button buttonSendJSON = (Button) findViewById(R.id.send_json);
        buttonSendJSON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("tes", "Hello World");
                    post(BASE_URL + "/api/tes", jsonObject.toString(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            onFailed();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            onSuccess();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        textViewStatus = (TextView) findViewById(R.id.status);
    }

    Call post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public Call uploadFile(String serverURL, File file, Callback callback) {

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg",
                        RequestBody.create(MediaType.parse("image/jpg"), file))

                .build();

        Request request = new Request.Builder().url(serverURL).post(formBody).build();

        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            File fileImage = null;
            try {
                fileImage = FileUtil.from(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadFile(BASE_URL + "/api/photo", fileImage, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onFailed();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.getString("status").equals("success")) {
                            onSuccess();
                        } else {
                            onFailed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onFailed();
                    }
                }
            });
        }
    }

    private void onSuccess() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewStatus.setText(R.string.success);
            }
        });
    }

    private void onFailed() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewStatus.setText(R.string.failed);
            }
        });
    }
}

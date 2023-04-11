package com.example.gpt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button button;
    private EditText edittext;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;

    private String conversation = "";

    private class MyTask extends AsyncTask<String, Void, List<String>> {

        private Context context;
        public MyTask(Context context) {
            this.context = context;
        }
        private OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS) // 设置连接超时为60秒
                .writeTimeout(180, TimeUnit.SECONDS) // 设置写超时为60秒
                .readTimeout(180, TimeUnit.SECONDS) // 设置读超时为60秒
                .build();

        @Override
        protected List<String> doInBackground(String... params) {
            String postData = params[0];
            // 执行网络请求操作
            List<String> results = new ArrayList<>();
            try {
                String url = "https://api.openai.com/v1/chat/completions"; // 替换为你的 API 地址
                String apiKey = "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"; // 替换为你的 API Key
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postData))
                        .build();
                //JSONObject jsonObjectSend = new JSONObject(postData);
                //conversation += jsonObjectSend.getJSONArray("messages").getJSONObject(0).toString();
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                response.close();
                // 将响应结果解析为字符串列表
                JSONObject jsonObject = new JSONObject(result);
                //JSONArray jsonArray = jsonObject.getJSONArray("choices");
                JSONArray choices = jsonObject.getJSONArray("choices");
                JSONObject choiceObject = choices.getJSONObject(0);
                JSONObject messageObject  = choiceObject.getJSONObject("message");
                conversation += "," + messageObject.toString();
                String content = messageObject.getString("content");
                results.add(content);
            } catch (IOException | JSONException e) {
                results.add(e.getMessage());
                return results;
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<String> results) {
            String joinedResults = TextUtils.join(", ", results);
            Toast toast = Toast.makeText(context,joinedResults, Toast.LENGTH_LONG);
            //toast.show();
            // 更新 UI
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String formattedTime = sdf.format(now);
            mAdapter.addAll(formattedTime + " " + results.toString());

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 ListView 和 ArrayAdapter
        mListView = findViewById(R.id.list_view_chat);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // 设置 ListView 的适配器
        mListView.setAdapter(mAdapter);

        button = findViewById(R.id.button_send);
        edittext = findViewById(R.id.edit_text_message);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_send:
                // 在这里编写 button1 的点击事件处理代码
                MyTask task = new MyTask(MainActivity.this);
                //task.execute("{\"model\": \"gpt-3.5-turbo\",\"messages\": [" + conversation + "{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}]}");
                if(conversation == ""){
                    task.execute("{\"model\": \"gpt-3.5-turbo\",\"messages\": [" + conversation + "{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}]}");
                    conversation += "{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}";
                }else {
                    task.execute("{\"model\": \"gpt-3.5-turbo\",\"messages\": [" + conversation + ",{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}]}");
                    conversation += ",{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}";
                }
                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String formattedTime = sdf.format(now);
                mAdapter.addAll(formattedTime + " " + edittext.getText().toString());
                edittext.setText("");
                break;
        }
    }
}
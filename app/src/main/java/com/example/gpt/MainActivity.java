package com.example.gpt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
    private RecyclerView mRecyclerView;
    private int apimode = 0;
    private EditText edittext;
    private ListView mListView;
    private Menu myMenu;

    private ArrayAdapter<String> mAdapter;
    private ChatAdapter mChatAdapter;
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
            mChatAdapter.addMessage(formattedTime + " " + results.toString());
        }
    }

    private class MyTaskImage extends AsyncTask<String, Void, List<String>> {

        private Context context;
        public MyTaskImage(Context context) {
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
                String url = "https://api.openai.com/v1/images/generations"; // 替换为你的 API 地址
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
                JSONArray dataArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String imageurl = dataObject.getString("url");
                    results.add(imageurl);
                }

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
//            Date now = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//            String formattedTime = sdf.format(now);
//            mAdapter.addAll(formattedTime + " " + results.toString());
//            mChatAdapter.addMessage(formattedTime + " " + results.toString());
            for (String url : results) {
                mChatAdapter.addImageUrl(url);
            }

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 ListView 和 ArrayAdapter
        mRecyclerView  = findViewById(R.id.recycler_view_chat);
        mChatAdapter = new ChatAdapter(getData());
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.my_image);
        //mChatAdapter.addImage(image);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // 设置 ListView 的适配器
        //mListView.setAdapter(mAdapter);
        mRecyclerView.setAdapter(mChatAdapter);

        button = findViewById(R.id.button_send);
        edittext = findViewById(R.id.edit_text_message);
        button.setOnClickListener(this);


    }

    private List<Object> getData() {
        // 返回要在RecyclerView中显示的数据集合
        List<Object> data = new ArrayList<>();
//        data.add("Hello");
//        data.add("Hi");
//        data.add("How are you?");
//        data.add("I'm fine, thanks!");
        return data;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_send:
                if(apimode == 0) {
                    // 在这里编写 button1 的点击事件处理代码
                    MyTask task = new MyTask(MainActivity.this);
                    //task.execute("{\"model\": \"gpt-3.5-turbo\",\"messages\": [" + conversation + "{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}]}");
                    if (conversation == "") {
                        task.execute("{\"model\": \"gpt-3.5-turbo\",\"messages\": [" + conversation + "{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}]}");
                        conversation += "{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}";
                    } else {
                        task.execute("{\"model\": \"gpt-3.5-turbo\",\"messages\": [" + conversation + ",{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}]}");
                        conversation += ",{\"role\": \"user\", \"content\": \"" + edittext.getText() + "\"}";
                    }
                    Date now = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String formattedTime = sdf.format(now);
                    mAdapter.addAll(formattedTime + " " + edittext.getText().toString());
                    mChatAdapter.addMessage(formattedTime + " " + edittext.getText().toString());
                    edittext.setText("");
                } else if (apimode == 1) {
                    MyTaskImage task = new MyTaskImage(MainActivity.this);
                    task.execute("{\n" +
                            "    \"prompt\":\"" + edittext.getText().toString() + "\",\n" +
                            "    \"n\": 5,\n" +
                            "    \"size\": \"1024x1024\"\n" +
                            "  }");
                    Date now = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String formattedTime = sdf.format(now);
                    mAdapter.addAll(formattedTime + " " + edittext.getText().toString());
                    mChatAdapter.addMessage(formattedTime + " " + edittext.getText().toString());
                    edittext.setText("");
                }else {

                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item:
                // 在此处处理菜单项点击事件

                MenuItem menuItem = myMenu.findItem(R.id.menu_item);
                if(menuItem.getTitle().toString().equals("Chat Mode")){
                    menuItem.setTitle("Image Mode");
                    apimode = 1;
                }else {
                    menuItem.setTitle("Chat Mode");
                    apimode = 0;
                }
                Toast toast = Toast.makeText(this,String.valueOf(apimode), Toast.LENGTH_LONG);
                toast.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
package com.kosmo.a34http01;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String TAG = "KOSMO123";

    TextView textResult;
    ProgressDialog dialog;
    int buttonResId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textResult = findViewById(R.id.text_result);
        Button btnJson = findViewById(R.id.btn_json);
        btnJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 누른 버튼의 아이디를 얻어온다.
                buttonResId = view.getId();
                /*
                버튼 클릭시 로컬 Spring API서버와의 통신을 위해 객체를 생성한 후
                첫번째 인수로 접속 URL을 전달한다. execute() 메소드 호출을 통해
                doInBackground() 가 호출된다.
                 */
                new AsyncHttpRequest().execute(
                        "http://192.168.0.10:8082/jsonrestapi/android/memberList.do"
                );
            }
        });

        // 서버와 통신시 진행대화창을 띄우기 위한 객체 생성
        dialog = new ProgressDialog(this);
        /*
        setProgressStyle : 스타일
        setIcon : 아이콘
        setTitle : 타이틀
        setMessage : 메세지
        setCancelable : back버튼에 대한 설정
                        (false로 설정시 back버튼을 눌러도 닫히지 않는다.)
         */
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle("회원 정보 리스트 가져오기");
        dialog.setMessage("서버로부터 응답을 기다리고 있습니다.");
        dialog.setCancelable(false);


    } //// onCreate()

    class AsyncHttpRequest extends AsyncTask<String, Void, String>{

        // doInBackground() 메소드 실행 전에 호출됨
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*
            서버와 통신 직전 진행대화창이 있는지 확인 후
            없다면 띄워준다.
            */
            if(!dialog.isShowing()){
                dialog.show();
            }
        }

        /*
        execute() 호출시 전달된 파라미터를 가변인자가 받게 된다.
        가변 인자는 여러개의 파라미터를 하나의 변수로 받을수 있도록
        배열로 사용하게 된다.
         */
        @Override
        protected String doInBackground(String... strings) {

            // 스프링 API 서버에서 반환하는 JSON 데이터를 저장할 변수
            StringBuffer sBuffer = new StringBuffer();

            try{
                // 0번째 인자를 통해 전달된 서버 URL을 인자로 URL 객체를 생성
                URL url = new URL(strings[0]);
                // URL을 통해 연결할 객체 생성
                HttpURLConnection connection =
                        (HttpURLConnection)url.openConnection();
                // 통신 방식을 POST로 설정
                connection.setRequestMethod("POST");
                // 쓰기모드 지정
                connection.setDoOutput(true);
                /*
                서버로 요청할 파라미터를 설정한다. 해당 예제에서는 통신시
                전달할 별도의 파라미터가 없으므로 아래 부분에 별다른
                설정은 없다.
                 */
                OutputStream out = connection.getOutputStream();

                /*
                out.write(strings[1].getBytes());
                파라미터가 2개 이상이라면 &로 문자열을 연결
                out.write("&".getBytes());
                out.write(strings[2].getBytes());
                */

                out.flush();
                out.close();

                // 서버에 요청이 전달되고 성공이라면 HTTP_OK로 확인 가능
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){

                    Log.i(TAG,"HTTP OK 성공");
                    // 서버로부터 받은 응답데이터인 JSON을 스트림을 통해 읽어 저장한다.
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(),
                                    "UTF-8")
                    );

                    String responseData;

                    while((responseData=reader.readLine()) !=null){
                        // 내용을 한줄씩 읽어서 StringBuffer 객체에 저장한다.
                        sBuffer.append(responseData+"\n\r");
                    }
                    reader.close();



                }else {
                    Log.i(TAG, "HTTP OK 안됨");
                }

                // 누른 버튼이 "회원리스트가져오기" 라면
                if(buttonResId == R.id.btn_json){

                    Log.i(TAG, sBuffer.toString());

                    // JSON 배열로 파싱한다.
                    JSONArray jsonArray = new JSONArray(sBuffer.toString());
                    // StringBuffer 객체를 비워줌(내용을 지움)
                    sBuffer.setLength(0);
                    // 배열의 크기만큼 반복하면서 JSON 객체를 키값을 통해 파싱한다.
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        // 파싱한 값은 StringBuffer 객체에 저장한다.
                        sBuffer.append("아이디 : "+jsonObject.getString("id")+"\n\r");
                        sBuffer.append("패스워드 : "+jsonObject.getString("pass")+"\n\r");
                        sBuffer.append("이름 : "+jsonObject.getString("name")+"\n\r");
                        sBuffer.append("가입날짜 : "+jsonObject.getString("regidate")+"\n\r");
                        sBuffer.append("-------------------------\n\r");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            /*
            파싱이 완료된 StringBuffer 객체를 String으로 변환 후 반환한다.
            여기서 반환된 값은 onPostExecute()로 전달된다.
             */
            return sBuffer.toString();
        }

        // doInBackground()가 정상 종료되면 해당 함수가 호출된다.
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // 진행 대화창을 닫아준다.
            dialog.dismiss();
            // 결과값을 텍스트뷰에 출력한다.
            textResult.setText(s);
        }
    }
}
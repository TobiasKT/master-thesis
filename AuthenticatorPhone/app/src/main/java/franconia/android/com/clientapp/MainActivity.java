package franconia.android.com.clientapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    //Public IP (MAC OS Terminal: ifconfig |grep inet)
   // private static final String HOSTNAME = "192.168.178.96";
    //private static int mPort = 8080;

//    Socket socket = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        new Thread() {

            @Override
            public void run() {



                try {
                    //Connecting
                    Log.i(LOG_TAG, "Attempting to connect server");
                    socket = new Socket(HOSTNAME, mPort);
                    Log.i(LOG_TAG, "Connection established");


                    // Receive message from the Server
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println("Message from the Server: " + bufferedReader.readLine());


                } catch (IOException ioe) {

                    Log.e(LOG_TAG, ioe.getMessage());
                }
            }
        }.start();
*/
    }

    public void onBtnSendClicked(View v) {

        ClientTask clientTask = new ClientTask(this.getApplicationContext());
        clientTask.execute();
/*
        EditText editText = (EditText)findViewById(R.id.editText);
        try {

            //Send Message to the Server
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(editText.getText().toString());
            bufferedWriter.newLine();
            bufferedWriter.flush();

        }catch (IOException ioe){
            Log.e(LOG_TAG, ioe.getMessage());
        }*/


    }
}



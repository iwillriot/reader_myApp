package coolcatmeow.org.helloworld;

import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private Button ourButton;
    private TextView ourMessage;
    private String result;
    private int i = 0;
    private int currentWord =0;


    private TextView statusView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private JSONArray loadUrl(String location) {
        LinkedList<String> lines = new LinkedList<String>();

        // Download URL to array of lines
        try {
            // Open URL
            URL url = new URL(location);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Open buffered reader
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader buf = new BufferedReader(isr);

            // Read data into lines array
            String line;
            while ((line = buf.readLine()) != null)
                lines.add(line);

            // Close keep-alive connections
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.d("SfvLug", "Invalid URL: " + location);
        } catch (IOException e) {
            Log.d("SfvLug", "Download failed for: " + location);
        }

        // Load text into JSON Object
        String text = TextUtils.join("", lines);
        try {
            return new JSONArray(text);
        } catch (JSONException e) {
            Log.d("SfvLug", "Invalid JSON Object: " + text);
            return null;
        }
    }


    /* Display the meeting status to the user */
    private void setStatus(final JSONArray status) {



        // Extract info from JSON object and format output messages
        try {
            JSONObject text = status.getJSONObject((i % status.length()));

            String qouteString = text.getString("text");

            Spannable wordtoSpan = new SpannableString(qouteString);

            int prev = 0, count = 0;

            //For loop for each word and create a new spannable
            for(String highLightWord: qouteString.split(" ")) {
                final int LOCALCOUNT = count++;

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {

                        currentWord++;
                        setStatus(status);

                        new AsyncTask<String, Void, JSONArray>() {


                            protected JSONArray doInBackground(String... args) {

                                return MainActivity.this.loadUrl(args[0]);
                            }

                        }.execute("http://peer-reader.ddns.net/store.php?book=0&student=0&word=" + LOCALCOUNT);

                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(true);
                    }
                };

                if(LOCALCOUNT == currentWord) {
                    wordtoSpan.setSpan(clickableSpan, prev, prev + highLightWord.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                else if ( currentWord > LOCALCOUNT){
                    wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), prev, prev + highLightWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                }
                prev = prev + highLightWord.length() + 1;

            }
            ourMessage.setMovementMethod(LinkMovementMethod.getInstance());
            ourMessage.setText(wordtoSpan);

        } catch (NullPointerException e) {
            ourMessage.setText("Error downloading meeting status");

        } catch (JSONException e) {

            ourMessage.setText("Error parsing meeting status: ");

        }

    }


    /* Trigger a refresh of the meeting status */
    private void loadStatus() {
        // Set URI location
        String location = "http://peer-reader.ddns.net/jj.php";

        ourMessage.setText("Loading status.. ");


        // Update status in a background thread
        //
        // In Android, we normally cannot access the network from the main
        // thread; doing so would cause the user interface to freeze during
        // data transfer.
        //
        // Again, android provides several ways around this, here we use an
        // AsyncTask which lets us run some code in a background thread and
        // then update the user interface once the background code has
        // finished.
        //
        // The first Java Generic parameter are:
        //   1. String     - argument for doInBackground, from .execute()
        //   2. Void       - not used here, normally used for progress bars
        //   3. JSONObject - the return type from doInBackground which is
        //                   passed to onPostExecute function.
        new AsyncTask<String, Void, JSONArray>() {

            // Called from a background thread, so we don't block the user
            // interface. Using AsyncTask synchronization is handled for us.
            protected JSONArray doInBackground(String... args) {
                // Java passes this as a variable argument array,
                // but we only use the first entry.
                return MainActivity.this.loadUrl(args[0]);
            }

            // Called once in the main thread once doInBackground finishes.
            // This is executed in the Main thread once again so that we can
            // update the user interface.
            protected void onPostExecute(JSONArray status) {
                ourMessage.setText("Loading Post Execute.. ");
                MainActivity.this.setStatus(status);
            }

        }.execute(location);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This is content on the screen
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Link Button to Textview
        ourButton = (Button) findViewById(R.id.button);
        ourMessage = (TextView) findViewById(R.id.textView);


        //Listen on the button to be click
        View.OnClickListener ourOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 currentWord = 0;
                 loadStatus();
                 i++;
            }
        };
//
        ourButton.setOnClickListener(ourOnClickListener);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //Add menu_main on screen
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
           Toast toastMessage = Toast.makeText(this, "Text value is now " + ourMessage.getText(), Toast.LENGTH_SHORT);
             toastMessage.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://coolcatmeow.org.helloworld/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://coolcatmeow.org.helloworld/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}

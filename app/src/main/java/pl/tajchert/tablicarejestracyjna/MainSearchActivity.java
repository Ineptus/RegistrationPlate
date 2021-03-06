package pl.tajchert.tablicarejestracyjna;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import pl.tajchert.tablicarejestracyjna.api.Tablica;


public class MainSearchActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "MainSearchActivity";
    private AddFloatingActionButton fab;
    private RequestQueue queue;
    private boolean votedForThatPlate = false;
    private Tablica currentTablica;
    private LinearLayout buttonsVotingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_search);



        queue = Volley.newRequestQueue(this);

        fab = (AddFloatingActionButton) findViewById(R.id.normal_plus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        LinearLayout voteUp = (LinearLayout) findViewById(R.id.voteUp);
        LinearLayout voteDown = (LinearLayout) findViewById(R.id.voteDown);
        buttonsVotingLayout = (LinearLayout) findViewById(R.id.buttonsVoting);

        voteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick UPVOTE!");
                if(currentTablica != null && currentTablica.getId() != null && votedForThatPlate == false){
                    vote(currentTablica.getId(), 1);
                }
            }
        });

        voteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick DOWNVOTE!");
                if(currentTablica != null && currentTablica.getId() != null && votedForThatPlate == false){
                    vote(currentTablica.getId(), (-1));
                }
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if(s != null && s.length() > 0) {
            s = "PO6A822";
            search(s);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(s.length() >= 5){
            //We can assume it is correct plate number
        }
        return false;
    }

    private void search(final String plateNumber) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, APIConstants.TABLICE_INFO_PLATE + plateNumber, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse response: " +response.toString());
                Gson gson = new Gson();
                Tablica tablica = gson.fromJson(response.toString(), Tablica.class);
                tablica.setId(plateNumber);
                currentTablica = tablica;
                setPlateView(tablica);
                Log.d(TAG, "onResponse tablica: " + tablica);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse :" + error.getMessage());
            }
        });
        queue.add(jsObjRequest);
    }

    private void vote(String plateNumber, final int value) {
        // 1 - upvote, (-1) - downvote
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, APIConstants.TABLICE_INFO_PLATE + plateNumber + APIConstants.TABLICE_INFO_VOTE_ADD  + value, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse response: " +response.toString());
                String status = response.optString("status");
                if(status != null && status.equals("ok")){
                    //OK!
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.vote_success), Toast.LENGTH_SHORT).show();
                    if(value == 1) {
                        TextView voteUpText = (TextView) findViewById(R.id.voteUpText);
                        voteUpText.setText((currentTablica.getLapkiGora() + 1) + "");
                    } else if(value == (-1)) {
                        TextView voteUpText = (TextView) findViewById(R.id.voteUpText);
                        voteUpText.setText((currentTablica.getLapkiGora() - 1) + "");
                    }
                } else {
                    //NOT OK
                    //Voted?
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.vote_repeat), Toast.LENGTH_SHORT).show();
                }
                //Set button off
                if(buttonsVotingLayout != null) {
                    buttonsVotingLayout.setAlpha(0.4f);
                }
                votedForThatPlate = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //NOT OK
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.vote_error), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }

    private void setPlateView(Tablica tablica) {
        CardView cardViewPlate = (CardView) findViewById(R.id.cardViewPlate);
        cardViewPlate.setVisibility(View.VISIBLE);

        CardView cardViewHint = (CardView) findViewById(R.id.cardViewHint);
        cardViewHint.setVisibility(View.GONE);

        TextView textViewPlateId = (TextView) findViewById(R.id.textCardPlate);
        textViewPlateId.setText(tablica.getId()+"");

        TextView voteUpText = (TextView) findViewById(R.id.voteUpText);
        voteUpText.setText(tablica.getLapkiGora()+"");
        TextView voteDownText = (TextView) findViewById(R.id.voteDownText);
        voteDownText.setText(tablica.getLapkiDol()+"");

        if(buttonsVotingLayout != null) {
            buttonsVotingLayout.setAlpha(1.0f);
        }
        votedForThatPlate = false;

        //TODO comments
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }
}

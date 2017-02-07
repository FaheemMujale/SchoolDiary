package com.lead.infosystems.schooldiary.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.lead.infosystems.schooldiary.Data.PostCommentData;
import com.lead.infosystems.schooldiary.Data.UserDataSP;
import com.lead.infosystems.schooldiary.Generic.MyVolley;
import com.lead.infosystems.schooldiary.Generic.ServerConnect;
import com.lead.infosystems.schooldiary.Generic.Utils;
import com.lead.infosystems.schooldiary.IVolleyResponse;
import com.lead.infosystems.schooldiary.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostComments extends AppCompatActivity {

    private ImageView post_img,propic;
    private TextView name,time,text,commentNoComment;
    private EditText commentText;
    private ProgressBar progressBar;
    private MyAdaptor adaptor;
    private boolean canClickLike = true;
    private UserDataSP user;
    private UserDataSP userDataSP;
    private Activity activity = this;
    private ExpandableHeightListView commentsList;
    private PostAnimData postAnimData = PostAdaptor.postAnimData;
    private ArrayList<PostCommentData> items = new ArrayList<>();
    public static String ANIM_DATA = "anim_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);
        getSupportActionBar().setTitle("Comment");
        Bundle b = getIntent().getExtras();
//        if(b != null){
//            postAnimData = (PostAnimData) b.getSerializable(ANIM_DATA);
//        }
        propic = (ImageView) findViewById(R.id.propic);
        post_img = (ImageView) findViewById(R.id.postimage);
        name = (TextView) findViewById(R.id.title);
        time = (TextView) findViewById(R.id.time_rcv);
        text = (TextView) findViewById(R.id.text);
        commentsList = (ExpandableHeightListView) findViewById(R.id.comment_list);
        commentText = (EditText) findViewById(R.id.comment_text);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView2);
        commentNoComment = (TextView) findViewById(R.id.comment_nocomment);
        progressBar = (ProgressBar) findViewById(R.id.comment_progress);
        userDataSP = new UserDataSP(getApplicationContext());
        user = new UserDataSP(getApplicationContext());
        commentText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_UP);
                    }
                });
                return false;
            }

        });
        populateViews();

        commentsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                String myName = userDataSP.getUserData(UserDataSP.FIRST_NAME)+" "+userDataSP.getUserData(UserDataSP.LAST_NAME);
                if(myName.contentEquals(items.get(position).getName()) ||
                        postAnimData.getItem().getStudentNum() == Integer.parseInt(userDataSP.getUserData(UserDataSP.NUMBER_USER))) {
                    final CharSequence[] item = {"Delete"};
                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                    dialog.setItems(item, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connect(Utils.DELETE, position);
                        }
                    });
                    dialog.show();
                }
                return false;
            }
        });

    }


    private void populateViews(){
        propic.setImageDrawable(((ImageView) postAnimData.getPropic()).getDrawable());
        name.setText(((TextView) postAnimData.getName()).getText());
        time.setText(((TextView) postAnimData.getTime()).getText());
        text.setText(((TextView) postAnimData.getText()).getText());


        if(postAnimData.isImageAvailable()){
            post_img.setVisibility(View.VISIBLE);
            post_img.setImageBitmap(postAnimData.getPostImageBitmap());
        }else{
            post_img.setVisibility(View.GONE);
        }
        connect(Utils.COMMENTS,0);
    }


    public void commentBtn(View v){
        if(commentText.getText().length()>0){
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST, Utils.LIKE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response != null && response.contains("DONE")){
                        // add only one item
                        connect(Utils.COMMENTS,0);
                        commentText.setText("");
                        Snackbar.make(findViewById(android.R.id.content),"Long press to delete your comment",Snackbar.LENGTH_LONG)
                                .show();
                        commentNoComment.setText("Comments");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),ServerConnect.connectionError(error),Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    HashMap<String,String> map = new HashMap<>();
                    map.put("number_user", userDataSP.getUserData(UserDataSP.NUMBER_USER));
                    map.put("post_id", postAnimData.getPostID());
                    map.put("comment_text",commentText.getText().toString());
                    return map;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy(2000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(policy);
            requestQueue.add(request);
        }else{
            Toast.makeText(getApplicationContext(),"No Comment text...",Toast.LENGTH_SHORT).show();
        }
    }

    private void connect(final String url, final int position){

        MyVolley volley = new MyVolley(getApplicationContext(), new IVolleyResponse() {
            @Override
            public void volleyResponse(String result) {
                if(result != MyVolley.RESPONSE_ERROR){
                    if(url == Utils.COMMENTS){
                        items.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            for(int i = 0 ;i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                items.add(new PostCommentData(jsonObject.getString("student_name"),
                                        jsonObject.getString(UserDataSP.PROPIC_URL),
                                        Utils.getTimeString(jsonObject.getString("date")),
                                        jsonObject.getString("comment_text"),jsonObject.getString("like"),
                                        jsonObject.getString("comment_id"),jsonObject.getString("user_liked")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            commentNoComment.setText("No comments");
                        }
                        adaptor = new MyAdaptor();
                        commentsList.setAdapter(adaptor);
                        commentsList.setExpanded(true);
                    }else if(url == Utils.DELETE){
                        if(result.contains("DONE")){
                            items.remove(position);
                            adaptor.notifyDataSetChanged();

                        }
                    }
                }else{
                    commentNoComment.setText("No comments");
                }

                if(items.size()>0){
                    commentNoComment.setText("Comments");
                }else{
                    commentNoComment.setText("No comments");
                }
                progressBar.setVisibility(View.GONE);
            }
        });
        volley.setUrl(url);
        if(url == Utils.COMMENTS){
            volley.setParams("post_id", postAnimData.getPostID());
            volley.setParams("number_user", userDataSP.getUserData(UserDataSP.NUMBER_USER));
        } else if(url == Utils.DELETE){
            volley.setParams("comment_id", items.get(position).getComment_id());
        }
        volley.connect();
    }

    class MyAdaptor extends ArrayAdapter<PostCommentData>{

        public MyAdaptor() {
            super(getApplicationContext(), R.layout.post_comment_item,items);
        }
        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView==null){
                itemView = getLayoutInflater().inflate(R.layout.post_comment_item,parent,false);
            }
            ImageView propic = (ImageView) itemView.findViewById(R.id.propic);
            TextView comment_name = (TextView) itemView.findViewById(R.id.title);
            TextView time = (TextView) itemView.findViewById(R.id.time_rcv);
            TextView text = (TextView) itemView.findViewById(R.id.text);
            final TextView likes = (TextView) itemView.findViewById(R.id.comment_likes_num);
            final LinearLayout likeView = (LinearLayout) itemView.findViewById(R.id.comments_likes);
            final TextView Like_btn = (TextView) itemView.findViewById(R.id.like);

            final PostCommentData currentItem = items.get(position);

            if(currentItem.getProfilePic_link() != null && currentItem.getProfilePic_link().contains("jpeg")){
                Picasso.with(activity.getApplicationContext()).load(Utils.SERVER_URL+currentItem.getProfilePic_link().replace("profilepic","propic_thumb"))
                        .networkPolicy(ServerConnect.checkInternetConenction(activity) ?
                                NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                        .into(propic);
            }else{
                propic.setImageDrawable(activity.getResources().getDrawable(R.drawable.defaultpropic));
            }
            comment_name.setText(currentItem.getName());
            time.setText(currentItem.getTime());
            text.setText(currentItem.getText());
            likes.setText(currentItem.getLikes()+"");
            setLikeView(currentItem.getLikes(),likes,likeView);

            Like_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!currentItem.isUser_liked()) {
                        connect(Utils.LIKE,currentItem.getComment_id(),Like_btn,currentItem,likeView,likes);
                    }else{
                        connect(Utils.DELETE,currentItem.getComment_id(),Like_btn,currentItem,likeView,likes);
                    }
                }
            });
            return itemView;
        }



    }
    private void connect(final String url, final String commentNum, final TextView like_btn
            , final PostCommentData currentItem, final LinearLayout likeView, final TextView likes){
        RequestQueue requestQueue = Volley.newRequestQueue(activity.getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response != null && response.contains("DONE")){
                    if(url == Utils.LIKE){
                        like_btn.setText("Unlike");
                        currentItem.setUser_liked(true);
                        currentItem.setLikes(currentItem.getLikes() + 1);
                    }else if(url == Utils.DELETE){
                        like_btn.setText("Like");
                        currentItem.setUser_liked(false);
                        currentItem.setLikes(currentItem.getLikes() - 1);
                    }
                }
                setLikeView(currentItem.getLikes(),likes,likeView);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity.getApplicationContext(),ServerConnect.connectionError(error),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("number_user",user.getUserData(UserDataSP.NUMBER_USER));
                map.put("comment_id",commentNum);
                return map;
            }
        };
        RetryPolicy retry = new DefaultRetryPolicy(2000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retry);
        requestQueue.add(request);
    }

    private void setLikeView(int likeNum,TextView likes,LinearLayout likeView){
        if(likeNum > 0){
            likes.setText(likeNum+"");
            likeView.setVisibility(View.VISIBLE);
        }else{
            likeView.setVisibility(View.INVISIBLE);
        }
    }
}

package com.skooterapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.skooterapp.data.Comment;
import com.skooterapp.data.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ViewPostActivity extends BaseActivity {

    private static final String LOG_TAG = ViewPostActivity.class.getSimpleName();
    protected List<Object> postList;
    protected Post mPost;
    protected ViewPostAdapter postAdapter;
    protected boolean canPerformActivity;

    protected LinearLayout flagView;
    protected LinearLayout deleteView;
    protected TextView typeIdView;
    protected TextView typeView;
    protected ListView listPosts;

    private final static int MAX_CHARACTERS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        setContentView(R.layout.activity_view_post);

        activateToolbarWithHomeEnabled("");

        flagView = (LinearLayout) findViewById(R.id.flag_view);
        deleteView = (LinearLayout) findViewById(R.id.delete_view);
        typeIdView = (TextView) findViewById(R.id.type_id);
        typeView = (TextView) findViewById(R.id.type);
        listPosts = (ListView) findViewById(R.id.list_posts);

        final int postId;

        Intent intent = getIntent();

        mPost = (Post) intent.getSerializableExtra(SKOOTER_POST);
        canPerformActivity = intent.getBooleanExtra("can_perform_activity", true);

        if (mPost == null) {
            postId = intent.getIntExtra(SKOOTER_POST_ID, 0);
            getCommentsForPostId(postId, userId, true);
        } else {
            postId = mPost.getId();
            getCommentsForPostId(mPost.getId(), userId, false);
            initListViews();
        }

        canPerformReplies(postId);
    }

    private void canPerformReplies(final int postId) {
        final TextView commentText = (TextView) findViewById(R.id.commentText);
        final Button commentBtn = (Button) findViewById(R.id.commentSkoot);

        if (canPerformActivity) {
            commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (commentText.getText().length() > 0 && commentText.getText().length() <= 200) {
                        String url = BaseActivity.substituteString(getResources().getString(R.string.comment_new), new HashMap<String, String>());
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user_id", Integer.toString(userId));
                        params.put("content", commentText.getText().toString());
                        params.put("location_id", Integer.toString(locationId));
                        params.put("post_id", Integer.toString(postId));

                        SkooterJsonObjectRequest jsonObjectRequest = new SkooterJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(LOG_TAG, response.toString());
                                commentText.setText("");
                                mPost.setUserCommented(true);
                                mPost.setCommentsCount(mPost.getCommentsCount() + 1);

                                postAdapter.notifyDataSetChanged();

                                final String SKOOT_COMMENTS = "comment";

                                try {
                                    JSONObject jsonObject = response.getJSONObject(SKOOT_COMMENTS);

                                    Comment commentObject = Comment.parseCommentFromJSONObject(jsonObject);
                                    if (commentObject != null) {
                                        postList.add(commentObject);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e(LOG_TAG, "Error processing JSON data");
                                }
                                postAdapter.notifyDataSetChanged();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                            }
                        });

                        AppController.getInstance().addToRequestQueue(jsonObjectRequest, "post_comment");

                    } else if (commentText.getText().length() > MAX_CHARACTERS) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                        alertDialogBuilder.setMessage("You cannot simply skoot with more than 200! For that you would have login through Facebook.");
                        alertDialogBuilder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                        alertDialogBuilder.setMessage("You cannot simply skoot with no content!");
                        alertDialogBuilder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
            });

            commentText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() >= MAX_CHARACTERS) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                        alertDialogBuilder.setMessage("You can reply with a maximum of 200 characters only!");
                        alertDialogBuilder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else {
            //listPosts.setEnabled(false);
            commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                    alertDialogBuilder.setMessage("You can't post outside 3 kms of the zone.");
                    alertDialogBuilder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
        }
    }

    protected void onPause() {
        super.onPause();
        //closing transition animations
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

    public void initListViews() {
        postList = new ArrayList<Object>();
        postList.add(mPost);

        postAdapter = new ViewPostAdapter(this, R.layout.list_view_post_row, postList, true, flagView, deleteView, typeIdView, typeView, canPerformActivity);
        listPosts.setAdapter(postAdapter);

        String tag = "load_comments";
    }

    public void getCommentsForPostId(int postId, int userId, final boolean parsePost) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", Integer.toString(userId));
        params.put("post_id", Integer.toString(postId));
        params.put("location_id", Integer.toString(BaseActivity.locationId));

        String url = substituteString(getResources().getString(R.string.skoot_single), params);

        SkooterJsonObjectRequest jsonObjectRequest = new SkooterJsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                final String SKOOT_COMMENTS = "comments";
                final String SKOOT_ACTIVITY = "activity_enabled";
                final String SKOOT = "skoot";

                Log.d(LOG_TAG, response.toString());
                try {
                    JSONArray jsonArray = response.getJSONArray(SKOOT_COMMENTS);
                    boolean canPerformActivity = response.getBoolean(SKOOT_ACTIVITY);

                    if (parsePost) {
                        mPost = Post.parsePostFromJSONObject(response.getJSONObject(SKOOT));
                        initListViews();
                    }

                    if(canPerformActivity != ViewPostActivity.this.canPerformActivity) {
                        ViewPostActivity.this.canPerformReplies(mPost.getId());
                        postAdapter = new ViewPostAdapter(ViewPostActivity.this, R.layout.list_view_post_row, postList, true, flagView, deleteView, typeIdView, typeView, canPerformActivity);
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Comment commentObject = Comment.parseCommentFromJSONObject(jsonArray.getJSONObject(i));
                        if (commentObject != null) {
                            postAdapter.add(commentObject);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error processing JSON data");
                }
                postAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest, "view_comments");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_share) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, mPost.getContent() + "- Shared via Skooter, a location based social network to connect with people nearby. http://get.skooterapp.com");
            startActivity(Intent.createChooser(share, "Share post with friends"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void dismissView(View view) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.flag_view);
        linearLayout.setVisibility(View.GONE);
        linearLayout = (LinearLayout) findViewById(R.id.delete_view);
        linearLayout.setVisibility(View.GONE);
    }

    public void flagContent(View view) {
        String type = (String) view.getTag();
        TextView typeId = (TextView) findViewById(R.id.type_id);
        TextView typeView = (TextView) findViewById(R.id.type);
        LinearLayout flagView = (LinearLayout) findViewById(R.id.flag_view);

        if (typeView.getText().equals("post")) {
            //Flag a post
            String url = substituteString(getResources().getString(R.string.flag_skoot), new HashMap<String, String>());

            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", Integer.toString(userId));
            params.put("post_id", typeId.getText().toString());
            params.put("type_id", type);

            SkooterJsonObjectRequest jsonObjectRequest = new SkooterJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(LOG_TAG, response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                }
            });

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_skoot");
        } else if (typeView.getText().equals("comment")) {
            //Flag a comment
            String url = substituteString(getResources().getString(R.string.flag_comment), new HashMap<String, String>());

            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", Integer.toString(userId));
            params.put("comment_id", typeId.getText().toString());
            params.put("type_id", type);

            SkooterJsonObjectRequest jsonObjectRequest = new SkooterJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(LOG_TAG, response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                }
            });

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_comment");
        }

        flagView.setVisibility(View.GONE);
    }

    public void acceptDelete(View view) {
        final TextView typeId = (TextView) findViewById(R.id.type_id);
        TextView typeView = (TextView) findViewById(R.id.type);
        LinearLayout deleteView = (LinearLayout) findViewById(R.id.delete_view);

        if (typeView.getText().equals("post")) {
            //Delete a post
            Map<String, String> params = new HashMap<String, String>();
            params.put("skoot_id", typeId.getText().toString());
            String url = substituteString(getResources().getString(R.string.skoot_delete), params);

            SkooterJsonObjectRequest jsonObjectRequest = new SkooterJsonObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(LOG_TAG, "Delete Post" + response.toString());
                    int position = Post.findPostPositionInListById(mHomePosts, Integer.parseInt(typeId.getText().toString()));

                    if (position >= 0) {
                        mHomePosts.remove(position);
                    }
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Delete Post Error: " + error.getMessage());
                }
            });

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_content");
        } else if (typeView.getText().equals("comment")) {
            //Delete a comment
            Map<String, String> params = new HashMap<String, String>();
            params.put("comment_id", typeId.getText().toString());
            String url = substituteString(getResources().getString(R.string.comment_delete), params);

            SkooterJsonObjectRequest jsonObjectRequest = new SkooterJsonObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(LOG_TAG, "Delete Comment" + response.toString());
                    List newList = postList.subList(1, postList.size());
                    Log.d(LOG_TAG, newList.toString());
                    List<Comment> commentList = (List<Comment>) newList;
                    int position = Comment.findCommentPositionInListById(commentList, Integer.parseInt(typeId.getText().toString()));

                    if (position >= 0) {
                        Post post = (Post) postList.get(0);
                        post.setCommentsCount(post.getCommentsCount() - 1);

                        mHomePosts.get(Post.findPostPositionInListById(mHomePosts, post.getId())).setCommentsCount(post.getCommentsCount());
                        postList.remove(position + 1);
                    }
                    postAdapter.notifyDataSetChanged();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Delete Comment Error: " + error.getMessage());
                }
            });

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_content");
        }
        deleteView.setVisibility(View.GONE);
    }
}
package com.tomtomkenya.africanludo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.tomtomkenya.africanludo.R;
import com.tomtomkenya.africanludo.adapter.MessageAdapter;
import com.tomtomkenya.africanludo.helper.AppConstant;
import com.tomtomkenya.africanludo.helper.Preferences;
import com.tomtomkenya.africanludo.model.Messages;
import com.tomtomkenya.africanludo.model.MyResponse;
import com.tomtomkenya.africanludo.model.Notification;
import com.tomtomkenya.africanludo.model.Sender;
import com.tomtomkenya.africanludo.remote.APIService;
import com.tomtomkenya.africanludo.utils.GetTimeAgo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    public Toolbar mChatToolbar;
    private DatabaseReference mRootRef, mUserRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircularImageView mProfileImage;

    private String mChatUser;
    private String mCurrentUserId;
    private String mMatchId;
    private String mRoomId;

    public ImageButton mChatAddBtn;
    public ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private static final int GALLERY_PICK = 1;

    private StorageReference mImageStorage;
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    public APIService mService;
    private String token, online;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mCurrentUserId = Preferences.getInstance(this).getString(Preferences.KEY_USER_ID);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);

        Bundle extras = getIntent() != null ? getIntent().getExtras() : null;
        mChatUser = extras != null ? extras.getString("user_id", "") : "";
        String userName = extras != null ? extras.getString("user_name", getString(R.string.app_name)) : getString(R.string.app_name);
        mMatchId = extras != null ? extras.getString("match_id", "") : "";
        mRoomId = buildRoomId(mMatchId, mCurrentUserId, mChatUser);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionBarView);

        // ---- Custom Action bar Items ----
        mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeenView = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);

        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList, this);

        mMessagesList = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);

        loadMessages();

        try {
            mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    try {
                        String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                        String image = Objects.requireNonNull(dataSnapshot.child("thumb_image").getValue()).toString();

                        mTitleView.setText(name);
                        Glide.with(ChatActivity.this).load(AppConstant.API_URL+image)
                                .apply(new RequestOptions().override(120,120))
                                .apply(new RequestOptions().placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .apply(RequestOptions.skipMemoryCacheOf(true))
                                .into(mProfileImage);
                    }catch (NullPointerException e){
                        mTitleView.setText("Guest User");
                        Glide.with(ChatActivity.this).load(AppConstant.API_URL+"default_avatar.jpg")
                                .apply(new RequestOptions().override(120,120))
                                .apply(new RequestOptions().placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .apply(RequestOptions.skipMemoryCacheOf(true))
                                .into(mProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                    mTitleView.setText(userName);
                    Glide.with(ChatActivity.this).load(AppConstant.API_URL+Preferences.getInstance(ChatActivity.this).getString(Preferences.KEY_PROFILE_PHOTO))
                            .apply(new RequestOptions().override(120,120))
                            .apply(new RequestOptions().placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .into(mProfileImage);
                }
            });
        }catch (NullPointerException e) {
            mTitleView.setText("User");
            Glide.with(ChatActivity.this).load(AppConstant.API_URL+"default_avatar.png")
                    .apply(new RequestOptions().override(120,120))
                    .apply(new RequestOptions().placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .into(mProfileImage);
        }

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                online = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();
                token = Objects.requireNonNull(dataSnapshot.child("device_token").getValue()).toString();

                if (TextUtils.equals(online, "true")) {
                    mLastSeenView.setText("online");
                } else {
                    long lastTime = 0;
                    if (TextUtils.isDigitsOnly(online)) {
                        try {
                            lastTime = Long.parseLong(online);
                        } catch (NumberFormatException ignored) {
                            lastTime = 0;
                        }
                    }
                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    try {
                        if (Objects.requireNonNull(lastSeenTime).equals("just now")) {
                            mLastSeenView.setText("online");
                        }
                        else {
                            mLastSeenView.setText(lastSeenTime);
                        }
                    }catch (Exception e) {
                        mLastSeenView.setText("online");
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, (databaseError, databaseReference) -> {
                        if(databaseError != null){
                            Log.d("CHAT_LOG", databaseError.getMessage());
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });


        mChatSendBtn.setOnClickListener(view -> sendMessage());

        mChatAddBtn.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
        });

        mRefreshLayout.setOnRefreshListener(() -> {
            mCurrentPage++;
            itemPos = 0;
            loadMoreMessages();
        });

    }

    private String buildRoomId(String matchId, String userA, String userB) {
        if (TextUtils.isEmpty(matchId) || TextUtils.isEmpty(userA) || TextUtils.isEmpty(userB)) {
            return matchId + "_" + userA + "_" + userB;
        }
        long first = safeParseLong(userA);
        long second = safeParseLong(userB);
        if (first == Long.MIN_VALUE || second == Long.MIN_VALUE) {
            String lower = userA.compareTo(userB) <= 0 ? userA : userB;
            String higher = userA.compareTo(userB) <= 0 ? userB : userA;
            return matchId + "_" + lower + "_" + higher;
        }
        long min = Math.min(first, second);
        long max = Math.max(first, second);
        return matchId + "_" + min + "_" + max;
    }

    private long safeParseLong(String value) {
        if (TextUtils.isDigitsOnly(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                return Long.MIN_VALUE;
            }
        }
        return Long.MIN_VALUE;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            if (data == null || data.getData() == null) {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                return;
            }
            Uri imageUri = data.getData();

            final String currentUserRef = "messages/" + mRoomId + "/" + mCurrentUserId;
            final String chatUserRef = "messages/" + mRoomId + "/" + mChatUser;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mRoomId).child(mCurrentUserId).push();
            final String push_id = user_message_push != null ? user_message_push.getKey() : null;
            if (push_id == null) {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                return;
            }

            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(task -> {

                if(task.isSuccessful()){
                    filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                        String download_url = uri.toString();

                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map<String, Object> messageUserMap = new HashMap<>();
                        messageUserMap.put(currentUserRef + "/" + push_id, messageMap);
                        messageUserMap.put(chatUserRef + "/" + push_id, messageMap);

                        mChatMessageView.setText("");
                        mRootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

                            if(databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage());
                            }
                        });

                    }).addOnFailureListener(e -> Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                }

            }).addOnFailureListener(e -> Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show());

        }

    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mRoomId).child(mCurrentUserId);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NotNull DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);

                } else {
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1) {
                    mLastKey = messageKey;

                }

                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NotNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(@NotNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NotNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {
        try {
            DatabaseReference messageRef = mRootRef.child("messages").child(mRoomId).child(mCurrentUserId);
            Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

            messageQuery.addChildEventListener(new ChildEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onChildAdded(@NotNull DataSnapshot dataSnapshot, String s) {
                    Messages message = dataSnapshot.getValue(Messages.class);

                    itemPos++;
                    if(itemPos == 1){
                        String messageKey = dataSnapshot.getKey();
                        mLastKey = messageKey;
                        mPrevKey = messageKey;
                    }

                    messagesList.add(message);
                    mAdapter.notifyDataSetChanged();
                    mMessagesList.scrollToPosition(messagesList.size() - 1);
                    mRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onChildChanged(@NotNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(@NotNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NotNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {

                }
            });
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message) && !TextUtils.isEmpty(mRoomId) && !TextUtils.isEmpty(mChatUser)){

            String current_user_ref = "messages/" + mRoomId + "/" + mCurrentUserId;
            String chat_user_ref = "messages/" + mRoomId + "/" + mChatUser;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mRoomId).child(mCurrentUserId).push();
            if (user_message_push == null) {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                return;
            }
            String push_id = user_message_push.getKey();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {
                if(databaseError != null){
                    Log.d("CHAT_LOG", databaseError.getMessage());
                }
                else {
                    sendNotification(Preferences.getInstance(ChatActivity.this).getString(Preferences.KEY_FULL_NAME), message);
                }

            });

        }
    }

    private void sendNotification(String name, String message) {
        mService = AppConstant.getFCMService();

        Map<String,String> map = new HashMap<>();
        map.put("title",name);
        map.put("message",message);
        map.put("click_action","MainActivity");

        //Create raw payload  to send
        Notification notification = new Notification(name,message,"MainActivity");
        Sender content =  new Sender(token,notification);

        mService.sendNotification(content).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {

            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                Log.e("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mUserRef.child("online").setValue("true");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostResume() {
        super.onPostResume();
        mLastSeenView.setText("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

}

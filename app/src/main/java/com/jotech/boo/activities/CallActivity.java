package com.jotech.boo.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.jotech.boo.R;
import com.jotech.boo.adapters.ChatAdapter;
import com.jotech.boo.models.ChatModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class CallActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int PERMISSION_REQUEST = 2;

    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int FPS = 30;

    String TAG="AppRTC: ";

    SurfaceTextureHelper surfaceTextureHelper;
    List<ChatModel> chatList;
    ChatAdapter chatAdapter;
    SurfaceViewRenderer surfaceView;
    SurfaceViewRenderer surfaceView2;
    EglBase rootEglBase;
    PeerConnectionFactory factory;
    FirebaseDatabase database;
    ValueEventListener listener1;
    FirebaseUser user;
    DatabaseReference rootRef;
    Query callerRef;
    Query remoteRef;
    String currentUser = "";
    String answerer = "";
    EditText typeText;
    Button sendText;
    RecyclerView msgs;
    ImageButton chatBtn;
    RelativeLayout msgsRl, typingRl;
    private ImageButton switchCam;

    AudioManager audioManager;
    PeerConnection peerConnection;
    boolean doubleBackToExitPressedOnce = false;
    MediaConstraints audioConstraints;
    VideoTrack videoTrackFromCamera;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    VideoCapturer videoCapturer;
    List<String> STUNList= Arrays.asList(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302",
            "stun:stun.vodafone.ro:3478",
            "stun:stun.samsungsmartcam.com:3478",
            "stun:stun.services.mozilla.com:3478"
    );

    DatabaseReference SendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);



        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUser = user.getUid();
        }

        surfaceView=findViewById(R.id.surface_view);
        surfaceView2=findViewById(R.id.surface_view2);
        typeText=findViewById(R.id.typeText);
        sendText=findViewById(R.id.sendText);
        msgs=findViewById(R.id.msgs);
        chatBtn = findViewById(R.id.chat);
        msgsRl = findViewById(R.id.msgsRl);
        typingRl = findViewById(R.id.typingRl);
        switchCam = findViewById(R.id.switch_cam);

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (typingRl.getVisibility() == View.GONE && msgsRl.getVisibility() == View.GONE){
                    typingRl.setVisibility(View.VISIBLE);
                    msgsRl.setVisibility(View.VISIBLE);
                    typeText.requestFocus();
                }
                else if (typingRl.getVisibility() == View.VISIBLE && msgsRl.getVisibility() == View.VISIBLE){
                    typingRl.setVisibility(View.GONE);
                    msgsRl.setVisibility(View.GONE);
                    typeText.clearFocus();

                }
            }
        });


        callerRef = rootRef.child("Users").child(currentUser);
        remoteRef = rootRef.child("WebRTC").child(currentUser);

        audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(!audioManager.isBluetoothScoOn()){
            audioManager.startBluetoothSco();
        }


        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    answerer = snapshot.child("incoming").getValue(String.class);
                    doCall();

                    rootRef.child("WebRTC").child(answerer).child("answerer").setValue(answerer);
                    rootRef.child("WebRTC").child(answerer).child("caller").setValue(currentUser);
                }else {
                    doAnswer();
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        };

        switchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
                cameraVideoCapturer.switchCamera(null);            }
        });

        callerRef.addValueEventListener(listener1);

        requestPermissions();

        initializeSurfaceViews();
        initializePeerConnectionFactory();
        createVideoTrackFromCameraAndShowIt();
        initializePeerConnections();
        startStreamingVideo();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        msgs.setHasFixedSize(false);
        msgs.setLayoutManager(linearLayoutManager);

        typeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    sendMsg();
                }
                return false;
            }
        });

        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });

        readMsgs();
    }

    private void sendMsg() {

        String typing = typeText.getText().toString();
        if (typing.length() == 0){
            Toast.makeText(CallActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            rootRef.child("Users").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        answerer = snapshot.child("incoming").getValue(String.class);
                        sendMessage(currentUser, answerer, typing);

                    }
                    else{
                        rootRef.child("WebRTC").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    answerer = snapshot.child("caller").getValue(String.class);

                                    sendMessage(currentUser, answerer, typing);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                }
            });
        }

        if (typingRl.getVisibility() == View.VISIBLE && msgsRl.getVisibility() == View.VISIBLE){

            hideChat(typingRl);
            hideChat(msgsRl);
        }
    }

    private void hideChat(RelativeLayout chatRl) {
        typingRl.postDelayed(new Runnable() {
            public void run() {
                typeText.clearFocus();
                chatRl.setVisibility(View.GONE);
                if (chatRl.getVisibility() == View.GONE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            }
        }, 7000);
    }


    private void sendMessage(String sender, String receiver,String messageE){

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", messageE);

        rootRef.child("WebRTC").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentUser)){
                    rootRef.child("WebRTC").child(currentUser).child("Chats").push().setValue(hashMap);
                }
                else{
                    rootRef.child("Users").child(currentUser).child("incoming").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                answerer = snapshot.getValue(String.class);

                                rootRef.child("WebRTC").child(answerer).child("Chats").push().setValue(hashMap);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
        typeText.setText("");
    }

    private void readMsgs() {
        chatList = new ArrayList<>();
        rootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    answerer = snapshot.child("incoming").getValue(String.class);

                    rootRef.child("WebRTC").child(answerer).child("Chats").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                chatList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    ChatModel chat = dataSnapshot.getValue(ChatModel.class);
                                    if (chat!=null){
                                        if (chat.getReceiver().equals(currentUser) && chat.getSender().equals(answerer)
                                                || chat.getReceiver().equals(answerer) && chat.getSender().equals(currentUser)) {
                                            chatList.add(chat);
                                        }
                                    }
                                    chatAdapter = new ChatAdapter(CallActivity.this, chatList);
                                    msgs.setAdapter(chatAdapter);

                                }
                                rootRef.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                        }
                    });
                }

                else {
                    rootRef.child("WebRTC").child(currentUser).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                answerer = snapshot.child("caller").getValue(String.class);

                                rootRef.child("WebRTC").child(currentUser).child("Chats").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                        chatList.clear();
                                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                            ChatModel chat = dataSnapshot.getValue(ChatModel.class);

                                            if (chat!=null){
                                                if(chat.getReceiver().equals(currentUser) && chat.getSender().equals(answerer)
                                                        || chat.getReceiver().equals(answerer) && chat.getSender().equals(currentUser)){
                                                    chatList.add(chat);
                                                }
                                            }
                                            chatAdapter = new ChatAdapter(CallActivity.this, chatList);

                                            msgs.setAdapter(chatAdapter);
                                        }
                                        rootRef.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                        }
                    });
                }
                rootRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            super.onBackPressed();
            //intent

        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        ConnectionStatus("CLOSED");

        if (videoCapturer != null) {
            try {
                CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
                cameraVideoCapturer.stopCapture();
                cameraVideoCapturer.dispose();
                cameraVideoCapturer = null;
                createVideoCapturer().stopCapture();
                peerConnection.dispose();
                videoCapturer.stopCapture();
                videoCapturer.dispose();
                videoCapturer = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        surfaceTextureHelper = null;
        rootEglBase.detachCurrent();
        rootEglBase.release();
        rootEglBase = null;
        PeerConnectionFactory.shutdownInternalTracer();
        callerRef.removeEventListener(listener1);
        Disconnect();
        MediaStream mediaStream = factory.createLocalMediaStream("CLOSED");
        mediaStream.removeTrack(localAudioTrack);
        mediaStream.removeTrack(videoTrackFromCamera);
        if (videoTrackFromCamera != null) {
            videoTrackFromCamera.setEnabled(false);
            videoTrackFromCamera.dispose();
            videoTrackFromCamera.removeSink(surfaceView);
            videoTrackFromCamera = null;
        }
        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }
        if (localAudioTrack != null) {
            localAudioTrack.dispose();
            localAudioTrack = null;
        }

        if (surfaceView != null) {
            surfaceView.release();
            surfaceView = null;
        }
        if (surfaceView2 != null) {
            surfaceView2.release();
            surfaceView2 = null;
        }
        rootRef.child("Users").child(currentUser).removeValue();
        rootRef.child("WebRTC").child(currentUser).removeValue();
        remoteRef.getRef().setValue(null);

        finishActivity(CallActivity.CONTEXT_INCLUDE_CODE);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Dynamic permissions are not required before Android M.
            //   onPermissionsGranted();
            return;
        }
        methodRequiresTwoPermission();

        String[] missingPermissions = getMissingPermissions();
        if (missingPermissions.length != 0) {
            requestPermissions(missingPermissions, PERMISSION_REQUEST);
        } else {
            // onPermissionsGranted();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private String[] getMissingPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to retrieve permissions.");
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            Log.w(TAG, "No requested permissions.");
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }
        Log.d(TAG, "Missing permissions: " + missingPermissions);

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    @AfterPermissionGranted(PERMISSION_REQUEST)
    private void methodRequiresTwoPermission() {
        String[] perms = getMissingPermissions();
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Requires Permission",
                    PERMISSION_REQUEST, perms);
        }
    }


    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();
        surfaceView.init(rootEglBase.getEglBaseContext(), null);
        surfaceView.setEnableHardwareScaler(true);
        surfaceView.setMirror(true);

        surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        surfaceView2.setEnableHardwareScaler(true);
        surfaceView2.setMirror(true);

        //add one more
    }
    private void initializePeerConnectionFactory() {
        VideoEncoderFactory encoderFactory;
        VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */,false);
        decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions());
        factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        //factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {

        videoCapturer = createVideoCapturer();
        //VideoSource videoSource=null;
        //Create a VideoSource instance
        VideoSource videoSource;
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        videoSource = factory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());


        VideoEncoderFactory videoEncoderFactory =
                new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext()
                        , true, true);
        for (int i = 0; i < videoEncoderFactory.getSupportedCodecs().length; i++) {
            Log.d(TAG, "Supported codecs: " + videoEncoderFactory.getSupportedCodecs()[i].name);
        }

        videoTrackFromCamera = factory.createVideoTrack("100", videoSource);

        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();

        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);

        if (videoCapturer != null) {
            //   videoCapturer.startCapture(1024, 720, 30);
            videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        }
        // And finally, with our VideoRenderer ready, we
        // can add our renderer to the VideoTrack.
        videoTrackFromCamera.setEnabled(true);

        videoTrackFromCamera.addSink(surfaceView);



    }

    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);

    }
    private void startStreamingVideo() {
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);

        //   sendMessage("got user media");
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }
    private VideoCapturer createVideoCapturer() {

        Logging.d(TAG, "Creating capturer using camera1 API.");
        //videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        Log.d(TAG, "createPeerConnection: ");
        //==
        // Add ICE Servers
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();

        // STUN 1
//        PeerConnection.IceServer.Builder iceServerBuilder = PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302");
        //      iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK); //this does the magic.
        //    PeerConnection.IceServer iceServer =  iceServerBuilder.createIceServer();

        for(String i:STUNList){
            PeerConnection.IceServer.Builder iceServerBuilder = PeerConnection.IceServer.builder(i);
            iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK); //this does the magic.
            PeerConnection.IceServer iceServer =  iceServerBuilder.createIceServer();
            iceServers.add(iceServer);
        }


        //==
        // ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        //iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        //MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                /*
                 * HAVE_LOCAL_OFFER
                 * HAVE_REMOTE_OFFER
                 */
                Log.d(TAG, "onSignalingChange: "+signalingState);

            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: "+iceConnectionState);
                ConnectionStatus(iceConnectionState.toString());
                /*
                 NEW,
    CHECKING,
    CONNECTED,
    COMPLETED,
    FAILED,
    DISCONNECTED,
    CLOSED;
    * */

            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: "+b);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: "+iceGatheringState);
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: "+iceCandidate);
                JSONObject message = new JSONObject();

                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    Log.d(TAG, "onIceCandidate: sending candidate " + message);
                    SendData2DB(message);
                    Log.d(TAG, "onIceCandidate: "+message.toString());
                } catch (JSONException e) {

                    Log.e(TAG, "onIceCandidate ERROR: "+e );
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: "+iceCandidates);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(true);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addSink(surfaceView2);

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }

            @Override
            public void onStandardizedIceConnectionChange(PeerConnection.IceConnectionState newState) {
                Log.d(TAG, "onStandardizedIceConnectionChange: "+newState.toString());

            }
        };

        //return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
        return factory.createPeerConnection(rtcConfig, pcObserver);
    }

    public void ConnectionStatus(String s){
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if(s.equals("CONNECTED")){
                        Toast.makeText(CallActivity.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.e(TAG, "ConnectionStatus: "+e );
                }
            }
        });


    }
    public void doCall() {
        Log.d(TAG, "doCall: ");

        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    SendData2DB(message);
                    Log.d(TAG, "onCreateSuccess: "+message.toString());
                    CandidateDBListner();
                } catch (JSONException e) {

                    Log.e(TAG, "onCreateSuccess ERROR: "+e );
                }
            }
        }, sdpMediaConstraints);

        AnswerDBListner();
    }


    public void doAnswer() {
        OfferDBListner();

    }


    public void doAnswerRun() {
        Log.d(TAG, "doAnswer: ");

        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    SendData2DB(message);
                    Log.d(TAG, "onCreateSuccess: "+message.toString());
                    CandidateDBListner();
                } catch (JSONException e) {
                    Log.e(TAG, "onCreateSuccess ERROR: "+e );
                }
            }
        },  new MediaConstraints());// new MediaConstraints()
    }

    void SendData2DB(JSONObject message){
        // Create a new user with a first and last name
        String type="unknown";

        Map<String, Object> data = new HashMap<>();
        try {
            type=message.get("type").toString();
            data.put(type, message.toString());
            if(type.equals("offer")){
            }
            if(type.equals("answer")){
            }
            if(type.equals("candidate")){
            }
            Log.d(TAG, "SendData2DB: "+type);

            if(type.equals("candidate")){
                remoteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            SendData = database.getReference("WebRTC/"+currentUser+"/candidate");
                            SendData.push().setValue(message.toString());
                        }
                        else {
                            rootRef.child("Users").child(currentUser).child("incoming").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                    answerer = snapshot.getValue(String.class);

                                    SendData = database.getReference("WebRTC/"+answerer+"/candidate");
                                    SendData.push().setValue(message.toString());
                                }

                                @Override
                                public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                    }
                });
            }else{

                remoteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            SendData = database.getReference("WebRTC/"+currentUser);
                            SendData.updateChildren(data);
                        }
                        else{
                            rootRef.child("Users").child(currentUser).child("incoming").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                    answerer = snapshot.getValue(String.class);

                                    SendData = database.getReference("WebRTC/"+answerer);
                                    SendData.updateChildren(data);
                                }

                                @Override
                                public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                    }
                });
            }

            //  SendData.setValue(message.toString());



        }catch (Exception e){
            Log.e(TAG, "SendData2DB: " + e);
        }


    }

    void OfferDBListner(){
        DatabaseReference OfferDB = database.getReference("WebRTC/"+currentUser+"/offer");
        OfferDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class)!=null){
                    String value = dataSnapshot.getValue(String.class);
                    try {
                        JSONObject message = new JSONObject(value);
                        Log.d(TAG, "OFFER JSON: " + message.toString());

                        peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
                        doAnswerRun();
                    }catch (JSONException err){
                        Log.d("Error", err.toString());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    void CandidateDBListner(){

        remoteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    DatabaseReference CandidateDB = database.getReference("WebRTC/"+currentUser+"/candidate");
                    CandidateDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    if(ds.getValue(String.class)!=null){
                                        String value = ds.getValue(String.class);
                                        try {
                                            JSONObject message = new JSONObject(value);
                                            Log.d(TAG, "CandidateDBListner(); " + message);

                                            IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                                            peerConnection.addIceCandidate(candidate);
                                        }catch (JSONException err){
                                            Log.d("Error", err.toString());
                                        }
                                    }
                                }
                            }
                            else{
                                rootRef.child("Users").child(currentUser).child("incoming").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                        answerer = snapshot.getValue(String.class);

                                        DatabaseReference CandidateDB = database.getReference("WebRTC/"+answerer+"/candidate");
                                        CandidateDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                                                        if(ds.getValue(String.class)!=null){
                                                            String value = ds.getValue(String.class);
                                                            try {
                                                                JSONObject message = new JSONObject(value);
                                                                Log.d(TAG, "CandidateDBListner(); " + message);

                                                                IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                                                                peerConnection.addIceCandidate(candidate);
                                                            }catch (JSONException err){
                                                                Log.d("Error", err.toString());
                                                            }
                                                        }
                                                    }
                                                }


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error) {
                                                Log.w(TAG, "Failed to read value.", error.toException());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

    }
    void AnswerDBListner(){
        DatabaseReference answerdbl = database.getReference("Users/"+currentUser+"/incoming");

        answerdbl.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                answerer = snapshot.getValue(String.class);


                DatabaseReference AnswerDB = database.getReference("WebRTC/"+answerer+"/answer");
                AnswerDB.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(String.class)!=null){
                            String value = dataSnapshot.getValue(String.class);
                            Log.d(TAG, "Value is: " + value);
                            try {
                                JSONObject message = new JSONObject(value);
                                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));

                            }catch (JSONException err){
                                Log.d("Error", err.toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    void Disconnect(){
        peerConnection.close();
    }
}
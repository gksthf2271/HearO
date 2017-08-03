package com.example.junseo.test03;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.junseo.test03.arduino.BluetoothPairActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{
    private FirebaseAuth firebaseAuth;
    private Button buttonLogout;

    private BluetoothAdapter bluetooth_;
    private BluetoothPairActivity btService = null;

    Vibrator mVibe; //진동
    Button blinking_animation = null; // 화재 애니메이션
    Button start = null; // 화재 애니메이션 시작
    Button blinking_animation2 = null; // 노크 애니메이션
    Button start2 = null; // 노크 애니메이션 시작
    Button blinking_animation3=null; // 음성 애니메이션
    Button start3 = null; // 음성 애니메이션 시작
    Button blinking_animation4=null; // 초인종 애니메이션
    Button start4 = null; // 초인종 애니메이션
    TextView firetext = null; // 불났어요 출력
    TextView doortext = null; // 노크 출력
    TextView voicetext = null; // 음성 출력
    TextView belltext = null; // 초인종 애니메이션
    LinearLayout back;
    boolean flag = FALSE;   //진동 울리면 TRUE 평소에 FALSE

    int[] img = {R.drawable.back1, R.drawable.back2, R.drawable.back3, R.drawable.back4, R.drawable.back5, R.drawable.back6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //배경 랜덤 설정
        back = (LinearLayout)findViewById(R.id.layout);
        Random ram = new Random();
        int num = ram.nextInt(img.length);
        back.setBackgroundResource(img[num]);
        blinking_animation = (Button) findViewById(R.id.blinking_animation);
        start = (Button) findViewById(R.id.start);
        firetext = (TextView) findViewById(R.id.firetext);
        mVibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        blinking_animation2 = (Button) findViewById(R.id.blinking_animation2);
        start2 = (Button) findViewById(R.id.start2);
        doortext = (TextView) findViewById(R.id.doortext);

        blinking_animation3 = (Button) findViewById(R.id.blinking_animation3);
        start3 = (Button) findViewById(R.id.start3);
        voicetext = (TextView) findViewById(R.id.voice);

        blinking_animation4 = (Button) findViewById(R.id.blinking_animation4);
        start4 = (Button) findViewById(R.id.start4);
        belltext = (TextView) findViewById(R.id.bell);



        blinking_animation.setOnClickListener(this);
        start.setOnClickListener(this);
        blinking_animation2.setOnClickListener(this);
        start2.setOnClickListener(this);
        blinking_animation3.setOnClickListener(this);
        start3.setOnClickListener(this);
        blinking_animation4.setOnClickListener(this);
        start4.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final ToggleButton tb = (ToggleButton)findViewById(R.id.HearMainbutton);
        final Button button1 = (Button)findViewById(R.id.button1);
        //텍스트 입력 버튼/
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent ttsIntent = new Intent(MenuActivity.this, TTSActivity.class);
                MenuActivity.this.startActivity(ttsIntent);
            }
        });
        final Button button2 = (Button)findViewById(R.id.button2);
        //상용구 버튼
        button2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent macroIntent = new Intent(MenuActivity.this, MacroActivity.class);
                MenuActivity.this.startActivity(macroIntent);
            }
        });
        final Button button3 = (Button)findViewById(R.id.button3);
        //STT 버튼
        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent sttIntent = new Intent(MenuActivity.this, STTActivity.class);
                MenuActivity.this.startActivity(sttIntent);
            }
        });

        //  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
      /*  fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        tb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (tb.isChecked()) {
                    tb.setBackgroundDrawable(getResources().getDrawable(R.drawable.hearobutton));
                }else
                {
                    tb.setBackgroundDrawable(getResources().getDrawable(R.drawable.hearobutton));
                }
            }
        });
        //메인 버튼 이미지 토글.
        if(tb.isChecked()){
            tb.setBackgroundDrawable(getResources().getDrawable(R.drawable.hearobuttonon));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //buttonLogout = (Button) findViewById(R.id.b+uttonLogout);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));

        }
        FirebaseUser user = firebaseAuth.getCurrentUser();



    }

    public void Vibrator_pattern(){

        long[] vibratePattern = {100, 100, 100};
        mVibe.vibrate(vibratePattern, 0);
        flag = TRUE;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        if(flag==TRUE)
        {
            mVibe.cancel();
            flag = FALSE;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUserLeaveHint() {  //홈버튼 이벤트
//여기서 감지
        Log.d("한솔", "Home Button Touch");
        if(flag==TRUE)
        {
            mVibe.cancel();
            flag = FALSE;
        }
        super.onUserLeaveHint();
    }


    public void NotificationFire() {


        Resources res = getResources();


        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);// 루트 액티비티 하나만 뜨게 플래그 잡아줌

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("HearO")
                .setContentText("불꽃남자")
                .setTicker("화재")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }
    public void NotificationDoor(){
        Resources res = getResources();
        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP); // 루트 액티비티 하나만 뜨게 플래그 잡아줌
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("HearO")
                .setContentText("똑똑!")
                .setTicker("노크")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }
    public void Notificationbell(){
        Resources res = getResources();
        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP); // 루트 액티비티 하나만 뜨게 플래그 잡아줌
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("HearO")
                .setContentText("띠잉동!")
                .setTicker("초인종")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_myinfo) {
            startActivity(new Intent(this, ProfileActivity.class)); //내 정보
        } else if (id == R.id.nav_version) {
            startActivity(new Intent(this, VersionActivity.class)); //앱 버전
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this,HelpActivity.class)); //도움말
        } else if (id == R.id.nav_module) {
            startActivity(new Intent(this, BluetoothPairActivity.class)); //모듈 연결/해제
        } else if (id == R.id.nav_alert) {
            startActivity(new Intent(this,AlarmActivity.class)); //알림 설정
        } else if (id == R.id.Logout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class)); //로그아웃
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.start:
                NotificationFire();
                blinking_animation4.setVisibility(View.INVISIBLE);
                blinking_animation3.setVisibility(View.INVISIBLE);
                blinking_animation2.setVisibility(View.INVISIBLE);
                doortext.setVisibility(View.GONE);
                voicetext.setVisibility(View.GONE);
                belltext.setVisibility(View.GONE);
                Vibrator_pattern();
                Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinking_animation);
                blinking_animation.startAnimation(startAnimation);
                firetext.setVisibility(View.VISIBLE);
                break;

            case R.id.blinking_animation:
                blinking_animation.clearAnimation();
                firetext.setVisibility(View.GONE);
                mVibe.cancel();
                break;

            case R.id.start2:
                NotificationDoor();
                mVibe.cancel();
                blinking_animation.clearAnimation();
                blinking_animation3.setVisibility(View.INVISIBLE);
                blinking_animation4.setVisibility(View.INVISIBLE);
                firetext.setVisibility(View.GONE);
                voicetext.setVisibility(View.GONE);
                belltext.setVisibility(View.GONE);

                blinking_animation2.setVisibility(View.VISIBLE);
                doortext.setVisibility(View.VISIBLE);
                break;

            case R.id.blinking_animation2:
                blinking_animation2.setVisibility(View.INVISIBLE);
                doortext.setVisibility(View.GONE);
                break;

            case R.id.start3:

                mVibe.cancel();
                blinking_animation.clearAnimation();
                firetext.setVisibility(View.GONE);
                blinking_animation2.setVisibility(View.GONE);
                doortext.setVisibility(View.GONE);
                blinking_animation4.setVisibility(View.INVISIBLE);
                belltext.setVisibility(View.GONE);

                blinking_animation3.setVisibility(View.VISIBLE);
                voicetext.setVisibility(View.VISIBLE);
                break;

            case R.id.blinking_animation3:
                blinking_animation3.setVisibility(View.INVISIBLE);
                voicetext.setVisibility(View.GONE);
                break;

            case R.id.start4:
                Notificationbell();
                mVibe.cancel();
                blinking_animation.clearAnimation();
                firetext.setVisibility(View.GONE);
                blinking_animation2.setVisibility(View.GONE);
                doortext.setVisibility(View.GONE);
                blinking_animation3.setVisibility(View.INVISIBLE);
                voicetext.setVisibility(View.GONE);

                blinking_animation4.setVisibility(View.VISIBLE);
                belltext.setVisibility(View.VISIBLE);
                break;
            case R.id.blinking_animation4:
                blinking_animation4.setVisibility(View.INVISIBLE);
                belltext.setVisibility(View.GONE);

                break;

        }

    }
    @Override
    public void onStart(){
        super.onStart();
        //블루투스 MainActivity 초기에 실행하기
        bluetooth_ = BluetoothAdapter.getDefaultAdapter();
        if (!bluetooth_.isEnabled()) {
            Toast.makeText(getApplicationContext(), "bluetooth is not enabled",
                    Toast.LENGTH_LONG).show();
            Intent enableintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableintent, 0);
            if(!bluetooth_.enable()) {
                Intent intent = new Intent(getApplicationContext(), BluetoothPairActivity.class);
                startActivityForResult(intent, 1001);
            }
        }
        else if(!bluetooth_.isDiscovering()){
            Intent intent = new Intent(getApplicationContext(), BluetoothPairActivity.class);
            startActivityForResult(intent, 1);
        }
    }
}

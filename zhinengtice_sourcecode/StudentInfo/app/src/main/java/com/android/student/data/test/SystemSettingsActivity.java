package com.android.student.data.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.utils.StringUtils;

/**
 * 系统设置
 * Created by hailong on 2016/10/19 0019.
 */
public class SystemSettingsActivity extends Activity {
    EditText ic_settings_admin_et;
    View ic_admin_save;
    CheckBox voice_bobao_check;
    CheckBox voice_tip_check;
    SeekBar bright_seek;
    SeekBar volume_seek;
    EditText ic_settings_et_1;
    EditText ic_settings_et_2;
    EditText ic_settings_et_3;
    EditText ic_settings_et_4;
    EditText ic_settings_et_5;
    EditText ic_settings_et_6;

    View ic_pin_save;

    AudioManager mAudioManager;
    boolean bobao = true;
    boolean tip = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_settings);
        initView();
        initData();
    }

    protected void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(SystemSettingsActivity.this, msg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

    }

    private void initView() {
        ((TextView) findViewById(R.id.title)).setText("系统设置");
        ic_settings_admin_et = (EditText) findViewById(R.id.ic_settings_admin_et);
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        String pinCode = sharedPreferences.getString("adminCode", "123456");
        ic_settings_admin_et.setText(pinCode);
        ic_admin_save = findViewById(R.id.ic_admin_save);
        ic_admin_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminCode = ic_settings_admin_et.getText().toString();
                if (StringUtils.isEmpty(adminCode) || adminCode.length() < 6) {
                    toast("密码格式错误");
                    return;
                }
                SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("adminCodeSet", true);
                editor.putString("adminCode", adminCode);
                editor.commit();
                toast("设置管理员密码成功");
            }
        });
        voice_bobao_check = (CheckBox) findViewById(R.id.voice_bobao_check);
        voice_tip_check = (CheckBox) findViewById(R.id.voice_tip_check);
        bright_seek = (SeekBar) findViewById(R.id.bright_seek);
        volume_seek = (SeekBar) findViewById(R.id.volume_seek);
        ic_settings_et_1 = (EditText) findViewById(R.id.ic_settings_et_1);
        ic_settings_et_2 = (EditText) findViewById(R.id.ic_settings_et_2);
        ic_settings_et_3 = (EditText) findViewById(R.id.ic_settings_et_3);
        ic_settings_et_4 = (EditText) findViewById(R.id.ic_settings_et_4);
        ic_settings_et_5 = (EditText) findViewById(R.id.ic_settings_et_5);
        ic_settings_et_6 = (EditText) findViewById(R.id.ic_settings_et_6);

        ic_settings_et_1.setText(sharedPreferences.getString("ic_pin_1", "FF"));
        ic_settings_et_2.setText(sharedPreferences.getString("ic_pin_2", "FF"));
        ic_settings_et_3.setText(sharedPreferences.getString("ic_pin_3", "FF"));
        ic_settings_et_4.setText(sharedPreferences.getString("ic_pin_4", "FF"));
        ic_settings_et_5.setText(sharedPreferences.getString("ic_pin_5", "FF"));
        ic_settings_et_6.setText(sharedPreferences.getString("ic_pin_6", "FF"));

        ic_pin_save = findViewById(R.id.ic_pin_save);
        ic_pin_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin1 = ic_settings_et_1.getText().toString();
                String pin2 = ic_settings_et_2.getText().toString();
                String pin3 = ic_settings_et_3.getText().toString();
                String pin4 = ic_settings_et_4.getText().toString();
                String pin5 = ic_settings_et_5.getText().toString();
                String pin6 = ic_settings_et_6.getText().toString();
                if (pin1.length() < 2 ||
                        pin2.length() < 2 ||
                        pin3.length() < 2 ||
                        pin4.length() < 2 ||
                        pin5.length() < 2 ||
                        pin6.length() < 2) {
                    Toast.makeText(SystemSettingsActivity.this, "完善密码", Toast.LENGTH_LONG).show();
                    return;
                }
                //保存下载密码
                SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("ic_pin_1", pin1);
                editor.putString("ic_pin_2", pin2);
                editor.putString("ic_pin_3", pin3);
                editor.putString("ic_pin_4", pin4);
                editor.putString("ic_pin_5", pin5);
                editor.putString("ic_pin_6", pin6);
                editor.commit();
                Toast.makeText(SystemSettingsActivity.this, "保存IC卡下载密码成功", Toast.LENGTH_LONG).show();
            }
        });
        View back_container = findViewById(R.id.back_container);
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        bobao = sharedPreferences.getBoolean("bobao", bobao);
        voice_bobao_check.setChecked(bobao);
        tip = sharedPreferences.getBoolean("tip", tip);
        voice_tip_check.setChecked(tip);
        voice_bobao_check.setOnCheckedChangeListener(checkedChangeListener);
        voice_tip_check.setOnCheckedChangeListener(checkedChangeListener);
        // 进度条绑定最大亮度，255是最大亮度
        bright_seek.setMax(255);
        // 取得当前亮度
        int normal = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 255);
        // 进度条绑定当前亮度
        bright_seek.setProgress(255 - normal);
        bright_seek.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume_seek.setMax(max);
        volume_seek.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        volume_seek.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (buttonView.getId() == R.id.voice_bobao_check) {
                editor.putBoolean("bobao", isChecked);
                editor.commit();
            } else if (buttonView.getId() == R.id.voice_tip_check) {
                editor.putBoolean("tip", isChecked);
                editor.commit();
            }
        }
    };
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == R.id.bright_seek) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 255 - progress);
            } else if (seekBar.getId() == R.id.volume_seek) {
                if (mAudioManager != null) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);
                    SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("volume", progress);
                    editor.commit();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}

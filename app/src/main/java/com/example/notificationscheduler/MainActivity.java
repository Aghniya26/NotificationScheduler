package com.example.notificationscheduler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.SelectableChannel;

public class MainActivity extends AppCompatActivity {
    private JobScheduler mScheduler;
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;
    private SeekBar mSeekBar;
    private static final int JOB_ID=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceChargingSwitch=findViewById(R.id.chargingSwitch);
        mDeviceIdleSwitch=findViewById(R.id.idleSwitch);
        mSeekBar=findViewById(R.id.seekBar);

        final TextView seekBarProgress=findViewById(R.id.seekBarProgress);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i>0){
                    seekBarProgress.setText(i+ " s");
                }else {
                    seekBarProgress.setText(" Not Set");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void scheduleJob(View view){
        mScheduler=(JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        RadioGroup networkOptions= findViewById(R.id.networkOptions);
        int selectedNetworkID=networkOptions.getCheckedRadioButtonId();
        int seekBarInteger= mSeekBar.getProgress();
        boolean seekBarSet=seekBarInteger>0;

        int selectedNetworkOptions= JobInfo.NETWORK_TYPE_NONE;
        switch (selectedNetworkID){
            case R.id.noNetwork:
                selectedNetworkOptions=JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                selectedNetworkOptions=JobInfo.NETWORK_TYPE_ANY;
                break;
            case  R.id.wifiNetwork:
                selectedNetworkOptions=JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        ComponentName serviceName=new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder=new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOptions)
                .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked())
                .setRequiresCharging(mDeviceChargingSwitch.isChecked());


        if(seekBarSet){
            builder.setOverrideDeadline(seekBarInteger*1000);
        }



        boolean constrainSet=( selectedNetworkOptions!=JobInfo.NETWORK_TYPE_NONE) ||
                mDeviceIdleSwitch.isChecked() || mDeviceChargingSwitch.isChecked() || seekBarSet;
        if(constrainSet){
            JobInfo myJobInfo=builder.build();
            mScheduler.schedule(myJobInfo);
            Toast.makeText(this, "Job Scheduled, job will run when "+
                    "the constraints are met.", Toast.LENGTH_SHORT).show();
        }else {
           Toast.makeText(this, "Please set at least one constraint", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelJobs(View view){
        if (mScheduler!=null) {
            mScheduler.cancelAll();
            mScheduler=null;
            Toast.makeText(this, "Jobs Canceled", Toast.LENGTH_SHORT).show();
        }
    }

}
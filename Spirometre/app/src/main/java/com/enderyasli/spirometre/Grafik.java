package com.enderyasli.spirometre;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.enderyasli.spirometre.Graph.PointValue;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;

public class Grafik extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    GraphView graphView;
    LineGraphSeries series;

    private Handler mHandler = new Handler();
    int i =0;
    int x=0;
    int y=0;

    String[] datalar;

    Button btn_insert;
    String address = null;
    byte buffer[];
    TextView textView,textView2;



    boolean stopThread;
    boolean deviceConnected=false;

    private final String DEVICE_ADDRESS="98:D3:34:90:74:C9";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        database= FirebaseDatabase.getInstance();
        databaseReference =database.getReference("degerler");

        textView=findViewById(R.id.tv);
        btn_insert = findViewById(R.id.btn_insert);
        graphView = findViewById(R.id.graphView);

       //  series= new LineGraphSeries();
       // graphView.addSeries(series);

        Intent newint = getIntent();
        setUiEnabled(false);

/*
        btn_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  while (i!=11){
                   mrun.run();
            }
                mHandler.removeCallbacks(mrun);
    }

        });
*/

    }
    public void onClickStart(View view) {



        if(BTinit())
        {
            if(BTconnect())
            {
                setUiEnabled(true);
                deviceConnected=true;
                beginListenForData();
                Toast.makeText(getApplicationContext(),"Bağlantı başarılı",Toast.LENGTH_LONG).show();

            }

        }







    }

/*
    @Override
    protected void onStart() {
        super.onStart();


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
               DataPoint[] dataPoint =new DataPoint[(int)dataSnapshot.getChildrenCount()];

               int index=0;
               for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren()){

                  PointValue pointValue =  myDataSnapshot.getValue(PointValue.class);
                  dataPoint[index]= new DataPoint(pointValue.getxValue(),pointValue.getyValue());
                  index++;



               }
               series.resetData(dataPoint);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    */

/*
    private Runnable mrun = new Runnable() {
        @Override
        public void run() {

            String id = databaseReference.push().getKey();

            PointValue pointValue = new PointValue(x, y);
            x++;
            y++;
            i++;
            databaseReference.child(id).setValue(pointValue);

            mHandler.postDelayed(this,1000);

        }
    };
*/


    public void setUiEnabled(boolean bool)
    {
        btn_insert.setEnabled(!bool);

        textView.setEnabled(bool);

    }


    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Önce Spirometre'ye bağlanmalısınız!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
                                    textView.append(string);
                                    String data = textView.getText().toString();
                                    datalar = data.split("\n");

                                    if(datalar.length>4){

                                        LineGraphSeries<DataPoint> series = new LineGraphSeries<>
                                                (new DataPoint[] {
                                                        new DataPoint(0, Double.parseDouble(datalar[0])),
                                                        new DataPoint(1, Double.parseDouble(datalar[1])),
                                                        new DataPoint(2, Double.parseDouble(datalar[2])),
                                                        new DataPoint(3, Double.parseDouble(datalar[3])),
                                                        new DataPoint(4, Double.parseDouble(datalar[4])),

                                                });
                                        graphView.addSeries(series);
                                     //   series.resetData(datalar);
                                   }else{
                                    System.out.println("veri gelmedi");
                                }

                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        Toast.makeText(getApplicationContext(),"hata threadde",Toast.LENGTH_LONG).show();
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }
}

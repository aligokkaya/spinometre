package com.enderyasli.spirometre;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;

import java.util.Set;
import java.util.ArrayList;

import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class BluetoothListe extends AppCompatActivity {

    TextView yazi;
    ListView cihazListesi;
    Button cihazlarıGöster;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> eslesmisCihazlar;
    public static String EXTRA_ADDRESS = "98:D3:34:90:74:C9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_liste);

        yazi = (TextView)findViewById(R.id.textView);
        cihazListesi = (ListView)findViewById(R.id.listView);
        cihazlarıGöster = (Button)findViewById(R.id.button);

        //Telefondaki bluetoothumuzu alıyoruz
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        //Bluetoothumuzun olup olmadıgına baktık
        if(myBluetooth == null){
            Toast.makeText(getApplicationContext(),"Telefonunuz Bluetooth Desteklemiyor",Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            //Bluetooth var ama acık degılse acmak icin istekte bulun
            Intent BTac = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BTac, 1);
            //cihazlarıGoster Methoduyla eslesmis cihazlar gosterilir
        }

        cihazlarıGöster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eslesmisCihazlarıGoster();
            }
        });

    }

    private void eslesmisCihazlarıGoster(){
        //eslesmis cihazları al
        eslesmisCihazlar = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if(eslesmisCihazlar.size() > 0){
            for(BluetoothDevice bt: eslesmisCihazlar){
                //Cihazın adını ve adresini listeye ekle
                list.add(bt.getName()+"\n"+bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(),"Eslesmis Cihaz Bulunamadı",Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_activated_1,list);
        cihazListesi.setAdapter(adapter);
        //Listeden bir cihaz sectigimizce cagrılacak method
        cihazListesi.setOnItemClickListener(cihazSec);
    }


    //ListeView'a tıklayıp secmemizi saglayan method
    private AdapterView.OnItemClickListener cihazSec = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Mac Addresini yani, viewdaki son 17 karakteri alıyoruz
            String info = ((TextView)view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent(BluetoothListe.this,BluetoothVeriAlma.class);
            startActivity(intent);
            //Yeni bir Activity baslatman icin bir intent tanımlıyoruz




        }
    };

}

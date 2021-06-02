package com.windypermadi.scannerlogos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    CustomProgressbar customProgress = CustomProgressbar.getInstance();
    public static boolean status_scan = false;
    public static String id_tiket;
    String nama_kota, nama_ibadah, nama_jenis_ibadah, jam, jumlah_reservasi, tgl_reservasi, nama_orang, idreservasi_ibadah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_scan).setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, Scan.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status_scan) {
            InkuiriScan(id_tiket);
        }
    }

    private void InkuiriScan(String kode_tiket) {
        customProgress.showProgress(MainActivity.this, false);
        AndroidNetworking.get(Config.url + "scan/scan.php")
                .addQueryParameter("barcode", kode_tiket)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        status_scan = false;
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject responses = response.getJSONObject(i);
                                idreservasi_ibadah = responses.optString("idreservasi_ibadah");
                                nama_kota = responses.optString("nama_kota");
                                nama_ibadah = responses.optString("nama_ibadah");
                                nama_jenis_ibadah = responses.optString("nama_jenis_ibadah");
                                jam = responses.optString("jam");
                                jumlah_reservasi = responses.optString("jumlah_reservasi");
                                tgl_reservasi = responses.optString("tgl_reservasi");
                                nama_orang = responses.optString("nama_orang");
                                popupBerhasil(kode_tiket, nama_kota, nama_ibadah, nama_jenis_ibadah, jam, jumlah_reservasi, tgl_reservasi, nama_orang);
                            }

                            customProgress.hideProgress();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        status_scan = false;
                        if (error.getErrorCode() == 400) {
                            try {
                                JSONObject body = new JSONObject(error.getErrorBody());
                                CustomLoadingProgress.errorDialog(MainActivity.this, body.optString("pesan"));
                            } catch (JSONException ignored) {
                            }
                        } else {
                            CustomLoadingProgress.errorDialog(MainActivity.this, "Sambunganmu dengan server terputus. Periksa sambungan internet, lalu coba lagi.");
                        }
                        customProgress.hideProgress();
                    }
                });
    }

    private void popupBerhasil(String barcode, String nama_kota, String nama_ibadah, String nama_jenis_ibadah, String jam, String jumlah_reservasi, String tgl_reservasi, String nama_orang) {
        customProgress.hideProgress();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.model_popup_inkuiri_scan, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = dialogBuilder.create();
        TextView text_nama = dialogView.findViewById(R.id.text_nama);
        TextView text_kota = dialogView.findViewById(R.id.text_kota);
        TextView text_tempat = dialogView.findViewById(R.id.text_tempat);
        TextView text_jenis = dialogView.findViewById(R.id.text_jenis);
        TextView text_jam = dialogView.findViewById(R.id.text_jam);
        text_nama.setText(nama_orang);
        text_kota.setText(nama_kota);
        text_tempat.setText(nama_ibadah);
        text_jenis.setText(nama_jenis_ibadah);
        text_jam.setText(jam);
        dialogView.findViewById(R.id.text_ok).setOnClickListener(v -> {
            ubahVerifikasi(barcode);
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void ubahVerifikasi(String idreservasi_ibadah) {
        AndroidNetworking.get(Config.url + "scan/ubahVerifikasi.php")
                .addQueryParameter("barcode", idreservasi_ibadah)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        customProgress.hideProgress();
                    }

                    @Override
                    public void onError(ANError error) {
                        customProgress.hideProgress();
                    }
                });
    }
}
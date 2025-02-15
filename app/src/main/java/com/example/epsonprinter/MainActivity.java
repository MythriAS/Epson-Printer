package com.example.epsonprinter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

public class MainActivity extends AppCompatActivity {
    private Printer printer;
    private TextView textView;
    private ImageView printIcon;
    private static final String PRINTER_IP = "192.168.29.167";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        printIcon = findViewById(R.id.printIcon);

        printIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printTextViewText();
            }
        });

        initializePrinter();
    }

    private void initializePrinter() {
        try {
            printer = new Printer(Printer.TM_M30, Printer.MODEL_ANK, this);

            printer.setReceiveEventListener(new ReceiveListener() {
                @Override
                public void onPtrReceive(Printer printer, int code, PrinterStatusInfo status, String printJobId) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Print Completed", Toast.LENGTH_SHORT).show());
                }
            });

            int timeout = 5000;
            printer.connect("TCP:" + PRINTER_IP, timeout);
            Toast.makeText(this, "Connected to Printer via Wi-Fi", Toast.LENGTH_SHORT).show();

        } catch (Epos2Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Printer Connection Failed: " + e.getErrorStatus(), Toast.LENGTH_LONG).show();
        }
    }

    private void printTextViewText() {
        if (printer == null) {
            Toast.makeText(this, "Printer not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            printer.addText(textView.getText().toString() + "\n");
            printer.addCut(Printer.CUT_FEED);
            printer.sendData(Printer.PARAM_DEFAULT);
            printer.clearCommandBuffer();
        } catch (Epos2Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Print Failed: " + e.getErrorStatus(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (printer != null) {
            try {
                printer.disconnect();
            } catch (Epos2Exception e) {
                e.printStackTrace();
            }
            printer = null;
        }
    }
}
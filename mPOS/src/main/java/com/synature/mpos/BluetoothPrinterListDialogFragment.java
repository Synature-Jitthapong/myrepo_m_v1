package com.synature.mpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothPrinterListDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    public static final String TAG = BluetoothPrinterListDialogFragment.class.getSimpleName();

    private static final String DEVICE_ADDRESS_START = " (";
    private static final String DEVICE_ADDRESS_END = ")";

    private final ArrayList<CharSequence> bondedDevices = new ArrayList<>();
    private ArrayAdapter<CharSequence> arrayAdapter;
    private String bluetoothAddress;
    private String bluetoothLogicalName;
    private OnSelectedPrinterListener onSelectedPrinterListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBondedDevices();
        arrayAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_single_choice, bondedDevices);
    }

    public void setOnSelectedPrinterListener(OnSelectedPrinterListener listener) {
        this.onSelectedPrinterListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.fragment_printer_select, null);
        ListView lvPrinter = (ListView) content.findViewById(R.id.listView1);

        lvPrinter.setAdapter(arrayAdapter);
        lvPrinter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvPrinter.setOnItemClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.please_select_printer);
        builder.setView(content);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onSelectedPrinterListener != null)
                    onSelectedPrinterListener.onSelectedPrinter(bluetoothLogicalName, bluetoothAddress);
            }

        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void setSelectedPrinter(final int position) {
        String device = (String) arrayAdapter.getItem(position);

        String logicalName = device.substring(0, device.indexOf(DEVICE_ADDRESS_START));
        String address = device.substring(device.indexOf(DEVICE_ADDRESS_START)
                        + DEVICE_ADDRESS_START.length(),
                device.indexOf(DEVICE_ADDRESS_END));
        bluetoothLogicalName = logicalName;
        bluetoothAddress = address;
    }

    private void setBondedDevices() {
        bondedDevices.clear();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter
                    .getBondedDevices();

            if (!bondedDeviceSet.isEmpty()) {
                for (BluetoothDevice device : bondedDeviceSet) {
                    bondedDevices.add(device.getName() + DEVICE_ADDRESS_START
                            + device.getAddress() + DEVICE_ADDRESS_END);
                }
                if (arrayAdapter != null) {
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        setSelectedPrinter(position);
    }

    public interface OnSelectedPrinterListener {
        void onSelectedPrinter(String bluetoothLogicalName, String bluetoothAddress);
    }
}

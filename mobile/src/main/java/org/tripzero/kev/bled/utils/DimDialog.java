package org.tripzero.kev.bled.utils;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.gc.materialdesign.views.Slider;

import org.tripzero.kev.bled.R;

/**
 * Created by ammonrees on 3/21/15.
 */
public class DimDialog extends DialogFragment {

    Button submit, cancel;
    Slider dimLevel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dim_dialog, null);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        dimLevel = (Slider) dialog.findViewById(R.id.slider);


        dimLevel.setOnValueChangedListener(new Slider.OnValueChangedListener() {

            @Override
            public void onValueChanged(int i) {

                // Set brightness etc etc
            }
        });

/*
        submit = (Button) dialog.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub



            }

        });

        cancel = (Button) dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                dismiss();

            }
        });
*/

        return dialog;
    }



}

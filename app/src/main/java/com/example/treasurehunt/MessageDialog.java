package com.example.treasurehunt;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.treasurehunt.Models.Treasure;

public class MessageDialog extends DialogFragment {


    private static final String TAG = "Message Dialog";

    private Treasure treasure;

    public MessageDialog(Treasure treasure) {
        this.treasure = treasure;
    }



    //widgets
    private TextView mActionOk, mMessage;


    //vars

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_dialog, container, false);
        mActionOk = view.findViewById(R.id.action_ok);
        mMessage = view.findViewById(R.id.message);

        String message ="You Found the treasure hidden by " + treasure.getUser().getUsername()+" . The message is : " + treasure.getMessage();

        mMessage.setText(message);


        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
            }
        });


        return view;
    }







}

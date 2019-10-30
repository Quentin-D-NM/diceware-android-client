package edu.cnm.deepdive.diceware.controller;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.model.Passphrase;

public class PassphraseFragment extends DialogFragment {

  public static PassphraseFragment newInstance(Passphrase passphrase) {
    PassphraseFragment fragment = new PassphraseFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }
}

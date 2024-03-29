package edu.cnm.deepdive.diceware.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.cnm.deepdive.diceware.R;
import edu.cnm.deepdive.diceware.model.Passphrase;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;
import edu.cnm.deepdive.diceware.view.PassphraseAdapteer;
import edu.cnm.deepdive.diceware.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity
    implements PassphraseFragment.OnCompleteListener {

  private ProgressBar waiting;
  private RecyclerView passphraseList;
  private MainViewModel viewModel;
  private GoogleSignInService signInService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupUI();
    setupViewModel();
    setupSignIn();
  }

  private void setupViewModel() {
    viewModel = ViewModelProviders.of(this).get(MainViewModel.class);//get view model of this context for the MainViewModel instance
    viewModel.getPassphrases().observe(this, (passphrases) -> {
      PassphraseAdapteer adapteer = new PassphraseAdapteer(this, passphrases,
          (view, position, passphrase) -> {
            Log.d("Passphrase click", passphrase.getKey());
            PassphraseFragment fragment =  new PassphraseFragment().newInstance(passphrase);
            fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
            },
          (menu, position, passphrase) -> {
            Log.d("Passphrase context", passphrase.getKey());
            getMenuInflater().inflate(R.menu.passphrase_context, menu);
            menu.findItem(R.id.delete_passphrase).setOnMenuItemClickListener(
                (item) -> {
                  Log.d("Delete selected", passphrase.getKey());
                  waiting.setVisibility(View.VISIBLE);
                  refreshSignIn(() -> viewModel.deletePassphrase(passphrase));
                  return true;
                });
          });
      passphraseList.setAdapter(adapteer);
      waiting.setVisibility(View.GONE);
    });
    viewModel.getThrowable().observe(this, (throwable) -> {
      if(throwable != null) {
        waiting.setVisibility(View.GONE);
        Toast.makeText(this, String.format("Unable to connect to server:%s", throwable.getMessage()), Toast.LENGTH_LONG).show();
      }
    });
  }

  private void setupSignIn() {
    signInService = GoogleSignInService.getInstance();
    signInService.getAccount().observe(this, (account) ->
        viewModel.setAccount(account));
  }

  private void setupUI() {
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> {
      //new calls a constructor factory methods dont need a new keyword
      PassphraseFragment fragment = PassphraseFragment.newInstance();
      fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
    });
    waiting = findViewById(R.id.waiting);
    passphraseList = findViewById(R.id.keyword_list);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean handled = true;
    switch (item.getItemId()) {
      case R.id.refresh:
        refreshSignIn(() -> viewModel.refreshPassphrases());
        break;
      case R.id.action_settings:
        break;
      case R.id.sign_out:
        signOut();
        break;
      default:
        handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  private void signOut() {
    signInService.signOut().addOnCompleteListener((task) ->{
      Intent intent = new Intent(this, LoginActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    });
  }

  private void refreshSignIn(Runnable runnable) {
    signInService.refresh()
        .addOnSuccessListener((account) -> runnable.run())
        .addOnFailureListener((e) -> signOut());
  }

  @Override
  public void complete(Passphrase passphrase) {
    waiting.setVisibility(View.VISIBLE);
    if (passphrase.getId() == 0) {
      viewModel.addPassphrase(passphrase);
    } else {
      viewModel.updatePassphrase(passphrase);
    }
  }
}

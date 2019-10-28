package edu.cnm.deepdive.diceware;

import android.app.Application;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import edu.cnm.deepdive.diceware.service.GoogleSignInService;

public class DicewareApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    GoogleSignInService.setApplicationContext(this);
  }
}

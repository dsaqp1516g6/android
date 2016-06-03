package upc.eetac.dsa.secretsites;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import upc.eetac.dsa.secretsites.client.SecretSitesClient;
import upc.eetac.dsa.secretsites.client.SecretSitesClientException;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A register screen that offers register.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView1;
    private EditText mPasswordView2;
    private EditText mEmailView;
    private EditText mFullnameView;
    private View mProgressView;
    private View mRegisterFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the register form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username_register);
        mPasswordView1 = (EditText) findViewById(R.id.password_register_1);
        mPasswordView2 = (EditText) findViewById(R.id.password_register_2);
        mEmailView = (EditText) findViewById(R.id.email_register);
        mFullnameView = (EditText) findViewById(R.id.fullname_register);
        mFullnameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignUpButton = (Button) findViewById(R.id.username_sign_up_button);
        mUsernameSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView1.setError(null);
        mPasswordView2.setError(null);
        mEmailView.setError(null);
        mFullnameView.setError(null);

        // Store values at the time of the register attempt.
        String username = mUsernameView.getText().toString();
        String password1 = mPasswordView1.getText().toString();
        String password2 = mPasswordView2.getText().toString();
        String email = mEmailView.getText().toString();
        String fullname = mFullnameView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password1, if the user entered one.
        if (!TextUtils.isEmpty(password1) && !isPasswordValid(password1)) {
            mPasswordView1.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView1;
            cancel = true;
        }

        // Check for a valid password2, if the user entered one.
        if (!TextUtils.isEmpty(password2) && !isPasswordsEquals(password1, password2)) {
            mPasswordView2.setError(getString(R.string.error_invalid_password2));
            focusView = mPasswordView2;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid fullname address.
        if (TextUtils.isEmpty(fullname)) {
            mFullnameView.setError(getString(R.string.error_field_required));
            focusView = mFullnameView;
            cancel = true;
        } else if (!isFullnameValid(fullname)) {
            mFullnameView.setError(getString(R.string.error_invalid_fullname));
            focusView = mFullnameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(username, password1, email, fullname);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
        //return password.length() > 4;
    }

    private boolean isPasswordsEquals(String password1, String password2) {
        if(password1.compareTo(password2) == 0)
            return true;
        return false;
    }

    private boolean isEmailValid(String email) {
        if(email.contains("@"))
            return true;
        return false;
    }

    private boolean isFullnameValid(String fullname) {
        return true;
    }


    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, String> {

        private final String mUsername;
        private final String mPassword;
        private final String mEmail;
        private final String mFullname;
        private String user = null;

        UserRegisterTask(String username, String password, String email, String fullname) {
            mUsername = username;
            mPassword = password;
            mEmail = email;
            mFullname = fullname;
        }

        @Override
        protected String doInBackground(Void... params) {
            SecretSitesClient client = SecretSitesClient.getInstance();
            try {
                this.user = client.register(mUsername, mPassword, mEmail, mFullname);
                return null;
            }
            catch (SecretSitesClientException ex) {
                return ex.getReason();
            }
        }

        @Override
        protected void onPostExecute(final String response) {
            mAuthTask = null;

            showProgress(false);

            if(this.user != null) {                         //Register is OK
                Intent returnIntent = new Intent();
                returnIntent.putExtra("user", this.user);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
            else if (response.contains("exists")) {             //Repeat Username
                mUsernameView.setError(getString(R.string.error_conflict_username));
                mUsernameView.requestFocus();
            }
            else {                                          //Unknow Errors
                Toast.makeText(RegisterActivity.this, response,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}


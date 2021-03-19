package com.developer.databasewithroom;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.developer.databasewithroom.database.UserRespository;
import com.developer.databasewithroom.local.UserDataSource;
import com.developer.databasewithroom.local.UserDatabase;
import com.developer.databasewithroom.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ListView listUser;
    private FloatingActionButton fab;

    List<User> userList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    private CompositeDisposable compositeDisposable;
    private UserRespository userRespository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listUser = findViewById(R.id.list);
        fab = findViewById(R.id.fab);

        compositeDisposable = new CompositeDisposable();

        arrayAdapter = new ArrayAdapter(MainActivity.this,  android.R.layout.simple_list_item_1, userList);
        registerForContextMenu(listUser);
        listUser.setAdapter(arrayAdapter);

        UserDatabase userDatabase = UserDatabase.getInstance(MainActivity.this); //create db
        Log.d("userDatabase ", userDatabase.toString());
        userRespository = UserRespository.getInstance(UserDataSource.getInstance(userDatabase.userDAO()));
//        Log.d("userRespository ", userRespository.toString());


       // loadData();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> e) {
                        User user = new User("NM", UUID.randomUUID().toString() + "@mail.ru");
                        userRespository.insertUser(user);
                        Log.d("userDatabase ", user.toString());
                        e.onComplete();
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer() {
                            @Override
                            public void accept(Object o) {
                                Toast.makeText(MainActivity.this, "User Added!", Toast.LENGTH_SHORT).show();

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Toast.makeText(MainActivity.this, ""+ throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }, new Action(){
                            @Override
                            public void run() throws Exception {
                                loadData();
                            }
                        });
            }
        });
    }

    private void loadData() {
        Disposable disposable = userRespository.getAllUsers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<User>>(){
                    @Override
                    public void accept(List<User> users) throws Exception {
                        onGetAllUserSuccess(users);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception{
                        Toast.makeText(MainActivity.this, ""+ throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void onGetAllUserSuccess(List<User> users) {

        userList.clear();
        users.addAll(users);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear:
                deleteAllUsers();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllUsers() {
        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws  Exception {
                userRespository.deleteAllUsers();
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                       // Toast.makeText(MainActivity.this, "User Added!", Toast.LENGTH_SHORT).show();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, ""+ throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, new Action(){

                    @Override
                    public void run() throws Exception {
                        loadData();
                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle("Select action:");
        menu.add(Menu.NONE,0,Menu.NONE, "UPDATE");
        menu.add(Menu.NONE,1,Menu.NONE, "DELETE");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final User user = userList.get(info.position);
        switch (item.getItemId()){
            case 0:
            {
                final EditText editText = new EditText( MainActivity.this);
                editText.setText(user.getName());
                editText.setHint("Enter your name");
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Edit")
                        .setMessage("Edit user name")
                        .setView(editText)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (TextUtils.isEmpty(editText.getText().toString())){
                                    return;
                                }else {
                                    user.setName(editText.getText().toString());
                                    updateUser(user);
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

            }
            break;
            case 1:
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Delete "+ user.toString())
                      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               deleteUser(user);
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

            }
            break;
        }
        return true;
    }

    private void deleteUser(final User user) {
        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws  Exception {
                userRespository.deleteUser(user);
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        // Toast.makeText(MainActivity.this, "User Added!", Toast.LENGTH_SHORT).show();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, ""+ throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, new Action(){

                    @Override
                    public void run() throws Exception {
                        loadData();
                    }
                });

        compositeDisposable.add(disposable);
    }

    private void updateUser(final User user) {
        Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws  Exception {
                userRespository.updateUser(user);
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                       // Toast.makeText(MainActivity.this, "User Added!", Toast.LENGTH_SHORT).show();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, ""+ throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, new Action(){

                    @Override
                    public void run() throws Exception {
                        loadData();
                    }
                });

        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

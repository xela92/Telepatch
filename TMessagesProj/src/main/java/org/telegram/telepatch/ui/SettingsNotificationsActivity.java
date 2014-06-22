/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.telepatch.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.telepatch.messenger.LocaleController;
import org.telegram.telepatch.messenger.TLObject;
import org.telegram.telepatch.messenger.TLRPC;
import org.telegram.telepatch.messenger.ConnectionsManager;
import org.telegram.telepatch.messenger.FileLog;
import org.telegram.telepatch.messenger.MessagesController;
import org.telegram.telepatch.messenger.R;
import org.telegram.telepatch.messenger.RPCRequest;
import org.telegram.telepatch.messenger.Utilities;
import org.telegram.telepatch.ui.Views.ActionBar.ActionBarLayer;
import org.telegram.telepatch.ui.Views.ActionBar.BaseFragment;

public class SettingsNotificationsActivity extends BaseFragment {
    private ListView listView;
    private boolean reseting = false;

    private int notificationsServiceRow;
    private int messageSectionRow;
    private int messageAlertRow;
    private int messagePreviewRow;
    private int messageVibrateRow;
    private int messageSoundRow;
    private int groupSectionRow;
    private int groupAlertRow;
    private int groupPreviewRow;
    private int groupVibrateRow;
    private int groupSoundRow;
    private int inappSectionRow;
    private int inappSwitchRow;
    private int inappSoundRow;
    private int inappVibrateRow;
    private int inappPreviewRow;
    private int eventsSectionRow;
    private int contactJoinedRow;
    private int pebbleSectionRow;
    private int pebbleAlertRow;
    private int resetSectionRow;
    private int resetNotificationsRow;
    private int rowCount = 0;
    private boolean inappCheckDisabled = false;

    @Override
    public boolean onFragmentCreate() {
        notificationsServiceRow = rowCount++;
        messageSectionRow = rowCount++;
        messageAlertRow = rowCount++;
        messagePreviewRow = rowCount++;
        messageVibrateRow = rowCount++;
        messageSoundRow = rowCount++;
        groupSectionRow = rowCount++;
        groupAlertRow = rowCount++;
        groupPreviewRow = rowCount++;
        groupVibrateRow = rowCount++;
        groupSoundRow = rowCount++;
        inappSectionRow = rowCount++;
        inappSwitchRow = rowCount++;
        inappSoundRow = rowCount++;
        inappVibrateRow = rowCount++;
        inappPreviewRow = rowCount++;
        eventsSectionRow = rowCount++;
        contactJoinedRow = rowCount++;
        pebbleSectionRow = rowCount++;
        pebbleAlertRow = rowCount++;
        resetSectionRow = rowCount++;
        resetNotificationsRow = rowCount++;

        return super.onFragmentCreate();
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        if (fragmentView == null) {
            actionBarLayer.setDisplayHomeAsUpEnabled(true, R.drawable.ic_ab_back);
            actionBarLayer.setBackOverlay(R.layout.updating_state_layout);
            actionBarLayer.setTitle(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds));
            actionBarLayer.setActionBarMenuOnItemClick(new ActionBarLayer.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            fragmentView = inflater.inflate(R.layout.settings_layout, container, false);
            ListAdapter listAdapter = new ListAdapter(getParentActivity());
            listView = (ListView)fragmentView.findViewById(R.id.listView);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                private boolean inAppIsClickable = true;
                public void setInAppClickable(boolean isClickable) {
                    inAppIsClickable = isClickable;
                }
                private boolean isInAppClickable() {
                    return inAppIsClickable;
                }
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)  {
                    if (i == messageAlertRow || i == groupAlertRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        boolean enabled;
                        if (i == messageAlertRow) {
                            enabled = preferences.getBoolean("EnableAll", true);
                            editor.putBoolean("EnableAll", !enabled);
                        } else if (i == groupAlertRow) {
                            enabled = preferences.getBoolean("EnableGroup", true);
                            editor.putBoolean("EnableGroup", !enabled);
                        }
                        editor.commit();
                        listView.invalidateViews();
                    } else if (i == messagePreviewRow || i == groupPreviewRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        boolean enabledAll = true;
                        boolean enabled;
                        if (i == messagePreviewRow) {
                            enabled = preferences.getBoolean("EnablePreviewAll", true);
                            editor.putBoolean("EnablePreviewAll", !enabled);
                        } else if (i == groupPreviewRow) {
                            enabled = preferences.getBoolean("EnablePreviewGroup", true);
                            editor.putBoolean("EnablePreviewGroup", !enabled);
                        }
                        editor.commit();
                        listView.invalidateViews();
                    } else if (i == messageVibrateRow || i == groupVibrateRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        boolean enabled;
                        if (i == messageVibrateRow) {
                            enabled = preferences.getBoolean("EnableVibrateAll", true);
                            editor.putBoolean("EnableVibrateAll", !enabled);
                        } else if (i == groupVibrateRow) {
                            enabled = preferences.getBoolean("EnableVibrateGroup", true);
                            editor.putBoolean("EnableVibrateGroup", !enabled);
                        }
                        editor.commit();
                        listView.invalidateViews();
                    } else if (i == messageSoundRow || i == groupSoundRow) {
                        try {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                            Intent tmpIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            Uri currentSound = null;

                            String defaultPath = null;
                            Uri defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                            if (defaultUri != null) {
                                defaultPath = defaultUri.getPath();
                            }

                            if (i == messageSoundRow) {
                                String path = preferences.getString("GlobalSoundPath", defaultPath);
                                if (path != null && !path.equals("NoSound")) {
                                    if (path.equals(defaultPath)) {
                                        currentSound = defaultUri;
                                    } else {
                                        currentSound = Uri.parse(path);
                                    }
                                }
                            } else if (i == groupSoundRow) {
                                String path = preferences.getString("GroupSoundPath", defaultPath);
                                if (path != null && !path.equals("NoSound")) {
                                    if (path.equals(defaultPath)) {
                                        currentSound = defaultUri;
                                    } else {
                                        currentSound = Uri.parse(path);
                                    }
                                }
                            }
                            tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentSound);
                            getParentActivity().startActivityForResult(tmpIntent, i);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    } else if (i == resetNotificationsRow) {
                        if (reseting) {
                            return;
                        }
                        reseting = true;
                        TLRPC.TL_account_resetNotifySettings req = new TLRPC.TL_account_resetNotifySettings();
                        ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
                            @Override
                            public void run(TLObject response, TLRPC.TL_error error) {
                                Utilities.RunOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessagesController.getInstance().enableJoined = true;
                                        reseting = false;
                                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.clear();
                                        editor.commit();
                                        if (listView != null) {
                                            listView.invalidateViews();
                                        }
                                        if (getParentActivity() != null) {
                                            Toast toast = Toast.makeText(getParentActivity(), R.string.ResetNotificationsText, Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                });
                            }
                        }, null, true, RPCRequest.RPCRequestClassGeneric);
                        //TODO qua metto lo switch generico per le notifiche in app --> onclick mi fa quanto segue
                    } else if (i == inappSwitchRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        boolean enabled = preferences.getBoolean("EnableInAppNotifications", false);
                        Log.i("xela92", "il click che ho fatto l'ha acceso?  " + !enabled);
                        editor.putBoolean("EnableInAppNotifications", !enabled);
                        editor.commit();
                        if (!preferences.getBoolean("EnableInAppNotifications", false)) {
                                inappCheckDisabled = true;
                                setInAppClickable(false);
                                Log.i("xela92", "ora NON sei clickabile");
                            } else if (preferences.getBoolean("EnableInAppNotifications", false)) {
                                inappCheckDisabled = false;
                                setInAppClickable(true);
                                Log.i("xela92", "Ok ora sei clickabile");
                        }
                        if (inappCheckDisabled) {
                            setInAppClickable(false);
                        } else if (!inappCheckDisabled) {
                            setInAppClickable(true);
                        }

                        listView.invalidateViews();
                    } else if (i == inappSoundRow) {
                        if (!isInAppClickable()) {
                            Log.i("xela92", "non sei clickabile.");
                        } else {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            boolean enabled = preferences.getBoolean("EnableInAppSounds", true);
                            editor.putBoolean("EnableInAppSounds", !enabled);
                            editor.commit();

                        }
                        listView.invalidateViews();
                    } else if (i == inappVibrateRow) {
                        if (!isInAppClickable()) {
                            Log.i("xela92", "non sei clickabile.");
                        } else {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            boolean enabled = preferences.getBoolean("EnableInAppVibrate", true);
                            editor.putBoolean("EnableInAppVibrate", !enabled);
                            editor.commit();
                        }
                        listView.invalidateViews();
                    } else if (i == inappPreviewRow) {
                        if (!isInAppClickable()) {
                            Log.i("xela92", "non sei clickabile.");
                        } else {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            boolean enabled = preferences.getBoolean("EnableInAppPreview", true);
                            editor.putBoolean("EnableInAppPreview", !enabled);
                            editor.commit();
                        }
                        listView.invalidateViews();
                    } else if (i == contactJoinedRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        boolean enabled = preferences.getBoolean("EnableContactJoined", true);
                        MessagesController.getInstance().enableJoined = !enabled;
                        editor.putBoolean("EnableContactJoined", !enabled);
                        editor.commit();
                        listView.invalidateViews();
                    } else if (i == pebbleAlertRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        boolean enabled = preferences.getBoolean("EnablePebbleNotifications", false);
                        editor.putBoolean("EnablePebbleNotifications", !enabled);
                        editor.commit();
                        listView.invalidateViews();
                    } else if (i == notificationsServiceRow) {
                        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                        boolean enabled = preferences.getBoolean("pushService", true);
                        if (!enabled) {
                            final SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("pushService", !enabled);
                            editor.commit();
                            listView.invalidateViews();
                            ApplicationLoader.startPushService();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setMessage(LocaleController.getString("NotificationsServiceDisableInfo", R.string.NotificationsServiceDisableInfo));
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ApplicationLoader.stopPushService();
                                    final SharedPreferences.Editor editor = preferences.edit();
                                    editor.putBoolean("pushService", false);
                                    editor.commit();
                                    listView.invalidateViews();
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showAlertDialog(builder);
                        }
                    }
                }
            });
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String name = null;
            if (ringtone != null) {
                Ringtone rng = RingtoneManager.getRingtone(getParentActivity(), ringtone);
                if (rng != null) {
                    if(ringtone.equals(Settings.System.DEFAULT_NOTIFICATION_URI)) {
                        name = LocaleController.getString("Default", R.string.Default);
                    } else {
                        name = rng.getTitle(getParentActivity());
                    }
                    rng.stop();
                }
            }

            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            if (requestCode == messageSoundRow) {
                if (name != null && ringtone != null) {
                    editor.putString("GlobalSound", name);
                    editor.putString("GlobalSoundPath", ringtone.toString());
                } else {
                    editor.putString("GlobalSound", "NoSound");
                    editor.putString("GlobalSoundPath", "NoSound");
                }
            } else if (requestCode == groupSoundRow) {
                if (name != null && ringtone != null) {
                    editor.putString("GroupSound", name);
                    editor.putString("GroupSoundPath", ringtone.toString());
                } else {
                    editor.putString("GroupSound", "NoSound");
                    editor.putString("GroupSoundPath", "NoSound");
                }
            }
            editor.commit();
            listView.invalidateViews();
        }
    }

    private class ListAdapter extends BaseAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return !(i == messageSectionRow || i == groupSectionRow || i == inappSectionRow || i == eventsSectionRow || i == pebbleSectionRow || i == resetSectionRow);
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_section_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_section_text);
                if (i == messageSectionRow) {
                    textView.setText(LocaleController.getString("MessageNotifications", R.string.MessageNotifications));
                } else if (i == groupSectionRow) {
                    textView.setText(LocaleController.getString("GroupNotifications", R.string.GroupNotifications));
                } else if (i == inappSectionRow) {
                    textView.setText(LocaleController.getString("InAppNotifications", R.string.InAppNotifications));
                } else if (i == eventsSectionRow) {
                    textView.setText(LocaleController.getString("Events", R.string.Events));
                } else if (i == pebbleSectionRow) {
                    textView.setText(LocaleController.getString("Pebble", R.string.Pebble));
                } else if (i == resetSectionRow) {
                    textView.setText(LocaleController.getString("Reset", R.string.Reset));
                }
            } if (type == 1) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_check_notify_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                View divider = view.findViewById(R.id.settings_row_divider);

                ImageView checkButton = (ImageView)view.findViewById(R.id.settings_row_check_button);
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                boolean enabled = false;
                boolean enabledAll = preferences.getBoolean("EnableAll", true);
                boolean enabledGroup = preferences.getBoolean("EnableGroup", true);

                if (i == messageAlertRow || i == groupAlertRow) {
                    if (i == messageAlertRow) {
                        enabled = enabledAll;
                    } else if (i == groupAlertRow) {
                        enabled = enabledGroup;
                    }
                    textView.setText(LocaleController.getString("Alert", R.string.Alert));
                    divider.setVisibility(View.VISIBLE);
                } else if (i == messagePreviewRow || i == groupPreviewRow) {
                    if (i == messagePreviewRow) {
                        enabled = preferences.getBoolean("EnablePreviewAll", true);
                    } else if (i == groupPreviewRow) {
                        enabled = preferences.getBoolean("EnablePreviewGroup", true);
                    }
                    textView.setText(LocaleController.getString("MessagePreview", R.string.MessagePreview));
                    divider.setVisibility(View.VISIBLE);
                } else if (i == messageVibrateRow || i == groupVibrateRow) {
                    if (i == messageVibrateRow) {
                        enabled = preferences.getBoolean("EnableVibrateAll", true);
                    } else if (i == groupVibrateRow) {
                        enabled = preferences.getBoolean("EnableVibrateGroup", true);
                    }
                    textView.setText(LocaleController.getString("Vibrate", R.string.Vibrate));
                    divider.setVisibility(View.VISIBLE);
                    //TODO qui metto il nome dell'etichetta dello switch e faccio i controlli sull'in-app
                } else if (i == inappSwitchRow) {
                    enabled = preferences.getBoolean("EnableInAppNotifications", false);
                    textView.setText(LocaleController.getString("InAppSwitch", R.string.InAppSwitch));
                    if (!preferences.getBoolean("EnableInAppNotifications", false)) {
                        inappCheckDisabled = true;
                    } else if (preferences.getBoolean("EnableInAppNotifications", false)){
                        inappCheckDisabled = false;
                    }
                    listView.invalidateViews();
                    divider.setVisibility(View.VISIBLE);
                } else if (i == inappSoundRow) {
                    enabled = preferences.getBoolean("EnableInAppSounds", true);
                    textView.setText(LocaleController.getString("InAppSounds", R.string.InAppSounds));
                    if (inappCheckDisabled) {
                        Log.i("xela92", "ok disattivo tutto");
                        checkButton.setVisibility(View.INVISIBLE);
                        textView.setTextColor(Color.LTGRAY);
                    } else if (!inappCheckDisabled){
                        checkButton.setVisibility(View.VISIBLE);
                        textView.setTextColor(Color.BLACK);
                    }
                    divider.setVisibility(View.VISIBLE);
                } else if (i == inappVibrateRow) {
                    enabled = preferences.getBoolean("EnableInAppVibrate", true);
                    textView.setText(LocaleController.getString("InAppVibrate", R.string.InAppVibrate));
                    if (inappCheckDisabled) {
                        Log.i("xela92", "ok disattivo tutto");
                        checkButton.setVisibility(View.INVISIBLE);
                        textView.setTextColor(Color.LTGRAY);
                    } else if (!inappCheckDisabled){
                        checkButton.setVisibility(View.VISIBLE);
                        textView.setTextColor(Color.BLACK);
                    }
                    divider.setVisibility(View.VISIBLE);
                } else if (i == inappPreviewRow) {
                    enabled = preferences.getBoolean("EnableInAppPreview", true);
                    textView.setText(LocaleController.getString("InAppPreview", R.string.InAppPreview));
                    if (inappCheckDisabled) {
                        Log.i("xela92", "ok disattivo tutto");
                        checkButton.setVisibility(View.INVISIBLE);
                        textView.setTextColor(Color.LTGRAY);
                    } else if (!inappCheckDisabled) {
                        checkButton.setVisibility(View.VISIBLE);
                        textView.setTextColor(Color.BLACK);
                    }
                    divider.setVisibility(View.INVISIBLE);
                } else if (i == contactJoinedRow) {
                    enabled = preferences.getBoolean("EnableContactJoined", true);
                    textView.setText(LocaleController.getString("ContactJoined", R.string.ContactJoined));
                    divider.setVisibility(View.INVISIBLE);
                } else if (i == pebbleAlertRow) {
                    enabled = preferences.getBoolean("EnablePebbleNotifications", false);
                    textView.setText(LocaleController.getString("Alert", R.string.Alert));
                    divider.setVisibility(View.INVISIBLE);
                } else if (i == notificationsServiceRow) {
                    enabled = preferences.getBoolean("pushService", true);
                    textView.setText(LocaleController.getString("NotificationsService", R.string.NotificationsService));
                    divider.setVisibility(View.INVISIBLE);
                }
                if (enabled) {
                    checkButton.setImageResource(R.drawable.btn_check_on);
                } else {
                    checkButton.setImageResource(R.drawable.btn_check_off);
                }
            } else if (type == 2) {
                if (view == null) {
                    LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = li.inflate(R.layout.settings_row_detail_layout, viewGroup, false);
                }
                TextView textView = (TextView)view.findViewById(R.id.settings_row_text);
                TextView textViewDetail = (TextView)view.findViewById(R.id.settings_row_text_detail);
                View divider = view.findViewById(R.id.settings_row_divider);
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                boolean enabledAll = preferences.getBoolean("EnableAll", true);
                boolean enabledGroup = preferences.getBoolean("EnableGroup", true);

                if (i == messageSoundRow || i == groupSoundRow) {
                    String name = null;
                    if (i == messageSoundRow) {
                        name = preferences.getString("GlobalSound", LocaleController.getString("Default", R.string.Default));
                    } else if (i == groupSoundRow) {
                        name = preferences.getString("GroupSound", LocaleController.getString("Default", R.string.Default));
                    }
                    if (name.equals("NoSound")) {
                        textViewDetail.setText(LocaleController.getString("NoSound", R.string.NoSound));
                    } else {
                        textViewDetail.setText(name);
                    }
                    textView.setText(LocaleController.getString("Sound", R.string.Sound));
                    divider.setVisibility(View.INVISIBLE);
                } else if (i == resetNotificationsRow) {
                    textView.setText(LocaleController.getString("ResetAllNotifications", R.string.ResetAllNotifications));
                    textViewDetail.setText(LocaleController.getString("UndoAllCustom", R.string.UndoAllCustom));
                    divider.setVisibility(View.INVISIBLE);
                }
            }

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == messageSectionRow || i == groupSectionRow || i == inappSectionRow || i == eventsSectionRow || i == pebbleSectionRow || i == resetSectionRow) {
                return 0;
            } else if (i == messageAlertRow || i == messagePreviewRow || i == messageVibrateRow ||
                    i == groupAlertRow || i == groupPreviewRow || i == groupVibrateRow || i == inappSwitchRow ||
                    i == inappSoundRow || i == inappVibrateRow || i == inappPreviewRow ||
                    i == contactJoinedRow ||
                    i == pebbleAlertRow || i == notificationsServiceRow) {
                return 1;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}

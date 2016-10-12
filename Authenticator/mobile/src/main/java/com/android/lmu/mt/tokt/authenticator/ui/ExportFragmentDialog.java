package com.android.lmu.mt.tokt.authenticator.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.lmu.mt.tokt.authenticator.R;
import com.android.lmu.mt.tokt.authenticator.data.TagData;
import com.android.lmu.mt.tokt.authenticator.database.DataEntry;
import com.android.lmu.mt.tokt.authenticator.database.EventEntry;
import com.android.lmu.mt.tokt.authenticator.database.TagEntry;
import com.android.lmu.mt.tokt.authenticator.shared.AppConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class ExportFragmentDialog extends DialogFragment {

    private Context mContext;
    private Realm mRealm;
    private ProgressBar dataProgressbar;
    private ProgressBar tagProgressbar;
    private ProgressBar eventProgressbar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export, container, false);

        mContext = view.getContext();
        dataProgressbar = (ProgressBar) view.findViewById(R.id.export_progress);
        tagProgressbar = (ProgressBar) view.findViewById(R.id.export_progress_tag);
        eventProgressbar = (ProgressBar) view.findViewById(R.id.export_progress_event);


        Button exportButton = (Button) view.findViewById(R.id.exportButton);
        exportButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {


                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                exportFile();
                            }
                        };

                        Thread t = new Thread(r);
                        t.start();
                    }
                }

        );


        view.findViewById(R.id.exportTagsButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                //exportTagsFromListFile();
                                exportTagsFromDBFile();
                            }
                        };

                        Thread t = new Thread(r);
                        t.start();
                    }
                }

        );

        view.findViewById(R.id.exportEventsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        //exportTagsFromListFile();
                        exportEventsFromDBFile();
                    }
                };

                Thread t = new Thread(r);
                t.start();
            }
        });

        Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        showConfirmDeleteDataDialog();
                    }
                }
        );

        Button closeButton = (Button) view.findViewById(R.id.close_dialog);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExportFragmentDialog.this.dismiss();
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(ExportFragmentDialog.this.getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteData() {
        mRealm = Realm.getInstance(mContext);
        mRealm.beginTransaction();

        RealmResults<DataEntry> resultData = mRealm.where(DataEntry.class).findAll();
        RealmResults<TagEntry> resultTag = mRealm.where(TagEntry.class).findAll();
        Log.e("Authenticator", "data rows after delete = " + resultData.size());
        Log.e("Authenticator", "tag rows after delete = " + resultTag.size());

        // Delete all matches
        resultData.clear();
        resultTag.clear();

        mRealm.commitTransaction();

        resultData = mRealm.where(DataEntry.class).findAll();
        resultTag = mRealm.where(TagEntry.class).findAll();
        Log.e("Authenticator", "data rows after delete = " + resultData.size());
        Log.e("Authenticator", "tag rows after delete = " + resultTag.size());

        ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ExportFragmentDialog.this.getActivity(),
                        getContext().getResources().getString(R.string.all_data_deleted), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void exportFile() {
        mRealm = Realm.getInstance(mContext);

        RealmResults<DataEntry> result = mRealm.where(DataEntry.class).findAll();
        final int total_row = result.size();
        final int total_col = 11;
        Log.i("Authenticator", "total_row = " + total_row);
        if (total_row > 0) {

            final String fileprefix = "export";
            final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
            final String filename = String.format("%s_%s.txt", fileprefix, date);

            final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Authenticator";

            final File logfile = new File(directory, filename);
            final File logPath = logfile.getParentFile();

            if (!logPath.isDirectory() && !logPath.mkdirs()) {
                Log.e("Authenticator", "Could not create directory for log files");
            }

            try {
                FileWriter filewriter = new FileWriter(logfile);
                BufferedWriter bw = new BufferedWriter(filewriter);

                ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataProgressbar.setMax(total_row);
                        dataProgressbar.setVisibility(View.VISIBLE);
                        dataProgressbar.setProgress(0);
                    }
                });

                // Write the string to the file
                for (int i = 1; i < total_row; i++) {
                    final int progress = i;
                    ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataProgressbar.setProgress(progress);
                        }
                    });

                    StringBuffer sb = new StringBuffer();
                    sb.append("{\"" + AppConstants.LOG_KEY_ANDROID_DEVICE + "\": ");
                    sb.append("\"" + result.get(i).getAndroidDevice().toString() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_USERNAME + "\": ");
                    sb.append("\"" + result.get(i).getUsername() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_TIMESTAMP + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getTimestamp()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_DATE + "\": ");
                    sb.append("\"" + convertTime(result.get(i).getTimestamp()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_X + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getX()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_Y + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getY()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_Z + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getZ()) + "\"");
                    sb.append(" ,");

                    sb.append("{\"" + AppConstants.LOG_KEY_ACCURACY + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getAccuracy()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_DATASOURCE + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getDataSource()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_SENSORNAME + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getSensorName()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_DATATYPE + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getDataType()) + "\"");
                    sb.append(" },");
                    sb.append("\n");
                    bw.write(sb.toString());
                }
                bw.flush();
                bw.close();

                ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dataProgressbar.setVisibility(View.GONE);
                            }
                        }, 1000);

                    }
                });


                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("*/*");

                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "Authenticator data export");
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));


                Log.i("Authenticator", "export finished!");
            } catch (IOException ioe) {
                Log.e("Authenticator", "IOException while writing Logfile");
            }
        } else {
            ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExportFragmentDialog.this.getActivity(),
                            getContext().getResources().getString(R.string.no_sensor_data), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


    private void exportTagsFromDBFile() {
        mRealm = Realm.getInstance(mContext);

        RealmResults<TagEntry> result = mRealm.where(TagEntry.class).findAll();
        final int total_row = result.size();
        final int total_col = 3;
        Log.i("Authenticator", "total_row = " + total_row);
        if (total_row > 0) {
            final String fileprefix = "export_tags_";
            final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
            final String filename = String.format("%s_%s.txt", fileprefix, date);

            final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Authenticator";

            final File logfile = new File(directory, filename);
            final File logPath = logfile.getParentFile();

            if (!logPath.isDirectory() && !logPath.mkdirs()) {
                Log.e("Authenticator", "Could not create directory for log files");
            }

            try {
                FileWriter filewriter = new FileWriter(logfile);
                BufferedWriter bw = new BufferedWriter(filewriter);

                ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tagProgressbar.setMax(total_row);
                        tagProgressbar.setVisibility(View.VISIBLE);
                        tagProgressbar.setProgress(0);
                    }
                });

                // Write the string to the file
                for (int i = 1; i < total_row; i++) {
                    final int progress = i;
                    ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tagProgressbar.setProgress(progress);
                        }
                    });

                    StringBuffer sb = new StringBuffer();
                    sb.append("{\"" + AppConstants.LOG_KEY_TAG_EVENT + "\": ");
                    sb.append("\"" + result.get(i).getEvent() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_USERNAME + "\": ");
                    sb.append("\"" + result.get(i).getUsername() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_TIMESTAMP + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getTimestamp()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_DATE + "\": ");
                    sb.append("\"" + convertTime(result.get(i).getTimestamp()) + "\"");
                    sb.append(" },");
                    sb.append("\n");
                    bw.write(sb.toString());
                }
                bw.flush();
                bw.close();

                ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tagProgressbar.setVisibility(View.GONE);
                            }
                        }, 1000);

                    }
                });


                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("*/*");

                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "Authenticator tag export");
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));


                Log.i("Authenticator", "export finished!");
            } catch (IOException ioe) {
                Log.e("Authenticator", "IOException while writing Logfile");
            }
        } else {
            ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExportFragmentDialog.this.getActivity(),
                            getContext().getResources().getString(R.string.no_tag_entries), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void exportEventsFromDBFile() {
        mRealm = Realm.getInstance(mContext);

        RealmResults<EventEntry> result = mRealm.where(EventEntry.class).findAll();
        final int total_row = result.size();
        final int total_col = 5;
        Log.i("Authenticator", "total_row = " + total_row);
        if (total_row > 0) {
            final String fileprefix = "export_events_";
            final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
            final String filename = String.format("%s_%s.txt", fileprefix, date);

            final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Authenticator";

            final File logfile = new File(directory, filename);
            final File logPath = logfile.getParentFile();

            if (!logPath.isDirectory() && !logPath.mkdirs()) {
                Log.e("Authenticator", "Could not create directory for log files");
            }

            try {
                FileWriter filewriter = new FileWriter(logfile);
                BufferedWriter bw = new BufferedWriter(filewriter);

                ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        eventProgressbar.setMax(total_row);
                        eventProgressbar.setVisibility(View.VISIBLE);
                        eventProgressbar.setProgress(0);
                    }
                });

                // Write the string to the file
                for (int i = 1; i < total_row; i++) {
                    final int progress = i;
                    ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            eventProgressbar.setProgress(progress);
                        }
                    });

                    StringBuffer sb = new StringBuffer();
                    sb.append("{\"" + AppConstants.LOG_KEY_APP_EVENT + "\": ");
                    sb.append("\"" + result.get(i).getEventName() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_USERNAME + "\": ");
                    sb.append("\"" + result.get(i).getUsername() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_STATE + "\": ");
                    sb.append("\"" + result.get(i).getState() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_USERNOTE + "\": ");
                    sb.append("\"" + result.get(i).getUsernote() + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_TIMESTAMP + "\": ");
                    sb.append("\"" + String.valueOf(result.get(i).getTimeStamp()) + "\"");
                    sb.append(" ,");

                    sb.append("\"" + AppConstants.LOG_KEY_DATE + "\": ");
                    sb.append("\"" + convertTime(result.get(i).getTimeStamp()) + "\"");
                    sb.append(" },");
                    sb.append("\n");
                    bw.write(sb.toString());
                }
                bw.flush();
                bw.close();

                ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dataProgressbar.setVisibility(View.GONE);
                            }
                        }, 1000);

                    }
                });


                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("*/*");

                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "Authenticator event export");
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));


                Log.i("Authenticator", "export finished!");
            } catch (IOException ioe) {
                Log.e("Authenticator", "IOException while writing Logfile");
            }
        } else {
            ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExportFragmentDialog.this.getActivity(),
                            getContext().getResources().getString(R.string.no_event_entries), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


    @Deprecated
    private void exportTagsFromListFile() {

        final String fileprefix = "export_tags_";
        final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
        final String filename = String.format("%s_%s.txt", fileprefix, date);

        final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Authenticator";

        final File logfile = new File(directory, filename);
        final File logPath = logfile.getParentFile();

        if (!logPath.isDirectory() && !logPath.mkdirs()) {
            Log.e("Authenticator", "Could not create directory for log files");
        }


        try {
            FileWriter filewriter = new FileWriter(logfile);
            BufferedWriter bw = new BufferedWriter(filewriter);

            ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tagProgressbar.setMax(MainActivity.getTags().size());
                    tagProgressbar.setVisibility(View.VISIBLE);
                    tagProgressbar.setProgress(0);
                }
            });

            int i = 0;
            for (TagData tag : MainActivity.getTags()) {
                final int progress = i;
                ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tagProgressbar.setProgress(progress);
                    }
                });
                ++i;

                StringBuilder sb = new StringBuilder();
                sb.append("{\"tag:");
                sb.append(tag.getTagName() + ", ");


                sb.append("\"" + AppConstants.LOG_KEY_TIMESTAMP + "\": ");
                sb.append("\"" + tag.getTimestamp() + "\"");
                sb.append(" ,");

                sb.append("\"" + AppConstants.LOG_KEY_DATE + "\": ");
                sb.append("\"" + tag.getTimestamp() + "\"");
                sb.append(" }, \n");

                bw.write(sb.toString());
            }
            bw.flush();
            bw.close();

            ExportFragmentDialog.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tagProgressbar.setVisibility(View.GONE);
                        }
                    }, 1000);

                }
            });

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("*/*");

            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "Authenticator data export");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));


            Log.i("Authenticator", "export finished!");
        } catch (IOException ioe) {
            Log.e("Authenticator", "IOException while writing Logfile");
        }
    }

    private String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
        return format.format(date);
    }

    /* --------------------- Confirmation Dialog --------------------- */
    public void showConfirmDeleteDataDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                deleteData();
                            }
                        }).start();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(ExportFragmentDialog.this.getActivity());
        builder.setMessage(getContext().getResources().getString(R.string.delete_data_dialog_text))
                .setTitle(getContext().getResources().getString(R.string.delete_data_dialog_title))
                .setPositiveButton(getContext().getResources().getString(R.string.delete), dialogClickListener)
                .setNegativeButton(getContext().getResources().getString(R.string.cancel), dialogClickListener)
                .show();
    }
}

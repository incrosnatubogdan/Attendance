package att.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


import DBHelper.TimetableSlot;
import Helper.HelperMethods;

public class MainActivity extends ActivityBaseClass
{
    TimetableSlot timetableForDay;
    Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListView();

    }
    void setListView()
    {
        String[] arr=getResources().getStringArray(R.array.listview_main_facilitator);

        ListAdapter adapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arr);

        ListView listView=(ListView)findViewById(R.id.listViewMain);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        String itemName = String.valueOf(parent.getItemAtPosition(position));
                        changeIntent(position, itemName);
                    }
                }
        );
    }



    void changeIntent(int pos,String itemName)
    {
        if(pos==0) // start taking attendance now
        {
            new GetTimetableForNow().execute();
        }
        if(pos==1) // view reports
        {
            //setRecurringAlarm(this);
            Intent newIntent=new Intent(this,FacultyReport.class);
            startActivity(newIntent);
        }
        if(pos==2) // manage courses
        {
            Intent newIntent=new Intent(this,ManageCourses.class);
            startActivity(newIntent);
        }
        else if(pos==4) // manage holiday
        {
            Intent newIntent=new Intent(this,ManageHolidays.class);
            startActivity(newIntent);
        }
        else if(pos==3) // view my calendar
        {
            Intent newIntent=new Intent(this,MyTimetable.class);
            startActivity(newIntent);
        }
        else if(pos==5) // view my schedule
        {
            Intent newIntent=new Intent(this,ManageMySchedule.class);
            startActivity(newIntent);
        }
    }
    private class GetTimetableForNow extends AsyncTask<String, Void, String>
    {
        InputStream is = null;
        String response = "";
        String returnString="";

        @Override
        protected String doInBackground(String... params)
        {
            String url_select = getString(R.string.serviceURL)+"/getTimetableForDayTime.php";

            try
            {
                String keys[]={"user_id","date","time"};

                Calendar dateCalendar = Calendar.getInstance();
                String myFormat = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                String date =sdf.format(dateCalendar.getTime());

                String format = "HH:mm";
                SimpleDateFormat sdfTime = new SimpleDateFormat(format, Locale.US);
                String time = sdfTime.format(dateCalendar.getTime());

                String values[]={HelperMethods.getCurrentLoggedinUser(MainActivity.this),date,time};
                response=HelperMethods.getResponse(url_select,keys,values);

                if(response.equals("null") || response==null)
                {
                    returnString="no data";
                    response="";
                }
                else
                {
                    Gson gson = new Gson();
                    timetableForDay = gson.fromJson(response, TimetableSlot.class);
                    returnString = "ok";
                }
            }
            catch (Exception ex)
            {
                returnString="Exception:"+ex.toString();
            }
            return returnString;
        }

        protected void onPostExecute(String v)
        {
            super.onPostExecute(v);
            // no exception found on previous call
            if(!v.toLowerCase().contains("exception"))
            {
                // if no data found then
                if(response.isEmpty() || response.equals("null"))
                {
                    Toast.makeText(MainActivity.this,"No courses found for today",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Intent intent=new Intent(MainActivity.this,FillAttendanceByFaculty.class);
                    intent.putExtra(context.getString(R.string.bundleKeyCourseCode),timetableForDay.getCourseCode());

                    String timingStart= HelperMethods.convertToStandardTime(timetableForDay.getStartTime());
                    String timingEnd= HelperMethods.convertToStandardTime(timetableForDay.getEndTime());
                    String time=timingStart + "-" + timingEnd;
                    String date=HelperMethods.convertDateFromSQLToUS(timetableForDay.getDate());

                    intent.putExtra(context.getString(R.string.bundleKeyAttendanceId),timetableForDay.getId());
                    intent.putExtra(context.getString(R.string.bundleKeyDate),date);
                    intent.putExtra(context.getString(R.string.bundleKeyTimings),time);
                    startActivity(intent);
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(),v,Toast.LENGTH_SHORT).show();
                //relativeLayout.setVisibility(View.INVISIBLE);
            }
        }
    }
}

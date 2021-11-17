package com.example.mysqlimagesuploaderlistview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ItemsActivity extends AppCompatActivity {
    class Teacher {
        private String name, description, imageURL;
        public Teacher (String name, String description, String imageURL){
            this.name = name;
            this.description = description;
            this.imageURL = imageURL;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getImageURL() {
            return imageURL;
        }
    }

    /*
    Our custom adapter class
    */

    public class ListViewAdapter extends BaseAdapter {
        Context c;
        ArrayList<Teacher> teachers;

        public ListViewAdapter(Context c, ArrayList<Teacher> teachers) {
            this.c = c;
            this.teachers = teachers;
        }

        @Override
        public int getCount() { return teachers.size(); }

        @Override
        public Object getItem(int i) { return teachers.get(i); }

        @Override
        public long getItemId(int i) { return i; }

        @Override
        public View getView (int i, View view, ViewGroup viewGroup) {

            if (view == null){
                view = LayoutInflater.from(c).inflate(R.layout.row_model, viewGroup, false);
            }

            TextView txtName = view.findViewById(R.id.nameTextView);
            TextView txtDescription = view.findViewById(R.id.descriptionTextView);
            ImageView teacherImageView = view.findViewById(R.id.imageView);

            final Teacher teacher = (Teacher) this.getItem(i);

            txtName.setText(teacher.getName());
            txtDescription.setText(teacher.getDescription());

            if (teacher.getImageURL() != null && teacher.getImageURL().length() > 0)
            {
                Picasso.get().load(teacher.getImageURL()).placeholder(R.drawable.imgplaceholder).into(teacherImageView);
            }
            else {
                Toast.makeText(c, "Empty image URL", Toast.LENGTH_LONG).show();
                Picasso.get().load(R.drawable.imgplaceholder).into(teacherImageView);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(c, teacher.getName(), Toast.LENGTH_SHORT).show();
                }
            });

            return view;
        }
    }

    /*
    Our HTTP Client
    */

    public class DataRetriever{
        private static final String PHP_MYSQL_SITE_URL="http://aquagauge.co.ke/spiritualteachers/index.php";
        //INSTANCE FIELDS
        private final Context c;
        private ListViewAdapter adapter;

        public DataRetriever(Context c) { this.c = c; }

        /*
        RETRIEVE/SELECT/REFRESH
        */

        public void retrieve (final ListView gv, final ProgressBar myProgressBar)
        {
            final ArrayList<Teacher> teachers = new ArrayList<>();

            myProgressBar.setIndeterminate(true);
            myProgressBar.setVisibility(View.VISIBLE);

            AndroidNetworking.get(PHP_MYSQL_SITE_URL)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response) {
                            JSONObject jo;
                            Teacher teacher;
                            try {
                                for (int i=0; i< response.length(); i++)
                                {
                                    jo = response.getJSONObject(i);
                                    int id = jo.getInt("id");
                                    String name = jo.getString("teacher_name");
                                    String description = jo.getString("teacher_description");
                                    String imageURL = jo.getString("teacher_image_url");

                                    teacher = new Teacher(name,description, imageURL);
                                    teachers.add(teacher);
                                }

                                adapter = new ListViewAdapter(c, teachers);
                                gv.setAdapter(adapter);
                                myProgressBar.setVisibility(View.GONE);

                            } catch (JSONException e)
                            {
                                myProgressBar.setVisibility(View.GONE);
                                Toast.makeText(c, "GOOD RESPONSE BUT JAVA CAN'T PARSE JSON IT RECEIVED" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onError(ANError anError) {
                            anError.printStackTrace();
                            myProgressBar.setVisibility(View.GONE);
                            Toast.makeText(c, "UNSUCCESSFUL: ERROR IS: " + anError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        ListView myGridView = findViewById(R.id.myListView);
        ProgressBar myDataLoaderProgressBar = findViewById(R.id.myDataLoaderProgressBar);

        new DataRetriever(ItemsActivity.this).retrieve(myGridView, myDataLoaderProgressBar);
    }
}
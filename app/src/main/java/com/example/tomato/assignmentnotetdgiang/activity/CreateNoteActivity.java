package com.example.tomato.assignmentnotetdgiang.activity;


import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.example.tomato.assignmentnotetdgiang.R;
import com.example.tomato.assignmentnotetdgiang.database.DBManager;
import com.example.tomato.assignmentnotetdgiang.model.Note;
import com.example.tomato.assignmentnotetdgiang.utils.AlarmReceiver;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CreateNoteActivity extends AppCompatActivity implements
        View.OnClickListener {

    final int REQUEST_CODE_GALLERY = 999;
    final int REQUEST_CODE_CAMERA = 888;

    ImageView ivNote = null;
    TextView tvTime, tvDate, tvTimeNow;
    EditText edtTitle, edtContent;
    ImageButton ibtnAlarm, ibtnDate, ibtnTime;
    LinearLayout llDate, llTime, llCreateNote;
    Calendar calendar;
    private Calendar mCalendar;
    private int mYear, mMonth, mHour, mMinute, mDay;
    private String mTime, mDate, mTimeNow;
    String mColor = "#F5F5F5";


    DBManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);


        initView();
        initEvent();
        initData();
        db = new DBManager(this);
        mCalendar = Calendar.getInstance();


    }

    private void initData() {
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        mTimeNow = simpleDateFormat.format(calendar.getTime());
        tvTimeNow.setText(mTimeNow.toString());
    }


    private void initEvent() {
        ibtnAlarm.setOnClickListener(this);
        ibtnTime.setOnClickListener(this);
        ibtnDate.setOnClickListener(this);
    }

    private void initView() {
        edtContent = findViewById(R.id.edt_content);
        edtTitle = findViewById(R.id.edt_title);
        ibtnAlarm = findViewById(R.id.ibtn_alarm);
        ibtnDate = findViewById(R.id.ibtn_date);
        ibtnTime = findViewById(R.id.ibtn_time);
        llDate = findViewById(R.id.ll_date);
        llTime = findViewById(R.id.ll_time);
        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);
        tvTimeNow = findViewById(R.id.tv_time_now);

        ivNote = findViewById(R.id.iv_note);
        //  ivNote.setImageResource(R.drawable.ic_imageview);

        llCreateNote = findViewById(R.id.ll_create_note);
    }

    // On clicking Time picker
    private void setTime() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                calendar.set(0, 0, 0, hourOfDay, minute);

                mHour = hourOfDay;
                mMinute = minute;

                mTime = simpleDateFormat.format(calendar.getTime());
                tvTime.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, hour, minute, true);
        tpd.show();
    }

    // On clicking Date picker
    private void setDate() {
        final Calendar calendar = Calendar.getInstance();
        int d = calendar.get(Calendar.DATE);
        int m = calendar.get(Calendar.MONTH);
        int y = calendar.get(Calendar.YEAR);
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // các thông số ở trên đã trả về các tham số ngày tháng năm
                calendar.set(year, month, dayOfMonth);    // phải có câu lệnh này không thì câu lệnh phía dưới mặc định trả về thời gian hiện tại
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

                month++;
                mDay = dayOfMonth;
                mMonth = month;
                mYear = year;

                mDate = simpleDateFormat.format(calendar.getTime());
                tvDate.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, y, m, d);
        dpd.show();
    }

    private Calendar convertStringToDateTime(String datetime) {
        Calendar calendar = null;
        Date date = null;
        calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            date = formatter.parse(datetime);
            calendar.setTime(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public void saveNote() {

        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        byte[] image = null;

        if (ivNote.getDrawable() != null) {       // kiểm tra đã chọn ảnh chưa
            image = imageViewToByte(ivNote);
        }

        // if date time != null --> set Alarm
        if (mDate != null && mTime != null) {

            Note note = new Note(title, content, mDate, mTime, image, mColor, mTimeNow);
          //  db.insertData(note);
            int ID = db.addNote(note);
            // tự thêm

            // Set up calender for creating the notification
            mCalendar.set(Calendar.MONTH, --mMonth);
            mCalendar.set(Calendar.YEAR, mYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
            mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
            mCalendar.set(Calendar.MINUTE, mMinute);
            mCalendar.set(Calendar.SECOND, 0);


            // Create a new notification
            new AlarmReceiver().setAlarm(getApplicationContext(), mCalendar, ID);

            // Create toast to confirm new reminder
            Toast.makeText(getApplicationContext(), "Saved",
                    Toast.LENGTH_SHORT).show();
        } else {

            // no alarm
            Note note = new Note(title, content, mDate, mTime, image, mColor, mTimeNow);
            db.insertData(note);
          //  int ID = db.addNote(note);
        }

        onBackPressed();
    }

    private void dialogColor() {
        final Dialog dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);       // không dùng thanh title
        dialog.setTitle("Color");
        dialog.setContentView(R.layout.dialog_color_custom);
        //dialog.setCanceledOnTouchOutside(false);        // không bị thoát khi nhấn ra bên ngoài

        // ánh xạ
        ImageButton ibtnWhite = dialog.findViewById(R.id.ibtn_color_white);
        ImageButton ibtnBlue = dialog.findViewById(R.id.ibtn_color_blue);
        ImageButton ibtnYellow = dialog.findViewById(R.id.ibtn_color_yellow);
        ImageButton ibtnOrange = dialog.findViewById(R.id.ibtn_color_orange);

        ibtnWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mColor = "#F5F5F5";
                llCreateNote.setBackgroundColor(Color.parseColor("#F5F5F5"));
                dialog.dismiss();
            }
        });

        ibtnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mColor = "#81D4FA";

                llCreateNote.setBackgroundColor(Color.parseColor("#81D4FA"));
                //  ivNote.setColorFilter(Color.parseColor("#81D4FA"));
                dialog.dismiss();
            }
        });
        ibtnYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mColor = "#FFEB3B";

                llCreateNote.setBackgroundColor(Color.parseColor("#FFEB3B"));
                dialog.dismiss();
            }
        });
        ibtnOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mColor = "#FFA000";

                llCreateNote.setBackgroundColor(Color.parseColor("#FFA000"));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void dialogImage() {
        final Dialog dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);       // không dùng thanh title
        dialog.setTitle("Image");
        dialog.setContentView(R.layout.dialog_image_custom);
        //dialog.setCanceledOnTouchOutside(false);        // không bị thoát khi nhấn ra bên ngoài

        // ánh xạ
        ImageButton ibtnCamera = dialog.findViewById(R.id.ibtn_camera);
        ImageButton ibtnPicture = dialog.findViewById(R.id.ibtn_picture);

        ibtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CreateNoteActivity.this, "camera", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);

                dialog.dismiss();
            }
        });

        ibtnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CreateNoteActivity.this, "picture", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static byte[] imageViewToByte(ImageView image) {

        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivNote.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ivNote.setImageBitmap(bitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            } else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // On clicking the back arrow
            // Discard any changes
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save_note_menu:
                saveNote();
                return true;
            case R.id.image_menu:
                dialogImage();
                return true;
            case R.id.color_menu:
                dialogColor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibtn_alarm:
                break;
            case R.id.ibtn_date:
                setDate();
                break;
            case R.id.ibtn_time:
                setTime();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

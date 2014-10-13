package steven.example.com.fsmtoqapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.gmail.yuyang226.flickrj.sample.android.FlickrHelper;
import com.gmail.yuyang226.flickrj.sample.android.FlickrjActivity;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.photos.SearchParameters;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;


public class Drawing extends Activity {
    private String[] options = {"smallBrush", "mediumBrush", "largeBrush", "eraser", "pencil", "brush"};
    File fileUri;
    Boolean uploaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        ImageButton b;
        b = (ImageButton) findViewById(R.id.colorButton);
        b.setColorFilter(Color.RED);

        for (String option : options) {
            int id = getResources().getIdentifier(option + "Button", "id", getPackageName());
            b = (ImageButton) findViewById(id);
            if (option.equals("brush") || option.equals("smallBrush")) {
                b.setColorFilter(Color.BLACK);
            }
            else{
                b.setColorFilter(Color.WHITE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void eraserOnClick(View v) {
        ((ImageButton) v).setColorFilter(0xFF000000);
        DrawingView view = (DrawingView) findViewById(R.id.drawing);
        view.setColor(0xffffffff);
        view.setStrokeWidth(30f);
        ImageButton b;
        for (String option : options) {
            int id = getResources().getIdentifier(option + "Button", "id", getPackageName());
            b = (ImageButton) findViewById(id);
            if (b != v) {
                b.setColorFilter(0xFFFFFFFF);
            }
        }
    }

    public void pencilOnClick(View v) {
        ((ImageButton) v).setColorFilter(0xFF000000);
        DrawingView view = (DrawingView) findViewById(R.id.drawing);
        view.setStrokeWidth(2f);
        if(view.getColor() == Color.WHITE){
            view.setColor(view.lastColor());
        }
        ImageButton b;
        for (String option : options) {
            int id = getResources().getIdentifier(option + "Button", "id", getPackageName());
            b = (ImageButton) findViewById(id);
            if (b != v) {
                b.setColorFilter(0xFFFFFFFF);
            }
        }
    }

    public void brushOnClick(View v) {
        ((ImageButton) v).setColorFilter(0xFF000000);
        ImageButton smallBrush = (ImageButton) findViewById(R.id.smallBrushButton);
        smallBrush.setColorFilter(0xFF000000);
        DrawingView view = (DrawingView) findViewById(R.id.drawing);
        view.setStrokeWidth(10f);
        if(view.getColor() == Color.WHITE){
            view.setColor(view.lastColor());
        }
        ImageButton b;
        for (String option : options) {
            if(option.equals("smallBrush")){
                continue;
            }
            int id = getResources().getIdentifier(option + "Button", "id", getPackageName());
            b = (ImageButton) findViewById(id);
            if (b != v) {
                b.setColorFilter(0xFFFFFFFF);
            }
        }
    }

    public void smallBrushOnClick(View v){
        ImageButton b;
        for (int i = 0; i < 3; i++) {
            String option = options[i];
            int id = getResources().getIdentifier(option + "Button", "id", getPackageName());
            b = (ImageButton) findViewById(id);
            b.setColorFilter(0xFFFFFFFF);
        }
        ((ImageButton) v).setColorFilter(0xFF000000);
        DrawingView view = (DrawingView) findViewById(R.id.drawing);
        view.setStrokeWidth(10f);
        ImageButton button = (ImageButton) findViewById(R.id.brushButton);
        button.setColorFilter(0xFF000000);
        if(view.getColor() == Color.WHITE){
            view.setColor(view.lastColor());
        }
    }

    public void mediumBrushOnClick(View v){
        ImageButton b;
        for (int i = 0; i < 6; i++) {
            String option = options[i];
            int id = getResources().getIdentifier(option + "Button", "id", getPackageName());
            b = (ImageButton) findViewById(id);
            b.setColorFilter(0xFFFFFFFF);
        }
        ((ImageButton) v).setColorFilter(0xFF000000);
        DrawingView view = (DrawingView) findViewById(R.id.drawing);
        view.setStrokeWidth(20f);
        ImageButton button = (ImageButton) findViewById(R.id.brushButton);
        button.setColorFilter(0xFF000000);
        if(view.getColor() == Color.WHITE){
            view.setColor(view.lastColor());
        }
    }

    public void largeBrushOnClick(View v){
        ImageButton b;
        for (int i = 0; i < 6; i++) {
            String option = options[i];
            int id = getResources().getIdentifier(option + "Button", "id", getPackageName());
            b = (ImageButton) findViewById(id);
            b.setColorFilter(0xFFFFFFFF);
        }
        ((ImageButton) v).setColorFilter(0xFF000000);
        DrawingView view = (DrawingView) findViewById(R.id.drawing);
        view.setStrokeWidth(30f);
        ImageButton button = (ImageButton) findViewById(R.id.brushButton);
        button.setColorFilter(0xFF000000);
        if(view.getColor() == Color.WHITE){
            view.setColor(view.lastColor());
        }
    }

    public void colorOnClick(View v){
        Intent intent = new Intent(getApplicationContext(), PickColor.class);
        startActivityForResult(intent, 102);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                ImageButton brush = (ImageButton) findViewById(R.id.brushButton);
                brushOnClick(brush);
                DrawingView view = (DrawingView) findViewById(R.id.drawing);
                String color = data.getStringExtra("color");
                view.setColor(Integer.parseInt(color));
                ImageButton colorButton = (ImageButton) findViewById(R.id.colorButton);
                colorButton.setColorFilter(Integer.parseInt(color));
            }
        }
    };

    public void shareOnClick(View v){
        DrawingView view = (DrawingView) findViewById(R.id.drawing);
        if(view.save()) {
            if (fileUri == null) {
                fileUri = new File("/mnt/sdcard/fsm.png");
            }
            Intent intent = new Intent(getApplicationContext(), FlickrjActivity.class);
            intent.putExtra("flickImagePath", fileUri.getAbsolutePath());
            startActivity(intent);

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        String svr="www.flickr.com";

                        REST rest=new REST();
                        rest.setHost(svr);

                        //initialize Flickr object with key and rest
                        Flickr flickr=new Flickr(FlickrHelper.API_KEY,rest);

                        //initialize SearchParameter object, this object stores the search keyword
                        SearchParameters searchParams=new SearchParameters();
                        searchParams.setSort(SearchParameters.INTERESTINGNESS_DESC);

                        //Create tag keyword array
                        String[] tags=new String[]{"cs160fsm"};
                        searchParams.setTags(tags);

                        //Initialize PhotosInterface object
                        PhotosInterface photosInterface=flickr.getPhotosInterface();
                        //Execute search with entered tags
                        PhotoList photoList=photosInterface.search(searchParams,20,1);

                        //get search result and fetch the photo object and get small square imag's url
                        if(photoList!=null){
                            //Get search result and check the size of photo result
                            Random random = new Random();
                            int seed = random.nextInt(photoList.size());
                            //get photo object
                            Photo photo= photoList.get(seed);
                            InputStream is = photo.getMediumAsStream();
                            final Bitmap bm = BitmapFactory.decodeStream(is);
                            Intent intent = new Intent(getApplicationContext(), ToqActivity.class);
                            intent.putExtra("fetchedImage", Bitmap.createScaledBitmap(bm, 250, 288, false));
                            startActivityForResult(intent,104);
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (FlickrException e) {
                        e.printStackTrace();
                    } catch (IOException e ) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
        }
    }
}

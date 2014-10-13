package steven.example.com.fsmtoqapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.Constants;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.Card;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.NotificationTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManager;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCards;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCardsException;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteResourceStore;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteToqNotification;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.CardImage;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.DeckOfCardsLauncherIcon;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.util.Logger;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.util.ParcelableUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


public class ToqActivity extends Activity {

    private final static String PREFS_FILE= "prefs_file";
    private final static String DECK_OF_CARDS_KEY= "deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY= "deck_of_cards_version_key";

    private HashMap<String, String> people;
    private String[] peopleArray = {"Jack Weinberg", "Joan Baez", "Michael Rossman", "Art Goldberg", "Jackie Goldberg", "Jack Weinberg"};
    private DeckOfCardsManager mDeckOfCardsManager;
    private RemoteDeckOfCards mRemoteDeckOfCards;
    private RemoteResourceStore mRemoteResourceStore;
    private CardImage[] mCardImages;
    private ToqBroadcastReceiver toqReceiver;
    private DeckOfCardsEventListener deckOfCardsEventListener;
    private Bitmap fetchedBM;
    private boolean inZone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toq);
        mDeckOfCardsManager = DeckOfCardsManager.getInstance(getApplicationContext());
        toqReceiver = new ToqBroadcastReceiver();
        people = new HashMap<String, String>();
        deckOfCardsEventListener= new DeckOfCardsEventListenerImpl();

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
//                double latitude = 37.869608;
//                double longitude = -122.259606;
                if(measure(latitude,longitude, 37.86965, -122.25914) && inZone != true){
                    inZone = true;
                    System.out.println(measure(latitude,longitude, 37.86965, -122.25914));
                    sendNotification();
                }
                System.out.println(inZone);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Get update every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Get update every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }

        init();
        setupUI();

        Intent intent = getIntent();
        fetchedBM = (Bitmap) intent.getParcelableExtra("fetchedImage");
        if(fetchedBM != null){
            addRandomFlickrCard(fetchedBM);
        }
    }

    private boolean measure(double lat1, double lon1, double lat2, double lon2){
            double R = 6378.137; // Radius of earth in KM
            double dLat = (lat2 - lat1) * Math.PI / 180.0;
            double dLon = (lon2 - lon1) * Math.PI / 180.0;
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                            Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double d = R * c;
        System.out.println( d*1000);

        if(d * 1000 <= 50) {
                return true;
            }
        else {
            inZone = false;
            return false;
        }
    }

    protected void addRandomFlickrCard(Bitmap bm) {
        ListCard listCard = mRemoteDeckOfCards.getListCard();
        SimpleTextCard simpleTextCard = (SimpleTextCard) listCard.get("card7");
        System.out.println("HERE I AM " + simpleTextCard);
        if(simpleTextCard != null) {
            listCard.remove(simpleTextCard);
            System.out.println("REMOVED");
        }
        simpleTextCard = new SimpleTextCard("card7");
        simpleTextCard.setHeaderText("Art by others");
        CardImage cardImage = new CardImage("card.image.7", bm);
        mRemoteResourceStore.addResource(cardImage);
        simpleTextCard.setCardImage(mRemoteResourceStore, cardImage);

        simpleTextCard.setReceivingEvents(false);
        simpleTextCard.setShowDivider(true);

        listCard.add(simpleTextCard);

        try {
            mDeckOfCardsManager.updateDeckOfCards(mRemoteDeckOfCards, mRemoteResourceStore);
            Intent intent = new Intent(getApplicationContext(), Drawing.class);
            startActivity(intent);
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to Create SimpleTextCard", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @see android.app.Activity#onStart()
     * This is called after onCreate(Bundle) or after onRestart() if the activity has been stopped
     */
    protected void onStart(){
        super.onStart();
        mDeckOfCardsManager.addDeckOfCardsEventListener(deckOfCardsEventListener);
        Log.d(Constants.TAG, "ToqApiDemo.onStart");
        // If not connected, try to connect
        if (!mDeckOfCardsManager.isConnected()){
            try{
                mDeckOfCardsManager.connect();
            }
            catch (RemoteDeckOfCardsException e){
                e.printStackTrace();
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

    private void setupUI() {

        findViewById(R.id.install_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                install();
            }
        });

        findViewById(R.id.uninstall_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uninstall();
            }
        });

    }

    private void sendNotification() {
        String[] message = new String[2];
        Random rand = new Random();
        int personIndex = rand.nextInt(6);
        message[0] = peopleArray[personIndex];
        message[1] = "Open app for instruction.";
        // Create a NotificationTextCard
        NotificationTextCard notificationCard = new NotificationTextCard(System.currentTimeMillis(),
                "Drawing Request!", message);

        // Draw divider between lines of text
        notificationCard.setShowDivider(true);
        // Vibrate to alert user when showing the notification
        notificationCard.setVibeAlert(true);
        // Create a notification with the NotificationTextCard we made
        RemoteToqNotification notification = new RemoteToqNotification(this, notificationCard);

        try {
            // Send the notification
            mDeckOfCardsManager.sendNotification(notification);
            Toast.makeText(this, "Sent Notification", Toast.LENGTH_SHORT).show();
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send Notification", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Installs applet to Toq watch if app is not yet installed
     */
    private void install() {
        boolean isInstalled = true;

        try {
            isInstalled = mDeckOfCardsManager.isInstalled();
        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (!isInstalled) {
            try {
                mDeckOfCardsManager.installDeckOfCards(mRemoteDeckOfCards, mRemoteResourceStore);
            } catch (RemoteDeckOfCardsException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: Cannot install application", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "App is already installed!", Toast.LENGTH_SHORT).show();
        }

        try{
            storeDeckOfCards();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void uninstall() {
        boolean isInstalled = true;

        try {
            isInstalled = mDeckOfCardsManager.isInstalled();
        }
        catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Can't determine if app is installed", Toast.LENGTH_SHORT).show();
        }

        if (isInstalled) {
            try{
                mDeckOfCardsManager.uninstallDeckOfCards();
            }
            catch (RemoteDeckOfCardsException e){
                Toast.makeText(this, getString(R.string.error_uninstalling_deck_of_cards), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.already_uninstalled), Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * Adds a deck of cards to the applet
     */
    private void addSimpleTextCard() {
        ListCard listCard = mRemoteDeckOfCards.getListCard();
        int currSize = listCard.size();

        // Create a SimpleTextCard with 1 + the current number of SimpleTextCards
        SimpleTextCard simpleTextCard = new SimpleTextCard(Integer.toString(currSize+1));

        simpleTextCard.setHeaderText("Header: " + Integer.toString(currSize+1));
        simpleTextCard.setTitleText("Title: " + Integer.toString(currSize+1));
        String[] messages = {"Message: " + Integer.toString(currSize+1)};
        simpleTextCard.setMessageText(messages);
        simpleTextCard.setReceivingEvents(false);
        simpleTextCard.setShowDivider(true);

        listCard.add(simpleTextCard);

        try {
            mDeckOfCardsManager.updateDeckOfCards(mRemoteDeckOfCards);
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to Create SimpleTextCard", Toast.LENGTH_SHORT).show();
        }
    }


    private void removeDeckOfCards() {
        ListCard listCard = mRemoteDeckOfCards.getListCard();
        if (listCard.size() == 0) {
            return;
        }

        listCard.remove(0);

        try {
            mDeckOfCardsManager.updateDeckOfCards(mRemoteDeckOfCards);
        } catch (RemoteDeckOfCardsException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete Card from ListCard", Toast.LENGTH_SHORT).show();
        }

    }

    // Initialise
    private void init(){

        // Create the resource store for icons and images
        mRemoteResourceStore= new RemoteResourceStore();

        DeckOfCardsLauncherIcon whiteIcon = null;
        DeckOfCardsLauncherIcon colorIcon = null;

        // Get the launcher icons
        try{
            whiteIcon= new DeckOfCardsLauncherIcon("white.launcher.icon", getBitmap("bw.png"), DeckOfCardsLauncherIcon.WHITE);
            colorIcon= new DeckOfCardsLauncherIcon("color.launcher.icon", getBitmap("color.png"), DeckOfCardsLauncherIcon.COLOR);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't get launcher icon");
            return;
        }

        mCardImages = new CardImage[6];
        try{
            mCardImages[0]= new CardImage("card.image.1", getBitmap("jack_weinberg_toq.png"));
            mCardImages[1]= new CardImage("card.image.2", getBitmap("joan_baez_toq.png"));
            mCardImages[2]= new CardImage("card.image.3", getBitmap("michael_rossman_toq.png"));
            mCardImages[3]= new CardImage("card.image.4", getBitmap("art_goldberg_toq.png"));
            mCardImages[4]= new CardImage("card.image.5", getBitmap("jackie_goldberg_toq.png"));
            mCardImages[5]= new CardImage("card.image.6", getBitmap("mario_savio_toq.png"));
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't get picture icon");
            return;
        }

        // Try to retrieve a stored deck of cards
        try {
            // If there is no stored deck of cards or it is unusable, then create new and store
            if ((mRemoteDeckOfCards = getStoredDeckOfCards()) == null){
                mRemoteDeckOfCards = createDeckOfCards();
                storeDeckOfCards();
            }
        }
        catch (Throwable th){
            th.printStackTrace();
            mRemoteDeckOfCards = null; // Reset to force recreate
        }
        Logger.d("CARDS ARE NULL: " + (mRemoteDeckOfCards.toString()));
        // Make sure in usable state
//        if (mRemoteDeckOfCards == null){
            mRemoteDeckOfCards = createDeckOfCards();
//        }

        // Set the custom launcher icons, adding them to the resource store
        mRemoteDeckOfCards.setLauncherIcons(mRemoteResourceStore, new DeckOfCardsLauncherIcon[]{whiteIcon, colorIcon});

        // Re-populate the resource store with any card images being used by any of the cards
        for (Iterator<Card> it= mRemoteDeckOfCards.getListCard().iterator(); it.hasNext();){

            String cardImageId= ((SimpleTextCard)it.next()).getCardImageId();

            if ((cardImageId != null) && !mRemoteResourceStore.containsId(cardImageId)){

                if (cardImageId.equals("card.image.1")){
                    mRemoteResourceStore.addResource(mCardImages[0]);
                }

            }
        }
    }

    // Read an image from assets and return as a bitmap
    private Bitmap getBitmap(String fileName) throws Exception{

        try{
            InputStream is= getAssets().open(fileName);
            return BitmapFactory.decodeStream(is);
        }
        catch (Exception e){
            throw new Exception("An error occurred getting the bitmap: " + fileName, e);
        }
    }

    private RemoteDeckOfCards getStoredDeckOfCards() throws Exception{

        if (!isValidDeckOfCards()){
            Log.w(Constants.TAG, "Stored deck of cards not valid for this version of the demo, recreating...");
            return null;
        }

        SharedPreferences prefs= getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        String deckOfCardsStr= prefs.getString(DECK_OF_CARDS_KEY, null);

        if (deckOfCardsStr == null){
            return null;
        }
        else{
            return ParcelableUtil.unmarshall(deckOfCardsStr, RemoteDeckOfCards.CREATOR);
        }

    }

    /**
     * Uses SharedPreferences to store the deck of cards
     * This is mainly used to
     */
    private void storeDeckOfCards() throws Exception{
        // Retrieve and hold the contents of PREFS_FILE, or create one when you retrieve an editor (SharedPreferences.edit())
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        // Create new editor with preferences above
        SharedPreferences.Editor editor = prefs.edit();
        // Store an encoded string of the deck of cards with key DECK_OF_CARDS_KEY
        editor.putString(DECK_OF_CARDS_KEY, ParcelableUtil.marshall(mRemoteDeckOfCards));
        // Store the version code with key DECK_OF_CARDS_VERSION_KEY
        editor.putInt(DECK_OF_CARDS_VERSION_KEY, Constants.VERSION_CODE);
        // Commit these changes
        editor.commit();
    }

    // Check if the stored deck of cards is valid for this version of the demo
    private boolean isValidDeckOfCards(){

        SharedPreferences prefs= getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        // Return 0 if DECK_OF_CARDS_VERSION_KEY isn't found
        int deckOfCardsVersion= prefs.getInt(DECK_OF_CARDS_VERSION_KEY, 0);

        return deckOfCardsVersion >= Constants.VERSION_CODE;
    }

    // Create some cards with example content
    private RemoteDeckOfCards createDeckOfCards(){

        ListCard listCard= new ListCard();

        SimpleTextCard simpleTextCard= new SimpleTextCard("card1");
        simpleTextCard.setHeaderText("Jack Weinberg");
        simpleTextCard.setTitleText("Draw \"FSM\"");
        simpleTextCard.setCardImage(mRemoteResourceStore, mCardImages[0]);
        simpleTextCard.setReceivingEvents(true);
        simpleTextCard.setShowDivider(true);
        listCard.add(simpleTextCard);

        // Card #2
        simpleTextCard= new SimpleTextCard("card2");
        simpleTextCard.setHeaderText("Joan Baez");
        simpleTextCard.setTitleText("Draw a Megaphone");
        simpleTextCard.setCardImage(mRemoteResourceStore, mCardImages[1]);
        simpleTextCard.setReceivingEvents(true);
        simpleTextCard.setShowDivider(true);
        listCard.add(simpleTextCard);

        // Card #3
        simpleTextCard= new SimpleTextCard("card3");
        simpleTextCard.setHeaderText("Michael Rossman");
        simpleTextCard.setTitleText("Draw \"Free Speech\"");
        simpleTextCard.setCardImage(mRemoteResourceStore, mCardImages[2]);
        simpleTextCard.setReceivingEvents(true);
        simpleTextCard.setShowDivider(true);
        listCard.add(simpleTextCard);

        // Card #4
        simpleTextCard= new SimpleTextCard("card4");
        simpleTextCard.setHeaderText("Art Goldberg");
        simpleTextCard.setTitleText("Draw \"Now\"");
        simpleTextCard.setCardImage(mRemoteResourceStore, mCardImages[3]);
        simpleTextCard.setReceivingEvents(true);
        simpleTextCard.setShowDivider(true);
        listCard.add(simpleTextCard);

        // Card #5
        simpleTextCard= new SimpleTextCard("card5");
        simpleTextCard.setHeaderText("Jackie Goldberg");
        simpleTextCard.setTitleText("Draw \"SLATE\"");
        simpleTextCard.setCardImage(mRemoteResourceStore, mCardImages[4]);
        simpleTextCard.setReceivingEvents(true);
        simpleTextCard.setShowDivider(true);
        listCard.add(simpleTextCard);

        // Card #6
        simpleTextCard= new SimpleTextCard("card6");
        simpleTextCard.setHeaderText("Mario Savio");
        simpleTextCard.setTitleText("Express your own view of Free Speech in an drawing");
        simpleTextCard.setCardImage(mRemoteResourceStore, mCardImages[5]);
        simpleTextCard.setReceivingEvents(true);
        simpleTextCard.setShowDivider(true);
        listCard.add(simpleTextCard);

        return new RemoteDeckOfCards(this, listCard);
    }

    private class DeckOfCardsEventListenerImpl implements DeckOfCardsEventListener {

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardOpen(java.lang.String)
         */
        public void onCardOpen(final String cardId){
            runOnUiThread(new Runnable(){
                public void run(){
//                    Toast.makeText(ToqActivity.this, getString(R.string.opened) + cardId, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(ToqActivity.this, Drawing.class);
                    startActivity(i);
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardVisible(java.lang.String)
         */
        public void onCardVisible(final String cardId){
//            runOnUiThread(new Runnable(){
//                public void run(){
//                    Toast.makeText(ToqActivity.this, getString(R.string.opened) + cardId, Toast.LENGTH_SHORT).show();
//
//                }
//
//            });
            return;
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardInvisible(java.lang.String)
         */
        public void onCardInvisible(final String cardId){
//            runOnUiThread(new Runnable(){
//                public void run(){
//                    Toast.makeText(ToqActivity.this, getString(R.string.opened) + cardId, Toast.LENGTH_SHORT).show();
//                }
//            });
            return;
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardClosed(java.lang.String)
         */
        public void onCardClosed(final String cardId){
//            runOnUiThread(new Runnable(){
//                public void run(){
//                    Toast.makeText(ToqActivity.this, getString(R.string.opened) + cardId, Toast.LENGTH_SHORT).show();
//                }
//            });
            return;
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption){
//            runOnUiThread(new Runnable(){
//                public void run(){
//                    Toast.makeText(ToqActivity.this, getString(R.string.opened) + cardId + " [" + menuOption + "]", Toast.LENGTH_SHORT).show();
//                }
//            });
            return;
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption, final String quickReplyOption){
//            runOnUiThread(new Runnable(){
//                public void run(){
//                    Toast.makeText(ToqActivity.this, getString(R.string.opened) + cardId + " [" + menuOption + ":" + quickReplyOption +
//                            "]", Toast.LENGTH_SHORT).show();
//                }
//            });
            return;
        }

    }


}

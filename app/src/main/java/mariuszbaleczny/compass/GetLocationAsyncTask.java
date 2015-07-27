package mariuszbaleczny.compass;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.Locale;

public class GetLocationAsyncTask extends AsyncTask<Location, Void, Address> {

    private Context context;

    public GetLocationAsyncTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected Address doInBackground(Location... params) {
        Location location = params[0];
        Address address = new Address(Locale.getDefault());
        Geocoder geocoder = new Geocoder(this.context, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        return address;
    }
}

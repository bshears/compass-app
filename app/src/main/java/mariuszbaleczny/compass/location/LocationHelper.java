package mariuszbaleczny.compass.location;

import android.location.Location;

import mariuszbaleczny.compass.Utils;

public class LocationHelper {

    private Double latitude;
    private Double longitude;
    private boolean latitudeCorrect;
    private boolean longitudeCorrect;
    private Location location;

    public LocationHelper(Location location) {
        latitude = Double.NaN;
        longitude = Double.NaN;
        longitudeCorrect = false;
        latitudeCorrect = false;
        this.location = location;
    }

    public Location getLocation() {
        if (isCorrect() && location != null) {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            return location;
        }
        return null;
    }

    public boolean isCorrect() {
        return latitudeCorrect && longitudeCorrect;
    }

    public boolean setLatitude(Double latitude) {
        if (latitude != null) {
            latitudeCorrect = Utils.isCoordinateInRange(latitude, true);
            this.latitude = latitudeCorrect ? latitude : Double.NaN;
        }
        return latitudeCorrect;
    }

    public boolean setLongitude(Double longitude) {
        if (longitude != null) {
            longitudeCorrect = Utils.isCoordinateInRange(longitude, false);
            this.longitude = longitudeCorrect ? longitude : Double.NaN;
        }
        return longitudeCorrect;
    }

}

package pl.marchuck.eeeeeeee;

import com.example.SQLiteField;
import com.example.SQLitePrimaryKey;
import com.example.SQLiteTable;

/**
 * Project "EEEEeeee"
 * <p/>
 * Created by Lukasz Marczak
 * on 14.10.16.
 */
@SQLiteTable("plain_geo_place")
public class PlainGeoPlace {

    @SQLiteField("id")
    @SQLitePrimaryKey
    private String uuid;

    @SQLiteField(value = "latitude", type = "DOUBLE")
    private double latitude;

    @SQLiteField(value = "longitude", type = "DOUBLE")
    private double longitude;

    @SQLiteField("description")
    private String description;

    @SQLiteField(value = "favourite", type = "INT")
    private int favourite;

    @SQLiteField(value = "label")
    private String label;

    @SQLiteField(value = "timestamp")
    private String timestamp;

    public PlainGeoPlace() {

    }

    public PlainGeoPlace(String uuid, double latitude, double longitude, String description, int favourite) {
        this.uuid = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.favourite = favourite;
    }
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFavourite() {
        return favourite;
    }

    public void setFavourite(int favourite) {
        this.favourite = favourite;
    }
}


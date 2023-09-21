import java.net.*;
import java.io.*;

public class WeatherObject {
    private String id;
    private String name;
    private String state;
    private String time_zone;
    private double lat;
    private double lon;
    private String local_date_time;
    private String local_date_time_full;
    private double air_temp;
    private double apparent_t;
    private String cloud;
    private double dewpt;
    private double press;
    private int rel_hum;
    private String wind_dir;
    private int wind_spd_kmh;
    private int wind_spd_kt;

    public WeatherObject() {
        this.id = null;
        this.name = null;
        this.state = null;
        this.time_zone = null;
        this.lat = 0;
        this.lon = 0;
        this.local_date_time = null;
        this.local_date_time_full = null;
        this.air_temp = 0;
        this.apparent_t = 0;
        this.cloud = null;
        this.dewpt = 0;
        this.press = 0;
        this.rel_hum = 0;
        this.wind_dir = null;
        this.wind_spd_kmh = 0;
        this.wind_spd_kt = 0;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTime_zone(String time_zone) {
        this.time_zone = time_zone;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setLocal_date_time(String local_date_time) {
        this.local_date_time = local_date_time;
    }

    public void setLocal_date_time_full(String local_date_time_full) {
        this.local_date_time_full = local_date_time_full;
    }

    public void setAir_temp(double air_temp) {
        this.air_temp = air_temp;
    }

    public void setApparent_t(double apparent_t) {
        this.apparent_t = apparent_t;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public void setDewpt(double dewpt) {
        this.dewpt = dewpt;
    }

    public void setPress(double press) {
        this.press = press;
    }

    public void setRel_hum(int rel_hum) {
        this.rel_hum = rel_hum;
    }

    public void setWind_dir(String wind_dir) {
        this.wind_dir = wind_dir;
    }

    public void setWind_spd_kmh(int wind_spd_kmh) {
        this.wind_spd_kmh = wind_spd_kmh;
    }

    public void setWind_spd_kt(int wind_spd_kt) {
        this.wind_spd_kt = wind_spd_kt;
    }

    public String getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getState() {
        return this.state;
    }
    
    public String getTime_zone() {
        return this.time_zone;
    }
    
    public double getLat() {
        return this.lat;
    }
    
    public double getLon() {
        return this.lon;
    }
    
    public String getLocal_date_time() {
        return this.local_date_time;
    }
    
    public String getLocal_date_time_full() {
        return this.local_date_time_full;
    }
    
    public double getAir_temp() {
        return this.air_temp;
    }
    
    public double getApparent_t() {
        return this.apparent_t;
    }
    
    public String getCloud() {
        return this.cloud;
    }
    
    public double getDewpt() {
        return this.dewpt;
    }
    
    public double getPress() {
        return this.press;
    }
    
    public int getRel_hum() {
        return this.rel_hum;
    }
    
    public String getWind_dir() {
        return this.wind_dir;
    }
    
    public int getWind_spd_kmh() {
        return this.wind_spd_kmh;
    }
    
    public int getWind_spd_kt() {
        return this.wind_spd_kt;
    }

    @Override
    public String toString() {
        return "WeatherObject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", timeZone='" + time_zone + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", localDateTime='" + local_date_time + '\'' +
                ", localDateTimeFull='" + local_date_time_full + '\'' +
                ", airTemp=" + air_temp +
                ", apparentT=" + apparent_t +
                ", cloud='" + cloud + '\'' +
                ", dewpt=" + dewpt +
                ", press=" + press +
                ", relHum=" + rel_hum +
                ", windDir='" + wind_dir + '\'' +
                ", windSpdKmh=" + wind_spd_kmh +
                ", windSpdKt=" + wind_spd_kt +
                '}';
    }
}

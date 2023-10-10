package lib;

/**
 * The WeatherObject class represents and stores weather data 
 * for a specific location at that given time.
 * 
 */
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
    private int updateValue;
    private int stationId;

    /**
     * Constructs a WeatherObject with intialised values for its properties.
     * 
     */
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
        this.updateValue = 0;
        this.stationId = -1;
    }

    /**
     * Sets the unique identifier for the weather data.
     *
     * @param id The new unique identifier to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets name of location for the weather data.
     *
     * @param id The new name of location to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the location state for the weather data.
     *
     * @param id The new location state to set.
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Sets the time zone for the weather data.
     *
     * @param id The new time zone to set.
     */
    public void setTime_zone(String time_zone) {
        this.time_zone = time_zone;
    }

    /**
     * Sets the latitute for the weather data.
     *
     * @param id The new latitute to set.
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * Sets the longitute for the weather data.
     *
     * @param id The new longitute to set.
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * Sets the local date time for the weather data.
     *
     * @param id The new local date time to set.
     */
    public void setLocal_date_time(String local_date_time) {
        this.local_date_time = local_date_time;
    }

    /**
     * Sets the local date time in full form for the weather data.
     *
     * @param id The new local date time in full form to set.
     */
    public void setLocal_date_time_full(String local_date_time_full) {
        this.local_date_time_full = local_date_time_full;
    }

    /**
     * Sets the air temperature for the weather data.
     *
     * @param id The new temperature to set.
     */
    public void setAir_temp(double air_temp) {
        this.air_temp = air_temp;
    }

    /**
     * Sets the apparent temperature for the weather data.
     *
     * @param id The new apparent temperature to set.
     */
    public void setApparent_t(double apparent_t) {
        this.apparent_t = apparent_t;
    }

    /**
     * Sets the amount of cloud for the weather data.
     *
     * @param id The new amount of cloud to set.
     */
    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    /**
     * Sets the dewpoint for the weather data.
     *
     * @param id The new dewpoint to set.
     */
    public void setDewpt(double dewpt) {
        this.dewpt = dewpt;
    }

    /**
     * Sets the pressure for the weather data.
     *
     * @param id The new pressure to set.
     */
    public void setPress(double press) {
        this.press = press;
    }

    /**
     * Sets the relative humidity for the weather data.
     *
     * @param id The new relative humidity to set.
     */
    public void setRel_hum(int rel_hum) {
        this.rel_hum = rel_hum;
    }

    /**
     * Sets the wind direction for the weather data.
     *
     * @param id The new wind direction to set.
     */
    public void setWind_dir(String wind_dir) {
        this.wind_dir = wind_dir;
    }

    /**
     * Sets the wind speed in kilometers per hour for the weather data.
     *
     * @param id The new wind speed(kmh) to set.
     */
    public void setWind_spd_kmh(int wind_spd_kmh) {
        this.wind_spd_kmh = wind_spd_kmh;
    }

    /**
     * Sets the wind speed in knots for the weather data.
     *
     * @param id The new wind speed(kt) to set.
     */
    public void setWind_spd_kt(int wind_spd_kt) {
        this.wind_spd_kt = wind_spd_kt;
    }

    /**
     * Gets the unique identifier for the weather data.
     *
     * @return The unique identifier.
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Gets the location name for the weather data.
     *
     * @return The location name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Gets the location state for the weather data.
     *
     * @return The location state.
     */
    public String getState() {
        return this.state;
    }
    
    /**
     * Gets the time zone for the weather data.
     *
     * @return The time zone.
     */
    public String getTime_zone() {
        return this.time_zone;
    }
    
    /**
     * Gets the latitude for the weather data.
     *
     * @return The latitude.
     */
    public double getLat() {
        return this.lat;
    }
    
    /**
     * Gets the longitute for the weather data.
     *
     * @return The longitute.
     */
    public double getLon() {
        return this.lon;
    }
    
    /**
     * Gets the local date time for the weather data.
     *
     * @return The local date time.
     */
    public String getLocal_date_time() {
        return this.local_date_time;
    }
    
    /**
     * Gets the local date time in full form for the weather data.
     *
     * @return The local date time in full form.
     */
    public String getLocal_date_time_full() {
        return this.local_date_time_full;
    }
    
    /**
     * Gets the air temperature for the weather data.
     *
     * @return The air temperature.
     */
    public double getAir_temp() {
        return this.air_temp;
    }
    
    /**
     * Gets the apparent temperature for the weather data.
     *
     * @return The apparent temperature.
     */
    public double getApparent_t() {
        return this.apparent_t;
    }
    
    /**
     * Gets the amount of cloud for the weather data.
     *
     * @return The amount of cloud.
     */
    public String getCloud() {
        return this.cloud;
    }
    
    /**
     * Gets the dewpoint for the weather data.
     *
     * @return The dewpoint.
     */
    public double getDewpt() {
        return this.dewpt;
    }
    
    /**
     * Gets the pressure for the weather data.
     *
     * @return The pressure.
     */
    public double getPress() {
        return this.press;
    }
    
    /**
     * Gets the relative humidity for the weather data.
     *
     * @return The relative humidity.
     */
    public int getRel_hum() {
        return this.rel_hum;
    }
    
    /**
     * Gets the wind direction for the weather data.
     *
     * @return The wind direction.
     */
    public String getWind_dir() {
        return this.wind_dir;
    }
    
    /**
     * Gets the wind speed in kilometers per hour for the weather data.
     *
     * @return The wind speed(kmp).
     */
    public int getWind_spd_kmh() {
        return this.wind_spd_kmh;
    }
    
    /**
     * Gets the wind speed in knots for the weather data.
     *
     * @return The wind speed(kt).
     */
    public int getWind_spd_kt() {
        return this.wind_spd_kt;
    }

    /**
     * Increments the update value if new weather data has been added.
     *
     * @param id The amount of times this weather has been left unchanged.
     */
    public void setUpdateValue(int updateValue) {
        this.updateValue = updateValue;
    }

    /**
     * Gets the update value holding how long since last update.
     *
     * @return The amount of times the weather data has been left unchanged.
     */
    public int getUpdateValue() {
        return this.updateValue;
    }

    /**
     * Sets the stationId for the weather data.
     *
     * @param id The new stationId to set.
     */
    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    /**
     * Gets the stationId for the weather data.
     *
     * @return The stationId.
     */
    public int getStationId() {
        return this.stationId;
    }

    /**
     * Returns a string representation of the WeatherObject.
     *
     * @return A string containing a formatted representation of the object's properties.
     */
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
                ", updateValue=" + updateValue +
                ", stationId=" + stationId +
                '}';
    }
}

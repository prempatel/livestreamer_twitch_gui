package twitchAPI;

import com.google.gson.JsonObject;

/**
 * Sets Variables from Twitch.tv JsonObject
 * 
 * @author Niklas 27.09.2014
 * 
 */
public class Twitch_Json {
    private boolean online;
    private String id;
    private String category;
    private String title;
    private String created_at;
    private String updated_at;
    private String meta_game;
    private String stream_type;
    private String name;
    private String broadcaster;
    private String username;
    private String status;
    private String channel_url;
    private String subcategory_title;
    private String screen_cap_url_large;
    private String screen_cap_url_small;
    private String screen_cap_url_medium;
    private String screen_cap_url_huge;
    private String category_title;
    private int current_viewers;
    private int views_count;

    public void load(JsonObject job) {
	setTitle(job.get("channel").getAsJsonObject().get("status")
		.getAsString());
	setMeta_game(job.get("game").getAsString());
	setCurrent_viewers(job.get("viewers").getAsInt());
	setScreen_cap_url_large(job.get("preview").getAsJsonObject()
		.get("large").getAsString());
	setScreen_cap_url_small(job.get("preview").getAsJsonObject()
		.get("small").getAsString());
	setScreen_cap_url_medium(job.get("preview").getAsJsonObject()
		.get("medium").getAsString());
	setCreated_At(job.get("created_at").getAsString());
	setUpdated_At(job.get("channel").getAsJsonObject().get("updated_at")
		.getAsString());
    }

    public boolean isOnline() {
	return this.online;
    }

    public void setOnline(boolean online) {
	this.online = online;
    }

    public String getId() {
	return this.id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getCategory() {
	return this.category;
    }

    public void setCategory(String category) {
	this.category = category;
    }

    public String getTitle() {
	return this.title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getCreated_At() {
	return this.created_at;
    }

    public void setCreated_At(String created_at) {
	this.created_at = created_at;
    }

    public String getUpdated_At() {
	return this.updated_at;
    }

    public void setUpdated_At(String updated_at) {
	this.updated_at = updated_at;
    }

    public String getMeta_game() {
	return this.meta_game;
    }

    public void setMeta_game(String meta_game) {
	this.meta_game = meta_game;
    }

    public String getStream_type() {
	return this.stream_type;
    }

    public void setStream_type(String stream_type) {
	this.stream_type = stream_type;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getBroadcaster() {
	return this.broadcaster;
    }

    public void setBroadcaster(String broadcaster) {
	this.broadcaster = broadcaster;
    }

    public String getUsername() {
	return this.username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getStatus() {
	return this.status;
    }

    public void setStatus(String status) {
	this.status = status;
    }

    public String getChannel_url() {
	return this.channel_url;
    }

    public void setChannel_url(String channel_url) {
	this.channel_url = channel_url;
    }

    public String getSubcategory_title() {
	return this.subcategory_title;
    }

    public void setSubcategory_title(String subcategory_title) {
	this.subcategory_title = subcategory_title;
    }

    public String getScreen_cap_url_large() {
	return this.screen_cap_url_large;
    }

    public void setScreen_cap_url_large(String screen_cap_url_large) {
	this.screen_cap_url_large = screen_cap_url_large;
    }

    public String getScreen_cap_url_small() {
	return this.screen_cap_url_small;
    }

    public void setScreen_cap_url_small(String screen_cap_url_small) {
	this.screen_cap_url_small = screen_cap_url_small;
    }

    public String getScreen_cap_url_medium() {
	return this.screen_cap_url_medium;
    }

    public void setScreen_cap_url_medium(String screen_cap_url_medium) {
	this.screen_cap_url_medium = screen_cap_url_medium;
    }

    public String getScreen_cap_url_huge() {
	return this.screen_cap_url_huge;
    }

    public void setScreen_cap_url_huge(String screen_cap_url_huge) {
	this.screen_cap_url_huge = screen_cap_url_huge;
    }

    public String getCategory_title() {
	return this.category_title;
    }

    public void setCategory_title(String category_title) {
	this.category_title = category_title;
    }

    public int getViews_count() {
	return this.views_count;
    }

    public void setViews_count(int views_count) {
	this.views_count = views_count;
    }

    /**
     * @return the current_viewers
     */
    public int getCurrent_viewers() {
	return current_viewers;
    }

    /**
     * @param current_viewers
     *            the current_viewers to set
     */
    public void setCurrent_viewers(int current_viewers) {
	this.current_viewers = current_viewers;
    }
}

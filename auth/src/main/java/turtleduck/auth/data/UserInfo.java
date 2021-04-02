package turtleduck.auth.data;

import java.util.List;

import io.vertx.core.json.JsonObject;

public class UserInfo {
	public Identity id;
	public String provider;
	public String sub;
	public String name;
	public String nickname;
	public String email;
	public boolean email_verified;
	public String website;
	public String profile;
	public String picture;
	public List<String> groups;
	
	public static UserInfo fromJson(JsonObject obj) {
		return obj.mapTo(UserInfo.class);
	}
	
	public JsonObject toJson() {
		return JsonObject.mapFrom(this);
	}
	
	public String toString() {
		return toJson().encode();
	}
}

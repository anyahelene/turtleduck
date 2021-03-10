package turtleduck.server.data;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class AuthOptions {
	public String provider_id;
	public String login_path;
	public String name;
	public String oauth_clientid;
	public String oauth_secret;
	public String oauth_site;
	public String authorization_endpoint = null;
	public String token_endpoint = null;
	public String end_session_endpoint = null;
	public String revocation_endpoint = null;
	public String userinfo_endpoint = null;
	public String userinfo_nick = "username";
	public String jwks_uri = null;
	public String scope = "openid email";
	public boolean oauth_discover = true;
	public String public_key;
	public boolean enabled;
	public String prompt;
	
	public static AuthOptions fromJson(JsonObject obj) {
		return obj.mapTo(AuthOptions.class);
	}
	
	public JsonObject toJson() {
		return JsonObject.mapFrom(this);
	}
	
	public String toString() {
		return toJson().encode();
	}
}

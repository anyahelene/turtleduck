package turtleduck.server.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import io.vertx.core.json.JsonObject;

public class Identity {
	@JsonIgnore
	private static final Map<Long, Identity> OBJECTS = new HashMap<>();

	public final int domain;
	public final int id;

	public Identity(int domain, int id) {
		super();
		this.domain = domain;
		this.id = id;
	}

	public static Identity get(int domain, int id) {
		long longid = domain;
		longid <<= 32;
		longid |= id;
		Identity obj = OBJECTS.get(longid);
		if (obj == null) {
			obj = new Identity(domain, id);
			OBJECTS.put(longid, obj);
		}
		return obj;
	}

	@JsonCreator
	public static Identity get(long longId) {
		Identity obj = OBJECTS.get(longId);
		if (obj == null) {
			int id = (int) (longId & 0xffffffff);
			int domain = (int) (longId >>> 32);
			obj = new Identity(domain, id);
			OBJECTS.put(longId, obj);
		}
		return obj;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + domain;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Identity)) {
			return false;
		}
		Identity other = (Identity) obj;
		if (domain != other.domain) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id(").append(domain).append(",").append(id).append(")");
		return builder.toString();
	}

	@JsonValue
	public long longValue() {
		long longid = domain;
		longid <<= 32;
		longid |= id;
		return longid;
	}

	public static Identity fromJson(JsonObject obj) {
		return obj.mapTo(Identity.class);
	}

	public JsonObject toJson() {
		return JsonObject.mapFrom(this);
	}
}

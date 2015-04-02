package io.github.tylerstonge.meetingroom;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Room implements ConfigurationSerializable {

	private String id;
	private String name;
	private String owner;
	
	public Room(String id, String name, String owner) {
		this.id = id;
		this.name = name;
		this.owner = owner;
	}
	
	public Room(Map<String, Object> map) {
		this.id = (String) map.get("id");
		this.name = (String) map.get("name");
		this.owner = (String) map.get("owner");
	}
	
	public static Room deserialize(Map<String, Object> map) {
		return new Room(map);
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("name", name);
		map.put("owner", owner);
		return map;
	}
	
	
	
}

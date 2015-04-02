package io.github.tylerstonge.meetingroom;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Room implements ConfigurationSerializable {

	private String id;
	private String name;
	private String owner;
	private SerializableLocation initialBlock;
	private boolean delete;
	
	public Room(String id, String name, String owner) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.delete = false;
	}
	
	public Room(Map<String, Object> map) {
		this.id = (String) map.get("id");
		this.name = (String) map.get("name");
		this.owner = (String) map.get("owner");
		this.initialBlock = (SerializableLocation) map.get("initialBlock");
	}
	
	public static Room deserialize(Map<String, Object> map) {
		return new Room(map);
	}
	
	public String getId() {
		return id;
	}

	public void markForDeletion() {
		this.delete = true;
	}
	
	public boolean isMarkedForDeletion() {
		return this.delete;
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
	
	public Location getInitialBlock() {
		return initialBlock.getLocation();
	}

	public void setInitialBlock(Location initialBlock) {
		this.initialBlock = new SerializableLocation(initialBlock);
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("name", name);
		map.put("owner", owner);
		map.put("initialBlock", initialBlock);
		return map;
	}
}

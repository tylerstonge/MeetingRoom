package io.github.tylerstonge.meetingroom;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
public class SerializableLocation implements ConfigurationSerializable
{
    private String worldName;
    private double x, y, z;
    private float yaw, pitch;
 
    public SerializableLocation(World world, double x, double y, double z,
            float yaw, float pitch)
    {
        this.worldName = world.getName();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
 
    public SerializableLocation(Location location)
    {
        this(location.getWorld(), location.getX(), location.getY(), location
                .getZ(), location.getYaw(), location.getPitch());
    }
 
    public SerializableLocation(Map<String, Object> map)
    {
        this(Bukkit.getWorld((String) map.get("world-name")),
        		(Double) map.get("x"), 
        		(Double) map.get("y"), 
        		(Double) map.get("z"), 
        		((Number) map.get("yaw")).floatValue(), 
        		((Number) map.get("pitch")).floatValue());
    }
 
    public Location getLocation()
    {
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
 
    @Override
    public Map<String, Object> serialize()
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("world-name", worldName);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("yaw", yaw);
        map.put("pitch", pitch);
        return map;
    }
 
}

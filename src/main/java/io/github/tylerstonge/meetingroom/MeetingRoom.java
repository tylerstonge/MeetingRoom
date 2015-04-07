package io.github.tylerstonge.meetingroom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MeetingRoom extends JavaPlugin implements Listener {

	HashMap<Integer, Room> roomBlocks = new HashMap<Integer, Room>();
	String defaultRoomName = "Room";
	Material meetingRoomCatalyst = Material.DIAMOND;
	int maxRoomSize = 400;

	ConfigAccessor data;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		ConfigurationSerialization.registerClass(Room.class);
		ConfigurationSerialization.registerClass(SerializableLocation.class);
		getServer().getPluginManager().registerEvents(this, this);

		data = new ConfigAccessor(this, "data.yml");
		loadRoomBlocks();
		scanAllRooms();
		
		meetingRoomCatalyst = Material.getMaterial(getConfig().getString("catalyst"));
		maxRoomSize = getConfig().getInt("maxsize");
		refreshPlayers();
	}

	@Override
	public void onDisable() {
		saveRoomBlocks();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(e.isCancelled())
			return;

		Player player = e.getPlayer();
		Block dest = e.getTo().getBlock();
		Block org = e.getFrom().getBlock();

		if(!roomBlocks.containsKey(org.getLocation().hashCode()) && roomBlocks.containsKey(dest.getLocation().hashCode())) {
			// Entering meeting room.
			getLogger().info(player.getDisplayName()+" has entered room!");
			MetadataManipulator.setMetadata(player, "meetingroom", roomBlocks.get(dest.getLocation().hashCode()), this);
			return;
		}

		if(roomBlocks.containsKey(org.getLocation().hashCode()) && !roomBlocks.containsKey(dest.getLocation().hashCode())) {
			// Leaving meeting room.
			MetadataManipulator.setMetadata(player, "meetingroom", null, this);
			getLogger().info(player.getDisplayName()+" has left room!");
			return;
		}
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		Player orig = e.getPlayer();
		if(MetadataManipulator.getMetadata(orig, "meetingroom", this) != "" && MetadataManipulator.getMetadata(orig, "meetingroom", this) != null) {
			e.setCancelled(true);
			String id = MetadataManipulator.getMetadata(orig, "meetingroom", this);
			for(Player dest : e.getRecipients()) {
				if(MetadataManipulator.getMetadata(dest, "meetingroom", this).equals(id)) {
					dest.sendMessage("["+orig.getName()+"->"+roomBlocks.get(orig.getLocation().getBlock().getLocation().hashCode()).getName()+"] "+e.getMessage());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {

		if((e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.hasItem() && e.hasBlock()) {
			Block targeted = e.getClickedBlock().getRelative(e.getBlockFace().getOppositeFace());
			if(e.getItem().getType() == meetingRoomCatalyst && e.getClickedBlock().getType() == Material.WOODEN_DOOR && targeted.getType() == Material.AIR) {
				if(!roomBlocks.containsKey(targeted.getLocation().hashCode())) {
					Player player = e.getPlayer();
					player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);

					Random rand = new Random();
					String id = Integer.toString(rand.nextInt(1000000));
					final Room r = new Room(id, defaultRoomName, player.getDisplayName());

					roomBlocks.put(targeted.getLocation().hashCode(), r);
					r.setInitialBlock(targeted.getLocation());
					addBlockToRoom(targeted, r);
					refreshPlayers();
				} else {
					scanRoom(roomBlocks.get(targeted.getLocation().hashCode()));
				}
			}
		}
	}

	@EventHandler
	public void onSignPlaced(SignChangeEvent e) {
		Player player = e.getPlayer();
		if(MetadataManipulator.getMetadata(player, "meetingroom", this) != null && MetadataManipulator.getMetadata(player, "meetingroom", this) != "") {
			Room room = roomBlocks.get(player.getLocation().getBlock().getLocation().hashCode());
			if(room.getOwner().equals(player.getDisplayName())) {
				if(e.getLine(0).equals("[meetingroom]")) {
					getLogger().info("Room set!");
					room.setName(e.getLine(1));
				}
			} else {
				player.sendMessage("You are not the owner of this meeting room.");
			}
		}
	}

	public void removeRoom(Room r, boolean displayError) {
		Iterator<Map.Entry<Integer, Room>> iter = roomBlocks.entrySet().iterator();
		String toDelete = r.getId();
		while(iter.hasNext()) {
			Entry<Integer, Room> entry = iter.next();
			if(toDelete.equals(entry.getValue().getId()))
				iter.remove();
		}
		if(displayError)
			this.getServer().getPlayer(r.getOwner()).sendMessage("This room is not enclosed, or exceeds maximum size.");
		refreshPlayers();
	}

	private void addBlockToRoom(Block t, final Room r) {
		if(roomBlocks.size() > maxRoomSize) {
			// Stop this madness
			r.markForDeletion();
			removeRoom(r, true);
			return;
		}

		Block nextBlock = null;

		nextBlock = t.getRelative(BlockFace.NORTH);
		if(nextBlock.getType() == Material.AIR && !roomBlocks.containsKey(nextBlock.getLocation().hashCode()) && !r.isMarkedForDeletion()) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.SOUTH);
		if(nextBlock.getType() == Material.AIR && !roomBlocks.containsKey(nextBlock.getLocation().hashCode()) && !r.isMarkedForDeletion()) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.EAST);
		if(nextBlock.getType() == Material.AIR && !roomBlocks.containsKey(nextBlock.getLocation().hashCode()) && !r.isMarkedForDeletion()) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.WEST);
		if(nextBlock.getType() == Material.AIR && !roomBlocks.containsKey(nextBlock.getLocation().hashCode()) && !r.isMarkedForDeletion()) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.DOWN);
		if(nextBlock.getType() == Material.AIR && !roomBlocks.containsKey(nextBlock.getLocation().hashCode()) && !r.isMarkedForDeletion()) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.UP);
		if(nextBlock.getType() == Material.AIR && !roomBlocks.containsKey(nextBlock.getLocation().hashCode()) && !r.isMarkedForDeletion()) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}
	}

	private void scanRoom(Room r) {
		removeRoom(r, false);
		Location loc = r.getInitialBlock();
		roomBlocks.put(loc.hashCode(), r);
		addBlockToRoom(loc.getWorld().getBlockAt(r.getInitialBlock()), r);
		refreshPlayers();
	}

	private void scanAllRooms() {
		Set<Room> uniqueRooms = new HashSet<Room>();
		for(Room r : roomBlocks.values()) {
			if(!uniqueRooms.contains(r)) {
				uniqueRooms.add(r);
			}
		}
		
		for(Room u : uniqueRooms) {
			scanRoom(u);
		}
	}

	private void refreshPlayers() {
		for(Player p : this.getServer().getOnlinePlayers()) {
			Integer hash = p.getLocation().getBlock().getLocation().hashCode();
			if(roomBlocks.containsKey(hash)) {
				MetadataManipulator.setMetadata(p, "meetingroom", hash, this);
			} else {
				MetadataManipulator.setMetadata(p, "meetingroom", null, this);
			}
		}
	}

	private void loadRoomBlocks() {
		roomBlocks = new HashMap<Integer, Room>();
		if(data.getConfig().isConfigurationSection("meetingroom.")) {
			Set<String> set = data.getConfig().getConfigurationSection("meetingroom.").getKeys(false);
			for(String hash : set) {
				Room r = (Room) data.getConfig().getConfigurationSection("meetingroom.").get(hash);
				roomBlocks.put(Integer.parseInt(hash), r);
			}
		}
	}

	private void saveRoomBlocks() {
		if(data.getConfig().isConfigurationSection("meetingroom.")) {
			Set<String> set = data.getConfig().getConfigurationSection("meetingroom.").getKeys(false);
			for(String hash : set) {
				data.getConfig().set("meetingroom."+hash, null);
			}
		}
		for(Map.Entry<Integer, Room> entry : roomBlocks.entrySet()) {
			data.getConfig().set("meetingroom."+entry.getKey(), entry.getValue());
		}
		data.saveConfig();
	}

}

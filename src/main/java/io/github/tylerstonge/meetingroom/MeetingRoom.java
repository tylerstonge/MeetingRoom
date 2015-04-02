package io.github.tylerstonge.meetingroom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MeetingRoom extends JavaPlugin implements Listener {

	HashMap<Integer, Room> roomBlocks = new HashMap<Integer, Room>();
	String defaultRoomName = "Room";
	Material meetingRoomMaterial = Material.OBSIDIAN;
	Material meetingRoomCatalyst = Material.DIAMOND;

	ConfigAccessor data;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		ConfigurationSerialization.registerClass(Room.class);
		getServer().getPluginManager().registerEvents(this, this);

		data = new ConfigAccessor(this, "data.yml");
		loadRoomBlocks();

		meetingRoomMaterial = Material.getMaterial(getConfig().getString("material"));
		meetingRoomCatalyst = Material.getMaterial(getConfig().getString("catalyst"));
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
		Block dest = e.getTo().getBlock().getRelative(BlockFace.DOWN);
		Block org = e.getFrom().getBlock().getRelative(BlockFace.DOWN);

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
					dest.sendMessage("["+orig.getName()+"->"+roomBlocks.get(orig.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().hashCode()).getName()+"] "+e.getMessage());
				}
			}
			getLogger().info("MESSAGE SENT: "+e.getMessage()+ "RECIPIENTS: "+e.getRecipients()+"ROOM: "+MetadataManipulator.getMetadata(orig, "meetingroom", this));
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {

		if((e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.hasItem() && e.hasBlock()) {
			Block targeted = e.getClickedBlock();
			if(e.getItem().getType() == meetingRoomCatalyst && targeted.getType() == meetingRoomMaterial && !roomBlocks.containsKey(targeted.getLocation().hashCode())) {
				Player player = e.getPlayer();
				player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);
				
				Random rand = new Random();
				String id = Integer.toString(rand.nextInt(1000000));
				final Room r = new Room(id, defaultRoomName, player.getDisplayName());
				roomBlocks.put(targeted.getLocation().hashCode(), r);
				addBlockToRoom(targeted, r);
				refreshPlayers();
			}
		}
	}

	@EventHandler
	public void onSignPlaced(SignChangeEvent e) {
		Player player = e.getPlayer();
		if(MetadataManipulator.getMetadata(player, "meetingroom", this) != null && MetadataManipulator.getMetadata(player, "meetingroom", this) != "") {
			Room room = roomBlocks.get(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().hashCode());
			if(room.getOwner() == player.getDisplayName()) {
				if(e.getLine(0).equals("[meetingroom]")) {
					getLogger().info("Room set!");
					room.setName(e.getLine(1));
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Block target = e.getBlock();
		if(roomBlocks.containsKey(target.getLocation().hashCode())) {
			String toDelete = roomBlocks.get(target.getLocation().hashCode()).getId();
			Iterator<Map.Entry<Integer, Room>> iter = roomBlocks.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<Integer, Room> entry = iter.next();
				if(toDelete.equals(entry.getValue().getId()))
					iter.remove();
			}
		}
		refreshPlayers();
	}

	private void addBlockToRoom(Block t, final Room r) {
		Block nextBlock = null;

		nextBlock = t.getRelative(BlockFace.NORTH);
		if(nextBlock.getType() == meetingRoomMaterial && !roomBlocks.containsKey(nextBlock.getLocation().hashCode())) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.SOUTH);
		if(nextBlock.getType() == meetingRoomMaterial && !roomBlocks.containsKey(nextBlock.getLocation().hashCode())) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.EAST);
		if(nextBlock.getType() == meetingRoomMaterial && !roomBlocks.containsKey(nextBlock.getLocation().hashCode())) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}

		nextBlock = t.getRelative(BlockFace.WEST);
		if(nextBlock.getType() == meetingRoomMaterial && !roomBlocks.containsKey(nextBlock.getLocation().hashCode())) {
			roomBlocks.put(nextBlock.getLocation().hashCode(), r);
			addBlockToRoom(nextBlock, r);
		}
	}
	
	private void refreshPlayers() {
		for(Player p : this.getServer().getOnlinePlayers()) {
			Integer hash = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().hashCode();
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

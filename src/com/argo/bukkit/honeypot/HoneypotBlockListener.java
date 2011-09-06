package com.argo.bukkit.honeypot;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

import com.argo.bukkit.honeypot.config.Config;

public class HoneypotBlockListener extends BlockListener {

    private static final Logger log = Honeypot.log;
    private Honeypot plugin;
    private HoneyStack honeyStack;
    private Config config;

    public HoneypotBlockListener(Honeypot instance) {
        plugin = instance;
        config = plugin.getConfig();
        honeyStack = plugin.getHoneyStack();
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (Honeyfarm.isPot(block.getLocation())) {
            Player player = event.getPlayer();
            if (!HoneypotPermissionsHandler.canBreak(player)) {
            	boolean usingHoneyPoints = false;
            	int blockPoints = 0;
            	int maxPoints = 0;
            	
            	// if offensePoints are defined, we use a pointMap to determine how many points this
            	// broken block is worth.  This allows some blocks to be worth more, so perhaps you
            	// can instaban if they steal honeypot diamonds, but maybe it takes 3 offenses if
            	// they're griefing your wooden structure or flower field or whatever.
            	if (config.getOffensePoints() > 0) {
            		usingHoneyPoints = true;
            		maxPoints = config.getOffensePoints();
            		blockPoints = 1;		// default point value is 1 if nothing is defined in the map
            		
            		// now lookup this block type in the map
            		Integer blockId = block.getTypeId();
            		Map<Integer, Integer> typeMap = config.getBlockPointMap();
            		if( typeMap != null ) {
            			Integer points = typeMap.get(blockId);
            			System.out.println("points for blockId "+blockId+" = "+points);
            			
            			if( points != null )
            				blockPoints = points.intValue();
            		}
            	}
            	// otherwise we use offenseCount if defined.
            	else if (config.getOffenseCount() > 1) {
            		usingHoneyPoints = true;
            		maxPoints = config.getOffenseCount();
            		blockPoints = 1;			// each break is worth 1 point
            	}
            	
            	if( usingHoneyPoints ) {
                    String playerName = player.getName();
                    honeyStack.breakHoneypot(playerName, block.getState(), blockPoints);

                    int points = honeyStack.getHoneyPoints(player.getName());

                    // log to both Honeypot logfile and system logs
                    String logMessage = "player " + playerName + 
                            " broke HoneyPot block at "
                            + Honeypot.prettyPrintLocation(block.getLocation()) 
                            + ", break count/points: " + points;

                    log.info("[Honeypot] " + logMessage);
                    if (config.getLogFlag()) {
                        Honeyfarm.log(logMessage);
                    }

                    if (points < maxPoints) {
                        return;		// do no further processing if they haven't reached the limit yet
                    } else {
                        honeyStack.rollBack(playerName);
                    }
                }
            	
            	// if make it here, there is no offensePoint or offenseCount defined, or this
            	// player has exceeded the limits and it's time for action.

                event.setCancelled(true);

                if (config.getKickFlag()) {
                    plugin.getBansHandler().kick(player, config.getPotSender(), 
                            config.getPotMsg());
                } else if (config.getBanFlag()) {
                	plugin.getBansHandler().ban(player, config.getPotSender(),
                            config.getPotReason());
                }

                String logMessage = "Player " + player.getName() + 
                        " was caught breaking a honeypot block at location "
                        + Honeypot.prettyPrintLocation(block.getLocation()) +
                        ".";

                log.info(logMessage);
                if (config.getLogFlag()) {
                    Honeyfarm.log(logMessage);
                }

                if (config.getShoutFlag()) {
                    plugin.getServer().broadcastMessage(ChatColor.DARK_RED + 
                            "[Honeypot]" + ChatColor.GRAY + " Player " + 
                            ChatColor.DARK_RED + player.getName() + 
                            ChatColor.GRAY + " was caught breaking a "
                            + "honeypot block.");
                }
            } else {
                player.sendMessage(ChatColor.GREEN + "Honeypot removed.");
                Honeyfarm.removePot(event.getBlock().getLocation());
            }
        }
    }
}

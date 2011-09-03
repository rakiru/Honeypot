/**
 * 
 */
package com.argo.bukkit.honeypot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

/** Class to manage HoneyPot breaks when the minimum offense count is > 1.  So if they are
 * allowed to break 2+ HoneyPot blocks before we take action, this keeps track of the number
 * of breaks a player has so far as well as what blocks they've broken, so we can roll
 * back the damage.
 * 
 * @author morganm
 *
 */
public class HoneyStack {
	private static final Logger log = Honeypot.log;
	
	private HashMap<String, Integer> honeypotCount = new HashMap<String, Integer>();
	private HashMap<String, ArrayList<Location>> playerHoneyBreaks = new HashMap<String, ArrayList<Location>>();
	private HashMap<Location, BlockState> honeyBlocks = new HashMap<Location, BlockState>();
	
	public Integer getBreakCount(String playerName) {
		return honeypotCount.get(playerName);
	}
	
	/** Called when a player breaks a HoneyPot, to increment their offense counter as well as
	 * record the broken block so we can roll it back later.
	 * 
	 * @param playerName
	 * @param bs
	 */
	public void breakHoneypot(String playerName, BlockState bs) {
		Location l = bs.getBlock().getLocation();
		if( honeyBlocks.get(l) == null )
			honeyBlocks.put(l, bs);
		
		Integer count = honeypotCount.get(playerName);
		if( count == null )
			count = 0;
		count += 1;
		honeypotCount.put(playerName, Integer.valueOf(count));
		
		ArrayList<Location> list = playerHoneyBreaks.get(playerName);
		if( list == null ) {
			list = new ArrayList<Location>();
			playerHoneyBreaks.put(playerName, list);
		}
		
		list.add(l);
	}
	
	/** Roll back any HoneyPot blocks that were broken by a given player.
	 * 
	 * @param playerName
	 */
	public void rollBack(String playerName) {
		ArrayList<Location> locations = playerHoneyBreaks.get(playerName);
		if( locations == null )
			return;
		
		for(Location location : locations) {
			// we try/catch so that even if one block fails (really shouldn't ever happen), the other
			// ones can still go through.
			try {
				BlockState bs = honeyBlocks.get(location);
				bs.update(true);
				log.info("[Honeypot] successfully rolled back Honeypot damage from player "+playerName+" at location "+Honeypot.prettyPrintLocation(location));
			}
			catch(Exception e) {
				log.warning("[Honeypot] error rolling back Honeypot block at location "+Honeypot.prettyPrintLocation(location));
				e.printStackTrace();
			}
		}
		
		playerHoneyBreaks.put(playerName, null);
		
		// NOTE: we intentionally do NOT reset the "honeypotCount" for this player. This way if an admin
		// is choosing to use "kick" intead of "ban", the player is insta-kicked if they log 	back in and
		// try to break more honeypots, since they're already over the minimum limit.
	}
	
	/** Called to rollback all HoneyPot damage, this is used if the plugin/server is shutdown
	 * to prevent the damage from being permanent.
	 * 
	 */
	public void rollBackAll() {
		Set<String> players = playerHoneyBreaks.keySet();
		if( players == null )
			return;
		
		for(String player : players) {
			rollBack(player);
		}
	}
}

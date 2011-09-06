package com.argo.bukkit.honeypot;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class HoneypotPlayerListener extends PlayerListener {
	private Honeypot plugin;
    private HoneyStack honeyStack;

    public HoneypotPlayerListener(Honeypot instance) {
    	plugin = instance;
    	honeyStack = plugin.getHoneyStack();
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
    	
    	if(Honeyfarm.getPotSelect(player) && event.getAction() == Action.RIGHT_CLICK_BLOCK){
    		if(HoneypotPermissionsHandler.canUseCmd(player) && player.getItemInHand().getTypeId() == plugin.getConfig().getToolId()) {
    			if(!Honeyfarm.isPot(event.getClickedBlock().getLocation())) {
    				Honeyfarm.createPot(event.getClickedBlock().getLocation());
    				player.sendMessage(ChatColor.GREEN + "Honeypot created. Destroy the block to remove the honeypot.");
    			} else {
    				player.sendMessage(ChatColor.DARK_RED + "That block is already marked as a honeypot.");
    			}
    		}
    	}
    }
    
    @Override
    public void onPlayerKick(PlayerKickEvent event) {
    	honeyStack.playerLogout(event.getPlayer().getName());
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
    	honeyStack.playerLogout(event.getPlayer().getName());
    }
}

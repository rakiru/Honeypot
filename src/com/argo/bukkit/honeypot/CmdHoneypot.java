
package com.argo.bukkit.honeypot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdHoneypot implements CommandExecutor {
	private Honeypot plugin;

    public CmdHoneypot(Honeypot instance) {
    	plugin = instance;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
    	if(args.length == 0) {
    		if(sender instanceof Player) {
    			Player player = (Player)sender;
    			if(!HoneypotPermissionsHandler.canUseCmd(player)) {
    				player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
    			} else {
    				if(Honeyfarm.getPotSelect(player)) {
    					player.sendMessage(ChatColor.GREEN + "Honeypot creation cancelled.");
    					Honeyfarm.setPotSelect(player, false);
    				} else {
    					player.sendMessage(ChatColor.GREEN + "Right click a block with a " + plugin.getConfig().getToolId() + " to create a honeypot. When finished, use /hp again.");
    					Honeyfarm.setPotSelect(player, true);
    				}
    			}
    		} else {
    			sender.sendMessage("Sorry, this command can only be used by players.");
    		}
    	} else if(args.length == 1) {
    		if(args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("s")) {
    			sender.sendMessage(ChatColor.GREEN + "Saving honeypot data...");
    			if(!Honeyfarm.saveData()) {
    				sender.sendMessage(ChatColor.DARK_RED + "Failed to save data.");
    			}
    		}
    		// added to allow live-reloading of data file  -morganm 5/20/11
    		else if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
    			sender.sendMessage(ChatColor.GREEN + "Reloading honeypot data from saved file...");
    			
    			if(!Honeyfarm.refreshData()) {
    				sender.sendMessage(ChatColor.DARK_RED + "Failed to load data.");
    			}
    		}
    	}
    	return true;
    }

}

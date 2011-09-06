package com.argo.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.argo.bukkit.honeypot.Honeypot;
import com.argo.bukkit.honeypot.config.Config;
import com.firestar.mcbans.mcbans;
import com.firestar.mcbans.mcbans_handler;
import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.ban;
import com.mcbans.firestar.mcbans.pluginInterface.kick;

/** This class was originally all static methods (arrrrg)..  Refactored to be an instance so we can
 * pass in instance variables such as the Config object we're using.  It really should be re-written
 * to use an Interface and each Ban type (mcbans, easyban, etc) should be their own implementation
 * of the interface and let polymorphism determine which one is used at run-time as opposed to a
 * switch statement.  #OOPftw
 * 
 * @author Argomirr, morganm
 *
 */
public class BansHandler {

    @SuppressWarnings("unused")
	private Honeypot plugin;
    private Config config;
	private bukkitInterface mcb3;
    private mcbans_handler mcb;
    private BansMethod bmethod = BansMethod.VANILLA; // default

    public BansHandler(Honeypot plugin) {
    	this.plugin = plugin;
    	config = plugin.getConfig();
    }
    
    public BansMethod setupbanHandler(JavaPlugin plugin) {
        // Check for MCBans
        Plugin testMCBans = plugin.getServer().getPluginManager().
                getPlugin("mcbans");
        if (testMCBans == null) //Compatibility for older MCBans releases
        {
            testMCBans = plugin.getServer().getPluginManager().
                    getPlugin("MCBans");
        }
        // Check for EasyBans
        Plugin testEB = plugin.getServer().getPluginManager().
                getPlugin("EasyBan");
        if (testEB == null) //Compatibility for oldEasyBan release
        {
            testEB = plugin.getServer().getPluginManager().
                    getPlugin("easyban");
        }
        // Check for KiwiAdmin
        Plugin testKA = plugin.getServer().getPluginManager().
                getPlugin("KiwiAdmin");
        if (testKA == null) //Compatibility for older bad-releases
        {
            testKA = plugin.getServer().getPluginManager().
                    getPlugin("kiwiadmin");
        }

        if (testMCBans != null) {
        	if( testMCBans.getDescription().getVersion().startsWith("3") ) {
        		mcb3 = (bukkitInterface) testMCBans;
	            bmethod = BansMethod.MCBANS3;
        	}
        	else {
	            mcb = ((mcbans) testMCBans).mcb_handler;
	            bmethod = BansMethod.MCBANS;
        	}
        } else if (testEB != null) {
            bmethod = BansMethod.EASYBAN;
        } else if (testKA != null) {
            bmethod = BansMethod.KABANS;
        } else {
            bmethod = BansMethod.VANILLA;
        }
        return bmethod;
    }

    public void ban(Player p, String sender, String reason) {
        // get player location (more useful than HP block loc.)
        if (config.getLocFlag() == true) {
            Location loc = p.getLocation();
            int locx = (int) loc.getX();
            int locy = (int) loc.getY();
            int locz = (int) loc.getZ();
            reason = reason
                    + " (" + locx + "," + locy + "," + locz + ")";
        }
        // use right ban method
        switch (bmethod) {
            case VANILLA:
                // fix for black screen after BAN
                p.kickPlayer(config.getPotMsg());
                VanillaBan(p);
                break;
            case MCBANS:
                MCBan(p, sender, reason, "");
                break;
            case MCBANS3:
            	MCBan3(p, sender, reason, "");
            	break;
            case EASYBAN:
                // also fix for black screen after BAN
                p.kickPlayer(config.getPotMsg());
                Eban(p, reason);
                break;
            case KABANS:
                KAban(p, reason);
                break;
            default:
                break;
        }
    }

    public void kick(Player p, String sender, String reason) {
        // use right kick method
        switch (bmethod) {
            case VANILLA:
                p.kickPlayer(reason);
                break;
            case MCBANS:
                MCBanKick(p, sender, reason);
                break;
            case MCBANS3:
            	MCBan3Kick(p, sender, reason);
            	break;
            case EASYBAN:
                EBkick(p, reason);
                break;
            case KABANS:
                KAkick(p, reason);
                break;
            default:
                p.kickPlayer(reason);
                break;
        }
    }

    private void MCBan3(Player player, String sender, String reason, String type) {
        player.kickPlayer(reason); //kick for good measure
        
        // "localBan" or "globalBan" - need to make a config option
        ban banControl = new ban( mcb3, "localBan", player.getName(), player.getAddress().toString(), sender, reason, "", "" );
		banControl.start();
    }
    private void MCBan3Kick(Player player, String sender, String reason) {
		kick kickControl = new kick( mcb3.Settings, mcb3, player.getName(), sender, reason );
		kickControl.start();
    }
    
    private void MCBan(Player player, String sender, String reason, String type) {
        player.kickPlayer(reason); //kick for good measure
        mcb.ban(player.getName(), sender, reason, type);
    }

    private void MCBanKick(Player player, String sender, String reason) {
        mcb.kick(player.getName(), sender, reason);
    }
    
    private void EBkick(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "ekick " + player.getName() + " " + reason);
    }
    
    private void KAkick(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "kick " + player.getName() + " " + reason);
    }    

    private void VanillaBan(Player player) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "ban " + player.getName());
    }

    private void Eban(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "eban " + player.getName() + " " + reason);
    }

    private void KAban(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "ban " + player.getName() + " " + reason);
    }
}

package com.argo.bukkit.util;

import com.argo.bukkit.honeypot.Settings;
import com.firestar.mcbans.mcbans;
import com.firestar.mcbans.mcbans_handler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BansHandler {

    private static mcbans_handler mcb;
    private static BansMethod bmethod = BansMethod.VANILLA; // default

    public static BansMethod setupbanHandler(JavaPlugin plugin) {
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
            mcb = ((mcbans) testMCBans).mcb_handler;
            bmethod = BansMethod.MCBANS;
        } else if (testEB != null) {
            bmethod = BansMethod.EASYBAN;
        } else if (testKA != null) {
            bmethod = BansMethod.KABANS;
        } else {
            bmethod = BansMethod.VANILLA;
        }
        return bmethod;
    }

    public static void ban(Player p, String sender, String reason) {
        // get player location (more useful than HP block loc.)
        if (Settings.getLocFlag() == true) {
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
                p.kickPlayer(Settings.getPotMsg());
                VanillaBan(p);
                break;
            case MCBANS:
                MCBan(p, sender, reason, "");
                break;
            case EASYBAN:
                // also fix for black screen after BAN
                p.kickPlayer(Settings.getPotMsg());
                Eban(p, reason);
                break;
            case KABANS:
                KAban(p, reason);
                break;
            default:
                break;
        }
    }

    public static void kick(Player p, String sender, String reason) {
        // use right kick method
        switch (bmethod) {
            case VANILLA:
                p.kickPlayer(reason);
                break;
            case MCBANS:
                MCBanKick(p, sender, reason);
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

    private static void MCBan(Player player, String sender, String reason, String type) {
        player.kickPlayer(reason); //kick for good measure
        mcb.ban(player.getName(), sender, reason, type);
    }

    private static void MCBanKick(Player player, String sender, String reason) {
        mcb.kick(player.getName(), sender, reason);
    }
    
    private static void EBkick(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "ekick " + player.getName() + " " + reason);
    }
    
    private static void KAkick(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "kick " + player.getName() + " " + reason);
    }    

    private static void VanillaBan(Player player) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "ban " + player.getName());
    }

    private static void Eban(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "eban " + player.getName() + " " + reason);
    }

    private static void KAban(Player player, String reason) {
        Bukkit.getServer().dispatchCommand(new ConsoleCommandSender(Bukkit.getServer()),
                "ban " + player.getName() + " " + reason);
    }
}

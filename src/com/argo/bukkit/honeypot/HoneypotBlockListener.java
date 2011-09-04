package com.argo.bukkit.honeypot;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

import com.argo.bukkit.util.BansHandler;

public class HoneypotBlockListener extends BlockListener {

    private static final Logger log = Honeypot.log;
    private Honeypot plugin;
    private HoneyStack honeyStack;

    public HoneypotBlockListener(Honeypot instance) {
        plugin = instance;
        honeyStack = plugin.getHoneyStack();
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (Honeyfarm.isPot(block.getLocation())) {
            Player player = event.getPlayer();
            if (!HoneypotPermissionsHandler.canBreak(player)) {
                if (Settings.getOffenseCount() > 1) {
                    String playerName = player.getName();
                    honeyStack.breakHoneypot(playerName, block.getState());

                    int count = honeyStack.getBreakCount(player.getName());

                    // log to both Honeypot logfile and system logs
                    String logMessage = "player " + playerName + 
                            " broke HoneyPot block at "
                            + Honeypot.prettyPrintLocation(block.getLocation()) 
                            + ", break count: " + count;

                    log.info("[Honeypot] " + logMessage);
                    if (Settings.getLogFlag()) {
                        Honeyfarm.log(logMessage);
                    }

                    if (count < Settings.getOffenseCount()) {
                        return;		// do no further processing if they haven't reached the limit yet
                    } else {
                        honeyStack.rollBack(playerName);
                    }
                }

                event.setCancelled(true);

                if (Settings.getKickFlag()) {
                    BansHandler.kick(player, Settings.getPotSend(), 
                            Settings.getPotMsg());
                } else if (Settings.getBanFlag()) {
                    BansHandler.ban(player, Settings.getPotSend(),
                            Settings.getPotRea());
                }

                String logMessage = "Player " + player.getName() + 
                        " was caught breaking a honeypot block at location "
                        + Honeypot.prettyPrintLocation(block.getLocation()) +
                        ".";

                log.info(logMessage);
                if (Settings.getLogFlag()) {
                    Honeyfarm.log(logMessage);
                }

                if (Settings.getShoutFlag()) {
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

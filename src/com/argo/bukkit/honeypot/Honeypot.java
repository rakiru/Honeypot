package com.argo.bukkit.honeypot;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.argo.bukkit.honeypot.config.Config;
import com.argo.bukkit.honeypot.config.PropertiesFile;
import com.argo.bukkit.honeypot.config.YMLFile;
import com.argo.bukkit.util.BansHandler;

public class Honeypot extends JavaPlugin {

    public static final Logger log = Logger.getLogger("Honeypot");
    private static Honeypot instance;
    private HoneypotBlockListener blockListener;
    private HoneypotPlayerListener playerListener;
    private HoneyStack honeyStack;
    private Config config;
    private BansHandler bansHandler;

    /** I think there's a correct "PluginManager" way to get plugin instances, but
     * I'm cheating and using a static instance ala the Singleton pattern for now.
     * 
     * @return
     */
    public static Honeypot getCurrentInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        honeyStack = new HoneyStack();

        createDirs();

        loadConfig();
        Honeyfarm.setLogPath(config.getLogPath());

        if (!Honeyfarm.refreshData()) {
            System.out.println("Honeypot: an error occured while trying to"
                    + " load the honeypot list.");
        }
        if (!HoneypotPermissionsHandler.setupPermissions(this)) {
            System.out.println("Honeypot: Permissions plugin not found, using "
                    + "default.");
        } else {
            System.out.println("Honeypot: Permissions plugin found, using "
                    + "that.");
        }
        
        bansHandler = new BansHandler(this);
        switch (bansHandler.setupbanHandler(this)) {
            case VANILLA:
                System.out.println("Honeypot: Didn't find ban plugin, using "
                        + "vanilla.");
                break;
            case MCBANS:
                System.out.println("Honeypot: MCBans plugin found, using "
                        + "that.");
                break;
            case MCBANS3:
                System.out.println("Honeypot: MCBans3 plugin found, using "
                        + "that.");
                break;
            case EASYBAN:
                System.out.println("Honeypot: EasyBan plugin found, using "
                        + "that.");
                break;
            case KABANS:
                System.out.println("Honeypot: KiwiAdmin plugin found, using "
                        + "that.");
                break;
            default:
                System.out.println("Honeypot: Didn't find ban plugin, using "
                        + "vanilla.");
                break;
        }

        blockListener = new HoneypotBlockListener(this);
        playerListener = new HoneypotPlayerListener(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Low,
                this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Highest,
                this);
        getCommand("honeypot").setExecutor(new CmdHoneypot(this));

        // schedule to run every minute (20 ticks * 60 seconds)
        getServer().getScheduler().scheduleSyncRepeatingTask(this, honeyStack, 1200, 1200);
        
        PluginDescriptionFile pdf = this.getDescription();
        System.out.println(pdf.getName() + " revision " + pdf.getVersion() +
                " by " + pdf.getAuthors().get(0) + " succesfully loaded.");
    }

    public void onDisable() {
        if (!Honeyfarm.saveData()) {
            System.out.println("Honeypot: an error occured while trying to save"
                    + " the honeypot list.");
        }

        honeyStack.rollBackAll();
        getServer().getScheduler().cancelAllTasks();

        PluginDescriptionFile pdf = this.getDescription();
        System.out.println(pdf.getName() + " revision " + pdf.getVersion() + 
                " by " + pdf.getAuthors().get(0) + " succesfully disabled.");
    }
    
    private void loadConfig() {
    	// bad code, we break the interface abstraction by looking for implementation-specific
    	// details, but I'm OK with this since this is intended simply as temporary a
    	// transition from the old properties file to new-style config.yml
    	File newFile = new File("plugins/Honeypot/config.yml");
    	File oldFile = new File("plugins/Honeypot/honeypot.properties");
    	
    	if( newFile.exists() ) {		// new-style config.yml exists?  use it
    		config = new YMLFile();
    	}
    	else if( oldFile.exists() ) { 	// no new-style exists, but old-style does, use that instead.
    		config = new PropertiesFile();
    	}
    	else {							// neither exists yet (new installation), create and use new-style
    		// TODO: add call to copy default YML into place. Bukkit will work fine without this, but if
    		// we build and copy one into place, we can have comments in the default file which helps
    		// people with the configuration.
    		
    		config = new YMLFile();
    		try {
    			config.load(this);
    		} catch(Exception e) {} 	// ignored for now, the real load is later
    		
    		((YMLFile) config).firstLoad();
    	}
    	
    	try {
    		config.load(this);
    	}
    	catch(Exception e) {
            System.out.println("Honeypot: an error occured while "
	            + "trying to load the properties file.");
    		e.printStackTrace();
    	}
    }

    public Config getConfig() { return config; }
    public BansHandler getBansHandler() { return bansHandler; }
    
    public void createDirs() {
        new File("plugins/Honeypot").mkdir();
    }

    public String getLogPath() {
        return config.getLogPath();
    }

    public HoneyStack getHoneyStack() {
        return honeyStack;
    }

    public static String prettyPrintLocation(Location l) {
        return "{world=" + l.getWorld().getName() + ", x=" + l.getBlockX() +
                ", y=" + l.getBlockY() + ", z=" + l.getBlockZ() + "}";
    }
}

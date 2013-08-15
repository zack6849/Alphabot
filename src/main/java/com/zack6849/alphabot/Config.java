package com.zack6849.alphabot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class Config {
    private static PropertiesConfiguration conf;
    public static PropertiesConfiguration customcmd;
    public static String CURRENT_DIR = "conf/";
    public static File cnf = new File(CURRENT_DIR + "/config.yml");
    public static File cmd = new File(CURRENT_DIR + "/custom-commands.yml");
    public static boolean IDENTIFY_WITH_NICKSERV;
    public static boolean VERIFY_ADMIN_NICKS;
    public static boolean DEBUG_MODE;
    public static boolean ACCEPT_INVITES;
    public static boolean ALLOW_FSERV;
    public static String PERMISSIONS_DENIED;
    public static String SERVER;
    public static String NOT_ADMIN;
    public static String PASSWORD;
    public static String NICK;
    public static String IDENT;
    public static String REALNAME;
    public static String NOTICE_IDENTIFIER;
    public static String PUBLIC_IDENTIFIER;
    public static String[] CHANS;
    public static ArrayList<String> ADMINS;
    public static ArrayList<String> EXEC_ADMINS;
    public static ArrayList<String> LOGGED_CHANS;

    public static void loadConfig() throws ConfigurationException, IOException {
        conf = new PropertiesConfiguration(cnf);
        customcmd = new PropertiesConfiguration(cmd);
        if (cmd.exists()) {
            customcmd.load(cmd);
        }
        else {
            cmd.getParentFile().mkdirs();
            cmd.createNewFile();
        }
        System.out.println(String.valueOf(cnf.exists()));
        if (cnf.exists()) {
            System.out.println(cnf.getAbsolutePath());
            conf.load(cnf);
        }
        else {
            cnf.getParentFile().mkdirs();
            cnf.createNewFile();
            System.out.println(cnf.getAbsolutePath());
            conf.setFile(cnf);
            conf.setProperty("SERVER", "irc.esper.net");
            conf.setProperty("BOT-NICKNAME", "Alphabot");
            conf.setProperty("BOT-IDENT", "Bot");
            conf.setProperty("BOT-REALNAME", "Alphabot");
            conf.setProperty("IDENTIFY-WITH-NICKSERV", true);
            conf.setProperty("NICKSERV-PASS", "password");
            conf.setProperty("CHANNELS", "#alphacraft #alphabot");
            conf.setProperty("VERIFY-BOT-ADMINS", true);
            conf.setProperty("BOT-ADMINS", "zack6849");
            conf.setProperty("EXEC-ADMINS", "zack6849");
            conf.setProperty("ALLOW-FILE-TRANSFER", false);
            conf.setProperty("PUBLIC-IDENTIFIER", "$");
            conf.setProperty("NOTICE-IDENTIFIER", "|");
            conf.setProperty("PERMISSIONS-DENIED", "Sorry you lack the necessary permissions to perform that action.");
            conf.setProperty("NOT-ADMIN", "Sorry, you are either not on the admins list or are not logged in as an admin.");
            conf.setProperty("DEBUG-MODE", true);
            conf.setProperty("ACCEPT-INVITIATIONS", true);
            conf.setProperty("LOGGED-CHANNELS","#alphacraft #alphabot");
            conf.save();
        }
        ADMINS = new ArrayList<String>();
        EXEC_ADMINS = new ArrayList<String>();
        IDENTIFY_WITH_NICKSERV = conf.getBoolean("IDENTIFY-WITH-NICKSERV");
        VERIFY_ADMIN_NICKS = conf.getBoolean("VERIFY-BOT-ADMINS");
        DEBUG_MODE = conf.getBoolean("DEBUG-MODE");
        PERMISSIONS_DENIED = conf.getString("PERMISSIONS-DENIED");
        NOT_ADMIN = conf.getString("NOT-ADMIN");
        PASSWORD = conf.getString("NICKSERV-PASS");
        NICK = conf.getString("BOT-NICKNAME");
        IDENT = conf.getString("BOT-IDENT");
        REALNAME = conf.getString("BOT-REALNAME");
        ALLOW_FSERV = conf.getBoolean("ALLOW-FILE-TRANSFER");
        NOTICE_IDENTIFIER = conf.getString("NOTICE-IDENTIFIER");
        PUBLIC_IDENTIFIER = conf.getString("PUBLIC-IDENTIFIER");
        CHANS = conf.getString("CHANNELS").split(" ");
        ADMINS = new ArrayList(Arrays.asList(conf.getString("BOT-ADMINS").split(" ")));
        EXEC_ADMINS = new ArrayList(Arrays.asList(conf.getString("EXEC-ADMINS").split(" ")));
        LOGGED_CHANS = new ArrayList(Arrays.asList(conf.getString("LOGGED-CHANNELS").split(" ")));
        SERVER = conf.getString("SERVER");
        ACCEPT_INVITES = conf.getBoolean("ACCEPT-INVITIATIONS");
    }

    public static PropertiesConfiguration getConfig() {
        return conf;
    }

    public static void reload() {
        try {
            System.out.println("Reloading config!");
            getConfig().clear();
            System.out.println("Cleared properies");
            System.out.println("Setting properties");
            conf.setProperty("SERVER", Config.SERVER);
            conf.setProperty("BOT-NICKNAME", Config.NICK);
            conf.setProperty("BOT-IDENT", Config.IDENT);
            conf.setProperty("BOT-REALNAME", Config.REALNAME);
            conf.setProperty("IDENTIFY-WITH-NICKSERV", Config.IDENTIFY_WITH_NICKSERV);
            conf.setProperty("NICKSERV-PASS", Config.PASSWORD);
            conf.setProperty("CHANNELS", Utils.removeBrackets(Arrays.toString(CHANS)).replaceAll(",", ""));
            conf.setProperty("VERIFY-BOT-ADMINS", Config.VERIFY_ADMIN_NICKS);
            conf.setProperty("BOT-ADMINS", Utils.removeBrackets(ADMINS.toString()).replaceAll(",", ""));
            conf.setProperty("EXEC-ADMINS", Utils.removeBrackets(EXEC_ADMINS.toString()).replaceAll(",", ""));
            conf.setProperty("ALLOW-FILE-TRANSFER", Config.ALLOW_FSERV);
            conf.setProperty("PUBLIC-IDENTIFIER", Config.PUBLIC_IDENTIFIER);
            conf.setProperty("NOTICE-IDENTIFIER", Config.NOTICE_IDENTIFIER);
            
            conf.setProperty("PERMISSIONS-DENIED", Config.PERMISSIONS_DENIED);
            conf.setProperty("NOT-ADMIN", Config.NOT_ADMIN);
            conf.setProperty("DEBUG-MODE", Config.DEBUG_MODE);
            conf.setProperty("ACCEPT-INVITIATIONS", Config.ACCEPT_INVITES);
            conf.setProperty("LOGGED-CHANNELS", Utils.removeBrackets(LOGGED_CHANS.toString()).replaceAll(",", ""));
            System.out.println("Done Setting propertis!\nSaving!");
            conf.save(cnf);
            System.out.println("Saved!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
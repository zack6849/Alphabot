/*
 * and open the template in the editor.
 */
package com.zack6849.alphabot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.UserSnapshot;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.WhoisEvent;

import bsh.Interpreter;

/**
 *
 * @author zack6849(zcraig29@gmail.com)
 */
public class Commands {
	static Interpreter i = new Interpreter();
    static List<String> owners = Bot.owners;
    static String perms = Config.PERMISSIONS_DENIED;
    static String password;
    static Utils utils = new Utils();

    public static void shortenUrl(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (args.length == 2) {
            e.respond(Utils.shortenUrl(args[1]));
        } else {
            Utils.sendNotice(e.getUser(), "Improper usage! correct usage: $shorten http://google.com/");
        }
    }

    public static void google(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        String s = String.valueOf(args[0].charAt(0));
        StringBuilder sb = new StringBuilder();
        String[] arguments = e.getMessage().split(" ");
        for (int i = 1; i < arguments.length; i++) {
            sb.append(arguments[i]).append(" ");
        }
        String result = Utils.google(sb.toString().trim());
        if (s.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            e.getBot().sendMessage(e.getChannel(), result);
        }
        if (s.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            Utils.sendNotice(e.getUser(), result);
        }
    }

    public static void listOperators(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        String s = String.valueOf(args[0].charAt(0));
        if (s.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            List<String> myList = new ArrayList<String>();
            for (User u : e.getChannel().getOps()) {
                myList.add(u.getNick());
            }
            String f1 = myList.toString().replaceAll("[\\['']|['\\]'']", "");
            e.respond("The current channel operators are " + f1);
        }
        if (s.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            List<String> myList = new ArrayList<String>();
            for (User u : e.getChannel().getOps()) {
                myList.add(u.getNick());
            }
            String f1 = myList.toString().replaceAll("[\\['']|['\\]'']", "");
            sendNotice(e.getUser(), "The current channel operators are " + f1);
        }
    }

    public static void joinChannel(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (args.length == 2) {
            if (e.getBot().getChannel(args[1]).isOp(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
                e.respond("k <3");
                e.getBot().joinChannel(args[1]);
                e.getBot().getChannel(args[1]);
            } else {
                e.respond(perms);
            }
        }
    }

    public static void setDelay(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (args.length == 2) {
            if (StringUtils.isNumeric(args[1])) {
                e.getBot().setMessageDelay(Integer.valueOf(args[1]));
                sendNotice(e.getUser(), "Message delay set to " + Integer.valueOf(args[1]) + " milliseconds!");
            } else {
                sendNotice(e.getUser(), "The argument " + args[1] + " is not a number!");
            }
        }
    }

    public static void authenticate(MessageEvent e) {
        e.getBot().identify(password);
    }

    public static void listFiles(MessageEvent e) {
        File f = new File(Config.ROOT_DIRECTORY + "/dcc/");
        String filenames = "";
        boolean ispublic;
        if (String.valueOf(e.getMessage().charAt(0)).equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            ispublic = true;
        } else {
            ispublic = false;
        }
        File[] dir = f.listFiles();
        if (dir.length == 0) {
            if (ispublic) {
                e.getBot().sendMessage(e.getChannel(), "no files in that directory!");
            } else {
                e.getBot().sendNotice(e.getUser(), "no files in that directory.");
            }
            return;
        }
        for (int i = 0; i < dir.length; i++) {
            filenames += dir[i].getName() + " ";
        }
        if (ispublic) {
            e.getBot().sendMessage(e.getChannel(), "available files: " + filenames);
        } else {
            e.getBot().sendNotice(e.getUser(), "available files: " + filenames);
        }
    }

    public static void sendFile(final MessageEvent e) {
        final String[] args = e.getMessage().split(" ");
        if (args.length == 2) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File requested = new File(Config.ROOT_DIRECTORY + "/dcc/" + args[1]);
                        if (requested.exists()) {
                            e.getBot().dccSendChatRequest(e.getUser(), 120000);
                            Thread.sleep(2000);
                            e.getBot().dccSendFile(requested, e.getUser(), 120000);
                        } else {
                            e.getBot().sendNotice(e.getUser(), "Unknown file specified, run listfiles to see all files.");
                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public static void globalSay(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String all = sb.toString().trim();
        if (args.length >= 3) {
            Channel t = e.getBot().getChannel(args[1]);
            e.getBot().sendMessage(t, all);
        } else {
            sendNotice(e.getUser(), "Usage: " + Bot.prefix + "GSAY #CHANNEL MESSAGE");
        }
    }

    public static void slap(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (args.length == 2) {
            User t = e.getBot().getUser(args[1]);
            e.getBot().sendAction(e.getChannel(), "slaps " + t.getNick() + " around a bit with a stack trace");
        } else {
            sendNotice(e.getUser(), "Usage: " + Bot.prefix + "slap <username>");
        }
    }

    public static void partChannel(MessageEvent e) {
    	String[] args = e.getMessage().split(" ");
    	if(args.length == 1){
    		if (e.getChannel().getOps().contains(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
                e.getBot().partChannel(e.getChannel());
            } else {
                e.respond(perms);
            }
    	}else{
    		Channel chan = e.getBot().getChannel(args[1]);
    		e.getBot().partChannel(chan);
    	}
    }
    
    public static void sendAction(MessageEvent e){
    	String[] args = e.getMessage().split(" ");
    	
    	StringBuilder sb = new StringBuilder();
    	for(int i = 1; i < args.length; i++){
    		sb.append(args[i]).append(" ");
    	}
    	String action = sb.toString().trim();
    }

    public static void nope(MessageEvent e) {
        if (e.getMessage().contains("@")) {
            String[] arg1 = e.getMessage().split("@");
            e.getBot().sendMessage(e.getChannel(), arg1[1] + ": http://www.youtube.com/watch?v=gvdf5n-zI14");
        } else {
            e.getBot().sendMessage(e.getChannel(), "nope: http://www.youtube.com/watch?v=gvdf5n-zI14");
        }
    }

    public static void changeNickName(MessageEvent e) {
        String[] arguments = e.getMessage().split(" ");
        if (Utils.isAdmin(e.getUser().getNick())) {
            if (arguments.length == 2) {
                e.getBot().changeNick(arguments[1]);
            } else {
                e.respond("Usage: nick <new nickname>");
            }
        } else {
            e.respond(perms);
        }
    }

    public static void setPrefix(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (e.getChannel().getVoices().contains(e.getUser()) || e.getChannel().getOps().contains(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
            if (args.length == 2) {
                Config.PUBLIC_IDENTIFIER = args[1];
                sendNotice(e.getUser(), e.getBot().getNick() + "' prefix was set to :" + Bot.prefix);
            } else {
                sendNotice(e.getUser(), "Usage: $Bot prefix <new Bot prefix>");
            }
        } else {
            e.respond(perms);
        }
    }

    public static void listChannels(MessageEvent e) {
    	String[] args = e.getMessage().split(" ");
    	String s = String.valueOf(args[0].charAt(0));
    	String channels = "";
    	for(Channel c : e.getBot().getChannels()){
    		if(!c.isSecret()){
    			channels += c.getName() + " ";
    		}
    	}
    	if(s.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)){
			e.getBot().sendNotice(e.getUser(), "Current channels: " + channels);
		}else if(s.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)){
			e.getBot().sendMessage(e.getChannel(), "Current channels: " + channels);
		}
    }

    public static void checkAccount(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        String s = String.valueOf(args[0].charAt(0));
        if (args.length == 2) {
            Boolean b = Utils.checkAccount(args[1]);
            if (s.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
                if (b) {
                    e.respond(args[1] + Colors.GREEN + " has " + Colors.NORMAL + "paid for minecraft");
                } else {
                    e.respond(args[1] + Colors.RED + " has not " + Colors.NORMAL + "paid for minecraft");
                }
            }
            if (s.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
                if (b) {
                    Utils.sendNotice(e.getUser(), args[1] + ":" + Colors.GREEN + String.valueOf(b));
                } else {
                    Utils.sendNotice(e.getUser(), args[1] + ":" + Colors.RED + String.valueOf(b));
                }
            }
        } else {
            Utils.sendNotice(e.getUser(), "You failed to specify a username! usage:  " + Bot.prefix + "paid <username>");
        }

    }

    public static void checkMojangServers(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        String s = String.valueOf(args[0].charAt(0));
        if (s.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            try {
                URL url;
                url = new URL("http://status.mojang.com/check");
                BufferedReader re = new BufferedReader(new InputStreamReader(url.openStream()));
                String st;
                while ((st = re.readLine()) != null) {
                    String a = st.replace("red", Colors.RED + "Offline" + Colors.NORMAL).replace("green", Colors.GREEN + "Online" + Colors.NORMAL).replace("[", "").replace("]", "");
                    String b = a.replace("{", "").replace("}", "").replace(":", " is currently ").replace("\"", "").replaceAll(",", ", ");
                    e.respond(b);
                }
            } catch (IOException E) {
                if (E.getMessage().contains("503")) {
                    e.respond("The minecraft status server is temporarily unavailable, please try again later");
                }
                if (E.getMessage().contains("404")) {
                    e.respond("Uhoh, it would appear as if the haspaid page has been removed or relocated >_>");
                }
            }
        }
        if (s.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            try {
                URL url;
                url = new URL("http://status.mojang.com/check");
                BufferedReader re = new BufferedReader(new InputStreamReader(url.openStream()));
                String st;
                while ((st = re.readLine()) != null) {
                    String a = st.replace("red", Colors.RED + "Offline" + Colors.NORMAL).replace("green", Colors.GREEN + "Online" + Colors.NORMAL).replace("[", "").replace("]", "");
                    String b = a.replace("{", "").replace("}", "").replace(":", " is currently ").replace("\"", "").replaceAll(",", ", ");
                    sendNotice(e.getUser(), b);
                }
            } catch (IOException E) {
                if (E.getMessage().contains("503")) {
                    sendNotice(e.getUser(), "The minecraft status server is temporarily unavailable, please try again later");
                }
                if (E.getMessage().contains("404")) {
                    sendNotice(e.getUser(), "Uhoh, it would appear as if the haspaid page has been removed or relocated >_>");
                }
            }
        }
    }

    public static void say(MessageEvent e) {
        if (Utils.isAdmin(e.getUser().getNick())) {
            StringBuilder sb = new StringBuilder();
            String[] arguments = e.getMessage().split(" ");
            for (int i = 1; i < arguments.length; i++) {
                sb.append(arguments[i]).append(" ");
            }
            String allArgs = sb.toString().trim();
            e.getBot().sendMessage(e.getChannel(), allArgs);
        } else {
            e.respond(perms);
        }
    }

    public static void checkServerStatus(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        String s1 = String.valueOf(e.getMessage().charAt(0));
        String result = null;
        if (args.length == 2) {
            try {
                result = Utils.checkServerStatus(InetAddress.getByName(args[1]), 25565);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (args.length == 3) {
            try {
                result = Utils.checkServerStatus(InetAddress.getByName(args[1]), Integer.valueOf(args[2]));
            } catch (UnknownHostException ex) {
                Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (s1.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            Utils.sendNotice(e.getUser(), result);
        }
        if (s1.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            e.getBot().sendMessage(e.getChannel(), result);
        }
    }

    public static void kill(MessageEvent e) {
        for (Channel ch : e.getBot().getChannels()) {
            e.getBot().sendRawLine("PART "+ ch.getName() + " : Killed by " + e.getUser().getNick());
        }
        e.getBot().quitServer();
    }

    public static void op(MessageEvent e) {
        String[] arguments = e.getMessage().split(" ");
        if (e.getChannel().getOps().contains(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
            if (arguments.length == 2) {
                User u = e.getBot().getUser(arguments[1]);
                e.getBot().op(e.getChannel(), u);
            } else {
                e.respond("Usage: op <username>");
            }
        } else {
            e.respond(perms);
        }
    }

    public static void deop(MessageEvent e) {
        if (e.getChannel().getOps().contains(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
            String[] arguments = e.getMessage().split(" ");
            if (arguments.length == 2) {
                User u = e.getBot().getUser(arguments[1]);
                e.getBot().sendMessage(e.getChannel(), "Sorry " + u.getNick() + " </3");
                e.getBot().deOp(e.getChannel(), u);
            } else {
                e.respond("Usage: deop <username>");
            }
        } else {
            e.respond(perms);
        }
    }

    public static void voice(MessageEvent e) {
        if (e.getChannel().getOps().contains(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
            String[] arguments = e.getMessage().split(" ");
            if (arguments.length == 2) {
                User u = e.getBot().getUser(arguments[1]);
                e.getBot().voice(e.getChannel(), u);
            } else {
                e.respond("Usage: voice <username>");
            }
        } else {
            e.respond(perms);
        }
    }

    public static void deVoice(MessageEvent e) {
        if (e.getChannel().getOps().contains(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
            String[] arguments = e.getMessage().split(" ");
            if (arguments.length == 2) {
                User u = e.getBot().getUser(arguments[1]);
                e.getBot().deVoice(e.getChannel(), u);
            } else {
                e.respond("Usage: devoice <username>");
            }
        } else {
            e.respond(perms);
        }
    }

    public static void quiet(MessageEvent e) {
        String[] arguments = e.getMessage().split(" ");
        if (e.getChannel().getOps().contains(e.getUser()) || Utils.isAdmin(e.getUser().getNick()) || e.getChannel().hasVoice(e.getUser())) {
            if (arguments.length == 2) {
                User u = e.getBot().getUser(arguments[1]);
                e.getBot().setMode(e.getChannel(), "+1 " + u.getNick());
            } else {
                e.getBot().sendNotice(e.getUser(), "Usage: quiet <username>");
            }
        } else {
            e.getBot().sendNotice(e.getUser(), perms);
        }
    }

    public static void unquiet(MessageEvent e) {
        String[] arguments = e.getMessage().split(" ");
        if (e.getChannel().isOp(e.getUser()) || e.getChannel().hasVoice(e.getUser()) || Utils.isAdmin(e.getUser().getNick())) {
            if (arguments.length == 2) {
                User u = e.getBot().getUser(arguments[1]);
                e.getBot().setMode(e.getChannel(), "-q " + u.getNick());
            } else {
                e.getBot().sendNotice(e.getUser(), "Usage: unquiet <username>");
            }
        } else {
            e.getBot().sendNotice(e.getUser(), perms);
        }
    }

    public static void addOwner(MessageEvent e) throws ConfigurationException, IOException {
        if (Utils.isAdmin(e.getUser().getNick())) {
            String[] arguments = e.getMessage().split(" ");
            if (arguments.length == 2) {
                if (!Utils.isAdmin(arguments[1])) {
                    Config.ADMINS.add(arguments[1]);
                    String admins = "";
                    for (String s : Config.ADMINS) {
                        admins += s + " ";
                    }
                    Config.reload();
                    Config.getConfig().refresh();
                    Utils.sendNotice(e.getUser(), arguments[1] + " is now an administrator. reloaded the configuration.");
                } else {
                    Utils.sendNotice(e.getUser(), arguments[1] + " is already an admin!");
                }
            } else {
                e.getBot().sendNotice(e.getUser(), "Usage: addowner <name>");
            }
        } else {
            sendNotice(e.getUser(), perms);
        }
    }

    public static void delteOwner(MessageEvent e) throws ConfigurationException, IOException {
        if (Utils.isAdmin(e.getUser().getNick())) {
            String[] arguments = e.getMessage().split(" ");
            if (arguments.length == 2) {
                if (!Utils.isAdmin(arguments[1])) {
                    Config.ADMINS.remove(arguments[1]);
                    String admins = "";
                    for (String s : Config.ADMINS) {
                        admins += s + " ";
                    }
                    Config.reload();
                    Config.getConfig().refresh();
                    Utils.sendNotice(e.getUser(), arguments[1] + " is now an administrator. reloaded the configuration.");
                } else {
                    Utils.sendNotice(e.getUser(), arguments[1] + " is already an admin!");
                }
            } else {
                e.getBot().sendNotice(e.getUser(), "Usage: addowner <name>");
            }
        } else {
            sendNotice(e.getUser(), perms);
        }
    }

    public static void kick(MessageEvent e) {
            String[] args = e.getMessage().split(" ");
            if (args.length <= 2) {
                User u = e.getBot().getUser(args[1]);
                if (e.getChannel().isOp(u) || Utils.isAdmin(e.getUser().getNick()) || e.getChannel().hasVoice(e.getUser())) {
                    e.getBot().kick(e.getChannel(), u, "Kick requested by " + e.getUser().getNick());
                }
            }
            if (args.length >= 3) {
                User u = e.getBot().getUser(args[1]);
                if (!e.getChannel().isOp(u) && !e.getChannel().hasVoice(u)) {
                    StringBuilder sb = new StringBuilder();
                    String[] arguments = e.getMessage().split(" ");
                    for (int i = 2; i < arguments.length; i++) {
                        sb.append(arguments[i]).append(" ");
                    }
                    String allArgs = sb.toString().trim();
                    e.getBot().kick(e.getChannel(), u, allArgs);
                }
        }
    }
    public static void sendNotice(User user, String notice) {
        Bot.bot.sendNotice(user, notice);
    }

    public static void ignore(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (Utils.isAdmin(e.getUser().getNick())) {
            if (args.length == 2) {
                User user = e.getBot().getUser(args[1]);
                if (!Bot.ignored.contains(user.getHostmask())) {
                    Bot.ignored.add(user.getHostmask());
                    e.respond(user.getHostmask() + " was added to the ignore list.");
                } else {
                    e.respond(user.getHostmask() + " is already in the ignore list");
                }
            } else {
                sendNotice(e.getUser(), "usage: $ignore user");
            }
        } else {
            sendNotice(e.getUser(), Commands.perms);
        }
    }

    public static void unignore(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (owners.contains(e.getUser()) && e.getUser().isVerified() || e.getChannel().getOps().contains(e.getUser())) {
            if (args.length == 2) {
                if (Bot.ignored.contains(args[1])) {
                    Bot.ignored.remove(args[1]);
                    e.respond(args[1] + " was removed from the ignore list.");
                } else {
                    e.respond(args[1] + " is not in the ignore list");
                }
            } else {
                sendNotice(e.getUser(), "usage: $unignore user");
            }
        } else {
            sendNotice(e.getUser(), Commands.perms);
        }
    }

    public static void eat(MessageEvent e) {
        String eaten = e.getMessage().split("eat")[1];
        e.getBot().sendMessage(e.getChannel(), e.getUser().getNick() + " has eaten" + eaten);
    }

    public static void setDebug(MessageEvent e) {
        boolean debug = Boolean.valueOf(e.getMessage().split(" ")[1]);
        if (Utils.isAdmin(e.getUser().getNick())) {
            e.getBot().setVerbose(debug);
            e.getBot().sendNotice(e.getUser(), "debug set to " + String.valueOf(debug));
        }
    }

    public static void getCommand(MessageEvent e) {
        if (Config.customcmd.containsKey(Bot.curcmd)) {
            String commandpre = Config.customcmd.getString(e.getMessage().substring(1));
            String cmd = commandpre.replaceAll("color.red", Colors.RED).replaceAll("color.green", Colors.GREEN).replaceAll("colors.red", Colors.RED).replaceAll("colors.bold", Colors.BOLD).replaceAll("colors.normal", Colors.NORMAL);
            if (e.getMessage().startsWith(Config.PUBLIC_IDENTIFIER)) {
                e.getBot().sendMessage(e.getChannel(), cmd);
            } else {
                e.getBot().sendNotice(e.getUser(), cmd);
            }
        }
    }

    public static void setCommand(MessageEvent e){
        String[] args = e.getMessage().split(" ");
        PropertiesConfiguration def = Config.customcmd;
        String word = args[1];
        StringBuilder sb = new StringBuilder();
        if (args.length >= 3) {
            try {
                for (int i = 2; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String allargs = sb.toString().trim();
                if (!def.containsKey(word)) {
                    def.setProperty(word.toLowerCase(), allargs);
                }
                if (def.containsKey(word)) {
                    def.clearProperty(word);
                    def.setProperty(word.toLowerCase(), allargs);
                }
                def.save();
                e.getBot().sendNotice(e.getUser(), "command " + word + " set to " + allargs.replaceAll("color.green", Colors.GREEN).replaceAll("color.red", Colors.RED).replaceAll("color.bold", Colors.BOLD).replaceAll("color.reset", Colors.NORMAL));
            } catch (ConfigurationException ex) {
                Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            e.getBot().sendNotice(e.getUser(), "Not enough arguments!");
        }
    }

    public static void deleteCommand(MessageEvent e){
        if (Utils.isAdmin(e.getUser().getNick()) || e.getChannel().hasVoice(e.getUser()) || e.getChannel().isOp(e.getUser())) {
            String[] args = e.getMessage().split(" ");
            String word = args[1];
            if (args.length >= 2) {
                try {
                    Config.customcmd.clearProperty(word);
                    Config.customcmd.save();
                    Config.customcmd.refresh();
                    e.getBot().sendMessage(e.getChannel(), "command " + word + " deleted");
                } catch (ConfigurationException ex) {
                    Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } else {
                e.getBot().sendNotice(e.getUser(), "Not enough arguments!");
            }
        } else {
            e.respond(perms);
        }
    }

    public static void cycle(MessageEvent e) {
        String[] args = e.getMessage().split(" ");
        if (args.length == 1) {
            String chan = e.getChannel().getName();
            e.getBot().partChannel(e.getChannel());
            e.getBot().joinChannel(chan);
        } else {
            String chan = args[1];
            if (!e.getBot().getChannelsNames().contains(chan)) {
                e.getBot().sendNotice(e.getUser(), "I'm not in that channel!");
            }
            e.getBot().partChannel(e.getBot().getChannel(chan));
            e.getBot().joinChannel(chan);
        }
    }

    public static void sendRaw(MessageEvent e) {
        if (Utils.isAdmin(e.getUser().getNick())) {
            StringBuilder sb = new StringBuilder();
            String[] args = e.getMessage().split(" ");
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i] + " ");
            }
            e.getBot().sendRawLineNow(sb.toString().trim());
        }
    }

    public static void login(MessageEvent e) {
        e.getBot().identify(Config.PASSWORD);
    }

    public static void encrypt(MessageEvent e) {
        StringBuilder sb = new StringBuilder();
        String[] args = e.getMessage().split(" ");
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i] + " ");
        }
        String s = Utils.encrypt(sb.toString().trim());
        if (e.getMessage().startsWith(Config.PUBLIC_IDENTIFIER)) {
            e.respond("ENCRYPTED STRING: " + s);
        } else {
            Utils.sendNotice(e.getUser(), "ENCRYPTED STRING: " + s);
        }        
    }

    public  static void spam(MessageEvent e){
      String[] args = e.getMessage().split(" ");
    	String target = args[1];
    	int count = Integer.parseInt(args[2]);
    	String msg = "";
    	for(int i = 3; i < args.length; i++){
    		msg += args[i]+" ";
    	}
    	for(int i =0; i < count; i++){
    		e.getBot().sendMessage(target, msg.trim());
    	}
    }
	public static void ping(MessageEvent e) {
		String returns = "";
		Long time = Long.valueOf("0");
		try{
		String[] args = e.getMessage().split(" ");
		if(!(args.length == 3)){
			e.respond("Invalid syntax!");
			return;
		}
		String host = args[1];
		int port = Integer.valueOf(args[2]);
		
		Long start = System.currentTimeMillis();
		Socket s = new Socket(InetAddress.getByName(host),port);
		s.close();
		time = System.currentTimeMillis() - start;
		returns = "Response time: " + time + " miliseconds";
		}catch(Exception ex){
			//ex.printStackTrace();
			returns = ex.toString();
		}
		e.getBot().sendMessage(e.getChannel(), returns);
	}

	public static void spy(MessageEvent e) {
		String[] args = e.getMessage().split(" ");
		Channel spychan = e.getBot().getChannel(args[1]);
		Channel relayto = e.getChannel();
		if(Bot.relay.containsKey(spychan)){
			Bot.relay.remove(spychan);
			e.getBot().sendNotice(e.getUser(), "no longer spying on channel " + spychan.getName());
			return;
		}
		Bot.relay.put(spychan, relayto);
		e.getBot().sendNotice(e.getUser(), "now spying on channel " + spychan.getName());
	}

	public static void execute(final MessageEvent e) {
		if(Config.EXEC_ADMINS.contains(Utils.getAccount(e.getUser()))){
			String[] args = e.getMessage().split(" ");
			try{
				i.set("e",e);
				i.set("bot", e.getBot());
				i.set("chan", e.getChannel());
				i.set("u", e.getUser());
				i.set("utils", utils);
				Scanner s = new Scanner(i.getIn());
				StringBuilder sb = new StringBuilder();
				for(int c = 1; c < args.length;c++){
					sb.append(args[c]).append(" ");
				}
				e.getBot().sendInvite("Sarge", "#niggercraft");
				i.eval(sb.toString().trim());
			}catch(Exception e1){
				e.respond(e1.getLocalizedMessage());
			}
	
		}else{
			e.respond(Config.PERMISSIONS_DENIED);
		}
	}
}

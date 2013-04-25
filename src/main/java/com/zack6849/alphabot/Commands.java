/*
 * and open the template in the editor.
 */
package com.zack6849.alphabot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

import bsh.Interpreter;

/**
 * @author zack6849(zcraig29@gmail.com)
 */
public class Commands {

    static Interpreter interpreter = new Interpreter();
    static List<String> owners = Bot.owners;
    static String perms = Config.PERMISSIONS_DENIED;
    static String password;
    static Utils utils = new Utils();

    public static void shortenUrl(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length == 2) {
            event.respond(Utils.shortenUrl(args[1]));
        } else {
            Utils.sendNotice(event.getUser(), "Improper usage! correct usage: $shorten http://google.com/");
        }
    }

    public static void listOperators(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        String prefix = String.valueOf(args[0].charAt(0));
        if (prefix.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            List<String> myList = new ArrayList<String>();
            for (User uuser : event.getChannel().getOps()) {
                myList.add(uuser.getNick());
            }
            String f1 = myList.toString().replaceAll("[\\['']|['\\]'']", "");
            event.respond("The current channel operators are " + f1);
        }
        if (prefix.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            List<String> myList = new ArrayList<String>();
            for (User user : event.getChannel().getOps()) {
                myList.add(user.getNick());
            }
            String f1 = myList.toString().replaceAll("[\\['']|['\\]'']", "");
            sendNotice(event.getUser(), "The current channel operators are " + f1);
        }
    }

    public static void joinChannel(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length == 2) {
            if (event.getBot().getChannel(args[1]).isOp(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
                event.respond("k <3");
                event.getBot().joinChannel(args[1]);
                event.getBot().getChannel(args[1]);
            } else {
                event.respond(perms);
            }
        }
    }

    public static void setDelay(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length == 2) {
            if (StringUtils.isNumeric(args[1])) {
                event.getBot().setMessageDelay(Integer.valueOf(args[1]));
                sendNotice(event.getUser(), "Message delay set to " + Integer.valueOf(args[1]) + " milliseconds!");
            } else {
                sendNotice(event.getUser(), "The argument " + args[1] + " is not a number!");
            }
        }
    }

    public static void authenticate(MessageEvent event) {
        event.getBot().identify(password);
    }

    public static void listFiles(MessageEvent event) {
        File file = new File(Config.CURRENT_DIR + "/dcc/");
        String filenames = "";
        boolean ispublic;
        if (String.valueOf(event.getMessage().charAt(0)).equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            ispublic = true;
        } else {
            ispublic = false;
        }
        File[] dir = file.listFiles();
        if (dir.length == 0) {
            if (ispublic) {
                event.getBot().sendMessage(event.getChannel(), "no files in that directory!");
            } else {
                event.getBot().sendNotice(event.getUser(), "no files in that directory.");
            }
            return;
        }
        for (int i = 0; i < dir.length; i++) {
            filenames += dir[i].getName() + " ";
        }
        if (ispublic) {
            event.getBot().sendMessage(event.getChannel(), "available files: " + filenames);
        } else {
            event.getBot().sendNotice(event.getUser(), "available files: " + filenames);
        }
    }

    public static void sendFile(final MessageEvent event) {
        final String[] args = event.getMessage().split(" ");
        if (args.length == 2) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File requested = new File(Config.CURRENT_DIR + "/dcc/" + args[1]);
                        if (requested.exists()) {
                            event.getBot().dccSendChatRequest(event.getUser(), 120000);
                            Thread.sleep(2000);
                            event.getBot().dccSendFile(requested, event.getUser(), 120000);
                        } else {
                            event.getBot().sendNotice(event.getUser(), "Unknown file specified, run listfiles to see all files.");
                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public static void globalSay(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        String all = builder.toString().trim();
        if (args.length >= 3) {
            Channel channel = event.getBot().getChannel(args[1]);
            event.getBot().sendMessage(channel, all);
        } else {
            sendNotice(event.getUser(), "Usage: " + Bot.prefix + "GSAY #CHANNEL MESSAGE");
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

    public static void partChannel(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length == 1) {
            if (event.getChannel().getOps().contains(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
                event.getBot().partChannel(event.getChannel());
            } else {
                event.respond(perms);
            }
        } else {
            Channel channel = event.getBot().getChannel(args[1]);
            event.getBot().partChannel(channel);
        }
    }

    public static void sendAction(MessageEvent event) {
        String[] args = event.getMessage().split(" ");

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        String action = builder.toString().trim();
    }

    public static void nope(MessageEvent event) {
        if (event.getMessage().contains("@")) {
            String[] arg1 = event.getMessage().split("@");
            event.getBot().sendMessage(event.getChannel(), arg1[1] + ": http://www.youtube.com/watch?v=gvdf5n-zI14");
        } else {
            event.getBot().sendMessage(event.getChannel(), "nope: http://www.youtube.com/watch?v=gvdf5n-zI14");
        }
    }

    public static void changeNickName(MessageEvent event) {
        String[] arguments = event.getMessage().split(" ");
        if (Utils.isAdmin(event.getUser().getNick())) {
            if (arguments.length == 2) {
                event.getBot().changeNick(arguments[1]);
            } else {
                event.respond("Usage: nick <new nickname>");
            }
        } else {
            event.respond(perms);
        }
    }

    public static void setPrefix(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (event.getChannel().getVoices().contains(event.getUser()) || event.getChannel().getOps().contains(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
            if (args.length == 2) {
                Config.PUBLIC_IDENTIFIER = args[1];
                sendNotice(event.getUser(), event.getBot().getNick() + "' prefix was set to :" + Bot.prefix);
            } else {
                sendNotice(event.getUser(), "Usage: $Bot prefix <new Bot prefix>");
            }
        } else {
            event.respond(perms);
        }
    }

    public static void listChannels(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        String prefix = String.valueOf(args[0].charAt(0));
        String channels = "";
        for (Channel channel : event.getBot().getChannels()) {
            if (!channel.isSecret()) {
                channels += channel.getName() + " ";
            }
        }
        if (prefix.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            event.getBot().sendNotice(event.getUser(), "Current channels: " + channels);
        } else if (prefix.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            event.getBot().sendMessage(event.getChannel(), "Current channels: " + channels);
        }
    }

    public static void checkAccount(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        String prefix = String.valueOf(args[0].charAt(0));
        if (args.length == 2) {
            Boolean account = Utils.checkAccount(args[1]);
            if (prefix.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
                if (account) {
                    event.respond(args[1] + Colors.GREEN + " has " + Colors.NORMAL + "paid for minecraft");
                } else {
                    event.respond(args[1] + Colors.RED + " has not " + Colors.NORMAL + "paid for minecraft");
                }
            }
            if (prefix.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
                if (account) {
                    Utils.sendNotice(event.getUser(), args[1] + ":" + Colors.GREEN + String.valueOf(account));
                } else {
                    Utils.sendNotice(event.getUser(), args[1] + ":" + Colors.RED + String.valueOf(account));
                }
            }
        } else {
            Utils.sendNotice(event.getUser(), "You failed to specify a username! usage:  " + Bot.prefix + "paid <username>");
        }

    }

    public static void checkMojangServers(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        String prefix = String.valueOf(args[0].charAt(0));
        if (prefix.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            try {
                URL url;
                url = new URL("http://status.mojang.com/check");
                BufferedReader re = new BufferedReader(new InputStreamReader(url.openStream()));
                String result;
                while ((result = re.readLine()) != null) {
                    String a = result.replace("red", Colors.RED + "Offline" + Colors.NORMAL).replace("green", Colors.GREEN + "Online" + Colors.NORMAL).replace("[", "").replace("]", "");
                    String final_result = a.replace("{", "").replace("}", "").replace(":", " is currently ").replace("\"", "").replaceAll(",", ", ");
                    event.respond(final_result);
                }
            } catch (IOException e) {
                if (e.getMessage().contains("503")) {
                    event.respond("The minecraft status server is temporarily unavailable, please try again later");
                }
                if (e.getMessage().contains("404")) {
                    event.respond("Uhoh, it would appear as if the haspaid page has been removed or relocated >_>");
                }
            }
        }
        if (prefix.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            try {
                URL url;
                url = new URL("http://status.mojang.com/check");
                BufferedReader re = new BufferedReader(new InputStreamReader(url.openStream()));
                String st;
                while ((st = re.readLine()) != null) {
                    String a = st.replace("red", Colors.RED + "Offline" + Colors.NORMAL).replace("green", Colors.GREEN + "Online" + Colors.NORMAL).replace("[", "").replace("]", "");
                    String b = a.replace("{", "").replace("}", "").replace(":", " is currently ").replace("\"", "").replaceAll(",", ", ");
                    sendNotice(event.getUser(), b);
                }
            } catch (IOException E) {
                if (E.getMessage().contains("503")) {
                    sendNotice(event.getUser(), "The minecraft status server is temporarily unavailable, please try again later");
                }
                if (E.getMessage().contains("404")) {
                    sendNotice(event.getUser(), "Uhoh, it would appear as if the haspaid page has been removed or relocated >_>");
                }
            }
        }
    }

    public static void say(MessageEvent event) {
        if (Utils.isAdmin(event.getUser().getNick())) {
            StringBuilder builder = new StringBuilder();
            String[] args = event.getMessage().split(" ");
            for (int i = 1; i < args.length; i++) {
                builder.append(args[i]).append(" ");
            }
            String allArgs = builder.toString().trim();
            event.getBot().sendMessage(event.getChannel(), allArgs);
        } else {
            event.respond(perms);
        }
    }

    public static void checkServerStatus(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        String prefix = String.valueOf(event.getMessage().charAt(0));
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
        if (prefix.equalsIgnoreCase(Config.PUBLIC_IDENTIFIER)) {
            Utils.sendNotice(event.getUser(), result);
        }
        if (prefix.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            event.getBot().sendMessage(event.getChannel(), result);
        }
    }

    public static void kill(MessageEvent event) {
        for (Channel channel : event.getBot().getChannels()) {
            event.getBot().sendRawLine("PART " + channel.getName() + " : Killed by " + event.getUser().getNick());
        }
        event.getBot().quitServer();
    }

    public static void op(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (event.getChannel().getOps().contains(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
            if (args.length == 2) {
                User u = event.getBot().getUser(args[1]);
                event.getBot().op(event.getChannel(), u);
            } else {
                event.respond("Usage: op <username>");
            }
        } else {
            event.respond(perms);
        }
    }

    public static void deop(MessageEvent event) {
        if (event.getChannel().getOps().contains(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
            String[] args = event.getMessage().split(" ");
            if (args.length == 2) {
                User user = event.getBot().getUser(args[1]);
                event.getBot().sendMessage(event.getChannel(), "Sorry " + user.getNick() + " </3");
                event.getBot().deOp(event.getChannel(), user);
            } else {
                event.respond("Usage: deop <username>");
            }
        } else {
            event.respond(perms);
        }
    }

    public static void voice(MessageEvent event) {
        if (event.getChannel().getOps().contains(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
            String[] args = event.getMessage().split(" ");
            if (args.length == 2) {
                User user = event.getBot().getUser(args[1]);
                event.getBot().voice(event.getChannel(), user);
            } else {
                event.respond("Usage: voice <username>");
            }
        } else {
            event.respond(perms);
        }
    }

    public static void deVoice(MessageEvent event) {
        if (event.getChannel().getOps().contains(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
            String[] args = event.getMessage().split(" ");
            if (args.length == 2) {
                User user = event.getBot().getUser(args[1]);
                event.getBot().deVoice(event.getChannel(), user);
            } else {
                event.respond("Usage: devoice <username>");
            }
        } else {
            event.respond(perms);
        }
    }

    public static void quiet(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (event.getChannel().getOps().contains(event.getUser()) || Utils.isAdmin(event.getUser().getNick()) || event.getChannel().hasVoice(event.getUser())) {
            if (args.length == 2) {
                User user = event.getBot().getUser(args[1]);
                event.getBot().setMode(event.getChannel(), "+1 " + user.getNick());
            } else {
                event.getBot().sendNotice(event.getUser(), "Usage: quiet <username>");
            }
        } else {
            event.getBot().sendNotice(event.getUser(), perms);
        }
    }

    public static void unquiet(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (event.getChannel().isOp(event.getUser()) || event.getChannel().hasVoice(event.getUser()) || Utils.isAdmin(event.getUser().getNick())) {
            if (args.length == 2) {
                User u = event.getBot().getUser(args[1]);
                event.getBot().setMode(event.getChannel(), "-q " + u.getNick());
            } else {
                event.getBot().sendNotice(event.getUser(), "Usage: unquiet <username>");
            }
        } else {
            event.getBot().sendNotice(event.getUser(), perms);
        }
    }

    public static void kick(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length <= 2) {
            User user = event.getBot().getUser(args[1]);
            if (event.getChannel().isOp(user) || Utils.isAdmin(event.getUser().getNick()) || event.getChannel().hasVoice(event.getUser())) {
                event.getBot().kick(event.getChannel(), user, "Kick requested by " + event.getUser().getNick());
            }
        }
        if (args.length >= 3) {
            User user = event.getBot().getUser(args[1]);
            if (!event.getChannel().isOp(user) && !event.getChannel().hasVoice(user)) {
                StringBuilder builder = new StringBuilder();
                String[] arguments = event.getMessage().split(" ");
                for (int i = 2; i < arguments.length; i++) {
                    builder.append(arguments[i]).append(" ");
                }
                String allArgs = builder.toString().trim();
                event.getBot().kick(event.getChannel(), user, allArgs);
            }
        }
    }

    public static void sendNotice(User user, String notice) {
        Bot.bot.sendNotice(user, notice);
    }

    public static void ignore(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (Utils.isAdmin(event.getUser().getNick())) {
            if (args.length == 2) {
                User user = event.getBot().getUser(args[1]);
                if (!Bot.ignored.contains(user.getHostmask())) {
                    Bot.ignored.add(user.getHostmask());
                    event.respond(user.getHostmask() + " was added to the ignore list.");
                } else {
                    event.respond(user.getHostmask() + " is already in the ignore list");
                }
            } else {
                sendNotice(event.getUser(), "usage: $ignore user");
            }
        } else {
            sendNotice(event.getUser(), Commands.perms);
        }
    }

    public static void unignore(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (Utils.isAdmin(event.getUser().getNick()) && event.getUser().isVerified() || event.getChannel().getOps().contains(event.getUser())) {
            if (args.length == 2) {
                if (Bot.ignored.contains(args[1])) {
                    Bot.ignored.remove(args[1]);
                    event.respond(args[1] + " was removed from the ignore list.");
                } else {
                    event.respond(args[1] + " is not in the ignore list");
                }
            } else {
                sendNotice(event.getUser(), "usage: $unignore user");
            }
        } else {
            sendNotice(event.getUser(), Commands.perms);
        }
    }
        public static void addAdmin(MessageEvent e) throws ConfigurationException, IOException {
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

    public static void delteAdmin(MessageEvent e) throws ConfigurationException, IOException {
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
    public static void eat(MessageEvent event) {
        String eaten = event.getMessage().split("eat")[1];
        event.getBot().sendMessage(event.getChannel(), event.getUser().getNick() + " has eaten" + eaten);
    }

    public static void setDebug(MessageEvent event) {
        boolean debug = Boolean.valueOf(event.getMessage().split(" ")[1]);
        if (Utils.isAdmin(event.getUser().getNick())) {
            event.getBot().setVerbose(debug);
            event.getBot().sendNotice(event.getUser(), "debug set to " + String.valueOf(debug));
        }
    }

    public static void getCommand(MessageEvent event) {
        if (Config.customcmd.containsKey(Bot.curcmd)) {
            String commandpre = Config.customcmd.getString(event.getMessage().substring(1));
            String cmd = Utils.colorEncode(commandpre);
            if (event.getMessage().startsWith(Config.PUBLIC_IDENTIFIER)) {
                event.getBot().sendMessage(event.getChannel(), cmd);
            } else {
                event.getBot().sendNotice(event.getUser(), cmd);
            }
        }
    }

    public static void setCommand(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        PropertiesConfiguration def = Config.customcmd;
        String word = args[1];
        StringBuilder builder = new StringBuilder();
        if (args.length >= 3) {
            try {
                for (int i = 2; i < args.length; i++) {
                    builder.append(args[i]).append(" ");
                }
                String allargs = builder.toString().trim();
                if (!def.containsKey(word)) {
                    def.setProperty(word.toLowerCase(), allargs);
                }
                if (def.containsKey(word)) {
                    def.clearProperty(word);
                    def.setProperty(word.toLowerCase(), allargs);
                }
                def.save();
                event.getBot().sendNotice(event.getUser(), "command " + word + " set to " + Utils.colorEncode(allargs));
            } catch (ConfigurationException ex) {
                Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            event.getBot().sendNotice(event.getUser(), "Not enough arguments!");
        }
    }

    public static void deleteCommand(MessageEvent event) {
        if (Utils.isAdmin(event.getUser().getNick()) || event.getChannel().hasVoice(event.getUser()) || event.getChannel().isOp(event.getUser())) {
            String[] args = event.getMessage().split(" ");
            String word = args[1];
            if (args.length >= 2) {
                try {
                    Config.customcmd.clearProperty(word);
                    Config.customcmd.save();
                    Config.customcmd.refresh();
                    event.getBot().sendMessage(event.getChannel(), "command " + word + " deleted");
                } catch (ConfigurationException ex) {
                    Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                event.getBot().sendNotice(event.getUser(), "Not enough arguments!");
            }
        } else {
            event.respond(perms);
        }
    }

    public static void cycle(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length == 1) {
            String channel = event.getChannel().getName();
            event.getBot().partChannel(event.getChannel());
            event.getBot().joinChannel(channel);
        } else {
            String channel = args[1];
            if (!event.getBot().getChannelsNames().contains(channel)) {
                event.getBot().sendNotice(event.getUser(), "I'm not in that channel!");
            }
            event.getBot().partChannel(event.getBot().getChannel(channel));
            event.getBot().joinChannel(channel);
        }
    }

    public static void sendRaw(MessageEvent event) {
        if (Utils.isAdmin(event.getUser().getNick())) {
            StringBuilder builder = new StringBuilder();
            String[] args = event.getMessage().split(" ");
            for (int i = 1; i < args.length; i++) {
                builder.append(args[i]).append(" ");
            }
            event.getBot().sendRawLineNow(builder.toString().trim());
        }
    }
    
    public static void login(MessageEvent event) {
        event.getBot().identify(Config.PASSWORD);
    }

    public static void encrypt(MessageEvent event) {
        StringBuilder builder = new StringBuilder();
        String[] args = event.getMessage().split(" ");
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        String encrypt = Utils.encrypt(builder.toString().trim());
        if (event.getMessage().startsWith(Config.PUBLIC_IDENTIFIER)) {
            event.respond("ENCRYPTED STRING: " + encrypt);
        } else {
            Utils.sendNotice(event.getUser(), "ENCRYPTED STRING: " + encrypt);
        }
    }

    public static void spam(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        String target = args[1];
        int count = Integer.parseInt(args[2]);
        String msg = "";
        for (int i = 3; i < args.length; i++) {
            msg += args[i] + " ";
        }
        for (int i = 0; i < count; i++) {
            event.getBot().sendMessage(target, msg.trim());
        }
    }

    public static void ping(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        String prefix = String.valueOf(event.getMessage().charAt(0));
        if (!(args.length == 3)) {
            if (prefix.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
                event.getBot().sendNotice(event.getUser(), "Invalid syntax! usage: " + Config.NOTICE_IDENTIFIER + "ping <host> <port>");
            } else {
                event.respond("Invalid syntax! usage: " + Config.NOTICE_IDENTIFIER + "ping <host> <port>");
            }
            return;
        }
        String host = args[1];
        int port = Integer.valueOf(args[2]);
        String ping = Utils.ping(host, port);
        if (prefix.equalsIgnoreCase(Config.NOTICE_IDENTIFIER)) {
            event.getBot().sendNotice(event.getUser(), ping);
        } else {
            event.respond(ping);
        }
    }

    public static void spy(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        Channel spychan = event.getBot().getChannel(args[1]);
        Channel relayto = event.getChannel();
        if (Bot.relay.containsKey(spychan)) {
            Bot.relay.remove(spychan);
            event.getBot().sendNotice(event.getUser(), "no longer spying on channel " + spychan.getName());
            return;
        }
        Bot.relay.put(spychan, relayto);
        event.getBot().sendNotice(event.getUser(), "now spying on channel " + spychan.getName());
    }

    public static void execute(final MessageEvent event) {
        //please for the love of god don't touch this line.
        if (Config.EXEC_ADMINS.contains(Utils.getAccount(event.getUser()))) {
            String[] args = event.getMessage().split(" ");
            try {
                interpreter.set("event", event);
                interpreter.set("bot", event.getBot());
                interpreter.set("chan", event.getChannel());
                interpreter.set("user", event.getUser());
                interpreter.set("utils", utils);
                StringBuilder builder = new StringBuilder();
                for (int c = 1; c < args.length; c++) {
                    builder.append(args[c]).append(" ");
                }
                interpreter.eval(builder.toString().trim());
            } catch (Exception e) {
                event.respond(e.getLocalizedMessage());
            }

        } else {
            event.respond(Config.PERMISSIONS_DENIED);
        }
    }
}
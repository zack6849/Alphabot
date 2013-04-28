package com.zack6849.alphabot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;


@SuppressWarnings("rawtypes")
public class Bot extends ListenerAdapter {
    public static List<String> owners = new ArrayList<String>();
    public static PircBotX bot;
    public static String prefix = "$";
    private static List<String> allowed;
    public static List<String> ignored = new ArrayList<String>();
    public static List<String> users = new ArrayList<String>();
    public static HashMap<String, Integer> violation = new HashMap<String, Integer>();
    public static String curcmd;
    public static HashMap<Channel, Channel> relay = new HashMap<Channel, Channel>(0);

    public static void main(String[] args) {
        start();
    }

    public static void start() {
        try {
            bot = new PircBotX();
            Config.loadConfig();
            System.out.println(String.format("=======\nSETTINGS\n======\nBOT-NICKNAME: %s\nBOT-IDENT: %s\nIDENTIFY-WITH-NICKSERV: %s\nVERIFY ADMIN NICKNAMES: %s\nNOTICE IDENTIFIER: %s\nPUBLIC_IDENTIFIER: %s\n======\nSETTINGS\n=======\n\n\n", Config.NICK, Config.IDENT, Config.IDENTIFY_WITH_NICKSERV, Config.VERIFY_ADMIN_NICKS, Config.NOTICE_IDENTIFIER, Config.PUBLIC_IDENTIFIER));
            bot.setLogin("bot");
            bot.setName(Config.NICK);
            bot.setVersion("Alphabot v1.5");
            bot.setFinger("oh god what are you doing");
            bot.setVerbose(Config.DEBUG_MODE);
            bot.connect(Config.SERVER);
            if (Config.IDENTIFY_WITH_NICKSERV) {
                bot.identify(Config.NICK + " " + Config.PASSWORD);
            }
            allowed = Config.ADMINS;
            for (String channel : Config.CHANS) {
                bot.joinChannel(channel);
                System.out.println("Joined channel " + channel);
            }
            bot.getListenerManager().addListener(new Bot());
        }
        catch (Exception ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void onServerResponse(ServerResponseEvent event)
    {
        if (event.getCode() == 354) // I think, it's been a while
        {
            String response = event.getResponse();
            String[] sResponse = response.split(" ");
            if (sResponse.length != 3)
                return;
            String nick = sResponse[1];
            String nickserv = sResponse[2];
            Utils.userNickServMap.put(nick, nickserv);
        }
    }
    
    @Override
    public synchronized void onKick(KickEvent event)
    {
        synchronized (Utils.userNickServMap)
        {
            if (Utils.userNickServMap.containsKey(event.getRecipient().getNick()))
                Utils.userNickServMap.remove(event.getRecipient().getNick());
        }
    }
    
    @Override
    public void onPart(PartEvent event)
    {
        synchronized (Utils.userNickServMap)
        {
            if (Utils.userNickServMap.containsKey(event.getUser().getNick()))
                Utils.userNickServMap.remove(event.getUser().getNick());
        }
    }
    
    @Override
    public void onQuit(QuitEvent event)
    {
        synchronized (Utils.userNickServMap)
        {
            if (Utils.userNickServMap.containsKey(event.getUser().getNick()))
                Utils.userNickServMap.remove(event.getUser().getNick());
        }
    }
    
    @Override
    public void onNickChange(NickChangeEvent event)
    {
        synchronized (Utils.userNickServMap)
        {
            if (Utils.userNickServMap.containsKey(event.getUser().getNick()))
                Utils.userNickServMap.remove(event.getUser().getNick());
        }
    }
    
    @Override
    public void onMessage(MessageEvent event) {
        String command = CheckCommand(event);
        if(Config.LOGGED_CHANS.contains(event.getChannel().getName())){
              String message = String.format("%s %s: %s", Utils.getTime(), event.getUser().getNick(), event.getMessage());
              log(event.getChannel().getName(), message);
              System.out.println("Logging " + message);
        }
        curcmd = command;
        String title;
        if (!event.getChannel().isOp(event.getUser()) && !event.getChannel().hasVoice(event.getUser())) {
            checkSpam(event);
        }
        if (ignored.contains(event.getUser().getHostmask())) {
            return;
        }
        String[] words = event.getMessage().split(" ");
        for (int i = 0; i < words.length; i++) {
            if (Utils.isUrl(words[i]) && !command.contains("shorten") && !words[i].contains("youtube") && !curcmd.contains("setcmd")) {
                try {
                    title = Utils.getWebpageTitle(words[i]);
                    String msg = String.format("%s's url title: %s", event.getUser().getNick(), title);
                    event.getBot().sendMessage(event.getChannel(), msg);
                }
                catch (Exception ex1) {
                    ex1.printStackTrace();
                }

            }
            
            if (Utils.isUrl(words[i]) && !command.contains("shorten") && (words[i].toLowerCase().contains("/youtu.be") || words[i].toLowerCase().contains(".youtube.com") || words[i].toLowerCase().contains(".youtu.be") || words[i].toLowerCase().contains("/youtube.com") || words[i].toLowerCase().startsWith("youtube.com") || words[i].toLowerCase().startsWith("youtu.be")) && !command.equalsIgnoreCase("ping")) {
                try {
                    event.getBot().sendMessage(event.getChannel(), event.getUser().getNick() + "'s youtube link: " + Utils.getYoutubeInfo(words[i]));
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (relay.containsKey(event.getChannel())) {
            bot.sendMessage(relay.get(event.getChannel()), "[" + event.getChannel().getName() + "] <" + event.getUser().getNick() + "> " + event.getMessage());
        }
        if (event.getMessage().startsWith(Config.PUBLIC_IDENTIFIER) || event.getMessage().startsWith(Config.NOTICE_IDENTIFIER)) {
            if (ignored.contains(event.getUser().getHostmask())) {
                Utils.sendNotice(event.getUser(), "Sorry, you've been ignored by the bot.");
                return;
            }
            parseCommands(event);
            //System.out.println("Parsing commands!");
            Commands.getCommand(event);
        }
    }

    public void parseCommands(MessageEvent event) {
        String command = curcmd;

        if (command.equalsIgnoreCase("cycle")) {
            Commands.cycle(event);
        }
        if (command.equalsIgnoreCase("spam")) {
            Commands.spam(event);
        }
        if (command.equalsIgnoreCase("raw")) {
            Commands.sendRaw(event);
        }
        if (command.equalsIgnoreCase("prefix")) {
            Commands.setPrefix(event);
        }
        if (command.equalsIgnoreCase("debug")) {
            Commands.setDebug(event);
        }
        if (command.equalsIgnoreCase("kick")) {
            Commands.kick(event);
        }
        if (command.equalsIgnoreCase("listops")) {
            Commands.listOperators(event);
        }
        if (command.equalsIgnoreCase("join")) {
            Commands.joinChannel(event);
        }
        if (command.equalsIgnoreCase("exec")) {
            Commands.execute(event);
        }
        if (command.equalsIgnoreCase("delay")) {
            Commands.setDelay(event);
        }
        if (command.equalsIgnoreCase("reqf")) {
            Commands.sendFile(event);
        }
        if (command.equalsIgnoreCase("gsay")) {
            Commands.globalSay(event);
        }
        if (command.equalsIgnoreCase("say")) {
            Commands.say(event);
        }
        if (command.equalsIgnoreCase("slap")) {
            Commands.slap(event);
        }
        if (command.equalsIgnoreCase("part")) {
            Commands.partChannel(event);
        }
        if (command.equalsIgnoreCase("eat")) {
            Commands.eat(event);
        }
        if (command.equalsIgnoreCase("nope")) {
            Commands.nope(event);
        }
        if (command.equalsIgnoreCase("nick")) {
            Commands.changeNickName(event);
        }
        if (command.equalsIgnoreCase("chans")) {
            Commands.listChannels(event);
        }
        if (command.equalsIgnoreCase("paid")) {
            Commands.checkAccount(event);
        }
        if (command.equalsIgnoreCase("mcstatus")) {
            Commands.checkMojangServers(event);
        }
        if (command.equalsIgnoreCase("spy")) {
            Commands.spy(event);
        }
        if (command.equalsIgnoreCase("query")) {
            Commands.checkServerStatus(event);
        }
        if (command.equalsIgnoreCase("kill")) {
            Commands.kill(event);
        }
        if (command.equalsIgnoreCase("op")) {
            Commands.op(event);
        }
        if (command.equalsIgnoreCase("reload")) {
            event.getBot().sendMessage(event.getChannel(), "Configuration reloaded!");
            try {
                Config.reload();
            }
            catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        if (command.equalsIgnoreCase("deop")) {
            Commands.deop(event);
        }
        if (command.equalsIgnoreCase("ping")) {
            Commands.ping(event);
        }
        if (command.equalsIgnoreCase("voice")) {
            Commands.voice(event);
        }
        if (command.equalsIgnoreCase("devoice")) {
            Commands.deVoice(event);
        }
        if (command.equalsIgnoreCase("quiet")) {
            Commands.quiet(event);
        }
        if (command.equalsIgnoreCase("unquiet")) {
            Commands.unquiet(event);
        }
        if (command.equalsIgnoreCase("login")) {
            Commands.login(event);
        }
        if (command.equalsIgnoreCase("ignore")) {
            Commands.ignore(event);
        }
        if (command.equalsIgnoreCase("unignore")) {
            Commands.unignore(event);
        }
        if (command.equalsIgnoreCase("shorten")) {
            Commands.shortenUrl(event);
        }
        if (command.equalsIgnoreCase("setcmd")) {
            Commands.setCommand(event);
        }
        if (command.equalsIgnoreCase("invalid")) {
            event.respond("Invalid command!");
        }
        if (command.equalsIgnoreCase("admin")) {
            try {
                Commands.addAdmin(event);
            }
            catch (Exception ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (command.equalsIgnoreCase("deladmin")) {
            try {
                Commands.delteAdmin(event);
            }
            catch (Exception ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (command.equalsIgnoreCase("listfiles")) {
            Commands.listFiles(event);
        }
        if (command.equalsIgnoreCase("sha")) {
            Commands.encrypt(event);
        }
        if (command.equalsIgnoreCase("delcmd")) {
            Commands.deleteCommand(event);
        }

    }

    public String CheckCommand(MessageEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length == 0) {
            return "invalid";
        }
        String cmd1 = args[0].substring(1);
        return cmd1;
    }

    public static String findUrl(MessageEvent event) throws MalformedURLException, IOException {
        String msg;
        String title = null;
        String[] words = event.getMessage().split(" ");
        for (int i = 0; i < words.length; i++) {
            if (Utils.isUrl(words[i])) {
                title = Utils.getWebpageTitle(words[i]);
            }
        }
        msg = String.format("%s's url title: %s", event.getUser().getNick(), title);
        return msg;
    }

    @Override
    public void onInvite(InviteEvent event) {
        if (Config.ACCEPT_INVITES) {
            event.getBot().joinChannel(event.getChannel());
        }
    }

    public void checkSpam(final MessageEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!users.contains(event.getUser().getNick())) {
                        if (violation.containsKey(event.getUser().getNick())) {
                            violation.put(event.getUser().getNick(), (Integer)violation.get(event.getUser().getNick()) + 1);
                        }
                        else {
                            violation.put(event.getUser().getNick(), 0);
                        }
                        users.add(event.getUser().getNick());
                        Thread.sleep(750);
                        users.remove(event.getUser().getNick());
                    }
                    else {
                        if ((Integer)violation.get(event.getUser().getNick()) > 3) {
                            event.getBot().kick(event.getChannel(), event.getUser(), "Too much spam bro.");
                            violation.put(event.getUser().getNick(), 0);
                            return;
                        }
                        event.getBot().setMode(event.getChannel(), "+q ", event.getUser());
                        users.remove(event.getUser().getNick());
                        Utils.sendNotice(event.getUser(), "You've been muted temporarily for spam.");
                        Thread.sleep(1000 * 10);
                        event.getBot().setMode(event.getChannel(), "-q ", event.getUser());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public static HashMap<String, BufferedWriter> fileWriterMap = new HashMap<String, BufferedWriter>();
    
    static
    {
        // Register a listener so when the VM shutdowns down we close files to prevent resource leaks
        Runtime.getRuntime().addShutdownHook(new Thread(new FileCloseThread()));
    }
    
    private static class FileCloseThread implements Runnable
    {
        @Override
        public void run()
        {
            // Loop through the values of the hashmap closing the BufferedWriters
            for (BufferedWriter bw : fileWriterMap.values())
                try
                {
                    bw.close();
                }
                catch (Exception e)
                {
                    //Already closed
                }
        }
    }
    
    public static BufferedWriter getOrCreateNewBW(String fileName) throws IOException
    {
        BufferedWriter bw = fileWriterMap.get(fileName);
        if (bw == null)
        {
            File f = new File(fileName);
            if(!f.exists()){
                f.getParentFile().mkdirs();
            }
            // Let me check in eclipse, netbeans doesn't have that good java doc stuff
            bw = new BufferedWriter(new FileWriter(f, true));
            fileWriterMap.put(fileName, bw);
        }
        
        return bw;
    }
    
    public static synchronized void log(String channel, String message){
        FileWriter fw = null;
        try {
            String file = "logs/" + channel + "/" + Utils.getMonth() + "/" + Utils.getDay() + ".txt";
            
            BufferedWriter bw = getOrCreateNewBW(file);
            bw.write(message);
            bw.newLine();
            bw.flush();//test again?
        }
        catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zack6849.alphabot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.RemoveChannelBanEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.SetChannelBanEvent;
import org.pircbotx.hooks.events.SetSecretEvent;
import org.pircbotx.hooks.events.SetTopicProtectionEvent;
import org.pircbotx.hooks.events.TopicEvent;


/**
 *
 * @author Zack
 */
public class LoggingListener extends ListenerAdapter {
    public static HashMap<String, BufferedWriter> fileWriterMap = new HashMap<String, BufferedWriter>();

    static {
        // Register a listener so when the VM shutdowns down we close files to prevent resource leaks
        Runtime.getRuntime().addShutdownHook(new Thread(new FileCloseThread()));
    }


    private static class FileCloseThread implements Runnable {
        @Override
        public void run() {
            // Loop through the values of the hashmap closing the BufferedWriters
            for (BufferedWriter bw : fileWriterMap.values()) {
                try {
                    bw.close();
                }
                catch (Exception e) {
                    //Already closed
                }
            }
        }
    }

    public static BufferedWriter getOrCreateNewBW(String fileName) throws IOException {
        BufferedWriter bw = fileWriterMap.get(fileName);
        if (bw == null) {
            File f = new File(fileName);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
            }
            bw = new BufferedWriter(new FileWriter(f, true));
            fileWriterMap.put(fileName, bw);
        }

        return bw;
    }

    public static synchronized void log(String channel, String message) {
        FileWriter fw = null;
        try {
            String file = "logs/" + channel + "/" + Utils.getMonth() + "/" + Utils.getDay() + ".txt";

            BufferedWriter bw = getOrCreateNewBW(file);
            bw.write(message);
            bw.newLine();
            bw.flush();
        }
        catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void onServerResponse(ServerResponseEvent event) {
        if (event.getCode() == 354) // I think, it's been a while
        {
            String response = event.getResponse();
            String[] sResponse = response.split(" ");
            if (sResponse.length != 3) {
                return;
            }
            String nick = sResponse[1];
            String nickserv = sResponse[2];
            Utils.userNickServMap.put(nick, nickserv);
        }
    }

    @Override
    public synchronized void onKick(KickEvent event) {
        if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
            if (event.getReason().isEmpty()) {
                event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("%s %s has been kicked from %s by %s ", Utils.getTime(), event.getRecipient().getNick(), event.getChannel().getName(), event.getSource().getNick()));
            }
            else {
                event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s has been kicked from %s by %s (%s)", event.getChannel().getName(), event.getRecipient().getNick(), event.getChannel().getName(), event.getSource().getNick(), event.getReason()));
            }
        }
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            if (event.getReason().isEmpty()) {
                log(event.getChannel().getName(), String.format("%s %s has been kicked from %s by %s ", Utils.getTime(), event.getRecipient().getNick(), event.getChannel().getName(), event.getSource().getNick()));
            }
            else {
                log(event.getChannel().getName(), String.format("[%s] %s has been kicked from %s by %s (%s)", event.getChannel().getName(), event.getRecipient().getNick(), event.getChannel().getName(), event.getSource().getNick(), event.getReason()));
            }
        }
        synchronized (Utils.userNickServMap) {
            if (Utils.userNickServMap.containsKey(event.getRecipient().getNick())) {
                Utils.userNickServMap.remove(event.getRecipient().getNick());
            }
        }
    }

    @Override
    public void onPart(PartEvent event) {
        if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
            if (event.getReason().isEmpty()) {
                 event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s (%s) has left %s (no reason)", event.getChannel().getName(), event.getUser().getNick(), event.getUser().getLogin() + "@" + event.getUser().getHostmask(), event.getChannel().getName()));
            }
            else {
                 event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s (%s) has left %s :%s", event.getChannel().getName(), event.getUser().getNick(), event.getUser().getLogin() + "@" + event.getUser().getHostmask(), event.getChannel().getName(), event.getReason()));
            }
        }
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            if (event.getReason().isEmpty()) {
                log(event.getChannel().getName(), String.format("%s %s (%s) has left %s (no reason)", Utils.getTime(), event.getUser().getNick(), event.getUser().getLogin() + "@" + event.getUser().getHostmask(), event.getChannel().getName()));
            }
            else {
                log(event.getChannel().getName(), String.format("%s %s (%s) has left %s :%s", Utils.getTime(), event.getUser().getNick(), event.getUser().getLogin() + "@" + event.getUser().getHostmask(), event.getChannel().getName(), event.getReason()));
            }
            synchronized (Utils.userNickServMap) {
                if (Utils.userNickServMap.containsKey(event.getUser().getNick())) {
                    Utils.userNickServMap.remove(event.getUser().getNick());
                }
            }
        }
    }

    @Override
    public void onQuit(QuitEvent event) {
        synchronized (Utils.userNickServMap) {
            if (Utils.userNickServMap.containsKey(event.getUser().getNick())) {
                Utils.userNickServMap.remove(event.getUser().getNick());
            }
        }
    }

    @Override
    public void onNickChange(NickChangeEvent event) {
        synchronized (Utils.userNickServMap) {
            if (Utils.userNickServMap.containsKey(event.getUser().getNick())) {
                Utils.userNickServMap.remove(event.getUser().getNick());
            }
        }
    }

    @Override
    public void onMessage(MessageEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            String message = String.format("%s %s: %s", Utils.getTime(), event.getUser().getNick(), Colors.removeFormattingAndColors(event.getMessage()));
            log(event.getChannel().getName(), message);
        }
    }

    @Override
    public void onJoin(JoinEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            log(event.getChannel().getName(), String.format("%s %s (%s) has joined %s", Utils.getTime(), event.getUser().getNick(), event.getUser().getLogin() + "@" + event.getUser().getHostmask(), event.getChannel().getName()));
        }
         if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
            event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s (%s) has joined %s", event.getChannel().getName(), event.getUser().getNick(), event.getUser().getLogin() + "@" + event.getUser().getHostmask(), event.getChannel().getName())); 
         }
    }

    @Override
    public void onMode(ModeEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            log(event.getChannel().getName(), String.format("%s %s sets mode %s on %s", Utils.getTime(), event.getUser().getNick(), event.getMode(), event.getChannel().getName()));
        }
        if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
            if (!event.getMode().contains("+b") && !event.getMode().contains("-b")) {
                event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s sets mode %s on %s", event.getChannel().getName(), event.getUser().getNick(), event.getMode(), event.getChannel().getName()));
            }
        }
    }

    @Override
    public void onSetSecret(SetSecretEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            log(event.getChannel().getName(), String.format("%s %s sets %s to secret", Utils.getTime(), event.getUser().getNick(), event.getChannel().getName()));
        }
    }

    @Override
    public void onTopic(TopicEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            log(event.getChannel().getName(), String.format("%s %s set the topic to: %s", Utils.getTime(), event.getUser().getNick(), event.getTopic()));
        }
        if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
           event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s set the topic to: %s", event.getChannel().getName(), event.getUser().getNick(), event.getTopic()));
        }
    }

    @Override
    public void onSetTopicProtection(SetTopicProtectionEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            log(event.getChannel().getName(), String.format("%s %s set topic protection to %s", Utils.getTime(), event.getUser().getNick(), String.valueOf(event.getChannel().hasTopicProtection())));
        }
        if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
           event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s set topic protection to %s", event.getChannel().getName(), event.getUser().getNick(), String.valueOf(event.getChannel().hasTopicProtection())));
        }
    }

    @Override
    public void onSetChannelBan(SetChannelBanEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            log(event.getChannel().getName(), String.format("%s %s set ban on %s", Utils.getTime(), event.getUser().getNick(), event.getHostmask()));
        }
        if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
           event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s set ban on %s", event.getChannel().getName(), event.getUser().getNick(), event.getHostmask()));
        }
    }

    @Override
    public void onRemoveChannelBan(RemoveChannelBanEvent event) {
        if (Config.LOGGED_CHANS.contains(event.getChannel().getName())) {
            log(event.getChannel().getName(), String.format("%s %s removes ban on %s", Utils.getTime(), event.getUser().getNick(), event.getHostmask()));
        }
        if (Bot.relay.containsKey(event.getChannel().getName().toLowerCase())) {
           event.getBot().sendMessage(Bot.relay.get(event.getChannel().getName().toLowerCase()), String.format("[%s] %s removes ban on %s", event.getChannel().getName(), event.getUser().getNick(), event.getHostmask()));
        }
    }
}

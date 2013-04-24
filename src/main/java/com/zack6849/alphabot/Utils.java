/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zack6849.alphabot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.hooks.events.WhoisEvent;

/**
 * @author zack6849(zcraig29@gmail.com)
 */
public class Utils
{

    public static String GOOGLE_API_KEY = "";

    /**
     * @param user   the user object to send the notice too
     * @param notice the string to notice the user with
     */
    public static void sendNotice(User user, String notice)
    {
        Bot.bot.sendNotice(user, notice);
    }

    public static boolean isAdmin(String user)
    {
        if (Config.ADMINS.contains(user) && Bot.bot.getUser(user).isVerified())
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static String htmlFormat(String s)
    {
        System.out.println(String.valueOf(new File("c:/").exists()));
        return s.replaceAll("<b>", "").replace("</b>", "").replace("&#39;", "'").replaceAll("&quot;", "'").replaceAll("   ", " ").replaceAll("&amp;", "&");
    }

    public static String removeBrackets(String s)
    {
        return s.replaceAll("[\\['']|['\\]'']", "");
    }

    /**
     * @param user the Minecraft username to check
     * @return returns a boolean depending upon if he username has paid or not
     *         (note: accounts that do not exists return false too)
     */
    public static boolean checkAccount(String user)
    {
        boolean paid = false;
        try
        {
            URL url = new URL("http://minecraft.net/haspaid.jsp?user=" + user);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = in.readLine();
            in.close();
            if (str != null)
            {
                paid = Boolean.valueOf(str);
            }
        } catch (java.io.IOException e1)
        {
        }
        return paid;
    }

    public static String getAccount(User user)
    {
        String returns = "";
        try
        {
            Bot.bot.sendRawLine("WHOIS " + user.getNick() + " " + user.getNick());
            WhoisEvent event = Bot.bot.waitFor(WhoisEvent.class);
            String tmp = event.getRegisteredAs();
            if (tmp != null)
            {
                returns = tmp;
            }
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returns;
    }

    public static List getChannels(User user)
    {
        List<Channel> returns = new ArrayList<Channel>();
        try
        {
            Bot.bot.sendRawLine("WHOIS " + user.getNick() + " " + user.getNick());
            WhoisEvent event = Bot.bot.waitFor(WhoisEvent.class);
            returns = event.getChannels();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return returns;
    }

    /*
     * @return a string with the status.
     */
    public static String checkMojangServers()
    {
        String returns = null;
        try
        {
            URL url;
            url = new URL("http://status.mojang.com/check");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String result;
            while ((result = reader.readLine()) != null)
            {
                String a = result.replace("red", Colors.RED + "Offline" + Colors.NORMAL).replace("green", Colors.GREEN + "Online" + Colors.NORMAL).replace("[", "").replace("]", "");
                String final_result = a.replace("{", "").replace("}", "").replace(":", " is currently ").replace("\"", "").replaceAll(",", ", ");
                returns = final_result;
            }
            reader.close();
        } catch (IOException e)
        {
            if (e.getMessage().contains("503"))
            {
                returns = "The minecraft status server is temporarily unavailable, please try again later";
            }
            if (e.getMessage().contains("404"))
            {
                returns = "Uhoh, it would appear as if the haspaid page has been removed or relocated >_>";
            }

        }
        return returns;
    }

    /**
     * @param longUrl the URL to shorten
     * @return the shortened URL
     */
    public static String shortenUrl(String longUrl)
    {
        String shortened = null;
        try
        {
            URL url;
            url = new URL("http://is.gd/create.php?format=simple&url=" + longUrl);
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()));
            shortened = bufferedreader.readLine();
            bufferedreader.close();
        } catch (Exception e)
        {
        }
        return shortened;
    }

    public static String checkServerStatus(InetAddress i, int port)
    {
        String returns = "Error.";
        try
        {
            Socket s = new Socket(i, 25565);
            DataInputStream SS_BF = new DataInputStream(s.getInputStream());
            DataOutputStream d = new DataOutputStream(s.getOutputStream());
            d.write(new byte[]{(byte) 0xFE, (byte) 0x01});
            SS_BF.readByte();
            short length = SS_BF.readShort();
            StringBuilder sb = new StringBuilder();
            for (int in = 0; in < length; in++)
            {
                char ch = SS_BF.readChar();
                sb.append(ch);
            }
            String all = sb.toString().trim();
            System.out.println(all);
            String[] args1 = all.split("\u0000");
            if (args1[3].contains("§"))
            {
                returns = "MOTD: " + args1[3].replaceAll("§[a-m]", "").replaceAll("§[1234567890]", "") + "   players: [" + args1[4] + "/" + args1[5] + "]";
            } else
            {
                returns = "MOTD: " + args1[3] + "   players: [" + args1[4] + "/" + args1[5] + "]";
            }
        } catch (UnknownHostException e1)
        {
            returns = "the host you specified is unknown. check your settings.";
        } catch (IOException e1)
        {
            returns = "sorry, we couldn't reach this server, make sure that the server is up and has query enabled.";
        }
        return returns;
    }

    public static String encrypt(String s)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            MessageDigest alg = MessageDigest.getInstance("SHA-512");
            alg.reset();
            alg.update(s.getBytes(Charset.forName("UTF-8")));

            byte[] digest = alg.digest();
            for (byte b : digest)
            {
                sb.append(Integer.toHexString(0xFF & b));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getYoutubeInfo(String s) throws IOException
    {
        String info;
        String title = null;
        String likes = null;
        String dislikes = null;
        String user = null;
        String veiws = null;
        String publishdate;
        Document doc = Jsoup.connect(s).userAgent("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17").get();
        for (Element e : doc.select("a"))
        {
            if (e.attr("class").equalsIgnoreCase("yt-uix-sessionlink yt-user-videos"))
            {
                user = e.attr("href").split("/user/")[1].split("/")[0];
            }
        }
        for (Element e : doc.select("span"))
        {
            if (e.attr("class").equalsIgnoreCase("watch-view-count"))
            {
                veiws = e.text();
            }
            if (e.attr("class").equalsIgnoreCase("likes-count"))
            {
                likes = e.text();
            }
            if (e.attr("class").equalsIgnoreCase("dislikes-count"))
            {
                dislikes = e.text();
            }
            if (e.attr("class").equalsIgnoreCase("watch-title  yt-uix-expander-head") || e.attr("class").equalsIgnoreCase("watch-title long-title yt-uix-expander-head"))
            {
                title = e.text();
            }
            if (e.attr("class").equalsIgnoreCase("watch-video-date"))
            {
                publishdate = e.text();
            }
        }
        info = title + " - " + user + "    veiws: " + veiws + "  likes: " + likes + "  dislikes: " + dislikes;
        //System.out.println(info);
        return info;
    }

    public static String getWebpageTitle(String s)
    {
        String title = "";
        String error = "none!";
        try
        {
            String content = new URL(s).openConnection().getContentType();
            if (!content.toLowerCase().contains("text/html"))
            {
                return "content type: " + content + " size: " + new URL(s).openConnection().getContentLength() / 1024 + "kb";
            }
            Document doc = Jsoup.connect(s).userAgent("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17").followRedirects(true).get();
            URLConnection c = new URL(s).openConnection();

            Elements links = doc.select("title");
            for (Element e1 : links)
            {
                title += e1.text().replaceAll("\n", "").replaceAll("\\s+", " ");
            }
        } catch (Exception e)
        {
            error = e.toString();
        }
        if (!error.equalsIgnoreCase("none"))
        {
            if (error.contains("404"))
            {
                return "404 file not found";
            }
            if (error.contains("502"))
            {
                return "502 bad gateway";
            }
            if (error.contains("401"))
            {
                return "401 unauthorized request";
            }
            if (error.contains("403"))
            {
                return "403 forbidden";
            }
            if (error.contains("500"))
            {
                return "500 internal server error";
            }
            if (error.contains("503"))
            {
                return "503 service unavailable (usually temporary, try again later)";
            }
        }
        return title;
    }

    public static boolean isUrl(String s)
    {
        String url_regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern p = Pattern.compile(url_regex);
        Matcher m = p.matcher(s);
        if (m.find())
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static String munge(String s)
    {
        StringBuilder sb = new StringBuilder();
        String[] normal = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        String[] munge = {"ä", "Б", "ċ", "đ", "ë", "ƒ", "ġ", "ħ", "í", "ĵ", "ķ", "ĺ", "ṁ", "ñ", "ö", "ρ", "ʠ", "ŗ", "š", "ţ", "ü", "v", "ω", "χ", "ÿ", "ź", "Å", "Β", "Ç", "Ď", "Ē", "Ḟ", "Ġ", "Ħ", "Í", "Ĵ", "Ķ", "Ĺ", "Μ", "Ν", "Ö", "Р", "Q", "Ŗ", "Š", "Ţ", "Ů", "Ṿ", "Ŵ", "Χ", "Ỳ", "Ż"};
        String[] replace = s.split("");
        for (String s1 : replace)
        {
            for (int i = 0; i < normal.length; i++)
            {
                if (s1.equalsIgnoreCase(normal[i]))
                {
                    s1 = munge[i];
                }
                sb.append(s1);
            }
        }
        return sb.toString();
    }
}

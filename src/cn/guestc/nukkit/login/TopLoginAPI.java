package cn.guestc.nukkit.login;

import cn.guestc.nukkit.login.Config.MysqlConfig;
import cn.guestc.nukkit.login.utils.ConfigData;
import cn.nukkit.Player;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.LoginChainData;
import com.google.common.base.Utf8;

import java.security.MessageDigest;
import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class TopLoginAPI {

    private static TopLoginAPI obj;

    private TopLogin plugin;

    private ArrayList<String> loginusers = new ArrayList<>();

    private Map<String,Object> language;

    protected ArrayList<String> banusers;

    public ConfigData cdata;

    private FormWindow Funlogin;
    private FormWindow Floginin;

    private HashMap<String,Integer> BanUserInMins = new HashMap<>(),BanCidMins = new HashMap<>(),BanUUIDMins = new HashMap<>();

    private HashMap<String,Integer> WrongMailCode = new HashMap<>(),WrongPasswd = new HashMap<>();
    private HashMap<String,String> MailVerifyCode = new HashMap<>();




    public static TopLoginAPI getObject(){
        return TopLoginAPI.obj;
    }

     protected TopLoginAPI(TopLogin toplogin){
        plugin = toplogin;
        TopLoginAPI.obj = this;
    }

    protected void init(){
        language = plugin.getLanuage();
        banusers = (ArrayList<String>) plugin.pconfig.get("ban-username");
        cdata = new ConfigData();
        cdata.UserMinLen = UserMin();
        cdata.UserMaxLen = UserMax();
        cdata.PasswdMaxLen = PasswdMax();
        cdata.PasswdMinLen = PasswdMin();
        cdata.MessageType = Byte.parseByte(plugin.pconfig.get("message-type").toString());
        cdata.UnloginMove = Boolean.parseBoolean(plugin.pconfig.get("unlogin-move").toString());
        cdata.UnloginMessage = Boolean.parseBoolean(plugin.pconfig.get("unlogin-message").toString());
        cdata.UnloginBreak = Boolean.parseBoolean(plugin.pconfig.get("unlogin-break").toString());
        cdata.UnloginCraft = Boolean.parseBoolean(plugin.pconfig.get("unlogin-craft").toString());
        cdata.UnloginPlace = Boolean.parseBoolean(plugin.pconfig.get("unlogin-place").toString());
        cdata.UnloginInteract = Boolean.parseBoolean(plugin.pconfig.get("unlogin-interact").toString());
        cdata.UnloginDropItem = Boolean.parseBoolean(plugin.pconfig.get("unlogin-dropitem").toString());
        cdata.UnloginPickItem = Boolean.parseBoolean(plugin.pconfig.get("unlogin-pickitem").toString());
        cdata.AutoLogin = Boolean.parseBoolean(plugin.pconfig.get("autologin").toString());
        cdata.AutoLoginValidHours = Integer.parseInt(plugin.pconfig.get("autologin-valid-hours").toString());
        cdata.LoginType = plugin.pconfig.get("login-type").toString();
        cdata.MultiServer = Boolean.parseBoolean(plugin.pconfig.get("multi-server").toString());
        cdata.MainServer = Boolean.parseBoolean(plugin.pconfig.get("main-server").toString());
        cdata.EnableFormUI = Boolean.parseBoolean(plugin.pconfig.get("enable-form-ui").toString());
        cdata.EnbaleMailVerify = Boolean.parseBoolean(plugin.pconfig.get("enable-mail-verify").toString());
        cdata.MailSmtpPort = plugin.pconfig.get("mail-smtp-port").toString();
        cdata.MailSmtpHost = plugin.pconfig.get("mail-smtp-host").toString();
        cdata.MailUser = plugin.pconfig.get("mail-user").toString();
        cdata.MailPasswd = plugin.pconfig.get("mail-passwd").toString();
        cdata.MailVerifyBanTime = Integer.parseInt(plugin.pconfig.get("mail-verify-ban-time").toString());
        cdata.MailVerifyWrongTime = Integer.parseInt(plugin.pconfig.get("mail-verify-ban-time").toString());
        cdata.PasswdWrongTime = Integer.parseInt(plugin.pconfig.get("allow-passwd-wrong-time").toString());
        cdata.PasswdWrongBanTime = Integer.parseInt(plugin.pconfig.get("passwd-wrong-ban").toString());

        Funlogin = new FormWindowSimple(getMessage("login-usage-ui-title"),getMessage("login-usage-ui-text"));
        Floginin = new FormWindowSimple(getMessage("login-in-ui-title"),getMessage("login-in-ui-text"));
    }

    public static String getPasswdFormStr(String str){
        byte[] bts;
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            bts = md5.digest(str.getBytes());
        }catch (Exception e){
            return null;
        }
        final char[] hex_d = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bts.length;i++){
            sb.append(hex_d[(bts[i] >> 4) & 0x0f]);
            sb.append(hex_d[bts[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static String getTime(){
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(new Date());
    }

    public static Date getTime(String date) throws ParseException {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.parse(date);
    }

    public void LoginIn(Player player){
        String name = player.getName();
        if(!loginusers.contains(name)){
            loginusers.add(name);
            plugin.getLogger().info(String.format(getMessage("user-login-message"),name,getNameFromIp(player.getAddress())));
            if(cdata.EnableFormUI){
                PluginTask<TopLogin> at = new PluginTask<TopLogin>(plugin) {
                    @Override
                    public void onRun(int x) {
                        player.showFormWindow(Floginin);
                    }
                };
                plugin.getServer().getScheduler().scheduleDelayedTask(at,20*8);
            }
        }
    }

    public void LoginOut(Player player){
        String name = player.getName();
        if(loginusers.contains(name)){
            loginusers.remove(name);
            plugin.dataHelper.LoginOut(player);
        }
        if (MailVerifyCode.containsKey(name)){
            MailVerifyCode.remove(name);
        }
    }

    public void WrongMailVerifyCode(Player p){
        String name = p.getName();
        int time = 1;
        if(WrongMailCode.containsKey(name)){
            int old_time = WrongMailCode.get(name);
            if(old_time > cdata.MailVerifyWrongTime){
                p.kick(String.format(getMessage("reg-mail-verify-wrong-code-ban"),old_time,cdata.MailVerifyBanTime),true);
                WrongMailCode.remove(name);
                BanUserInMins.put(name.toLowerCase(),cdata.MailVerifyBanTime);
                return;
            }
            time = old_time + 1;
        }
        WrongMailCode.put(name,time);
    }

    public void WrongPasswd(Player p){
        String name = p.getName();
        int time = 1;
        if(WrongPasswd.containsKey(name)){
            int old_time = WrongPasswd.get(name);
            if(old_time > cdata.PasswdWrongTime){
                p.kick(getMessage("login-in-wrong-passwd-ban"),true);
                WrongPasswd.remove(name);
                BanUserInMins.put(name.toLowerCase(),cdata.PasswdWrongBanTime);
                return;
            }
            time = old_time + 1;
        }
        WrongPasswd.put(name,time);
    }

    public void AutoLogin(Player player){
        String user = player.getName();
        if(cdata.AutoLogin){
            Date ltime = plugin.dataHelper.getLastTime(user);
            if(ltime != null){
                Date ntime = new Date();
                if((ntime.getTime() - ltime.getTime()) <= (1000*60*60* cdata.AutoLoginValidHours)){
                    LoginChainData data = player.getLoginChainData();
                    if(data.getClientId() == plugin.dataHelper.getCid(user) && data.getClientUUID().toString().equals(plugin.dataHelper.getUUID(user))){
                        Message(player,String.format(getMessage("autologin"),cdata.AutoLoginValidHours));
                        LoginIn(player);
                    }
                    return;
                }
            }
        }
        if(cdata.EnableFormUI){
            PluginTask<TopLogin> at = new PluginTask<TopLogin>(plugin) {
                @Override
                public void onRun(int x) {
                    player.showFormWindow(Funlogin);
                }
            };
            plugin.getServer().getScheduler().scheduleDelayedTask(at,20*8);
        }
        Message(player,getMessage("login-in-message"));
    }

    public boolean isLogin(String user){
        if(loginusers.contains(user))return true;
        if(cdata.MultiServer){
            MysqlConfig mc = (MysqlConfig) plugin.dataHelper;
            return mc.isLogin(user);
        }
        return false;
    }

    public void Message(Player player,String msg){
        if(!cdata.UnloginMessage){
            TextPacket pk = new TextPacket();
            pk.type = cdata.MessageType;
            pk.offset = 600;
            pk.message = msg;
            player.dataPacket(pk);
            return;
        }
        player.sendMessage(msg);
    }

    public void Message(Player player,String msg,byte type){
        if(!cdata.UnloginMessage){
            TextPacket pk = new TextPacket();
            pk.type = type;
            pk.offset = 600;
            pk.message = msg;
            player.dataPacket(pk);
            return;
        }
        player.sendMessage(msg);
    }

    public String getMessage(String str){
        if(language.containsKey(str)){
            return language.get(str).toString();
        }
        return null;
    }

    public void BanCid(String cid,int mins){
        if(!BanCidMins.containsKey(cid)){
            BanCidMins.put(cid,mins);
        }
    }
    public void BanUUID(String uuid,int mins){
        if(!BanUUIDMins.containsKey(uuid)){
            BanUUIDMins.put(uuid,mins);
        }
    }

    public int UserMin(){
        if(plugin.pconfig.exists("username-min")){
            return Integer.parseInt(plugin.pconfig.get("username-min").toString());
        }
        return 3;
    }

    public int UserMax(){
        if(plugin.pconfig.exists("username-max")){
            return Integer.parseInt(plugin.pconfig.get("username-max").toString());
        }
        return 16;
    }

    public int PasswdMin(){
        if(plugin.pconfig.exists("passwd-min")){
            return Integer.parseInt(plugin.pconfig.get("passwd-min").toString());
        }
        return 8;
    }

    public int PasswdMax(){
        if(plugin.pconfig.exists("passwd-max")){
            return Integer.parseInt(plugin.pconfig.get("passwd-max").toString());
        }
        return 16;
    }

    public boolean isBanReg(String user){
        if(banusers.contains(user.toLowerCase()))return true;
        for (String u:banusers) {
            if(Pattern.matches(u,user))return true;
        }
        return false;
    }

    public static boolean isMail(String str){
        String pattern = "^\\w{2,}@([A-Za-z0-9]{2,}(\\.[A-Za-z0-9]{2,})+)$";
        return Pattern.matches(pattern,str);
    }

    public static String ArrayToString(String[] args){
        StringBuilder sb = new StringBuilder();
        for (String arg : args){
            sb.append(arg);
        }
        return sb.toString();
    }

    public String CheckPasswd(String passwd){
        String remsg =null;
        if(passwd.length() > cdata.PasswdMaxLen){
            remsg = getMessage("reg-passwd-max-lenght");
        }
        if(passwd.length() < cdata.PasswdMinLen){
            remsg = getMessage("reg-passwd-min-lenght");
        }
        return remsg;
    }

    public static String getNameFromIp(String ip){
        //todo
        return ip;
    }

    public boolean isBan(Player p){
        if(BanUserInMins.containsKey(p.getName().toLowerCase()))return true;
        LoginChainData data = p.getLoginChainData();
        StringBuilder cid = new StringBuilder();
        cid.append(data.getClientId());
        if(BanCidMins.containsKey(cid.toString()))return true;
        if(BanCidMins.containsKey(data.getClientUUID().toString()))return true;
        return false;
    }

    public void ReduceMinute(){
        Set<String> bus = BanUUIDMins.keySet();
        for(String u : bus){
            int mins = BanUUIDMins.get(u);
            if(mins <= 1){
                BanUUIDMins.remove(u);
            }else{
                --mins;
                BanUUIDMins.put(u,mins);
            }
        }
        Set<String> bcs = BanCidMins.keySet();
        for(String u : bcs){
            int mins = BanCidMins.get(u);
            if(mins <= 1){
                BanCidMins.remove(u);
            }else{
                --mins;
                BanCidMins.put(u,mins);
            }
        }
        Set<String> buss = BanUserInMins.keySet();
        for(String u : buss){
            int mins = BanUserInMins.get(u);
            if(mins <= 1){
                BanUserInMins.remove(u);
            }else{
                --mins;
                BanUserInMins.put(u,mins);
            }
        }
    }


    public ArrayList<String> getLoginUsers(){
        return loginusers;
    }

    public String RandCode(Player p){
        StringBuilder sb = new StringBuilder();
        Random rd = new Random();
        for(int x = 0;x <4;x++){
            sb.append(rd.nextInt(9));
        }
        MailVerifyCode.put(p.getName(),sb.toString());
        return sb.toString();
    }

    public boolean VerifyMailCode(Player p ,String code){
        if(MailVerifyCode.containsKey(p.getName())){
            if(MailVerifyCode.get(p.getName()).equals(code)){
                MailVerifyCode.remove(p.getName());
                return true;
            }
        }
        return false;
    }

    public String getMailContent(String name,String code){
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "</head>\n" +
                "<body>\n" +
                "<h2>"+name+" Your Code:</h2>\n" +
                "<br>\n" +
                "<h1>"+code+"</h1>\n" +
                "</body>\n" +
                "</html>";
    }

    public boolean sendMailAsync(String mail,String content,Player player){
        AsyncTask at = new AsyncTask() {
            @Override
            public void onRun() {
                try {
                    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                    final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
                    Properties props = new Properties();
                    props.setProperty("mail.smtp.host",cdata.MailSmtpHost);
                    props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
                    props.setProperty("mail.smtp.socketFactory.fallback", "false");
                    //邮箱发送服务器端口,这里设置为465端口
                    props.setProperty("mail.smtp.port", cdata.MailSmtpPort);
                    props.setProperty("mail.smtp.socketFactory.port", cdata.MailSmtpPort);
                    props.put("mail.smtp.auth", "true");
                    final String username = cdata.MailUser;
                    final String password = cdata.MailPasswd;
                    Session session = Session.getDefaultInstance(props, new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

                    Message msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress("dtsmcpe@163.com"));
                    msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mail));
                    //Transport transport = session.getTransport();
                    msg.setSubject("【DawnTribe】Server Network 邮箱地址绑定认证");
                    msg.setContent(content,"text/html;charset=utf-8");
                    Transport.send(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage("发送邮件出现异常,请将此情况反馈给管理员");
                }
            }
        };
        plugin.getServer().getScheduler().scheduleAsyncTask(plugin,at);
        plugin.getLogger().warning("end sendmail");
         return false;
    }




}

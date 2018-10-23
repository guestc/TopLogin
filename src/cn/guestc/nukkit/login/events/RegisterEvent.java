package cn.guestc.nukkit.login.events;

import cn.guestc.nukkit.login.TopLogin;
import cn.guestc.nukkit.login.TopLoginAPI;
import cn.guestc.nukkit.login.utils.UserData;
import cn.nukkit.Player;
import cn.nukkit.api.API;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDespawnEvent;
import cn.nukkit.event.entity.EntityEvent;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;

import javax.swing.text.html.parser.Entity;
import java.util.Date;
import java.util.HashMap;

public class RegisterEvent implements Listener {
    private TopLogin plugin;

    private TopLoginAPI API;

    private FormWindow Funreg;

    private FormWindow Funlogin;


    public enum RegisterState{
        confirmName,
        Passwd,
        confirmPasswd,
        Mail,
        MailVerify
    }

    private HashMap<String,RegisterState> registers = new HashMap<>();

    private HashMap<String, UserData> reging = new HashMap<>();

    private  HashMap<String,String> login_mail = new HashMap<>();

    public RegisterEvent(TopLogin toplogin){
        plugin = toplogin;
        API = plugin.api;
        Funreg = new FormWindowSimple(API.getMessage("unreg-usage-ui-title"),API.getMessage("unreg-usage-ui-text"));
        Funlogin = new FormWindowSimple(API.getMessage("login-usage-ui-title"),API.getMessage("login-usage-ui-text"));
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!plugin.dataHelper.IsRegister(name)){
            API.Message(player,API.getMessage("reg-comfirm-name"),(byte) 1);
        }else{
            API.AutoLogin(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = false)
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        API.LoginOut(player);
        if(registers.containsKey(name)){
            registers.remove(name);
        }
        if(reging.containsKey(name)){
            reging.remove(name);
        }
        if(login_mail.containsKey(name)){
            login_mail.remove(name);
        }
    }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=false)
    public void onCommandPre(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        String msg = event.getMessage();
        if(!API.isLogin(name)){
            if(API.cdata.LoginType.equals("command")){
                if(!registers.containsKey(name) && plugin.dataHelper.IsRegister(name)){
                    if(msg.substring(0,"/login ".length()).equals("/login ")){
                        String passwd = TopLoginAPI.getPasswdFormStr(msg.replace("/login ",""));
                        if(plugin.dataHelper.VerifyPasswd(player.getName(),passwd)){
                            API.Message(player,API.getMessage("login-in-success"));
                            API.LoginIn(player);
                        }else{
                            API.WrongPasswd(player);
                            API.Message(player,API.getMessage("login-in-wrong-passwd"));
                            API.Message(player,API.getMessage("login-in-message"));
                        }
                    }
                }
            }
            if(msg.substring(0,"/login-mail".length()).equals("/login-mail")){
                if(!login_mail.containsKey(name)){
                    String code = API.RandCode(player);
                    String mail = plugin.dataHelper.getMail(name);
                    if(mail != null){
                        API.sendMailAsync(mail,API.getMailContent(name,code),player);
                        login_mail.put(name,code);
                        API.Message(player,String.format(API.getMessage("login-in-with-mail-send"),mail));
                    }else{
                        API.Message(player,API.getMessage("login-in-with-mail-no-mail"));
                    }
                }else{
                    API.Message(player,API.getMessage("login-in-with-mail-usage"));
                }

            }
            event.setCancelled(true);
        }

    }

    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=false)
    public void onChat(PlayerChatEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        String msg = event.getMessage();
        if(registers.containsKey(name)){
            switch(registers.get(name)){
                case confirmName:
                    if(msg.toLowerCase().equals(name.toLowerCase())){
                        registers.put(name,RegisterState.Passwd);
                        API.Message(player,API.getMessage("reg-passwd"),(byte) 1);
                        reging.put(name,new UserData());
                        event.setCancelled(true);
                        return;
                    }
                    API.Message(player,API.getMessage("reg-comfirm-name-wrong"),(byte) 1);
                    break;
                case Mail:
                    if(!TopLoginAPI.isMail(msg)){
                        API.Message(player,API.getMessage("reg-mail-wrong"),(byte) 1);
                        API.Message(player,API.getMessage("reg-mail"),(byte) 1);
                        event.setCancelled(true);
                        return;
                    }
                    UserData ud1 = reging.get(name);
                    ud1.mail = msg;
                    reging.put(name,ud1);
                    if(API.cdata.EnbaleMailVerify){
                        registers.put(name,RegisterState.MailVerify);
                        API.Message(player,String.format(API.getMessage("reg-mail-verify"),msg),(byte) 1);
                        String code = API.RandCode(player);
                        API.sendMailAsync(msg,API.getMailContent(player.getName(),code),player);
                    }else{
                        String passwd = TopLoginAPI.getPasswdFormStr(ud1.passwd);
                        plugin.dataHelper.AddUser(name,passwd,msg);
                        API.Message(player,API.getMessage("reg-success"),(byte) 1);
                        API.Message(player,String.format(API.getMessage("reg-success-return-msg"),name,ud1.passwd,msg),(byte) 1);
                        API.LoginIn(player);
                        reging.remove(name);
                        registers.remove(name);
                    }
                    break;

                case MailVerify:
                    if(!API.VerifyMailCode(player,msg)){
                        API.Message(player,API.getMessage("reg-mail-verify-wrong-code"),(byte) 1);
                        event.setCancelled(true);
                        API.WrongMailVerifyCode(player);
                        return;
                    }
                    UserData ud2 = reging.get(name);
                    String passwd = TopLoginAPI.getPasswdFormStr(ud2.passwd);
                    plugin.dataHelper.AddUser(name,passwd,ud2.mail);
                    API.Message(player,API.getMessage("reg-success"),(byte) 1);
                    API.Message(player,String.format(API.getMessage("reg-success-return-msg"),name,ud2.passwd,ud2.mail),(byte) 1);
                    API.LoginIn(player);
                    reging.remove(name);
                    registers.remove(name);
                    //todo
                    break;
                case Passwd:
                    String remsg = API.CheckPasswd(msg);
                    if(remsg != null){
                        API.Message(player,API.getMessage("reg-comfirm-name-wrong"),(byte) 1);
                        API.Message(player,remsg,(byte) 1);
                        event.setCancelled(true);
                        return;
                    }
                    API.Message(player,API.getMessage("reg-passwd-comfirm"),(byte) 1);
                    registers.put(name,RegisterState.confirmPasswd);
                    UserData ud = reging.get(name);
                    ud.name = name;
                    ud.passwd = msg;
                    reging.put(name,ud);
                    break;
                case confirmPasswd:
                    if(!reging.get(name).passwd.equals(msg)){
                        API.Message(player,API.getMessage("reg-passwd-comfirm-not"),(byte) 1);
                        API.Message(player,API.getMessage("reg-passwd-comfirm"),(byte) 1);
                        event.setCancelled(true);
                        return;
                    }
                    API.Message(player,API.getMessage("reg-mail"),(byte) 1);
                    registers.put(name,RegisterState.Mail);
                    break;
            }
            event.setCancelled(true);
        }else{
            if(!API.isLogin(name)){
                if(login_mail.containsKey(name)){
                    if(msg.equals(login_mail.get(name))){
                        API.LoginIn(player);
                        API.Message(player,API.getMessage("login-in-success"));
                        login_mail.remove(name);
                    }else{
                        API.Message(player,API.getMessage("login-in-with-mail-wrong-code"));
                        API.WrongMailVerifyCode(player);
                    }
                }else if(API.cdata.LoginType.equals("text")){
                    if(plugin.dataHelper.VerifyPasswd(name,TopLoginAPI.getPasswdFormStr(msg))){
                        API.LoginIn(player);
                        API.Message(player,API.getMessage("login-in-success"));
                    }else{
                        API.Message(player,API.getMessage("login-in-wrong-passwd"));
                        API.Message(player,API.getMessage("login-in-message"));
                        API.WrongPasswd(player);
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=false)
    public void PreLogin(PlayerPreLoginEvent event){
        if(!event.isCancelled()){
            String name = event.getPlayer().getName();
            int ban = API.isBan(event.getPlayer());
            if(ban != 0){
                if(ban == 1){
                    event.setKickMessage(API.getMessage("time-username-banned"));
                }else{
                    event.setKickMessage(API.getMessage("time-device-banned"));
                }
                event.setCancelled(true);
                return;
            }
            if(!plugin.dataHelper.IsRegister(name)){
                String msg = null;
                if(!plugin.dataHelper.canRegister()){
                    msg = API.getMessage("cant-reg");
                }
                if(API.isBanReg(name)){
                    msg = API.getMessage("reg-comfirm-name-banned");
                }
                if(name.length() > API.cdata.UserMaxLen){
                    msg = API.getMessage("reg-comfirm-name-max-lenght");
                }
                if(name.length() < API.cdata.UserMinLen){
                    msg = API.getMessage("reg-comfirm-name-min-lenght");
                }
                if(msg != null){
                    event.setKickMessage(msg);
                    event.setCancelled(true);
                    return;
                }
                registers.put(name,RegisterState.confirmName);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=false)
    public void onLoadDone(DataPacketReceiveEvent event){
        if(event.getPacket() instanceof SetLocalPlayerAsInitializedPacket){
            showForm(event.getPlayer());
        }
    }

    public void showForm(Player p){
        String name = p.getName();
        if(!plugin.dataHelper.IsRegister(name)){
            if(API.cdata.EnableFormUI){
                AsyncTask at = new AsyncTask() {
                    @Override
                    public void onRun() {
                        p.showFormWindow(Funreg);
                    }
                };
                plugin.getServer().getScheduler().scheduleAsyncTask(plugin,at);
            }
        }else{
            if(!API.isLogin(name)){
                if(API.cdata.EnableFormUI){
                    AsyncTask at = new AsyncTask() {
                        @Override
                        public void onRun() {
                            p.showFormWindow(Funlogin);
                        }
                    };
                    plugin.getServer().getScheduler().scheduleAsyncTask(plugin,at);
                }
            }
        }
    }
}

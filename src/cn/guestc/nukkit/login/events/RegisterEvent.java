package cn.guestc.nukkit.login.events;

import cn.guestc.nukkit.login.TopLogin;
import cn.guestc.nukkit.login.TopLoginAPI;
import cn.guestc.nukkit.login.utils.UserData;
import cn.nukkit.Player;
import cn.nukkit.api.API;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;

import java.util.Date;
import java.util.HashMap;

public class RegisterEvent implements Listener {
    private TopLogin plugin;

    private TopLoginAPI API;

    private FormWindow Funreg;


    public enum RegisterState{
        confirmName,
        Passwd,
        confirmPasswd,
        Mail,
        MailVerify
    }

    private HashMap<String,RegisterState> registers = new HashMap<>();

    private HashMap<String, UserData> reging = new HashMap<>();

    public RegisterEvent(TopLogin toplogin){
        plugin = toplogin;
        API = plugin.api;
        Funreg = new FormWindowSimple(API.getMessage("unreg-usage-ui-title"),API.getMessage("unreg-usage-ui-text"));
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!plugin.dataHelper.IsRegister(name)){
            if(API.cdata.EnableFormUI){
                PluginTask<TopLogin> at = new PluginTask<TopLogin>(plugin) {
                    @Override
                    public void onRun(int x) {
                        player.showFormWindow(Funreg);
                    }
                };
                plugin.getServer().getScheduler().scheduleDelayedTask(at,20*8);
            }
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
    }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=false)
    public void onCommandPre(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        String name = player.getName();
        if(!API.isLogin(name)){
            if(API.cdata.LoginType.equals("command")){
                if(!registers.containsKey(name) && plugin.dataHelper.IsRegister(name)){
                    String msg = event.getMessage();
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
                    plugin.dataHelper.AddUser(name,passwd,msg);
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
                if(API.cdata.LoginType.equals("text")){
                    if(plugin.dataHelper.VerifyPasswd(name,TopLoginAPI.getPasswdFormStr(msg))){
                        API.LoginIn(player);
                        API.Message(player,API.getMessage("login-in-success"));
                    }else{
                        API.Message(player,API.getMessage("login-in-wrong-passwd"));
                        API.Message(player,API.getMessage("login-in-message"));
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
            if(API.isBan(event.getPlayer())){
                event.setKickMessage(API.getMessage("time-banned"));
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
}

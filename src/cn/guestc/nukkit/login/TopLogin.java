package cn.guestc.nukkit.login;

import cn.guestc.nukkit.login.events.LoginEvent;
import cn.guestc.nukkit.login.events.RegisterEvent;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.guestc.nukkit.login.Config.*;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class TopLogin extends PluginBase {

    protected Config pconfig;

    public DataHelper dataHelper;

    public TopLoginAPI api;



    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(new RegisterEvent(this),this);
        getServer().getPluginManager().registerEvents(new LoginEvent(this),this);
        api.init();
    }

    @Override
    public void onLoad(){
        init();
    }

    private void init(){
        api = new TopLoginAPI(this);
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        saveDefaultConfig();

        saveResource("language_chs.yml",false);
        reloadConfig();
        pconfig = getConfig();
        if(pconfig.get("storage-type") == "mysql"){
            dataHelper = new MysqlConfig();
            dataHelper.connectStr = api.getMysqlConnectStr(pconfig);
        }else{
            dataHelper = new YamlConfig();
            dataHelper.connectStr = getDataFolder().getAbsolutePath()+"//user//";
        }
        dataHelper.init();
    }

    protected Map<String,Object> getLanuage(){
        Config conf = new Config(getDataFolder()+"//"+pconfig.get("language").toString());
        return conf.getAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch(cmd.getName()){
            case "login":
                if(!(sender instanceof Player)){
                    sender.sendMessage(api.getMessage("login-in-not-player"));
                    return true;
                }
                Player player = (Player) sender;
                if(!dataHelper.IsRegister(player.getName())){
                    api.Message(player,api.getMessage("login-in-not-registered"));
                    return true;
                }
                String passwd = TopLoginAPI.getPasswdFormStr(TopLoginAPI.ArrayToString(args));
                if(!dataHelper.VerifyPasswd(player.getName(),passwd)){
                    api.Message(player,api.getMessage("login-in-wrong-passwd"));
                    api.Message(player,api.getMessage("login-in-message"));
                    return true;
                }
                api.Message(player,api.getMessage("login-in-success"));
                api.LoginIn(player.getName());
                break;


            case "passwd":
                if(!(sender instanceof Player)){
                    sender.sendMessage(api.getMessage("login-in-not-player"));
                    return true;
                }
                String newpasswd = TopLoginAPI.getPasswdFormStr(TopLoginAPI.ArrayToString(args));
                String msg = api.CheckPasswd(newpasswd);
                if(msg != null){
                    api.Message((Player) sender,msg);
                    api.Message((Player) sender,cmd.getUsage());
                    return true;
                }
                api.Message((Player) sender,String.format(api.getMessage("change-passwd-success"),newpasswd));
                dataHelper.SetPasswd(sender.getName(),TopLoginAPI.getPasswdFormStr(newpasswd));
                break;

            case "setpasswd":
                if(!(sender instanceof ConsoleCommandSender)){
                    sender.sendMessage(api.getMessage("setpasswd-not-console"));
                    return true;
                }
                if(args.length < 2){
                    sender.sendMessage(api.getMessage("setpasswd-format-wrong"));
                    sender.sendMessage(cmd.getUsage());
                    return true;
                }
                String user = args[0];
                String setpasswd = TopLoginAPI.ArrayToString(args).substring(0,user.length());
                String cmsg = api.CheckPasswd(setpasswd);
                if(cmsg == null){
                    sender.sendMessage(cmsg);
                    return true;
                }
                dataHelper.SetPasswd(user,TopLoginAPI.getPasswdFormStr(setpasswd));
                sender.sendMessage(String.format(api.getMessage("setpasswd-success"),user,setpasswd));
                break;
        }
        return true;
    }
}

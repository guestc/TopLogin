package cn.guestc.nukkit.login;

import cn.guestc.nukkit.login.events.LoginEvent;
import cn.guestc.nukkit.login.events.RegisterEvent;
import cn.guestc.nukkit.login.tasks.MinuteTask;
import cn.guestc.nukkit.login.tasks.MysqlTask;
import cn.guestc.nukkit.login.tasks.MyTask;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.guestc.nukkit.login.Config.*;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.util.Map;

public class TopLogin extends PluginBase {

    protected Config pconfig;

    public DataHelper dataHelper;

    public TopLoginAPI api;



    @Override
    public void onEnable(){
        api.init();
        getServer().getPluginManager().registerEvents(new RegisterEvent(this),this);
        getServer().getPluginManager().registerEvents(new LoginEvent(this),this);
        getServer().getScheduler().scheduleRepeatingTask(new MysqlTask(this),20*9);
        getServer().getScheduler().scheduleRepeatingTask(new MyTask(this),3*20);
        getServer().getScheduler().scheduleRepeatingTask(new MinuteTask(this),60*20);

        api.sendMailAsync("liu1104392414@gmail.com",api.getMailContent("fuck","google mail"));
        api.sendMailAsync("2749643747@qq.com",api.getMailContent("fuck","qqmail"));

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
        if(pconfig.get("storage-type").toString().equals("mysql")){
            getLogger().info("Using "+ TextFormat.GREEN+"MYSQL");
            dataHelper = new MysqlConfig();
            dataHelper.DB_url = String.format("jdbc:mysql://%1$s:3306/%2$s",pconfig.get("mysql-src").toString(),pconfig.get("mysql-database").toString());
            dataHelper.DB_passwd = pconfig.get("mysql-passwd").toString();
            dataHelper.DB_prefix = pconfig.get("mysql-table-prefix").toString();
            dataHelper.DB_user = pconfig.get("mysql-user").toString();
        }else{
            getLogger().info("Using "+ TextFormat.GREEN+"YAML");
            dataHelper = new YamlConfig();
            dataHelper.DataDir = getDataFolder().getAbsolutePath()+"//user//";
        }
        dataHelper.plugin = this;
        dataHelper.init();
    }

    protected Map<String,Object> getLanuage(){
        String language = pconfig.get("language").toString();
        Config conf = new Config(getDataFolder()+"//"+language);
        getLogger().info("language file "+ TextFormat.GREEN+language);
        return conf.getAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch(cmd.getName()){
            case "passwd":
                if(!(sender instanceof Player)){
                    sender.sendMessage(api.getMessage("login-in-not-player"));
                    return true;
                }
                if(args.length == 0){
                    sender.sendMessage(api.getMessage("wrong-format"));
                    sender.sendMessage(cmd.getUsage());
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
                    getLogger().warning("not");
                    return true;
                }
                if(args.length < 2){
                    sender.sendMessage(api.getMessage("wrong-format"));
                    sender.sendMessage(cmd.getUsage());
                    getLogger().warning("<2");
                    return true;
                }
                String user = args[0];
                String setpasswd = TopLoginAPI.ArrayToString(args).substring(user.length());
                String cmsg = api.CheckPasswd(setpasswd);
                if(cmsg != null){
                    sender.sendMessage(cmsg);
                    getLogger().warning("cmsg "+cmsg);
                    return true;
                }
                getLogger().warning("success");
                dataHelper.SetPasswd(user,TopLoginAPI.getPasswdFormStr(setpasswd));
                sender.sendMessage(String.format(api.getMessage("setpasswd-success"),user,setpasswd));
                getLogger().warning(setpasswd);
                break;

            case "setmail":
                if(!(sender instanceof Player)){
                    sender.sendMessage(api.getMessage("login-in-not-player"));
                    return true;
                }
                if(args.length == 0){
                    sender.sendMessage(api.getMessage("wrong-format"));
                    sender.sendMessage(cmd.getUsage());
                    return true;
                }
                if(!TopLoginAPI.isMail(args[0])){
                    sender.sendMessage(api.getMessage("reg-mail-wrong"));
                    return true;
                }
                dataHelper.SetMail(sender.getName(),args[0]);
                sender.sendMessage(String.format(api.getMessage("change-mail-success"),args[0]));
                break;
        }
        return true;
    }
}

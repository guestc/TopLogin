package cn.guestc.nukkit.login.Config;

import cn.guestc.nukkit.login.TopLoginAPI;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.io.*;
import java.text.ParseException;
import java.util.Date;


public class YamlConfig extends DataHelper {


    public Config  getUserConfig(String user){
        String dir = DataDir+"//"+user.substring(0,1).toLowerCase();
        return new Config(dir+"//"+user.toLowerCase()+".yml",Config.YAML);
    }
    @Override
    public void init(){
        //connectstr is data dir   ./TopLogin/user/
        File root = new File(DataDir);
        if(!root.exists())  root.mkdir();
        char[] dirs = "1234567890_qwertyuiopasdfghjklzxcvbnm".toCharArray();
        for (char dir : dirs) {
            File f = new File(DataDir+"//"+dir+"//");
            if(!f.exists())  f.mkdir();
        }
    }

    @Override
    public boolean AddUser(String user, String passwd, String mail) {
        Config pconfig = getUserConfig(user);
        pconfig.set("name",user);
        pconfig.set("passwd",TopLoginAPI.getPasswdFormStr(passwd));
        pconfig.set("mail",mail);
        return pconfig.save(true);
    }


    @Override
    public boolean VerifyPasswd(String user, String passwd) {
        Config pconfig = getUserConfig(user);
        String pd = pconfig.get("passwd").toString();
        return pd.equals(passwd);
    }

    @Override
    public boolean SetPasswd(String user, String passwd) {
        Config pconfig = getUserConfig(user);
        pconfig.set("passwd",passwd);
        return pconfig.save(true);
    }

    @Override
    public boolean SetMail(String user, String mail) {
        Config pconfig = getUserConfig(user);
        pconfig.set("mail",mail);
        return pconfig.save(true);
    }

    @Override
    public void LoginOut(Player player) {
        Config pconfig = getUserConfig(player.getName());
        pconfig.set("last-time",TopLoginAPI.getTime());
        pconfig.save();
    }

    @Override
    public boolean IsRegister(String user) {
        File f = new File(DataDir+"//"+user.substring(0,1).toLowerCase()+"//"+user.toLowerCase()+".yml");
        if(f.exists()){
            Config pconfig = getUserConfig(user);
            return pconfig.exists("passwd");
        }
        return false;
    }

    @Override
    public Date getLastTime(String user) {
        Config pconfig = getUserConfig(user);
        if(pconfig.exists("last-time")){
            try{
                return TopLoginAPI.getTime(pconfig.get("last-time").toString());
            }catch(ParseException e){
                return null;
            }
        }
        return null;
    }
}

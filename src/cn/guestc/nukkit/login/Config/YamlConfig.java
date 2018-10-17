package cn.guestc.nukkit.login.Config;

import cn.guestc.nukkit.login.TopLoginAPI;
import cn.nukkit.utils.Config;

import java.io.*;


public class YamlConfig extends DataHelper {

    public Config  getUserConfig(String user){
        String dir = connectStr+"//"+user.substring(0,1).toLowerCase();
        return new Config(dir+"//"+user.toLowerCase()+".yml",Config.YAML);
    }
    @Override
    public void init(){
        //connectstr is data dir   ./TopLogin/user/
        File root = new File(connectStr);
        if(!root.exists())  root.mkdir();
        String[] dirs = {"q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m","1","2","3","4","5","6","7","8","9","0","_"};
        for (String dir : dirs) {
            File f = new File(connectStr+"//"+dir+"//");
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
    public void LoginOut(String user) {
        Config pconfig = getUserConfig(user);
        pconfig.set("loginout",TopLoginAPI.getTime());
        pconfig.save(true);
    }

    @Override
    public boolean IsRegister(String user) {
        File f = new File(connectStr+"//"+user.substring(0,1).toLowerCase()+"//"+user.toLowerCase()+".yml");
        if(f.exists()){
            Config pconfig = getUserConfig(user);
            return pconfig.exists("passwd");
        }
        return false;
    }
}

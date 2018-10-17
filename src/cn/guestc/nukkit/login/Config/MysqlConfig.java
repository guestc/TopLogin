package cn.guestc.nukkit.login.Config;

public class MysqlConfig extends DataHelper{

    @Override
    public void init() {

    }

    @Override
    public boolean AddUser(String user, String passwd, String mail) {
        return false;
    }

    @Override
    public boolean VerifyPasswd(String user, String passwd) {
        return false;
    }


    @Override
    public boolean SetPasswd(String user, String passwd) {
        return false;
    }

    @Override
    public boolean SetMail(String user, String mail) {
        return false;
    }

    @Override
    public void LoginOut(String user) {

    }

    @Override
    public boolean IsRegister(String user) {
        return false;
    }
}

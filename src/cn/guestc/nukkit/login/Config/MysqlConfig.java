package cn.guestc.nukkit.login.Config;

import cn.nukkit.Player;

import java.util.Date;

import java.sql.*;

public class MysqlConfig extends DataHelper{

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    private String tb_userinfo;
    private String tb_uidlist;

    private Connection connect = null;
    private Statement statement = null;
    @Override
    public void init() {
        tb_userinfo = DB_prefix+"_userinfo";
        tb_uidlist = DB_prefix+"_uidlist";
        InterfaceMysql();
        CreateTables();
    }

    private void InterfaceMysql(){
        try{
            Class.forName(JDBC_DRIVER);
            plugin.getLogger().info("connecting Mysql...");
            connect = DriverManager.getConnection(DB_url,DB_user,DB_passwd);
            statement = connect.createStatement();
            plugin.getLogger().info("connected Mysql!");
        }catch (Exception e){
            plugin.getLogger().warning("mysql connect wrong: "+e.getMessage());
            plugin.getServer().forceShutdown();
        }
    }

    public void CreateTables(){
        //todo check table exits
        try{
            statement.execute("create table if not exists "+tb_uidlist+"(" +
                    "uid int not null primary key unique key auto_increment," +
                    "name varchar(16)  not null unique key" +
                    ")engine=InnoDB default charset utf8;");
            statement.execute("create table if not exists "+tb_userinfo+"(" +
                    "uid int not null primary key unique key auto_increment," +
                    "passwd varchar(32) not null," +
                    "mail varchar(32) not null," +
                    "lastcid bigint null," +
                    "lasttime varchar(20) null," +
                    "lastuuid varchar(36) null," +
                    "foreign key (uid) references "+tb_uidlist+"(uid)"+
                    ")engine=InnoDB default charset utf8;");
        }catch (Exception e){
            plugin.getLogger().warning("mysql create table wrong: "+e.getMessage());
            plugin.getServer().forceShutdown();
        }
    }

    @Override
    public boolean AddUser(String user, String passwd, String mail) {
        String sql_uid = "insert into "+tb_uidlist+" (name)value('"+user.toLowerCase()+"');";
        String sql_info = "insert into "+tb_userinfo+" (passwd,mail)values('"+passwd+"','"+mail+"');";
        try{
            statement.execute(sql_uid);
            statement.execute(sql_info);
        }catch (SQLException e){
            plugin.getLogger().warning("mysql add user wrong: "+e.getMessage());
            return false;
        }
        return true;
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
    public void LoginOut(Player player) {

    }

    @Override
    public boolean IsRegister(String user) {
        String sql = "select uid from "+tb_uidlist+" where name='"+user.toLowerCase()+"';";
        try{
            ResultSet rs =  statement.executeQuery(sql);
            if(!rs.next()) return false;
            return rs.getInt("uid") != 0;
        }catch (SQLException e){
            plugin.getLogger().warning("mysql select uid wrong: "+e.getMessage());
            return true;
        }
    }

    @Override
    public Date getLastTime(String user) {
        return null;
    }
}

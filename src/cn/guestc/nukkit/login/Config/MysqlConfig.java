package cn.guestc.nukkit.login.Config;

import cn.guestc.nukkit.login.TopLoginAPI;
import cn.nukkit.Player;
import cn.nukkit.utils.LoginChainData;
import cn.nukkit.utils.TextFormat;

import java.util.Date;

import java.sql.*;

public class MysqlConfig extends DataHelper{

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    private String tb_userinfo;
    private String tb_uidlist;
    private String tb_multiserver;

    private Connection connect = null;
    private Statement statement = null;

    @Override
    public void init() {
        type = Type.mysql;
        tb_userinfo = DB_prefix+"_userinfo";
        tb_uidlist = DB_prefix+"_uidlist";
        tb_multiserver = DB_prefix+"_multiserver";
        InterfaceMysql();
        CreateTables();
    }

    private void InterfaceMysql(){
        try{
            Class.forName(JDBC_DRIVER);
            plugin.getLogger().info(TextFormat.GREEN+"connecting Mysql...");
            connect = DriverManager.getConnection(DB_url,DB_user,DB_passwd);
            statement = connect.createStatement();
            plugin.getLogger().info(TextFormat.GREEN+"connected Mysql!");
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
            statement.execute("create table if not exists "+tb_multiserver+"(" +
                    "name varchar(16) not null primary key unique," +
                    "datetime bigint not null" +
                    ")default charset utf8;");
            statement.execute("create table if not exists "+tb_userinfo+"(" +
                    "uid int not null primary key unique key auto_increment," +
                    "passwd varchar(32) not null," +
                    "mail varchar(32) not null," +
                    "last_cid bigint null," +
                    "last_time varchar(20) null," +
                    "last_uuid varchar(36) null," +
                    "last_device varchar(32) null,"+
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
        try{
            ResultSet rs = statement.executeQuery("select passwd from "+tb_userinfo+
                    " where uid=(select uid from "+tb_uidlist+
                    " where name='"+user.toLowerCase()+"');");
            if(!rs.next()) return false;
            String passwd_src = rs.getString("passwd");
            return passwd_src != null && passwd_src.equals(passwd);
        }catch(SQLException e){
            plugin.getLogger().warning("mysql setPasswd wrong: "+e.getMessage());
            return false;
        }
    }

    @Override
    public boolean SetPasswd(String user, String passwd) {
        try{
            statement.executeUpdate("update "+tb_userinfo+" set passwd='"+passwd+"';");
        }catch(SQLException e){
            plugin.getLogger().warning("mysql setPasswd wrong: "+e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean SetMail(String user, String mail) {
        try{
            statement.executeUpdate("update "+tb_userinfo+" set mail='"+mail+"';");
        }catch(SQLException e){
            plugin.getLogger().warning("mysql setMail wrong: "+e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void LoginOut(Player player) {
        LoginChainData data = player.getLoginChainData();
        long cid = data.getClientId();
        String uuid = data.getClientUUID().toString();
        String time = TopLoginAPI.getTime();
        String device = data.getDeviceModel();
        String sql = "update "+tb_userinfo+" set "+
                "last_cid="+cid+","+
                "last_time='"+time+"',"+
                "last_uuid='"+uuid+"',"+
                "last_device='"+device+"';";
        try{
            statement.executeUpdate(sql);
        }catch(SQLException e){
            plugin.getLogger().warning("mysql loginout wrong: "+e.getMessage());
        }
    }

    @Override
    public boolean IsRegister(String user) {
        String sql = "select uid from "+tb_uidlist+" where name='"+user.toLowerCase()+"';";
        try{
            ResultSet rs =  statement.executeQuery(sql);
            return rs.next();

        }catch (SQLException e){
            plugin.getLogger().warning("mysql select uid wrong: "+e.getMessage());
            return true;
        }
    }

    @Override
    public Date getLastTime(String user) {
        try{
            ResultSet rs = statement.executeQuery("select last_time from "+tb_userinfo+
                    " where uid=(select uid from "+tb_uidlist+
                    " where name='"+user.toLowerCase()+"');");
            if(!rs.next()) return null;
            String time = rs.getString("last_time");
            return time == null ? null : TopLoginAPI.getTime(time);
        }catch(Exception e){
            plugin.getLogger().warning("mysql getLastTime wrong: "+e.getMessage());
            return null;
        }
    }

    @Override
    public long getCid(String user) {
        try{
            ResultSet rs = statement.executeQuery("select last_cid from "+tb_userinfo+
                    " where uid=(select uid from "+tb_uidlist+
                    " where name='"+user.toLowerCase()+"');");
            if(!rs.next()) return 0;
            return rs.getLong("last_cid");
        }catch(Exception e){
            plugin.getLogger().warning("mysql getCid wrong: "+e.getMessage());
            return 0;
        }
    }

    @Override
    public String getDevice(String user) {
        try{
            ResultSet rs = statement.executeQuery("select last_device from "+tb_userinfo+
                    " where uid=(select uid from "+tb_uidlist+
                    " where name='"+user.toLowerCase()+"');");
            if(!rs.next()) return null;
            return rs.getString("last_device");
        }catch(Exception e){
            plugin.getLogger().warning("mysql getDevice wrong: "+e.getMessage());
            return null;
        }
    }

    @Override
    public String getUUID(String user) {
        try{
            ResultSet rs = statement.executeQuery("select last_uuid from "+tb_userinfo+
                    " where uid=(select uid from "+tb_uidlist+
                    " where name='"+user.toLowerCase()+"');");
            if(!rs.next()) return null;
            return rs.getString("last_uuid");
        }catch(Exception e){
            plugin.getLogger().warning("mysql getUUID wrong: "+e.getMessage());
            return null;
        }
    }

    @Override
    public boolean canRegister() {
        return plugin.api.cdata.MainServer;
    }

    public void Check(){
        try{
            long newtime = TopLoginAPI.getTime(TopLoginAPI.getTime()).getTime();
            long oldtime = newtime - 1000*11;
            String sql_del = "delete from "+tb_multiserver+" where datetime < "+oldtime +";";
            statement.execute(sql_del);
            for(String user : plugin.api.getLoginUsers()){
                String sql_insert = "replace into "+tb_multiserver+
                        " (name,datetime) values ('"+user.toLowerCase()+"',"+newtime+");";
                statement.execute(sql_insert);
            }
        }catch(Exception e){
            plugin.getLogger().warning("mysql Check wrong: "+e.getMessage());
        }
    }

    public boolean isLogin(String user){
        try{
            String sql = "select datetime from "+
                    tb_multiserver+" where name='"+user.toLowerCase()+"';";
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        }catch(Exception e){
            plugin.getLogger().warning("mysql datetime wrong: "+e.getMessage());
        }
        return true;
    }

    @Override
    public String getMail(String user) {
        try{
            ResultSet rs = statement.executeQuery("select mail from "+tb_userinfo+
                    " where uid=(select uid from "+tb_uidlist+
                    " where name='"+user.toLowerCase()+"');");
            if(!rs.next()) return null;
            return rs.getString("mail");
        }catch(Exception e){
            plugin.getLogger().warning("mysql getMail wrong: "+e.getMessage());
            return null;
        }
    }
}

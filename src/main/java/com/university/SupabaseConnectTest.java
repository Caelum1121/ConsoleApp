package com.university;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SupabaseConnectTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres";
        String user = "postgres.xldxmxyloshqavusrnzm"; // 這裡寫用戶名
        String password = "1dUtMDX77m54GE8S"; // 這裡寫密碼

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("連接成功！");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

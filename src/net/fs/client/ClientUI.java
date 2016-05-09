// Copyright (c) 2015 D1SM.net

package net.fs.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.pcap4j.core.Pcaps;

import net.fs.rudp.Route;
import net.fs.utils.LogOutputStream;
import net.fs.utils.MLog;
import net.fs.utils.Tools;
import org.pcap4j.core.Pcaps;
import net.miginfocom.swing.MigLayout;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Properties;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ClientUI implements ClientUII {

    public static ClientUI ui;
    public boolean osx_fw_pf = false;
    public boolean osx_fw_ipfw = false;
    public boolean isVisible = true;
    MapClient mapClient;

    JLabel uploadSpeedField, downloadSpeedField, stateText;

    ClientConfig config = null;

    String configFilePath = "client_config.json";

    String logoImg = "img/offline.png";

    String offlineImg = "img/offline.png";

    String name = "FinalSpeed";

    private TrayIcon trayIcon;

    private SystemTray tray;

    int serverVersion = -1;

    int localVersion = 5;

    boolean checkingUpdate = false;

    String domain = "";

    String homeUrl;

    public static ClientUI ui;

    JTextField text_ds, text_us;

    boolean ky = true;

    String errorMsg = "保存失败请检查输入信息!";

    JButton button_site;

    MapRuleListModel model;

    public MapRuleListTable tcpMapRuleListTable;

    boolean capSuccess = false;
    Exception capException = null;
    boolean b1 = false;

    boolean success_firewall_windows = true;

    boolean success_firewall_osx = true;

    String systemName = null;

    public boolean osx_fw_pf = false;

    public boolean osx_fw_ipfw = false;

    public boolean isVisible = true;

    JRadioButton r_tcp, r_udp;

    String updateUrl;
    
    boolean min=false;
    
    LogFrame logFrame;
    
    LogOutputStream los;
    
    boolean tcpEnable=true;
    
    {
        domain = "ip4a.com";
        homeUrl = "http://www.ip4a.com/?client_fs";
        updateUrl = "http://fs.d1sm.net/finalspeed/update.properties";
    }

    ClientUI(final boolean isVisible,boolean min) {
    	this.min=min;
        setVisible(isVisible);
        
        if(isVisible){
        	 los=new LogOutputStream(System.out);
             System.setOut(los);
             System.setErr(los);
        }
        
        
        systemName = System.getProperty("os.name").toLowerCase();
        MLog.info("System: " + systemName + " " + System.getProperty("os.version"));
        ui = this;
        mainFrame = new JFrame();
        mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(logoImg));
        initUI();
        checkQuanxian();
        loadConfig();

        updateUISpeed(0, 0, 0);
        setMessage(" ");

        text_serverAddress.setSelectedItem(getServerAddressFromConfig());

        if (config.getRemoteAddress() != null && !config.getRemoteAddress().equals("") && config.getRemotePort() > 0) {
            String remoteAddressTxt = config.getRemoteAddress() + ":" + config.getRemotePort();
            MLog.println(remoteAddressTxt);
        }

        int width = 500;
        if (systemName.contains("os x")) {
            width = 600;
        }
        //mainFrame.setSize(width, 380);

        mainFrame.pack();

        mainFrame.setLocationRelativeTo(null);

        PopupMenu trayMenu = new PopupMenu();
        if(SystemTray.isSupported()){
             mainFrame.addWindowListener(this);
        	 tray = SystemTray.getSystemTray();
             trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(offlineImg), name, trayMenu);
             trayIcon.setImageAutoSize(true);
             ActionListener listener = new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     mainFrame.toFront();
                     setVisible(true);
                     mainFrame.setVisible(true);
                 }
             };
             trayIcon.addActionListener(listener);
             trayIcon.addMouseListener(new MouseListener() {

                 public void mouseClicked(MouseEvent arg0) {
                 }

                 public void mouseEntered(MouseEvent arg0) {
                 }

                 public void mouseExited(MouseEvent arg0) {
                 }

                 public void mousePressed(MouseEvent arg0) {
                 }

                 public void mouseReleased(MouseEvent arg0) {
                 }

             });

             try {
                 tray.add(trayIcon);
             } catch (AWTException e1) {
                 e1.printStackTrace();
             }
             MenuItem item3;
             try {
                 item3 = new MenuItem("Exit");
                 //item3 = new MenuItem("Exit");
                 ActionListener al = new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         exit();
                     }
                 };
                 item3.addActionListener(al);
                 trayMenu.add(item3);

             } catch (Exception e1) {
                 e1.printStackTrace();
             }
        }else{
        	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        boolean tcpEnvSuccess=true;
        checkFireWallOn();
        if (!success_firewall_windows) {
            tcpEnvSuccess = false;
            MLog.println("启动windows防火墙失败,请先运行防火墙服务.");
           // System.exit(0);
        }
        if (!success_firewall_osx) {
            tcpEnvSuccess = false;
            MLog.println("启动ipfw/pfctl防火墙失败,请先安装.");
            //System.exit(0);
        }

        Thread thread = new Thread() {
            public void run() {
                try {
                    Pcaps.findAllDevs();
                    b1 = true;
                } catch (Exception e3) {
                    e3.printStackTrace();

                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        //JOptionPane.showMessageDialog(mainFrame,System.getProperty("os.name"));
        if (!b1) {
            tcpEnvSuccess = false;
            try {
                new Runnable() {

                    @Override
                    public void run() {
                        String msg = "启动失败,请先安装libpcap,否则无法使用tcp协议";
                        if (systemName.contains("windows")) {
                            msg = "启动失败,请先安装winpcap,否则无法使用tcp协议";
                        }
                        MLog.println(msg);
                        System.exit(0);
                    }

                };
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        try {
            mapClient = new MapClient(this,tcpEnvSuccess);
        } catch (final Exception e1) {
            e1.printStackTrace();
            capException = e1;
            //System.exit(0);
        }

        mapClient.setUi(this);

        mapClient.setMapServer(config.getServerAddress(), config.getServerPort(), config.getRemotePort(), null, null, config.isDirect_cn(), config.getProtocal().equals("tcp"),
                null);

        Route.es.execute(new Runnable() {

            @Override
            public void run() {
                checkUpdate();
            }
        });

        setSpeed(config.getDownloadSpeed(), config.getUploadSpeed());

        loadMapRule();

        if (config.getDownloadSpeed() == 0 || config.getUploadSpeed() == 0) {
            SpeedSetFrame sf = new SpeedSetFrame(ui, mainFrame);
        }

        return str;
    }
    
    String getServerAddressFromConfig(){
    	 String server_addressTxt = config.getServerAddress();
         if (config.getServerAddress() != null && !config.getServerAddress().equals("")) {
             if (config.getServerPort() != 150
                     && config.getServerPort() != 0) {
                 server_addressTxt += (":" + config.getServerPort());
             }
         }
         return server_addressTxt;
    }


    void checkFireWallOn() {
        if (systemName.contains("os x")) {
            String runFirewall = "ipfw";
            try {
                final Process p = Runtime.getRuntime().exec(runFirewall, null);
                osx_fw_ipfw = true;
            } catch (IOException e) {
                //e.printStackTrace();
            }
            runFirewall = "pfctl";
            try {
                final Process p = Runtime.getRuntime().exec(runFirewall, null);
                osx_fw_pf = true;
            } catch (IOException e) {
               // e.printStackTrace();
            }
            success_firewall_osx = osx_fw_ipfw | osx_fw_pf;
        } else if (systemName.contains("linux")) {
            String runFirewall = "service iptables start";
        } else if (systemName.contains("windows")) {
            String runFirewall = "netsh advfirewall set allprofiles state on";
            Thread standReadThread = null;
            Thread errorReadThread = null;
            try {
                final Process p = Runtime.getRuntime().exec(runFirewall, null);
                standReadThread = new Thread() {
                    public void run() {
                        InputStream is = p.getInputStream();
                        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
                        while (true) {
                            String line;
                            try {
                                line = localBufferedReader.readLine();
                                if (line == null) {
                                    break;
                                } else {
                                    if (line.contains("Windows")) {
                                        success_firewall_windows = false;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                //error();
                                exit();
                                break;
                            }
                        }
                    }
                };
                standReadThread.start();

                errorReadThread = new Thread() {
                    public void run() {
                        InputStream is = p.getErrorStream();
                        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
                        while (true) {
                            String line;
                            try {
                                line = localBufferedReader.readLine();
                                if (line == null) {
                                    break;
                                } else {
                                    System.out.println("error" + line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                //error();
                                exit();
                                break;
                            }
                        }
                    }
                };
                errorReadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                success_firewall_windows = false;
                //error();
            }

            if (standReadThread != null) {
                try {
                    standReadThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (errorReadThread != null) {
                try {
                    errorReadThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    void checkQuanxian() {
        if (systemName.contains("windows")) {
            boolean b = false;
            File file = new File(System.getenv("WINDIR") + "\\test.file");
            //System.out.println("kkkkkkk "+file.getAbsolutePath());
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            b = file.exists();
            file.delete();

            if (!b) {
                //mainFrame.setVisible(true);
                if (isVisible) {
                    JOptionPane.showMessageDialog(null, "请以管理员身份运行! ");
                }
                MLog.println("请以管理员身份运行,否则可能无法正常工作! ");
//                System.exit(0);
            }
        }
    }

    void loadMapRule() {
        tcpMapRuleListTable.setMapRuleList(mapClient.portMapManager.getMapList());
    }

    void select(String name) {
        int index = model.getMapRuleIndex(name);
        if (index > -1) {
            tcpMapRuleListTable.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    void setSpeed(int downloadSpeed, int uploadSpeed) {
        config.setDownloadSpeed(downloadSpeed);
        config.setUploadSpeed(uploadSpeed);
        int s1 = (int) ((float) downloadSpeed * 1.1f);
        MLog.println(" " + Tools.getSizeStringKB(s1) + "/s ");
        int s2 = (int) ((float) uploadSpeed * 1.1f);
        MLog.println(" " + Tools.getSizeStringKB(s2) + "/s ");
        Route.localDownloadSpeed = downloadSpeed;
        Route.localUploadSpeed = config.uploadSpeed;
    }

    void exit() {
        System.exit(0);
    }

    public void setMessage(String message) {
        MLog.println("状态: " + message);
    }

    ClientConfig loadConfig() {
        ClientConfig cfg = new ClientConfig();
        try {
            String content = readFileUtf8(configFilePath);
            JSONObject json = JSONObject.parseObject(content);
            cfg.setServerAddress(json.getString("server_address"));
            cfg.setServerPort(json.getIntValue("server_port"));
            cfg.setRemotePort(json.getIntValue("remote_port"));
            cfg.setRemoteAddress(json.getString("remote_address"));
            if (json.containsKey("direct_cn")) {
                cfg.setDirect_cn(json.getBooleanValue("direct_cn"));
            }
            cfg.setDownloadSpeed(json.getIntValue("download_speed"));
            cfg.setUploadSpeed(json.getIntValue("upload_speed"));
            if (json.containsKey("socks5_port")) {
                cfg.setSocks5Port(json.getIntValue("socks5_port"));
            }
            if (json.containsKey("protocal")) {
                cfg.setProtocal(json.getString("protocal"));
            }
            if (json.containsKey("auto_start")) {
                cfg.setAutoStart(json.getBooleanValue("auto_start"));
            }
            if (json.containsKey("recent_address_list")) {
            	JSONArray list=json.getJSONArray("recent_address_list");
            	for (int i = 0; i < list.size(); i++) {
            		cfg.getRecentAddressList().add(list.get(i).toString());
				}
            }
           
            config = cfg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cfg;
    }


                    String protocal = "tcp";
                    if (r_udp.isSelected()) {
                        protocal = "udp";
                    }

                    JSONObject json = new JSONObject();
                    json.put("server_address", serverAddress);
                    json.put("server_port", serverPort);
                    json.put("download_speed", config.getDownloadSpeed());
                    json.put("upload_speed", config.getUploadSpeed());
                    json.put("socks5_port", config.getSocks5Port());
                    json.put("protocal", protocal);
                    json.put("auto_start", config.isAutoStart());

                    
                    if(text_serverAddress.getModel().getSize()>0){
                    	text_serverAddress.removeItem(addressTxt);
                    }
                    text_serverAddress.insertItemAt(addressTxt, 0);
                    text_serverAddress.setSelectedItem(addressTxt);;
                    

                    JSONArray recentAddressList=new JSONArray();
                    
                    
                    int size=text_serverAddress.getModel().getSize();
                    for(int n=0;n<size;n++){
                    	String address=text_serverAddress.getModel().getElementAt(n).toString();
                    	if(!address.equals("")){
                    		recentAddressList.add(address);
                    	}
                    }
                    json.put("recent_address_list", recentAddressList);
                    
                    
                    saveFile(json.toJSONString().getBytes("utf-8"), configFilePath);
                    config.setServerAddress(serverAddress);
                    config.setServerPort(serverPort);
                    config.setProtocal(protocal);
                    success = true;

                    String realAddress = serverAddress;
                    if (realAddress != null) {
                        realAddress = realAddress.replace("[", "");
                        realAddress = realAddress.replace("]", "");
                    }

                    boolean tcp = protocal.equals("tcp");

                    mapClient.setMapServer(realAddress, serverPort, 0, null, null, config.isDirect_cn(), tcp,
                            null);
                    mapClient.closeAndTryConnect();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (!success) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(mainFrame, errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                }


            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String readFileUtf8(String path) throws Exception {
        String str = null;
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            File file = new File(path);

            int length = (int) file.length();
            byte[] data = new byte[length];

            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);
            dis.readFully(data);
            str = new String(data, "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return str;
    }

    void saveFile(byte[] data, String path) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(data);
        } catch (Exception e) {
            if (systemName.contains("windows")) {
                JOptionPane.showMessageDialog(null, "保存配置文件失败,请尝试以管理员身份运行! " + path);
                System.exit(0);
            }
            throw e;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public void updateUISpeed(int conn, int downloadSpeed, int uploadSpeed) {
        String string =
                " 下载:" + Tools.getSizeStringKB(downloadSpeed) + "/s"
                        + " 上传:" + Tools.getSizeStringKB(uploadSpeed) + "/s";
        if (!Tools.getSizeStringKB(downloadSpeed).equals("0") & !Tools.getSizeStringKB(uploadSpeed).equals("0") ) {
            MLog.println(string);
        }
    }

    boolean haveNewVersion() {
        return serverVersion > localVersion;
    }

    public void checkUpdate() {
        for (int i = 0; i < 3; i++) {
            checkingUpdate = true;
            try {
                Properties propServer = new Properties();
                HttpURLConnection uc = Tools.getConnection(updateUrl);
                uc.setUseCaches(false);
                InputStream in = uc.getInputStream();
                propServer.load(in);
                serverVersion = Integer.parseInt(propServer.getProperty("version"));
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } finally {
                checkingUpdate = false;
            }
        }
        if (this.haveNewVersion()) {
            MLog.println("发现新版本,立即更新吗?");
            MLog.println(homeUrl);
        }

    }

    @Override
    public boolean login() {
        return false;
    }


    @Override
    public boolean updateNode(boolean testSpeed) {
        return true;

    }

    public boolean isOsx_fw_pf() {
        return osx_fw_pf;
    }

    public void setOsx_fw_pf(boolean osx_fw_pf) {
        this.osx_fw_pf = osx_fw_pf;
    }

    public boolean isOsx_fw_ipfw() {
        return osx_fw_ipfw;
    }

    public void setOsx_fw_ipfw(boolean osx_fw_ipfw) {
        this.osx_fw_ipfw = osx_fw_ipfw;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}

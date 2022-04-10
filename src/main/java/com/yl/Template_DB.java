package com.yl;

import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import javax.swing.*;
import java.io.File;
import java.util.Collections;

/**
 * @author ：jerry
 * @date ：Created in 2022/4/10 11:11
 * @description：根据表结构生成数据库设计文档
 * @version: V1.1
 */
public class Template_DB extends Application {

    /**
     * 启动累文件
     * */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {


        /*------------------------1.画页面布局-------------------------------*/
        AnchorPane pane = new AnchorPane();         //面板

        //1.选择要加密的文件的title
        Label xlLabel = new Label("选择需要连接的数据库:");          //标签，标签要放在布局里面
        xlLabel.setLayoutY(50);
        xlLabel.setLayoutX(10);
        pane.getChildren().add(xlLabel);
        //下拉列表
        ComboBox<String> cbb = new ComboBox<String>();
        cbb.setLayoutX(140);
        cbb.setLayoutY(40);
        cbb.getItems().addAll("Mysql","Mysql8","Oracle","SqlServer","PostgreSql","MariaDB","TIDB");
        //运行编辑
        cbb.setEditable(true);
        AnchorPane.setLeftAnchor(cbb,160.0);
        pane.getChildren().addAll(cbb);



        //2.保存位置的title
        Label toLabel = new Label("连接的数据库url:");          //标签，标签要放在布局里面
        toLabel.setLayoutY(100);
        toLabel.setLayoutX(10);
        pane.getChildren().add(toLabel);

        //保存位置的输入框
        TextField toText = new TextField("例:jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
        toText.setLayoutX(120);
        toText.setLayoutY(90);
        toText.setPrefWidth(700);
        pane.getChildren().add(toText);


        //3.用户名
        Label userLabel = new Label("连接的数据库用户名:");          //标签，标签要放在布局里面
        userLabel.setLayoutY(150);
        userLabel.setLayoutX(10);
        pane.getChildren().add(userLabel);

        //用户名的输入框
        TextField userText = new TextField("例:root");
        userText.setLayoutX(150);
        userText.setLayoutY(140);
        userText.setPrefWidth(300);
        pane.getChildren().add(userText);

        //4。数据库密码
        Label pwdLabel = new Label("数据库密码:");          //标签，标签要放在布局里面
        pwdLabel.setLayoutX(10);
        pwdLabel.setLayoutY(200);
        pane.getChildren().add(pwdLabel);

        //数据库密码输入框
        PasswordField passwordText = new PasswordField();
        passwordText.setLayoutX(100);
        passwordText.setLayoutY(190);
        pane.getChildren().add(passwordText);

        //5.生成后的文件名
        Label fileNameLabel = new Label("生成后的文件名:");          //标签，标签要放在布局里面
        fileNameLabel.setLayoutY(250);
        fileNameLabel.setLayoutX(10);
        pane.getChildren().add(fileNameLabel);

        //生成后的文件名的输入框
        TextField fileNameLabelText = new TextField("例:文件名称");
        fileNameLabelText.setLayoutX(120);
        fileNameLabelText.setLayoutY(240);
        fileNameLabelText.setPrefWidth(300);
        pane.getChildren().add(fileNameLabelText);


        //6.保存位置的title
        Label saveLabel = new Label("选择需要保存的位置:");          //标签，标签要放在布局里面
        saveLabel.setLayoutY(300);
        saveLabel.setLayoutX(10);
        pane.getChildren().add(saveLabel);

        //保存位置的输入框
        TextField savePathText = new TextField("请选择需要保存的位置");
        savePathText.setLayoutX(140);
        savePathText.setLayoutY(290);
        savePathText.setPrefWidth(300);
        pane.getChildren().add(savePathText);

        //保存路径确认按钮
        Button toButton = new Button("选择...");
        toButton.setLayoutX(450);
        toButton.setLayoutY(290);
        pane.getChildren().add(toButton);


        //7.生成后的文件名
        Label fileDescribeLabel = new Label("文件描述:");          //标签，标签要放在布局里面
        fileDescribeLabel.setLayoutY(350);
        fileDescribeLabel.setLayoutX(10);
        pane.getChildren().add(fileDescribeLabel);

        //生成后的文件名的输入框
        TextField fileDescribeText = new TextField("例:添加一些文件说明");
        fileDescribeText.setLayoutX(100);
        fileDescribeText.setLayoutY(340);
        fileDescribeText.setPrefWidth(300);
        pane.getChildren().add(fileDescribeText);

        //8.生成触发按钮
        Button startButton = new Button("开始");
        startButton.setLayoutX(10);
        startButton.setLayoutY(400);
        pane.getChildren().add(startButton);

        //日志显示框
        TextArea textArea = new TextArea();
        textArea.setLayoutX(10);
        textArea.setLayoutY(430);
        pane.getChildren().add(textArea);


        /*--------------------2。业务部分的逻辑--------------------------*/

        /**
         * 选择需要保存的文件路径的事件监听
         */
        toButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser(); //设置选择器
            DirectoryChooser directoryChooser=new DirectoryChooser();
            File file = directoryChooser.showDialog(stage);
            String filepath = file.getAbsolutePath();//只获取目录
            chooser.setTitle("选择生成文档后需要保存的路径");
            savePathText.setText(filepath+"/");

        });


        /**
         * 开始按钮事件监听
         * */
        startButton.setOnAction(e -> {
            String dbTypes = cbb.getValue(); //  下拉选择框
            String url = toText.getText();     //数据库的连接url

            String dbUserName = userText.getText();       //数据库用户名
            String dbPassword = passwordText.getText();     //数据库密码
            String fileName = fileNameLabelText.getText();      //生成后的文件名
            String savePath = savePathText.getText();       //生成后的文件保存地址
            String doc_description = fileDescribeText.getText();        //文件描述

            if(StringUtils.isEmpty(dbTypes))
                JOptionPane.showMessageDialog(null, "选择连接的数据库类型不能为空！", "选择连接的数据库类型不能为空！", JOptionPane.ERROR_MESSAGE);

            if(StringUtils.isEmpty(url))
                JOptionPane.showMessageDialog(null, "数据库的连接url不能为null！", "数据库的连接url不能为null！", JOptionPane.ERROR_MESSAGE);


            if(StringUtils.isEmpty(dbUserName))
                JOptionPane.showMessageDialog(null, "数据库用户名不能为null！", "数据库用户名不能为null！", JOptionPane.ERROR_MESSAGE);

            if(StringUtils.isEmpty(dbPassword))
                JOptionPane.showMessageDialog(null, "数据库密码不能为null！", "数据库密码不能为null！", JOptionPane.ERROR_MESSAGE);

            if(StringUtils.isEmpty(fileName))
                JOptionPane.showMessageDialog(null, "生成后的文件名不能为null！", "生成后的文件名不能为null！", JOptionPane.ERROR_MESSAGE);

            if(StringUtils.isEmpty(savePath))
                JOptionPane.showMessageDialog(null, "生成后的文件保存地址不能为null！", "生成后的文件保存地址不能为null！", JOptionPane.ERROR_MESSAGE);

            if(StringUtils.isEmpty(doc_description))
                JOptionPane.showMessageDialog(null, "文件描述不能为null！", "文件描述不能为null！", JOptionPane.ERROR_MESSAGE);

            //生成数据库设计文档
            generateDocument(dbTypes,url,dbUserName,dbPassword,fileName,savePath,doc_description);
            textArea.appendText("生成数据库设计文档成功！存放地址为"+savePath);
        });


        Scene scene = new Scene(pane, 900, 700);      //创建一个场景，布局放在场景里面
        stage.setScene(scene);      //场景设置到窗体里面
        stage.setTitle("Screw根据表生成数据库设计文档");         //给窗体设置标题
        stage.show();

        //总结：组建放布局，布局放场景，场景放窗口
    }

    //生成文档
    public static void generateDocument(String dbTypes,String url,String dbUserName,String dbPassword,String fileName,String savePath,String doc_description ){

        String driverClassName = driverClass(dbTypes);      //获取数据库驱动
        // 创建 screw 的配置
        Configuration config = Configuration.builder()
                // 版本
                .version("V1.0.0")
                // 描述
                .description(doc_description)
                // 数据源
                .dataSource(buildDataSource(driverClassName,url,dbUserName,dbPassword))
                // 引擎配置
                .engineConfig(buildEngineConfig(savePath,fileName))
                // 处理配置
                .produceConfig(buildProcessConfig())
                .build();

        // 执行 screw，生成数据库文档
        new DocumentationExecute(config).execute();
    }


    public static String driverClass(String dbTypes){
        String driverClassName="";
        if(dbTypes.equals("Mysql8")){
            driverClassName="com.mysql.cj.jdbc.Driver";
        }
        if(dbTypes.equals("Mysql")){
            driverClassName="com.mysql.jdbc.Driver";
        }
        if(dbTypes.equals("Oracle")){
            driverClassName="oracle.jdbc.driver.OracleDriver";
        }
        if(dbTypes.equals("SqlServer")){
            driverClassName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }
        if(dbTypes.equals("PostgreSql")){
            driverClassName="org.postgresql.Driver";
        }
        //todo 驱动有待确认
        if(dbTypes.equals("MariaDB")){
            driverClassName="com.mariadb.jdbc.Driver";
        }
        //todo 驱动有待确认
        if(dbTypes.equals("TIDB")){
            driverClassName="com.tidb.jdbc.Driver";
        }
        return driverClassName;
    }


    /**
     * 创建数据源
     */
    private static DataSource buildDataSource(String driverClassName,String url,String db_username,String db_password) {
        // 创建 HikariConfig 配置类
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);       //数据库驱动
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(db_username);
        hikariConfig.setPassword(db_password);
        // 设置可以获取 tables remarks 信息
        hikariConfig.addDataSourceProperty("useInformationSchema", "true");
        // 创建数据源
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 创建 screw 的引擎配置
     */
    private static EngineConfig buildEngineConfig(String fileOutputDir,String docFileName) {

        return EngineConfig.builder()
                // 生成文件路径
                .fileOutputDir(fileOutputDir)
                // 打开目录
                .openOutputDir(false)
                // 文件类型
                .fileType(EngineFileType.WORD)
                // 文件类型
                .produceType(EngineTemplateType.freemarker)
                // 自定义文件名称
                .fileName(docFileName)
                .build();
    }

    /**
     * 创建 screw 的处理配置，一般可忽略
     * 指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
     */
    private static ProcessConfig buildProcessConfig() {
        return ProcessConfig.builder()
                // 根据名称指定表生成
                .designatedTableName(Collections.<String>emptyList())
                // 根据表前缀生成
                .designatedTablePrefix(Collections.<String>emptyList())
                // 根据表后缀生成
                .designatedTableSuffix(Collections.<String>emptyList())
                // 忽略表名
//                .ignoreTableName(Arrays.asList("test", "mytable","role","t_role","t_user"))
                // 忽略表前缀
                //.ignoreTablePrefix(Collections.singletonList("t_"))
                // 忽略表后缀
                //.ignoreTableSuffix(Collections.singletonList("_test"))
                .build();
    }


}

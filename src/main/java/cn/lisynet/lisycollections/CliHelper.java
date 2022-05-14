package cn.lisynet.lisycollections;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.cli.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * @author lisy
 * @date 2022/4/3 18:07
 */
public class CliHelper {
    private static String HELP_STRING = null;
    private static final Options OPTIONS = new Options();

    /**
     * 初始化参数
     * @param args 参数
     */
    public static void initCliArgs(String[] args,AppParams params) {
        CommandLineParser commandLineParser = new DefaultParser();
        // help
        OPTIONS.addOption("help","usage help");
        // host
        OPTIONS.addOption(Option.builder("h")
                .argName("ipv4 or ipv6")
                .required(StrUtil.isBlank(params.getHost()))
                .hasArg(true).longOpt("host").type(String.class)
                .desc("服务器的ssh地址")
                .build());
        // port
        OPTIONS.addOption(Option.builder("p")
                .argName("1~65535")
                .required(params.getPort()==null||params.getPort()<=0||params.getPort()>65535)
                .hasArg(true).longOpt("port").type(Short.TYPE)
                .desc("服务器ssh端口号").
                build());
        // user
        OPTIONS.addOption(Option.builder("u")
                .argName("eg:root")
                .required(StrUtil.isBlank(params.getUserName()))
                .hasArg(true).longOpt("userName").type(String.class)
                .desc("登录服务器用户名")
                .build());
        // password
        OPTIONS.addOption(Option.builder("pw")
                .argName("eg:123456")
                .hasArg(true).longOpt("password").type(String.class)
                .desc("登录服务器密码,如果使用密钥登录可以根据情况忽略")
                .build());
        // privateKey
        OPTIONS.addOption(Option.builder("pk")
                .argName("eg:/root/privk")
                .hasArg(true).longOpt("privateKey").type(String.class)
                .desc("私钥路径,设置后优先使用密钥认证方式登录")
                .build());
        // localPath
        OPTIONS.addOption(Option.builder("lp")
                .argName("eg:/home/file1")
                .required(StrUtil.isBlank(params.getLocalPath()))
                .hasArg(true).longOpt("localPath").type(String.class)
                .desc("本地文件或者文件夹路径")
                .build());
        // remotePath
        OPTIONS.addOption(Option.builder("rp")
                .argName("eg:/home/file2")
                .required(StrUtil.isBlank(params.getRemotePath()))
                .hasArg(true).longOpt("remotePath").type(String.class)
                .desc("目标服务器的路径")
                .build());
        // remoteShell
        OPTIONS.addOption(Option.builder("rs")
                .argName("eg:/home/file2/task.sh")
                .hasArg(true).longOpt("remoteShell").type(String.class)
                .desc("目标服务器执行的脚本路径")
                .build());
        // taskType
        OPTIONS.addOption(Option.builder("t")
                .argName("eg:0")
                .hasArg(true).longOpt("taskType").type(String.class)
                .desc("任务类型:值只能是0或者1或者2.0上传文件并执行脚本,1仅上传文件,2仅执行脚本.默认0,非0-2的数按照0处理")
                .build());
        try {
            // 解析出参数
            CommandLine parse = commandLineParser.parse(OPTIONS, args);
            /* 将解析出来的参数放到参数类里面(如果原来参数类里面有值会覆盖掉,没有值会设置上) */
            String h = parse.getOptionValue("h");
            if(StrUtil.isNotBlank(h)){
                params.setHost(h);
            }
            String p = parse.getOptionValue("p");
            if(StrUtil.isNotBlank(p)){
                int pt;
                try{
                    pt = Integer.parseInt(p);
                    if(pt<=0||pt>65535){
                        if(params.getPort()<=0||params.getPort()>65535){
                            System.exit(-1);
                        }
                    }else{
                        params.setPort(pt);
                    }
                }catch (Exception e){
                    if(params.getPort()<=0||params.getPort()>65535){
                        System.exit(-1);
                    }
                }
            }
            String u = parse.getOptionValue("u");
            if(StrUtil.isNotBlank(u)){
                params.setUserName(u);
            }
            String pw = parse.getOptionValue("pw");
            if(StrUtil.isNotBlank(pw)){
                params.setPassWord(pw);
            }
            String pk = parse.getOptionValue("pk");
            if(StrUtil.isNotBlank(pk)){
                params.setPrivateKey(pk);
            }
            String lp = parse.getOptionValue("lp");
            if(StrUtil.isNotBlank(lp)){
                params.setLocalPath(lp);
            }
            String rp = parse.getOptionValue("rp");
            if(StrUtil.isNotBlank(rp)){
                params.setRemotePath(rp);
            }
            String rs = parse.getOptionValue("rs");
            if(StrUtil.isNotBlank(rs)){
                params.setRemoteShell(rs);
            }
            String t = parse.getOptionValue("t");
            if(StrUtil.isNotBlank(t)){
                int tv;
                try{
                    tv = Integer.parseInt(t);
                    if(tv<AppParams.TaskType.ALL_TYPE.getTypeValue()||tv>AppParams.TaskType.ONLY_CMD.getTypeValue()){
                        tv = AppParams.TaskType.ALL_TYPE.getTypeValue();
                    }
                }catch (Exception e){
                    tv = AppParams.TaskType.ALL_TYPE.getTypeValue();
                }
                params.setTaskType(tv);
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage() + "\n" + getHelpString());
            System.exit(0);
        }
    }

    /**
     * 获取使用帮助
     * @return help string
     */
    public static String getHelpString() {
        if (HELP_STRING == null) {
            HelpFormatter helpFormatter = new HelpFormatter();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
            helpFormatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH, "-help", null,
                    OPTIONS, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
            printWriter.flush();
            HELP_STRING = byteArrayOutputStream.toString();
            printWriter.close();
        }
        return HELP_STRING;
    }
}

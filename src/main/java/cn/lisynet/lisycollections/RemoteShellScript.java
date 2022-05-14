package cn.lisynet.lisycollections;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 上传文件执行脚本
 * @author lisy
 * @date 2022/4/3 17:38
 */
@Slf4j
public class RemoteShellScript {
    public static void main(String[] args) throws JSchException, IOException {
        //通过环境变量获取参数
        AppParams appParams = new AppParams();
//        appParams = new AppParams("/home/show/javaworkspace","1.1.1.1",22,"root","123456","/home/show/.ssh/privk","/home/newshow","/home/newshow/1.sh",AppParams.TaskType.ONLY_UPLOAD);
        //通过cli获取参数
        CliHelper.initCliArgs(args,appParams);
        // 创建跟服务器连接的会话
        Session session = getSession(appParams);
        // 执行任务
        doTask(appParams,session);
        // 断开连接
        session.disconnect();

        System.out.println("执行完成");
    }

    /**
     * 完成上传或者执行脚本的任务
     * @param appParams       参数
     * @param session         连接会话
     * @throws IOException    io异常
     * @throws JSchException  ssh客户端异常
     */
    private static void doTask(AppParams appParams,Session session) throws IOException, JSchException {
        //任务类别
        int tv = appParams.getTaskType();
        if(tv<AppParams.TaskType.ALL_TYPE.getTypeValue()||tv>AppParams.TaskType.ONLY_CMD.getTypeValue()){
            tv = AppParams.TaskType.ALL_TYPE.getTypeValue();
        }
        if(tv==AppParams.TaskType.ALL_TYPE.getTypeValue()){
            uploadFile(appParams,session);
            if(StrUtil.isNotBlank(appParams.getRemoteShell())){
                exeShell(appParams,session);
            }
        }else if(tv==AppParams.TaskType.ONLY_UPLOAD.getTypeValue()){
            uploadFile(appParams,session);
        }else{
            if(StrUtil.isNotBlank(appParams.getRemoteShell())){
                exeShell(appParams,session);
            }
        }
    }

    /**
     * 获取跟服务器连接的会话
     * @param appParams 本地跟服务器的相关参数
     * @return 连接会话
     */
    private static Session getSession(AppParams appParams) {
        Session session;
        if(StrUtil.isBlank(appParams.getPrivateKey())){
            // 如果没设置私钥,使用账号密码登录
            session = JschUtil.openSession(appParams.getHost(), appParams.getPort(), appParams.getUserName(), appParams.getPassWord());
        }else{
            // 如果设置了私钥,加载私钥(有可能私钥跟密码都设置了)
            byte[] passphrase = null;
            if(StrUtil.isNotBlank(appParams.getPassWord())){
                passphrase = appParams.getPassWord().getBytes(StandardCharsets.UTF_8);
            }
            session = JschUtil.openSession(appParams.getHost(), appParams.getPort(),appParams.getUserName(),appParams.getPrivateKey(),passphrase);
        }
        return session;
    }

    /**
     * 上传文件
     * @param  appParams       参数
     * @param  session         连接会话
     * @throws ZipException    打包异常
     */
    private static void uploadFile(AppParams appParams,Session session) throws ZipException {
        uploadFile(appParams,session,false);
    }
    /**
     * 上传文件
     * @param  appParams       参数
     * @param  session         连接会话
     * @param  sftpCloseFlag   上传文件后是否关闭会话 true关闭会话 false不关闭会话
     * @throws ZipException    打包异常
     */
    private static void uploadFile(AppParams appParams,Session session,boolean sftpCloseFlag) throws ZipException {
        // 待上传到文件或者文件夹(无论是文件还是文件夹都会打包成zip之后再上传)
        File file = new File(appParams.getLocalPath());
        // 打包的zip文件临时路径
        String tempPath = System.getProperty("java.io.tmpdir")+ File.separator;
        // zip文件保存全名称
        String zipTempPath = tempPath + "rsfiles.zip";
        File tempZipFile = new File(zipTempPath);

        //通过sftp上传到服务器,建立连接
        Sftp sftp = JschUtil.createSftp(session);
        // 将文件(或者文件夹)打包成zip
        if(file.isDirectory()){
            //排除文件目录,排除".svn","target",".idea"等等无关内容
            List<String> excludeDir = Arrays.asList(".svn","target",".idea","logs","node_modules",".git");
            ZipParameters zipParameters = new ZipParameters();
            //仅仅打包指定目录下面的文件,不包含根文件夹(非常重要!!!)
            zipParameters.setIncludeRootFolder(false);
            zipParameters.setExcludeFileFilter(f->{
                if(f.isDirectory()){
                    if(CollUtil.isNotEmpty(excludeDir)){
                        return excludeDir.contains(f.getName());
                    }else{
                        return false;
                    }
                }else{
                    //可以拓展排除的文件
                    return false;
                }
            });
            //打包
            new ZipFile(tempZipFile).addFolder(new File(appParams.getLocalPath()),zipParameters);
        }else{
            new ZipFile(tempZipFile).addFile(file);
        }
        try{
            // 上传到目标服务器
            sftp.put(tempZipFile.getAbsolutePath(),appParams.getRemotePath());
            if(sftpCloseFlag){
                sftp.close();
            }
        }finally {
            //删除zip文件
            if(tempZipFile.exists()) {
                boolean delete = tempZipFile.delete();
                if(delete){
                    log.debug("临时zip文件已经删除");
                }
            }
        }
    }

    /**
     * 执行shell脚本
     * @param appParams       参数
     * @param session         会话
     * @throws IOException    io异常
     * @throws JSchException  ssh客户端异常
     */
    private static void exeShell(AppParams appParams,Session session) throws IOException, JSchException {
        //建立可执行管道
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        // 远端脚本的全路径
        String remoteShellScript = appParams.getRemoteShell();
        // 执行脚本命令"sh shell"
        channelExec.setCommand("sh " + remoteShellScript);
        // 获取执行脚本可能出现的错误日志
        channelExec.setErrStream(System.err);
        //脚本执行结果输出，对于程序来说是输入流
        InputStream in = channelExec.getInputStream();
        // 60 秒执行管道超时
        channelExec.connect(60*1000);
        // 从远程主机读取输入流，获得脚本执行结果
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                //执行结果打印到程序控制台
                System.out.print(new String(tmp, 0, i));
            }
            if (channelExec.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                //获取退出状态，状态0表示脚本被正确执行
                System.out.println("exit-status: "
                        + channelExec.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        channelExec.disconnect();
    }
}

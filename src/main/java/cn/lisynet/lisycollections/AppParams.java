package cn.lisynet.lisycollections;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 参数
 * 使用无参构造时,优先从环境变量获取参数
 * 也可以使用全参构造,把所有参数传进来
 * @author lisy
 * @date 2022/3/12 13:08
 */
@Data
public class AppParams {
    /**
     * 本地路径
     */
    private String localPath;
    /**
     * 服务器ip
     */
    private String host;
    /**
     * 服务器ssh端口号
     */
    private Integer port;
    /**
     * 服务器用户名
     */
    private String userName;
    /**
     * 服务器密码
     */
    private String passWord;
    /**
     * 证书私钥
     */
    private String privateKey;
    /**
     * 远端路径
     */
    private String remotePath;
    /**
     * 执行的远端脚本
     */
    private String remoteShell;
    /**
     * 任务类型(只能是0,1,2中的某个值,0上传文件并执行脚本,1仅上传文件,2仅执行脚本.默认0,非0-2的数按照0处理)
     */
    private Integer taskType;

    /**
     * 创建类时,从环境变量中获取参数
     */
    public AppParams(){
        this.localPath = System.getenv("localPath");
        this.host = System.getenv("host");
        if(StrUtil.isNotBlank(System.getenv("port"))){
            try {
                this.port = Integer.parseInt(System.getenv("port"));
            }catch (Exception e){
                this.port = null;
            }
        }
        this.userName = System.getenv("userName");
        this.passWord = System.getenv("passWord");
        this.privateKey = System.getenv("privateKey");
        this.remotePath = System.getenv("remotePath");
        this.remoteShell = System.getenv("remoteShell");
        if(StrUtil.isNotBlank(System.getenv("taskType"))){
            try {
                int tv = Integer.parseInt(System.getenv("taskType"));
                if(tv< TaskType.ALL_TYPE.typeValue||tv>TaskType.ONLY_CMD.typeValue){
                    tv = TaskType.ALL_TYPE.typeValue;
                }
                this.taskType = tv;
            }catch (Exception e){
                this.taskType = TaskType.ALL_TYPE.typeValue;
            }
        }
    }

    public AppParams(String localPath, String host, Integer port, String userName, String passWord, String privateKey, String remotePath, String remoteShell, TaskType taskType) {
        this.localPath = localPath;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.passWord = passWord;
        this.privateKey = privateKey;
        this.remotePath = remotePath;
        this.remoteShell = remoteShell;
        this.taskType = taskType.getTypeValue();
    }

    /**
     * 任务类型
     */
    public enum TaskType{
        /**
         * 所有任务类型
         */
        ALL_TYPE(0),
        /**
         * 仅仅执行上传
         */
        ONLY_UPLOAD(1),
        /**
         * 仅完成执行脚本
         */
        ONLY_CMD(2);

        private final int typeValue;
        TaskType(int typeValue){
            this.typeValue = typeValue;
        }

        public int getTypeValue() {
            return typeValue;
        }
    }
}

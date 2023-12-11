package com.tokensTool.pandoraNext.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.DockerClientBuilder;
import com.tokensTool.pandoraNext.anno.Log;
import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.systemSetting;
import com.tokensTool.pandoraNext.pojo.token;
import com.tokensTool.pandoraNext.service.apiService;
import com.tokensTool.pandoraNext.service.systemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yangyang
 * @create 2023-11-07 14:55
 */
@Slf4j
@RestController()
@RequestMapping("/api")
public class apiController {
    private systemService systemService;

    @Autowired
    public void setSystemService(systemService systemService) {
        this.systemService = systemService;
    }

    @Value("${deployPosition}")
    private String deployPosition;

    public String deploy = "default";

    private apiService apiService;

    /**
     * 部署方法
     */
    @Value("${deployWay}")
    private String deployWay;

    /**
     * 重载接口
     */
    private final static String reloadUrl = "/api/setup/reload";

    @Autowired
    public void setSystemService(apiService apiService) {
        this.apiService = apiService;
    }
    /**
     * 主机先前IP
     */
    private static String previousIPAddress = "";
    /**
     * @param name
     * @return 通过name获取到（tokens.json）文件里的全部值
     */

    @GetMapping("selectToken")
    public Result selectToken(@RequestParam("name") String name){
        try {
            List<token> res = apiService.selectToken(name);
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败");
        }
    }

    /**
     * @param token
     * @return 添加token和其余变量到指定文件（tokens.json）
     */
    @Log
    @PostMapping("addToken")
    public Result addToken(@RequestBody token token){
        try {
            String res = apiService.addToken(token);
            if(res.length() > 300){
                return Result.success(res);
            }
            else if(res.length() == 0){
                return Result.success("添加成功，已装填你的token");
            }
            else{
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败");
        }
    }


    /**
     * @param token
     * @return 通过传入token，修改（tokens.json）文件里的值
     */
    @Log
    @PostMapping("requiredToken")
    public Result requiredToken(@RequestBody token token){
        try {
            String res = apiService.requiredToken(token);
            if(res.equals("修改成功！")){
                return Result.success(res);
            }
            else{
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改token失败");
        }
    }

    /**
     * @param name
     * @return 通过token用户名，删除（tokens.json）文件里的值
     */
    @Log
    @PutMapping("deleteToken")
    public Result deleteToken(@RequestParam String name){
        try {
            String res = apiService.deleteToken(name);
            log.info(res);
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * @return 通过访问restart，重启PandoraNext服务
     */
    @GetMapping("/restart")
    public Result restartContainer() {
        try {
            restartContainer(systemService.selectSetting().getContainerName());
            return Result.success("重启PandoraNext镜像成功");
        } catch (Exception e) {
            log.error("重启PandoraNext镜像失败！", e);
            return Result.error("重启PandoraNext镜像失败！");
        }
    }
    /**
     * @return 通过访问close，关闭PandoraNext服务
     */
    @GetMapping("/close")
    public Result closeContainer() {
        String containerName = systemService.selectSetting().getContainerName();
        if (deployWay.contains("docker")) {
            try {
                docker(containerName,"stop");
                return Result.success("暂停PandoraNext镜像成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Result.error("暂停PandoraNext镜像失败");
        }
        else if (deployWay.equals("releases")) {
            try {
                closeRelease(containerName);
                return Result.success("暂停PandoraNext应用成功");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return Result.error("jar用错名称");
        }
    }

    private static boolean isContainerStopped(DockerClient dockerClient, String containerIdOrName) {
        try {
            // 获取容器状态
            String containerStatus = dockerClient.inspectContainerCmd(containerIdOrName).exec().getState().getStatus();
            // 判断容器状态是否为 "exited"
            return containerStatus.equals("exited");
        } catch (DockerException e) {
            e.printStackTrace();
            // 处理异常情况，例如容器不存在等
            return false;
        }
    }


    /**
     * @return 通过访问open，开启PandoraNext服务
     */
    @GetMapping("/open")
    public Result openContainer(){
        // 要检查的容器ID或名称
        String containerName = systemService.selectSetting().getContainerName();
        if (deployWay.contains("docker")) {
            try {
                // Docker 客户端初始化
                DockerClient dockerClient = DockerClientBuilder.getInstance().build();
                // 检查容器状态
                boolean isPaused = isContainerStopped(dockerClient, containerName);
                if (isPaused) {
                    log.info("容器 " + containerName + " 已停止。");
                    docker(containerName,"start");
                    return Result.success("开启PandoraNext镜像成功");
                }
                // 关闭 Docker 客户端连接
                dockerClient.close();
                return Result.success("容器 " + containerName + " 未停止,不能重复启动");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if (deployWay.equals("releases")) {
            try {
                openRelease(containerName);
                return Result.success("开启PandoraNext镜像成功");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return Result.success("jar用错名称");
        }
    }


    /**
     * @return 通过访问open，重载PandoraNext服务
     */
    @GetMapping("/reload")
    public Result reloadContainer(){
        try {
            String externalIP = previousIPAddress;
            systemSetting systemSetting = systemService.selectSetting();
            String bingUrl = systemSetting.getBing();
            String[] parts = bingUrl.split(":");
            String baseUrlWithoutPath = "http://" + externalIP + ":" + parts[1] + "/" +systemSetting.getProxy_api_prefix();
            if (parts.length != 2) {
                return Result.error("bind填写有误，无法提取port");
            }
            log.info("重载的PandoraNext服务Url:"+baseUrlWithoutPath);

            String reloadCommand = "curl -i -X POST " + baseUrlWithoutPath + reloadUrl;
            // 执行重载进程的命令
            Process reloadProcess = executeCommand(reloadCommand);
            log.info("重载命令:"+reloadCommand);
            // 等待重载进程完成
            try {
                int exitCode = reloadProcess.waitFor();
                if (exitCode != 0) {
                    log.info("无法重载PandoraNext服务");
                    return Result.success("无法重载PandoraNext服务");
                }
                return Result.success("重置PandoraNext服务成功！");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success("无法重载PandoraNext服务");
    }
    /**
     * containerName
     * 重启containerName的容器
     * 分为docker和releases
     */
    public void restartContainer(String containerName){
        log.info(deployWay);
        if (deployWay.contains("docker")) {
          docker(containerName,"restart");
        }
        else if (deployWay.equals("releases")) {
            try {
                try {
                    //先确保是开启状态
                    openRelease(containerName);
                    //再关闭
                    Thread.sleep(500);
                    closeRelease(containerName);
                    //在重启
                    Thread.sleep(500);
                    openRelease(containerName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                log.info("无法重启PandoraNext服务");
                throw new RuntimeException(e);
            }
        }
        else {
            log.info("jar包填错信息");
        }
    }

    /**
     * releases命令
     * containeeName：容器名
     * 关闭容器项目
     */
    public void closeRelease(String containName){
        try {
            String killCommand = "pkill " + containName;
            log.info(killCommand);
            int exitCode = 0;
            try {
                // 执行杀死进程的命令
                Process killProcess = executeCommand(killCommand);
                // 等待杀死进程完成
                try {
                    exitCode = killProcess.waitFor();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (exitCode != 0) {
                log.info("无法关闭PandoraNext服务");
                throw new RuntimeException("无法关闭PandoraNext服务");
            }
            log.info("关闭PandoraNext服务成功！");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * releaser命令
     * containeeName：容器名
     * 开启容器项目
     */
    public void openRelease(String containerName){
        try {
            String projectRoot;
            if(deploy.equals(deployPosition)){
                projectRoot = System.getProperty("user.dir");
                log.info(projectRoot);
            }
            else{
                projectRoot = deployPosition;
            }
            String startCommand = "cd " + projectRoot + " && nohup ./" + containerName + " > output.log 2>&1 & echo $! > pid.txt";
            log.info(startCommand);
            Process startProcess = executeCommand(startCommand);
            int exitCode = startProcess.waitFor();
            if (exitCode != 0) {
                log.info("无法启动PandoraNext服务");
                throw new RuntimeException("无法启动PandoraNext服务");
            }
            log.info("启动PandoraNext服务成功！");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * docker命令
     * containeeName：容器名
     * way:命令方法（启动：start 暂停：stop 重启：restart）
     */
    public void docker(String containerName,String way){
        try {
            String dockerCommand = "docker "+ way + " " + containerName;
            Process process = executeCommand(dockerCommand);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.info("无法"+way+"PandoraNext服务");
                throw new RuntimeException("无法"+way+"PandoraNext服务");
            }
            log.info(way+"PandoraNext服务");
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * release 命令函数
     * command：命令
     * 用 bash ,-c ，来包裹命令增加其稳定性
     */
    public Process executeCommand(String command){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            return processBuilder.start();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Value("${pandoara_Ip}")
    private String pandoara_Ip;

    /**
     * pandoara_Ip要是填写的是default
     * 每隔60分钟刷新一次ip,若地址发生变化并重新验证
     * 如不是则放回："Ip将采用用户设置："+pandoara_Ip
     */
    @Scheduled(fixedRate = 3600000)
    public void autoCheckIp(){
        if(! pandoara_Ip.equals("default")){
            if(previousIPAddress != pandoara_Ip){
                previousIPAddress = pandoara_Ip;
            }
            log.info("Ip将采用用户设置："+pandoara_Ip);
            return;
        }
        String currentIPAddress = apiService.getIp();
        if(currentIPAddress == "失败"){
            log.info("获取IP失败！");
            return;
        }
        if (!currentIPAddress.equals(previousIPAddress)) {
            log.info("IP地址已变化，新的IP地址是：" + currentIPAddress);
            previousIPAddress = currentIPAddress;
        } else {
            log.info("IP地址未发生变化。");
        }
    }
}

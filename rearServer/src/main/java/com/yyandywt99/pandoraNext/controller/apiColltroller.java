package com.yyandywt99.pandoraNext.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.pojo.token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yangyang
 * @create 2023-11-07 14:55
 */
@Slf4j
@RestController()
@RequestMapping("/api")
public class apiColltroller {
    @Autowired
    private com.yyandywt99.pandoraNext.service.apiService apiService;

    @Value("${deployPosition}")
    private String deployPosition;

    public String deploy = "default";

    /**
     * @param name
     * @return 通过name获取到（tokens.json）文件里的全部值
     */
    @GetMapping("seleteToken")
    public Result seleteToken(@RequestParam("name") String name){
        try {
            List<token> res = apiService.seleteToken(name);
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
    @Value("${deployWay}")
    private String deployWay;
    /**
     * @return 通过访问restart，重启PandoraNext服务
     */
    @GetMapping("/restart")
    public Result restartContainer() {
        try {
            restartContainer("PandoraNext");
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
        String containerName = "PandoraNext";
        if (deployWay.contains("docker")) {
            docker(containerName,"pause");
            return Result.success("暂停PandoraNext镜像成功");
        }
        else if (deployWay.equals("releases")) {
            try {
                closeRelease(containerName);
                return Result.success("暂停PandoraNext镜像成功");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return Result.success("jar用错名称");
        }
    }

    private static boolean isContainerPaused(DockerClient dockerClient, String containerIdOrName) {
        // 使用 Docker Java API 查询容器信息
        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerIdOrName).exec();

        // 获取容器状态
        return containerInfo.getState().getPaused();
    }
    /**
     * @return 通过访问open，开启PandoraNext服务
     */
    @GetMapping("/open")
    public Result openContainer(){
        // 要检查的容器ID或名称
        String containerName = "PandoraNext";
        if (deployWay.contains("docker")) {
            try {
                // Docker 客户端初始化
                DockerClient dockerClient = DockerClientBuilder.getInstance().build();
                // 检查容器状态
                boolean isPaused = isContainerPaused(dockerClient, containerName);
                if (isPaused) {
                    log.info("容器 " + containerName + " 已暂停。");
                    docker(containerName,"unpause");
                    return Result.success("开启PandoraNext镜像成功");
                }
                // 关闭 Docker 客户端连接
                dockerClient.close();
                return Result.success("容器 " + containerName + " 未暂停,不能重复启动");
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
     * way:命令方法（启动：start 暂停：pause 重启：restart）
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
}

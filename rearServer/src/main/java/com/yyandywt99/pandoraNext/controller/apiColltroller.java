package com.yyandywt99.pandoraNext.controller;

import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.pojo.token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    /**
     * @return 通过访问restart，重启PandoraNext服务
     */
    @GetMapping("/restart")
    public Result restartContainer() {
        try {
            restartDockerContainer("PandoraNext");
            return Result.success("重启PandoraNext服务成功");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Result.error("重启PandoraNext服务失败！");
        }
    }

    @Value("${deployWay}")
    private String deployWay;
    private void restartDockerContainer(String containerName) throws IOException, InterruptedException {
        Process process = null;
        if(deployWay.contains("docker")){
            String[] command = {"docker", "restart", containerName};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            process = processBuilder.start();
        }
        else if(deployWay.contains("Releases")){
            //重启命令为nohup ./PandoraNext &
            String[] command = {"nohup", "./"+containerName, "&"};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            process = processBuilder.start();
        }
        else{
            throw new RuntimeException("未正常启动jar包，没有正确配置好启动方法");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.info("无法重启PandoraNext服务");
            throw new RuntimeException("无法重启PandoraNext服务");
        }
        log.info("重启PandoraNext服务成功！");
    }
}

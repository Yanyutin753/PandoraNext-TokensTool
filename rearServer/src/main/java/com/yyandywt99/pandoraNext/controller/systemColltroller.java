package com.yyandywt99.pandoraNext.controller;

import com.yyandywt99.pandoraNext.anno.Log;
import com.yyandywt99.pandoraNext.pojo.Result;
import com.yyandywt99.pandoraNext.pojo.systemSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yangyang
 * @create 2023-11-18 9:55
 */
@RestController
@RequestMapping("/api")
public class systemColltroller {
    @Autowired
    private com.yyandywt99.pandoraNext.service.systemService systemService;

    /**
     * 修改config.json里的系统值
     * @return "修改成功！"or"修改失败"
     */
    @Log
    @PostMapping("requireSetting")
    public Result requireSetting(@RequestBody systemSetting setting){
        try {
            String res = systemService.requiredSetting(setting);
            if(res.contains("成功")){
                return Result.success(res);
            }
            else {
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改失败！");
        }
    }

    /**
     * 选择config.json里的系统值
     * @return "修改成功！"or"修改失败"
     */
    @GetMapping("selectSetting")
    public Result selectSetting() {
        try {
            systemSetting res = systemService.selectSetting();
            if (res != null) {
                return Result.success(res);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.error("获取失败！");
    }
}

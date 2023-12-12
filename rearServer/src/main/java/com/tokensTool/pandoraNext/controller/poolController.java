package com.tokensTool.pandoraNext.controller;

import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.poolToken;
import com.tokensTool.pandoraNext.service.poolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yangyang
 * @create 2023-12-11 10:49
 */
@RestController
@Component
@RequestMapping("/api")
public class poolController {
    @Autowired
    private poolService poolService;

    @PostMapping("addPoolToken")
    public Result addPoolToken(@RequestBody poolToken poolToken){
        try {
            String res = poolService.addPoolToken(poolToken);
            if(res.contains("成功")){
                return Result.success(res);
            }
            else{
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("添加失败！");
    }

    @PostMapping("deletePoolToken")
    public Result deletePoolToken(@RequestBody poolToken poolToken){
        try {
            String res = poolService.deletePoolToken(poolToken);
            if(res.contains("成功")){
                return Result.success(res);
            }
            else{
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("删除失败！");
    }

    @PostMapping("refreshSimplyPoolToken")
    public Result refreshSimplyPoolToken(@RequestBody poolToken poolToken){
        try {
            String res = poolService.refreshSimplyToken(poolToken);
            if(res.contains("成功")){
                return Result.success(res);
            }
            else{
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新失败！");
    }

    @PostMapping("changePoolToken")
    public Result changePoolToken(@RequestBody poolToken poolToken){
        try {
            String res = poolService.changePoolToken(poolToken);
            if(res.contains("成功")){
                return Result.success(res);
            }
            else{
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("修改失败！");
    }

    @GetMapping("selectPoolToken")
    public Result selectPoolToken(@RequestParam("name") String name){
        try {
            List<poolToken> res = poolService.selectPoolToken(name);
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败");
        }
    }
}

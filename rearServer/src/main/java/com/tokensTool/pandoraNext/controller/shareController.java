package com.tokensTool.pandoraNext.controller;

import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.shareToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yangyang
 * @create 2024-01-08 10:21
 */

@Slf4j
@RestController
@Component
@RequestMapping("/api")
public class shareController {
    @Autowired
    private com.tokensTool.pandoraNext.service.shareService shareService;

    @PostMapping("addShareToken")
    public Result addPoolToken(@RequestBody shareToken shareToken) {
        try {
            String res = shareService.addShareToken(shareToken);
            if (res.contains("成功")) {
                return Result.success(res);
            } else {
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("添加失败！");
    }

    @PostMapping("requireShareToken")
    public Result requireShareToken(@RequestBody shareToken shareToken) {
        try {
            String res = shareService.requireShareToken(shareToken);
            if (res.contains("成功")) {
                return Result.success(res);
            } else {
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("修改失败！");
    }

    @PutMapping("deleteShareToken")
    public Result deleteShareToken(@RequestBody shareToken shareToken) {
        try {
            String res = shareService.deleteShareToken(shareToken);
            if (res.contains("成功")) {
                return Result.success(res);
            } else {
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("删除失败！");
    }

    @GetMapping("selectShareToken")
    public Result selectShareToken(@RequestParam("name") String name) {
        try {
            List<shareToken> res = shareService.selectShareToken(name);
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败");
        }
    }
}

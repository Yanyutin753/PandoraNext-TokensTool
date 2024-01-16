package com.tokensTool.pandoraNext.controller;

import com.tokensTool.pandoraNext.pojo.Result;
import com.tokensTool.pandoraNext.pojo.poolToken;
import com.tokensTool.pandoraNext.service.poolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yangyang
 * @create 2023-12-11 10:49
 */
@Slf4j
@RestController
@Component
@RequestMapping("/api")
public class poolController {
    @Autowired
    private poolService poolService;

    @PostMapping("addPoolToken")
    public Result addPoolToken(@RequestBody poolToken poolToken) {
        try {
            String res = poolService.addPoolToken(poolToken);
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

    @PostMapping("requirePoolToken")
    public Result requirePoolToken(@RequestBody poolToken poolToken) {
        try {
            String res = poolService.toRequirePoolToken(poolToken);
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

    @PostMapping("deletePoolToken")
    public Result deletePoolToken(@RequestBody poolToken poolToken) {
        try {
            String res = poolService.deletePoolToken(poolToken);
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

    @PostMapping("deletePoolTokenList")
    public Result deletePoolTokenList(@RequestBody List<poolToken> poolTokens) {
        try {
            int count = 0;
            for (poolToken poolToken : poolTokens) {
                String res = poolService.deletePoolToken(poolToken);
                if (res.contains("成功")) {
                    count++;
                }
            }
            return Result.success("删除成功：" + count + "，失败：" + (poolTokens.size() - count));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("删除失败！");
    }

    @PostMapping("refreshSimplyPoolToken")
    public Result refreshSimplyPoolToken(@RequestBody poolToken poolToken) {
        try {
            String res = poolService.refreshSimplyToken(poolToken);
            if (res.contains("成功")) {
                return Result.success(res);
            } else {
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新失败！");
    }

    @PostMapping("refreshSimplyPoolTokenList")
    public Result refreshSimplyPoolTokenList(@RequestBody List<poolToken> poolTokens) {
        try {
            int count = 0;
            for (poolToken poolToken : poolTokens) {
                String res = poolService.refreshSimplyToken(poolToken);
                if (res.contains("成功")) {
                    count++;
                }
            }
            return Result.success("刷新成功：" + count + "，失败：" + (poolTokens.size() - count));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("刷新失败！");
    }

    @PostMapping("changePoolToken")
    public Result changePoolToken(@RequestBody poolToken poolToken) {
        try {
            String res = poolService.changePoolToken(poolToken);
            if (res.contains("成功")) {
                return Result.success(res);
            } else {
                return Result.error(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("更换失败！");
    }

    @PostMapping("changePoolTokenList")
    public Result changePoolTokenList(@RequestBody List<poolToken> poolTokens) {
        try {
            int count = 0;
            for (poolToken poolToken : poolTokens) {
                String res = poolService.changePoolToken(poolToken);
                if (res.contains("成功")) {
                    count++;
                }
            }
            return Result.success("更换成功：" + count + "，失败：" + (poolTokens.size() - count));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("更换失败！");
    }

    @GetMapping("selectPoolToken")
    public Result selectPoolToken(@RequestParam("name") String name) {
        try {
            List<poolToken> res = poolService.selectPoolToken(name);
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败");
        }
    }

    @GetMapping("updateAllPoolToken")
    public Result updateAllPoolToken() {
        try {
            String res = poolService.refreshAllPoolTokens();
            return Result.success(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("刷新失败");
        }
    }

    @PostMapping("verifySimplyPoolToken")
    public Result verifySimplyPoolToken(@RequestBody poolToken poolToken) {
        String res = poolService.verifySimplyPoolToken(poolToken);
        if (res != null) {
            return Result.success(res);
        } else {
            return Result.error("poolToken失效或请求网址失效，请刷新或检查请求网址!");
        }
    }

    @PostMapping("verifySimplyPoolTokenList")
    public Result verifySimplyPoolTokenList(@RequestBody List<poolToken> poolTokens) {
        try {
            int count = 0;
            for (poolToken poolToken : poolTokens) {
                String res = poolService.verifySimplyPoolToken(poolToken);
                if (res.contains("正常")) {
                    count++;
                }
            }
            return Result.success("检验请求成功：" + count + "，失败：" + (poolTokens.size() - count));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("poolToken失效或请求网址失效，请刷新或检查请求网址!");
    }

    @GetMapping("verifyAllPoolToken")
    public Result verifyAllPoolToken() {
        try {
            String res = poolService.verifyAllPoolToken();
            if (res != null) {
                return Result.success(res);
            } else {
                return Result.error("请确保PandoraNext正常启动且tokensTool填写PandoraNext访问地址正确！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("请确保PandoraNext正常启动且tokensTool填写PandoraNext访问地址正确！");
    }
}

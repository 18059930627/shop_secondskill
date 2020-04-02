package com.qf.controller;

import com.qf.entity.Goods;
import com.qf.entity.GoodsSecondKill;
import com.qf.entity.ResultData;
import com.qf.feign.GoodsFeign;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.apache.commons.io.IOUtils;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * @Author chenzhongjun
 * @Date 2019/12/27
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsFeign goodsFeign;

    /**
     * 添加商品
     *
     * @param goods
     * @return
     */
    @RequestMapping("/addGoods")
    public String insertGoods(Goods goods, GoodsSecondKill goodsSecondKill) {
        goods.setGoodsSecondKill(goodsSecondKill);
        int result = goodsFeign.insertGoods(goods);
        return "redirect:http://localhost/back/goods/list";
    }

    /**
     * 获得商品信息
     *
     * @return
     */
    @RequestMapping("/list")
    public String getGoodsList(ModelMap map) {
        //使用feign调用goods服务
        List<Goods> goodsList = goodsFeign.getGoodsList();
        map.put("goodsList", goodsList);
        return "goods_list";
    }

    /**
     * 上传图片
     *
     * @param file
     * @return
     */
    @RequestMapping("uploader")
    @ResponseBody
    public ResultData<String> uploader(MultipartFile file) {
        //存到本地
        String fileName = UUID.randomUUID().toString();
        String path = "E:/images";
        String targetPath = path + "/" + fileName;
        try (
                InputStream inputStream = file.getInputStream();
                OutputStream os = new FileOutputStream(targetPath);

        ) {
            //拷贝
            IOUtils.copy(inputStream, os);
            return new ResultData<String>().setCode(ResultData.ResultCodeList.OK).setData(targetPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResultData<String>().setCode(ResultData.ResultCodeList.ERROR);
    }

    @RequestMapping("showImage")
    public void showImage(String imagePath, HttpServletResponse response) {
        //读本地磁盘的图片
        try (
                FileInputStream fis = new FileInputStream(new File(imagePath));
                OutputStream fos = response.getOutputStream();

        ) {
            IOUtils.copy(fis, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

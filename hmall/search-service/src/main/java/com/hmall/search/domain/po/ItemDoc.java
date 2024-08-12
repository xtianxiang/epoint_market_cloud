package com.hmall.search.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author xu
 * @version 1.0
 * @date 2024/7/15 16:54
 * @DESCRIPTION
 */
@Data
@ApiModel(description = "索引库实体")
@NoArgsConstructor
public class ItemDoc {

    /**
     * 商品id
     */

    private Long id;

    /**
     * SKU名称
     */
    private String name;

    /**
     * 价格（分）
     */
    private Integer price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 类目名称
     */
    private String category;

    /**
     * 品牌名称
     */
    private String brand;

    /**
     * 规格
     */
    private String spec;

    /**
     * 销量
     */
    private Integer sold;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 是否是推广广告，true/false
     */
    @TableField("isAD")
    private Boolean isAD;

    /**
     * 商品状态 1-正常，2-下架，3-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long creater;

    /**
     * 修改人
     */
    private Long updater;
    
    private List<String> suggestion;
    public ItemDoc(Item item){
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.stock = item.getStock();
        this.image = item.getImage();
        this.category = item.getCategory();
        this.brand = item.getBrand();
        this.spec = item.getSpec();
        this.sold = item.getSold();
        this.commentCount = item.getCommentCount();
        this.isAD = item.getIsAD();
        this.status = item.getStatus();
        this.createTime = item.getCreateTime();
        this.updateTime = item.getUpdateTime();
        this.creater = item.getCreater();
        this.updater = item.getUpdater();

        // 自动补全字段的处理
        this.suggestion = new ArrayList<>();
        // 添加商品描述
        this.suggestion.add(this.name);
        // 添加品牌
        this.suggestion.add(this.brand);
        // 添加分类
        this.suggestion.add(this.category);
    }
}

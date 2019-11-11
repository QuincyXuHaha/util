package com.quincy.project.util.excel;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * excel 数据内容
 *
 * @author xuguangquan
 * @date 2019/10/31 星期四
 */
@Data
@Accessors(chain = true)
public class TtlProductInfoPo {

    private Long id;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long branchId;
    private String branchName;
    private Long shopId;
    private String shopName;
    private Double price;
    private Integer stock;
    private Integer salesNum;
    private String createTime;
    private String updateTime;
    private Byte isDel;

}

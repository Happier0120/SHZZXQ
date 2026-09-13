package com.example.propertysupervision.protocol.schema;

import java.util.*;

public final class FieldRegistry {
    /**
     * 普通业务域定义。
     *
     * Key:
     * Field编号
     *
     * Value:
     *  Field物理编码规则
     */
    private static final Map<Integer, FieldSpec> FIELDS = new HashMap<>();

    /**
     * 协议文档中存在Field编号，
     * 但没有明确给出“变长位数/最大长度”的字段
     */
    private static final Set<Integer> UNDEFINED_SPEC_FIELDS = Collections.unmodifiableSet(
            new HashSet<Integer>(Arrays.asList(11, 31, 70, 127))
    );

    static {
        // Field1 ~ 10
        register(1, 1, 5);          // 报文类型
        register(2, 2, 24);         // 报文编号
        register(3, 1, 6);          // 子报文数
        register(4, 1, 2);          // 报文发起方编号
        register(5, 2, 14);         // 报文产生时间
        register(6, 1, 8);          // 报文截止交易日期
        register(7, 1, 3);          // 动作代码
        register(8, 2, 24);         // 原始报文编号
        register(9, 1, 2);          // 报文处理代码
        register(10, 3, 256);       // 报文处理信息

        // Field 11：备用字段，协议未给长度规则

        // Field 12 ~ 20
        register(12, 2, 12);        // 工程ID
        register(13, 2, 20);        // 施工ID
        register(14, 1, 1);         // 交易方式
        register(15, 1, 1);         // 业务种类
        register(16, 1, 2);         // 增值业务种类
        register(17, 2, 40);        // 增值凭证号码
        register(18, 1, 8);         // 增值起息日
        register(19, 1, 8);         // 增值到期日
        register(20, 1, 2);         // 存期

        // Field 21 ~ 30
        register(21, 1, 2);         // 时间类型
        register(22, 2, 32);        // 监管账户会计账号的行号
        register(23, 2, 32);        // 收款账户会计账号的行号
        register(24, 1, 8);         // 预结息日期
        register(25, 1, 8);         // 预结息利率
        register(26, 2, 16);        // 预结利息金额
        register(27, 1, 8);         // 开始划转日期
        register(28, 1, 8);         // 最迟划转日期
        register(29, 1, 8);         // 有效期
        register(30, 1, 12);        // 混合小区剩余待划分金额

        // Field 31：备用字段，协议未给长度规则

        // Field 32 ~ 40
        register(32, 2, 12);        // 维修资金账号1
        register(33, 1, 2);         // 维修资金账号1类型
        register(34, 2, 12);        // 维修资金账号2
        register(35, 1, 2);         // 维修资金账号2类型
        register(36, 2, 15);        // 增值账号
        register(37, 1, 2);         // 增值账号类型
        register(38, 2, 12);        // 归属门牌幢资金账号
        register(39, 2, 12);        // 归属项目资金账号
        register(40, 2, 12);        // 归属业主大会资金账号

        // Field 41 ~ 50
        register(41, 2, 12);        // 归属区县资金账号
        register(42, 2, 12);        // 归属区县调整户账号
        register(43, 2, 15);        // 归属门牌增值账号
        register(44, 2, 15);        // 归属业主大会增值账号
        register(45, 2, 12);        // 归属业主大会会计跟踪账号
        register(46, 2, 12);        // 归属区县会计跟踪账号
        register(47, 2, 40);        // 维修资金账号1的行号
        register(48, 2, 80);        // 审价单位名称
        register(49, 3, 160);       // 施工单位
        register(50, 1, 2);         // 区县编号

        // Field 51 ~ 60
        register(51, 2, 10);        // 业主大会编号
        register(52, 2, 80);        // 客户名称
        register(53, 3, 160);       // 客户地址
        register(54, 1, 6);         // 客户邮编
        register(55, 2, 40);        // 客户电话
        register(56, 2, 40);        // 联系人1
        register(57, 1, 2);         // 证件类型1
        register(58, 2, 40);        // 联系人证件号码1
        register(59, 2, 40);        // 联系人2
        register(60, 1, 2);         // 证件类型2

        // Field 61 ~ 69
        register(61, 2, 40);        // 联系人证件号码2
        register(62, 2, 20);        // 组织代码
        register(63, 2, 20);        // 物业代码
        register(64, 2, 30);        // 物业管理合同编号
        register(65, 2, 64);        // 会计账号
        register(66, 2, 80);        // 会计账户名称
        register(67, 2, 80);        // 账户开户银行名称
        register(68, 2, 60);        // 银行分支机构编号
        register(69, 1, 8);         // 户数

        // Field 70：备用字段，协议未给长度规则

        // Field 71 ~ 80
        register(71, 2, 80);        // 项目名称
        register(72, 3, 160);       // 项目地址
        register(73, 1, 6);         // 项目邮编
        register(74, 2, 40);        // 项目电话
        register(75, 2, 40);        // 项目联系人
        register(76, 2, 16);        // 项目应归集额
        register(77, 2, 16);        // 专项维修资金交纳标准（元/平方米）
        register(78, 2, 20);        // 维修对象ID
        register(79, 2, 80);        // 维修对象名称
        register(80, 1, 8);         // 实际竣工日期

        // Field 81 ~ 90
        register(81, 2, 16);        // 足额交纳标准
        register(82, 3, 160);       // 门牌地址
        register(83, 2, 18);        // 产业代码
        register(84, 2, 40);        // 业主姓名
        register(85, 3, 160);       // 产业地址
        register(86, 1, 2);         // 电梯标志
        register(87, 2, 80);        // 交款单位名称
        register(88, 2, 10);        // 建筑面积
        register(89, 2, 16);        // 应交款
        register(90, 2, 80);        // 物业管理区域名称

        // Field 91 ~ 100
        register(91, 2, 40);        // 退款领取人姓名
        register(92, 2, 30);        // 退款领取人账号
        register(93, 3, 160);       // 退款原因
        register(94, 2, 20);        // 证件种类
        register(95, 2, 40);        // 证件号码
        register(96, 2, 10);        // 拆迁许可证
        register(97, 2, 60);        // 注销原因
        register(98, 3, 255);       // 灭失证明相关文件
        register(99, 2, 10);        // 注销房地产登记编号
        register(100, 2, 80);       // 划出银行分支机构名称

        // Field 101 ~ 110
        register(101, 2, 16);       // 施工合同金额
        register(102, 2, 60);       // 划出银行分支机构编号
        register(103, 2, 80);       // 划入开户银行名称
        register(104, 2, 64);       // 划入开户行会计账号
        register(105, 1, 1);        // 是否足额
        register(106, 2, 16);       // 审定价
        register(107, 2, 16);       // 需补足金额
        register(108, 2, 16);       // 交易金额
        register(109, 2, 16);       // 账户余额
        register(110, 2, 16);       // 利息交易金额

        // Field 111 ~ 120
        register(111, 2, 16);       // 当日总收入
        register(112, 2, 16);       // 当日总支出
        register(113, 2, 16);       // 赎回本金金额
        register(114, 2, 16);       // 账户余额2
        register(115, 2, 16);       // 账户余额3
        register(116, 1, 2);        // 决议类型
        register(117, 2, 16);       // 工程决议金额
        register(118, 2, 16);       // 保证金金额
        register(119, 1, 1);        // 借贷标记
        register(120, 3, 160);      // 打印备注信息

        // Field 121 ~ 126
        register(121, 2, 16);       // 工作密钥
        register(122, 2, 14);       // 密钥生成时间
        register(123, 3, 800);      // 不平原因
        register(124, 2, 12);       // 资料ID
        register(125, 2, 50);       // 资料编码
        register(126, 2, 2);        // 资料类型

        // Field 127：MAC校验
        // 协议没有给出变长位数和最大元素长度
        // 暂不注册


        /**
         * Field 128：报文标志
         *
         * 协议表中虽然写的是：
         * 变长位数 = 1
         * 最大长度 = 1
         *
         * 但实际打包规则中，它不是普通字段数据，
         * 而是直接使用 Bitmap 第128位：
         *
         * 0 = 没有后续子报文
         * 1 = 还有后续子报文
         *
         * 因此这里不注册，由 BitmapCodec 单独处理。
         */

    }

    private FieldRegistry() {
        // 工具类不允许实例化
    }

    private static void register(int fieldNo, int lengthDigits, int maxLength) {
        FieldSpec spec = new FieldSpec(fieldNo, lengthDigits, maxLength);

        if (FIELDS.containsKey(fieldNo)) {
            throw new IllegalStateException("Field " + fieldNo + " 重复注册");
        }

        FIELDS.put(fieldNo, spec);
    }

    public static FieldSpec get(int fieldNo) {
        FieldSpec spec = FIELDS.get(fieldNo);

        if (spec != null) {
            return spec;
        }

        if (UNDEFINED_SPEC_FIELDS.contains(fieldNo)) {
            throw new IllegalArgumentException(
                "Field " + fieldNo + " 在V1.3协议中存在，但未定义变长位数/最大长度，暂不能进行通用编解码"
            );
        }

        if (fieldNo == 128) {
            throw new IllegalArgumentException(
                "Field 128 是Bitmap后续报文标志位，不应作为普通Field编码"
            );
        }

        throw new IllegalArgumentException("未知Field编号：" + fieldNo);
    }

    public static boolean contains(int fieldNo) {
        return FIELDS.containsKey(fieldNo);
    }

}

/*
 * 极物圈 - 统一建表脚本
 * 包含：建库、保留表结构、初始化角色/菜单/权限、聊天与用户基础数据
 * 敏感字段（密码、盐、手机、邮箱等）已置空，部署后请自行修改管理员密码
 */
CREATE DATABASE IF NOT EXISTS `jiwu-chat-db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `jiwu-chat-db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `chat_contact`;
CREATE TABLE `chat_contact`
(
    `id`            bigint UNSIGNED                                              NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
    `room_id`       bigint                                                       NOT NULL COMMENT '房间id',
    `read_time`     datetime(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '阅读到的时间',
    `active_time`   datetime(3)                                                  NULL     DEFAULT NULL COMMENT '会话内消息最后更新的时间(只有普通会话需要维护，全员会话不需要维护)',
    `last_msg_id`   bigint                                                       NULL     DEFAULT NULL COMMENT '会话最新消息id',
    `pin_time`      datetime(3)                                                  NULL     DEFAULT NULL COMMENT '置顶时间（置顶非空）',
    `notice_status` tinyint                                                      NOT NULL DEFAULT 0 COMMENT '提醒状态 0默认提醒 1接收消息但不提醒 2收进群助手且不提醒 3屏蔽群消息',
    `shield_status` tinyint                                                      NOT NULL DEFAULT 0 COMMENT '免打扰状态 0否 1是',
    `create_time`   datetime(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`   datetime(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `read_msg_id`   bigint                                                       NULL     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_uid_room_id` (`user_id` ASC, `room_id` ASC) USING BTREE,
    INDEX `idx_room_id_read_time` (`room_id` ASC, `read_time` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE,
    INDEX `idx_pin_time` (`pin_time` DESC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 18636
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话列表'
  ROW_FORMAT = DYNAMIC;

BEGIN;
INSERT INTO `chat_contact` (`id`, `user_id`, `room_id`, `read_time`, `active_time`, `last_msg_id`, `pin_time`,
                            `notice_status`, `shield_status`, `create_time`, `update_time`, `read_msg_id`)
VALUES (1, '100001', 1, '2025-11-23 15:00:00.000', NULL, 1, NULL, 0, 0, '2025-11-23 15:00:00.000',
        '2025-11-23 15:00:00.000', 1);
COMMIT;

-- ----------------------------
-- Table structure for chat_group_member
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_member`;
CREATE TABLE `chat_group_member`
(
    `id`          bigint UNSIGNED                                              NOT NULL AUTO_INCREMENT COMMENT 'id',
    `group_id`    bigint                                                       NOT NULL COMMENT '群组id',
    `userId`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成员uid',
    `role`        int                                                          NOT NULL COMMENT '成员角色 1群主 2管理员 3普通成员',
    `create_time` datetime(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uq_group_user` (`group_id` ASC, `userId` ASC) USING BTREE,
    INDEX `idx_group_id_role` (`group_id` ASC, `role` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 793
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '群成员表'
  ROW_FORMAT = DYNAMIC;



BEGIN;
INSERT INTO `chat_group_member` (`id`,
                                 `group_id`,
                                 `userId`,
                                 `role`,
                                 `create_time`,
                                 `update_time`)
VALUES (1,
        1,
        '100001',
        1,
        '2025-11-23 15:00:00.000',
        '2025-11-23 15:00:00.000');
COMMIT;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`
(
    `id`           bigint                                                       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `room_id`      bigint                                                       NOT NULL COMMENT '会话表id',
    `from_uid`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息发送者uid',
    `content`      mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '消息内容',
    `reply_msg_id` bigint                                                       NULL     DEFAULT NULL COMMENT '回复的消息内容',
    `status`       int                                                          NOT NULL COMMENT '消息状态 0删除 1正常 ',
    `gap_count`    int                                                          NULL     DEFAULT NULL COMMENT '与回复的消息间隔多少条',
    `type`         int                                                          NULL     DEFAULT 1 COMMENT '消息类型 1正常文本 2.撤回消息',
    `extra`        json                                                         NULL COMMENT '扩展信息',
    `create_time`  datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_room_id` (`room_id` ASC) USING BTREE,
    INDEX `idx_from_uid` (`from_uid` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 10630
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表'
  ROW_FORMAT = DYNAMIC;

# 系统初始化消息
BEGIN;
INSERT INTO `chat_message` (`id`, `room_id`, `from_uid`, `content`, `reply_msg_id`, `status`, `gap_count`, `type`,
                            `extra`, `create_time`, `update_time`)
VALUES (1, 1, '100001', '欢迎大家，畅所欲言！', NULL, 1, NULL, 8, NULL, '2025-11-23 15:00:00.000', '2025-11-23 15:00:00.000');
COMMIT;

-- ----------------------------
-- Table structure for chat_room
-- ----------------------------
DROP TABLE IF EXISTS `chat_room`;
CREATE TABLE `chat_room`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `type`        int             NOT NULL COMMENT '房间类型 1群聊 2单聊 3ai聊',
    `hot_flag`    int             NULL     DEFAULT 0 COMMENT '是否全员展示 0否 1是',
    `last_msg_id` bigint          NULL     DEFAULT NULL COMMENT '会话中的最后一条消息id',
    `ext_json`    json            NULL COMMENT '额外信息（根据不同类型房间有不同存储的东西）',
    `update_time` datetime(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '群最后消息的更新时间（热点群不需要写扩散，只更新这里）',
    `create_time` datetime(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3766
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '房间表'
  ROW_FORMAT = DYNAMIC;
-- ----------------------------
-- Records of chat_room
-- ----------------------------
BEGIN;
INSERT INTO `chat_room` (`id`, `type`, `hot_flag`, `last_msg_id`, `ext_json`, `update_time`, `create_time`)
VALUES (1, 1, 1, NULL, NULL, '2025-11-22 16:12:46.794', '2025-11-22 16:12:46.794');
COMMIT;
-- ----------------------------
-- Table structure for chat_room_group
-- ----------------------------
DROP TABLE IF EXISTS `chat_room_group`;
CREATE TABLE `chat_room_group`
(
    `id`            bigint UNSIGNED                                               NOT NULL AUTO_INCREMENT COMMENT 'id',
    `room_id`       bigint                                                        NOT NULL COMMENT '房间id',
    `name`          varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '群名称',
    `avatar`        varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群头像',
    `ext_json`      json                                                          NULL COMMENT '额外信息（根据不同类型房间有不同存储的东西）',
    `delete_status` int                                                           NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)',
    `create_time`   datetime(3)                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time`   datetime(3)                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_room_id` (`room_id` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 97
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '群聊房间表'
  ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Records of chat_room_group
-- ----------------------------
BEGIN;

INSERT INTO
    `chat_room_group` (
        `id`,
        `room_id`,
        `name`,
        `avatar`,
        `ext_json`,
        `delete_status`,
        `create_time`,
        `update_time`
    )
VALUES (
        1,
        1,
        '极物圈 - 官方群聊 🥝',
        'image/logo.png',
        '{\"notice\": \"欢迎加入本群，参与讨论前请遵守以下规则：\\n1、遵守法律法规，不发布违法内容。\\n2、自行判断信息真伪，群不保证信息准确性。\\n3、保护个人隐私，不分享敏感信息。\\n4、尊重版权，不分享侵权材料。\\n5、禁止广告和未经许可的推销。\\n6、群管理员不承担成员交易或互动后果。\\n本公告可能随时更新。感谢合作。\"}',
        0,
        '2023-12-18 17:09:20.987',
        '2025-10-13 13:04:02.013'
    );

COMMIT;

-- ----------------------------
-- Table structure for chat_room_self
-- ----------------------------
DROP TABLE IF EXISTS `chat_room_self`;
CREATE TABLE `chat_room_self`
(
    `id`          bigint UNSIGNED                                              NOT NULL AUTO_INCREMENT COMMENT 'id',
    `room_id`     bigint                                                       NOT NULL COMMENT '房间id',
    `uid1`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'uid1（更小的uid）',
    `uid2`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'uid2（更大的uid）',
    `room_key`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间key由两个uid拼接，先做排序uid1_uid2',
    `status`      int                                                          NOT NULL COMMENT '房间状态 0正常 1禁用(删好友了禁用)',
    `create_time` datetime(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` datetime(3)                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `room_key` (`room_key` ASC) USING BTREE,
    INDEX `idx_room_id` (`room_id` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 389
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '单聊房间表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_user_apply
-- ----------------------------
DROP TABLE IF EXISTS `chat_user_apply`;
CREATE TABLE `chat_user_apply`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '申请人uid',
    `type`        tinyint                                                       NOT NULL COMMENT '申请类型 1加好友',
    `target_id`   varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '接收人uid',
    `msg`         varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请信息',
    `status`      tinyint                                                       NOT NULL COMMENT '申请状态 0待审批 1同意',
    `read_status` tinyint                                                       NOT NULL COMMENT '阅读状态 0未读 1已读',
    `create_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_uid_target_id` (`user_id` ASC, `target_id` ASC) USING BTREE,
    INDEX `idx_target_id_read_status` (`target_id` ASC, `read_status` ASC) USING BTREE,
    INDEX `idx_target_id` (`target_id` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 482
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户申请表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_user_friend
-- ----------------------------
DROP TABLE IF EXISTS `chat_user_friend`;
CREATE TABLE `chat_user_friend`
(
    `id`            bigint                                                       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'uid',
    `friend_uid`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '好友uid',
    `delete_status` tinyint(1)                                                   NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常,1-删除)',
    `create_time`   datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_uid_friend_uid` (`user_id` ASC, `friend_uid` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 830
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户联系人表'
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `id`             varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'id',
    `name`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '名称',
    `code`           varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '菜单编码',
    `parent_id`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '父节点',
    `type`           tinyint(1)                                                    NOT NULL DEFAULT 1 COMMENT '节点类型：（1页面，2按钮）',
    `sys_type`       tinyint UNSIGNED                                              NOT NULL DEFAULT 1 COMMENT '用户类型（0前台，1管理...）',
    `link_url`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL     DEFAULT '' COMMENT '页面对应的地址',
    `sort_order`     int                                                           NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time`    datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `component_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '组件位置',
    `icon`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '图标',
    `on_icon`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '激活图标',
    PRIMARY KEY (`id`, `code`) USING BTREE,
    INDEX `parent_id_i` (`parent_id` ASC) USING BTREE COMMENT '父菜单id',
    INDEX `order_i` (`sort_order` DESC) USING BTREE COMMENT '权重索引'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713968540587622402', '商品', 'goods', NULL, 1, 1, '/goods', 97, '2023-10-17 01:21:59', '2023-11-24 15:14:18',
        '', 'i-solar:bag-smile-broken', 'i-solar:bag-smile-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713968772394221570', '商品列表', 'goods:info', '1713968540587622402', 1, 1, '/goods/info', 99,
        '2023-10-17 01:22:54', '2023-11-24 15:16:39', '', 'i-solar:box-broken', 'i-solar:box-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713968978506514433', '商品分类', 'goods:category', '1713968540587622402', 1, 1, '/goods/category', 98,
        '2023-10-17 01:23:43', '2023-11-24 15:17:01', '', 'i-solar:widget-add-outline', 'i-solar:widget-add-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713969700325261314', '活动', 'event', NULL, 1, 1, '/event', 96, '2023-10-17 01:26:35', '2023-11-24 15:17:42',
        '', 'i-solar:sale-broken', 'i-solar:sale-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713969968467116033', '活动列表', 'event:info', '1713969700325261314', 1, 1, '/event/info', 99,
        '2023-10-17 01:27:39', '2023-11-24 15:18:01', '', 'i-solar:tag-broken', 'i-solar:tag-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713972407501029377', '活动商品', 'event:goods', '1713969700325261314', 1, 1, '/event/goods', 98,
        '2023-10-17 01:37:21', '2023-11-24 15:18:40', '', 'i-solar:box-minimalistic-broken',
        'i-solar:box-minimalistic-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713974993499160577', '订单', 'orders', NULL, 1, 1, '/orders', 95, '2023-10-17 01:47:37',
        '2023-11-24 15:19:22', '', 'i-solar:bill-check-broken', 'i-solar:bill-check-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713975135413436417', '订单列表', 'orders:info', '1713974993499160577', 1, 1, '/orders/info', 99,
        '2023-10-17 01:48:11', '2023-11-24 15:19:48', '', 'i-solar:cart-check-broken', 'i-solar:cart-check-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713979498676584450', '订单评价', 'orders:comment', '1713974993499160577', 1, 1, '/orders/comment', 98,
        '2023-10-17 02:05:31', '2023-11-24 15:20:04', '', 'i-solar:chat-line-broken', 'i-solar:chat-line-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713982007906369538', '账单', 'bills', NULL, 1, 1, '/bills', 94, '2023-10-17 02:15:30', '2023-11-24 15:23:26',
        '', 'i-solar:bill-list-linear', 'i-solar:bill-list-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713982194359959553', '充值套餐', 'bills:recharge', '1713982007906369538', 1, 1, '/bills/recharge', 99,
        '2023-10-17 02:16:14', '2023-11-24 15:23:00', '', 'i-solar:wallet-2-broken', 'i-solar:wallet-2-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1713986036686356481', '账单列表', 'bills:info', '1713982007906369538', 1, 1, '/bills/info', 98,
        '2023-10-17 02:31:30', '2023-11-24 15:22:42', '', 'i-solar:card-transfer-line-duotone',
        'i-solar:card-transfer-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1725323245752918018', '社区', 'community', NULL, 1, 1, '/community', 93, '2023-11-17 09:21:31',
        '2023-11-24 15:23:50', NULL, 'i-solar:ufo-3-broken', 'i-solar:ufo-3-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1725323486329806849', '帖子列表', 'community:post', '1725323245752918018', 1, 1, '/community/post', 99,
        '2023-11-17 09:22:29', '2023-11-24 15:24:56', NULL, 'i-solar:sticker-smile-circle-2-broken',
        'i-solar:sticker-smile-circle-2-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1725323599773147138', '分类列表', 'community:category', '1725323245752918018', 1, 1, '/community/category', 98,
        '2023-11-17 09:22:56', '2023-11-24 15:24:28', NULL, 'i-solar:widget-2-broken', 'i-solar:widget-add-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1725323795416457217', '评论列表', 'community:comment', '1725323245752918018', 1, 1, '/community/comment', 97,
        '2023-11-17 09:23:42', '2023-11-24 15:25:31', NULL, 'i-solar:chat-line-broken', 'i-solar:chat-line-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1809493209816670210', '工具', 'tools', NULL, 1, 1, '/tools', 92, '2024-07-06 15:42:55', '2024-07-06 15:44:03',
        '', 'i-solar:inbox-archive-linear', 'i-solar:inbox-archive-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1809493416335810562', '聊天', 'chat', '1809493209816670210', 1, 1, '/tools/chat', 99, '2024-07-06 15:43:44',
        '2024-07-06 15:44:13', '@/views/tools/ChatView.vue', 'i-solar:chat-line-broken',
        'i-solar:chat-line-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1811752663585009665', '资源', 'res', NULL, 1, 1, '', 98, '2024-07-12 21:21:10', '2024-07-12 21:21:10', NULL,
        'i-solar:library-line-duotone', 'i-solar:library-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1895161541196709890', 'AI模块', 'ai', NULL, 1, 1, '', 98, '2025-02-28 01:18:16', '2025-02-28 01:19:39', NULL,
        'i-ri:color-filter-ai-line', 'i-ri:color-filter-ai-fill');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('1895161852346957825', '机器人管理', 'ai:robot', '1895161541196709890', 1, 1, '/ai/robot', 99,
        '2025-02-28 01:19:31', '2025-02-28 01:19:31', '@/views/ai/RobotView.vue', 'i-ri:robot-2-line',
        'i-ri:robot-2-fill');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('2709640055336398850', '首页', 'home', NULL, 1, 1, '/home', 99, '2023-10-16 03:00:53', '2024-05-24 23:40:24',
        '@/views/IndexView.vue', 'i-solar:home-2-outline', 'i-solar:home-2-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('2709640055336398851', '系统', 'sys', NULL, 1, 1, '/sys', 98, '2023-10-16 03:00:53', '2024-02-10 20:19:52',
        '@/views/sys/IndexView.vue', 'i-solar:laptop-outline', 'i-solar:laptop-bold');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('2709640055336398852', '用户', 'user', NULL, 1, 1, '/user', 98, '2023-10-16 03:00:53', '2024-02-10 20:15:47',
        '@/views/user/MenuView.vue', 'i-solar:shield-user-broken', 'i-solar:shield-user-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('5339640055336398841', '用户列表', 'user:info', '2709640055336398852', 1, 1, '/user/info', 99,
        '2023-10-16 03:00:53', '2024-02-10 20:22:15', '@/views/user/InfoView.vue',
        'i-solar:lock-keyhole-minimalistic-broken', 'i-solar:lock-keyhole-minimalistic-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('5339640055336398842', '角色列表', 'user:role', '2709640055336398852', 1, 1, '/user/role', 98,
        '2023-10-16 03:00:53', '2024-02-10 20:22:34', '@/views/user/RoleView.vue', 'i-solar:users-group-rounded-broken',
        'i-solar:users-group-rounded-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('5339640055336398843', '权限列表', 'user:permission', '2709640055336398852', 1, 1, '/user/permission', 98,
        '2023-10-16 03:00:53', '2024-02-10 20:21:51', '@/views/user/PermissionView.vue',
        'i-solar:users-group-rounded-broken', 'i-solar:users-group-rounded-bold-duotone');
INSERT INTO `sys_menu` (`id`, `name`, `code`, `parent_id`, `type`, `sys_type`, `link_url`, `sort_order`, `create_time`,
                        `update_time`, `component_path`, `icon`, `on_icon`)
VALUES ('53396400553363988434', '菜单列表', 'user:menu', '2709640055336398852', 1, 1, '/user/menu', 98,
        '2023-10-16 03:00:53', '2024-02-10 20:21:32', '@/views/user/MenuView.vue', 'i-solar:signpost-2-broken',
        'i-solar:signpost-2-bold-duotone');
COMMIT;

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
    `id`          char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NOT NULL COMMENT '权限ID',
    `parent_id`   char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NULL DEFAULT NULL COMMENT '所属父级权限ID',
    `code`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限唯一CODE代码',
    `name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
    `intro`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限介绍',
    `creator`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建人',
    `create_time` datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`, `code`) USING BTREE,
    UNIQUE INDEX `code` (`code` ASC) USING BTREE COMMENT '权限CODE代码',
    INDEX `parent_id` (`parent_id` ASC) USING BTREE COMMENT '父级权限ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
BEGIN;
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105282', NULL, 'admin:user:permission:init:add', '初始化全部权限（超级用户）',
        '初始化全部权限（超级用户）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105283', NULL, 'admin:user:role:list:{page}:{size}:view', '获取角色列表（分页）',
        '获取角色列表（分页）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105284', NULL, 'admin:user:{userId}:view', '获取用户信息（管理员）', '获取用户信息（管理员）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105285', NULL, 'admin:user:permission:list:valid:view', '获取权限列表（未使用）',
        '获取权限list列表', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105286', NULL, 'admin:total:bills:list:view', '获取账单统计列表（管理员主页）',
        '获取账单统计列表（管理员主页）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105287', NULL, 'admin:goods:add', '添加商品', '添加商品', 'SUPER_ADMIN', '2023-11-21 02:35:45',
        '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105288', NULL, 'admin:orders:list:{page}:{size}:view', '获取订单列表（分页）', '获取订单列表（分页）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105289', NULL, 'admin:user:permission:{id}:del', '删除权限（单条）', '删除权限（单条）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105290', NULL, 'admin:res:file:del', '删除oss未使用文件(管理员)', '删除oss未使用文件(管理员)',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105291', NULL, 'admin:community:category:some:del', '批量删除社区分类（管理员）',
        '删除社区分类，管理员删除', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105292', NULL, 'admin:wallet:combo:list:view', '获取充值套餐列表', '获取充值套餐列表',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105293', NULL, 'admin:total:orders:view', '获取订单统计（管理员主页）', '获取订单统计（管理员主页）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105294', NULL, 'admin:total:main:view', '获取首页概览统计（管理员主页）',
        '获取首页概览统计（管理员主页）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105295', NULL, 'admin:wallet:combo:one:add', '添加充值套餐', '添加充值套餐', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105296', NULL, 'admin:community:post:comment:batchDel', '批量删除社区评论（管理员）',
        '删除社区评论，管理员删除', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105297', NULL, 'admin:total:order:sales:list:view', '获取订单统计列表（管理员主页）',
        '获取订单统计列表（管理员主页）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680187105298', NULL, 'admin:goods:sku:{gid}:batchDel', '删除商品规格（批量）', '删除商品规格（批量）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299586', NULL, 'admin:goods:category:one:{id}:del', '删除分类（单个）', '删除分类（单个）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299587', NULL, 'admin:community:post:comment:{page}:{size}:view', '获取社区帖子评论列表（分页）',
        '获取社区帖子评论列表（翻页），管理员', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299588', NULL, 'admin:sys:redis:all:del', 'Redis清空缓存（管理员）',
        'Redis清空缓存（管理员），谨慎使用', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299589', NULL, 'admin:event:goods:{eid}:{id}:del', '删除活动商品（单个）', '删除活动商品（单个）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299590', NULL, 'admin:community:post:{id}:view', '管理员获取帖子详情（分页）',
        '管理员获取帖子详情（分页筛选）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299591', NULL, 'admin:community:post:{userId}:{id}:del', '删除社区帖子（管理员）',
        '删除帖子，审核,管理员删除', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299592', NULL, 'admin:user:info:{userId}:add', '添加管理员用户（管理员）', '添加管理员用户（管理员）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299593', NULL, 'admin:user:menu:{mid}:edit', '修改菜单', '修改菜单(单条添加)', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299594', NULL, 'admin:bills:list:{page}:{size}:view', '获取账单列表（分页）（管理员）',
        '管理员获取账单列表（分页）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299595', NULL, 'admin:community:category:add', '修改社区分类（管理员）', '修改社区分类，管理员修改',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299596', NULL, 'admin:user:logout:{userId}:del', '用户强制下线（管理员操作）',
        '用户强制下线（管理员操作）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299597', NULL, 'admin:event:list:view', '获取全部活动列表', '获取全部活动列表', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299598', NULL, 'admin:goods:category:some:add', '批量添加分类', '批量添加分类ids', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299599', NULL, 'admin:res:file:edit', '消费Oss文件（管理员）', '消费Oss文件（管理员），谨慎使用',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299600', NULL, 'admin:orders:refund:{userId}:{id}:edit', '订单退款（同意）', '订单同意用户退款',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299601', NULL, 'admin:goods:{id}:edit', '修改商品', '修改商品', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299602', NULL, 'admin:res:video:add', '获取上传临时凭证（视频）(管理员)',
        '获取上传临时凭证（视频）(管理员)', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299603', NULL, 'admin:event:{id}:del', '删除活动', '删除活动', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299604', NULL, 'admin:goods:category:tree:view', '获取所有分类（树形）', '获取所有分类（树形）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299605', NULL, 'admin:community:category:list:view', '获取社区分类（管理员）',
        '获取社区分类，管理员修改', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299606', NULL, 'admin:goods:category:some::batchDel', '删除分类（批量）', '删除分类（批量）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299607', NULL, 'admin:user:role:bind:{userId}:edit', '关联用户角色（管理员）',
        '添加、修改关联用户角色（管理员）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299608', NULL, 'admin:user:permission:{id}:edit', '修改权限', '修改权限', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299609', NULL, 'admin:res:image:add', '获取上传临时凭证（图片）(管理员)',
        '获取上传临时凭证（图片）(管理员)', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299610', NULL, 'admin:event:goods:list:add', '添加活动商品（批量）', '添加活动商品（批量）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299611', NULL, 'admin:community:post:list:{page}:{size}:view', '管理员获取社区帖子（分页）',
        '管理员获取社区帖子（分页筛选）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299612', NULL, 'admin:goods:category:one:{id}:edit', '修改分类', '修改分类', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299613', NULL, 'admin:goods:category:one:add', '添加分类', '添加分类', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299614', NULL, 'admin:user:{page}:{size}:view', '分页获取用户列表（管理员）',
        '分页获取用户列表（管理员）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299615', NULL, 'admin:event:goods:{id}:view', '获取活动商品 (部分)', '获取活动商品 (部分)',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299616', NULL, 'admin:user:permission:list:exist:view', '获取权限列表（有效）',
        '获取有效已使用的权限列表（已存在、已使用）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299617', NULL, 'admin:orders:refund:delivery:edit', '订单发货', '订单发货填写发货', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299618', NULL, 'admin:goods:batchDel', '删除商品（批量）', '删除商品（批量）', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299619', NULL, 'res:user:files:del', '删除oss未使用文件', '用户消费者删除oss未使用文件',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299620', NULL, 'admin:goods:sku:{gid}:{id}:del', '删除商品规格（单个）', '删除商品规格（单个）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299621', NULL, 'admin:community:post:status:{id}:edit', '修改帖子状态（管理员）',
        '修改帖子状态，管理员修改、审核', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299622', NULL, 'admin:user:role:edit', '修改角色', '修改角色信息', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299623', NULL, 'admin:wallet:combo:{id}:edit', '修改充值套餐', '修改充值套餐', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299624', NULL, 'admin:orders:{userId}:{id}:view', '获取订单详细信息', '获取订单详细信息',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299625', NULL, 'res:user:video:add', '获取上传临时凭证（视频）', '用户消费者获取上传临时凭证（视频）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299626', NULL, 'admin:user:menu:add', '添加菜单', '添加菜单(单条添加)', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299627', NULL, 'admin:user:permission:some:batchDel', '删除权限（批量）', '删除权限的批量删除',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299628', NULL, 'admin:orders:delivery:{userId}:{id}:view', '获取订单发货信息', '获取订单发货信息',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299629', NULL, 'admin:community:category:{id}:del', '删除社区分类（管理员）',
        '删除社区分类，管理员删除', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299630', NULL, 'user:bills:list:page:size:view', '分页获取账单', '用户分页获取账单',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299631', NULL, 'user:bills:total:detail:view', '获取账单统计（详细）', '用户获取账单统计详细信息',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299632', NULL, 'admin:wallet:combo:one:{id}:del', '删除充值套餐（单个）', '删除充值套餐（单个）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299633', NULL, 'admin:user:menu:tree:view', '获取菜单列表（树）', '获取菜单列表（树）', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299634', NULL, 'admin:user:permission:add', '添加权限（单条）', '添加权限单条添加', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299635', NULL, 'admin:event:add', '添加活动', '添加活动', 'SUPER_ADMIN', '2023-11-21 02:35:45',
        '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299636', NULL, 'admin:goods:sku:add', '添加商品规格', '添加商品规格', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299637', NULL, 'admin:event:goods:add', '添加活动商品 (单个)', '添加活动商品 (单个)',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299638', NULL, 'admin:user:disable:{userId}:del', '用户禁用（管理员操作）', '用户禁用（管理员操作）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299639', NULL, 'admin:user:role:add', '添加角色（单条）', '添加角色单条添加', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299640', NULL, 'admin:user:role:batchDel', '删除角色（批量）', '删除角色批量', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299641', NULL, 'res:user:image:add', '获取上传临时凭证（图片）', '用户消费者获取上传临时凭证（图片）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299642', NULL, 'admin:goods:{id}:view', '获取商品详细信息', '获取商品详细信息', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299643', NULL, 'admin:event:goods:{eid}:batchDel', '删除活动商品（批量）', '删除活动商品（批量）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299644', NULL, 'admin:goods:category:list:view', '获取所有分类（列表）', '获取所有分类（列表）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299645', NULL, 'admin:event:{id}:edit', '修改活动', '修改活动', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299646', NULL, 'admin:goods:sku:{gid}:view', '获取商品规格（商品id）', '获取商品规格（商品id）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299647', NULL, 'admin:goods:sku:{gid}:{id}:edit', '修改商品规格', '修改商品规格', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299648', NULL, 'admin:user:role:tree:{page}:{size}:view', '获取角色列表（树）', '获取角色列表（树）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299649', NULL, 'admin:res:file:add', '获取上传临时凭证（文件）(管理员)',
        '获取上传临时凭证（文件）(管理员)', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299650', NULL, 'admin:community:category:{id}:edit', '修改社区分类（管理员）',
        '修改社区分类，管理员修改', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299651', NULL, 'admin:goods:{id}:del', '删除商品（单个）', '删除商品（单个）', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299652', NULL, 'admin:goods:{page}:{size}:view', '获取商品列表（分页）', '获取商品信息列表分页',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299653', NULL, 'admin:event:goods:list:{page}:{size}:view', '获取活动商品列表 (分页)',
        '获取活动商品列表 (分页)', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299654', NULL, 'admin:user:info:{userId}:edit', '修改用户信息（管理员）', '修改用户信息（管理员）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299655', NULL, 'admin:event:goods:{eid}:{id}:edit', '修改活动商品', '修改活动商品', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299656', NULL, 'admin:wallet:combo:some:batchDel', '删除充值套餐（批量）', '删除充值套餐（批量）',
        'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299657', NULL, 'admin:community:post:{id}:edit', '修改社区帖子（管理员）',
        '修改帖子，审核,管理员修改', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299658', NULL, 'admin:user:menu:bind:{userId}:edit', '关联角色菜单',
        '添加、修改关联用户角色（管理员）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299659', NULL, 'admin:user:role:codes:{userId}:view', '获取用户角色列表',
        '获取用户角色列表（code）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299660', NULL, 'admin:user:permission:list:{page}:{size}:view', '获取权限列表（分页）',
        '获取权限列表（分页）', 'SUPER_ADMIN', '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1726670680191299661', NULL, 'admin:user:menu:batchDel', '删除菜单（批量）', '删除菜单批量', 'SUPER_ADMIN',
        '2023-11-21 02:35:45', '2023-11-21 02:35:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1728106439295246337', NULL, 'admin:orders:comment:list:{page}:{size}:view', '获取订单评论列表（分页）（管理员）',
        '获取订单评论列表（分页）（管理员）', 'SUPER_ADMIN', '2023-11-25 01:40:56', '2023-11-25 01:40:56');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1767981957767229441', NULL, 'admin:community:post:del:{userId}:{id}:del', '删除社区帖子（管理员软删除）',
        '删除帖子，审核,管理员删除', 'SUPER_ADMIN', '2024-03-14 02:32:01', '2024-03-13 18:32:00');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1809488069147664386', NULL, 'res:user:image:audio', '获取上传临时凭证（音频）',
        '用户消费者获取上传临时凭证（音频）', 'SUPER_ADMIN', '2024-07-06 15:22:29', '2024-07-06 15:22:29');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1809848498695327745', NULL, 'user:info:check:code:get', '验证用户获取验证码(常规用户)',
        '验证用户获取验证码(常规用户)，用于修改密码等', 'SUPER_ADMIN', '2024-07-07 15:14:42', '2024-07-07 07:14:42');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1809848498695327746', NULL, 'user:info:code:{key}:get', '获取新手机/邮箱验证码(常规用户)',
        '获取新手机/邮箱验证码(常规用户)，预览用户不可使用', 'SUPER_ADMIN', '2024-07-07 15:14:42',
        '2024-07-07 07:14:42');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1809848498695327747', NULL, 'user:info:phone:edit', '更换手机号(常规用户)',
        '更换手机号(常规用户)，预览用户不可使用', 'SUPER_ADMIN', '2024-07-07 15:14:42', '2024-07-07 07:14:42');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1809848498695327748', NULL, 'user:info:pwd:{type}:edit', '修改密码V2(常规用户)',
        '修改密码V2(常规用户)，预览用户不可使用', 'SUPER_ADMIN', '2024-07-07 15:14:42', '2024-07-07 07:14:42');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1809848498695327749', NULL, 'user:info:pwd:edit', '修改密码(常规用户)', '修改密码(常规用户)，预览用户不可使用',
        'SUPER_ADMIN', '2024-07-07 15:14:42', '2024-07-07 07:14:42');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1809848498695327750', NULL, 'user:info:email:edit', '更换邮箱(常规用户)',
        '更换邮箱(常规用户)，预览用户不可使用', 'SUPER_ADMIN', '2024-07-07 15:14:42', '2024-07-07 07:14:42');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1843713190920417282', NULL, 'res:user:file:add', '获取上传临时凭证（文件）', '用户消费者获取上传临时凭证（文件）',
        'SUPER_ADMIN', '2024-10-09 02:00:54', '2024-10-08 18:00:53');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1851677126001139713', NULL, 'admin:sys:redis:keys:view', 'Redis键集合（管理员）', 'Redis键集合（管理员），谨慎使用',
        'SUPER_ADMIN', '2024-10-31 01:26:44', '2024-10-30 17:26:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1851677126026305537', NULL, 'admin:sys:redis:keys:del', 'Redis删除key（管理员）',
        'Redis删除key缓存（管理员），谨慎使用', 'SUPER_ADMIN', '2024-10-31 01:26:44', '2024-10-30 17:26:44');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1885609255285997569', NULL, 'user:info:edit', '修改基本信息(常规用户)',
        '修改基本信息(常规用户)，预览用户不可使用', 'SUPER_ADMIN', '2025-02-01 16:40:54', '2025-02-01 08:40:54');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1885609255285997570', NULL, 'user:info:avatar:edit', '用户头像更改(常规用户)',
        '用户头像更改(常规用户)，预览用户不可使用', 'SUPER_ADMIN', '2025-02-01 16:40:54', '2025-02-01 08:40:54');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1889692414654976002', NULL, 'admin:user:info:pwd:{userId}:edit', '修改用户密码（管理员）',
        '修改用户密码（管理员）- 部分情况下使用', 'SUPER_ADMIN', '2025-02-12 23:05:55', '2025-02-12 15:05:55');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1891252819399446530', NULL, 'chat/user/friend/apply', '申请好友（包括申请机器人）', '好友模块', 'SUPER_ADMIN',
        '2025-02-17 06:26:25', '2025-02-16 22:26:24');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1906759850713571330', NULL, 'admin:ai:model:code', '获取可选模型列表（管理员）', '获取可选模型列表',
        'SUPER_ADMIN', '2025-04-01 01:25:49', '2025-04-01 01:25:48');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1906759850713571331', NULL, 'admin:ai:robot:update', '修改机器人（管理员）', '修改机器人', 'SUPER_ADMIN',
        '2025-04-01 01:25:49', '2025-04-01 01:25:48');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1906759850713571332', NULL, 'admin:ai:robot:delete', '删除机器人（管理员）', '删除机器人', 'SUPER_ADMIN',
        '2025-04-01 01:25:49', '2025-04-01 01:25:48');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1906759850713571333', NULL, 'admin:ai:model:list', '获取模型列表（+余额）(管理员)',
        '获取模型列表（+余额）(管理员)，可定时调用', 'SUPER_ADMIN', '2025-04-01 01:25:49', '2025-04-01 01:25:48');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1906759850713571334', NULL, 'admin:ai:robot:add', '添加机器人（管理员）', '添加机器人', 'SUPER_ADMIN',
        '2025-04-01 01:25:49', '2025-04-01 01:25:48');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1906759850713571335', NULL, 'res:utils:translation:sse', '流式翻译文本(sse)', '用户消费者翻译文本(sse)',
        'SUPER_ADMIN', '2025-04-01 01:25:49', '2025-04-01 01:25:48');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1906759850713571336', NULL, 'res:utils:translation', '翻译文本', '用户消费者翻译文本', 'SUPER_ADMIN',
        '2025-04-01 01:25:49', '2025-04-01 01:25:48');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1964065910411202561', NULL, 'sys:api-key:create', '创建API密钥（用户）', '创建API密钥（用户）', 'SUPER_ADMIN',
        '2025-09-06 04:39:38', '2025-09-06 04:39:38');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1964065910411202562', NULL, 'sys:api-key:page', '分页查询API密钥（用户）', '分页查询API密钥（用户）',
        'SUPER_ADMIN', '2025-09-06 04:39:38', '2025-09-06 04:39:38');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1964065910411202563', NULL, 'sys:api-key:detail', '获取API密钥详情（用户）', '获取API密钥详情（用户）',
        'SUPER_ADMIN', '2025-09-06 04:39:38', '2025-09-06 04:39:38');
INSERT INTO `sys_permission` (`id`, `parent_id`, `code`, `name`, `intro`, `creator`, `create_time`, `update_time`)
VALUES ('1964065910411202564', NULL, 'sys:api-key:update', '更新API密钥（用户）', '更新API密钥（用户）', 'SUPER_ADMIN',
        '2025-09-06 04:39:38', '2025-09-06 04:39:38');
COMMIT;

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NOT NULL,
    `parent_id`   char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NULL DEFAULT NULL COMMENT '所属父级角色ID',
    `name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
    `code`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色唯一CODE代码',
    `intro`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色介绍',
    `create_time` datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建人（id）',
    PRIMARY KEY (`id`, `code`) USING BTREE,
    UNIQUE INDEX `code` (`code` ASC) USING BTREE,
    INDEX `parent_id` (`parent_id` ASC) USING BTREE COMMENT '父级角色ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('1709640055336398850', NULL, '作者', 'ROLE_KIWI', '用于作者前台日常测试', '2023-10-05 02:42:07',
        '2023-10-07 01:41:21', '100001');
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('1709642621461942274', '5819236053864939521', '访客管理员', 'ROLE_ADMIN_DEFAULT', '作为访客试用后台',
        '2023-10-05 02:52:19', '2025-11-21 10:31:14', '100001');
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('1709959399039766529', NULL, '普通用户', 'ROLE_CUSTOMER_DEFAULT', '常规用户，常规资源配置',
        '2023-10-05 23:51:05', '2025-09-06 04:40:47', '100001');
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('1739350468044570626', '5819236053864939521', '商品管理员', 'ROLE_ADMIN_GOODS', NULL, '2023-12-26 02:20:42',
        '2023-12-26 02:20:42', '100001');
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('1739848802325307393', NULL, '机器人', 'ROLE_ROBOT', '主要用于自动回复的客服、AI机器人', '2023-12-27 11:20:54',
        '2023-12-27 11:20:54', '100001');
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('5819236053864939521', NULL, '超级管理员', 'SUPER_ADMIN', '系统的全部权限,包括增删查改管理员',
        '2023-05-06 01:44:29', '2024-10-09 02:01:30', '5819236053864939521');
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('5819236053864939524', '5819236053864939521', '客服', 'ROLE_SERVICE', '客服', '2023-05-06 01:44:29',
        '2023-10-16 04:06:04', '5819236053864939521');
INSERT INTO `sys_role` (`id`, `parent_id`, `name`, `code`, `intro`, `create_time`, `update_time`, `creator`)
VALUES ('5819236053864939525', NULL, '访客用户', 'ROLE_CUSTOMER', '访问网站内容，只能访问部分', '2023-05-06 01:44:29',
        '2025-04-14 00:49:06', '5819236053864939521');
COMMIT;

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`
(
    `id`          char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NOT NULL COMMENT 'id',
    `role_id`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色ID',
    `menu_id`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单ID',
    `creator`     char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NULL DEFAULT NULL COMMENT '创建人id',
    `create_time` datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `index_role_id` (`role_id` ASC) USING BTREE COMMENT '角色ID',
    INDEX `index_menu_id` (`menu_id` ASC) USING BTREE COMMENT '权限ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色—菜单表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1716136841992077314', '1716135295912902657', '2709640055336398850', '100001', '2023-10-23 00:58:02',
        '2023-10-23 00:58:02');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1716136841992077315', '1716135295912902657', '1713968540587622402', '100001', '2023-10-23 00:58:02',
        '2023-10-23 00:58:02');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1716136841992077316', '1716135295912902657', '1713968772394221570', '100001', '2023-10-23 00:58:02',
        '2023-10-23 00:58:02');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1716136841992077317', '1716135295912902657', '1713968978506514433', '100001', '2023-10-23 00:58:02',
        '2023-10-23 00:58:02');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739353455173353474', '1739350468044570626', '2709640055336398850', '100001', '2023-12-26 02:32:34',
        '2023-12-26 02:32:34');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739353455173353475', '1739350468044570626', '1713968540587622402', '100001', '2023-12-26 02:32:34',
        '2023-12-26 02:32:34');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739353455173353476', '1739350468044570626', '1713968772394221570', '100001', '2023-12-26 02:32:34',
        '2023-12-26 02:32:34');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739353455173353477', '1739350468044570626', '1713968978506514433', '100001', '2023-12-26 02:32:34',
        '2023-12-26 02:32:34');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739353455173353478', '1739350468044570626', '1713969700325261314', '100001', '2023-12-26 02:32:34',
        '2023-12-26 02:32:34');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739353455173353479', '1739350468044570626', '1713969968467116033', '100001', '2023-12-26 02:32:34',
        '2023-12-26 02:32:34');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739353455173353480', '1739350468044570626', '1713972407501029377', '100001', '2023-12-26 02:32:34',
        '2023-12-26 02:32:34');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848385', '5819236053864939524', '1713974993499160577', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848386', '5819236053864939524', '1713975135413436417', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848387', '5819236053864939524', '1713968540587622402', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848388', '5819236053864939524', '1713968772394221570', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848389', '5819236053864939524', '2709640055336398850', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848390', '5819236053864939524', '1713979498676584450', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848391', '5819236053864939524', '1713968978506514433', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848392', '5819236053864939524', '1809493209816670210', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1809811239480848393', '5819236053864939524', '1809493416335810562', '100001', '2024-07-07 12:46:39',
        '2024-07-07 04:46:38');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009282', '5819236053864939521', '1725323245752918018', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009283', '5819236053864939521', '1725323486329806849', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009284', '5819236053864939521', '1811752663585009665', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009286', '5819236053864939521', '1809493209816670210', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009287', '5819236053864939521', '1809493416335810562', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009288', '5819236053864939521', '1713968540587622402', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009289', '5819236053864939521', '1713968772394221570', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009290', '5819236053864939521', '1713969700325261314', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009291', '5819236053864939521', '1713969968467116033', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009292', '5819236053864939521', '1713982007906369538', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009293', '5819236053864939521', '1713982194359959553', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009294', '5819236053864939521', '1713974993499160577', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009295', '5819236053864939521', '1713975135413436417', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009296', '5819236053864939521', '2709640055336398852', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009297', '5819236053864939521', '5339640055336398841', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009298', '5819236053864939521', '2709640055336398850', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009299', '5819236053864939521', '1713986036686356481', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009300', '5819236053864939521', '5339640055336398843', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009301', '5819236053864939521', '5339640055336398842', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009302', '5819236053864939521', '2709640055336398851', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009303', '5819236053864939521', '1713979498676584450', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009304', '5819236053864939521', '1725323599773147138', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009305', '5819236053864939521', '53396400553363988434', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009306', '5819236053864939521', '1713968978506514433', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009307', '5819236053864939521', '1713972407501029377', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009308', '5819236053864939521', '1725323795416457217', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009309', '5819236053864939521', '1895161541196709890', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1895161961143009310', '5819236053864939521', '1895161852346957825', '100001', '2025-02-28 01:19:57',
        '2025-02-28 01:19:56');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764289', '1709642621461942274', '2709640055336398850', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764290', '1709642621461942274', '1713968540587622402', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764291', '1709642621461942274', '1713968772394221570', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764292', '1709642621461942274', '1713969700325261314', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764293', '1709642621461942274', '1713969968467116033', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764294', '1709642621461942274', '1713982007906369538', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764295', '1709642621461942274', '1713982194359959553', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166730764296', '1709642621461942274', '1725323245752918018', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958594', '1709642621461942274', '1725323486329806849', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958595', '1709642621461942274', '1713974993499160577', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958596', '1709642621461942274', '1713975135413436417', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958597', '1709642621461942274', '2709640055336398851', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958598', '1709642621461942274', '1713979498676584450', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958599', '1709642621461942274', '1725323599773147138', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958600', '1709642621461942274', '1713968978506514433', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958601', '1709642621461942274', '1713972407501029377', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958602', '1709642621461942274', '1713986036686356481', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958603', '1709642621461942274', '1725323795416457217', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958604', '1709642621461942274', '2709640055336398852', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958605', '1709642621461942274', '5339640055336398841', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958606', '1709642621461942274', '5339640055336398842', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958607', '1709642621461942274', '5339640055336398843', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958608', '1709642621461942274', '53396400553363988434', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958609', '1709642621461942274', '1895161541196709890', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958610', '1709642621461942274', '1895161852346957825', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958611', '1709642621461942274', '1811752663585009665', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958613', '1709642621461942274', '1809493209816670210', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991559166734958614', '1709642621461942274', '1809493416335810562', '100001', '2025-11-21 01:28:01',
        '2025-11-21 01:28:01');
COMMIT;


DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`
(
    `id`            char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NOT NULL COMMENT '权限ID',
    `role_id`       bigint                                                       NOT NULL COMMENT '角色ID',
    `permission_id` bigint                                                       NOT NULL COMMENT '权限ID',
    `creator`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `roleid_id` (`role_id` ASC) USING BTREE COMMENT '角色ID',
    INDEX `permissionid_id` (`permission_id` ASC) USING BTREE COMMENT '权限ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色—权限表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485186', 1739350468044570626, 1726670680191299589, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485187', 1739350468044570626, 1726670680191299655, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485188', 1739350468044570626, 1726670680191299643, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485189', 1739350468044570626, 1726670680191299615, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485190', 1739350468044570626, 1726670680191299637, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485191', 1739350468044570626, 1726670680191299653, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485192', 1739350468044570626, 1726670680191299610, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485193', 1739350468044570626, 1726670680191299651, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485194', 1739350468044570626, 1726670680191299601, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485195', 1739350468044570626, 1726670680191299642, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485196', 1739350468044570626, 1726670680191299652, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485197', 1739350468044570626, 1726670680187105287, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485198', 1739350468044570626, 1726670680191299620, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485199', 1739350468044570626, 1726670680191299618, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485200', 1739350468044570626, 1726670680191299647, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485201', 1739350468044570626, 1726670680187105298, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485202', 1739350468044570626, 1726670680191299646, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485203', 1739350468044570626, 1726670680191299636, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739350468107485204', 1739350468044570626, 1726670680187105294, '100001', '2023-12-26 02:20:42',
        '2023-12-26 02:20:41');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739848802346278914', 1739848802325307393, 1726670680191299649, '100001', '2023-12-27 11:20:54',
        '2023-12-27 11:20:53');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739848802346278915', 1739848802325307393, 1726670680187105290, '100001', '2023-12-27 11:20:54',
        '2023-12-27 11:20:53');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739848802346278916', 1739848802325307393, 1726670680191299599, '100001', '2023-12-27 11:20:54',
        '2023-12-27 11:20:53');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739848802346278917', 1739848802325307393, 1726670680191299609, '100001', '2023-12-27 11:20:54',
        '2023-12-27 11:20:53');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1739848802346278918', 1739848802325307393, 1726670680191299602, '100001', '2023-12-27 11:20:54',
        '2023-12-27 11:20:53');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1911461651912273921', 5819236053864939525, 1726670680191299630, '100001', '2025-04-14 00:49:06',
        '2025-04-14 00:49:05');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1911461651912273922', 5819236053864939525, 1891252819399446530, '100001', '2025-04-14 00:49:06',
        '2025-04-14 00:49:05');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1911461651912273923', 5819236053864939525, 1726670680191299631, '100001', '2025-04-14 00:49:06',
        '2025-04-14 00:49:05');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1911461651912273924', 5819236053864939525, 1906759850713571336, '100001', '2025-04-14 00:49:06',
        '2025-04-14 00:49:05');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1911461651912273925', 5819236053864939525, 1906759850713571335, '100001', '2025-04-14 00:49:06',
        '2025-04-14 00:49:05');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163394', 1709959399039766529, 1726670680191299619, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163395', 1709959399039766529, 1809848498695327750, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163396', 1709959399039766529, 1809488069147664386, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163397', 1709959399039766529, 1726670680191299630, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163398', 1709959399039766529, 1726670680191299641, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163399', 1709959399039766529, 1891252819399446530, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163400', 1709959399039766529, 1726670680191299631, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163401', 1709959399039766529, 1809848498695327745, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163402', 1709959399039766529, 1809848498695327746, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163403', 1709959399039766529, 1809848498695327747, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163404', 1709959399039766529, 1726670680191299625, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163405', 1709959399039766529, 1809848498695327748, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163406', 1709959399039766529, 1885609255285997569, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163407', 1709959399039766529, 1809848498695327749, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163408', 1709959399039766529, 1843713190920417282, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163409', 1709959399039766529, 1964065910411202563, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163410', 1709959399039766529, 1964065910411202562, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163411', 1709959399039766529, 1964065910411202561, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163412', 1709959399039766529, 1964065910411202564, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163413', 1709959399039766529, 1885609255285997570, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163414', 1709959399039766529, 1906759850713571336, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1964066200271163415', 1709959399039766529, 1906759850713571335, '100001', '2025-09-06 04:40:47',
        '2025-09-06 04:40:47');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886529', 1709642621461942274, 1726670680191299616, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886530', 1709642621461942274, 1726670680191299615, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886531', 1709642621461942274, 1726670680191299659, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886532', 1709642621461942274, 1726670680187105283, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886533', 1709642621461942274, 1726670680187105284, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886534', 1709642621461942274, 1726670680191299597, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886535', 1709642621461942274, 1726670680191299652, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886536', 1709642621461942274, 1726670680191299653, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886537', 1709642621461942274, 1726670680191299611, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886538', 1709642621461942274, 1726670680191299633, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886539', 1709642621461942274, 1726670680191299614, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886540', 1709642621461942274, 1906759850713571333, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886541', 1709642621461942274, 1906759850713571330, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886542', 1709642621461942274, 1726670680191299660, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886543', 1709642621461942274, 1728106439295246337, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886544', 1709642621461942274, 1726670680187105297, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886545', 1709642621461942274, 1906759850713571336, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886546', 1709642621461942274, 1906759850713571335, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886547', 1709642621461942274, 1726670680191299605, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886548', 1709642621461942274, 1726670680191299604, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886549', 1709642621461942274, 1726670680191299648, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886550', 1709642621461942274, 1726670680191299628, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886551', 1709642621461942274, 1726670680187105292, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886552', 1709642621461942274, 1726670680191299609, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886553', 1709642621461942274, 1726670680187105293, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886554', 1709642621461942274, 1726670680187105294, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886555', 1709642621461942274, 1726670680191299587, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886556', 1709642621461942274, 1726670680191299642, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886557', 1709642621461942274, 1726670680191299644, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886558', 1709642621461942274, 1726670680191299624, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886559', 1709642621461942274, 1726670680191299646, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886560', 1709642621461942274, 1851677126001139713, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886561', 1709642621461942274, 1726670680191299590, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886562', 1709642621461942274, 1726670680191299594, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886563', 1709642621461942274, 1726670680187105285, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870468886564', 1709642621461942274, 1726670680187105286, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `creator`, `create_time`, `update_time`)
VALUES ('1991695870477275137', 1709642621461942274, 1726670680187105288, '100001', '2025-11-21 10:31:14',
        '2025-11-21 10:31:14');
COMMIT;

DROP TABLE IF EXISTS `sys_secure_invoke_record`;
CREATE TABLE `sys_secure_invoke_record`
(
    `id`                 bigint UNSIGNED                                       NOT NULL AUTO_INCREMENT COMMENT 'id',
    `secure_invoke_json` json                                                  NOT NULL COMMENT '请求快照参数json',
    `status`             tinyint                                               NOT NULL COMMENT '状态 1待执行 2已失败',
    `next_retry_time`    datetime(3)                                           NOT NULL COMMENT '下一次重试的时间',
    `retry_times`        int                                                   NOT NULL COMMENT '已经重试的次数',
    `max_retry_times`    int                                                   NOT NULL COMMENT '最大重试次数',
    `fail_reason`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '执行失败的堆栈',
    `update_time`        datetime(3)                                           NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
    `create_time`        datetime(3)                                           NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_next_retry_time` (`next_retry_time` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 9877
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '本地消息表'
  ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`                varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci             NOT NULL,
    `username`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci             NOT NULL COMMENT '用户名',
    `password`          char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci               NULL     DEFAULT NULL COMMENT '密码',
    `email`             varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci             NULL     DEFAULT NULL COMMENT '邮箱',
    `phone`             varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci             NULL     DEFAULT NULL COMMENT '手机号',
    `nickname`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci             NOT NULL COMMENT '昵称',
    `gender`            enum ('男','女','保密') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '保密' COMMENT '性别',
    `avatar`            varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci            NULL     DEFAULT 'default.png' COMMENT '头像',
    `birthday`          datetime                                                                 NULL     DEFAULT NULL COMMENT '生日',
    `user_type`         int                                                                      NOT NULL DEFAULT 0 COMMENT '用户类型(0前台、1后台)',
    `create_time`       datetime                                                                 NULL     DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime                                                                 NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `last_login_time`   datetime                                                                 NULL     DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci             NULL     DEFAULT NULL COMMENT '最后登录ip',
    `status`            int                                                                      NOT NULL DEFAULT 1 COMMENT '用户状态',
    `is_email_verified` int                                                                      NOT NULL DEFAULT 0 COMMENT '是否邮箱验证',
    `is_phone_verified` int                                                                      NOT NULL DEFAULT 0 COMMENT '是否手机号验证',
    `slogan`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci            NULL     DEFAULT NULL COMMENT '个性签名',
    `active_status`     bigint                                                                   NULL     DEFAULT NULL COMMENT '是否登录',
    PRIMARY KEY (`id`, `username`) USING BTREE,
    UNIQUE INDEX `username` (`username` ASC) USING BTREE,
    UNIQUE INDEX `email` (`email` ASC) USING BTREE,
    UNIQUE INDEX `phone` (`phone` ASC) USING BTREE,
    INDEX `user_password_index` (`password` ASC) USING BTREE,
    INDEX `user_username_index` (`username` ASC) USING BTREE,
    INDEX `user_email_index` (`email` ASC) USING BTREE,
    INDEX `user_phone_index` (`phone` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `username`, `password`, `email`, `phone`, `nickname`, `gender`, `avatar`, `birthday`,
                        `user_type`, `create_time`, `update_time`, `last_login_time`, `last_login_ip`, `status`,
                        `is_email_verified`, `is_phone_verified`, `slogan`, `active_status`)
VALUES ('100001', 'superAdmin', '$2a$10$wYCg65sqlRmNa0hPnN3Ue.xK/pxEVB9vi8MWe42U3nBgBBDWwpN1q', NULL, NULL,
        '超级管理员', '男', 'image/2025-08-30/f31e4890-5196-4e61-9970-4aeb27e84571', NULL, 1, '2025-05-02 11:29:50',
        '2025-11-23 02:08:44', '2025-11-23 01:37:39', '', 1, 0, 0, '', 1);
COMMIT;

DROP TABLE IF EXISTS `sys_user_address`;
CREATE TABLE `sys_user_address`
(
    `id`          varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'id',
    `name`        varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '收货人',
    `user_id`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '用户id',
    `is_default`  int                                                           NOT NULL DEFAULT 0 COMMENT '是否默认',
    `province`    varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '省份',
    `city`        varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '城市',
    `county`      varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '区/县',
    `address`     varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
    `postal_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '邮编',
    `phone`       varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '手机号',
    `create_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `address_user_id` (`user_id` ASC) USING BTREE,
    INDEX `address_phone` (`phone` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收货地址表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `id`          char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NOT NULL,
    `user_id`     char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NOT NULL COMMENT '用户ID',
    `role_id`     char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NOT NULL COMMENT '角色ID',
    `creator`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
    `create_time` datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_userid_roleid` (`user_id` ASC, `role_id` ASC) USING BTREE COMMENT '用户角色唯一',
    INDEX `userid_id` (`user_id` ASC) USING BTREE COMMENT '用户ID',
    INDEX `role_id` (`role_id` ASC) USING BTREE COMMENT '角色ID'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色关联表'
  ROW_FORMAT = DYNAMIC;
-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `creator`, `create_time`, `update_time`)
VALUES ('4032058398348884480', '100001', '5819236053864939521', '100001', '2023-05-06 01:44:29', '2023-08-24 14:39:33');
DROP TABLE IF EXISTS `sys_user_salt`;
CREATE TABLE `sys_user_salt`
(
    `user_id` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NOT NULL,
    `salt`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户密码盐值',
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户密钥表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_salt
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_salt` (`user_id`, `salt`)
VALUES ('100001', '/eaV/OB2FaO4KQ==');
COMMIT;

-- ----------------------------
-- Table structure for user_bills
-- ----------------------------
DROP TABLE IF EXISTS `user_bills`;
CREATE TABLE `user_bills`
(
    `id`            char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '账单id',
    `user_id`       char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '用户id',
    `orders_id`     char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '订单id',
    `voucher_id`    char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '代金卷id',
    `amount`        decimal(10, 2)                                             NOT NULL COMMENT '收支额度',
    `title`         char(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '日常消费' COMMENT '消费类型名称',
    `type`          int                                                        NOT NULL DEFAULT 0 COMMENT '收支类型，0:支出 1:收入',
    `currency_type` int                                                        NOT NULL DEFAULT 0 COMMENT '类型，0:金钱,1:积分',
    `create_time`   datetime                                                   NULL     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime                                                   NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `user_id_i` (`user_id` ASC) USING BTREE,
    INDEX `orders_id_i` (`orders_id` ASC) USING BTREE,
    INDEX `voucher_id_i` (`voucher_id` ASC) USING BTREE,
    INDEX `create_time_i` (`create_time` DESC) USING BTREE,
    INDEX `type_i` (`type` ASC, `currency_type` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户账单表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_recharge_combo
-- ----------------------------
DROP TABLE IF EXISTS `user_recharge_combo`;
CREATE TABLE `user_recharge_combo`
(
    `id`          int                                                          NOT NULL AUTO_INCREMENT,
    `name`        varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '套餐名称',
    `discount`    float                                                        NOT NULL DEFAULT 0 COMMENT '折扣',
    `amount`      decimal(10, 2)                                               NOT NULL COMMENT '充值额度',
    `points`      bigint                                                       NOT NULL DEFAULT 0 COMMENT '送积分',
    `create_time` datetime                                                     NULL     DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 10020
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '充值套餐表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_recharge_combo
-- ----------------------------
BEGIN;
INSERT INTO `user_recharge_combo` (`id`, `name`, `discount`, `amount`, `points`, `create_time`, `update_time`)
VALUES (10011, '充值50元送200积分', 1, 50.00, 200, '2023-05-06 01:45:31', '2023-05-06 01:45:31');
INSERT INTO `user_recharge_combo` (`id`, `name`, `discount`, `amount`, `points`, `create_time`, `update_time`)
VALUES (10012, '充值100元送500积分', 1, 100.00, 500, '2023-05-06 01:45:31', '2023-05-06 01:45:31');
INSERT INTO `user_recharge_combo` (`id`, `name`, `discount`, `amount`, `points`, `create_time`, `update_time`)
VALUES (10013, '充值200元送1000积分', 1, 200.00, 1000, '2023-05-06 01:45:31', '2023-05-06 01:45:31');
INSERT INTO `user_recharge_combo` (`id`, `name`, `discount`, `amount`, `points`, `create_time`, `update_time`)
VALUES (10014, '充值500元送2000积分', 1, 500.00, 2000, '2023-05-06 01:45:31', '2023-05-06 01:45:31');
INSERT INTO `user_recharge_combo` (`id`, `name`, `discount`, `amount`, `points`, `create_time`, `update_time`)
VALUES (10016, '充值1000元送9000积分', 1, 1000.00, 9000, '2023-09-28 17:57:50', '2023-09-29 03:07:12');
COMMIT;

DROP TABLE IF EXISTS `user_wallet`;
CREATE TABLE `user_wallet`
(
    `user_id`     char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
    `balance`     decimal(10, 2)                                            NOT NULL COMMENT '余额',
    `recharge`    decimal(10, 2)                                            NOT NULL COMMENT '充值总额',
    `spend`       decimal(10, 2)                                            NOT NULL COMMENT '消费总额',
    `points`      bigint                                                    NOT NULL DEFAULT 0 COMMENT '总积分',
    `create_time` datetime                                                  NULL     DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime                                                  NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`) USING BTREE,
    INDEX `user_wallet_index` (`balance` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户钱包表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- chat_message_reaction 消息表情反应表 (2026-02-17)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `chat_message_reaction` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `msg_id`      bigint       NOT NULL COMMENT '消息ID',
    `room_id`     bigint       NOT NULL COMMENT '房间ID',
    `user_id`     varchar(20)  NOT NULL COMMENT '用户ID',
    `emoji_type`  varchar(32)  NOT NULL COMMENT 'emoji编码（对应枚举值）',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_msg_user_emoji` (`msg_id`, `user_id`, `emoji_type`),
    KEY `idx_msg_id` (`msg_id`),
    KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息emoji反应表';

-- ----------------------------
-- 上下文查询与消息内容字段 (add_context_indexes + update_chat_message)
-- ----------------------------
ALTER TABLE `chat_message` ADD INDEX `idx_chat_message_context` (`room_id`, `type`, `status`, `id`);
ALTER TABLE `chat_message` ADD INDEX `idx_chat_message_time` (`room_id`, `create_time`, `type`, `status`);
ALTER TABLE `chat_message` MODIFY COLUMN `content` MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容';

SET FOREIGN_KEY_CHECKS = 1;

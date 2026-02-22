package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.constant.PermissionConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.main.mapper.sys.MenuMapper;
import com.jiwu.api.common.main.mapper.sys.RoleMenuMapper;
import com.jiwu.api.common.main.pojo.sys.Menu;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.jiwu.api.common.main.pojo.sys.RoleMenu;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertMenuDTO;
import com.jiwu.api.user.common.dto.SelectMenuListDTO;
import com.jiwu.api.user.common.dto.UpdateMenuDTO;
import com.jiwu.api.user.common.vo.MenuTreeVO;
import com.jiwu.api.user.service.AdminUserMenuService;
import com.jiwu.api.user.service.AdminUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理员系统服务
 *
 * @className: AdminServiceImpl
 * @author: Kiwi23333
 * @description: 管理员系统服务
 * @date: 2023/8/25 14:44
 */
@Service
@Slf4j
public class AdminMenuServiceImpl implements AdminUserMenuService {

    @Resource
    private RedisUtil<String, Object> redisUtil;
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private PermissionConstant permissionConstant;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 获取菜单树
     *
     * @return 菜单列表
     */
    @Override
    public Result<List<MenuTreeVO>> getMenuTree() {
        // 1、读取旧缓存
        Object redis = redisUtil.hGet(PermissionConstant.SYS_ALL_ROLE_MENU_MAP, "tree");
        if (redis != null) {
            return Result.ok((List<MenuTreeVO>) redis);
        }
        // 2、sql
        List<Menu> list = menuMapper.selectList(new LambdaQueryWrapper<Menu>().orderByDesc(Menu::getSortOrder));
        List<MenuTreeVO> tree = MenuTreeVO.buildTree(list.stream().map(MenuTreeVO::toMenuTreeVO).collect(Collectors.toList()));
        // 缓存
        if (!tree.isEmpty()) {
            redisUtil.hPut(PermissionConstant.SYS_ALL_ROLE_MENU_MAP, "tree", tree, 1, TimeUnit.HOURS);
        }
        return Result.ok(tree);
    }

    /**
     * 添加菜单
     *
     * @param dto 参数
     * @return 影响行
     */
    @Override
    public Result<Integer> addMenuPage(InsertMenuDTO dto) {

        // 1、父菜单是否存在
        if (StringUtils.isNotBlank(dto.getParentId()) && !menuMapper.exists(new LambdaQueryWrapper<Menu>()
                .select(Menu::getId)
                .eq(Menu::getId, dto.getParentId())
                .last("LIMIT 1"))) {
            return Result.fail(ResultStatus.LINK_NULL_ERR.getCode(), "父菜单项不存在！");
        }
        // 2、插入
        if (menuMapper.insert(InsertMenuDTO.toMenu(dto)) != 1) {
            return Result.fail(ResultStatus.INSERT_ERR.getCode(), "添加失败，请稍后再试！");
        }
        // 3、清除菜单缓存
        permissionConstant.delUserMenuMap();
        return Result.ok("添加成功！", 1);
    }

    /**
     * 删除菜单（批量）
     *
     * @param ids id集合
     * @return 影响行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> batchDelMenuPage(List<String> ids) {
        ids = ids.stream().distinct().collect(Collectors.toList());
        // 1、del
        if (menuMapper.deleteBatchIds(ids) != ids.size()) {
            throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "删除失败，部分不存在！");
        }
        // 2、删除附属 => 角色-关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().in(RoleMenu::getMenuId, ids));
        // 3、清除菜单缓存
        permissionConstant.delUserMenuMap();
        permissionConstant.delUserRoleMap();
        return Result.ok("删除成功！", ids.size());
    }

    /**
     * 绑定角色-菜单
     *
     * @param roleId  角色id
     * @param ids     绑定列表
     * @param adminId 创建人
     * @return 影响行
     */
    @Override
    public Result<Integer> bindRoleMenu(String roleId, List<String> ids, String adminId) {
        // 1、过滤
        ids = ids.stream().distinct().collect(Collectors.toList());
        if (ids.size() > 100) {
            return Result.fail(ResultStatus.PARAM_ERR.getCode(), "绑定菜单数不能超过100个！");
        }
        // 查询ids有效菜单个数
        Long checkMenu = menuMapper.selectCount(new LambdaQueryWrapper<Menu>().in(Menu::getId, ids));
        if (checkMenu != ids.size()) {
            return Result.fail(ResultStatus.PARAM_ERR.getCode(), "参数错误，请填写正确的菜单id！");
        }
        // 2、开启事务
        List<String> bindIds = ids;
        return transactionTemplate.execute(action -> {
            // 2、删除旧菜单
            int delCount = roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));

            if (bindIds.isEmpty()) {
                return Result.ok("修改成功，用户已解绑！", delCount);
            }
            // 3、构建新映射关系
            List<RoleMenu> newList = new ArrayList<>();
            for (String menuId : bindIds) {
                newList.add(new RoleMenu()
                        .setRoleId(roleId)
                        .setMenuId(menuId)
                        .setCreator(adminId)
                );
            }
            // 4、批量新增
            int insertSize = roleMenuMapper.insertBatchSomeColumn(newList);
            if (insertSize != bindIds.size()) {
                throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "部分添加失败，稍后再试！");
            }
            // 3、清除菜单角色缓存
            permissionConstant.delUserMenuMap(roleId, adminId);
            return Result.ok("绑定成功！", bindIds.size());
        });


    }

    /**
     * 获取菜单列表
     *
     * @param dto 参数
     * @return 列表
     */
    @Override
    public Result<List<Menu>> getMenuList(SelectMenuListDTO dto) {

        if (StringUtils.isNotBlank(dto.getRoleId())) {
            return this.selectRoleMenu(dto);
        }
        LambdaQueryWrapper<Menu> qw = new LambdaQueryWrapper<>(); // 创建查询条件
        // 查询条件
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            qw.or().like(Menu::getName, dto.getKeyword());
            qw.or().like(Menu::getCode, dto.getKeyword());
        }
        if (dto.getParentId() != null) {
            qw.eq(true, Menu::getParentId, dto.getParentId());
        }
        if (dto.getCode() != null) {
            qw.or().eq(true, Menu::getCode, dto.getCode());
        }
        // 时间排序
        if (dto.getTimeSort() != null) {
            qw.orderBy(true, dto.getTimeSort() == 0, Menu::getCreateTime);
        }
        qw.orderByDesc(true, Menu::getSortOrder);

        // 转化
        return Result.ok(menuMapper.selectList(qw));
    }

    @Autowired
    private AdminUserRoleService roleService;

    /**
     * 获取用户的菜单列表
     *
     * @param userId 用户id
     * @return 列表
     */
    @Override
    public Result<List<Menu>> getMenuList(String userId) {
        // 1、获取用户角色列表
//        Object redis = redisUtil.hGet(PermissionConstant.SYS_ALL_ROLE_MENU_MAP, "userId:" + userId);
//        if (redis != null) {
//            return Result.ok((List<Menu>) redis);
//        }
        List<String> rids = roleService.selectUserRoleList(userId).stream().map(Role::getId).collect(Collectors.toList());
        if (rids.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }
        // 2、获取用户菜单
        MPJLambdaWrapper<RoleMenu> qw = new MPJLambdaWrapper<>();
        qw.selectAll(Menu.class)
                .join("JOIN", Menu.class, Menu::getId, RoleMenu::getMenuId)
                .in(RoleMenu::getRoleId, rids);
        // 3、获取成功
        List<Menu> list = roleMenuMapper.selectJoinList(Menu.class, qw);
        // 4、缓存
        redisUtil.hPut(PermissionConstant.SYS_ALL_ROLE_MENU_MAP, "userId:" + userId, list);
        list.sort(Comparator.comparingInt(Menu::getSortOrder).reversed());
        return Result.ok(list);
    }

    /**
     * 修改菜单
     *
     * @param id  菜单id
     * @param dto 参数
     * @return 影响行数
     */
    @Override
    public Result<Integer> updateMenuPage(String id, UpdateMenuDTO dto) {
        // 1、检验父id
        if (StringUtils.isNotBlank(dto.getParentId())) {
            if (id.equals(dto.getParentId())) {
                throw new BusinessException(ResultStatus.LINK_NULL_ERR.getCode(), "修改失败，父菜单id错误！");
            } else {
                if (menuMapper.selectOne(new LambdaQueryWrapper<Menu>().eq(Menu::getId, dto.getParentId()).select(Menu::getId)) == null) {
                    return Result.fail("修改失败，父菜单不存在！");
                }
            }
        }

        // 2、修改
        if (menuMapper.updateById(UpdateMenuDTO.toMenu(id, dto)) != 1) {
            return Result.fail(ResultStatus.UPDATE_ERR.getCode(), "修改失败，请稍后再试！");
        }

        // 3、清除菜单缓存
        permissionConstant.delUserMenuMap();
        return Result.ok(1);
    }


    /**
     * 查询角色下的菜单列表
     *
     * @param dto 参数
     * @return 列表
     */
    private Result<List<Menu>> selectRoleMenu(SelectMenuListDTO dto) {
        // 菜单
        MPJLambdaWrapper<RoleMenu> qw = new MPJLambdaWrapper<RoleMenu>()
                .selectAll(Menu.class);

        // 查询条件
        qw.eq(RoleMenu::getRoleId, dto.getRoleId());
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            qw.or().like(Menu::getName, dto.getKeyword());
            qw.or().like(Menu::getId, dto.getKeyword());
        }
        if (dto.getParentId() != null) {
            qw.eq(true, Menu::getParentId, dto.getParentId());
        }
        if (dto.getCode() != null) {
            qw.or().eq(true, Menu::getCode, dto.getCode());
        }
        // 时间排序
        if (dto.getTimeSort() != null) {
            qw.orderBy(true, dto.getTimeSort() == 0, Menu::getCreateTime);
        }
        qw.orderByDesc(true, Menu::getSortOrder);

        // 左链接
        qw.leftJoin(Menu.class, Menu::getId, RoleMenu::getMenuId);
        // 结果
        return Result.ok(roleMenuMapper.selectJoinList(Menu.class, qw));
    }


}

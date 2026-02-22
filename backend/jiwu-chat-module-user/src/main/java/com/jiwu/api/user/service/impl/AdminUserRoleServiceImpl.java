package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.main.mapper.sys.PermissionMapper;
import com.jiwu.api.common.main.mapper.sys.RolePermissionMapper;
import com.jiwu.api.common.main.pojo.sys.Permission;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.jiwu.api.common.main.pojo.sys.RolePermission;
import com.jiwu.api.common.main.pojo.sys.UserRole;
import com.jiwu.api.common.main.mapper.sys.RoleMapper;
import com.jiwu.api.common.main.mapper.sys.UserRoleMapper;
import com.jiwu.api.common.constant.PermissionConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.enums.UserType;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.user.common.dto.InsertRoleDTO;
import com.jiwu.api.user.common.dto.SelectPageRoleDTO;
import com.jiwu.api.user.common.dto.UpdateRoleDTO;
import com.jiwu.api.user.common.vo.RoleTreeVO;
import com.jiwu.api.user.common.dto.RoleDTO;
import com.jiwu.api.user.service.AdminUserRoleService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户角色表
 *
 * @className: UserSaltService
 * @author: Kiwi2333
 * @date: 2023/4/13 14:54
 */
@Service
@Slf4j
public class AdminUserRoleServiceImpl implements AdminUserRoleService {
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private PermissionConstant permissionConstant;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 添加用户用户角色关联（默认用户）
     *
     * @param userId 用户id
     * @return int 受影响行数
     */
    @Override
    public int addUserRoleCustomer(String userId) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getName, "普通用户")
                .eq(Role::getCode, "ROLE_CUSTOMER_DEFAULT")
                .last("LIMIT 1"));
        if (role == null) return 0;
        UserRole userRole = new UserRole();
        userRole.setUserId(userId).setRoleId(role.getId());
        return userRoleMapper.insert(userRole);
    }

    /**
     * 获取用户所有角色
     *
     * @param userId 用户id
     * @return 角色 code列表
     */
    @Override
    public List<String> selectUserRole(String userId) {
        Object redis = redisUtil.hGet(PermissionConstant.SYS_USER_ROLE_MAP, userId);
        if (redis != null) {
            return (List<String>) redis;
        }
        MPJLambdaWrapper<UserRole> qw = new MPJLambdaWrapper<UserRole>()
                .select(Role::getId, Role::getCode)
                .join("JOIN", Role.class, Role::getId, UserRole::getRoleId);
        qw.eq(UserRole::getUserId, userId);
        List<RoleDTO> list = userRoleMapper.selectJoinList(RoleDTO.class, qw);
        List<String> data = new ArrayList<>();
        if (!list.isEmpty()) {
            data = list.stream().map(RoleDTO::getCode).collect(Collectors.toList());
            redisUtil.hPut(PermissionConstant.SYS_USER_ROLE_MAP, userId, data);
        }
        return data;
    }

    /**
     * 获取用户所有角色列表
     *
     * @param userId 用户userId
     * @return 角色 code列表
     */
    @Override
    public List<Role> selectUserRoleList(String userId) {
        MPJLambdaWrapper<UserRole> qw = new MPJLambdaWrapper<UserRole>()
                .selectAll(Role.class)
                .join("JOIN", Role.class, Role::getId, UserRole::getRoleId);
        qw.eq(UserRole::getUserId, userId);
        return userRoleMapper.selectJoinList(Role.class, qw);
    }


    /**
     * 获取角色列表（分页）
     *
     * @param page 页
     * @param size 数
     * @param dto  筛选dto
     * @return 分页数据Result<IPage < Role>>
     */
    @Override
    public Result<IPage<Role>> getRolePage(int page, int size, SelectPageRoleDTO dto) {

        // 创建分页对象，指定当前页码和每页记录数
        Page<Role> pages = new Page<>(page, size);
        LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>(); // 创建查询条件
        // 查询条件
        if (dto.getName() != null) {
            qw.or().like(Role::getName, dto.getName());
            qw.or().like(Role::getIntro, dto.getName());
        }
        if (dto.getParentId() != null) {
            qw.or().eq(true, Role::getParentId, dto.getParentId());
        }
        if (dto.getCode() != null) {
            qw.or().eq(true, Role::getCode, dto.getCode());
        }
        // 时间排序
        if (dto.getTimeSort() != null) {
            qw.orderBy(true, dto.getTimeSort() == 0, Role::getCreateTime);
        }
        qw.orderByAsc(true, Role::getCode);
        return Result.ok(roleMapper.selectPage(pages, qw));
    }

    /**
     * 获取角色列表（树）
     *
     * @return 分页数据Result<List < Role>>
     */
    @Override
    public Result<List<RoleTreeVO>> getRoleTree() {
        // 1、读取旧缓存
        Object redis = redisUtil.hGet(PermissionConstant.SYS_USER_ROLE_MAP, "tree");
        if (redis != null) {
            return Result.ok((List<RoleTreeVO>) redis);
        }
        // 2、sql
        List<Role> list = roleMapper.selectList(null);
        List<RoleTreeVO> tree = RoleTreeVO.buildTree(list.stream().map(RoleTreeVO::toRoleTreeVO).collect(Collectors.toList()));
        // 缓存
        if (!tree.isEmpty()) {
            redisUtil.hPut(PermissionConstant.SYS_USER_ROLE_MAP, "tree", tree, 1, TimeUnit.HOURS);
        }
        return Result.ok(tree);
    }


    /**
     * 添加角色
     *
     * @param adminId 添加者权限
     * @param dto     参数
     * @return 行数
     */
    @Override
    public Result<Integer> addRolePage(String adminId, InsertRoleDTO dto) {
        // 1、生成role
        Role role = InsertRoleDTO.toRole(dto);
        // 创建人
        role.setCreator(adminId);
        // 父id不存在
        if (!StringUtil.isNullOrEmpty(dto.getParentId()) && !roleMapper.exists(new LambdaQueryWrapper<Role>().eq(Role::getId, dto.getParentId()))) {
            return Result.fail(ResultStatus.NULL_ERR.getCode(), "添加失败，父角色不存在！");
        }
        // 2、去重复权限
        Set<String> set = new HashSet<>(dto.getPermissionList());
        if (dto.getPermissionList().size() != set.size()) {
            return Result.fail(ResultStatus.INSERT_ERR.getCode(), "添加失败，权限有重复项！");
        }
        // 3、查询权限集合
        Long permissionSize = permissionMapper.selectCount(new LambdaQueryWrapper<Permission>().in(Permission::getId, dto.getPermissionList()));
        if (permissionSize != set.size()) {// 返回不存在列表
            return Result.fail(ResultStatus.NULL_ERR.getCode(), "添加失败，权限部分不存在！");
        }
        // a、开启事务
        return transactionTemplate.execute(action -> {
            // 4、添加角色
            if (roleMapper.insert(role) == 0) {
                throw new BusinessException(ResultStatus.INSERT_ERR.getCode(), "添加失败，请稍后再试！");
            }
            // 获取自动生成的主键id
            String roleId = role.getId();
            // 准备添加关联列表
            List<RolePermission> rolePermissionList = new ArrayList<>();
            for (String pid : dto.getPermissionList()) {
                rolePermissionList.add(new RolePermission()
                        .setCreator(adminId)
                        .setRoleId(roleId)
                        .setPermissionId(pid));
            }
            // 5、添加角色权限关联(批量)
            if (rolePermissionMapper.insertBatchSomeColumn(rolePermissionList) != set.size()) {
                throw new BusinessException(ResultStatus.INSERT_ERR.getCode(), "添加失败，请稍后再试！");
            }
            permissionConstant.delUserRoleMap();
            // 6、添加成功
            return Result.ok("添加成功！", set.size());
        });
    }


    /**
     * 批量删除角色（包括关联权限）
     *
     * @param ids 删除id列表
     * @return 删除行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> batchDelRolePage(List<String> ids) {
        // 1、检查是否在使用
        if (userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .in(UserRole::getRoleId, ids)) > 0) {
            throw new BusinessException(ResultStatus.LINK_NULL_ERR.getCode(), "删除失败，部分角色被使用！");
        }
        // 2、检查删除对象是否存在
        if (roleMapper.selectCount(new LambdaQueryWrapper<Role>().in(Role::getId, ids).notIn(Role::getCode, Arrays.asList(UserType.IGNORE_ROLES))) != ids.size()) {
            throw new BusinessException(ResultStatus.NULL_ERR.getCode(), "删除失败，部分角色不存在！");
        }
        // 3、批量删除
        if (roleMapper.deleteBatchIds(ids) != ids.size()) {
            throw new BusinessException(ResultStatus.NULL_ERR.getCode(), "删除失败，部分角色不存在！");
        }
        // 4、删除关联数据
        int rolePerMissionSize = rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, ids));
//        if (rolePerMissionSize == 0) {
//            throw new BusinessException(ResultStatus.NULL_ERR.getCode(), "角色权限关联删除失败，请稍后再试！");
//        }
        // 删除全部角色map
        permissionConstant.delUserRoleMap();
        permissionConstant.delPermissionMap();
        log.warn("删除角色警告{}", rolePerMissionSize);
        return Result.ok("删除成功！", ids.size());
    }

    /**
     * 修改角色
     *
     * @param adminId 角色id
     * @param roleId  更新roleId
     * @param dto     修改参数
     * @return 行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> updateRolePage(String adminId, String roleId, UpdateRoleDTO dto) {
        Role newRole = UpdateRoleDTO.toRole(dto).setId(roleId);
        Role oldRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getId, roleId)
                .select(Role::getId,Role::getCode));

        // 1、修改permission列表
        if (dto.getPermissionList() != null) {
            Set<String> newPermissionSet = new HashSet<>(dto.getPermissionList());
            if (newPermissionSet.size() != dto.getPermissionList().size()) {
                Result.fail("修改失败，权限列表数据中有重复项！");
            }
            // 1）删除原本映射关系
            rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
            // 2）构建新映射关系
            if (!newPermissionSet.isEmpty()) {
                List<RolePermission> list = new ArrayList<>();
                for (String pid : newPermissionSet) {
                    list.add(new RolePermission().setRoleId(roleId).setCreator(adminId).setPermissionId(pid));
                }
                rolePermissionMapper.insertBatchSomeColumn(list);
            }
        }
        // 3、更新
        if (roleMapper.updateById(newRole) != 1) {
            Result.fail(ResultStatus.UPDATE_ERR.getCode(), "修改失败，请稍后再试！");
        }

        // 删除角色
        permissionConstant.delUserRoleMap();
        // 删除角色对应权限的缓存
        permissionConstant.delPermissionMap(roleId);
        permissionConstant.delPermissionMap(oldRole.getCode());
        return Result.ok("修改成功！", 1);
    }

    /**
     * 用户绑定角色
     *
     * @param userId  用户id
     * @param ids     角色id列表
     * @param adminId 创建人id
     * @return 绑定数
     */
    @Override
    public Result<Integer> addUserRoleBind(String userId, List<String> ids, String adminId) {
        // 1、过滤
        if (ids.size() > 25) {
            return Result.fail(ResultStatus.PARAM_ERR.getCode(), "绑定角色数不能超过25个！");
        }
        // 查询ids有效角色个数
        Long validSize = roleMapper.selectCount(new LambdaQueryWrapper<Role>().in(Role::getId, ids));
        if (validSize != ids.size()) {
            return Result.fail(ResultStatus.PARAM_ERR.getCode(), "参数错误，请填写正确的角色id！");
        }
        // 2、开启事务
        return transactionTemplate.execute(action -> {
            // 2、删除旧角色
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
            if (ids.isEmpty()) {
                return Result.ok("修改成功，用户已解绑！", 0);
            }
            // 3、构建新映射关系
            List<UserRole> newList = new ArrayList<>();
            for (String roleId : ids) {
                newList.add(new UserRole()
                        .setUserId(userId)
                        .setRoleId(roleId)
                        .setCreator(adminId)
                );
            }

            int insertSize = userRoleMapper.insertBatchSomeColumn(newList);
            if (insertSize != ids.size()) {
                throw new BusinessException(ResultStatus.DEFAULT_ERR.getCode(), "部分");
            }
            // 清除用户权限缓存
            permissionConstant.delUserRoleMap(userId);
            return Result.ok("绑定成功！", insertSize);
        });
    }

}

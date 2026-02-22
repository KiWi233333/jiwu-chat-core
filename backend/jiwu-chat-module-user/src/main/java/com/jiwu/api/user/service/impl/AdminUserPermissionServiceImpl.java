package com.jiwu.api.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.constant.PermissionConstant;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.user.common.dto.InsertPermissionDTO;
import com.jiwu.api.user.common.dto.SelectPagePermissionDTO;
import com.jiwu.api.user.common.dto.SelectPermissionDTO;
import com.jiwu.api.user.common.dto.UpdatePermissionDTO;
import com.jiwu.api.user.common.vo.ValidPermissionVO;
import com.jiwu.api.user.common.dto.PermissionInfoDTO;
import com.jiwu.api.user.service.AdminUserPermissionService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.jiwu.api.common.main.mapper.sys.PermissionMapper;
import com.jiwu.api.common.main.mapper.sys.RolePermissionMapper;
import com.jiwu.api.common.main.mapper.sys.UserRoleMapper;
import com.jiwu.api.common.main.pojo.sys.Permission;
import com.jiwu.api.common.main.pojo.sys.Role;
import com.jiwu.api.common.main.pojo.sys.RolePermission;
import com.jiwu.api.common.main.pojo.sys.UserRole;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 用户权限业务
 *
 * @className: UserSaltService
 * @author: Kiwi2333
 * @date: 2023/4/13 14:54
 */
@Service
public class AdminUserPermissionServiceImpl implements AdminUserPermissionService {


    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PermissionConstant permissionConstant;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private WebApplicationContext context;

    /**
     * 获取用户权限列表
     *
     * @param roleCode 权限id
     * @return 权限列表
     */
    @Override
    public List<String> selectUserPermission(String roleCode) {
        // 1、缓存
        Object redisData = redisUtil.hGet(PermissionConstant.SYS_ALL_ROLE_PERMISSION_MAP, roleCode);
        if (redisData != null) {
            return (List<String>) redisData;
        }
        // 2、sql
        MPJLambdaWrapper<UserRole> qw = new MPJLambdaWrapper<UserRole>()
                // 权限code
                .select(Permission::getCode, Permission::getName)
                .eq(Role::getCode, roleCode)
                .join("JOIN", Role.class, Role::getId, UserRole::getRoleId)
                .join("JOIN", RolePermission.class, RolePermission::getRoleId, Role::getId)
                .join("JOIN", Permission.class, Permission::getId, RolePermission::getPermissionId);
        List<PermissionInfoDTO> list = userRoleMapper.selectJoinList(PermissionInfoDTO.class, qw);
        try {
            List<String> data = list.stream().map(PermissionInfoDTO::getCode).collect(Collectors.toList());
            // 3、缓存
            redisUtil.hPut(PermissionConstant.SYS_ALL_ROLE_PERMISSION_MAP, roleCode, data);
            return data;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    /**
     * 初始化权限注解（自动新增到数据库）
     *
     * @return 新增内容列表
     */
    @Override
    public List<Permission> initPermission() {
        Set<String> set = new HashSet<>();
        // 先查询数据库 找到我们所有的权限放到Set中
        List<Permission> permissions = permissionMapper.selectList(null);
        if (permissions != null) {
            permissions.forEach((permission) -> {
                set.add(permission.getCode());
            });
        }
        //这里我们编写保存方法
        //首先我们得先拿到我们 Controller的方法 在拿到我们方法上面的注解
        //我们的容器已经有了，就在 RequestMappingHandlerMapping 里面，它扫描了全部的controller 方法
        //这也是为什么拦截器能找到我们对应的方法路径
        RequestMappingHandlerMapping handlerMapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = handlerMapping.getHandlerMethods();
        //我们拿到所有的value也就是 methods
        Collection<HandlerMethod> methods = handlerMethodMap.values();
        //得到方法上的注解  标记我们想要的注解
        List<Permission> permissionsList = new ArrayList<>();
        for (HandlerMethod method : methods) {
            //判断每一个方法上有没有这个注解，如果没有就跳过
            ReqPermission requestPermission = method.getMethodAnnotation(ReqPermission.class);
            if (requestPermission == null)
                continue;
            String permissionCode = requestPermission.expression();
            String name = requestPermission.name();
            String intro = StringUtil.isNullOrEmpty(requestPermission.intro()) ? name : requestPermission.intro();
            //为了防止 重复存入，我们使用Set集合 的无序不可重复 特性存储
            //能到这里说明是有值的，那我就拿出来，存到数据库中
            if (set.contains(permissionCode))
                continue;    // 如果我们存过了 那就直接跳过
            //把这个 表达式 存到 set里面
            set.add(permissionCode);
            permissionsList.add(new Permission().setName(name)
                    .setIntro(intro)
                    .setCode(permissionCode)
                    .setCreator("SUPER_ADMIN"));
        }
        if (!permissionsList.isEmpty()) {
            permissionMapper.insertBatchSomeColumn(permissionsList);
        }
        return permissionsList;
    }


    /**
     * 获取权限列表（分页）
     *
     * @param page 页
     * @param size 数
     * @param dto  筛选dto
     * @return 分页数据Result<IPage < List < Permission>>>
     */
    @Override
    public IPage<Permission> getPermissionPage(int page, int size, SelectPagePermissionDTO dto) {
        if (StringUtil.isNullOrEmpty(dto.getRoleId())) {
            // 创建分页对象，指定当前页码和每页记录数
            Page<Permission> pages = new Page<>(page, size);
            LambdaQueryWrapper<Permission> qw = new LambdaQueryWrapper<>(); // 创建查询条件
            // 查询条件
            if (dto.getName() != null) {
                qw.or().like(Permission::getName, dto.getName());
                qw.or().like(Permission::getIntro, dto.getName());
            }
            if (dto.getCode() != null) {
                qw.or().like(true, Permission::getCode, dto.getCode());
            }
            if (dto.getCreator() != null) {
                qw.or().like(true, Permission::getCreator, dto.getCreator());
            }
            if (dto.getParentId() != null) {
                qw.or().eq(true, Permission::getParentId, dto.getParentId());
            }
            // 时间排序
            if (dto.getTimeSort() != null) {
                qw.orderBy(true, dto.getTimeSort() == 0, Permission::getCreateTime);
            }
            qw.orderByAsc(true, Permission::getCode);
            return permissionMapper.selectPage(pages, qw);
        } else {
            LambdaQueryWrapper<Permission> qw = new LambdaQueryWrapper<>();
            // 分页器
            Page<RolePermission> pages = new Page<>(page, size);
            // 查询条件
            if (dto.getName() != null) {
                qw.or().like(Permission::getName, dto.getName());
                qw.or().like(Permission::getIntro, dto.getName());
            }
            if (dto.getCode() != null) {
                qw.or().eq(true, Permission::getCode, dto.getCode());
            }
            if (dto.getCreator() != null) {
                qw.or().eq(true, Permission::getCreator, dto.getCreator());
            }
            if (dto.getParentId() != null) {
                qw.or().eq(true, Permission::getParentId, dto.getParentId());
            }
            // 时间排序
            if (dto.getTimeSort() != null) {
                qw.orderBy(true, dto.getTimeSort() == 0, Permission::getCreateTime);
            }
            // 角色id分页查询
            Page<RolePermission> rpPage = rolePermissionMapper.selectPage(pages,
                    new LambdaQueryWrapper<RolePermission>()
                            .eq(RolePermission::getRoleId, dto.getRoleId()));
            // 新分页
            Page<Permission> pPage = new Page<>(page, size);
            pPage.setTotal(rpPage.getTotal())
                    .setPages(rpPage.getPages())
                    .setCurrent(rpPage.getCurrent())
                    .setSize(rpPage.getSize());
            // 关联有数据
            if (!rpPage.getRecords().isEmpty()) {
                List<Permission> list = permissionMapper.selectList(qw
                        .in(Permission::getId, rpPage.getRecords().stream()
                                .map(RolePermission::getPermissionId)
                                .collect(Collectors.toList())));
                pPage.setRecords(list);
            }
            return pPage;
        }
    }

    /**
     * 修改权限
     *
     * @param id  权限id
     * @param dto 参数
     * @return 成功条数
     */
    @Override
    public Integer updatePermissionPage(String id, UpdatePermissionDTO dto) {
        // 1、sql
        if (dto.getCode() != null && !checkCodeValid(dto.getCode())) {
            throw new BusinessException("修改失败，不存在该权限资源！");
        }
        // 2、修改
        if (permissionMapper.updateById(UpdatePermissionDTO.toPermission(dto).setId(id)) == 0) {
            throw new BusinessException("修改失败，稍后再试！");
        }
        // 3、删除缓存
        permissionConstant.delPermissionMap();
        return 1;
    }

    /**
     * 添加权限
     *
     * @param adminId 管理员id（添加者）
     * @param dto     参数
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addPermissionPage(String adminId, InsertPermissionDTO dto) {
        // 1、查询是否存在
        if (permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getCode, dto.getCode())
                .last("LIMIT 1")) != null) {
            throw new BusinessException("权限已经存在，权限码冲突！");
        }
        if (!checkCodeValid(dto.getCode())) {
            throw new BusinessException("添加失败，不存在该权限资源！");
        }
        // 2、sql
        Permission permission = InsertPermissionDTO.toPermission(dto);
        permission.setCreator(adminId);
        if (permissionMapper.insert(permission) == 0) {
            throw new BusinessException("添加失败，稍后再试！");
        }
        // 父id
        if (!StringUtil.isNullOrEmpty(dto.getParentId()) && dto.getParentId().equals(permission.getId())) {
            throw new BusinessException(ResultStatus.LINK_NULL_ERR.getCode(), "添加失败，父id不能为自身！");
        }
        return 1;
    }

    /**
     * 验证是否存在该权限资源
     *
     * @param code 权限code
     * @return bol
     */
    private boolean checkCodeValid(String code) {
        RequestMappingHandlerMapping handlerMapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = handlerMapping.getHandlerMethods();
        //我们拿到所有的value也就是 methods
        Collection<HandlerMethod> methods = handlerMethodMap.values();
        for (HandlerMethod method : methods) {
            //判断每一个方法上有没有这个注解，如果没有就跳过
            ReqPermission requestPermission = method.getMethodAnnotation(ReqPermission.class);
            if (requestPermission == null)
                continue;
            if (requestPermission.expression().equals(code)) {
                return true;
            }
        }
        return false;
    }


    private Map<String, ValidPermissionVO> getPermissionCodeSet() {
        RequestMappingHandlerMapping handlerMapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = handlerMapping.getHandlerMethods();
        //我们拿到所有的value也就是 methods
        Collection<HandlerMethod> methods = handlerMethodMap.values();
        //得到方法上的注解  标记我们想要的注解
        Map<String, ValidPermissionVO> res = new HashMap<>();
        for (HandlerMethod method : methods) {
            //判断每一个方法上有没有这个注解，如果没有就跳过
            ReqPermission requestPermission = method.getMethodAnnotation(ReqPermission.class);
            if (requestPermission == null)
                continue;
            String permissionCode = requestPermission.expression();
            //为了防止 重复存入，我们使用Set集合 的无序不可重复 特性存储
            //能到这里说明是有值的，那我就拿出来，存到数据库中
            if (res.containsKey(permissionCode))
                continue;    // 如果我们存过了 那就直接跳过
            //把这个 表达式 存到 set里面
            res.put(permissionCode,
                    new ValidPermissionVO()
                            .setCode(requestPermission.expression())
                            .setName(requestPermission.name())
                            .setIntro(requestPermission.intro()));
        }
        return res;
    }


    /**
     * 获取可用的权限codes（新增、修改权限）
     *
     * @return code列表
     */
    @Override
    public List<ValidPermissionVO> getPermissionValidList() {
        // 1、sql
        Map<String, ValidPermissionVO> map = getPermissionCodeSet();
        List<Permission> list = permissionMapper.selectList(new LambdaQueryWrapper<Permission>().select(Permission::getCode));
        // 2、过滤未使用（未添加）的权限
        Set<String> set = list.stream().map(Permission::getCode).collect(Collectors.toSet());
        List<ValidPermissionVO> newList = new ArrayList<>();
        map.forEach((k, v) -> {
            if (!set.contains(k)) {
                newList.add(v);
            }
        });
        return newList;
    }


    /**
     * 删除权限
     *
     * @param id 权限id
     * @return 删除行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer delPermissionPage(String id) {
        // 1、删除被使用的状态
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermissionId, id));

        // 2、删除权限
        if (permissionMapper.deleteById(id) != 1) {
            throw new BusinessException(ResultStatus.DELETE_ERR.getCode(), "删除失败，该权限不存在！");
        }
        // 删除缓存
        permissionConstant.delPermissionMap();
        return 1;
    }


    /**
     * 删除权限（批量）
     *
     * @param ids ids未使用
     * @return 删除条数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchDelPermissionPage(List<String> ids) {
        // 1、sql查询是否被依赖
//        List<RolePermission> list = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
//                .in(RolePermission::getPermissionId, ids));
//        if (list != null && !list.isEmpty()) {
//            throw new BusinessException(ResultStatus.LINK_NULL_ERR.getCode(), "删除失败，部分权限正被使用！");
//        }
        // 1、删除关联数据
        ids = ids.stream().distinct().collect(Collectors.toList());
        int count = rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getPermissionId, ids));
        // 2、删除本体
        if (permissionMapper.deleteBatchIds(ids) != ids.size()) {
            throw new BusinessException(ResultStatus.DELETE_ERR.getCode(), "删除失败，该权限不存在！");
        }

        permissionConstant.delPermissionMap();
        if (count > 0) {
            return ids.size();
        }
        return ids.size();
    }

    /**
     * 获取已有权限列表
     *
     * @param dto 查询参数
     * @return 列表
     */
    @Override
    public List<Permission> getPermissionExistList(SelectPermissionDTO dto) {
        LambdaQueryWrapper<Permission> qw = new LambdaQueryWrapper<>();
        // 1、查询参数
        if (!StringUtil.isNullOrEmpty(dto.getName())) {
            qw.or().like(Permission::getName, dto.getName());
            qw.or().like(Permission::getIntro, dto.getName());
        }
        if (!StringUtil.isNullOrEmpty(dto.getCode())) {
            qw.or().like(Permission::getCode, dto.getCode());
        }
        if (!StringUtil.isNullOrEmpty(dto.getParentId())) {
            qw.or().eq(Permission::getId, dto.getParentId());
        }
        if (!StringUtil.isNullOrEmpty(dto.getCreator())) {
            qw.or().eq(Permission::getCreator, dto.getCreator());
        }
        // 单次结果
        qw.last("LIMIT 200");
        // 2、res 筛选
        return permissionMapper.selectList(qw);
    }

}

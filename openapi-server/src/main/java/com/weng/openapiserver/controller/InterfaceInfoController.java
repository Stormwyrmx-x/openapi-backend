package com.weng.openapiserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weng.openapiserver.common.InterfaceInfoEnum;
import com.weng.openapiserver.common.Result;
import com.weng.openapiserver.common.ResultCodeEnum;
import com.weng.openapiserver.exception.BusinessException;
import com.weng.openapiserver.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.weng.openapiserver.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.weng.openapiserver.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.weng.openapiserver.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.weng.openapiserver.model.dto.page.PageRequest;
import com.weng.openapiserver.model.entity.InterfaceInfo;
import com.weng.openapiserver.model.entity.User;
import com.weng.openapiserver.service.InterfaceInfoService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
@Validated
//在默认配置下，Spring MVC并不会自动验证简单类型的方法参数（如`String`、`int`等）
//若要让这些约束注解生效，需要在控制器方法参数前使用`@Valid`或`@Validated`注解来触发验证
@RequiredArgsConstructor
public class InterfaceInfoController
{
    private final InterfaceInfoService interfaceInfoService;

    // region 增删改查

    /**
     * 增加
     *
     * @param interfaceInfoAddRequest
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")/*or #interfaceInfo.userId == authentication.principal.id*/
    public Result<Long> addInterfaceInfo(@RequestBody @Validated InterfaceInfoAddRequest interfaceInfoAddRequest,
                                         @AuthenticationPrincipal User user){
        //@RequestBody默认不接受空的数据，也就是什么都不传=null。
        //如果开启了required = false，那么可以接受null。这个时候如果不传请求体，那么interfaceInfoAddParam为null
        //@Validated就不起作用。只有当不为null时，才会起作用
        //如果传了空json，@RequestBody就会默认会拆成interfaceInfoAddParam(name=null,age=null...)这个时候@Validated就起作用了
        /**
         * controller层倾向于对请求参数本身的校验(所有都要)，不涉及业务逻辑本身(越少越好)
         * service层是对业务逻辑的校验（有可能被controller 之外的类调用)
         */
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        //新增
        interfaceInfo.setCreateUser(user.getId());
        interfaceInfoService.save(interfaceInfo);//MyBatis-Plus 会直接抛出异常，返回的false没什么用。
        return Result.success(interfaceInfo.getId());
    }

    /**
     * 删除
     *
     * @return
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    //因为是路径参数，所以id一定会有值，不可能为null，所以这里校验是否大于0即可
    public Result<Boolean> deleteInterfaceInfo(@Min(value = 1,message = "id必须大于0") @PathVariable Long id) {
        //判断是否存在
        interfaceInfoService.isExist(id);
        boolean flag = interfaceInfoService.removeById(id);
        return Result.success(flag);
    }

    /**
     * 修改
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Boolean> updateInterfaceInfo(@Validated @RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        //判断是否存在
        interfaceInfoService.isExist(interfaceInfoUpdateRequest.id());
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        //参数校验(这里已经被@Validated注解校验过了)
//        interfaceInfoService.validInterfaceInfo(interfaceInfo);
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        interfaceInfo.setUpdateTime(LocalDateTime.now());
        boolean flag = interfaceInfoService.updateById(interfaceInfo);
        return Result.success(flag);
    }


    /**
     * 根据 id 查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<InterfaceInfo> getInterfaceInfoById(@Min(value = 1,message = "id必须大于0")
                                                          @PathVariable Long id) {
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return Result.success(interfaceInfo);
    }

    /**
     * 获取列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        LambdaQueryWrapper<InterfaceInfo>lambdaQueryWrapper=new LambdaQueryWrapper<>();

        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(lambdaQueryWrapper);
        return Result.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     *
     * @param pageRequest
     * @return
     */
    @GetMapping(value = "/page")//pageRequest不可能为null，只可能为pageRequest(size=null,current=null)
    public Result<Page<InterfaceInfo>> listInterfaceInfoByPage(@Validated PageRequest pageRequest) {
        //请求参数进来先拼成一个对象，如果不传那么默认会拼成pageRequest(size=null,current=null)
        //这个时候@Validated注解就会校验size和current。所以这里@NotNull注解就不起作用了，因为pageRequest不为null
        LambdaQueryWrapper<InterfaceInfo>interfaceInfoLambdaQueryWrapper=new LambdaQueryWrapper<>();
        interfaceInfoLambdaQueryWrapper.orderByAsc(InterfaceInfo::getCreateTime);
        Page<InterfaceInfo>interfaceInfoPage=new Page<>(pageRequest.getCurrent(),pageRequest.getSize());
        interfaceInfoService.page(interfaceInfoPage,interfaceInfoLambdaQueryWrapper);
        return Result.success(interfaceInfoPage);
    }

    // endregion

    /**
     * 发布接口
     *
     * @param
     * @return
     */
    @PutMapping("/online/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Boolean> onlineInterfaceInfo(@Min(value = 1,message = "id必须大于0") @PathVariable Long id) throws IOException
    {
        //判断是否存在
        InterfaceInfo info = interfaceInfoService.isExist(id);
        //调用接口，这里使用接口默认的requestParam
        String result = interfaceInfoService.invokeInterfaceInfo(info.getMethod(), info.getUrl(), info.getRequestParam());
        if (!StringUtils.hasText(result))
        {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR,"接口验证失败");
        }
        //修改接口状态
        InterfaceInfo interfaceInfo = InterfaceInfo.builder()
                .id(id)
                .status(InterfaceInfoEnum.ONLINE.getCode())
                .build();
        boolean flag = interfaceInfoService.updateById(interfaceInfo);
        return Result.success(flag);
    }

    /**
     * 下线接口
     *
     * @param
     * @return
     */
    @PutMapping("/offline/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Boolean> offlineInterfaceInfo(@Min(value = 1,message = "id必须大于0") @PathVariable Long id) {
        //判断是否存在
        interfaceInfoService.isExist(id);
        //修改接口状态
        InterfaceInfo interfaceInfo = InterfaceInfo.builder()
                .id(id)
                .status(InterfaceInfoEnum.OFFLINE.getCode())
                .build();
        boolean flag = interfaceInfoService.updateById(interfaceInfo);
        return Result.success(flag);
    }

    @PostMapping("/invoke")
    public Result<String> invokeInterfaceInfo(@Validated @RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,@AuthenticationPrincipal User user) throws IOException
    {
        //判断是否存在
        InterfaceInfo info = interfaceInfoService.isExist(interfaceInfoInvokeRequest.id());
        //判断接口状态
        if(!Objects.equals(info.getStatus(), InterfaceInfoEnum.ONLINE.getCode()))
        {
            throw new BusinessException(ResultCodeEnum.OPERATION_ERROR,"接口已关闭");
        }
        //InterfaceInfoInvokeRequest仅包含了id和requestParam。调用时仅允许用户改变requestParam
        //调用接口
        String result = interfaceInfoService.invokeInterfaceInfo(info.getMethod(), info.getUrl(), interfaceInfoInvokeRequest.requestParam());
        if (!StringUtils.hasText(result))
        {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR,"接口验证失败");
        }
        //用户调用接口成功后，将调用接口的用户的接口调用次数+1（不是接口创建者的调用次数+1）
        Integer count = interfaceInfoService.addInvokeCount(info.getId(), user.getId());//这里是调用接口的用户id，不是接口创建人id
        if (count<=0)
        {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR,"接口调用次数更新失败");
        }
        return Result.success(result);
    }


}

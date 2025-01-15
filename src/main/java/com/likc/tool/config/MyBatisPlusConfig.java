package com.likc.tool.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.likc.tool.common.CurrentUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * MyBatisPlus配置
 * 插件：乐观锁，分页
 * 填充策略：修改原有的填充策略（有值不填充）为无论怎么样都填充
 */
@Configuration
public class MyBatisPlusConfig implements MetaObjectHandler {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 关闭被left join表不参与where条件时 计算total会被省略的问题
        paginationInnerInterceptor.setOptimizeJoin(false);

        /**
         * 分页插件
         */
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        /**
         * 乐观锁
         */
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        Instant instant = Instant.now();
        LocalDateTime localDateTime = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createBy", Long.class, CurrentUser.get().getId());
        this.strictInsertFill(metaObject, "updateBy", Long.class, CurrentUser.get().getId());
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, localDateTime);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, localDateTime);
        this.strictInsertFill(metaObject, "createTimestamp", Long.class, instant.toEpochMilli());
        this.strictInsertFill(metaObject, "updateTimestamp", Long.class, instant.toEpochMilli());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updateBy", Long.class, CurrentUser.get().getId());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTimestamp", Long.class, Instant.now().toEpochMilli());
    }

    /**
     * 实际填充代码
     * @param metaObject
     * @param fieldName
     * @param fieldVal
     * @return
     */
    @Override
    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
//        原有逻辑字段value是空才填充，注释掉使得value有值也会被强制填充
//        if (metaObject.getValue(fieldName) == null) {
        Object obj = fieldVal.get();
        if (Objects.nonNull(obj)) {
            metaObject.setValue(fieldName, obj);
        }
//        }
        return this;
    }

}

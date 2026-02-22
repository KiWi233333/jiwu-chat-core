package com.jiwu.api.chat.common.event.producer;

import com.jiwu.api.common.annotation.SecureInvokeConfigurer;
import com.jiwu.api.common.main.mapper.sys.SysSecureInvokeRecordMapper;
import com.jiwu.api.common.main.secure_invoke.service.SysSecureInvokeService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Description:
 */
@Configuration
@EnableScheduling
public class TransactionAutoConfig {

    @Bean
    public SysSecureInvokeService getSecureInvokeService(SysSecureInvokeRecordMapper dao) {
        return new SysSecureInvokeService(dao, executor);
    }

    @Bean
    public MqProducer getMQProducer() {
        return new MqProducer();
    }
    
    @Nullable
    protected Executor executor;

    /**
     * Collect any {@link AsyncConfigurer} beans through autowiring.
     */
    @Autowired
    void setConfigurers(ObjectProvider<SecureInvokeConfigurer> configurers) {
        Supplier<SecureInvokeConfigurer> configurer = SingletonSupplier.of(() -> {
            List<SecureInvokeConfigurer> candidates = configurers.stream().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                return null;
            }
            if (candidates.size() > 1) {
                throw new IllegalStateException("Only one SecureInvokeConfigurer may exist");
            }
            return candidates.get(0);
        });
        executor = Optional.ofNullable(configurer.get()).map(SecureInvokeConfigurer::getSecureInvokeExecutor).orElse(ForkJoinPool.commonPool());
    }

}

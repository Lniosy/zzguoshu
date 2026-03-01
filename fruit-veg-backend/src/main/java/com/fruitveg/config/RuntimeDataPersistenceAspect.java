package com.fruitveg.config;

import com.fruitveg.service.RuntimeDataService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RuntimeDataPersistenceAspect {

    private final RuntimeDataService runtimeDataService;

    public RuntimeDataPersistenceAspect(RuntimeDataService runtimeDataService) {
        this.runtimeDataService = runtimeDataService;
    }

    @AfterReturning("execution(public * com.fruitveg.service.RuntimeDataService.*(..)) && !execution(public void com.fruitveg.service.RuntimeDataService.persistStateSilently(..))")
    public void afterAnyPublicCall() {
        runtimeDataService.persistStateSilently();
    }
}

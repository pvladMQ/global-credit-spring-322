package com.tanzu.creditengine.config;

import com.tanzu.creditengine.entity.CreditScoreCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

/**
 * GemFire configuration for the CreditScoreCache region.
 */
@Configuration
@EnableGemfireRepositories(basePackages = "com.tanzu.creditengine.repository")
@EnableEntityDefinedRegions(basePackageClasses = CreditScoreCache.class, clientRegionShortcut = ClientRegionShortcut.PROXY)
@org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
public class GemFireConfig {

    @org.springframework.context.annotation.Bean("CreditScoreCache")
    public org.springframework.data.gemfire.client.ClientRegionFactoryBean<String, CreditScoreCache> creditScoreCacheRegion(
            org.apache.geode.cache.GemFireCache gemfireCache) {

        org.springframework.data.gemfire.client.ClientRegionFactoryBean<String, CreditScoreCache> clientRegion = new org.springframework.data.gemfire.client.ClientRegionFactoryBean<>();

        clientRegion.setCache(gemfireCache);
        clientRegion.setClose(false); // Important: Keep region open on context close
        clientRegion.setShortcut(org.apache.geode.cache.client.ClientRegionShortcut.PROXY);

        return clientRegion;
    }
}
